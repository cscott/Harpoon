// EnvBuilder.java, created Thu Oct 28 22:38:04 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EnvBuilder;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.HMethod;

import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.UniqueName;

import harpoon.IR.Quads.QuadSSI;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.Iterator;
import java.util.Set;

/**
 * <code>EnvBuilder</code>
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: EnvBuilder.java,v 1.2 2002-02-25 20:56:49 cananian Exp $
 */
public class EnvBuilder {
    protected final CachingCodeFactory ucf;
    protected final HCode hc;
    protected final HCodeElement hce;
    protected static int counter = 0;
    public Temp[] liveout;
    protected final Linker linker;
    protected TypeMap typemap;
    protected boolean recycle;


    /** Creates a <code>EnvBuilder</code>. Requires that the 
     *  <code>HCode</code> and <code>HCodeElement</code> objects
     *  be in quad-no-ssa form because <code>QuadLiveness</code>
     *  works with quad-no-ssa. <code>HCodeFactory</code> must
     *  be an <code>CachingCodeFactory</code>.
     */
    public EnvBuilder(CachingCodeFactory ucf, HCode hc, HCodeElement hce, Temp[] lives, Linker linker,TypeMap typemap, boolean recycle) {
	this.ucf = ucf;
        this.hc = hc;
	this.hce = hce;
	this.liveout = lives;
	this.linker=linker;
	this.typemap=typemap;
	this.recycle=recycle;
    }

    public HClass makeEnv() {
	System.out.println("Entering EnvBuilder.makeEnv()");
	HClass template = null;
	try {
	    template = 
		linker.forName("harpoon.Analysis.EnvBuilder.EnvTemplate");
	} catch (NoClassDefFoundError e) {
	    System.err.println("Caught exception " + e.toString() +
			       " in EnvBuilder::makeEnv()");
	    System.err.println("Cannot find " + 
			       "harpoon.Analysis.EnvBuilder.EnvTemplate");
	}
	String envName=UniqueName.uniqueClassName("harpoon.Analysis.EnvBuilder.EnvTemplate", linker);
	HClass env = linker.createMutableClass(envName, template);
	HClassMutator envmutator=env.getMutator();
	
	HConstructor[] c = env.getConstructors();
	Util.assert(c.length == 1, 
		    "There should be exactly one constructor in " +
		    "synthesized environment class. Found " + c.length);

	HConstructor nc = c[0];
	HMethodMutator ncmutator=nc.getMutator();



	int size=0;
	for (int ii=0;ii<liveout.length;ii++)
	    if (typemap.typeMap(hce,liveout[ii])!=HClass.Void)
		size++;

	String[] parameterNames = new String[size];
	HClass[] parameterTypes = new HClass[size];
	HField[] fields = new HField[size];

	System.out.println("Starting SCCAnalysis");
	System.out.println("Finished SCCAnalysis");
	for (int i=0,j=0; i<liveout.length; i++) {
	    if (typemap.typeMap(hce,liveout[i])!=HClass.Void) {
		    String tempName = liveout[i].name();
		    HClass type = typemap.typeMap(hce, liveout[i]);  
		    envmutator.addDeclaredField(tempName, type);
		    
		    parameterNames[j] = tempName;
		    parameterTypes[j] = type;

		    try {
			fields[j] = env.getField(parameterNames[j]);
		    } catch (NoSuchFieldError e) {
			System.err.println("Caught exception " + e.toString() +
					   "in harpoon.Analysis.EnvBuilder." +
					   "EnvBuilder::makeEnv()");
			System.err.println("Cannot find synthesized field " +
					   parameterNames[i]);
		    }
		    j++;
	    }
	}
	ncmutator.setParameterNames(parameterNames);
	ncmutator.setParameterTypes(parameterTypes);

	HMethod hrecycle=null;
	if (recycle)
	    hrecycle=envmutator.addDeclaredMethod("recycle", parameterTypes,
						  HClass.Void);
	
	
	ucf.put(nc, new EnvCode(nc, fields,linker));
	if (recycle)
	    ucf.put(hrecycle,new EnvCode(hrecycle,fields,null));
	
	System.out.println("Leaving EnvBuilder.makeEnv()");
	return env;
    }

    private String getStringID() {
	return Integer.toString(counter++);
    }
}
