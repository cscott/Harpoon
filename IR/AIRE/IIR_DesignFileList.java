// IIR_DesignFileList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_DesignFileList</code> class represents
 * ordered sets containing zero or more <code>IIR_DesignFile</code>s.
 * Within the predefined <code>IIR</code> data structures, 
 * <code>IIR_DesignFileList</code>s only serve as a global, static
 * data element from which all files within the IIR data structures
 * may eventually be reached.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignFileList.java,v 1.2 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignFileList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_DESIGN_FILE_LIST; }
    //CONSTRUCTOR:
    public IIR_DesignFileList() { }
    //METHODS:  
    public void prepend_element(IIR_DesignFile element)
    { super._prepend_element(element); }
    public void append_element(IIR_DesignFile element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_DesignFile existing_element,
			     IIR_DesignFile new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_DesignFile existing_element,
			      IIR_DesignFile new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_DesignFile existing_element)
    { return super._remove_element(existing_element); }
    public IIR_DesignFile 
	get_successor_element(IIR_DesignFile element)
    { return (IIR_DesignFile)super._get_successor_element(element); }
    public IIR_DesignFile 
	get_predecessor_element(IIR_DesignFile element)
    { return (IIR_DesignFile)super._get_predecessor_element(element); }
    public IIR_DesignFile get_first_element()
    { return (IIR_DesignFile)super._get_first_element(); }
    public IIR_DesignFile get_nth_element(int index)
    { return (IIR_DesignFile)super._get_nth_element(index); }
    public IIR_DesignFile get_last_element()
    { return (IIR_DesignFile)super._get_last_element(); }
    public int get_element_position(IIR_DesignFile element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

