/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TrackAnalyzer;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * import com.sun.jna.NativeLong; import com.sun.jna.Pointer; / import
 * java.util.ArrayList; /* import fftw3.FFTW3Library; import
 * fftw3.FFTW3Library.fftw_plan; import java.nio.DoubleBuffer;
 */
/**
 *
 * @author Thomas
 */
/*
 * public class FftwAnalyser extends SpectrumAnalyser { private int
 * fftFrameSize; private FftPostProcessor pp; private Pointer fftInput; private
 * Pointer fftResult; private DoubleBuffer inbuf; private DoubleBuffer
 * resultbuf; private ArrayList<Float> window; public FFTW3Library fftw =
 * FFTW3Library.INSTANCE; private int sizeofDouble = 8; private final fftw_plan
 * fftPlan;
 *
 * public FftwAnalyser(int f, Parameters params) throws Exception { super(f,
 * params); fftFrameSize = params.getFftFrameSize(); pp =
 * FftPostProcessor.getFftPostProcessor(f, params); //fftInput =
 * (fftw_complex*)fftw.fftw_malloc(sizeof(fftw_complex)*fftFrameSize); fftInput
 * = fftw.fftw_malloc(new NativeLong(sizeofDouble*fftFrameSize)); //fftResult =
 * (fftw_complex*)fftw_malloc(sizeof(fftw_complex)*fftFrameSize); fftResult =
 * fftw.fftw_malloc(new NativeLong(sizeofDouble*fftFrameSize)); inbuf =
 * fftInput.getByteBuffer(0, sizeofDouble * fftFrameSize).asDoubleBuffer();
 * resultbuf = fftResult.getByteBuffer(0, sizeofDouble *
 * fftFrameSize).asDoubleBuffer(); // plan 1-dimensional DFT //fftPlan =
 * fftw_plan_dft_1d(fftFrameSize, fftInput, fftResult, FFTW_FORWARD,
 * FFTW_ESTIMATE); fftPlan = fftw.fftw_plan_dft_1d(fftFrameSize, inbuf,
 * resultbuf, FFTW3Library.FFTW_FORWARD, FFTW3Library.FFTW_ESTIMATE); // prep
 * temporal window function WindowFunction wf =
 * WindowFunction.getWindowFunction(params.getTemporalWindow()); window = new
 * ArrayList<Float>(fftFrameSize); for (int i=0; i<fftFrameSize; i++){
 * window.add( wf.window(i,fftFrameSize)); }
 *
 * }
 * public Chromagram chromagram(AudioData audio) { //@todo lock
 * //boost::mutex::scoped_lock lock(analyserMutex); Chromagram ch = new
 * Chromagram((audio.getSampleCount()/hopSize) + 1,bins); for (int i = 0; i <
 * audio.getSampleCount(); i += hopSize){ for (int j = 0; j < fftFrameSize;
 * j++){ if(i+j < audio.getSampleCount()) fftInput[j][0] = (float)
 * (audio.getSample(i+j) * window.get(j)); // real part, windowed else
 * fftInput[j][0] = (float)0.0; // zero-pad if no PCM data remaining
 * fftInput[j][1] = (float) 0.0; // zero out imaginary part }
 * fftw_execute(fftPlan); ArrayList<Float> cv = pp.chromaVector(fftResult); for
 * (int j=0; j<bins; j++) ch.setMagnitude(i/hopSize,j,cv.get(j)); } return ch;
 *
 * }
 *
 * }
 */
public class FftwAnalyser extends SpectrumAnalyser {

	private int fftFrameSize;
	private FftPostProcessor pp;
	private double[] fftInput;
	private double[] fftResult;
	private ArrayList<Float> window;
	private int sizeofDouble = 8;
	private final DoubleFFT_1D fft;

	public FftwAnalyser(int f, Parameters params) throws Exception {
		super(f, params);
		fftFrameSize = params.getFftFrameSize();
		fft = new DoubleFFT_1D(fftFrameSize);
		pp = FftPostProcessor.getFftPostProcessor(f, params);
		//fftInput = (fftw_complex*)fftw.fftw_malloc(sizeof(fftw_complex)*fftFrameSize);
		//fftInput = fftw.fftw_malloc(new NativeLong(sizeofDouble*fftFrameSize));
		fftInput = new double[fftFrameSize * 2]; //2 because these are complex values with real and img. part
		//fftResult = (fftw_complex*)fftw_malloc(sizeof(fftw_complex)*fftFrameSize);
		//fftResult = fftw.fftw_malloc(new NativeLong(sizeofDouble*fftFrameSize));
		// plan 1-dimensional DFT
		//fftPlan = fftw_plan_dft_1d(fftFrameSize, fftInput, fftResult, FFTW_FORWARD, FFTW_ESTIMATE);
		//fftPlan = fftw.fftw_plan_dft_1d(fftFrameSize, inbuf, resultbuf, FFTW3Library.FFTW_FORWARD, FFTW3Library.FFTW_ESTIMATE);	
		// prep temporal window function
		WindowFunction wf = WindowFunction.getWindowFunction(params.getTemporalWindow());
		window = new ArrayList<Float>(fftFrameSize);
		for (int i = 0; i < fftFrameSize; i++) {
			window.add(wf.window(i, fftFrameSize));
		}

	}

	@Override
	public Chromagram chromagram(AudioData audio) throws Exception {
		//@todo lock
		//boost::mutex::scoped_lock lock(analyserMutex);
		Chromagram ch = new Chromagram((audio.getSampleCount() / hopSize) + 1, bins);
		for (int i = 0; i < audio.getSampleCount(); i += hopSize) {
			for (int j = 0; j < fftFrameSize; j++) {
				if (i + j < audio.getSampleCount()) {
					fftInput[j * 2] = (double) (audio.getSample(i + j) * window.get(j)); // real part, windowed
				} else {
					fftInput[j * 2] = 0.0; // zero-pad if no PCM data remaining
				}
				fftInput[j * 2 + 1] = 0.0; // zero out imaginary part
			}
			fftResult = Arrays.copyOf(fftInput, fftInput.length);
			fft.complexForward(fftResult);
			ArrayList<Float> cv = pp.chromaVector(fftResult);

			for (int j = 0; j < bins; j++) {
				ch.setMagnitude(i / hopSize, j, cv.get(j));
			}
		}
		return ch;

	}
}