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

public class Chromagram {

    private int hops;
    private int bins;
    private ArrayList<ArrayList<Float>> chromaData;

    private void tuningHarte(Parameters params) throws Exception {
        /*
         * This is quite involved, and it's only an approximation of Harte's
         * method based on his thesis rather than a port of his code, but it
         * works well for e.g. Strawberry Fields Forever and other recordings he
         * mentioned as being difficult from a tuning perspective.
         */
        int oct = params.getOctaves();
        int bps = (bins / oct) / 12;
        // find peaks; anything that's higher energy than the mean for this hop and higher energy than its neighbours.
        ArrayList<ArrayList<Float>> peakLocations = new ArrayList<ArrayList<Float>>();
        ArrayList<ArrayList<Float>> peakMagnitudes = new ArrayList<ArrayList<Float>>();
        for (int hop = 0; hop < hops; hop++) {
            // find mean magnitude for this hop
            float meanVal = 0;
            for (int bin = 0; bin < bins; bin++) {
                meanVal += chromaData.get(hop).get(bin);
            }
            meanVal /= bins;
            // find peak bins
            ArrayList<Integer> peakBins = new ArrayList<Integer>(bins);
            for (int bin = 1; bin < bins - 1; bin++) {
                float binVal = getMagnitude(hop, bin);
                // currently every peak over mean. Tried all peaks but accuracy dropped.
                if (binVal > meanVal && binVal > getMagnitude(hop, bin - 1) && binVal > getMagnitude(hop, bin + 1)) {
                    peakBins.add(bin);
                }
            }
            // quadratic interpolation to find a more precise peak position and magnitude.
            ArrayList<Float> peakLocationsRow = new ArrayList<Float>(peakBins.size());
            ArrayList<Float> peakMagnitudesRow = new ArrayList<Float>(peakBins.size());
            for (int peak = 0; peak < peakBins.size(); peak++) {
                float alpha = getMagnitude(hop, peakBins.get(peak) - 1);
                float beta = getMagnitude(hop, peakBins.get(peak));
                float gamma = getMagnitude(hop, peakBins.get(peak) + 1);
                float peakLocation = ((alpha - gamma) / (alpha - (2 * beta) + gamma)) / 2;
                float peakMagnitude = beta - ((1 / 4) * (alpha - gamma) * peakLocation);
                peakLocationsRow.add(peakBins.get(peak) + peakLocation);
                peakMagnitudesRow.add(peakMagnitude);
            }
            peakLocations.add(peakLocationsRow);
            peakMagnitudes.add(peakMagnitudesRow);
        }
        // determine tuning distribution of peaks. Centre bin = concert tuning.
        ArrayList<Float> peakTuningDistribution = new ArrayList<Float>(bps * 10);
        for (int i = 0; i < bps * 10; i++) {
            peakTuningDistribution.add(new Float(0.0));
        }
        for (int hop = 0; hop < hops; hop++) {
            for (int peak = 0; peak < peakLocations.get(hop).size(); peak++) {
                // @todo check if fmodf is equal to the usage of %
                float peakLocationMod = peakLocations.get(hop).get(peak) % (float) bps;
                peakLocationMod *= 10;
                int peakLocationInt = (int) (peakLocationMod + 0.5);
                peakLocationInt += 5;
                peakTuningDistribution.set(peakLocationInt % (bps * 10), (float) (peakTuningDistribution.get(peakLocationInt % (bps * 10)) + (peakMagnitudes.get(hop).get(peak) / 1000.0)));
            }
        }
        // now find the tuning peak; the subdivision of a semitone that most peaks are tuned to.
        float tuningMax = 0;
        int tuningPeak = -1;
        for (int i = 0; i < bps * 10; i++) {
            if (peakTuningDistribution.get(i) > tuningMax) {
                tuningMax = peakTuningDistribution.get(i);
                tuningPeak = i;
            }
        }
        // now discard (zero out, for ease) any peaks that sit >= 0.2 semitones (e.g. 6 bins for 3bps) away from the tuning peak.
        // figure out which tuning bins to keep
        ArrayList<Integer> binsToKeep = new ArrayList<Integer>();
        for (int i = (1 - (bps * 2)); i < bps * 2; i++) {
            binsToKeep.add((tuningPeak + i + (bps * 10)) % (bps * 10));
        }
        // and discard the others
        ArrayList<ArrayList<Float>> twelveBpoChroma = Utils.newFloatArrayList2D(hops, 12 * oct);
        for (int hop = 0; hop < hops; hop++) {
            for (int peak = 0; peak < peakLocations.get(hop).size(); peak++) {
                float peakLocationMod = peakLocations.get(hop).get(peak) % (float) bps;
                peakLocationMod *= 10;
                int peakLocationInt = (int) (peakLocationMod + 0.5);
                peakLocationInt += 5;
                boolean discardMe = true;
                for (int i = 0; i < binsToKeep.size(); i++) {
                    if (peakLocationInt == binsToKeep.get(i)) {
                        discardMe = false;
                    }
                }
                if (!discardMe) { // this is a valid peak for the tuned chromagram
                    int tunedPeakLocation = peakLocations.get(hop).get(peak).intValue();
                    tunedPeakLocation /= bps;
                    twelveBpoChroma.get(hop).set(tunedPeakLocation,
                            twelveBpoChroma.get(hop).get(tunedPeakLocation) + peakMagnitudes.get(hop).get(peak));
                }
            }
        }
        chromaData = twelveBpoChroma;
        bins = 12 * oct;

    }

    private void tuningBinAdaptive(Parameters params) {

        /*
         * This is designed to tune for each semitone bin rather than for the
         * whole recording; aimed at dance music with individually detuned
         * elements, rather than music that is internally consistent but off
         * concert pitch.
         */
        int oct = params.getOctaves();
        int bps = (bins / oct) / 12;
        ArrayList<ArrayList<Float>> twelveBpoChroma = Utils.newFloatArrayList2D(hops, 12 * oct);
        for (int st = 0; st < 12 * oct; st++) {
            ArrayList<Float> oneSemitoneChroma = new ArrayList<Float>(bps);
            for (int i = 0; i < bps; i++) {
                oneSemitoneChroma.add(new Float(0.0));
            }
            for (int h = 0; h < hops; h++) {
                for (int b = 0; b < bps; b++) {
                    oneSemitoneChroma.set(b, oneSemitoneChroma.get(b) + chromaData.get(h).get(st * bps + b));
                }
            }
            // determine highest energy tuning bin
            int whichBin = 0;
            float max = oneSemitoneChroma.get(0);
            for (int i = 1; i < bps; i++) {
                if (oneSemitoneChroma.get(i) > max) {
                    max = oneSemitoneChroma.get(i);
                    whichBin = i;
                }
            }
            for (int h = 0; h < hops; h++) {
                float weighted = (float) 0.0;
                for (int b = 0; b < bps; b++) {
                    weighted += (chromaData.get(h).get(st * bps + b) * (b == whichBin ? 1.0 : params.getDetunedBandWeight()));
                }
                twelveBpoChroma.get(h).set(st, weighted);
            }
        }
        chromaData = twelveBpoChroma;
        bins = 12 * oct;

    }

    Chromagram(Chromagram c) {
        hops = c.hops;
        bins = c.bins;
		chromaData = c.chromaData;
    }

    
    Chromagram(int h, int b) {
        hops = h;
        bins = b;
        chromaData = Utils.newFloatArrayList2D(hops, bins);
    }

    public void setMagnitude(int h, int b, float val) throws Exception {
        if (h >= hops) {
            String ss = "Cannot set magnitude of out-of-bounds hop (" + h + "/" + hops + ")";
            throw new Exception(ss);
        }
        if (b >= bins) {
            String ss = "Cannot set magnitude of out-of-bounds bin (" + b + "/" + bins + ")";
            throw new Exception(ss);
        }
        chromaData.get(h).set(b, new Float(val));
    }

    public float getMagnitude(int h, int b) throws Exception {
        if (h >= hops) {
            String ss = "Cannot get magnitude of out-of-bounds hop (" + h + "/" + hops + ")";
            throw new Exception(ss);
        }
        if (b >= bins) {
            String ss = "Cannot get magnitude of out-of-bounds bin (" + b + "/" + bins + ")";
            throw new Exception(ss);
        }
        return chromaData.get(h).get(b);
    }

    public int getHops() {
        return hops;
    }

    public int getBins() {
        return bins;
    }

    public void reduceToOneOctave(Parameters params) {
        int oct = params.getOctaves();
        int bpo = bins / oct;
        if (bpo == bins) {
            return;
        }
        ArrayList<ArrayList<Float>> oneOctaveChroma = Utils.newFloatArrayList2D(hops, bpo);
        for (int h = 0; h < hops; h++) {
            for (int b = 0; b < bpo; b++) {
                float singleBin = (float) 0.0;
                for (int o = 0; o < oct; o++) {
                    singleBin += chromaData.get(h).get(o * bpo + b);
                }
                oneOctaveChroma.get(h).set(b, singleBin / oct);
            }
        }
        chromaData = oneOctaveChroma;
        bins = bpo;

    }

    public void reduceTuningBins(Parameters params) throws Exception {
        int oct = params.getOctaves();
        if (bins == 12 * oct) {
            return;
        }
        if (params.getTuningMethod() == Parameters.tuning_method_t.TUNING_BIN_ADAPTIVE) {
            tuningBinAdaptive(params);
        } else {
            tuningHarte(params);
        }
    }
}
