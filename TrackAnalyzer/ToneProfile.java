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

import TrackAnalyzer.Parameters.tone_profile_t;
import java.util.ArrayList;

public class ToneProfile {

	private Binode tonic;
	float profileMean;

	private void free() {
		Binode p = tonic;
		do {
			Binode zap = p;
			p = p.r;
			zap = null;
		} while (p != tonic);

	}

	;

	public ToneProfile(tone_profile_t whichProfile, boolean majorScale, Parameters params) {
		float p[] = new float[12];
		if (whichProfile == tone_profile_t.TONE_PROFILE_SILENT) {
			p[0] = 0;
			p[1] = 0;
			p[2] = 0;
			p[3] = 0;
			p[4] = 0;
			p[5] = 0;
			p[6] = 0;
			p[7] = 0;
			p[8] = 0;
			p[9] = 0;
			p[10] = 0;
			p[11] = 0;
		} else if (whichProfile == tone_profile_t.TONE_PROFILE_TEMPERLEY) {
			if (majorScale) {
				p[0] = (float) 5.0;
				p[1] = (float) 2.0;
				p[2] = (float) 23.5;
				p[3] = (float) 22.0;
				p[4] = (float) 24.5;
				p[5] = (float) 24.0;
				p[6] = (float) 22.0;
				p[7] = (float) 24.5;
				p[8] = (float) 22.0;
				p[9] = (float) 23.5;
				p[10] = (float) 21.5;
				p[11] = (float) 24.0;
			} else {
				p[0] = (float) 25.0;
				p[1] = (float) 22.0;
				p[2] = (float) 23.5;
				p[3] = (float) 24.5;
				p[4] = (float) 22.0;
				p[5] = (float) 24.0;
				p[6] = (float) 22.0;
				p[7] = (float) 24.5;
				p[8] = (float) 23.5;
				p[9] = (float) 22.0;
				p[10] = (float) 21.5;
				p[11] = (float) 24.0;
			}
		} else if (whichProfile == tone_profile_t.TONE_PROFILE_GOMEZ) {
			if (majorScale) {
				p[0] = (float) 0.82;
				p[1] = (float) 0.00;
				p[2] = (float) 0.55;
				p[3] = (float) 0.00;
				p[4] = (float) 0.53;
				p[5] = (float) 0.30;
				p[6] = (float) 0.08;
				p[7] = (float) 1.00;
				p[8] = (float) 0.00;
				p[9] = (float) 0.38;
				p[10] = (float) 0.00;
				p[11] = (float) 0.47;
			} else {
				p[0] = (float) 0.81;
				p[1] = (float) 0.00;
				p[2] = (float) 0.53;
				p[3] = (float) 0.54;
				p[4] = (float) 0.00;
				p[5] = (float) 0.27;
				p[6] = (float) 0.07;
				p[7] = (float) 1.00;
				p[8] = (float) 0.27;
				p[9] = (float) 0.07;
				p[10] = (float) 0.10;
				p[11] = (float) 0.36;
			}
		} else if (whichProfile == tone_profile_t.TONE_PROFILE_SHAATH) {
			if (majorScale) {
				p[0] = (float) 6.6;
				p[1] = (float) 2.0;
				p[2] = (float) 3.5;
				p[3] = (float) 2.3;
				p[4] = (float) 4.6;
				p[5] = (float) 4.0;
				p[6] = (float) 2.5;
				p[7] = (float) 5.2;
				p[8] = (float) 2.4;
				p[9] = (float) 3.7;
				p[10] = (float) 2.3;
				p[11] = (float) 3.4;
			} else {
				p[0] = (float) 6.5;
				p[1] = (float) 2.7;
				p[2] = (float) 3.5;
				p[3] = (float) 5.4;
				p[4] = (float) 2.6;
				p[5] = (float) 3.5;
				p[6] = (float) 2.5;
				p[7] = (float) 5.2;
				p[8] = (float) 4.0;
				p[9] = (float) 2.7;
				p[10] = (float) 4.3;
				p[11] = (float) 3.2;
			}
		} else if (whichProfile == tone_profile_t.TONE_PROFILE_KRUMHANSL) {
			if (majorScale) {
				p[0] = (float) 6.35;
				p[1] = (float) 2.23;
				p[2] = (float) 3.48;
				p[3] = (float) 2.33;
				p[4] = (float) 4.38;
				p[5] = (float) 4.09;
				p[6] = (float) 2.52;
				p[7] = (float) 5.19;
				p[8] = (float) 2.39;
				p[9] = (float) 3.66;
				p[10] = (float) 2.29;
				p[11] = (float) 2.88;
			} else {
				p[0] = (float) 6.33;
				p[1] = (float) 2.68;
				p[2] = (float) 3.52;
				p[3] = (float) 5.38;
				p[4] = (float) 2.60;
				p[5] = (float) 3.53;
				p[6] = (float) 2.54;
				p[7] = (float) 4.75;
				p[8] = (float) 3.98;
				p[9] = (float) 2.69;
				p[10] = (float) 3.34;
				p[11] = (float) 3.17;
			}
		} else { // Custom
			ArrayList<Float> ctp = params.getCustomToneProfile();
			if (majorScale) {
				for (int i = 0; i < 12; i++) {
					p[i] = (float) ctp.get(i);
				}
			} else {
				for (int i = 0; i < 12; i++) {
					p[i] = (float) ctp.get(i + 12);
				}
			}
		}

		// copy into doubly-linked circular list
		tonic = new Binode(p[0]);
		Binode q = tonic;
		for (int i = 1; i < 12; i++) {
			q.r = new Binode(p[i]);
			q.r.l = q;
			q = q.r;
		}
		q.r = tonic;
		tonic.l = q;
		// offset from A to C (3 semitones) if specified in Parameters
		if (params.getOffsetToC()) {
			for (int i = 0; i < 3; i++) {
				tonic = tonic.r;
			}
		}

		// get mean in preparation for correlation
		profileMean = (float) 0.0;
		for (int i = 0; i < 12; i++) {
			profileMean += (p[i] / 12.0);
		}

	}

	/*
	 * Determines cosine similarity between input vector and profile scale.
	 * input = array of 12 floats relating to an octave starting at A natural
	 * offset = which scale to test against; 0 = A, 1 = Bb, 2 = B, 3 = C etc
	 */
	public float cosine(ArrayList<Float> input, int offset) {
		// Rotate starting pointer left for offset. Each step shifts the position
		// of the tonic one step further right of the starting pointer (or one semitone up).
		Binode p = tonic;
		for (int i = 0; i < offset; i++) {
			p = p.l;
		}
		float intersection = (float) 0.0;
		float profileNorm = (float) 0.0;
		float inputNorm = (float) 0.0;
		for (int i = 0; i < 12; i++) {
			intersection += input.get(i) * p.n;
			profileNorm += Math.pow((p.n), 2);
			inputNorm += Math.pow((input.get(i)), 2);
			p = p.r;
		}
		if (profileNorm > 0 && inputNorm > 0) // divzero
		{
			return (float) (intersection / (Math.sqrt(profileNorm) * Math.sqrt(inputNorm)));
		} else {
			return 0;
		}

	}

	/*
	 * Krumhansl's correlation between input vector and profile scale. input =
	 * array of 12 floats relating to an octave starting at A natural offset =
	 * which scale to test against; 0 = A, 1 = Bb, 2 = B, 3 = C etc
	 */
	public float correlation(ArrayList<Float> input, float inputMean, int offset) {
		Binode p = tonic;
		for (int i = 0; i < offset; i++) {
			p = p.l;
		}
		float sumTop = (float) 0.0;
		float sumBottomLeft = (float) 0.0;
		float sumBottomRight = (float) 0.0;
		for (int i = 0; i < 12; i++) {
			float xMinusXBar = (float) (p.n - profileMean);
			float yMinusYBar = input.get(i) - inputMean;
			sumTop += xMinusXBar * yMinusYBar;
			sumBottomLeft += Math.pow(xMinusXBar, 2);
			sumBottomRight += Math.pow(yMinusYBar, 2);
			p = p.r;
		}
		if (sumBottomRight > 0 && sumBottomLeft > 0) // divzero
		{
			return (float) (sumTop / Math.sqrt(sumBottomLeft * sumBottomRight));
		} else {
			return 0;
		}

	}
}
