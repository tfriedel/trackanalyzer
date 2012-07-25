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

import TrackAnalyzer.Parameters.key_t;
import java.util.ArrayList;

public class KeyFinder {

	private SpectrumAnalyserFactory saFactory = new SpectrumAnalyserFactory();



	public KeyDetectionResult findKey(AudioData audio, Parameters params) throws Exception {
		KeyDetectionResult result = new KeyDetectionResult();
		// make audio stream monaural
		audio.reduceToMono();

		// get spectrum analyser
		SpectrumAnalyser sa = saFactory.getSpectrumAnalyser(audio.getFrameRate(), params);

		// run spectrum analysis
		Chromagram ch = sa.chromagram(audio);

		// reduce chromagram
		ch.reduceTuningBins(params);
		result.fullChromagram = new Chromagram(ch);
		ch.reduceToOneOctave(params);
		result.oneOctaveChromagram = new Chromagram(ch);

		// get harmonic change signal
		Segmentation segmenter = Segmentation.getSegmentation(params);
		result.harmonicChangeSignal = segmenter.getRateOfChange(ch, params);

		// get track segmentation
		ArrayList<Integer> segmentBoundaries = segmenter.getSegments(result.harmonicChangeSignal, params);
		segmentBoundaries.add(ch.getHops()); // sentinel

		// get key estimates for each segment
		KeyClassifier hc = new KeyClassifier(params);
		ArrayList<Float> keyWeights = new ArrayList<Float>(24); // TODO: not ideal using int cast of key_t enum
		for (int i = 0; i < 24; i++) {
			keyWeights.add(new Float(0));
		}

		for (int s = 0; s < segmentBoundaries.size() - 1; s++) {
			KeyDetectionSegment segment = new KeyDetectionSegment();
			segment.firstWindow = segmentBoundaries.get(s);
			segment.lastWindow = segmentBoundaries.get(s + 1) - 1;
			// collapse segment's time dimension, for a single chroma vector and a single energy value
			ArrayList<Float> segmentChroma = new ArrayList<Float>(ch.getBins());
			for (int i = 0; i < ch.getBins(); i++) {
				segmentChroma.add(new Float(0));
			}
			// for each relevant hop of the chromagram
			for (int hop = segment.firstWindow; hop <= segment.lastWindow; hop++) {
				// for each bin
				for (int bin = 0; bin < ch.getBins(); bin++) {
					float value = ch.getMagnitude(hop, bin);
					segmentChroma.set(bin, segmentChroma.get(bin) + value);
					segment.energy += value;
				}
			}
			segment.key = hc.classify(segmentChroma);
			if (segment.key != key_t.SILENCE) {
				keyWeights.set(segment.key.ordinal(), keyWeights.get(segment.key.ordinal()) + segment.energy);
			}
			result.segments.add(segment);
		}

		// get global key
		result.globalKeyEstimate = key_t.SILENCE;
		float mostCommonKeyWeight = (float) 0.0;
		for (key_t key : key_t.values()) {
			if (!key.equals(key_t.SILENCE)) {
				if (keyWeights.get(key.ordinal()) > mostCommonKeyWeight) {
					mostCommonKeyWeight = keyWeights.get(key.ordinal());
					result.globalKeyEstimate = key;
				}
			}

		}
		return result;
	}
}