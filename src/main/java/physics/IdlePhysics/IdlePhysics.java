package physics.IdlePhysics;

import board.Board;
import classes.Command;
import classes.Pair;
import physics.Physics;

public class IdlePhysics extends Physics {
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
