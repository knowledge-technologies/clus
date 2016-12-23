package clus.util;

import java.util.Random;

public class NonstaticRandom {
	public final static int RANDOM_EXTRATREE = 0;
	public final static int RANDOM_SAMPLE = 1;
	public final static int RANDOM_INT_RANFOR_TREE_DEPTH = 2;
	public final static int RANDOM_SELECTION = 3;
		
	private Random[] m_Random;
	private int m_Lenght = 4;
	
	public NonstaticRandom(int seed){
		m_Random = new Random[m_Lenght];
		Random seedGenerator = new Random(seed);
		for(int i = 0; i < m_Lenght; i++){
			m_Random[i] = new Random(seedGenerator.nextInt());
		}
	}
		
	public int nextInt(int which, int max) {
		return m_Random[which].nextInt(max);
	}
	
	public double nextDouble(int which) {
		return m_Random[which].nextDouble();
	}
	
	public Random getRandom(int idx) {
		return m_Random[idx];
	}

}
