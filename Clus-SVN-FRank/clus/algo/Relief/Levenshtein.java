package clus.algo.Relief;

import java.util.HashMap;

import clus.data.rows.DataTuple;
import clus.data.type.StringAttrType;

public class Levenshtein {
	String m_str1, m_str2;
	int m_len1, m_len2;
	int m_k; // needed for the bijective conversion of pairs of ints in computeDist to a single int
	double[][] m_memo;
	double m_dist = -1.0;
	double m_charDist = 1.0; // distance between two different characters
	
	public Levenshtein(String str1, String str2){
    	m_str1 = str1;
    	m_str2 = str2;
    	m_len1 = m_str1.length();
    	m_len2 = m_str2.length();
    	m_memo = new double[m_len1 + 1][m_len2 + 1];
    	m_k = m_len2; // if m_str2 is empty, computeDist will return len1 in the first step --> no problem 
    	
    	if(m_len1 == 0 && m_len2 == 0){
    		m_dist = 0.0;
    	} else if(m_len1 == 0 || m_len2 == 0){
    		m_dist = 1.0; // after normalization
    	} else{
    		computeDist();
    		m_dist = m_memo[m_len1][m_len2] / Math.max(m_len1, m_len2);
    	}		
	}
	public Levenshtein(DataTuple t1, DataTuple t2, StringAttrType attr){
		this(attr.getString(t1), attr.getString(t2));
	}

	public void computeDist(){
		for(int j = 0; j < m_len2; j++){
			m_memo[0][j] = j;
		}
		for(int i = 0; i < m_len1; i++){
			m_memo[i][0] = i;
		}
		for(int i = 1; i < m_len1 + 1; i++){
			for(int j = 1; j < m_len2 + 1; j++){
				double ans = Math.min(m_memo[i - 1][j] +  1.0,
						  			  Math.min(m_memo[i][j - 1] + 1.0,
						  					   m_memo[i - 1][j - 1] + (m_str1.charAt(i - 1) == m_str2.charAt(j - 1) ? 0.0 : m_charDist)));
				m_memo[i][j] = ans;
			}
		}
	}
	public double getDist(){
		return m_dist;
	}
}
