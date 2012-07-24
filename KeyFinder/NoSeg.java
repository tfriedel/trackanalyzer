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
public class NoSeg extends Segmentation {

	@Override
	public ArrayList<Float> getRateOfChange(Chromagram ch, Parameters params) {
		ArrayList<Float> val = new ArrayList<Float>(ch.getHops());
		for (int i=0; i< ch.getHops();i++) {
			val.add(new Float(0));
		}
		return val;
	}

	@Override
	public ArrayList<Integer> getSegments(ArrayList<Float> a, Parameters params) {
		ArrayList<Integer> val = new ArrayList<Integer>(1);
		val.add(new Integer(0));
		return val;
	}
	
}
