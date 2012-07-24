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

public class WormEvent extends Event {

	public double tempo;
	public double loudness;
	public String label;

	public WormEvent(double on, double off, double eoff, int pitch, int vel,
			double beat, double dur, int flags, int cmd, int ch, int tr) {
		super(on, off, eoff, pitch, vel, beat, dur, flags, cmd, ch, tr);
		tempo = -1;
		loudness = -1;
		label = null;
	} // WormEvent

	public WormEvent(double time, double t, double l, double beat, int flags) {
		super(time, 0, 0, 0, 0, beat, 0, flags);
		tempo = t;
		loudness = l;
		label = null;
	} // constructor

} // class WormEvent
