package clus.ext.hierarchical.mlcForHmlc;

public class MLaverageAUPRC extends MLROCAndPRCurve implements MlcHmlcSubError {
    private final int m_Measure = averageAUPRC;
    
    public MLaverageAUPRC(int dim) {
        super(dim);
    }    

    @Override
    public double compute(int dim) {
        return getModelError(m_Measure);
    }

    @Override
    public String getName() {
        return "averageAUPRC";
    }
}
