// AllocationNumberingStub.java, created Sat Feb  1 19:45:56 2003 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

import harpoon.IR.Quads.Quad;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.Code;


/**
 * <code>AllocationNumberingStub</code> is a very simple
 * implementation of <code>AllocationNumberingInterf</code> designed
 * to avoid serialization (serialization is buggy in many, if not all,
 * JVMs).  Instead, <code>AllocationNumberingStub</code>s can be
 * <i>textualized</i> to/from an ASCII file.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: AllocationNumberingStub.java,v 1.4 2004-02-08 03:21:32 cananian Exp $
 */
public class AllocationNumberingStub implements AllocationNumberingInterf {

    // nobody can directly create such an object!
    private AllocationNumberingStub() {}
    
    /** Create an <code>AllocationNumberingStub</code> by parsing a
     text file.  This is the only approved way of creating an
     <code>AllocationNumberingStub</code> object.

     @param linker Linker; given a string, gives the
     <code>HClass</code> objects that represents the class with that
     name (this is used mostly for convenience: it's easier to work
     with <code>HClass</code>es and <code>HMethod</code>s instead of
     just strings).

     @param filename Name of the file that contains the
     <i>textualized</i> version of the
     <code>AllocationNumberingStub</code>
    */
    public AllocationNumberingStub(Linker linker, String filename)
	throws IOException {
	BufferedReader br = new BufferedReader(new FileReader(filename));
	int nb_methods = readInt(br);
	for(int i = 0; i < nb_methods; i++)
		readMethodData(linker, br);	
    }
    
    private final Map method2quadID2counter = new HashMap();
    
    private Map getMap4Method(HMethod hm) {
	Map map = (Map) method2quadID2counter.get(hm);
	if(map == null) {
	    map = new HashMap();
	    method2quadID2counter.put(hm, map);
	}
	return map;
    }
    
    public int allocID(Quad q) {
	HMethod hm = q.getFactory().getMethod();
	Integer allocID = 
	    (Integer) getMap4Method(hm).get(new Integer(q.getID()));
	if(allocID == null)
	    throw new UnknownAllocationSiteError
		("Quad unknown: " + q + " #" + q.getID());
	return allocID.intValue();
    }

        
    /////////////// PARSING METHODS ///////////////////////////////
    private void readMethodData(Linker linker, BufferedReader br) 
	throws IOException {
	HMethod hm = readMethod(linker, br);
	int nb_allocs = readInt(br);
	for(int i = 0; i < nb_allocs; i++) {
	    int quadID = readInt(br);
	    int counter = readInt(br);
	    getMap4Method(hm).put
		(new Integer(quadID), new Integer(counter));
	}
    }
    
    private static HClass readClass(Linker linker, BufferedReader br)
	throws IOException {
	String class_name = readString(br);
	HClass hc = (HClass) primitives.get(class_name);
	return (hc != null) ? hc : linker.forName(class_name);
    }
    
    private static Map primitives;
    static {
	primitives = new HashMap();
	primitives.put("boolean", HClass.Boolean);
	primitives.put("byte",    HClass.Byte);
	primitives.put("char"   , HClass.Char);
	primitives.put("double",  HClass.Double);
	primitives.put("float",   HClass.Float);
	primitives.put("int",     HClass.Int);
	primitives.put("long",    HClass.Long);
	primitives.put("short",   HClass.Short);
	primitives.put("void",    HClass.Void);
    }
    
    private static HMethod  readMethod(Linker linker, BufferedReader br)
	throws IOException {
	HClass hc = readClass(linker, br);
	String method_name = readString(br);
	int nb_params = readInt(br);
	HClass[] ptypes = new HClass[nb_params];
	for(int i = 0; i < nb_params; i++)
	    ptypes[i] = readClass(linker, br);
	return hc.getMethod(method_name, ptypes);
    }

    private static int readInt(BufferedReader br) throws IOException {
	return new Integer(br.readLine()).intValue();
    }

    private static String readString(BufferedReader br) throws IOException {
	br.readLine(); // eat the line added as comment
	int size = readInt(br);
	char[] chars = new char[size];
	for(int i = 0; i < size; i++)
	    chars[i] = (char) readInt(br);
	String str = new String(chars);
	return str;
    }


    ////////////////// MINI SERIALIZATON //////////////////////////////////
    /** Write into a file a simplified version of an
	AllocationNumbering object.  This method does a very simple
	serialization, I would call it <i>textualization</i>.  It
	outputs just enough information to retrieve the ID of each
	quad; the output has only ASCII characters.

	One can construct an <code>AllocationNumberingStub</code> stub
	from the textualized file image, using the appropriate
	constructor.

	@param an <code>AllocationNumbering</code> to textualize

	@param filename Name of the file to write the textualization into.

	@param linker Linker used to load the classes of the compiled
	program.  If non-null, it will be used to parse the file back
	into an <code>AllocationNumberingStub</code> and verify that
	the unique IDs for the allocation sites did not change.  If
	null, no verification will be performed.  */
    public static void writeToFile(AllocationNumbering an, String filename,
				   Linker linker) throws IOException {
	PrintWriter pw =
	    new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	Relation method2allocs = getMethod2Allocs(an);
	writeInt(pw, method2allocs.keys().size());
	for(Object hmO : method2allocs.keys()) {
	    HMethod hm = (HMethod) hmO;
	    writeMethodSignature(pw, hm);
	    writeAllocs(an, pw, method2allocs.getValues(hm));
	}
	pw.close();
	
	if(linker != null) {
	    AllocationNumberingStub ans = 
		new AllocationNumberingStub(linker, filename);
	    for(Object quadO : an.getAllocs()) {
		Quad quad = (Quad) quadO;
		assert 
		    an.allocID(quad) == ans.allocID(quad) :
		    "Textualization error";
	    }
	}
    }

    // produce a relation method m -> allocations sites inside m
    private static Relation getMethod2Allocs(AllocationNumbering an) {
	Relation method2allocs = new RelationImpl();
	for(Object quadO : an.getAllocs()) {
	    Quad quad = (Quad) quadO;
	    HMethod hm = quad.getFactory().getMethod();
	    method2allocs.add(hm, quad);
	}
	return method2allocs;
    }

    // write to pw a textual representation of hm's signature
    private static void writeMethodSignature(PrintWriter pw, HMethod hm) {
	writeClass(pw, hm.getDeclaringClass());
	writeString(pw, hm.getName());
	HClass[] ptypes = hm.getParameterTypes();
	writeInt(pw, ptypes.length);
	for(int i = 0; i < ptypes.length; i++)
	    writeClass(pw, ptypes[i]);
    }

    // write the name of the class hc to pw.
    private static void writeClass(PrintWriter pw, HClass hc) {
	writeString(pw, hc.getName());
    }

    // write to pw the unique identifiers for all allocations from allocs
    private static void writeAllocs(AllocationNumbering an,
				    PrintWriter pw, Collection allocs) {
	writeInt(pw, allocs.size());
	for(Object quadO : allocs) {
	    Quad quad = (Quad) quadO;
	    writeInt(pw, quad.getID());
	    writeInt(pw, an.allocID(quad));
	}
    }

    // write a single string to pw: the string is interpreted as an
    // array of characters (small ints).  We output the length of the
    // string (an integer), followed by the integers corresponding to
    // the characters from the string.  writeInt is employed for
    // displaying each integer.
    private static void writeString(PrintWriter pw, String str) {
	pw.println("// " + str); // added as a comment for debugging
	int length = str.length();
	writeInt(pw, length);
	for(int i = 0; i < length; i++)
	    writeInt(pw, (int) str.charAt(i));
    }

    // write one integer to pw; each integer is on a line on its own
    private static void writeInt(PrintWriter pw, int i) {
	pw.println(i);
    }
}
