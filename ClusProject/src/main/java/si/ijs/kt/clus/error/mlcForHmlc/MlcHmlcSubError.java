package si.ijs.kt.clus.error.mlcForHmlc;

public interface MlcHmlcSubError {
    public double compute(int dim);
    
    public void addExample(boolean[] actual, double[] predicted, boolean[] predictedThresholded);
    
    public String getName();
}
