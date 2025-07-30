package physics;

import board.Board;
import classes.Command;
import classes.Pair;

public abstract class Physics {

    protected final Board board;
    protected Pair startCell;
    protected Pair endCell;
    protected double[] currPosM;        // (x,y) in metres
    protected final double param;       // generic parameter (speed or duration)
    public long startMs;

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
}