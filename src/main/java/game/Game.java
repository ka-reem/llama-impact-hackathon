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
    private ChatPanel chatPanel;
    private JPanel gamePanel;
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
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // Create main game panel
        gamePanel = new JPanel();
        gamePanel.setBackground(Color.GRAY); // Temporary background
        add(gamePanel, BorderLayout.CENTER);

        // Create chat panel
        chatPanel = new ChatPanel();
        chatPanel.setVisible(false);
        add(chatPanel, BorderLayout.EAST);

        // Add toolbar with buttons
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Menu");
        JButton toggleChatButton = new JButton("Toggle Chat");
        
        backButton.addActionListener(e -> returnToMenu());
        toggleChatButton.addActionListener(e -> toggleChat());
        
        toolbar.add(backButton);
        toolbar.add(toggleChatButton);
        add(toolbar, BorderLayout.NORTH);
    }

    private void toggleChat() {
        isChatVisible = !isChatVisible;
        chatPanel.setVisible(isChatVisible);
        revalidate();
        repaint();
    }

    private void returnToMenu() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
            GameMenu menu = new GameMenu();
            menu.display();
        }
    }
}