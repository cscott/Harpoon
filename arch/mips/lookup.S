#include <regdef.h>
#include "segments.h"

# _lookup: tweak the return address on an exception so that execution
#          resumes at the proper exception handler.
# effect:  changes the return address of the caller to
#	   the fixed-up value found in the fixup table.
# clobbers: r1, r2, r3, condition codes. Does not touch r0.
# [note that this function does not adhere to standard calling convention]
		

CODE_SEGMENT
	
1:	.word fixup_start	    # first entry in fixup table
	.word fixup_end - 8	    # last entry in fixup table

	.globl _lookup_handler
_lookup_handler:    
	lw  t0, -4(fp)	# key (== return address of caller)
	lw  t1, 1b		# left
	lw  t2, 1b+4    # right
2:	blt t2, t1, 3f  # while (r >= l) ...
	add t4, t1, t2	# x = (l + r) / 2
    div t4, 2
	ldr t5, 0(t4)   # load x->label
	beq t0, t5, 4f		# compare key with x->label
    blt t0, t5, 5f      # if (key < x->label) r = x-1;
    add t1, t4, 8       # else l = x+1;
    b   2b
5:  
    sub   t2, t4, 8    
	b     2b			# loop if not found.
4:      
	lw  t4, 4(t4)   # found match!  load x->value.
	sw  t4, -4(fp)  # and store as return address of caller.
	j  $31          # return from _lookup.
3:
  	lw  $0, 1($0)   #  [die horrible death if no match found]
    .end _lookup_handler