/*
 
 Class: ParseNodeDOTVisitor
 Author: Dan Roy
 Purpose: Traverses a ParseNode tree and generates a DOT file that represents the parse
          tree.

*/

package MCC.IR;

import java.util.*;

public class ParseNodeDOTVisitor {
    
    java.io.PrintWriter output;
    int tokennumber;
    int color;

    private ParseNodeDOTVisitor(java.io.OutputStream output) {
        tokennumber = 0;
        color = 0;
        this.output = new java.io.PrintWriter(output, true);
    }

    private String getNewID(String name) {
        tokennumber = tokennumber + 1;
        return new String (name+tokennumber);
    }

    public static void visit(java.io.OutputStream output, ParseNode root) {
        ParseNodeDOTVisitor visitor = new ParseNodeDOTVisitor(output);
        visitor.make(root);
    }
    
    private void make(ParseNode root) {
        output.println("digraph dotvisitor {");
        output.println("\tsize=\"7, 10\";");
        traverse(root, getNewID("root"));
        output.println("}\n");
    }

    private String newColor() {


        if (color == 0) {
            color++;
            return new String("red");
        } else if (color == 1) {
            color++;
            return new String("green");
        } else {
            color = 0;
            return new String("blue");            
        }
    }

    private void traverse(ParseNode node, String nodeid) {
        output.println("\t" + nodeid + " [label=\"" + node.getLabel() + "\",shape=box];");       
        ParseNodeVector children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            ParseNode child = children.elementAt(i);
            String childid = getNewID("node");
            output.println("\t" + nodeid + " -> " + childid + ";");
            if (child.getLabel()=="rule") {
                output.println("\tnode [color=" + newColor() + "];");
            }
            traverse(child, childid);
        }
    }
}
