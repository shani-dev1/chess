package physics.IdlePhysics;

import board.Board;
import classes.Command;
import classes.Pair;
import enums.EState;
import physics.Physics;

import java.util.List;

public class MovePhysics extends Physics {
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
            return new Command(nowMs, null, EState.DONE, List.of());
        }
        return null;
    }

    public double getSpeedCellsPerSec() { return param; }
}