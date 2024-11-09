package game;

import javax.swing.*;

import game.GroqClient;

import java.awt.*;
import java.awt.event.*;

import game.GroqClient;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class Game extends JPanel {
    private GroqClient groqClient;
    private JTextArea gameArea;
    private JTextField playerInput;
    private String gameContext;

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
    
    public Game() {
        String apiKey = loadApiKey();
        groqClient = new GroqClient(apiKey);
        gameContext = "You are running an interactive game that focuses on teaching the user a specific topic. make sure to not change that topic unless directed to. keep responses under 50 words and make them fun.";
        
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(600, 400));
        
        // Game display area
        gameArea = new JTextArea();
        gameArea.setEditable(false);
        gameArea.setLineWrap(true);
        gameArea.setWrapStyleWord(true);
        gameArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(gameArea);
        
        // Player input area
        playerInput = new JTextField();
        JButton submitButton = new JButton("Submit");
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(playerInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);
        
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Action listeners
        submitButton.addActionListener(e -> processPlayerInput());
        playerInput.addActionListener(e -> processPlayerInput());
        
        // Start the game
        startNewGame();
    }
    
    private void startNewGame() {
        try {
            String startPrompt = gameContext + "\n Ask the user what topic they'd like to learn";
            String response = groqClient.generateResponse(startPrompt);
            appendToGame("Chat Agent: " + response + "\n\nWhat is the right answer?");
        } catch (Exception e) {
            appendToGame("Error starting game: " + e.getMessage());
        }
    }
    
    private void processPlayerInput() {
        String input = playerInput.getText().trim();
        if (!input.isEmpty()) {
            appendToGame("\nYou: " + input);
            playerInput.setText("");
            
            try {
                String prompt = gameContext + "\nMessage: " + input + "\nRespond with the answer to the previous question or tell them if its right or wrong. Then as a teacher ask them another related question with a fun game and an answer:";
                String response = groqClient.generateResponse(prompt);
                appendToGame("\nChat Agent: " + response );//+ "\n\nWhat is the right answer?");
            } catch (Exception e) {
                appendToGame("\nError: " + e.getMessage());
            }
        }
    }
    
    private void appendToGame(String text) {
        gameArea.append(text + "\n");
        gameArea.setCaretPosition(gameArea.getDocument().getLength());
    }
}