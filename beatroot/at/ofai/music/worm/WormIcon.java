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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

class WormIcon {
	protected int xSize, ySize;
	protected int[] dx;
	protected int[] dy;

	public WormIcon(int type) {
		switch (type) {
			case 1:
				xSize = 40;
				ySize = 40;
				dx = new int[]{3,-1,0,1,2,4, 4, 2, 1,1,2,4, 4, 2, 1, 0,-1};
				dy = new int[]{8, 5,5,4,3,2,-3,-4,-5,5,4,3,-2,-3,-4,-5,-5};
				break;
			case 2:
				xSize = 300;
				ySize = 100;
				dx = new int[] {
						10,   4, 3, 2, 0,-2,-3,-4,-3,-2,-1, 0, 0, 1, 1, 1,		// P
						25,  4, 4, 2,-1,-3,-4,-4,-3,-1, 1, 3, 4, 4, 4,			// e
						13,  0, 0, 0, 1, 3, 5, 3, 1,							// r
						13,  0, 0, 0,-4, 8,-4, 0, 1, 3, 5, 3, 1,				// f
						17, -5,-3,-1, 1, 3, 5, 5, 3, 1,-1,-3,-5,				// o
						20,  0, 0, 0, 1, 3, 5, 3, 1,							// r
						10, -1, 0, 1, 2, 4, 3, 1, 1, 3, 4, 2, 1, 0,-1,			// m
						30, -1, 0,-1,-3,-5,-5,-3,-1, 1, 3, 5, 5, 3, 1, 0, 1,	// a
						12,  0, 0, 0, 1, 3, 5, 3, 1, 0, 0, 0,					// n
						28, -3,-5,-5,-3,-1, 1, 3, 5, 5, 3,						// c
						17,  4, 4, 2,-1,-3,-4,-4,-3,-1, 1, 3, 4, 4, 4,			// e
						-180,-1, 0, 1, 2, 4, 4, 2, 1, 1, 2, 4, 4, 2, 1, 0,-1,	// W
						20, -5,-3,-1, 1, 3, 5, 5, 3, 1,-1,-3,-5,				// o
						20,  0, 0, 0, 1, 3, 5, 3, 1,							// r
						10, -1, 0, 1, 2, 4, 3, 1, 1, 3, 4, 2, 1, 0,-1			// m
				};
				dy = new int[] {
						20,  0,-2,-3,-5,-3,-2, 0, 2, 3, 4, 4, 4, 4, 5, 5,		// P
						-8,  0,-1,-3,-3,-2,-1, 2, 3, 4, 5, 4, 2, 0,-1,			// e
						2,  -4,-4,-4,-3,-1, 0, 1, 3,							// r
						12, -4,-4,-4, 0, 0,-4,-4,-3,-2, 0, 2, 3,				// f
						3,   1, 3, 5, 5, 3, 1,-1,-3,-5,-5,-3,-1,				// o
						16, -4,-4,-4,-3,-1, 0, 1, 3,							// r
						13, -4,-4,-4,-3, 1, 3, 4,-4,-3,-1, 3, 4, 4, 4,			// m
						-16, 5, 4, 4, 3, 1,-1,-3,-4,-4,-3,-1, 1, 3, 4, 4, 5,	// a
						0,  -4,-4,-4,-3,-1, 0, 1, 3, 4, 4, 4,					// n
						-14,-3,-1, 1, 3, 5, 5, 3, 1,-1,-3,						// c
						-5,  0,-1,-3,-3,-2,-1, 2, 3, 4, 5, 4, 2, 0,-1,			// e
						20,  5, 5, 4, 3, 2,-3,-4,-5, 5, 4, 3,-2,-3,-4,-5,-5,	// W
						4,   1, 3, 5, 5, 3, 1,-1,-3,-5,-5,-3,-1,				// o
						16, -4,-4,-4,-3,-1, 0, 1, 3,							// r
						13, -4,-4,-4,-3, 1, 3, 4,-4,-3,-1, 3, 4, 4, 4			// m
				};
				break;
			case 3:
				xSize = 136;
				ySize = 48;
				dx = new int[] {
						9,  -1, 0, 1, 2, 4, 4, 2, 1, 1, 2, 4, 4, 2, 1, 0,-1,
						20, -5,-3,-1, 1, 3, 5, 5, 3, 1,-1,-3,-5,
						20,  0, 0, 0, 1, 3, 5, 3, 1,
						10, -1, 0, 1, 2, 4, 3, 1, 1, 3, 4, 2, 1, 0,-1
				};
				dy = new int[] {
						12,  5, 5, 4, 3, 2,-3,-4,-5, 5, 4, 3,-2,-3,-4,-5,-5,
						4,   1, 3, 5, 5, 3, 1,-1,-3,-5,-5,-3,-1,
						16, -4,-4,-4,-3,-1, 0, 1, 3,
						13, -4,-4,-4,-3, 1, 3, 4,-4,-3,-1, 3, 4, 4, 4
				};
				break;
		}
	}

	public static Image getWormIcon(int type, Component c) {
		return new WormIcon(type).getImage(c);
	}

	public int getX() { return xSize; }
	public int getY() { return ySize; }

	public Image getImage(Component c) {
		Image img = c.createImage(xSize, ySize);
		Graphics g = img.getGraphics();
		g.setColor(WormConstants.backgroundColor);
		g.fillRect(0, 0, xSize, ySize);
		int x = 0;
		int y = 0;
		for (int i = 0; i < dx.length; i++) {
			x += dx[i];
			y += dy[i];
			g.setColor(WormConstants.wormHeadRimColor);
			g.fillOval(x-1, y-1, 9, 9);
			g.setColor(WormConstants.wormHeadColor);
			g.fillOval(x, y, 7, 7);
		}
		g.setColor(WormConstants.wormHeadRimColor);
		g.drawLine(x+2, y+2, x+2, y+2);
		g.drawLine(x+4, y+2, x+4, y+2);
		return img;
	} // getImage()
	
} // class WormIcon
