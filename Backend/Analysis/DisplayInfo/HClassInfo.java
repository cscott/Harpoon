package harpoon.Backend.Anaylsis.DisplayInfo;

import harpoon.ClassFile.*;

import java.util.Hashtable;

public class HClassInfo
{
  private int         m_depth;
  private Hashtable   m_fields;
  private Hashtable   m_methods;
  private HClass      m_class;

  public HClassInfo(HClass hc)
    {
      m_class = hc;
      for (m_depth=0; (hc=hc.getSuperclass()) != null; m_depth++); 	
    }

  public int depth()
    {
      return m_depth;
    }

  public String toString()
    {
      StringBuffer sb = new StringBuffer("");
      sb.append("CLASSNAME: " );
      sb.append(m_class.getName());
      sb.append("\n");
      sb.append("DEPTH: ");
      sb.append(m_depth);
      
      return sb.toString();
    }
}

