package harpoon.IR.Tree;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The <code>Data</code> codeview is an implementation of the 
 * <code>HData</code> interface which exposes an <code>HClass</code>'s 
 * static data.  Unlike other codeviews which are associated with particular
 * methods, the each <code>Data</code> codeview is associated with an
 * class.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: Data.java,v 1.1.2.1 1999-07-28 18:17:31 duncan Exp $
 */
class Data extends Code implements HData { 
    public static final String codename = "tree-data";

    private final EdgeInitializer  edgeInitializer;
    private final HClass           cls;
    
    public Data(HClass cls, Frame frame) { 
	super(cls.getMethod("<clinit>", new HClass[0]), null, frame);
	this.cls = cls;

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
	//                                                           //
	// Construct layout of static class data                     //
	//                                                           //
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//

	List       up     = new ArrayList();  // class data above the cls ptr
	List       down   = new ArrayList();  // class data below the cls ptr
	List       iList  = new ArrayList();  // list of class's interfaces
	LinkedList sfList = new LinkedList(); // list of class's static fields
	OffsetMap  offm   = frame.getOffsetMap();

	// Add interface list pointer
	Label iListPtr = new Label();
	add(offm.interfaceListOffset(cls), _DATA(iListPtr), up, down);
	
	// Add class tag
	add(offm.tagOffset(cls),
	    _DATA(new CONST(tf, null, offm.classTag())), up, down);
	    
	// FIX: need to lay out GC tables
	// FIX: need tables for reflection 

	// Construct null-terminated list of interfaces and
	// add all interface methods
	iList.add(iListPtr);
	HClass[] interfaces = cls.getInterfaces();
	for (int i=0; i<interfaces.length; i++) { 
	    HClass    iFace    = interfaces[i];
	    HMethod[] iMethods = iFace.getMethods();
	    iList.add(_DATA(offm.label(iFace)));  // Add to interface list
	    for (int j=0; j<iMethods.length; j++) { 
		add(offm.offset(iMethods[i]), 
		    _DATA(offm.label(cls.getMethod  // Point to class method
				     (iMethods[i].getName(),
				      iMethods[i].getParameterTypes()))),
		    up,
		    down);
	    }
	}
	    
	// Add display to list
	for (HClass sCls=cls; sCls!=null; sCls=cls.getSuperclass()) 
	    add(offm.displayOffset(sCls),_DATA(offm.label(sCls)),up,down);
	
	// Add class methods to list 
	HMethod[] methods = cls.getMethods();
	for (int i=0; i<methods.length; i++) 
	    add(offm.offset(methods[i]),_DATA(offm.label(methods[i])),up,down);

	HField[]  fields  = cls.getDeclaredFields();
	for (int i=0; i<fields.length; i++) { 
	    HField field = fields[i];
	    if (field.isStatic()) { 
		if (field.getType().isPrimitive()) {
		    sfList.addLast(offm.label(field));
		    sfList.addLast(_DATA(new CONST(tf, null, 0)));
		}
		else { 
		    sfList.addFirst(_DATA(new CONST(tf, null, 0)));
		    sfList.addFirst(offm.label(field));
		}
	    }
	}
	
	List upR = new ArrayList(); // Reverse the upward list
	for (int i=0; i<up.size(); i++) upR.add(up.size()-i, up.get(i));
	
	// At last, assign the root element
	this.tree = new SEQ
	    (tf, null, 
	     Stm.toStm(upR),
	     new SEQ
	     (tf, null, 
	      Stm.toStm(down),
	      new SEQ
	      (tf, null, 
	       Stm.toStm(sfList),
	       Stm.toStm(iList))));
	
	// Compute the edges of this HData
	(this.edgeInitializer = new EdgeInitializer()).computeEdges();
    }
    
    // Copy constructor, should only be used by the clone() method 
    private Data(HClass cls, Tree tree, Frame frame) { 
	super(cls.getMethod("<clinit>", new HClass[0]), tree, frame);
	this.cls = cls;
	final CloningTempMap ctm = 
	    new CloningTempMap
	    (tree.getFactory().tempFactory(), this.tf.tempFactory());
	this.tree = (Tree)Tree.clone(this.tf, ctm, tree);
	(this.edgeInitializer = new EdgeInitializer()).computeEdges();

	// Must update the temps in your frame when you clone the tree form
	// Failure to do this causes an inconsistency between the new temps
	// created for the new frame, and the frame's registers mapped
	// using ctm in Tree.clone(). 
	Temp[] oldTemps = tree.getFactory().getFrame().getAllRegisters();
	Temp[] newTemps = this.tf.getFrame().getAllRegisters();
	for (int i=0; i<oldTemps.length; i++) 
	    newTemps[i] = oldTemps[i]==null?null:ctm.tempMap(oldTemps[i]);
    }
    
    /** Clone this data representation. The clone has its own copy
     *  of the Tree */
    public HData clone(HClass cls) { 
	return new Data(cls, this.tree, this.frame);
    }

    /** Return the <code>HClass</code> that this data view belongs to */
    public HClass getHClass() { return this.cls; } 

    /** Return the name of this data view. */
    public String getName() { return codename; } 
    
    /** Returns <code>true</code>.  */
    public boolean isCanonical() { return true; } 

    /** 
     * Recomputes the control-flow graph exposed through this codeview
     * by the <code>HasEdges</code> interface of its elements.  
     * This method should be called whenever the tree structure of this
     * codeview is modified. 
     */
    public void recomputeEdges() { this.edgeInitializer.computeEdges(); }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                   Disallowed methods                     *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    /** Throws an <code>Error</code>.  Use 
     *  <code>clone(harpoon.ClassFile.HClass)</code> instead.  */
    public HCode clone(HMethod method, Frame frame) { 
	throw new Error("Use clone(harpoon.ClassFile.HClass) instead");
    }
    
    /** Throws an <code>Error</code>.  */
    public DList derivation(HCodeElement hce, Temp t) { 
	throw new Error("No derivation information for TreeData objects");
    }

    /** Throws an <code>Error</code>.  Static class data is not associated
     *  with a particular method.  */
    public HMethod getMethod() { 
	throw new Error("No method associated with TreeData objects");
    }

    /** Throws an <code>Error</code>. */
    public HClass typeMap(HCode hc, Temp t) { 
	throw new Error("No typemap information for TreeData objects");
    }

    /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                          *
     *                    Utility methods                       *
     *                                                          *
     *+++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    private void add(int index, Object elem, List up, List down) { 
	if (index<0) up.add(-index, elem);
	else         down.add(index, elem);
    }
  
    private DATA _DATA(Exp e) { return new DATA(tf, null, e); }

    private DATA _DATA(Label l) {return new DATA(tf,null,new NAME(tf,null,l));}
}

