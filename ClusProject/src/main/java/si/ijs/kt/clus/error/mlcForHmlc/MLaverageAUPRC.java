package si.ijs.kt.clus.error.mlcForHmlc;

public class MLaverageAUPRC extends MLROCAndPRCurve implements MlcHmlcSubError {
    private final CurveType m_Measure = CurveType.averageAUPRC;
    
    public MLaverageAUPRC(int dim) {
        super(dim);
    }    

    @Override
    public double getModelError(int dim) {
        return super.getCurveError(m_Measure);
    }

    @Override
    public String getName() {
        return "averageAUPRC";
    }

    @Override
    public void add(MlcHmlcSubError other) {
        super.add(other);        
    }
}
