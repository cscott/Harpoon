package harpoon.Backend.Maps;

import harpoon.Analysis.InterfaceMethodMap;
import harpoon.Backend.Analysis.DisplayInfo.HClassInfo;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * The OffsetMap32 class implements the abstract methods of OffsetMap,
 * specializing them for 32-bit architectures.
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: OffsetMap32.java,v 1.1.2.2 1999-02-05 03:16:20 duncan Exp $
 */
public class OffsetMap32 extends OffsetMap
{
  private static final int WORDSIZE = 4;

  private ClassDepthMap   m_cdm = new ClassDepthMap() {
    public int classDepth(HClass hc) { return m_hci.depth(hc); }
    public int maxDepth() { throw new Error("Not impl:  maxDepth()"); }
  };
  
  private FieldMap        m_fm = new FieldMap() {
    public int fieldOrder(HField hf) { return m_hci.getFieldOffset(hf); }
  };

  private MethodMap  m_cmm = new MethodMap() {
    public int methodOrder(HMethod hm) { return m_hci.getMethodOffset(hm); }
  };

  private InterfaceMethodMap m_imm; 

  private Hashtable  m_fields; // Cache of field-orderings
  private HClassInfo m_hci;

  public OffsetMap32() {
    m_fields = new Hashtable();
    m_hci    = new HClassInfo();
  }

  public int offset(HField hf) {
    HClass    hc;
    HField[]  orderedFields;
    int       fieldOrder, offset;

    hc = hf.getDeclaringClass();
    if (!m_fields.containsKey(hf))
      {
	HField[] fields  = hc.getDeclaredFields();
	orderedFields    = new HField[fields.length];
	for (int i=0; i<fields.length; i++)
	  {
	    orderedFields[m_fm.fieldOrder(fields[i])] = fields[i];
	  }
	m_fields.put(hf, orderedFields);
      }
    orderedFields  = (HField[])m_fields.get(hf);
    fieldOrder     = m_fm.fieldOrder(hf);
    offset         = fieldsOffset(hc);
    
    for (int i=0; i<fieldOrder; i++)
      {
	offset += size(orderedFields[i].getType());
      }
    return offset;
  }

  public int offset(HMethod hm) {
    HClass hc;
    int    offset;

    hc = hm.getDeclaringClass();
    if (hc.isInterface())
      {
	offset = -(m_imm.methodOrder(hm)*WORDSIZE);
      }
    else
      {
	offset = (methodsOffset(hm.getDeclaringClass()) + 
		  (m_cmm.methodOrder(hm)*WORDSIZE));
      }
    return offset;
  }

  public Label label(HField hf) {
    Util.assert(hf.isStatic());

    // Generate label:
    StringBuffer sb = new StringBuffer("");
    sb.append("S_FIELD_");
    sb.append(getFieldSignature(hf));
    return new Label(sb.toString());
  }

  public Label label(HMethod hm) {
    StringBuffer sb = new StringBuffer("");
    sb.append("METHOD_");
    sb.append(getMethodSignature(hm));
    return new Label(sb.toString());
  }

  public int size(HClass hc) {
    int size;
    
    if (hc.isPrimitive())
      if (hc.isInstanceOf(HClass.Double) || hc.isInstanceOf(HClass.Long))
	size = 2*WORDSIZE;
      else
	size = WORDSIZE;
    else
      size = WORDSIZE;
    
    return size;
  }

  public int classOffset(HClass hc) {
    Util.assert(!hc.isPrimitive());
    return 0;
  }

  public int displayOffset(HClass hc) {
    return 0;  // Is this right?
  }

  public int elementsOffset(HClass hc) {
    Util.assert(hc.isArray());
    return 2 * WORDSIZE;
  }
  
  public int fieldsOffset(HClass hc) {
    Util.assert((!hc.isPrimitive()) && (!hc.isArray()));
    return 2 * WORDSIZE;
  }

  public int hashcodeOffset(HClass hc) {
    Util.assert(!hc.isPrimitive());
    return 1 * WORDSIZE;
  }

  public int lengthOffset(HClass hc) {
    Util.assert(hc.isArray());
    return -1 * WORDSIZE;
  }

  public int methodsOffset(HClass hc) {
    return (displayOffset(hc) + 
	    ((1 + m_cdm.classDepth(hc)) * WORDSIZE));
  }

  private String getFieldSignature(HField hf)
    {
      String token = null;
      for (StringTokenizer st = new StringTokenizer(hf.toString());
	   st.hasMoreTokens();)
	{
	  token = st.nextToken();
	}
      return token;
    }

  private String getMethodSignature(HMethod hm)
    {
      HClass[] paramTypes;
      StringBuffer sb;
      
      sb = new StringBuffer("");
      sb.append(hm.getName());
      paramTypes = hm.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++)
	{
	  sb.append(paramTypes[i].toString());
	  sb.append("_");
	}
      
      return sb.toString();

    }

}
