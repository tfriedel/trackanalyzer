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

import java.text.NumberFormat;
import java.io.*;

/** A simple utility class for easier formatted output of numeric data.
 *  Formatting is controlled by static objects, so that number widths and
 *  precisions can be set once, and used multiple times.
 */
public class Format {

	/** The object which performs formatting of integers */
	protected static NumberFormat intFormat = NumberFormat.getInstance();

	/** The object which performs formatting of doubles */
	protected static NumberFormat doubleFormat = NumberFormat.getInstance();
	
	/** The preferred notation for positive numbers (default is no '+' sign) */
	protected static char plusSign = ' ';

	/** Set the number of digits to appear after the decimal point
	 *  @param dp the number of characters after the decimal point
	 */
	public static void setPostDigits(int dp) {
		doubleFormat.setMinimumFractionDigits(dp);
		doubleFormat.setMaximumFractionDigits(dp);
	} // setPostDigits()
	
	/** Set the number of digits to appear before the decimal point.
	 *  If the number does not require this many digits, it will be
	 *  padded with spaces.
	 *  @param dp the number of characters before the decimal point
	 */
	public static void setPreDigits(int dp) {
		doubleFormat.setMinimumIntegerDigits(dp);
	} // setPreDigits()
	
	/** Set the number of digits for displaying integers.
	 *  If the number does not require this many digits, it will be
	 *  padded with spaces.
	 *  @param dp the number of digits for displaying integers
	 */
	public static void setIntDigits(int dp) {
		intFormat.setMinimumIntegerDigits(dp);
		intFormat.setMinimumFractionDigits(0);
		intFormat.setMaximumFractionDigits(0);
	} // setIntegerDigits()

	/** Set whether digits should be grouped in 3's as in 12,000,000.
	 *  @param flag true if grouping should be used, false if not
	 */	
	public static void setGroupingUsed(boolean flag) {
		doubleFormat.setGroupingUsed(flag);
		intFormat.setGroupingUsed(flag);
	} // setGroupingUsed()

	/** Sets the initial character for positive numbers (usually blank or '+')
	 *  @param c the character to prefix to positive numbers
	 */
	public static void setPlusSign(char c) {
		plusSign = c;
	} // setPlusSign()

	/** Initialise the formatting objects with the desired settings.
	 *  @param id       the number of characters for displaying integers
	 *  @param did      the number of characters before the decimal point
	 *  @param dfd      the number of characters after the decimal point
	 *  @param grouping true if grouping should be used, false if not
	 */
	public static void init(int id, int did, int dfd, boolean grouping) {
		setIntDigits(id);
		setPreDigits(did);
		setPostDigits(dfd);
		setGroupingUsed(grouping);
	} // init()

	/** Convert a double to a String with a set number of decimal places and
	 *  padding to the desired minimum number of characters before the decimal
	 *  point.
	 *  @param n the number to convert
	 *  @param id the number of characters before the decimal point
	 *  @param fd the number of characters after the decimal point
	 */
	public static String d(double n, int id, int fd) {
		setPreDigits(id);
		return d(n, fd);
	} // d()

	/** Convert a double to a String with a set number of decimal places
	 *  @param n the number to convert
	 *  @param fd the number of characters after the decimal point
	 */
	public static String d(double n, int fd) {
		setPostDigits(fd);
		return d(n);
	} // d()

	/** Convert a double to a String. The number of decimal places and
	 *  characters before the decimal point are stored in the static members
	 *  of this class.
	 *  @param n the number to convert
	 */
	public static String d(double n) {
		String s;
		if (Double.isNaN(n))
			return "NaN";
		s = doubleFormat.format(n);
		if (n >= 0)
			s = plusSign + s;
		char[] c = s.toCharArray();
		int i;
		for (i = 1; (i < c.length-1) && (c[i] == '0') && (c[i+1] != '.'); i++) {
			c[i] = c[i-1];
			c[i-1] = ' ';
		}
		if (i > 1)
			s = new String(c);
		return s;
	} // d()

	/** Convert an integer to a String with padding to the desired minimum width
	 *  @param n the number to convert
	 *  @param id the desired minimum width
	 */
	public static String i(int n, int id) {
		setIntDigits(id);
		return i(n);
	} // i()

	/** Convert an integer to a String with padding to the desired minimum width
	 *  @param n the number to convert
	 */
	public static String i(int n) {
		return (n < 0)? intFormat.format(n): plusSign+intFormat.format(n);
	} // i()

	/** Output an array to file as an assignment statement for input to Matlab
	 *  @param data the values to print to 4 decimal places
	 *  @param name the variable name in the Matlab assignment statement; the
	 *  file name is the same name with ".m" appended, or standard out if
	 *  unable to open this file
	 */
	public static void matlab(double[] data, String name) {
		matlab(data, name, 4);
	} // matlab()
	
	/** Output an array to file as an assignment statement for input to Matlab
	 *  @param data the values to print
	 *  @param name the variable name in the Matlab assignment statement; the
	 *  file name is the same name with ".m" appended, or standard out if
	 *  unable to open this file
	 *  @param dp the number of decimal places to print
	 */
	public static void matlab(double[] data, String name, int dp) {
		setPostDigits(dp);
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream(name+".m"));
		} catch (FileNotFoundException e) {
			out = System.out;
		}
		matlab(data, name, out);
		if (out != System.out)
			out.close();
	} // matlab

	/** Output an array to a printstream as an assignment statement for input to
	 *  Matlab
	 *  @param data the values to print
	 *  @param name the variable name in the Matlab assignment statement
	 *  @param out  the output stream to print to
	 */
	public static void matlab(double[] data, String name, PrintStream out) {
		out.println(name + " = [");
		setGroupingUsed(false);
		for (int i=0; i < data.length; i++)
			out.println(d(data[i]));
		out.println("];");
	} // matlab()

} // class Format
