/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KeyFinder;

import it.sauronsoftware.jave.*;
import java.io.File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;

/**
 *
 * @author Thomas
 */
public class TrackAnalyzer {
	public static void decodeAudioFile(File input, File wavoutput) throws IllegalArgumentException, InputFormatException, EncoderException {
		assert wavoutput.getName().endsWith(".wav");
		AudioAttributes audio = new AudioAttributes();
		audio.setCodec("pcm_s16le");
		audio.setChannels(new Integer(1));
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
		if (args.length < 1) {
			System.out.println("usage: KeyFinder inputfile.wav");
		} else {
			String filename = args[0];
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
			System.out.println(camelotKey(r.globalKeyEstimate));
			if (writeTags) {
				if (filename.endsWith(".mp3")) {
					AudioFile f = AudioFileIO.read(new File(filename));
					setCustomTag(f, "KEY_START", camelotKey(r.globalKeyEstimate));
				}
				if (temp != null) {
					temp.delete();
				}
			}
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
	public static boolean setCustomTag(AudioFile audioFile, String description, String text) {
		FrameBodyTXXX txxxBody = new FrameBodyTXXX();
		txxxBody.setDescription(description);
		txxxBody.setText(text);

		// Get the tag from the audio file
		// If there is no ID3Tag create an ID3v2.3 tag
		Tag tag = audioFile.getTagOrCreateAndSetDefault();
		if (tag.hasField("TXXX")) {
			System.out.println("has TXXX");
		}

		if (tag.hasField("KEY_STARTT")) {
			System.out.println("has KEY_STARTT");
		}
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
			frame = new ID3v23Frame("TXXX");
		} else if (tag instanceof ID3v24Tag) {
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
