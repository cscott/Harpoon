// RealtimeRuntime.java, created Wed Jan 31 16:35:49 2001 by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;

import harpoon.Backend.Generic.Runtime.ObjectBuilder.Info;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Maps.DefaultNameMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.Backend.PreciseC.Frame;
import harpoon.Backend.Runtime1.AllocationStrategy;
import harpoon.Backend.Runtime1.Data;
import harpoon.Backend.Runtime1.ObjectBuilder;
import harpoon.Backend.Runtime1.ObjectBuilder.RootOracle;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;

import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.SEGMENT;

import harpoon.Temp.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>RealtimeRuntime</code> is a trivial extension of 
 * <code>harpoon.Backend.Runtime2.Runtime</code> that allows constants
 * to be tagged with an ImmortalMemory and emits extra const char* data
 * for debugging purposes when Realtime.DEBUG_REF is turned on.
 * 
 * @author Wes Beebee <wbeebee@mit.edu>
 * @version $Id: RealtimeRuntime.java,v 1.5 2002-08-12 12:02:22 wbeebee Exp $
 */

public class RealtimeRuntime extends harpoon.Backend.Runtime1.Runtime {

    /** Create a RealtimeRuntime. */

    public RealtimeRuntime(Frame frame, AllocationStrategy as,
			   final HMethod main, final boolean prependUnderscore)
    {
	super(frame, as, main, prependUnderscore,
	      new RootOracle() {
		      public Object get(final HField hf, Info addlinfo) {
			  final HClass memoryArea = main
			      .getDeclaringClass().getLinker()
			      .forName("javax.realtime.ImmortalMemory");
			  final HClass hfClass = hf.getDeclaringClass();
			  final NameMap nm = 
			      new DefaultNameMap(prependUnderscore);
			  if (hfClass.getName().equals("java.lang.Object") &&
			      hf.getName().equals("memoryArea")) {
			      return new ObjectInfo() {
				      public HClass type() {
					  return memoryArea;
				      }
				      public Label label() {
					  return nm.label(hfClass, 
							  "constantMemoryArea");
				      }
				      public Object get(HField hff) {
					  return NOT_A_VALUE; // doesn't traverse
				      }
				  };
			  } else {
			      return NOT_A_VALUE;
			  }
		      }
		  });
    }

    /** Tag all classes with <code>javax.realtime.ImmortalMemory</code> 
     *  <code>java.lang.Object.memoryArea</code> that has a field 
     *  <code>javax.realtime.ImmortalMemory.constant = true;</code>
     *
     *  Also emit data to deal with const char*'s that can be created
     *  when <code>Realtime.DEBUG_REF</code>.
     */

    public List<HData> classData(final HClass hc) {
	class DataConstMemoryArea extends Data {
	    DataConstMemoryArea() {
		super("memArea-data", hc, RealtimeRuntime.this.frame);

		ObjectInfo constMemAreaObject = new ObjectInfo() {
			public HClass type() { return memoryArea; }
			public Label label() { return label; }
			public Object get(HField hf) {
			    if (hf.getName().equals("constant")) {
				return new Boolean(true);
			    } else if (hf.getType() == HClass.Boolean) {
				return new Boolean(false);
			    } else if (hf.getType() == HClass.Byte) {
				return new Byte((byte)0);
			    } else if (hf.getType() == HClass.Short) {
				return new Short((short)0);
			    } else if (hf.getType() == HClass.Int) {
				return new Integer(0);
			    } else if (hf.getType() == HClass.Long) {
				return new Long(0);
			    } else if (hf.getType() == HClass.Float) {
				return new Float(0.0);
			    } else if (hf.getType() == HClass.Double) {
				return new Double(0.0);
			    } else if (hf.getType() == HClass.Char) {
				return new Character(' ');
			    } else {
				return null;
			    }
			}

			final HClass memoryArea =
			    RealtimeRuntime.this.frame.getLinker()
			    .forName("javax.realtime.ImmortalMemory");
			final Label label = 
			    getNameMap().label(hc, "constantMemoryArea");
		    };

		List stmlist = new ArrayList();
		stmlist.add(new SEGMENT(tf, null, SEGMENT.STATIC_OBJECTS));
		stmlist.add(ob.buildObject(tf, constMemAreaObject, true));
		this.root = (HDataElement) Stm.toStm(stmlist);
	    }    
	}
	
	class DataConstCharPointer extends Data {
	    DataConstCharPointer() {
		super("constChar-data", hc, RealtimeRuntime.this.frame);

		this.root = (HDataElement)
		    RealtimeAllocationStrategy.emitStrings(tf, null);
	    }
	}
	
	List<HData> r = super.classData(hc);
	ArrayList<HData> datalst = new ArrayList<HData>(r.size() + (Realtime.DEBUG_REF?2:1));
	datalst.addAll(r);
	datalst.add(new DataConstMemoryArea());
	if (Realtime.DEBUG_REF) {
	    datalst.add(new DataConstCharPointer());
	}
	return datalst;
    }

    /** Initialize the tree builder with masking turned on if needed. */

    protected harpoon.Backend.Runtime1.TreeBuilder initTreeBuilder() {
	if (System.getProperty("harpoon.runtime", "1").equals("2")) {
	    return new harpoon.Backend.Runtime2
		.TreeBuilder(this, frame.getLinker(), as,
			     frame.pointersAreLong(), 
			     (Realtime.NOHEAP_CHECKS||Realtime.NOHEAP_MASK)?4:0) { };
	} else {
	    return new harpoon.Backend.Runtime1
		.TreeBuilder(this, frame.getLinker(), as,
			     frame.pointersAreLong(),
			     (Realtime.NOHEAP_CHECKS||Realtime.NOHEAP_MASK)?4:0) { };
	    
	}
    }

    public String resourcePath(String basename) {
	if (basename.equals("init-safe.properties")&&
	    Realtime.RTJ_PERF) {
	    return "harpoon/Analysis/Realtime/"+basename;
	} else {
	    return super.resourcePath(basename);
	}
    }
}
