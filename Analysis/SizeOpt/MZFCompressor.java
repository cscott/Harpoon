// MZFCompressor.java, created Fri Nov  9 21:23:56 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HFieldMutator;
import harpoon.ClassFile.HInitializer;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.Util.Collections.SnapshotIterator;
import harpoon.Util.Default;
import harpoon.Util.Default.PairList;
import harpoon.Util.ParseUtil;
import harpoon.Util.ParseUtil.BadLineException;
import harpoon.Util.ParseUtil.StringParser;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * The <code>MZFCompressor</code> class implements a class transformation
 * aimed at eliminating "mostly-zero" (or "mostly (any constant)")
 * fields from classes (thus reducing memory consumption).  Each class
 * is transformed into several, each with one additional field added.
 * Thus we only need allocate fields that this particular instance
 * will actually use.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MZFCompressor.java,v 1.7 2002-09-03 16:40:23 cananian Exp $
 */
public class MZFCompressor {
    final HCodeFactory parent;
    final Set<HMethod> callable = new HashSet<HMethod>();
    final Set<HClass> allClasses = new HashSet<HClass>();
    
    /** Creates a <code>MZFCompressor</code>, using the field profiling
     *  information found in the resource at <code>resourcePath</code>.
     *  This resource should be a unprocessed file in the format output
     *  by the <code>SizeCounters</code> package. */
    public MZFCompressor(Frame frame, HCodeFactory hcf, ClassHierarchy ch,
			 String resourcePath) {
	Relinker linker = (Relinker) frame.getLinker();
	ConstructorClassifier cc = new ConstructorClassifier(hcf, ch);
        ProfileParser pp = new ProfileParser(linker, resourcePath);
	// get 'stop list' of classes we should not touch.
	Set<HClass> stoplist =
	    parseStopListResource(frame, "mzf-unsafe.properties");
	// process the profile data.
	// collect all good fields; sort them (within class) by savedbytes.
	Set<HField> flds = new HashSet<HField>();
	Map<HClass,List<PairList<HField,Number>>> listmap =
	    new HashMap<HClass,List<PairList<HField,Number>>>();
	for (Iterator<HClass> it=ch.instantiatedClasses().iterator();
	     it.hasNext(); ){
	    HClass hc = it.next();
	    if (stoplist.contains(hc)) continue; // skip this class.
	    List<PairList<HField,Number>> sorted = sortFields(hc, pp, cc);
	    if (sorted.size()>0) listmap.put(hc, sorted);
	    for (Iterator<PairList<HField,Number>> it2=sorted.iterator();
		 it2.hasNext(); )
		flds.add( (HField) it2.next().get(0) );
	}
	flds = Collections.unmodifiableSet(flds);
	listmap = Collections.unmodifiableMap(listmap);
	// before we change any classes (in Field2Method, below)
	// pull all callable constructors through the ConstructorClassifier
	// to cache their results.
	this.callable.addAll(ch.callableMethods());
	for (Iterator<HMethod> it=callable.iterator(); it.hasNext(); ) {
	    HMethod hm = it.next();
	    if (isConstructor(hm)) cc.isGood(hm);
	}
	// make accessors for the 'good' fields.
	Field2Method f2m = new Field2Method(hcf, flds);
	hcf = new CachingCodeFactory(f2m.codeFactory());
	// add getters and setters to 'callable' set.
	callable.addAll(f2m.getter2field.keySet());
	callable.addAll(f2m.setter2field.keySet());
	// pull every callable method of each relevant class through hcf
	for (Iterator<HClass> it=listmap.keySet().iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
	    for (Iterator<HMethod> it2=Arrays.asList(hc.getDeclaredMethods())
		     .iterator(); it2.hasNext(); ) {
		HMethod hm = it2.next();
		if (callable.contains(hm))
		    hcf.convert(hm);
	    }
	}
	// okay.  foreach relevant class, split it.
	Map<HField,HClass> field2class = new HashMap<HField,HClass>();
	this.allClasses.addAll(ch.classes());
	for (Iterator<HClass> it=listmap.keySet().iterator(); it.hasNext(); ) {
	    HClass hc = it.next();
	    splitOne(linker, (CachingCodeFactory) hcf,
		     hc, listmap.get(hc), f2m, field2class);
	}
	field2class = Collections.unmodifiableMap(field2class);
	// chain through MZFWidenType to change INSTANCEOF, ANEW, and
	// TYPESWITCH quads to use the new supertype of a split class.
	hcf = new MZFWidenType(hcf,linker, listmap, field2class,
			       callable, allClasses).codeFactory();
	// chain through MZFChooser to pick the appropriate superclass
	// at each instantiation site.
	hcf = new MZFChooser(hcf, cc, listmap, field2class).codeFactory();
	// cache, to be safe.
	hcf = new CachingCodeFactory(hcf);
	// now delete fields which we can represent using an external hashtable
	hcf = new MZFExternalize(hcf,linker, pp, stoplist, flds).codeFactory();
	// we should be done now.
	this.parent = new CachingCodeFactory(hcf);
    }
    public HCodeFactory codeFactory() { return parent; }

    /** Return a list.  element 0 of the list is the field most likely
     *  to save the most bytes. Elements are pairs; first element is
     *  the field; second element is the 'mostly value'. */
    List<PairList<HField,Number>> sortFields(HClass hc, ProfileParser pp,
					      ConstructorClassifier cc) {
	// make a comparator that operates on Map.Entries contained in the
	// ProfileParser's savedBytesMap map, which sorts on 'saved bytes'.
	final Comparator c = new Comparator() {
		public int compare(Object o1, Object o2) {
		    // compare based on values in the entry; these
		    // are Longs representing 'saved bytes'
		    return ((Comparable) ((Map.Entry)o1).getValue())
			.compareTo(((Map.Entry)o2).getValue());
		}
	    };
	// okay, now make a list of fields and their maximal 'saved bytes'
	// mostly-N entry.
	HField[] flds = hc.getDeclaredFields();
	List l = new ArrayList(flds.length);
	for (Iterator it=Arrays.asList(flds).iterator(); it.hasNext(); ) {
	    HField hf = (HField) it.next();
	    // filter out 'bad' fields.
	    if (!cc.isGood(hf)) continue;
	    // some fields have no information?
	    if (pp.savedBytesMap(hf).entrySet().size()==0)
		// we can end up with no information about a field if it is
		// instantiable but never actually instantiated in the program
		// (typically because it is instantiated in native code).
		continue; // skip these fields.
	    // find the 'mostly value' which will save the most space.
	    Map.Entry me = (Map.Entry)
		Collections.max(pp.savedBytesMap(hf).entrySet(), c);
	    // add an entry to l for sorting.
	    l.add(Default.pair(hf, me));
	}
	// okay, now sort the fields by saved bytes.
	Collections.sort(l, new Comparator() {
		// compare second element of pair.
		// (negate comparator so largest 'saved bytes' field is first)
		public int compare(Object o1, Object o2) {
		    return -c.compare(((List)o1).get(1), ((List)o2).get(1));
		}
	    });
	// finally, create a final list by stripping the 'saved bytes' info
	// (which we no longer need) and keeping only the field/mostlyN info.
	List<PairList<HField,Number>> nl =
	    new ArrayList<PairList<HField,Number>>(l.size());
	for (Iterator it=l.iterator(); it.hasNext(); ) {
	    List pair = (List) it.next();
	    HField hf = (HField) pair.get(0);
	    Map.Entry me = (Map.Entry) pair.get(1);
	    Number mostlyN = (Number) me.getKey();
	    nl.add(Default.pair(hf, mostlyN));
	}
	nl = Collections.unmodifiableList(nl);
	// ta-da!
	return nl;
    }

    // splits one class.
    void splitOne(Relinker relinker, CachingCodeFactory hcf,
		  HClass hc, List<PairList<HField,Number>> sortedFields,
		  Field2Method f2m, Map<HField,HClass> field2class) {
	assert sortedFields.size()>0;
	// for each entry in the sorted fields list, make a split.
	for (Iterator<PairList<HField,Number>> it=sortedFields.iterator();
	     it.hasNext(); ) {
	    List pair = it.next();
	    hc = moveOne(relinker, hcf, hc,
			 (HField)pair.get(0),
			 ((Number)pair.get(1)).longValue(),
			 f2m, field2class);
	    allClasses.add(hc);
	}
	// done!
    }
    /** Create a class with all the fields of <code>oldC</code> except for
     *  <code>hf</code>.  In the new class, <code>hf</code> has constant
     *  value <code>val</code>. */
    HClass moveOne(Relinker relinker, CachingCodeFactory hcf,
		   HClass oldC, HField hf, long val,
		   Field2Method f2m, Map<HField,HClass> field2class) {
	assert !field2class.containsKey(hf);
	// make a copy of our empty Template class.
	HClass hcT = relinker.forClass(Template.class);
	HClass newC = relinker.createMutableClass
	    (oldC.getName()+"$$"+hf.getName(), hcT);
	// remove all constructors from newC (since we're going to
	// clone them from oldC)
	for (Iterator<HConstructor> it =
		 Arrays.asList(newC.getConstructors()).iterator();
	     it.hasNext(); )
	    newC.getMutator().removeConstructor(it.next());
	// insert this new class between hcS and its superclass.
	newC.getMutator().setSuperclass(oldC.getSuperclass());
	oldC.getMutator().setSuperclass(newC);
	// move interfaces from oldC to newC.
	for (Iterator<HClass> it =
		 Arrays.asList(oldC.getInterfaces()).iterator();
	     it.hasNext(); )
	    newC.getMutator().addInterface(it.next());
	oldC.getMutator().removeAllInterfaces();
	// fetch representations for everything callable before we start
	// messing with the fields.
	for (Iterator<HMethod> it =
		 Arrays.asList(oldC.getDeclaredMethods()).iterator();
	     it.hasNext(); ) {
	    HMethod hm = it.next();
	    if (callable.contains(hm))
		hcf.convert(hm);
	}
	// move all but the desired field to newC
	// (also strip 'private' modifier)
	HField[] allF = oldC.getDeclaredFields();
	for (int i=0; i<allF.length; i++)
	    if (!hf.equals(allF[i])) {
		relinker.move(allF[i], newC);
		allF[i].getMutator().removeModifiers(Modifier.PRIVATE);
	    }
	// move all non-constructor non-static methods of oldC to newC
	// (move static initializers to newC, since it has the static fields)
	// copy the constructors.
	HMethod[] allM = oldC.getDeclaredMethods();
	HMethod getter = f2m.field2getter.get(hf);
	HMethod setter = f2m.field2setter.get(hf);
	for (int i=0; i<allM.length; i++) {
	    if (allM[i].isStatic() && !(allM[i] instanceof HInitializer))
		continue;
	    if (isConstructor(allM[i])) {
		// copy, don't move.
		HMethod newcon = (allM[i] instanceof HConstructor) ?
		    newC.getMutator().addConstructor
		      ((HConstructor)allM[i]) :
		    newC.getMutator().addDeclaredMethod
		      (allM[i].getName(), allM[i]);
		if (callable.contains(allM[i])) {
		    harpoon.IR.Quads.Code hcode =
			(harpoon.IR.Quads.Code) hcf.convert(allM[i]);
		    hcf.put(newcon, hcode.clone(newcon).hcode());
		    callable.add(newcon);
		}
	    } else relinker.move(allM[i], newC);
	}	    
	// getter and setter are now in newC.  copy implementation to oldC.
	HMethod fullgetter =
	    oldC.getMutator().addDeclaredMethod(getter.getName(), getter);
	HMethod fullsetter =
	    oldC.getMutator().addDeclaredMethod(setter.getName(), setter);
	harpoon.IR.Quads.Code gettercode = (harpoon.IR.Quads.Code)
	    hcf.convert(getter);
	harpoon.IR.Quads.Code settercode = (harpoon.IR.Quads.Code)
	    hcf.convert(setter);
	hcf.put(fullgetter, gettercode.clone(fullgetter).hcode());
	hcf.put(fullsetter, settercode.clone(fullsetter).hcode());
	// rewrite newC's getter and setter.
	hcf.put(getter, makeGetter(hcf, getter, hf, val));
	hcf.put(setter, makeSetter(hcf, setter, hf, val));
	// add copied oldC getter/setter to callable set.
	callable.add(fullgetter);
	callable.add(fullsetter);
	// done!
	field2class.put(hf, newC);
	return newC;
    }
    static class Template { }

    /** make a getter method that returns constant 'val'. */
    HCode<Quad> makeGetter(HCodeFactory hcf,
			   HMethod getter, HField hf, long val) {
	// xxx cheat: get old getter and replace GET with CONST.
	// would be better to make this from scratch.
	HCode<Quad> hc = hcf.convert(getter);
	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (hc.getElementsI()); it.hasNext(); ) {
	    Quad aquad = it.next();
	    if (aquad instanceof GET) {
		GET q = (GET) aquad;
		assert q.field().equals(hf);
		// type of CONST depends on type of hf.
		HClass type=widen(hf.getType());
		CONST nc;
		if (!type.isPrimitive()) {
		    // pointer.  only val==0 makes sense.
		    assert val==0;
		    nc = new CONST(q.getFactory(), q, q.dst(),
				   null, HClass.Void);
		} else
		    nc = new CONST(q.getFactory(), q, q.dst(),
				   wrap(type, val), type);
		Quad.replace(q, nc);
	    }
	}
	// done!
	return hc;
    }
    static HClass widen(HClass hc) {
	if (hc==HClass.Boolean || hc==HClass.Byte ||
	    hc==HClass.Short || hc==HClass.Char)
	    return HClass.Int;
	else return hc;
    }
    static Object wrap(HClass type, long val) {
	return wrap(type, new Long(val));
    }
    static Object wrap(HClass type, Number n) {
	assert type.isPrimitive() : type;
	if (type==HClass.Int) return new Integer(n.intValue());
	if (type==HClass.Long) return new Long(n.longValue());
	if (type==HClass.Float) return new Float(n.floatValue());
	if (type==HClass.Double) return new Double(n.doubleValue());
	assert false : ("unknown type: "+type);
	return null;
    }
    /** make a setter method that does nothing (but perhaps verifies
     *  that the value to set is equal to 'val'). */
    HCode<Quad> makeSetter(HCodeFactory hcf,
			   HMethod setter, HField hf, long val) {
	// xxx cheat: get old setter and remove the SET operand.
	// would be better to make this from scratch.
	HCode<Quad> hc = hcf.convert(setter);
	for (Iterator<Quad> it=new SnapshotIterator<Quad>
		 (hc.getElementsI()); it.hasNext(); ) {
	    Quad q = it.next();
	    if (q instanceof SET)
		q.remove();
	}
	// done!
	return hc;
    }
    // utility.
    ///////// copied from harpoon.Analysis.Quads.DefiniteInitOracle.
    /** return an approximation to whether this is a constructor
     *  or not.  it's always safe to return false. */
    private static boolean isConstructor(HMethod hm) {
	// this is tricky, because we want split constructors to
	// count, too, even though renamed constructors (such as
	// generated by initcheck, for instance) won't always be
	// instanceof HConstructor.  Look for names starting with
	// '<init>', as well.
	if (hm instanceof HConstructor) return true;
	if (hm.getName().startsWith("<init>")) return true;
	// XXX: what about methods generated by RuntimeMethod Cloner?
	// we could try methods ending with <init>, but then the
	// declaringclass information would be wrong.
	//if (hm.getName().endsWidth("<init>")) return true;//not safe yet.
	return false;
    }
    //---------------------------------------------
    /** Parse a "stop list" of classes we should not attempt to optimize */
    Set<HClass> parseStopListResource(final Frame frame, String resname) {
	final Set<HClass> stoplist = new HashSet<HClass>();
	try {
	ParseUtil.readResource
	    (frame.getRuntime().resourcePath(resname),
	     new StringParser() {
		 public void parseString(String s) throws BadLineException {
		     stoplist.add(ParseUtil.parseClass(frame.getLinker(), s));
		 }
	     });
	} catch (java.io.IOException ioex) {
	    System.err.println("SKIPPING REST OF "+resname+": "+ioex);
	}
	return stoplist;
    }
}
