import javax.imageio.ImageIO;
import javax.sound.sampled.*;
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
import java.net.URL;

public class MarioFSM extends JPanel implements ActionListener {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int RELEASE_LEFT = 2;
    public static final int RELEASE_RIGHT = 3;
    public static final int SPACE = 4;
    public static final int RELEASE_DOWN = 5;
    public static final int DOWN = 6;
    public static final int PNG_MARIO_SPRITE_HEIGHT = 56;

    private Clip clip;

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

    private BufferedImage marioSprite, marioMoving, marioCrouching, marioMoving_1, marioMoving_2, marioMoving_3, marioStopping;
    private BufferedImage background;

    private State currentState = State.IDLE;
    private int marioX = 100, marioY = 268;
    private int velocityY = 0;
    private final int GROUND_Y = 268;
    private int direction = 0;
    private int lastFacingDirection = RIGHT;
    private int currentWalkState = 0;
    private double walkCounter = 0;
    private int walkingMomentum = 0;
    private boolean isDecelarating = false;

    public MarioFSM(int screenWidth, int screenHeight) {
        SCREEN_WIDTH = screenWidth;
        SCREEN_HEIGHT = screenHeight;
        loadImages();
        loadSound();
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
            marioSprite = ImageIO.read(new File("../resources/mario_idle.png"));
            marioJumping = ImageIO.read(new File("../resources/mario_jumping.png"));
            marioMoving = ImageIO.read(new File("../resources/mario_moving.png"));
            marioCrouching = ImageIO.read(new File("../resources/mario_crouching.png"));
            marioMoving_1 = ImageIO.read(new File("../resources/mario_moving_1.png"));
            marioMoving_2 = ImageIO.read(new File("../resources/mario_moving_2.png"));
            marioMoving_3 = ImageIO.read(new File("../resources/mario_moving_3.png"));
            marioStopping = ImageIO.read(new File("../resources/mario_stopping.png"));

            // background = ImageIO.read(new File("resources/background.png"));
        } catch (IOException e) {
            System.out.println("Error: Could not find image files!");
            e.printStackTrace();
        }
    }

    public void handleInput(int keyCode, boolean pressed) {
        if (pressed && keyCode == KeyEvent.VK_LEFT) {
            if(input == RIGHT) {
                return;
            }

            input = LEFT;
            direction = LEFT;
            lastFacingDirection = LEFT;

            startMove();
        } else if (pressed && keyCode == KeyEvent.VK_RIGHT) {
            if(input == LEFT) {
                return;
            }

            input = RIGHT;
            direction = RIGHT;
            lastFacingDirection = RIGHT;
            
            startMove();
        } else if (!pressed && keyCode == KeyEvent.VK_LEFT) {
            input = RELEASE_LEFT;
            // direction = RELEASE_LEFT;
            isDecelarating = true;
            // currentWalkState = 0;
            // walkCounter = 0;
            // walkingMomentum = 0;
        } else if (!pressed && keyCode == KeyEvent.VK_RIGHT) {
            input = RELEASE_RIGHT;
            // direction = RELEASE_RIGHT;
            isDecelarating = true;
            // currentWalkState = 0;
            // walkCounter = 0;
            // walkingMomentum = 0;
        } else if (pressed && keyCode == KeyEvent.VK_SPACE) {
            input = SPACE;
            startJump();
        } else if (!pressed && keyCode == KeyEvent.VK_DOWN) {
            input = RELEASE_DOWN;
            isDecelarating = true;
        } else if (pressed && keyCode == KeyEvent.VK_DOWN) {
            input = DOWN;
        }

        printKey(keyCode, pressed);

        state = table[state][input];
        currentState = State.values()[state];
        System.out.println(currentState);
    }

    private void startMove() {
        if(currentState == State.IDLE || currentState == State.JUMP_IDLE) {
            currentWalkState = 1;
            walkingMomentum = 1;
        }
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
        if (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING) {
            if (clip == null) {
                loadSound();
            }
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
            
            velocityY = -15; // Upward force
        }
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
        if(currentState == State.MOVING || currentState == State.JUMP_MOVING) {
            walkingMomentum += 1;
            if (walkingMomentum > 32) { 
                walkingMomentum = 32;
            }
        }

        if (direction == LEFT) {
            if (marioX >= 8) {
                // marioX -= 5;
                marioX -= walkingMomentum / 8;
            }
        } else if (direction == RIGHT) {
            if (marioX <= SCREEN_WIDTH - 55) {
                // marioX += 5;
                marioX += walkingMomentum / 8;
            }
        }

        // if(currentState == State.IDLE || currentState == State.JUMP_IDLE) {
        //     walkingMomentum -= 2; // Decrease momentum when not accelerating
        //     if (walkingMomentum < 0) {
        //         walkingMomentum = 0;
        //     }
        // }

        // walkCounter = (walkCounter + 1) % 8; // Adjust this value to change walking animation speed
        // walkCounter++;
        walkCounter += (double) walkingMomentum / 16; // Adjust this value to change walking animation speed
        if (walkCounter >= 4) { //Adjust this value to change walking animation speed
            walkCounter = 0;

            currentWalkState++;
            if(currentWalkState >= 4) {
                currentWalkState = 1;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == State.JUMP_IDLE) {
            performJump();
        } else if (currentState == State.JUMP_MOVING) {
            performJump();
        } 
        
        if (currentState == State.MOVING || currentState == State.JUMP_MOVING) {
            performMove();
        }
        
        if ((currentState == State.IDLE || currentState == State.JUMP_IDLE || currentState == State.CROUCHING) && isDecelarating) {
            walkingMomentum -= 1;
            if (walkingMomentum < 0) {
                walkingMomentum = 0;
                currentWalkState = 0;
                walkCounter = 0;
                isDecelarating = false;
                direction = -1;
            }
            
            if(walkingMomentum > 0) {
                performMove();
            }
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

            if (currentState == State.MOVING){
                newMario = getCurrentMarioMovementImage();
            } else if (currentState == State.CROUCHING) {
                newMario = marioCrouching;
            } else if (currentState == State.JUMP_IDLE || currentState == State.JUMP_MOVING) {
                newMario = marioJumping;
            } else if (currentState == State.IDLE) {
                if (walkingMomentum > 0) {
                    newMario = getCurrentMarioMovementImage();
                } else {
                    newMario = marioSprite;
                }
            }

            if (lastFacingDirection == LEFT){
                newMario = createFlipped(newMario);
            }

            int height = newMario.getHeight();
            
            final int CROUCHING_HEIGHT = 50;
            if(height < CROUCHING_HEIGHT) {
                if(currentState == State.CROUCHING) {
                    marioY = GROUND_Y + (PNG_MARIO_SPRITE_HEIGHT - height);
                    // height = newMario.getHeight();
                    System.out.println("CROUCHING HEIGHT: " + height);
                    System.out.println("Y POSITION: " + marioY);    
                } 
                // else {
                //     height = newMario.getHeight() + (PNG_MARIO_SPRITE_HEIGHT - newMario.getHeight());
                // }
            } else if (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING) {
                marioY = GROUND_Y;
            }
            
            g.drawImage(newMario, marioX, marioY, newMario.getWidth(), height, null);


            // if (lastFacingDirection == LEFT && (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING))
            //     newMario = createFlipped(marioMoving);
            // else if (lastFacingDirection == RIGHT
            //         && (currentState != State.JUMP_IDLE && currentState != State.JUMP_MOVING))
            //     newMario = marioMoving;
            // else if (currentState == State.JUMP_IDLE || currentState == State.JUMP_MOVING) {
            //     if (lastFacingDirection == LEFT)
            //         newMario = createFlipped(marioJumping);
            //     else
            //         newMario = marioJumping;
            // }

            // if (currentState == State.CROUCHING) {
            //     // Draw Mario shorter/squashed
            //     if (lastFacingDirection == LEFT)
            //         newMario = createFlipped(marioCrouching);
            //     else
            //         newMario = marioCrouching;

            //     g.drawImage(newMario, marioX, marioY, newMario.getWidth(), newMario.getHeight(), null);
            // } else {
            //     // Draw Mario normally
            //     g.drawImage(newMario, marioX, marioY, newMario.getWidth(), newMario.getHeight(), null);
            //     // if(direction == LEFT) {
            //     // g.drawImage(marioSprite, marioX, marioY, -40, 50, null);
            //     // } else if (direction == RIGHT)
            //     // g.drawImage(marioSprite, marioX, marioY, 40, 50, null);
            // }
        } else {
            g.setColor(Color.RED);
            if (currentState == State.CROUCHING) {
                // g.drawImage(newMario, marioX, marioY + 20, 40, 30, null);
                g.fillRect(marioX, marioY + 20, 40, 30);
            } else {
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
        g.drawString("Walking Momentum: " + walkingMomentum, 20, 40);
        g.drawString("Walk Counter: " + walkCounter, 20, 60);
        g.drawString("isDecelarating: " + isDecelarating, 20, 80);
    }

    private BufferedImage getCurrentMarioMovementImage() {
        switch (currentWalkState) {
            case 1:
                return marioMoving_1;
            case 2:
                return marioMoving_2;
            case 3:
                return marioMoving_3;
            default:
                return marioMoving;
        }
    }

    public void loadSound() {
        try {
            File soundFile = new File("../resources/audio/smb_jump-super.wav");
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + soundFile.getAbsolutePath());
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
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
