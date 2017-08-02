
package si.ijs.kt.clus.data.rows;

import si.ijs.kt.clus.util.jeans.util.compound.DoubleObject;
import si.ijs.kt.clus.util.jeans.util.sort.MSortable;


public class RowDataSortHelper implements MSortable {

    protected int m_NbRows = 0;
    public DataTuple[] missing;
    public DataTuple[] zero;
    public DoubleObject[] other;


    public void resize(int nbrows) {
        if (nbrows > m_NbRows) {
            missing = new DataTuple[nbrows];
            zero = new DataTuple[nbrows];
            DoubleObject[] new_other = new DoubleObject[nbrows];
            if (m_NbRows > 0) {
                System.arraycopy(other, 0, new_other, 0, m_NbRows);
            }
            for (int i = m_NbRows; i < nbrows; i++) {
                new_other[i] = new DoubleObject();
            }
            other = new_other;
            m_NbRows = nbrows;
        }
    }


    @Override
    public double getDouble(int i) {
        return other[i].getValue();
    }


    @Override
    public void swap(int i, int j) {
        DoubleObject obj_i = other[i];
        other[i] = other[j];
        other[j] = obj_i;
    }
}
