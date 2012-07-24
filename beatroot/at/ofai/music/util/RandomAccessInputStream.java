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
import java.io.*;

public class RandomAccessInputStream extends InputStream {

	protected RandomAccessFile r;
	protected long markPosition = 0;
	
	public RandomAccessInputStream(String name) throws FileNotFoundException {
		r = new RandomAccessFile(name, "r");
	} // constructor

	public RandomAccessInputStream(File f) throws FileNotFoundException {
		r = new RandomAccessFile(f, "r");
	} // constructor

    /** Returns the number of bytes that can be read (or skipped over) from
	 *  this input stream without blocking by the next caller of a method for
	 *  this input stream.
	 */
	public int available() throws IOException {
		long availableBytes = r.length() - r.getFilePointer();
		if (availableBytes > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else
			return (int)availableBytes;
	} // available()
	
	/** Closes this input stream and releases any system resources associated
	 *  with the stream.
	 */
	public void close() throws IOException {
		r.close();
	} // close()
	
	/** Marks the current position in this input stream.
	 *  Warning: Use mark() instead of mark(int).
	 *  IOExceptions are caught, because InputStream doesn't allow them to be
	 *  thrown. The exception is printed and the mark position invalidated.
	 *  @param readlimit Ignored
	 */
	public void mark(int readlimit) {
		try {
			mark();
		} catch (IOException e) {
			e.printStackTrace();
			markPosition = -1;
		}
	} // mark()

	/** Marks the current position in this input stream.
	 */
	public void mark() throws IOException {
		markPosition = r.getFilePointer();
	} // mark()

	/** This input stream supports the mark and reset methods.
	 *  @return true
	 */
	public boolean markSupported() {
		return true;
	} // markSupported()

	/** Reads the next byte of data from the input stream.
	 */
	public int read() throws IOException {
		return r.read();
	} // read()
	
	/** Reads some number of bytes from the input stream and stores them into
	 *  the buffer array b.
	 */
	public int read(byte[] b) throws IOException {
		return r.read(b);
	} // read()

	/** Reads up to len bytes of data from the input stream into an array of
	 *  bytes.
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		return r.read(b, off, len);
	} // read()
	
	/** Repositions this stream to the position at the time the mark method
	 *  was last called on this input stream. 
	 */
	public void reset() throws IOException {
		if (markPosition < 0)
			throw new IOException("reset(): invalid mark position");
		r.seek(markPosition);
	} // reset()
	
	/** Skips over and discards n bytes of data from this input stream.
	 */
	public long skip(long n) throws IOException {
		long pos = r.getFilePointer();
		r.seek(n + pos);
		return r.getFilePointer() - pos;
	} // skip()

	/** Seek to a position n bytes after the mark.
	 */
	public long seekFromMark(long n) throws IOException {
		r.seek(markPosition + n);
		return r.getFilePointer() - markPosition;
	} // seekFromMark()

} // class RandomAccessInputStream
