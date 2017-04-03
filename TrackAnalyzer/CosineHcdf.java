/**
 * ***********************************************************************
 *
 * Copyright 2012 Ibrahim Sha'ath
 *
 * This file is part of LibKeyFinder.
 *
 * LibKeyFinder is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * LibKeyFinder is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * LibKeyFinder. If not, see <http://www.gnu.org/licenses/>.
 *
 ************************************************************************
 */
/**
 * **********************************************************************
 * This file was modified/ported to Java in 2012 by Thomas Friedel
***********************************************************************
 */
package TrackAnalyzer;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
//@todo test this class
public class CosineHcdf extends Segmentation {

	@Override
	public ArrayList<Float> getRateOfChange(Chromagram ch, Parameters params) {
		int hops = ch.getHops();
		int bins = ch.getBins();
		int gaussianSize = params.getHcdfGaussianSize();
		float gaussianSigma = params.getHcdfGaussianSigma();
		int padding = 0; // as opposed to gaussianSize/2
		ArrayList<Float> cosine = new ArrayList<Float>(hops + padding);
		for (int hop = 0; hop < hops; hop++) {
			float top = (float) 0.0;
			float bottom = (float) 0.0;
			for (int bin = 0; bin < bins; bin++) {
				float mag = 0;
				try {
					mag = ch.getMagnitude(hop, bin);
				} catch (Exception ex) {
					Logger.getLogger(CosineHcdf.class.getName()).log(Level.SEVERE, null, ex);
				}
				top += mag;
				bottom += Math.pow(mag, 2);
			}
			float cos;
			if (bottom > 0.0) {
				cos = (float) (top / Math.sqrt(bottom) * Math.sqrt(bins * Math.sqrt(2)));
			} else {
				cos = (float) 0.0;
			}
			cosine.add(cos);
		}
		// gaussian
		ArrayList<Float> gaussian = new ArrayList<Float>(gaussianSize);
		for (int i = 0; i < gaussianSize; i++) {
			gaussian.add((float) Math.exp(-1 * (Math.pow(i - gaussianSize / 2, 2) / (2 * gaussianSigma * gaussianSigma))));
		}

		ArrayList<Float> smoothed = new ArrayList<Float>(hops);
		for (int i = 0; i < hops; i++) {
			smoothed.add((float) 0);
		}
		for (int hop = padding; hop < (hops + padding); hop++) {
			float conv = 0;
			for (int k = 0; k < gaussianSize; hop++) {
				int frm = hop - (gaussianSize / 2) + k;
				if (frm >= 0 && frm < (hops + padding)) {
					conv += cosine.get(frm) * gaussian.get(k);
				}
			}
			smoothed.set(hop - padding, conv);
		}
		// rate of change of hcdf signal; look at all hops except first.
		ArrayList<Float> rateOfChange = new ArrayList<Float>(hops);
		for (int i = 0; i < hops; i++) {
			rateOfChange.add((float) 0);
		}
		for (int hop = 1; hop < hops; hop++) {
			float change = (float) ((smoothed.get(hop) - smoothed.get(hop - 1)) / 90.0);
			change = (change >= 0 ? change : change * (float) -1.0);
			change = change / (float) 0.16; // very cheeky magic number; for display purposes in KeyFinder GUI app
			rateOfChange.set(hop, change);
		}

		// fudge first
		rateOfChange.set(0, rateOfChange.get(1));
		return rateOfChange;
	}

	@Override
	public ArrayList<Integer> getSegments(ArrayList<Float> rateOfChange, Parameters params) {
		// Pick peaks
		ArrayList<Integer> changes = new ArrayList<Integer>(1); // start vector with a 0 to enable first classification
		changes.add(0);
		int neighbours = params.getHcdfPeakPickingNeighbours();
		for (int hop = 0; hop < rateOfChange.size(); hop++) {
			boolean peak = true;
			for (int i = -neighbours; i <= neighbours; i++) {
				if (i != 0 && hop + i < rateOfChange.size()) // only test valid neighbours
				{
					if (rateOfChange.get(hop + i) >= rateOfChange.get(hop)) {
						peak = false; // there's a neighbour of higher or equal value; this isn't a peak
					}
				}
			}
			if (peak) {
				changes.add(hop);
			}
		}
		return changes;
	}
}
