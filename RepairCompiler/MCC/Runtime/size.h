class typeobject {
 public:
  typeobject();
  int getfield(int type, int fieldindex); //returns type
  int isArray(int type, int fieldindex); //returns if array
  int numElements(int type, int fieldindex);  //returns number of elements
  int size(int type);
  int getnumfields(int type);
  bool issubtype(int subtype, int type);
  void reset();
};
