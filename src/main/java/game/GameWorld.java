package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.net.URL;  // Add this import

public class GameWorld extends JPanel implements Runnable {

    private BufferedImage world;
    private Player t1;
    private final JFrame frame;  
    private long tick = 0;
    private NPC npc;
    private Game game;  // Add reference to main game
    private boolean isNearNPC = false;

    public GameWorld(Game game) {
        this.game = game;
        this.frame = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.tick++;
                this.t1.update(); // update tank
                
                // Check NPC interaction
                boolean currentlyNearNPC = npc.isPlayerInRange(t1);
                if (currentlyNearNPC != isNearNPC) {
                    isNearNPC = currentlyNearNPC;
                    if (isNearNPC) {
                        game.showNPCChat("Hello traveler! Would you like to chat?");
                    }
                }
                
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
            System.out.println("Attempting to load tank1.png...");
            URL resourceUrl = GameWorld.class.getClassLoader().getResource("tank1.png");
            System.out.println("Resource URL: " + resourceUrl);
            BufferedImage originalImg = ImageIO.read(Objects.requireNonNull(resourceUrl));
            
            // Sprite sizing
            t1img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = t1img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImg, 0, 0, 20, 20, null);
            g2d.dispose();
            
        } catch (Exception ex) {
            System.out.println("Error loading tank1.png: " + ex.getMessage());
            ex.printStackTrace();
            // Default small image if loading fails
            t1img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
        }

        // Center the tank by accounting for its size
        float startX = (GameConstants.GAME_SCREEN_WIDTH - t1img.getWidth()) / 2f;
        float startY = (GameConstants.GAME_SCREEN_HEIGHT - t1img.getHeight()) / 2f;
        t1 = new Player(startX, startY, 0, 0, (short) 0, t1img);
        
        PlayerControl tc1 = new PlayerControl(t1, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE);
        this.frame.addKeyListener(tc1);  

        // Create NPC
        try {
            BufferedImage npcImg = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = npcImg.createGraphics();
            g2d.setColor(Color.BLUE);
            g2d.fillOval(0, 0, 20, 20);
            g2d.dispose();
            
            // Position NPC somewhere in the world
            npc = new NPC(400, 400, npcImg);
        } catch (Exception ex) {
            System.out.println("Error creating NPC: " + ex.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D buffer = world.createGraphics();
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT);
        this.t1.drawImage(buffer);
        this.npc.drawImage(buffer);  // Draw NPC
        g2.drawImage(world, 0, 0, null);
    }
}
