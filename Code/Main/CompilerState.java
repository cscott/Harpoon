// CompilerState.java, created Wed Apr  2 13:26:51 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Util.Collections.PersistentMap;

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
 * Also, <code>CompilerState.EMPTY_STATE</code> contains the initial,
 * empty compiler state (initial meaning &quot;at the beginning of the
 * compiler&quot;)
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CompilerState.java,v 1.6 2003-07-09 21:11:16 cananian Exp $ */
public class CompilerState implements Cloneable, Serializable {
   
    private CompilerState() { 
	attribs = new PersistentMap/*<String,Object>*/();
    }

    public static CompilerState EMPTY_STATE = new CompilerState();

    private HMethod main;
    private Set roots;
    private Linker linker;
    private HCodeFactory hcf;
    private ClassHierarchy classHierarchy;
    // TODO: [ALEX] It's unclear why frame should be here; the backend
    // should be only one compiler stage, instead of part of the
    // compiler state
    // CSA: because some stages depend indirectly on which backend is
    //      selected?
    private Frame frame;
    // other attributes (that don't occcur that often and don't
    // deserve separate fields)
    private PersistentMap<String,Object> attribs;

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


    /** @return (persistent) map from attribute names to attribute
        values */
    public PersistentMap<String,Object> getAttributes() { return attribs; }


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
    public CompilerState changeRoots(Set roots) {
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
    public CompilerState changeClassHierarchy(ClassHierarchy classHierarchy) {
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

    
    /** @return identical copy of <code>this</code> compiler state,
        with the exception of the attribute map, which is now set to
        <code>attribs</code> */
    public CompilerState changeAttributes(PersistentMap<String,Object> attribs) {
	CompilerState newCS = this.newCopy();
	newCS.attribs = attribs;
	return newCS;
    }
}
