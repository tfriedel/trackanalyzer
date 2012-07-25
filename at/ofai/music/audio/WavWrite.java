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

package at.ofai.music.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WavWrite {

	public static void toByte(byte[] out, String data, int offset) {
		try {
			byte[] b = data.getBytes("US-ASCII");
			for (int i = 0; i < b.length; i++)
				out[offset++] = b[i];
		} catch (UnsupportedEncodingException e) {
			System.err.println(e);
		}
	} // toByte()

	public static void toByte(byte[] out, long data,int offset,int len){
		for (int stop = offset + len; offset < stop; offset++) {
			out[offset] = (byte)data;
			data >>= 8;
		}
	} // toByte()

	/** Opens a file output stream and writes a WAV file header to it */
	public static FileOutputStream open(String fileName, int byteLength,
				int channels, int rate, int audioSize) {
		FileOutputStream out;
		try {
			out = new FileOutputStream(new File(fileName));
			byte[] wavHeader = new byte[44];
			toByte(wavHeader, "RIFF", 0);
			toByte(wavHeader, byteLength+36, 4, 4);
			toByte(wavHeader, "WAVEfmt ", 8);
			toByte(wavHeader, 16, 16, 4);				// chunk length
			toByte(wavHeader, 1, 20, 2);				// PCM encoding
			toByte(wavHeader, channels, 22, 2);			// channels
			toByte(wavHeader, rate, 24, 4);				// sampling rate
			toByte(wavHeader, audioSize * channels * rate, 28, 4);// bytes per s
			toByte(wavHeader, audioSize * channels, 32, 2);	// block alignment
			toByte(wavHeader, 8 * audioSize, 34, 2);	// bits per sample
			toByte(wavHeader, "data", 36);
			toByte(wavHeader, byteLength, 40, 4);
			out.write(wavHeader);
		} catch (FileNotFoundException e) {
			System.err.println("WavWrite: Error opening output file: "+
								fileName + "\n" + e);
			return null;
		} catch (IOException e) {
			System.err.println("Error writing output file header\n"+e);
			return null;
		}
		return out;
	} // open()

} // class WavWrite()
