TrackAnalyzer is a java based command line tool that estimates the musical key
and bpm of digital recordings, to aid DJs in harmonic mixing. The key
analyzer is a port of KeyFinder by Ibrahim Sha'ath (www.ibrahimshaath.co.uk/keyfinder).
The bpm component is basically a modified version of Simon Dixon's BeatRoot.
You also need these libraries:
JAudioTagger - used for reading/writing tags of audio files
jTransforms - fast fourier transforms
Jave - ffmpeg wrapper
Apache Math
JCommander - command line parsing

You can use TrackAnalyzer like this:
java -jar TrackAnalyzer.jar *.mp3 -w -o results.txt

BPM and key information will be calculated and written to the tags
(KEY_START and BPM fields).

A current build of TrackAnalyzer can be downloaded at
https://dl.dropbox.com/u/367262/TrackAnalyzer.zip

TrackAnalyzer is licensed under the GPL, see gpl.txt.

Copyright 2012 Thomas Friedel.
