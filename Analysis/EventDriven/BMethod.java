
/**
 * BMethod.java
 *
 *
 * Created: Sat Mar 18 22:06:48 2000
 *
 * @author root
 * @version
 */
package harpoon.Analysis.EventDriven;

import harpoon.ClassFile.HMethod;


public interface BMethod {
    public HMethod swop (final HMethod m);
    public HMethod[] blockingMethods();
}

