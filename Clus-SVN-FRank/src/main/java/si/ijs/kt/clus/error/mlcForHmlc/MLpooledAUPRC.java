package si.ijs.kt.clus.error.mlcForHmlc;

public class MLpooledAUPRC extends MLROCAndPRCurve implements MlcHmlcSubError {
    private final int m_Measure = pooledAUPRC;

    public MLpooledAUPRC(int dim) {
        super(dim);
    }

    @Override
    public double compute(int dim) {
        return getModelError(m_Measure);
    }

    @Override
    public String getName() {
        return "pooledAUPRC";
    }

}
