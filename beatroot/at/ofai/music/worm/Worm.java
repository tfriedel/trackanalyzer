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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.WindowConstants;

import at.ofai.music.util.FrameMargins;
import at.ofai.music.util.Format;

public class Worm extends JPanel implements Runnable, HierarchyBoundsListener {
	static final long serialVersionUID = 0;
	double[] x;				// Circular buffer of x-coordinates (in BPM)
	double[] y;				// Circular buffer of y-coordinates (in dB)
	String[] labels;		// Circular buffer of labels
	int tail;				// pointer for circular buffer
	private double xmin, xmax, ymin, ymax,		// Extremes of plotable display
			xSum, xCount,						// For autoScale() on x-axis
			xScale, yScale;						// To convert values to pixels
	int xSize, ySize;							// Size of this panel 
	Color[] rimColours, bodyColours;			// Range of colours of worm
	static final int STOP = 0, PLAY = 1, PAUSE = 2;
	int state;
	static final int MIN_WORM_SIZE = 4;	// Diameter of worm
	static final int WORM_SIZE = 24;	// Diameter of worm
	static final double X_MIN_DEF = 60;	// Default is always
	static final double X_MAX_DEF = 120;	//  on the screen
	static final double Y_MIN_DEF = 35;
	static final double Y_MAX_DEF = 85;
	double[] xmem;	// memory of past values for smoothing
	double[] ymem;
	int memSize;	// number of points in xmem and ymem
	int smoothMode;
	public static final int NONE=0, EXPONENTIAL=1, HALF_GAUSS=2, FULL_GAUSS=3;
	public static final String[] smoothLabels = {
		"No Smoothing", "Exponential", "Half Gaussian", "Full Gaussian"};
	static final int DEFAULT_MODE = HALF_GAUSS;
	static final double sDecay = 0.97;		// for automatic scaling
	static final double xDecay = 0.98;		// for exponential smoothing
	static final double yDecay = 0.97;
	// Decay constants: 0.5000 -> -6dB / 1pt		2 ^ (-1 / k)
	//                  0.7071 -> -6dB / 2pt
	//                  0.7937 -> -6dB / 3pt 
	//                  0.8409 -> -6dB / 4pt 
	//                  0.8706 -> -6dB / 5pt 
	//                  0.8909 -> -6dB / 6pt 
	//                  0.9389 -> -6dB / 11pt 
	//                  0.9576 -> -6dB / 16pt 
	//                  0.9675 -> -6dB / 21pt 
	//                  0.9737 -> -6dB / 26pt 
	//                  0.9779 -> -6dB / 31pt 
	boolean autoScaleMode;
	JButton playButton;
	JCheckBoxMenuItem autoIndicator;
	JRadioButtonMenuItem[] smoothButtons;
	AudioWorm audio;
	int wait;		// Number of points to hold back (due to audio buffer size)
	double framePeriod;		// Time between successive points
	String inputPath, inputFile, matchFile, wormFileName,
			loudnessUnits, tempoUnits;
	double timingOffset;
	JFrame theFrame;
	WormControlPanel controlPanel;
	WormScrollBar scrollBar;
	WormFile wormFile;
	Thread playThread;

	public Worm(JFrame f) {
		x = new double[WormConstants.wormLength];
		y = new double[WormConstants.wormLength];
		labels = new String[WormConstants.wormLength];
		xmem = new double[WormConstants.wormLength];
		ymem = new double[WormConstants.wormLength];
		rimColours = new Color[WormConstants.wormLength];
		bodyColours = new Color[WormConstants.wormLength];
		smoothMode = DEFAULT_MODE;
		autoScaleMode = true;
		setGlow(false);
		loudnessUnits = "dB";
		tempoUnits = "BPM";
		init();
		theFrame = f;
		playThread = new Thread(this);
		playThread.start();
		xSize = WormConstants.X_SZ;
		ySize = WormConstants.Y_SZ;
		setSize(xSize, ySize);
		repaint();
	} // default constructor

	public void setGlow(boolean flag) {
		if (flag)
			WormConstants.setNightColours();
		else
			WormConstants.setDayColours();
		int r1 = WormConstants.wormHeadColor.getRed();
		int r2 = WormConstants.wormTailColor.getRed();
		int r3 = WormConstants.wormHeadRimColor.getRed();
		int r4 = WormConstants.wormTailRimColor.getRed();
		int g1 = WormConstants.wormHeadColor.getGreen();
		int g2 = WormConstants.wormTailColor.getGreen();
		int g3 = WormConstants.wormHeadRimColor.getGreen();
		int g4 = WormConstants.wormTailRimColor.getGreen();
		int b1 = WormConstants.wormHeadColor.getBlue();
		int b2 = WormConstants.wormTailColor.getBlue();
		int b3 = WormConstants.wormHeadRimColor.getBlue();
		int b4 = WormConstants.wormTailRimColor.getBlue();
		for (int i = 0; i < WormConstants.wormLength; i++) {
			bodyColours[i] = new Color(
	(r1 * i + r2 * (WormConstants.wormLength - i)) / WormConstants.wormLength,
	(g1 * i + g2 * (WormConstants.wormLength - i)) / WormConstants.wormLength,
	(b1 * i + b2 * (WormConstants.wormLength - i)) / WormConstants.wormLength);
			rimColours[i] = new Color(
	(r3 * i + r4 * (WormConstants.wormLength - i)) / WormConstants.wormLength,
	(g3 * i + g4 * (WormConstants.wormLength - i)) / WormConstants.wormLength,
	(b3 * i + b4 * (WormConstants.wormLength - i)) / WormConstants.wormLength);
		}
		if (controlPanel != null) {
			Component[] c = controlPanel.getComponents();
			for (int i = 0; i < c.length; i++) {
				c[i].setForeground(WormConstants.buttonTextColor);
				c[i].setBackground(WormConstants.buttonColor);
			}
		}
		if (scrollBar != null)
			scrollBar.setBackground(WormConstants.backgroundColor);
		repaint();
	} // setGlow()
	
	void init() {
		state = STOP;
		clear();
		xRescale(X_MIN_DEF, X_MAX_DEF);
		yRescale(Y_MIN_DEF, Y_MAX_DEF);
		wait = 0;
		framePeriod = 0;
		timingOffset = 0;
		xSum = 0.600;
	} // init()

	public void clearWithoutRepaint() {
		tail = 0;
		memSize = 0;
		for (int i=0; i < WormConstants.wormLength; i++)
			x[i] = -1;
		xCount = 0;
	} // clearWithoutRepaint()

	public void clear() {
		clearWithoutRepaint();
		repaint();
	} // clear()

	void editParameters() {
		if (wormFile != null)
			wormFile.editParameters();
	} // editParameters()

	void save(String s) {
		if ((wormFile != null) && (s != null))
			wormFile.write(s);
	}
	void setInputFile(String s) { inputFile = s; }
	void setInputFile(String path, String file) {
		inputPath = path;
		if (!path.endsWith("/"))
			inputPath += '/';
		inputFile = file;
	}
	String getInputFile() { return inputFile; }
	String getInputPath() { return inputPath; }
	void setMatchFile(String s) { matchFile = s; }
	String getMatchFile() { return matchFile; }
	void clearWormFile() {
		clear();
		wormFileName = null;
		wormFile = null;
	}
	void setWormFile(String s) {
		if ((s != null) && (s.length() > 0)) {
			clear();
			wormFileName = s;
			wormFile = new WormFile(this, s);
		}
	}
	public WormFile getWormFile() { return wormFile; }
	public String getWormFileName() { return wormFileName; }
	void setPlayButton(JButton b) { playButton = b; }
	void setAutoButton(JCheckBoxMenuItem b) {
		autoIndicator = b;
		autoIndicator.setSelected(autoScaleMode);
	}
	void setSmoothButtons(JRadioButtonMenuItem[] sb) {
		smoothButtons = sb;
		smoothButtons[smoothMode].setSelected(true);
	}
	public void setDelay(int t) { wait = t; }
	public void setFramePeriod(double t) { framePeriod = t; }
	void setSmoothMode(int mode) {
		smoothMode = mode;
		if (smoothButtons != null)
			smoothButtons[mode].setSelected(true);
	}
	int getSmoothMode() { return smoothMode; }
	public void smooth() {
		if (wormFile != null) {
			new WormSmoothDialog(this, wormFile);
		}
	}
	void setFileDelay(int d) { AudioWorm.fileDelay = d; }
	void setFileDelayString(String s) {
		try {
			AudioWorm.fileDelay = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			System.err.println("Invalid delay: " + s);
		}
	}
	int getFileDelay() { return AudioWorm.fileDelay; }
	String getFileDelayString() { return "" + AudioWorm.fileDelay; }
	void setTimingOffset(double d) { timingOffset = d; }
	void setTimingOffsetString(String s) {
		try {
			timingOffset = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			System.err.println("Invalid offset: " + s);
		}
	}
	String getTimingOffsetString() { return Format.d(timingOffset, 4); }
	double getTimingOffset() { return timingOffset; }
	void setControlPanel(WormControlPanel p) { controlPanel = p; }
	void setScrollBar(WormScrollBar p) { scrollBar = p; }
	void setTitle(String t) { theFrame.setTitle(t); }
	void setLoudnessUnits(String s) { loudnessUnits = s; }
	void setTempoUnits(String s) { tempoUnits = s; }
	void setAxis(String s) {
		StringTokenizer tk = new StringTokenizer(s);
		try {
			xRescale(Double.parseDouble(tk.nextToken()),
					 Double.parseDouble(tk.nextToken()));
			yRescale(Double.parseDouble(tk.nextToken()),
					 Double.parseDouble(tk.nextToken()));
			setAutoScaleMode(false);
		} catch (NoSuchElementException e) {	// ignore illegal values
			System.err.println("Illegal axes specification: " + e);
		} catch (NumberFormatException e) {		// ignore illegal values
			System.err.println("Illegal axes specification: " + e);
		}
	} // setAxis()
	
	// Starts/continues playing in a separate thread; immediately returns
	void play() {
		playButton.setText("Pause");
		playButton.repaint();
		if (state == STOP) {
			clear();
			audio = new AudioWorm(this);
		}
		state = PLAY;
		audio.start();
		synchronized(this) { // run() can now enter loop to process audio blocks
			notify();		 // informs playThread that it can start playing
		}
	} // play()

	public void run() {		// Code for play, executed as separate thread
		while (true) {		// This is always running
			try {
				synchronized(this) {	// wait until there is something to play
					wait();
				}
				Thread.sleep(200);	// wait 0.2s
				try {
					while ((state == PLAY) && audio.nextBlock())
						;
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					audio.ti.showTime();
				}
				//System.out.println("loop ended " + (state != PLAY));
				if (state == PLAY) {		// end of file; let audio drain
				//	audio.ti.saveHist();	//SD for dance test
					for ( ; wait > 0; wait--) {
					//	System.err.println("DEBUG: wait = " + wait);
						repaint();
						Thread.sleep((int)(AudioWorm.averageCount *
										AudioWorm.windowTime * 1000));	// wait 1.2s
					}
					//System.err.println("DEBUG: wait = " + wait);
					repaint();
					stop();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	} // run()

	void pause() {
		if (state == PLAY) {
			state = PAUSE;
			playButton.setText("Cont");
			playButton.repaint();
			audio.pause();
		}
	} // pause()

	void stop() {
		state = STOP;
		playButton.setText("Play");
		playButton.repaint();
		if (audio != null)
			audio.stop();
	} // stop()

	public void paint(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getHeight();
		// Paint background
		g.setColor(WormConstants.backgroundColor);
		g.fillRect(0, 0, xSize, ySize);
		if (xCount == 0) {
			int x = xSize / 2;
			int y = ySize / 2;
			g.drawImage(WormIcon.getWormIcon(2,theFrame), x - 150, y - 50, null);
			g.setColor(WormConstants.axesColor);
			g.drawString(WormConstants.title,
						 x - fm.stringWidth(WormConstants.title) / 2, y + 70);
			return;
		}
		// Draw worm itself
		int labelLeft = xSize - WormConstants.sideMargin -
						fm.stringWidth("Time:9999.9m");
		int labelRight = xSize - WormConstants.sideMargin - fm.stringWidth("m");
		int labelHeight = WormConstants.footMargin + ht + 10;
		String barLabel = null, beatLabel = null, trackLabel = null,
			   timeLabel = null, prev = "0";
		for (int i = 0; i < WormConstants.wormLength - wait; i++) {
			int ind = (tail + i) % WormConstants.wormLength;
			int d = MIN_WORM_SIZE +
						WORM_SIZE * i / (WormConstants.wormLength - wait);
			int xx = WormConstants.sideMargin + (int)((x[ind] - xmin) * xScale);
			int yy = WormConstants.footMargin + (int)((ymax - y[ind]) * yScale);
			if (x[ind] >= 0) {	// if there is data, draw a circle
				int e = 0;
				if (labels[ind].indexOf(':') < 0)
					timeLabel = labels[ind];
				else {
					StringTokenizer st = new StringTokenizer(labels[ind], ":");
					barLabel = st.nextToken();
					beatLabel = st.nextToken();
					trackLabel = st.nextToken();
					timeLabel = st.nextToken();
					if (!barLabel.equals(prev)) {
						prev = barLabel;
						e = 4;
					}
				}
				g.setColor(rimColours[i]);	// add a dark rim
				g.fillOval(xx - (d+e) / 2, yy - (d+e) / 2, d+e, d+e);
				if (e == 0) {
					g.setColor(bodyColours[i]);
					g.fillOval(xx - (d-2) / 2, yy - (d-2) / 2, d-2, d-2);
				}
				if (i == WormConstants.wormLength - wait - 1) {	// draw face
					g.setColor(WormConstants.axesColor);
					g.drawString("Time: ", labelLeft, labelHeight);
					g.drawString(timeLabel,
							labelRight - fm.stringWidth(timeLabel),
							labelHeight);
					if (barLabel != null) {
						g.drawString("Bar: ", labelLeft, labelHeight + ht);
						g.drawString(barLabel,
								labelRight - fm.stringWidth(barLabel),
								labelHeight + ht);
					}
					if ((beatLabel != null) && (beatLabel.length() > 0)) {
						g.drawString("Beat: ", labelLeft, labelHeight + 2 * ht);
						g.drawString(beatLabel,
								labelRight - fm.stringWidth(beatLabel),
								labelHeight + 2 * ht);
					}
					if (e == 0)
						g.setColor(WormConstants.wormFaceColor);
					else
						g.setColor(WormConstants.altFaceColor);
					if (barLabel != null) {
						int wd = fm.stringWidth(barLabel); 
						g.drawString(barLabel, xx - wd / 2, yy + ht / 2);
					} else {	// draw face :)
						g.fillOval(xx -d / 5 -2, yy - d / 5, 5, 2);	// l eye
						g.fillOval(xx +d / 5 -2, yy - d / 5, 5, 2);	// r eye
						g.fillOval(xx - 2, yy - 3, 3, 3);			// nose
						g.drawArc(xx - d / 4, yy - d / 4, d/2, d/2, 220, 100);
					}
				}
			}
		}
		// Draw axes and labels
		g.setColor(WormConstants.axesColor);
		g.drawRect(WormConstants.sideMargin, WormConstants.footMargin,
					xSize - 2 * WormConstants.sideMargin,
					ySize - 2 * WormConstants.footMargin);
		for (int i = 1; i < 10; i++) {
			int z = WormConstants.sideMargin +
						i * (xSize - 2 * WormConstants.sideMargin) / 10;
			String label = Format.d(xmin + i * (xmax - xmin) / 10, 1); // xlabel
			int wd = fm.stringWidth(label);
			g.drawString(label, z - wd / 2, ySize - 5);
			g.drawLine(z, ySize - WormConstants.footMargin - 2,
						z, ySize - WormConstants.footMargin + 2);
			g.drawLine(z, WormConstants.footMargin - 2,
						z, WormConstants.footMargin + 2);
			z = WormConstants.footMargin +
						i * (ySize - 2 * WormConstants.footMargin) / 10;
			label = Format.d(ymax - i * (ymax - ymin) / 10, 1);		// ylabel
			wd = fm.stringWidth(label);
			g.drawString(label, WormConstants.sideMargin - wd - 5, z + ht / 2);
			g.drawLine(WormConstants.sideMargin - 2, z,
						WormConstants.sideMargin + 2, z);
			g.drawLine(xSize - WormConstants.sideMargin - 2, z,
						xSize - WormConstants.sideMargin + 2, z);
		}
		int wd = fm.stringWidth(loudnessUnits);
		g.drawString(loudnessUnits, WormConstants.sideMargin - wd - 5,
						WormConstants.footMargin + ht / 2);
		wd = fm.stringWidth(tempoUnits);
		g.drawString(tempoUnits, xSize - WormConstants.sideMargin - wd / 2,
						ySize - 5);
	} // paint()

	public void print() {
		for (int i = 0; i < WormConstants.wormLength; i++) {
			int ind = (tail + i) % WormConstants.wormLength;
			System.out.println(i+" ["+ind+"] = ("+x[ind]+", "+y[ind]+")");
		}
		System.out.println("Tail = " + tail);
	} // print()

	public void setPoints(double[] x1, double[] y1, String[] flags,
							int start, int len) {
		if (start < len - WormConstants.wormLength)
			start = len - WormConstants.wormLength;
	//	int bar = 0;
	//	int beat = 0;
	//	int track = 0;
		int i = start - 10;	// try 10 steps back for smoothing
		if (i < 0)
			i = 0;
		double smoothx = x1[i];
		double smoothy = y1[i];
		double decay = 0.95;
	//	System.err.println("*************************************************");
		for ( ; i < len; i++) {
		//	System.err.println(i + ": " + x1[i] + " " + y1[i] + " " + flags[i]);
		//	if ((flags[i] & WormFile.BAR) != 0)
		//		bar++;
		//	if ((flags[i] & WormFile.BEAT) != 0)
		//		beat++;
		//	if ((flags[i] & WormFile.TRACK) != 0)
		//		track++;
			smoothx = smoothx * decay + x1[i] * (1 - decay);
			smoothy = smoothy * decay + y1[i] * (1 - decay);
			if (i >= start) {
				tail = i % WormConstants.wormLength;
				x[tail] = smoothx;
				y[tail] = smoothy;
		//		labels[tail] = bar + ":" + beat + ":" + track + ":" +
				labels[tail] = flags[i] + Format.d(i * framePeriod, 1);
			}
		}
		xCount = len;
		repaint();
	} // setPoints()
	
	void addPoint(double newx, double newy, String theLabel) {
		if ((smoothMode == NONE) || (memSize == 0)) {	// no smooth or 1st pt
			xmem[0] = newx;
			ymem[0] = newy;
			memSize = 1;
		} else if (smoothMode == EXPONENTIAL) { // exp decaying average (IIR)
			xmem[0] = xDecay * xmem[0] + (1 - xDecay) * newx;
			ymem[0] = yDecay * ymem[0] + (1 - yDecay) * newy;
			memSize = 1;
			newx = xmem[0];
			newy = ymem[0];
		} else if (smoothMode == HALF_GAUSS) {	// Gaussian smoothing
			double r = 120 / newx / framePeriod;		// 2 beats == half bar??
			int k = (int)Math.ceil(4 * r);	// cut off values <<< 0.01
			if (memSize == WormConstants.wormLength)
				memSize--;
			for (int i = memSize; i > 0; i--) {
				xmem[i] = xmem[i-1];
				ymem[i] = ymem[i-1];
			}
			xmem[0] = newx;
			ymem[0] = newy;
			memSize++;
			double xTotal = 0;
			double yTotal = 0;
			double eTotal = 0;
			for (int i = 0; (i < memSize) && (i <= k); i++) {
				double e = (double)i / r;
				e = Math.exp(-e * e / 2);
				xTotal += e * xmem[i];
				yTotal += e * ymem[i];
				eTotal += e;
			}
			if (eTotal != 0) {
				xTotal /= eTotal;
				yTotal /= eTotal;
			}
			newx = xTotal;
			newy = yTotal;
		} else {	// (smoothMode == FULL_GAUSS) {}  // Retrospective smoothing
			double r = 120 / newx / framePeriod;		// 2 beats == half bar??
			int k = (int)Math.ceil(3 * r);	// cut off values << 0.01
			if (k > memSize / 2)
				k = memSize / 2;
			if (memSize == WormConstants.wormLength)
				memSize--;
			for (int i = memSize; i > 0; i--) {
				xmem[i] = xmem[i-1];
				ymem[i] = ymem[i-1];
			}
			xmem[0] = newx;
			ymem[0] = newy;
			memSize++;
			for (int start = k; start <= 2 * k; start++) {
				double xTotal = 0;
				double yTotal = 0;
				double eTotal = 0;
				for (int i = start - k; (i < memSize) && (i <= 2 * k); i++) {
					double e = (double)(i - start) / r;
					e = Math.exp(-e * e / 2);
					xTotal += e * xmem[2 * k - i];
					yTotal += e * ymem[2 * k - i];
					eTotal += e;
				}
				if (eTotal != 0) {
					xTotal /= eTotal;
					yTotal /= eTotal;
				}
				newx = xTotal;
				newy = yTotal;
				int index = (tail - (2 * k - start) +
						WormConstants.wormLength) % WormConstants.wormLength;
				x[index] = newx;
				y[index] = newy;
			}
		}
	// Calculate x-axis bounds (with decaying average +-10%) and rescale
		xSum = xSum * sDecay + newx;
		xCount++;
		if (autoScaleMode && (
				(newx < xmin) || (newx > xmax) ||
				(xmin > xSum * (1 - sDecay) * 0.97) ||
				(xmax < xSum * (1 - sDecay) * 1.03))) {
			autoScale();
		}
	// Update for all methods
		x[tail] = newx;
		y[tail] = newy;
		labels[tail] = theLabel;
		tail = (tail + 1) % WormConstants.wormLength;
	} // addPoint()

	public void ancestorMoved(HierarchyEvent e) {}

	public void ancestorResized(HierarchyEvent e) {
		if ((xSize == getWidth()) && (ySize == getHeight()))
			return;
		xSize = getWidth();
		ySize = getHeight();
		xRescale(xmin, xmax);
		yRescale(ymin, ymax);
	} // ancestorResized()

	void xMoveRight(boolean right) {
		setAutoScaleMode(false);
		double diff = (xmax - xmin) / (right? -2: 2);
		// if (xmin < diff)
		// 	diff = xmin;
		xRescale(xmin - diff, xmax - diff);
	} // xMoveRight()

	void xZoom(boolean in) {
		setAutoScaleMode(false);
		double diff = (xmax - xmin) / (in? -4: 2);
		// if (xmin < diff)
		// 	xRescale(0, xmax + diff);
		// else
		xRescale(xmin - diff, xmax + diff);
	} // xZoom()
	
	void xRescale(double min, double max) {
		xSize = getWidth();
		xmin = min;
		xmax = max;
		xScale = (double)(xSize - 2 * WormConstants.sideMargin) / (xmax - xmin);
		repaint();
	} // xRescale()
	
	void yRescale(double min, double max) {
		ySize = getHeight();
		ymin = min;
		ymax = max;
		yScale = (double)(ySize - 2 * WormConstants.footMargin) / (ymax - ymin);
		repaint();
	} // yRescale()

	void yMoveDown(boolean down) {
		double diff = (ymax - ymin) / (down? 4: -4);
		// if (ymin < diff)
		// 	diff = ymin;
		yRescale(ymin - diff, ymax - diff);
	} // yMoveDown()

	void yZoom(boolean in) {
		double diff = (ymax - ymin) / (in? -4: 2);
		// if (ymin < diff)
		// 	yRescale(0, ymax + diff);
		// else
		yRescale(ymin - diff, ymax + diff);
	} // yZoom()

	void setAutoScaleMode(boolean set) {
		autoScaleMode = set;
		if (autoScaleMode)
			autoScale();
		if (autoIndicator != null)
			autoIndicator.setSelected(autoScaleMode);
	} // setAutoScaleMode()

	void autoScale() {
		double factor = 0.1;
		xRescale(xSum * (1-sDecay) * (1-factor),xSum * (1-sDecay) * (1+factor));
	} // autoScale()

	public static void main(String[] args) {
		createInFrame(args);
	} // main()

	public static Worm createInFrame(String[] args) {
		JFrame f = new JFrame(WormConstants.title);
		Worm w = new Worm(f);
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-d")) {
				if (args[i].length() == 2)
					AudioWorm.fileDelay = Integer.parseInt(args[++i]);
				else
					AudioWorm.fileDelay =Integer.parseInt(args[i].substring(2));
			} else if (args[i].endsWith(".wav") || args[i].endsWith(".mp3"))
				w.setInputFile(args[i]);
			else if (args[i].endsWith(".match"))
				w.setMatchFile(args[i]);
			else if (args[i].endsWith(".worm"))
				w.setWormFile(args[i]);
			else
				w.setTimingOffsetString(args[i]);
		}
		f.getContentPane().setBackground(WormConstants.backgroundColor);
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(),
					BoxLayout.Y_AXIS));
		f.getContentPane().add(w);
		f.getContentPane().add(new WormScrollBar(w));
		f.getContentPane().add(new WormControlPanel(w));
		w.addHierarchyBoundsListener(w);
		// or f.getContentPane().addHier... -- both seem to work the same
		Dimension borderSize = FrameMargins.get(false);
		f.setSize(w.getWidth() + borderSize.width,
				  w.getHeight() + borderSize.height + WormConstants.cpHeight);
		GraphicsConfiguration gc = f.getGraphicsConfiguration();
		Rectangle bounds = gc.getBounds();
		f.setLocation(bounds.x + (bounds.width - f.getWidth()) / 2,
					  bounds.y + (bounds.height - f.getHeight()) / 2);
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setIconImage(WormIcon.getWormIcon(1,f));
		if (args.length > 0)
			w.play();
		return w;
	} // createInFrame()

} // class Worm
