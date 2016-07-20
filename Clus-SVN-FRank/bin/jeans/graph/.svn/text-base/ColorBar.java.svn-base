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
import java.awt.event.*;
import jeans.util.CallBackFunction;

/** This class encapsulates the controls which manage one color. Is compound of
  * a Scrollbar and two TextFields. The Tree represent the same value,
  * graphically, Hex. value and Dec. value.
  **/

public class ColorBar extends Panel {

	public final static long serialVersionUID = 1;

	Scrollbar sbCol;
	CallBackFunction chvalue;
        Label laCol = new Label();
        TextField tfDec = new TextField("FF");
        TextField tfHex = new TextField("255");

        public ColorBar(CallBackFunction modified, String label, boolean horiz) {
		chvalue = modified;
                laCol.setText(label);
		Panel sPanel = new Panel();
		sPanel.setLayout(new PercentLayout("50%d p 50%d",2,0,false));
		if (horiz) {
			sbCol = new Scrollbar(Scrollbar.HORIZONTAL, 255, 8, 0, 255);
			sPanel.add(sbCol);
			setLayout(new PercentLayout("p 100% p p",2,0,false));
	                add(laCol);
		        add(sPanel);
                	add(tfHex);
			add(tfDec);
		} else {
			sbCol = new Scrollbar(Scrollbar.VERTICAL, 255, 8, 0, 255);
			sPanel.add(sbCol);
			setLayout(new PercentLayout("p p p 100%",2,0,true));
	                add(laCol);
                	add(tfHex);
			add(tfDec);
		        add(sPanel);
		}
		sbCol.addAdjustmentListener(new CAdjustmentListener());
		ActionListener action_listener = new CActionListener();
		tfDec.addActionListener(action_listener);
		tfHex.addActionListener(action_listener);
	}

	public void setColor(Color col) {
		sbCol.setBackground(col);
	}

	public void saveValue(int val) {
                tfHex.setText(convHex(val));
                tfDec.setText(Integer.toString(val));
	}

        public int getValue() {
		return sbCol.getValue();
	}

        public void setValue(int value) {
		sbCol.setValue(value);
		saveValue(value);
	}

	public static String convHex(int value) {
                String aux = Integer.toString(value,16).toUpperCase();
                if (aux.length() == 1) aux = "0" + aux;
                return aux;
        }

	private class CAdjustmentListener implements AdjustmentListener {

		public void adjustmentValueChanged(AdjustmentEvent e) {
                        int val = sbCol.getValue();
			saveValue(val);
			chvalue.callBackFunction(this);
		}

	}

	private class CActionListener implements ActionListener {

		public void actionPerformed(ActionEvent ev) {
			TextField fld = (TextField)ev.getSource();
			try {
	                        int value = Integer.parseInt(fld.getText(), fld == tfDec ? 10 : 16);
        	                if (value >= 0 && value <= 255) sbCol.setValue(value);
				chvalue.callBackFunction(this);
			} catch (NumberFormatException e) {
				saveValue(getValue());
			}
		}

	}
}

