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

package jeans.graph;

import java.awt.*;
import jeans.util.CallBackFunction;

public class ColorSelector extends Panel implements CallBackFunction {

	public final static long serialVersionUID = 1;

	CallBackFunction call_back = null;
	ColorBar red, green, blue;

        public ColorSelector(CallBackFunction modified, boolean horiz) {
		call_back = modified;
		if (horiz) setLayout(new GridLayout(0,1));
		else setLayout(new GridLayout(1,0));
		add(red = new ColorBar(this,"R",horiz));
		add(green = new ColorBar(this,"G",horiz));
		add(blue = new ColorBar(this,"B",horiz));
		red.setColor(Color.red);
		green.setColor(Color.green);
		blue.setColor(Color.blue);
	}

	public void setColor(int red, int green, int blue) {
		this.red.setValue(red);
		this.green.setValue(green);
		this.blue.setValue(blue);
	}

	public void setColor(Color color) {
		setColor(color.getRed(), color.getGreen(), color.getBlue());
	}

	public Color getColor() {
		int red = this.red.getValue();
		int green = this.green.getValue();
		int blue = this.blue.getValue();
		return new Color(red, green, blue);
	}

	public void callBackFunction(Object obj) {
		if (call_back != null)
			call_back.callBackFunction(obj);
	}

}

