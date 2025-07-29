import java.util.List;

// Abstract class representing physics behavior for a piece
public abstract class Physics {

    protected final Board board;                   // Reference to the board (for conversions and access)
    protected Moves.Pair startCell;                // Starting cell of the movement
    protected Moves.Pair endCell;                  // Ending cell of the movement
    protected double[] currPosM;                   // Current position in meters (x, y)
    protected final double param;                  // Generic parameter (can be speed or duration)
    protected long startMs;                        // Start timestamp of the movement (milliseconds)

    // Constructor
    protected Physics(Board board, double param) {
        this.board = board;
        this.param = param;
    }

    // ---------------- abstract ----------------

    // Resets physics state from a command (must be implemented by subclass)
    public abstract void reset(Command cmd);

    // Updates position/state based on current time (must be implemented by subclass)
    public abstract Command update(long nowMs);

    // ---------------- helpers -----------------

    // Returns current position in meters
    public double[] getPosM() { return currPosM; }

    // Returns current position in pixels
    public int[] getPosPix()  { return board.mToPix(currPosM[0], currPosM[1]); }

    // Returns current cell coordinates
    public Moves.Pair getCurrCell() { return board.mToCellPair(currPosM[0], currPosM[1]); }

    public long getStartMs() { return startMs; }

    // Can this piece be captured?
    public boolean canBeCaptured() { return true; }

    // Can this piece capture others?
    public boolean canCapture() { return true; }

    // Does this piece block other movements?
    public boolean isMovementBlocker() { return false; }

    /* ---------------- concrete subclasses ---------------- */

    // -----------------------------------------------
    // IdlePhysics: piece is not moving, just waiting
    public static class IdlePhysics extends Physics {
        public IdlePhysics(Board board) { super(board, 0.0); }

        @Override
        public void reset(Command cmd) {
            if (cmd.type.equals("done")) {
                if (endCell == null) {
                    if (startCell != null) {
                        endCell = startCell; // fallback to last known
                    } else {
                        endCell = new Moves.Pair(0,0); // default
                    }
                }
                startCell = endCell;
            } else if (cmd.params.isEmpty()) {
                startCell = endCell = new Moves.Pair(0,0); // no parameters provided
            } else {
                startCell = endCell = (Moves.Pair) cmd.params.get(0); // get from command
            }
            currPosM = board.cellToM(startCell); // convert to position
            startMs = cmd.timestamp;
        }

        @Override
        public Command update(long nowMs) { return null; } // idle does nothing

        @Override
        public boolean canCapture() { return true; }
        @Override
        public boolean isMovementBlocker() { return true; }
    }

    // -----------------------------------------------
    // MovePhysics: piece moves between two cells
    public static class MovePhysics extends Physics {
        private double[] movementVec;     // Unit vector direction of movement
        private double movementVecLength; // Distance between start and end in meters
        private double durationSec;       // Total movement time in seconds

        public MovePhysics(Board board, double speedCellsPerSec) {
            super(board, speedCellsPerSec); // param = speed
        }

        @Override
        public void reset(Command cmd) {
            startCell = (Moves.Pair) cmd.params.get(0);
            endCell   = (Moves.Pair) cmd.params.get(1);
            currPosM  = board.cellToM(startCell);
            startMs   = cmd.timestamp;

            double[] startPos = board.cellToM(startCell);
            double[] endPos   = board.cellToM(endCell);
            movementVec = new double[]{ endPos[0]-startPos[0], endPos[1]-startPos[1] };
            movementVecLength = Math.hypot(movementVec[0], movementVec[1]); // distance
            if (movementVecLength == 0) movementVecLength = 1; // prevent divide-by-zero
            movementVec[0] /= movementVecLength; // normalize
            movementVec[1] /= movementVecLength;
            durationSec = movementVecLength / param; // time = distance / speed
        }

        @Override
        public Command update(long nowMs) {
            double secondsPassed = (nowMs - startMs) / 1000.0; // time in seconds
            currPosM = board.cellToM(startCell);
            currPosM[0] += movementVec[0] * secondsPassed * param;
            currPosM[1] += movementVec[1] * secondsPassed * param;

            if (secondsPassed >= durationSec) {
                return new Command(nowMs, null, "done", List.of()); // movement complete
            }
            return null;
        }

        public double getSpeedCellsPerSec() { return param; }
    }

    // -----------------------------------------------
    // StaticTemporaryPhysics: stays in place for a fixed time
    public static class StaticTemporaryPhysics extends Physics {
        private final double durationSec;

        public StaticTemporaryPhysics(Board board, double durationSec) {
            super(board, durationSec); // param = duration
            this.durationSec = durationSec;
        }

        @Override
        public void reset(Command cmd) {
            startCell = endCell = (Moves.Pair) cmd.params.get(0);
            currPosM = board.cellToM(startCell);
            startMs = cmd.timestamp;
        }

        @Override
        public Command update(long nowMs) {
            double sec = (nowMs - startMs) / 1000.0;
            if (sec >= durationSec) {
                return new Command(nowMs, null, "done", List.of()); // finished resting/waiting
            }
            return null;
        }

        public double getDurationSec() { return durationSec; }
    }

    // -----------------------------------------------
    // JumpPhysics: temporary state, cannot be captured
    public static class JumpPhysics extends StaticTemporaryPhysics {
        public JumpPhysics(Board board, double durationSec) { super(board, durationSec); }
        @Override public boolean canBeCaptured() { return false; }
    }

    // -----------------------------------------------
    // RestPhysics: temporary state, cannot capture and blocks movement
    public static class RestPhysics extends StaticTemporaryPhysics {
        public RestPhysics(Board board, double durationSec) { super(board, durationSec); }
        @Override public boolean canCapture() { return false; }
        @Override public boolean isMovementBlocker() { return true; }
    }
}
