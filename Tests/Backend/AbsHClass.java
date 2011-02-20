package harpoon.ClassFile;



/**
 * AbsHClass.java is a stub HClass (representing an interface) to allow
 * for direct-to-instr compilation.
 *
 *
 * Created: Mon Jul 24 18:59:05 2000
 *
 * @author Felix S. Klock
 * @version
 */
public class AbsHClass extends HClassImpl {
    
    public HMethod hm;

    public AbsHClass() {
	super(Loader.systemLinker);
    }

    public boolean isInterface() { return true; }
    public String getName() {  return "AbsClass"; }
    public String getDescriptor() { return ""; }
    public HField[] getDeclaredFields() { return new HField[0]; }
    public HMethod[] getDeclaredMethods() {return new HMethod[]{hm};}
    public int getModifiers() { return 0; }
    public HClass getSuperclass() { return null; }
    public HClass[] getInterfaces() { return new HClass[0]; }
    
} // AbsHClass





