/*  BeatRoot: An interactive beat tracking system
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
	http://www.gnu.org/licenses/gpl.txt or write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package at.ofai.music.beatroot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JTextField;

/** The panel at the bottom of BeatRoot's GUI containing the buttons */
class ControlPanel extends JPanel {

	static final long serialVersionUID = 0; // avoid compiler warning

	/** The listener object which handles key, button and menu events */
	EventProcessor jobq;

	/** For layout of the grid of buttons (i.e. not including the zoom buttons) */
	class ButtonPanel extends JPanel {
		static final long serialVersionUID = 0; // avoid compiler warning

		/**
		 * Constructor:
		 * 
		 * @param r
		 *            Number of rows of buttons
		 * @param c
		 *            NUmber of columns of buttons
		 */
		public ButtonPanel(int r, int c) {
			super(new GridLayout(r, c));
		} // constructor

		/**
		 * Adds a button to this panel.
		 * 
		 * @param s
		 *            The text on the button
		 * @param al
		 *            The event handler for when the button is pressed
		 */
		public void addButton(String s, ActionListener al) {
			JButton b = new JButton(s);
			b.addActionListener(al);
			add(b);
		} // addButton()
	} // inner class ButtonPanel

	/**
	 * Constructor:
	 * 
	 * @param displayPanel
	 *            The main panel of BeatRoot's GUI
	 * @param scroller
	 *            The scrollbar on BeatRoot's GUI
	 * @param args
	 *            Not used - was used for a file name in BeatRoot 0.4 for
	 *            experiments reported in Dixon, Goebl and Cambouropoulos, Music
	 *            Perception, 2006
	 * @param j
	 *            The object which handles button, key and menu events
	 */
	public ControlPanel(BeatTrackDisplay displayPanel, JScrollBar scroller,
			String args, EventProcessor j) {
		setPreferredSize(new Dimension(1000, 100));
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.black, 5));

		//JLabel fileName = new JLabel(); // "File: <none>");
		JTextField argumentField = new JTextField(24);
		jobq = j;

		ButtonPanel buttonPanel;
		if ((args != null) && args.endsWith(".tmf")) { // never happens
			buttonPanel = new ButtonPanel(3, 2);
			buttonPanel.addButton("New", jobq);
			buttonPanel.addButton(GUI.PLAY, jobq);
			buttonPanel.addButton(GUI.CLEAR_BEATS, jobq);
			buttonPanel.addButton(GUI.STOP, jobq);
			buttonPanel.addButton(GUI.SAVE_BEATS, jobq);
		} else { // always happens
			Box optionsBox = new Box(BoxLayout.X_AXIS);
			optionsBox.add(new JLabel("Options: "));
			optionsBox.add(argumentField);

			int upperInitValue = 34;
			int lowerInitValue = 84;
			JTextField upperParamText = new JTextField(Integer.toString(upperInitValue),4);
			upperParamText.setEditable(false);
			JTextField lowerParamText = new JTextField(Integer.toString(lowerInitValue),4);
			lowerParamText.setEditable(false);
			OnsetParameterListener onsetParameterListener = new OnsetParameterListener(
					displayPanel, upperInitValue, lowerInitValue,
					upperParamText, lowerParamText);

			JSlider upperParamSlider = new JSlider(0, 100, upperInitValue);
			upperParamSlider.addChangeListener(onsetParameterListener);
			upperParamSlider.setName("upperSlider");

			JSlider lowerParamSlider = new JSlider(0, 100, lowerInitValue);
			lowerParamSlider.addChangeListener(onsetParameterListener);
			lowerParamSlider.setName("lowerSlider");
			
			
			JPanel upperParamBox = new JPanel();
			upperParamBox.add(upperParamSlider);
			upperParamBox.add(upperParamText);
			JPanel lowerParamBox = new JPanel(new FlowLayout());
			lowerParamBox.add(lowerParamSlider);
			lowerParamBox.add(lowerParamText);
			JPanel viewPanel = new JPanel(new GridLayout(1, 3));
			JLabel viewLabel = new JLabel("View range (s): ");
			JButton dec = new JButton("-");
			JTextField zoomField = new JTextField(5);
			zoomField.setActionCommand("text");
			JButton inc = new JButton("+");
			viewPanel.add(viewLabel);
			viewPanel.add(dec);
			viewPanel.add(zoomField);
			viewPanel.add(inc);
			ZoomListener viewListener = new ZoomListener(displayPanel,
					scroller, zoomField);
			inc.addActionListener(viewListener);
			zoomField.addActionListener(viewListener);
			dec.addActionListener(viewListener);

			JPanel fileAndOptions = new JPanel(new GridLayout(4, 1));
			fileAndOptions.setBorder(BorderFactory
					.createEmptyBorder(0, 5, 0, 0));
			//fileAndOptions.add(fileName);
			fileAndOptions.add(upperParamBox);
			fileAndOptions.add(lowerParamBox);
			fileAndOptions.add(optionsBox);
			fileAndOptions.add(viewPanel);
			add(fileAndOptions, BorderLayout.WEST);
			buttonPanel = new ButtonPanel(3, 4);
			buttonPanel.addButton(GUI.LOAD_AUDIO, jobq);
			buttonPanel.addButton(GUI.BEAT_TRACK, jobq);
			buttonPanel.addButton(GUI.SAVE_BEATS, jobq);
			buttonPanel.addButton(GUI.PLAY, jobq);
			buttonPanel.addButton(GUI.CLEAR_BEATS, jobq);
			buttonPanel.addButton(GUI.UNDO, jobq);
			buttonPanel.addButton(GUI.PLAY_AUDIO, jobq);
			buttonPanel.addButton(GUI.STOP, jobq);
			buttonPanel.addButton(GUI.LOAD_BEATS, jobq);
			buttonPanel.addButton(GUI.REDO, jobq);
			buttonPanel.addButton(GUI.PLAY_BEATS, jobq);
		}
		buttonPanel.addButton(GUI.EXIT, jobq);
		add(buttonPanel, BorderLayout.EAST);
	} // ControlPanel() constructor

} // class ControlPanel
