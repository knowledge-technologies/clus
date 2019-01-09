package si.ijs.kt.clus.error.mlcForHmlc;

import java.io.Serializable;

public class MLaverageAUROC extends MLROCAndPRCurve implements MlcHmlcSubError, Serializable {
    private final CurveType m_Measure = CurveType.averageAUROC;

    public MLaverageAUROC(int dim) {
        super(dim);
    }
    @Override
    public double getModelError(int dim) {
        return super.getCurveError(m_Measure);
    }

    @Override
    public String getName() {
        return "averageAUROC";
    }
    @Override
    public void add(MlcHmlcSubError other) {
        super.add(other);
    }
}
