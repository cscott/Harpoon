/*
** $Header: /home/cananian/git-conversion/cvs/Runtime/arch/mips/floatem.h,v 1.2 2003-05-21 20:46:27 wbeebee Exp $
*/
/*
Each of the following typedef's defines the most convenient type that holds
integers of at least as many bits as specified.  For example, `uint8' should
be the most convenient type that can hold unsigned integers of as many
as 8 bits.  The `flag' type must be able hold either a 0 or 1.  For any
reasonable implementation of C, `flag', `uint8', and `int8' should all be
typedef'd to the same as `int'.
*/
typedef int flag;
typedef int uint8;
typedef int int8;
typedef int uint16;
typedef int int16;
typedef unsigned int uint32;
typedef signed int int32;

/*
Each of the following typedef's defines a type that holds integers of exactly
the number of bits specified.  For instance, for most implementation of C,
`bits16' and `sbits16' should be typedef'd to `unsigned short' and `signed
short' (or `short'), respectively.
*/
typedef unsigned short bits16;
typedef signed short sbits16;
typedef unsigned int bits32;
typedef signed int sbits32;

int8 exceptionFlags;

#define FLOAT_INEXACT    1
#define FLOAT_UNDERFLOW  2
#define FLOAT_OVERFLOW   4
#define FLOAT_DIVBYZERO  8
#define FLOAT_INVALID   16

/* eW MIPS convention for long longs */
#define BIGENDIAN

void wordToSingle( sbits32 *, bits32 * );
void singleToWord( bits32 *, sbits32 * );
void roundSingleToWord( bits32 *, sbits32 * );
void wordToDouble( sbits32 *, bits32 * );
void doubleToWord( bits32 *, sbits32 * );
void roundDoubleToWord( bits32 *, sbits32 * );
void singleToDouble( bits32 *, bits32 * );
void doubleToSingle( bits32 *, bits32 * );

void addSingle( bits32 *, bits32 *, bits32 * );
void subSingle( bits32 *, bits32 *, bits32 * );
void mulSingle( bits32 *, bits32 *, bits32 * );
void divSingle( bits32 *, bits32 *, bits32 * );
void sqrtSingle( bits32 *, bits32 * );
flag eqSingle( bits32 *, bits32 * );
flag leSingle( bits32 *, bits32 * );
flag ltSingle( bits32 *, bits32 * );
flag awareEqSingle( bits32 *, bits32 * );
flag awareLeSingle( bits32 *, bits32 * );
flag awareLtSingle( bits32 *, bits32 * );
flag isNaNSingle( bits32 *, flag );

void addDouble( bits32 *, bits32 *, bits32 * );
void subDouble( bits32 *, bits32 *, bits32 * );
void mulDouble( bits32 *, bits32 *, bits32 * );
void divDouble( bits32 *, bits32 *, bits32 * );
void sqrtDouble( bits32 *, bits32 * );
flag eqDouble( bits32 *, bits32 * );
flag leDouble( bits32 *, bits32 * );
flag ltDouble( bits32 *, bits32 * );
flag awareEqDouble( bits32 *, bits32 * );
flag awareLeDouble( bits32 *, bits32 * );
flag awareLtDouble( bits32 *, bits32 * );
flag isNaNDouble( bits32 *, flag );

