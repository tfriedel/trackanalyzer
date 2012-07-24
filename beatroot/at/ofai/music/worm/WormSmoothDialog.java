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

import at.ofai.music.util.Parameters;

class WormSmoothDialog extends Parameters {

	static final long serialVersionUID = 0;
	
	public WormSmoothDialog (Worm w, WormFile wf) {
		super(w.theFrame, "Smoothing parameters");
		setDouble("Before", 1);
		setDouble("After", 1);
		String[] labels = new String[]{"Tracks","Beats","Bars","Seconds"};
		int[] levels = {WormFile.TRACK, WormFile.BEAT, WormFile.BAR, 0};
		setChoice("Units", labels, 1);
		setVisible(true);
		try {
			double before = getDouble("Before");
			double after = getDouble("After");
			String smoothLevel = getChoice("Units");
			int smoothIndex = 0;
			for (int i = 0; i < labels.length; i++)
				if (smoothLevel.equals(labels[i]))
					smoothIndex = i;
			if ((after > 0) && (before > 0))
				wf.smooth(Worm.FULL_GAUSS, before, after, levels[smoothIndex]);
			else
				wf.smooth(Worm.NONE, 0, 0, 0);
		} catch (NumberFormatException e) {}
	}

} // class WormSmoothDialog
