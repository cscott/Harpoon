## Flex_MIPS.pl a perl script to generate a .S assembly file (that
## is run through cpp before hitting the assembler)
## Flex_Mips.S, created Thu Jun 15 15:14:34 EDT 2000 by witchel
## Copyright (C) 2000 Emmett Witchel <witchel@lcs.mit.edu>
## Licensed under the terms of the GNU GPL; see COPYING for details.
## This is a library of support routines.  They form the necessary
##	link time and run time support for the flex compiler system on
##	IRIX/MIPS.


sub print_ll_shifts {
print <<'LLSHIFTSDONE';
.text    
.align 2    
    /* All LL shift routines come from Kane, MIPS RISC Architecture */
    /* V1:  V0 = A1:A0 << (A2 mod 64) */
    .globl __ll_lshift
    .ent __ll_lshift
__ll_lshift:
    .set noreorder
    .cpload $25
    .set reorder
    .frame sp, 0, $31
    sll  t0, a2, 32-6
    bgez t0, 1f
    sll  v1, a0, a2
    li   v0, 0
    b    3f
1:  sll  v1, a1, a2
    beq  t0, 0, 2f
    negu t1, a2
    srl  t2, a0, t1
    or   v1, t2   
2:  sll  v0, a0, a2
3:
    j    $31
.end __ll_lshift

    /* Logical  right shift */
    /* V1:  V0 = A1:A0 >>> (A2 mod 64) */
.globl __ull_rshift
.ent __ull_rshift
__ull_rshift:
    .set noreorder
    .cpload $25
    .set reorder
    .frame sp, 0, $31
    sll  t0, a2, 32-6
    bgez t0, 1f
    srl  v0, a1, a2
    li   v1, 0
    b    3f
1:  srl  v0, a0, a2
    beq  t0, 0, 2f
    negu t1, a2
    srl  t2, a1, t1
    or   v0, t2   
2:  sll  v1, a1, a2
3:
    j    $31
.end __ull_rshift
        
    /* Arithmetic right shift */
    /* V1:  V0 = A1:A0 >> (A2 mod 64) */
.globl __ll_rshift
.ent __ll_rshift
__ll_rshift:
    .set noreorder
    .cpload $25
    .set reorder
    .frame sp, 0, $31
    sll  t0, a2, 32-6
    bgez t0, 1f
    sra  v0, a1, a2
    sra  v1, a1, 31
    b    3f
1:  srl  v0, a0, a2
    beq  t0, 0, 2f
    negu t1, a2
    sll  t2, a1, t1
    or   v0, t2   
2:  sra  v1, a1, a2
3:
    j    $31
.end __ll_rshift

    /* V1:V0 = A1:A0 * A3:A2  12 inst, 42 cycles */
.globl __ll_mul
.ent __ll_mul
__ll_mul:    
    .set noreorder
    .cpload $25
    .set reorder
    .frame sp, 0, $31
    multu  a0, a2
    mflo   v0
    mfhi   v1
    multu  a1, a2
    mflo   t0
    addu   v1, t0
    multu  a0, a3
    mflo   t0
    addu   v1, t0
    j      $31
.end __ll_mul
LLSHIFTSDONE
}

sub print_rem {
print <<'DOUBLEREMDONE';
.text    
.align 2    
.globl __d_rem
.ent __d_rem
__d_rem:
    .set noreorder
    .cpload $25
    .set reorder
    .frame sp, 48, $31
    subu sp, 48
    sw   ra, 44(sp)
    sw   a0, 0(sp)
    sw   a1, 4(sp)
    ldc1 $f0, 0(sp)
    mov.d $f12, $f0
    sw   a2, 8(sp)
    sw   a3, 12(sp)
    ldc1 $f0, 8(sp)
    mov.d $f14, $f0
    jal  d_rem
    sdc1 $f0, 0(sp)
    lw   v0, 0(sp)
    lw   v1, 4(sp)
    lw   ra, 44(sp)
    addu sp, 48
    j    ra
.end __d_rem
DOUBLEREMDONE
print <<'FLOATREMDONE';
.text    
.align 2    
.globl __f_rem
.ent __f_rem
__f_rem:
    .set noreorder
    .cpload $25
    .set reorder
    .frame sp, 48, $31
    subu sp, 48
    sw   ra, 44(sp)
    sw   a0, 0(sp)
    sw   a1, 4(sp)
    lwc1 $f12, 0(sp)
    lwc1 $f14, 4(sp)
    jal  f_rem
    swc1 $f0, 0(sp)
    lw   v0, 0(sp)
    lw   v1, 4(sp)
    lw   ra, 44(sp)
    addu sp, 48
    j    ra
.end __f_rem
FLOATREMDONE

}


################################################################
#  Jacket routines for software floating point.
#   The compiler passes everything in registers.  Our C functions
#   expect pointers, so we put args on the stack and then call func

sub Call_SoftFp {
  # $name is a string, 
  # $proto is a prototype for the called function v = void, f = float,
  #    d = double, of = output float, od = output double
  # $negate is a Boolean
  my($name, $proto, $negate, $prefix_code) = @_;
  print '.set noreorder', "\n";
  print '.cpload $25', "\n";
  print '.set reorder', "\n";
  print '.frame sp, 48, $31', "\n";
  print 'subu   sp, 48', "\n";
  print 'sw     $31, 44(sp)', "\n";

  if($prefix_code) {
    print "$prefix_code";
  }

  my $reg    = 4;
  my $preg   = 4;  # parameter register
  my $offset = 24;
  my $ret_lo = 0;
  my $ret_hi = 0;
  foreach $pro (@$proto) {
    if($pro eq "f") {
      print 'sw     $', $reg,  ', ', $offset, "(sp)\n";
      print 'la     $', $preg, ', ', $offset, "(sp)\n";
      $reg++;
      $preg++;
      $offset += 4;
    } elsif($pro eq "d") {
      print 'sw     $', $reg,  ', ', $offset, "(sp)\n";
      print 'la     $', $preg, ', ', $offset, "(sp)\n";
      $reg++;
      $offset += 4;
      $preg++;
      print 'sw     $', $reg, ', ', $offset, "(sp)\n";
      $reg++;
      $offset += 4;
    } elsif($pro eq "of" ) {
      print 'la     $', $preg, ", 16(sp)\n";
      $ret_lo = 1;
    } elsif($pro eq "od" ) {
      print 'la     $', $preg, ", 16(sp)\n";
      $ret_lo = 1;
      $ret_hi = 1;
    }
  }
  if($offset > 44) { die "SoftFp stack space is insufficient\n"; }
  # '  lousy quote analysis in emacs perl mode
  print "jal    $name\n";
  if($ret_lo) {
    print "lw     v0, 16(sp) \n";
    if($ret_hi) {
      print "lw     v1, 20(sp) \n";
    }
  }
  if($negate) {
    print "seq    v0, v0, zero\n";
  }
print<<'ENDEPILOG'
lw     $31, 44(sp)
addu   sp, 48
j      $31
ENDEPILOG
}

sub Print_Asm_Jacket {
  my ($label, $emulation_function, $load_return, $negate, $prefix_code) 
    = @_;
print "
.globl $label
.ent $label
$label:
";
  Call_SoftFp($emulation_function, $load_return, $negate, $prefix_code);
  print ".end $label\n\n";
}

################################################################
## Print the Functions
## First include the defines for a0, t8, etc.
print "#include <regdef.h>\n";
## XXX This are wrong, try shifting by 32!
# print_ll_shifts();
################################################################
## More long long support
Print_Asm_Jacket("__ll_lshift", "ll_lshift", [d,f,od], 0);
Print_Asm_Jacket("__ll_rshift", "ll_rshift", [d,f,od], 0);
Print_Asm_Jacket("__ull_rshift", "ull_rshift", [d,f,od], 0);
Print_Asm_Jacket("__ll_div", "ll_div", [d,d,od], 0);
Print_Asm_Jacket("__ll_mul", "ll_mul", [d,d,od], 0);
Print_Asm_Jacket("__ll_rem", "ll_rem", [d,d,od], 0);

################################################################
## Arithmetic
Print_Asm_Jacket("__addsf3", "addSingle", [f,f,of], 0);
Print_Asm_Jacket("__subsf3", "subSingle", [f,f,of], 0);
Print_Asm_Jacket("__mulsf3", "mulSingle", [f,f,of], 0);
Print_Asm_Jacket("__divsf3", "divSingle", [f,f,of], 0);
Print_Asm_Jacket("__adddf3", "addDouble", [d,d,od], 0);
Print_Asm_Jacket("__subdf3", "subDouble", [d,d,od], 0);
Print_Asm_Jacket("__muldf3", "mulDouble", [d,d,od], 0);
Print_Asm_Jacket("__divdf3", "divDouble", [d,d,od], 0);

################################################################
## Comparisons
Print_Asm_Jacket("__f_seq", "eqSingle", [f,f], 0);
Print_Asm_Jacket("__f_sne", "eqSingle", [f,f], 1);
Print_Asm_Jacket("__f_sgt", "leSingle", [f,f], 1);
Print_Asm_Jacket("__f_sle", "leSingle", [f,f], 0);
Print_Asm_Jacket("__f_sge", "ltSingle", [f,f], 1);
Print_Asm_Jacket("__f_slt", "ltSingle", [f,f], 0);

Print_Asm_Jacket("__d_seq", "eqDouble", [d,d], 0);
Print_Asm_Jacket("__d_sne", "eqDouble", [d,d], 1);
Print_Asm_Jacket("__d_sgt", "leDouble", [d,d], 1);
Print_Asm_Jacket("__d_sle", "leDouble", [d,d], 0);
Print_Asm_Jacket("__d_sge", "ltDouble", [d,d], 1);
Print_Asm_Jacket("__d_slt", "ltDouble", [d,d], 0);

################################################################
## Negation
$float_prefix = "
move  a1, a0
.data
1: .float 0.0
.text
lw  a0, 1b
";
Print_Asm_Jacket("__f_neg", "subSingle", [f,f,of], 0, $float_prefix);

$double_prefix ="
move  a2, a0
move  a3, a1
.data
1: .double 0.0
.text
lw  a0, 1b
lw  a1, 1b + 4
";
Print_Asm_Jacket("__d_neg", "subDouble", [d,d,od], 0, $double_prefix);

################################################################
## Casts
Print_Asm_Jacket("__i2f", "wordToSingle", [f,of], 0);
Print_Asm_Jacket("__f2i", "singleToWord", [f,of], 0);
Print_Asm_Jacket("__i2d", "wordToDouble", [f,od], 0);
Print_Asm_Jacket("__l2f", "dwordToSingle", [d,of], 0);
Print_Asm_Jacket("__l2d", "dwordToDouble", [d,od], 0);
Print_Asm_Jacket("__d2l", "doubleToDWord", [d,od], 0);
Print_Asm_Jacket("__d2i", "doubleToWord", [d,of], 0);
Print_Asm_Jacket("__f2d", "singleToDouble", [f,od], 0);
Print_Asm_Jacket("__d2f", "doubleToSingle", [d,of], 0);
print_rem();
