// Tester.java, created Thu Jan 14 15:22:22 1999 by pnkfelix
package harpoon.Analysis.GraphColoring.Test;

import harpoon.ClassFile.*;
import harpoon.Analysis.GraphColoring.*;
import java.util.Vector;
import java.util.Enumeration;


/**
 * <code>Tester</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Tester.java,v 1.1.2.1 1999-01-14 23:18:59 pnkfelix Exp $
 */

public class Tester  {
    
    static SparseNode makeNode(String s) {
	class TestNode extends SparseNode {
	String name;
	    
	    public String toString() {
		String rtrn = null;
		if (color != null) {
		    rtrn = name + " " + color;
		} else {
		    rtrn = name + " uncolored";
		}
		return rtrn;
	    }
	}

	TestNode tn = new TestNode();
	tn.name = s;
	return tn;
    }
    
    static Vector makeNodeSet(int num) {
	Vector nodes = new Vector();
	char c = 'A';
	String s = "";
	while (num > 0) {
	    if (c > 'Z') {
		s += "-";
		c = 'A';
	    }
	    nodes.addElement(makeNode( s + c ));
	    c++;
	}
	return nodes;
    }
    
    public static void main(String[] args) {
	class TestColorFactory extends ColorFactory {
	    int number = 0;
	    
	    protected Color newColor() {
		class TestColor extends Color {
		    int number;
		    
		    public String toString() {
			return "Color #" + number;
		    }
		}
		
		TestColor tc = new TestColor();
		tc.number = number;
		number++;
		return tc;
	    }
	}

	SparseGraph graphA = new SparseGraph();

	try {
	    Node nodeA = makeNode("A");
	    Node nodeB = makeNode("B");
	    Node nodeC = makeNode("C");
	    Node nodeD = makeNode("D");

	    System.out.println("----------------");

	    graphA.addNode(nodeA);
	    graphA.addNode(nodeB);
	    graphA.addNode(nodeC);
	    graphA.addNode(nodeD);
	    
	    SimpleGraphColorer.findColoring( graphA, new TestColorFactory());
	    
	    printNodes(graphA.getNodes());

	    graphA.resetGraph();
	    
	    System.out.println("----------------");

	    graphA.addEdge(nodeA, nodeB);
	    graphA.addEdge(nodeC, nodeD);

	    SimpleGraphColorer.findColoring( graphA, new TestColorFactory());
	    
	    printNodes(graphA.getNodes());

	    graphA.resetGraph();
	    
	    System.out.println("----------------");
	    
	    graphA.addEdge(nodeA, nodeD);

	    SimpleGraphColorer.findColoring( graphA, new TestColorFactory());
	    
	    printNodes(graphA.getNodes());

	    graphA.resetGraph();
	    
	    System.out.println("----------------");
	    
	    graphA.addEdge(nodeB, nodeD);

	    SimpleGraphColorer.findColoring( graphA, new TestColorFactory());
	    
	    printNodes(graphA.getNodes());

	    graphA.resetGraph();
	    
	    System.out.println("----------------");
	    System.out.println("----------------");
 
	    SparseGraph graphB = new SparseGraph();
	    
	    Node nA = makeNode("A"); graphB.addNode(nA);
	    Node nB = makeNode("B"); graphB.addNode(nB);
	    Node nC = makeNode("C"); graphB.addNode(nC);
	    Node nD = makeNode("D"); graphB.addNode(nD);
	    Node nE = makeNode("E"); graphB.addNode(nE);
	    Node nF = makeNode("F"); graphB.addNode(nF);
	    Node nG = makeNode("G"); graphB.addNode(nG);
	    Node nH = makeNode("H"); graphB.addNode(nH);
	    Node nI = makeNode("I"); graphB.addNode(nI);
	    Node nJ = makeNode("J"); graphB.addNode(nJ);
	    Node nK = makeNode("K"); graphB.addNode(nK);
	    Node nL = makeNode("L"); graphB.addNode(nL);
	    
	    graphB.addEdge(nB, nC);
	    graphB.addEdge(nB, nF);
	    graphB.addEdge(nB, nG);
	    graphB.addEdge(nB, nJ);
	    
	    graphB.addEdge(nC, nD);
	    graphB.addEdge(nC, nG);
	    graphB.addEdge(nC, nH);

	    graphB.addEdge(nD, nE);
	    graphB.addEdge(nD, nG);
	    graphB.addEdge(nD, nH);
	    graphB.addEdge(nD, nI);

	    graphB.addEdge(nE, nH);
	    graphB.addEdge(nE, nI);

	    graphB.addEdge(nG, nH);
	    graphB.addEdge(nG, nK);
	    graphB.addEdge(nG, nL);

	    graphB.addEdge(nH, nJ);
	    graphB.addEdge(nH, nL);

	    graphB.addEdge(nI, nK);

	    graphB.addEdge(nK, nL);

	    SimpleGraphColorer.findColoring( graphB, new TestColorFactory());
	    
	    printNodes(graphB.getNodes());

	    System.out.println("----------------");
	    

	} catch (Exception e) {
	    e.printStackTrace();
	}
	
    }
    
    private static void printNodes(Enumeration nodes) {
	while (nodes.hasMoreElements() ) {
	    System.out.println(nodes.nextElement());
	}
    }
    
    /** <code>Tester</code> is purely functional; it can not be constructed. 
     */
    private Tester() {
        
    }
    
}
