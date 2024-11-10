package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class ChatPanel extends JPanel {
    private GroqClient groqClient;
    private JTextArea chatArea;
    private JTextField playerInput;
    private String gameContext;
    private ArrayList<String> messages;
    private TaxData taxData;  // Add this field
    private PDFReader pdfReader;

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
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        // Player input area
        playerInput = new JTextField();
        JButton submitButton = new JButton("Submit");
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(playerInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        
        // Add PDF upload button
        JButton uploadButton = new JButton("Upload Tax Document");
        uploadButton.addActionListener(e -> uploadPDF());
        
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(uploadButton);
        
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
            String startPrompt = gameContext + "\n Ask the user what topic they'd like to learn";
            String response = groqClient.generateResponse(startPrompt);
            appendToChat("Chat Agent: " + response + "\n\nWhat is the right answer?");
        } catch (Exception e) {
            appendToChat("Error starting chat: " + e.getMessage());
        }
    }

    private void processPlayerInput() {
        String input = playerInput.getText().trim();
        if (!input.isEmpty()) {
            appendToChat("\nYou: " + input);
            playerInput.setText("");
            
            try {
                // Update prompt to handle tax-specific conversation
                String prompt = gameContext + 
                              "\nDocument content: " + (pdfReader.getContent() != null ? "Available" : "Not uploaded") +
                              "\nCurrent tax information: " + taxData.toString() + 
                              "\nUser response: " + input + 
                              "\nAnalyze their answer against the tax document, verify information, and ask the next relevant question.";
                
                String response = groqClient.generateResponse(prompt);
                appendToChat("\nTax Advisor: " + response);
                
                // Parse the response and update tax data
                updateTaxData(input, response);
                
                // Check if we have all needed tax information
                if (taxData.isComplete()) {
                    generateTaxForm();
                }
            } catch (Exception e) {
                appendToChat("\nError: " + e.getMessage());
            }
        }
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
                gameContext = "You are a tax advisor. Using this tax document: " + content + 
                            "\nAsk relevant questions to help fill out the tax form. Keep responses under 50 words.";
                appendToChat("Tax Advisor: I've reviewed your tax document. Let me help you fill out the form.");
            } catch (IOException ex) {
                appendToChat("Error reading PDF: " + ex.getMessage());
            }
        }
    }

    private void updateTaxData(String userInput, String aiResponse) {
        // Simple parsing of numerical values from user input
        if (aiResponse.toLowerCase().contains("income")) {
            try {
                double income = extractNumber(userInput);
                taxData.setIncome(income);
            } catch (NumberFormatException e) {
                appendToChat("\nTax Advisor: Could you please provide your income as a number?");
            }
        }
        // Add more parsing for other tax fields
    }

    private double extractNumber(String text) {
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
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

    private void appendToChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void addMessage(String sender, String message) {
        String formattedMessage = sender + ": " + message + "\n";
        chatArea.append(formattedMessage);
        messages.add(formattedMessage);
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}

// Add this new class in the same file or create a new file
class TaxData {
    private double income;
    private boolean isComplete;
    
    public void setIncome(double income) {
        this.income = income;
        checkCompletion();
    }
    
    public double getIncome() {
        return income;
    }
    
    private void checkCompletion() {
        isComplete = income > 0; // Add more conditions as needed
    }
    
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String toString() {
        return "Income: $" + income;
    }
}