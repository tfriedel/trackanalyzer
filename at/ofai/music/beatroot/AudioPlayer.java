/*  BeatRoot: An interactive beat tracking system
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
	http://www.gnu.org/licenses/gpl.txt or write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package at.ofai.music.beatroot;

import javax.sound.sampled.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import at.ofai.music.audio.WavWrite;
import at.ofai.music.util.Event;
import at.ofai.music.util.EventList;

public class AudioPlayer implements Runnable {

	/** The file chooser dialog window for opening and saving files */
	protected JFileChooser jfc;
	
	/** BeatRoot's graphical user interface */
	protected GUI gui;
	
	/** The file currently loaded by this AudioPlayer */
	protected AudioFile currentFile;
	
	/** A new file that has been loaded to play next */
	protected AudioFile requestedFile;
	
	/** The position (in bytes) of playback, relative to the beginning of the file */
	protected long currentPosition;
	
	/** The requested playback position, in bytes, relative to the beginning of the file */
	protected long requestedPosition;
	
	/** The byte position in the current file to stop playing */
	protected long endPosition;
	
	/** The time that the audio playback last started, in nanoseconds */
	protected long startNanoTime;
	
	/** The time in the playback file where the audio playback last started */
	protected double startTime;
	
	/** A flag indicating to the play thread that playing should stop */
	protected boolean stopRequested;
	
	/** A flag set by the play thread indicating whether audio playback is active */ 
	protected boolean playing;
	
	/** The object to which audio output is written */
	protected SourceDataLine audioOut;
	
	/** The buffer size used by audioOut */
	protected int outputBufferSize;
	
	/** Audio input buffer for the current input file */
	protected byte[] readBuffer;
	
	/** A second audio input buffer used when crossfading between two audio files */
	protected byte[] readBuffer2;
	
	/** The positions in samples of beats for playback of audio with click marking beat times */
	protected long[] beats;
	
	/** The metrical level of each beat (see beats[]) */
	protected int[] level;
	
	/** The index in beats[] of the next beat (relative to the current playback position */
	protected int beatIndex;
	
	/** An array of percussion sounds */
	protected int[][] click;
	
	/** The size of the buffer for reading audio input */
	protected static final int readBufferSize = 2048;				// 46ms@44.1kHz
	
	/** The default buffer size for audio output */
	protected static final int defaultOutputBufferSize = 32768;		// was 16384;
	
	/** The relative volume of percussion (for the click track) relative to the input audio */
	protected static double volume = 1.0;
	
	/** Flag for debugging output */
	protected boolean debug = false;
	
	/** Flag indicating the current mode of playing, whether audio should be played */
	protected boolean playAudio;
	
	/** Flag indicating the current mode of playing, whether the beats should be played */
	protected boolean playBeats;

	/** Constructor
	 *  @param f The input audio file
	 *  @param ch The FileChooser object for opening and saving files
	 */
	public AudioPlayer(AudioFile f, JFileChooser ch) {
		gui = null;
		currentFile = f;
		jfc = ch;
		currentPosition = 0;
		requestedFile = null;
		requestedPosition = -1;
		endPosition = -1;
		startTime = 0;
		startNanoTime = 0;
		stopRequested = false;
		playing = false;
		outputBufferSize = 0;
		readBuffer = new byte[readBufferSize];
		readBuffer2 = new byte[readBufferSize];
		beats = null;
		beatIndex = 0;
		click = null;
		playAudio = true;
		playBeats = true;
		new Thread(this).start();			// for audio playback
	} // constructor

	/** Notify play thread to play audio with beats */
	public void play() {
		play(true, true);
	}
	
	/** Notify play thread to play either audio or beats.
	 * @param audioOnly Flag indicating whether to play audio (true) or play beats (false)
	 */
	public void play(boolean audioOnly) {
		if (audioOnly)
			play(true, false);
		else
			play(false, true);
	} // play()
	
	/** Notify play thread to play audio, beats or both.
	 * @param audio Flag indicating whether audio should be played
	 * @param beats Flag indicating whether beats should be played
	 */
	public void play(boolean audio, boolean beats) {
		synchronized(this) {
			playAudio = audio;
			playBeats = beats;
			if (!playing) {
				endPosition = -1;
				if (gui.displayPanel.startSelection >= 0) {
					int correction = 0;	// 3 * outputBufferSize;
					setPosition((int)(gui.displayPanel.startSelection *
							currentFile.frameRate) * currentFile.frameSize + correction);
					if (gui.displayPanel.endSelection > gui.displayPanel.startSelection)
						endPosition = (int)(gui.displayPanel.endSelection *
								currentFile.frameRate) * currentFile.frameSize + correction;
				}
				notify();
			}
		}
	} // play()
	
	/** Notifies play thread to pause playing */
	public void pause() {
		if (playing)
			stopRequested = true;
	} // pause()

	/** Notifies play thread to stop playing.
	 * @param resetPosition Flag indicating whether to reset the playback position to the beginning of the file
	 */
	public void stop(boolean resetPosition) {
		if (playing) {
			if (debug)
				System.err.println("STOP playing " + resetPosition);
			stopRequested = true;
			if (resetPosition)
				requestedPosition = 0;
		} else if (resetPosition)
			setPosition(0);
	} // stop()

	/** Notifies play thread to stop playing or reset position if not playing. */
	public void stop() {
		stop(!playing);
	} // stop()

	/** Notifies the play thread to pause if playing or play if paused/stopped. */
	public void togglePlay() {
		if (playing)
			pause();
		else
			play();
	} // togglePlay()

	/** Changes the position and possibly the file for input data, and updates the GUI accordingly.
	 * @param positionRequested The new file position (in bytes) for audio input
	 */
	protected void setPosition(long positionRequested) {
		setPosition(positionRequested, true);
	}

	/** Changes the position and possibly the file for input data.
	 * @param positionRequested The new file position (in bytes) for audio input
	 * @param update Flag to indicate whether the GUI should be updated or not
	 */
	protected void setPosition(long positionRequested, boolean update) {
		if (requestedFile != null) {
			currentFile = requestedFile;
			requestedFile = null;
		}
		if (currentFile != null) {
			try {
				currentPosition = currentFile.setPosition(positionRequested);
				if (currentPosition != positionRequested)
					System.err.println("Warning: setPosition() failed: " +
						currentPosition + " instead of " + positionRequested);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (update)
				gui.displayPanel.scrollTo(currentPosition /
						currentFile.frameSize / currentFile.frameRate, false);
		}
	} // setPosition()

	/** Conditionally changes the position and possibly the file for input data,
	 *  if the play thread is idle.  The GUI is not updated.
	 *  @param time New play position (in seconds)
	 */
	public void ifSetPosition(double time) {
		if (!playing)
			setPosition((int)(time * currentFile.frameRate) * currentFile.frameSize, false);
	} // ifSetPosition()
	
	/** Changes the playback position relative to the current playback position.
	 * @param time Offset of new play position (in seconds) relative to present play position.
	 */
	public void skip(double time) {
		if (currentFile != null)
			skip(Math.round(time * currentFile.frameRate) * currentFile.frameSize);
	} // skip()

	/** Changes the playback position relative to the current playback position.
	 * @param bytes Offset of new play position (in bytes) relative to present play position.
	 */
	public void skip(long bytes) {
		if (currentFile != null) {
			long newPosn = correctedPosition() + bytes;
			if (newPosn < 0)
				newPosn = 0;
			if (playing)
				requestedPosition = newPosn;
			else
				setPosition(newPosn);
		}
	} // skip()

	/** Returns the current playback time in seconds from when play was started.
	 *  Note that DataLine.getLongFramePosition() is not reliable - it counts in units
	 *  of output buffers or simple fractions thereof - i.e. what has been sent to the
	 *  soundcard rather than what has been rendered by the soundcard.
	 *  @return Playback time in seconds
	 */
	public double getCurrentTime() {
		if (playing)
			return (System.nanoTime() - startNanoTime) / 1e9 + startTime;
		else if (currentFile == null)
			return -1;
		if (debug)
			System.err.println("getCurrentTime(): not playing");
		return currentPosition / currentFile.frameSize / currentFile.frameRate;
	} // getCurrentTime()
	
	/** Returns current playback position, corrected for audio buffered in the soundcard.
	 * @return Current playback position in bytes relative to the beginning of the file.
	 */
	public long correctedPosition() {
		if (audioOut == null)
			return currentPosition;
		return currentPosition - (outputBufferSize - audioOut.available());
	} // correctedPosition()

	/** Returns the length of the current or requested file.
	 * @return Length in bytes of the current or requested audio input file.
	 */
	public long getCurrentFileLength() {
		if (requestedFile != null) {
			try {	// avoid race conditions
				return requestedFile.length;
			} catch (NullPointerException e) {}
		}
		try {	// avoid race conditions
			return currentFile.length;
		} catch (NullPointerException e) {}
		return 0;
	} // getCurrentFileLength()

	/** Saves audio with beats as a WAV file, with name determined by a file chooser dialog. */
	public void save() {
		File f = null;
		if ((jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) &&
				(!jfc.getSelectedFile().exists() ||
					(JOptionPane.showConfirmDialog(null,
					"File " + jfc.getSelectedFile().getAbsolutePath() +
					" exists.\nDo you want to replace it?", "Are you sure?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)))
			f = jfc.getSelectedFile();
		if (f == null)
			return;
		try {
			FileOutputStream fos = WavWrite.open(f.getPath(),
					(int)currentFile.length, currentFile.channels,
					(int)Math.round(currentFile.frameRate),
					currentFile.frameSize / currentFile.channels);
			initBeats();
			setPosition(0);
			while (currentPosition < currentFile.length) {
				int bytesRead = currentFile.audioIn.read(readBuffer);
				if (bytesRead > 0) {
					addBeats(readBuffer, bytesRead);
					fos.write(readBuffer, 0, bytesRead);
					currentPosition += bytesRead;
				} else
					break;
			}
			if (currentPosition < currentFile.length)
				System.err.println("saveAudio(): error: wrote " + currentPosition +
						" of " + currentFile.length + " bytes");
			fos.close();
		} catch (IOException e) {
			System.err.println("IOException while saving data");
		}
	} // save()

	/** Open a new audio input file, with the path being chosen by a file chooser dialog. */
	public void load() {
		if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			load(jfc.getSelectedFile().getAbsolutePath());
	} // load()

	/** Open a new audio input file with the given path.
	 *  @param fileName The relative or absolate path name of the new audio input file. */
	public void load(String fileName) {
		if (new File(fileName).exists())
			setCurrentFile(new AudioFile(fileName));
		else
			System.err.println("File " + fileName + " does not exist.");
	} // load()

	/** Change the input file for audio playback (in a thread-safe way).
	 *  @param newFile The new input file.
	 */
	public void setCurrentFile(AudioFile newFile) {
		if (newFile == currentFile)
			return;
		if (newFile == null) {
			System.err.println("setCurrentFile(): null file");
			return;
		}
		if (currentFile != null) {
			synchronized(this) {	// make it thread-safe
				if (playing) {
					requestedFile = newFile;
					requestedPosition = 0;
				} else {
					currentFile = newFile;
					setPosition(0);
				}
			}
		} else {
			currentFile = newFile;
			setPosition(0);
		}
	} // setCurrentFile()

	/** Implements the Runnable interface for the audio playback thread.
	 *  This method has two loops: an outer "request" loop and an inner "play" loop.
	 *  The outer loop waits for a signal (notify()) from the play() method, and then
	 *  initialises audio output and click track data, and enters the inner play loop.
	 *  The inner loop reads audio from the input file and writes to the audio device,
	 *  exiting only at the end of file or an external request to stop or pause playback,
	 *  or to switch playback to a new file.
	 */
	public void run() {
		int bytesRead, bytesWritten;
		while (true) {
			try {
				if ((currentFile == null) || stopRequested || !playing) {
					synchronized(this) {
						playing = false;
						wait();
						playing = true;
						stopRequested = false;
					}
					if (currentFile == null)
						continue;
					if (currentPosition == currentFile.length)	// necessary???
						setPosition(0);
				}
				if (audioOut != null) {
					audioOut.stop();
					audioOut.flush();
				}
				if ((audioOut == null) ||
							!currentFile.format.matches(audioOut.getFormat())) {
					audioOut= AudioSystem.getSourceDataLine(currentFile.format);
					audioOut.open(currentFile.format, defaultOutputBufferSize);
					outputBufferSize = audioOut.getBufferSize();
				}
				initBeats();
				boolean doDrain = false;
				startNanoTime = System.nanoTime();
				startTime = currentPosition / currentFile.frameRate / currentFile.frameSize;
				audioOut.start();
				if (debug)
					System.err.println("Entering play() loop");
				while (true) {			// PLAY loop
					synchronized(this) {
						if ((requestedPosition < 0) && !stopRequested)
							bytesRead = currentFile.audioIn.read(readBuffer);
						else if (stopRequested ||
									((requestedPosition >= 0) &&
										(requestedFile != null) &&
										!currentFile.format.matches(
											requestedFile.format))) {
							if (doDrain)
								audioOut.drain();
							audioOut.stop();
							if (requestedPosition >= 0) {
								setPosition(requestedPosition);
								requestedPosition = -1;
							} else if (!doDrain)
								setPosition((int)(getCurrentTime() * currentFile.frameRate) *
											currentFile.frameSize);
							else
								setPosition(currentPosition);	// for scrollTo()
							audioOut.flush();
							doDrain = false;
							break;
						} else {	// requestedPosition >= 0 && format matches
							bytesRead = currentFile.audioIn.read(readBuffer);
							setPosition(requestedPosition);
							requestedPosition = -1;
							if (bytesRead == readBuffer.length) {
								int read = currentFile.audioIn.read(readBuffer2);
								if (read == bytesRead) {	// linear crossfade
									int sample, sample2;
									for (int i = 0; i < read; i += 2) {
										if (currentFile.format.isBigEndian()) {
											sample = (readBuffer[i+1] & 0xff) |
													 (readBuffer[i] << 8);
											sample2= (readBuffer2[i+1] & 0xff) |
													 (readBuffer2[i] << 8);
											sample = ((read-i) * sample +
														i * sample2) / read;
											readBuffer[i] = (byte)(sample >> 8);
											readBuffer[i+1] = (byte)sample;
										} else {
											sample = (readBuffer[i] & 0xff) |
													 (readBuffer[i+1] << 8);
											sample2 = (readBuffer2[i] & 0xff) |
													 (readBuffer2[i+1] << 8);
											sample = ((read-i) * sample +
														i * sample2) / read;
											readBuffer[i+1] = (byte)(sample>>8);
											readBuffer[i] = (byte)sample;
										}
									}
								} else {
									bytesRead = read;
									for (int i = 0; i < read; i++)
										readBuffer[i] = readBuffer2[i];
								}
							} else
								bytesRead = currentFile.audioIn.read(readBuffer);
						}
					} // synchronized
					if ((endPosition >= 0) && (currentPosition + bytesRead > endPosition))
						bytesRead = (int) (endPosition - currentPosition);
					if (!playAudio)
						for (int i = 0; i < bytesRead; i++)
							readBuffer[i] = 0;
					if (playBeats)
						addBeats(readBuffer, bytesRead);
					bytesWritten = 0;
					if (bytesRead > 0)
						bytesWritten = audioOut.write(readBuffer, 0,bytesRead);
					if (bytesWritten > 0) {
						currentPosition += bytesWritten;
						gui.displayPanel.scrollTo(getCurrentTime(), true);
					}
					if ((endPosition >= 0) && (currentPosition >= endPosition)) {
						stopRequested = true;
						doDrain = true;
					} else if (bytesWritten < readBufferSize) {
						if (currentPosition != currentFile.length)
							System.err.println("read error: unexpected EOF");
						stopRequested = true;
						doDrain = true;
					}
				} // play loop
			} catch (InterruptedException e) {
				playing = false;
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				playing = false;
				audioOut = null;
				e.printStackTrace();
			} catch (IOException e) {
				playing = false;
				e.printStackTrace();
			} catch (Exception e) {		// catchall e.g. for changing instr during playing etc.
				playing = false;
				e.printStackTrace();
			}
		} // process request loop
	} // run

	/** Initialise the percussion sounds for audio playback with beats.
	 *  Reads each of the sounds into an array for quick and simple playback.
	 */
	protected void initClicks() {
		click = new int[7][];
		for (int i = 0; i < GUI.percussionCount; i++)
			click[i] = new AudioFile(gui.getPercussionSound(i)).read();
	} // initClicks()
	
	/** Initialise the beat list ready for audio playback with beats.
	 *  Copies beats into an array for quick and simple retrieval during playback.
	 *  Might not be the best solution; e.g. direct use of the beat list would be
	 *  possible if checks were made for concurrent modification
	 */
	protected void initBeats() {
		EventList beatTimes = gui.getBeatData();
		beats = new long[beatTimes.size()];
		level = new int[beatTimes.size()];
		int mask = (1 << GUI.percussionCount) - 1;
		int i = 0;
		for (Event ev: beatTimes.l) {
			beats[i] = (long) Math.round(ev.keyDown * currentFile.frameRate) *
						currentFile.frameSize;
			int lev = 0;
			for (int flag = ev.flags & mask; flag > 0; flag >>= 1)
				lev++;
			level[i] = lev > 0? lev - 1: 0;
			i++;
		}
		beatIndex = 0;
		while ((beatIndex < beats.length) && (beats[beatIndex] < currentPosition))
			beatIndex++;
	} // initBeats()
	
	/** Adds a sequence of percussive sounds marking the beats at various metrical levels to an audio sample.
	 *  @param buffer The audio sample without beats
	 *  @param bytes  The length of the audio sample in bytes
	 */
	protected void addBeats(byte[] buffer, int bytes) {
		if ((beats == null) || (bytes == 0)) 
			return;
		int clickLen;
		if (beatIndex > 0) {	// complete tail of previous beat if necessary
			clickLen = click[level[beatIndex - 1]].length;
			if (beats[beatIndex-1] + clickLen * currentFile.frameSize > currentPosition) {
				int start = (int) (beats[beatIndex-1] - currentPosition);
				int len = clickLen + start / currentFile.frameSize;
				if (bytes < len)
					len = bytes;
				addBeat(buffer, start, len, level[beatIndex - 1]);
			}
		}
		while ((beatIndex < beats.length) && (beats[beatIndex] < currentPosition + bytes)) {
			clickLen = click[level[beatIndex]].length;
			int start = (int) (beats[beatIndex] - currentPosition);
			int len;
			if (start < 0)
				len = Math.min(bytes, clickLen * currentFile.frameSize + start);
			else
				len = Math.min(bytes - start, clickLen * currentFile.frameSize);
			addBeat(buffer, start, len, level[beatIndex]);
			beatIndex++;
		}
	} // addBeats()
	
	/** Adds a percussion sound to an audio sample at a specified time.
	 *  @param buffer The audio sample without the beats being marked
	 *  @param offset Offset of the percussion sound relative to the audio sample
	 *  @param len    Length of the sound to add (in bytes)
	 *  @param level  Metrical level of the percussion sound (determines instrument)
	 */
	protected void addBeat(byte[] buffer, int offset, int len, int level) {
		int i1 = 0;
		int i2 = 0;
		int sample;
		if (offset < 0)
			i1 = -offset / currentFile.frameSize;
		else
			i2 = offset;
		for (int j = 0; j < len; j += currentFile.frameSize) {
			for (int k = 0; k < currentFile.channels; k++) {
				if (currentFile.format.isBigEndian())
					sample = (buffer[i2+1] & 0xff) | (buffer[i2] << 8);
				else
					sample = (buffer[i2] & 0xff) | (buffer[i2+1] << 8);
				sample += (int) (click[level][i1] * volume);
				if (sample > Short.MAX_VALUE)
					sample = Short.MAX_VALUE;
				else if (sample < Short.MIN_VALUE)
					sample = Short.MIN_VALUE;
				if (currentFile.format.isBigEndian()) {
					buffer[i2+1] = (byte) sample;
					buffer[i2] = (byte) (sample >> 8);
				} else {
					buffer[i2] = (byte) sample;
					buffer[i2+1] = (byte) (sample >> 8);
				}
				i2 += 2;
			}
			i1++;
		}
	} // addBeat()
	
//	/** Implements the interface ChangeListener */
//	public void stateChanged(ChangeEvent e) {
//		int value = gui.playSlider.getValue();
//		if ((value == gui.oldSlider) || (currentFile == null))
//			return;
//		// System.err.println("Change event: new value " + value);
//		if (gui.playSlider.getValueIsAdjusting())
//			stopRequested = true;
//		else {
//			// System.err.println("\tChange event: applying change *********");
//			long newPosn = currentFile.length * value / gui.maxSlider;
//			newPosn = newPosn / currentFile.frameSize * currentFile.frameSize;
//			if (playing)
//				requestedPosition = newPosn;
//			else
//				setPosition(newPosn);
//		}
//	} // stateChanged()
	
} // class AudioPlayer
