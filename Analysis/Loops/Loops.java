//Loops.java
//Interface for loops

package harpoon.Analysis.Loops;

import harpoon.Util.WorkSet;
import harpoon.ClassFile.HCode;

/**     Loop interface defined here*/

public interface Loops {

        /** Returns entrances to the Loop
         *  This is a WorkSet of HCodeElements.*/
	public WorkSet Loopentrances();

        /** Returns backedges in the Loop
         *  This is a WorkSet of HCodeElements.*/
	public WorkSet Loopbackedges();

        /** Returns nodes that have edges exiting the loop
         *  This is a WorkSet of HCodeElements.*/
	public WorkSet Loopexits();

        /** Returns elements of this loops and all nested loop.
         *  This is a WorkSet of HCodeElements.*/
	public WorkSet LoopincElements();

        /** Returns elements of this loop not in any nested loop.
         *  This is a WorkSet of HCodeElements.*/
	public WorkSet LoopexcElements();

        /** Returns a WorkSet containing Loops that are nested.*/
	public WorkSet NestedLoops();

        /** Returns the loop immediately nesting this loop.
         *  If this is the highest level loop, returns a null pointer.*/
	public Loops ParentLoop();
}
