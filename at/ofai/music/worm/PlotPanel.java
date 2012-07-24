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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;

import at.ofai.music.util.Format;
import at.ofai.music.util.PSPrinter;

class PlotPanel extends JPanel implements HierarchyBoundsListener {
	
	static final long serialVersionUID = 0;
	
	JFrame theFrame;					// Parent container of this panel
	String title;
	public static final int NONE = 0,
							HOLLOW = 1,
							FILLED = 2,
							JOIN = 4,
							IMPULSE = 8,
							STEP = 16;
	int plots;
	PlotData[] data;
	PlotData current;
	int currentPtr;
	AxisData xAxis, yAxis;
	boolean initialised;
	FontMetrics fm;		// of the font used for axis labels
	int ht;				// the height of this font

	class AxisData {
		double min, max, maxValue, minValue, scale;
		int ticks, digits, margin, size, maxTicks;
		String label;
		boolean isVertical;

		public AxisData(boolean v) {
			isVertical = v;
			margin = isVertical? WormConstants.footMargin:
								 WormConstants.sideMargin;
			size = isVertical? WormConstants.Y_SZ: WormConstants.X_SZ;
			setMaxTicks(10,10);
			ticks = maxTicks;
			digits = 2;
			label = "";
		}
		
		void test() {
			try {
				java.io.BufferedReader b =
						new java.io.BufferedReader(
						new java.io.InputStreamReader(System.in));
				while (true) {
					StringTokenizer t = new StringTokenizer(b.readLine());
					double mn = Double.parseDouble(t.nextToken());
					double mx = Double.parseDouble(t.nextToken());
					setBounds(mn, mx);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		} // test()

		void setMaxTicks(double mn, double mx) {
			if (fm == null)
				maxTicks = 10;
			else if (isVertical)
				maxTicks = Math.max(3, Math.min(10, size / ht * 2 / 3));
			else {
				int wd = Math.max(fm.stringWidth(Format.d(mn, digits)),
								  fm.stringWidth(Format.d(mx, digits)));
				maxTicks = Math.max(3, Math.min(10, size / wd * 2 / 3));
			}
		} // setMaxTicks()
		
		void setBounds(double mn, double mx) {
			if (mn > mx) {
				double tmp = mn;
				mn = mx;
				mx = tmp;
			}
			setMaxTicks(mn, mx);
			double diff = (mx - mn) / maxTicks;
			double base = Math.pow(10, Math.floor(Math.log(diff)/Math.log(10)));
			int head = (int)Math.ceil(diff / base);
			if (head > 5)
				diff = 10 * base;
			else if (head > 2)
				diff = 5 * base;
			else // head = 1 or 2
				diff = head * base;
			int low = (int) Math.floor(mn / diff);
			int high = (int) Math.ceil(mx / diff);
			if (high - low > maxTicks) {
				if (head > 5)
					diff = 20 * base;
				else if (head > 2)
					diff = 10 * base;
				else if (head == 2)
					diff = 5 * base;
				else
					diff = 2 * base;
				low = (int) Math.floor(mn / diff);
				high = (int) Math.ceil(mx / diff);
			}
			int pow = (int)Math.floor(Math.log(diff)/Math.log(10));
			if (pow < 0)
				digits = -pow;
			else
				digits = 0;
			ticks = high - low;	// alt. code (below) replaces this line
			rescale(low * diff, high * diff);
			return;
		} // setBounds()

// Alternative code which keeps the number of ticks fixed.
//			int empty = maxTicks - high + low;
//			int[] next = {5,4,4};
//			int count = 0;
//			for (int n = 2; empty > 0; ) {
//				System.out.println("need to shift bounds outwards: "
//									+low * diff+ " " +high * diff+ " "+empty);
//				int rem = low % n;
//				if (rem < 0)
//					rem += n;
//				int rem1 = high % n;
//				if (rem1 < 0)
//					rem1 += n;
//				System.out.println(low + " % " + n + " = " + rem);
//				System.out.println(high + " % " + n + " = " + rem1);
//				if ((rem != 0) && (rem <= empty))
//					low -= rem;
//				else if ((rem1 != 0) && (n - rem1 <= empty))
//					high += n - rem1;
//				else if (n >= maxTicks)	// force exit
//					high += empty;
//				else
//					n = n * next[count++ % 3] / 2;
//				empty = maxTicks - high + low;
//			}
//			rescale(low * diff, high * diff);

		void translate(boolean positive) {
			double diff = (max - min) / (positive? -4: 4);
			rescale(min - diff, max - diff);
		} // translate()

		void zoom(boolean in, boolean positive) {
			double diff = (max - min) / (in? -2: 1);
			if (positive)
				setBounds(min, max + diff);
			else
				setBounds(min - diff, max);
		} // zoom()
		
		void rescale(double newMin, double newMax) {
			size = isVertical? getHeight(): getWidth();
			min = newMin;
			max = newMax;
			scale = (double)(size - 2 * margin) / (max - min);
			repaint();
		} // rescale()

	} // class AxisData

	class PlotData {
		double[] x;							// x-coordinates
		double[] y;							// y-coordinates
		Color pointColour, joinColour;		// Colours of plot
		int pointSize;						// Size of a point
		int mode;
		int length;

		protected PlotData(double[] xData, double[] yData) {
			this(xData, yData, xData.length);
		} // constructor
		
		protected PlotData(double[] xData, double[] yData, int len) {
			x = xData;
			y = yData;
			length = len;
			pointColour = Color.blue;
			joinColour = Color.red;
			pointSize = 6;
			mode = IMPULSE | HOLLOW;
			if ((xData != null) && (yData != null)) {
				if (plots < data.length) {
					currentPtr = plots;
					data[plots++] = this;
				} else
					throw new RuntimeException("Too many plots");
			}
		} // constructor

	} // plotData

	public PlotPanel(JFrame frame) {
		this(frame, null, null);
	} // constructor
	
	public PlotPanel(JFrame frame, double[] xData, double[] yData) {
		theFrame = frame;
		title = "";
		data = new PlotData[10];
		currentPtr = plots = 0;
		initialised = false;
		if ((xData != null) && (yData != null))
			current = new PlotData(xData, yData);
		xAxis = new AxisData(false);
		yAxis = new AxisData(true);
		setSize(xAxis.size, yAxis.size);
		fitAxes();
		frame.addKeyListener(new PlotListener(this));
	} // constructor

	public void fitAxes(int current) {
		setCurrent(current);
		fitAxes();
	} // fitAxes()

	public void fitAxes() {
		if (current != null) {
			xAxis.setBounds(min(current.x, current.length), max(current.x, current.length));
			yAxis.setBounds(min(current.y, current.length), max(current.y, current.length));
		}
	} // fitAxes()

	void resize() {
		xAxis.setBounds(xAxis.min, xAxis.max);
		yAxis.setBounds(yAxis.min, yAxis.max);
	} // resize()

	void update() {
		repaint();
	} // update()

	void setCurrent(int c) {
		if (c < plots) {
			currentPtr = c;
			current = data[currentPtr];
		}
	} // setCurrent()

	void rotateCurrent() {
		if (plots != 0) {
			currentPtr = (currentPtr + 1) % plots;
			current = data[currentPtr];
		}
	} // rotateCurrent()

	void clear() {
		plots = 0;
		current = null;
	} // clear

	// Allows multiple plots, similar to Matlab's "hold on"
	boolean addPlot(double[] xData, double[] yData, Color c, int m) {
		if (addPlot(xData, yData)) {
			current.pointColour = c;
			current.joinColour = c;
			current.mode = m;
			return true;
		}
		return false;
	} // addPlot()

	boolean addPlot(double[] xData, double[] yData) {
		if ((xData != null) && (yData != null) && (plots < data.length)) {
			current = new PlotData(xData, yData);
			if (!initialised) {
				resize();
				initialised = true;
			}
			return true;
		}
		return false;
	} // addPlot()

	void setLength(int index, int len) {
		data[index].length = len;
	} // setLength()
	
	void setTitle(String t) { title = t; theFrame.setTitle(t); }

	void setXLabel(String s) { xAxis.label = s; }
	void setYLabel(String s) { yAxis.label = s; }
	
	void setAxis(String s) {
		StringTokenizer tk = new StringTokenizer(s);
		try {
			xAxis.rescale(Double.parseDouble(tk.nextToken()),
					 Double.parseDouble(tk.nextToken()));
			yAxis.rescale(Double.parseDouble(tk.nextToken()),
					 Double.parseDouble(tk.nextToken()));
		} catch (NoSuchElementException e) {	// ignore illegal values
			System.err.println("Illegal axes specification: " + e);
		} catch (NumberFormatException e) {		// ignore illegal values
			System.err.println("Illegal axes specification: " + e);
		}
	} // setAxis()

	void setXAxis(double min, double max) { xAxis.rescale(min, max); }
	void setYAxis(double min, double max) { yAxis.rescale(min, max); }

	void setMode(int m) { if (current != null) { current.mode = m; repaint(); }}

	void close() { theFrame.dispose(); }
	
	public void print(int res) {
		PSPrinter.print(this, res);
	} // print()

	public void paint(Graphics g) {
		if (fm == null) {
			fm = g.getFontMetrics();
			ht = fm.getHeight();
		}
		// Paint background
		g.setColor(WormConstants.backgroundColor);
		g.fillRect(0, 0, xAxis.size, yAxis.size);
		// Plot points
		int xprev = 0, yprev = 0;
		int yZero = yAxis.margin + (int)(yAxis.max * yAxis.scale);
		Shape saveClip = g.getClip();
		g.clipRect(xAxis.margin, yAxis.margin, xAxis.size - 2 * xAxis.margin,
					yAxis.size - 2 * yAxis.margin);
		for (int j = 0; j < plots; j++) {
			for (int i = 0; i < data[j].x.length; i++) {
				int xx = xAxis.margin +
							(int)((data[j].x[i] - xAxis.min) * xAxis.scale);
				int yy = yAxis.margin +
							(int)((yAxis.max - data[j].y[i]) * yAxis.scale);
				int sz = data[j].pointSize;
				g.setColor(data[j].pointColour);
				if ((data[j].mode & HOLLOW) != 0)
					g.drawOval(xx - sz / 2, yy - sz / 2, sz, sz);
				if ((data[j].mode & FILLED) != 0)
					g.fillOval(xx - sz / 2, yy - sz / 2, sz, sz);
				g.setColor(data[j].joinColour);
				if ((data[j].mode & IMPULSE) != 0)
					g.drawLine(xx, yy, xx, yZero);
				else if (((data[j].mode & JOIN) != 0) && (i != 0)) {
					if ((data[j].mode & STEP) != 0) {
						g.drawLine(xx, yy, xx, yprev);	// join with step fn
						g.drawLine(xx, yprev, xprev, yprev); 
					} else
						g.drawLine(xx, yy, xprev, yprev);	// join directly
				}
				xprev = xx;
				yprev = yy;
			}
		}
		g.setClip(saveClip);
		// Draw axes and labels
		g.setColor(WormConstants.axesColor);
		g.drawRect(xAxis.margin, yAxis.margin, xAxis.size - 2 * xAxis.margin,
					yAxis.size - 2 * yAxis.margin);
		for (int i = 1; i < xAxis.ticks; i++) {
			int z = xAxis.margin +
						i * (xAxis.size - 2 * xAxis.margin) / xAxis.ticks;
			String label = Format.d(xAxis.min + i * (xAxis.max - xAxis.min) /
									xAxis.ticks, xAxis.digits);
			int wd = fm.stringWidth(label);
			g.drawString(label, z - wd / 2, yAxis.size - 5);
			g.drawLine(z, yAxis.size - yAxis.margin - 2,
						z, yAxis.size - yAxis.margin + 2);
			g.drawLine(z, yAxis.margin - 2,
						z, yAxis.margin + 2);
		}
		for (int i = 1; i < yAxis.ticks; i++) {
			int z = yAxis.margin + i*(yAxis.size-2 * yAxis.margin)/yAxis.ticks;
			String label = Format.d(yAxis.max - i * (yAxis.max - yAxis.min) /
									yAxis.ticks, yAxis.digits);
			int wd = fm.stringWidth(label);
			g.drawString(label, xAxis.margin - wd - 5, z + ht / 2);
			g.drawLine(xAxis.margin - 2, z,
						xAxis.margin + 2, z);
			g.drawLine(xAxis.size - xAxis.margin - 2, z,
						xAxis.size - xAxis.margin + 2, z);
		}
		int wd = fm.stringWidth(yAxis.label);
		g.drawString(yAxis.label, xAxis.margin - wd - 5, yAxis.margin + ht / 2);
		wd = fm.stringWidth(xAxis.label);
		g.drawString(xAxis.label, xAxis.size - xAxis.margin-wd/2, yAxis.size-5);
	} // paint()

	public void ancestorMoved(HierarchyEvent e) {}

	public void ancestorResized(HierarchyEvent e) {
		if ((xAxis.size == getWidth()) && (yAxis.size == getHeight()))
			return;
		xAxis.setBounds(xAxis.min, xAxis.max);
		yAxis.setBounds(yAxis.min, yAxis.max);
		xAxis.setBounds(xAxis.min, xAxis.max);	// recalculate to fix labels
		yAxis.setBounds(yAxis.min, yAxis.max);
	} // ancestorResized()

	void xMoveRight(boolean right) { xAxis.translate(right); }
	void xZoom(boolean in, boolean right) { xAxis.zoom(in, right); }
	void yMoveDown(boolean down) { yAxis.translate(down); }
	void yZoom(boolean in, boolean down) { yAxis.zoom(in, down); }

	static double min(double[] d) {
		if ((d == null) || (d.length == 0))
			return 0;
		return min(d, d.length);
	} // max()
	
	static double min(double[] d, int len) {
		if (d == null)
			return 0;
		if (d.length < len)
			len = d.length;
		if (len == 0)
			return 0;
		double m = d[0];
		for (int i = 1; i < d.length; i++)
			if ((m == Double.NaN) || (d[i] < m))
				m = d[i];
		return m;
	} // min()
	
	static double max(double[] d) {
		if ((d == null) || (d.length == 0))
			return 0;
		return max(d, d.length);
	} // max()
	
	static double max(double[] d, int len) {
		if (d == null)
			return 0;
		if (d.length < len)
			len = d.length;
		if (len == 0)
			return 0;
		double m = d[0];
		for (int i = 1; i < len; i++)
			if ((m == Double.NaN) || (d[i] > m))
				m = d[i];
		return m;
	} // max()

} // class PlotPanel
