// strongarm.flex, created Tue Feb 16 23:26:16 1999 by andyb
// Copyright (C) 1999 Andy Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

%% 
// inside the class
private void emitMoveConst(Temp t, Number n) {
    // constant immediates are rather restricted in possible values in the
    // StrongARM. Most simplistic approach to get values which will
    // be valid is to split a value up into 8-bit segments.
    int i = n.intValue(), j = 0;
    int vals[] = new int[4];
    if (i & 0xff != 0) vals[j++] = i & 0xff;
    if (i & 0xff00 != 0) vals[j++] = i & 0xff00;
    if (i & 0xff0000 != 0) vals[j++] = i & 0xff0000;
    if (i & 0xff000000 != 0) vals[j++] = i & 0xff000000;
    if (j == 0) {
        emit(new Instr("mov `d0, #0", null, new Temp[] { t.temp() }));
    } else {
        emit(new Instr("mov `d0, #"+vals[0], null, new Temp[] { t.temp() }));
        for (int k = 1; k < j; k++)
            emit(new Instr("add `d0, `s0, #"+vals[k],
                           new Temp[] { t.temp() },
                           new Temp[] { t.temp() }));
    }
}
%%

MOVE(MEM<i,a>(CONST(c)), e1) %extra { t } %weight <numinstr, 2>
%{
emitMoveConst(t.temp(), c);
emit(new Instr("str `s0, [`s1, #0]", 
               new Temp[] { e1.temp(), t.temp() }, 
               null));
}%

MOVE(MEM<l>(CONST(c)), e1) %extra { t }	%weight <numisntr, 3>
%{
emitMoveConst(t.temp(), c);
emit(new Instr("str `s0, [`s1, #0]", 
               new Temp[] { e1.high(), t.temp() }, 
               null));
emit(new Instr("str `s0, [`s1, #4]", 
               new Temp[] { e1.low(), t.temp() }, 
               null));
}%

MOVE(TEMP<i,a>(t), e2) %weight <numinstr, 1>
%{
emit(new Instr("mov `d0, `s0", 
               new Temp[] { e2.temp() }, 
               new Temp[] { t.temp() }));
}%

MOVE(TEMP<l>(t), e2) %weight <numinstr, 2>
%{
emit(new Instr("mov `d0, `s0", 
               new Temp[] { e2.low() }, 
               new Temp[] { t.low() }));
emit(new Instr("mov `d0, `s0", 
               new Temp[] { e2.high() }, 
               new Temp[] { t.high() }));
}%

BINOP<i,a>(ADD, e1, e2)=t3 %weight <numinstr, 1>
%{
emit(new Instr("add `d0, `s1, `s2", 
               new Temp[] { e1.temp(), e2.temp() }, 
               new Temp[] { t3.temp() }));
}%

BINOP<l>(ADD, e1, e2)=t3 %weight <numinstr, 2>
%{
emit(new Instr("adds `d0, `s1, `s2",
               new Temp[] { e1.low(), e2.low() },
               new Temp[] { t3.low() }));
emit(new Instr("adc `d0, `s1, `s2",
               new Temp[] { e1.high(), e2.high() },
               new Temp[] { t3.high() }));
}%

JUMP(NAME(l)) %weight <numinstr, 1>
%{
emit(new Instr("b " + l.toString(),
               null,
               null));
}%

JUMP(e1) %weight <numinstr, 1>
%{
emit(new Instr("mov r15, `s0",
               new Temp[] { e1.temp(); },
               null));
// delay slot????
}%

LABEL(l) %weight <numinstr, 0>
%{
emit(new InstrLABEL(l.toString() + ":", l));
}%

CONST<i,a>(c)=t %weight <numinstr, 1>
%{
emitMoveConst(t.temp(), c);
}%

CONST<l>(c)=t %weight <numinstr, 2>
%{
emitMoveConst(t.low(), new Number(c.intValue()));
emitMoveConst(t.high(), new Number(c.longValue() >>> 32));
}%
