/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TrackAnalyzer;

import java.util.ArrayList;

/**
 *
 * @author Thomas
 */
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
	private Object factoryMutex;

	public SpectrumAnalyserFactory() {
		analysers = new ArrayList<SpectrumAnalyserWrapper>(0);
	}

	public SpectrumAnalyser getSpectrumAnalyser(int f, Parameters p) throws Exception {
		//@todo lock
		//boost::mutex::scoped_lock lock(factoryMutex);
		for (int i = 0; i < analysers.size(); i++) {
			if (analysers.get(i).chkFrameRate() == f && p.equivalentForSpectralAnalysis(analysers.get(i).chkParams())) {
				return analysers.get(i).getSpectrumAnalyser();
			}
		}
		// no match found, build a new spectrum analyser
		analysers.add(new SpectrumAnalyserWrapper(f, p, new FftwAnalyser(f, p)));
		return analysers.get(analysers.size() - 1).getSpectrumAnalyser();

	}
}