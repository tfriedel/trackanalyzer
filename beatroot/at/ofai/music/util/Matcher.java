/*
	Copyright (C) 2001, 2006 by Simon Dixon

	This program is free software; you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation; either version 2 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along
	with this program (the file gpl.txt); if not, download it from
	http://www.gnu.org/licenses/gpl.txt or write to the
	Free Software Foundation, Inc.,
	51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package at.ofai.music.util;

/**	A simple parser for Prolog-type notation, but only handling the subset of
 *	Prolog used in "match" files.
 */
public class Matcher {

	/** The unparsed part of the current line of text */
	protected String s;

	/** The constructor is initialised with the input line of text for parsing*/
	public Matcher(String data) { s = data; }

	/** Reinitialise the parser with a new line of input */
	public void set(String data) { s = data; }

	/** Return the unparsed part of the input line */
	public String get() { return s; }

	/** Returns true if there is input data remaining */
	public boolean hasData() {
		return (s != null) && (s.length() > 0);
	} // hasData()

	/** Matches a String with the unparsed input data.
	 *	If the complete String occurs at the beginning of the unparsed data,
	 *	the unparsed data is advanced to the end of the String; otherwise
	 *	the data is left unchanged.
	 *
	 *	@param m	the String to match
	 *	@return		true if m matches the beginning of the unparsed data
	 */
	public boolean matchString(String m) {
		if (s.startsWith(m)) {
			s = s.substring(m.length());
			return true;
		}
		return false;
	} // matchString()

	/**	Skips input up to and including the next instance of a given character.
	 *	It is an error for the character not to occur in the data.
	 *	@param c	the character to skip to
	 */
	public void skip(char c) {
		int index = s.indexOf(c);
		if (index >= 0)
			s = s.substring(index + 1);
		else
			throw new RuntimeException("Parse error in skip(), expecting " + c);
	} // skip()

	/**	Removes whitespace from the beginning and end of the line.
	 */
	public void trimSpace() {
		s = s.trim();
	} // trimSpace()

	/**	Returns and consumes the next character of unparsed data. */
	public char getChar() {
		char c = s.charAt(0);
		s = s.substring(1);
		return c;
	} // getChar()

	/** Returns and consumes an int value from the head of the unparsed data. */
	public int getInt() {
		int sz = 0;
		trimSpace();
		while ((sz < s.length()) && (Character.isDigit(s.charAt(sz)) ||
					((sz==0) && (s.charAt(sz) == '-'))))
			sz++;
		int val = Integer.parseInt(s.substring(0, sz));
		s = s.substring(sz);
		return val;
	} // getInt()

	/**	Returns and consumes a double value, with two limitations:
	 *	1) exponents are ignored  e.g. 5.4e-3 is read as 5.4;
	 *	2) a value terminated by a 2nd "." causes an Exception to be thrown
	 */
	public double getDouble() {
		int sz = 0;
		trimSpace();
		while ((sz < s.length()) && (Character.isDigit(s.charAt(sz)) ||
					((sz==0)&&(s.charAt(sz) == '-')) || (s.charAt(sz) == '.')))
			sz++;
		double val = Double.parseDouble(s.substring(0, sz));
		s = s.substring(sz);
		return val;
	} // getDouble()

	/** Returns and consumes a string terminated by the first comma,
	 *	parenthesis, bracket or brace. Equivalent to getString(false).
	 */
	public String getString() {
		return getString(false);
	} // getString()

	/**
	 *	Returns and consumes a string terminated by various punctuation symbols.
	 *	Terminators include: '(', '[', '{', ',', '}', ']' and ')'.
	 *	An Exception is thrown if no terminator is found.
	 *
	 *	@param extraPunctuation Specifies whether '-' and '.' are terminators
	 */
	public String getString(boolean extraPunctuation) {
		char[] stoppers = {'(','[','{',',','}',']',')','-','.'};
		int index1 = s.indexOf(stoppers[0]);
		for (int i = 1; i < stoppers.length - (extraPunctuation? 0:2); i++) {
			int index2 = s.indexOf(stoppers[i]);
			if (index1 >= 0) {
				if ((index2 >= 0) && (index1 > index2))
					index1 = index2;
			} else
				index1 = index2;
		}
		if (index1 < 0)
			throw new RuntimeException("getString(): no terminator: " + s);
		String val = s.substring(0, index1);
		s = s.substring(index1);
		return val;
	} // getString()

	/** Returns and consumes a comma-separated list of terms, surrounded by a
	 *	matching set of parentheses, brackets or braces.
	 *	The list may have any number of levels of recursion.
	 *	@return	The return value is a linked list of the terms
	 *			(which themselves may be lists or String values)
	 */
	public ListTerm getList() {
		if ("([{".indexOf(s.charAt(0)) >= 0)
			return new ListTerm(getChar());
		return null;
	} // getList()

	/**	Returns and consumes a Prolog-style predicate, consisting of a functor
	 *	followed by an optional list of arguments in parentheses.
	 */
	public Predicate getPredicate() {
		return new Predicate();
	} // getPredicate()

	class Predicate {

		String head;
		ListTerm args;

		protected Predicate() {
			head = getString(true);
			args = getList();
		}
		
		public Object arg(int index) {
			ListTerm t = args;
			for (int i = 0; i < index; i++)
				t = t.next;
			return t.term;
		} // arg

		public String toString() {
			return (args == null)? head: head + args;
		}

	} // inner class Predicate

	class ListTerm {

		Object term;
		ListTerm next;
		char opener, closer;

		protected ListTerm(char c) {
			opener = c;
			term = null;
			next = null;
			if (hasData()) {
				switch(s.charAt(0)) {
					case '(':
					case '[':
					case '{':
						term = new ListTerm(getChar());
						break;
					default:
						term = getString();
						break;
				}
			}
			if (hasData()) {
				closer = getChar();
				switch(closer) {
					case ')':
						if (opener == '(')
							return;
						break;
					case ']':
						if (opener == '[')
							return;
						break;
					case '}':
						if (opener == '{')
							return;
						break;
					case ',':
						next = new ListTerm(opener);
						return;
				}
			}
			throw new RuntimeException("Parse error in ListTerm(): " + s);
		} // constructor

		public String toString() {
			String s = "" + opener;
			for (ListTerm ptr = this; ptr != null; ptr = ptr.next)
				s += ptr.term.toString() + ptr.closer;
			return s;
		} // toString()

	} // inner class ListTerm

} // class Matcher

