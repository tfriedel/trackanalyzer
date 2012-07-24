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

import java.io.File;
import javax.swing.filechooser.FileFilter;

/** Implements a set of file filters for the file types used by BeatRoot.
 *  An object of this class represents a specific file type, which is identified
 *  by the extension of the file name.
 */
public class FileFilters extends FileFilter {

	/** The extension corresponding to this file type */
	protected String suffix;
	
	/** A text description of this file type */
	protected String description;

	public static final FileFilters waveFileFilter = new FileFilters(".wav", "Wave");
	public static final FileFilters sndFileFilter = new FileFilters(".snd", "Sun sound");
	public static final FileFilters midiFileFilter = new FileFilters(".mid", "Midi");
	public static final FileFilters tmfFileFilter = new FileFilters(".tmf", "Midi text");
	public static final FileFilters textFileFilter = new FileFilters(".txt", "ASCII text");
	public static final FileFilters csvFileFilter = new FileFilters(".csv", "MS Excel");
	public static final FileFilters matchFileFilter = new FileFilters(".match", "Score match");

	/** Constructor
	 *  @param suff The extension (suffix) of the file name
	 *  @param desc A text description of the file type
	 */
	public FileFilters(String suff, String desc) {
		suffix = suff;
		description = desc + " files (*" + suff + ")";
	} // constructor

	/** Checks file names for a given extension.
	 *  @param f The File object whose name is to be checked
	 *  @return True for directories and files with the given extension.
	 */
	public boolean accept(File f) {
		return (f!=null) && (f.isDirectory()||f.getName().endsWith(suffix));
	} // accept()

	/** The description of the file type used in the file dialog's choice box.
	 *  @return A String describing the file type.
	 */
	public String getDescription() {
		return description;
	} // getDescription()
	
} // class FileFilters
