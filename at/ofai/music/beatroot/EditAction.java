/*  BeatRoot: An interactive beat tracking system
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
	http://www.gnu.org/licenses/gpl.txt or write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package at.ofai.music.beatroot;

/** Implements undo and redo for most beat editing actions.
 *  EditActions make up a doubly linked list containing the
 *  sequence of changes made to the beat list in their order 
 *  of occurrence.  */
public class EditAction {
	
	/** The original position of a beat, before editing took place.
	 *  A negative value indicates the addition of a new beat. */
	public double from;
	
	/** The new position of a beat, after editing took place.
	 *  A negative value indicates the deletion of a beat. */
	public double to;
	
	/** The next edit action in order of occurrence */
	public EditAction next;
	
	/** The previous edit action in order of occurrence */
	public EditAction prev;
	
	/** The head of the list of edit actions, marked by a dummy EditAction object */
	private final static EditAction HEAD = new EditAction(-1, -1, null, null);
	
	/** The EditAction which would be undone by clicking on "Undo".  Usually this
	 *  is the tail of the list, unless a series of undos (possibly interspersed
	 *  with a smaller number of redos) has just taken place. */
	private static EditAction current = HEAD;
	
	/** The main panel of BeatRoot's GUI, which is called to perform undo/redo */
	private static BeatTrackDisplay display = null;
	
	/** A flag indicating whether debugging information should be printed. */
	public static boolean debug = false;

	/** Constructor:
	 * @param f The original beat time
	 * @param t The new beat time
	 * @param n The next EditAction
	 * @param p The previous EditAction
	 */
	private EditAction(double f, double t, EditAction n, EditAction p) {
		from = f;
		to = t;
		next = n;
		prev = p;
		if (p != null)
			p.next = this;
		if (n != null)
			n.prev = this;
	} // constructor

	/** Prints the queue of EditActions (for debugging) */
	private static void printAll() {
		System.out.println("REDO actions: ***************");
		for (EditAction ptr = current.next; ptr != null; ptr = ptr.next)
			ptr.print();
		System.out.println("UNDO actions: ***************");
		for (EditAction ptr = current; ptr != HEAD; ptr = ptr.prev)
			ptr.print();
	} // printAll()
	
	/** Prints a single EditAction (for debugging) */
	private void print() {
		if (from < 0)
			System.out.println("Add: " + to);
		else if (to < 0)
			System.out.println("Remove: " + from);
		else
			System.out.println("Move: " + from + " to " + to);
	} // print()
	
	/** Set up a hook to the GUI's data panel for performing undo/redo */
	public static void setDisplay(BeatTrackDisplay btd) {
		display = btd;
	} // setDisplay()
	
	/** Add a new EditAction to the list */
	public static void add(double from, double to) {
		current = new EditAction(from, to, null, current);
		if (debug)
			printAll();
	} // add()
	
	/** Clear the list of EditActions */
	public static void clear() {
		current = HEAD;
		HEAD.next = null;
	} // clear()
	
	/** Undo the last EditAction */
	public static void undo() {
		if (display == null)
			System.err.println("undo() failed: No callback object");
		else if (current != HEAD) {
			if (debug)
				current.print();
			if (current.to < 0)
				display.addBeat(current.from);
			else if (current.from < 0)
				display.removeBeat(current.to);
			else
				display.moveBeat(current.to, current.from);
			current = current.prev;
		}
	} // undo()

	/** Redo the last EditAction which was just undone */
	public static void redo() {
		if (display == null)
			System.err.println("redo() failed: No callback object");
		else if (current.next != null) {
			current = current.next;
			if (debug)
				current.print();
			if (current.to < 0)
				display.removeBeat(current.from);
			else if (current.from < 0)
				display.addBeat(current.to);
			else
				display.moveBeat(current.from, current.to);
		}
	} // redo()

} // class EditAction
