// StmList.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>StmList</code>s for singly-linked lists of <code>Stm</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: StmList.java,v 1.1.2.1 1999-01-14 05:55:00 cananian Exp $
 */
public final class StmList {
    /** The statement at this list entry. */
    public final Stm head;
    /** The next list entry. */
    public final StmList tail;
    /** List constructor. */
    public StmList(Stm head, StmList tail)
    { this.head=head; this.tail=tail; }
}



