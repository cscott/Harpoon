// EnvBuilder.java, created Thu Oct 28 22:38:04 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EnvBuilder;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassSyn;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HConstructorSyn;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldSyn;
import harpoon.ClassFile.UpdateCodeFactory;
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
 * @version $Id: EnvBuilder.java,v 1.1.2.3 1999-11-12 07:47:47 cananian Exp $
 */
public class EnvBuilder {
    protected final UpdateCodeFactory ucf;
    protected final HCode hc;
    protected final HCodeElement hce;
    protected static int counter = 0;
    public Temp[] liveout;

    /** Creates a <code>EnvBuilder</code>. Requires that the 
     *  <code>HCode</code> and <code>HCodeElement</code> objects
     *  be in quad-no-ssa form because <code>QuadLiveness</code>
     *  works with quad-no-ssa. <code>HCodeFactory</code> must
     *  be an <code>UpdateCodeFactory</code>.
     */
    public EnvBuilder(UpdateCodeFactory ucf, HCode hc, HCodeElement hce) {
	this.ucf = ucf;
        this.hc = hc;
	this.hce = hce;
    }

    public HClass makeEnv() {
	System.out.println("Entering EnvBuilder.makeEnv()");
	HClass template = null;
	try {
	    template = 
		HClass.forName("harpoon.Analysis.EnvBuilder.EnvTemplate");
	} catch (NoClassDefFoundError e) {
	    System.err.println("Caught exception " + e.toString() +
			       " in EnvBuilder::makeEnv()");
	    System.err.println("Cannot find " + 
			       "harpoon.Analysis.EnvBuilder.EnvTemplate");
	}

	HClassSyn env = new HClassSyn(template);
	HConstructor[] c = env.getConstructors();
	Util.assert(c.length == 1, 
		    "There should be exactly one constructor in " +
		    "synthesized environment class. Found " + c.length);

	HConstructorSyn nc = new HConstructorSyn(env, c[0]);

	try {
	    env.removeDeclaredMethod(c[0]);
	} catch (NoSuchMethodError e) {
	    System.err.println("Caught exception " + e.toString() +
			       " in EnvBuilder::makeEnv()");
	    System.err.println("Cannot find default constructor for " +
			       "harpoon.Analysis.EnvBuilder.EnvTemplate");
	}

	final QuadLiveness ql = new QuadLiveness(this.hc);
	Set s = ql.getLiveOut(this.hce);
	Object[] vars = s.toArray();
	this.liveout = new Temp[vars.length];
	String[] parameterNames = new String[vars.length];
	HClass[] parameterTypes = new HClass[vars.length];
	HField[] fields = new HField[vars.length];

	System.out.println("Starting SCCAnalysis");
	TypeMap map = ((QuadNoSSA) this.hc).typeMap;
	System.out.println("Finished SCCAnalysis");
	for (int i=0; i<vars.length; i++) {
	    this.liveout[i] = (Temp)vars[i];
	    String tempName = ((Temp)vars[i]).name();
	    HClass type = map.typeMap(this.hce, (Temp)vars[i]);  
	    new HFieldSyn(env, tempName, type);

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
	nc.setParameterNames(parameterNames);
	nc.setParameterTypes(parameterTypes);

	ucf.update(nc, new EnvCode(nc, fields));

	System.out.println("Leaving EnvBuilder.makeEnv()");
	return env;
    }

    private String getStringID() {
	return Integer.toString(counter++);
    }
}
