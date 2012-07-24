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

import java.awt.*;
import javax.swing.*;

/** Java has problems communicating with window managers like fvwm2.
 *  This class is a workaround to find the size of borders of JFrames in
 *  the current environment, so that programs can size their JFrames correctly.
 *  Since we don't know how big borders are until after the window is created,
 *  we create a dummy JFrame, query the size of its borders, and destroy it.
 *  The values are saved for later calls to this class, so that the dummy JFrame
 *  is only created once.
 */
public class FrameMargins {

	protected static Insets i = null;
	protected static Dimension insetsWithMenu = null;
	protected static Dimension insetsWithoutMenu = null;
	protected static Dimension topLeftWithMenu = null;
	protected static Dimension topLeftWithoutMenu = null;

	/** Returns the total size of the insets of a JFrame, that is the size of
	 *  the title bar, menu bar (if requested) and the borders of the JFrame.
	 *  In other words, the return value is the difference in size between the
	 *  JFrame itself and its content pane.
	 *  @param withMenuFlag indicates whether a menu bar should be included in
	 *  the calculations
	 *  @return the height and width of the insets of a JFrame, unless
	 *  <code>getInsets()</code> returns a ridiculously large value, in which
	 *  case we gracefully return a guess of (30,20).
	 */
	public static Dimension get(boolean withMenuFlag) {
		if (i == null) {
			JFrame f = new JFrame("Get size of window borders");
			JMenuBar mb = new JMenuBar();
			f.setJMenuBar(mb);
			mb.add(new JMenu("OK"));
			f.setVisible(true);
			i = f.getInsets();
			f.dispose();
			if ((i.left>100) || (i.right>100) || (i.top>100) || (i.bottom>100)){
				i.left = 10;	// Code around a bug in getInsets()
				i.right = 10;	//  - don't believe ridiculously high values
				i.top = 20;
				i.bottom = 10;
			}
			insetsWithMenu = new Dimension(i.left + i.right,
										i.top + i.bottom + mb.getHeight());
			insetsWithoutMenu = new Dimension(i.left + i.right, i.top+i.bottom);
			topLeftWithoutMenu = new Dimension(i.left, i.top);
			topLeftWithMenu = new Dimension(i.left, i.top + mb.getHeight());
		}
		return withMenuFlag? insetsWithMenu: insetsWithoutMenu;
	} // get()

	/** Returns the location of the content pane with respect to its JFrame.
	 *  @param withMenuFlag indicates whether a menu bar should be included in
	 *  the calculations
	 *  @return the x and y offsets of the top left corner of the content pane
	 *  from the top left corner of the JFrame
	 */
	public static Dimension getOrigin(boolean withMenuFlag) {
		if (i == null)
			get(withMenuFlag);
		return withMenuFlag? topLeftWithMenu: topLeftWithoutMenu;
	} // getOrigin()

	/** Returns the Insets object for a JFrame.
	 *  @return the Insets object measuring the size of the borders of a JFrame
	 */
	public static Insets getFrameInsets() {
		if (i == null)
			get(false);
		return i;
	} // getFrameInsets()

} // class FrameMargins
