package clus.ext.hierarchical.mlcForHmlc;

public interface MlcHmlcSubError {
    public double compute(int dim);
    
    public void addExample(boolean[] actual, boolean[] predictedThresholded);
    
    public String getName();
}
