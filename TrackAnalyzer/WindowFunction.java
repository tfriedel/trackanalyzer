/*************************************************************************

  Copyright 2012 Ibrahim Sha'ath

  This file is part of LibKeyFinder.

  LibKeyFinder is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  LibKeyFinder is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with LibKeyFinder.  If not, see <http://www.gnu.org/licenses/>.

*************************************************************************/

/************************************************************************
 This file was modified/ported to Java in 2012 by Thomas Friedel
************************************************************************/ 
package TrackAnalyzer;

public abstract class WindowFunction {

	public WindowFunction() {
	}

	public static WindowFunction getWindowFunction(Parameters.temporal_window_t w) {
		if (w == Parameters.temporal_window_t.WINDOW_HANN) {
			return new HannWindow();
		} else if (w == Parameters.temporal_window_t.WINDOW_HAMMING) {
			return new HammingWindow();
		} else {
			return new BlackmanWindow();
		}
	}

	abstract public float window(int n, int N);
}

class HannWindow extends WindowFunction {

	@Override
	public float window(int n, int N) {
		return (float) (0.5 * (1.0 - Math.cos((2 * Math.PI * n) / (N - 1))));
	}
}

class HammingWindow extends WindowFunction {

	@Override
	public float window(int n, int N) {
		return (float) (0.54 - (0.46 * Math.cos((2 * Math.PI * n) / (N - 1))));
	}
}

class BlackmanWindow extends WindowFunction {

	@Override
	public float window(int n, int N) {
		return (float) (0.42 - (0.5 * Math.cos((2 * Math.PI * n) / (N - 1))) + (0.08 * Math.cos((4 * Math.PI * n) / (N - 1))));

	}
}
