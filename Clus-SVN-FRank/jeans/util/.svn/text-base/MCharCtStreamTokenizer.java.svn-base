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

package jeans.util;

import java.io.*;

public class MCharCtStreamTokenizer extends MStreamTokenizer {

	protected long m_charNo = 0, m_charModulo = 1;
	protected CallBackFunction m_callback = null;

	public MCharCtStreamTokenizer(Reader myreader) {
		super(myreader);
	}

	public void setCallbackFunction(long modulo, CallBackFunction callback) {
		m_charModulo = modulo;
		m_callback = callback;
	}

	public long getCharNo() {
		return m_charNo;
	}

/*	public int readChar() throws IOException {
		if (pushbackchar == -1) {
			int ch = reader.read();
			m_charNo++;
			if (m_callback != null &&
				(m_charNo % m_charModulo == 0)) m_callback.callBackFunction(this);
			if (ch == 10 && prevcr == false) lineno++;
			prevcr = (ch == 13);
			if (prevcr) lineno++;
			return ch;
		} else {
			int betw = pushbackchar;
			pushbackchar = -1;
			return betw;
		}
		return 0;
	}		*/
}
