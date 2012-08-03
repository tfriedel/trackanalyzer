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

	@Parameter(names = "-o", description = "write results to text file")
	public String writeList = "";
	
	@Parameter(names = "-l", description = "text file containing list of audio files")
	public String filelist = "";

	@Parameter(names = {"--help","-h","-?"}, help = true)
	public boolean help;

	public static final String DEBUG = "-debug";
	@Parameter(names = DEBUG, description = "Used to debug TrackAnalyzer")
	public Boolean debug = Boolean.FALSE;
}
