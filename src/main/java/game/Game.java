package game;

import javax.swing.*;

import game.GroqClient;

import java.awt.*;
import java.awt.event.*;

import game.GroqClient;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class Game extends JFrame {
    private GameWorld gameWorld;
    private ChatPanel chatPanel;
    private boolean isChatVisible = false;

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
        // Set window properties
        this.setTitle("Tank Game");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Added keyboard input
        this.setFocusable(true);
        this.requestFocus();
        
        // Create and setup GameWorld
        gameWorld = new GameWorld(this);
        gameWorld.setPreferredSize(new Dimension(GameConstants.GAME_SCREEN_WIDTH, 
                                               GameConstants.GAME_SCREEN_HEIGHT));
        gameWorld.InitializeGame();
        
        // Create toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Menu");
        JButton toggleChatButton = new JButton("Toggle Chat");
        JButton uploadPDFButton = new JButton("Upload Tax PDF");  // Add new button
        
        backButton.addActionListener(e -> returnToMenu());
        toggleChatButton.addActionListener(e -> toggleChat());
        uploadPDFButton.addActionListener(e -> chatPanel.uploadPDF());  // Add action
        
        toolbar.add(backButton);
        toolbar.add(toggleChatButton);
        toolbar.add(uploadPDFButton);  // Add to toolbar
        
        // Layout setup
        this.setLayout(new BorderLayout());
        this.add(toolbar, BorderLayout.NORTH);
        this.add(gameWorld, BorderLayout.CENTER);
        
        // Create and add chat panel
        chatPanel = new ChatPanel();
        chatPanel.setVisible(false);
        chatPanel.setPreferredSize(new Dimension(200, GameConstants.GAME_SCREEN_HEIGHT));
        this.add(chatPanel, BorderLayout.EAST);
        
        // Pack and center the window
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        
        // Start the game thread
        Thread thread = new Thread(gameWorld);
        thread.start();
    }

    private void toggleChat() {
        isChatVisible = !isChatVisible;
        chatPanel.setVisible(isChatVisible);
        revalidate();
        repaint();
    }

    // Quest like interaction

    public void showNPCChat(String message) {
        if (!isChatVisible) {
            toggleChat();
        }
        chatPanel.addMessage("NPC", message);
    }

    private void returnToMenu() {
        this.dispose();  
        GameMenu menu = new GameMenu();
        menu.display();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.setVisible(true);
    }
}