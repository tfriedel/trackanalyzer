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
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import at.ofai.music.util.FrameMargins;

public class Plot extends JFrame {
	
	static final long serialVersionUID = 0;	// silence compiler warning
	
	JFrame frame;
	public PlotPanel panel;

	public Plot(double[] xData, double[] yData) {
		this();
		panel.addPlot(xData, yData);
	}

	public Plot() {
		frame = new JFrame();
		panel = new PlotPanel(frame);
		frame.getContentPane().setBackground(WormConstants.backgroundColor);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),
					BoxLayout.Y_AXIS));
		frame.getContentPane().add(panel);
		panel.addHierarchyBoundsListener(panel);
		Dimension borderSize = FrameMargins.get(false);
		frame.setSize(panel.getWidth() + borderSize.width,
				  panel.getHeight() + borderSize.height);
				  // + WormConstants.cpHeight);
		GraphicsConfiguration gc = frame.getGraphicsConfiguration();
		Rectangle bounds = gc.getBounds();	// [x=0 y=0 w=1280 h=1024]
		frame.setLocation(bounds.x + (bounds.width - frame.getWidth()) / 2,
					  bounds.height - frame.getHeight());
		//			  bounds.y + (bounds.height - frame.getHeight()) / 2);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		frame.setIconImage(WormIcon.getWormIcon(1, frame));
	}

	public void setTitle(String s) { panel.setTitle(s); }
	public void setAxis(String s) { panel.setAxis(s); }
	public void setXAxis(double min, double max) { panel.setXAxis(min, max); }
	public void setYAxis(double min, double max) { panel.setYAxis(min, max); }
	public void setLength(int i, int l) { panel.setLength(i, l); }
	public void rotateCurrent() { panel.rotateCurrent(); }
	public void update() { panel.update(); }
	public void fitAxes() { panel.fitAxes(); }
	public void fitAxes(int current) { panel.fitAxes(current); }
	public void setMode(int m) { panel.setMode(m); }
	public void clear() { panel.clear(); }
	public void close() { frame.setVisible(false); }
	public void addPlot(double[] x, double[] y) {
		addPlot(x, y, Color.blue);
	}
	public void addPlot(double[] x, double[] y, Color c) {
		addPlot(x, y, c, PlotPanel.IMPULSE | PlotPanel.HOLLOW);
	}
	public void addPlot(double[] x, double[] y, Color c, int mode) {
		panel.addPlot(x, y, c, mode);
	}

	public static void main(String[] args) {	// simple test of this class
		double[] x = new double[100];
		double[] y = new double[100];
		for (int i = 0; i < 100; i++) {
			x[i] = i;
			y[i] = Math.sin(2 * Math.PI * i / 50);
		}
		Plot testPlot = new Plot(x, y);
		testPlot.panel.xAxis.test();	//SD: remove
		testPlot.frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) { System.exit(0); } });
	} // main()

} // class Plot
