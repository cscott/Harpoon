package harpoon.Analysis.MemOpt;

/* This computes SSI liveness via a simple O(E) algorithm.
   FIXME: add comments, implement Liveness
   FIXME: make non-recursive postfix sort.
*/

import harpoon.IR.Quads.*;
import java.util.*;
import harpoon.Temp.*;
import harpoon.Util.Collections.*;
import harpoon.ClassFile.*;


public class SSILiveness {

    private MultiMap liveOut;
    
    public SSILiveness(HCode hcode) {
        Set reached = new HashSet();
        List sorted = new LinkedList();

        postfixSort((Quad)hcode.getElementsI().next(), sorted, reached);

        liveOut = new GenericMultiMap(new AggregateSetFactory());

        for (Iterator it = sorted.iterator(); it.hasNext();  ) {
            Quad q = (Quad) it.next();

            // compute this node's liveOut
            for (int j = 0; j<q.nextLength(); j++) {
                Edge edge = q.nextEdge(j);

                liveOut.addAll(q, liveOn_helper(edge));
            }
            
        }
    }


    public Set getLiveOn(Edge edge) {
        return liveOn_helper(edge);
    }
    
    private Set liveOn_helper(Edge edge) {
        Quad to = (Quad) edge.toCFG();
        int branch = edge.which_pred();

        Collection lvOut_to = liveOut.getValues(to);

        Set lvOn = new LinearSet(lvOut_to.size());
        lvOn.addAll(lvOut_to);

        lvOn.removeAll(to.defC());

        // treat phis separately
        if (to instanceof PHI) {
            PHI qPhi = (PHI) to;

            for (int i = 0; i<qPhi.numPhis(); i++) {
                lvOn.add(qPhi.src(i, branch));
            }
        } else lvOn.addAll(to.useC());

        return lvOn;
    }
        
    private void postfixSort(Quad q, List nodes, Set reached) {
        reached.add(q);

        for (int i = 0; i<q.nextLength(); i++) {
            Quad son = (Quad) q.nextEdge(i).to();
            if (!reached.contains(son)) {
                postfixSort(son, nodes, reached);
            }
        }

        nodes.add(q);
    }

    public String toString() {
        return " liveOut mmap: " + liveOut;
    }
        
        
}
