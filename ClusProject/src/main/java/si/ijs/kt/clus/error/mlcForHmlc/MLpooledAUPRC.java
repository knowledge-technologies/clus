package si.ijs.kt.clus.error.mlcForHmlc;

import java.io.Serializable;

public class MLpooledAUPRC extends MLROCAndPRCurve implements MlcHmlcSubError, Serializable {
    private final CurveType m_Measure = CurveType.pooledAUPRC;

    public MLpooledAUPRC(int dim) {
        super(dim);
    }

    @Override
    public double getModelError(int dim) {
        return super.getCurveError(m_Measure);
    }

    @Override
    public String getName() {
        return "pooledAUPRC";
    }

    @Override
    public void add(MlcHmlcSubError other) {
        super.add(other);
    }
}
