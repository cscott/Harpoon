package harpoon.Interpret.Tree;

import harpoon.Analysis.QuadSSA.TypeInfo;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.Quad;

import java.util.Enumeration;

public class TestTypeInfo {

  public static void main(String[] args) {
    HClass  hc = HClass.forName("java.lang.Character");
    HMethod hm = hc.getMethod("<clinit>", new HClass[] { });
    HCodeFactory hcf = harpoon.IR.Quads.QuadSSA.codeFactory();
    //hcf = harpoon.IR.LowQuad.LowQuadSSA.CodeFactory(hcf);
    HCode code = hcf.convert(hm);
    TypeInfo tym = new TypeInfo();
    for (Enumeration e = code.getElementsE(); e.hasMoreElements();) {
      Quad q = (Quad)e.nextElement();
      for (int i=0; i<q.def().length; i++) {
	tym.typeMap(code, q.def()[i]);
      }
    }
  }
}
