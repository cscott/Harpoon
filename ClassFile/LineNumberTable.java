package ClassFile;

class LineNumberTable {
  int start_pc;
  int line_number;

  LineNumberTable(ClassDataInputStream in) throws java.io.IOException {
    start_pc = in.read_u2();
    line_number = in.read_u2();
  }
}
