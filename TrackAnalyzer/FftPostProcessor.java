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

public abstract class FftPostProcessor {

	protected int bins;
	protected int fftFrameSize;
	protected int frameRate;

	public static FftPostProcessor getFftPostProcessor(int fr, Parameters params) throws Exception {
		return new DirectSkPostProc(fr, params);
	}

	public FftPostProcessor(int fr, Parameters params) throws Exception {
		if (fr < 1) 
			throw new Exception("Frame rate must be > 0");
		frameRate = fr;
		bins = params.getOctaves() * params.getBpo();
		fftFrameSize = params.getFftFrameSize();
	}

	abstract public ArrayList<Float> chromaVector(double[] fftResult);
}
