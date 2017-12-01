package si.ijs.kt.clus.algo.kNN.methods;

import si.ijs.kt.clus.data.rows.DataTuple;

public class NN {

    private DataTuple m_Tuple;
    private double m_Distance;


    public NN(DataTuple tuple, double dist) {
        m_Tuple = tuple;
        m_Distance = dist;
    }


    public DataTuple getTuple() {
        return m_Tuple;
    }


    public double getDistance() {
        return m_Distance;
    }
}
