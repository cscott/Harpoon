// Grapher.java, created Thu Nov 23 13:09:52 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Set;
/**
 * <code>Grapher</code> is an abstract interface for specifying the
 * graph properties of data structures so that generic graph algorithms
 * may be applied.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Grapher.java,v 1.2.2.1 2002-03-04 19:10:56 cananian Exp $
 */
public interface Grapher<N> {
    boolean isEdge(N from, N to);
    Set<N> succSet(N node);
    Set<N> predSet(N node);
}
