package harpoon.Analysis.MemOpt;

/* This colors a graph with as few colors as possible, returning
   the resulting color classes (similar to GraphColoring.UnboundedGraphColorer,
   qbut with much better results).
   
   It does not currently implement GraphColoring, instead gets the graph as
   a collection of nodes and a "contains edge" relation (MultiMap) and
   returns the classes not the colors. We use it in IncompatibilityAnalysis
   on what is conceptually a symmetric relation, not a graph. IMHO, it
   should stay this way since we really don't need the extra complication
   of ColorableGraph and friends in IncompatibilityAnalysis. But I could
   change it if needed.

   Uses a Most-saturated-node heuristic, with O(E + N log N) complexity.

   FIXME: add comments
   FIXME: maybe change to GraphColoring semantics.
*/

import harpoon.Util.MaxPriorityQueue;
import harpoon.Util.BinHeapPriorityQueue;
import net.cscott.jutil.MultiMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public final class MyGraphColorer {

    public static Collection colorGraph(Collection nodes,
                                        MultiMap edges) {

        MaxPriorityQueue pqueue = new BinHeapPriorityQueue(nodes.size());

        // this could be optimized by implementing a custom addAll in pqueue
        // then building would take O(n) instead of O(n log n)
        //   but this is really too insignificant to matter
        for (Iterator it = nodes.iterator(); it.hasNext(); ) {
            Object node = it.next();
            pqueue.insert(node, edges.getValues(node).size());
        }

        Collection classes = new LinkedList();
        
        while (!pqueue.isEmpty()) {
            Object candidate = pqueue.deleteMax();

            boolean assigned = false;

            for (Iterator it = classes.iterator();
                 it.hasNext() && !assigned; ) {
                Collection members = (Collection) it.next();

                boolean compatible = true;
                for (Iterator it2 = members.iterator();
                     it2.hasNext() && compatible; ) {
                    Object member = it2.next();

                    compatible &= !edges.contains(candidate, member);
                }

                if (compatible) {
                    members.add(candidate);
                    assigned = true;
                }
            }

            if (!assigned) {
                Collection newClass = new LinkedList();
                newClass.add(candidate);

                classes.add(newClass);
            }

            // now decrease priorities for this guys neighbours
            for (Iterator it = edges.getValues(candidate).iterator();
                 it.hasNext(); ) {
                Object  neighbour = it.next();
                if (pqueue.contains(neighbour)) {
                    pqueue.changePriority(neighbour, -1);
                }
            }
        }
        return classes;
    }
}
