package physics.IdlePhysics;

import board.Board;
import classes.Command;
import classes.Pair;
import enums.EState;
import physics.Physics;

import java.util.List;

public class StaticTemporaryPhysics extends Physics {
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
            return new Command(nowMs, null, EState.DONE, List.of());
        }
        return null;
    }

    public double getDurationSec() { return durationSec; }
}
