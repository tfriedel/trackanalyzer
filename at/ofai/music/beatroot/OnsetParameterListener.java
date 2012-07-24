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

import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A listener class for onset parameter slider, which notifies the GUI's main
 * panel when the scrollbar is moved.
 * 
 * @author Sebastian Flossmann
 */
class OnsetParameterListener implements ChangeListener {
	BeatTrackDisplay display;
	/* The current value of the upper slider */
	int p1;
	
	/* The current value of the lower slider */
	int p2;
	
	/* The Textfield showing the current value of the upper slider */
	JTextField text1;
	
	/* The Textfield showing the current value of the lower slider */
	JTextField text2;

	public OnsetParameterListener(BeatTrackDisplay btd, int upper, int lower, JTextField upperText, JTextField lowerText){
		display = btd;
		p1 = upper;
		p2 = lower;
		text1 = upperText;
		text2 = lowerText;
	}

	public void stateChanged(ChangeEvent e) {
		JSlider slider = (JSlider) e.getSource();
		if (slider.getName() == "upperSlider") {
			p1 = slider.getValue();
//			if (p1 >= p2) {
//				p1 = p2;
//				slider.setValue(p2);
//			}
			text1.setText(Integer.toString(p1));
		} else {
			p2 = slider.getValue();
//			if (p2 <= p1) {
//				p2 = p1;
//				slider.setValue(p1);
//			}
			text2.setText(Integer.toString(p2));
		}
		display.setOnsetDetectionParam(p1/100.0, p2/100.0);
	} // stateChanged()

} // class OnsetParameterListener