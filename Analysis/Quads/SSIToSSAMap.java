// SSIToSSAMap.java, created Thu Jun 17 16:11:41 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.WorkSet;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.ClassFile.HCode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
/**
 * An <code>SSIToSSAMap</code> allows you to look at an SSI
 * representation "with glasses on" so that it appears as SSA.  This
 * is often useful when the additional value-information
 * discrimination of SSI form is unnecessary, but converting the input
 * to SSA is undesirable.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: SSIToSSAMap.java,v 1.1.2.1 2001-11-14 20:50:05 cananian Exp $
 */
public class SSIToSSAMap implements TempMap {
    
    /** Creates a <code>SSIToSSAMap</code> for the <code>HCode</code>
     *  <code>hc</code>. */
    public SSIToSSAMap(HCode hc) {
	//We need to build to map in the constructor
	//Set HCode pointer
        this.hc=hc;

	//Set forward SSI->SSA Map
	this.forward=new HashMap();

	//Set backward SSA->set of SSI Temps Map
        this.backward=new HashMap();

	//List of sigmas
	this.sigmas=new WorkSet();

	//List of phis
	this.phis=new WorkSet();

	//List of phi destinations
	this.phiresults=new WorkSet();

	//Build the map
	buildmap();
	//debug();
    }

    void debug() {
	//Print out the map
	Set W=forward.keySet();
	Iterator it=W.iterator();
	while (it.hasNext()) {
	    Temp t=(Temp)it.next();
	    Temp tt=(Temp)forward.get(t);
	    System.out.println(t.toString()+"--->"+tt.toString());
        }
    }

    void buildmap() {
	//Find all of the Sigmas and Phis
	findSigmaPhis();

	//Add the Sigmas into the map
	addSigmas();

	//Add the Phis into the map
       	addPhis();
    }

    void addSigmas() {
	//Iterate over the set of sigmas

	Iterator iterate=sigmas.iterator();
	while (iterate.hasNext()) {

	    //Put the current SIGMA object in sigma
	    SIGMA sigma=(SIGMA) iterate.next();
	    int numbersigmas=sigma.numSigmas();
	    int splitting=sigma.arity();

	    //Loop over each individual sigma in it
      	    for (int i=0;i<numbersigmas;i++) {
		
		
		Temp tmpsrc;
		WorkSet backset;

		//Need to see if the source of the sigma is already in our map
		if (forward.get(sigma.src(i))!=null) {

		    //If so, we need to look at its source to get
		    //the original SSA assignment
		    tmpsrc=(Temp)forward.get(sigma.src(i));

		    //We also need to get its backset to add the
		    //destinations of our sigmas to
		    backset=(WorkSet)backward.get(tmpsrc);
		    }
		else {
		    //The source isn't in the map
		    //Create a new backwards entry for it
		    if (backward.containsKey(sigma.src(i))) {
			backset=(WorkSet)backward.get(sigma.src(i));
		    }
		    else {
			backset=new WorkSet();

			//Push this entry in the backwards map
			backward.put(sigma.src(i), backset);
		    }
		    
		    //Set the source up for the destinations of the sigmas
		    tmpsrc=sigma.src(i);
		}
		for (int j=0;j<splitting;j++) {
		    //src(i)--><dst(i,0),dst(i,1)...dst(i,splitting)>

		    //Put us in the forward map
		    forward.put((Temp)sigma.dst(i,j),tmpsrc);

		    //Add us to the set of things referencing tmpsrc
		    backset.push((Temp)sigma.dst(i,j));

		    //look for nodes referencing us
		    WorkSet back=(WorkSet)backward.get(sigma.dst(i,j));
		    
		    if (back!=null) {
			//get rid of our backward reference set
			backward.remove(sigma.dst(i,j));

			//Iterate through temps referencing us
			Iterator fix=back.iterator();

			while (fix.hasNext()) {
    			    Temp t=(Temp)fix.next();

			    //Fix their forward map
			    forward.put(t,tmpsrc);
			    //Add them to the set referencing tmpsrc
			    backset.push(t);
			}
		    }
      		}
	    }
	}
    }

    void addPhis() {
	boolean dirty=true;
	//Iterate through the phi's until we stop
	//adding phi's into our maps
	while (dirty) {
	    dirty=false;

	    
	    Iterator iterate = phis.iterator();
	    while (iterate.hasNext()) {
		//Put the next phi in a variable
		InternalPhi nxtPhi=(InternalPhi)iterate.next();

		//Phi's with only one unique definition
		//Can be eliminated
		Temp uniquedef=null;

		//Set flag that keeps track of whether we can
		//put this phi in our mapping
		boolean good=true;

		//Set this boolean to flag if
		//we have a definition from outside
		//the set {phi destinations, sigma destinations}

		boolean outsidedef=false;

		//Loop through each phi src arguement

 		for (int i=0;i<nxtPhi.src.length;i++) {

		    //Possible Conditions:
		    //1) All sources are either the destination of the Phi
		    //or one unique Temp
		    //Then add this phi to our mapping
		    //
		    //2) One source is not in the set {phi destinations,
		    //sigma sources, sigma destinations}
		    //
		    //
		    //3) Two sources are not in the set
		    //{phi destination, sigma destinations}
		    //Useless phi, toss it!
		   
		    if (forward.containsKey(nxtPhi.src[i])) {
			//This phi is in our forward mapping already
			Temp tmp=(Temp)forward.get(nxtPhi.src[i]);

			//Check to see if this references our destination
			//if so just ignore it
			if (tmp!=nxtPhi.dst) {
			    //If not, we need to see if we already have found an unique reference
			    if (uniquedef==null)
				//if not, set Temp to this reference
				uniquedef=tmp;
			    else {
				//If we have found one already, see if they are the same one
				if (uniquedef!=tmp) {
				    //Nope...  Set flag, and break out of here
				    good=false;
				    break;
				}
			    }
      			}
		    } else {
			if (!phiresults.contains(nxtPhi.src[i])) {
			    //Approach here is to get useless nodes out of our list to speed up iterations

				if (outsidedef) {
				    //If we already have a unique outside definition, trash this one
				    if(uniquedef!=nxtPhi.src[i]) {
					phiresults.remove(nxtPhi.dst);
					good=false;
					iterate.remove();
					break;
				    }
  				} else {
				    //Make this out unique definition
				    outsidedef=true;
				  
				    if (uniquedef==null)
					//Good we don't have a def yet
					uniquedef=nxtPhi.src[i];
				    else {
					//we already have one, is it the same?
					if (uniquedef!=nxtPhi.src[i]) {
					    //nope...toss it back to the pool
					    good=false;
					    break;
					}
				    }
				}
			} else {
			    //This one has a src that is in the Phi pool
			    if (nxtPhi.src[i]!=nxtPhi.dst) {
				//Make sure it isn't ourself
				if (uniquedef==null)
				    //Store a uniquedef
				    uniquedef=nxtPhi.src[i];
				else {
				    //We have a uniquedef, see if they are the same
				    if (uniquedef!=nxtPhi.src[i]) {
					//Toss it back into the pool for the next iteration
					good=false;
					break;
				    }
				}				
			    }
			}
		    }
	        }
		if (good) {
		    //Set the dirty flag to true for another pass
		    dirty=true;

		    phiresults.remove(nxtPhi.dst);

		    //Get rid of this phi in the phi WorkSet
		    iterate.remove();

		    //Need to look for references looking at our destination
		    if (backward.containsKey(nxtPhi.dst)) {
			//Fix each of them
			WorkSet tmp=(WorkSet)backward.get(nxtPhi.dst);
			Iterator iterat=tmp.iterator();
			Temp dest;
			//Correct the forward references
			while(iterat.hasNext()) {
			    forward.put(iterat.next(),uniquedef);
			}

			//Make a new super set of people looking at uniquedef
			WorkSet tmp2;
			if (backward.containsKey(uniquedef))
			    tmp2=(WorkSet)backward.get(uniquedef);
			else
			    tmp2=new WorkSet();

			//Add the set of people looking at us to this new set
			iterat=tmp.iterator();
			while(iterat.hasNext()) {
			    tmp2.push((Temp)iterat.next());
			}

			//Add us into the set
			tmp2.push(nxtPhi.dst);
			//Hand off the new set
			backward.put(uniquedef,tmp2);
			//No one is referencing us anymore....tell the backward list
			backward.remove(nxtPhi.dst);
			//Add out forward link
			forward.put(nxtPhi.dst, uniquedef);
		    } else {
			//Just add us...
			forward.put(nxtPhi.dst,uniquedef);
			if (backward.containsKey(uniquedef)) {
			    //Set of references to uniquedef exist, add us
			    ((WorkSet)backward.get(uniquedef)).push(nxtPhi.dst);
			} else {
			    //No set exist, create on with us in it...
			    WorkSet us=new WorkSet();
			    us.push(nxtPhi.dst);
			    backward.put(uniquedef,us);
			}
		    }
   		}
	    }
	}
    }

    void findSigmaPhis() {
	LowQuadVisitor visitor = new LowQuadVisitor(false/*non-strict*/) {
	    public void visit(Quad q) {
	    }
	    
	    public void visit(SIGMA q) {
		sigmas.push(q);
	    }

	    public void visit(PHI q) {
		//create list of phi temps
		int numberofphis=q.numPhis();
		for (int i=0;i<numberofphis;i++) {
		    phiresults.push(q.dst(i));
		    phis.push(new InternalPhi(q.src(i),q.dst(i)));
    		}
	    }
	};

	Iterator iterate=hc.getElementsI();
	while (iterate.hasNext()) {
	    Quad thisone=(Quad) iterate.next();
	    thisone.accept(visitor);
       	}
    }

    public Temp tempMap(Temp t) {
        if (forward.containsKey(t))
   	    return ((Temp)forward.get(t));
	else
            return t;
    }

    private class InternalPhi {
	public Temp [] src;
	public Temp dst;
	InternalPhi (Temp [] src, Temp dst) {
	    this.src=src;
	    this.dst=dst;
	}
    }

    HCode hc;
    HashMap forward;
    HashMap backward;
    WorkSet sigmas;
    WorkSet phis;
    WorkSet phiresults;
}



