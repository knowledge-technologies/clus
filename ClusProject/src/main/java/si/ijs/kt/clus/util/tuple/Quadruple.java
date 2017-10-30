package si.ijs.kt.clus.util.tuple;

/**
 * Implements 4-Tuple.
 * 
 * @author matejp
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 */
public class Quadruple<T1, T2, T3, T4> {	

    private T1 m_First;
    private T2 m_Second;
    private T3 m_Third;
    private T4 m_Fourth;

    public Quadruple(T1 first, T2 second, T3 third, T4 fourth) {
        m_First = first;
        m_Second = second;
        m_Third = third;
        m_Fourth = fourth;
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
    
    public T4 getFourth(){
        return m_Fourth;
    }
}
