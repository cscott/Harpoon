/*
 
 Class: ParseNode
 Author: Dan Roy
 Purpose: ParseNode is used to represent a parse production

*/

package MCC.IR;

import java.util.*;

public class ParseNode implements Walkable {

    private String label;
    private ParseNode parent;
    private ParseNodeVector children;
    private int line;

    //private SymbolTable st;

    public ParseNode(String label) {
	this.label = label;
	this.line = -1;
	this.parent = null;
	children = new ParseNodeVector();
    }

    public ParseNode ( String label, int line ) {
	this.label = label;
	this.line = line;
	this.parent = null;
	children = new ParseNodeVector();
    }

    public void setLabel( String label ) {
	this.label = label;
    }

    public String getLabel() {
	return label;
    }

    /*
    public void setSymbolTable(SymbolTable st) {
	if (st == null) {
	    throw new IRException("symboltable is null!");
	}
	this.st = st;
    }

    public SymbolTable getSymbolTable() {
	if (st == null) {
	    if (parent != null) {
		return parent.getSymbolTable();
	    } else {
		return null;
	    }
	} else {
	    return st;
	}
    }
    */

    public int getLine() {
	if (line >= 0) {
	    return line;
	} else {
	    if (parent != null) {
		return parent.getLine();
	    } else {
		return 0;
	    }
	}
    }

    public void setParent( ParseNode parent ) {
	this.parent = parent;
    }

    public ParseNode getParent() {
	return parent;
    }

    public ParseNode insertChild(ParseNode child) {
	if (child == null) {
	    throw new NullPointerException("Can't add null node to parse tree");
	}

	children.insertElementAt(child, 0);
	child.setParent(this);
	return child;
    }

    public ParseNode insertChild(String newlabel) {
	ParseNode child = new ParseNode(newlabel, -1);
	return insertChild(child);
    }

    public ParseNode addChild( ParseNode child ) {

	if (child == null) 
	    throw new NullPointerException("Can't add null node to parse tree");

	children.addElement (child);
	child.setParent(this);
	return child;
    }

    public ParseNode addChild( String newlabel ) {
	
	ParseNode child = new ParseNode(newlabel, -1);
	children.addElement(child);
	child.setParent(this);
	return child;
    }

    public ParseNode addChild (String newlabel, int line) {
	ParseNode child = new ParseNode(newlabel, line);
	children.addElement(child);
	child.setParent(this);
	return child;
    }

    public ParseNodeVector getChildren() {
	return children;
    }

    public ParseNode getChild (String label) {
	int i;
	ParseNode p;

	for (i = 0; i < children.size(); i++) {
	    p = children.elementAt(i);
	    if (p.getLabel().equals(label)) {
		return p;
	    }
	}

	return null;
    }

    public ParseNode getRoot() {
        return (parent == null) ? this : parent.getRoot();
    }

    public String getTerminal () {
	ParseNode pn = children.elementAt(0);
	if (pn == null) {
	    return null;
	} else {
	    return pn.getLabel();
	}
    }


    public ParseNodeVector getChildren(String label) {
	int i;
	ParseNodeVector v = new ParseNodeVector();

	for (i = 0; i < children.size(); i++) {
	    ParseNode pn = children.elementAt(i);
	    if (pn.getLabel().equals(label))
		v.addElement(pn);
	}

	return v;
    }

    public String getNodeName() {
	return label + " - " + getLine();
    }

    public int getNeighborCount() {
	return children.size();
    }

    public Object getNeighbor(int index) {
	return children.elementAt(index);
    }

    public String doIndent(int indent) {

	String output = new String();
        for(int i=0;i<indent;i++) output += " ";
	return output;
    }

    public String PPrint(int indent, boolean recursive) {

        String output = new String();

	if (children.size()==0) {
	    output += doIndent(indent) + "<" + label + "/>\n";
	} else {
	    output += doIndent(indent) + "<" + label + ">\n";
	    indent += 2;
	    
	    if (recursive) {
		for (int i = 0; i < children.size(); i++) {
		    Walkable w = (Walkable)children.elementAt(i);
		    output += w.PPrint(indent, true);
		}
	    } else {
		for (int i = 0; i < children.size(); i++) {
		    Walkable w = (Walkable)children.elementAt(i);
		    output += doIndent(indent) + "<" + w.getNodeName() + "/>\n";
		}	   
	    }
	    
	    indent -= 2;
	    output += doIndent(indent) + "</" + label + ">\n";
	}

	return output;	
    }

}

