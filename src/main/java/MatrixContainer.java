import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.inverse.InvertMatrix;

public class MatrixContainer {

    private static INDArray F = null;
    private static INDArray dX = null;
    private static INDArray dY = null;
    private static INDArray Ax = null;
    private static INDArray Ay = null;

    public static void setF(INDArray F) {
        MatrixContainer.F = F;
        if (Ax == null && dX != null) {
            calculateAx();
        }
        if (Ay == null && dY != null) {
            calculateAy();
        }
    }

    public static void setdX(INDArray dX) {
        MatrixContainer.dX = dX;
        if (Ax == null && F != null) {
            calculateAx();
        }
    }

    public static void setdY(INDArray dY) {
        MatrixContainer.dY = dY;
        if (Ay == null && F != null) {
            calculateAy();
        }
    }

    private static void calculateAx() {
        Ax = F.transpose().mmul(F);
        Ax = InvertMatrix.invert(Ax, false);
        Ax = Ax.mmul(F.transpose()).mmul(dX);
    }

    private static void calculateAy() {
        Ay = F.transpose().mmul(F);
        Ay = InvertMatrix.invert(Ay, false);
        Ay = Ay.mmul(F.transpose()).mmul(dY);
    }

    public static INDArray getAx() {
        return Ax;
    }

    public static INDArray getAy() {
        return Ay;
    }
}
