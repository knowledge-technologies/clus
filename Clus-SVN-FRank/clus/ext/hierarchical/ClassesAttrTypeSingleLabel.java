
	/*************************************************************************
	 * Clus - Software for Predictive Clustering                             *
	 * Copyright (C) 2007                                                    *
	 *    Katholieke Universiteit Leuven, Leuven, Belgium                    *
	 *    Jozef Stefan Institute, Ljubljana, Slovenia                        *
	 *                                                                       *
	 * This program is free software: you can redistribute it and/or modify  *
	 * it under the terms of the GNU General Public License as published by  *
	 * the Free Software Foundation, either version 3 of the License, or     *
	 * (at your option) any later version.                                   *
	 *                                                                       *
	 * This program is distributed in the hope that it will be useful,       *
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
	 * GNU General Public License for more details.                          *
	 *                                                                       *
	 * You should have received a copy of the GNU General Public License     *
	 * along with this program.  If not, see <http://www.gnu.org/licenses/>. *
	 *                                                                       *
	 * Contact information: <http://www.cs.kuleuven.be/~dtai/clus/>.         *
	 *************************************************************************/

	package clus.ext.hierarchical;

	import jeans.util.array.*;

	import java.io.*;
	import java.util.*;

	import clus.io.*;
	import clus.main.*;
	import clus.util.*;
	import clus.data.type.*;
	import clus.data.io.ClusReader;
import clus.data.rows.*;

	public class ClassesAttrTypeSingleLabel extends ClassesAttrType {

		public final static long serialVersionUID = Settings.SERIAL_VERSION_ID;

		public ClassesAttrTypeSingleLabel(String name) {
			super(name);
		}

		public ClassesAttrTypeSingleLabel(String name, ClassHierarchy hier) {
			super(name,hier);
		}
		
		public ClassesAttrTypeSingleLabel(String name, String atype) {
			super(name,atype);
		}
		
		
		public ClusAttrType cloneType() {
			ClassesAttrTypeSingleLabel at = new ClassesAttrTypeSingleLabel(m_Name, m_Hier);
			cloneType(at);
			return at;
		}
				
		public void updatePredictWriterSchema(ClusSchema schema) {
			String name = getName();
			schema.addAttrType(new StringAttrType(name+"-a"));
		}
		
		public String getPredictionWriterString(DataTuple tuple) {
			StringBuffer buf = new StringBuffer();
			buf.append(getString(tuple));
			return buf.toString();
		}
		
		
		
	
}
