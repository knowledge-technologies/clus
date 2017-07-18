
package si.ijs.kt.clus.util;

import java.util.Random;


public class ClusRandomNonstatic {

    public final static int RANDOM_EXTRATREE = 0;
    public final static int RANDOM_SAMPLE = 1;
    public final static int RANDOM_INT_RANFOR_TREE_DEPTH = 2;
    public final static int RANDOM_SELECTION = 3;
    public final static int RANDOM_SEED = 4;

    private Random[] m_Random;
    private int m_Length = 5;
    private int m_Seed;

    
    public ClusRandomNonstatic(int seed) {
        m_Seed = seed; // remember the initial seed
        m_Random = new Random[m_Length];
        Random seedGenerator = new Random(seed);
        for (int i = 0; i < m_Length; i++) {
            m_Random[i] = new Random(seedGenerator.nextInt());
        }
    }


    public int nextInt(int which, int max) {
        return m_Random[which].nextInt(max);
    }


    public int nextInt(int which) {
        return m_Random[which].nextInt();
    }


    public double nextDouble(int which) {
        return m_Random[which].nextDouble();
    }


    public Random getRandom(int idx) {
        return m_Random[idx];
    }
    
    public int getSeed(){
    	return m_Seed;
    }

}
