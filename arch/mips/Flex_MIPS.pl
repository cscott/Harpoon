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
    .globl _ll_lshift
    .ent _ll_lshift
_ll_lshift:
    .set noreorder
    .cpload $25
    .set reorder
    .frame $sp, 0, $31
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
.end _ll_lshift

    /* Logical  right shift */
    /* V1:  V0 = A1:A0 >>> (A2 mod 64) */
.globl _lll_rshift
.ent _lll_rshift
_lll_rshift:
    .set noreorder
    .cpload $25
    .set reorder
    .frame $sp, 0, $31
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
.end _lll_rshift
        
    /* Arithmetic right shift */
    /* V1:  V0 = A1:A0 >> (A2 mod 64) */
.globl _lla_rshift
.ent _lla_rshift
_lla_rshift:
    .set noreorder
    .cpload $25
    .set reorder
    .frame $sp, 0, $31
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
.end _lla_rshift

    /* V1:V0 = A1:A0 * A3:A2  12 inst, 42 cycles */
.globl _ll_mul
.ent _ll_mul
_ll_mul:    
    .set noreorder
    .cpload $25
    .set reorder
    .frame $sp, 0, $31
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
.end _ll_mul
LLSHIFTSDONE
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
  my($name, $proto, $negate) = @_;
  print '.set noreorder', "\n";
  print '.cpload $25', "\n";
  print '.set reorder', "\n";
  print '.frame $sp, 0, $31', "\n";
  print 'subu   $sp, 32', "\n";
  print 'sw     $31, 24($sp)', "\n";

  my $reg    = 4;
  my $preg   = 4;
  my $offset = 8;
  my $ret_lo = 0;
  my $ret_hi = 0;
  foreach $pro (@$proto) {
    if($pro eq "f") {
      print 'sw     $', $reg, ', ', $offset, "(sp)\n";
      print 'la     $', $reg, ', ', $offset, "(sp)\n";
      $reg++;
      $preg++;
      $offset += 4;
    } elsif($pro eq "d") {
      print 'sw     $', $reg, ', ', $offset, "(sp)\n";
      print 'la     $', $reg, ', ', $offset, "(sp)\n";
      $reg++;
      $offset += 4;
      $preg++;
      print 'sw     $', $reg, ', ', $offset, "(sp)\n";
      $reg++;
      $offset += 4;
    } elsif($pro eq "of" ) {
      print 'la     $', $preg, ", 0(sp)\n";
      $ret_lo = 1;
    } elsif($pro eq "od" ) {
      print 'la     $', $preg, ", 0(sp)\n";
      $ret_lo = 1;
      $ret_hi = 1;
    }
  }
  # '  lousy quote analysis in emacs perl mode
  print "jal    $name\n";
  if($ret_lo) {
    print "lw     v0, 0(sp) \n";
    if($ret_hi) {
      print "lw     v1, 4(sp) \n";
    }
  }
  if($negate) {
    print "not    v0, v0 \n";
  }
print<<'ENDEPILOG'
lw     $31, 24($sp)
addu   $sp, 32
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
  if($prefix_code) {
    print "$prefix_code";
  }
  Call_SoftFp($emulation_function, $load_return, $negate);
  print ".end $label\n\n";
}

################################################################
## Print the Functions
## First include the defines for a0, t8, etc.
print "#include <regdef.h>\n";
print_ll_shifts();

################################################################
## Arithmetic
Print_Asm_Jacket("_addsf3", "addSingle", [f,f,of], 0);
Print_Asm_Jacket("_subsf3", "subSingle", [f,f,of], 0);
Print_Asm_Jacket("_mulsf3", "mulSingle", [f,f,of], 0);
Print_Asm_Jacket("_divsf3", "divSingle", [f,f,of], 0);
Print_Asm_Jacket("_adddf3", "addDouble", [d,d,od], 0);
Print_Asm_Jacket("_subdf3", "subDouble", [d,d,od], 0);
Print_Asm_Jacket("_muldf3", "mulDouble", [d,d,od], 0);
Print_Asm_Jacket("_divdf3", "divDouble", [d,d,od], 0);

################################################################
## Comparisons
Print_Asm_Jacket("_f_seq", "eqSingle", [f,f], 0);
Print_Asm_Jacket("_f_sne", "eqSingle", [f,f], 1);
Print_Asm_Jacket("_f_sgt", "gtSingle", [f,f], 0);
Print_Asm_Jacket("_f_sle", "gtSingle", [f,f], 1);
Print_Asm_Jacket("_f_sge", "geSingle", [f,f], 0);
Print_Asm_Jacket("_f_slt", "geSingle", [f,f], 1);

Print_Asm_Jacket("_d_seq", "eqDouble", [d,d], 0);
Print_Asm_Jacket("_d_sne", "eqDouble", [d,d], 1);
Print_Asm_Jacket("_d_sgt", "gtDouble", [d,d], 0);
Print_Asm_Jacket("_d_sle", "gtDouble", [d,d], 1);
Print_Asm_Jacket("_d_sge", "geDouble", [d,d], 0);
Print_Asm_Jacket("_d_slt", "geDouble", [d,d], 1);

################################################################
## Negation
$float_prefix =
"   move  a1, a0
    li    a0, 0
";
Print_Asm_Jacket("_f_neg", "subSingle", [f,f,of], 0, $float_prefix);

$double_prefix =
"   move  a2, a0
    move  a3, a1
    li    a0, 0
    li    a1, 0
";
Print_Asm_Jacket("_d_neg", "subDouble", [d,d,od], 0, $float_prefix);

################################################################
## Casts
Print_Asm_Jacket("_i2f", "wordToSingle", [f,of], 0);
Print_Asm_Jacket("_f2i", "singleToWord", [f,of], 0);
Print_Asm_Jacket("_i2d", "wordToDouble", [f,od], 0);
Print_Asm_Jacket("_d2i", "doubleToWord", [d,of], 0);
Print_Asm_Jacket("_f2d", "singleToDouble", [f,od], 0);
Print_Asm_Jacket("_d2f", "doubleToSingle", [d,of], 0);

