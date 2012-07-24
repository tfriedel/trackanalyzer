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

import java.awt.Color;
import at.ofai.music.util.Colors;

class WormConstants implements Colors {
public static String version = " 1.4-RC2 "; // DON'T EDIT THIS LINE; see make.sh
	public static String title = "Performance Worm v" + version.substring(1) +
								 "(c) 2002 ofaiMusic <simon@ofai.at>";
	public static int X_SZ = 800;		// Default x-size of Worm Panel
	public static int Y_SZ = 500;		// Default y-size of Worm Panel
	public static int cpHeight = 50;	// Height of control panel
	public static int footMargin = 20;	// Distance from x-axis to top/bottom
	public static int sideMargin = 40;	// Distance from y-axis to sides
	public static int wormLength = 300;	// Number of points in the worm
	public static Color buttonTextColor = Color.black;
	public static Color buttonColor = Color.white;
	public static Color axesColor = Color.black;
	public static Color backgroundColor = Color.white;
	public static Color wormHeadColor = Color.red;
	public static Color wormTailColor = Color.white;// new Color(255,240,240);
	public static Color wormHeadRimColor = Color.black;
	public static Color wormTailRimColor = Color.white;
	public static Color wormFaceColor = Color.black;
	public static Color altFaceColor = Color.white;
	// BROWN version: new Color(255, 200, 160);

	public Color getBackground() { return backgroundColor; }
	public Color getForeground() { return axesColor; }
	public Color getButton() { return buttonColor; }
	public Color getButtonText() { return buttonTextColor; }
	
	public static void setDayColours() {
		buttonTextColor = Color.black;
		buttonColor = Color.white;
		axesColor = Color.black;
		backgroundColor = Color.white;
		wormHeadColor = Color.red;
		wormTailColor = Color.white;
		wormHeadRimColor = Color.black;
		wormTailRimColor = Color.white;
		wormFaceColor = Color.black;
		altFaceColor = Color.white;
	} // setDayColours()

	public static void setNightColours() {
		buttonTextColor = Color.yellow;
		buttonColor = Color.black;
		axesColor = Color.yellow;
		backgroundColor = Color.black;
		wormHeadColor = Color.green;
		wormTailColor = Color.black;
		wormHeadRimColor = Color.yellow;
		wormTailRimColor = Color.black;
		wormFaceColor = Color.black;
		altFaceColor = Color.black;
	} // setNightColours()
	
	public static void setGlow(boolean flag) {
		if (flag)
			setNightColours();
		else
			setDayColours();
	} // setGlow()

} // class WormConstants
