package TrackAnalyzer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgsBenchmark {
    @Parameter
    public List<String> filenames = new ArrayList<String>();
	
	@Parameter(names = "--duration", description = "cut track to length <duration> s for faster analysis")
	public int duration = -1;
	
	@Parameter(names = "-setToneProfile", description = "sets tone profile to one of 6")
	public int toneProfile = 4;
	
	@Parameter(names = "-setHopSize", description = "hopsize")
	public int hopSize = 8192;
	
	@Parameter(names = "-setOctaves", description = "octaves")
	public int octaves = 6;
	
	@Parameter(names = "-setTemporalWindow", description = "temporal window (0-2")
	public int temporalWindow = 0;

	@Parameter(names = "-setSegmentation", description = "segmentation (0,2)")
	public int segmentation = 0;
	
	@Parameter(names = "-setSimilarityMeasure", description = "similarity measure (0-1)")
	public int similarityMeasure = 0;
	
	@Parameter(names = "-setDetunedBandweight", description = "bandweight")
	public float detunedBandweight = 0.2f;


	@Parameter(names = "-setGaussianSigma", description = "gaussian sigma")
	public float hcdfGaussianSigma = (float) 8.0;
	
	
	@Parameter(names = "-setTuningMethod", description = "tuning method")
	public int tuningMethod = 0;
	
	@Parameter(names = "-setDirectSkStretch", description = "sets spectral kernal bandwidth (between zero and 4)")
	public double directSkStretch = 0.8;
	
	@Parameter(names = "-1", description = "nothing")
	public boolean nothing = false;
	
	@Parameter(names = "-l", description = "text file containing list of audio files")
	public String filelist = "";

	@Parameter(names = "-o", description = "write results to text file")
	public String writeList = "";
	
	@Parameter(names = {"--help","-h","-?"}, help = true)
	public boolean help;

	public static final String DEBUG = "-debug";
	@Parameter(names = DEBUG, description = "Used to debug TrackAnalyzer")
	public boolean debug = false;
}
