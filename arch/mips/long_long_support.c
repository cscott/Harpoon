#include <math.h>

typedef unsigned int uint32;
typedef long long int64;
typedef unsigned long long uint64;

#define TRACE(x) /*x*/

void
ll_lshift( int64* aPtr, uint32* bPtr, int64* zPtr)
{
   TRACE(printf("%lld (%x %x) << %d = ", *aPtr, *(uint32*)aPtr, *((uint32*)aPtr + 1), *bPtr));
   *zPtr = *aPtr << *bPtr;
   TRACE(printf("%x %x\n", *(uint32*)zPtr,*((uint32*)zPtr+1)));
}
void
ll_rshift( int64* aPtr, uint32* bPtr, int64* zPtr)
{
   TRACE(printf("%x%x >> %d = ", *(uint32*)aPtr, *((uint32*)aPtr + 1), *bPtr));
   *zPtr = *aPtr >> *bPtr;
   TRACE(printf("%x%x\n", *(uint32*)zPtr,*((uint32*)zPtr+1)));
}
void
ull_rshift( uint64* aPtr, uint32* bPtr, uint64* zPtr)
{
   TRACE(printf("%x%x >>> %d = ", *(uint32*)aPtr, *((uint32*)aPtr + 1), *bPtr));
   *zPtr = *aPtr >> *bPtr;
   TRACE(printf("%x%x\n", *(uint32*)zPtr,*((uint32*)zPtr+1)));
}

void
ll_div( int64* aPtr, int64* bPtr, int64* zPtr)
{
   TRACE(printf("%x%x / %x%x = ", *(uint32*)aPtr, *((uint32*)aPtr + 1), *(uint32*)bPtr, *((uint32*)bPtr + 1)));
   *zPtr = *aPtr / *bPtr;
   TRACE(printf("%x%x\n", *(uint32*)zPtr,*((uint32*)zPtr+1)));
}

void
ll_mul( int64* aPtr, int64* bPtr, int64* zPtr)
{
   TRACE(printf("%x%x * %x%x = ", *(uint32*)aPtr, *((uint32*)aPtr + 1), *(uint32*)bPtr, *((uint32*)bPtr + 1)));
   *zPtr = *aPtr * *bPtr;
   TRACE(printf("%x %x\n", *(uint32*)zPtr,*((uint32*)zPtr+1)));
}

void
ll_rem( int64* aPtr, int64* bPtr, int64* zPtr)
{
   TRACE(printf("%x%x % %x%x = ", *(uint32*)aPtr, *((uint32*)aPtr + 1), *(uint32*)bPtr, *((uint32*)bPtr + 1)));
   *zPtr = *aPtr % *bPtr;
   TRACE(printf("%x%x\n", *(uint32*)zPtr,*((uint32*)zPtr+1)));
}

double
d_rem( double a, double b)
{
   //printf("LLL %g %% %g = %g (%g)\n", a, b, fmod(a,b), fmod(b,a));
   //printf("LLL %g %% %g = %g \n", a, b, fmod(a,b));
   return fmod(a,b);
}

float
f_rem( float a, float b)
{
   //printf("LLL %g %% %g = %g (%g)\n", a, b, fmod(a,b), fmod(b,a));
   //printf("LLL %g %% %g = %g \n", a, b, fmod(a,b));
   return fmod(a,b);
}
