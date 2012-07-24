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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import at.ofai.music.util.Event;
import at.ofai.music.util.EventList;

/** Main panel of BeatRoot's GUI, which displays the audio and beat data
*  and allows editing, scrolling, selecting, etc
*  @author Simon Dixon
*/
public class BeatTrackDisplay
			 extends JPanel
			 implements MouseListener, MouseMotionListener {

	/** avoid compiler warning */
	public static final long serialVersionUID = 0;
	
	/** handle to the GUI (parent) object */
	protected GUI gui;
	
	/** width in pixels of this panel */
	protected final int defaultXSize = 1000;
	protected int xSize;
	
	/** height in pixels of this panel */
	protected int ySize;
	
	/** background image showing audio data but not beats */
	protected BufferedImage img;
	
	/** the Graphics object for drawing on <code>img</code> */
	protected Graphics gImage;
	
	/** the clip rectangle for painting only those areas which have changed */
	protected Rectangle clipRect;
	
	/** x-coordinate corresponding to time t = 0 (could be off-screen) */
	protected int x0;
	
	/** y-coordinate of top of data area */
	protected int yTop;
	
	/** y-coordinate of the bottom of the spectrogram and top of the amplitude envelope area */
	protected int yMid;
	
	/** y-coordinate of bottom of data area (position of x-axis) */
	protected int yBottom;
	
	/** size of font for inter-beat intervals and axis labels */
	protected int fontSize;

	/** beat data encoded as a list of Events */
	protected EventList beats;
	
	/** the current beat (in editing and annotation operations) */
	protected Event selectedBeat;
	
	/** location of selected beat (previous()) in the EventList <code>beats</code> */
	protected ListIterator<Event> beatPtr;
	
	/** time of selected beat (for undo) */
	protected double selectedBeatTime;
	
	/** beginning of selected data region */
	protected double startSelection;

	/** end of selected data region (may be < start) */
	protected double endSelection;

	/** flag indicating whether a region is selected */
	protected boolean regionSelected;
	
	/** the direction of scrolling when dragging outside the displayed bounds */
	protected int scrollDirection;
	
	/** a list of onset events for passing to the tempo induction and beat tracking methods */
	protected EventList onsetList;
	
	/** the times of onsets (in seconds) */
	protected double[] onsets;
	
	/** the times of offsets (in seconds) for MIDI data */
	protected double[] offsets;
	
	/** the pitches of MIDI notes */
	protected int[] pitches;
	
	/** the times corresponding to each point in the <code>magnitudes</code> array */
	protected double[] env;
	
	/** smoothed amplitude envelope of audio signal */ 
	protected int[] magnitudes;
	
	/** time interval between frames of spectrogram data (hop time) */
	protected double tInc;
	
	/** spectrogram data */
	protected double[][] spectro;
	
	/** number of frames of valid spectrogram data */
	protected int spectroLength;
	
	/** colour map used in the spectrogram */
	public static final Color[] colour = {
           new Color( 11,  0,  0),
           new Color( 21,  0,  0),
           new Color( 32,  0,  0),
           new Color( 43,  0,  0),
           new Color( 53,  0,  0),
           new Color( 64,  0,  0),
           new Color( 74,  0,  0),
           new Color( 85,  0,  0),
           new Color( 96,  0,  0),
           new Color(106,  0,  0),
           new Color(117,  0,  0),
           new Color(128,  0,  0),
           new Color(138,  0,  0),
           new Color(149,  0,  0),
           new Color(159,  0,  0),
           new Color(170,  0,  0),
           new Color(181,  0,  0),
           new Color(191,  0,  0),
           new Color(202,  0,  0),
           new Color(212,  0,  0),
           new Color(223,  0,  0),
           new Color(234,  0,  0),
           new Color(244,  0,  0),
           new Color(255,  0,  0),
           new Color(255, 11,  0),
           new Color(255, 21,  0),
           new Color(255, 32,  0),
           new Color(255, 43,  0),
           new Color(255, 53,  0),
           new Color(255, 64,  0),
           new Color(255, 74,  0),
           new Color(255, 85,  0),
           new Color(255, 96,  0),
           new Color(255,106,  0),
           new Color(255,117,  0),
           new Color(255,128,  0),
           new Color(255,138,  0),
           new Color(255,149,  0),
           new Color(255,159,  0),
           new Color(255,170,  0),
           new Color(255,181,  0),
           new Color(255,191,  0),
           new Color(255,202,  0),
           new Color(255,212,  0),
           new Color(255,223,  0),
           new Color(255,234,  0),
           new Color(255,244,  0),
           new Color(255,255,  0),
           new Color(255,255, 16),
           new Color(255,255, 32),
           new Color(255,255, 48),
           new Color(255,255, 64),
           new Color(255,255, 80),
           new Color(255,255, 96),
           new Color(255,255,112),
           new Color(255,255,128),
           new Color(255,255,143),
           new Color(255,255,159),
           new Color(255,255,175),
           new Color(255,255,191),
           new Color(255,255,207),
           new Color(255,255,223),
           new Color(255,255,239),
           new Color(255,255,255)};
	
	/** the colour of the amplitude envelope */
   static Color AUDIO_COLOUR = new Color(100,20,120);

   /** the colour of the MIDI piano roll */
   static Color MIDI_COLOUR = Color.BLUE;

   /** the colour of the onset markers on the amplitude envelope */
   static Color ONSET_COLOUR = Color.GREEN;

   /** the colour of the lines marking the beats */
   static Color BEAT_COLOUR = Color.BLACK;

   /** the colour of the text (for IBIs and axes) */
   static Color TEXT_COLOUR = Color.BLACK;

   /** the background colour of a selected region */
   static Color SELECTION_COLOUR = new Color(240,200,200);

	/** the threshold below which the minimum colour is used in the spectrogram */
   double loThreshold;

   /** the threshold above which the maximum colour is used in the spectrogram */
	double hiThreshold;
	
	/** the ratio of hop size to frame size (for aligning the spectrogram with the audio) */
	double overlap;
	
	/** Scaling factor for displaying the amplitude envelope */
	double audioScale;
	
	/** the minimum time that might need to be displayed for this input file (seconds) */
	double minimumTime;
	
	/** the maximum time that might need to be displayed for this input file (seconds) */
	double maximumTime;
	
	/** the time corresponding to the leftmost point on the panel (seconds) */
	double visibleTimeStart;
	
	/** the duration of the visible audio data (seconds) */
	double visibleTimeLength;
	
	/** the current time of audio output (for cursor position) */
	double currentTime;
	
	/** smallest displayed midi pitch */
	int midiMin;
	
	/** largest displayed midi pitch */
	int midiMax;
	
	/** font used for displaying text */
	Font font;
	
	/** metrics for calculating the size of printed strings */
	FontMetrics metrics;
	
	/** Constants defining the various elements that can be displayed on the main data panel */
	public static final int SHOW_BEATS = 1;
	public static final int SHOW_IBI = 2;
	public static final int SHOW_DATA = 4;
	public static final int SHOW_ONSETS = 8;
	public static final int SHOW_AUDIO = 1024;
	public static final int SHOW_SPECTRO = 2048;
	public static final int SHOW_MIDI = 4096;
	
	/** the default display mode */
	public static final int DEFAULT_MODE = SHOW_IBI | SHOW_ONSETS |
										   SHOW_DATA | SHOW_BEATS;
	
	/** a bit string indicating what items are displayed on the main data panel */
	int mode;
	
	/** for synchronising audio and graphics, a constant related to the size of the audio buffer in seconds */
	public static double adjustment = 0.15;
	
	/** For evaluation of beat tracking results, the largest difference allowed between
	 *  the computed and annotated beat times.
	 */
	public static double allowedError = 0.1;

	/** For evaluation of beat tracking results, indicates whether <code>allowedError</code>
	 *  is interpreted as absolute (in seconds) or relative (0-1).
	 */
	public static boolean useRelativeError = false;
	
	/** For evaluation, select whether to use the P-score or T-score */
	public static boolean usePScore = false;

	/** mode for scrolling the display:
	 * 	if true the cursor remains centred on the screen and the data scrolls;
	 *  if false the cursor moves and the data is changed just before
	 *  the cursor reaches the edge of the screen.
	 */
	public static boolean centred = false;

	/** Flag for enabling debugging output */
	public static boolean debug = false;

	/** Constructor:
	 *  @param g A handle to the parent GUI object
	 *  @param b The list of beats
	 */
	public BeatTrackDisplay(GUI g, EventList b) {
		gui = g;
		mode = DEFAULT_MODE;
		midiMin = 21;
		midiMax = 108;
		onsetList = null;
		onsets = new double[0]; 
		offsets = new double[0];
		pitches = new int[0];
		beats = b;
		beatPtr = beats.listIterator();
		env = new double[0];
		magnitudes = new int[0];
		clipRect = new Rectangle();
		// setBorder(BorderFactory.createLineBorder(Color.black));
		addMouseListener(this);
		addMouseMotionListener(this);
		xSize = -1;
		init(true);
	} // BeatTrackDisplay constructor

	/** Maps a double to a colour for displaying the spectrogram
	 * @param c The energy value
	 * @return The corresponding colour for this energy value
	 */
   Color getColour(double c) {
   	int max = colour.length - 1;
       int index = (int)(max - (c - loThreshold) *
							max / (hiThreshold - loThreshold));
       return colour[index < 0? 0: (index > max? max: index)];
   } // getColour()

   /** Initialises the panel after new data is loaded or preferences are changed.
    *  @param resetSelection Indicates whether the selected region should be cleared
    */
	synchronized public void init(boolean resetSelection) {
		selectedBeat = null;
		scrollDirection = 0;
		minimumTime = maximumTime = 0;
		if ((onsets.length > 0) && ((env.length == 0) || ((mode & SHOW_MIDI) != 0)))
			maximumTime = offsets[offsets.length - 1];
		else if (env.length > 0)
			maximumTime = env[env.length - 1];
		if (beats.size() != 0) {
			if (minimumTime > beats.l.get(0).keyDown)
				minimumTime = beats.l.get(0).keyDown;
			if (maximumTime < beats.l.get(beats.size()-1).keyDown)
				maximumTime = beats.l.get(beats.size()-1).keyDown;
		}
		if (resetSelection) {
			currentTime = 0;
			visibleTimeLength = 5.0;
		}
		if (centred) {
			minimumTime = minimumTime - visibleTimeLength / 2;
			maximumTime = maximumTime + visibleTimeLength / 2;
		}
		if (resetSelection || (endSelection > maximumTime) ||
							(startSelection > maximumTime) ||
							(visibleTimeStart > maximumTime) ||
							(visibleTimeStart < minimumTime)) {
			startSelection = endSelection = -1.0;
			visibleTimeStart = minimumTime;
		}
		if (resetSelection) {
			fontSize = 10;
			font = new Font("Helvetica", Font.PLAIN, fontSize);
			gui.setFont(font);
			metrics = getFontMetrics(font);
			yTop = 3 + metrics.getAscent();
			yMid = yTop + 352;
			yBottom = yMid + 150;
			ySize = yBottom + 20;
			if (xSize == -1)
				xSize = defaultXSize;
			setPreferredSize(new Dimension(xSize, ySize));
		}
		x0 = xSize;
		loThreshold = gui.preferences.getDouble(GUI.LOW_THRESHOLD);
		hiThreshold = gui.preferences.getDouble(GUI.HIGH_THRESHOLD);
		audioScale = gui.preferences.getDouble(GUI.AUDIO_SCALE_FACTOR);
	} // init()
	
	/** Changes the display mode (which elements are displayed on the panel).
	 *  Constant values (SHOW_BEATS, etc.) are defined above.
	 *  @param on  A bit string indicating the elements that should be switched on
	 *  @param off A bit string indicating the elements that should be switched off
	 */
	public void setMode(int on, int off) { mode |= on; mode &= ~0 - off; }

	/** @return The minimum time that might need to be displayed for this data */ 
	synchronized public int getMinimum() {
		return (int) (minimumTime * 1000.0);
	} // getMinimum()

	/** @return The maximum time that might need to be displayed for this data */ 
	synchronized public int getMaximum() {
		return (int) (maximumTime * 1000.0);
	} // getMaximum()

	/** Returns the time corresponding to the left edge of the display; used in scrolling
	 * @return The time in milliseconds of the left edge of the display
	 */
	synchronized public int getValue() {
		return (int) (visibleTimeStart * 1000.0);
	} // getValue()

	/** Scrolls the data so that the left edge of the panel corresponds to the given time.
	 *  @param value The given time in milliseconds
	 */
	synchronized public void setValue(int value) {
		if (value != getValue()) {
			visibleTimeStart = (double)value / 1000.0;
			if (centred) {
				currentTime = visibleTimeStart + visibleTimeLength / 2;
				gui.skipTo(currentTime);
			}
			if (scrollDirection < 0)
				endSelection = locationToTime(0);
			else if (scrollDirection > 0)
				endSelection = locationToTime(xSize);
			repaintImage();
		}
	} // setValue()

	/** Ensures that the given time is visible on the screen, by requesting a scroll if it is off the screen.
	 * @param time The current time of audio output in seconds
	 * @param isRunning Flag indicating whether audio output is active
	 */
	public void ensureShowing(double time, boolean isRunning) {
		if ((time <= visibleTimeStart) || (time >= visibleTimeStart + visibleTimeLength))
			scrollTo(time, isRunning);
		else
			repaint();
	} // ensureShowing()
	
	/** Updates the screen to show data for the current time, by scrolling and/or moving the cursor.
	 *  @param time The current time of audio output in seconds
	 *  @param isRunning Flag indicating whether audio output is active
	 */
	public void scrollTo(double time, boolean isRunning) {
		time = time - (isRunning? adjustment: 0);	// compensate for buffered audio
		if (!isRunning || Math.abs(currentTime - time) > 0.05) {	// only update every 50ms, or when stopped
			currentTime = time;
			if (centred)
				gui.scroller.setValue((int)Math.round(1000 * (time - visibleTimeLength / 2)));
			else if (time - visibleTimeStart > visibleTimeLength * 0.9)
				gui.scroller.setValue((int)Math.round(1000 * (time - visibleTimeLength * 0.1)));
			else if (time - visibleTimeStart < visibleTimeLength * 0.1)	
				gui.scroller.setValue((int)Math.round(1000 * (time - visibleTimeLength * 0.9)));
			repaint();
		}
	} // scrollTo()
	
	/** Gets the start (<code>selectedStart</code>) and length
	 *  (<code>selectedLength</code>) of the region of audio which has been
	 *  selected (if any).  The result is returned as a String containing
	 *  a list of attribute-value pairs, where the attribute and value
	 *  are separated by '=' and the pairs are separated by a space.
	 *  @param prefix Other attribute-value pairs 
	 *  @return The prefix followed by the start and length pairs if set
	 */
	synchronized public String getSelectedRegion(String prefix) {
		if (startSelection < 0)
			return prefix;
		if (prefix.length() > 0)
			prefix += ' ';
		prefix += "selectedStart=" + startSelection;
		if (endSelection >= 0)
			prefix += " selectedLength=" + (endSelection - startSelection);
		return prefix;
	} // getSelectedRegion()

	/** Gets the length of audio that is visible at any one time (i.e. zoom factor).
	 *  @return Length in milliseconds
	 */ 
	synchronized public int getVisibleAmount() {
		return (int) (visibleTimeLength * 1000.0);
	} // getVisibleAmount()

	/** Sets the length of audio that is visible at any one time (i.e. zoom factor).
	 *  @param msec Length in milliseconds
	 */
	synchronized public void setVisibleAmount(int msec) {
		visibleTimeLength = (double)msec / 1000.0;
		if (visibleTimeStart + visibleTimeLength > maximumTime)
			visibleTimeStart = maximumTime - visibleTimeLength;
		if (visibleTimeStart < minimumTime)
			visibleTimeStart = minimumTime;
		if (visibleTimeStart + visibleTimeLength > maximumTime)
			visibleTimeLength = maximumTime - minimumTime;
		x0 = (int)(-visibleTimeStart / visibleTimeLength * xSize);
		PanelScroller.passMessage = false;
		gui.scroller.setValue((int)(visibleTimeStart * 1000));
		gui.scroller.setVisibleAmount((int)(visibleTimeLength * 1000));
		gui.scroller.setValue((int)(visibleTimeStart * 1000));
		repaintImage();
		repaint();
		PanelScroller.passMessage = true;
	} // setVisibleAmount()

	/** Don't clear before repainting */
	public void update(Graphics g) {
		paint(g);
	} // update()

	/** Renders the panel.
	 *  @param g The Graphics object for painting on this panel. */
	synchronized public void paint(Graphics g) {
		//xSize = gui.getWidth();
		//System.out.println("xSize repainted: now="+xSize);
		/*if (xSize != getWidth()) {
			xSize = getWidth();
			System.out.println("xSize changed: now="+xSize);
			init(false);
		}*/
		if (scrollDirection != 0) {
			gui.scroll(scrollDirection);
		}
		if (img != null)
			g.drawImage(img, 0, 0, null);
		else {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		g.setFont(font);
		paintAxes(g);
		paintBeats(g);
	} // paint()

	/** Clears the background image to white */
	protected void clearImage() {
       if (gImage != null) {
           gImage.setColor(Color.white);
           gImage.fillRect(0, 0, xSize, ySize);
       }
   } // clearImage()
	
	/** Updates the background image, after creating it if necessary */
	synchronized protected void repaintImage() {
		if ((img == null) || (xSize != getWidth()) || (ySize != getHeight())) {
			xSize = getWidth();
			ySize = getHeight();
			x0 = xSize;
			img = getGraphicsConfiguration().createCompatibleImage(xSize, ySize);
			gImage = img.getGraphics();
			clearImage();
			clipRect.y = 0;
			clipRect.height = ySize;
		}
		int dx = (int)(-visibleTimeStart / visibleTimeLength * xSize) - x0;
		x0 += dx;
		if ((dx != 0) && (Math.abs(dx) < xSize))
			gImage.copyArea(0, 0, xSize, ySize, dx, 0);
		if (dx < 0) {			// scroll left
			clipRect.x = Math.max(0, xSize + dx);
			clipRect.width = xSize - clipRect.x;
		} else if (dx > 0) {	// scroll right
			clipRect.x = 0;
			clipRect.width = Math.min(xSize, dx);
		} else {				// not a scroll but a real repaint request
			clipRect.x = 0;
			clipRect.width = xSize;
		}
		//gImage.setClip(clipRect);
		paintBackground(gImage);
		if ((mode & SHOW_MIDI) != 0)
			paintMidiData(gImage);
		if ((mode & SHOW_AUDIO) != 0)
			paintAudioData(gImage);
		if ((mode & SHOW_SPECTRO) != 0)
			paintSpectroData(gImage);
		repaint();
	} // repaintImage()

	/** Paints the background of the spectrogram and selected area (if any).
	 *  @param g The Graphics object to paint to (i.e. <code>img</code>)
	 */
	synchronized protected void paintBackground(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
		if (startSelection >= 0) {
			int start, end;
			if (startSelection <= endSelection) {
				start = timeToLocation(startSelection);
				end = timeToLocation(endSelection);
			} else if (endSelection < 0) {
				start = timeToLocation(startSelection);
				end = xSize;
			} else {
				end = timeToLocation(startSelection);
				start = timeToLocation(endSelection);
			}
			if (start > xSize)
				return;
			else if (start < clipRect.x)
				start = clipRect.x;
			if (end < clipRect.x)
				return;
			else if (end >= clipRect.x + clipRect.width)
				end = clipRect.x + clipRect.width - 1;
			g.setColor(SELECTION_COLOUR);
			g.fillRect(start, yMid, end-start+1, 151);
		}
	} // paintBackground()

	/** Paints the time axis and labels 
	 *  @param g The Graphics object to paint to
	 */
	synchronized protected void paintAxes(Graphics g) {
		g.setColor(Color.gray);
		int cursorPosn = timeToLocation(currentTime);
		g.drawLine(cursorPosn, 0, cursorPosn, ySize);
		// g.drawLine(xSize/2, ySize - 5, xSize/2, ySize); // mark the centre
		g.setColor(Color.black);
		g.drawLine(0,yBottom,xSize,yBottom);
		double tickGap = Math.ceil(visibleTimeLength / 7.5);
		if (tickGap <= 1.0)
			tickGap = 1.0 / Math.ceil(7.5 / visibleTimeLength);
		double ticks = Math.ceil(visibleTimeStart / tickGap) * tickGap;
		for ( ; ticks < visibleTimeStart + visibleTimeLength; ticks += tickGap){
			String label = Double.toString(ticks);
			int position = label.indexOf('.');
			if (position < 0) {
				position = label.length();
				label += ".";
			}
			label += "000";
			label = label.substring(0, position + (tickGap < 0.5 ? 3 : 2));
			position = timeToLocation(ticks) - metrics.stringWidth(label) / 2;
			if ((position>0) && (position+metrics.stringWidth(label) < xSize)) {
				g.drawString(label, position, yBottom + metrics.getAscent() +5);
				position = timeToLocation(ticks);
				g.drawLine(position, yBottom-5, position, yBottom+5);
			}
		}
		g.drawString("["+beats.size()+" beats]", 5, this.yTop+10); // WG: added to show number of beats
		if (gui.audioPlayer.currentFile!=null) {  // WG: added to show length of audio file
			String label = Double.toString(gui.audioPlayer.currentFile.length / 
					gui.audioPlayer.currentFile.frameRate / gui.audioPlayer.currentFile.frameSize);
			int position = label.indexOf('.');
			if (position < 0) {
				position = label.length();
				label += ".";
			}
			label += "000";
			label = label.substring(0, position + (tickGap < 0.5 ? 3 : 2));
			g.drawString(label, xSize-metrics.stringWidth(label)-2, this.ySize-5);
		}
	} // paintAxes()

	/** Paints the beats and inter-beat intervals
	 *  @param g The Graphics object to paint to
	 */
	synchronized protected void paintBeats(Graphics g) {
		g.setColor(BEAT_COLOUR);
		int xLocation, prevLocation = 0;
		for (Event ev: beats.l) {
			xLocation = timeToLocation(ev.keyDown);
			if (xLocation < 0)
				continue;
			if (xLocation > xSize)
				break;
			if ((mode & SHOW_BEATS) != 0) {
				if (selectedBeat == ev)
					g.drawRect(xLocation-1, 0, 2, yBottom-1);
				else
					g.fillRect(xLocation-1, 0, 2, yBottom-1);
					//was g.drawLine(xLocation, 0, xLocation, yBottom-1);
				int pos = 4;
				int flag = ev.flags & 0x7f;
				for ( ; flag != 0; flag >>= 1, pos += 3)
					if ((flag & 1) != 0)
						g.drawLine(xLocation - 5, pos, xLocation + 5, pos);
			}
			if (((mode & SHOW_IBI) != 0) && (prevLocation != 0)) {
				// show inter-beat intervals (in msec)
				int xd = (int)(1000.0 * (locationToTime(xLocation) -
									 locationToTime(prevLocation)));
				String label = Integer.toString(xd);
				xd = (xLocation + prevLocation - metrics.stringWidth(label))/2;
				g.setColor(TEXT_COLOUR);
				g.drawString(label, xd, fontSize);
				g.setColor(BEAT_COLOUR);
			}
			prevLocation = xLocation;
		}
	} // paintBeats()

	/** Paints MIDI data in piano-roll notation.
	 *  @param g The Graphics object to paint to (i.e. <code>img</code>)
	 */
	synchronized protected void paintMidiData(Graphics g) {
		g.setColor(MIDI_COLOUR);
		int wd = (yMid - yTop) / (midiMax - midiMin + 1);
		for (int i=0; i<onsets.length; i++) {
			if ((pitches[i] < midiMin) || (pitches[i] > midiMax))
				continue;
			int xNoteOn = timeToLocation(onsets[i]);
			int xNoteOff = timeToLocation(offsets[i]);
			if (xNoteOff < clipRect.x)
				continue;
			if (xNoteOn > clipRect.x + clipRect.width)
				break;
			xNoteOff -= xNoteOn + 1;	// convert to offset
			if ((mode & SHOW_ONSETS) != 0) {
				g.setColor(ONSET_COLOUR);
				g.drawLine(xNoteOn, yBottom, xNoteOn, yBottom-100);
				g.drawLine(xNoteOn-1, yBottom-5, xNoteOn+1, yBottom-5);
				g.setColor(MIDI_COLOUR);
			}
			if ((mode & SHOW_DATA) != 0)
				g.fillRect(xNoteOn,wd*(midiMax-pitches[i]),xNoteOff,2);
		}
	} // paintMidiData()

	/** Paints the audio amplitude envelope and onset markers.
	 *  @param g The Graphics object to paint to (i.e. <code>img</code>)
	 */
	synchronized protected void paintAudioData(Graphics g) {
		g.setColor(AUDIO_COLOUR);
		int i = 0;
		if (env.length > 1)
			i = (int)(locationToTime(clipRect.x) / (env[1] - env[0]));
		int xPrev = 0;
		if (i < env.length)
			xPrev = timeToLocation(env[i==0? 0: i-1]);
		for (int j = 0; i < env.length; i++) {
			int xLocation = timeToLocation(env[i]);
			if (xLocation < clipRect.x)
				continue;
			if (xLocation > clipRect.x + clipRect.width)
				break;
			int yHi = yBottom - (int) (audioScale * magnitudes[i]);
			if (yHi < yMid + 5)
				yHi = yMid + 5;
			if ((mode & SHOW_DATA) != 0) {
				if (xPrev < xLocation - 1)
					g.fillRect(xPrev+1, yHi, xLocation-xPrev, yBottom - yHi);
				else
					g.drawLine(xLocation, yHi, xLocation, yBottom-1);
			}
			while ((j < onsets.length) &&
						(timeToLocation(onsets[j]) <= xPrev))
				j++;
			while ((j < onsets.length) &&
						(timeToLocation(onsets[j]) <= xLocation)) {
				g.setColor(ONSET_COLOUR);
				if ((mode & SHOW_ONSETS) != 0)
					g.fillRect(timeToLocation(onsets[j])-1, yHi-20, 3, 20);
				g.setColor(AUDIO_COLOUR);
				j++;
			}
			xPrev = xLocation;
		}
	} // paintAudioData()
	
	/** Paints the spectrogram data.
	 *  @param g The Graphics object to paint to (i.e. <code>img</code>)
	 */
	synchronized protected void paintSpectroData(Graphics g) {
		int sizeT = spectroLength;
		int sizeF = spectro[0].length;	// 84 (for audio, cf 88? for midi)
		int i = (int)(locationToTime(clipRect.x) / tInc) - 1;
		if (i < 0)
			i = 0;
		int yd = (yMid - yTop) / sizeF;	// 4 (cf midi 3?)
		for ( ; i < sizeT; i++) {
			int x1 = timeToLocation(tInc * (i + overlap / 2));
			int xd = timeToLocation(tInc * (i + overlap / 2 + 1)) - x1;
			if (x1+xd <= clipRect.x)
				continue;
			if (x1 > clipRect.x + clipRect.width)
				break;
			int y1 = yMid;
			for (int pitch = 0; pitch < sizeF; pitch++) {
				g.setColor(getColour(spectro[i][pitch]));
				y1 -= yd;
				g.fillRect(x1, y1, xd, yd);
			}
		}
	} // paintSpectroData()

	/** Finds the x-coordinate corresponding to the given time.
	 *  @param time Time in seconds
	 *  @return The corresponding x-coordinate
	 */
	synchronized public int timeToLocation(double time) {
		return (int)((time-visibleTimeStart)/visibleTimeLength*(double)xSize);
	}

	/** Finds the time corresponding to a given x-coordinate.
	 *  @param loc The given x-coordinate
	 *  @return The corresponding time in seconds
	 */
	synchronized public double locationToTime(int loc) {
		double tmp = (double)loc/(double)xSize * visibleTimeLength;
		if (tmp + visibleTimeStart < 0)
			return 0;
		return tmp + visibleTimeStart;
	}

	/** Finds the closest beat to a given time.
	 *  Returns with beatPtr pointing to the beat (as next()), unless the list is empty
	 *  @param x The horizontal location on the panel representing the time to search around
	 *  @param nextHighest If true, the returned beat must occur after the given time
	 */
	synchronized void findClosestBeat(int x, boolean nextHighest) {
		selectedBeat = null;
		double time = locationToTime(x);
		if (!beatPtr.hasNext() && !beatPtr.hasPrevious())	// empty list
			return;
		while (beatPtr.hasPrevious()) {
			if (time > beatPtr.previous().keyDown)
				break;
		}
		while (beatPtr.hasNext()) {
			if (time <= beatPtr.next().keyDown) {
				beatPtr.previous();
				break;
			}
		}
		if (nextHighest || !beatPtr.hasPrevious())	// found, or x <= first beat
			return;
		if (!beatPtr.hasNext()) {					// x > last beat
			beatPtr.previous();
			return;
		}
		double tn = beatPtr.next().keyDown;
		beatPtr.previous();
		double tp = beatPtr.previous().keyDown;
		if (Math.abs(time - tn) <= Math.abs(time - tp))
			beatPtr.next();
	} // findClosestBeat()
	
	/** Turns keyboard annotation of metrical levels on and off */
	public void toggleAnnotateMode() {
		if (selectedBeat == null) {
			selectBeat(xSize/2, -1);
			if (selectedBeat != null)
				ensureShowing(selectedBeat.keyDown, false);
		} else {
			selectedBeat = null;
		}
		repaint();
	} // toggleAnnotateMode()
	
	/** Selects the first beat */
	public void selectFirstBeat() {
		if (selectedBeat != null) {
			while (beatPtr.hasPrevious())
				beatPtr.previous();
			selectedBeat = beatPtr.next();
			ensureShowing(selectedBeat.keyDown, false);
		}
	} // selectFirstBeat()
	
	/** Selects the last beat */
	public void selectLastBeat() {
		if (selectedBeat != null) {
			while (beatPtr.hasNext())
				selectedBeat = beatPtr.next();
			ensureShowing(selectedBeat.keyDown, false);
		}
	} // selectLastBeat()
	
	/** Selects the beat after the currently selected beat */
	public void selectNextBeat() {
		if (selectedBeat != null) {
			if (beatPtr.hasNext())
				selectedBeat = beatPtr.next();
			ensureShowing(selectedBeat.keyDown, false);
		}
	} // selectNextBeat()
	
	/** Selects the beat before the currently selected beat */
	public void selectPreviousBeat() {
		if (selectedBeat != null) {
			beatPtr.previous();
			if (beatPtr.hasPrevious())
				selectedBeat = beatPtr.previous();
			beatPtr.next();
			ensureShowing(selectedBeat.keyDown, false);
		}
	} // selectPreviousBeat()

	/** Deletes the currently selected beat */
	public void removeSelectedBeat() {
		if (selectedBeat != null) {
			beatPtr.previous();
			Event e = beatPtr.next();
			beatPtr.remove();
			EditAction.add(e.keyDown, -1);
			if (beatPtr.hasNext())
				selectedBeat = beatPtr.next();
			else if (beatPtr.hasPrevious()) {
				beatPtr.previous();
				selectedBeat = beatPtr.next();
			} else
				selectedBeat = null;
			if (selectedBeat != null)
				ensureShowing(selectedBeat.keyDown, false);
		}
	} // removeSelectedBeat()

	/** Interpolates a new beat between the currently selected and the following beat */
	public void addAfterSelectedBeat() {
		if (selectedBeat != null) {
			double beatInterval;
			if (beatPtr.hasNext()) {
				beatInterval = beatPtr.next().keyDown - selectedBeat.keyDown;
				EditAction.add(-1, selectedBeat.keyDown + beatInterval / 2);
				addBeat(selectedBeat.keyDown + beatInterval / 2);
				if (!beatPtr.hasNext())
					beatPtr.previous();
				selectedBeat = beatPtr.next();
			} else {
				beatPtr.previous();
				if (beatPtr.hasPrevious()) {
					beatInterval = selectedBeat.keyDown - beatPtr.previous().keyDown;
					EditAction.add(-1, selectedBeat.keyDown + beatInterval);
					addBeat(selectedBeat.keyDown + beatInterval);
					beatPtr.previous();
					selectedBeat = beatPtr.next();
				} else {
					beatPtr.next();
					return;
				}
			}
			ensureShowing(selectedBeat.keyDown, false);
		}
	} // addAfterSelectedBeat()

	/** Marks the selected beat with all metrical levels up to and including x.
	 *  @param x The given highest metrical level
	 */
	public void setMetricalLevel(int x) {
		if (selectedBeat != null) {		// TODO: add undo
			if (x > 0)
				selectedBeat.flags = (1 << x) - 1;
			else
				selectedBeat.flags = 0;
			selectNextBeat();
		}
	} // setMetricalLevel()

	/** Marks (only) the selected metrical level as specified.
	 *  @param level The given metrical level
	 *  @param len The number of lowest-level beats in one unit of this metrical level
	 *  @param phase The position of the first lowest-level beat relative to the
	 *          boundaries of this metrical level, where 0 means it is aligned
	 */
	public void markMetricalLevel(int level, int len, int phase) {
		findClosestBeat(timeToLocation(startSelection), true);
		if (level > 0)
			level = 1 << (level - 1);
		phase = len - phase;
		while (beatPtr.hasNext()) {		// TODO: add undo
			Event ev = beatPtr.next();
			if ((endSelection >= 0) && (ev.keyDown > endSelection))
				break;
			if (level == 0)
				ev.flags = 0;
			else if (phase % len == 0)
				ev.flags |= level;
			else
				ev.flags &= -1 ^ level;
			phase++;
		}
		repaint();
	} // markMetricalLevel()
	
	/** Selects the nearest beat to a given x-coordinate.
	 *  @param x The x-coordinate (e.g. of a mouse-click) near which a beat is sought
	 *  @param err The allowed error in the beat location: if negative, no limit is
	 *   imposed on the beat, otherwise the beat must be within <code>err</code> pixels
	 *   of <code>x</code>. If no suitable beat is found, there is no selected beat.
	 */
	synchronized public void selectBeat(int x, int err) {
		findClosestBeat(x, false);
		if (beatPtr.hasNext()) {
			selectedBeat = beatPtr.next();
			if ((err < 0) || (Math.abs(x-timeToLocation(selectedBeat.keyDown)) <= err))
				return;
			else
				selectedBeat = null;
		}
	} // selectBeat()
	
	/** Finds the nearest detected onset time location to a given x-coordinate.
	 * @param x The x-coordinate (e.g. of a mouse-click) near shich a beat is sought
	 * @return The onset time (s) of the nearest detected onset.
	 * inserted by WG, 7 Aug 2009
	 */
	synchronized public double selectOnset(int x) {
		if (onsets.length == 0)
			return -1.0;
		int i = 0;
		while (i < onsets.length-1 && timeToLocation(onsets[i]) <= x){
			i++;
		}
		if (i == 0)
			return onsets[i];
		if (Math.abs(timeToLocation(onsets[i])-x) <= Math.abs(x-timeToLocation(onsets[i-1])))
			return onsets[i];
		else
			return onsets[i-1];
	} // selectOnset()

	/** Creates a new Event object representing a beat.
	 *  @param time The time of the beat in seconds
	 *  @param beatNum The index of the beat
	 *  @return The Event object representing the beat
	 */
	public static Event newBeat(double time, int beatNum) {
		return new Event(time,time, time, 56, 64, beatNum, 0, 1);
	} // newBeat()

	/** Adds a beat at the current playback time, with an adjustment for audio latency.
	 *  This was a failed attempt at a tapping interface to BeatRoot, as
	 *  the timing doesn't appear to be at all stable on my platform. */
	public void addBeatNow() {
		double time = gui.audioPlayer.getCurrentTime() - adjustment - 0.05;	// adjust for latencies
		addBeat(time);
		EditAction.add(-1, time);
		if (debug)
			System.err.printf("Add at: %5.3f\n", time);
	} // addBeatNow()

	//public void addBeatCurrentTime() {
	//	beatPtr.add(newBeat())
	//}
	
	/** Adds a beat at the given time.
	 *  @param time Time in seconds of the new beat
	 */
	public synchronized void addBeat(double time) {
		findClosestBeat(timeToLocation(time), true);
		beatPtr.add(newBeat(time,0));
		repaint();
	} // addBeat()

	/** Changes the time of a beat, and re-sorts the list. If no beat is found
	 *  at the given time, an error message is printed and nothing further occurs.
	 *  @param t1 The original time of the beat
	 *  @param t2 The new time of the beat
	 */
	public synchronized void moveBeat(double t1, double t2) {
		selectBeat(timeToLocation(t1), 0);
		if (selectedBeat == null)
			System.err.println("move(): no beat found");
		else {
			selectedBeat.keyDown = t2;
			reorderBeats();
		}
		selectedBeat = null;
		repaint();
	} // moveBeat()

	/** Deletes a beat.
	 *  @param time The time of the beat (in seconds)
	 */
	public synchronized void removeBeat(double time) {
		selectBeat(timeToLocation(time), 0);
		if (selectedBeat == null)
			System.err.println("removeBeat(): no beat found");
		else
			beatPtr.remove();
		selectedBeat = null;
		repaint();
	} // removeBeat()
	
	/** Clears all beats in the selected area and sets beatPtr 
	 *  to point between the beats surrounding the deleted area. */
	public synchronized void clearBeats() {
		if (startSelection == -1)
			endSelection = -1;
		findClosestBeat(timeToLocation(startSelection), true);
		while (beatPtr.hasNext()) {
			Event e = beatPtr.next();
			if ((endSelection >= 0) && (e.keyDown > endSelection)) {
				beatPtr.previous();
				break;
			}
			EditAction.add(e.keyDown, -1);
			beatPtr.remove();
		}
		repaint();
	} // clearBeats()
	
	/** The current beat (pointed to by <code>beatPtr</code>) is moved
	 *  to its correct place in the otherwise sorted list of beats. */
	public void reorderBeats() {
		beatPtr.previous();
		selectedBeat = beatPtr.next();
		beatPtr.remove();
		while (beatPtr.hasNext())
			if (selectedBeat.compareTo(beatPtr.next()) < 0) {
				beatPtr.previous();
				break;
			}
		while (beatPtr.hasPrevious())
			if (selectedBeat.compareTo(beatPtr.previous()) >= 0) {
				beatPtr.next();
				break;
			}
		beatPtr.add(selectedBeat);
	} // reorderBeats()
	
	/** Perform beat tracking where the GUI is not active;
	 *  there is no selected region.
	 *  @param events The onsets or peaks in a feature list
	 *  @return The list of beats, or an empty list if beat tracking fails
	 */
	public static EventList beatTrack(EventList events) {
		return beatTrack(events, null);
	}
	
	/** Perform beat tracking where the GUI is not active;
	 *  there is no selected region.
	 *  @param events The onsets or peaks in a feature list
	 *  @param beats The initial beats which are given, if any
	 *  @return The list of beats, or an empty list if beat tracking fails
	 */
	public static EventList beatTrack(EventList events, EventList beats) {
		AgentList agents = null;
		int count = 0;
		double beatTime = -1;
		if (beats != null) {
			count = beats.size() - 1;
			beatTime = beats.l.getLast().keyDown;
		}
		if (count > 0) { // tempo given by mean of initial beats
			double ioi = (beatTime - beats.l.getFirst().keyDown) / count;
			agents = new AgentList(new Agent(ioi), null);
		} else									// tempo not given; use tempo induction
			agents = Induction.beatInduction(events);
		if (beats != null)
			for (AgentList ptr = agents; ptr.ag != null; ptr = ptr.next) {
				ptr.ag.beatTime = beatTime;
				ptr.ag.beatCount = count;
				ptr.ag.events = new EventList(beats);
			}
		agents.beatTrack(events, -1);
		Agent best = agents.bestAgent();
		if (best != null) {
			best.fillBeats(beatTime);
			return best.events;
		}
		return new EventList();
	} // beatTrack()/1
	
	/** Performs automatic beat tracking and updates the GUI accordingly.
	 *  If a region is selected, the beats outside the region are kept
	 *  intact and the tempo preserved across the region boundaries. */
	public void beatTrack() {
		AgentList agents = null;
		double beatTime = -1.0;
		int count = 0;
		clearBeats();							// clears the selected region
		EventList endBeats = new EventList();
		while (beatPtr.hasNext()) {				// save beats after the selected region
			endBeats.add(beatPtr.next());
			beatPtr.remove();
		}
		if (beats.size() > 1) {					// tempo given by mean of initial beats
			count = beats.size() - 1;
			beatTime = beats.l.getLast().keyDown;
			double ioi = (beatTime - beats.l.getFirst().keyDown) / count;
			agents = new AgentList(new Agent(ioi), null);
		} else if (endBeats.size() > 1) {		// tempo given by mean of final beats
			double ioi = (endBeats.l.getLast().keyDown - endBeats.l.getFirst().keyDown) /
							(endBeats.size() - 1);
			agents = new AgentList(new Agent(ioi), null);
		} else									// tempo not given; use tempo induction
			agents = Induction.beatInduction(onsetList);
		for (AgentList ptr = agents; ptr.ag != null; ptr = ptr.next) {
			ptr.ag.beatTime = beatTime;
			ptr.ag.beatCount = count;
			ptr.ag.events = new EventList(beats);
		}
		//onsetList.print();
		agents.beatTrack(onsetList, endSelection);
		Agent best = agents.bestAgent();
		if (best != null) {
			best.fillBeats(startSelection);
			best.events.add(endBeats);
			gui.setBeatData(best.events);
		} else
			System.err.println("No best agent");
	} // beatTrack()

	/** Constant representing an unknown relationship between metrical levels */
	protected static final double UNKNOWN = -1.0;
	
	/** Finds the mean tempo (as inter-beat interval) from an array of beat times
	 *  @param d An array of beat times
	 *  @return The average inter-beat interval
	 */
	public static double getAverageIBI(double[] d) {
		if ((d == null) || (d.length < 2))
			return -1.0;
		return (d[d.length - 1] - d[0]) / (d.length - 1);
	} // getAverageIBI()
	
	/** Finds the median tempo (as inter-beat interval) from an array of beat times
	 *  @param d An array of beat times
	 *  @return The median inter-beat interval
	 */
	public static double getMedianIBI(double[] d) {
		if ((d == null) || (d.length < 2))
			return -1.0;
		double[] ibi = new double[d.length-1];
		for (int i = 1; i < d.length; i++)
			ibi[i-1] = d[i] - d[i-1];
		Arrays.sort(ibi);
		if (ibi.length % 2 == 0)
			return (ibi[ibi.length / 2] + ibi[ibi.length / 2 - 1]) / 2;
		else
			return ibi[ibi.length / 2];
	} // getAverageIBI()
	
	/** Estimates the metrical relationship between two beat sequences.
	 *  @param beats The system's beat times
	 *  @param correct The annotated beat times
	 *  @return A double encoding the metrical relationship between the sequences,
	 *   which ideally should be an integer or reciprocal of an integer.
	 */
	public static double getRhythmicLevel(double[] beats, double[] correct) {
	    // correct is "this"
	    if ((beats.length < 2) || (correct.length < 2))
	        return UNKNOWN;
	    double scoreIBI = getAverageIBI(correct);
	    double resultIBI = getAverageIBI(beats);
	    double ratio = scoreIBI / resultIBI;
	    double roundedRatio = UNKNOWN;  // indicates error and avoids div by 0 error
	    if (     (0.22 < ratio) && (ratio < 0.28))
	        roundedRatio = 4.0;
	    else if ((0.3 < ratio) && (ratio < 0.367))
	        roundedRatio = 3.0;
	    else if ((0.4 < ratio) && (ratio < 0.6))
	        roundedRatio = 2.0;
	    else if ((0.6 < ratio) && (ratio < 0.75))
	        roundedRatio = 1.5;
	    else if ((0.9 < ratio) && (ratio < 1.3))
	        roundedRatio = 1.0;
       else if ((1.35 < ratio) && (ratio < 1.6))
           roundedRatio = 2.0/3.0;
       else if ((1.8 < ratio) && (ratio < 2.5))
           roundedRatio = 0.5;
       else if ((2.7 < ratio) && (ratio < 3.5))
           roundedRatio = 1.0/3.0;
       else if ((3.6 < ratio) && (ratio < 4.6))
           roundedRatio = 0.25;
       else if ((7.6 < ratio) && (ratio < 8.6))
           roundedRatio = 0.125;
       return roundedRatio;
   } // getRhythmicLevel()

	/** Evaluates a beat tracking solution against an annotation of the data.
	 *  @param beatsFile The file name of the annotation data
	 */
	public void evaluate(String beatsFile) {
		evaluate(beatsFile, beats);
	} // evaluate()
	
	/** Evaluates a beat tracking solution against an annotation of the data.
	 *  @param beatsFile The file name of the annotation data
	 *  @param beats The list of beats to be evaluated
	 */
	public static void evaluate(String beatsFile, EventList beats) {
		evaluate(beatsFile, beats.toOnsetArray());
	} // evaluate()

	/** Evaluates a beat tracking solution against an annotation of the data.
	 *  @param annotationFile The file name of the annotation data
	 *  @param testFile The file name containing the beat times (as text) to be evaluated
	 */
	public static void evaluate(String annotationFile, String testFile) {
		double[] beats = AudioProcessor.getFeatures(testFile);
	    if (beats == null) {
	    	System.err.println("Error in test file");
	        return;
	    }
	    evaluate(annotationFile, beats);
	} // evaluate()
	
	/** Evaluates a beat tracking solution against an annotation of the data.
	 *  @param beatsFile The file name of the annotation data
	 *  @param beatsArr The array of beat times to be evaluated
	 */
	public static void evaluate(String beatsFile, double[] beatsArr) {
		double[] correct = AudioProcessor.getFeatures(beatsFile);
	    if ((correct == null) || (correct.length == 0)) {
	    	System.err.println("Error in evaluation file");
	        return;
	    }
	    int fp = 0;
	    int fn = 0;
	    int ok = 0;
	    int extra = 0;
	    double metricalLevel = getRhythmicLevel(beatsArr, correct);
	    double errorWindow = allowedError;
	    if (useRelativeError)
	    	errorWindow *= getMedianIBI(correct);
	    // double[] revBeats = makeRealBeatList(correctBeats, metricalLevel, true);
	    int b = 0;	// index of beats to be evaluated
	    int c = 0;	// index of correct (annotated) beats
	    for ( ; b < beatsArr.length && (beatsArr[b] < correct[c]); b++)
	        extra++;    // skip any "beats" before music starts
	    while ((c < correct.length) && (b < beatsArr.length)) {
	        if (Math.abs(beatsArr[b] - correct[c]) < errorWindow) {
	            ok++;
	            b++;
	            c++;
	        } else if (beatsArr[b] < correct[c]) {
	            fp++;
	            b++;
	        } else if (beatsArr[b] > correct[c]) {
	            fn++;
	            c++;
	        }
	    }
       fn += correct.length - c;
	    while (b < beatsArr.length) {
	        if (correct[correct.length - 1] < beatsArr[b])
	            extra++;
	        else
	            fp++;
	        b++;
	    }
	    if (usePScore) {
	    	System.out.printf("%4.2f %5.3f %5.3f", metricalLevel, getMedianIBI(correct), getMedianIBI(beatsArr));
	    	System.out.printf("  ok: " + ok + "  f+: " + fp + "  f-: " + fn +
	       				  "  Score: %5.1f\n", (double)(ok*100) / (double)(Math.max(fp, fn) + ok));
	    } else {
	    	System.out.printf("%4.2f %5.3f %5.3f", metricalLevel, getAverageIBI(correct), getAverageIBI(beatsArr));
	    	System.out.printf("  ok: " + ok + "  f+: " + fp + "  f-: " + fn +
	       				  "  Score: %5.1f\n", (double)(ok*100) / (double)(fp+fn+ok));
	    }
	    if (ok + fp + extra != beatsArr.length)
	    	System.err.println("Beat count wrong " + extra + " " + beatsArr.length + " " + correct.length);
	} // evaluate()


	// Implementation of interfaces MouseListener and MouseMotionListener
	
	/** Ignore composite events (already processed as press/drag/release events) */
	public void mouseClicked(MouseEvent e) {}
	
	/** Ignore mouse exit events */
	public void mouseExited(MouseEvent e) {}
	
	/** Ignore mouse movement with no button pressed */
	public void mouseMoved(MouseEvent e) {}

	/** Request focus for key events whenever the mouse enters the window. */
	public void mouseEntered(MouseEvent e) { requestFocusInWindow(); }

	/** A mouse button press can be used to move (left button), add (middle button),
	 *  or delete (right button) a beat, or to select or deselect a region, by clicking
	 *  on the axis.
	 * 	@param e The object describing the event that occurred
	 */
	public void mousePressed(MouseEvent e) {
		synchronized(this) {
			int x = e.getX();
			int y = e.getY();
			if (e.isControlDown()) { // WG added CTRL + Left Mouse shifts beats along detected onsets
				if ((y >= yTop) && (y < yBottom) && // all clicks in the spectrogram
						SwingUtilities.isLeftMouseButton(e)) { // Left Button
					selectBeat(x, 10); 
					if (selectedBeat != null)
						//selectedBeatTime = selectedBeat.keyDown;
						selectedBeatTime = selectOnset(x);
				} else if ((y >= yTop) && (y < yBottom)) { // all clicks in the spectrogram
					if (SwingUtilities.isRightMouseButton(e)) { // WG changed from Middle to Right
						addBeat(locationToTime(x));
						beatPtr.previous();
						selectedBeat = beatPtr.next();
						selectedBeatTime = -1;
					} else {
						selectBeat(x, 10);
						if (selectedBeat != null)
							selectedBeatTime = selectedBeat.keyDown;
					}
				}
			} else {
				if ((y > 0) && (y < yTop)) {
					if (!gui.audioPlayer.playing) {
						currentTime = locationToTime(x);
						gui.skipTo(currentTime);
						scrollTo(currentTime, false);
					}
				} else if ((y >= yTop) && (y < yBottom)) { // all clicks in the spectrogram
					if (SwingUtilities.isRightMouseButton(e)) { // WG changed from Middle to Right
						addBeat(locationToTime(x));
						beatPtr.previous();
						selectedBeat = beatPtr.next();
						selectedBeatTime = -1;
					} else {
						selectBeat(x, 10);
						if (selectedBeat != null)
							selectedBeatTime = selectedBeat.keyDown;
					}
				} else if ((y >= yBottom) && (y < ySize)) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						regionSelected = true;
						if (e.isShiftDown() && (startSelection >= 0)) 
							endSelection = locationToTime(x);
						else {
							startSelection = locationToTime(x);
							endSelection = -1.0;
						}
					} else {
						regionSelected = false;
						startSelection = endSelection = -1.0;
						repaintImage();
					}
				}
			} 
			repaint();
		}
	} // mousePressed()

	/** Called when the mouse is moved with a button down; if a beat is selected, it is moved under
	 *  the cursor, and if a region is selected it is expanded or shrunk under the cursor.
	 */
	public void mouseDragged(MouseEvent e) {
		synchronized(this) {
			int x = e.getX();
			int y = e.getY();
			if ((selectedBeat != null) && (x >= 0) && (x <= xSize) &&
					(y >= 0) && (y <= ySize)) {
				if (!SwingUtilities.isMiddleMouseButton(e)) { 
					if (e.isControlDown()) {
						selectedBeat.keyDown = selectOnset(x);

					} else {
						selectedBeat.keyDown = locationToTime(x);
					}
					reorderBeats();
				}
			} else if (regionSelected) {
				if ((startSelection >= 0) && SwingUtilities.isLeftMouseButton(e)) {
					if (x < 0)
						scrollDirection = -1;
					else if (x >= xSize)
						scrollDirection = 1;
					else
						scrollDirection = 0;
					if (Math.abs(timeToLocation(startSelection) - x) <= 15) // WG changed from '==0' to '<=15'; to avoid region selection, when all to end selection was desired! Aug 2009
						endSelection = -1;
					else
						endSelection = locationToTime(x);
				}
				repaintImage();
			}
			repaint();
		}
	} // mouseDragged()

	/** Called when the mouse button is released, to finalise move/add/delete operations. */
	public void mouseReleased(MouseEvent e) {
		mouseDragged(e);
		if (selectedBeat != null) {
			if (SwingUtilities.isLeftMouseButton(e)) // move
				EditAction.add(selectedBeatTime, selectedBeat.keyDown);
			else if (SwingUtilities.isRightMouseButton(e)) // add // WG changed from Middle to Right
				EditAction.add(-1, selectedBeat.keyDown);
			else if (SwingUtilities.isMiddleMouseButton(e)) { // right button, delete
				beatPtr.remove();
				EditAction.add(selectedBeatTime, -1);
			}
		}
		selectedBeat = null;
		regionSelected = false;
		scrollDirection = 0;
	} // mouseReleased()

	
	// Various get and set methods
	
	/** @return the list of beats */
	synchronized public EventList getBeats() {
		return beats;
	} // getBeats()

	/** @return the array of onset times */
	synchronized public double[] getOnsets() {
		return onsets;
	} // getOnsets()

	/** @return the array of offset times */
	synchronized public double[] getOffsets() {
		return offsets;
	} // getOffsets()

	/** @return the array of MIDI pitches */
	synchronized public int[] getPitches() {
		return pitches;
	} // getPitches()

	/** Sets the onset times as a list of Events, for use by the beat tracking methods. 
	 *  @param on The times of onsets in seconds
	 */
	synchronized public void setOnsetList(EventList on) {
		onsetList = on;
	} // setOnsetList()

	/** Sets the array of onset times, for displaying MIDI or audio input data.
	 *  @param on The times of onsets in seconds
	 */
	synchronized public void setOnsets(double[] on) {
		onsets = on;
	} // setOnsets()

	/** Sets the array of offset times, for displaying MIDI input data.
	 *  @param off The array of MIDI offset times
	 */
	synchronized public void setOffsets(double[] off) {
		offsets = off;
		// setMode(SHOW_MIDI, SHOW_AUDIO | SHOW_SPECTRO);
	} // setOffsets()

	/** Sets the array of times of amplitude envelope points, for displaying.
	 *  @param envTimes The array of times in seconds corresponding to the values in <code>magnitudes</code>
	 */
	synchronized public void setEnvTimes(double[] envTimes) {
		env = envTimes;
		setMode(SHOW_AUDIO, SHOW_MIDI);
	} // setEnvTimes()

	/** Sets the array of magnitude values, for displaying.
	 *  @param mag The array of amplitude envelope values
	 */
	synchronized public void setMagnitudes(int[] mag) {
		magnitudes = mag;
	} // setMagnitudes()

	/** Sets the array of pitch values, for displaying MIDI input data.
	 *  @param p The array of MIDI pitch values
	 */
	synchronized public void setPitches(int[] p) {
		pitches = p;
	} // setPitches()

	/** Sets the list of beats.
	 * @param b The list of beats
	 */
	synchronized public void setBeats(EventList b) {
		beats = b;
		selectedBeat = null;
		beatPtr = beats.listIterator();
	} // setBeats()

	/** Sets the data for the spectrogram.
	 * @param spec The spectrogram data, indexed by time and frequency
	 * @param len The number of frames of spectrogram data
	 * @param inc The time interval between frames of spectrogram data
	 * @param lap The ratio between hop size and frame size
	 */
	synchronized public void setSpectro(double[][] spec, int len, double inc, double lap) {
		setMode(SHOW_AUDIO | SHOW_SPECTRO, SHOW_MIDI);
		spectro = spec;
		spectroLength = len;
		tInc = inc;
		overlap = lap;
	} // setSpectro()
	
	/**
	 * WG: To resize the panel in the x dimension.
	 */
	public void resizeX() {
		setPreferredSize(new Dimension(xSize,ySize));
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(double currentTime) {
		this.currentTime = currentTime;
	}
	
	public void setOnsetDetectionParam(double param1,double param2) {
		gui.setOnsetDetectionParameter(param1,param2);
	}

} // class BeatTrackDisplay