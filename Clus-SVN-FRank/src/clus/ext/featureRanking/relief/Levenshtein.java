
package clus.ext.featureRanking.relief;

import clus.data.rows.DataTuple;
import clus.data.type.StringAttrType;


public class Levenshtein {

    String m_str1, m_str2;
    int m_len1, m_len2;
    double[] m_memo;
    double m_dist = -1.0;
    double m_charDist = 1.0; // distance between two different characters


    public Levenshtein(String str1, String str2) {
        m_str1 = str1;
        m_str2 = str2;
        m_len1 = m_str1.length();
        m_len2 = m_str2.length();
        m_memo = new double[m_len1 + 1 + m_len2];

        if (m_len1 == 0 && m_len2 == 0) {
            m_dist = 0.0;
        }
        else if (m_len1 == 0 || m_len2 == 0) {
            m_dist = 1.0; // after normalization
        }
        else {
            computeDist();
            m_dist = m_memo[m_len2] / Math.max(m_len1, m_len2);
        }
    }


    public Levenshtein(DataTuple t1, DataTuple t2, StringAttrType attr) {
        this(attr.getString(t1), attr.getString(t2));
    }


    /**
     * Dynamically computes distance between the strings {@code m_str1} and {@code m_str2} and needs only O(
     * {@code m_len1 + m_len2}) space.
     */
    public void computeDist() {
        for (int i = m_len1 - 1; i >= 0; i--) {
            m_memo[i] = m_len1 - i;
        }
        for (int i = m_len1 + 1; i < m_memo.length; i++) {
            m_memo[i] = i - m_len1;
        }
        int processed = 0;
        int ind;
        int place1 = processed, place2 = processed;
        boolean nextRound = true;
        while (nextRound) {
            m_memo[m_len1] = Math.min(Math.min(m_memo[m_len1 - 1] + 1.0, m_memo[m_len1 + 1] + 1.0), m_memo[m_len1] + (m_str1.charAt(place1) == m_str2.charAt(place2) ? 0.0 : m_charDist));
            for (int i = processed + 1; i < m_len1; i++) {
                ind = m_len1 - (i - processed);
                m_memo[ind] = Math.min(Math.min(m_memo[ind - 1] + 1.0, m_memo[ind + 1] + 1.0), m_memo[ind] + (m_str1.charAt(i) == m_str2.charAt(place2) ? 0.0 : m_charDist));
            }
            for (int j = processed + 1; j < m_len2; j++) {
                ind = m_len1 + (j - processed);
                m_memo[ind] = Math.min(Math.min(m_memo[ind - 1] + 1.0, m_memo[ind + 1] + 1.0), m_memo[ind] + (m_str1.charAt(place1) == m_str2.charAt(j) ? 0.0 : m_charDist));
            }
            if (processed < m_len1 - 1)
                place1++;
            if (processed < m_len2 - 1)
                place2++;
            processed++;
            nextRound = m_len2 > processed || m_len1 > processed;
        }
    }


    public double getDist() {
        return m_dist;
    }
}
