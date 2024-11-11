package game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.net.URL;  // Add this import
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;

public class GameWorld extends JPanel implements Runnable {

    private BufferedImage world;
    private BufferedImage mapBackground;  // Add this field
    private Player t1;
    private final JFrame frame;  
    private long tick = 0;
    private NPC npc1;  // rename from npc to npc1
    private NPC npc2;  // add second NPC
    private Game game;  // Add reference to main game
    private boolean isNearNPC1 = false;  // Add separate flags for each NPC
    private boolean isNearNPC2 = false;

    // Add viewport tracking
    private int viewportX = 0;
    private int viewportY = 0;
    private final int SCROLL_SPEED = 5;
    private Rectangle viewport;
    private static final float ZOOM_LEVEL = 1.0f;  // Changed from 0.75f to 1.0f
    private static final int MAP_SCALE = 2;  // Add this constant

    public GameWorld(Game game) {
        this.game = game;
        this.frame = game;
        viewport = new Rectangle(0, 0, GameConstants.GAME_SCREEN_WIDTH, GameConstants.GAME_SCREEN_HEIGHT);
        
        // Add key bindings for scrolling
        addKeyBindings();
        
        // Make panel focusable
        setFocusable(true);
        // Request focus initially
        requestFocusInWindow();
    }

    private void addKeyBindings() {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("LEFT"), "scrollLeft");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("RIGHT"), "scrollRight");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("UP"), "scrollUp");
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("DOWN"), "scrollDown");

        this.getActionMap().put("scrollLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(-SCROLL_SPEED, 0);
            }
        });
        this.getActionMap().put("scrollRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(SCROLL_SPEED, 0);
            }
        });
        this.getActionMap().put("scrollUp", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(0, -SCROLL_SPEED);
            }
        });
        this.getActionMap().put("scrollDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scrollViewport(0, SCROLL_SPEED);
            }
        });
    }

    private void scrollViewport(int dx, int dy) {
        if (mapBackground != null) {
            viewportX = Math.max(0, Math.min(viewportX + dx, mapBackground.getWidth() - viewport.width));
            viewportY = Math.max(0, Math.min(viewportY + dy, mapBackground.getHeight() - viewport.height));
            repaint();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                this.tick++;
                this.t1.update(); // update tank
                
                // Check both NPCs interaction
                boolean nearNPC1 = npc1.isPlayerInRange(t1);
                boolean nearNPC2 = npc2.isPlayerInRange(t1);
                
                // Handle NPC1 interaction
                if (nearNPC1 != isNearNPC1) {
                    isNearNPC1 = nearNPC1;
                    if (isNearNPC1) {
                        game.showNPCChat("Tax Advisor 1: Hello! I can help you file your personal income taxes.");
                    }
                }
                
                // Handle NPC2 interaction
                if (nearNPC2 != isNearNPC2) {
                    isNearNPC2 = nearNPC2;
                    if (isNearNPC2) {
                        game.showNPCChat("Tax Advisor 2: Greetings! Need help with business tax deductions?");
                    }
                }
                
                // Update viewport to follow player
                updateViewportPosition();
                
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

        // Debug map loading
        try {
            System.out.println("Looking for map.png in resources...");
            URL mapUrl = getClass().getClassLoader().getResource("map.png");
            System.out.println("Map URL: " + mapUrl);
            
            if (mapUrl != null) {
                BufferedImage originalMap = ImageIO.read(mapUrl);
                
                // Scale map to fit screen exactly
                int scaledWidth = GameConstants.GAME_SCREEN_WIDTH * MAP_SCALE;
                int scaledHeight = GameConstants.GAME_SCREEN_HEIGHT * MAP_SCALE;
                
                mapBackground = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = mapBackground.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(originalMap, 0, 0, scaledWidth, scaledHeight, null);
                g2d.dispose();
                
                System.out.println("Map scaled to: " + scaledWidth + "x" + scaledHeight);
            } else {
                System.err.println("ERROR: map.png not found in resources!");
                // Create a default background with grid
                mapBackground = createDefaultBackground();
            }
        } catch (IOException e) {
            System.err.println("Error loading map.png: " + e.getMessage());
            e.printStackTrace();
            mapBackground = createDefaultBackground();
        }

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
        this.addKeyListener(tc1);  // Add key listener to panel as well

        // Create NPCs with more spacing
        try {
            // Create first NPC (blue) - Tax Guide near the hut
            BufferedImage npc1Img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = npc1Img.createGraphics();
            g2d.setColor(Color.BLUE);
            g2d.fillOval(0, 0, 20, 20);
            g2d.dispose();
            
            // Position NPC1 near the hut
            npc1 = new NPC(GameConstants.GAME_SCREEN_WIDTH / 4, GameConstants.GAME_SCREEN_HEIGHT / 3, npc1Img);
            
            // Create second NPC (green) - Tax Explorer near the water
            BufferedImage npc2Img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            g2d = npc2Img.createGraphics();
            g2d.setColor(Color.GREEN);
            g2d.fillOval(0, 0, 20, 20);
            g2d.dispose();
            
            // Position NPC2 near the water
            npc2 = new NPC(GameConstants.GAME_SCREEN_WIDTH * 3 / 4, GameConstants.GAME_SCREEN_HEIGHT * 2 / 3, npc2Img);
        } catch (Exception ex) {
            System.out.println("Error creating NPCs: " + ex.getMessage());
        }
    }

    private BufferedImage createDefaultBackground() {
        BufferedImage defaultBg = new BufferedImage(
            GameConstants.GAME_SCREEN_WIDTH,
            GameConstants.GAME_SCREEN_HEIGHT,
            BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2d = defaultBg.createGraphics();
        // Draw a grid pattern
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, defaultBg.getWidth(), defaultBg.getHeight());
        g2d.setColor(Color.GRAY);
        for (int x = 0; x < defaultBg.getWidth(); x += 50) {
            g2d.drawLine(x, 0, x, defaultBg.getHeight());
        }
        for (int y = 0; y < defaultBg.getHeight(); y += 50) {
            g2d.drawLine(0, y, defaultBg.getWidth(), y);
        }
        g2d.dispose();
        return defaultBg;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        Graphics2D buffer = world.createGraphics();

        // Enable better rendering
        buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        buffer.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Clear the background first
        buffer.setColor(Color.BLACK);
        buffer.fillRect(0, 0, world.getWidth(), world.getHeight());
        
        // Draw visible portion of map
        if (mapBackground != null) {
            buffer.drawImage(mapBackground, 
                           0, 0,                                    // Destination coordinates
                           GameConstants.GAME_SCREEN_WIDTH,         // Destination width
                           GameConstants.GAME_SCREEN_HEIGHT,        // Destination height
                           viewportX, viewportY,                    // Source coordinates
                           viewportX + GameConstants.GAME_SCREEN_WIDTH,  // Source width
                           viewportY + GameConstants.GAME_SCREEN_HEIGHT, // Source height
                           null);
        }

        // Adjust game object positions relative to viewport
        AffineTransform old = buffer.getTransform();
        buffer.translate(-viewportX, -viewportY);
        
        // Draw game objects
        this.t1.drawImage(buffer);
        this.npc1.drawImage(buffer);
        this.npc2.drawImage(buffer);
        
        buffer.setTransform(old);
        buffer.dispose();
        g2.drawImage(world, 0, 0, this);
    }

    // Update player movement to account for scrolling
    public void updatePlayerPosition(Player player) {
        // Keep player within map bounds
        float newX = Math.max(viewportX, Math.min(player.getX(), viewportX + viewport.width - player.getWidth()));
        float newY = Math.max(viewportY, Math.min(player.getY(), viewportY + viewport.height - player.getHeight()));
        player.setPosition(newX, newY);
        
        // Scroll viewport if player is near edges
        if (player.getX() < viewportX + 100) scrollViewport(-SCROLL_SPEED, 0);
        if (player.getX() > viewportX + viewport.width - 100) scrollViewport(SCROLL_SPEED, 0);
        if (player.getY() < viewportY + 100) scrollViewport(0, -SCROLL_SPEED);
        if (player.getY() > viewportY + viewport.height - 100) scrollViewport(0, SCROLL_SPEED);
    }

    private void updateViewportPosition() {
        // Center viewport on player with bounds checking
        int targetX = (int)t1.getX() - (GameConstants.GAME_SCREEN_WIDTH / 2);
        int targetY = (int)t1.getY() - (GameConstants.GAME_SCREEN_HEIGHT / 2);
        
        // Smooth scrolling with bounds checking
        viewportX = (int)Math.max(0, Math.min(targetX, mapBackground.getWidth() - GameConstants.GAME_SCREEN_WIDTH));
        viewportY = (int)Math.max(0, Math.min(targetY, mapBackground.getHeight() - GameConstants.GAME_SCREEN_HEIGHT));
    }

    public void resetPlayerMovement() {
        if (t1 != null) {
            t1.resetMovement();
        }
    }
}
