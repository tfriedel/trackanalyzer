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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import at.ofai.music.worm.Worm;
import at.ofai.music.worm.WormFile;
import at.ofai.music.worm.WormParameters;
import java.util.ArrayList;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

// Adapted from eventList::readMatchFile in beatroot/src/eventMidi.cpp

// Reads in a Prolog score+performance (.match) file; returns it as an eventList
// Lines in the match file can be of the form:
//		hammer_bounce-PlayedNote.
//		info(Attribute, Value).
//		insertion-PlayedNote.
//		ornament(Anchor)-PlayedNote.
//		ScoreNote-deletion.
//		ScoreNote-PlayedNote.
//		ScoreNote-trailing_score_note.
//		trailing_played_note-PlayedNote.
//		trill(Anchor)-PlayedNote.
// where ScoreNote is of the form
//		snote(Anchor,[NoteName,Modifier],Octave,Bar:Beat,Offset,Duration,
//				BeatNumber,DurationInBeats,ScoreAttributesList)
//		e.g. snote(n1,[b,b],5,1:1,0,3/16,0,0.75,[s])
// and PlayedNote is of the form
//		note(Number,[NoteName,Modifier],Octave,Onset,Offset,AdjOffset,Velocity)
//		e.g. note(1,[a,#],5,5054,6362,6768,53)

class WormFileParseException extends RuntimeException {

	static final long serialVersionUID = 0;
	public WormFileParseException(String s) {
		super(s);
	} // constructor

} // class WormFileParseException

class MatchFileParseException extends RuntimeException {

	static final long serialVersionUID = 0;
	public MatchFileParseException(String s) {
		super(s);
	} // constructor

} // class MatchFileParseException

class BTFileParseException extends RuntimeException {

	static final long serialVersionUID = 0;
	public BTFileParseException(String s) {
		super(s);
	} // constructor

} // class BTFileParseException


// Process the strings which label extra features of notes in match files.
// We assume no more than 32 distinct labels in a file.
class Flags {

	String[] labels = new String[32];
	int size = 0;
	
	int getFlag(String s) {
		if ((s == null) || s.equals(""))
			return 0;
		//int val = 1;
		for (int i = 0; i < size; i++)
			if (s.equals(labels[i]))
				return 1 << i;
		if (size == 32)	{
			System.err.println("Overflow: Too many flags: " + s);
			size--;
		}
		labels[size] = s;
		return 1 << size++;
	} // getFlag()

	String getLabel(int i) {
		if (i >= size)
			return "ERROR: Unknown flag";
		return labels[i];
	} // getLabel()

} // class Flags


// A score/match/midi file is represented as an EventList object,
//  which contains pointers to the head and tail links, and some
//  class-wide parameters. Parameters are class-wide, as it is
//  assumed that the Worm has only one input file at a time.
public class EventList implements Serializable {

	public LinkedList<Event> l;

	protected static boolean timingCorrection = false;
	protected static double timingDisplacement = 0;
	protected static int clockUnits = 480;
	protected static int clockRate = 500000;
	protected static double metricalLevel = 0;
	public static final double UNKNOWN = Double.NaN;
	protected static boolean noMelody = false;
	protected static boolean onlyMelody = false;
	protected static Flags flags = new Flags();

	public EventList() {
		l = new LinkedList<Event>();
	} // constructor

	public EventList(EventList e) {
		this();
		ListIterator<Event> it = e.listIterator();
		while (it.hasNext())
			add(it.next());
	} // constructor

	public EventList(Event[] e) {
		this();
		for (int i=0; i < e.length; i++)
			add(e[i]);
	} // constructor

	public void add(Event e) {
		l.add(e);
	} // add()

	public void add(EventList ev) {
		l.addAll(ev.l);
	} // add()

	public void insert(Event newEvent, boolean uniqueTimes) {
		ListIterator<Event> li = l.listIterator();
		while (li.hasNext()) {
			int sgn = newEvent.compareTo(li.next());
			if (sgn < 0) {
				li.previous();
				break;
			} else if (uniqueTimes && (sgn == 0)) {
				li.remove();
				break;
			}
		}
		li.add(newEvent);
	} // insert()

	public ListIterator<Event> listIterator() {
		return l.listIterator();
	} // listIterator()

	public Iterator<Event> iterator() {
		return l.iterator();
	} // iterator()

	public int size() {
		return l.size();
	} // size()

	public Event[] toArray() {
		return toArray(0);
	} // toArray()

	public double[] toOnsetArray() {
		double[] d = new double[l.size()];
		int i = 0;
		for (Iterator<Event> it = l.iterator(); it.hasNext(); i++)
			d[i] = it.next().keyDown;
		return d;
	} // toOnsetArray()

	public Event[] toArray(int match) {
		int count = 0;
		for (Event e : l)
			if ((match == 0) || (e.midiCommand == match))
				count++;
		Event[] a = new Event[count];
		int i = 0;
		for (Event e : l)
			if ((match == 0) || (e.midiCommand == match))
				a[i++] = e;
		return a;
	} // toArray()

	public void writeBinary(String fileName) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
										new FileOutputStream(fileName));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	} // writeBinary()

	public static EventList readBinary(String fileName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(
										new FileInputStream(fileName));
			EventList e = (EventList) ois.readObject();
			ois.close();
			return e;
		} catch (IOException e) {
			System.err.println(e);
			return null;
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			return null;
		}
	} // readBinary()

	public void writeMIDI(String fileName) {
		writeMIDI(fileName, null);
	} // writeMIDI()

	public void writeMIDI(String fileName, EventList pedal) {
		try {
			MidiSystem.write(toMIDI(pedal), 1, new File(fileName));
		} catch (Exception e) {
			System.err.println("Error: Unable to write MIDI file " + fileName);
			e.printStackTrace();
		}
	} // writeMIDI()

	public Sequence toMIDI(EventList pedal) throws InvalidMidiDataException {
		final int midiTempo = 1000000;
		Sequence s = new Sequence(Sequence.PPQ, 1000);
		Track[] tr = new Track[16];
		tr[0] = s.createTrack();
		MetaMessage mm = new MetaMessage();
		byte[] b = new byte[3];
		b[0] = (byte)((midiTempo >> 16) & 0xFF);
		b[1] = (byte)((midiTempo >> 8) & 0xFF);
		b[2] = (byte)(midiTempo & 0xFF);
		mm.setMessage(0x51, b, 3);
		tr[0].add(new MidiEvent(mm, 0L));
		for (Event e : l) {		// from match or beatTrack file
			if (e.midiCommand == 0)	// skip beatTrack file
				break;
			if (tr[e.midiTrack] == null)
				tr[e.midiTrack] = s.createTrack();
			//switch (e.midiCommand) 
			//case ShortMessage.NOTE_ON:
			//case ShortMessage.POLY_PRESSURE:
			//case ShortMessage.CONTROL_CHANGE:
			//case ShortMessage.PROGRAM_CHANGE:
			//case ShortMessage.CHANNEL_PRESSURE:
			//case ShortMessage.PITCH_BEND:
			ShortMessage sm = new ShortMessage();
			sm.setMessage(e.midiCommand, e.midiChannel,
							e.midiPitch, e.midiVelocity);
			tr[e.midiTrack].add(new MidiEvent(sm,
						(long)Math.round(1000 * e.keyDown)));
			if (e.midiCommand == ShortMessage.NOTE_ON) {
				sm = new ShortMessage();
				sm.setMessage(ShortMessage.NOTE_OFF, e.midiChannel, e.midiPitch, 0);
				tr[e.midiTrack].add(new MidiEvent(sm, (long)Math.round(1000 * e.keyUp)));
			}
		}
		if (pedal != null) {	// from MIDI file
	//		if (t.size() > 0)	// otherwise beatTrack files leave an empty trk
	//			t = s.createTrack();
			for (Event e : pedal.l) {
				if (tr[e.midiTrack] == null)
					tr[e.midiTrack] = s.createTrack();
				ShortMessage sm = new ShortMessage();
				sm.setMessage(e.midiCommand, e.midiChannel, 
								e.midiPitch, e.midiVelocity);
				tr[e.midiTrack].add(new MidiEvent(sm,
						(long)Math.round(1000 * e.keyDown)));
				if (e.midiCommand == ShortMessage.NOTE_ON) {
					sm = new ShortMessage();
					sm.setMessage(ShortMessage.NOTE_OFF, e.midiChannel,
									e.midiPitch,e.midiVelocity);
					tr[e.midiTrack].add(new MidiEvent(sm,
							(long)Math.round(1000 * e.keyUp)));
				}
				//catch (InvalidMidiDataException exception) {}
			}
		}
		return s;
	} // toMIDI()

	public static EventList readMidiFile(String fileName) {
		return readMidiFile(fileName, 0);
	} // readMidiFile()

	public static EventList readMidiFile(String fileName, int skipTrackFlag) {
		EventList list = new EventList();
		Sequence s;
		try {
			s = MidiSystem.getSequence(new File(fileName));
		} catch (Exception e) {
			e.printStackTrace();
			return list;
		}
		double midiTempo = 500000;
		double tempoFactor = midiTempo / s.getResolution() / 1000000.0;
		// System.err.println(tempoFactor);
		Event[][] noteOns = new Event[128][16];
		Track[] tracks = s.getTracks();
		for (int t = 0; t < tracks.length; t++, skipTrackFlag >>= 1) {
			if ((skipTrackFlag & 1) == 1)
				continue;
			for (int e = 0; e < tracks[t].size(); e++) {
				MidiEvent me = tracks[t].get(e);
				MidiMessage mm = me.getMessage();
				double time = me.getTick() * tempoFactor;
				byte[] mesg = mm.getMessage();
				int channel = mesg[0] & 0x0F;
				int command = mesg[0] & 0xF0;
				if (command == ShortMessage.NOTE_ON) {
					int pitch = mesg[1] & 0x7F;
					int velocity = mesg[2] & 0x7F;
					if (noteOns[pitch][channel] != null) {
						if (velocity == 0) {	// NOTE_OFF in disguise :(
							noteOns[pitch][channel].keyUp = time;
							noteOns[pitch][channel].pedalUp = time;
							noteOns[pitch][channel] = null;
						} else
 							System.err.println("Double note on: n=" + pitch +
									" c=" + channel +
									" t1=" + noteOns[pitch][channel] +
									" t2=" + time);
					} else {
						Event n = new Event(time, 0, 0, pitch, velocity, -1, -1,
										0, ShortMessage.NOTE_ON, channel, t);
						noteOns[pitch][channel] = n;
						list.add(n);
					}
				} else if (command == ShortMessage.NOTE_OFF) {
					int pitch = mesg[1] & 0x7F;
					noteOns[pitch][channel].keyUp = time;
					noteOns[pitch][channel].pedalUp = time;
					noteOns[pitch][channel] = null;
				} else if (command == 0xF0) {
					if ((channel == 0x0F) && (mesg[1] == 0x51)) {
						midiTempo = (mesg[5] & 0xFF) |
									((mesg[4] & 0xFF) << 8) |
									((mesg[3] & 0xFF) << 16);
						tempoFactor = midiTempo / s.getResolution() / 1000000.0;
					//	System.err.println("Info: Tempo change: " + midiTempo +
					//						"  tf=" + tempoFactor);
					}
				} else if (mesg.length > 3) {
					System.err.println("midi message too long: " + mesg.length);
					System.err.println("\tFirst byte: " + mesg[0]);
				} else {
					int b0 = mesg[0] & 0xFF;
					int b1 = -1;
					int b2 = -1;
					if (mesg.length > 1)
						b1 = mesg[1] & 0xFF;
					if (mesg.length > 2)
						b2 = mesg[2] & 0xFF;
					list.add(new Event(time, time, -1, b1, b2, -1, -1, 0,
										b0 & 0xF0, b0 & 0x0F, t));
				}
			}
		}
		for (int pitch = 0; pitch < 128; pitch++)
			for (int channel = 0; channel < 16; channel++)
				if (noteOns[pitch][channel] != null)
					System.err.println("Missing note off: n=" + 
							noteOns[pitch][channel].midiPitch + " t=" +
							noteOns[pitch][channel].keyDown);
		return list;
	} // readMidiFile()

	public void print() {
		for (Iterator<Event> i = l.iterator(); i.hasNext(); )
			i.next().print(flags);
	} // print()

	public static void setTimingCorrection(double corr) {
		timingCorrection = corr >= 0;
		timingDisplacement = corr;
	} // setTimingCorrection()

	public static EventList readBeatsAsText(String fileName) throws Exception {
		EventList list = new EventList();
		BufferedReader inputFile = new BufferedReader(new FileReader(fileName));
		String s = inputFile.readLine();
		if (s.startsWith("###"))
			return readLabelFile(fileName);
		int beats = 0;
		int pitch = 56;
		int vol = 80;
		int ch = 10;
		int track = 0;
		int fl = 1;
		while (s != null) {
			int ind = s.indexOf(',');
			if (ind < 0)
				ind = s.indexOf(' ');
			double time = 0;
			if (ind >= 0) {
				String tmp = s.substring(0,ind).trim();
				if (tmp.length() == 0) {
					s = inputFile.readLine();
					continue;
				}
				time = Double.parseDouble(tmp);
				s = s.substring(ind+1);
			} else {
				String tmp = s.trim();
				if (tmp.length() > 0)
					time = Double.parseDouble(tmp);
				s = inputFile.readLine();
			}
			list.add(new Event(time, time, time, pitch, vol, ++beats,
				1.0, fl, ShortMessage.NOTE_ON, ch, track));
		}
		return list;
	} // readBeatsAsText()
	
	public static EventList readBeatTrackFile(String fileName) throws Exception{
		if (!fileName.endsWith(".tmf")) // || fileName.endsWith(".csv"))
			return readBeatsAsText(fileName);
		else {
			EventList list = new EventList();
			BufferedReader inputFile = new BufferedReader(new FileReader(fileName));
			Matcher s = new Matcher(inputFile.readLine());
			if (!s.matchString("MFile"))
				throw new BTFileParseException("Header not found");
			s.getInt();	// skip fileType
			int tracks = s.getInt();
			int div = s.getInt();
			int tempo = 500000;	// default tempo
			double tf = 1e6 / tempo * div;
			int lineCount = 1;
			int beats = 0;
			for (int track = 0; track < tracks; track++) {
				s.set(inputFile.readLine());
				lineCount++;
				if (!s.matchString("MTrk"))
					throw new BTFileParseException("MTrk not found");
				s.set(inputFile.readLine());
				lineCount++;
				while (!s.matchString("TrkEnd")) {
					double time = s.getInt() / tf;
					s.trimSpace();
					if (s.matchString("Tempo")) {
						tempo = s.getInt();
						tf = 1e6 / tempo * div;
					} else if (s.matchString("On")) {
						s.trimSpace();
						s.matchString("ch=");
						int ch = s.getInt();
						s.trimSpace();
						if (!s.matchString("n="))
							s.matchString("note=");
						int pitch = s.getInt();
						s.trimSpace();
						if (!s.matchString("v="))
							s.matchString("vol=");
						int vol = s.getInt();
						s.set(inputFile.readLine());
						lineCount++;
						s.getInt();
						s.trimSpace();
						s.matchString("Off");
						s.skip('v');
						s.matchString("ol");
						s.matchString("=");
						int flags = s.getInt();
						list.add(new Event(time, time, time, pitch, vol, ++beats,
								1.0, flags, ShortMessage.NOTE_ON, ch, track));
					} else if (!s.matchString("Meta TrkEnd")) {
						System.err.println("Unmatched text on line " + lineCount +
								": " + s.get());
					}
					s.set(inputFile.readLine());
					lineCount++;
				}
			}
			return list;
		}
	} // readBeatTrackFile()

	public void writeBeatsAsText(String fileName) throws Exception {
		PrintStream out = new PrintStream(new File(fileName));
		char separator = '\n';
		if (fileName.endsWith(".csv"))
			separator = ',';
		for (Iterator<Event> it = iterator(); it.hasNext(); ) {
			Event e = it.next();
			out.printf("%5.3f%c", e.keyDown, it.hasNext()? separator: '\n');
		}
		out.close();
	} // writeBeatsAsText()

	public double getBPM() {
        double maxbpm = 165;
		double minbpm = 67;
		ArrayList<Double> onsetList = new ArrayList<Double>();
		for (Iterator<Event> it = iterator(); it.hasNext(); ) {
			Event e = it.next();
			onsetList.add(e.keyDown);
		}
		DescriptiveStatistics stats = new DescriptiveStatistics();
		if (onsetList.size()>1) 
			for (int i=1;i<onsetList.size();i++) {
				stats.addValue(onsetList.get(i)-onsetList.get(i-1));
			}
		
		double median = stats.getPercentile(50);
		double bpm = 60/median;
        if (bpm > maxbpm) {
            bpm = bpm/2;
        } else if (bpm < minbpm) {
			bpm = bpm*2;
		}
		
		return bpm;
	}
			
	public void writeBeatTrackFile(String fileName) throws Exception {
		if (fileName.endsWith(".txt") || fileName.endsWith(".csv"))
			writeBeatsAsText(fileName);
		else {
			PrintStream out = new PrintStream(new File(fileName));
			out.println("MFile 0 1 500");
			out.println("MTrk");
			out.println("     0 Tempo 500000");
			int time = 0;
			for (Iterator<Event> it = iterator(); it.hasNext(); ) {
				Event e = it.next();
				time = (int) Math.round(1000 * e.keyDown);
				out.printf("%6d On   ch=%3d n=%3d v=%3d\n",
							time, e.midiChannel, e.midiPitch, e.midiVelocity);
				out.printf("%6d Off  ch=%3d n=%3d v=%3d\n",
							time, e.midiChannel, e.midiPitch, e.flags);
			}
			out.printf("%6d Meta TrkEnd\nTrkEnd\n", time);
			out.close();
		}
	} // writeBeatTrackFile()


	/** Reads a file containing time,String pairs into an EventList. */
	public static EventList readLabelFile(String fileName) throws Exception {
		EventList list = new EventList();
		BufferedReader inputFile = new BufferedReader(new FileReader(fileName));
		Matcher s = new Matcher(inputFile.readLine());
		int prevBar = 0;
		int beats = 0;
		int pitch = 56;
		int vol = 80;
		int ch = 10;
		int track = 0;
		while (s.hasData()) {
			if (!s.matchString("#")) {
				double time = s.getDouble();
				String label = s.get().trim();
				int colon = label.indexOf(':');
				int beat = 0;
				if (colon < 0)
					colon = label.length();
				else
					beat = Integer.parseInt(label.substring(colon+1));
				int bar = Integer.parseInt(label.substring(0, colon));
				int flags = WormFile.BEAT;
				if (bar != prevBar) {
					flags |= WormFile.BAR;
					prevBar = bar;
				}
				WormEvent ev = new WormEvent(time, time, time, pitch, vol,
						++beats,1.0,flags, ShortMessage.NOTE_ON, ch, track);
				ev.label = label;
				list.add(ev);
//				System.out.println(time + " " + label);
			}
			s.set(inputFile.readLine());
		}
		return list;
	} // readLabelFile()

	public void writeLabelFile(String fileName) throws Exception {
		PrintStream out = new PrintStream(new File(fileName));
		out.printf("###Created automatically\n");
		for (Event ev : l)
			out.printf("%5.3f\t%s\n", ev.keyDown, ((WormEvent)ev).label);
		out.close();
	} // writeLabelFile()

	public static EventList readWormFile(String fileName) throws Exception {
		EventList list = new EventList();
		BufferedReader inputFile = new BufferedReader(new FileReader(fileName));
		Matcher s = new Matcher(inputFile.readLine());
		int lineCount = 1;
		if (!s.matchString("WORM Version:"))
			throw new WormFileParseException("WORM format: header not found");
		if (s.getDouble() < 1.01)
			throw new WormFileParseException("WORM format: v1.0 not supported");
		int dataCountDown = -1;
		int beat = 0;
		while (true) {
			s.set(inputFile.readLine());
			lineCount++;
			if (dataCountDown == 0) {
				if (s.hasData())
					System.err.println("Ignoring trailing data past line " +
										lineCount);
				return list;
			} else if (!s.hasData())
				throw new WormFileParseException("Unexpected EOF");
			if (dataCountDown < 0) {
				if (s.matchString("Length:"))
					dataCountDown = s.getInt();
			} else {
				double time = s.getDouble();
				double tempo = s.getDouble();
				double loudness = s.getDouble();
				int flags = s.getInt();
				if ((flags & WormFile.TRACK) != 0)
					beat++;		// i.e. always, as index for comparing files 
				list.add(new WormEvent(time, tempo, loudness, beat, flags));
				dataCountDown--;
			}
		}
	} // readWormFile()

	public static String getAudioFileFromWormFile(String wormFile) {
		return getWormFileAttribute(wormFile, "AudioFile");
	} // getAudioFileFromWormFile()

	public static double getTrackLevelFromWormFile(String wormFile) {
		String level = getWormFileAttribute(wormFile,WormParameters.TRACKLEVEL);
		try {
			int i = level.indexOf("/");
			if (i >= 0)
				return Double.parseDouble(level.substring(0,i)) /
						Double.parseDouble(level.substring(i+1));
			else
				return Double.parseDouble(level);
		} catch (Exception e) {
			System.err.println("Error getting TrackLevel:\n" + e);
			return 1;
		}
	} // getTrackLevelFromWormFile()

	public static String getWormFileAttribute(String wormFile, String attr) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(wormFile));
			String line = r.readLine();
			attr += ":";
			while (line != null) {
				if (line.startsWith(attr))
					return line.substring(attr.length()).trim();
				line = r.readLine();
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	} // getWormFileAttribute()

	public static EventList readMatchFile(String fileName) throws Exception {
		EventList list = new EventList();
		boolean startNote = timingCorrection;
		int eventFlags, numerator, denominator;
		String element;
		BufferedReader inputFile = new BufferedReader(new FileReader(fileName));
		double versionNumber = 1.0;
		double onset, offset, eOffset, beat, duration;
		int velocity, pitch, octave;
		int lineCount = 1;
		Matcher s = new Matcher(inputFile.readLine());
		while (s.hasData()) {
			eventFlags = 0;
			beat = UNKNOWN;
			duration = UNKNOWN;
			// System.out.println("Processing line " + lineCount);
			if (s.matchString("info(")) {	// meta-data
				if (s.matchString("timeSignature,")) {
					numerator = s.getInt();
					// ss1 << "beatsPerBar=" << numerator << ends;
					s.skip('/');
					denominator = s.getInt();
					// ss2 << "beatUnits=" << denominator;
				} else if (s.matchString("beatSubdivision,")) {
					// strcpy(buf, "beatSubdivisions=");
					// int i = strlen(buf);
					// f.getline(buf+i, SZ-i, ']');
					// strcat(buf, "]");
					// parameters->add(buf);
					s.skip(']');
				} else if (s.matchString("matchFileVersion,")) {
					versionNumber = s.getDouble();
				} else if (s.matchString("midiClockUnits,")) {
					clockUnits = s.getInt();
				} else if (s.matchString("midiClockRate,")) {
					clockRate = s.getInt();
				}
				s.set("%");	// don't expect the second half of the Prolog term
			} else if (s.matchString("snote(")) {
				s.skip(',');	// identifier
				s.skip(']');	// note name
				s.skip(',');	// ',' after note name
				s.skip(',');	// octave
				s.skip(',');	// onset time (in beats, integer part, bar:beat)
				boolean isBt = s.matchString("0");
				s.skip(',');	// onset time (in beats, fractional part)
				s.skip(',');	// duration (in beats, fraction)
				try {
					beat = s.getDouble();
				} catch (NumberFormatException e) {
					System.err.println("Bad beat number on line " + lineCount);
					beat = UNKNOWN;
				}
				if ((beat == Math.rint(beat)) != isBt)
					System.err.println("Inconsistent beats on line "+lineCount);
				s.skip(',');	// onset time (in beats, decimal) 
				try {
					duration = s.getDouble() - beat;
				} catch (NumberFormatException e) {
					System.err.println("Bad duration on line " + lineCount);
					duration = UNKNOWN;
				}
				s.skip(',');	// offset time (in beats, decimal)
				s.skip('[');	// additional info (e.g. melody/arpeggio/grace)
				do {
					element = s.getString();
					eventFlags |= flags.getFlag(element);
				} while (s.matchString(","));
				s.skip('-');
			} else if (s.matchString("trill(")) {
				eventFlags |= flags.getFlag("trill");
				s.skip('-');
			} else if (s.matchString("ornament(")) {
				eventFlags |= flags.getFlag("ornament");
				s.skip('-');
			} else if (s.matchString("trailing_played_note-") ||
					   s.matchString("hammer_bounce-") ||   
					   s.matchString("no_score_note-") ||
					   s.matchString("insertion-")) {
				eventFlags |= flags.getFlag("unscored");
			} else if (!s.matchString("%")) {		// Prolog comment
				throw new MatchFileParseException("error 4; line "+lineCount);
			}
			// READ 2nd term of Prolog expression
			if (s.matchString("note(")) {
				s.skip('[');	// skip identifier
				String note = s.getString();
				switch(Character.toUpperCase(note.charAt(0))) {
					case 'A': pitch =  9; break;
					case 'B': pitch = 11; break;
					case 'C': pitch =  0; break; 
					case 'D': pitch =  2; break;
					case 'E': pitch =  4; break;
					case 'F': pitch =  5; break;
					case 'G': pitch =  7; break;
					default:  throw new MatchFileParseException(
											"Bad note on line " + lineCount);
				}
				s.skip(',');
				String mod = s.getString();
				for (int i = 0; i < mod.length(); i++) {
					switch (mod.charAt(i)) {
						case '#': pitch++; break;
						case 'b': pitch--; break;
						case 'n': break;
						default: throw new MatchFileParseException("error 5 " +
																	lineCount);
					}
				}
				s.skip(',');
				octave = s.getInt();
				pitch += 12 * octave;
				s.skip(',');
				onset = s.getInt();
				s.skip(',');
				offset = s.getInt();
				if (versionNumber > 1.0) {
					s.skip(',');
					eOffset = s.getInt();
				} else
					eOffset = offset;
				s.skip(',');
				velocity = s.getInt();
				onset /= clockUnits * 1000000.0 / clockRate;
				offset /= clockUnits * 1000000.0 / clockRate;
				eOffset /= clockUnits * 1000000.0 / clockRate;
				if (timingCorrection) {
					if (startNote) {
						timingDisplacement = onset - timingDisplacement;
						startNote = false;
					}
					onset -= timingDisplacement;
					offset -= timingDisplacement;
					eOffset -= timingDisplacement;
				}
				int m = flags.getFlag("s");
				if ((((eventFlags & m) != 0) && !noMelody) ||
						(((eventFlags & m) == 0) && !onlyMelody)) {
					Event e = new Event(onset, offset, eOffset, pitch, velocity,
										beat, duration, eventFlags);
					list.add(e);
				}
			} else if (!s.matchString("no_played_note.") &&
					   !s.matchString("trailing_score_note.") &&
					   !s.matchString("deletion.") &&
					   !s.matchString("%"))
				throw new MatchFileParseException("error 6; line " + lineCount);
			s.set(inputFile.readLine());
			lineCount++;
		}
		return list;
	} // readMatchFile()

	public static void main(String[] args) throws Exception {	// quick test
		//System.out.println("Test");
		//readLabelFile(args[0]).writeLabelFile("tmp.txt");
		readLabelFile(args[0]).print();
		System.exit(0);
		EventList el = readMatchFile(args[0]);
		WormFile wf = new WormFile(null, el);
		if (args.length >= 2) {
			double sm = Double.parseDouble(args[1]);
			wf.smooth(Worm.FULL_GAUSS, sm, sm, 0);
		} else
			wf.smooth(Worm.NONE, 0, 0, 0);
		wf.write("worm.out");
		if (args.length == 3)
			el.print();
	} // main()

} // class EventList
