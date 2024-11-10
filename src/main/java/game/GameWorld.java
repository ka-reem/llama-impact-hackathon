package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class GameWorld extends JPanel implements Runnable {

    private BufferedImage world;
    private Player t1;
    private final JFrame frame;  // Changed from Launcher to JFrame
    private long tick = 0;

    public GameWorld(JFrame frame) {  // Changed constructor parameter
        this.frame = frame;
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.tick++;
                this.t1.update(); // update tank
                this.repaint();   // redraw game
                /*
                 * Sleep for 1000/144 ms (~6.9ms). This is done to have our 
                 * loop run at a fixed rate per/sec. 
                */
                Thread.sleep(1000 / 144);
            }
        } catch (InterruptedException ignored) {
            System.out.println(ignored);
        }
    }

    public void resetGame() {
        this.tick = 0;
        this.t1.setX(300);
        this.t1.setY(300);
    }

    public void InitializeGame() {
        this.world = new BufferedImage(GameConstants.GAME_SCREEN_WIDTH,
                GameConstants.GAME_SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_RGB);

        BufferedImage t1img = null;
        try {
            t1img = ImageIO.read(
                    Objects.requireNonNull(GameWorld.class.getClassLoader().getResource("tank1.png"))
            );
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        t1 = new Player(300, 300, 0, 0, (short) 0, t1img);
        PlayerControl tc1 = new PlayerControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        this.frame.addKeyListener(tc1);  // Changed from lf.getJf() to frame
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D buffer = world.createGraphics();
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT);
        this.t1.drawImage(buffer);
        g2.drawImage(world, 0, 0, null);
    }
}
