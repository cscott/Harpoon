// CompilerState.java, created Wed Apr  2 13:26:51 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Analysis.ClassHierarchy;

import java.util.Set;
import java.io.Serializable;

/**
 * <code>CompilerState</code> is an immutable tuple that encapsulates
 * the date required while compiling a program.  Ideally, each stage
 * of the compiler receives a <code>CompilerState</code>, does some
 * transformations and returns a new <code>CompilerState</code>
 * object.
 *
 * <p> Because this tuple has many fields, and their number is likely
 * to vary, the portable way of generating a new
 * <code>CompilerState</code> is by using one of the
 * &quot;change*&quot; methods.  This is also supposed to be very
 * convenient: most of the compiler stages will modify only a small
 * part of the compiler state (most usually, only a new code factory).
 * <code>new</code> should be used only for the very first
 * <code>CompilerState</code>.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CompilerState.java,v 1.2 2003-04-08 05:25:22 salcianu Exp $ */
public class CompilerState implements Cloneable, Serializable {
   
    /** Creates a <code>CompilerState</code>.
	
	@param main main method of the compiler program

	@param roots set of roots for the compiled program

	@param linker linker that loads the classes of the compiled program

       	@param classHierarchy class hierarchy for the compiled program
       	(optional, may be null)

	@param hcf code factory that produces the code for the methods
	from the compiled program

	@param frame backend details */
    public CompilerState(HMethod main, Set roots, Linker linker,
			 HCodeFactory hcf, ClassHierarchy classHierarchy,
			 Frame frame) {
	this.main   = main;
	this.roots  = roots;
	this.linker = linker;
	this.hcf    = hcf;
	this.classHierarchy = classHierarchy;
	this.frame  = frame;        
    }

    private HMethod main;
    private Set roots;
    private Linker linker;
    private HCodeFactory hcf;
    private ClassHierarchy classHierarchy;
    // TODO: [ALEX] It's unclear why frame should be here; the backend
    // should be only one compiler stage, instead of part of the
    // compiler state
    private Frame frame;

    /** @return main method of the compiled program */
    public HMethod getMain() { return main; }

    /** @return set of roots for the compiled program */
    public Set getRoots() { return roots; }

    /** @return linker that loads the classes of the compiled program */
    public Linker getLinker() { return linker; }

    /** @return code factory that provides the code of the methods
        from the compiled program. */
    public HCodeFactory getCodeFactory() { return hcf; }

    /** @return class hierarchy for the compiled program; */
    public ClassHierarchy getClassHierarchy() { return classHierarchy; }

    /** @return backend specific details for the compiled program */
    public Frame getFrame() { return frame; }


    // helper method used by the change* methods: returns a clone of
    // this object and takes care of the possible exception).  Using
    // this method in the implem. of change* methods protects them
    // from fields addition / deletion
    private CompilerState newCopy() {
	try {
	    return (CompilerState) this.clone();
	} catch (CloneNotSupportedException e) {
	    throw new Error("Should not happen");
	}
    }

    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the main method, which is now set to
        <code>main</code> */
    public CompilerState changeMain(HMethod main) {
	CompilerState newCS = this.newCopy();
	newCS.main = main;
	return newCS;
    }

    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the set of roots, which is now set to
        <code>roots</code> */
    public CompilerState changeSet(Set roots) {
	CompilerState newCS = this.newCopy();
	newCS.roots = roots;
	return newCS;
    }

    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the linker, which is now set to
        <code>linker</code> */
    public CompilerState changeLinker(Linker linker) {
	CompilerState newCS = this.newCopy();
	newCS.linker = linker;
	return newCS;
    }

    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the code factory, which is now set to
        <code>hcf</code> */
    public CompilerState changeCodeFactory(HCodeFactory hcf) {
	CompilerState newCS = this.newCopy();
	newCS.hcf = hcf;
	return newCS;
    }

    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the class hierarchy, which is now set to
        <code>classHierarchy</code> */
    public CompilerState changeCodeFactory(ClassHierarchy classHierarchy) {
	CompilerState newCS = this.newCopy();
	newCS.classHierarchy = classHierarchy;
	return newCS;
    }

    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the frame, which is now set to
        <code>frame</code> */
    public CompilerState changeFrame(Frame frame) {
	CompilerState newCS = this.newCopy();
	newCS.frame = frame;
	return newCS;
    }    
}
