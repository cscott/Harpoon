// ProfileParser.java, created Fri Nov  9 21:30:05 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.NoSuchClassException;
import harpoon.Util.Collections.AggregateMapFactory;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MapSet;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Default;
import harpoon.Util.ParseUtil;
import harpoon.Util.ParseUtil.BadLineException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
/**
 * The <code>ProfileParser</code> class parses the output produced
 * by <code>SizeCounters</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ProfileParser.java,v 1.1.2.4 2001-11-14 01:39:25 cananian Exp $
 */
class ProfileParser {
    // lines are in the format: 'mzf_savedbytes_<classname>: <number>'
    // the line 'MZF START CONSTANT <number>' indicates a new 'mostly-N'
    // section.

    
    /** Creates a <code>ProfileParser</code>. */
    public ProfileParser(Linker linker, String resourcePath) {
	try {
	    ParseUtil.readResource(resourcePath, new ResourceParser(linker));
	} catch (IOException ioex) {
	    throw new RuntimeException(ioex.toString());
	}
    }
    /** Returns a map from 'mostly values' (Integers) to how many
     *  bytes can be saved (Longs). */
    Map valueInfo(HField hf) {
	return Collections.unmodifiableMap
	    (((MapSet)results.getValues(hf)).asMap());
    }
    final MultiMap results = new GenericMultiMap
	(Factories.mapSetFactory(new AggregateMapFactory()));
    public String toString() { return results.toString(); }
    class ResourceParser implements ParseUtil.StringParser {
	final Linker linker;
	ResourceParser(Linker linker) { this.linker = linker; }
	int constant = 0; // mutable as we parse.
	public void parseString(String s) throws BadLineException {
	    // 'MZF START CONSTANT <number>' lines indicate new section.
	    if (s.startsWith("MZF START CONSTANT ")) {
		String number =
		    s.substring("MZF START CONSTANT ".length()).trim();
		try {
		    constant = Integer.parseInt(number);
		} catch (NumberFormatException e) {
		    throw new BadLineException("Not a number: "+number);
		}
		return;
	    }
	    // ignore any line which doesn't begin with 'mzf_savedbytes_'
	    if (!s.startsWith("mzf_savedbytes_")) return;
	    int colon = s.indexOf(':');
	    if (colon<0) return; // ignore lines with no colon.
	    if (!(colon+1<s.length()))
		throw new BadLineException("No number part.");
	    String fieldname = s.substring("mzf_savedbytes_".length(), colon);
	    String number = s.substring(colon+1).trim();
	    long val;
	    try {
		val = Long.parseLong(number);
	    } catch (NumberFormatException e) {
		throw new BadLineException("Not a number: "+number);
	    }
	    int under = fieldname.lastIndexOf('_');
	    if (under<0)
		throw new BadLineException("No field part: "+fieldname);
	    try {
		HField hf = parseField(fieldname);
		results.add(hf, Default.entry
			    (new Integer(constant), new Long(val)));
		// in perl terms: $results{$hf}{$constant}=$val;
	    } catch (BadLineException ble) {
		/* assume that this field has been removed. */
		System.err.println("WARNING: can't find "+fieldname);
	    }
	}
	// any thing will do here.  just something to initialize with.
	private String last_success = "java.lang.String.offset";
	HField parseField(String fieldname) throws BadLineException {
	    // start by assuming that all '_'s should be '.'s.
	    fieldname = fieldname.replace('_', '.');
	    // now we're going to convert the .'s back to underscores
	    // one-by-one to try to come up with a working fieldname.
	    HashSet tried = new HashSet();
	    LinkedList ll = new LinkedList(Collections.singleton(fieldname));
	    // efficiency hack: if normalized fieldname starts the same way
	    // as last_success, seed this search with the matching part
	    // of last_success. (try class, package, etc)
	    for (int dot=last_success.lastIndexOf('.'); dot>0;
		 dot=last_success.lastIndexOf('.' , dot-1)) {
		String piece = last_success.substring(0, dot);
		if (fieldname.startsWith(piece.replace('_','.'))) {
		    // this is the seed we'll start with.
		    String seed = piece + fieldname.substring(piece.length());
		    ll.addFirst(seed);
		    break;
		}
	    }
	    // okay, first in first out queue.
	    while (!ll.isEmpty()) {
		String candidate = (String) ll.removeFirst();
		// try it.
		HField hf = tryProper(candidate);
		if (hf!=null) {
		    // it's good!  cache this success.
		    last_success =
			hf.getDeclaringClass().getName()+"."+hf.getName();
		    return hf; // we're done!
		}
		// nope.  change one of the dots to a '_' and push
		// back on the list.
		StringBuffer sb = new StringBuffer(candidate);
		int dot=candidate.indexOf('.');
		while (dot>=0) {
		    // change this dot to an underscore.
		    sb.setCharAt(dot, '_');
		    String s = sb.toString();
		    // check whether this has been tried already.
		    if (tried.add(s))
			// hasn't been done.  add it to *end of* list.
			ll.addLast(s);
		    // okay, reset to an dot.
		    sb.setCharAt(dot, '.');
		    // advance to the next dot.
		    if (!(dot+1 < candidate.length())) break;
		    dot=candidate.indexOf('.', dot+1);
		}
	    }
	    // oh, no!  we ran out of candidates.  this must be a bad field.
	    fieldname = fieldname.replace('.', '_'); // convert back to _
	    throw new BadLineException("Field not found: "+fieldname);
	}
	HField tryProper(String properField) {
	    int dot = properField.lastIndexOf('.');
	    if (dot<0 || !(dot+1<properField.length())) return null;
	    String fieldPart = properField.substring(dot+1);
	    String classPart = properField.substring(0, dot);
	    try {
		HClass hc = linker.forName(classPart);
		HField hf = hc.getDeclaredField(fieldPart);
		return hf; // success!
	    } catch (NoSuchFieldError ex) {
		return null; // not a valid field.
	    } catch (NoSuchClassException ex) {
		return null; // not a valid class.
	    }
	}
    }
    ////////////////////////////////////////////////////
    // tester.
    public static void main(String[] args) throws IOException {
	System.out.println(new ProfileParser(Loader.systemLinker, args[0]));
    }
}
