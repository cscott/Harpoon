//Loops.java
//Interface for loops
package harpoon.Analysis.Loops;

import harpoon.Util.WorkSet;
import harpoon.ClassFile.HCode;


public interface Loops {
	public WorkSet Loopentries();
	public WorkSet Loopbackedges();
	public WorkSet Loopexits();
	public WorkSet LoopincElements();
	public WorkSet LoopexcElements();
	public WorkSet NestedLoops();
	public Loops ParentLoop();
}
