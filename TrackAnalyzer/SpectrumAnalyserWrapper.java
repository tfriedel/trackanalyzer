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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class SpectrumAnalyserWrapper {

	private int frate;
	private Parameters params;
	private SpectrumAnalyser sa;

	public SpectrumAnalyserWrapper(int f, Parameters p, SpectrumAnalyser s) {
		frate = f;
		params = p;
		sa = s;
	}

	public SpectrumAnalyser getSpectrumAnalyser() {
		return sa;
	}

	public Parameters chkParams() {
		return params;
	}

	public int chkFrameRate() {
		return frate;
	}
}

class SpectrumAnalyserFactory {

	private ArrayList<SpectrumAnalyserWrapper> analysers;
	private Lock factoryMutex;

	public SpectrumAnalyserFactory() {
		factoryMutex = new ReentrantLock();
		analysers = new ArrayList<SpectrumAnalyserWrapper>(0);
	}

	public SpectrumAnalyser getSpectrumAnalyser(int f, Parameters p) throws Exception {
		factoryMutex.lock();
		try {
			for (int i = 0; i < analysers.size(); i++) {
				if (analysers.get(i).chkFrameRate() == f && p.equivalentForSpectralAnalysis(analysers.get(i).chkParams())) {
					return analysers.get(i).getSpectrumAnalyser();
				}
			}
			// no match found, build a new spectrum analyser
			analysers.add(new SpectrumAnalyserWrapper(f, p, new FftwAnalyser(f, p)));
			return analysers.get(analysers.size() - 1).getSpectrumAnalyser();
		} finally {
			factoryMutex.unlock();
		}
	}
}