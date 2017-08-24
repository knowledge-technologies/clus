package si.ijs.kt.clus.util.jeans.util;

/**
 * Implements 2-Tuple.
 * 
 * @author matejp
 *
 * @param <T1>
 * @param <T2>
 */
public class Pair<T1, T2> {
    private T1 m_First;
    private T2 m_Second;

    public Pair(T1 first, T2 second) {
        m_First = first;
        m_Second = second;
    }
    
    public T1 getFirst(){
        return m_First;
    }
    
    public T2 getSecond(){
        return m_Second;
    }

}
