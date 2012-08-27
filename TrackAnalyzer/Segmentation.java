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

public abstract class Segmentation {

	public static Segmentation getSegmentation(Parameters params) {
		
		if (params.getSegmentation() == Parameters.segmentation_t.SEGMENTATION_COSINE) {
			return new CosineHcdf();
		}
		/*	
		} else if (params.getSegmentation() == Parameters.segmentation_t.SEGMENTATION_HARTE) {
			return new HarteHcdf();
		} else if (params.getSegmentation() == Parameters.segmentation_t.SEGMENTATION_ARBITRARY) {
			return new ArbitrarySeg();
		} else {
			return new NoSeg();
		}
		*/
		else return new NoSeg();
	}

	public abstract ArrayList<Float> getRateOfChange(Chromagram ch, Parameters params);

	public abstract ArrayList<Integer> getSegments(ArrayList<Float> a, Parameters params);
}
