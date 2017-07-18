package si.ijs.kt.clus.error.mlcForHmlc;

public class MLweightedAUPRC extends MLROCAndPRCurve implements MlcHmlcSubError {
    private final int m_Measure = weightedAUPRC;
    
    public MLweightedAUPRC(int dim) {
        super(dim);
    }

    @Override
    public double compute(int dim) {
        return getModelError(m_Measure);
    }

    @Override
    public String getName() {
        return "weightedAUPRC";
    }

}
