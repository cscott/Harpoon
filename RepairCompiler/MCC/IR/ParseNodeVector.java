package MCC.IR;

import java.util.Vector;

public class ParseNodeVector {
    private Vector v;

    public ParseNodeVector() {
	v = new Vector();
    }

    public void addElement(ParseNode pn) {
        v.addElement(pn);
    }

    public void insertElementAt(ParseNode pn, int n) {
	v.insertElementAt(pn, n);
    }

    public ParseNode elementAt(int i) {
        return (ParseNode) v.elementAt(i);
    }

    public int size() {
        return v.size();
    }
}
