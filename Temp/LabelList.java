package harpoon.Temp;

/**
 * A <code>LabelList</code> is a simple singly-linked list of
 * <code>Label</code>s.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LabelList.java,v 1.3.2.3 1999-08-04 04:34:12 cananian Exp $
 */
public final class LabelList {
    /* The head of the list. */
   public final Label head;
    /* The tail of the list. */
   public final LabelList tail;
    /* Constructor. */
   public LabelList(Label head, LabelList tail)
   { this.head=head; this.tail=tail; }
}

