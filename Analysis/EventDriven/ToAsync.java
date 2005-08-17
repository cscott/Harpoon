// ToAsync.java, created Thu Nov 11 12:41:56 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.TypeInfo;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.MetaMethods.MetaAllCallers;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.Analysis.ContBuilder.ContBuilder;
import harpoon.Analysis.EnvBuilder.EnvBuilder;
import jpaul.DataStructs.Relation;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSI;
import harpoon.Temp.Temp;
import harpoon.Util.BasicBlocks.BBConverter;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import net.cscott.jutil.WorkSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>ToAsync</code>
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: ToAsync.java,v 1.5 2005-08-17 23:40:51 salcianu Exp $
 */
public class ToAsync {
    protected final CachingCodeFactory ucf;
    protected final HCode hc;
    protected final ClassHierarchy ch;
    protected final Linker linker;
    protected boolean optimistic;
    protected HCodeFactory hcf;
    protected boolean recycle;
    protected Set classes;

    Set blockingmm;
    MetaCallGraph mcg;



    /** Creates a <code>ToAsync</code>. */
    public ToAsync(CachingCodeFactory ucf, HCode hc, ClassHierarchy ch, Linker linker, boolean optimistic, MetaCallGraph mcg, boolean recycle, Set classes) {
	this.linker=linker;
        this.ucf = ucf;
	this.hc = hc;
	this.ch = ch;
	this.optimistic=optimistic;
	this.mcg=mcg;
	this.recycle=recycle;
	this.classes=classes;
    }
    
    public void metaStuff(BMethod bm) {
	//using hcf for now!
	//BBConverter bbconv=new BBConverter(hcf);
	//mcg=new MetaCallGraphImpl(bbconv, ch, hc.getMethod());
	MetaAllCallers mac=new MetaAllCallers(mcg);
	HMethod[] bmethods=bm.blockingMethods();

	WorkSet mm=new WorkSet();
	Relation mrelation=mcg.getSplitRelation();
	for (int i=0;i<bmethods.length;i++) {
	    mm.addAll(mrelation.getValues(bmethods[i]));
	}
	blockingmm=new WorkSet(mm);
	for (Iterator i=mm.iterator();i.hasNext();) {
	    MetaMethod[] mma=mac.getTransCallers((MetaMethod)i.next());
	    blockingmm.addAll(java.util.Arrays.asList(mma));
	}
	for (Iterator i=blockingmm.iterator();i.hasNext();) {
	    System.out.println(i.next()+" BLOCKS");
	}
    }

    public HMethod transform() {
	System.out.println("Entering ToAsync.transform()");
	Set blockingcalls;

	AllCallers.MethodSet bm = optimistic?((AllCallers.MethodSet)new BlockingMethodsOpt(linker)):((AllCallers.MethodSet)new BlockingMethods(linker));
	
	if (mcg==null) {
	    AllCallers ac = new AllCallers(ch, ucf);
	    blockingcalls = ac.getCallers(bm);

	} else {

	    metaStuff((BMethod)bm);
	    //CHEAP MetaHack...
	    //Real algorithm should work on a callsite basis.
	    blockingcalls=new WorkSet();
	    for (Object mmO : blockingmm) {
		MetaMethod mm = (MetaMethod) mmO;
		blockingcalls.add(mm.getHMethod());
	    }
	}

	HashMap old2new=new HashMap();
	HMethod nhm=AsyncCode.makeAsync(old2new, hc.getMethod(),
					ucf,linker,optimistic);
	
	WorkSet async_todo=new WorkSet();
	async_todo.push(hc);

	WorkSet other=new WorkSet();
	WorkSet done_other=new WorkSet();
	   
	while (!async_todo.isEmpty()|!other.isEmpty()) {
	    QuadSSI selone=null;
	    boolean status=false;
	    if (async_todo.isEmpty()) {
		HMethod hm=(HMethod) other.pop();
		done_other.push(hm);
		selone=(QuadSSI) ucf.convert(hm);
		if (selone==null)
		    continue;
		status=true;
	    } else
		selone=(QuadSSI) async_todo.pop();
	    final QuadLiveness ql = new QuadLiveness(selone);
	    final TypeMap typemap=new TypeInfo(selone);

	    System.out.println("ToAsync is running AsyncCode on "+selone);
	    AsyncCode.buildCode(selone, old2new, async_todo,
				ql,blockingcalls,ucf,  bm, hc.getMethod(), 
				linker,ch,other, done_other,status, typemap, 
				optimistic,recycle,classes);
	}
	return nhm;
    }



    static class BlockingMethods implements AllCallers.MethodSet,BMethod  {
	final Linker linker;
	public BlockingMethods(Linker linker) {
	    this.linker=linker;
	}

	/** Returns true if the <code>HMethod</code> blocks, false
	 *  otherwise. Checks against a list of known blocking methods.
	 */
	public boolean select (final HMethod m) {
	    if (swop(m) != null) {
		System.out.println(m.toString());
		return true;
	    } else
		return false;
	}

	/** Returns the corresponding asynchronous method for a given
	 *  blocking method if one exists.
	 */
	final private Map cache = new HashMap();

	public HMethod[] blockingMethods() {
	    final HClass is = linker.forName("java.io.InputStream");
	    final HClass fis = linker.forName("java.io.FileInputStream");
	    final HClass os = linker.forName("java.io.OutputStream");
	    final HClass fos = linker.forName("java.io.FileOutputStream");
	    final HClass ss = linker.forName("java.net.ServerSocket");
	    final HClass fd = linker.forName("java.io.FileDescriptor");
	    final HClass b = HClass.Byte;
	    final HClass HCthrd = linker.forName("java.lang.Thread");

	    return new HMethod[] {
		fd.getMethod("sync", new HClass[0]),
		is.getMethod("skip", new HClass[]{HClass.Long}),
		HCthrd.getMethod("join", new HClass[0]),
		    
		is.getDeclaredMethod("read", new HClass[0]),

		is.getDeclaredMethod("read", 
		   new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),

		is.getDeclaredMethod("read", 
		   new HClass[] {HClassUtil.arrayClass(linker, b, 1),
				 HClass.Int, HClass.Int}),

		fis.getDeclaredMethod("read", new HClass[0]),
			    
		fis.getDeclaredMethod("read", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),

		fis.getDeclaredMethod("read", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
				  HClass.Int, HClass.Int}),

		os.getDeclaredMethod("write", new HClass[]{HClass.Int}),

	        os.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),

		os.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
		    HClass.Int, HClass.Int}),

		fos.getDeclaredMethod("write", new HClass[]{HClass.Int}),

		fos.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),
		    
		fos.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
		    HClass.Int, HClass.Int}),

		ss.getDeclaredMethod("accept", new HClass[0])
		    };
	}

	public HMethod swop (final HMethod m) {
	    final HClass is = linker.forName("java.io.InputStream");
	    final HClass fis = linker.forName("java.io.FileInputStream");
	    final HClass os = linker.forName("java.io.OutputStream");
	    final HClass fos = linker.forName("java.io.FileOutputStream");
	    final HClass ss = linker.forName("java.net.ServerSocket");
	    final HClass fd = linker.forName("java.io.FileDescriptor");
	    final HClass b = HClass.Byte;
	    final HClass HCthrd = linker.forName("java.lang.Thread");

	    HMethod retval = (HMethod)cache.get(m);
	    if (retval == null) {
		if(m.equals(fd.getMethod("sync",
					 new HClass[0]))) {
		    retval=fd.getMethod("syncAsync", new HClass[0]);
		    cache.put(m,retval);
		} else if (m.equals(is.getMethod("skip",
						 new HClass[]{HClass.Long}))) {
		    retval=is.getMethod("skipAsync", new HClass[]{HClass.Long});
		    cache.put(m,retval);
		} else if (m.equals(HCthrd.getMethod("join",
						     new HClass[0]))) {
		    retval=HCthrd.getMethod("join_Async",
					    new HClass[0]);
		    cache.put(m, retval);
		} else if (is.equals(m.getDeclaringClass())) {
		    if (m.getName().equals("read")) {
			final HMethod bm1 = 
			    is.getDeclaredMethod("read", new HClass[0]);
			final HMethod bm2 = is.getDeclaredMethod("read", 
                            new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = is.getDeclaredMethod("read", 
			    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					  HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = is.getMethod("readAsync", 
						  new HClass[0]);
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = is.getMethod("readAsync", 
			        new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = is.getMethod("readAsync", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (fis.equals(m.getDeclaringClass())) {
		    if (m.getName().equals("read")) {
			final HMethod bm1 = 
			    fis.getDeclaredMethod("read", new HClass[0]);
			final HMethod bm2 = fis.getDeclaredMethod("read", 
                            new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = fis.getDeclaredMethod("read", 
			    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					  HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = fis.getMethod("readAsync", 
						  new HClass[0]);
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = fis.getMethod("readAsync", 
			        new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = fis.getMethod("readAsync", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (os.equals(m.getDeclaringClass())){
		    if (m.getName().equals("write")) {
			final HMethod bm1 = 
			    os.getDeclaredMethod("write", new HClass[]{HClass.Int});
			final HMethod bm2 = 
			    os.getDeclaredMethod("write", 
						 new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = 
			    os.getDeclaredMethod("write", 
						 new HClass[] {HClassUtil.arrayClass(linker, b, 1),
							       HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = os.getMethod("writeAsync", 
						  new HClass[] {HClass.Int});
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = os.getMethod("writeAsync", 
				  new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = os.getMethod("writeAsync", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (fos.equals(m.getDeclaringClass())){
		    if (m.getName().equals("write")) {
			final HMethod bm1 = 
			    fos.getDeclaredMethod("write", new HClass[]{HClass.Int});
			final HMethod bm2 = 
			    fos.getDeclaredMethod("write", 
						  new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = 
			    fos.getDeclaredMethod("write", 
						 new HClass[] {HClassUtil.arrayClass(linker, b, 1),
							       HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = fos.getMethod("writeAsync", 
						  new HClass[]{HClass.Int});
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = fos.getMethod("writeAsync", 
				  new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = fos.getMethod("writeAsync", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (ss.equals(m.getDeclaringClass())) {
		    final HMethod bm4 = 
			ss.getDeclaredMethod("accept", new HClass[0]);
		    if (bm4.equals(m)) {
			retval = ss.getDeclaredMethod("acceptAsync", 
						      new HClass[0]);
			cache.put(m, retval);
		    }
		}
	    }
	    return retval;
	} // swop

    } // BlockingMethods

    static class BlockingMethodsOpt implements AllCallers.MethodSet,BMethod {
	final Linker linker;
	public BlockingMethodsOpt(Linker linker) {
	    this.linker=linker;
	}

	/** Returns true if the <code>HMethod</code> blocks, false
	 *  otherwise. Checks against a list of known blocking methods.
	 */
	public boolean select (final HMethod m) {
	    if (swop(m) != null) {
		System.out.println(m.toString());
		return true;
	    } else
		return false;
	}


	public HMethod[] blockingMethods() {
	    final HClass is = linker.forName("java.io.InputStream");
	    final HClass fis = linker.forName("java.io.FileInputStream");
	    final HClass os = linker.forName("java.io.OutputStream");
	    final HClass fos = linker.forName("java.io.FileOutputStream");
	    final HClass ss = linker.forName("java.net.ServerSocket");
	    final HClass fd = linker.forName("java.io.FileDescriptor");
	    final HClass b = HClass.Byte;
	    final HClass HCthrd = linker.forName("java.lang.Thread");

	    return new HMethod[] {
		fd.getMethod("sync", new HClass[0]),
		is.getMethod("skip", new HClass[]{HClass.Long}),
		HCthrd.getMethod("join", new HClass[0]),
		    
		is.getDeclaredMethod("read", new HClass[0]),

		is.getDeclaredMethod("read", 
		   new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),

		is.getDeclaredMethod("read", 
		   new HClass[] {HClassUtil.arrayClass(linker, b, 1),
				 HClass.Int, HClass.Int}),

		fis.getDeclaredMethod("read", new HClass[0]),
			    
		fis.getDeclaredMethod("read", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),

		fis.getDeclaredMethod("read", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
				  HClass.Int, HClass.Int}),

		os.getDeclaredMethod("write", new HClass[]{HClass.Int}),

	        os.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),

		os.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
		    HClass.Int, HClass.Int}),

		fos.getDeclaredMethod("write", new HClass[]{HClass.Int}),

		fos.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1)}),
		    
		fos.getDeclaredMethod("write", 
		    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
		    HClass.Int, HClass.Int}),
		    ss.getDeclaredMethod("accept", new HClass[0])
		    };
	}

	/** Returns the corresponding asynchronous method for a given
	 *  blocking method if one exists.
	 */
	final private Map cache = new HashMap();
	public HMethod swop (final HMethod m) {
	    final HClass is = linker.forName("java.io.InputStream");
	    final HClass fis = linker.forName("java.io.FileInputStream");
	    final HClass os = linker.forName("java.io.OutputStream");
	    final HClass fos = linker.forName("java.io.FileOutputStream");
	    final HClass ss = linker.forName("java.net.ServerSocket");
	    final HClass fd = linker.forName("java.io.FileDescriptor");
	    final HClass b = HClass.Byte;
	    final HClass HCthrd = linker.forName("java.lang.Thread");

	    HMethod retval = (HMethod)cache.get(m);
	    if (retval == null) {
		if(m.equals(fd.getMethod("sync",
					 new HClass[0]))) {
		    retval=fd.getMethod("syncAsyncO", new HClass[0]);
		    cache.put(m,retval);
		} else if (m.equals(is.getMethod("skip",
						 new HClass[]{HClass.Long}))) {
		    retval=is.getMethod("skipAsyncO", new HClass[]{HClass.Long});
		    cache.put(m,retval);
		} else if (m.equals(HCthrd.getMethod("join",
						new HClass[0]))) {
			retval=HCthrd.getMethod("join_AsyncO",
					    new HClass[0]);

		    cache.put(m, retval);
		} else if (is.equals(m.getDeclaringClass())) {
		    if (m.getName().equals("read")) {
			final HMethod bm1 = 
			    is.getDeclaredMethod("read", new HClass[0]);
			final HMethod bm2 = is.getDeclaredMethod("read", 
                            new HClass[] {HClassUtil.arrayClass(linker,b, 1)});
			final HMethod bm3 = is.getDeclaredMethod("read", 
			    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					  HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = is.getMethod("readAsyncO", 
						  new HClass[0]);
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = is.getMethod("readAsyncO", 
			        new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = is.getMethod("readAsyncO", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (fis.equals(m.getDeclaringClass())) {
		    if (m.getName().equals("read")) {
			final HMethod bm1 = 
			    fis.getDeclaredMethod("read", new HClass[0]);
			final HMethod bm2 = fis.getDeclaredMethod("read", 
                            new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = fis.getDeclaredMethod("read", 
			    new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					  HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = fis.getMethod("readAsyncO", 
						  new HClass[0]);
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = fis.getMethod("readAsyncO", 
			        new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = fis.getMethod("readAsyncO", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (os.equals(m.getDeclaringClass())){
		    if (m.getName().equals("write")) {
			final HMethod bm1 = 
			    os.getDeclaredMethod("write", new HClass[]{HClass.Int});
			final HMethod bm2 = 
			    os.getDeclaredMethod("write", 
						 new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = 
			    os.getDeclaredMethod("write", 
						 new HClass[] {HClassUtil.arrayClass(linker, b, 1),
							       HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = os.getMethod("writeAsyncO", 
						  new HClass[] {HClass.Int});
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = os.getMethod("writeAsyncO", 
				  new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = os.getMethod("writeAsyncO", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (fos.equals(m.getDeclaringClass())){
		    if (m.getName().equals("write")) {
			final HMethod bm1 = 
			    fos.getDeclaredMethod("write", new HClass[]{HClass.Int});
			final HMethod bm2 = 
			    fos.getDeclaredMethod("write", 
						  new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			final HMethod bm3 = 
			    fos.getDeclaredMethod("write", 
						 new HClass[] {HClassUtil.arrayClass(linker, b, 1),
							       HClass.Int, HClass.Int});
			if (bm1.equals(m)) {
			    retval = fos.getMethod("writeAsyncO", 
						  new HClass[]{HClass.Int});
			    cache.put(m, retval);
			} else if (bm2.equals(m)) {
			    retval = fos.getMethod("writeAsyncO", 
				  new HClass[] {HClassUtil.arrayClass(linker, b, 1)});
			    cache.put(m, retval);
			} else if (bm3.equals(m)) {
			    retval = fos.getMethod("writeAsyncO", 
				new HClass[] {HClassUtil.arrayClass(linker, b, 1),
					      HClass.Int, HClass.Int});
			    cache.put(m, retval);
			}
		    }
		} else if (ss.equals(m.getDeclaringClass())) {
		    final HMethod bm4 = 
			ss.getDeclaredMethod("accept", new HClass[0]);
		    if (bm4.equals(m)) {
			retval = ss.getDeclaredMethod("acceptAsyncO", 
						      new HClass[0]);
			cache.put(m, retval);
		    }
		}
	    }
	    return retval;
	} // swop

    } // BlockingMethodsOpt

} // ToAsync



