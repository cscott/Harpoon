package harpoon.Backend.Analysis.DisplayInfo;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Backend.Maps.InlineMap;
import harpoon.Backend.Maps.OffsetMap;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * The <code>HClassInfo</code> class provides various useful bits of 
 * information about an <code>HClass</code>.  When the <code>HClassInfo</code>
 * object for some <code>HClass</code> is requested, the 
 * <code>HClassInfo</code> object is cached, as are <code>HClassInfo</code>'s 
 * for everything above the <code>HClass</code> in the class hierarchy.  
 *
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version 1.1.2.5
 * @see     harpoon.ClassFile.HClass
 */
public class HClassInfo
{
  private static int         initial_field_offset  = 0;
  private static int         initial_method_offset = 0;
  private static Hashtable   cached_classes        = new Hashtable();
  private static OffsetMap   offset_map;
  private static InlineMap   inline_map;

  private HClass             m_hClass;
  private HClassInfo         m_scInfo;
  private int                m_depth;
  private Hashtable          m_classObjects;
 
  private int                m_currentMethodOffset       = 0;
  private int                m_currentFieldOffset        = 0;
  private int                m_currentStaticFieldOffset  = 0;

  /**
   * @return an <code>HClassInfo</code> object representing
   *         the specified class. 
   */
  public static HClassInfo getClassInfo(String name)
    {
      Object hClassInfo;
      HClass hc;

      hc = HClass.forName(name);
      hClassInfo = cached_classes.get(hc);
      if (hClassInfo == null)
	{
	  hClassInfo = HClassInfo.getClassInfo(hc);
	}
      return (HClassInfo)hClassInfo;
    }

  /**
   * @return an <code>HClassInfo</code> object representing
   *         the specified class. 
   */
  public static HClassInfo getClassInfo(HClass hc)
    {
      Object      hClassInfo;
      HClass      superclass;
      HClassInfo  superclassInfo;

      hClassInfo = cached_classes.get(hc);
      if (hClassInfo == null)
	{
	  superclass = hc.getSuperclass();
	  if (superclass == null)
	    {
	      hClassInfo = new HClassInfo(hc);
	    }
	  else
	    {
	      hClassInfo = new HClassInfo(getClassInfo(superclass), hc);
	    }
	  cached_classes.put(hc, hClassInfo);
	}
      return (HClassInfo)hClassInfo;
    }

  /**
   * Clears the <code>HClassInfo</code> cache
   */
  public static void resetCache()
    {
      cached_classes = new Hashtable();
    }

  public static void setInitialFieldOffset(int offset)
    {
      initial_field_offset = offset;
    }

  public static void setInitialMethodOffset(int offset)
    {
      initial_method_offset = offset;
    }

  public static void setOffsetMap(OffsetMap o_map)
    {
      offset_map = o_map;
    }

  /**
   * @return a clone of this <code>HClassInfo</code>
   */
  public Object clone()
    {
      return new HClassInfo(m_hClass, m_depth, m_classObjects);
    }

  /**
   * @return the depth in the class hierarchy 
   */
  public int depth()
    {
      return m_depth;
    }

  /**
   * @return an array of fields in this class, in no particular order.
   */
  public HField[] getFields()
    {
      HField[] fields;
      Vector    v;

      v = new Vector();

      if (m_scInfo != null)
	{
	  fields = m_scInfo.getFields();
	  for (int i = 0; i < fields.length; i++)
	    {
	      v.addElement(fields[i]);
	    }
	}

      for (Enumeration e = m_classObjects.keys(); e.hasMoreElements();)
	{
	  Object next = e.nextElement();
	  if (next instanceof HField) v.addElement(next);
	}

      fields = new HField[v.size()];
      v.copyInto(fields);
      return fields;
    }

  /**
   * @return an <code>Enumeration</code> of <code>HField</code> 
   *         objects, representing the fields of this class.  
   */
  public Enumeration getFieldsE()
    {
      Vector    v       = new Vector();
      HField[]  fields  = getFields();

      for (int i = 0; i < fields.length; i++)
	{
	  v.addElement(fields[i]);
	}

      return v.elements();
    }

  /**
   * @return the offset (in bytes) of the specified field.
   *         The field can be a static or a non-static field.
   *         NOTE that static fields are currently placed directly 
   *         after methods.  This will be changed in the final version.
   */
  public int getFieldOffset(HField hf)
    {
      Object iOffset = m_classObjects.get(hf);

      if (iOffset == null)
	{
	  return (m_scInfo == null) ? -1 : m_scInfo.getFieldOffset(hf);
	}
      else
	{
	  return (((Integer)iOffset).intValue() + 
		  ((hf.isStatic()) ?
		   initial_method_offset + m_currentMethodOffset :
		   initial_field_offset));
	}
    }
	      

  /**
   * @return an array of all methods in this class, in no particular order
   */
  public HMethod[] getMethods()
    {
      HMethod[] methods;
      Vector    v;

      v = new Vector();

      if (m_scInfo != null)
	{
	  methods = m_scInfo.getMethods();
	  for (int i = 0; i < methods.length; i++)
	    {
	      v.addElement(methods[i]);
	    }
	}

      for (Enumeration e = m_classObjects.keys(); e.hasMoreElements();)
	{
	  Object next = e.nextElement();
	  if (next instanceof HMethod) v.addElement(next);
	}

      methods = new HMethod[v.size()];
      v.copyInto(methods);
      return methods;
    }

  /**
   * @return an <code>Enumeration</code> of HMethod objects representing
   *         all of methods in this class, in no particular order
   */
  public Enumeration getMethodsE()
    {
      Vector    v       = new Vector();
      HMethod[] methods = getMethods();

      for (int i = 0; i < methods.length; i++)
	{
	  v.addElement(methods[i]);
	}

      return v.elements();
    }

  /**
   * @return the offset (in bytes) of the specified method
   */
  public int getMethodOffset(HMethod hm)
    {
      Object iOffset = m_classObjects.get(hm);

      if (iOffset == null)
	{
	  return (m_scInfo == null) ? -1 : m_scInfo.getMethodOffset(hm);
	}
      else
	{
	  return ((Integer)iOffset).intValue() + initial_method_offset;
	}
    }

  /**
   * @return a (vaguely) human-readable representation of this 
   *         <code>HClassInfo</code>
   */
  public String toString()
    {
      StringBuffer sb = new StringBuffer("CLASS: ");
      sb.append(m_hClass.getName());
      sb.append("\nDEPTH: ");
      sb.append(m_depth);
      sb.append("\nCONTENTS:\n");
      sb.append(m_classObjects.toString());

      return sb.toString();
    }

  private HClassInfo(HClass hc)
    {
      m_depth               = 0;      
      m_hClass              = hc;
      m_scInfo              = null;
      m_classObjects        = new Hashtable();
      extend(hc);
    }

  private HClassInfo(HClassInfo hci, HClass hc)
    {
      m_depth               = hci.depth() + 1;
      m_hClass              = hc;
      m_scInfo              = hci;
      m_classObjects        = new Hashtable();
      extend(hc);
    }

  private void extend(HClass hc)
    {
      HField[] hFields = hc.getDeclaredFields();
      for (int i = 0; i < hFields.length; i++)
	{
	  addField(hFields[i]);
	}

      HMethod[] hMethods = hc.getDeclaredMethods();
      for (int i = 0; i < hMethods.length; i++)
	{
	  addMethod(hMethods[i]);
	}
    }


  private HClassInfo(HClass hc, int depth, Hashtable classObjects)
    {
      m_hClass         = hc;
      m_depth          = depth;
      m_classObjects   = (Hashtable)(classObjects.clone());
    }

  private void addField(HField hf)
    {
      int size = 4; // offset_map.size(hf, inline_map);

      if (hf.isStatic())
	{
	  m_classObjects.put(hf, new Integer(m_currentStaticFieldOffset));
	  m_currentStaticFieldOffset += size;
	}
      else
	{
	  m_classObjects.put(hf, new Integer(m_currentFieldOffset));
	  m_currentFieldOffset += size;
	}
    }

  private void addMethod(HMethod hm)
    {
      int    size     = 4; // use OffsetMap here
      int    offset;
      String sig      = getSignature(hm);

      if (getMethod(sig) == null)
	{
	  offset                 = m_currentMethodOffset;
	  m_currentMethodOffset += size;
	}
      else
	{
	  HMethod scMethod = (HMethod)(m_classObjects.get(sig));
	  offset           = getMethodOffset(scMethod);
	}

      m_classObjects.put(sig, hm);
      m_classObjects.put(hm, new Integer(offset));
    }

  private HMethod getMethod(String sig)
    {
      Object hMethod = m_classObjects.get(sig);
      if (sig == null) return (m_scInfo == null) ? null : m_scInfo.getMethod(sig);
      else return (HMethod)hMethod;
    }

  private String getSignature(HMethod hm)
    {
      HClass[] paramTypes;
      StringBuffer sb;

      sb = new StringBuffer("");
      sb.append(hm.getName());
      paramTypes = hm.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++)
	  sb.append(paramTypes[i].toString());
      return sb.toString();
    }

}

