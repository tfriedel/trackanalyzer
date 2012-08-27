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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
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
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class TrackAnalyzer {

	CommandLineArgs c = new CommandLineArgs();
	BufferedWriter writeListWriter;
	ArrayList<String> filenames = new ArrayList<String>();
	public final KeyFinder k;
	public final Parameters p;

	TrackAnalyzer(String[] args) throws Exception {

		JCommander jcommander = new JCommander(c, args);
		jcommander.setProgramName("TrackAnalyzer");
		if ((c.filenames.size() == 0 && Utils.isEmpty(c.filelist)) || c.help) {
			jcommander.usage();
			System.exit(-1);
		}
		if (c.debug) {
			Logger.getLogger(TrackAnalyzer.class.getName()).setLevel(Level.ALL);
		} else {
			Logger.getLogger(TrackAnalyzer.class.getName()).setLevel(Level.WARNING);
		}
		// we have a list, read all the filenames in the list and 
		// collect them in 'filenames'
		if (!Utils.isEmpty(c.filelist)) {
			try {
				//use buffering, reading one line at a time
				//FileReader always assumes default encoding is OK!
				BufferedReader input = new BufferedReader(new FileReader(new File(c.filelist)));
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
		}
		// add filenames from command line
		filenames.addAll(c.filenames);

		if (!Utils.isEmpty(c.writeList)) {
			try {
				writeListWriter = new BufferedWriter(new FileWriter(c.writeList));
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
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
		if (!Utils.isEmpty(c.writeList)) {
			try {
				writeListWriter.write(filename + ";" + key + ";" + bpm + ";" + wroteTags);
				writeListWriter.newLine();
				writeListWriter.flush();
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * one worker thread for analyzing a track
	 */
	private final class WorkTrack implements Callable<Boolean> {

		String filename;

		WorkTrack(String filename) {
			this.filename = filename;
		}

		@Override
		public Boolean call() {
			return analyzeTrack(filename, c.writeTags);
		}
	}

	/**
	 * writes bpm and key to KEY_START and BPM fields in the tag
	 *
	 * @param filename
	 * @param formattedBpm
	 * @param key
	 */
	public boolean updateTags(String filename, String formattedBpm, String key) {
		File file = new File(filename);
		try {
			AudioFile f = AudioFileIO.read(file);
			if (!setCustomTag(f, "KEY_START", key)) {
				throw new IOException("Error writing Key Tag");
			}
			if (!c.noBpm) {
				Tag tag = f.getTag();
				if (tag instanceof Mp4Tag) {
					if (!setCustomTag(f, "BPM", formattedBpm)) {
						throw new IOException("Error writing BPM Tag");
					}
				}
				tag.setField(FieldKey.BPM, formattedBpm);
			}
			f.commit();
			return true;
		} catch (Exception e) {
			System.out.println("problem with tags in file " + filename);
			return false;
		}
	}

	/**
	 * runs key and bpm detector on
	 *
	 * @filename, optionally writes tags
	 * @param filename
	 * @return
	 */
	public boolean analyzeTrack(String filename, boolean writeTags) {
		String wavfilename = "";
		AudioData data = new AudioData();
		File temp = null;
		File temp2 = null;
		try {
			temp = File.createTempFile("keyfinder", ".wav");
			temp2 = File.createTempFile("keyfinder2", ".wav");
			wavfilename = temp.getAbsolutePath();
			// Delete temp file when program exits.
			temp.deleteOnExit();
			temp2.deleteOnExit();
			decodeAudioFile(new File(filename), temp, 44100);
			decodeAudioFile(temp, temp2);
		} catch (Exception ex) {
			Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "error while decoding" + filename + ".");
			if (temp.length() == 0) {
				logDetectionResult(filename, "-", "-", false);
				temp.delete();
				temp2.delete();
				return false;
			}
		}

		KeyDetectionResult r;
		try {
			data.loadFromAudioFile(temp2.getAbsolutePath());
			r = k.findKey(data, p);
			if (r.globalKeyEstimate == Parameters.key_t.SILENCE) {
				System.out.println("SILENCE");
			}
		} catch (Exception ex) {
			Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			logDetectionResult(filename, "-", "-", false);
			deleteTempFiles(temp, temp2);
			return false;
		}

		String formattedBpm = "0";
		if (!c.noBpm) {
			// get bpm
			if (c.hiQuality) {
				try {
					//decodeAudioFile(new File(filename), temp, 44100);
					//@todo hiquality stuff
				} catch (Exception ex) {
					Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "couldn't decode " + filename + " for hiquality bpm detection.", ex);
				}
			}
			double bpm = BeatRoot.getBPM(wavfilename);
			if (Double.isNaN(bpm) && !c.hiQuality) {
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
			} else if (Double.isNaN(bpm) && c.hiQuality) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "bpm couldn't be detected for " + filename + ".");
			}
			if (!Double.isNaN(bpm)) {
				formattedBpm = new DecimalFormat("#.#").format(bpm).replaceAll(",", ".");
			}
		}
		System.out.printf("%s key: %s BPM: %s\n", filename, Parameters.camelotKey(r.globalKeyEstimate), formattedBpm);

		boolean wroteTags = false;
		if (c.writeTags) {
			wroteTags = updateTags(filename, formattedBpm, Parameters.camelotKey(r.globalKeyEstimate));
		}
		logDetectionResult(filename, Parameters.camelotKey(r.globalKeyEstimate), formattedBpm, wroteTags);
		deleteTempFiles(temp, temp2);
		return true;
	}

	private void deleteTempFiles(File temp, File temp2) {
		if (temp != null) {
			temp.delete();
		}
		if (temp2 != null) {
			temp2.delete();
		}
	}
	/**
	 * This is the main loop of the program. For every file in the filenames
	 * list, the file gets decoded and downsampled to a 4410 hz mono wav file.
	 * Then key and bpm detectors are run, the result is logged in a txt file
	 * and written to the tag if possible.
	 */
	public void run() throws ExecutionException {
		int nThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		CompletionService<Boolean> pool;
		pool = new ExecutorCompletionService<Boolean>(threadPool);

		nextFile:
		for (String filename : filenames) {
			//new worker thread
			pool.submit(new WorkTrack(filename));
		}
		for (int i = 0; i < filenames.size(); i++) {
			Boolean result;
			//Compute the result
			try {
				result = pool.take().get();
			} catch (InterruptedException e) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, e);
			}
		}
		threadPool.shutdown();
		if (!Utils.isEmpty(c.writeList)) {
			try {
				writeListWriter.close();
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
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
				tag = newTagV23;
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
			//TagField field = new Mp4TagTextField("----:com.apple.iTunes:"+description, text);
			TagField field;
			field = new Mp4TagReverseDnsField(Mp4TagReverseDnsField.IDENTIFIER
					+ ":" + "com.apple.iTunes" + ":" + description,
					"com.apple.iTunes", description, text);
			//TagField field = new Mp4TagTextField(description, text);
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
			Logger.getLogger(TrackAnalyzer.class.getName()).log(Level.WARNING, "couldn't write key information for " + audioFile.getFile().getName() + " to tag, because this format is not supported.");
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
}
