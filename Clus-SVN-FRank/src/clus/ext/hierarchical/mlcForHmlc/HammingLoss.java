package clus.ext.hierarchical.mlcForHmlc;

public class HammingLoss implements MlcHmlcSubError{
    private int m_NbWrong;
    private int m_NbKnown;
    
    public HammingLoss(){
        m_NbWrong = 0;
        m_NbKnown = 0;
    }
    
    public double compute(int dimensions){
        return ((double) m_NbWrong) / m_NbKnown / dimensions;
    }
    
    public void addExample(boolean[] actual, boolean[] predictedThresholded){
        for(int i = 0; i < actual.length; i++){
            if(actual[i] != predictedThresholded[i]){
                m_NbWrong += 1;
            }
        }
        m_NbKnown++;
    }
    
    public String getName(){
        return "HammingLoss";
    }
}
