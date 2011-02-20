// LocPair.java, created Mon Sep 13 00:56:30 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Tools.Annotation.Lex;

/**
 * <code>LocPair</code> is a line number, character position pair
 * to denote a location in the input file.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LinePos.java,v 1.2 2002-02-25 21:08:26 cananian Exp $
 */
public class LinePos {
    /** Line number in the source file.  Starts at 1. */
    public final int line;
    /** Character position in the line.  Starts at 0. */
    public final int pos;
    /** Creates a <code>LocPair</code>. */
    public LinePos(int line, int pos) {
	this.line = line; this.pos = pos;
    }
    /** Human-readable representation. */
    public String toString() {
	return "line "+line+"; position "+pos;
    }
}
