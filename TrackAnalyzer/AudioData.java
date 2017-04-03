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

import java.io.File;
import java.util.ArrayList;

public class AudioData {

    private int channels = 0;
    private int frameRate = 0;
    private int sampleCount = 0; // number of total samples on all channels
    //private ArrayList<Double> samples = new ArrayList<Double>();
    private double[] samples = new double[0];



    public int getChannels() {
        return channels;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public double getSample(int n) throws Exception {
        if (n >= sampleCount) {
            String msg = "Cannot get out-of-bounds sample " + n + "/" + sampleCount + ")";
            throw new Exception(msg);
        }
        return samples[n];
    }

    public void setSample(int n, double x) throws Exception {
        if (n >= sampleCount) {
            String msg = "Cannot set out-of-bounds sample " + n + "/" + sampleCount + ")";
            throw new Exception(msg);
        }
        samples[n] = x;
    }

    public double[] getSamples() {
        return samples;
    }

    public void setChannels(int n) throws Exception {
        if (n < 1) {
            throw new Exception("Channels must be > 0");
        }
        channels = n;
    }

    public void setFrameRate(int n) throws Exception {
        if (n < 1) {
            throw new Exception("Frame rate must be > 0");
        }
        frameRate = n;
    }

    public void addToSampleCount(int newSamples) {
        //samples.ensureCapacity(sampleCount + newSamples);
		samples = new double[sampleCount + newSamples];
        //for (int i = 0; i < newSamples; i++) {
        //    samples.add(0.0);
        //}
        sampleCount += newSamples;
    }

    public void reduceToMono() {
        if (channels == 1) {
            return;
        }
        //ArrayList<Double> newStream = new ArrayList<Double>(sampleCount / channels);
        double[] newStream = new double[sampleCount / channels];
        for (int i = 0; i < sampleCount; i += channels) {
            double mono = 0.0;
            for (int j = 0; j < channels; j++) {
                mono += samples[i + j];
            }
            mono /= channels;
            newStream[i/channels]=mono;
        }
        samples = newStream;
        sampleCount /= channels;
        channels = 1;
    }

	/**
	 * cuts the audio file to a shorter version of length duration, starting
	 * at offset 60 seconds, or earlier if not possible at 60 sec. 
	 * @param duration 
	 */
	public void cutLength(int duration) {
		int bps = frameRate * channels;
		double[] new_samples = new double[duration * bps];
		int start_offset = 60;
		// if file too short to start at start_offset, start at length of track - duration
		start_offset = Math.min(start_offset*bps, sampleCount-duration*bps);
		if (start_offset < 0)
			start_offset = 0;
		if (duration * bps > sampleCount)
			return;
		else {
			System.arraycopy(samples, start_offset, new_samples, 0, duration*bps);
			samples = new_samples;
			sampleCount = duration * frameRate;
		}
	}
	
    public void writeWavFile(String filename) {
        try {
            String outputfilename = filename;
            // Create a wav file with the name specified as the first argument
            WavFile wavFile = WavFile.newWavFile(new File(outputfilename), getChannels(),
                    getSampleCount() / getChannels(), 16, getFrameRate());

            // Create a buffer of 100 frames
            //double[] buffer = Utils.doubleArrayListToPrimitive(getSamples());

            // Write the buffer
            wavFile.writeFrames(getSamples(), getSampleCount() / getChannels());

            // Close the wavFile
            wavFile.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    public void loadFromAudioFile(String pathName) throws Exception {
        try {
            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(new File(pathName));

            // Display information about the wav file
            //wavFile.display();

            // Get the number of audio channels in the wav file
            channels = wavFile.getNumChannels();
            sampleCount = (int) wavFile.getNumFrames() * channels;


            double[] buffer = new double[sampleCount];

            int framesRead;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            do {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, sampleCount/channels);

                // Loop through frames and look for minimum and maximum value
                for (int s = 0; s < framesRead * channels; s++) {
                    if (buffer[s] > max) {
                        max = buffer[s];
                    }
                    if (buffer[s] < min) {
                        min = buffer[s];
                    }
                }
            } while (framesRead != 0);

            //samples = Utils.doubleArrayAsList(buffer);
			samples = buffer;
            frameRate = (int) wavFile.getSampleRate();
            // Close the wavFile
            wavFile.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
