/**
 * ***********************************************************************
 *
 * Copyright 2012 Thomas Friedel
 *
 * This file is part of TrackAnalyzer.
 *
 * TrackAnalyzer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * TrackAnalyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * TrackAnalyzer. If not, see <http://www.gnu.org/licenses/>.
 *
 ************************************************************************
 */
package TrackAnalyzer;

import at.ofai.music.beatroot.BeatRoot;
import it.sauronsoftware.jave.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

public class TrackAnalyzer {

	boolean logging;
	boolean writeTags;
	BufferedWriter logger;
	ArrayList<String> filenames = new ArrayList<String>();
	public final KeyFinder k;
	public final Parameters p;

	TrackAnalyzer(String[] args) throws Exception {
		logging = true;
		writeTags = true;
		if (args.length < 1) {
			System.out.println("usage: TrackAnalyzer inputfile.mp3");
			System.out.println("    or TrackAnalyzer -l filelist.txt");
			System.exit(-1);
		}
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

		try {
			logger = new BufferedWriter(new FileWriter("c:\\temp\\TrackAnalyezerLog.txt"));
		} catch (IOException ex) {
			Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
		}
		k = new KeyFinder();
		p = new Parameters();
		p.setHopSize(8192);
	}

	/**
	 * Decodes an audio file (mp3, flac, wav, etc. everything which can be
	 * decoded by ffmpeg) to a downsampled wav file.
	 *
	 * @param input an audio file which will be decoded to wav
	 * @param wavoutput the output wav file
	 * @throws IllegalArgumentException
	 * @throws InputFormatException
	 * @throws EncoderException
	 */
	public static void decodeAudioFile(File input, File wavoutput) throws IllegalArgumentException, InputFormatException, EncoderException {
		decodeAudioFile(input, wavoutput, 4410);
	}

	/**
	 * Decodes an audio file (mp3, flac, wav, etc. everything which can be
	 * decoded by ffmpeg) to a downsampled wav file.
	 *
	 * @param input an audio file which will be decoded to wav
	 * @param wavoutput the output wav file
	 * @param samplerate the samplerate of the output wav.
	 * @throws IllegalArgumentException
	 * @throws InputFormatException
	 * @throws EncoderException
	 */
	public static void decodeAudioFile(File input, File wavoutput, int samplerate) throws IllegalArgumentException, InputFormatException, EncoderException {
		assert wavoutput.getName().endsWith(".wav");
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("pcm_s16le");
		audio.setChannels(Integer.valueOf(1));
		audio.setSamplingRate(new Integer(samplerate));
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("wav");
		attrs.setAudioAttributes(audio);
		Encoder encoder = new Encoder();
		encoder.encode(input, wavoutput, attrs);

	}

	/**
	 * this writes a line to a txt file with the result of the detection process
	 * for one file.
	 *
	 * @param filename the filename of the audio file we just processed
	 * @param key the result of the key detector (or "-")
	 * @param bpm the result of the bpm detector (or "-")
	 * @param wroteTags true if tags were written successfully
	 */
	public void logDetectionResult(String filename, String key, String bpm, boolean wroteTags) {
		if (logging) {
			try {
				logger.write(filename + ";" + key + ";" + bpm + ";" + wroteTags);
				logger.newLine();
				logger.flush();
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * This is the main loop of the program. For every file in the filenames
	 * list, the file gets decoded and downsampled to a 4410 hz mono wav file.
	 * Then key and bpm detectors are run, the result is logged in a txt file
	 * and written to the tag if possible.
	 */
	public void run() {
		boolean needsDownsampling = true;
		String wavfilename;
		int nThreads = 2;
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		
		nextFile:
		for (String filename : filenames) {
			AudioData data = new AudioData();
			File temp = null;
			try {
				temp = File.createTempFile("keyfinder", ".wav");
				wavfilename = temp.getAbsolutePath();
				// Delete temp file when program exits.
				temp.deleteOnExit();
				decodeAudioFile(new File(filename), temp);
			} catch (Exception ex) {
				logDetectionResult(filename, "-", "-", false);
				continue nextFile;
			}
			needsDownsampling = false;

			try {
				data.loadFromAudioFile(wavfilename);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 if (needsDownsampling) {
			 PrimaryDownsampler downsampler = new PrimaryDownsampler();
			 try {
			 data.reduceToMono();
			 data = downsampler.downsample(data, 10);
			 } catch (Exception e) {
			 e.printStackTrace();
			 }
			 }
			 */
			//data.writeWavFile("C:\\temp\\output.wav");

			KeyDetectionResult r;
			try {
				r = k.findKey(data, p);
			} catch (Exception ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				logDetectionResult(filename, "-", "-", false);
				continue nextFile;
			}
			System.out.println("filename: " + filename);
			System.out.println("key: " + camelotKey(r.globalKeyEstimate));

			// get bpm
			double bpm = BeatRoot.getBPM(wavfilename);
			if (Double.isNaN(bpm)) {
				try {
					// bpm couldn't be detected. try again with a higher quality wav.
					Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "bpm couldn't be detected for " + filename + ". Trying again.");
					decodeAudioFile(new File(filename), temp, 44100);
					bpm = BeatRoot.getBPM(wavfilename);
					if (Double.isNaN(bpm)) {
						Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "bpm still couldn't be detected for " + filename + ".");
					} else {
						Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.INFO, "bpm now detected correctly for " + filename);
					}
				} catch (Exception ex) {
					logDetectionResult(filename, "-", "-", false);
				}
			}
			String formattedBpm = "0";
			if (!Double.isNaN(bpm))
				formattedBpm = new DecimalFormat("#.#").format(bpm).replaceAll(",", ".");
			System.out.printf("BPM: %s\n", formattedBpm);

			if (writeTags) {
				File file = new File(filename);
				try {
					AudioFile f = AudioFileIO.read(file);
					if (!setCustomTag(f, "KEY_START", camelotKey(r.globalKeyEstimate))) {
						throw new IOException("Error reading Tags");
					}
					Tag tag = f.getTag();
					tag.setField(FieldKey.BPM, formattedBpm);
					f.commit();
					logDetectionResult(filename, camelotKey(r.globalKeyEstimate), formattedBpm, true);
				} catch (Exception e) {
					System.out.println("problem with tags in file " + filename);
					logDetectionResult(filename, camelotKey(r.globalKeyEstimate), formattedBpm, false);
				}
			}
			if (temp != null) {
				temp.delete();
			}
		}
		try {
			logger.close();
		} catch (IOException ex) {
			Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		Logger.getLogger(TrackAnalyzer.class.getName()).setLevel(Level.ALL);
		TrackAnalyzer ta = new TrackAnalyzer(args);
		ta.run();

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
		if (tag instanceof AbstractID3Tag) {
			// If there is only a ID3v1 tag, copy data into new ID3v2.3 tag
			if (!(tag instanceof ID3v23Tag || tag instanceof ID3v24Tag)) {
				Tag newTagV23 = null;
				if (tag instanceof ID3v1Tag) {
					newTagV23 = new ID3v23Tag((ID3v1Tag) audioFile.getTag()); // Copy old tag data               
				}
				if (tag instanceof ID3v22Tag) {
					newTagV23 = new ID3v23Tag((ID3v22Tag) audioFile.getTag()); // Copy old tag data              
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
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, e);
				return false;
			}
		} else if (tag instanceof FlacTag) {
			try {
				((FlacTag) tag).setField(description, text);
			} catch (KeyNotFoundException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FieldDataInvalidException ex) {
				return false;
			}
		} else if (tag instanceof Mp4Tag) {
			TagField field = new Mp4TagTextField(description, text);
			try {
				tag.setField(field);
			} catch (FieldDataInvalidException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			}
		} else if (tag instanceof VorbisCommentTag) {
			try {
				((VorbisCommentTag) tag).setField(description, text);
			} catch (KeyNotFoundException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FieldDataInvalidException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			}
		} else {
			// tag not implented
			Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "couldn't write key information for {0} to tag, because this format is not supported.", audioFile.getFile().getName());
			return false;
		}
		
		// write changes in tag to file
		try {
			audioFile.commit();
		} catch (CannotWriteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return a camelot key string representation of the key
	 */
	public static String camelotKey(Parameters.key_t key) {
		return Parameters.camelot_key[key.ordinal()];
	}
}
