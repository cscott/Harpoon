// LoopFinder.java
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Loops;

import harpoon.Analysis.Loops.Loops;
import harpoon.Util.WorkSet;
import harpoon.Analysis.DomTree;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.Temp.Temp;
import harpoon.Util.HClassUtil;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;


import java.util.Hashtable;
import java.util.Iterator;
/**
 * <code>LoopFinder</code> implements Dominator Tree Loop detection.
 * 
 * @author  Brian Demsky
 * @version $Id: LoopFinder.java,v 0.1 1999/06/13 17:33:28 bdemsky
 */

public class LoopFinder implements Loops {

    DomTree dominator=new DomTree();
    HCode hc,lasthc;
    WorkSet setofloops;
    Loop root;
    Loop ptr;

    /** Creates a <code>Loop</code>. */
    public LoopFinder(HCode hc) {
       this.hc=hc;
       analyze();
       this.ptr=root;    
    }

    public LoopFinder(HCode hc, Loop root, Loop ptr) {
       this.lasthc=hc;
       this.hc=hc;
       this.root=root;
       this.ptr=ptr;
    }

    /*-----------------------------*/
    // Interface

    public Loops GetRootLoop(HCode hc) {
      this.hc=hc;
      analyze();
      return new LoopFinder(hc,root,root);
    }
        
    public WorkSet Loopentries() {
      analyze();
      WorkSet entries=new WorkSet();
      entries.push(ptr.header);
      return entries;
    }

    public WorkSet Loopbackedges() {
      analyze();
      WorkSet A=new WorkSet();
      Iterator iterate=ptr.entries.iterator();
      while (iterate.hasNext()) {
        HCodeElement hce=(HCodeElement)iterate.next();
        for (int i=0;i<((HasEdges)hce).succ().length;i++) {
          if (((HasEdges)hce).succ()[i].to()==ptr.header) {
            A.push(hce);
            break;
          }
        }
      }
      return A;
    }

    public WorkSet Loopexits() {
      analyze();
      WorkSet A=new WorkSet();
      Iterator iterate=ptr.entries.iterator();
      while (iterate.hasNext()) {
        HCodeElement hce=(HCodeElement)iterate.next();
        for (int i=0;i<((HasEdges)hce).succ().length;i++) {
          if (!ptr.entries.contains(((HasEdges)hce).succ()[i].to())) {
            A.push(hce);
            break;
          }
        }
      }
      return A;
    }

    public WorkSet LoopincElements() {
      analyze();
      WorkSet A=new WorkSet(ptr.entries);
      return A;
    }

    public WorkSet LoopexcElements() {
      analyze();
      WorkSet A=new WorkSet(ptr.entries);
      WorkSet todo=new WorkSet();
      //Get the children
      Iterator iterat=ptr.children.iterator();
      while (iterat.hasNext())
        todo.push(iterat.next());
      //Go down the tree
      while(!todo.isEmpty()) {
         Loop currptr=(Loop)todo.pop();
         Iterator iterate=currptr.children.iterator();
         while (iterate.hasNext()) {
           todo.push(iterate.next());
         }
         iterate=currptr.entries.iterator();
         while (iterate.hasNext())
           A.remove(iterate.next());
      }
      return A;
    }

    public WorkSet NestedLoops() {
      analyze();
      WorkSet L=new WorkSet();
      Iterator iterate=ptr.children.iterator();
      while (iterate.hasNext())
        L.push(new LoopFinder(hc,root,(Loop) iterate.next()));
      return L;
    }

    public Loops ParentLoop() {
      analyze();
      if (ptr.parent!=null)
        return new LoopFinder(hc,root,ptr.parent);
      else return null;
    }

    /*---------------------------*/
    // public information accessor methods.

    public void test(HCode hc) {
      this.hc=hc;
      analyze();
    }

    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */

    /** Main analysis method. */
    void analyze() {
       if (hc!=lasthc) {
         if (! (hc.getRootElement() instanceof HasEdges) )
           throw new Error(hc.getName() + " does not implement HasEdges");
         else lasthc=hc;
         root=new Loop();
         root.header=hc.getRootElement();
         setofloops=new WorkSet();
         findloopheaders(hc.getRootElement());
         buildtree();
       }
    } 
    // end analysis.

    void buildtree() {
       while(!setofloops.isEmpty()) {
          Loop A=(Loop) setofloops.pull();
          System.out.println("=========");
          if (addnode(A, root)!=1) 
             System.out.println("Error in LoopFinder");
       }
    }

    int addnode(Loop A, Loop treenode) {
       System.out.println("Addnode:"+treenode.header.toString());
       System.out.println("Addnode"+A.header.toString());
       if (treenode.entries.contains(A.header))
         if (treenode.header!=A.header) {
           int stored=0;
           Iterator iterate=treenode.children.iterator();
           Loop temp=new Loop();
           while (iterate.hasNext()) {
              temp=(Loop) iterate.next();
              stored=addnode(A,temp);
              if (stored!=0) break;
           }
           if (stored==0) {
              //Store under this node
              treenode.children.push(A);
              temp=A;
           }
           if (stored!=1) {
             //Have to make sure that none of the nodes under this one
             //are children of the new node
             Iterator iterate2=treenode.children.iterator();
             temp.parent=treenode;
             while (iterate2.hasNext()) {
               Loop temp2=(Loop)iterate2.next();
               if (temp2!=temp)
                 if (temp.entries.contains(temp2.header)) {
                    temp.children.push(temp2);
                    iterate2.remove();
                 }
             }
           }
           return 1;
         } else {
           //need to combine loops
           while (!A.entries.isEmpty()) {
             treenode.entries.push(A.entries.pull());
           }
           //let the previous caller know that they have stuff todo
           return 2;
         }
       else return 0;
    }

    void findloopheaders(HCodeElement current_node) {
        visit(current_node);
        root.entries.push(current_node);
        HCodeElement[] children=dominator.children(hc, current_node);
        for (int i=0;i<children.length;i++)
          findloopheaders(children[i]);
    }


   public void visit(HCodeElement q) {
      Loop A=new Loop();
      WorkSet B=new WorkSet();
      for (int i=0;i<(((HasEdges)q).succ()).length;i++) {
         HCodeElement temp=q;
	 while ((temp!=(hc.getRootElement()))&&
         (((HasEdges)q).succ()[i]).to()!=temp) {
           temp=dominator.idom(hc,temp);
         }
         if (((HasEdges)q).succ()[i].to()==temp) {
            //found a loop
            A.entries.push(temp); //Push the header
            A.header=temp;
            B.push(q); //Put the backedge in the todo list
            while(!B.isEmpty()) {
               HCodeElement newnode=(HCodeElement)B.pull();
               for (int j=0;j<((HasEdges)newnode).pred().length;j++) {
                  if (!A.entries.contains(((HasEdges)newnode).pred()[j].from()))
                     B.push(((HasEdges)newnode).pred()[j].from());
                  }
                  A.entries.push(newnode);
               }
               setofloops.push(A);
            }
         }
      }

      public class Loop {
         public WorkSet entries=new WorkSet();
         public HCodeElement header;
         public WorkSet children=new WorkSet();
         public Loop parent;
      }
}
