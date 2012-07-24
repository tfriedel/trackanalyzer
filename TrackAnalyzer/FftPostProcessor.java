/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package KeyFinder;

import java.util.ArrayList;

/**
 *
 * @author Thomas
 */
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
