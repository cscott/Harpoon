package harpoon.Temp;

/**
 * A <code>LabelList</code> is a simple singly-linked list of
 * <code>Label</code>s.
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

