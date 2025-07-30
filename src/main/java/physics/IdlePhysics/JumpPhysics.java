package physics.IdlePhysics;

import board.Board;

public class JumpPhysics extends StaticTemporaryPhysics {
    public JumpPhysics(Board board, double durationSec) { super(board, durationSec); }
    @Override public boolean canBeCaptured() { return false; }
}