package physics.IdlePhysics;

import board.Board;

public class RestPhysics extends StaticTemporaryPhysics {
    public RestPhysics(Board board, double durationSec) { super(board, durationSec); }
    @Override public boolean canCapture() { return false; }
    @Override public boolean isMovementBlocker() { return true; }
}