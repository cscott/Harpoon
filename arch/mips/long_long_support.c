typedef unsigned int uint32;
typedef unsigned long long uint64;

void
ll_div( uint64* aPtr, uint64* bPtr, uint64* zPtr)
{
   *zPtr = *aPtr / *bPtr;
}

void
ll_mod( uint64* aPtr, uint64* bPtr, uint64* zPtr)
{
   *zPtr = *aPtr % *bPtr;
}

void
ll_rem( uint64* aPtr, uint64* bPtr, uint64* zPtr)
{
   ll_mod(aPtr, bPtr, zPtr);
   *zPtr = *aPtr - (*bPtr * *zPtr);
}
