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

public class Parameters {

	/**
	 * @return a camelot key string representation of the key
	 */
	public static String camelotKey(Parameters.key_t key) {
		return Parameters.camelot_key[key.ordinal()];
	}

	private boolean offsetToC;
	private int hopSize;
	private int fftFrameSize;
	private int octaves;
	private int bps;
	private int arbitrarySegments;
	private int hcdfPeakPickingNeighbours;
	private int hcdfGaussianSize;
	private float hcdfGaussianSigma;
	private float stFreq;
	private float directSkStretch;
	private float detunedBandWeight;
	private temporal_window_t temporalWindow;
	private segmentation_t segmentation;
	private similarity_measure_t similarityMeasure;
	private tone_profile_t toneProfile;
	private tuning_method_t tuningMethod;
	private ArrayList<Float> binFreqs = new ArrayList<Float>();
	private ArrayList<Float> customToneProfile = new ArrayList<Float>();

	public enum key_t {

		A_MAJOR, A_MINOR,
		B_FLAT_MAJOR, B_FLAT_MINOR,
		B_MAJOR, B_MINOR,
		C_MAJOR, C_MINOR,
		D_FLAT_MAJOR, D_FLAT_MINOR,
		D_MAJOR, D_MINOR,
		E_FLAT_MAJOR, E_FLAT_MINOR,
		E_MAJOR, E_MINOR,
		F_MAJOR, F_MINOR,
		G_FLAT_MAJOR, G_FLAT_MINOR,
		G_MAJOR, G_MINOR,
		A_FLAT_MAJOR, A_FLAT_MINOR,
		SILENCE
	}

	public final static String camelot_key[] = {
		"11B","8A",
		"6B", "3A",
		"1B", "10A",
		"8B", "5A",
		"3B", "12A",
		"10B","7A",
		"5B", "2A",
		"12B","9A",
		"7B", "4A",
		"2B", "11A",
		"9B", "6A",
		"4B", "1A",
		"SILENCE"
	};

	public enum temporal_window_t {

		WINDOW_BLACKMAN,
		WINDOW_HANN,
		WINDOW_HAMMING
	}

	public enum segmentation_t {

		SEGMENTATION_NONE,
		SEGMENTATION_ARBITRARY,
		SEGMENTATION_COSINE,
		SEGMENTATION_HARTE
	}

	public enum similarity_measure_t {

		SIMILARITY_COSINE,
		SIMILARITY_CORRELATION
	}

	public enum tone_profile_t {

		TONE_PROFILE_SILENT,
		TONE_PROFILE_KRUMHANSL,
		TONE_PROFILE_TEMPERLEY,
		TONE_PROFILE_GOMEZ,
		TONE_PROFILE_SHAATH,
		TONE_PROFILE_CUSTOM
	}

	public enum tuning_method_t {

		TUNING_HARTE,
		TUNING_BIN_ADAPTIVE
	}

	public Parameters() {
		// defaults
		stFreq = (float) 27.5;
		offsetToC = true;
		octaves = 6;
		bps = 1;
		temporalWindow = temporal_window_t.WINDOW_BLACKMAN;
		fftFrameSize = 16384;
		hopSize = fftFrameSize / 4;
		directSkStretch = (float) 0.8;
		tuningMethod = tuning_method_t.TUNING_HARTE;
		detunedBandWeight = (float) 0.2;
		segmentation = segmentation_t.SEGMENTATION_NONE;
		hcdfGaussianSize = 35;
		hcdfGaussianSigma = (float) 8.0;
		hcdfPeakPickingNeighbours = 4;
		arbitrarySegments = 3;
		toneProfile = tone_profile_t.TONE_PROFILE_SHAATH;
		similarityMeasure = similarity_measure_t.SIMILARITY_COSINE;
		float[] custom = {
			1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 0, 1, // major
			1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0 // minor
		};
		customToneProfile = Utils.floatArrayAsList(custom);
		// and other prep
		generateBinFreqs();
	}

	public boolean equivalentForSpectralAnalysis(Parameters that) {
		if (temporalWindow != that.temporalWindow) {
			return false;
		}
		if (bps != that.bps) {
			return false;
		}
		if (stFreq != that.stFreq) {
			return false;
		}
		if (octaves != that.octaves) {
			return false;
		}
		if (offsetToC != that.offsetToC) {
			return false;
		}
		if (fftFrameSize != that.fftFrameSize) {
			return false;
		}
		if (directSkStretch != that.directSkStretch) {
			return false;
		}
		return true;
	}

	/**
	 * * getters **
	 */
	/**
	 * ************
	 */
	/**
	 * @return the offsetToC
	 */
	public boolean getOffsetToC() {
		return offsetToC;
	}

	/**
	 * @return the hopSize
	 */
	public int getHopSize() {
		return hopSize;
	}

	/**
	 * @return the fftFrameSize
	 */
	public int getFftFrameSize() {
		return fftFrameSize;
	}

	/**
	 * @return the octaves
	 */
	public int getOctaves() {
		return octaves;
	}

	/**
	 * @return the arbitrarySegments
	 */
	public int getArbitrarySegments() {
		return arbitrarySegments;
	}

	/**
	 * @return the hcdfPeakPickingNeighbours
	 */
	public int getHcdfPeakPickingNeighbours() {
		return hcdfPeakPickingNeighbours;
	}

	/**
	 * @return the hcdfGaussianSize
	 */
	public int getHcdfGaussianSize() {
		return hcdfGaussianSize;
	}

	/**
	 * @return the hcdfGaussianSigma
	 */
	public float getHcdfGaussianSigma() {
		return hcdfGaussianSigma;
	}

	/**
	 * @return the stFreq
	 */
	public float getStartingFreqA() {
		return stFreq;
	}

	/**
	 * @return the directSkStretch
	 */
	public float getDirectSkStretch() {
		return directSkStretch;
	}

	/**
	 * @return the detunedBandWeight
	 */
	public float getDetunedBandWeight() {
		return detunedBandWeight;
	}

	/**
	 * @return the temporalWindow
	 */
	public temporal_window_t getTemporalWindow() {
		return temporalWindow;
	}

	/**
	 * @return the segmentation
	 */
	public segmentation_t getSegmentation() {
		return segmentation;
	}

	/**
	 * @return the similarityMeasure
	 */
	public similarity_measure_t getSimilarityMeasure() {
		return similarityMeasure;
	}

	/**
	 * @return the toneProfile
	 */
	public tone_profile_t getToneProfile() {
		return toneProfile;
	}

	/**
	 * @return the tuningMethod
	 */
	public tuning_method_t getTuningMethod() {
		return tuningMethod;
	}

	public int getBpo() {
		return bps * 12;
	}

	public float getBinFreq(int n) throws Exception {
		int max = octaves * 12 * bps;
		if (n >= max) {
			String ss = "Cannot get out-of-bounds frequency index (" + n + "/" + max + ")";
			throw new Exception(ss);
		}
		return binFreqs.get(n).floatValue();
	}

	public float getLastFreq() {
		return binFreqs.get(binFreqs.size() - 1);
	}

	/**
	 * @return the customToneProfile
	 */
	public ArrayList<Float> getCustomToneProfile() {
		return customToneProfile;
	}

	/**
	 * * setters **
	 */
	/**
	 * ************
	 */
	/**
	 * @param offsetToC the offsetToC to set
	 */
	public void setOffsetToC(boolean offsetToC) {
		this.offsetToC = offsetToC;
		generateBinFreqs();
	}

	/**
	 * @param hopSize the hopSize to set
	 */
	public void setHopSize(int hopSize) throws Exception {
		if (hopSize < 1) {
			throw new Exception("Hop size must be > 0");
		}
		this.hopSize = hopSize;
	}

	/**
	 * @param fftFrameSize the fftFrameSize to set
	 */
	public void setFftFrameSize(int fftFrameSize) throws Exception {
		if (fftFrameSize < 1) {
			throw new Exception("FFT frame size must be > 0");
		}
		this.fftFrameSize = fftFrameSize;
	}

	/**
	 * @param octaves the octaves to set
	 */
	public void setOctaves(int octaves) throws Exception {
		if (octaves < 1) {
			throw new Exception("Octaves must be > 0");
		}
		this.octaves = octaves;
		generateBinFreqs();
	}

	/**
	 * @param bps the bps to set
	 */
	public void setBps(int bps) throws Exception {
		if (bps < 1) {
			throw new Exception("Bands per semitone must be > 0");
		}
		this.bps = bps;
		generateBinFreqs();
	}

	/**
	 * @param arbitrarySegments the arbitrarySegments to set
	 */
	public void setArbitrarySegments(int arbitrarySegments) throws Exception {
		if (arbitrarySegments < 1) {
			throw new Exception("Arbitrary segments must be > 0");
		}
		this.arbitrarySegments = arbitrarySegments;
	}

	/**
	 * @param hcdfPeakPickingNeighbours the hcdfPeakPickingNeighbours to set
	 */
	public void setHcdfPeakPickingNeighbours(int hcdfPeakPickingNeighbours) {
		this.hcdfPeakPickingNeighbours = hcdfPeakPickingNeighbours;
	}

	/**
	 * @param hcdfGaussianSize the hcdfGaussianSize to set
	 */
	public void setHcdfGaussianSize(int hcdfGaussianSize) throws Exception {
		if (hcdfGaussianSize < 1) {
			throw new Exception("Gaussian size must be > 0");
		}
		this.hcdfGaussianSize = hcdfGaussianSize;
	}

	/**
	 * @param hcdfGaussianSigma the hcdfGaussianSigma to set
	 */
	public void setHcdfGaussianSigma(float hcdfGaussianSigma) throws Exception {
		if (hcdfGaussianSigma < 1) {
			throw new Exception("Gaussian sigma must be > 0");
		}
		this.hcdfGaussianSigma = hcdfGaussianSigma;
	}

	/**
	 * @param stFreq the stFreq to set
	 */
	public void setStartingFreqA(float a) throws Exception {
		if (a < 27.5) {
			throw new Exception("Starting frequency must be >= 27.5 Hz");
		}
		if (a != 27.5 && a != 55.0 && a != 110.0 && a != 220.0
				&& a != 440.0 && a != 880.0 && a != 1760.0 && a != 3520.0) {
			throw new Exception("Starting frequency must be an A (2^n * 27.5 Hz)");
		}
		stFreq = a;
		generateBinFreqs();

	}

	/**
	 * @param directSkStretch the directSkStretch to set
	 */
	public void setDirectSkStretch(float directSkStretch) throws Exception {
		if (directSkStretch <= 0) {
			throw new Exception("Spectral kernel stretch must be > 0");
		}
		this.directSkStretch = directSkStretch;
	}

	/**
	 * @param detunedBandWeight the detunedBandWeight to set
	 */
	public void setDetunedBandWeight(float detunedBandWeight) throws Exception {
		if (detunedBandWeight < 0) {
			throw new Exception("Detuned band weight must be >= 0");
		}
		this.detunedBandWeight = detunedBandWeight;
	}

	/**
	 * @param temporalWindow the temporalWindow to set
	 */
	public void setTemporalWindow(temporal_window_t temporalWindow) {
		this.temporalWindow = temporalWindow;
	}

	/**
	 * @param segmentation the segmentation to set
	 */
	public void setSegmentation(segmentation_t segmentation) {
		this.segmentation = segmentation;
	}

	/**
	 * @param similarityMeasure the similarityMeasure to set
	 */
	public void setSimilarityMeasure(similarity_measure_t similarityMeasure) {
		this.similarityMeasure = similarityMeasure;
	}

	/**
	 * @param toneProfile the toneProfile to set
	 */
	public void setToneProfile(tone_profile_t toneProfile) {
		this.toneProfile = toneProfile;
	}

	/**
	 * @param tuningMethod the tuningMethod to set
	 */
	public void setTuningMethod(tuning_method_t tuningMethod) {
		this.tuningMethod = tuningMethod;
	}

	/**
	 * @param customToneProfile the customToneProfile to set
	 */
	public void setCustomToneProfile(ArrayList<Float> v) throws Exception {
		if (v.size() != 24) {
			throw new Exception("Custom tone profile must have 24 elements");
		}
		for (int i = 0; i < 24; i++) {
			if (v.get(i) < 0) {
				throw new Exception("Custom tone profile elements must be >= 0");
			}
		}
		// Exception handling for occasional problem on OSX Leopard.
		try {
			customToneProfile = v;
		} catch (Exception e) {
			throw new Exception("Unknown exception setting custom tone profile");
		}
	}

	private void generateBinFreqs() {
		int bpo = bps * 12;
		binFreqs.clear();
		float freqRatio = (float) Math.pow(2, 1.0 / bpo);
		float octFreq = stFreq;
		float binFreq;
		int concertPitchBin = bps / 2;
		for (int i = 0; i < octaves; i++) {
			binFreq = octFreq;
			// offset as required
			if (offsetToC) {
				binFreq *= Math.pow(freqRatio, 3);
			}
			// tune down for bins before first concert pitch bin (if bps > 1)
			for (int j = 0; j < concertPitchBin; j++) {
				binFreqs.add((float) (binFreq / Math.pow(freqRatio, concertPitchBin - j)));
			}
			// and tune all other bins
			for (int j = concertPitchBin; j < bpo; j++) {
				binFreqs.add(binFreq);
				binFreq *= freqRatio;
			}
			octFreq *= 2;
		}

	}
}
