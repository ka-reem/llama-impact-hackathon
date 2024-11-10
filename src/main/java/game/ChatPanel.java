package game;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;  // Add this import
import java.util.Properties;

public class ChatPanel extends JPanel {
    private GroqClient groqClient;
    private JTextPane chatArea;  // Changed from JTextArea to JTextPane
    private JTextField playerInput;
    private String gameContext;
    private ArrayList<String> messages;
    private TaxData taxData;  // Add this field
    private PDFReader pdfReader;
    private boolean isProcessing = false;
    private Color userBubbleColor = new Color(220, 248, 198);
    private Color botBubbleColor = new Color(230, 230, 230);

    public ChatPanel() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
        taxData = new TaxData();
        pdfReader = new PDFReader();
        gameContext = "You are a friendly tax advisor helping someone file their taxes. Ask one question at a time about their tax information. Keep responses under 50 words. Start by asking for their annual income.";
        
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 400));
        messages = new ArrayList<>();
        
        // Chat display area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        // Player input area
        playerInput = new JTextField();
        playerInput.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton submitButton = new JButton("Submit");  // Changed from "Send" back to "Submit"
        submitButton.setBackground(new Color(0, 132, 255));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(playerInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        
        // Add PDF upload button
        JButton uploadButton = new JButton("Upload Tax Document");
        uploadButton.addActionListener(e -> uploadPDF());
        
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(uploadButton);
        
        // Add finish button
        JButton finishButton = new JButton("Finish & Generate PDF");
        finishButton.setBackground(new Color(255, 69, 0));
        finishButton.setForeground(Color.WHITE);
        finishButton.addActionListener(e -> finishConversation());
        
        topPanel.add(finishButton);  // Add to existing topPanel
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
        
        submitButton.addActionListener(e -> processPlayerInput());
        playerInput.addActionListener(e -> processPlayerInput());
        
        startChat();
    }

    private String loadApiKey() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Unable to find config.properties");
                return ""; 
            }
            props.load(input);
            return props.getProperty("groq.api.key");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void startChat() {
        try {
            String startPrompt = "You are helping fill out a 1040 tax form. Start by asking for the taxpayer's full name. " +
                               "Ask one question at a time. Keep responses friendly and clear.";
            String response = groqClient.generateResponse(startPrompt);
            appendToChat("Tax Advisor: " + response);
        } catch (Exception e) {
            appendToChat("Error starting chat: " + e.getMessage());
        }
    }

    private void processPlayerInput() {
        String input = playerInput.getText().trim();
        if (!input.isEmpty() && !isProcessing) {
            isProcessing = true;
            playerInput.setEnabled(false);
            
            appendMessage("You", input, userBubbleColor);
            playerInput.setText("");
            
            // Show typing indicator
            JLabel typingLabel = addTypingIndicator();
            
            // Process in background
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    String prompt = buildPrompt(input);
                    return groqClient.generateResponse(prompt);
                }
                
                @Override
                protected void done() {
                    try {
                        String response = get();
                        removeTypingIndicator(typingLabel);
                        appendMessage("Tax Advisor", response, botBubbleColor);
                        updateTaxData(input, response);
                        
                        if (taxData.isComplete()) {
                            generateTaxForm();
                        }
                    } catch (Exception e) {
                        appendToChat("\nError: " + e.getMessage());
                    }
                    isProcessing = false;
                    playerInput.setEnabled(true);
                    playerInput.requestFocus();
                }
            };
            worker.execute();
        }
    }

    private String buildPrompt(String userInput) {
        String nextField = taxData.getNextRequiredField();
        String fieldType = getFieldType(nextField);
        
        return String.format(
            "You are helping fill out a 1040 tax form. Context:\n" +
            "Current information: %s\n" +
            "User's response: %s\n\n" +
            "Instructions:\n" +
            "1. We need to collect: %s\n" +
            "2. Acknowledge their answer if given\n" +
            "3. Ask about %s if not provided yet\n" +
            "4. Keep responses under 50 words\n" +
            "5. IMPORTANT: Include '%s:' in your question",
            taxData.toString(),
            userInput,
            fieldType,
            fieldType,
            fieldType
        );
    }

    private String getFieldType(String fieldName) {
        if (fieldName == null) return "all information collected";
        if (fieldName.contains("f1_02")) return "name";
        if (fieldName.contains("f1_04")) return "ssn";
        if (fieldName.contains("f1_08")) return "address";
        if (fieldName.contains("c1_1")) return "filing status";
        if (fieldName.contains("f1_12")) return "income";
        return "next field";
    }

    private void appendMessage(String sender, String message, Color bubbleColor) {
        StyledDocument doc = new DefaultStyledDocument();
        try {
            // Create message bubble style
            SimpleAttributeSet bubbleStyle = new SimpleAttributeSet();
            StyleConstants.setBackground(bubbleStyle, bubbleColor);
            StyleConstants.setFontFamily(bubbleStyle, "Arial");
            StyleConstants.setFontSize(bubbleStyle, 14);
            StyleConstants.setLeftIndent(bubbleStyle, 10);
            StyleConstants.setRightIndent(bubbleStyle, 10);
            StyleConstants.setSpaceAbove(bubbleStyle, 5);
            StyleConstants.setSpaceBelow(bubbleStyle, 5);
            
            // Add sender name
            doc.insertString(doc.getLength(), sender + ":\n", bubbleStyle);
            
            // Add message
            doc.insertString(doc.getLength(), message + "\n\n", bubbleStyle);
            
            chatArea.setStyledDocument(doc);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private JLabel addTypingIndicator() {
        JLabel label = new JLabel("Tax Advisor is typing...");
        label.setForeground(Color.GRAY);
        label.setFont(new Font("Arial", Font.ITALIC, 12));
        add(label, BorderLayout.NORTH);
        revalidate();
        return label;
    }

    private void removeTypingIndicator(JLabel label) {
        remove(label);
        revalidate();
        repaint();
    }

    public void uploadPDF() {  // Change from private to public
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
            }
            public String getDescription() {
                return "PDF Files (*.pdf)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = pdfReader.readPDF(fileChooser.getSelectedFile());
                // Print available fields for debugging
                pdfReader.printAllFields();
                gameContext = "You are a tax advisor. Using this tax document: " + content + 
                            "\nAsk relevant questions to help fill out the form. Keep responses under 50 words.";
                appendToChat("Tax Advisor: I've reviewed your tax document. Let me help you fill out the form.");
            } catch (IOException ex) {
                appendToChat("Error reading PDF: " + ex.getMessage());
            }
        }
    }

    private void updateTaxData(String userInput, String aiResponse) {
        // Check for conversation ending phrases
        if (userInput.toLowerCase().contains("done") || 
            userInput.toLowerCase().contains("finish") || 
            userInput.toLowerCase().contains("exit")) {
            finishConversation();
            return;
        }

        String nextField = taxData.getNextRequiredField();
        if (nextField != null) {
            // Clean the user input before storing
            String cleanInput = userInput.trim();
            // Special handling for SSN format
            if (nextField.contains("f1_04")) {
                cleanInput = cleanInput.replaceAll("[^0-9]", "");
            }
            taxData.addAnswer(nextField, cleanInput);
            System.out.println("Added answer for field: " + nextField + " = " + cleanInput);
        }

        if (taxData.getNextRequiredField() == null) {
            finishConversation();
        }
    }

    private String extractFieldName(String aiResponse) {
        String response = aiResponse.toLowerCase();
        // Map common questions to actual form fields
        if (response.contains("name:") || response.contains("full name")) 
            return "topmostSubform[0].Page1[0].f1_02[0]";
        if (response.contains("ssn:") || response.contains("social security")) 
            return "topmostSubform[0].Page1[0].f1_04[0]";
        if (response.contains("address:") || response.contains("live at")) 
            return "topmostSubform[0].Page1[0].f1_08[0]";
        if (response.contains("filing status") || response.contains("marital status")) 
            return "topmostSubform[0].Page1[0].c1_1[0]";
        if (response.contains("income:") || response.contains("earn") || response.contains("wages")) 
            return "topmostSubform[0].Page1[0].Line1[0].f1_12[0]";
        return null;
    }

    private double extractNumber(String text) {
        try {
            // First try to find numbers with dollar signs
            if (text.contains("$")) {
                text = text.substring(text.indexOf("$") + 1);
            }
            
            // Remove any commas from numbers like "50,000"
            text = text.replaceAll(",", "");
            
            // Find any number in the text (including decimals)
            String[] words = text.split("\\s+");
            for (String word : words) {
                try {
                    return Double.parseDouble(word.replaceAll("[^0-9.]", ""));
                } catch (NumberFormatException ignored) {
                    // Continue to next word if this one isn't a number
                }
            }
            throw new NumberFormatException("No valid number found");
            
        } catch (NumberFormatException e) {
            return 0.0; // Return 0 instead of showing error message
        }
    }

    private void generateTaxForm() {
        String taxForm = "=== Tax Form Summary ===\n" +
                        "Annual Income: $" + taxData.getIncome() + "\n" +
                        "Estimated Tax: $" + (taxData.getIncome() * 0.2) + "\n" +
                        "==================";
        appendToChat("\n" + taxForm);
        
        // Add option to save to file
        JButton saveButton = new JButton("Save Tax Form");
        saveButton.addActionListener(e -> saveTaxForm(taxForm));
        add(saveButton, BorderLayout.NORTH);
        revalidate();
    }

    private void saveTaxForm(String taxForm) {
        try (PrintWriter out = new PrintWriter("tax_form.txt")) {
            out.println(taxForm);
            appendToChat("\nTax form saved to tax_form.txt");
        } catch (IOException e) {
            appendToChat("\nError saving tax form: " + e.getMessage());
        }
    }

    private void finishConversation() {
        try {
            Map<String, String> finalAnswers = taxData.getAllAnswers();
            if (finalAnswers.isEmpty()) {
                appendMessage("Tax Advisor", "I don't have any information to save yet. Would you like to answer some questions first?", botBubbleColor);
                return;
            }

            // Add any missing required fields with default values
            if (!finalAnswers.containsKey("topmostSubform[0].Page1[0].f1_02[0]")) {
                finalAnswers.put("topmostSubform[0].Page1[0].f1_02[0]", "Not Provided");
            }

            pdfReader.fillAndSaveForm(finalAnswers);
            appendMessage("Tax Advisor", 
                "I've saved your information to the PDF. Here's what I recorded:\n" + 
                taxData.getFormattedSummary(), 
                botBubbleColor
            );
            
            // Open the most recently created PDF
            File directory = new File(".");
            File[] files = directory.listFiles((dir, name) -> name.startsWith("completed_1040_"));
            if (files != null && files.length > 0) {
                File latest = files[files.length - 1];
                Desktop.getDesktop().open(latest);
            }
        } catch (IOException e) {
            appendToChat("Error saving PDF: " + e.getMessage());
        }
    }

    private void appendToChat(String text) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), text + "\n", null);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(String sender, String message) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            doc.insertString(doc.getLength(), sender + ": " + message + "\n", null);
            messages.add(sender + ": " + message);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}