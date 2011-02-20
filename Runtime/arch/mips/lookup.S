#include <regdef.h>
#include <config.h>
#include "segments.h"

 # _lookup: tweak the return address on an exception so that execution 
 #          resumes at the proper exception handler.
 # effect:  changes the return address of the caller to
 #	   the fixed-up value found in the fixup table.
 # clobbers: a0, a1, a2, a3, t4. Does not touch v0.
 #  It uses arg regs since they tend to have short live ranges anyway
 # [note that this function does not adhere to standard calling convention]
		

CODE_SEGMENT

1:	.word fixup_start	    # first entry in fixup table
	.word fixup_end - 8	    # last entry in fixup table

	.globl _lookup_handler
   	.ent _lookup_handler
_lookup_handler:
    lw   a0, -4(fp)  # key (== return address of caller), put there by
                     # calling convention  
	lw   a1, 1b		 # left
	lw   a2, 1b + 4  # right
2:	blt  a2, a1, 3f  # while (r >= l) ...
    srl  a3, a1, 1
    srl  t4, a2, 1   # High bit is set in vpekoe, so avoid int overflow
	addu a3, a3, t4  # x = (l + r) / 2
    srl  a3, a3, 3   # records are 8 byte aligned
    sll  a3, a3, 3
	lw   t4, 0(a3)   # load x->label
	beq  a0, t4, 4f	 # compare key with x->label
    blt  a0, t4, 5f  # if (key < x->label) r = x-1;
    addu a1, a3, 8   # else l = x+1;
    b    2b
5:  
    sub  a2, a3, 8    
	b    2b			 # loop if not found.
4:      
	lw   a3, 4(a3)   # found match!  load x->value.
	sw   a3, -4(fp)  # and store as return address of caller.
	j    ra          # return from _lookup.
3:
  	lw   $0, 1($0)   #  [die horrible death if no match found]
    .end _lookup_handler
