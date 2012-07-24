/*  Performance Worm: Visualisation of Expressive Musical Performance
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

package at.ofai.music.worm;

import javax.swing.JScrollBar;
import java.awt.Adjustable;
import java.awt.Dimension;

class WormScrollBar extends JScrollBar {

	static final long serialVersionUID = 0;
	
	protected Worm worm;

	public WormScrollBar(Worm w) {
		super(Adjustable.HORIZONTAL, 0, 10, 0, 1010);
		// setOrientation(Adjustable.HORIZONTAL);
		// setVisibleAmount(...);
		// setMinimum(...);
		// setMaximum(...);
		// setValue(...);
		setUnitIncrement(10);
		setBlockIncrement(100);
		worm = w;
		setBackground(WormConstants.buttonColor);
		setPreferredSize(new Dimension(w.getWidth(), 17));
		worm.setScrollBar(this);
	} // constructor

	public void setValue(int value) {
		if (worm.audio != null)
			worm.audio.skipTo(value);
		super.setValue(value);
	} // setValue()

	public void setValueNoFeedback(int value) { super.setValue(value); }

	// // MouseListener Interface
	// public void mouseClicked(MouseEvent e) {}
	// public void mouseEntered(MouseEvent e) {}
	// public void mouseExited(MouseEvent e) {}
	// public void mousePressed(MouseEvent e) {}
	// public void mouseReleased(MouseEvent e) {}
	// 
	// // AdjustmentListener interface
	// public void adjustmentValueChanged(AdjustmentEvent e) {}

} // class WormScrollBar
