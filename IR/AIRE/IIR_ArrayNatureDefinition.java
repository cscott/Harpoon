// IIR_ArrayNatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ArrayNatureDefinition</code> class represents
 * natures containing zero or more instances of the same elemental subtype.
 * Multi-dimensional arrays and arrays include record (or other elements)
 * may be represented using composite array elements.  For example, the
 * first dimension of a two-dimensional array would have an element
 * which is itself an array.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ArrayNatureDefinition.java,v 1.4 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ArrayNatureDefinition extends IIR_CompositeNatureDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ARRAY_NATURE_DEFINITION; }

    /** The constructor methods creates a valid array type definition. */
    public IIR_ArrayNatureDefinition() { }

    //METHODS:  
    public void set_index_subtype( IIR_ScalarTypeDefinition index_subtype)
    { _index_subtype = index_subtype; }
 
    public IIR_ScalarTypeDefinition get_index_subtype()
    { return _index_subtype; }
 
    public void set_element_subtype(IIR_NatureDefinition element_subtype)
    { _element_subtype = element_subtype; }
 
    public IIR_NatureDefinition get_element_subtype()
    { return _element_subtype; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ScalarTypeDefinition _index_subtype;
    IIR_NatureDefinition _element_subtype;
} // END class

