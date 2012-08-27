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

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import java.util.ArrayList;
import java.util.Arrays;

public class FftwAnalyser extends SpectrumAnalyser {

	private int fftFrameSize;
	private FftPostProcessor pp;
	private double[] fftInput;
	private ArrayList<Float> window;
	private final DoubleFFT_1D fft;

	public FftwAnalyser(int f, Parameters params) throws Exception {
		super(f, params);
		fftFrameSize = params.getFftFrameSize();
		fft = new DoubleFFT_1D(fftFrameSize);
		pp = FftPostProcessor.getFftPostProcessor(f, params);
		//fftInput = (fftw_complex*)fftw.fftw_malloc(sizeof(fftw_complex)*fftFrameSize);
		//fftInput = fftw.fftw_malloc(new NativeLong(sizeofDouble*fftFrameSize));
		fftInput = new double[fftFrameSize * 2]; //2 because these are complex values with real and img. part
		//fftResult = (fftw_complex*)fftw_malloc(sizeof(fftw_complex)*fftFrameSize);
		//fftResult = fftw.fftw_malloc(new NativeLong(sizeofDouble*fftFrameSize));
		// plan 1-dimensional DFT
		//fftPlan = fftw_plan_dft_1d(fftFrameSize, fftInput, fftResult, FFTW_FORWARD, FFTW_ESTIMATE);
		//fftPlan = fftw.fftw_plan_dft_1d(fftFrameSize, inbuf, resultbuf, FFTW3Library.FFTW_FORWARD, FFTW3Library.FFTW_ESTIMATE);	
		// prep temporal window function
		WindowFunction wf = WindowFunction.getWindowFunction(params.getTemporalWindow());
		window = new ArrayList<Float>(fftFrameSize);
		for (int i = 0; i < fftFrameSize; i++) {
			window.add(wf.window(i, fftFrameSize));
		}

	}

	@Override
	public Chromagram chromagram(AudioData audio) throws Exception {
		analyserMutex.lock();
		try {
			int sampleCount = audio.getSampleCount();
			Chromagram ch = new Chromagram((sampleCount / hopSize) + 1, bins);
			for (int i = 0; i < sampleCount; i += hopSize) {
				for (int j = 0; j < fftFrameSize; j++) {
					if (i + j < sampleCount) {
						fftInput[j] = (double) (audio.getSample(i + j) * window.get(j)); // real part, windowed
					} else {
						fftInput[j] = 0.0; // zero-pad if no PCM data remaining
					}
				}
				fft.realForwardFull(fftInput);
				ArrayList<Float> cv = pp.chromaVector(fftInput);

				for (int j = 0; j < bins; j++) {
					ch.setMagnitude(i / hopSize, j, cv.get(j));
				}
			}
			return ch;
		} finally {
			analyserMutex.unlock();
		}
	}
}