/*
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

package at.ofai.music.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import at.ofai.music.util.Format;

/** A utility class for converting graphical user interface components to
 *  PostScript, which can be sent directly to a printer or printed to a file.
 *  This gives much higher quality illustrations for articles
 *  than if a screenshot is used, since scaling should not reduce quality.
 *  The only requirement is that the component to be printed has a
 *  <code>paint(Graphics)</code> method.
 *  <p>There are some bugs in this code which require manual editing of
 *  the PostScript file. First, there doesn't seem to be any way to include
 *  the bounding box, although it is possible to calculate it. Second, the
 *  cliprect produced in the PostScript output is wrong.
 *  (Check: has this been fixed in more recent Java versions?
 *  Apparently not, as of 1.5.0, but if scaling is not performed, the cliprect
 *  is OK and the bounding box correct.)
 *  See {@link PSPrinter#print(Graphics, PageFormat, int)}
 */
public class PSPrinter implements Printable {

	/** the component to be converted */
	Component component;
	/** the desired graphical resolution in pixels per inch */
	int resolution;	// can't work out how to ask the system

	/** Print a GUI component to a PostScript printer or file.
	 *  The 2 forms of this method are the normal ways of accessing this class.
	 *  This form has problems printing some components. It is recommended to
	 *  use the other version.
	 *  @param c the component to be rendered in PostScript
	 *  @param r the resolution of the printer in pixels per inch
	 */
	public static void print(Component c, int r) {
		new PSPrinter(c, r).doPrint();
	}

	/** Print a GUI component to a PostScript printer or file.
	 *  The 2 forms of this method are the normal ways of accessing this class.
	 *  If no resolution is given, the picture is not scaled, and the cliprect
	 *  is then correct. This is the recommended version to use.
	 *  @param c the component to be rendered in PostScript
	 */
	public static void print(Component c) {
		new PSPrinter(c, -1).doPrint();
	}
	
	/** Constructs a PSPrinter for a given graphical component and resolution.
	 *  @param c   the component to be rendered in PostScript
	 *  @param res the resolution of the printer in pixels per inch; set res to
	 *             -1 for no scaling (avoids the apparently buggy cliprect)
	 */
	public PSPrinter(Component c, int res) {
		component = c;
		resolution = res;
	} // constructor

	/** Produces a print dialog and executes the requested print job.
	 *  The print job performs its task by callback of the
	 *  {@link PSPrinter#print(Graphics, PageFormat, int)} method.
	 */
	public void doPrint() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);	// tell it where the rendering code is
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	} // doPrint()

	/** The callback method for performing the printing / Postscript conversion.
	 *  The resulting PostScript file requires some post-editing.
	 *  In particular, there are two problems to be dealt with:
	 *  <p>1) The file has no bounding box. This method prints the correct
	 *  bounding box to stardard output, and it must then be cut and pasted
	 *  into the PostScript file. (There must be a better way!)
	 *  <p>2) The cliprect is wrong (but only if the resolution is specified).
	 *  This is solved by using resolution = -1 or by deleting the lines
	 *  in the PostScript file from <code>newpath</code> to <code>clip</code>.
	 *  (I don't know if this causes problems for components that try to draw
	 *  outside of their area.)
	 *  @param g  the graphics object used for painting
	 *  @param f  the requested page format (e.g. A4)
	 *  @param pg the page number (must be 0, or we report an error)
	 *  @return   the error status; if the page is successfully rendered,
	 *  Printable.PAGE_EXISTS is returned, otherwise if a page number greater
	 *  than 0 is requested, Printable.NO_SUCH_PAGE is returned
	 *  @throws   PrinterException thrown when the print job is terminated
	 */
	public int print(Graphics g, PageFormat f, int pg) throws PrinterException {
		if (pg >= 1)
			 return Printable.NO_SUCH_PAGE;
		Graphics2D g2 = (Graphics2D) g;
		double wd = component.getWidth();
		double ht = component.getHeight();
		double imwd = f.getImageableWidth();
		double imht = f.getImageableHeight();
		double corr = resolution / 72.0;
		double scaleFactor = corr * Math.min(imwd / wd, imht / ht);
		double xmin = f.getImageableX();
		double ymin = f.getImageableY();
		AffineTransform scale = new AffineTransform(scaleFactor, 0,
													0, scaleFactor,
													corr * xmin, corr * ymin);
		Format.setGroupingUsed(false);
		double pgHt = f.getHeight();
		if (resolution > 0) {
			g2.setTransform(scale);
			System.out.println("%%BoundingBox: " +
					Format.d(xmin, 0) + " " +
					Format.d(pgHt - ymin - ht * scaleFactor / corr, 0) + " " +
					Format.d(xmin + wd * scaleFactor / corr, 0) + " " +
					Format.d(pgHt - ymin, 0));
		} else {
			g2.setClip(0, 0, (int)wd, (int)ht); 
			System.out.println("%%BoundingBox: " +
					Format.d(0, 0) + " " +
					Format.d(pgHt - ht, 0) + " " +
					Format.d(wd, 0) + " " +
					Format.d(pgHt, 0));
		}

		// System.out.println(f.getWidth() + " " + f.getHeight() + " " +
		// 				f.getImageableX() + " " + f.getImageableY() + " " +
		// 				f.getImageableWidth() + " " + f.getImageableHeight());
		// Letter = 8.5x11" 612x792pt  DEFAULT
		// A4 = 210x297mm   595x842pt

	//	AffineTransform scale = new AffineTransform(2.5, 0, 0, 2.5, 200, 1000);
	//	g2.setTransform(scale);		// The figures need some fiddling.
									// In particular, the PostScript file has:
									// 1) no bounding box (add manually)
									// 2) wrong cliprect (delete lines from
									//                    "newpath" to "clip")
		component.printAll(g2);
		return Printable.PAGE_EXISTS;
	} // print()

} // class PSPrinter
