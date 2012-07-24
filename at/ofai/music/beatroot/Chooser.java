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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

/** An extension of the Swing file chooser for specific file types. */
public class Chooser extends JFileChooser {
	
	static final long serialVersionUID = 0;	// avoid compiler warning
	
	File file = null;

	/** Constructor for BeatRoot's file chooser */
	public Chooser() {
		super(FileSystemView.getFileSystemView().getHomeDirectory()); // WG changed from super(".")
		addChoosableFileFilter(getAcceptAllFileFilter());
	} // constructor

	/** Opens a load dialog and returns the chosen file name.
	 * @param ff A file filter determining the type of file that can be selected
	 * @return The chosen file name for loading data, or null if the action
	 *         is cancelled or the file doesn't exist
	 */
	public String getInputName(FileFilters ff) {
		String pathName = null;
		addChoosableFileFilter(ff);
		if (file != null) {
			pathName = file.getAbsolutePath();
			int index = pathName.lastIndexOf(".");
			if (index > 0)
				pathName = pathName.substring(0, index);
			file = new File(pathName + ff.suffix);
			setSelectedFile(file);
		}
		if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			if (getSelectedFile().exists()) {
				file = getSelectedFile();
				pathName = getSelectedFile().getAbsolutePath();
			}
		}
		removeChoosableFileFilter(ff);
		return pathName;
	} // getInputName()
	
	/** Opens a load dialog for loading audio data.
	 * @return The name of an existing audio file, or null
	 */
	public String getAudioInName() {
		return getInputName(FileFilters.waveFileFilter);
	} // getAudioInName()
	
	/** Opens a load dialog for loading beat data.
	 * @return The name of an existing beat file, or null
	 */
	public String getBeatInName() {
		addChoosableFileFilter(FileFilters.textFileFilter);
		addChoosableFileFilter(FileFilters.csvFileFilter);
		String iName = getInputName(FileFilters.tmfFileFilter);
		removeChoosableFileFilter(FileFilters.textFileFilter);
		removeChoosableFileFilter(FileFilters.csvFileFilter);
		return iName;
		// return getInputName(FileFilters.tmfFileFilter);
	} // getBeatInName()

	/** Opens a save dialog and returns the chosen file name.
	 * @param ff A file filter determining the type of file that can be selected
	 * @return The chosen file name for saving data, or null if the action is cancelled
	 */
	public String getOutputName(FileFilters ff) {
		String pathName = null;
		addChoosableFileFilter(ff);
		if (file != null) {
			pathName = file.getAbsolutePath();
			int index = pathName.lastIndexOf(".");
			if (index > 0)
				pathName = pathName.substring(0, index);
			file = new File(pathName + ff.suffix);
			setSelectedFile(file);
		}
		if (showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			if (!getSelectedFile().exists() || (
						JOptionPane.showConfirmDialog(null,
						"File " + getSelectedFile().getAbsolutePath() +
						" exists.\nDo you want to replace it?", "Are you sure?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
				pathName = getSelectedFile().getAbsolutePath();
				int index = pathName.lastIndexOf(".");
				if (index > 0)
					pathName = pathName.substring(0, index);
				file = new File(pathName + ((FileFilters)getFileFilter()).suffix);
				pathName = file.getAbsolutePath();
			}
		}
		removeChoosableFileFilter(ff);
		return pathName;
	} // getOutputName()

	/** Opens a dialog to get a file name for audio output.
	 *  @return The file name for audio output */
	public String getAudioOutName() {
		return getOutputName(FileFilters.waveFileFilter);
	} // getAudioOutName()
	
	/** Opens a dialog to get a file name for saving beat information.
	 *  @return The file name for saving beat information */
	public String getBeatOutName() {
		addChoosableFileFilter(FileFilters.textFileFilter);
		addChoosableFileFilter(FileFilters.csvFileFilter);
		String oName = getOutputName(FileFilters.tmfFileFilter);
		/*if (getFileFilter()==FileFilters.textFileFilter &&
				!oName.endsWith(".txt"))
			oName = oName + ".txt";
		if (getFileFilter()==FileFilters.tmfFileFilter &&
				!oName.endsWith(".tmf"))
			oName = oName + ".tmf";
		if (getFileFilter()==FileFilters.csvFileFilter &&
				!oName.endsWith(".csv"))
			oName = oName + ".csv";*/
		removeChoosableFileFilter(FileFilters.textFileFilter);
		removeChoosableFileFilter(FileFilters.csvFileFilter);
		return oName;
	} // getBeatOutName()

} // class Chooser
