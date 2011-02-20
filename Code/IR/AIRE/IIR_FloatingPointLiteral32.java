// IIR_FloatingPointLiteral32.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_FloatingPointLiteral32</code> is a
 * floating point literal class capable of representing any literal
 * value within the range covered by an IEEE single precision
 * representation.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingPointLiteral32.java,v 1.4 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingPointLiteral32 extends IIR_Literal
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FLOATING_POINT_LITERAL32).
     * @return <code>IR_Kind.IR_FLOATING_POINT_LITERAL32</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FLOATING_POINT_LITERAL32; }
    
    
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

