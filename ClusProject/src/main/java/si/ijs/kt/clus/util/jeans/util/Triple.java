package si.ijs.kt.clus.util.jeans.util;

/**
 * Implements 2-Tuple.
 * 
 * @author matejp
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 */
public class Triple<T1, T2, T3> {
    private T1 m_First;
    private T2 m_Second;
    private T3 m_Third;

    public Triple(T1 first, T2 second, T3 third) {
        m_First = first;
        m_Second = second;
        m_Third = third;
    }
    
    public T1 getFirst(){
        return m_First;
    }
    
    public T2 getSecond(){
        return m_Second;
    }
    
    public T3 getThird(){
        return m_Third;
    }

}
