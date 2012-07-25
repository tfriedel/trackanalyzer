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

import java.util.ArrayList;

public class DirectSkPostProc extends FftPostProcessor {

	private ArrayList<ArrayList<Float>> mySpecKernel;
	private ArrayList<Integer> binOffsets;

	private final float kernelWindow(float n, float N) {
		// discretely sampled continuous function, but different to other window functions
		return (float) (1.0 - Math.cos((2 * Math.PI * n) / N)); // based on Hann; no need to halve since we normalise later

	}

	public DirectSkPostProc(int fr, Parameters params) throws Exception {
		super(fr, params);
// TODO check that last frequency doesn't go over Nyquist, and for sufficient low end resolution.
		binOffsets = new ArrayList<Integer>(bins);
		for (int i=0; i<bins; i++)
			binOffsets.add(0);
		mySpecKernel = Utils.newFloatArrayList2D(bins, 0);
		float myQFactor = (float) (params.getDirectSkStretch() * (Math.pow(2,(1.0 / params.getBpo()))-1));
		for (int i = 0; i < bins; i++){
		  float centreOfWindow = params.getBinFreq(i) * fftFrameSize / fr;
		  float widthOfWindow = centreOfWindow * myQFactor;
		  float beginningOfWindow = centreOfWindow - (widthOfWindow / 2);
		  float endOfWindow = beginningOfWindow + widthOfWindow;
		  float sumOfCoefficients = (float) 0.0;
		  for (int thisFftBin = 0; thisFftBin < fftFrameSize; thisFftBin++){
			if((float)thisFftBin < beginningOfWindow)
			  continue; // haven't got to useful fft bins yet
			if((float)thisFftBin > endOfWindow)
			  break; // finished with useful fft bins
			if(binOffsets.get(i) == 0)
			  binOffsets.set(i, thisFftBin); // first useful fft bin
			float coefficient = kernelWindow(thisFftBin-beginningOfWindow,widthOfWindow);
			sumOfCoefficients += coefficient;
			mySpecKernel.get(i).add(coefficient);
		  }
		  // normalisation by sum of coefficients and frequency of bin; models CQT very closely
		  for (int j = 0; j < mySpecKernel.get(i).size(); j++)
			mySpecKernel.get(i).set(j, mySpecKernel.get(i).get(j) / sumOfCoefficients * params.getBinFreq(i));
		}

	}

	@Override
	public final ArrayList<Float> chromaVector(double[] fftResult) {
		ArrayList<Float> cv = new ArrayList<Float>(bins);
		for (int i = 0; i < bins; i++) {
			float sum = (float) 0.0;
			for (int j = 0; j < mySpecKernel.get(i).size(); j++) {
				int binNum = binOffsets.get(i) + j;
				double real = fftResult[binNum*2];
				double imag = fftResult[binNum*2+1];
				float magnitude = (float) Math.sqrt((real * real) + (imag * imag));
				sum += (magnitude * mySpecKernel.get(i).get(j));
			}
			cv.add(new Float(sum));
		}
		return cv;
	}
}
