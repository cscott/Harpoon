// IIR_FloatingPointLiteral32.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_FloatingPointLiteral32</code> is a
 * floating point literal class capable of representing any literal
 * value within the range covered by an IEEE single precision
 * representation.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingPointLiteral32.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingPointLiteral32 extends IIR_Literal
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_FLOATING_POINT_LITERAL32
    
    
    //METHODS:  
    static IIR_FloatingPointLiteral32 get_value( float value)
    { return new IIR_FloatingPointLiteral32( value ); }
 
    public float get_value()
    { return _value; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_FloatingPointLiteral32(float value) {
	_value = value;
    }
    float _value;
} // END class

