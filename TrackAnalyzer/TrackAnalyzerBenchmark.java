/**
 * ***********************************************************************
 *
 * Copyright 2012 Thomas Friedel
 *
 * This file is part of TrackAnalyzer.
 *
 * TrackAnalyzer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * TrackAnalyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * TrackAnalyzer. If not, see <http://www.gnu.org/licenses/>.
 *
 ************************************************************************
 */
package TrackAnalyzer;

import at.ofai.music.beatroot.BeatRoot;
import it.sauronsoftware.jave.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.*;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class TrackAnalyzerBenchmark {

	CommandLineArgsBenchmark c = new CommandLineArgsBenchmark();
	BufferedWriter writeListWriter;
	ArrayList<String> filenames = new ArrayList<String>();
	//public final KeyFinder k;
	public final Parameters p;
	String seed = "-1";

	TrackAnalyzerBenchmark(String[] args) throws Exception {

		JCommander jcommander = new JCommander(c, args);
		jcommander.setProgramName("TrackAnalyzerBenchmark");
		if ((c.filenames.size() == 0 && Utils.isEmpty(c.filelist)) || c.help) {
			jcommander.usage();
			System.exit(-1);
		}
		if (c.debug) {
			Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).setLevel(Level.ALL);
		} else {
			Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).setLevel(Level.WARNING);
		}
		// we have a list, read all the filenames in the list and 
		// collect them in 'filenames'
		if (!Utils.isEmpty(c.filelist)) {
			try {
				//use buffering, reading one line at a time
				//FileReader always assumes default encoding is OK!
				BufferedReader input = new BufferedReader(new FileReader(new File(c.filelist)));
				try {
					String line = null; //not declared within while loop
					/*
					 * readLine is a bit quirky :
					 * it returns the content of a line MINUS the newline.
					 * it returns null only for the END of the stream.
					 * it returns an empty String if two newlines appear in a row.
					 */
					while ((line = input.readLine()) != null) {
						filenames.add(line);
					}
				} finally {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
		}
		//filenames.addAll(c.filenames);
		filenames.add(c.filenames.get(0));
		
		if (!Utils.isEmpty(c.writeList)) {
			try {
				writeListWriter = new BufferedWriter(new FileWriter(c.writeList));
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		p = new Parameters();
		p.setHopSize(8192);

		p.setToneProfile(Parameters.tone_profile_t.values()[c.toneProfile]);
		System.out.println("Tone profile: " +p.getToneProfile());
		
		p.setDirectSkStretch((float)c.directSkStretch);
		System.out.println("DirectSkStretch: " +Double.toString(p.getDirectSkStretch()));
		
		p.setHopSize(c.hopSize);
		System.out.println("HopSize: " +Integer.toString(p.getHopSize()));
	
		p.setOctaves(c.octaves);
		System.out.println("Octaves: " +Integer.toString(p.getOctaves()));
		
		p.setTemporalWindow(Parameters.temporal_window_t.values()[c.temporalWindow]);
		System.out.println("Temporal Window: " +p.getTemporalWindow());
		
		p.setSegmentation(Parameters.segmentation_t.values()[c.segmentation]);
		System.out.println("Segmentation: " +p.getSegmentation());

		p.setSimilarityMeasure(Parameters.similarity_measure_t.values()[c.similarityMeasure]);
		System.out.println("Similarity Measure: " + p.getSimilarityMeasure());
		
		p.setDetunedBandWeight(c.detunedBandweight);
		System.out.println("Detuned bandweight: " +Float.toString(p.getDetunedBandWeight()));
		
		p.setHcdfGaussianSigma(c.hcdfGaussianSigma);
		System.out.println("Detuned bandweight: " +Float.toString(p.getHcdfGaussianSigma()));
		
		p.setTuningMethod(Parameters.tuning_method_t.values()[c.tuningMethod]);
		System.out.println("Tuning Method: " +p.getTuningMethod());
		seed = args[4];
	}

	/**
	 * this writes a line to a txt file with the result of the detection process
	 * for one file.
	 *
	 * @param filename the filename of the audio file we just processed
	 * @param key the result of the key detector (or "-")
	 * @param bpm the result of the bpm detector (or "-")
	 * @param wroteTags true if tags were written successfully
	 */
	public void logDetectionResult(String filename, String key, String bpm, boolean wroteTags) {
		if (!Utils.isEmpty(c.writeList)) {
			try {
				writeListWriter.write(filename + ";" + key + ";" + bpm + ";" + wroteTags);
				writeListWriter.newLine();
				writeListWriter.flush();
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * one worker thread for analyzing a track
	 */
	private final class WorkTrack implements Callable<Boolean> {

		String filename;

		WorkTrack(String filename) {
			this.filename = filename;
		}

		@Override
		public Boolean call() {
			return analyzeTrack(filename);
		}
	}

	/**
	 * runs key detector on
	 *
	 * @filename, optionally writes tags
	 * @param filename
	 * @return
	 */
	public boolean analyzeTrack(String filename) {
		KeyFinder k = new KeyFinder();
		String wavfilename = "";
		AudioData data = new AudioData();
		assert(filename.toLowerCase().endsWith(".wav"));
		File temp2 = new File(filename);
		wavfilename = temp2.getAbsolutePath();
		//decodeAudioFile(new File(filename), temp2);
		KeyDetectionResult r;
		try {
			synchronized (this) {
				data.loadFromAudioFile(temp2.getAbsolutePath());
			}
			if (c.duration != -1) {
				data.cutLength(c.duration);
			}
			r = k.findKey(data, p);
		} catch (Exception ex) {
			Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).log(Level.SEVERE, null, ex);
			logDetectionResult(filename, "-", "-", false);
			return false;
		}
		String new_filename = new File(filename).getName();
		String referenceKey = new_filename.substring(0, new_filename.indexOf("-"));
		System.out.println("found key: "+ Parameters.camelotKey(r.globalKeyEstimate));
		System.out.printf("Result for ParamILS: SAT, -1, -1, %s, %s",
				Double.toString(1-MirexScore.mirexScore(referenceKey, 
				Parameters.camelotKey(r.globalKeyEstimate))),
				seed
				);
		return true;
	}

	/**
	 * This is the main loop of the program. For every file in the filenames
	 * list, the file gets decoded and downsampled to a 4410 hz mono wav file.
	 * Then key and bpm detectors are run, the result is logged in a txt file
	 * and written to the tag if possible.
	 */
	public void run() throws ExecutionException {
		int nThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
		CompletionService<Boolean> pool;
		pool = new ExecutorCompletionService<Boolean>(threadPool);

		nextFile:
		for (String filename : filenames) {
			//new worker thread
			pool.submit(new WorkTrack(filename));
		}
		for (int i = 0; i < filenames.size(); i++) {
			Boolean result;
			//Compute the result
			try {
				result = pool.take().get();
			} catch (InterruptedException e) {
				Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).log(Level.SEVERE, null, e);
			}
		}
		threadPool.shutdown();
		if (!Utils.isEmpty(c.writeList)) {
			try {
				writeListWriter.close();
			} catch (IOException ex) {
				Logger.getLogger(TrackAnalyzerBenchmark.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {
		TrackAnalyzerBenchmark ta = new TrackAnalyzerBenchmark(args);
		ta.run();
	}

}
