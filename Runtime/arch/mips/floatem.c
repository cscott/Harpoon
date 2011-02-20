#ifndef NO_RCSID
const char* floatem_rcsid = "$Header: /home/cananian/git-conversion/cvs/Runtime/arch/mips/floatem.c,v 1.3 2003-05-21 20:46:27 wbeebee Exp $";
#endif
/*

This code is based on the IEEE Floating-point Emulation Package written by
John R. Hauser, specialized slightly for use with SPERT.

The basic IEEE Floating-point Emulation Package was made possible by the
International Computer Science Institute, Suite 600, 1947 Center Street,
Berkeley, California 94704, with funding in part from the National Science
Foundation under grant MIP-9311980.  It was written as part of a project to
build a fixed-point vector processor in collaboration with the University of
California at Berkeley, overseen by Profs. Nelson Morgan and John Wawrzynek.
Contact John Hauser at `jhauser@cs.berkeley.edu' for more information.

*/

#include "floatem.h"

#define DEFAULTSINGLENAN 0x7FBFFFFF
#define DEFAULTDOUBLENANHIGH 0x7FF7FFFF
#define DEFAULTDOUBLENANLOW 0xFFFFFFFF

#define TRACE(x) /*x*/

static  void raiseInexact( void )
{

    exceptionFlags |= FLOAT_INEXACT;

}

static  void raiseUnderflow( void )
{

    exceptionFlags |= FLOAT_UNDERFLOW;

}

static  void raiseOverflow( void )
{

    exceptionFlags |= FLOAT_OVERFLOW;

}

static  void raiseDivideByZero( void )
{

    exceptionFlags |= FLOAT_DIVBYZERO;

}

static  void raiseInvalid( void )
{

    exceptionFlags |= FLOAT_INVALID;

}

static  void shiftDown32Jamming( bits32 a, uint16 count, bits32 *zPtr )
{
    bits32 z;

    if ( count == 0 ) {
        z = a;
    }
    else if ( count < 32 ) {
        z = ( a>>count ) | ( 0 < ( a<<( -count & 31 ) ) );
    }
    else {
        z = ( 0 < a );
    }
    *zPtr = z;

}

static  void
 shiftDown64ExtraJamming( bits32 a0, bits32 a1, bits32 a2, uint16 count,
                          bits32 *z0Ptr, bits32 *z1Ptr, bits32 *z2Ptr )
{
    bits32 z0, z1, z2;
    int8 negCount = -count & 31;

    if ( count == 0 ) {
        z2 = a2;
        z1 = a1;
        z0 = a0;
    }
    else {
        if ( count < 32 ) {
            z2 = a1<<negCount;
            z1 = ( a0<<negCount ) | ( a1>>count );
            z0 = a0>>count;
        }
        else {
            if ( count == 32 ) {
                z2 = a1;
                z1 = a0;
            }
            else {
                a2 |= a1;
                if ( count < 64 ) {
                    z2 = a0<<negCount;
                    z1 = a0>>( count & 31 );
                }
                else {
                    z2 = ( count == 64 ) ? a0 : ( 0 < a0 );
                    z1 = 0;
                }
            }
            z0 = 0;
        }
        z2 |= ( 0 < a2 );
    }
    *z2Ptr = z2;
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static  void
 shiftDown64Jamming(
     bits32 a0, bits32 a1, uint16 count, bits32 *z0Ptr, bits32 *z1Ptr )
{
    bits32 z0, z1;
    int8 negCount = -count & 31;

    if ( count == 0 ) {
        z1 = a1;
        z0 = a0;
    }
    else if ( count < 32 ) {
        z1 = ( a0<<negCount ) | ( a1>>count ) | ( 0 < ( a1<<negCount ) );
        z0 = a0>>count;
    }
    else {
        if ( count == 32 ) {
            z1 = a0 | ( 0 < a1 );
        }
        else if ( count < 64 ) {
            z1 = ( a0>>( count & 31 ) ) | ( 0 < ( ( a0<<negCount ) | a1 ) );
        }
        else {
            z1 = ( 0 < ( a0 | a1 ) );
        }
        z0 = 0;
    }
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static  void
 shiftDown64(
     bits32 a0, bits32 a1, uint16 count, bits32 *z0Ptr, bits32 *z1Ptr )
{
    bits32 z0, z1;
    int8 negCount = -count & 31;

    if ( count == 0 ) {
        z1 = a1;
        z0 = a0;
    }
    else if ( count < 32 ) {
        z1 = ( a0<<negCount ) | ( a1>>count );
        z0 = a0>>count;
    }
    else {
        z1 = ( count < 64 ) ? ( a0>>( count & 31 ) ) : 0;
        z0 = 0;
    }
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static  void
 shortShiftUp64(
     bits32 a0, bits32 a1, uint16 count, bits32 *z0Ptr, bits32 *z1Ptr )
{

    *z1Ptr = a1<<count;
    *z0Ptr = ( count == 0 ) ? a0 : ( a0<<count ) | ( a1>>( -count & 31 ) );

}

static  void
 shortShiftUp96( bits32 a0, bits32 a1, bits32 a2, uint16 count,
                 bits32 *z0Ptr, bits32 *z1Ptr, bits32 *z2Ptr )
{
    bits32 z0, z1, z2;
    int8 negCount;

    z2 = a2<<count;
    z1 = a1<<count;
    z0 = a0<<count;
    if ( 0 < count ) {
        negCount = ( -count & 31 );
        z1 |= a2>>negCount;
        z0 |= a1>>negCount;
    }
    *z2Ptr = z2;
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static  void
 add64(
     bits32 a0, bits32 a1, bits32 b0, bits32 b1, bits32 *z0Ptr, bits32 *z1Ptr )
{
    bits32 z1;

    z1 = a1 + b1;
    *z1Ptr = z1;
    *z0Ptr = a0 + b0 + ( z1 < a1 );

}

static  void
 add96( bits32 a0, bits32 a1, bits32 a2, bits32 b0, bits32 b1, bits32 b2,
        bits32 *z0Ptr, bits32 *z1Ptr, bits32 *z2Ptr )
{
    bits32 z0, z1, z2;
    int8 carry0, carry1;

    z2 = a2 + b2;
    carry1 = ( z2 < a2 );
    z1 = a1 + b1;
    carry0 = ( z1 < a1 );
    z0 = a0 + b0;
    z1 += carry1;
    z0 += ( z1 < carry1 );
    z0 += carry0;
    *z2Ptr = z2;
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static  void
 sub64(
     bits32 a0, bits32 a1, bits32 b0, bits32 b1, bits32 *z0Ptr, bits32 *z1Ptr )
{

    *z1Ptr = a1 - b1;
    *z0Ptr = a0 - b0 - ( a1 < b1 );

}

static  void
 sub96( bits32 a0, bits32 a1, bits32 a2, bits32 b0, bits32 b1, bits32 b2,
        bits32 *z0Ptr, bits32 *z1Ptr, bits32 *z2Ptr )
{
    bits32 z0, z1, z2;
    int8 borrow0, borrow1;

    z2 = a2 - b2;
    borrow1 = ( a2 < b2 );
    z1 = a1 - b1;
    borrow0 = ( a1 < b1 );
    z0 = a0 - b0;
    z0 -= ( z1 < borrow1 );
    z1 -= borrow1;
    z0 -= borrow0;
    *z2Ptr = z2;
    *z1Ptr = z1;
    *z0Ptr = z0;

}

#if (SIZEOF_LONG_LONG==8)

typedef unsigned long long bits64;

static  void
 mul32To64( bits32 a, bits32 b, bits32 *z0Ptr, bits32 *z1Ptr )
{
    bits64 z;

    z = ( (bits64) a ) * ( (bits64) b );
    *z1Ptr = z;
    *z0Ptr = z>>32;

}

#else

static  void
 mul32To64( bits32 a, bits32 b, bits32 *z0Ptr, bits32 *z1Ptr )
{
    bits16 aHigh, aLow, bHigh, bLow;
    bits32 z0, zMiddle, zMiddleOther, z1;

    aLow = ( a & 0xFFFF );
    aHigh = a>>16;
    bLow = ( b & 0xFFFF );
    bHigh = b>>16;
    z1 = aLow * bLow;
    zMiddle = aLow * bHigh;
    zMiddleOther = aHigh * bLow;
    z0 = aHigh * bHigh;
    zMiddle += zMiddleOther;
    z0 += ( ( zMiddle < zMiddleOther )<<16 ) + ( zMiddle>>16 );
    zMiddle = zMiddle<<16;
    z1 += zMiddle;
    z0 += ( z1 < zMiddle );
    *z1Ptr = z1;
    *z0Ptr = z0;

}

#endif

static  void
 mul64By32To96( bits32 a0, bits32 a1, bits32 b,
                bits32 *z0Ptr, bits32 *z1Ptr, bits32 *z2Ptr )
{
    bits32 z0, z1, z2, more1;

    mul32To64( a1, b, &z1, &z2 );
    mul32To64( a0, b, &z0, &more1 );
    add64( z0, more1, 0, z1, &z0, &z1 );
    *z2Ptr = z2;
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static  void
 mul64To128( bits32 a0, bits32 a1, bits32 b0, bits32 b1,
             bits32 *z0Ptr, bits32 *z1Ptr, bits32 *z2Ptr, bits32 *z3Ptr )
{
    bits32 z0, z1, z2, z3;
    bits32 more1, more2;

    mul32To64( a1, b1, &z2, &z3 );
    mul32To64( a1, b0, &z1, &more2 );
    add64( z1, more2, 0, z2, &z1, &z2 );
    mul32To64( a0, b0, &z0, &more1 );
    add64( z0, more1, 0, z1, &z0, &z1 );
    mul32To64( a0, b1, &more1, &more2 );
    add64( more1, more2, 0, z2, &more1, &z2 );
    add64( z0, z1, 0, more1, &z0, &z1 );
    *z3Ptr = z3;
    *z2Ptr = z2;
    *z1Ptr = z1;
    *z0Ptr = z0;

}

static bits32 estimateDiv64To32( bits32 a0, bits32 a1, bits32 b )
{
    bits32 z;
    bits32 rem0, rem1;
    bits32 term0, term1;
    bits32 b0, b1;

    if ( b <= a0 ) return 0xFFFFFFFF;
    b0 = b>>16;
    z = ( b0<<16 <= a0 ) ? 0xFFFF0000 : ( a0 / b0 )<<16;
    mul32To64( b, z, &term0, &term1 );
    sub64( a0, a1, term0, term1, &rem0, &rem1 );
    while ( ( (sbits32) rem0 ) < 0 ) {
        z -= 0x10000;
        b1 = b<<16;
        add64( rem0, rem1, b0, b1, &rem0, &rem1 );
    }
    rem0 = ( rem0<<16 ) | ( rem1>>16 );
    z |= ( b0<<16 <= rem0 ) ? 0xFFFF : rem0 / b0;
    return z;

}

static bits16 sqrtOddAdjustments[ ] = {
    0x0004, 0x0022, 0x005D, 0x00B1, 0x011D, 0x019F, 0x0236, 0x02E0,
    0x039C, 0x0468, 0x0545, 0x0631, 0x072B, 0x0832, 0x0946, 0x0A67
};

static bits16 sqrtEvenAdjustments[ ] = {
    0x0A2D, 0x08AF, 0x075A, 0x0629, 0x051A, 0x0429, 0x0356, 0x029E,
    0x0200, 0x0179, 0x0109, 0x00AF, 0x0068, 0x0034, 0x0012, 0x0002
};

static bits32 estimateSqrt32( int16 aExp, bits32 a )
{
    int8 index = ( a>>27 ) & 15;
    bits32 z;

    if ( ( aExp & 1 ) != 0 ) {
        z = 0x4000 + ( a>>17 ) - sqrtOddAdjustments[ index ];
        z = ( ( a / z )<<14 ) + ( z<<15 );
        a = a>>1;
    }
    else {
        z = 0x8000 + ( a>>17 ) - sqrtEvenAdjustments[ index ];
        z = ( ( a / z ) + z );
        z = ( 0x20000 <= z ) ? 0xFFFF8000 : ( z<<15 );
        if ( z <= a ) return (bits32) ( ( (sbits32) a )>>1 );
    }
    return ( ( estimateDiv64To32( a, 0, z ) )>>1 ) + ( z>>1 );

}

static int8 countLeadingZerosHigh[] = {
    8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
    3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};

static int8 countLeadingZeros( bits32 a )
{
    int8 shiftCount;

    shiftCount = 0;
    if ( a < 0x10000 ) {
        shiftCount += 16;
        a = a<<16;
    }
    if ( a < 0x1000000 ) {
        shiftCount += 8;
        a = a<<8;
    }
    shiftCount += countLeadingZerosHigh[ a>>24 ];
    return shiftCount;

}

static  flag eq64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
{

    return ( a0 == b0 ) && ( a1 == b1 );

}

static  flag le64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
{

    return ( a0 < b0 ) || ( ( a0 == b0 ) && ( a1 <= b1 ) );

}

static  flag lt64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
{

    return ( a0 < b0 ) || ( ( a0 == b0 ) && ( a1 < b1 ) );

}

static  flag ne64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
{

    return ( a0 != b0 ) || ( a1 != b1 );

}

static  sbits32 loadWord( sbits32 *aPtr )
{

    return *aPtr;

}

static  void storeWord( sbits32 z, sbits32 *zPtr )
{

    *zPtr = z;

}

static  bits32 loadSingle( bits32 *aPtr )
{

    return *aPtr;

}

static  bits32 extractSingleSig( bits32 a )
{

    return a & 0x7FFFFF;

}

static  int16 extractSingleExp( bits32 a )
{

    return ( a>>23 ) & 0xFF;

}

static  flag extractSingleSign( bits32 a )
{

    return a>>31;

}

static void
 normalizeSingleSubnormal( bits32 aSig, int16 *zExpPtr, bits32 *zSigPtr )
{
    int8 shiftCount;

    shiftCount = countLeadingZeros( aSig ) - 8;
    *zSigPtr = aSig<<shiftCount;
    *zExpPtr = 1 - shiftCount;

}

static  flag bothZeroSingle( bits32 a, bits32 b )
{

    return ( ( ( a | b )<<1 ) == 0 );

}

static  void storeWholeSingle( bits32 z, bits32 *zPtr )
{

    *zPtr = z;

}

static  void
 storeSingle( flag zSign, int16 zExp, bits32 zSig, bits32 *zPtr )
{

    storeWholeSingle( ( zSign<<31 ) + ( zExp<<23 ) + zSig, zPtr );

}

static void
 roundAndStoreSingle( flag zSign, int16 zExp, bits32 zSig, bits32 *zPtr )
{
    int8 roundBits = zSig & 0x7F;

    if ( 0xFE - 1 <= ( (bits16) zExp ) ) {
        if (    ( 0xFE - 1 < zExp )
             || (    ( zExp == 0xFE - 1 )
                  && ( ( (sbits32) ( zSig + 0x40 ) ) < 0 ) )
           ) {
            raiseOverflow();
            raiseInexact();
            storeSingle( zSign, 0xFF, 0, zPtr );
            return;
        }
        if ( zExp < 0 ) {
            shiftDown32Jamming( zSig, -zExp, &zSig );
            zExp = 0;
            roundBits = zSig & 0x7F;
            if ( ( roundBits != 0 ) && ( zSig < 0x3FFFFFC0 ) ) {
                raiseUnderflow();
            }
        }
    }
    if ( roundBits != 0 ) raiseInexact();
    zSig += 0x40;
    zSig = zSig>>7;
    if ( ( roundBits ^ 0x40 ) == 0 ) zSig &= ~1;
    if ( zSig == 0 ) zExp = 0;
    storeSingle( zSign, zExp, zSig, zPtr );

}

static void
 normalizeRoundAndStoreSingle(
     flag zSign, int16 zExp, bits32 zSig, bits32 *zPtr )
{
    int8 shiftCount;

    shiftCount = countLeadingZeros( zSig ) - 1;
    zSig = zSig<<shiftCount;
    zExp -= shiftCount;
    roundAndStoreSingle( zSign, zExp, zSig, zPtr );

}

static  bits32 loadDouble1( bits32 *aPtr )
{

#ifdef BIGENDIAN
    return *( aPtr + 1 );
#else
    return *aPtr;
#endif

}

static  bits32 loadDouble0( bits32 *aPtr )
{

#ifdef BIGENDIAN
    return *aPtr;
#else
    return *( aPtr + 1 );
#endif

}

static  bits32 extractDoubleSig0( bits32 a0 )
{

    return a0 & 0xFFFFF;

}

static  int16 extractDoubleExp( bits32 a0 )
{

    return ( a0>>20 ) & 0x7FF;

}

static  flag extractDoubleSign( bits32 a0 )
{

    return a0>>31;

}

static void
 normalizeDoubleSubnormal(
     bits32 aSig0, bits32 aSig1,
     int16 *zExpPtr, bits32 *zSig0Ptr, bits32 *zSig1Ptr
 )
{
    int8 shiftCount;

    if ( aSig0 == 0 ) {
        shiftCount = countLeadingZeros( aSig1 ) - 11;
        if ( shiftCount < 0 ) {
            *zSig0Ptr = aSig1>>( - shiftCount );
            *zSig1Ptr = aSig1<<( shiftCount & 31 );
        }
        else {
            *zSig0Ptr = aSig1<<shiftCount;
            *zSig1Ptr = 0;
        }
        *zExpPtr = - shiftCount - 31;
    }
    else {
        shiftCount = countLeadingZeros( aSig0 ) - 11;
        shortShiftUp64( aSig0, aSig1, shiftCount, zSig0Ptr, zSig1Ptr );
        *zExpPtr = 1 - shiftCount;
    }

}

static  flag
 bothZeroDouble( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
{

    return ( ( ( ( a0 | b0 )<<1 ) | a1 | b1 ) == 0 );

}

static  void storeWholeDouble( bits32 z0, bits32 z1, bits32 *zPtr )
{

#ifdef BIGENDIAN
    *( zPtr + 1 ) = z1;
    *zPtr = z0;
#else
    *zPtr = z1;
    *( zPtr + 1 ) = z0;
#endif

}

static  void
 storeDouble( flag zSign, int16 zExp, bits32 zSig0, bits32 z1, bits32 *zPtr )
{

    storeWholeDouble( ( zSign<<31 ) + ( zExp<<20 ) + zSig0, z1, zPtr );

}

static void
 roundAndStoreDouble(
     flag zSign, int16 zExp, bits32 zSig0, bits32 zSig1, bits32 zSig2,
     bits32 *zPtr
 )
{

    if ( 0x7FE - 1 <= ( (bits16) zExp ) ) {
        if (    ( 0x7FE - 1 < zExp )
             || (    ( zExp == 0x7FE - 1 )
                  && eq64( 0x001FFFFF, 0xFFFFFFFF, zSig0, zSig1 )
                  && ( ( (sbits32) zSig2 ) < 0 )
                )
           ) {
            raiseOverflow();
            raiseInexact();
            storeDouble( zSign, 0x7FF, 0, 0, zPtr );
            return;
        }
        if ( zExp < 0 ) {
            shiftDown64ExtraJamming(
                zSig0, zSig1, zSig2, -zExp, &zSig0, &zSig1, &zSig2 );
            zExp = 0;
            if (    ( 0 < ( (sbits32) zSig2 ) )
                 || (    ( zSig2 != 0 )
                      && lt64( zSig0, zSig1, 0x000FFFFF, 0xFFFFFFFF ) )
               ) {
                raiseUnderflow();
            }
        }
    }
    if ( zSig2 != 0 ) raiseInexact();
    if ( ( (sbits32) zSig2 ) < 0 ) {
        add64( zSig0, zSig1, 0, 1, &zSig0, &zSig1 );
        if ( ( zSig2<<1 ) == 0 ) zSig1 &= ~1;
    }
    if ( ( zSig0 | zSig1 ) == 0 ) zExp = 0;
    storeDouble( zSign, zExp, zSig0, zSig1, zPtr );

}

static void
 normalizeRoundAndStoreDouble(
     flag zSign, int16 zExp, bits32 zSig0, bits32 zSig1, bits32 *zPtr )
{
    int8 shiftCount;
    bits32 zSig2;

    if ( zSig0 == 0 ) {
        zSig0 = zSig1;
        zSig1 = 0;
        zExp -= 32;
    }
    shiftCount = countLeadingZeros( zSig0 ) - 11;
    if ( 0 <= shiftCount ) {
        zSig2 = 0;
        shortShiftUp64( zSig0, zSig1, shiftCount, &zSig0, &zSig1 );
    }
    else {
        shiftDown64ExtraJamming(
            zSig0, zSig1, 0, -shiftCount, &zSig0, &zSig1, &zSig2 );
    }
    zExp -= shiftCount;
    roundAndStoreDouble( zSign, zExp, zSig0, zSig1, zSig2, zPtr );

}

void wordToSingle( sbits32 *aPtr, bits32 *zPtr )
{
    sbits32 a;
    flag zSign;

    a = loadWord( aPtr );
    if ( a == 0 ) {
        storeWholeSingle( 0, zPtr );
    }
    else {
        zSign = ( a < 0 );
        if ( zSign ) a = -a;
        normalizeRoundAndStoreSingle( zSign, 0x7F + 30 - 1, a, zPtr );
    }

}

void singleToWord( bits32 *aPtr, sbits32 *zPtr )
{
    bits32 a;
    flag aSign;
    int16 aExp;
    bits32 aSig;
    sbits32 z;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a ) - 0x7F - 31;
    aSign = extractSingleSign( a );
    if ( 30 - 31 < aExp ) {
        raiseInvalid();
        if ( !aSign || ( ( aExp == 0xFF - 0x7F - 31 ) && ( aSig != 0 ) ) ) {
            z = 0x7FFFFFFF;
        }
        else {
            z = 0x80000000;
        }
    }
    else if ( aExp < -31 ) {
        z = 0;
    }
    else {
        z = ( ( 0x800000 | aSig )<<8 )>>( -aExp );
        if ( aSign ) z = -z;
    }
    storeWord( z, zPtr );

}

void roundSingleToWord( bits32 *aPtr, sbits32 *zPtr )
{
    bits32 a;
    flag aSign;
    int16 aExp;
    bits32 aSig;
    sbits32 z, zExtra;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a ) - 0x7F - 31;
    aSign = extractSingleSign( a );
    if ( 30 - 31 < aExp ) {
        raiseInvalid();
        if ( !aSign || ( ( aExp == 0xFF - 0x7F - 31 ) && ( aSig != 0 ) ) ) {
            z = 0x7FFFFFFF;
        }
        else {
            z = 0x80000000;
        }
    }
    else if ( aExp < -32 ) {
        z = 0;
    }
    else {
        aSig |= 0x800000;
        if ( -8 <= aExp ) {
            z = aSig<<( aExp + 8 );
        }
        else {
            aExp += 8;
            zExtra = aSig<<( aExp & 31 );
            z = aSig>>( -aExp );
            if ( ( (sbits32) zExtra ) < 0 ) {
                z++;
                if ( ( zExtra + zExtra ) == 0 ) z &= ~1;
            }
        }
        if ( aSign ) z = -z;
    }
    storeWord( z, zPtr );

}

void wordToDouble( sbits32 *aPtr, bits32 *zPtr )
{
    sbits32 a;
    flag zSign;
    int8 shiftCount;
    bits32 zSig0, zSig1;

    TRACE(printf("i2d %d ", *aPtr));
    a = loadWord( aPtr );
    if ( a == 0 ) {
        storeWholeDouble( 0, 0, zPtr );
    }
    else {
        zSign = ( a < 0 );
        if ( zSign ) a = -a;
        shiftCount = countLeadingZeros( a ) - 11;
        if ( 0 <= shiftCount ) {
            zSig0 = a<<shiftCount;
            zSig1 = 0;
        }
        else {
            shiftDown64( a, 0, -shiftCount, &zSig0, &zSig1 );
        }
        storeDouble( zSign, 0x3FF + 20 - 1 - shiftCount, zSig0, zSig1, zPtr );
    }
    TRACE(printf("%14g\n", *(double*)zPtr));
}

void doubleToWord( bits32 *aPtr, sbits32 *zPtr )
{
    bits32 a0;
    flag aSign;
    int16 aExp;
    bits32 aSig0, aSig1;
    sbits32 z;

    aSig1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 ) - 0x3FF - 31;
    aSign = extractDoubleSign( a0 );
    TRACE(printf("(%14g) Sgn %d, exp %d, sig0 %x sig1 %x ",*(double*)aPtr, aSign, aExp, aSig0, aSig1));
    if ( 30 - 31 < aExp ) {
        raiseInvalid();
        if (    !aSign
             || (    ( aExp == 0x7FF - 0x3FF - 31 )
                  && ( ( aSig0 | aSig1 ) != 0 ) )
           ) {
            z = 0x7FFFFFFF;
        }
        else {
            z = 0x80000000;
        }
    }
    else if ( aExp < -31 ) {
        z = 0;
    }
    else {
        aSig0 = ( aSig0 | 0x100000 )<<11 | ( aSig1>>( -11 & 31 ) );
        z = aSig0>>( -aExp );
        if ( aSign ) z = -z;
    }
    TRACE(printf("word=%x\n", z));
    storeWord( z, zPtr );

}

void roundDoubleToWord( bits32 *aPtr, sbits32 *zPtr )
{
    bits32 a0;
    flag aSign;
    int16 aExp;
    bits32 aSig0, aSig1;
    sbits32 z, zExtra;

    aSig1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 ) - 0x3FF - 31;
    aSign = extractDoubleSign( a0 );
    if ( 30 - 31 < aExp ) {
        raiseInvalid();
        if (    !aSign
             || (    ( aExp == 0x7FF - 0x3FF - 31 )
                  && ( ( aSig0 | aSig1 ) != 0 ) )
           ) {
            z = 0x7FFFFFFF;
        }
        else {
            z = 0x80000000;
        }
    }
    else if ( aExp < -32 ) {
        z = 0;
    }
    else {
        aSig0 |= 0x100000;
        if ( -11 <= aExp ) {
            shortShiftUp64( aSig0, aSig1, aExp + 11, (bits32*)&z, (bits32*)&zExtra );
        }
        else {
            aExp += 11;
            z = aSig0>>( -aExp );
            zExtra = ( aSig0<<( aExp & 31 ) ) | ( 0 < aSig1 );
        }
        if ( ( (sbits32) zExtra ) < 0 ) {
            z++;
            if ( ( zExtra + zExtra ) == 0 ) z &= ~1;
        }
        if ( aSign ) z = -z;
    }
    storeWord( z, zPtr );

}

void singleToDouble( bits32 *aPtr, bits32 *zPtr )
{
    bits32 a;
    flag aSign;
    int16 aExp, zExp;
    bits32 aSig;
    bits32 zSig0, zSig1;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    if ( aExp == 0 ) {
        if ( aSig == 0 ) {
            storeDouble( aSign, 0, 0, 0, zPtr );
            return;
        }
        normalizeSingleSubnormal( aSig, &aExp, &aSig );
        aExp--;
    }
    shiftDown64( aSig, 0, 3, &zSig0, &zSig1 );
    if ( aExp == 0xFF ) {
        zExp = 0x7FF;
    }
    else {
        zExp = aExp - 0x7F + 0x3FF;
    }
    storeDouble( aSign, zExp, zSig0, zSig1, zPtr );

}

void doubleToSingle( bits32 *aPtr, bits32 *zPtr )
{
    bits32 a0;
    flag aSign;
    int16 aExp;
    bits32 aSig0, aSig1, zSig;
    bits32 allZero;

    aSig1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 );
    aSign = extractDoubleSign( a0 );
    if ( aExp == 0 ) {
        if ( ( aSig0 | aSig1 ) != 0 ) raiseInexact();
        storeSingle( aSign, 0, 0, zPtr );
        return;
    }
    shiftDown64Jamming( aSig0, aSig1, 22, &allZero, &zSig );
    if ( aExp == 0x7FF ) {
        if ( ( zSig & 0x7F ) != 0 ) {
            zSig = 0x3FFFFF;
        }
        else {
            zSig = zSig>>7;
        }
        storeSingle( aSign, 0xFF, zSig, zPtr );
    }
    else {
        zSig |= 0x40000000;
        aExp += -0x3FF + 0x7F - 1;
        roundAndStoreSingle( aSign, aExp, zSig, zPtr );
    }

}

static void
 addSingleSigs( bits32 *aPtr, bits32 *bPtr, flag zSign, bits32 *zPtr )
{
    bits32 a, b;
    int16 aExp, bExp, zExp;
    bits32 aSig, bSig, zSig;
    int16 expDiff;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a );
    b = loadSingle( bPtr );
    bSig = extractSingleSig( b );
    bExp = extractSingleExp( b );
    aSig = aSig<<6;
    bSig = bSig<<6;
    expDiff = aExp - bExp;
    if ( 0 < expDiff ) {
        if ( aExp == 0xFF ) {
            storeWholeSingle( a, zPtr );
            return;
        }
        if ( bExp == 0 ) {
            expDiff--;
        }
        else {
            bSig |= 0x20000000;
        }
        shiftDown32Jamming( bSig, expDiff, &bSig );
        zExp = aExp;
    }
    else if ( expDiff < 0 ) {
        if ( bExp == 0xFF ) {
            if ( bSig != 0 ) {
                storeWholeSingle( b, zPtr );
            }
            else {
                storeSingle( zSign, 0xFF, 0, zPtr );
            }
            return;
        }
        if ( aExp == 0 ) {
            expDiff++;
        }
        else {
            aSig |= 0x20000000;
        }
        shiftDown32Jamming( aSig, -expDiff, &aSig );
        zExp = bExp;
    }
    else {
        if ( aExp == 0xFF ) {
            if ( ( aSig == 0 ) && ( bSig != 0 ) ) {
                storeWholeSingle( b, zPtr );
            }
            else {
                storeWholeSingle( a, zPtr );
            }
            return;
        }
        if ( aExp == 0 ) {
            storeSingle( zSign, 0, ( aSig + bSig )>>6, zPtr );
            return;
        }
        zSig = 0x40000000 + aSig + bSig;
        zExp = aExp;
        goto roundAndStore;
    }
    aSig |= 0x20000000;
    zSig = ( aSig + bSig )<<1;
    zExp--;
    if ( ( (sbits32) zSig ) < 0 ) {
        zSig = aSig + bSig;
        zExp++;
    }
roundAndStore:
    roundAndStoreSingle( zSign, zExp, zSig, zPtr );

}

static void
 subSingleSigs( bits32 *aPtr, bits32 *bPtr, flag zSign, bits32 *zPtr )
{
    bits32 a, b;
    int16 aExp, bExp, zExp;
    bits32 aSig, bSig, zSig;
    int16 expDiff;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a );
    b = loadSingle( bPtr );
    bSig = extractSingleSig( b );
    bExp = extractSingleExp( b );
    aSig = aSig<<7;
    bSig = bSig<<7;
    expDiff = aExp - bExp;
    if ( 0 < expDiff ) goto aExpBigger;
    if ( expDiff < 0 ) goto bExpBigger;
    if ( aExp == 0xFF ) {
        if ( aSig != 0 ) {
            storeWholeSingle( a, zPtr );
        }
        else if ( bSig != 0 ) {
            storeWholeSingle( b, zPtr );
        }
        else {
            raiseInvalid();
            storeWholeSingle( DEFAULTSINGLENAN, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        aExp = 1;
        bExp = 1;
    }
    if ( bSig < aSig ) goto aBigger;
    if ( aSig < bSig ) goto bBigger;
    storeWholeSingle( 0, zPtr );
    return;
bExpBigger:
    if ( bExp == 0xFF ) {
        if ( bSig != 0 ) {
            storeWholeSingle( b, zPtr );
        }
        else {
            storeSingle( zSign ^ 1, 0xFF, 0, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        expDiff++;
    }
    else {
        aSig |= 0x40000000;
    }
    shiftDown32Jamming( aSig, -expDiff, &aSig );
    bSig |= 0x40000000;
bBigger:
    zSig = bSig - aSig;
    zExp = bExp;
    zSign ^= 1;
    goto normalizeRoundAndStore;
aExpBigger:
    if ( aExp == 0xFF ) {
        storeWholeSingle( a, zPtr );
        return;
    }
    if ( bExp == 0 ) {
        expDiff--;
    }
    else {
        bSig |= 0x40000000;
    }
    shiftDown32Jamming( bSig, expDiff, &bSig );
    aSig |= 0x40000000;
aBigger:
    zSig = aSig - bSig;
    zExp = aExp;
normalizeRoundAndStore:
    zExp--;
    normalizeRoundAndStoreSingle( zSign, zExp, zSig, zPtr );

}

void addSingle( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    flag aSign, bSign;

    aSign = extractSingleSign( loadSingle( aPtr ) );
    bSign = extractSingleSign( loadSingle( bPtr ) );
    if ( aSign == bSign ) {
        addSingleSigs( aPtr, bPtr, aSign, zPtr );
    }
    else {
        subSingleSigs( aPtr, bPtr, aSign, zPtr );
    }

}

void subSingle( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    flag aSign, bSign;

    aSign = extractSingleSign( loadSingle( aPtr ) );
    bSign = extractSingleSign( loadSingle( bPtr ) );
    if ( aSign == bSign ) {
        subSingleSigs( aPtr, bPtr, aSign, zPtr );
    }
    else {
        addSingleSigs( aPtr, bPtr, aSign, zPtr );
    }

}

void mulSingle( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    bits32 a, b;
    flag aSign, bSign, zSign;
    int16 aExp, bExp, zExp;
    bits32 aSig, bSig, zSig0, zSig1;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    b = loadSingle( bPtr );
    bSig = extractSingleSig( b );
    bExp = extractSingleExp( b );
    bSign = extractSingleSign( b );
    zSign = aSign ^ bSign;
    if ( aExp == 0xFF ) {
        if ( aSig != 0 ) {
            storeWholeSingle( a, zPtr );
        }
        else if ( ( bExp == 0xFF ) && ( bSig != 0 ) ) {
            storeWholeSingle( b, zPtr );
        }
        else if ( ( bExp | bSig ) == 0 ) {
            raiseInvalid();
            storeWholeSingle( DEFAULTSINGLENAN, zPtr );
        }
        else {
            storeSingle( zSign, 0xFF, 0, zPtr );
        }
        return;
    }
    if ( bExp == 0xFF ) {
        if ( bSig != 0 ) {
            storeWholeSingle( b, zPtr );
        }
        else if ( ( aExp | aSig ) == 0 ) {
            raiseInvalid();
            storeWholeSingle( DEFAULTSINGLENAN, zPtr );
        }
        else {
            storeSingle( zSign, 0xFF, 0, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) {
            storeSingle( zSign, 0, 0, zPtr );
            return;
        }
        normalizeSingleSubnormal( aSig, &aExp, &aSig );
    }
    else {
        aSig |= 0x800000;
    }
    if ( bExp == 0 ) {
        if ( bSig == 0 ) {
            storeSingle( zSign, 0, 0, zPtr );
            return;
        }
        normalizeSingleSubnormal( bSig, &bExp, &bSig );
    }
    else {
        bSig |= 0x800000;
    }
    aSig = aSig<<7;
    bSig = bSig<<8;
    mul32To64( aSig, bSig, &zSig0, &zSig1 );
    zSig0 |= ( 0 < zSig1 );
    zExp = aExp + bExp - 0x7F;
    if ( 0 <= ( (sbits32) ( zSig0<<1 ) ) ) {
        zSig0 += zSig0;
        zExp--;
    }
    roundAndStoreSingle( zSign, zExp, zSig0, zPtr );

}

void divSingle( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    bits32 a, b;
    flag aSign, bSign, zSign;
    int16 aExp, bExp, zExp;
    bits32 aSig, bSig, zSig;
    bits32 rem0, rem1;
    bits32 term0, term1;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    b = loadSingle( bPtr );
    bSig = extractSingleSig( b );
    bExp = extractSingleExp( b );
    bSign = extractSingleSign( b );
    zSign = aSign ^ bSign;
    if ( aExp == 0xFF ) {
        if ( aSig != 0 ) {
            storeWholeSingle( a, zPtr );
        }
        else if ( bExp == 0xFF ) {
            if ( bSig != 0 ) {
                storeWholeSingle( b, zPtr );
            }
            else {
                raiseInvalid();
                storeWholeSingle( DEFAULTSINGLENAN, zPtr );
            }
        }
        else {
            storeSingle( zSign, 0xFF, 0, zPtr );
        }
        return;
    }
    if ( bExp == 0xFF ) {
        if ( bSig != 0 ) {
            storeWholeSingle( b, zPtr );
        }
        else {
            storeSingle( zSign, 0, 0, zPtr );
        }
        return;
    }
    if ( bExp == 0 ) {
        if ( bSig == 0 ) {
            if ( ( aExp | aSig ) == 0 ) {
                raiseInvalid();
                storeWholeSingle( DEFAULTSINGLENAN, zPtr );
            }
            else {
                raiseDivideByZero();
                storeSingle( zSign, 0xFF, 0, zPtr );
            }
            return;
        }
        normalizeSingleSubnormal( bSig, &bExp, &bSig );
    }
    else {
        bSig |= 0x800000;
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) {
            storeSingle( zSign, 0, 0, zPtr );
            return;
        }
        normalizeSingleSubnormal( aSig, &aExp, &aSig );
    }
    else {
        aSig |= 0x800000;
    }
    zExp = aExp - bExp + 0x7E - 1;
    aSig = aSig<<7;
    bSig = bSig<<8;
    if ( bSig <= ( aSig + aSig ) ) {
        aSig = aSig>>1;
        zExp++;
    }
    zSig = estimateDiv64To32( aSig, 0, bSig );
    if ( ( zSig & 0x3F ) <= 2 ) {
        mul32To64( bSig, zSig, &term0, &term1 );
        sub64( aSig, 0, term0, term1, &rem0, &rem1 );
        while ( ( (sbits32) rem0 ) < 0 ) {
            zSig--;
            add64( rem0, rem1, 0, bSig, &rem0, &rem1 );
        }
        zSig |= ( 0 < rem1 );
    }
    roundAndStoreSingle( zSign, zExp, zSig, zPtr );

}

void sqrtSingle( bits32 *aPtr, bits32 *zPtr )
{
    bits32 a;
    flag aSign;
    int16 aExp, zExp;
    bits32 aSig, zSig;
    bits32 rem0, rem1;
    bits32 term0, term1;

    a = loadSingle( aPtr );
    aSig = extractSingleSig( a );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    if ( aExp == 0xFF ) {
        if ( ( aSig != 0 ) || ( aSign == 0 ) ) {
            storeWholeSingle( a, zPtr );
        }
        else {
            raiseInvalid();
            storeWholeSingle( DEFAULTSINGLENAN, zPtr );
        }
        return;
    }
    if ( aSign ) {
        if ( ( aExp | aSig ) == 0 ) {
            storeWholeSingle( a, zPtr );
        }
        else {
            raiseInvalid();
            storeWholeSingle( DEFAULTSINGLENAN, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) {
            storeWholeSingle( 0, zPtr );
            return;
        }
        normalizeSingleSubnormal( aSig, &aExp, &aSig );
    }
    else {
        aSig |= 0x800000;
    }
    zExp = ( ( aExp - 0x7F )>>1 ) + 0x7F - 1;
    aSig = aSig<<8;
    zSig = estimateSqrt32( aExp, aSig );
    zSig = ( 0xFFFFFFFD < zSig ) ? 0xFFFFFFFF : zSig + 2;
    if ( ( zSig & 0x7F ) <= 5 ) {
        aSig = aSig>>( aExp & 1 );
        mul32To64( zSig, zSig, &term0, &term1 );
        sub64( aSig, 0, term0, term1, &rem0, &rem1 );
        while ( ( (sbits32) rem0 ) < 0 ) {
            zSig--;
            shortShiftUp64( 0, zSig, 1, &term0, &term1 );
            term1 |= 1;
            add64( rem0, rem1, term0, term1, &rem0, &rem1 );
        }
        zSig |= ( 0 < ( rem0 | rem1 ) );
    }
    shiftDown32Jamming( zSig, 1, &zSig );
    roundAndStoreSingle( 0, zExp, zSig, zPtr );

}

flag eqSingle( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a, b;
    int16 aExp, bExp;

    a = loadSingle( aPtr );
    aExp = extractSingleExp( a );
    b = loadSingle( bPtr );
    bExp = extractSingleExp( b );
    if (    ( ( aExp == 0xFF ) && ( extractSingleSig( a ) != 0 ) )
         || ( ( bExp == 0xFF ) && ( extractSingleSig( b ) != 0 ) ) ) {
        raiseInvalid();
        return 0;
    }
    return ( a == b ) || bothZeroSingle( a, b );

}

flag leSingle( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a, b;
    flag aSign, bSign;
    int16 aExp, bExp;

    a = loadSingle( aPtr );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    b = loadSingle( bPtr );
    bExp = extractSingleExp( b );
    bSign = extractSingleSign( b );
    if (    ( ( aExp == 0xFF ) && ( extractSingleSig( a ) != 0 ) )
         || ( ( bExp == 0xFF ) && ( extractSingleSig( b ) != 0 ) ) ) {
        raiseInvalid();
        return 0;
    }
    if ( aSign != bSign ) return aSign || bothZeroSingle( a, b );
    return ( a == b ) || ( aSign ^ ( a < b ) );

}

flag ltSingle( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a, b;
    flag aSign, bSign;
    int16 aExp, bExp;

    a = loadSingle( aPtr );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    b = loadSingle( bPtr );
    bExp = extractSingleExp( b );
    bSign = extractSingleSign( b );
    if (    ( ( aExp == 0xFF ) && ( extractSingleSig( a ) != 0 ) )
         || ( ( bExp == 0xFF ) && ( extractSingleSig( b ) != 0 ) ) ) {
        raiseInvalid();
        return 0;
    }
    if ( aSign != bSign ) return aSign && ! bothZeroSingle( a, b );
    return ( a != b ) && ( aSign ^ ( a < b ) );

}

flag awareEqSingle( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a, b;
    int16 aExp, bExp;

    a = loadSingle( aPtr );
    aExp = extractSingleExp( a );
    b = loadSingle( bPtr );
    bExp = extractSingleExp( b );
    if (    ( ( aExp == 0xFF ) && ( extractSingleSig( a ) != 0 ) )
         || ( ( bExp == 0xFF ) && ( extractSingleSig( b ) != 0 ) ) ) {
        return 0;
    }
    return ( a == b ) || bothZeroSingle( a, b );

}

flag awareLeSingle( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a, b;
    flag aSign, bSign;
    int16 aExp, bExp;

    a = loadSingle( aPtr );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    b = loadSingle( bPtr );
    bExp = extractSingleExp( b );
    bSign = extractSingleSign( b );
    if (    ( ( aExp == 0xFF ) && ( extractSingleSig( a ) != 0 ) )
         || ( ( bExp == 0xFF ) && ( extractSingleSig( b ) != 0 ) ) ) {
        return 0;
    }
    if ( aSign != bSign ) return aSign || bothZeroSingle( a, b );
    return ( a == b ) || ( aSign ^ ( a < b ) );

}

flag awareLtSingle( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a, b;
    flag aSign, bSign;
    int16 aExp, bExp;

    a = loadSingle( aPtr );
    aExp = extractSingleExp( a );
    aSign = extractSingleSign( a );
    b = loadSingle( bPtr );
    bExp = extractSingleExp( b );
    bSign = extractSingleSign( b );
    if (    ( ( aExp == 0xFF ) && ( extractSingleSig( a ) != 0 ) )
         || ( ( bExp == 0xFF ) && ( extractSingleSig( b ) != 0 ) ) ) {
        return 0;
    }
    if ( aSign != bSign ) return aSign && ! bothZeroSingle( a, b );
    return ( a != b ) && ( aSign ^ ( a < b ) );

}

flag isNaNSingle( bits32 *aPtr, flag signalOnNaN )
{
    bits32 a;

    a = loadSingle( aPtr );
    if (    ( extractSingleExp( a ) == 0xFF )
         && ( extractSingleSig( a ) != 0 ) ) {
        if ( signalOnNaN ) raiseInvalid();
        return 1;
    }
    return 0;

}

static void
 addDoubleSigs( bits32 *aPtr, bits32 *bPtr, flag zSign, bits32 *zPtr )
{
    bits32 a0, a1, b0, b1;
    int16 aExp, bExp, zExp;
    bits32 aSig0, aSig1, bSig0, bSig1;
    bits32 zSig0, zSig1, zSig2;
    int16 expDiff;

    a1 = loadDouble1( aPtr );
    aSig1 = a1;
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 );
    b1 = loadDouble1( bPtr );
    bSig1 = b1;
    b0 = loadDouble0( bPtr );
    bSig0 = extractDoubleSig0( b0 );
    bExp = extractDoubleExp( b0 );
    expDiff = aExp - bExp;
    if ( 0 < expDiff ) {
        if ( aExp == 0x7FF ) {
            storeWholeDouble( a0, a1, zPtr );
            return;
        }
        if ( bExp == 0 ) {
            expDiff--;
        }
        else {
            bSig0 |= 0x100000;
        }
        shiftDown64ExtraJamming(
            bSig0, bSig1, 0, expDiff, &bSig0, &bSig1, &zSig2 );
        zExp = aExp;
    }
    else if ( expDiff < 0 ) {
        if ( bExp == 0x7FF ) {
            if ( ( bSig0 | bSig1 ) != 0 ) {
                storeWholeDouble( b0, b1, zPtr );
            }
            else {
                storeDouble( zSign, 0x7FF, 0, 0, zPtr );
            }
            return;
        }
        if ( aExp == 0 ) {
            expDiff++;
        }
        else {
            aSig0 |= 0x100000;
        }
        shiftDown64ExtraJamming(
            aSig0, aSig1, 0, -expDiff, &aSig0, &aSig1, &zSig2 );
        zExp = bExp;
    }
    else {
        if ( aExp == 0x7FF ) {
            if ( ( ( aSig0 | aSig1 ) == 0 ) && ( ( bSig0 | bSig1 ) != 0 ) ) {
                storeWholeDouble( b0, b1, zPtr );
            }
            else {
                storeWholeDouble( a0, a1, zPtr );
            }
            return;
        }
        add64( aSig0, aSig1, bSig0, bSig1, &zSig0, &zSig1 );
        if ( aExp == 0 ) {
            storeDouble( zSign, 0, zSig0, zSig1, zPtr );
            return;
        }
        zSig2 = 0;
        zSig0 |= 0x200000;
        zExp = aExp;
        goto shiftDown1;
    }
    aSig0 |= 0x100000;
    add64( aSig0, aSig1, bSig0, bSig1, &zSig0, &zSig1 );
    zExp--;
    if ( zSig0 < 0x200000 ) goto roundAndStore;
    zExp++;
shiftDown1:
    shiftDown64ExtraJamming( zSig0, zSig1, zSig2, 1, &zSig0, &zSig1, &zSig2 );
roundAndStore:
    roundAndStoreDouble( zSign, zExp, zSig0, zSig1, zSig2, zPtr );

}

static void
 subDoubleSigs( bits32 *aPtr, bits32 *bPtr, flag zSign, bits32 *zPtr )
{
    bits32 a0, a1, b0, b1;
    int16 aExp, bExp, zExp;
    bits32 aSig0, aSig1, bSig0, bSig1, zSig0, zSig1;
    int16 expDiff;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bSig0 = extractDoubleSig0( b0 );
    bExp = extractDoubleExp( b0 );
    shortShiftUp64( aSig0, a1, 10, &aSig0, &aSig1 );
    shortShiftUp64( bSig0, b1, 10, &bSig0, &bSig1 );
    expDiff = aExp - bExp;
    if ( 0 < expDiff ) goto aExpBigger;
    if ( expDiff < 0 ) goto bExpBigger;
    if ( aExp == 0x7FF ) {
        if ( ( aSig0 | aSig1 ) != 0 ) {
            storeWholeDouble( a0, a1, zPtr );
        }
        else if ( ( bSig0 | bSig1 ) != 0 ) {
            storeWholeDouble( b0, b1, zPtr );
        }
        else {
            raiseInvalid();
            storeWholeDouble(
                DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        aExp = 1;
        bExp = 1;
    }
    if ( bSig0 < aSig0 ) goto aBigger;
    if ( aSig0 < bSig0 ) goto bBigger;
    if ( bSig1 < aSig1 ) goto aBigger;
    if ( aSig1 < bSig1 ) goto bBigger;
    storeWholeDouble( 0, 0, zPtr );
    return;
bExpBigger:
    if ( bExp == 0x7FF ) {
        if ( ( bSig0 | bSig1 ) != 0 ) {
            storeWholeDouble( b0, b1, zPtr );
        }
        else {
            storeDouble( zSign ^ 1, 0x7FF, 0, 0, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        expDiff++;
    }
    else {
        aSig0 |= 0x40000000;
    }
    shiftDown64Jamming( aSig0, aSig1, -expDiff, &aSig0, &aSig1 );
    bSig0 |= 0x40000000;
bBigger:
    sub64( bSig0, bSig1, aSig0, aSig1, &zSig0, &zSig1 );
    zExp = bExp;
    zSign ^= 1;
    goto normalizeRoundAndStore;
aExpBigger:
    if ( aExp == 0x7FF ) {
        storeWholeDouble( a0, a1, zPtr );
        return;
    }
    if ( bExp == 0 ) {
        expDiff--;
    }
    else {
        bSig0 |= 0x40000000;
    }
    shiftDown64Jamming( bSig0, bSig1, expDiff, &bSig0, &bSig1 );
    aSig0 |= 0x40000000;
aBigger:
    sub64( aSig0, aSig1, bSig0, bSig1, &zSig0, &zSig1 );
    zExp = aExp;
normalizeRoundAndStore:
    zExp--;
    normalizeRoundAndStoreDouble( zSign, zExp - 10, zSig0, zSig1, zPtr );

}

void addDouble( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    flag aSign, bSign;

    TRACE(printf("%14g + %14g = ", *(double*)aPtr, *(double*)bPtr));
    aSign = extractDoubleSign( loadDouble0( aPtr ) );
    bSign = extractDoubleSign( loadDouble0( bPtr ) );
    if ( aSign == bSign ) {
        addDoubleSigs( aPtr, bPtr, aSign, zPtr );
    }
    else {
        subDoubleSigs( aPtr, bPtr, aSign, zPtr );
    }
    TRACE(printf("%14g\n", *(double*)zPtr));

}

void subDouble( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    flag aSign, bSign;

    TRACE(printf("%14g - %14g = ", *(double*)aPtr, *(double*)bPtr));
    aSign = extractDoubleSign( loadDouble0( aPtr ) );
    bSign = extractDoubleSign( loadDouble0( bPtr ) );
    if ( aSign == bSign ) {
        subDoubleSigs( aPtr, bPtr, aSign, zPtr );
    }
    else {
        addDoubleSigs( aPtr, bPtr, aSign, zPtr );
    }
    TRACE(printf("%14g\n", *(double*)zPtr));
}

void mulDouble( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    bits32 a0, a1, b0, b1;
    flag aSign, bSign, zSign;
    int16 aExp, bExp, zExp;
    bits32 aSig0, aSig1, bSig0, bSig1;
    bits32 zSig0, zSig1, zSig2, zSig3;

    TRACE(printf("%14g * %14g = ", *(double*)aPtr, *(double*)bPtr));
    a1 = loadDouble1( aPtr );
    aSig1 = a1;
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 );
    aSign = extractDoubleSign( a0 );
    b1 = loadDouble1( bPtr );
    bSig1 = b1;
    b0 = loadDouble0( bPtr );
    bSig0 = extractDoubleSig0( b0 );
    bExp = extractDoubleExp( b0 );
    bSign = extractDoubleSign( b0 );
    zSign = aSign ^ bSign;
    if ( aExp == 0x7FF ) {
        if ( ( aSig0 | aSig1 ) != 0 ) {
            storeWholeDouble( a0, a1, zPtr );
        }
        else if ( ( bExp == 0x7FF ) && ( ( bSig0 | bSig1 ) != 0 ) ) {
            storeWholeDouble( b0, b1, zPtr );
        }
        else if ( ( bExp | bSig0 | bSig1 ) == 0 ) {
            raiseInvalid();
            storeWholeDouble(
                DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
        }
        else {
            storeDouble( zSign, 0x7FF, 0, 0, zPtr );
        }
        return;
    }
    if ( bExp == 0x7FF ) {
        if ( ( bSig0 | bSig1 ) != 0 ) {
            storeWholeDouble( b0, b1, zPtr );
        }
        else if ( ( aExp | aSig0 | aSig1 ) == 0 ) {
            raiseInvalid();
            storeWholeDouble(
                DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
        }
        else {
            storeDouble( zSign, 0x7FF, 0, 0, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        if ( ( aSig0 | aSig1 ) == 0 ) {
            storeDouble( zSign, 0, 0, 0, zPtr );
            return;
        }
        normalizeDoubleSubnormal( aSig0, aSig1, &aExp, &aSig0, &aSig1 );
    }
    else {
        aSig0 |= 0x100000;
    }
    if ( bExp == 0 ) {
        if ( ( bSig0 | bSig1 ) == 0 ) {
            storeDouble( zSign, 0, 0, 0, zPtr );
            return;
        }
        normalizeDoubleSubnormal( bSig0, bSig1, &bExp, &bSig0, &bSig1 );
    }
    shortShiftUp64( bSig0, bSig1, 12, &bSig0, &bSig1 );
    mul64To128( aSig0, aSig1, bSig0, bSig1, &zSig0, &zSig1, &zSig2, &zSig3 );
    add64( zSig0, zSig1, aSig0, aSig1, &zSig0, &zSig1 );
    zSig2 |= ( 0 < zSig3 );
    zExp = aExp + bExp - 0x3FF - 1;
    if ( 0x200000 <= zSig0 ) {
        shiftDown64ExtraJamming(
            zSig0, zSig1, zSig2, 1, &zSig0, &zSig1, &zSig2 );
        zExp++;
    }
    roundAndStoreDouble( zSign, zExp, zSig0, zSig1, zSig2, zPtr );
    TRACE(printf("%14g\n", *(double*)zPtr));
}

void divDouble( bits32 *aPtr, bits32 *bPtr, bits32 *zPtr )
{
    bits32 a0, a1, b0, b1;
    flag aSign, bSign, zSign;
    int16 aExp, bExp, zExp;
    bits32 aSig0, aSig1, bSig0, bSig1, zSig0, zSig1, zSig2;
    bits32 rem0, rem1, rem2, rem3;
    bits32 term0, term1, term2, term3;

    TRACE(printf("%14g / %14g = ", *(double*)aPtr, *(double*)bPtr));
    a1 = loadDouble1( aPtr );
    aSig1 = a1;
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 );
    aSign = extractDoubleSign( a0 );
    b1 = loadDouble1( bPtr );
    bSig1 = b1;
    b0 = loadDouble0( bPtr );
    bSig0 = extractDoubleSig0( b0 );
    bExp = extractDoubleExp( b0 );
    bSign = extractDoubleSign( b0 );
    zSign = aSign ^ bSign;
    if ( aExp == 0x7FF ) {
        if ( ( aSig0 | aSig1 ) != 0 ) {
            storeWholeDouble( a0, a1, zPtr );
        }
        else if ( bExp == 0x7FF ) {
            if ( ( bSig0 | bSig1 ) != 0 ) {
                storeWholeDouble( b0, b1, zPtr );
            }
            else {
                raiseInvalid();
                storeWholeDouble(
                    DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
            }
        }
        else {
            storeDouble( zSign, 0x7FF, 0, 0, zPtr );
        }
        return;
    }
    if ( bExp == 0x7FF ) {
        if ( ( bSig0 | bSig1 ) != 0 ) {
            storeWholeDouble( b0, b1, zPtr );
        }
        else {
            storeDouble( zSign, 0, 0, 0, zPtr );
        }
        return;
    }
    if ( bExp == 0 ) {
        if ( ( bSig0 | bSig1 ) == 0 ) {
            if ( ( aExp | aSig0 | aSig1 ) == 0 ) {
                raiseInvalid();
                storeWholeDouble(
                    DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
            }
            else {
                raiseDivideByZero();
                storeDouble( zSign, 0x7FF, 0, 0, zPtr );
            }
            return;
        }
        normalizeDoubleSubnormal( bSig0, bSig1, &bExp, &bSig0, &bSig1 );
    }
    else {
        bSig0 |= 0x100000;
    }
    if ( aExp == 0 ) {
        if ( ( aSig0 | aSig1 ) == 0 ) {
            storeDouble( zSign, 0, 0, 0, zPtr );
            return;
        }
        normalizeDoubleSubnormal( aSig0, aSig1, &aExp, &aSig0, &aSig1 );
    }
    else {
        aSig0 |= 0x100000;
    }
    zExp = aExp - bExp + 0x3FE - 1;
    shortShiftUp64( aSig0, aSig1, 11, &aSig0, &aSig1 );
    shortShiftUp64( bSig0, bSig1, 11, &bSig0, &bSig1 );
    if ( le64( bSig0, bSig1, aSig0, aSig1 ) ) {
        shiftDown64( aSig0, aSig1, 1, &aSig0, &aSig1 );
        zExp++;
    }
    zSig0 = estimateDiv64To32( aSig0, aSig1, bSig0 );
    mul64By32To96( bSig0, bSig1, zSig0, &term0, &term1, &term2 );
    sub96( aSig0, aSig1, 0, term0, term1, term2, &rem0, &rem1, &rem2 );
    while ( ( (sbits32) rem0 ) < 0 ) {
        zSig0--;
        add96( rem0, rem1, rem2, 0, bSig0, bSig1, &rem0, &rem1, &rem2 );
    }
    zSig1 = estimateDiv64To32( rem1, rem2, bSig0 );
    if ( ( zSig1 & 0x3FF ) <= 4 ) {
        mul64By32To96( bSig0, bSig1, zSig1, &term1, &term2, &term3 );
        sub96( rem1, rem2, 0, term1, term2, term3, &rem1, &rem2, &rem3 );
        while ( ( (sbits32) rem1 ) < 0 ) {
            zSig1--;
            add96( rem1, rem2, rem3, 0, bSig0, bSig1, &rem1, &rem2, &rem3 );
        }
        zSig1 |= ( 0 < ( rem1 | rem2 | rem3 ) );
    }
    shiftDown64ExtraJamming( zSig0, zSig1, 0, 11, &zSig0, &zSig1, &zSig2 );
    roundAndStoreDouble( zSign, zExp, zSig0, zSig1, zSig2, zPtr );
    TRACE(printf("%14g\n", *(double*)zPtr));
}

void sqrtDouble( bits32 *aPtr, bits32 *zPtr )
{
    bits32 a0, a1;
    flag aSign;
    int16 aExp, zExp;
    bits32 aSig0, aSig1, zSig0, zSig1, zSig2;
    bits32 rem0, rem1, rem2, rem3;
    bits32 term0, term1, term2, term3;
    bits32 shiftedRem0, shiftedRem1;

    a1 = loadDouble1( aPtr );
    aSig1 = a1;
    a0 = loadDouble0( aPtr );
    aSig0 = extractDoubleSig0( a0 );
    aExp = extractDoubleExp( a0 );
    aSign = extractDoubleSign( a0 );
    if ( aExp == 0x7FF ) {
        if ( ( ( aSig0 | aSig1 ) != 0 ) || ( aSign == 0 ) ) {
            storeWholeDouble( a0, a1, zPtr );
        }
        else {
            raiseInvalid();
            storeWholeDouble(
                DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
        }
        return;
    }
    if ( aSign ) {
        if ( ( aExp | aSig0 | aSig1 ) == 0 ) {
            storeWholeDouble( a0, a1, zPtr );
        }
        else {
            raiseInvalid();
            storeWholeDouble(
                DEFAULTDOUBLENANHIGH, DEFAULTDOUBLENANLOW, zPtr );
        }
        return;
    }
    if ( aExp == 0 ) {
        if ( ( aSig0 | aSig1 ) == 0 ) {
            storeWholeDouble( 0, 0, zPtr );
            return;
        }
        normalizeDoubleSubnormal( aSig0, aSig1, &aExp, &aSig0, &aSig1 );
    }
    else {
        aSig0 |= 0x100000;
    }
    zExp = ( ( aExp - 0x3FF )>>1 ) + 0x3FF - 1;
    shortShiftUp64( aSig0, aSig1, 11, &aSig0, &aSig1 );
    zSig0 = estimateSqrt32( aExp, aSig0 );
    zSig0 = ( 0xFFFFFFFD < zSig0 ) ? 0xFFFFFFFF : zSig0 + 2;
    if ( ( aExp & 1 ) != 0 ) shiftDown64( aSig0, aSig1, 1, &aSig0, &aSig1 );
    mul32To64( zSig0, zSig0, &term0, &term1 );
    sub64( aSig0, aSig1, term0, term1, &rem0, &rem1 );
    while ( ( (sbits32) rem0 ) < 0 ) {
        zSig0--;
        shortShiftUp64( 0, zSig0, 1, &term0, &term1 );
        term1 |= 1;
        add64( rem0, rem1, term0, term1, &rem0, &rem1 );
    }
    shortShiftUp64( rem0, rem1, 31, &shiftedRem0, &shiftedRem1 );
    zSig1 = estimateDiv64To32( shiftedRem0, shiftedRem1, zSig0 );
    if ( ( zSig1 & 0x3FF ) <= 5 ) {
        if ( zSig1 == 0 ) zSig1 = 1;
        mul32To64( zSig0, zSig1, &term1, &term2 );
        shortShiftUp64( term1, term2, 1, &term1, &term2 );
        sub64( rem1, 0, term1, term2, &rem1, &rem2 );
        mul32To64( zSig1, zSig1, &term2, &term3 );
        sub96( rem1, rem2, 0, 0, term2, term3, &rem1, &rem2, &rem3 );
        while ( ( (sbits32) rem1 ) < 0 ) {
            zSig1--;
            shortShiftUp96( 0, zSig0, zSig1, 1, &term1, &term2, &term3 );
            term3 |= 1;
            add96( rem1, rem2, rem3, term1, term2, term3,
                   &rem1, &rem2, &rem3 );
        }
        zSig1 |= ( 0 < ( rem1 | rem2 | rem3 ) );
    }
    shiftDown64ExtraJamming( zSig0, zSig1, 0, 11, &zSig0, &zSig1, &zSig2 );
    roundAndStoreDouble( 0, zExp, zSig0, zSig1, zSig2, zPtr );

}

flag eqDouble( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a0, a1, b0, b1;
    int16 aExp, bExp;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aExp = extractDoubleExp( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bExp = extractDoubleExp( b0 );
    if (    ( ( aExp == 0x7FF ) && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) )
         || ( ( bExp == 0x7FF ) && ( ( extractDoubleSig0( b0 ) | b1 ) != 0 ) )
       ) {
        raiseInvalid();
        return 0;
    }
    return    ( a1 == b1 )
           && ( ( a0 == b0 ) || bothZeroDouble( a0, a1, b0, b1 ) );

}

flag leDouble( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a0, a1, b0, b1;
    flag aSign, bSign;
    int16 aExp, bExp;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aExp = extractDoubleExp( a0 );
    aSign = extractSingleSign( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bExp = extractDoubleExp( b0 );
    bSign = extractSingleSign( b0 );
    if (    ( ( aExp == 0x7FF ) && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) )
         || ( ( bExp == 0x7FF ) && ( ( extractDoubleSig0( b0 ) | b1 ) != 0 ) )
       ) {
        raiseInvalid();
        return 0;
    }
    if ( aSign != bSign ) return aSign || bothZeroDouble( a0, a1, b0, b1 );
    return aSign ? le64( b0, b1, a0, a1 ) : le64( a0, a1, b0, b1 );

}

flag ltDouble( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a0, a1, b0, b1;
    flag aSign, bSign;
    int16 aExp, bExp;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aExp = extractDoubleExp( a0 );
    aSign = extractSingleSign( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bExp = extractDoubleExp( b0 );
    bSign = extractSingleSign( b0 );
    if (    ( ( aExp == 0x7FF ) && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) )
         || ( ( bExp == 0x7FF ) && ( ( extractDoubleSig0( b0 ) | b1 ) != 0 ) )
       ) {
        raiseInvalid();
        return 0;
    }
    if ( aSign != bSign ) return aSign && ! bothZeroDouble( a0, a1, b0, b1 );
    return aSign ? lt64( b0, b1, a0, a1 ) : lt64( a0, a1, b0, b1 );

}

flag awareEqDouble( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a0, a1, b0, b1;
    int16 aExp, bExp;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aExp = extractDoubleExp( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bExp = extractDoubleExp( b0 );
    if (    ( ( aExp == 0x7FF ) && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) )
         || ( ( bExp == 0x7FF ) && ( ( extractDoubleSig0( b0 ) | b1 ) != 0 ) )
       ) {
        return 0;
    }
    return    ( a1 == b1 )
           && ( ( a0 == b0 ) || bothZeroDouble( a0, a1, b0, b1 ) );

}

flag awareLeDouble( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a0, a1, b0, b1;
    flag aSign, bSign;
    int16 aExp, bExp;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aExp = extractDoubleExp( a0 );
    aSign = extractSingleSign( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bExp = extractDoubleExp( b0 );
    bSign = extractSingleSign( b0 );
    if (    ( ( aExp == 0x7FF ) && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) )
         || ( ( bExp == 0x7FF ) && ( ( extractDoubleSig0( b0 ) | b1 ) != 0 ) )
       ) {
        return 0;
    }
    if ( aSign != bSign ) return aSign || bothZeroDouble( a0, a1, b0, b1 );
    return aSign ? le64( b0, b1, a0, a1 ) : le64( a0, a1, b0, b1 );

}

flag awareLtDouble( bits32 *aPtr, bits32 *bPtr )
{
    bits32 a0, a1, b0, b1;
    flag aSign, bSign;
    int16 aExp, bExp;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    aExp = extractDoubleExp( a0 );
    aSign = extractSingleSign( a0 );
    b1 = loadDouble1( bPtr );
    b0 = loadDouble0( bPtr );
    bExp = extractDoubleExp( b0 );
    bSign = extractSingleSign( b0 );
    if (    ( ( aExp == 0x7FF ) && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) )
         || ( ( bExp == 0x7FF ) && ( ( extractDoubleSig0( b0 ) | b1 ) != 0 ) )
       ) {
        return 0;
    }
    if ( aSign != bSign ) return aSign && ! bothZeroDouble( a0, a1, b0, b1 );
    return aSign ? lt64( b0, b1, a0, a1 ) : lt64( a0, a1, b0, b1 );

}

flag isNaNDouble( bits32 *aPtr, flag signalOnNaN )
{
    bits32 a0, a1;

    a1 = loadDouble1( aPtr );
    a0 = loadDouble0( aPtr );
    if (    ( extractDoubleExp( a0 ) == 0x7FF )
         && ( ( extractDoubleSig0( a0 ) | a1 ) != 0 ) ) {
        if ( signalOnNaN ) raiseInvalid();
        return 1;
    }
    return 0;

}

