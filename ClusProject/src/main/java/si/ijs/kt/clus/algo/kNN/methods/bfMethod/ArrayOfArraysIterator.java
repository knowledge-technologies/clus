package si.ijs.kt.clus.algo.kNN.methods.bfMethod;

import java.util.Iterator;

import si.ijs.kt.clus.data.rows.DataTuple;


public class ArrayOfArraysIterator<T> implements Iterable<T> {
	
	private T[][] m_Array2D;
	private int m_CurrentArray;
	private int m_CurrentPosition;

	public ArrayOfArraysIterator(T[][] array2d) {
		m_Array2D = array2d;
		m_CurrentPosition = 0;
		m_CurrentArray = 0;
		firstNonempty();
	}
	
	@Override
	public Iterator<T> iterator() {
		Iterator<T> myIter = new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return m_CurrentArray < m_Array2D.length - 1 || m_CurrentArray == m_Array2D.length - 1 && m_CurrentPosition < m_Array2D[m_CurrentArray].length;
			}

			@Override
			public T next() {
				T item = m_Array2D[m_CurrentArray][m_CurrentPosition];				
				if (m_CurrentPosition == m_Array2D[m_CurrentArray].length - 1) {
					m_CurrentArray++;
					m_CurrentPosition = 0;
					firstNonempty();
				} else {
					m_CurrentPosition++;
				}
				return item;
			}
		};
		return myIter;
	}
	
	private void firstNonempty() {
		while (m_CurrentArray < m_Array2D.length && 0 == m_Array2D[m_CurrentArray].length) {
			m_CurrentArray++;
		}
	}

}
