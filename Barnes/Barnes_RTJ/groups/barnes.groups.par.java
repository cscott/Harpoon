/******************************************************************************/
/*                                                                            */
/*  Copyright (c) 1996 University of California at Santa Barbara              */
/*                                                                            */
/*  All rights reserved.                                                      */
/*                                                                            */
/*  Permission is given to use, copy, and modify this software for any        */
/*  non-commercial purpose as long as this copyright notice is not removed    */
/*  All other uses, including redistribution in whole or in part, are         */
/*  forbidden without prior written permission.                               */
/*                                                                            */
/*  This software is provided with absolutely no warranty and no support.     */
/*                                                                            */
/*  For further information contact: pedro@cs.ucsb.edu or martin@cs.ucsb.edu  */
/*                                                                            */
/******************************************************************************/

import java.io.*;
import java.lang.Math.*;

class vec { 
  public static final int NDIM   = 3;
  public static final int NSUB = 8; 			// (1 << NDIM)
  double value[] = new double[NDIM];
  vec() { 
    this.clear();
  }
  synchronized void clear() { 
    for (int i = 0; i < NDIM; i++) { 
      value[i] = 0.0;
    }
  }
  synchronized void unit() { 
    for (int i = 0; i < NDIM; i++) { 
      value[i] = 1.0;
    }
  }
  synchronized void add(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      value[i] += v.value[i];
    }
  }
  synchronized void sub(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      value[i] -= v.value[i];
    }
  }
  synchronized void load(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      value[i] = v.value[i];
    }
  }
  void store(vec v) { 
    synchronized (v) { 
      for (int i = 0; i < NDIM; i++) {
        v.value[i] = value[i];
      }
    }
  }
  synchronized void div(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      value[i] /= v.value[i];
    }
  }
  synchronized void prod(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      value[i] *= v.value[i];
    }
  }
  synchronized void scale(double s) { 
   for (int i = 0; i < NDIM; i++) {
      value[i] *= s;
    }
  }
  double dotProd(vec v) { 
    double d = 0.0;
    for (int i = 0; i < NDIM; i++) {
       d += value[i] * v.value[i];
    }
    return d; 
  }
  synchronized void crossProd(vec u, vec w) { 
    value[0] = u.value[1]*w.value[2] - u.value[2]*w.value[1];
    value[1] = u.value[2]*w.value[0] - u.value[0]*w.value[2];
    value[2] = u.value[0]*w.value[1] - u.value[1]*w.value[0];
  }
  synchronized void min(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      if (value[i] > v.value[i]) value[i] = v.value[i];
    }
  }
  synchronized void max(vec v) { 
   for (int i = 0; i < NDIM; i++) {
      if (value[i] < v.value[i]) value[i] = v.value[i];
    }
  }
  double norm(){ 
    double d=0.0;
    for(int i=0; i < NDIM; i++) {
      d += value[i]; 
    }
    return d;
  }
  double squareNorm(){ 
    double d=0.0;
    for(int i=0; i < NDIM; i++) {
      d += (value[i]*value[i]); 
    }
    return d;
  }
  double getElement(int i) { 
    return(value[i]);
  }
  synchronized void setElement(int i, double v) { 
    value[i] = v;
  }
  synchronized void clearElement(int i) { 
    value[i] = 0.0;
  }
  synchronized void addElement(int i, double v) { 
    value[i] += v;
  }
  void read(DataInputStream s) throws java.io.IOException { 
    for (int i = 0; i < NDIM; i++) { 
      value[i] = s.readDouble();
    }
  }
  void print() { 
    System.out.print("(");
    for (int i = 0; i < NDIM; i++) { 
      System.out.print(value[i]);
      System.out.print(" ");
    }
    System.out.print(")");
  }
}

class mat {
  public static final int NDIM   = 3;
  double value[][] = new double[NDIM][NDIM];
  mat() { 
    this.clear();
  }
  void clear() { 
    for(int i = 0; i < NDIM; i++) {
      for(int j = 0; j < NDIM; j++) {
        value[i][j] = 0.0;
      }
    }
  }
  void unit() { 
    for(int i = 0; i < NDIM; i++) {
      for(int j = 0; j < NDIM; j++) {
        value[i][j] = 1.0;
      }
    }
  }
  void store(mat m) { 
    for(int i = 0; i < NDIM; i++) {
      for(int j = 0; j < NDIM; j++) {
        value[i][j] = m.value[i][j];
      }
    }
  }
  void add(mat m) { 
    for(int i = 0; i < NDIM; i++) {
      for(int j = 0; j < NDIM; j++) {
        value[i][j] += m.value[i][j];
      }
    }
  }
  void outProd(vec v, vec u) { 
    for(int i = 0; i < NDIM; i++) {
      for(int j = 0; j < NDIM; j++) {
        value[i][j] = v.value[i]*u.value[j];
      }
    }
  }
  void print() { 
    System.out.print("[");
    for (int i = 0; i < NDIM; i++) { 
      System.out.print("(");
      for (int j = 0; j < NDIM; j++) { 
        System.out.print(value[i][j]);
        System.out.print(" ");
      }
      System.out.print(")");
    }
    System.out.print("]");
  }
}

// ----------- FILE: body.h ----------

class node {	// inherited by all CELL, LEAF and BODY classes
  public static final int LEAFMAXBODIES = 10;
  public static final int IMAX	= 1073741824;		// (1 << MAXLEVEL)

  public static final int CELL = 01;
  public static final int LEAF = 02;
  public static final int BODY = 04;

  public int type;         
  public double mass;      /* total mass of node */
  public vec pos = new vec();       /* position of node */
  public int level;
  public node parent;     /* ptr to parent of this node in tree */
  public int child_num;    /* Index that this node should be put at in parent cell */
  node() { 
    type = 0; 
    mass = 0.0; 
    pos.clear(); 
    level = 0; 
    parent = null; 
    child_num =0;
  }
  int Type() { 
    return type; 
  }
  double Mass()	{ 
    return mass; 
  }
  vec Pos() { 
    return pos; 
  }
  int Level() { 
    return level; 
  }
  node Parent() { 
    return parent; 
  }
  int ChildNum() { 
    return child_num; 
  }
  void setChildNum(int n) { 
    child_num = n; 
  }
  void setKind(int t) { 
    type = t; 
  }
  void setMass(double m) { 
    mass = m; 
  }
  void setLevel(int d) { 
    level =d; 
  }
  void setParent(node p) { 
    parent = p; 
  }
  void PosvecStore(vec p) { 
    pos.store(p); 
  }
  void PosvecLoad(vec p) { 
    pos.load(p); 
  }
  void PosvecAdd(vec p) { 
    pos.add(p); 
  }
  void clear() { 
    mass= 0.0; 
    pos.clear(); 
    level = 0; 
    parent = null; 
    child_num =0; 
  }
  void adjLevel(node p) {
    if (p == null) level = (IMAX >> 1);
    else level = (p.Level() >> 1);
  }
  int subindex(int x[], int l) {
    int i, k;
    boolean yes;

    i = 0;
    yes = false;
    for(k = 0; k < vec.NDIM; k++) {
      if((((x[k] & l) != 0) && !yes) || (((x[k] & l) == 0) && yes)) {
        i += vec.NSUB >> (k + 1);
        yes = true;
      }
      else yes = false;
    }
    return (i);
  }
  void computecofm(){
    int i;
    node nptr;
    body p;
    leaf le;
    cell ce;
    double m;
    vec tmpv = new vec();

    if(type == LEAF){
      le = (leaf)this;
      for(i=0; i < le.NumBody(); i++) {
        p = le.Bodyp(i);
        m = p.Mass();
        mass += m;
        p.PosvecStore(tmpv);
        tmpv.scale(m);
        pos.add(tmpv);
      }
      pos.store(tmpv);
      tmpv.scale(1.0/mass);
      pos.load(tmpv);
    } else {  
      ce = (cell)this;
      for(i=0; i < vec.NSUB; i++){
        nptr = ce.Subp(i);
        if(nptr != null) {
          nptr.computecofm();
          m = nptr.Mass();
          mass += m;
          nptr.PosvecStore(tmpv);
          tmpv.scale(m);
          pos.add(tmpv);
        }
      }
      pos.store(tmpv);
      tmpv.scale(1.0/mass);
      pos.load(tmpv);
    }
  }
}

// ----------------------------------------------------------------------------
// BODY: data structure used to represent particles.
// ----------------------------------------------------------------------------
class body extends node {
  public vec vel = new vec(); /* velocity of body                             */
  public vec acc = new vec(); /* acceleration of body                         */
  public double phi;          /* potential at body                            */
  public vec tmp = new vec(); /* Skratch vector		                */

  double subdiv(node p, vec res) { 
    int i;
    double drsq, d;
    drsq = 0.0;
    for (i = 0; i < vec.NDIM; i++) {
      d = p.pos.value[i]-pos.value[i];
      drsq += d*d;
      res.value[i] = d;
    }
    return(drsq);
  }
  body() { 
    type = BODY;
    vel.clear();
    acc.clear();
    tmp.clear();
    phi = 0.0;
  }
  vec Vel() { 
    return vel; 
  }
  vec Acc() { 
    return acc; 
  }
  double Phi() { 
    return phi; 
  }
  vec Tmp() { 
    return tmp; 
  }
  void BodyClear() { 
    super.clear(); 
    vel.clear(); 
    acc.clear(); 
    tmp.clear();
    phi = 0.0;
  }
  void VelvecAdd(vec p)	{ 
    vel.add(p); 
  }
  void VelvecStore(vec p) { 
    vel.store(p); 
  }
  void VelvecLoad(vec p) { 
    vel.load(p); 
  }
  void AccvecStore(vec p) { 
    acc.store(p); 
  }

  double computeInter(node p, double epsSq, vec res){
    int i;
    double drabs, inc, mor3;
    double drsq, d;
  
    drsq = epsSq;
    for (i = 0; i < vec.NDIM; i++) {
      d = p.pos.value[i]-pos.value[i];
      drsq += d*d;
      res.value[i] = d;
    }
    drabs = Math.sqrt(drsq);
    inc = p.Mass() / drabs;
    mor3 = inc / drsq;
    res.scale(mor3);
    return inc;
  }

  void openCell(node n, double tolsq, double dsq, double epssq){
    int i;
    node nn;

    for(i=0; i < vec.NSUB; i++) {
      nn = ((cell)n).Subp(i);
      if(nn != null){
        walksub(nn,tolsq,(dsq/4.0),epssq);
      }
    }
  }

  void openLeaf(node n, double epssq, int iter){
    int i;
    body bptr;

    for(i=0; i < iter; i++){
      bptr = ((leaf)n).Bodyp(i);
      if(bptr != this){
        gravsub((node)bptr, epssq);
      }
    }
  }

  void walksub(node n, double tolsq, double dsq, double epssq){
    double drsq;
    vec ai = new vec();
    drsq = subdiv(n,ai);
    if((tolsq * drsq) < dsq){ 
      if((n.Type()) == CELL){
        openCell(n,tolsq,dsq,epssq);
      } else { 
        openLeaf(n,epssq,((leaf)n).NumBody());
      }
    } else {
      fastgravsub(n, ai, drsq, epssq);
    }
  }

  void advance(double hts, double ts){
    vec dvel = new vec();
    vec vel1 = new vec(); 
    vec dpos = new vec();

    acc.store(dvel);
    vel.store(vel1);
    dvel.scale(hts);
    vel1.add(dvel);
    dpos.load(vel1);
    dpos.scale(ts);
    dvel.scale(2.0);
  
    vel.add(dvel);
    pos.add(dpos);
  }

  void fastgravsub(node p, vec ai, double pdrsq, double epssq) {
    double drsq;
    double drabs, inc, mor3;
    vec tmpv = new vec();

    drsq = pdrsq + epssq;
    drabs = Math.sqrt(drsq);
    inc = p.Mass() / drabs;
    //  phi -= inc;
    updatePhi(inc);
    mor3 = inc / drsq;
    tmpv.load(ai);
    tmpv.scale(mor3);
    acc.add(tmpv);
  }

  void gravsub(node p, double epsSq){
    double phii;
    vec ai = new vec();
  
    phii = computeInter(p,epsSq,ai);
    //  phi -= phii;
    updatePhi(phii);
    acc.add(ai);
  }

  void updatePhi(double inc) {
    phi -= inc;
  }

  void swapAcc() {
    vec tmpv = new vec();

    phi = 0.0;
    acc.store(tmpv); 
    tmp.load(tmpv);
    acc.clear();
  }

  void startVel(double f){
    vec tmpv1 = new vec();
    vec tmpv2 = new vec();
 
    acc.store(tmpv1); 
    tmp.store(tmpv2);
    tmpv1.sub(tmpv2); 
    tmpv1.scale(f); 
    vel.add(tmpv1);
  }

  void dump(){
    System.out.print(mass);
    System.out.print(" ");
    pos.print();
    vel.print();
    acc.print();
    System.out.print("\n");
   }
}

// ----------------------------------------------------------------------------
// LEAF: structure used to represent leaf nodes of tree.
// ----------------------------------------------------------------------------
class leaf extends node {
  int num_bodies;
  body bodyp[] = new body[LEAFMAXBODIES];    // bodies of leaf
  leaf() { 
    type = LEAF;
  }
  body Bodyp(int idx) {
    return bodyp[idx]; 
  }
  void setBodyp(int idx, body p) { 
    bodyp[idx] = p; 
  }
  int NumBody() { 
    return num_bodies; 
  }
  void setNumBody(int n) { 
    num_bodies = n; 
  }
  boolean full() { 
    return (num_bodies == LEAFMAXBODIES); 
  }
  void LeafClear() { 
    super.clear();
    num_bodies = 0;
    for (int i = 0; i < LEAFMAXBODIES; i++) {
      bodyp[i] = null;
    }
  }

  void LeafAddBody(body bp) {                  // Assumes enough capacity
    bp.setParent(this);
    bp.setLevel(Level());
    bp.setChildNum(num_bodies);
    bodyp[num_bodies] = bp;
    num_bodies++;
  }

  synchronized boolean AddBody(cell parent, body p, int myIndex) {
    if(full()) {
      Subdivide(parent, myIndex);
      return(false);
    } else {
      LeafAddBody(p);
      return(true);
    }
  }

  void Subdivide(cell parent, int idx){
    cell c;
    leaf le;
    vec tmpv = new vec();
    int i, index, num, Lev;
    int xp[] = new int[vec.NDIM];
    body loctab[] = new body[LEAFMAXBODIES];
    body p;
  
    num = num_bodies;
    for(i=0; i < num; i++) {
      loctab[i] = bodyp[i];
      bodyp[i] = null;
    }
    num_bodies = 0;
    c = barnes.Nbody.getcells().makecell(parent);
    c.setChildNum(child_num);
    parent.setSubp(idx,c);
    Lev = level;
  
    p = loctab[0];
    p.PosvecStore(tmpv);
    barnes.Nbody.intcoord(xp,tmpv);
    index = subindex(xp, Lev);
    c.setSubp(index,this);
    child_num = index;
    parent = c;
    level = (Lev >> 1);
    LeafAddBody(p);
  
    for(i=1; i < num; i++) {
      p = loctab[i];
      p.PosvecStore(tmpv);
      barnes.Nbody.intcoord(xp,tmpv);
      index = subindex(xp, Lev);
      le = (leaf)(c.Subp(index));
      if(le == null) {
        le = barnes.Nbody.getleaves().makeleaf(c);
        le.setChildNum(index);
        c.setSubp(index,le);
      }
      le.LeafAddBody(p);
    }
  }
}

// ----------------------------------------------------------------------------
// CELL: structure used to represent internal nodes of tree.
// ----------------------------------------------------------------------------
class cell extends node {
  node subp[] = new node[vec.NSUB];    // descendents of cell
  cell() { 
    type = CELL; 
  }
  node Subp(int idx) { 
    return subp[idx]; 
  }
  void setSubp(int idx, node p) { 
    subp[idx] = p; 
  }
  void CellClear() { 
    super.clear();
    for (int i = 0; i < vec.NSUB; i++) {
      subp[i] = null;
    }
  }

  synchronized boolean AddBodyToNewLeaf(int kidIndex, body p, int coords[]) { 
    node ptr = subp[kidIndex];
    if (ptr == null) { 
      leaf le = barnes.Nbody.getleaves().makeleaf(this);
      le.setChildNum(kidIndex);
      le.LeafAddBody(p);
      subp[kidIndex] = le;
      return(true);
    } else { 
      return(false);
    }
  }
  synchronized boolean AddBodyToExistingLeaf(body p, int kidIndex) { 
    node ptr = subp[kidIndex];
    if (ptr.Type() == LEAF) {
      leaf le = (leaf)ptr;
      return(le.AddBody(this, p, kidIndex));
    } 
    return(false);
  }

  void CellAddBody(body p, int coords[]) {
    int Lev, kidIndex;
    node ptr;
    leaf le;
  
    kidIndex = subindex(coords,level);
    ptr = subp[kidIndex];
    Lev = (level >> 1);
    if(Lev != 0){
      if (ptr == null) {
        if (!AddBodyToNewLeaf(kidIndex, p, coords)) { 
          CellAddBody(p, coords);
        }
      } else {
        if (ptr.Type() == LEAF) {
          if (!AddBodyToExistingLeaf(p, kidIndex)) { 
            CellAddBody(p, coords);
          }
        } else {
          ((cell)ptr).CellAddBody(p, coords);
        }
      }
    } else {
      System.err.print(" *** Error: Not enough levels in tree...(CellAddBody)");
    }
  }
}

// ----------------------------------------------------------------------------
// CELLSET: set of cells.
// ----------------------------------------------------------------------------
class cellset {
  int	numcell;       	/* no. cells  used in celltab 	*/
  int	maxcell;       	/* max no. of cells  allocated 	*/
  cell	celltab[];	/* array of cells allocated 	*/
  cellset(int max) { 
    int i; 
    celltab = new cell[max]; 
    maxcell=max; 
    numcell=0; 
    for(i=0; i < maxcell; i++) {
      celltab[i] = new cell();
      celltab[i].clear();
    }
  }
  void CellSetclear()	{
    for(int i=0; i < maxcell; i++) {
      celltab[i].clear(); 
    }
  }
  void CellSetReset()	{ 
    numcell = 0; 
  }
  synchronized cell newcell() {
    cell c;
    if(numcell == maxcell){
      System.out.print("newcell: More than ");
      System.out.print(maxcell);
      System.out.print(" cells; increase fcells");
      return null;
    }
    c = celltab[numcell];
    numcell++;
    c.CellClear();
    return c;
  }
  cell makecell(node parent) {
    cell c;
    c = newcell();
    c.setParent(parent);
    if(c == null) return null;
    c.adjLevel(parent);
    return c;
  }
  cell getcell(int idx) { 
    return celltab[idx]; 
  }
}

// ----------------------------------------------------------------------------
// LEAFSET: set of leaves.
// ----------------------------------------------------------------------------
class leafset {
  int	numleaf;       	/* no. leaves used in leafctab 	*/
  int	maxleaf;       	/* max no. of leaves allocated 	*/
  leaf	leaftab[];	/* array of leaves allocated 	*/
  leafset(int max) { 
    leaftab = new leaf[max]; 
    for (int i = 0; i < max; i++) { 
      leaftab[i] = new leaf();
    }
    maxleaf=max; 
    numleaf=0; 
    LeafSetClear(); 
  }
  void LeafSetClear() { 
    for(int i=0; i < maxleaf; i++) {
      leaftab[i].clear(); 
    }
  }
  void LeafSetReset() { 
    numleaf = 0; 
  }
  synchronized leaf newleaf() {
    leaf p;
    if(numleaf == maxleaf){
      System.out.print("newleaf: More than ");
      System.out.print(maxleaf);
      System.out.print(" leaves; increase fleaves");
      return null;
    }
    p = leaftab[numleaf];
    numleaf++;
    p.LeafClear();
    return p;
  }

  leaf makeleaf(node parent){
    leaf l;
    l = newleaf();
    if(l == null) return null;
    l.setParent(parent);
    l.adjLevel(parent);
    return (l);
  }
  leaf getleaf(int idx) { 
    return leaftab[idx]; 
  }
}

// ----------------------------------------------------------------------------
// BODYSET: set of particle.
// ----------------------------------------------------------------------------
class bodyset {
  int   numbody;        /* no. bodies used in bodytab   */
  int   maxbody;        /* max no. of bodies allocated  */
  body  bodytab[];       /* array of bodies   allocated  */
  bodyset(int m) { 
    int i; 
    bodytab = new body[m]; 
    maxbody=m; 
    numbody=0; 
    for(i=0; i < maxbody; i++) { 
      bodytab[i] = new body();
      bodytab[i].clear(); 
    }
  }
  void BodySetClear() { 
    for(int i=0; i < maxbody; i++) {
      bodytab[i].clear();
    }
  }
  void BodySetReset() { 
    numbody = 0; 
  }
  body getbody(int idx)  { 
    return bodytab[idx]; 
  }
  body newbody(){
    body p;
    if(numbody == maxbody){
      System.out.print("newbody: More than ");
      System.out.print(maxbody);
      System.out.print(" bodies; increase fbody");
      return null;
    }
    p = bodytab[numbody];
    numbody++;
    return p;
  }
}

// ----------- FILE: simulation.h ----------
class parms {
  String headline;/* message describing calculation */
  String infile;  /* file name for snapshot input */
  String outfile; /* file name for snapshot output */
  double  dtime;          /* timestep for leapfrog integrator */
  double  dtout;          /* time between data outputs */
  double  tstop;          /* time to stop calculation */
  double  fcells;         /* ratio of cells/leaves allocated */
  double  fleaves;        /* ratio of leaves/bodies allocated */
  double  tol;            /* accuracy parameter: 0.0 => exact */
  double  tolsq;          /* square of previous */
  double  eps;            /* potential softening parameter */
  double  epssq;          /* square of previous */
  double  dthf;           /* half time step */
  String defaults[];   /* pairs of name = value */
  static final int num_defaults = 30;

  String  getheadline() { 
    return headline; 
  }
  void setheadline(String s) { 
    headline = s; 
  }
  String getinfile()    { 
    return infile; 
  }
  String getoutfile()   { 
    return outfile; 
  }
  double getdtime()     { 
    return dtime; 
  }
  double  getdtout()      { return dtout; }
  double  gettstop()      { return tstop; }
  double  getfcells()     { return fcells; }
  double  getfleaves()    { return fleaves; }
  double  gettol()        { return tol; }
  double  gettolsq()      { return tolsq; }
  double  geteps()        { return eps; }
  double  getepssq()      { return epssq; }
  double  getdthf()       { return dthf; }


  parms()  {
    int i;
    // SET DEFAULT PARAMETER VALUES
    headline = "";
    infile = "";
    outfile = "";
    dtime = 0.0;
    dtout = 0.0;
    tstop = 0.0;
    fcells = 0.0;
    fleaves = 0.0;
    tol = 0.0;
    tolsq = 0.0;
    eps = 0.0;
    epssq = 0.0;
    dthf = 0.0;

    defaults = new String[num_defaults];

    /* file names for input/output                                 */
    defaults[0] = "in=";
    defaults[1] = "out=";   /* stream of output snapshots      */
    /* params, used if no input specified, to make a Plummer Model   */
    defaults[2] = "nbody=32";  /* number of particles to generate */
    defaults[3] = "seed=123";    /* random number generator seed    */
    /* params to control N-body integration                          */
    defaults[4] = "dtime=0.025"; /* integration time-step      */
    defaults[5] = "eps=0.05";    /* usual potential softening  */
    defaults[6] = "tol=1.0";     /* cell subdivision tolerence */
    defaults[7] = "fcells=2.0";  /* cell allocation parameter  */
    defaults[8] = "fleaves=0.5"; /* leaf allocation parameter  */
    defaults[9] = "tstop=0.075"; /* time to stop integration   */
    defaults[10] = "dtout=0.25"; /* data-output interval       */
    defaults[11] = "dump=0";     /* flag for dumping position data */
    defaults[12] = "parallel=0"; /* run tasks in parallel or not */
  }

  void readParameterFile(String name) throws java.io.FileNotFoundException, java.io.IOException { 
    BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
    int i = 0;
    while (true) { 
      String s = b.readLine();
      if (s == null) break;
      defaults[i] = s;
      i++;
    } 
  }

  void setparms(String fnames[], double args[]){
    headline = fnames[0];
    infile = fnames[1];
    outfile = fnames[2];
    dtime = args[0];
    dtout = args[1];
    tstop = args[2];
    fcells = args[3];
    fleaves = args[4];
    tol = args[5];
    tolsq = args[6];
    eps = args[7];
    epssq = args[8];
    dthf = args[9];
  }

  int getiparam(String name){
    return (new Integer(getparam(name))).intValue();
  }

  double getdparam(String name){
    return (new Double(getparam(name))).doubleValue();
  }

  String getparam(String name) {
    int i, leng;

    if(defaults == null){
      System.err.print("getparam: called before initparam\n");
    }
    for (i = 0; i < defaults.length; i++) { 
      if (defaults[i].indexOf(name) == 0) {
        return(defaults[i].substring(defaults[i].indexOf("=")+1));
      }
    }
    if(i < 0) {
     System.err.print("getparam: " + name + " unknown\n");
    }
    return("");
  }

  void loadInfile(){
    infile = getparam("in");
  }

  void loadParms(){
    outfile = getparam("out");
    dtime = getdparam("dtime");
    dthf = 0.5 * dtime;
    eps = getdparam("eps");
    epssq = eps*eps;
    tol = getdparam("tol");
    tolsq = tol*tol;
    fcells = getdparam("fcells");
    fleaves = getdparam("fleaves");
    tstop = getdparam("tstop");
    dtout = getdparam("dtout");
  }
}


class NBodySystem {
  int nbody;
  cell BH_tree;        
  cellset cells;      
  leafset leaves;    
  bodyset bodies;   

  double mtot;      
  vec etot;     
  mat keten;   
  mat peten;  
  vec cmphasePos;   
  vec cmphaseVel;  
  vec amvec;      

  vec mincorner;       
  vec maxcorner;      

  double size;    
  double tnow;          
  double tout;         

  int nstep;          
  int n2bcalc;          
  int nbccalc;         
  long computestart;
  long computeend;
  long tracktime;
  long partitiontime;
  long treebuildtime;
  long forcecalctime;
  long updatetime;

  void pickshell(vec v, double rad){
    int k;
    double rsq, rsc;

    do {
      for(k = 0; k < vec.NDIM; k++) {
        v.value[k] = random.xrand(-1.0, 1.0);
      }
      rsq = v.dotProd(v);
    } while (rsq > 1.0);
    rsc = rad / Math.sqrt(rsq);
    v.scale(rsc);
  }

  void startrun(parms p) throws java.io.IOException, java.io.FileNotFoundException {
    int seed = 123;
    String name, headbuf;

    p.loadInfile();
    name = p.getinfile();
    if (name.length() > 1) {
      System.out.print("Hack code: input file " + name + "\n");
      headbuf = p.getheadline();
    } else {
      nbody = p.getiparam("nbody");
      if(nbody < 1) { 
        System.out.print("startrun: absurd nbody\n");
      }
      seed = p.getiparam("seed");
      p.setheadline("Hack code: Plummer model");
    }
    p.loadParms();

    // allocation of data structures filing is done later
    cells = new cellset((int)(p.getfcells()*p.getfleaves()*nbody));
    leaves = new leafset((int)(p.getfleaves()*nbody));
    bodies = new bodyset(nbody);

    tout = tnow + p.getdtout();
    random.pranset(seed);

    name = p.getinfile();
    if(name.length() > 1) {
      loadData(name);
    } else {
      loadTestData();
    }
  }

  void stepsystem(parms p) throws java.lang.InterruptedException {
    int i;
    body bptr;
    double incr, hts, ts, stop, tol, eps;

    long trackstart, trackend;
    long treebuildstart, treebuildend;
    long forcecalcstart, forcecalcend;
    long updatestart, updateend;
    int totalstep = 0;

    incr = p.getdtime();
    hts = p.getdthf();
    ts = p.getdtime();
    stop = p.gettstop();
    tol = p.gettolsq();
    eps = p.getepssq();

    computestart = System.currentTimeMillis();
    while (tnow < stop + 0.1 * incr) {
      trackstart = System.currentTimeMillis();
      leaves.LeafSetReset();
      cells.CellSetReset();
      BH_tree = cells.makecell(null);
      treebuildstart = System.currentTimeMillis();

      maketree();
      ((node)BH_tree).computecofm();
      treebuildend = System.currentTimeMillis();
      treebuildtime += treebuildend - treebuildstart;

      forcecalcstart = System.currentTimeMillis();
      computeForces(tol,hts,eps); 
      forcecalcend = System.currentTimeMillis();
      forcecalctime += forcecalcend - forcecalcstart;

      /* advance bodies */
      updatestart = System.currentTimeMillis();
      Advance(hts,ts);

      updateend = System.currentTimeMillis();
      updatetime += updateend - updatestart;
      trackend = System.currentTimeMillis();
      tracktime += trackend - trackstart;

      /* compute bounding box */
      setbound();
      nstep++;
      tnow = tnow + incr;
      totalstep++;
      computeend = System.currentTimeMillis();
      System.out.print("step = ");
      System.out.print(totalstep);
      System.out.print(" simulated time = ");
      System.out.print(tnow);
      System.out.print(" compute time = ");
      System.out.print(computeend);
      System.out.print(" "); 
      System.out.print(computestart);
      System.out.print(" "); 
      System.out.print((computeend-computestart)/1000.0);
      System.out.print("\n");
      if (totalstep == 2) { 
        clearTiming();
        clearStats();
        computestart = System.currentTimeMillis();
      }
    }
    computeend = System.currentTimeMillis();
    computeend = computeend - computestart;
    computestart = 0;
  }
  NBodySystem() {  
    nbody = 0; 
    cells = null; 
    leaves = null; 
    bodies = null; 
  }
  bodyset getbodies()    { return bodies; }
  leafset getleaves()    { return leaves; }
  cellset getcells()     { return cells; }

 
  void dump(){
    int i;
    body p;
 
    for (i = 0; i < nbody ; i++){
      p = bodies.getbody(i);
      p.dump();
    }
  }

  void init() {
    etot = new vec();
    keten = new mat();
    peten = new mat();
    cmphasePos = new vec();
    cmphaseVel = new vec();
    amvec = new vec();
    mincorner = new vec();
    maxcorner = new vec();
  }

  void clearStats(){
    nstep = 0;
    n2bcalc = 0;
    nbccalc = 0;
  }

  void intcoord(int xp[], vec rp) {
    int k;
    double xsc;
    vec v = new vec();

    mincorner.store(v);
    for(k = 0; k < vec.NDIM; k++) {
      xsc = (rp.value[k] - v.value[k]) / size;
        xp[k] = (int)(Math.floor(node.IMAX * xsc));
    }
  }

  void updateStats(){
    int nttot, nbavg, ncavg;
    nttot = n2bcalc + nbccalc;
    nbavg = (int) ((double) n2bcalc / (double) nbody);
    ncavg = (int) ((double) nbccalc / (double) nbody);
  }

  void outputStats(){
    System.out.print("\n\nINTERACTIONS\tTOTAL\tITER\tAVG\n");
    System.out.print("body-body\t\t");
    System.out.print(n2bcalc);
    System.out.print("\t"); 
    System.out.print(n2bcalc/nstep);
    System.out.print("\t");
    System.out.print((int)Math.sqrt((double)(n2bcalc/nstep)));
    System.out.print("\n");
    System.out.print("body-cell\t\t");
    System.out.print(nbccalc);
    System.out.print("\t");
    System.out.print(nbccalc/nstep);
    System.out.print("\t");
    System.out.print((int)Math.sqrt((double)(nbccalc/nstep)));
    System.out.print(" \n");
  }

// ----------------------------------------------------------------------------
  void updateEnergy() {
    int k;
    mat m1 = new mat();
    vec v0 = new vec();
    vec v1 = new vec();
    vec v2 = new vec();
    vec v3 = new vec();
    vec v4 = new vec();
    double m, velsq;
    body p;

    mtot = 0.0;
    etot.clear();
    keten.clear();
    peten.clear();
    cmphasePos.clear();
    cmphaseVel.clear();
    amvec.clear();
    v4.clear();
    for(k = 0; k < nbody; k++) {
      p = bodies.getbody(k);
      m = p.Mass();
      mtot += m;
      p.VelvecStore(v1);
      velsq = v1.dotProd(v1);
      v4.value[1] += 0.5 * m * velsq;
      v4.value[2] += 0.5 * m * p.Phi();
      p.PosvecStore(v1);
      p.VelvecStore(v2);
      p.AccvecStore(v3);
      v0.load(v2);
      v0.scale(0.5*m);
      m1.outProd(v0,v2);
      keten.add(m1);
      v0.load(v1);
      v0.scale(m);
      m1.outProd(v0,v3);
      peten.add(m1);
      v0.load(v1);
      v0.scale(m);
      cmphasePos.add(v0);
      v0.load(v2);
      v0.scale(m);
      cmphaseVel.add(v0);
      v0.crossProd(v1,v2);
      v0.scale(m);
      amvec.add(v0);
    }
    v4.value[0] = v4.value[1] + v4.value[2];
    etot.load(v4);

    if (mtot != 0.0){
      cmphasePos.scale(1.0/mtot);
      cmphaseVel.scale(1.0/mtot);
    }
  }

  void outputEnergy(){
    System.out.print("\n\nENERGY TOTALS\n");
    System.out.print(" MASS):        ");
    System.out.print(mtot); 
    System.out.print("\n");
    System.out.print(" ENERGY:     "); 
    etot.print(); 
    System.out.print("\n");
    System.out.print(" KIN ENERGY: ");
    keten.print(); 
    System.out.print("\n");
    System.out.print(" POT ENERGY: "); 
    peten.print(); 
    System.out.print("\n");
    System.out.print(" AGGREGATE CM POS: "); 
    cmphasePos.print(); 
    System.out.print("\n");
    System.out.print(" AGGREGATE CM VEL: "); 
    cmphaseVel.print(); 
    System.out.print("\n");
    System.out.print(" ANGULAR MOMENTUM: "); 
    amvec.print(); 
    System.out.print("\n");
  }

  void output(parms sim) {
    if( (tout - 0.01 * sim.getdtime()) <= tnow)
      tout += sim.getdtout();
  }
  
  void clearTiming(){
    tracktime = 0;
    updatetime = 0;
    partitiontime = 0;
    treebuildtime = 0;
    forcecalctime = 0;
  }

  void initOutput(parms p){
    System.out.print("\n\t\t");
    System.out.print(p.getheadline());
    System.out.print("\n\n");
    System.out.print("nbody   dtime   eps   tol   dtout   tstop   fcells\n");
    System.out.print(nbody);
    System.out.print("     ");
    System.out.print(p.getdtime());
    System.out.print("   ");
    System.out.print(p.geteps());
    System.out.print("   ");
    System.out.print(p.gettol());
    System.out.print("   ");
    System.out.print(p.getdtout());
    System.out.print("    ");
    System.out.print(p.gettstop());
    System.out.print("   ");
    System.out.print(p.getfcells());
    System.out.print("\n\n");
  }

  void outputTiming(){
    int ticks;
    double dticks;
  
    dticks = 1000.0;
    System.out.print("COMPUTESTART  = ");
    System.out.print((computestart / dticks));
    System.out.print("\n");
    System.out.print("COMPUTEEND    = ");
    System.out.print((computeend / dticks));
    System.out.print("\n");
    System.out.print("COMPUTETIME   = ");
    System.out.print(((computeend - computestart) / dticks));
    System.out.print("\n");
    System.out.print("TRACKTIME     = ");
    System.out.print((tracktime / dticks));
    System.out.print("\n");
    System.out.print("PARTITIONTIME = ");
    System.out.print((partitiontime / dticks));
    System.out.print("\t");
    System.out.print(((float)partitiontime)/tracktime);
    System.out.print("\n");
    System.out.print("TREEBUILDTIME = ");
    System.out.print((treebuildtime / dticks));
    System.out.print("\t");
    System.out.print(((float)treebuildtime)/tracktime);
    System.out.print("\n");
    System.out.print("FORCECALCTIME = ");
    System.out.print((forcecalctime / dticks));
    System.out.print("\t");
    System.out.print(((float)forcecalctime)/tracktime);
    System.out.print("\n");
    System.out.print("UPDATEPOSTIME = ");
    System.out.print((updatetime / dticks));
    System.out.print("\t");
    System.out.print(((float)updatetime)/tracktime);
    System.out.print("\n");
    System.out.print("RESTTIME      = ");
    System.out.print(
      ((tracktime - partitiontime -
       updatetime - treebuildtime - forcecalctime)
      / dticks));
    System.out.print("\t");
    System.out.print(((float)(tracktime-partitiontime- updatetime - treebuildtime-
          forcecalctime))/tracktime);
    System.out.print("\n");
  }

  void loadData(String name) throws java.io.FileNotFoundException, java.io.IOException {
    int i, ndim;
    body p;
    double m; 
    vec tmp = new vec();
    DataInputStream instr = new DataInputStream(new FileInputStream(name));
  
    System.err.print("Reading input file : ");
    System.err.print(name);
    System.err.print("\n");
    if (instr == null) {
       System.err.print("loadData: cannot find file ");
       System.err.print(name);
       System.err.print("\n");
    }
    nbody = instr.readInt();
    if (nbody < 1) {
      System.err.print("loadData: nbody = ");
      System.err.print(nbody);
      System.err.print(" is absurd\n");
    }
    ndim = instr.readInt();
    if(ndim != vec.NDIM) {
      System.err.print("inputdata: NDIM = ");
      System.err.print(vec.NDIM);
      System.err.print("ndim = ");
      System.err.print(ndim);
      System.err.print(" is absurd\n");
    }
    tnow = instr.readInt();
  
    for(i = 0; i < nbody; i++){
      p = bodies.getbody(i);
      m = instr.readDouble();
      p.setMass(m);
    }
    for(i = 0; i < nbody; i++){
      p = bodies.getbody(i);
      tmp.read(instr);
      p.PosvecLoad(tmp);
    }
    for(i = 0; i < nbody; i++){
      p = bodies.getbody(i);
      tmp.read(instr);
      p.VelvecLoad(tmp);
    }
    instr.close();
  }

  void loadTestData(){
    double rsc, vsc, r, v, x, y, offset, tmp;
    vec cmr = new vec();
    vec cmv = new vec();
    vec tmpv = new vec();
    vec tmpv2 = new vec();
    body p, cp;
    int halfnbody, i, k;
  
    tnow = 0.0;
    rsc = 9 * Math.PI / 16;
    vsc = Math.sqrt(1.0 / rsc);
  
    cmr.clear();
    cmv.clear();
  
    halfnbody = nbody / 2;
    if (nbody % 2 != 0) halfnbody++;
  
    for(k = 0; k < halfnbody; k++) {
      p = bodies.newbody();
      p.setMass((double)(1.0/nbody));
  
      /* reject radii greater than 10 */
      do {
        tmp = random.xrand(0.0, 0.999);
        r = 1.0 / Math.sqrt(Math.pow(tmp, -2.0/3.0) - 1);
      } while (r > 9.0);
  
      pickshell(tmpv, rsc * r);
        p.PosvecAdd(tmpv);
      cmr.add(tmpv);
      do{
        x = random.xrand(0.0, 1.0);
        y = random.xrand(0.0, 0.1);
      } while (y > x*x * Math.pow(1 - x*x, 3.5));
      v = Math.sqrt(2.0) * x / Math.pow(1 + r*r, 0.25);
      pickshell(tmpv, vsc * v);
      p.VelvecAdd(tmpv);
      cmv.add(tmpv);
    }
  
    offset = 4.0;
    for(k = halfnbody; k < nbody; k++) {
      p = bodies.newbody();
      p.setMass((double)(1.0/nbody));
  
      cp = bodies.getbody(k-halfnbody);
      cp.PosvecStore(tmpv);
      tmpv2.clear();
      for(i=0; i < vec.NDIM; i++){
        tmpv2.value[i] = tmpv.value[i] + offset;
        cmr.add(tmpv2);
      }
      p.PosvecAdd(tmpv2);
      cp.VelvecStore(tmpv);
      tmpv2.clear();
      for(i=0; i < vec.NDIM; i++){
        tmpv2.value[i] = tmpv.value[i];
          cmv.add(tmpv2);
      }
      p.VelvecAdd(tmpv2);
    }
  
    cmr.scale(1.0/nbody);
    cmv.scale(1.0/nbody);
  
    for(k=0; k < nbody; k++) {
      p = bodies.getbody(k);
      p.PosvecStore(tmpv);
      tmpv.sub(cmr);
      p.PosvecLoad(tmpv);
      p.VelvecStore(tmpv);
      tmpv.sub(cmv);
      p.VelvecLoad(tmpv);
    }
  }

  void setbound() {
    int i;
    vec tmp1 = new vec();
    vec tmp2 = new vec();
    double side;
    body p;
  
    mincorner.unit();
    mincorner.scale(1E99);
    maxcorner.unit();
    maxcorner.scale(-1E99);
    for(i=0; i < nbody; i++) {
      p = bodies.getbody(i);
      p.PosvecStore(tmp1);
      mincorner.min(tmp1);
      maxcorner.max(tmp1);
    }
  
    side = 0.0;
    maxcorner.store(tmp1);
    mincorner.store(tmp2);
    tmp1.sub(tmp2);
    for(i=0; i < vec.NDIM; i++)
      if(side < tmp1.value[i])
        side = tmp1.value[i];
    
    for(i=0; i < vec.NDIM; i++)
      tmp1.value[i] = -side/100000;
    mincorner.add(tmp1);
    size = 1.00002*side;
  }
  
  void maketreeIteration(int i) { 
      int xp[] = new int[vec.NDIM];
      vec v = new vec();

      body p = bodies.getbody(i);
      p.PosvecStore(v);
      intcoord(xp,v);
      BH_tree.CellAddBody(p,xp);
  }

  void maketreeParallelLoop(final int first, final int num) {
    final NBodySystem nb = this;
    final int numThreads = barnes.numThreads;
    barnes.run(new Runnable () {
	public void run () {
	  int j = 0;
	  maketreeThread t[] = new maketreeThread[numThreads+1];
	  for (int i = 0; i < num; i+=(int)(num/numThreads)) {
	    t[j] = new maketreeThread(nb, first+i,
				      ((i+numThreads)<num)?
				      (first+i+numThreads):(first+num));
	    t[j++].start();
	  }
	  for (int i = 0; i < j; i++) {
	    try {
	      t[i].join();
	    } catch (java.lang.InterruptedException e) {
	      System.err.print("InterruptedException in maketreeParallelLoop\n");
	    }
	  }
	}
      });
  }

  void maketreeLoop(final int first, final int num) {
    final NBodySystem nb = this;
    barnes.run(new Runnable () {
	public void run () {
	  for (int i = 0; i < num; i++) {
	    nb.maketreeIteration(first+i);
	  }
	}
      });
  }
  
  void maketree() throws java.lang.InterruptedException {
    if (barnes.simparms.getiparam("parallel") == 0) {
      maketreeLoop(0, nbody);
    } else if (barnes.simparms.getiparam("parallel") == 1) {
      maketreeParallelLoop(0, nbody);
    } 
  }

  void AdvanceIteration(double hts, double ts, int i) { 
      body p;
      p = bodies.getbody(i);
      p.advance(hts, ts);
  }

  void AdvanceParallelLoop(final double hts, final double ts, final int first, final int num) {
    final NBodySystem nb = this;
    final int numThreads = barnes.numThreads;
    barnes.run(new Runnable () {
	public void run () {
	  int j = 0;
	  AdvanceThread t[] = new AdvanceThread[numThreads+1];
	  for (int i = 0; i < num; i+=(int)(num/numThreads)) {
	    t[j] = new AdvanceThread(nb, hts, ts, first+i,
				     ((i+numThreads)<num)?
				     (first+i+numThreads):(first+num));
	    t[j++].start();
	  }
	  for (int i = 0; i < j; i++) {
	    try {
	      t[i].join();
	    } catch (java.lang.InterruptedException e) {
            System.err.print("InterruptedException in AdvanceParallelLoop\n");
	    }
	  }
	}
      });
  }

  void AdvanceLoop(final double hts, final double ts, final int first, final int num) {
    final NBodySystem nb = this;
    barnes.run(new Runnable () {
	public void run () {
	  for (int i = 0; i < num; i++) {
	    nb.AdvanceIteration(hts, ts, first+i);
	  }
	}
      });
  }

  void Advance(double hts, double ts) throws java.lang.InterruptedException {
    if (barnes.simparms.getiparam("parallel") == 0) {
      AdvanceLoop(hts, ts, 0, nbody);
    } else if (barnes.simparms.getiparam("parallel") == 1) {
      AdvanceParallelLoop(hts, ts, 0,nbody);
    } 
  }

  void SwapAccsIteration(int i) { 
      body p;
      p = bodies.getbody(i);
      p.swapAcc();
  }

  void SwapAccsParallelLoop(final int first, final int num) {
    final NBodySystem nb = this;
    final int numThreads = barnes.numThreads;
    barnes.run(new Runnable () {
	public void run () {
	  int j = 0;
	  SwapAccsThread t[] = new SwapAccsThread[numThreads+1];
	  for (int i = 0; i < num; i+=(int)(num/numThreads)) {
	    t[j] = new SwapAccsThread(nb, first+i,
				      ((i+numThreads)<num)?
				      (first+i+numThreads):(first+num));
	    t[j++].start();
	  }
	  for (int i = 0; i < j; i++) {
	    try {
	      t[i].join();
	    } catch (java.lang.InterruptedException e) {
	      System.err.print("InterruptedException in SwapAccsParallelLoop\n");
	    }
	  }
	}
      });
  }

  void SwapAccsLoop(final int first, final int num) {
    final NBodySystem nb = this;
    barnes.run(new Runnable () {
	public void run () {
	  for (int i = 0; i < num; i++) {
	    nb.SwapAccsIteration(first+i);
	  }
	}
      });
  }

  void SwapAccs() throws java.lang.InterruptedException {
    if (barnes.simparms.getiparam("parallel") == 0) {
      SwapAccsLoop(0, nbody);
    } else if (barnes.simparms.getiparam("parallel") == 1) {
      SwapAccsParallelLoop(0, nbody);
    } 
  }

  void ComputeAccelsIteration(double tol, double eps, int i) { 
      body p;
      p = bodies.getbody(i);
      p.walksub(BH_tree, tol, size*size, eps);
  }

  void ComputeAccelsParallelLoop(final double tol, final double eps, final int first, final int num) {
    final NBodySystem nb = this;
    final int numThreads = barnes.numThreads;
    barnes.run(new Runnable () {
	public void run() {
	  int j = 0;
	  ComputeAccelsThread t[] = new ComputeAccelsThread[numThreads+1];
	  for (int i = 0; i < num; i+=(int)(num/numThreads)) {
	    t[j] = new ComputeAccelsThread(nb, tol, eps, first+i,
					   ((i+numThreads)<num)?
					   (first+i+numThreads):(first+num));
	    t[j++].start();
	  }
	  for (int i = 0; i < j; i++) {
	    try {
	      t[i].join();
	    } catch (java.lang.InterruptedException e) {
	      System.err.print("InterruptedException in ComputeAccelsParallelLoop\n");
	    }
	  }
	}
      });
  }

  void ComputeAccelsLoop(final double tol, final double eps, final int first, final int num) {
    final NBodySystem nb = this;
    barnes.run(new Runnable () {
	public void run() {
	  for (int i = 0; i < num; i++) {
	    nb.ComputeAccelsIteration(tol, eps, first+i);
	  }
	}
      });
  }

  void ComputeAccels(double tol, double eps) throws java.lang.InterruptedException {
    if (barnes.simparms.getiparam("parallel") == 0) {
      ComputeAccelsLoop(tol, eps, 0, nbody);
    } else if (barnes.simparms.getiparam("parallel") == 1) {
      ComputeAccelsParallelLoop(tol, eps, 0,nbody);
    } 
  }

  void StartVelsIteration(double hts, int i) { 
      body p;
      p = bodies.getbody(i);
      p.startVel(hts);
  }

  void StartVelsParallelLoop(final double hts, final int first, final int num) {
    final NBodySystem nb = this;
    final int numThreads = barnes.numThreads;
    barnes.run(new Runnable () {
	public void run() {
	  int j = 0;
	  StartVelsThread t[] = new StartVelsThread[numThreads+1];
	  for (int i = 0; i < num; i+=(int)(num/numThreads)) {
	    t[j] = new StartVelsThread(nb, hts, first+i,
				       ((i+numThreads)<num)?
				       (first+i+numThreads):(first+num));
	    t[j++].start();
	  }
	  for (int i = 0; i < j; i++) {
	    try {
	      t[i].join();
	    } catch (java.lang.InterruptedException e) {
	      System.err.print("InterruptedException in StartVelsParallelLoop\n");
	    }
	  }
	}
      });
  }

  void StartVelsLoop(final double hts, final int first, final int num) {
    final NBodySystem nb = this;
    barnes.run(new Runnable () {
	public void run() {
	  for (int i = 0; i < num; i++) {
	    nb.StartVelsIteration(hts, first+i);
	  }
	}
      });
  }
  
  void StartVels(double hts) throws java.lang.InterruptedException {
    if (barnes.simparms.getiparam("parallel") == 0) {
      StartVelsLoop(hts, 0, nbody);
    } else if (barnes.simparms.getiparam("parallel") == 1) {
      StartVelsParallelLoop(hts, 0, nbody);
    } 
  }

  void computeForces(double tol, double hts, double eps) throws java.lang.InterruptedException {
    SwapAccs();
    ComputeAccels(tol,eps);
    if(nstep > 0){
      StartVels(hts);
    }
  }
}
  
class random {
  static final int       MULT    = 1103515245;
  static final int       ADD     = 12345;
  static final int       MASK    = 0x7FFFFFFF;
  static final double    TWOTO31 = 2147483648.0;
  
  static int A = 1;
  static int B = 0;
  static int randx = 1;
  static int lastrand;   /* the last random number */

  static double prand() {
     lastrand = randx;
     randx = (A*randx+B) & MASK;
     return((double)lastrand/TWOTO31);
  }

  static double xrand(double xl, double xh) { 
    return (xl + (xh - xl) * prand()); }
  
  static void pranset(int seed) {
     A = 1; B = 0;
     randx = (A*seed+B) & MASK;
     A = (MULT * A) & MASK;
     B = (MULT*B + ADD) & MASK;
  }
}

class maketreeThread extends Thread {
  NBodySystem nb;
  int i, j;

  maketreeThread(NBodySystem n, int i, int j) {
    nb = n;
    this.i = i;
    this.j = j;
  }
  public void run() {
    for (int idx=i; idx<j; idx++) { 
      nb.maketreeIteration(idx);
    }
  }
}

class AdvanceThread extends Thread {
  NBodySystem nb;
  double hts, ts;
  int i, j;

  AdvanceThread(NBodySystem b, double h, double t, int i, int j) { 
    nb = b;
    hts = h;
    ts = t;
    this.i = i;
    this.j = j;
  }

  public void run() { 
    for (int idx=i; idx<j; idx++) {
      nb.AdvanceIteration(hts,ts,idx);
    }
  }
}

class ComputeAccelsThread extends Thread {
  NBodySystem nb;
  double tol, eps;
  int i, j;

  ComputeAccelsThread(NBodySystem b, double t, double e, int i, int j) { 
    nb = b;
    tol = t;
    eps = e;
    this.i = i;
    this.j = j;
  }
  public void run() { 
    for (int idx=i; idx<j; idx++) {
      nb.ComputeAccelsIteration(tol,eps,idx);
    }
  }
}

class SwapAccsThread extends Thread {
  NBodySystem nb;
  int i, j;

  SwapAccsThread(NBodySystem n, int i, int j) {
    nb = n;
    this.i = i;
    this.j = j;
  }
  public void run() {
    for (int idx=i; idx<j; idx++) {
      nb.SwapAccsIteration(idx);
    }
  }
}

class StartVelsThread extends Thread {
  NBodySystem nb;
  double hts;
  int i, j;

  StartVelsThread(NBodySystem b, double h, int i, int j) { 
    nb = b;
    hts = h;
    this.i = i;
    this.j = j;
  }

  public void run() { 
    for (int idx=i; idx<j; idx++) {
      nb.StartVelsIteration(hts, idx);
    }
  }
}

class barnes { 
  public static parms simparms = new parms();
  public static NBodySystem Nbody = new NBodySystem();
  public static final int NO_RTJ = 0;
  public static final int CT_MEMORY = 1;
  public static final int VT_MEMORY = 2;
  public static long ctsize = 0;
  public static int RTJ_alloc_method;
  public static int numThreads;

  public static void run(Runnable r) {
    switch (RTJ_alloc_method) {
    case NO_RTJ: {
      r.run();
      break;
    }
    case CT_MEMORY: {
      (new javax.realtime.CTMemory(ctsize)).enter(r);
      break;
    }
    case VT_MEMORY: {
      (new javax.realtime.VTMemory(1000, 1000)).enter(r);
      break;
    } 
    default: {
      System.out.println("Invalid memory area type!");
      System.exit(1);
    }
    }
  }
    
  public static void main(String args[]) throws java.lang.InterruptedException, java.io.IOException, java.io.FileNotFoundException { 
    if (args.length < 2) {
      System.out.print("usage: java barnes <input filename> <numThreads> <noRTJ | CT | VT> [stats | nostats] [ctsize]\n");
      return;
    }
    if (args[2].equalsIgnoreCase("noRTJ")) {
      RTJ_alloc_method = NO_RTJ;
    } else if (args[1].equalsIgnoreCase("CT")) {
      RTJ_alloc_method = CT_MEMORY;
      ctsize = Long.parseLong(args[4]);
    } else if (args[1].equalsIgnoreCase("VT")) {
      RTJ_alloc_method = VT_MEMORY;
    } else {
      System.out.println("Invalid memory area type argument");
      return;
    }
    numThreads = Integer.parseInt(args[1]);

    simparms.readParameterFile(args[0]);
    Nbody.init();
    Nbody.startrun(simparms);
    Nbody.initOutput(simparms);
    Nbody.setbound();
    Nbody.clearTiming();
    Nbody.clearStats();
    Nbody.stepsystem(simparms);
    Nbody.outputTiming();
    Nbody.output(simparms);
    if (simparms.getiparam("dump") == 1) { 
      Nbody.dump();
    }

    if ((RTJ_alloc_method != NO_RTJ) &&
	(args[3].equalsIgnoreCase("stats"))) {
      javax.realtime.Stats.print();
    }
  }
}
