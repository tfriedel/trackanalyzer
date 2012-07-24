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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import at.ofai.music.util.RandomAccessInputStream;
import at.ofai.music.audio.Convert;

/** A simple class for reading audio data from files. */
public class AudioFile {

	/** The path name of the audio file (relative or absolute) */
	protected String path;
	
	/** The raw stream from which data is read */
	private RandomAccessInputStream underlyingStream;
	
	/** The stream from which audio is read (after conversion if necessary) */
	protected AudioInputStream audioIn;
	
	/** The format of the audio data */
	protected AudioFormat format;

	/** The amount of audio data in bytes */
	protected long length;
	
	/** The size of an audio frame (i.e. one sample for each channel) */
	protected int frameSize;
	
	/** The sampling rate */
	protected float frameRate;
	
	/** The number of channels in the audio stream */
	protected int channels;

	/** Constructor
	 *  @param pathName The relative or absolute path name of the audio file
	 */
	public AudioFile(String pathName) {
		path = pathName;
		try {
			if (new File(pathName).exists()) {
				underlyingStream = new RandomAccessInputStream(pathName);
				audioIn = AudioSystem.getAudioInputStream(underlyingStream);
				audioIn.mark(0);
				underlyingStream.mark();	// after the audio header
			} else {
				java.net.URL url = ClassLoader.getSystemResource(pathName);
				if (url == null)
					BeatRoot.error("Resource not found");
				audioIn = AudioSystem.getAudioInputStream(url);
				audioIn.mark(0);
			}
			format = audioIn.getFormat();
			frameSize = format.getFrameSize();
			frameRate = format.getFrameRate();
			channels = format.getChannels();
			length = audioIn.getFrameLength() * frameSize;
		} catch (java.io.IOException e) {	// includes FileNotFound
			System.err.println("File not found."); //e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			System.err.println("File not supported."); //e.printStackTrace();
		}
	} // constructor

	/** Returns a String representation of the audio file, consisting of
	 *  path name, length and frame size.
	 */
	public String toString() {
		return path + "  Size: " + length + "/" + frameSize;
	} // toString()

	/** Moves the read pointer to the specified time in seconds. */
	public double setPosition(double time) throws java.io.IOException {
		long position = (long)Math.round(time * frameRate) * frameSize;
		return setPosition(position) / frameSize / frameRate;
	} // setPosition()

	/** Moves the read pointer to the specified byte position. */
	public long setPosition(long position) throws java.io.IOException {
		if (audioIn == null)
			return -1;
		audioIn.reset();
		if (position < 0)
			position = 0;
		if (position > length)
			position = length;
		// must be multiple of frameSize
		return underlyingStream.seekFromMark(position / frameSize * frameSize);
	} // setPosition()

	/** Reads some number of bytes from the audio input stream and stores them into the buffer array.
	 *  @param buffer The buffer for storing audio data
	 *  @return The number of bytes read, or -1 if there is no more data (EOS)
	 */
	public int read(byte[] buffer) {
		try {
			return audioIn.read(buffer);
		} catch (IOException e) {
			System.err.println(e);
			return -1;
		}
	} // read()/1
	
	/** Reads a mono 16-bit audio file and returns the whole file as an int array 
	 *  @return The content of the audio file
	 */
	public int[] read() {
		final int buffSize = 16392;
		byte[] buffer = new byte[buffSize * frameSize];
		int[] data = new int[(int)(length / frameSize)];
		int len = 0;
		while (true) {
			int bytesRead = read(buffer);
			if (bytesRead <= 0)
				break;
			Convert.monoShortToInt(buffer, 0, bytesRead,
					data, len, format.isBigEndian());
			len += bytesRead / frameSize;
		}
		if (len != data.length)
			System.err.println("Warning: read(): " + len + " of " +
					data.length + " samples sucessfully read.");
		return data;
	} // read()/0

	/** Unit test for reading audio from jar file */
	public static void main(String[] args) {
		int[] data = new AudioFile("audio/33-metclick.wav").read();
		System.out.println("Length: " + data.length);
	} // main()
	
} // class AudioFile
