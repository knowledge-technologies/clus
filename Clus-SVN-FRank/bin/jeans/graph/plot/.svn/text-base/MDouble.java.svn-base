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

package jeans.graph.plot;

import jeans.util.*;

import java.awt.*;

public class MDouble {

	public final static int RND_FLOOR = 0;
	public final static int RND_CEIL = 1;
	public final static int RND_ROUND = 2;

	protected final static double[] vb =
		{12, 1.2, 3, 21, 0.01, 1500, 1350, 0.2654};

	protected int m_iNbDigits = 2;
	protected double m_fReal, m_fDigit;
	protected int m_iPower;

	public void setRounding(int nb) {
		m_iNbDigits = nb;
	}

	public void setFloorValue(double value) {
		setValue(value, RND_FLOOR);
	}

	public void setCeilValue(double value) {
		setValue(value, RND_CEIL);
	}

	public void setRoundValue(double value) {
		setValue(value, RND_ROUND);
	}

	public int getWidth(FontMetrics fm) {
		String digit = double2String(m_fDigit);
		int wd = fm.stringWidth(digit);
		if (m_iPower != 0) {
			wd += fm.stringWidth(".10");
			wd += fm.stringWidth(String.valueOf(m_iPower));
		}
		return wd;
	}

	public void draw(FontMetrics fm, Graphics g, int x, int y) {
		String digit = double2String(m_fDigit);
		int ypos = y + fm.getMaxAscent();
		g.drawString(digit, x, ypos);
		if (m_iPower != 0) {
			x += fm.stringWidth(digit);
			g.drawString(".", x, ypos-3);
			x += fm.charWidth('.');
			g.drawString("10", x, ypos);
			x += fm.stringWidth("10");
			String pow = String.valueOf(m_iPower);
			g.drawString(pow, x, ypos-4);
		}
	}

	public static void killZeros(StringBuffer buffer) {
		int len = buffer.length();
		int pos = len-1;
		while (pos > 0 && buffer.charAt(pos) == '0') pos--;
		if (pos != len-1) {
			if (pos != 0 && buffer.charAt(pos) != '.') pos++;
			buffer.delete(pos, len);
		}
	}

	public String double2String(double value) {
		return StringUtils.roundDouble(m_fReal,2);
/*		int nb = getNbDigits(value);
		if (nb > 0) {
			double res = value / pow10(nb - 1);
			int dotpos = nb-1;
			StringBuffer str = new StringBuffer();
			if (res < 0.0) {
				str.append('-');
				res = -res;
			}
			for (int i = 0; i < m_iNbDigits; i++) {
				int digit = (int)Math.floor(res+1e-7);
				res = (res-digit)*10;
				str.append((char)('0'+digit));
				if (i == dotpos && i != m_iNbDigits-1) str.append('.');
			}
			if (dotpos < m_iNbDigits-1) {
				killZeros(str);
			} else {
				for (int i = 0; i < Math.max(0, nb-m_iNbDigits); i++)
					str.append('0');
			}
			return str.toString();
		} else if (nb < 0) {
			double res = value * pow10(-nb);
			StringBuffer str = new StringBuffer();
			if (res < 0.0) {
				str.append('-');
				res = -res;
			}
			str.append("0.");
			for (int i = 0; i < -nb-1; i++)
				str.append('0');
			for (int i = 0; i < m_iNbDigits; i++) {
				int digit = (int)Math.floor(res+1e-7);
				res = (res-digit)*10;
				str.append((char)('0'+digit));
			}
			killZeros(str);
			return str.toString();
		} else {
			return "0";
		}*/
	}

	public void setValue(double value, int mode) {
		m_fReal = value;
/*
		int nb = getNbDigits(value);

		double sign = 1.0;
		if (value < 0.0) {
			value = -value;
			sign = -1.0;
			if (mode == RND_FLOOR) mode = RND_CEIL;
			else if (mode == RND_CEIL) mode = RND_FLOOR;
		}

		if (nb > 0) {
			value /= pow10(nb - m_iNbDigits);
			m_fDigit = round(value, mode) / pow10(m_iNbDigits-1);
			m_iPower = nb - 1;
		} else if (nb < 0) {
			value = value*pow10(m_iNbDigits - nb - 1)+1e-5;
			m_fDigit = round(value, mode) / pow10(m_iNbDigits-1);
			m_iPower = nb;
		} else {
			m_fDigit = 0.0;
			m_iPower = 0;
		}

		if (m_iPower == 1) {
			m_iPower = 0;
			m_fDigit *= 10.0;
		}
		if (m_iPower == -1) {
			m_iPower = 0;
			m_fDigit /= 10.0;
		}

		m_fDigit *= sign;
		m_fReal = m_fDigit*pow10(m_iPower);
*/

	}

	public static double round(double value, int mode) {
		switch (mode) {
			case RND_FLOOR:
				return Math.floor(value);
			case RND_CEIL:
				return Math.ceil(value);
			case RND_ROUND:
				return Math.round(value);
		}
		return 0.0;
	}

	public static double pow10(int nb) {
		if (nb > 0) {
			double res = 10.0;
			while (nb > 1) {
				res *= 10.0;
				nb--;
			}
			return res;
		} else if (nb < 0) {
			double res = 0.1;
			while (nb < -1) {
				res /= 10.0;
				nb++;
			}
			return res;
		} else {
			return 1.0;
		}
	}

	public static int getNbDigits(double number) {
		double val = Math.abs(number);
		if (val == 0.0f) return 0;
		if (val >= 1.0f) return (int)Math.floor(Math.log(val)/Math.log(10f)+1e-7)+1;
		else return -(int)Math.ceil(-Math.log(val)/Math.log(10f)-1e-7);
	}

	public float getFloat() {
		return (float)m_fReal;
	}

	public double getDigit() {
		return m_fDigit;
	}

	public int getPower() {
		return m_iPower;
	}

	public static void main(String[] args) {
		MDouble fl = new MDouble();
		for (int i = 0; i < vb.length; i++)
			System.out.println("double "+vb[i]+" "+fl.double2String(vb[i]));

	}
}
