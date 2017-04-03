package TrackAnalyzer;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {
    @Parameter
    public List<String> filenames = new ArrayList<String>();
	
    @Parameter(names = "-w", description = "write to Tags")
	public boolean writeTags = false;

    @Parameter(names = "--hiquality", description = "don't downsample for bpm detection")
	public boolean hiQuality = false;
	
	@Parameter(names = "-o", description = "write results to text file")
	public String writeList = "";
	
	@Parameter(names = "--nobpm", description = "don't detect bpm")
	public boolean noBpm = false;
	
	@Parameter(names = "--duration", description = "cut track to length <duration> s for faster analysis")
	public int duration = -1;
	
	@Parameter(names = "-l", description = "text file containing list of audio files")
	public String filelist = "";

	@Parameter(names = {"--help","-h","-?"}, help = true)
	public boolean help;

	public static final String DEBUG = "-debug";
	@Parameter(names = DEBUG, description = "Used to debug TrackAnalyzer")
	public boolean debug = false;
}
