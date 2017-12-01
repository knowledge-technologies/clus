package si.ijs.kt.clus.error.mlcForHmlc;

public class MLweightedAUPRC extends MLROCAndPRCurve implements MlcHmlcSubError {
    private final CurveType m_Measure = CurveType.weightedAUPRC;
    
    public MLweightedAUPRC(int dim) {
        super(dim);
    }

    @Override
    public double getModelError(int dim) {
        return super.getCurveError(m_Measure);
    }

    @Override
    public String getName() {
        return "weightedAUPRC";
    }

    @Override
    public void add(MlcHmlcSubError other) {
        super.add(other);
    }
}
