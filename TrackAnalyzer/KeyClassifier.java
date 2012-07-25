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
import TrackAnalyzer.Parameters.similarity_measure_t;
import java.util.ArrayList;

public class KeyClassifier {

	private ToneProfile major;
	private ToneProfile minor;
	private ToneProfile silence;
	private similarity_measure_t similarityMeasure;

	public KeyClassifier(Parameters params) {
		// Profiles
		major = new ToneProfile(params.getToneProfile(), true, params);
		minor = new ToneProfile(params.getToneProfile(), false, params);
		silence = new ToneProfile(Parameters.tone_profile_t.TONE_PROFILE_SILENT, true, params);
		similarityMeasure = params.getSimilarityMeasure();
	}

	public key_t classify(ArrayList<Float> chroma) {
		ArrayList<Float> scores = new ArrayList<Float>(24);
		for (int i= 0; i<24; i++)
			scores.add(new Float(0));
		float bestScore = (float) 0.0;
		if (similarityMeasure == Parameters.similarity_measure_t.SIMILARITY_CORRELATION) {
			float chromaMean = (float) 0.0;
			for (int i = 0; i < chroma.size(); i++) {
				chromaMean += chroma.get(i);
			}
			chromaMean /= chroma.size();
			for (int i = 0; i < 12; i++) { // for each pair of profiles
				float sc = major.correlation(chroma, chromaMean, i); // major
				scores.set(i * 2, sc);
				sc = minor.correlation(chroma, chromaMean, i); // minor
				scores.set((i * 2) + 1, sc);
			}
			bestScore = silence.correlation(chroma, chromaMean, 0);
		} else {
			// Cosine measure
			for (int i = 0; i < 12; i++) { // for each pair of profiles
				float sc = major.cosine(chroma, i); // major
				scores.set(i * 2, sc);
				sc = minor.cosine(chroma, i); // minor
				scores.set((i * 2) + 1, sc);
			}
			bestScore = silence.cosine(chroma, 0);
		}
		// find best match, starting with silence
		key_t bestMatch = key_t.SILENCE;
		for (key_t key : key_t.values()) {
			if (!key.equals(key_t.SILENCE)) {
				if (scores.get(key.ordinal()) > bestScore) {
					bestScore = scores.get(key.ordinal());
					bestMatch = key;
				}
			}
		}
		return bestMatch;

	}
}
