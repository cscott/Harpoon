// IIR_FloatingPointLiteral64.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FloatingPointLiteral64</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingPointLiteral64.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingPointLiteral64 extends IIR_Literal
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_FLOATING_POINT_LITERAL64
    
    
    //METHODS:  
    static IIR_FloatingPointLiteral64 get_value( double value)
    { return new IIR_FloatingPointLiteral64(value); }

    public double get_value() { return _value; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_FloatingPointLiteral64(double value) {
	_value = value;
    }
    double _value;
} // END class

