// IIR_LTFAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LTFAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LTFAttribute.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LTFAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LTF_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_LTFAttribute( ) { }
    //METHODS:  
    public void set_num(IIR num)
    { _num = num; }
 
    public IIR get_num()
    { return _num; }
 
    public void set_den(IIR den)
    { _den = den; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _num;
    IIR _den;
} // END class

