// this is my parser.
//global scope
%% --> public class
// inside the class
%%

MOVE(MEM<i,a>(CONST(c)), e1) %extra { t }
{
emit(new Instr("mov `d0, #"+c, null, new Temp[] { t.temp() }));
emit(new Instr("str `s0, [`s1, #0]", new Temp[] { e1.temp(), t.temp() }, null));
}

MOVE(MEM<l>(CONST(c)), e1) %extra { t }
{
emit(new Instr("mov `d0, #"+c, null, new Temp[] { t.temp() }));
emit(new Instr("str `s0, [`s1, #0]", new Temp[] { e1.high(), t.temp() }, null));
emit(new Instr("str `s0, [`s1, #4]", new Temp[] { e1.low(), t.temp() }, null));
}

MOVE(TEMP<i,a>(t), e2) 
{
emit(new Instr("mov `d0, `s0", new Temp[] { e2.temp() }, new Temp[] { t.temp() }));
}

MOVE(TEMP<l>(t), e2)
{
emit(new Instr("mov `d0, `s0", new Temp[] { e2.low() }, new Temp[] { t.low() }));
emit(new Instr("mov `d0, `s0", new Temp[] { e2.high() }, new Temp[] { t.high() }));
}

BINOP<i,a>(ADD, t1, t2)=t3
{
emit(new Instr("add `d0, `s1, `s2", new Temp[] { t1.temp(), t2.temp() }, new Temp[] { t3.temp() }));
}

// further work:
1) is this powerful enough?  will it handle the tricky ARM cases?
2) is this compact enough?  do we need to write a lot of repeated code/elements
3) is this clear?
4) is this complete: are there Tree forms that don't work easily.
