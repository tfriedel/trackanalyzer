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

package at.ofai.music.util;

public class ArrayPrint {

	public static void show(String s, double[] arr, int line) {
		System.out.println(s + " (length = " + arr.length + ")");
		for (int i = 0; i < arr.length; i++) {
			System.out.printf("%7.3f ", arr[i]);
			if (i % line == line - 1)
				System.out.println();
		}
		System.out.println();
	} // show()
		
	void show(String s, int[] arr, int line) {
		System.out.println(s + " (length = " + arr.length + ")");
		for (int i = 0; i < arr.length; i++) {
			System.out.printf("%7d ", arr[i]);
			if (i % line == line - 1)
				System.out.println();
		}
		System.out.println();
	} // show()

}
