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
	private GraphNode source;
        private String dotnodeparams = new String();

        public Edge(String label, GraphNode target) {
            this.label = label;
            this.target = target;
        }

        public String getLabel() {
            return label;
        }

	public void setSource(GraphNode s) {
	    this.source=s;
	}

	public GraphNode getSource() {
	    return source;
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
    Vector inedges = new Vector();
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

    public static void computeclosure(Collection nodes, Collection removed) {
	Stack tovisit=new Stack();
	tovisit.addAll(nodes);
	while(!tovisit.isEmpty()) {
	    GraphNode gn=(GraphNode)tovisit.pop();
	    for(Iterator it=gn.edges();it.hasNext();) {
		Edge edge=(Edge)it.next();
		GraphNode target=edge.getTarget();
		if (!nodes.contains(target)) {
		    if ((removed==null)||
			(!removed.contains(target))) {
			nodes.add(target);
			tovisit.push(target);
		    }
		}
	    }
	}
    }

    public static void boundedcomputeclosure(Collection nodes, Collection removed,int depth) {
	Stack tovisit=new Stack();
	tovisit.addAll(nodes);
	for(int i=0;i<depth&&!tovisit.isEmpty();i++) {
	    GraphNode gn=(GraphNode)tovisit.pop();
	    for(Iterator it=gn.edges();it.hasNext();) {
		Edge edge=(Edge)it.next();
		GraphNode target=edge.getTarget();
		if (!nodes.contains(target)) {
		    if ((removed==null)||
			(!removed.contains(target))) {
			nodes.add(target);
			tovisit.push(target);
		    }
		}
	    }
	}
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

    public Iterator inedges() {
        return inedges.iterator();
    }

    public void addEdge(Edge newedge) {
	newedge.setSource(this);
        edges.addElement(newedge);
	GraphNode tonode=newedge.getTarget();
	tonode.inedges.addElement(newedge);
    }

    void reset() {
	    discoverytime = -1;
	    finishingtime = -1;
	    status = UNVISITED;
    }

    void resetscc() {
	status = UNVISITED;
    }

    void discover(int time) {
	discoverytime = time;
	status = PROCESSING;
    }

    void finish(int time) {
        assert status == PROCESSING;
	finishingtime = time;
        status = FINISHED;
    }

    /** Returns finishing time for dfs */

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
	Collection special;
        
        public static void visit(java.io.OutputStream output, Collection nodes) {
	    visit(output,nodes,null);
	}

        public static void visit(java.io.OutputStream output, Collection nodes, Collection special) {
            DOTVisitor visitor = new DOTVisitor(output);
	    visitor.special=special;
            visitor.nodes = nodes;
            visitor.make();
        }
        
        private void make() {
            output.println("digraph dotvisitor {");
            output.println("\trotate=90;");
	    /*            output.println("\tpage=\"8.5,11\";");
			  output.println("\tnslimit=1000.0;");
			  output.println("\tnslimit1=1000.0;");
			  output.println("\tmclimit=1000.0;");
			  output.println("\tremincross=true;");*/
            output.println("\tnode [fontsize=10,height=\"0.1\", width=\"0.1\"];");
            output.println("\tedge [fontsize=6];");
            traverse();
            output.println("}\n");
        }
                
        private void traverse() {            
	    Set cycleset=GraphNode.findcycles(nodes);

            Iterator i = nodes.iterator();
            while (i.hasNext()) {
                GraphNode gn = (GraphNode) i.next();
                Iterator edges = gn.edges();
                String label = gn.getTextLabel(); // + " [" + gn.discoverytime + "," + gn.finishingtime + "];";
		String option="";
		if (cycleset.contains(gn))
		    option=",style=bold";
		if (special!=null&&special.contains(gn))
		    option+=",shape=box";
                output.println("\t" + gn.getLabel() + " [label=\"" + label + "\"" + gn.dotnodeparams + option+"];");

                while (edges.hasNext()) {
                    Edge edge = (Edge) edges.next();
                    GraphNode node = edge.getTarget();
		    if (nodes.contains(node)) {
			String edgelabel = useEdgeLabels ? "label=\"" + edge.getLabel() + "\"" : "label=\"\"";
			output.println("\t" + gn.getLabel() + " -> " + node.getLabel() + " [" + edgelabel + edge.dotnodeparams + "];");
		    }
                }
            }
        }
    }

    /** This function returns the set of nodes involved in cycles. 
     *	It only considers cycles containing nodes in the set 'nodes'.
    */
    public static Set findcycles(Collection nodes) {
	HashSet cycleset=new HashSet();
	SCC scc=DFS.computeSCC(nodes);
	if (!scc.hasCycles())
	    return cycleset;
	for(int i=0;i<scc.numSCC();i++) {
	    if (scc.hasCycle(i))
		cycleset.addAll(scc.getSCC(i));
	}
	return cycleset;
    }

    public static class SCC {
	boolean acyclic;
	HashMap map,revmap;
	int numscc;
	public SCC(boolean acyclic, HashMap map,HashMap revmap,int numscc) {
	    this.acyclic=acyclic;
	    this.map=map;
	    this.revmap=revmap;
	    this.numscc=numscc;
	}

	/** Returns whether the graph has any cycles */
	public boolean hasCycles() {
	    return !acyclic;
	}

	/** Returns the number of Strongly Connected Components */
	public int numSCC() {
	    return numscc;
	}

	/** Returns the strongly connected component number for the GraphNode gn*/
	public int getComponent(GraphNode gn) {
	    return ((Integer)revmap.get(gn)).intValue();
	}

	/** Returns the set of nodes in the strongly connected component i*/
	public Set getSCC(int i) {
	    Integer scc=new Integer(i);
	    return (Set)map.get(scc);
	}

	/** Returns whether the strongly connected component i contains a cycle */
	boolean hasCycle(int i) {
	    Integer scc=new Integer(i);
	    Set s=(Set)map.get(scc);
	    if (s.size()>1)
		return true;
	    Object [] array=s.toArray();
	    GraphNode gn=(GraphNode)array[0];
	    for(Iterator it=gn.edges();it.hasNext();) {
		Edge e=(Edge)it.next();
		if (e.getTarget()==gn)
		    return true; /* Self Cycle */
	    }
	    return false;
	}
    }

    /**
     * DFS encapsulates the depth first search algorithm 
     */
    public static class DFS {

        int time = 0;
	int sccindex = 0;
        Collection nodes;
	Vector finishingorder=null;
	HashMap sccmap;
	HashMap sccmaprev;

        private DFS(Collection nodes) { 
            this.nodes = nodes;
        }
	/** Calculates the strong connected components for the graph composed
	 *  of the set of nodes 'nodes'*/
	public static SCC computeSCC(Collection nodes) {
	    if (nodes==null) {
		throw new NullPointerException();
	    }
	    DFS dfs=new DFS(nodes);
	    dfs.sccmap=new HashMap();
	    dfs.sccmaprev=new HashMap();
	    dfs.finishingorder=new Vector();
	    boolean acyclic=dfs.go();
            for (Iterator it = nodes.iterator();it.hasNext();) {
                GraphNode gn = (GraphNode) it.next();
                gn.resetscc();
            }
	    for(int i=dfs.finishingorder.size()-1;i>=0;i--) {
		GraphNode gn=(GraphNode)dfs.finishingorder.get(i);
		if (gn.getStatus() == UNVISITED) {
		    dfs.dfsprev(gn);
		    dfs.sccindex++; /* Increment scc index */
		}
	    }
	    return new SCC(acyclic,dfs.sccmap,dfs.sccmaprev,dfs.sccindex);
	}

	void dfsprev(GraphNode gn) {
	    if (gn.getStatus()==FINISHED||!nodes.contains(gn))
		return;
	    gn.setStatus(FINISHED);
	    Integer i=new Integer(sccindex);
	    if (!sccmap.containsKey(i))
		sccmap.put(i,new HashSet());
	    ((Set)sccmap.get(i)).add(gn);
	    sccmaprev.put(gn,i);
	    for(Iterator edgeit=gn.inedges();edgeit.hasNext();) {
		Edge e=(Edge)edgeit.next();
		GraphNode gn2=e.getSource();
		dfsprev(gn2);
	    }
	}

        public static boolean depthFirstSearch(Collection nodes) {
            if (nodes == null) {
                throw new NullPointerException();
            }
            
            DFS dfs = new DFS(nodes);
            return dfs.go();
        }

        private boolean go() {           
            Iterator i;
            time = 0;
            boolean acyclic=true;
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
                    if (!dfs(gn))
			acyclic=false;
                } 
            }
	    return acyclic;
        }

        private boolean dfs(GraphNode gn) {
	    boolean acyclic=true;
            gn.discover(time++);
            Iterator edges = gn.edges();

            while (edges.hasNext()) {
                Edge edge = (Edge) edges.next();
                GraphNode node = edge.getTarget();
		if (!nodes.contains(node)) /* Skip nodes which aren't in the set */
		    continue;
                if (node.getStatus() == UNVISITED) {
                    if (!dfs(node))
			acyclic=false;
                } else if (node.getStatus()==PROCESSING) {
		    acyclic=false;
		}
            }
	    if (finishingorder!=null)
		finishingorder.add(gn);
            gn.finish(time++);
	    return acyclic;
        }

    } /* end DFS */

}
