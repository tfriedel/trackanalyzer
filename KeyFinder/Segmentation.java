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
public abstract class Segmentation {

	public static Segmentation getSegmentation(Parameters params) {
		/*
		if (params.getSegmentation() == Parameters.segmentation_t.SEGMENTATION_COSINE) {
			return new CosineHcdf();
		} else if (params.getSegmentation() == Parameters.segmentation_t.SEGMENTATION_HARTE) {
			return new HarteHcdf();
		} else if (params.getSegmentation() == Parameters.segmentation_t.SEGMENTATION_ARBITRARY) {
			return new ArbitrarySeg();
		} else {
			return new NoSeg();
		}
		*/
	    return new NoSeg();
	}

	public abstract ArrayList<Float> getRateOfChange(Chromagram ch, Parameters params);

	public abstract ArrayList<Integer> getSegments(ArrayList<Float> a, Parameters params);
}
