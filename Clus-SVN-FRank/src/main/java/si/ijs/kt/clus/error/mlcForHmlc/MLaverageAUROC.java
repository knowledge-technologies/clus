package si.ijs.kt.clus.error.mlcForHmlc;

public class MLaverageAUROC extends MLROCAndPRCurve implements MlcHmlcSubError {
    private final int m_Measure = averageAUROC;

    public MLaverageAUROC(int dim) {
        super(dim);
    }
    @Override
    public double compute(int dim) {
        return getModelError(m_Measure);
    }

    @Override
    public String getName() {
        return "averageAUROC";
    }

}
