public class KeyboardProducer extends Thread {
    // Reference to the game instance
    private final Game game;
    // BlockingQueue to send commands from keyboard input to the game logic
    private final java.util.concurrent.BlockingQueue<Command> queue;
    // KeyboardProcessor to interpret key inputs and manage cursor/actions
    private final KeyboardProcessor processor;
    // Player number (e.g., 1 or 2) this producer is associated with
    private final int player;
    // Volatile boolean flag to control the running state of the thread safely across threads
    private volatile boolean running = true;

    /**
     * Constructor to initialize the keyboard input thread.
     *
     * @param game The game instance
     * @param queue The command queue to send parsed commands
     * @param processor The keyboard processor that interprets keys
     * @param player Player number (1 or 2)
     */
    public KeyboardProducer(Game game,
                            java.util.concurrent.BlockingQueue<Command> queue,
                            KeyboardProcessor processor,
                            int player) {
        this.game = game;
        this.queue = queue;
        this.processor = processor;
        this.player = player;
        // Mark this thread as daemon so JVM can exit without waiting for this thread
        setDaemon(true);
    }

    /**
     * The thread's main loop. Currently, it doesn't capture real keyboard input.
     * Instead, it just sleeps repeatedly while running is true.
     * This allows tests to verify start/stop behaviour without blocking.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(100);  // Sleep to avoid busy-waiting
            }
        } catch (InterruptedException ignored) {
            // If interrupted (usually on stop request), exit gracefully
        }
    }

    /**
     * Stops the thread safely by setting running to false and interrupting sleep.
     */
    public void stopProducer() {
        running = false;
        this.interrupt();
    }

    /**
     * Provides access to the associated KeyboardProcessor, useful for tests or external usage.
     *
     * @return The KeyboardProcessor linked to this producer
     */
    public KeyboardProcessor getProcessor() { return processor; }

    /**
     * Returns the player number (1 or 2) that this producer is associated with.
     *
     * @return Player number
     */
    public int getPlayer() { return player; }
}
