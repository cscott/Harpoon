// IIR_IfStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_IfStatement</code> provides for the optional,
 * selective execution of one or more sequential statement lists. Such
 * statements may appear anywhere sequential statements are allowed.
 * <p>
 * The IIR_IfStatement uses a chain of <code>IIR_Elsif</code> tuples to
 * contain the elsif parts of the if statement.  The <code>IIR_Elsif</code>
 * tuple combines a condition and a sequence of statements to execute if
 * the condition is true.  If the recursion does not encounter a TRUE.
 * The final else sequence of statements is the else_sequence in
 * <code>IIR_IfStatement</code>.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IfStatement.java,v 1.2 1998-10-10 09:21:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IfStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_IF_STATEMENT
    //CONSTRUCTOR:
    public IIR_IfStatement() { }
    //METHODS:  
    public void set_elsif(IIR_Elsif condition)
    { _elsif = condition; }
 
    public IIR_Elsif get_elsif()
    { return _elsif; }
 
    //MEMBERS:  
    public IIR_SequentialStatementList then_sequence;
    public IIR_SequentialStatementList else_sequence;

// PROTECTED:
    IIR_Elsif _elsif;
} // END class

