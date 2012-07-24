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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.TextField;
import java.io.File;
import javax.swing.filechooser.FileFilter;

public class MyFileChooser extends JFileChooser {

	static final long serialVersionUID = 0;	// silence compiler warning
	
	public MyFileChooser() {
		super(".");
		addChoosableFileFilter(MyFileFilter.mp3Filter);
		addChoosableFileFilter(MyFileFilter.waveFilter);
		addChoosableFileFilter(MyFileFilter.matchFilter);
		addChoosableFileFilter(MyFileFilter.wormFilter);
		addChoosableFileFilter(getAcceptAllFileFilter());
	} // constructor

	public String browseOpen(TextField t, FileFilter ff) {
		setSelectedFile(new File(t.getText()));
		setFileFilter(ff);
		if (showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			t.setText(getSelectedFile().getAbsolutePath());
		return t.getText();
	} // browseOpen()

	public String browseSave() {
		// setSelectedFile(s);
		setFileFilter(MyFileFilter.wormFilter);
		if (showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			if (!getSelectedFile().exists() || (
				JOptionPane.showConfirmDialog(null, "File " +
						getSelectedFile().getAbsolutePath() +
						" exists.\nDo you want to replace it?",
						"Are you sure?", JOptionPane.YES_NO_OPTION) ==
						JOptionPane.YES_OPTION))
			return getSelectedFile().getAbsolutePath();
		}
		return null;
	} // browseSave()
	
} // class MyFileChooser
