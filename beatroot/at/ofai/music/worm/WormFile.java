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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.awt.Frame;
import at.ofai.music.util.Event;
import at.ofai.music.util.EventList;
import at.ofai.music.util.Format;
import at.ofai.music.util.MatchTempoMap;

// Read/write performance worm data
public class WormFile {

	Worm worm;
	double outFramePeriod, inFramePeriod;
	int length;
	double[] time;
	double[] inTempo, outTempo;
	double[] inIntensity, outIntensity;
	int[] inFlags, outFlags;
	String[] label;
	public static final int
		TRACK=1, BEAT=2, BAR=4, SEG1=8, SEG2=16, SEG3=32, SEG4=64;
	public static final double defaultFramePeriod = 0.1;	// 10 FPS
	WormParameters info;

	private WormFile(Frame f) {
		info = new WormParameters(f);
		inFramePeriod = defaultFramePeriod;
		outFramePeriod = defaultFramePeriod;
	} // shared constructor

	public WormFile(int size) {
		this(null);
		length = size;
		init();
	} // constructor

	public WormFile(int size, double step) {
		this(size);
		inFramePeriod = step;
	} // constructor

	public WormFile(EventList el, double step) {
		this(null);
		inFramePeriod = step;
		convertList(el);
	} // constructor

	public WormFile(Worm w, EventList el) {
		this(w == null? null: w.theFrame);
		worm = w;
		convertList(el);
	} // constructor

	public WormFile(Worm w, String fileName) {
		this(w.theFrame);
		worm = w;
		read(fileName);
	} // constructor

	public void init() {
		inTempo = new double[length];
		inIntensity = new double[length];
		inFlags = new int[length];
		time = new double[length];
	} // init()

	public void smooth(int mode, double left, double right, int smoothLevel) {
		if (worm != null)
			worm.setSmoothMode(Worm.NONE);
		info.smoothing = "None";
		if ((outFramePeriod == 0) || ((inFramePeriod == 0) && (time == null))) {
			System.err.println("Error: smooth() frameLength unspecified");
			return;
		}
		if (inFramePeriod != 0) {
			for (int i = 0; i < length; i++)
				time[i] = inFramePeriod * i;
		}
		int outLength = 1+(int) Math.ceil(time[time.length-1] / outFramePeriod);
		if ((outTempo == null) || (outTempo.length != outLength)) {
			outTempo = new double[outLength];
			outIntensity = new double[outLength];
			outFlags = new int[outLength];
			label = new String[outLength];
		}
		if (mode == Worm.NONE) {
			int i = 0, o = 0;
			while (o * outFramePeriod < time[0]) {
				outTempo[o] = inTempo[0];
				outIntensity[o] = inIntensity[0];
				o++;
			}
			for ( ; i < time.length - 1; i++) {
				while (o * outFramePeriod < time[i+1]) {
					outTempo[o] = inTempo[i];
					outIntensity[o] = inIntensity[i];
					o++;
				}
			}
			while (o < outLength) {
				outTempo[o] = inTempo[i];
				outIntensity[o] = inIntensity[i];
				o++;
			}
		} else {
			info.smoothing = "Gaussian" + "\t" + Format.d(left, 4) +
									 "\t" + Format.d(right, 4);
			if (smoothLevel != 0) {
				int count = 0;
				double first = 0, last = 0;
				for (int i = 0; i < time.length; i++)
					if ((inFlags[i] & smoothLevel) != 0) {
						if (count == 0)
							first = time[i];
						else
							last = time[i];
						count++;
					}
				if (count < 2)
					System.err.println("Warning: Beat data not available");
				else {
					double IBI = (last - first) / (count - 1); 
					left *= IBI;
					right *= IBI;
					info.smoothing += "\t" +Format.d(IBI,4) + "\t" +smoothLevel;
					System.out.println("Smoothing parameters (seconds): pre=" +
							Format.d(left,3) + " post=" + Format.d(right,3));
				}
			}
			int start = 0;
			for (int o = 0; o < outLength; o++) {
				double sum = 0, val = 0, tempo = 0, intensity = 0;
				for (int i = start; i < time.length; i++) {
					double d = o * outFramePeriod - time[i];
					if (d > 4 * left) {	// average over 4 stddevs
						start++;
						continue;
					}
					if (d < -4 * right)
						break;
					if (d < 0)
						val = Math.exp(-d*d/(left*left*2));
					else
						val = Math.exp(-d*d/(right*right*2));
					sum += val;
					tempo += val * inTempo[i];
					intensity += val * inIntensity[i];
				}
				if (sum == 0) {		// assume this only occurs at beginning
					outTempo[o] = inTempo[0];
					outIntensity[o] = inIntensity[0];
				} else {
					outTempo[o] = tempo / sum;
					outIntensity[o] = intensity / sum;
				}
			}
		}
		for (int i = 0; i < outFlags.length; i++)
			outFlags[i] = 0;
		for (int i = 0; i < inFlags.length; i++)
			outFlags[(int)Math.round(time[i] / outFramePeriod)] |= inFlags[i];
		int bar = 0;
		int beat = 0;
		int track = 0;
		for (int i = 0; i < outFlags.length; i++) {
			if ((outFlags[i] & BAR) != 0)
				bar++;
			if ((outFlags[i] & BEAT) != 0)
				beat++;
			if ((outFlags[i] & TRACK) != 0)
				track++;
			label[i] = bar + ":" + beat + ":" + track + ":" +
						Format.d(i * outFramePeriod, 1);
		}
	} // smooth()
	
	public void editParameters() {
		info.editParameters();
		update();
	} // editParameters()

	public void update() {
		length = info.length;
		inFramePeriod = info.framePeriod;
		worm.setTitle(info.composer + ", " + info.piece +
						", played by " + info.performer);
		// not used (?) : beatLevel trackLevel upbeat beatsPerBar
		if ((inTempo == null) || (inTempo.length != length))
			init();
		worm.setInputFile(info.audioPath, info.audioFile);
		worm.setSmoothMode(Worm.NONE);
		if (info.axis.length() > 0)
			worm.setAxis(info.axis);
		worm.setFramePeriod(outFramePeriod);
		worm.setLoudnessUnits(info.loudnessUnits);
	} // update()

	public void convertList(EventList el) {
		double tMax = 0;
		int count = 0;
		for (Iterator<Event> i = el.iterator(); i.hasNext(); ) {
			double pedalUpTime = i.next().pedalUp;
			if (pedalUpTime > tMax)
				tMax = pedalUpTime;
			count++;
		}
		length = (int)Math.ceil(tMax / inFramePeriod);
		init();
		// double[] decayFactor = new double[128];
		// for (int i = 0; i < 128; i++)
		// 	decayFactor[i] = Math.max(5.0, (i - 6.0) / 3.0) * inFramePeriod;
		// 	// was Math.pow(0.1, inFramePeriod);	// modify for pitch?
		for (Iterator<Event> i = el.l.iterator(); i.hasNext(); ) {
			Event e = i.next();
			double loudness = 30.29 * Math.pow(e.midiVelocity, 0.2609);
			loudness += (e.midiPitch - 66.0) / 12.0; // +1dB / oct
			int start = (int)Math.floor(e.keyDown / inFramePeriod);
			if (start < 0)
				start = 0;
			int stop = (int)Math.ceil((e.pedalUp + 0.5) / inFramePeriod);
			if (stop > inIntensity.length)
				stop = inIntensity.length;
			for (int t = start; t < stop; t++) {
				if (loudness > inIntensity[t])
					inIntensity[t] = loudness;
				loudness -= Math.max(5.0, (e.midiPitch - 6.0) / 3.0) *
											inFramePeriod;
				// was: mult by decay factor. But since vals are dB, we subtract
			}
		}
		MatchTempoMap tMap = new MatchTempoMap(count);
		for (Iterator<Event> i = el.l.iterator(); i.hasNext(); ) {
			Event e = i.next();
			tMap.add(e.keyDown, e.scoreBeat);
		}
		// el.print();
		// tMap.print();	// for debugging
		tMap.dump(inTempo, inFramePeriod);
	} // convertList()

	public void write(String fileName) {
		PrintStream out;
		try {
			out = new PrintStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			System.err.println("Unable to open output file " + fileName);
			return;
		}
		info.write(out, outTempo.length, outFramePeriod);
		for (int i = 0; i < outTempo.length; i++) {
			if (outFramePeriod == 0)
				out.print(Format.d(time[i],3) + " ");
			out.println(Format.d(outTempo[i],4) +" "+
						Format.d(outIntensity[i],4) +" "+outFlags[i]);
		}
		out.close();
	} // write()

	public void read(String fileName) {
		try {
			File f = new File(fileName);
			if (!f.isFile())	// a local hack for UNC file names under Windows
				f = new File("//fichte" + fileName);
			if (!f.isFile())
				throw(new FileNotFoundException("Could not open " + fileName));
			BufferedReader in = new BufferedReader(new FileReader(f));
			String input = info.read(in);
			update();
			int index = 0;
			int bar = 0;
			while ((input != null) && (index < length)) {
				StringTokenizer tk = new StringTokenizer(input);
				if (inFramePeriod != 0)
					time[index] = Double.parseDouble(tk.nextToken());
				inTempo[index] = Double.parseDouble(tk.nextToken());
				inIntensity[index] = Double.parseDouble(tk.nextToken());
				if (tk.hasMoreTokens())
					inFlags[index] = Integer.parseInt(tk.nextToken());
				else
					inFlags[index] = 0;
				input = in.readLine();
				index++;
			}
			in.close();
			smooth(Worm.NONE, 0, 0, 0);
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println("IOException reading " + fileName);
		} catch (Exception e) {
			System.err.println("Error parsing file " + fileName + ": " + e);
			e.printStackTrace();
		}
	} // read()

} // class WormFile
