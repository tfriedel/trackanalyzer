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

import at.ofai.music.util.Format;

// Stores a list of corresponding performance (real) times and
//   notated (score or MIDI) times. Automatically resizes when necessary.
//   Very inefficient if the list is edited after being used.
//   Assumes a monotonic mapping (t1 > t2  <=>  s1 > s2)
public class MatchTempoMap implements TempoMap {

	protected double[] realTime;		// in seconds
	protected double[] scoreTime;		// in beats or MIDI units
	protected int[] repeats;			// for calculating the average if needed
	protected int size;					// number of entries being used

	public MatchTempoMap() {
		this(5000);		// Mozart files are up to 3251 notes
	} // default constructor

	public MatchTempoMap(int sz) {
		realTime = new double[sz];
		scoreTime = new double[sz];
		repeats = new int[sz];
		size = 0;
	} // constructor

	protected void makeSpace() {
		if (size == realTime.length)
			resize(new MatchTempoMap(2 * size));
	} // makeSpace()

	protected void closeList() {
		if (size != realTime.length)
			resize(new MatchTempoMap(size));
	} // closeList()

	protected void resize(MatchTempoMap newList) {
		for (int i = 0; i < size; i++) {
			newList.realTime[i] = realTime[i];
			newList.scoreTime[i] = scoreTime[i];
			newList.repeats[i] = repeats[i];
		}
		realTime = newList.realTime;
		scoreTime = newList.scoreTime;
		repeats = newList.repeats;
	} // resize()

	public double toRealTime(double sTime) {
		closeList();
		return lookup(sTime, scoreTime, realTime);
	} // toRealTime()
	
	public double toScoreTime(double rTime) {
		closeList();
		return lookup(rTime, realTime, scoreTime);
	} // toScoreTime()
	
	public double lookup(double value, double[] domain, double[] range) {
		int index = java.util.Arrays.binarySearch(domain, value);
		if (index >= 0)
			return range[index];
		if ((size == 0) || ((size == 1) &&
				((range[0] == 0) || (domain[0] == 0))))
			throw new RuntimeException("Insufficient entries in tempo map");
		if (size == 1)
			return value * range[0] / domain[0];
		index = -1 - index;		// do linear interpolation
		if (index == 0) 		// unless at ends, where it is extrapolation
			index++;
		else if (index == size)
			index--;
		return (range[index] * (value - domain[index - 1]) +
				range[index - 1] * (domain[index] - value)) /
				(domain[index] - domain[index - 1]);
	} // lookup()
	
	public void add(double rTime, double sTime) {
		if (Double.isNaN(sTime))
			return;
		makeSpace();
		int index;
		for (index = 0; index < size; index++)
			if (sTime <= scoreTime[index])
				break;
		if ((index == size) || (sTime != scoreTime[index])) {
			for (int j = size; j > index; j--) {
				scoreTime[j] = scoreTime[j-1];
				realTime[j] = realTime[j-1];
				repeats[j] = repeats[j-1];
			}
			size++;
			scoreTime[index] = sTime;
			realTime[index] = rTime;
			repeats[index] = 1;
		} else {	// average time of multiple nominally simultaneous notes
			realTime[index] = (repeats[index] * realTime[index] + rTime) /
								(repeats[index] + 1);
			repeats[index]++;
		}
	} // add()

	public void dump(double[] tempo, double step) {
		if (size < 2) {
			System.err.println("dump() failed: Empty tempo map");
			return;
		}
		double[] tmp = new double[tempo.length];
		int i = 0;
		for (int j = 1; j < size; j++)
			for ( ; i * step < realTime[j]; i++)
				tmp[i] = (realTime[j] - realTime[j - 1]) /
							(scoreTime[j] - scoreTime[j - 1]);
		for ( ; i < tmp.length; i++)
			tmp[i] = (realTime[size - 1] - realTime[size - 2]) /
							(scoreTime[size - 1] - scoreTime[size - 2]);
		int window = (int)(0.1 / step);     // smooth over 2.0 second window
		double sum = 0;
		for (i = 0; i < tmp.length; i++) {
			sum += tmp[i];
			if (i >= window) {
				sum -= tmp[i - window];
				tempo[i] = sum / window;
			} else
				tempo[i] = sum / (i + 1);
			// System.out.println(i + " " + Format.d(tmp[i],3) +
			// 					   " " + Format.d(tempo[i], 3));
			if (tempo[i] != 0)
				tempo[i] = 60.0 / tempo[i];
		}
	} // dump

	public void print() {
		System.out.println("Score  |  Perf.\n-------+-------");
		for (int i = 0; i < size; i++)
			System.out.println(Format.d(scoreTime[i], 3) + "  |  " +
								Format.d(realTime[i], 3));
	} // print()

	public static void main(String[] args) { // unit test
		TempoMap mtm = new MatchTempoMap();
		mtm.add(0.6, 1);
		mtm.add(0.8, 2);
		mtm.add(0.95, 2.5);
		mtm.add(1.0, 3);
		double[] st = {0, 1, 2, 3, 4};
		for (int i = 0 ; i < st.length; i++)
			System.out.println(st[i] + " -> " + mtm.toRealTime(st[i]) +
							   " -> " + mtm.toScoreTime(mtm.toRealTime(st[i])));
		double[] rt = {0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
		for (int i = 0 ; i < rt.length; i++)
			System.out.println(rt[i] + " => " + mtm.toScoreTime(rt[i]) +
							   " => " + mtm.toRealTime(mtm.toScoreTime(rt[i])));
	} // main()

} // class MatchTempoMap
