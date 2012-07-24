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

/** A listener class for the scrollbar, which notifies the GUI's main
 *  panel when the scrollbar is moved.
 *  @author Simon Dixon
 */
class PanelScroller implements AdjustmentListener {
	
	/** A reference to the main data panel of the BeatRoot GUI,
	 *  so that it can be updated when the scroller is moved. */
	BeatTrackDisplay controlled;
	static boolean passMessage = true;

	/** Constructor:
	 *  @param btd A reference to the main data panel of the BeatRoot GUI
	 */
	public PanelScroller(BeatTrackDisplay btd) {
		controlled = btd;
	} // constructor

	/** Called by the event handling system when the scroller is moved.
	 *  Implements the AdjustmentListener interface.
	 *  @param e The event indicating the change in the scroller position
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (passMessage)
			controlled.setValue(e.getValue());
	} // adjustmentValueChanged()
	
} // class PanelScroller
