package harpoon.Backend.Maps;

import harpoon.Analysis.InterfaceMethodMap;
import harpoon.Analysis.QuadSSA.ClassHierarchy;
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
 * @version  $Id: OffsetMap32.java,v 1.1.2.3 1999-02-12 08:07:37 duncan Exp $
 */
public class OffsetMap32 extends OffsetMap
{
  private static int WORDSIZE = 4;

  private ClassDepthMap       m_cdm;
  private FieldMap            m_fm;
  private Hashtable           m_fields; // Cache of field-orderings
  private HClassInfo          m_hci;
  private InterfaceMethodMap  m_imm; 
  private MethodMap           m_cmm;

  public OffsetMap32(ClassHierarchy ch) {
    m_cmm     = new MethodMap() {
      public int methodOrder(HMethod hm) { return m_hci.getMethodOffset(hm); }
    };
    m_cdm     = new ClassDepthMap() {
      public int classDepth(HClass hc) { return m_hci.depth(hc); }
      public int maxDepth() { throw new Error("Not impl:  maxDepth()"); }
    };
    m_fm      = new FieldMap() {
      public int fieldOrder(HField hf) { return m_hci.getFieldOffset(hf); }
    };
    m_fields  = new Hashtable();
    m_hci     = new HClassInfo();
    m_imm     = new InterfaceMethodMap(ch.classes());
  }

  public int offset(HField hf) {
    Util.assert(!hf.isStatic());
    HClass    hc;
    HField[]  fields, orderedFields;
    int       fieldOrder, offset;
    
    hc = hf.getDeclaringClass();
    if (!m_fields.containsKey(hf)) {
      fields         = hc.getDeclaredFields();
      orderedFields  = new HField[fields.length];
      for (int i=0; i<fields.length; i++) {
	orderedFields[m_fm.fieldOrder(fields[i])] = fields[i];
      }
      m_fields.put(hf, orderedFields);
    }
    orderedFields  = (HField[])m_fields.get(hf);
    fieldOrder     = m_fm.fieldOrder(hf);
    offset         = fieldsOffset(hc);
    
    for (int i=0; i<fieldOrder; i++) {
      offset += size(orderedFields[i].getType(), false);  // inlines nothing
    }
    return offset;
  }

  public int offset(HMethod hm) {
    Util.assert(!hm.isStatic());
    int offset;
    
    HClass hc = hm.getDeclaringClass();
    if (hc.isInterface())
      offset = -(m_imm.methodOrder(hm)*WORDSIZE);
    else 
      offset = (methodsOffset(hm.getDeclaringClass()) + 
		(m_cmm.methodOrder(hm)*WORDSIZE));
    return offset;
  }

  public Label label(HField hf) {
    Util.assert(hf.isStatic());

    StringBuffer sb = new StringBuffer("");
    sb.append("S_FIELD_");
    sb.append(getFieldSignature(hf));
    return new Label(sb.toString());
  }

  public Label label(HMethod hm) {
    Util.assert(hm.isStatic());

    StringBuffer sb = new StringBuffer("");
    sb.append("METHOD_");
    sb.append(getMethodSignature(hm));
    return new Label(sb.toString());
  }

  public int size(HClass hc) {
    return size(hc, true);
  }
    
  private int size(HClass hc, boolean inline) {
    int size;    
    if (hc.isPrimitive()) {
      if (hc==HClass.Double || hc==HClass.Long) { size = 2*WORDSIZE; }
      else { size = WORDSIZE; }
    }
    else {
      if (inline) {
	size = (2*WORDSIZE);  // Includes HashCode and Class pointer
	HField[] hf = hc.getDeclaredFields();
	for (int i=0; i<hf.length; i++) {
	  size += hf[i].isStatic()?0:size(hf[i].getType(), false);
	}
      }
      else { size = WORDSIZE; }
    }
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

  public int hashCodeOffset(HClass hc) {
    Util.assert(!hc.isPrimitive());
    return 1 * WORDSIZE;
  }

  public int lengthOffset(HClass hc) {
    Util.assert(hc.isArray());
    return -1 * WORDSIZE;
  }

  public int methodsOffset(HClass hc) {
    return displayOffset(hc) + m_cdm.classDepth(hc)*WORDSIZE;
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
