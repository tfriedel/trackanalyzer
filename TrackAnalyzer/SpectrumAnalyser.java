/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TrackAnalyzer;

/**
 *
 * @author Thomas
 */
public abstract class SpectrumAnalyser {
	protected int bins;
	protected int hopSize;
	protected int frameRate;
	protected Object analyserMutex;	
	public SpectrumAnalyser(int f, Parameters params){
	    bins = params.getOctaves() * params.getBpo();
    	hopSize = params.getHopSize();
	    frameRate = f;
	} 	
	abstract public Chromagram chromagram(AudioData data) throws Exception;
	
}
