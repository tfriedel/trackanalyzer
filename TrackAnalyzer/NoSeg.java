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
		val.add(Integer.valueOf(0));
		return val;
	}
	
}
