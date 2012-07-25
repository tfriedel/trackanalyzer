/*************************************************************************

  Copyright 2012 Thomas Friedel

  This file is part of TrackAnalyzer.

  TrackAnalyzer is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  TrackAnalyzer is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with TrackAnalyzer.  If not, see <http://www.gnu.org/licenses/>.

*************************************************************************/

package TrackAnalyzer;

import at.ofai.music.beatroot.BeatRoot;
import it.sauronsoftware.jave.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;

public class TrackAnalyzer {

	public static void decodeAudioFile(File input, File wavoutput) throws IllegalArgumentException, InputFormatException, EncoderException {
		assert wavoutput.getName().endsWith(".wav");
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("pcm_s16le");
		audio.setChannels(Integer.valueOf(1));
		audio.setSamplingRate(new Integer(4410));
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("wav");
		attrs.setAudioAttributes(audio);
		Encoder encoder = new Encoder();
		encoder.encode(input, wavoutput, attrs);
	}

	public static void main(String[] args) throws Exception {
		boolean needsDownsampling = true;
		boolean writeTags = true;
		String wavfilename;
		ArrayList<String> filenames = new ArrayList<String>();
		if (args.length < 1) {
			System.out.println("usage: TrackAnalyzer inputfile.mp3");
			System.out.println("    or TrackAnalyzer -l filelist.txt");
		} else {
			// we have a list, read all the filenames in the list and 
			// collect them in 'filenames'
			if (args[0].equals("-l")) {
				assert (args.length > 1);
				try {
					//use buffering, reading one line at a time
					//FileReader always assumes default encoding is OK!
					BufferedReader input = new BufferedReader(new FileReader(new File(args[1])));
					try {
						String line = null; //not declared within while loop
        				/*
						 * readLine is a bit quirky :
						 * it returns the content of a line MINUS the newline.
						 * it returns null only for the END of the stream.
						 * it returns an empty String if two newlines appear in a row.
						 */
						while ((line = input.readLine()) != null) {
							filenames.add(line);
						}
					} finally {
						input.close();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(-1);
				}
			} else {
				// we don't have a list, but a single audiofile. append it to
				// the empty list
				filenames.add(args[0]);
			}
			
			for (String filename : filenames) {
				AudioData data = new AudioData();
				File temp = null;
				if (!filename.endsWith(".wav")) {
					temp = File.createTempFile("keyfinder", ".wav");
					wavfilename = temp.getAbsolutePath();
					// Delete temp file when program exits.
					temp.deleteOnExit();
					decodeAudioFile(new File(filename), temp);
					needsDownsampling = false;
				} else {
					wavfilename = filename;
					needsDownsampling = true;
				}

				try {
					data.loadFromAudioFile(wavfilename);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (needsDownsampling) {
					PrimaryDownsampler downsampler = new PrimaryDownsampler();
					try {
						data.reduceToMono();
						data = downsampler.downsample(data, 10);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//data.writeWavFile("C:\\temp\\output.wav");
				KeyFinder k = new KeyFinder();
				Parameters p = new Parameters();
				p.setHopSize(8192);
				KeyDetectionResult r = k.findKey(data, p);
				System.out.println("filename: " + filename);
				System.out.println("key: " + camelotKey(r.globalKeyEstimate));

				// get bpm
				double bpm = BeatRoot.getBPM(wavfilename);
				String formattedBpm = new DecimalFormat("#.#").format(bpm).replaceAll(",", ".");
				System.out.printf("BPM: %s\n", formattedBpm);

				if (writeTags) {
					File file = new File(filename);
					try {
						AudioFile f = AudioFileIO.read(file);
						if (filename.endsWith(".mp3")) {
							if (!setCustomTag(f, "KEY_START", camelotKey(r.globalKeyEstimate))) {
								throw new IOException("Error reading Tags");
							}
						}
						Tag tag = f.getTag();
						tag.setField(FieldKey.BPM, formattedBpm);
						f.commit();
					} catch (IOException e) {
						System.out.println("problem with tags in file " + filename);
					}
					if (temp != null) {
						temp.delete();
					}
				}
			}
			System.exit(0);

		}
	}

	/**
	 * This will write a custom ID3 tag (TXXX). This works only with MP3 files
	 * (Flac with ID3-Tag not tested).
	 *
	 * @param description The description of the custom tag i.e. "catalognr"
	 * There can only be one custom TXXX tag with that description in one MP3
	 * file
	 * @param text The actual text to be written into the new tag field
	 * @return True if the tag has been properly written, false otherwise
	 */
	public static boolean setCustomTag(AudioFile audioFile, String description, String text) throws IOException {
		FrameBodyTXXX txxxBody = new FrameBodyTXXX();
		txxxBody.setDescription(description);
		txxxBody.setText(text);

		// Get the tag from the audio file
		// If there is no ID3Tag create an ID3v2.3 tag
		Tag tag = audioFile.getTagOrCreateAndSetDefault();
		// If there is only a ID3v1 tag, copy data into new ID3v2.3 tag
		if (!(tag instanceof ID3v23Tag || tag instanceof ID3v24Tag)) {
			Tag newTagV23 = null;
			if (tag instanceof ID3v1Tag) {
				newTagV23 = new ID3v23Tag((ID3v1Tag) audioFile.getTag()); // Copy old tag data               
			}
			if (tag instanceof ID3v22Tag) {
				newTagV23 = new ID3v23Tag((ID3v11Tag) audioFile.getTag()); // Copy old tag data              
			}
			audioFile.setTag(newTagV23);
		}

		AbstractID3v2Frame frame = null;
		if (tag instanceof ID3v23Tag) {
			if (((ID3v23Tag) audioFile.getTag()).getInvalidFrames() > 0) {
				throw new IOException("read some invalid frames!");
			}
			frame = new ID3v23Frame("TXXX");
		} else if (tag instanceof ID3v24Tag) {
			if (((ID3v24Tag) audioFile.getTag()).getInvalidFrames() > 0) {
				throw new IOException("read some invalid frames!");
			}
			frame = new ID3v24Frame("TXXX");
		}

		frame.setBody(txxxBody);

		try {
			tag.setField(frame);
		} catch (FieldDataInvalidException e) {
			e.printStackTrace();
			return false;
		}

		try {
			audioFile.commit();
		} catch (CannotWriteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String camelotKey(Parameters.key_t key) {
		return Parameters.camelot_key[key.ordinal()];
	}
}
