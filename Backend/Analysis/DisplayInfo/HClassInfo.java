/*
 * NOTES:
 *
 * NEED A PLACE FOR STATIC FIELDS
 */


package harpoon.Backend.Analysis.DisplayInfo;

import harpoon.ClassFile.*;
import java.util.*;

public class HClassInfo
{
  private static int         initial_field_offset  = 0;
  private static int         initial_method_offset = 0;
  private static Hashtable   cached_classes        = new Hashtable();

  private HClass    m_hClass;
  private int       m_depth;
  private Hashtable m_fields;
  private Vector    m_orderedFields;
  private Hashtable m_methods;
  private Vector    m_orderedMethods;

  public static HClassInfo getClassInfo(String name)
    {
      Object hClassInfo = cached_classes.get(name);
      if (hClassInfo == null)
	{
	  HClass hc  = HClass.forName(name);
	  hClassInfo = HClassInfo.getClassInfo(hc);
	}
      return (HClassInfo)hClassInfo;
    }

  public static HClassInfo getClassInfo(HClass hc)
    {
      Object hClassInfo = cached_classes.get(hc.getName());
      if (hClassInfo == null)
	{
	  HClass superclass = hc.getSuperclass();
	  if (superclass == null)
	    {
	      hClassInfo = new HClassInfo(hc);
	    }
	  else
	    {
	      HClassInfo 
		superclassInfo = HClassInfo.getClassInfo(superclass);
	      hClassInfo = superclassInfo.clone();
	      ((HClassInfo)hClassInfo).extend(hc);
	    }
	}
      return (HClassInfo)hClassInfo;
    }
      
  public Object clone()
    {
      return new HClassInfo(m_hClass, m_depth,
			    m_fields, m_orderedFields,
			    m_methods, m_orderedMethods);
    }


  public String toString()
    {
      StringBuffer sb = new StringBuffer("CLASS: ");
      sb.append(m_hClass.getName());
      sb.append("\nDEPTH: ");
      sb.append(m_depth);
      sb.append("\nFIELDS: ");
      sb.append(m_orderedFields.toString());
      sb.append("\nMETHODS: ");
      sb.append(m_orderedMethods.toString());
      
      return sb.toString();
    }

  private HClassInfo(HClass hc)
    {
      m_hClass         = hc;
      m_fields         = new Hashtable();
      m_orderedFields  = new Vector();
      m_methods        = new Hashtable();
      m_orderedMethods = new Vector();
      extend(hc);
      m_depth = 0;
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

  private void extend(HClass hc)
    {
      m_hClass = hc;
      m_depth++;

      HField[] hFields = hc.getDeclaredFields();
      for (int i = 0; i < hFields.length; i++)
	{
	  HField hField = hFields[i];
	  if (m_fields.get(hField.getName()) == null)
	    {
	      m_orderedFields.addElement(hField);
	      m_fields.put(hField.getName(), hField);
	    }
	  m_fields.put(hField, new Integer(-1));
	}

      HMethod[] hMethods = hc.getDeclaredMethods();

      //
      // relies on the accuracy of toString()
      for (int i = 0; i < hMethods.length; i++)
	{
	  HMethod hMethod = hMethods[i];
	  if (m_methods.get(hMethod.toString()) == null)
	    {
	      m_orderedMethods.addElement(hMethod);
	      m_methods.put(hMethod.toString(), hMethod);
	    }
	  m_methods.put(hMethod, new Integer(-1));
	}
    }

}

