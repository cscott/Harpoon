typedef unsigned int uint32;
typedef long long int64;
typedef unsigned long long uint64;

void
ll_lshift( int64* aPtr, uint32* bPtr, int64* zPtr)
{
   *zPtr = *aPtr << *bPtr;
}
void
ll_rshift( int64* aPtr, uint32* bPtr, int64* zPtr)
{
   *zPtr = *aPtr >> *bPtr;
}
void
ull_rshift( uint64* aPtr, uint32* bPtr, int64* zPtr)
{
   *zPtr = *aPtr >> *bPtr;
}

void
ll_div( int64* aPtr, int64* bPtr, int64* zPtr)
{
   *zPtr = *aPtr / *bPtr;
}

void
ll_mul( int64* aPtr, int64* bPtr, int64* zPtr)
{
   *zPtr = *aPtr * *bPtr;
}

void
ll_rem( int64* aPtr, int64* bPtr, int64* zPtr)
{
   *zPtr = *aPtr % *bPtr;
}
