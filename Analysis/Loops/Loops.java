// Loops.java, created Wed Jun 13 17:38:43 1998 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.Loops;

import harpoon.Util.WorkSet;
import harpoon.ClassFile.HCode;
import java.util.Set;
/**
 * <code>Loops</code> contains the interface to be implemented by objects
 * generating nested loops trees.
 *
 *
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Loops.java,v 1.1.2.6 2001-06-15 08:25:52 cananian Exp $
 */

public interface Loops {

    /** Returns entrances to the Loop.
     *  This is a <code>Set</code> of <code>HCodeElement</code>s.*/
    public Set loopEntrances();
    
    /** Returns backedges in the Loop.
     *  This is a <code>Set</code> of <code>HCodeEdge</code>s.*/
    public Set loopBackedges();

    /** Returns nodes that have edges exiting the loop.
     *  This is a <code>Set</code> of <code>HCodeEdge</code>s.*/
    public Set loopExits();
    
    /** Returns elements of this loops and all nested loop.
     *  This is a <code>Set</code> of <code>HCodeElement</code>s.*/
    public Set loopIncelements();
    
    /** Returns elements of this loop not in any nested loop.
     *  This is a <code>Set</code> of <code>HCodeElement</code>s.*/
    public Set loopExcelements();
    
    /** Returns a <code>Set</code> containing <code>Loops</code> that are nested.*/
    public Set nestedLoops();
    
    /** Returns the loop immediately nesting this loop.
     *  If this is the highest level loop, returns a null pointer.*/
    public Loops parentLoop();
}
