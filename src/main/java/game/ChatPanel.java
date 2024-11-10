
package game;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class ChatPanel extends JPanel {
    private GroqClient groqClient;
    private JTextArea chatArea;
    private JTextField playerInput;
    private String gameContext;

    public ChatPanel() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
        gameContext = "You are running an interactive game that focuses on teaching the user a specific topic. make sure to not change that topic unless directed to. keep responses under 50 words and make them fun.";
        
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 400));
        
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
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
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
                String prompt = gameContext + "\nMessage: " + input + "\nRespond with the answer to the previous question or tell them if its right or wrong. Then as a teacher ask them another related question with a fun game and an answer:";
                String response = groqClient.generateResponse(prompt);
                appendToChat("\nChat Agent: " + response );
            } catch (Exception e) {
                appendToChat("\nError: " + e.getMessage());
            }
        }
    }

    private void appendToChat(String text) {
        chatArea.append(text + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
}