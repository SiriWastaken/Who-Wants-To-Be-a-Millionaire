import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * GameScreenPanel acts as the controller and interactive layer for gameplay.
 * It manages clicks, hovers, answer submissions, animation timings, 
 * and routes out directly to the game over screen on failures or time outs.
 */
public class GameScreenPanel extends JPanel {

    private final GameSession session;
    private final GameScreenRenderer renderer;
    private final JFrame parentFrame;

    // Boundary maps for interactive layout click zones
    private final Rectangle backButtonBounds = new Rectangle(20, 20, 90, 36);
    private final Rectangle[] answerBounds = new Rectangle[4];
    private final Rectangle[] lifelineBounds = new Rectangle[4];

    // Interaction tracking state variables
    private int hoveredAnswerIndex = -1;
    private int hoveredLifelineIndex = -1;
    private boolean hoveringBack = false;
    private int selectedAnswerIndex = -1;
    private boolean answerRevealed = false;
    private boolean inputBlocked = false;

    private final Timer gameLoopTimer;

    public GameScreenPanel(JFrame parentFrame, GameSession session) {
        this.parentFrame = parentFrame;
        this.session = session;
        this.renderer = new GameScreenRenderer();

        setPreferredSize(new Dimension(1100, 750));
        setFocusable(true);

        // Configure layout button nodes dynamically
        initLayoutBounds();

        // High-frequency UI tick loop (updates timers and animations smoothly)
        gameLoopTimer = new Timer(1000, e -> handleGameTick());
        gameLoopTimer.start();

        // Repaint driver for smooth fluid pulse color changes
        Timer repaintTimer = new Timer(50, e -> repaint());
        repaintTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getPoint());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMovement(e.getPoint());
            }
        });
    }

    private void initLayoutBounds() {
        // Setup answer cards grid matrix positions
        int startX = 80;
        int startY = 400;
        int boxWidth = 450;
        int boxHeight = 54;
        int gapX = 40;
        int gapY = 24;

        answerBounds[0] = new Rectangle(startX, startY, boxWidth, boxHeight);
        answerBounds[1] = new Rectangle(startX + boxWidth + gapX, startY, boxWidth, boxHeight);
        answerBounds[2] = new Rectangle(startX, startY + boxHeight + gapY, boxWidth, boxHeight);
        answerBounds[3] = new Rectangle(startX + boxWidth + gapX, startY + boxHeight + gapY, boxWidth, boxHeight);

        // Setup bottom row utility lifeline blocks
        int lifeStartX = 80;
        int lifeStartY = 580;
        int lifeWidth = 210;
        int lifeHeight = 44;
        int lifeGap = 33;

        for (int i = 0; i < 4; i++) {
            lifelineBounds[i] = new Rectangle(lifeStartX + i * (lifeWidth + lifeGap), lifeStartY, lifeWidth, lifeHeight);
        }
    }

    private void handleGameTick() {
        if (session.isFinished()) {
            return;
        }

        // Advance backend timer tracking increments
        session.tick();

        // CRITICAL FIX: If an active game session expires due to timeout, immediately route directly to Game Over
        if (session.isFinished() && "Time expired.".equals(session.getStatusMessage())) {
            triggerGameOverSequence();
        }
    }

    private void handleMouseMovement(Point point) {
        if (inputBlocked || session.isFinished()) {
            hoveringBack = false;
            hoveredAnswerIndex = -1;
            hoveredLifelineIndex = -1;
            return;
        }

        // If high stakes choice overlay is pending, re-map layout regions contextually to options 0 and 1
        if (session.isHighStakesDecisionPending()) {
            int center = getWidth() / 2;
            Rectangle playBox = new Rectangle(center - 310, 370, 280, 90);
            Rectangle walkBox = new Rectangle(center + 30, 370, 280, 90);

            if (playBox.contains(point)) hoveredAnswerIndex = 0;
            else if (walkBox.contains(point)) hoveredAnswerIndex = 1;
            else hoveredAnswerIndex = -1;
            return;
        }

        hoveringBack = backButtonBounds.contains(point);

        hoveredAnswerIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (answerBounds[i].contains(point) && !session.isAnswerEliminated(i)) {
                hoveredAnswerIndex = i;
                break;
            }
        }

        hoveredLifelineIndex = -1;
        for (int i = 0; i < 4; i++) {
            if (lifelineBounds[i].contains(point)) {
                hoveredLifelineIndex = i;
                break;
            }
        }
    }

    private void handleMouseClick(Point point) {
        if (inputBlocked || session.isFinished()) return;

        // Redirect interaction events explicitly when the inline PlayOrWalk UI overlay is active
        if (session.isHighStakesDecisionPending()) {
            int center = getWidth() / 2;
            Rectangle playBox = new Rectangle(center - 310, 370, 280, 90);
            Rectangle walkBox = new Rectangle(center + 30, 370, 280, 90);

            if (playBox.contains(point)) {
                session.chooseHighStakesDecision(true);
                resetInterfaceState();
            } else if (walkBox.contains(point)) {
                session.chooseHighStakesDecision(false);
                triggerGameOverSequence();
            }
            return;
        }

        if (backButtonBounds.contains(point)) {
            gameLoopTimer.stop();
            parentFrame.setContentPane(new MainMenu().getContentPane());
            parentFrame.validate();
            return;
        }

        // Process answer selections
        for (int i = 0; i < 4; i++) {
            if (answerBounds[i].contains(point) && !session.isAnswerEliminated(i)) {
                executeAnswerSubmissionSequence(i);
                return;
            }
        }

        // Process lifelines click selections
        for (int i = 0; i < 4; i++) {
            if (lifelineBounds[i].contains(point)) {
                handleLifelineTrigger(i);
                return;
            }
        }
    }

    private void executeAnswerSubmissionSequence(int answerIndex) {
        inputBlocked = true;
        selectedAnswerIndex = answerIndex;
        session.pauseTimer();

        // Validate answer target state via backend checks
        boolean correct = (session.getCorrectAnswerIndex() == answerIndex);
        session.setAnswerAnimating(answerIndex, correct);
        answerRevealed = true;

        // Freeze interface window frame for exactly 2 seconds to showcase results
        Timer freezeTimer = new Timer(2000, e -> {
            session.clearAnswerAnimation();
            
            // Apply result status updates to core session registers
            session.submitAnswer(answerIndex);

            if (!correct) {
                // If wrong choice submitted, complete freeze time and transition straight to game over screen
                triggerGameOverSequence();
            } else {
                // Clean up transient selection flags for the upcoming question round
                resetInterfaceState();
                session.resumeTimer();
            }
        });
        freezeTimer.setRepeats(false);
        freezeTimer.start();
    }

    private void handleLifelineTrigger(int index) {
        switch (index) {
            case 0:
                session.useSwap();
                break;
            case 1:
                if (!session.isAudiencePollUsed()) {
                    session.useAudiencePoll();
                    Question currentQ = session.getCurrentQuestion();
                    if (currentQ != null) {
                        AudiencePollDialog dialog = new AudiencePollDialog(new JDialog(parentFrame, true), currentQ);
                        dialog.pack();
                        dialog.setLocationRelativeTo(this);
                        dialog.setVisible(true);
                    }
                }
                break;
            case 2:
                session.useFiftyFifty();
                break;
            case 3:
                session.usePhoneAFriend();
                break;
        }
        resetInterfaceState();
    }

    private void resetInterfaceState() {
        selectedAnswerIndex = -1;
        answerRevealed = false;
        inputBlocked = false;
        hoveredAnswerIndex = -1;
        hoveredLifelineIndex = -1;
    }

    private void triggerGameOverSequence() {
        gameLoopTimer.stop();

        // 1. Extract clean integer/boolean primitives for custom GameOverScreen
        int finalWinnings = session.getScore();
        boolean failed = (session.getStatusType() == GameSession.StatusType.FAILURE);

        java.awt.Container parent = this.getParent();
        if (parent instanceof javax.swing.JSplitPane) {
            javax.swing.JSplitPane splitPane = (javax.swing.JSplitPane) parent;
            
            // Create a clean instance of the original screen configuration
            GameOverScreen gameOver = new GameOverScreen(finalWinnings, failed);
            
            splitPane.setLeftComponent(gameOver);
            
            splitPane.setDividerLocation(820); 
            
            gameOver.requestFocusInWindow();
            
            splitPane.revalidate();
            splitPane.repaint();
        } else {
            // Fallback layout wrapper path
            parentFrame.setContentPane(new GameOverScreen(finalWinnings, failed));
            parentFrame.validate();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.paint((Graphics2D) g, this);
    }

    // Expose component layouts cleanly to internal drawing dependencies
    public GameSession getSession() { return session; }
    public Rectangle getBackButtonBounds() { return backButtonBounds; }
    public boolean isHoveringBack() { return hoveringBack; }
    public Rectangle[] getAnswerBounds() { return answerBounds; }
    public int getHoveredAnswerIndex() { return hoveredAnswerIndex; }
    public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
    public boolean isAnswerRevealed() { return answerRevealed; }
    public boolean isInputBlocked() { return inputBlocked; }
    public Rectangle[] getLifelineBounds() { return lifelineBounds; }
    public int getHoveredLifelineIndex() { return hoveredLifelineIndex; }
    public boolean wasLastAnswerCorrect() { return session.wasLastAnswerCorrect(); }
}