// EnvBuilder.java, created Thu Oct 28 22:38:04 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EnvBuilder;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.ClassFile.HClass;
//import harpoon.ClassFile.HClassSyn;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
//import harpoon.ClassFile.HConstructorSyn;
import harpoon.ClassFile.HField;
//import harpoon.ClassFile.HFieldSyn;
import harpoon.ClassFile.UpdateCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HMethodMutator;

import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.UniqueName;

import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.Iterator;
import java.util.Set;

/**
 * <code>EnvBuilder</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EnvBuilder.java,v 1.1.2.5 2000-01-13 23:51:47 bdemsky Exp $
 */
public class EnvBuilder {
    protected final UpdateCodeFactory ucf;
    protected final HCode hc;
    protected final HCodeElement hce;
    protected static int counter = 0;
    public Temp[] liveout;
    protected final Linker linker;

    /** Creates a <code>EnvBuilder</code>. Requires that the 
     *  <code>HCode</code> and <code>HCodeElement</code> objects
     *  be in quad-no-ssa form because <code>QuadLiveness</code>
     *  works with quad-no-ssa. <code>HCodeFactory</code> must
     *  be an <code>UpdateCodeFactory</code>.
     */
    public EnvBuilder(UpdateCodeFactory ucf, HCode hc, HCodeElement hce, Temp[] lives, Linker linker) {
	this.ucf = ucf;
        this.hc = hc;
	this.hce = hce;
	this.liveout = lives;
	this.linker=linker;
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

	String[] parameterNames = new String[liveout.length];
	HClass[] parameterTypes = new HClass[liveout.length];
	HField[] fields = new HField[liveout.length];

	System.out.println("Starting SCCAnalysis");
	TypeMap map = ((QuadNoSSA) this.hc).typeMap;
	System.out.println("Finished SCCAnalysis");
	for (int i=0; i<liveout.length; i++) {
	    //	    this.liveout[i] = (Temp)vars[i];
	    String tempName = liveout[i].name();
	    HClass type = map.typeMap(this.hce, liveout[i]);  
	    envmutator.addDeclaredField(tempName, type);
		//	    new HFieldSyn(env, tempName, type);

	    parameterNames[i] = tempName;
	    parameterTypes[i] = type;

	    try {
		fields[i] = env.getField(parameterNames[i]);
	    } catch (NoSuchFieldError e) {
		System.err.println("Caught exception " + e.toString() +
				   "in harpoon.Analysis.EnvBuilder." +
				   "EnvBuilder::makeEnv()");
		System.err.println("Cannot find synthesized field " +
				   parameterNames[i]);
	    }
	}
	ncmutator.setParameterNames(parameterNames);
	ncmutator.setParameterTypes(parameterTypes);

	ucf.update(nc, new EnvCode(nc, fields));

	System.out.println("Leaving EnvBuilder.makeEnv()");
	return env;
    }

    private String getStringID() {
	return Integer.toString(counter++);
    }
}
