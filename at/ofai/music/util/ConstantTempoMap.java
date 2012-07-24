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

public class ConstantTempoMap implements TempoMap {
	
	protected double interBeatInterval;

	public ConstantTempoMap(double bpm) {
		interBeatInterval = 60 / bpm;
	} // constructor

	public ConstantTempoMap() {
		this(120);
	} // default constructor

	public void add(double time, double tempo) {
		throw new RuntimeException("ConstantTempoMap: cannot change tempo");
	} // add()
		
	public double toRealTime(double value) {
		return value * interBeatInterval;
	} // toRealTime()

	public double toScoreTime(double value) {
		return value / interBeatInterval;
	} // toScoreTime()

	public static void main(String[] args) { // unit test
		TempoMap mtm = new ConstantTempoMap(100);
		System.out.println(mtm.toRealTime(1));
		System.out.println(mtm.toScoreTime(mtm.toRealTime(1)));
		System.out.println(mtm.toScoreTime(4));
		System.out.println(mtm.toRealTime(mtm.toScoreTime(4)));
		mtm.add(5, 120);
	} // main()

} // ConstantTempoMap
