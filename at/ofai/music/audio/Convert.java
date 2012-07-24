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

public class Convert {

	public static void monoShortToInt(byte[] in, int[] out,boolean isBigEndian){
		monoShortToInt(in, 0, in.length, out, 0, isBigEndian);
	} // monoShortToInt()/3
	
	public static void monoShortToInt(byte[] in, int inIndex, int bytes,
						int[] out, int outIndex, boolean isBigEndian){
		if (isBigEndian)
			for ( ; inIndex < bytes; inIndex += 2)
				out[outIndex++] = (in[inIndex+1] & 0xff) | (in[inIndex] << 8);
		else
			for ( ; inIndex < bytes; inIndex += 2)
				out[outIndex++] = (in[inIndex] & 0xff) | (in[inIndex+1] << 8);
	} // monoShortToInt()

	public static void monoShortToDouble(byte[] in, double[] out,
											boolean isBigEndian) {
		int j = 0;
		if (isBigEndian)
			for (int i = 0; i < in.length; i += 2)
				out[j++] = ((in[i+1] & 0xff) | (in[i] << 8)) / 32768.0;
		else
			for (int i = 0; i < in.length; i += 2)
				out[j++] = ((in[i] & 0xff) | (in[i+1] << 8)) / 32768.0;
	} // monoShortToDouble()

} // class Convert
