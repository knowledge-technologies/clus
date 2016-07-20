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

package jeans.graph.swing;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import jeans.graph.PercentLayout;
import jeans.util.PropertyInterface;

public class JIntegerPropertyControl extends JPanel implements ActionListener {

	public final static long serialVersionUID = 1;

	private PropertyInterface props;
	private int prop, delta;
	private Object backup;
	private JTextField field;

	public JIntegerPropertyControl(PropertyInterface props, int prop, String label, int wd, int delta, int align) {
		this.props = props;
		this.prop = prop;
		this.delta = delta;
		backup = props.getProperty(prop);
		setLayout(new PercentLayout("100% p p",3,0,false));
		add(new JLabel(label,align));
		add(field = new JTextField(wd));
		add(makeSpins());
		updateField();
	}

	public void updateField() {
		field.setText(props.getProperty(prop).toString());
	}

	public void updateProperty() throws NumberFormatException {
		props.setIntegerProperty(prop, Integer.parseInt(field.getText()));
	}

	public void restoreProperty() {
		props.setProperty(prop, backup);
	}

	public void increase(int delta) {
		try {
			int value = Integer.parseInt(field.getText()) + delta;
			props.setIntegerProperty(prop, value);
		} catch (NumberFormatException e) {}
		updateField();
	}


	private JPanel makeSpins() {
		JPanel spin = new JPanel();
		spin.setLayout(new GridLayout(1,2,3,3));
		JButton button = new JButton("<");
		button.addActionListener(this);
		spin.add(button);
		button = new JButton(">");
		button.addActionListener(this);
		spin.add(button);
		return spin;
	}

	public void actionPerformed(ActionEvent event) {
		JButton source = (JButton)event.getSource();
		if (source.getText().equals("<")) {
			increase(-delta);
		} else {
			increase(+delta);
		}
	}
}



