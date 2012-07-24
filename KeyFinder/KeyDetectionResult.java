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
public class KeyDetectionResult {

	public Chromagram fullChromagram;
	public Chromagram oneOctaveChromagram;
	public ArrayList<Float> harmonicChangeSignal = new ArrayList<Float>();
	public ArrayList<KeyDetectionSegment> segments = new ArrayList<KeyDetectionSegment>();
	public Parameters.key_t globalKeyEstimate;
}
