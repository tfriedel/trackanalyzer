package TrackAnalyzer;

public class MirexScore {
	/**
	 * calculate mirex score between keys a and b (camelot notation)
	 *
	 *	MIREX score weightings
	 *	Key relation Score
	 *	Exact match (tonic) 1.0
	 *	Perfect Fifth (dominant) 0.5
	 *	Perfect fourth (subdominant) 0.5
	 *	Relative major/minor 0.3
	 *	Parallel major/minor 0.2
	 * @param camelotA - string of 2 or 3 characters containing camelot key, e.g. 8A
	 * @param camelotB - string of 2 or 3 characters containing camelot key, e.g. 8A
	 * @return 
	 */
	public static float mirexScore(String camelotA, String camelotB) {
		int a_nr, b_nr;
		try {
		a_nr = Integer.parseInt(camelotA.substring(0, camelotA.length()-1));
		b_nr = Integer.parseInt(camelotB.substring(0, camelotB.length()-1));
		} catch(NumberFormatException e) {
			return 0;
		}
		char a_gender = camelotA.charAt(camelotA.length()-1);
		char b_gender = camelotB.charAt(camelotB.length()-1);
		if (camelotA.equals(camelotB))
			return (float)1.0;
		else if (((a_nr - b_nr) % 12 == 1  ||  (b_nr - a_nr) % 12 == 1) &&
				  a_gender == b_gender)
			return (float)0.5;
		else if ( 
				(a_gender == 'A' && b_gender == 'B' && (a_nr + 3) % 12 == (b_nr % 12))
				||
				(a_gender == 'B' && b_gender == 'A' && (a_nr % 12 == (b_nr + 3) % 12))
				)
			return (float)0.2;
		else
			return 0;
	}
}
