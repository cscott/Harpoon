/* trace takes a pointer to an object and traces the pointers w/in it */
#define START_BIT 2

#ifndef WITH_GENERATIONAL_GC
void func_proto(jobject_unwrapped unaligned_ptr)
#else
void func_proto(jobject_unwrapped unaligned_ptr, int wbtype)
#endif
#ifndef WITH_OLD_TRACER
{
  jobject_unwrapped obj, *fields;
  struct claz *claz_ptr;
  int bits_needed;

  obj = PTRMASK(unaligned_ptr);

  claz_ptr = obj->claz;

  /* this object contains no pointers */
  if (claz_ptr->gc_info.bitmap == 0)
    {
      COLLECT_NOPTR_STATS();
      return;
    }

  fields = (jobject_unwrapped *)obj;

  /* each word in the object (including the header words) is */
  /* represented by a corresponding bit in the GC bitmap. if */
  /* the object is too large for the in-line bitmap, then an */
  /* auxiliary bitmap is used. here we determine whether the */
  /* bitmap is in-lined based on the size of the object. in */
  /* future, we may be cleverer by stealing the low bit to */
  /* determine whether the in-line bitmap is used, since the */
  /* low 2 bits are free when we use the auxiliary bitmap. */
  bits_needed = (claz_ptr->size + SIZEOF_VOID_P - 1)/SIZEOF_VOID_P;

  if (claz_ptr->component_claz == NULL)
    {
      int bitmaps_needed =
	(bits_needed + BITS_IN_GC_BITMAP - 1)/BITS_IN_GC_BITMAP;

      if (bitmaps_needed == 1)
	{
	  ptroff_t bitmap = (claz_ptr->gc_info.bitmap >> START_BIT);
	  int i = START_BIT;
	  
	  for( ; bitmap != 0; i++)
	    {
	      if ((bitmap & 1) && (fields[i] != NULL))
#ifndef WITH_GENERATIONAL_GC
		handle_ref(&fields[i]);
#else
		handle_ref(&fields[i], wbtype);
#endif
	      bitmap >>= 1;
	    }
	}
      else
	{
	  ptroff_t *bitmap_ptr = claz_ptr->gc_info.ptr;
	  int i;
	  
	  COLLECT_LRGOBJ_STATS();
	  assert(bitmaps_needed > 1);

	  /* the outer loop iterates through the bitmaps */
	  /* the inner loop iterates through the bits */
	  for (i = 0; i < bitmaps_needed; i++)
	    {
	      int j;
	      ptroff_t bitmap = bitmap_ptr[i];
	  
	      /* as we examine each bit in the bitmap, starting */
	      /* from bit zero, we clear it. since we are only */
	      /* interested in bits that are set, we can stop */
	      /* examining a bitmap when all the remaining bits */
	      /* are clear. that's why the loop termination */
	      /* condition in this for loop is a bit strange. */
	      for (j = i*BITS_IN_GC_BITMAP; bitmap != 0; j++)
		{
		  /* if current bit is set, then handle */
		  /* corresponding reference in object, if any. */
		  /* the current bit is always the low bit */
		  /* since we shift the bitmap right at each */
		  /* iteration. */
		  if ((bitmap & 1) && (fields[j] != NULL))
#ifndef WITH_GENERATIONAL_GC
		    handle_ref(&fields[j]);
#else
		    handle_ref(&fields[j], wbtype);
#endif
		  /* shift bitmap one right; this should always shift */
		  /* in a zero since ptroff_t is always unsigned */
		  bitmap = bitmap >> 1;
		}
	    }
	}
    }
  else
    {
      struct aarray *arr = (struct aarray *)obj;
      int bitmaps_needed = /* for arrays, we keep an extra bit for fields */
	(bits_needed + BITS_IN_GC_BITMAP)/BITS_IN_GC_BITMAP;

      if (bitmaps_needed == 1)
	{
	  ptroff_t bitmap = (claz_ptr->gc_info.bitmap >> START_BIT);
	  int i = START_BIT;

	  for( ; bitmap != 0; i++)
	    {
	      if (bitmap & 1)
		{
		  if (i == bits_needed)
		    {
		      // last bit refers to the array elements
		      jobject_unwrapped *elements =
			(jobject_unwrapped *) arr->element_start;
		      int k;

		      for(k = 0; k < arr->length; k++)
			{
			  if (elements[k] != NULL)
#ifndef WITH_GENERATIONAL_GC
			    handle_ref(&elements[k]);
#else
			    handle_ref(&elements[k], wbtype);
#endif
			}
		      break;
		    }
		  if (fields[i] != NULL)
#ifndef WITH_GENERATIONAL_GC
		    handle_ref(&fields[i]);
#else
		    handle_ref(&fields[i], wbtype);
#endif
		}
	      bitmap >>= 1;
	    }
	}
      else
	{
	  ptroff_t *bitmap_ptr = claz_ptr->gc_info.ptr;
	  int i;

	  COLLECT_LRGOBJ_STATS();
	  assert(bitmaps_needed >= 0);

	  /* the outer loop iterates through the bitmaps */
	  /* the inner loop iterates through the bits */
	  for (i = 0; i < bitmaps_needed; i++)
	    {
	      int j;
	      ptroff_t bitmap = bitmap_ptr[i];

	      /* as we examine each bit in the bitmap, starting */
	      /* from bit zero, we clear it. since we are only */
	      /* interested in bits that are set, we can stop */
	      /* examining a bitmap when all the remaining bits */
	      /* are clear. that's why the loop termination */
	      /* condition in this for loop is a bit strange. */
	      for (j = i*BITS_IN_GC_BITMAP; bitmap != 0; j++)
		{
		  /* if current bit is set, then handle */
		  /* corresponding reference in object, if any. */
		  /* the current bit is always the low bit */
		  /* since we shift the bitmap right at each */
		  /* iteration. */
		  if (bitmap & 1)
		    {
		      /* for arrays, the last bit of the bitmap */
		      /* is used for the array elements */
		      if (j == bits_needed)
			{
			  // last bit refers to the array elements
			  jobject_unwrapped *elements =
			    (jobject_unwrapped *) arr->element_start;
			  int k;
			  
			  for(k = 0; k < arr->length; k++)
			    {
			      if (elements[k] != NULL)
#ifndef WITH_GENERATIONAL_GC
				handle_ref(&elements[k]);
#else
				handle_ref(&elements[k], wbtype);
#endif
			    }
			  break;
			}
		      if (fields[j] != NULL)
#ifndef WITH_GENERATIONAL_GC
			  handle_ref(&fields[j]);
#else
			  handle_ref(&fields[j], wbtype);
#endif
		    }
		  
		  /* shift bitmap one right; this should always shift */
		  /* in a zero since ptroff_t is always unsigned */
		  bitmap = bitmap >> 1;
		}
	    }
	}
    }
}
#else
{
  jobject_unwrapped obj;
  int bits_needed, bitmaps_needed, i;
  ptroff_t *bitmap_ptr;
  struct aarray *arr = NULL;
  jobject_unwrapped *fields;
  jobject_unwrapped *elements = NULL;
  struct claz *claz_ptr;

  obj = PTRMASK(unaligned_ptr);

  /* this object contains no pointers */
  if (obj->claz->gc_info.bitmap == 0)
    {
      COLLECT_NOPTR_STATS();
      return;
    }

  claz_ptr = obj->claz;

  /* each word in the object (including the header words) is */
  /* represented by a corresponding bit in the GC bitmap. if */
  /* the object is too large for the in-line bitmap, then an */
  /* auxiliary bitmap is used. here we determine whether the */
  /* bitmap is in-lined based on the size of the object. in */
  /* future, we may be cleverer by stealing the low bit to */
  /* determine whether the in-line bitmap is used, since the */
  /* low 2 bits are free when we use the auxiliary bitmap. */
  bits_needed = (claz_ptr->size + SIZEOF_VOID_P - 1)/SIZEOF_VOID_P;

  if (claz_ptr->component_claz != NULL)
    {
      /* for arrays, we keep an extra bit for fields */
      bits_needed++;
      arr = (struct aarray *)obj;
      error_gc("Object is an array.n", "");
    }

  fields = (jobject_unwrapped *)obj;

  bitmaps_needed = (bits_needed + BITS_IN_GC_BITMAP - 1)/BITS_IN_GC_BITMAP;
  error_gc("object size = %d bytesn", claz_ptr->size);
  error_gc("bitmaps_needed = %dn", bitmaps_needed);
  assert(bitmaps_needed >= 0);

  if (bitmaps_needed > 1)
    {
      bitmap_ptr = claz_ptr->gc_info.ptr;
      COLLECT_LRGOBJ_STATS();
    }
  else
    bitmap_ptr = &(claz_ptr->gc_info.bitmap);

  /* the outer loop iterates through the bitmaps */
  /* the inner loop iterates through the bits */
  for (i = 0; i < bitmaps_needed; i++)
    {
      int j;
      ptroff_t bitmap = bitmap_ptr[i];
      print_bitmap(bitmap);

      /* as we examine each bit in the bitmap, starting */
      /* from bit zero, we clear it. since we are only */
      /* interested in bits that are set, we can stop */
      /* examining a bitmap when all the remaining bits */
      /* are clear. that's why the loop termination */
      /* condition in this for loop is a bit strange. */
      for (j = i*BITS_IN_GC_BITMAP; bitmap != 0; j++)
	{
 	  /* if current bit is set, then handle */
	  /* corresponding reference in object, if any. */
 	  /* the current bit is always the low bit */
	  /* since we shift the bitmap right at each */
	  /* iteration. */
	  if (bitmap & 1)
	    {
	      /* for arrays, the last bit of the bitmap */
              /* is used for the array elements */
	      if (arr != NULL && j == (bits_needed - 1))
		{
		  /* we should be looking at the last bit */
		  assert(bitmap == 1 && i == (bitmaps_needed - 1));
		  error_gc("    array contains pointers.n", "");
		  elements = (jobject_unwrapped *)(arr->element_start);
		  break;
		}
	      else if (fields[j] != NULL)
		{
		  error_gc("    field at %p ", fields[j]);
#ifndef WITH_GENERATIONAL_GC
		  handle_ref(&fields[j]);
#else
		  handle_ref(&fields[j], wbtype);
#endif
		}
	    }

 	  /* shift bitmap one right; this should always shift */
 	  /* in a zero since ptroff_t is always unsigned */
	  bitmap = bitmap >> 1;
	}
    }
  /* handle arrays of pointers here */
  if (elements != NULL)
    {
      int k;
      assert(arr != NULL);
      /* iterate through all the elements of the array, ignoring null ptrs */
      for (k = 0; k < arr->length; k++)
	{
	  if (elements[k] != NULL)
	    {
	      error_gc("    array element at %p ", elements[k]);
#ifndef WITH_GENERATIONAL_GC
	      handle_ref(&elements[k]);
#else
	      handle_ref(&elements[k], wbtype);
#endif
	    }
	}
    }
}
#endif
