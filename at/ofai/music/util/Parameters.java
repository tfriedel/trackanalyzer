/*
	Copyright (C) 2001, 2006 by Simon Dixon

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along
	with this program (the file gpl.txt); if not, download it from
	http://www.gnu.org/licenses/gpl.txt or write to the
	Free Software Foundation, Inc.,
	51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package at.ofai.music.util;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import at.ofai.music.util.FrameMargins;

public class Parameters extends JDialog implements ActionListener {
	
	abstract class Value {
		protected JComponent component;
		abstract protected Object getValue();
		abstract protected void update();
	} // abstract class Value


	class ChoiceValue extends Value {

		String[] choices;
		int currentChoice;

		protected ChoiceValue(String[] values) { this(values, 0); }
		protected ChoiceValue(String[] values, int init) {
			choices = values;
			currentChoice = init;
			component = new JComboBox(values);
			((JComboBox)component).setSelectedIndex(currentChoice);
			component.setBackground(colors.getBackground());
			component.setForeground(colors.getForeground());
		} // constructor
		
		protected Object getValue() { return choices[currentChoice]; }
		public String toString() { return choices[currentChoice]; }
		
		protected void update() {
			int tmp = ((JComboBox)component).getSelectedIndex();
			if (tmp >= 0)
				currentChoice = tmp;
		} // update()

	} // class ChoiceValue

	
	class StringValue extends Value {
	
		String currentValue;
	
		protected StringValue() { this(""); }
		protected StringValue(String init) {
			currentValue = init;
			component = new JTextField(currentValue);
			component.setBackground(colors.getBackground());
			component.setForeground(colors.getForeground());
		} // constructor

		protected Object getValue() { return currentValue; }
		public String toString() { return currentValue; }

		protected void update() {
			currentValue = ((JTextField)component).getText();
		} // update()

	} // class StringValue


	class DoubleValue extends Value {
	
		double currentValue;

		protected DoubleValue() { this(0); }
		protected DoubleValue(double init) {
			currentValue = init;
			component = new JTextField(Double.toString(currentValue));
			component.setBackground(colors.getBackground());
			component.setForeground(colors.getForeground());
		} // constructor

		protected Object getValue() { return new Double(currentValue); }
		public String toString() { return "" + currentValue; }

		protected void update() {
			try {
				double tmp =
						Double.parseDouble(((JTextField)component).getText());
				currentValue = tmp;
			} catch (NumberFormatException e) {}
		} // update()

	} // class DoubleValue


	class IntegerValue extends Value {
	
		int currentValue;

		protected IntegerValue() { this(0); }
		protected IntegerValue(int init) {
			currentValue = init;
			component = new JTextField(Integer.toString(currentValue));
			component.setBackground(colors.getBackground());
			component.setForeground(colors.getForeground());
		} // constructor

		protected Object getValue() { return new Integer(currentValue); }
		public String toString() { return "" + currentValue; }

		protected void update() {
			try {
				int tmp = Integer.parseInt(((JTextField)component).getText());
				currentValue = tmp;
			} catch (NumberFormatException e) {}
		} // update()

	} // class IntegerValue


	class BooleanValue extends ChoiceValue {

		boolean currentValue;

		protected BooleanValue() { this(true); }
		protected BooleanValue(boolean init) {
			super(new String[]{"True", "False"}, init? 0: 1);
			currentValue = init;
		} // constructor

		protected Object getValue() { return new Boolean(currentValue); }
		public String toString() { return "" + currentValue; }

		protected void update() {
			super.update();
			currentValue = (currentChoice == 0);
		} // update()
			
	} // class BooleanValue


	protected ArrayMap map;
	protected Frame parent;
	protected JLabel[] keyFields;
	protected JComponent[] valueFields;
	protected int sz;
	protected Colors colors;
	protected JPanel panel1, panel2;
	protected JButton okButton, cancelButton;
	protected boolean cancelled;
	static final long serialVersionUID = 0;

	public Parameters(Frame f, String name) {
		this(f, name, new Colors() {
			public Color getBackground() { return Color.white; }
			public Color getForeground() { return Color.black; }
			public Color getButton()	 { return Color.white; }
			public Color getButtonText() { return Color.black; }
		});
	} // constructor

	public Parameters(Frame f, String name, Colors c) {
		super(f, name, true);
		colors = c;
		setLocationRelativeTo(f);
		Container pane = getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		panel1 = new JPanel();
		panel2 = new JPanel();
		pane.add(panel1);
		pane.add(panel2);
		panel1.setBackground(colors.getBackground());
		panel2.setBackground(colors.getBackground());
		getRootPane().setBorder(
					BorderFactory.createLineBorder(colors.getBackground(), 10));
		map = new ArrayMap();
		okButton = new JButton("OK");
		okButton.setBackground(colors.getButton());
		okButton.setForeground(colors.getButtonText());
		okButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.setBackground(colors.getButton());
		cancelButton.setForeground(colors.getButtonText());
		cancelButton.addActionListener(this);
		parent = f;
		cancelled = false;
		setVisible(false);
	} // constructor

	public void print() {
		sz = map.size();
		System.out.println("at.ofai.music.util.Parameters: size = " + sz);
		for (int i = 0; i < sz; i++) {
			ArrayMap.Entry e = map.getEntry(i);
			System.out.println(e.getKey() + " : " + e.getValue());
		}
	} // print()

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okButton) {
			for (int i = 0; i < sz; i++)
				((Value)map.getEntry(i).getValue()).update();
			cancelled = false;
		} else
			cancelled = true;
		setVisible(false);
	}

	public boolean wasCancelled() {
		return cancelled;
	}

	public void setVisible(boolean flag) {
		if (!flag) {
			super.setVisible(false);
			return;
		}
		sz = map.size();
		keyFields = new JLabel[sz];
		valueFields = new JComponent[sz];
		panel1.removeAll();
		panel2.removeAll();
		panel1.setLayout(new GridLayout(sz + 1, 1, 10, 5));
		panel2.setLayout(new GridLayout(sz + 1, 1, 10, 5));
		for (int i = 0; i < sz; i++) {
			ArrayMap.Entry e = map.getEntry(i);
			keyFields[i] = new JLabel((String) e.getKey());
			panel1.add(keyFields[i]);
			valueFields[i] = (JComponent) ((Value)e.getValue()).component;
			panel2.add(valueFields[i]);
		}
		panel1.add(okButton);
		panel2.add(cancelButton);
		pack();
		Dimension dim = getContentPane().getSize();
		Dimension margins = FrameMargins.get(false);
		int wd = dim.width + margins.width + 20;
		int ht = dim.height + margins.height + 20;
		int x = 0;
		int y = 0;
		if (parent != null) {
			x = parent.getLocation().x + (parent.getWidth() - wd) / 2;
			y = parent.getLocation().y + (parent.getHeight() - ht) / 2;
		}
	//	System.out.println("wd=" + wd + " ht=" + ht + " loc=" + x + "," + y);
	//  java version "1.3.0rc1" has bugs in location/size with fvwm2
	//	super.setLocation(-wd/2, -ht/2); // x, y);
		super.setLocation(x, y);
		super.setSize(wd, ht);
		super.setVisible(true);
	} // setVisible()

	public boolean contains(String key) {
		return map.containsKey(key);
	} // contains()

	public String getString(String key) {
		return ((StringValue)map.get(key)).currentValue;
	} // getString()
	
	public double getDouble(String key) {
		return ((DoubleValue)map.get(key)).currentValue;
	} // getDouble()
	
	public int getInt(String key) {
		return ((IntegerValue)map.get(key)).currentValue;
	} // getInt()

	public boolean getBoolean(String key) {
		return ((BooleanValue)map.get(key)).currentValue;
	} // getBoolean()

	public String getChoice(String key) {
		return (String) ((ChoiceValue)map.get(key)).getValue();
	} // getChoice()

	public void setString(String key, String value) {
		map.put(key, new StringValue(value));
	} // setString()

	public void setDouble(String key, double value) {
		map.put(key, new DoubleValue(value));
	} // setDouble()

	public void setInt(String key, int value) {
		map.put(key, new IntegerValue(value));
	} // setInt()

	public void setBoolean(String key, boolean value) {
		map.put(key, new BooleanValue(value));
	} // setBoolean()

	public void setChoice(String key, String[] choices, int value) {
		map.put(key, new ChoiceValue(choices, value));
	} // setChoice()

} // class Parameters
