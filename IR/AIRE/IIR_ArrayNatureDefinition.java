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
 * @version $Id: IIR_ArrayNatureDefinition.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ArrayNatureDefinition extends IIR_CompositeNatureDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ARRAY_NATURE_DEFINITION

    /** The constructor methods creates a valid array type definition. */
    public IIR_ArrayNatureDefinition() { }

    //METHODS:  
    public void set_index_subtype( IIR_ScalarTypeDefinition index_subtype)
    { _index_subtype = index_subtype; }
 
    public IIR_ScalarTypeDefinition get_index_subtype()
    { return index_subtype; }
 
    public void set_element_subtype(IIR_TypeDefinition element_subtype)
    { _element_subtype = element_subtype; }
 
    public IIR_NatureDefinition get_element_subtype()
    { return element_subtype; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ScalarTypeDefinition _index_subtype;
    IIR_TypeDefinition _element_subtype;
} // END class

