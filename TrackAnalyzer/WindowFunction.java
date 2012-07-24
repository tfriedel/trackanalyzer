/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TrackAnalyzer;

/**
 *
 * @author Thomas
 */
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
