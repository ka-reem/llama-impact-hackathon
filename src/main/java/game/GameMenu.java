package game;

import javax.swing.*;
import java.awt.*;

public class GameMenu extends JFrame {
    private JButton startButton;
    private JButton optionsButton;
    private JButton exitButton;

    public GameMenu() {
        setTitle("Game Name");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new GridLayout(3, 1, 10, 10));

        startButton = new JButton("Start Game");
        optionsButton = new JButton("Options");
        exitButton = new JButton("Exit");

        add(startButton);
        add(optionsButton);
        add(exitButton);

        startButton.addActionListener(e -> startGame());
        optionsButton.addActionListener(e -> openOptions());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void startGame() {
        // System.out.println("Starting game"); // debugging
        JFrame gameFrame = new JFrame("Game Title");
        Game game = new Game(); // when we make a game.java
        // initalized in Game.java now
        // JPanel gamePanel = new JPanel();
        // gamePanel.setPreferredSize(new Dimension(400,400));
        // gamePanel.setBackground(Color.GRAY);

        gameFrame.add(game);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null); // center window
        gameFrame.setVisible(true);

        this.dispose(); // removes menu
    }

    private void openOptions() {
        System.out.println("Opening options"); // debugging
    }

    public void display() {
        setVisible(true);
    }
}