package ClassFile;

class ExceptionTable {
  int start_pc;
  int end_pc;
  int handler_pc;
  int catch_type_index;
  ConstantClass catch_type;

  ExceptionTable(ClassDataInputStream in, ConstantPoolInfo cp[]) 
       throws java.io.IOException 
  {
    start_pc = in.read_u2();
    end_pc = in.read_u2();
    handler_pc = in.read_u2();
    catch_type_index = in.read_u2();
    catch_type = (ConstantClass) cp[catch_type_index];
  }
}
