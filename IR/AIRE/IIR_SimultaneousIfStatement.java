// IIR_SimultaneousIfStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousIfStatement</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousIfStatement.java,v 1.1 1998-10-10 07:53:44 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousIfStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMULTANEOUS_IF_STATEMENT
    //CONSTRUCTOR:
    public IIR_SimultaneousIfStatement(){}
    //METHODS:  
    public void set_condition(IIR condition){
       _condition = condition;
    }
 
    public IIR get_condition(){return _condition;}
 
    public void set_elsif(IIR_SimultaneousElsif elsif){
       _elsif = elsif; 
    }
 
    public IIR_SimultaneousElsif get_elsif(){return _elsif;}
 
    //MEMBERS:  
    IIR_SimultaneousStatementList then_statement_list;
    IIR_SimultaneousStatementList else_statement_list;

// PROTECTED:
    IIR _condition;
    IIR_SimultaneousElsif _elsif;
} // END class

