package MCC.IR;

import java.util.*;
import java.io.*;

public class GraphNode {

    public static boolean useEdgeLabels;

    /* NodeStatus enumeration pattern ***********/
    
    public static final NodeStatus UNVISITED = new NodeStatus("UNVISITED");
    public static final NodeStatus PROCESSING = new NodeStatus("PROCESSING");
    public static final NodeStatus FINISHED = new NodeStatus("FINISHED");

    public static class NodeStatus {
        private static String name;
        private NodeStatus(String name) { this.name = name; }
        public String toString() { return name; }
    }

    /* Edge *****************/

    public static class Edge {
        
        private String label;
        private GraphNode target;
        private String dotnodeparams = new String();

        public Edge(String label, GraphNode target) {
            this.label = label;
            this.target = target;
        }

        public String getLabel() {
            return label;
        }

        public GraphNode getTarget() {
            return target;
        }

        public void setDotNodeParameters(String param) {
            if (param == null) {
                throw new NullPointerException();
            }
            if (param.length() > 0) {
                dotnodeparams = "," + param;
            } else {
                dotnodeparams = new String();
            }
        }

    }

    int discoverytime = -1;
    int finishingtime = -1; /* used for searches */
    Vector edges = new Vector();  
    String nodelabel;
    String textlabel;
    NodeStatus status = UNVISITED;    
    String dotnodeparams = new String();
    Object owner = null;

    public GraphNode(String label) {
        this.nodelabel = label;
        this.textlabel = label;
    }

    public GraphNode(String label, Object owner) {
        this.nodelabel = label;
        this.textlabel = label;
        this.owner = owner;
    }

    public GraphNode(String label, String textlabel, Object owner) {
        this.nodelabel = label;
        this.textlabel = textlabel;
        this.owner = owner;
    }

    public Object getOwner() {
        return owner;
    }

    public void setDotNodeParameters(String param) {
        if (param == null) {
            throw new NullPointerException();
        }
        if (param.length() > 0) {
            dotnodeparams = "," + param;
        } else {
            dotnodeparams = new String();
        }
    }
    
    public void setStatus(NodeStatus status) {
        if (status == null) {
            throw new NullPointerException();
        }
        this.status = status;
    }

    public String getLabel() {
        return nodelabel;
    }

    public String getTextLabel() {
        return textlabel;
    }
    
    public NodeStatus getStatus() {
        return this.status;
    }

    public Iterator edges() {
        return edges.iterator();
    }

    public void addEdge(Edge newedge) {
        edges.addElement(newedge);
    }

    public void reset() {
        discoverytime = -1;
        finishingtime = -1;
        status = UNVISITED;
    }

    public void discover(int time) {
        discoverytime = time++;
        status = PROCESSING;
    }

    public void finish(int time) {
        assert status == PROCESSING;
        finishingtime = time++;
        status = FINISHED;
    }

    public int getFinishingTime() {
        return finishingtime;
    }

    public static class DOTVisitor {
        
        java.io.PrintWriter output;
        int tokennumber;
        int color;
      
        private DOTVisitor(java.io.OutputStream output) {
            tokennumber = 0;
            color = 0;
            this.output = new java.io.PrintWriter(output, true);
        }
        
        private String getNewID(String name) {
            tokennumber = tokennumber + 1;
            return new String (name+tokennumber);
        }

        Collection nodes;
        
        public static void visit(java.io.OutputStream output, Collection nodes) {
            DOTVisitor visitor = new DOTVisitor(output);
            visitor.nodes = nodes;
            visitor.make();

        }
        
        private void make() {
            output.println("digraph dotvisitor {");
            output.println("\trotate=90;");
            output.println("\tpage=\"8.5,11\";");
            output.println("\tnslimit=1000.0;");
            output.println("\tnslimit1=1000.0;");
            output.println("\tmclimit=1000.0;");
            output.println("\tremincross=true;");
            output.println("\tnode [fontsize=10,height=\"0.1\", width=\"0.1\"];");
            output.println("\tedge [fontsize=6];");

            traverse();

            output.println("}\n");
        }
                
        private void traverse() {            
            Iterator i = nodes.iterator();
            while (i.hasNext()) {
                GraphNode gn = (GraphNode) i.next();
                Iterator edges = gn.edges();
                String label = gn.getTextLabel(); // + " [" + gn.discoverytime + "," + gn.finishingtime + "];";
                output.println("\t" + gn.getLabel() + " [label=\"" + label + "\"" + gn.dotnodeparams + "];");

                while (edges.hasNext()) {
                    Edge edge = (Edge) edges.next();
                    GraphNode node = edge.getTarget();
                    String edgelabel = useEdgeLabels ? "label=\"" + edge.getLabel() + "\"" : "label=\"\"";
                    output.println("\t" + gn.getLabel() + " -> " + node.getLabel() + " [" + edgelabel + edge.dotnodeparams + "];");
                }
            }
        }
    }
    
    /**
     * DFS encapsulates the depth first search algorithm 
     */
    public static class DFS {

        int time = 0;
        Collection nodes;

        private DFS(Collection nodes) { 
            this.nodes = nodes;
        }

        public static void depthFirstSearch(Collection nodes) {
            if (nodes == null) {
                throw new NullPointerException();
            }
            
            DFS dfs = new DFS(nodes);
            dfs.go();
        }

        private void go() {           
            Iterator i;
            time = 0;
            
            i = nodes.iterator();
            while (i.hasNext()) {
                GraphNode gn = (GraphNode) i.next();
                gn.reset();            
            }            

            i = nodes.iterator();
            while (i.hasNext()) {
                GraphNode gn = (GraphNode) i.next();
                assert gn.getStatus() != PROCESSING;                    
                if (gn.getStatus() == UNVISITED) {
                    dfs(gn);
                } 
            }
        }

        private void dfs(GraphNode gn) {
            gn.discover(time++);            
            Iterator edges = gn.edges();

            while (edges.hasNext()) {
                Edge edge = (Edge) edges.next();
                GraphNode node = edge.getTarget();
                if (node.getStatus() == UNVISITED) {
                    dfs(node);
                }
            }

            gn.finish(time++);
        }                        

    } /* end DFS */

}
