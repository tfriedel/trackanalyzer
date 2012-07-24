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

import javax.swing.JCheckBoxMenuItem;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Key, menu, and button event processing. All user interaction with the
 *  system is processed by the single EventProcessor object, which has
 *  handles to the other main objects for performing the requested actions.
 */
class EventProcessor implements ActionListener, KeyListener {

	/** Handle to BeatRoot's GUI */
	protected GUI gui;
	
	/** Handle to BeatRoot's audio player */
	protected AudioPlayer audioPlayer;
	
	/** Handle to BeatRoot's audio processor */
	protected AudioProcessor audioProcessor;
	
	/** Handle to BeatRoot's file chooser */
	protected Chooser chooser;

	/** Flag for enabling debugging output */
	public static boolean debug = false;

	/** Constructor:
	 *  @param g Handle to BeatRoot's GUI
	 *  @param ap Handle to BeatRoot's audio player
	 *  @param proc Handle to BeatRoot's audio processor
	 *  @param ch Handle to BeatRoot's file chooser
	 */
	public EventProcessor(GUI g, AudioPlayer ap, AudioProcessor proc, Chooser ch) {
		gui = g;
		audioPlayer = ap;
		audioProcessor = proc;
		chooser = ch;
	} // constructor

	/** Processes all user menu and button actions.
	 *  @param e The Java event handling system's representation of the user action */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		boolean flag = false;
		Object o = e.getSource();
		if (o instanceof JCheckBoxMenuItem)
			flag = ((JCheckBoxMenuItem)o).isSelected();
		if (command.equals(GUI.EXIT))
			BeatRoot.quit();
		else if (command.equals(GUI.PLAY))
			audioPlayer.play();
		else if (command.equals(GUI.PLAY_BEATS))
			audioPlayer.play(false);
		else if (command.equals(GUI.PLAY_AUDIO))
			audioPlayer.play(true);
		else if (command.equals(GUI.STOP))
			audioPlayer.stop();
		else if (command.equals(GUI.LOAD_AUDIO))
			gui.loadAudioData();
		else if (command.equals(GUI.SAVE_AUDIO))
			audioPlayer.save();
		else if (command.equals(GUI.LOAD_BEATS))
			gui.loadBeatData();
		else if (command.equals(GUI.SAVE_BEATS))
			gui.saveBeatData();
		else if (command.equals(GUI.UNDO))
			EditAction.undo();
		else if (command.equals(GUI.REDO))
			EditAction.redo();
		else if (command.equals(GUI.EDIT_PREFERENCES))
			gui.editPreferences();
		else if (command.equals(GUI.EDIT_PERCUSSION))
			gui.editPercussionSounds();
		else if (command.equals(GUI.SHOW_BEATS))
			gui.setMode(BeatTrackDisplay.SHOW_BEATS, flag);
		else if (command.equals(GUI.SHOW_IBIS))
			gui.setMode(BeatTrackDisplay.SHOW_IBI, flag);
		else if (command.equals(GUI.SHOW_WAVE))
			gui.setMode(BeatTrackDisplay.SHOW_AUDIO, flag);
		else if (command.equals(GUI.SHOW_SPECTRO))
			gui.setMode(BeatTrackDisplay.SHOW_SPECTRO, flag);
		else if (command.equals(GUI.BEAT_TRACK))
			gui.displayPanel.beatTrack();
		else if (command.equals(GUI.CLEAR_BEATS))
			gui.clearBeatData();
		else if (command.equals(GUI.MARK_METRICAL_LEVEL))
			gui.markMetricalLevel();
		else if (command.equals(GUI.CLEAR_METRICAL_LEVELS))
			gui.clearMetricalLevels();
		else
			BeatRoot.warning("Operation not implemented");
	} // actionPerformed()
	
	/** Processes user key events which are not associated with menu items.
	 *  Keystrokes are only processed if no modifiers are present (e.g. shift, alt, mouse buttons).
	 *  Since key releases are considered irrelevant, all processing is done here. */
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		int modifiers = e.getModifiers();
		if (modifiers == 0) {
			switch (keyCode) {
			case KeyEvent.VK_A:
				audioPlayer.stop(false);
				gui.displayPanel.toggleAnnotateMode();
				break;
			/*case KeyEvent.VK_LEFT:
				gui.displayPanel.selectPreviousBeat();
				break;
			case KeyEvent.VK_RIGHT:
				gui.displayPanel.selectNextBeat();
				break;*/
			case KeyEvent.VK_HOME:
				gui.displayPanel.selectFirstBeat();
				break;
			case KeyEvent.VK_END:
				gui.displayPanel.selectLastBeat();
				break;
			case KeyEvent.VK_0:
				gui.displayPanel.setMetricalLevel(0);
				break;
			case KeyEvent.VK_1:
				gui.displayPanel.setMetricalLevel(1);
				break;
			case KeyEvent.VK_2:
				gui.displayPanel.setMetricalLevel(2);
				break;
			case KeyEvent.VK_3:
				gui.displayPanel.setMetricalLevel(3);
				break;
			case KeyEvent.VK_4:
				gui.displayPanel.setMetricalLevel(4);
				break;
			case KeyEvent.VK_5:
				gui.displayPanel.setMetricalLevel(5);
				break;
			case KeyEvent.VK_6:
				gui.displayPanel.setMetricalLevel(6);
				break;
			case KeyEvent.VK_7:
				gui.displayPanel.setMetricalLevel(7);
				break;
			case KeyEvent.VK_X:
				gui.displayPanel.removeSelectedBeat();
				break;
			case KeyEvent.VK_C:
				gui.displayPanel.addAfterSelectedBeat();
				break;
			case KeyEvent.VK_SLASH:
				if (debug)
					System.err.print(System.nanoTime() / 1000000 % 100000);
				gui.displayPanel.addBeatNow();
				break;
			case KeyEvent.VK_V:
				gui.displayPanel.addBeat(gui.displayPanel.getCurrentTime());
				break;
			case KeyEvent.VK_P:
				audioPlayer.play();
				break;
			case KeyEvent.VK_S:
				audioPlayer.stop();
				break;
			case KeyEvent.VK_SPACE: // WG. inserted (4 Aug 2009)
				if (audioPlayer.playing)
					audioPlayer.stop();
				else
					audioPlayer.play();
				break;
			case KeyEvent.VK_B: // WG. inserted (4 Aug 2009)
				gui.displayPanel.beatTrack();
				break;
			case KeyEvent.VK_Z: // WG. inserted (5 Aug 2009)
				gui.displayPanel.clearBeats();
				break;
			case KeyEvent.VK_RIGHT:
				gui.scroll(.025);
				break;
			case KeyEvent.VK_LEFT:
				gui.scroll(-.025);
				break;
			} // switch
		} else if (modifiers == KeyEvent.CTRL_MASK) {
			switch (keyCode) {
			case KeyEvent.VK_RIGHT:
				gui.scroll(.1);
				break;
			case KeyEvent.VK_LEFT:
				gui.scroll(-.1);
				break;
			} // switch
		} else if (modifiers == KeyEvent.ALT_MASK) {
			switch (keyCode) {
			case KeyEvent.VK_RIGHT:
				gui.scroll(.005);
				break;
			case KeyEvent.VK_LEFT:
				gui.scroll(-.005);
				break;
			} // switch			
		}
	} // keyPressed()
	
	/** Ignore key releases, since processing is performed as soon as the key is pressed.
	 *  Implements part of interface KeyListener */
	public void keyReleased(KeyEvent e) {}
	
	/** Ignore KeyEvents indicating that a key was typed, since keyPressed() has
	 *  already dealt with this keystroke.
	 *  Implements part of interface KeyListener */
	public void keyTyped(KeyEvent e) {}
	
} // class EventProcessor
