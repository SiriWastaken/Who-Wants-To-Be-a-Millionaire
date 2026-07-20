import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Handles the cinematic, timed, hardware-antialiased 12-second opening sequence
 * modeled directly after the studio animations of "Who Wants to Be a Millionaire".
 * Safely manages the temporary intro track process before gameplay handoff.
 */
public class IntroCutscene extends JPanel {

    // --- Typography Configuration ---
    private final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private final Font SKIP_FONT = new Font("SansSerif", Font.BOLD, 12);

    // --- Design System Color Palette ---
    private final Color BACKGROUND_TOP = new Color(10, 8, 28);
    private final Color BACKGROUND_BOTTOM = new Color(3, 2, 10);
    private final Color STUDIO_BLUE = new Color(99, 102, 241);
    private final Color STUDIO_CYAN = new Color(6, 182, 212);
    private final Color ACCENT_GOLD = new Color(245, 158, 11);

    // --- Animation Timeline Constants ---
    // 12 seconds total * 1000ms / 16ms per frame = ~750 frames (targeting 60 frames per second)
    private final int MAX_FRAMES = 750;
    private final int LOGO_TARGET_SIZE = 500;

    private final Timer ANIMATION_TIMER;
    private final Runnable ON_COMPLETION;

    private Image logoImage;
    private Process mp3Process; // Holds native sub-process running platform-specific audio playback
    private int frame = 0;
    private boolean isSkipped = false;

    /**
     * Initializes the intro sequencer with synchronized MP3 soundtrack playback.
     * 
     * @param onCompletion Callback executable routed back to launch GameScreenPanel safely.
     */
    public IntroCutscene(Runnable onCompletion) {
        this.ON_COMPLETION = onCompletion;
        setPreferredSize(new Dimension(1100, 760));
        setFocusable(true);

        // Safe asset resolution pipeline loading logo binary disk files
        try {
            logoImage = ImageIO.read(new File("src/assets/logoImage.png"));
        } catch (Exception e) {
            System.err.println(
                    "Warning: Asset 'src/assets/logoImage.png' not found. Falling back to graphical placeholder.");
            logoImage = null;
        }

        // Initialize and play the intro soundtrack clip
        initAudio();

        // Establish core physics cycle frame rate timing (~16.6ms intervals mapping to 60Hz)
        ANIMATION_TIMER = new Timer(16, e -> tickAnimation());
        ANIMATION_TIMER.start();

        // Mouse click interceptor anywhere over surface intercepts cutscene and immediately invokes skip process
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                skipCutscene();
            }
        });
    }

    /**
     * Spawns a background native process specifically playing the intro portion.
     * Maps across environment configurations dynamically to prevent freezing main UI execution threads.
     */
    private void initAudio() {
        try {
            File audioFile = new File("src/assets/Gamesoundtrack.mp3");

            // Simple casing correction check fallback
            if (!audioFile.exists()) {
                audioFile = new File("src/assets/GameSoundtrack.mp3");
            }

            if (audioFile.exists()) {
                String OS = System.getProperty("os.name").toLowerCase();
                // Platform Runtime Strategy Pattern routing execution hooks via OS binary requirements
                if (OS.contains("mac")) {
                    // macOS native core audio engine interface command line execution path
                    mp3Process = Runtime.getRuntime().exec(new String[] { "afplay", audioFile.getAbsolutePath() });
                } else {
                    // Windows shell execution command line path executing sound targets asynchronously minimized
                    mp3Process = Runtime.getRuntime()
                            .exec(new String[] { "cmd", "/c", "start", "/min", audioFile.getAbsolutePath() });
                }
            } else {
                System.err.println("Warning: Audio file 'Gamesoundtrack.mp3' not found in src/assets/");
            }
        } catch (Exception e) {
            System.err.println("Warning: MP3 player initialization failed: " + e.getMessage());
        }
    }

    /**
     * Updates frame counters and routes display refresh pipelines at regular intervals.
     */
    private void tickAnimation() {
        frame++;
        if (frame >= MAX_FRAMES) {
            completeCutscene();
        } else {
            repaint(); // Request window manager graphics draw invocation pass
        }
    }

    /**
     * Thread-safe interceptor blocking double execution triggers during quick mouse clicks.
     */
    private synchronized void skipCutscene() {
        if (!isSkipped) {
            isSkipped = true;
            completeCutscene();
        }
    }

    /**
     * Halts internal clock operations and releases sub-process resources smoothly.
     */
    private void completeCutscene() {
        ANIMATION_TIMER.stop();

        // Kills this specific process instance so the gameplay panel can freely spin up its next audio section
        if (mp3Process != null) {
            mp3Process.destroy();
        }

        // Handoff visual operations to underlying game logic panels safely
        if (ON_COMPLETION != null) {
            ON_COMPLETION.run();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Configure advanced graphic enhancement metrics pipeline presets
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int centerX = w / 2;
        int centerY = h / 2;

        // --- PHASE 1: Ambient Backdrop Space ---
        GradientPaint bg = new GradientPaint(0, 0, BACKGROUND_TOP, 0, h, BACKGROUND_BOTTOM);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        // Smoothly fade up ambient center spot illumination over the initial 90 frames (1.5 seconds)
        float ambientAlpha = Math.min(1.0f, frame / 90.0f);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ambientAlpha * 0.15f));
        Point2D centerPoint = new Point2D.Float(centerX, centerY);
        RadialGradientPaint centerGlow = new RadialGradientPaint(centerPoint, 450f, new float[] { 0f, 1f },
                new Color[] { STUDIO_BLUE, new Color(0, 0, 0, 0) });
        g2.setPaint(centerGlow);
        g2.fillRect(0, 0, w, h);
        g2.setComposite(AlphaComposite.SrcOver); // Reset channel blend mode rules back to absolute standard copy configuration

        // --- PHASE 2: Studio Cross-Beams Rotation ---
        // Begins at 1 second mark. Renders opposing intersecting vector arrays mimicking studio searchlights
        if (frame >= 60) {
            double angle1 = (frame - 60) * 0.008;  // Clockwise angular scale velocity
            double angle2 = -(frame - 60) * 0.005; // Counter-clockwise angular scale velocity

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2.setStroke(new BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Line Vector A (Primary Indigo Searchlight)
            g2.setColor(STUDIO_BLUE);
            g2.drawLine(
                    (int) (centerX + Math.cos(angle1) * 600), (int) (centerY + Math.sin(angle1) * 600),
                    (int) (centerX - Math.cos(angle1) * 600), (int) (centerY - Math.sin(angle1) * 600));
            
            // Line Vector B (Secondary Cyan Searchlight)
            g2.setColor(STUDIO_CYAN);
            g2.drawLine(
                    (int) (centerX + Math.sin(angle2) * 600), (int) (centerY + Math.cos(angle2) * 600),
                    (int) (centerX - Math.sin(angle2) * 600), (int) (centerY - Math.cos(angle2) * 600));
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // --- PHASE 3 & 4: Logo Scale Up & Stable Position ---
        // Triggers at 5-second checkpoint boundary marks. 
        // Manages composite image interpolation scaling along with an integrated rotational kinetic spin.
        if (frame >= 300) {
            // Evaluates a 3-second cubic easing out curve transformation scaling pass (300 to 480 frames tracking metrics)
            float progress = Math.min(1.0f, (frame - 300) / 180.0f);
            float currentScale = (float) (1.0 - Math.pow(1.0 - progress, 3)); // Cubic ease-out calculation formula
            int size = (int) (LOGO_TARGET_SIZE * currentScale);

            if (size > 0) {
                // Isolate canvas state transformations to ensure nested rotation calculations don't pollute parent panel rules
                Graphics2D gLogo = (Graphics2D) g2.create();
                gLogo.translate(centerX, centerY);

                // Spin deceleration tracking matrix modifier loops
                if (progress < 1.0f) {
                    double rotationAngle = (1.0f - progress) * 6.5; // Decreasing spin speed until lock criteria is matched
                    gLogo.rotate(rotationAngle);
                }

                gLogo.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, progress));

                // Process high-intensity radial glare behind logo center frame layers
                float glareRadius = size * 0.7f;
                RadialGradientPaint backGlow = new RadialGradientPaint(
                        new Point2D.Float(0, 0), Math.max(1f, glareRadius),
                        new float[] { 0f, 0.4f, 1f },
                        new Color[] { new Color(255, 255, 255, 120), new Color(99, 102, 241, 35),
                                new Color(0, 0, 0, 0) });
                gLogo.setPaint(backGlow);
                gLogo.fillRect(-size, -size, size * 2, size * 2);

                // Render image data asset or standard fallback vector structures if source resolution failures occur
                if (logoImage != null) {
                    gLogo.drawImage(logoImage, -size / 2, -size / 2, size, size, null);
                } else {
                    gLogo.setStroke(new BasicStroke(6f));
                    gLogo.setColor(ACCENT_GOLD);
                    gLogo.drawOval(-size / 2, -size / 2, size, size);
                    gLogo.setFont(new Font("SansSerif", Font.BOLD, (int) (32 * currentScale)));
                    gLogo.setColor(Color.WHITE);
                    FontMetrics fmL = gLogo.getFontMetrics();
                    String fallbackText = "FINAL ANSWER?";
                    gLogo.drawString(fallbackText, -fmL.stringWidth(fallbackText) / 2, fmL.getAscent() / 2 - 5);
                }

                // Commented out because it doesn't fit the current logo size but if desired, the size of the panel can be fixed
                // if (progress >= 1.0f) {
                //     float fadePulse = (float) (Math.sin((frame - 480) * 0.06) * 0.15 + 0.85);
                //     gLogo.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadePulse));
                //     gLogo.setStroke(new BasicStroke(2f));
                //     gLogo.setColor(ACCENT_GOLD);
                //     gLogo.drawOval(-size / 2 - 12, -size / 2 - 12, size + 24, size + 24);
                // }

                gLogo.dispose(); // Release isolated matrix buffer allocation layers safely
            }
        }

        // --- Phase 4 Subtitle Fade In ---
        // Triggers around the ~8.6 second point threshold mark. Introduces text layer with an opacity fade-in sweep.
        if (frame >= 520) {
            float textAlpha = Math.min(1.0f, (frame - 520) / 50.0f); // 50-frame quick opacity fade loop handler
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textAlpha));
            g2.setFont(SUBTITLE_FONT);
            g2.setColor(new Color(156, 163, 175));
            drawCentered(g2, "ARE YOU READY TO BE A MILLIONAIRE?", centerY + 220, w);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // Persistent Skip Overlay Indicator (Static anchor baseline text line)
        g2.setFont(SKIP_FONT);
        g2.setColor(new Color(156, 163, 175, 110));
        g2.drawString("CLICK ANYWHERE TO SKIP", 30, h - 35);
    }

    /**
     * String positioning helper managing basic horizontal centering calculations.
     */
    private void drawCentered(Graphics2D g2, String text, int y, int panelWidth) {
        FontMetrics fm = g2.getFontMetrics();
        int x = (panelWidth - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }
}