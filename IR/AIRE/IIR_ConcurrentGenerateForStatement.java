// IIR_ConcurrentGenerateForStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentGenerateForStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentGenerateForStatement.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentGenerateForStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONCURRENT_GENERATE_FOR_STATEMENT).
     * @return <code>IR_Kind.IR_CONCURRENT_GENERATE_FOR_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_GENERATE_FOR_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ConcurrentGenerateForStatement() { }
    //METHODS:  
    public void set_generate_parameter_specification(IIR_ConstantDeclaration generate_parameter_specification)
    { _generate_parameter_specification = generate_parameter_specification; }
 
    public IIR_ConstantDeclaration get_generate_parameter_specification()
    { return _generate_parameter_specification; }
 
    //MEMBERS:  
    public IIR_DeclarationList block_declarative_part;
    public IIR_ConcurrentStatementList concurrent_statement_part;

// PROTECTED:
    IIR_ConstantDeclaration _generate_parameter_specification;
} // END class

