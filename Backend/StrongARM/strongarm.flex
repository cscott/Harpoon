// this is my parser.
//global scope
%% --> public class
// inside the class
%%

// Some notes:
// Perhaps make all Tree's (or at least Exp's) Typed? Will deal with
// calling subtress, etc.
// 
// Allow multiple definitions for each tree pattern based on type ->
//  allows each definition to share common namespace

MOVE(MEM(CONST(c)), e1) 
%type <e1, INT> <e1, POINTER>
{
Temp t;
emit(new Instr("mov `d0, #"+c, null, new Temp[] { t }));
emit(new Instr("str `s0, [`s1, #0]", new Temp[] { e1, t }, null));
}
%type <e1, LONG>
{
Temp t;
emit(new Instr("mov `d0, #"+c, null, new Temp[] { t }));
emit(new Instr("str `s0, [`s1, #0]", new Temp[] { e1.high(), t }, null));
emit(new Instr("str `s0, [`s1, #4]", new Temp[] { e1.low(), t }, null));
}

MOVE(e1, e2) 
%type <e1, INT> <e1, POINTER>
{
emit(new Instr("mov `d0, `s0", new Temp[] { e2 }, new Temp[] { e1 }));
}
%type <e1, LONG>
{
emit(new Instr("mov `d0, `s0", new Temp[] { e2.low() }, new Temp[] { e1.low() }));
emit(new Instr("mov `d0, `s0", new Temp[] { e2.hi() }, new Temp[] { e1.hi() }));
}

// further work:
1) is this powerful enough?  will it handle the tricky ARM cases?
2) is this compact enough?  do we need to write a lot of repeated code/elements
3) is this clear?
4) is this complete: are there Tree forms that don't work easily.
think also about typing:
  MEM
  MEM32
  MEM64
  MEMI
  MEMF
  MEM32I
  MEM32F
  MEM64I
  MEM64F

MOVE(m1=MEM(CONST(c)), e) %pred %( m1 instanceof MEMA )% // nah.
