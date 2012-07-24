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

import java.io.File;
import javax.swing.filechooser.FileFilter;

class MyFileFilter extends FileFilter {

	public static final MyFileFilter mp3Filter =
						new MyFileFilter(".mp3", "MPEG1 level 3");
	public static final MyFileFilter waveFilter =
						new MyFileFilter(".wav", "Wave");
	public static final MyFileFilter matchFilter =
						new MyFileFilter(".match", "Match");
	public static final MyFileFilter wormFilter =
						new MyFileFilter(".worm", "Worm");

	protected String suffix;
	protected String description;
	
	public MyFileFilter(String suff, String desc) {
		suffix = suff;
		description = desc + " files (*" + suff + ")";
	} // constructor

	public boolean accept(File f) {
		return (f != null) && (f.isDirectory() || f.getName().endsWith(suffix));
	} // accept()

	public String getDescription() { return description; }

} // class MyFileFilter
