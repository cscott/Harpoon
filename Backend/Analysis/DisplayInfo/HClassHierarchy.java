package harpoon.Backend.Analysis.DisplayInfo;

import harpoon.ClassFile.*;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 *
 * @author Duncan Bryce
 * @see harpoon.Backend.Analysis.HClassInfo
 */
public class HClassHierarchy
{
  private Hashtable m_classes;

  public HClassHierarchy() 
    {
      m_classes = new Hashtable();
    }
  
  /**
   * @param hc  an <code>HClass</code> object representing the 
   *            class of interest
   * @return    an <code>HClassInfo</code> object describing the
   *            class given by the parameter
   */
  public HClassInfo getClassInfo(HClass hc)
    {
      Object hci = m_classes.get(hc.getName());
      if (hci == null)
	{
	  hci = new HClassInfo(hc);
	  m_classes.put(hc.getName(), hci);
	  for (hc=hc.getSuperclass(); hc != null; hc=hc.getSuperclass())
	    {
	      m_classes.put(hc.getName(), new HClassInfo(hc));
	    }
	}
	  
      return (HClassInfo)hci;
    }

  /**
   * @param name  the name of the class 
   * @return an <code>HClassInfo</code> object describing the class
   *         named by the parameter
   */
  public HClassInfo getClassInfo(String name)
    {
      Object hci = m_classes.get(name);
      if (hci == null)
	{
	  HClass hc = HClass.forName(name);
	  hci = new HClassInfo(hc);
	  m_classes.put(name, hci);
	  for (hc=hc.getSuperclass(); hc != null; hc=hc.getSuperclass())
	    {
	      m_classes.put(hc.getName(), new HClassInfo(hc));
	    }
	}
	  
      return (HClassInfo)hci;
    }

  /**
   * @return a human-readable representation of this 
   * <code>HClassHierarchy</code>
   */
  public String toString()
    {
      StringBuffer sb = new StringBuffer("");
      for (Enumeration e = m_classes.elements(); e.hasMoreElements();)
	{
	  sb.append(((HClassInfo)(e.nextElement())).toString());
	  sb.append("\n");
	}

      return sb.toString();
    }
}

