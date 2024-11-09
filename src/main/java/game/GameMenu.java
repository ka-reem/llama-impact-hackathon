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
        System.out.println("Starting game"); // debugging
    }

    private void openOptions() {
        System.out.println("Opening options"); // debugging
    }

    public void display() {
        setVisible(true);
    }
}