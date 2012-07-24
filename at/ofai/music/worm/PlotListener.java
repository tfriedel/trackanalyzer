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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class PlotListener implements KeyListener {
	PlotPanel callback;
	public PlotListener(PlotPanel p) { callback = p; }
	public void keyTyped(KeyEvent e) {}	// doesn't get all keys; keyCode == 0
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)		// CTRL + key
			switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:	callback.yZoom(true, false);	break;
				case KeyEvent.VK_DOWN:	callback.yZoom(false, false);	break;
				case KeyEvent.VK_LEFT:	callback.xZoom(false, false);	break;
				case KeyEvent.VK_RIGHT:	callback.xZoom(true, false);	break;
			}
		else if ((e.getModifiers() & KeyEvent.SHIFT_MASK) != 0)	// SHIFT + key
			switch (e.getKeyCode()) {
				case KeyEvent.VK_P:		callback.print(600);			break;
				case KeyEvent.VK_UP:	callback.yZoom(false, true);	break;
				case KeyEvent.VK_DOWN:	callback.yZoom(true, true);		break;
				case KeyEvent.VK_LEFT:	callback.xZoom(true, true);		break;
				case KeyEvent.VK_RIGHT:	callback.xZoom(false, true);	break;
			}
		else													// normal key
			switch (e.getKeyCode()) {
				case KeyEvent.VK_0:		callback.setMode(0);			break;
				case KeyEvent.VK_1:		callback.setMode(1);			break;
				case KeyEvent.VK_2:		callback.setMode(2);			break;
				case KeyEvent.VK_3:		callback.setMode(3);			break;
				case KeyEvent.VK_4:		callback.setMode(4);			break;
				case KeyEvent.VK_5:		callback.setMode(5);			break;
				case KeyEvent.VK_6:		callback.setMode(6);			break;
				case KeyEvent.VK_7:		callback.setMode(7);			break;
				case KeyEvent.VK_8:		callback.setMode(8);			break;
				case KeyEvent.VK_9:		callback.setMode(9);			break;
				case KeyEvent.VK_F:		callback.fitAxes();				break;
				case KeyEvent.VK_N:		callback.rotateCurrent();		break;
				case KeyEvent.VK_P:		callback.print(300);			break;
				case KeyEvent.VK_R:		callback.resize();				break;
				case KeyEvent.VK_U:		callback.update();				break;
				case KeyEvent.VK_Q:		callback.close();				break;
				case KeyEvent.VK_UP:	callback.yMoveDown(false);		break;
				case KeyEvent.VK_DOWN:	callback.yMoveDown(true);		break;
				case KeyEvent.VK_LEFT:	callback.xMoveRight(false);		break;
				case KeyEvent.VK_RIGHT:	callback.xMoveRight(true);		break;
			}
	} // keyReleased()

} // class PlotListener
