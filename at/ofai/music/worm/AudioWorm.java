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
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.net.URL;
import at.ofai.music.util.Format;
import at.ofai.music.util.EventList;

/** AudioWorm is the class that does the hard work.
  * The constructor initialises the audio objects which process the data.
  * Each call to nextBlock() reads in a new block of data and sends it to
  * the output device, as well as processing it and sending a new tempo
  * estimate to the Worm object. If the output buffer is not too low, the
  * the Worm object is asked to update its display and the audio processing
  * sleeps for 75ms to allow this to happen.
  **/
public class AudioWorm {

	Worm gui;				// The object that displays the data
	AudioInputStream in;	// Input stream (from WAV file or sound card)
	AudioInputStream orig = null;
	boolean isConverting = false;
	TargetDataLine targetDataLine;
	boolean isFileInput;
	AudioFormat inputFormat;
	SourceDataLine out;
	AudioFormat outputFormat;
	int outputBufferSize;
	int frameSize;
	double frameRate;
	int channels;
	int sampleSizeInBytes;
	static final float defaultSampleRate = 44100;
		// for kiefer's sound card:	  44101.0F instead of 44100
	static double windowTime = 0.010;
	static int averageCount = 10;
	static int fileDelay = 180;		// for liszt; 120 for bach; 70 for Schubert
	int windowSize;
	double normalise;
	byte[] inputBuffer;
	int bytesRead;
	int blockCount;
	TempoInducer ti;
	WormFile wormData;
	String audioFileName, audioFilePath;
	File audioFile;
	URL audioURL;
	long bytePosition;	// Number of bytes that have been read from input file
	long jumpPosition;	// Requested new bytePosition (or -1 for none)
	long fileLength;	// Length of input file in bytes
	
	public AudioWorm(Worm w) {
		gui = w;
		jumpPosition = -1;
		targetDataLine = null;
		// Input from audio file, with optional matchFile data
		String matchFile = w.getMatchFile();
		if ((matchFile != null) && !matchFile.equals("")) {
			try {
				EventList.setTimingCorrection(w.getTimingOffset());
				wormData = new WormFile(w, EventList.readMatchFile(matchFile));
				if (Math.abs(windowTime * averageCount -
								wormData.outFramePeriod) > 1e-5)
					throw new Exception("Incompatible parameters in AudioWorm");
				wormData.smooth(Worm.FULL_GAUSS, 1, 1, 0);
			} catch (Exception e) {
				e.printStackTrace();
				wormData = null;
			}
		} else {
			wormData = w.getWormFile();
		}
		ti = new TempoInducer(windowTime);
		audioFile = null;
		audioURL = null;
		audioFileName = w.getInputFile();
		audioFilePath = w.getInputPath();
		if (audioFileName == null)
			audioFileName = "";
		if (audioFilePath == null)
			audioFilePath = "";
		isFileInput = (!audioFileName.equals(""));
		if (!isFileInput) {
			initSoundCardInput(w);
			return;
		}
		if (audioFileName.startsWith("http:"))
			try {
				audioURL = new URL(audioFileName);
			} catch (java.net.MalformedURLException e) {
				e.printStackTrace();
			}
		else {
			audioFile = new File(audioFileName);
			if (!audioFile.isFile())
				audioFile = new File(audioFilePath + audioFileName);
			if (!audioFile.isFile()) // local hacks for UNC file names + Windows
				audioFile = new File("//fichte" + audioFileName);
			if (!audioFile.isFile())
				audioFile = new File("//fichte" + audioFilePath +audioFileName);
		}
		resetAudioFile();
		init(gui);
		if ((wormData != null) && (wormData.time[0] > 0.5))
			skipTo(wormData.time[0] - 0.5);
	} // AudioWorm constructor

	protected void resetAudioFile() {
		try {
			isConverting = false;
			orig = null;
			if (audioFile == null)
				in = AudioSystem.getAudioInputStream(audioURL);
			else {
				if (!audioFile.isFile())
					throw(new FileNotFoundException("No file: "+audioFileName));
				in = AudioSystem.getAudioInputStream(audioFile);
			}
			inputFormat = in.getFormat();
			if (inputFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
				AudioFormat desiredFormat = new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					inputFormat.getSampleRate(),
					16,
					inputFormat.getChannels(),
					inputFormat.getChannels() * 2,
					inputFormat.getSampleRate(),
					false);
		//		System.out.println("Old Format\n" + inputFormat + "\n\n" +
		//							"Desired Format\n" + desiredFormat);
				orig = in;
				in = AudioSystem.getAudioInputStream(desiredFormat, orig);
				inputFormat = in.getFormat();
		//		System.out.println("New Format\n" + inputFormat);
				isConverting = true;
			}
			fileLength = in.available();	// note this is 0 for mp3 files
		//	System.out.println("Bytes available: " + fileLength);
			bytePosition = 0;
			bytesRead = 0;
			blockCount = 0;
		} catch (IOException e) {	// includes FileNotFoundException
			e.printStackTrace();
			in = null;
		} catch (IllegalArgumentException e) {	// conversion not supported
			e.printStackTrace();
			in = null;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			in = null;
		}
	} // resetAudioFile()

	// Live input from microphone, line in, etc (selected by external mixer)
	protected void initSoundCardInput(Worm w) {
		//System.out.println("Entering initSoundCardInput()");
		if (in != null) {
			try {
				in.close();
				//System.out.println("in CLOSED");
			} catch (Exception e) {}
			in = null;
		}
		if (targetDataLine != null) {
			try {
				targetDataLine.close();
				//System.out.println("targetDataLine CLOSED");
			} catch (Exception e) {}
			targetDataLine = null;
		}
		inputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					  defaultSampleRate, 16, 2, 4, defaultSampleRate, false);
		Mixer.Info[] mInfo = AudioSystem.getMixerInfo();
		System.out.println("Number of mixers: " + mInfo.length);
		for (int i = 0; i < mInfo.length; i++) {
			System.out.println("Mixer info : " + mInfo[i]);
			Mixer t = AudioSystem.getMixer(mInfo[i]);
			Line.Info[] li = t.getTargetLineInfo(); // get input devices
			Class c;
			System.out.println("Number of target lines: " + li.length);
			for (int j = 0; j < li.length; j++) {   // find one that matches
				System.out.println("Line info: " + li[j]);
				c = li[j].getLineClass();
				AudioFormat[] af = ((DataLine.Info)li[j]).getFormats();
				for (int k = 0; k < af.length; k++) {
					// some sound cards support 44101 Hz but not 44100 Hz!
					double err = checkAudioFormats(af[k], inputFormat);
					if (err < 0.01) {	// match OK (allow up to 1% error)
						if (err >= 0)		// sample rate is within 1%
							inputFormat = af[k];
						// else device accepts any sample rate; use default
						DataLine.Info info = new DataLine.Info(c, inputFormat);
						try {
							System.out.println("Getting line with " + info);
							if (AudioSystem.getLine(info)
												instanceof TargetDataLine){
								targetDataLine = null;
								targetDataLine =
									(TargetDataLine)AudioSystem.getLine(info);
								System.out.println("Opening line ... ");
								targetDataLine.open(inputFormat); // , 16384);
								// buffer size request ignored
								//   default size: bach 65536; kiefer 16384
								// System.out.println("Buffer: " +
								// 	targetDataLine.getBufferSize());
								System.out.println("Creating AudioInputStream");
								in = new AudioInputStream(targetDataLine);
								init(w);
								return;
							}
						} catch (Exception e) {
							System.err.println("Unable to open input line");
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			}
		}
		throw new RuntimeException("No suitable input line found");
	} // initSoundCardInput()
	
	/** Normal initialisation of AudioWorm for reading from a PCM file
		or for reading compressed data which is uncompressed by AudioSystem.
	 **/
	protected void init(Worm w) {
		if (out != null)
			out.close();
		gui = w;
		gui.setDelay(isFileInput? fileDelay / averageCount: 0);
		gui.setFramePeriod(averageCount * windowTime);
		try {
			if (inputFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
				throw new UnsupportedAudioFileException("Not PCM_SIGNED but " + 
			//	System.out.println("Input format: " +
													inputFormat.getEncoding());
			frameSize = inputFormat.getFrameSize();
			frameRate = inputFormat.getFrameRate();
			channels = inputFormat.getChannels();
			sampleSizeInBytes = frameSize / channels;
			windowSize = (int)(windowTime * frameRate);
			normalise = (double) channels * windowSize *
						(1 << (inputFormat.getSampleSizeInBits() - 1));
			inputBuffer = new byte[windowSize * frameSize];
			bytePosition = 0;
			bytesRead = 0;
			blockCount = 0;
			if (!isFileInput)
				return;
			Mixer.Info[] mInfo = AudioSystem.getMixerInfo();
			for (int i = 0; i < mInfo.length; i++) {
				Mixer t = AudioSystem.getMixer(mInfo[i]);
				Line.Info[] li = t.getSourceLineInfo();	// get output devices
				Class c;
				for (int j = 0; j < li.length; j++) {	// find one that matches
					c = li[j].getLineClass();
					AudioFormat[] af = ((DataLine.Info)li[j]).getFormats();
					for (int k = 0; k < af.length; k++) {
						// some sound cards support 44101 Hz but not 44100 Hz!
						double err = checkAudioFormats(af[k], inputFormat);
						if (err < 0.01) {	// match OK (allow up to 1% error)
							if (err < 0)	// device accepts any sample rate
								outputFormat = inputFormat;
							else			// sample rate is within 1%
								outputFormat = af[k];
							outputBufferSize = (int)outputFormat.getFrameRate()
												* frameSize * 1;	// 1 sec bf
							DataLine.Info info = new DataLine.Info(c,
												outputFormat, outputBufferSize);
							if (AudioSystem.getLine(info) instanceof
									SourceDataLine) {
								out = (SourceDataLine)AudioSystem.getLine(info);
								out.open();
								return;	// accept the first one we find
							}
						}
					}
				}
			}
			throw new LineUnavailableException("Unable to find output device" +
												" matching:\n\t" + inputFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			System.exit(1);
		}
	} // init()

	public void start() { 
		//System.out.println("Start called");//DEBUG
		if (isFileInput) {
			gui.setDelay(fileDelay / averageCount);
			out.start();
		} else {
			//System.out.println("Flushing targetDataLine");//DEBUG
			targetDataLine.flush();
			//System.out.println("Restarting targetDataLine");//DEBUG
			targetDataLine.start();
		}
		//System.out.println("Start completed");//DEBUG
	} // start()

	public void pause() {
		//System.out.println("Pause called");//DEBUG
		if (isFileInput)
			out.stop();
		else
			targetDataLine.stop();
		//System.out.println("Pause completed");//DEBUG
	} // pause()

	public void stop() {
		//System.out.println("Stop called");//DEBUG
		if (isFileInput) {
			out.stop();
			out.flush();
		} else {
			targetDataLine.stop();
			//System.out.println("Flushing targetDataLine");//DEBUG
			targetDataLine.flush();
		}
		//System.out.println("Stop completed");//DEBUG
	} // stop()

	public boolean nextBlock() throws IOException {
		double rms = 0, tempo = 0;
		for (int i = 0; i < averageCount; i++) {	// 5 => 20 FPS
			int waitCount = 1;//D
			while ((in.available() < inputBuffer.length) && !isFileInput) {
				try {
					int before = in.available();
					//System.out.print("WAIT: "+(waitCount++)+" "+before);//DEBG
					Thread.sleep((int)(1000.0 * windowTime));
					int after = in.available();
					//System.out.println(" " + after +
					//					";   input line active/running: " +
					//					targetDataLine.isActive() + "/" +
					//					targetDataLine.isRunning());
					if ((waitCount > 5) && (before == after)) {
						// bytesRead = in.read(inputBuffer); // HANGS HERE
					//	System.out.println("Read(): " + bytesRead +
					//						" " + in.available());
						break;
					}
					if ((waitCount > 3) && (!targetDataLine.isActive() ||
											!targetDataLine.isRunning()))
						return false;
				} catch (InterruptedException e) {}
			}
			long avail = (isConverting? orig.available(): in.available());
			// This isn't really correct for uncompressing, since the number
			//  of bytes will be different after uncompression, but it is a
			//  hack to get around the fact that in.available() returns 0.
			if (avail >= inputBuffer.length) {
				double tmp = processWindow();
				if (bytesRead < 0) {
					System.err.println("nextBlock(): Audio read error");
					return false;
				}
				rms += tmp * tmp;
				if (wormData == null)
					tempo = ti.getTempo(tmp); // rms amp or dB?
				// tempo = ti.getTempo(120 + 20 / Math.log(10) * Math.log(tmp));
				if (isFileInput) {
					if (ti.onset)	// mark detected onsets with a click
						for (int j = 0; j < 882; ) {
							inputBuffer[j++] = (byte)(100.0 *
												Math.sin(2.0*Math.PI*j/441.0));
							inputBuffer[j++] = 0;
						}
					int chk = out.write(inputBuffer,0,bytesRead);
				//	if (chk != bytesRead) {	// shouldn't happen; write() blocks
				//		System.err.println("Problem writing " + bytesRead +
				//						   " bytes.  Only " + chk +" written.");
				//	}
				}
				blockCount++;
			} else {
				System.err.println("nextBlock(): Audio data not available");
				return false;
			}
		}
		double dB = Math.max(0, 120 + 20 / Math.log(10) *
								Math.log(Math.sqrt(rms / averageCount)));
		int index = (blockCount - 1) / averageCount;
		if (wormData != null) {
			gui.scrollBar.setValueNoFeedback( // don't want to call skipAudio()
							(int)(1000 * bytePosition / fileLength));
			if (index >= wormData.outTempo.length) {
			//	System.err.println("DEBUG: end of wormfile");
			//	System.err.println("DEBUG: " + blockCount +
			//					   " " + index +
			//					   " " + wormData.outIntensity[index]);
				return false;
			}
			gui.addPoint(wormData.outTempo[index], wormData.outIntensity[index],
							wormData.label[index]);
		} else {
			gui.addPoint(60 / tempo, dB, Format.d((blockCount-1)*windowTime,1));
			// System.out.println(Format.d(tempo,3) + " " + Format.d(dB, 3));
		}
		if (!isFileInput) {
			gui.repaint();
			return true;
		}
		int space = out.available();
		double buffContents = (double)(outputBufferSize - space) *
								windowTime / inputBuffer.length;
		// System.out.println("dB = " + Format.d(dB) +
		// 				"  In buffer = " + Format.d(buffContents) +
		// 				" sec  Space left = " + space +
		// 				" bytes  Size = " + outputBufferSize);
		if (buffContents > 0.1) {				// shouldn't starve?
			gui.repaint();
			try {					// Give it some time to paint.
				Thread.sleep(75);
			} catch (InterruptedException e) {}
			// According to System.currentTimeMillis(), paint() takes ~16ms,
			// but it updates much smoother (i.e. calls paint() more frequently)
			// when the value is higher. Not sure how to convince it otherwise.
		}
		return true;
	} // nextBlock()
	
	/** Reads a block of audio data, summing the channels and returning the
	 *   normalised (in the range 0.0-1.0) RMS average of the sample values.
	 *   Assumes sufficient data is queued, or else blocks while it waits
	 *   for the buffer to fill sufficiently.
	 **/
	protected double processWindow() throws IOException {
		if (jumpPosition >= 0)
			skipAudio();
		bytesRead = in.read(inputBuffer);
		bytePosition += bytesRead;
		// System.out.println("read(): " + bytePosition);//DEBUG
		if (wormData != null)		// only need to play audio; no calculation
			return 0;
		long sample;
		double sum = 0;
		if (sampleSizeInBytes == 1) {		// 8 bit samples
			if (channels == 1) {
				for (int i = 0; i < bytesRead; i += frameSize) {
					sample = ((int)inputBuffer[i]);
					sum += (double)(sample * sample);
				}
			} else if (channels == 2) {
				for (int i = 0; i < bytesRead; i += frameSize) {
					sample = ((int)inputBuffer[i]) +
							 ((int)inputBuffer[i+1]);
					sum += (double)(sample * sample);
				}
			} else {
				for (int i = 0; i < bytesRead; ) {
					sample = 0;
					for (int c = 0; c < channels; c++, i++)
						sample += ((int)inputBuffer[i]);
					sum += (double)(sample * sample);
				}
			}
		} else if (sampleSizeInBytes == 2) {	// 16 bit samples
			if (inputFormat.isBigEndian()) {
				if (channels == 1) {
					for (int i = 0; i < bytesRead; i += frameSize) {
						sample = ((int)inputBuffer[i] << 8) |
								 ((int)inputBuffer[i+1] & 0xFF);
						sum += (double)(sample * sample);
					}
				} else if (channels == 2) {
					for (int i = 0; i < bytesRead; i += frameSize) {
						sample = (((int)inputBuffer[i] << 8) |
								 ((int)inputBuffer[i+1] & 0xFF)) +
								 (((int)inputBuffer[i+2] << 8) |
								 ((int)inputBuffer[i+3] & 0xFF));
						sum += (double)(sample * sample);
					}
				} else {
					for (int i = 0; i < bytesRead; ) {
						sample = 0;
						for (int c = 0; c < channels; c++) {
							sample += ((int)inputBuffer[i] << 8) |
									  ((int)inputBuffer[i+1] & 0xFF);
							i += 2;
						}
						sum += (double)(sample * sample);
					}
				}
			} else {	// little-endian
				if (channels == 1) {
					for (int i = 0; i < bytesRead; i += frameSize) {
						sample = ((int)inputBuffer[i+1] << 8) |
								 ((int)inputBuffer[i] & 0xFF);
						sum += (double)(sample * sample);
					}
				} else if (channels == 2) {
					for (int i = 0; i < bytesRead; i += frameSize) {
						sample = (((int)inputBuffer[i+1] << 8) |
								 ((int)inputBuffer[i] & 0xFF)) +
								 (((int)inputBuffer[i+3] << 8) |
								 ((int)inputBuffer[i+2] & 0xFF));
						sum += (double)(sample * sample);
					}
				} else {
					for (int i = 0; i < bytesRead; ) {
						sample = 0;
						for (int c = 0; c < channels; c++) {
							sample += ((int)inputBuffer[i+1] << 8) |
									  ((int)inputBuffer[i] & 0xFF);
							i += 2;
						}
						sum += (double)(sample * sample);
					}
				}
			}
		} else {	// not 8-bit or 16-bit samples
			for (int i = 0; i < bytesRead; ) {
				long longSample = 0;
				for (int c = 0; c < channels; c++) {
					if (inputFormat.isBigEndian()) {
						sample = (int)inputBuffer[i++];
						for (int b = 1; b < sampleSizeInBytes; b++, i++)
							sample = (sample<<8) | ((int)inputBuffer[i] & 0xFF);
					} else {
						sample = 0;
						int b;
						for (b = 0; b < sampleSizeInBytes-1; b++, i++)
							sample |= ((int)inputBuffer[i] & 0xFF) << (b * 8);
						sample |= ((int)inputBuffer[i++]) << (b * 8);
					}
					longSample += sample;
				}
				sum += (double)longSample * (double)longSample;
			}
		}
		return Math.sqrt(sum) / normalise;
	} // processWindow()

	/** Returns the relative difference  |a-b| / ((a+b)/2)  in sample rates,
	 *  or 2.0 if the formats do not match in some other characteristic,
	 *  or -1 if AudioFormat `out' allows all sampling rates;
	 *  normal return value is in [0,2).
	 **/
	public static double checkAudioFormats(AudioFormat out, AudioFormat in) {
		if ((out.getChannels() != in.getChannels()) ||
				(out.getSampleSizeInBits() != in.getSampleSizeInBits()) ||
				(out.getEncoding() != in.getEncoding()) ||
				(out.isBigEndian() != in.isBigEndian()) ||
				(out.getSampleRate() == 0.0F))
			return 2.0;
		if (out.getSampleRate() < 0)
			return -1.0;
		return Math.abs(2.0 * (out.getSampleRate() - in.getSampleRate()) /
							  (out.getSampleRate() + in.getSampleRate()));
	} // checkAudioFormats()

	/** Returns the RMS average of an array of double.
	 **/
	public static double rms(double[] data) {
		double sum = 0;
		for (int i = 0; i < data.length; i++)
			sum += data[i] * data[i];
		return Math.sqrt(sum / (double)data.length);
	} // rms()

	/** Reposition the audio file input to a new point in seconds.
	 **/
	public void skipTo(double time) {
		jumpPosition = Math.round(time * frameRate) * frameSize;
		if (jumpPosition > fileLength)
			jumpPosition = fileLength;
	} // skipTo()

	/** Reposition the audio file input (from Scrollbar) on a scale of
	 *  0 (beginning of file) to 1000 (end of file).
	 **/
	public void skipTo(int thousandths) {
		jumpPosition = fileLength / frameSize * thousandths / 1000 * frameSize;
	} // skipTo()

	/** For input from an audio file, performs repositioning within the file,
	 *  after a call to the public method skipTo().
	 **/
	protected void skipAudio() {
		// gui.pause();
		long toSkip = jumpPosition;
		long hasSkipped = 0;
		if (jumpPosition >= bytePosition)			// fast forward
			toSkip -= bytePosition;
		else if (jumpPosition < bytePosition)		// rewind
			resetAudioFile();
		try {
			while (toSkip > hasSkipped) {	// due to buffering, skip() must be
				//  called multiple times before it reaches the requested spot
				//  (see JavaSound mailing list Item #4658 (5 Nov 2000 20:58))
				long skipped = in.skip(toSkip - hasSkipped);
				if (skipped <= 0)
					throw new IOException("skip() error: "+skipped+" returned");
				hasSkipped += skipped;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		bytePosition += hasSkipped;
		if (out != null)
			out.flush();
		int currentPoint = (blockCount - 1) / averageCount;
		blockCount = (int) (bytePosition / (long)inputBuffer.length);
		if (wormData != null) {
			int stop = Math.min(wormData.outTempo.length, 
								(blockCount - 1) / averageCount);
			int start = Math.max(stop - WormConstants.wormLength, 0);
			if (currentPoint < stop)
				start = Math.max(start, currentPoint);
			else
				gui.clear();
			for (int index = start; index < stop; index++)
				gui.addPoint(wormData.outTempo[index],
						wormData.outIntensity[index], wormData.label[index]);
			gui.repaint();
		}
		jumpPosition = -1;
	} // skipAudio()

} // class AudioWorm
