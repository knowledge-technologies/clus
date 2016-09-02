package clus.algo.Relief;

import java.util.HashMap;

import clus.data.rows.DataTuple;
import clus.data.type.StringAttrType;

public class Levenshtein {
	String m_str1, m_str2;
	int m_len1, m_len2;
	int m_k; // needed for the bijective conversion of pairs of ints in computeDist to a single int
	HashMap<Integer, Double> m_memo;
	double m_dist = -1.0;
	double m_charDist = 1.0; // distance between two different characters
	
	public Levenshtein(DataTuple t1, DataTuple t2, StringAttrType attr){
    	m_str1 = attr.getString(t1);
    	m_str2 = attr.getString(t2);
    	m_len1 = m_str1.length();
    	m_len2 = m_str2.length();
    	HashMap<Integer, Double> memo = new HashMap<Integer, Double>();
    	m_k = m_len2; // if m_str2 is empty, computeDist will return len1 in the first step --> no problem 
    	
    	if(m_len1 == 0 && m_len2 == 0){
    		m_dist = 0.0;
    	} else if(m_len1 == 0 || m_len2 == 0){
    		m_dist = 1.0; // after normalization
    	} else{
    		m_dist = computeDist(m_len1 - 1, m_len2 - 1) / Math.max(m_len1, m_len2);
    	}
	}
	
	public double computeDist(int i, int j){
		int key = i * m_k + j;
		if (m_memo.containsKey(key)){
			return m_memo.get(key);
		} else if(i == 0){
			return ((double) j);
		} else if (j == 0){
			return ((double) i);
		} else{
			double ans = Math.min(computeDist(i - 1, j) + 1.0,
								  Math.min(computeDist(i, j - 1) + 1.0,
										   computeDist(i - 1, j - 1) + (m_str1.charAt(i) == m_str2.charAt(j) ? 0.0 : m_charDist)));
			m_memo.put(key, ans);
			return ans;
		}
	}
	public double getDist(){
		return m_dist;
	}
}
