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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// An implementation of the Map interface, backed by an ArrayList, which
//  preserves the elements in the order that they are added to the map.
//  Operations will take linear rather than constant time (as for the efficient
//  implementations of Map). Operations are not synchronized; caveat programmer!
// Used by class Parameters
// Updated to use generics; demonstrates that generics do not necessarily make
//  programs more readable, simple, safe, etc.
class ArrayMap implements Map<String,Object> {

	protected ArrayList<Entry> entries;

	protected class Entry implements Map.Entry<String,Object>,
									 Comparable<Object> {
		protected String key;
		protected Object value;
		protected Entry(String k, Object v) {
			key = k;
			value = v;
		} // constructor
		public boolean equals(Object o) {
			return (o instanceof Entry) && key.equals(((Entry)o).key) &&
											value.equals(((Entry)o).value);
		} // equals()
		public String getKey() { return key; }
		public Object getValue() { return value; }
		public Object setValue(Object newValue) {
			Object oldValue = value;
			value = newValue;
			return oldValue;
		} // setValue()
		public int hashCode() {
			return (key==null? 0 : key.hashCode()) ^
				   (value==null? 0 : value.hashCode());
		} // hashCode()
		public int compareTo(Object o) {
			return key.compareTo(((Entry)o).key);
		} // compareTo()
	} // inner class Entry

	public ArrayMap() { entries = new ArrayList<Entry>(); }//default constructor
	public ArrayMap(Map<String,Object> m) { this(); putAll(m); }		// copy constructor

	// Returns the index of an entry, given its key, or -1 if it is not in map.
	//  Note that ArrayList.indexOf() can't be used, because it doesn't call
	//  ArrayMap$Entry.equals()  [bug?? or does it call key.equals(entry)??]
	public int indexOf(String key) {
		for (int i = 0; i < size(); i++)
			if (key.equals(entries.get(i).key))
				return i;
		return -1;
	} // indexOf()

	// Returns the map entry at the given index
	public Entry getEntry(int i) { return entries.get(i); }

	// Removes all mappings from this map (optional operation).
	public void clear() { entries.clear(); }

	// Returns true if this map contains a mapping for the specified key.
    public boolean containsKey(Object key) {
		return indexOf((String)key) >= 0;
	} // containsKey()

	// Returns true if this map maps one or more keys to the specified value.
	public boolean containsValue(Object value) {
		for (int i = 0; i < size(); i++)
			if (value.equals(entries.get(i).value))
				return true;
		return false;
	} // containsValue()

	// Returns a set view of the mappings contained in this map.
	public Set<Map.Entry<String,Object>> entrySet() {
		TreeSet<Map.Entry<String,Object>> s =
					new TreeSet<Map.Entry<String,Object>>();
		for (int i = 0; i < size(); i++)
			s.add(entries.get(i));
		return s;
	} // entrySet()

	// Compares the specified object with this map for equality.
	public boolean equals(Object o) { return (o == this); }
	
	// Returns the value to which this map maps the specified key.
	public Object get(Object key) {
		int i = indexOf((String)key);
		if (i == -1)
			return null;
		return entries.get(i).value;
	} // get()

	// Returns the hash code value for this map.
	public int hashCode() {
		int h = 0;
		for (int i = 0; i < size(); i++)
			 h ^= entries.get(i).hashCode();
		return h;
	} // hashCode()
	
	// Returns true if this map contains no key-value mappings.
	public boolean isEmpty() { return entries.isEmpty(); }
	
	// Returns a set view of the keys contained in this map.
	public Set<String> keySet() {
		TreeSet<String> s = new TreeSet<String>();
		for (int i = 0; i < size(); i++)
			s.add(entries.get(i).key);
		return s;
	} // keySet()

	// Associates the specified value with the specified key in this map
	public Object put(String key, Object value) {
		int i = indexOf(key);
		if (i < 0) {
			entries.add(new Entry(key, value));
			return null;
		} else
			return entries.get(i).setValue(value);
	} // put()

	// Copies all of the mappings from the specified map to this map
	public void putAll(Map m) {
		// The following warning seems to be unavoidable:
		// warning: [unchecked] unchecked conversion
		Map<String,Object> m1 = (Map<String,Object>)m;
		for (Map.Entry<String,Object> me : m1.entrySet()) {
			put(me.getKey(), me.getValue());
		}
	} // putAll()

	// Removes the mapping for this key from this map if present
	public Object remove(Object key) {
		int i = indexOf((String)key);
		if (i < 0)
			return null;
		return entries.remove(i);
	} // remove()

	// Returns the number of key-value mappings in this map.
	public int size() { return entries.size(); }

	// Returns a collection view of the values contained in this map.
	public Collection<Object> values() {
		ArrayList<Object> s = new ArrayList<Object>();
		for (int i = 0; i < size(); i++)
			s.add(entries.get(i).value);
		return s;
	} // values()

} // class ArrayMap
