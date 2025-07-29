import java.util.List;

public abstract class Physics {

    protected final Board board;
    protected Pair startCell;
    protected Pair endCell;
    protected double[] currPosM;        // (x,y) in metres
    protected final double param;       // generic parameter (speed or duration)
    protected long startMs;

    protected Physics(Board board, double param) {
        this.board = board;
        this.param = param;
    }

    // ---------------- abstract ----------------
    public abstract void reset(Command cmd);
    public abstract Command update(long nowMs);

    // ---------------- helpers -----------------
    public double[] getPosM() { return currPosM; }
    public int[] getPosPix()  { return board.mToPix(currPosM[0], currPosM[1]); }
    public Pair getCurrCell() { return board.mToCellPair(currPosM[0], currPosM[1]); }
    public long getStartMs() { return startMs; }

    public boolean canBeCaptured() { return true; }
    public boolean canCapture() { return true; }
    public boolean isMovementBlocker() { return false; }

    /* ---------------- concrete subclasses ---------------- */

    public static class IdlePhysics extends Physics {
        public IdlePhysics(Board board) { super(board, 0.0); }

        @Override
        public void reset(Command cmd) {
            if (cmd.type.equals("done")) {
                if (endCell == null) {
                    // fallback to previous known position if any
                    if (startCell != null) {
                        endCell = startCell;
                    } else {
                        endCell = new Pair(0,0);
                    }
                }
                startCell = endCell;
            } else if (cmd.params.isEmpty()) {
                startCell = endCell = new Pair(0,0);
            } else {
                startCell = endCell = (Pair) cmd.params.get(0);
            }
            currPosM = board.cellToM(startCell);
            startMs = cmd.timestamp;
        }

        @Override
        public Command update(long nowMs) { return null; }

        @Override
        public boolean canCapture() { return true; }
        @Override
        public boolean isMovementBlocker() { return true; }
    }

    public static class MovePhysics extends Physics {
        private double[] movementVec;     // normalised vector
        private double movementVecLength;
        private double durationSec;
        public MovePhysics(Board board, double speedCellsPerSec) { super(board, speedCellsPerSec); }

        @Override
        public void reset(Command cmd) {
            startCell = (Pair) cmd.params.get(0);
            endCell   = (Pair) cmd.params.get(1);
            currPosM  = board.cellToM(startCell);
            startMs   = cmd.timestamp;

            double[] startPos = board.cellToM(startCell);
            double[] endPos   = board.cellToM(endCell);
            movementVec = new double[]{ endPos[0]-startPos[0], endPos[1]-startPos[1] };
            movementVecLength = Math.hypot(movementVec[0], movementVec[1]);
            if (movementVecLength == 0) movementVecLength = 1; // avoid div/0
            movementVec[0] /= movementVecLength;
            movementVec[1] /= movementVecLength;
            durationSec = movementVecLength / param; // param = speed(m/s)
        }

        @Override
        public Command update(long nowMs) {
            double secondsPassed = (nowMs - startMs) / 1000.0;
            currPosM = board.cellToM(startCell);
            currPosM[0] += movementVec[0] * secondsPassed * param;
            currPosM[1] += movementVec[1] * secondsPassed * param;
            if (secondsPassed >= durationSec) {
                return new Command(nowMs, null, "done", List.of());
            }
            return null;
        }

        public double getSpeedCellsPerSec() { return param; }
    }

    public static class StaticTemporaryPhysics extends Physics {
        private final double durationSec;
        public StaticTemporaryPhysics(Board board, double durationSec) {
            super(board, durationSec);
            this.durationSec = durationSec;
        }
        @Override
        public void reset(Command cmd) {
            startCell = endCell = (Pair) cmd.params.get(0);
            currPosM = board.cellToM(startCell);
            startMs = cmd.timestamp;
        }
        @Override
        public Command update(long nowMs) {
            double sec = (nowMs - startMs) / 1000.0;
            if (sec >= durationSec) {
                return new Command(nowMs, null, "done", List.of());
            }
            return null;
        }

        public double getDurationSec() { return durationSec; }
    }

    public static class JumpPhysics extends StaticTemporaryPhysics {
        public JumpPhysics(Board board, double durationSec) { super(board, durationSec); }
        @Override public boolean canBeCaptured() { return false; }
    }

    public static class RestPhysics extends StaticTemporaryPhysics {
        public RestPhysics(Board board, double durationSec) { super(board, durationSec); }
        @Override public boolean canCapture() { return false; }
        @Override public boolean isMovementBlocker() { return true; }
    }
}