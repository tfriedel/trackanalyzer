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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import at.ofai.music.util.EventList;
import at.ofai.music.util.Parameters;

/** The main window of BeatRoot's graphical user interface. */
public class GUI extends JFrame {

	/** The object which processes key, button and menu events */
	protected EventProcessor listener;
	
	/** The object which handles sound output */
	protected AudioPlayer audioPlayer;
	
	/** The object which reads and processes audio */
	protected AudioProcessor audioProcessor;
	
	/** BeatRoot's file chooser object */
	protected Chooser chooser;
	
	/** Dialog for setting preferences */
	protected Parameters preferences;
	
	/** Dialog for specifying metrical levels */
	protected Parameters metricalLevels;
	
	/** Dialog for setting the percussion sounds which are played on beats */
	protected Parameters percussionSounds;
	
	/** BeatRoot's menu bar */
	protected JMenuBar menuBar;
	
	/** The main data panel, which displays audio and beat data, and is a component of this window */ 
	protected BeatTrackDisplay displayPanel;
	
	/** The scroller for showing or changing the position of the viewport relative to the whole audio file */
	protected JScrollBar scroller;
	protected int scrollBarWidth = 1000; // pixel
	
	/** An intermediate level panel containing the displayPanel and scroller */
	protected JPanel scrollPane;
	
	/** The panel containing buttons and text fields, situated at the bottom of the window */
	protected ControlPanel controlPanel;
	
	/** The current list of beat times */
	protected EventList beats;
	
	/** Avoid compiler warning */
	static final long serialVersionUID = 0;
	
	/** Flag for enabling printing of debugging information */
	public static boolean debug = false;
	
	/** Name of program - displayed as part of window title */
	public static final String title = "BeatRoot";
	
	/** Version number of program - displayed as part of window title.
	 *  DO NOT EDIT: This line is also used in creating the file name of the jar file. */
	public static final String version = "0.5.6";
	
	/** Strings displayed on menus and buttons */
	public static final String LOAD_AUDIO = "Load Audio Data";
	public static final String LOAD_BEATS = "Load Beat Data";
	public static final String SAVE_AUDIO = "Save Audio Data";
	public static final String SAVE_BEATS = "Save Beat Data";
	public static final String EXIT = "Exit";
	public static final String UNDO = "Undo";
	public static final String REDO = "Redo";
	public static final String EDIT_PREFERENCES = "Preferences";
	public static final String EDIT_PERCUSSION = "Edit Percussion Sounds";
	public static final String SHOW_WAVE = "Waveform";
	public static final String SHOW_SPECTRO = "Spectrogram";
	public static final String SHOW_IBIS = "Inter-beat Intervals";
	public static final String SHOW_BEATS = "Beats";
	public static final String PLAY = "Play with beats";
	public static final String PLAY_AUDIO = "Play without beats";
	public static final String PLAY_BEATS = "Play beats only";
	public static final String STOP = "Stop";
	public static final String BEAT_TRACK = "Track beats";
	public static final String CLEAR_BEATS = "Clear beats";
	public static final String MARK_METRICAL_LEVEL = "Mark metrical level";
	public static final String CLEAR_METRICAL_LEVELS = "Clear metrical levels";

	/** Strings displayed in preferences window */
	public static final String LOW_THRESHOLD = "Low threshold";
	public static final String HIGH_THRESHOLD = "High threshold";
	public static final String AUDIO_SCALE_FACTOR = "Audio scale factor";
	public static final String CLICK_VOLUME = "Click track volume";
	
	/** Default values of preferences */
	public static double DEFAULT_LOW_THRESHOLD = 5.0;
	public static double DEFAULT_HIGH_THRESHOLD = 15.0;
	public static double DEFAULT_SCALE_FACTOR = 1.0;
	public static double DEFAULT_CLICK_VOLUME = 1.0;
	
	/** Strings displayed in metrical level window */
	public static final String LEVEL = "Metrical level";
	public static final String LENGTH = "Length";
	public static final String PHASE = "Phase";

    /** Constants defining metrical levels (see at.ofai.music.worm.WormFile) */
    public static final int	TRACK=1, BEAT=2, BAR=4, SEG1=8, SEG2=16, SEG3=32, SEG4=64;

    /** Constants and default file names for percussion sounds for each metrical levels */
    public static final int percussionCount = 7;
	public static final String PERCUSSION_STRINGS[][] = {
		{"Level 1: Track", "audio/77-woodblk2.wav"},
		{"Level 2: Beat", "audio/76-woodblk1.wav"},
		{"Level 3: Bar", "audio/33-metclick.wav"},
		{"Level 4: Phrase 1", "audio/34-metbell.wav"},
		{"Level 5: Phrase 2", "audio/56-cowbell.wav"},
		{"Level 6: Phrase 3", "audio/53-cymbell.wav"},
		{"Level 7: Phrase 4", "audio/81-triangl2.wav"}
	};

	/** Constructor: creates the GUI for BeatRoot
	 * @param ap Handle to the audio player object
	 * @param proc Handle to the audio processor object
	 * @param ch Handle to the file chooser object
	 */
	public GUI(AudioPlayer ap, AudioProcessor proc, Chooser ch) {
		super(title + " " + version);
		audioPlayer = ap;
		ap.gui = this;
		audioProcessor = proc;
		chooser = ch;
		beats = new EventList();
		listener = new EventProcessor(this, ap, proc, ch);
		try {
			UIManager.setLookAndFeel(
				UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) { }
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				BeatRoot.quit();
			}
		});

		menuBar = new JMenuBar();
		menuBar.add(makeFileMenu());
		menuBar.add(makeEditMenu());
		menuBar.add(makeViewMenu());
		menuBar.add(makePlayMenu());
		menuBar.add(makeBeatTrackMenu());
		
		preferences = new Parameters(this, "Preferences");
		setPreferences();
		metricalLevels = new Parameters(this, "Mark Metrical Levels");
		setMetricalLevels();
		percussionSounds = new Parameters(this, "Select Percussion Sounds");
		setPercussionSounds();

		displayPanel = new BeatTrackDisplay(this, beats);
		displayPanel.addKeyListener(listener);
		displayPanel.setBeats(beats);
		//displayPanel.addPropertyChangeListener(arg0)
		EditAction.setDisplay(displayPanel);
		
		scroller = new JScrollBar();
		scroller.setMinimum(displayPanel.getMinimum());
		scroller.setMaximum(displayPanel.getMaximum());
		scroller.setValue(displayPanel.getValue());
		scroller.setVisibleAmount(displayPanel.getVisibleAmount());
		scroller.setUnitIncrement(100);
		scroller.setBlockIncrement(400);
		scroller.setOrientation(Adjustable.HORIZONTAL);
		scroller.setPreferredSize(new Dimension(scrollBarWidth, 17));
		
		scrollPane = new JPanel();
		scrollPane.setLayout(new BorderLayout());
		scrollPane.setPreferredSize(new Dimension(scrollBarWidth+10, displayPanel.ySize+17+10));
		scrollPane.setBackground(Color.black);
		scrollPane.add(displayPanel, BorderLayout.CENTER);
		scrollPane.add(scroller, BorderLayout.SOUTH);
		scroller.addAdjustmentListener(new PanelScroller(displayPanel));

		String fileName = null;
		if (ap.currentFile != null)
			fileName = ap.currentFile.path;
		controlPanel = new ControlPanel(displayPanel, scroller, fileName, listener);

		setJMenuBar(menuBar);
		
		JPanel pane = new JPanel();
		pane.setBackground(Color.black);
		pane.setLayout(new BorderLayout());
		pane.add(scrollPane, BorderLayout.CENTER);
		pane.add(controlPanel, BorderLayout.SOUTH);
		setContentPane(pane);
		pack();
	} // constructor

	/** Creates a menu item with the given text and key codes.
	 *  @param text The text that appears on the menu
	 *  @param menuKey The key to access the menu item when the menu is open
	 *  @param altKey The shortcut key to access the menu item using the ALT key
	 *  @param isCheckBox Flag indicating whether the menu item is a binary flag
	 *  @return The menu item
	 */
	protected JMenuItem makeMenuItem(String text, int menuKey,
									int altKey, boolean isCheckBox) {
		JMenuItem menuItem;
		if (isCheckBox) {
			menuItem = new JCheckBoxMenuItem(text);
			menuItem.setMnemonic(menuKey);
			menuItem.setSelected(true);
		} else
			menuItem = new JMenuItem(text, menuKey);
		if (altKey != 0)
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
									altKey, ActionEvent.ALT_MASK));
		menuItem.addActionListener(listener);
		return menuItem;
	} // makeMenuItem()
	
	/** Creates the file menu */
	protected JMenu makeFileMenu() {
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("File menu");
		menu.add(makeMenuItem(LOAD_AUDIO, KeyEvent.VK_L, KeyEvent.VK_L, false));
		menu.add(makeMenuItem(LOAD_BEATS, KeyEvent.VK_B, KeyEvent.VK_B, false));
		menu.add(new JSeparator());
		menu.add(makeMenuItem(SAVE_AUDIO, KeyEvent.VK_A, KeyEvent.VK_A, false));
		menu.add(makeMenuItem(SAVE_BEATS, KeyEvent.VK_S, KeyEvent.VK_S, false));
		menu.add(new JSeparator());
		menu.add(makeMenuItem(EXIT, KeyEvent.VK_X, KeyEvent.VK_X, false));
		return menu;
	} // makeFileMenu()
	
	/** Creates the edit menu */
	protected JMenu makeEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menu.add(makeMenuItem(UNDO, KeyEvent.VK_U, 0, false));
		menu.add(makeMenuItem(REDO, KeyEvent.VK_R, 0, false));
		menu.add(new JSeparator());
		menu.add(makeMenuItem(EDIT_PERCUSSION, KeyEvent.VK_S, 0, false));
		menu.add(new JSeparator());
		menu.add(makeMenuItem(EDIT_PREFERENCES, KeyEvent.VK_P, 0, false));
		return menu;
	} // makeEditMenu()

	/** Creates the view menu */
	protected JMenu makeViewMenu() {
		JMenu menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		menu.add(makeMenuItem(SHOW_WAVE, KeyEvent.VK_W, 0, true));
		menu.add(makeMenuItem(SHOW_SPECTRO, KeyEvent.VK_S, 0, true));
		menu.add(makeMenuItem(SHOW_IBIS, KeyEvent.VK_I, 0, true));
		menu.add(makeMenuItem(SHOW_BEATS, KeyEvent.VK_B, 0, true));
		return menu;
	} // makeViewMenu()

	/** Creates the play menu */
	protected JMenu makePlayMenu() {
		JMenu menu = new JMenu("Play");
		menu.setMnemonic(KeyEvent.VK_L);
		menu.add(makeMenuItem(PLAY, KeyEvent.VK_P, 0, false));
		menu.add(makeMenuItem(PLAY_AUDIO, KeyEvent.VK_A, 0, false));
		menu.add(makeMenuItem(PLAY_BEATS, KeyEvent.VK_B, 0, false));
		menu.add(new JSeparator());
		menu.add(makeMenuItem(STOP, KeyEvent.VK_S, 0, false));
		return menu;
	} // makePlayMenu()

	/** Creates the beat tracking menu */
	protected JMenu makeBeatTrackMenu() {
		JMenu menu = new JMenu("BeatTrack");
		menu.setMnemonic(KeyEvent.VK_T);
		menu.add(makeMenuItem(BEAT_TRACK, KeyEvent.VK_B, KeyEvent.VK_B, false));
		menu.add(makeMenuItem(CLEAR_BEATS, KeyEvent.VK_Z, KeyEvent.VK_Z, false));
		menu.add(makeMenuItem(MARK_METRICAL_LEVEL, KeyEvent.VK_M, KeyEvent.VK_M, false));
		menu.add(makeMenuItem(CLEAR_METRICAL_LEVELS, KeyEvent.VK_L, 0, false));
		return menu;
	} // makeBeatTrackMenu()

	/** Loads and processes an audio file chosen with a file open dialog. */
	public void loadAudioData() {
		loadAudioData(chooser.getAudioInName());
	} // loadAudioData()
	
	/** Loads and processes a given audio file.
	 *  @param fileName The name of the audio file to open
	 */
	public void loadAudioData(String fileName) {
		audioPlayer.setCurrentFile(new AudioFile(fileName));
		audioProcessor.setInputFile(fileName);
		setTitle(title + " " + version + " - " + fileName);
		audioProcessor.processFile();
		audioProcessor.setDisplay(displayPanel);	// after processing
		updateDisplay(true);
	} // loadAudioData()

	/** Loads beat data from a file chosen by a file open dialog. */
	public void loadBeatData() {
		loadBeatData(chooser.getBeatInName());
	} // loadBeatData()
	
	/** Loads beat data from a given file.
	 *  @param fileName The name of the file to open
	 */
	public void loadBeatData(String fileName) {
		if (fileName != null) {
			try {
				if (fileName.endsWith(".tmf"))
					beats = EventList.readBeatTrackFile(fileName);
				else if (fileName.endsWith(".lbl"))
					beats = EventList.readLabelFile(fileName);
				else // if (fileName.endsWith(".txt"))
					beats = EventList.readBeatsAsText(fileName);
				setBeatData(beats);
			} catch (Exception e) {
				System.err.println("Error loading beat data: " + e);
			}
		}
	} // loadBeatData()

	/** Saves beat data to a file chosen by a file save dialog. */
	// TODO Check for the correct extension of exported file names
	public void saveBeatData() {
		try {
			beats.writeBeatTrackFile(chooser.getBeatOutName());
		} catch (Exception ex) {
			System.err.println("Error writing beat file: " + ex);
		}
	} // saveBeatData()
	
	/** Returns the list of beats */
	public EventList getBeatData() {
		return beats;
	} // getData()
	
	/** NOT USED:
	 *  Sets the data for the amplitude envelope and onsets on the display.
	 *  @param onsets The list of onset times
	 *  @param envTimes The list of times corresponding to envelope values (<code>envMag</code>)
	 *  @param envMags The values of the signal magnitude at each of these times
	 */
	public void setAudioData(double[] onsets, double[] envTimes, int[] envMags){
		displayPanel.setOnsets(onsets);
		displayPanel.setEnvTimes(envTimes);
		displayPanel.setMagnitudes(envMags);
		updateDisplay(true);
	} // setAudioData()

	/** NOT USED:
	 *  Sets the data for a MIDI piano-roll display.
	 *  @param onsets The onset times of each note
	 *  @param offsets The offset times of each note
	 *  @param pitches The MIDI pitches of each note
	 */
	public void setMidiData(double[] onsets, double[] offsets, int[] pitches) {
		displayPanel.setOnsets(onsets);
		displayPanel.setOffsets(offsets);
		displayPanel.setPitches(pitches);
		updateDisplay(true);
	} // setMidiData()

	/** Set the list of beats displayed on this window.
	 *  @param b The list of beats
	 */
	public void setBeatData(EventList b) {
		beats = b;
		displayPanel.setBeats(beats);
		updateDisplay(false);
		EditAction.clear();			// TODO: undo?
	} // setBeatData()

	/** Clear all beats.  Note that this action can't be undone. */
	public void clearBeatData() {
		EditAction.clear();
		displayPanel.clearBeats();	// TODO: undo?
	} // clearBeatData()
	
	/** NOT USED
	 *  Sets the data for displaying the spectrogram of the audio signal.
	 *  @param data The spectrogram data, indexed by time and frequency
	 *  @param len The number of frames of spectrogram data
	 *  @param tInc The time between successive frames (hop time)
	 *  @param overlap The ratio of hop size to frame size; used for centering the frames in the display
	 */
    public void setSpectroData(double[][] data, int len, double tInc,
							   double overlap) {
        displayPanel.setSpectro(data, len, tInc, overlap);
        updateDisplay(true);
    } // setSpectroData()

    /** Redraws the data panel when new data is loaded or the mode or preferences are changed.
     *  @param resetSelection Indicates whether the selected region should be reset
     */
	void updateDisplay(boolean resetSelection) {
		displayPanel.init(resetSelection);
		scroller.setMinimum(displayPanel.getMinimum());
		scroller.setMaximum(displayPanel.getMaximum());
		scroller.setValue(displayPanel.getValue());
		scroller.setVisibleAmount(displayPanel.getVisibleAmount());
		displayPanel.clearImage();
		displayPanel.repaintImage();
		displayPanel.repaint();
	} // updateDisplay()

	/** Changes the display mode (which elements are displayed on the data panel).
	 *  Constant values (SHOW_BEATS, etc.) are defined in BeatTrackDisplay.java
	 *  @param mode A bit string indicating the elements that should be switched on or off
	 *  @param flag Indicates whether the elements should be switched on (true) or off (false) 
	 */
	public void setMode(int mode, boolean flag) {
		if (flag)
			displayPanel.setMode(mode, 0);
		else
			displayPanel.setMode(0, mode);
		updateDisplay(false);
	} // setMode()

	/** Send a request to the audio player to skip to a given time if it is not playing */
	public void skipTo(double time) {
		audioPlayer.ifSetPosition(time);
	} // skipTo()
	
	/** Scroll the display by a given amount.
	 *  Used in dragging a selection beyond the left or right edge of the display.
	 *  @param dir The direction and number of units to scroll
	 */
	public void scroll(int dir) {
		scroller.setValue(scroller.getValue() + dir * scroller.getUnitIncrement());
	} // scroll()
	
	/** Scroll the display by a given amount in seconds. Used for keyboard input 
	 * @param incr amount of shift in seconds (positive to the right, vice versa)
	 * inserted by WG, Aug 2009.
	 */
	public void scroll(double incr) {
		displayPanel.setCurrentTime(displayPanel.getCurrentTime() + incr);
		skipTo(displayPanel.getCurrentTime());
		displayPanel.scrollTo(displayPanel.getCurrentTime(), false);
	}

	/** Copies default values into preferences dialog. */
	public void setPreferences() {
		preferences.setDouble(LOW_THRESHOLD, DEFAULT_LOW_THRESHOLD);
		preferences.setDouble(HIGH_THRESHOLD, DEFAULT_HIGH_THRESHOLD);
		preferences.setDouble(AUDIO_SCALE_FACTOR, DEFAULT_SCALE_FACTOR);
		preferences.setDouble(CLICK_VOLUME, DEFAULT_CLICK_VOLUME);
		// preferences.setChoice("Test", new String[]{"red", "green", "blue"}, 1);
	} // setPreferences()
	
	/** Opens preferences dialog and updates the display accordingly. */
	public void editPreferences() {
		preferences.setVisible(true);
		if (!preferences.wasCancelled()) {
			updateDisplay(false);
			AudioPlayer.volume = preferences.getDouble(CLICK_VOLUME);
		}
	} // editPreferences()
	
	/** Initialises the metrical levels dialog */
	public void setMetricalLevels() {
		metricalLevels.setInt(LEVEL, 1);
		metricalLevels.setInt(LENGTH, 1);
		metricalLevels.setInt(PHASE, 0);
	} // setMetricalLevels()
	
	/** Clears any metrical level annotations from the beats */
	public void clearMetricalLevels() {
		displayPanel.markMetricalLevel(0,1,0);
	} // clearMetricalLevels()
	
	/** Opens the metrical levels dialog and annotates the beat data correspondingly */
	public void markMetricalLevel() {
		metricalLevels.setVisible(true);
		if (!metricalLevels.wasCancelled())
			displayPanel.markMetricalLevel(metricalLevels.getInt(LEVEL),
					metricalLevels.getInt(LENGTH), metricalLevels.getInt(PHASE));
	} // markMetricalLevel()

	/** Initialises the percussion sound dialog and sound buffers */
	public void setPercussionSounds() {
		for (int i = 0; i < percussionCount; i++)
			percussionSounds.setString(PERCUSSION_STRINGS[i][0], PERCUSSION_STRINGS[i][1]);
		audioPlayer.initClicks();
	} // setPercussionSounds()

	/** Opens the percussion sound selection dialog and updates the sound buffers accordingly */
	public void editPercussionSounds() {
		percussionSounds.setVisible(true);
		if (!percussionSounds.wasCancelled())
			audioPlayer.initClicks();
	} // editPercussionSounds()

	/** Returns the file name of a percussion sound for playing at beat times.
	 *  @param level The requested metrical level
	 *  @return The file name of the percussion sound for the requested metrical level
	 */
	public String getPercussionSound(int level) {
		return percussionSounds.getString(PERCUSSION_STRINGS[level][0]);
	} // getPercussionSound()
	
	public void setOnsetDetectionParameter(double param1,double param2) {
		if (audioProcessor.audioFileName != null) {
			audioProcessor.findOnsets(param1, param2);
			audioProcessor.setDisplay(displayPanel); // after processing
			updateDisplay(false);
		}
	} // setOnsetDetectionParameter()
	
} // class GUI
