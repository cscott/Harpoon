// IIR_Allocator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_Allocator</code> class dynamically
 * allocates an object of specified subtype.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Allocator.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Allocator extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ALLOCATOR
    //CONSTRUCTOR:
    /** The constructor initializes an allocated object. */
    public IIR_Allocator() { }
    //METHODS: 
    /** Type mark methods denote the subtype of the object to be allocated.*/
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    /** Type mark methods denote the subtype of the object to be allocated.*/
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _type_mark;
    IIR _value;
} // END class

