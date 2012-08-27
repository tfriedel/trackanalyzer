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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SpectrumAnalyser {
	protected int bins;
	protected int hopSize;
	protected int frameRate;
	protected final Lock analyserMutex;	
	public SpectrumAnalyser(int f, Parameters params){
	    bins = params.getOctaves() * params.getBpo();
    	hopSize = params.getHopSize();
	    frameRate = f;
		analyserMutex = new ReentrantLock();
	} 	
	abstract public Chromagram chromagram(AudioData data) throws Exception;
	
}
