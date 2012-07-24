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

import java.awt.event.*;
import javax.swing.*;

/** A listener class for notifying the GUI's main panel about changes in the zoom level. */
class ZoomListener implements ActionListener {

	/** The scrollbar for selecting what part of the audio data is displayed */
	JScrollBar scroller;

	/** The main data panel of BeatRoot's GUI */
	BeatTrackDisplay displayPanel;
	
	/** An editable text field containing the length (in seconds) of visible audio data */
	JTextField valueField;

	/** Constructor:
	 * @param btd The main data panel of BeatRoot's GUI
	 * @param sb  The scrollbar for selecting what part of the audio data is displayed
	 * @param vf  The text field containing the length (in seconds) of visible audio data
	 */
	public ZoomListener(BeatTrackDisplay btd, JScrollBar sb, JTextField vf) {
		displayPanel = btd;
		scroller = sb;
		valueField = vf;
		valueField.setText(Double.toString(btd.getVisibleAmount()/1000.0));
	} // ZoomListener constructor

	/** Increments or decrements and rounds the length of visible audio data.
	 *  @param value The original length of visible audio data
	 *  @param sign  The direction of change (+1.0 or -1.0) 
	 *  @return      The new length of visible audio data
	 */
	public static double delta(double value, double sign) {
		if (value >= 5.0)
			return Math.floor(value + sign * value / 5.0 + 0.5);
		if (value >= 1.0)
			return Math.floor(5.0 * (value + sign * value / 5.0) + 0.5) / 5.0;
		if (value >= 0.5)
			return Math.floor(10.0 * (value + sign * value / 5.0) + 0.5) / 10.0;
		return Math.floor(10.0 * (value + sign * 0.1) + 0.5) / 10.0;
	} // delta()

	/** Called when a zoom button is pressed or the text field is edited.
	 *  Implements the ActionListener interface.
	 *  @param e The object indicating what kind of event occurred
	 */
	public void actionPerformed(ActionEvent e) {
		double value = (double)displayPanel.getVisibleAmount() / 1000.0;
		try {
			String comm = e.getActionCommand();
			if (comm.equals("+"))
				value = delta(value, -1.0);
			else if (comm.equals("-"))
				value = delta(value, 1.0);
			else
				value = Double.parseDouble(valueField.getText());
			if (value < 0.1)
				value = 0.1;
			if (value > displayPanel.maximumTime - displayPanel.minimumTime)
				value = displayPanel.maximumTime - displayPanel.minimumTime;
			displayPanel.setVisibleAmount((int)(value * 1000.0));
			valueField.setText(Double.toString(value));
		} catch (NumberFormatException nfe) {
			valueField.setText("");
		}
	} // actionPerformed()

} // class ZoomListener
