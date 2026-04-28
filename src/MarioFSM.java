import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MarioFSM extends JPanel implements ActionListener {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int RELEASE_LEFT = 2;
    public static final int RELEASE_RIGHT = 3;
    public static final int SPACE = 4;
    public static final int RELEASE_DOWN = 5;
    public static final int DOWN = 6;

    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private BufferedImage marioJumping;

    enum State {
        IDLE, MOVING, JUMP_IDLE, JUMP_MOVING, CROUCHING
    }

    int[][] table = {
            { 1, 1, 0, 0, 2, 0, 4 },
            { 1, 1, 0, 0, 3, 1, 1 },
            { 3, 3, 2, 2, 2, 2, 2 },
            { 3, 3, 2, 2, 3, 3, 3 },
            { 4, 4, 4, 4, 4, 0, 4 }
    };

    int state = 0, input = 0;

    private BufferedImage marioSprite, marioMoving;
    private BufferedImage background;

    private State currentState = State.IDLE;
    private int marioX = 100, marioY = 268;
    private int velocityY = 0;
    private final int GROUND_Y = 268;
    private int direction = 0;
    private int lastFacingDirection = RIGHT;

    public MarioFSM(int screenWidth, int screenHeight) {
        SCREEN_WIDTH = screenWidth;
        SCREEN_HEIGHT = screenHeight;
        loadImages();
        Timer timer = new Timer(20, this);
        timer.start();
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleInput(e.getKeyCode(), false);
            }
        });
    }

    private void loadImages() {
        try {
            // Loading images from the project folder
            marioSprite = ImageIO.read(new File("../resources/mario_idle.png"));
            marioJumping = ImageIO.read(new File("../resources/mario_jumping.png"));
            marioMoving = ImageIO.read(new File("../resources/mario_moving.gif"));
            // marioSprite = ImageIO.read(new File("D:\\Projects\\Spring Boot\\Mario -
            // Application of Finite State Machine\\resources\\mario_idle.png"));
            // marioJumping = ImageIO.read(new File("D:\\Projects\\Spring Boot\\Mario -
            // Application of Finite State Machine\\resources\\mario_jumping.png"));

            // background = ImageIO.read(new File("resources/background.png"));
        } catch (IOException e) {
            System.out.println("Error: Could not find image files!");
            e.printStackTrace();
        }
    }

    public void handleInput(int keyCode, boolean pressed) {
        // switch (currentState) {
        // case IDLE:
        // if (pressed && keyCode == KeyEvent.VK_RIGHT){
        // direction = 1;
        // currentState = State.MOVING;
        // }
        // if (pressed && keyCode == KeyEvent.VK_LEFT){
        // direction = 0;
        // currentState = State.MOVING;
        // }
        // if (pressed && keyCode == KeyEvent.VK_SPACE) startJump();
        // if (pressed && keyCode == KeyEvent.VK_DOWN) currentState = State.CROUCHING;
        // break;
        //
        // case MOVING:
        // if (!pressed && keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_LEFT)
        // currentState = State.IDLE;
        // if (pressed && keyCode == KeyEvent.VK_SPACE) startJump();
        // break;
        //
        // case JUMPING:
        // // In a pure DFA, transitions out of JUMPING
        // // usually happen via physics events, not just key presses.
        // break;
        //
        // case CROUCHING:
        // if (!pressed && keyCode == KeyEvent.VK_DOWN) currentState = State.IDLE;
        // break;
        // }

        if (pressed && keyCode == KeyEvent.VK_LEFT) {
            input = LEFT;
            direction = LEFT;
            lastFacingDirection = LEFT;
        } else if (pressed && keyCode == KeyEvent.VK_RIGHT) {
            input = RIGHT;
            direction = RIGHT;
            lastFacingDirection = RIGHT;
        } else if (!pressed && keyCode == KeyEvent.VK_LEFT) {
            input = RELEASE_LEFT;
            direction = RELEASE_LEFT;
        } else if (!pressed && keyCode == KeyEvent.VK_RIGHT) {
            input = RELEASE_RIGHT;
            direction = RELEASE_RIGHT;
        } else if (pressed && keyCode == KeyEvent.VK_SPACE) {
            input = SPACE;
            startJump();
        } else if (!pressed && keyCode == KeyEvent.VK_DOWN) {
            input = RELEASE_DOWN;
        } else if (pressed && keyCode == KeyEvent.VK_DOWN) {
            input = DOWN;
        }

        printKey(keyCode, pressed);

        state = table[state][input];
        currentState = State.values()[state];
        System.out.println(currentState);
    }

    private void printKey(int keyCode, boolean pressed) {
        if (pressed && keyCode == KeyEvent.VK_LEFT) {
            System.out.println("VK_LEFT");
        } else if (pressed && keyCode == KeyEvent.VK_RIGHT) {
            System.out.println("VK_RIGHT");
        } else if (!pressed && keyCode == KeyEvent.VK_LEFT) {
            System.out.println("VK_RELEASE_LEFT");
        } else if (!pressed && keyCode == KeyEvent.VK_RIGHT) {
            System.out.println("VK_RELEASE_RIGHT");
        } else if (pressed && keyCode == KeyEvent.VK_SPACE) {
            System.out.println("VK_SPACE");
        } else if (!pressed && keyCode == KeyEvent.VK_DOWN) {
            System.out.println("VK_RELEASE_DOWN");
        } else if (pressed && keyCode == KeyEvent.VK_DOWN) {
            System.out.println("VK_DOWN");
        }
    }

    private void startJump() {
        if (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING)
            velocityY = -15; // Upward force
    }

    private void performJump() {
        marioY += velocityY;
        velocityY += 1; // Gravity

        if (marioY >= GROUND_Y) {
            marioY = GROUND_Y;
            if (currentState == State.JUMP_IDLE) {
                currentState = State.IDLE;
                state = State.IDLE.ordinal();
            } else if (currentState == State.JUMP_MOVING) {
                currentState = State.MOVING;
                state = State.MOVING.ordinal();
            }
        }
    }

    private void performMove() {
        if (direction == LEFT) {
            if (marioX >= 8)
                marioX -= 5;
        } else if (direction == RIGHT) {
            if (marioX <= SCREEN_WIDTH - 55)
                marioX += 5;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Physics logic for JUMPING state
        if (currentState == State.JUMP_IDLE) {
            performJump();
        }

        if (currentState == State.JUMP_MOVING) {
            // performMove();
            performJump();
        }

        if (currentState == State.MOVING || currentState == State.JUMP_MOVING) {
            performMove();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // if (background != null) {
        // g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        // }

        if (marioSprite != null) {
            BufferedImage newMario = marioSprite;
            if (lastFacingDirection == LEFT && (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING))
                newMario = createFlipped(marioSprite);
            else if (lastFacingDirection == RIGHT
                    && (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING))
                newMario = marioMoving;
            else if (currentState == State.JUMP_IDLE || currentState == State.JUMP_MOVING) {
                if (lastFacingDirection == LEFT)
                    newMario = createFlipped(marioJumping);
                else
                    newMario = marioJumping;
            }

            if (currentState == State.CROUCHING) {
                // Draw Mario shorter/squashed
                g.drawImage(newMario, marioX, marioY + 20, 40, 30, null);
            } else {
                // Draw Mario normally
                g.drawImage(newMario, marioX, marioY, 40, 50, null);
                // if(direction == LEFT) {
                // g.drawImage(marioSprite, marioX, marioY, -40, 50, null);
                // } else if (direction == RIGHT)
                // g.drawImage(marioSprite, marioX, marioY, 40, 50, null);
            }
        } else {
            g.setColor(Color.RED);
            if (currentState == State.CROUCHING) {
                // Draw Mario shorter/squashed
                // g.drawImage(newMario, marioX, marioY + 20, 40, 30, null);
                g.fillRect(marioX, marioY + 20, 40, 30);
            } else {
                // Draw Mario normally
                g.fillRect(marioX, marioY, 40, 50);
                // if(direction == LEFT) {
                // g.drawImage(marioSprite, marioX, marioY, -40, 50, null);
                // } else if (direction == RIGHT)
                // g.drawImage(marioSprite, marioX, marioY, 40, 50, null);
            }
        }

        // g.setColor(Color.RED);
        // if(currentState == State.CROUCHING) {
        // g.fillRect(marioX, marioY + 20, 40, 30);
        // } else {
        // g.fillRect(marioX, marioY, 40, 50);
        // }

        g.setColor(Color.BLACK);
        g.drawString("Current State: " + currentState, 20, 20);
    }

    private static BufferedImage createFlipped(BufferedImage image) {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-image.getWidth(), 0));
        return createTransformed(image, at);
    }

    private static BufferedImage createTransformed(
            BufferedImage image, AffineTransform at) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

}
