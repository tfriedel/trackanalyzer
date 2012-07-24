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

public class Util {

	public static double rms(double[] d) {
		double sum = 0;
		for (int i=0; i < d.length; i++)
			sum += d[i] * d[i];
		return Math.sqrt(sum / d.length);
	}

	public static double min(double[] d) {
		double min = d[0];
		for (int i=1; i < d.length; i++)
			if (d[i] < min)
				min = d[i];
		return min;
	}

	public static double max(double[] d) {
		double max = d[0];
		for (int i=1; i < d.length; i++)
			if (d[i] > max)
				max = d[i];
		return max;
	}

	public static double threshold(double value, double min, double max) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

} // class Util
