package harpoon.Backend.Analysis.DisplayInfo;

import harpoon.ClassFile.*;
import harpoon.Backend.Maps.*;

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
 * @version 1.1.2.3
 * @see     harpoon.ClassFile.HClass
 */
public class HClassInfo
{
  private static int         initial_field_offset  = 0;
  private static int         initial_method_offset = 0;
  private static Hashtable   cached_classes        = new Hashtable();
  private static OffsetMap   offset_map;
  private static InlineMap   inline_map;

  private HClass    m_hClass;
  private int       m_depth;
  private Hashtable m_fields;
  private Vector    m_orderedFields;
  private Vector    m_orderedStaticFields;
  private Hashtable m_methods;
  private Vector    m_orderedMethods;

  private int       m_currentMethodOffset       = 0;
  private int       m_currentFieldOffset        = 0;
  private int       m_currentStaticFieldOffset  = 0;

  /**
   * @return an <code>HClassInfo</code> object representing
   *         the specified class. 
   */
  public static HClassInfo getClassInfo(String name)
    {
      Object hClassInfo;
      HClass hc;

      hClassInfo = cached_classes.get(name);
      if (hClassInfo == null)
	{
	  hc         = HClass.forName(name);  
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

      hClassInfo = cached_classes.get(hc.getName());
      if (hClassInfo == null)
	{
	  hClassInfo = new HClassInfo(hc);
	  superclass = hc.getSuperclass();
	  if (superclass != null)
	    {
	      superclassInfo = HClassInfo.getClassInfo(superclass);
	      hClassInfo = new HClassInfo(hc);
	      ((HClassInfo)hClassInfo).inherit(superclassInfo);
	    }
	  cached_classes.put(hc.getName(), hClassInfo);
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
      return new HClassInfo(m_hClass, m_depth,
			    m_fields, m_orderedFields,
			    m_methods, m_orderedMethods);
    }

  /**
   * @return the depth in the class hierarchy 
   */
  public int depth()
    {
      return m_depth;
    }

  /**
   * @return an array of fields in this class, ordered by their
   *         layout in memory.  Does not return any static fields.
   */
  public HField[] getFields()
    {
      HField[] fields = new HField[m_orderedFields.size()];
      m_orderedFields.copyInto(fields);
      return fields;
    }

  /**
   * @return an <code>Enumeration</code> of <code>HField</code> 
   *         objects, representing the fields of this class.  
   *         Does not return any static fields.
   */
  public Enumeration getFieldsE()
    {
      return m_orderedFields.elements();
    }

  /**
   * @return the offset (in bytes) of the specified field.
   *         The field can be a static or a non-static field.
   *         NOTE that static fields are currently placed directly 
   *         after methods.  This will be changed in the final version.
   */
  public int getFieldOffset(HField hf)
    {
      Object iOffset = m_fields.get(hf);
      if (hf.isStatic())
	return (iOffset == null) ? -1 : (((Integer)iOffset).intValue() + 
					 initial_method_offset + 
					 m_currentMethodOffset);
      else
	return (iOffset == null) ? -1 : (((Integer)iOffset).intValue() + 
					 initial_field_offset);
    }

  /**
   * @return an array of all methods in this class,
   *         ordered by their layout in memory
   */
  public HMethod[] getMethods()
    {
      HMethod[] methods = new HMethod[m_orderedMethods.size()];
      m_orderedMethods.copyInto(methods);
      return methods;
    }

  /**
   * @return an <code>Enumeration</code> of HMethod objects representing
   *         all of methods in this class, ordered by their layout in
   *         memory.
   */
  public Enumeration getMethodsE()
    {
      return m_orderedMethods.elements();
    }

  /**
   * @return the offset (in bytes) of the specified method
   */
  public int getMethodOffset(HMethod hm)
    {
      Object iOffset = m_methods.get(hm);
      return (iOffset == null) ? -1 : (((Integer)iOffset).intValue() + 
				       initial_method_offset);
    }

  /**
   * @return an array of static fields in this class, ordered by their
   *         layout in memory. 
   */  
  public HField[] getStaticFields()
    {
      HField[] staticFields = new HField[m_orderedStaticFields.size()];
      m_orderedStaticFields.copyInto(staticFields);
      return staticFields;
    }

  /**
   * @return an <code>Enumeration</code> of <code>HField</code> objects, 
   *         representing the static fields of this class.  
   */
  public Enumeration getStaticFieldsE()
    {
      return m_orderedStaticFields.elements();
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
      sb.append("\nFIELDS:\n");
      sb.append(m_fields.toString());
      sb.append("\nMETHODS: ");
      sb.append(m_methods.toString());
      
      return sb.toString();
    }

  private HClassInfo(HClass hc)
    {
      m_hClass              = hc;
      m_depth               = 0;
      m_fields              = new Hashtable();
      m_orderedFields       = new Vector();
      m_orderedStaticFields = new Vector();
      m_methods             = new Hashtable();
      m_orderedMethods      = new Vector();
      initialize(hc);
    }

  private HClassInfo(HClass hc, int depth,
		     Hashtable fields, Vector orderedFields,
		     Hashtable methods, Vector orderedMethods)
    {
      m_hClass         = hc;
      m_depth          = depth;
      m_fields         = (Hashtable)(fields.clone());
      m_orderedFields  = (Vector)(orderedFields.clone());
      m_methods        = (Hashtable)(methods.clone());
      m_orderedMethods = (Vector)(orderedMethods.clone());
    }

  private void addField(HField hf)
    {
      int size = offset_map.size(hf, inline_map);

      m_fields.put(hf.getName(), hf);
      if (hf.isStatic())
	{
	  m_orderedStaticFields.addElement(hf);
	  m_fields.put(hf, new Integer(m_currentStaticFieldOffset));
	  m_currentStaticFieldOffset += size;
	}
      else
	{
	  m_orderedFields.addElement(hf);
	  m_fields.put(hf, new Integer(m_currentFieldOffset));
	  m_currentFieldOffset += size;
	}
    }

  private void addMethod(HMethod hm, Vector methodVector)
    {
      int size = 4; // use OffsetMap here

      m_orderedMethods.addElement(hm);
      methodVector.addElement(hm);
      m_methods.put(hm, new Integer(m_currentMethodOffset));
      m_currentMethodOffset += size;
    }

  private void initialize(HClass hc)
    {
      HField   hField;
      HField[] hFields;
      HMethod   hMethod;
      HMethod[] hMethods;
      Object    methodVector;

      hFields = hc.getDeclaredFields();
      for (int i = 0; i < hFields.length; i++)
	{
	  hField = hFields[i];
	  addField(hField);
	}

      hMethods = hc.getDeclaredMethods();
      for (int i = 0; i < hMethods.length; i++)
	{
	  hMethod      = hMethods[i];
	  methodVector = m_methods.get(hMethod.getName());
	  if (methodVector == null)
	    {
	      methodVector = new Vector();
	      m_methods.put(hMethod.getName(), methodVector);
	    }
	  addMethod(hMethod, (Vector)methodVector);
	}
    }

  private void inherit(HClassInfo hci)
    {
      boolean      isFound;
      HField       hField;
      HField[]     hFields;
      HMethod      hMethod, tempMethod;
      HMethod[]    hMethods;
      Object       methodVector;
      Vector       methodVectorV;

      m_depth = hci.depth();  m_depth++;

      hFields = hci.getFields();
      for (int i = 0; i < hFields.length; i++)
	{
	  hField = hFields[i];
	  if (m_fields.get(hField.getName()) == null) { addField(hField); }
	}

      hFields = hci.getStaticFields();
      for (int i = 0; i < hFields.length; i++)
	{
	  hField = hFields[i];
	  if (m_fields.get(hField.getName()) == null) { addField(hField); }
	}

      hMethods = hci.getMethods();
      for (int i = 0; i < hMethods.length; i++)
	{
	  hMethod      = hMethods[i];
	  methodVector = m_methods.get(hMethod.getName());

	  if (methodVector == null)
	    {
	      methodVector = new Vector();
	      m_methods.put(hMethod.getName(), methodVector);
	      addMethod(hMethod, (Vector)methodVector);
	    }
	  else 
	    {
	      isFound        = false; 
	      methodVectorV  = (Vector)methodVector; 
	      for (int j=0, size=methodVectorV.size(); j < size; j++)
		{
		  tempMethod = (HMethod)(methodVectorV.elementAt(j));
		  if (similar(hMethod, tempMethod)) { isFound = true; break; }
		}

	      if (!isFound) addMethod(hMethod, methodVectorV);
	    }
	}
    }

  private boolean similar(HMethod m1, HMethod m2)
    {
      HClass[]  m1Types, m2Types;

      if (m1 == m2) return true;
      else
	{
	  m1Types = m1.getParameterTypes();
	  m2Types = m2.getParameterTypes();

	  if (m1Types.length != m2Types.length) return false;
	  else
	    {
	      for (int i = 0; i < m1Types.length; i++)
		{
		  if (!m1Types[i].getName().equals(m2Types[i].getName())) return false;
		}
	    }
	}

      return true;
    }
}

