//Loops.java
//Interface for loops

package harpoon.Analysis.Loops;

import harpoon.Util.WorkSet;
import harpoon.ClassFile.HCode;
import java.util.Set;


public interface Loops {

        /** Returns entrances to the Loop.
         *  This is a <code>Set</code> of <code>HCodeElements</code>.*/
	public Set Loopentrances();

        /** Returns backedges in the Loop.
         *  This is a <code>Set</code> of <code>HCodeElements</code>.*/
	public Set Loopbackedges();

        /** Returns nodes that have edges exiting the loop.
         *  This is a <code>Set</code> of <code>HCodeElements</code>.*/
	public Set Loopexits();

        /** Returns elements of this loops and all nested loop.
         *  This is a <code>Set</code> of <code>HCodeElements</code>.*/
	public Set LoopincElements();

        /** Returns elements of this loop not in any nested loop.
         *  This is a <code>Set</code> of <code>HCodeElements</code>.*/
	public Set LoopexcElements();

        /** Returns a <code>Set</code> containing <code>Loops</code> that are nested.*/
	public Set NestedLoops();

        /** Returns the loop immediately nesting this loop.
         *  If this is the highest level loop, returns a null pointer.*/
	public Loops ParentLoop();
}
