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

import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.IOException;
import at.ofai.music.util.Parameters;

public class WormParameters extends Parameters {

	static final long serialVersionUID = 0;
	public static final String VERSION = "WORM Version";
	public static final String FRAMEPERIOD = "FrameLength";
	public static final String COMPOSER = "Composer";
	public static final String PIECE = "Piece";
	public static final String PERFORMER = "Performer";
	public static final String KEY = "Key";
	public static final String YEAR = "YearOfRecording";
	public static final String INDICATION = "Indication";
	public static final String BEATLEVEL = "BeatLevel";
	public static final String TRACKLEVEL = "TrackLevel";
	public static final String STARTBAR = "StartBarNumber";
	public static final String UPBEAT = "Upbeat";
	public static final String BEATSPERBAR = "BeatsPerBar";
	public static final String LENGTH = "Length";
	public static final String AUDIOPATH = "AudioPath";
	public static final String AUDIOFILE = "AudioFile";
	public static final String SMOOTHING = "Smoothing";
	public static final String AXIS = "Axis";
	public static final String RESOLUTION = "Time Resolution";
	public static final String UNITS = "LoudnessUnits";
	public static final String TEMPOLATE = "TempoLate";
	public static final String TITLE = "Edit Worm Parameters";
	public static final String SEP = ":\t";
	public static final char SEPCHAR = ':';

	protected double framePeriod;
	protected String trackLevel, loudnessUnits, version, composer, piece,
				performer, key, year, indication, audioFile, audioPath,
				smoothing, axis, beatLevel, upbeat, startBar, tempoLate;
	protected int beatsPerBar, length;
	
	public WormParameters(java.awt.Frame f) {
		super(f, TITLE);
		composer = "Unknown composer";
		piece = "unknown piece";
		performer = "unknown performer";
		key = "";
		year = "";
		indication = "";
		beatLevel = "1/4";
		trackLevel = "1.0";
		upbeat = "0";
		startBar = "1";
		beatsPerBar = 4;
		length = 0;
		audioFile = "";
		audioPath = "";
		smoothing = "";
		axis = "";
		version = "1.0";
		loudnessUnits = "dB";
		tempoLate = "";
		framePeriod = WormFile.defaultFramePeriod;
	} // constructor

	public void editParameters() {
		editParameters(true);
	} // editParameters()

	public void editParameters(boolean doEdit) {
		setString(COMPOSER, composer);
		setString(PIECE, piece);
		setString(PERFORMER, performer);
		setString(KEY, key);
		setString(YEAR, year);
		setString(INDICATION, indication);
		setString(BEATLEVEL, beatLevel);	// e.g. 3/8
		setString(TRACKLEVEL, trackLevel);
		setString(UPBEAT, upbeat);
		setString(STARTBAR, startBar);
		setInt(BEATSPERBAR, beatsPerBar);
		setInt(LENGTH, length);
		setString(AUDIOPATH, audioPath);
		setString(AUDIOFILE, audioFile);
		setString(SMOOTHING, smoothing);
		setString(AXIS, axis);
		setString(VERSION, version);
		setDouble(RESOLUTION, framePeriod);
		setString(UNITS, loudnessUnits);
		setString(TEMPOLATE, tempoLate);
		setVisible(doEdit);
		composer = getString(COMPOSER);
		piece = getString(PIECE);
		performer = getString(PERFORMER);
		key = getString(KEY);
		year = getString(YEAR);
		indication = getString(INDICATION);
		beatLevel = getString(BEATLEVEL);	// e.g. 3/8
		trackLevel = getString(TRACKLEVEL);
		upbeat = getString(UPBEAT);
		startBar = getString(STARTBAR);
		beatsPerBar = getInt(BEATSPERBAR);
		length = getInt(LENGTH);
		audioPath = getString(AUDIOPATH);
		audioFile = getString(AUDIOFILE);
		smoothing = getString(SMOOTHING);
		axis = getString(AXIS);
		version = getString(VERSION);
		framePeriod = getDouble(RESOLUTION);
		loudnessUnits = getString(UNITS);
		tempoLate = getString(TEMPOLATE);
	} // editParameters()

	public void write(PrintStream out, int length, double outFramePeriod) {
		out.println(VERSION + SEP + version);
		out.println(FRAMEPERIOD + SEP + outFramePeriod);
		out.println(UNITS + SEP + loudnessUnits);
		if ((audioPath.length() > 0) && !audioPath.endsWith("/"))
			audioPath += "/";
		out.println(AUDIOFILE + SEP + audioPath + audioFile);
		out.println(SMOOTHING + SEP + smoothing);
		out.println(COMPOSER + SEP + composer);
		out.println(PIECE + SEP + piece);
		out.println(PERFORMER + SEP + performer);
		out.println(BEATLEVEL + SEP + beatLevel);
		out.println(TRACKLEVEL + SEP + trackLevel);
		out.println(UPBEAT + SEP + upbeat);
		out.println(STARTBAR + SEP + startBar);
		out.println(BEATSPERBAR + SEP + beatsPerBar);
		out.println(AXIS + SEP + axis);
		out.println(TEMPOLATE + SEP + tempoLate);
		out.println(LENGTH + SEP + length);
	} // write()

	public String read(BufferedReader in) throws IOException {
		String input = in.readLine();
		if (input == null)
			throw new RuntimeException("Empty input file");
		if (!input.startsWith("WORM"))
			throw new RuntimeException("Bad header format: not a WORM file");
		int delimiter = input.indexOf(SEPCHAR);
		while (delimiter >= 0) {
			String attribute = input.substring(0,delimiter).trim();
			String value = input.substring(delimiter+1).trim();
			if (attribute.equalsIgnoreCase(VERSION))
				version = value;
			else if (attribute.equalsIgnoreCase(FRAMEPERIOD))
				framePeriod = Double.parseDouble(value);
			else if (attribute.equalsIgnoreCase(UNITS))
				loudnessUnits = value;
			else if (attribute.equalsIgnoreCase(LENGTH))
				length = Integer.parseInt(value);
			else if (attribute.equalsIgnoreCase(AUDIOFILE)) {
				int index = value.lastIndexOf('/');
				if (index >= 0)
					audioPath = value.substring(0, index);
				audioFile = value.substring(index + 1);
			} else if (attribute.equalsIgnoreCase(SMOOTHING))
				smoothing = value;
			else if (attribute.equalsIgnoreCase(COMPOSER))
				composer = value;
			else if (attribute.equalsIgnoreCase(PIECE))
				piece = value;
			else if (attribute.equalsIgnoreCase(PERFORMER))
				performer = value;
			else if (attribute.equalsIgnoreCase(KEY))
				key = value;
			else if (attribute.equalsIgnoreCase(INDICATION))
				indication = value;
			else if (attribute.equalsIgnoreCase(YEAR))
				year = value;
			else if (attribute.equalsIgnoreCase(BEATLEVEL))
				beatLevel = value;
			else if (attribute.equalsIgnoreCase(TRACKLEVEL))
				trackLevel = value;
			else if (attribute.equalsIgnoreCase(STARTBAR))
				startBar = value;
			else if (attribute.equalsIgnoreCase(UPBEAT))
				upbeat = value;
			else if (attribute.equalsIgnoreCase(BEATSPERBAR))
				beatsPerBar = Integer.parseInt(value);
			else if (attribute.equalsIgnoreCase(AXIS))
				axis = value;
			else if (attribute.equalsIgnoreCase(TEMPOLATE))
				tempoLate = value;
			else
				System.err.println("Warning: Unrecognised header data: " +
									attribute + SEP + value);
			input = in.readLine();
			if (input != null)
				delimiter = input.indexOf(SEPCHAR);
			else
				break;
		}
		return input;
	} // read()

	public double getTrackLevel() {
		try {
            int i = trackLevel.indexOf("/");
			if (i >= 0)
				return Double.parseDouble(trackLevel.substring(0,i)) /
						Double.parseDouble(trackLevel.substring(i+1));
			else
				return Double.parseDouble(trackLevel);
		} catch (Exception e) {
			System.err.println("Error getting TrackLevel:\n" + e);
			return 1;
		}
	} // getTrackLevel()

} // WormParameters
