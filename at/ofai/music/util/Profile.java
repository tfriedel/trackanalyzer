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

public class Profile {

	public static final int MAX_SIZE = 20;
	private static long[] tmin = new long[MAX_SIZE];
	private static long[] tmax = new long[MAX_SIZE];
	private static long[] tsum = new long[MAX_SIZE];
	private static long[] tprev = new long[MAX_SIZE];
	private static int[] tcount = new int[MAX_SIZE];

	public static void report(int i) {
		if ((i < 0) || (i >= MAX_SIZE) || (tcount[i] == 0))
			return;
		System.err.println("Profile " + i + ": " + tcount[i] + " calls;  " +
					(tmin[i]/1000.0) + " - " + (tmax[i]/1000.0) + ";  Av: " +
					(tsum[i] / tcount[i] / 1000.0));
	} // report()

	public static void report() {
		for (int i = 0; i < MAX_SIZE; i++)
			report(i);
	} // report()

	public static void start(int i) {
		tprev[i] = System.nanoTime();
	} // start()

	public static void log(int i) {
		long tmp = System.nanoTime();
		long t = (tmp - tprev[i]) / 1000;
		tprev[i] = tmp;
		tsum[i] += t;
		if ((tcount[i] == 0) || (t > tmax[i]))
			tmax[i] = t;
		if ((tcount[i] == 0) || (t < tmin[i]))
			tmin[i] = t;
		tcount[i]++;
	} // log()

} // class Profile
