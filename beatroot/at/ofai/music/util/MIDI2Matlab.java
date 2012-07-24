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

import java.util.ListIterator;

class MIDI2Matlab {

	public static void main(String[] args) {
		EventList e = EventList.readMidiFile(args[0]);
		Event[] events = e.toArray(0x90);
		int len = events.length;
		double[] pitch = new double[len];
		double[] vel = new double[len];
		double[] onset = new double[len];
		double[] offset = new double[len];
		for (int i=0; i<len; i++) {
			pitch[i] = events[i].midiPitch;
			vel[i] = events[i].midiVelocity;
			onset[i] = events[i].keyDown;
			offset[i] = events[i].keyUp;
		}
		Format.init(1,5,3,false);
		System.out.println("notes = zeros(" + len + ",4);");
		Format.matlab(onset, "notes(:,1)", System.out);
		Format.matlab(offset, "notes(:,2)", System.out);
		Format.matlab(pitch, "notes(:,3)", System.out);
		Format.matlab(vel, "notes(:,4)", System.out);
		//	if (event.midiCommand == 0x90)
		//		count++;
		//	else
		//		System.out.println("*** Other command: " + event.midiCommand);
	} // main()
}
