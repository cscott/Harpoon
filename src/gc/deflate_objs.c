#include "deflate_objs.h"
#include "cp_heap.h"
#include "precise_gc.h"

FLEX_MUTEX_DECLARE_STATIC(inflated_objs_mutex);

/* effects: updates list of inflated objects with 
   new locations, deflating any that have been 
   garbage-collected
 */
void deflate_freed_objs (struct copying_heap *h)
{
  struct obj_list *prev = NULL;
  struct obj_list *unit;

  FLEX_MUTEX_LOCK(&inflated_objs_mutex);

  unit = h->inflated_objs;
  
  while(unit != NULL)
    {
      jobject_unwrapped infl_obj = unit->obj;

      // update objects that have been moved
      if (IN_TO_SPACE(infl_obj->claz, *h))
	{
	  // forward pointer appropriately
	  unit->obj = PTRMASK(infl_obj->claz);
	  prev = unit;
	  unit = unit->next;
	}
      else
	{
	  // free objects that have been garbage collected
	  // and remove from list the objects that have been 
	  // moved to the old generation
	  struct obj_list *to_free = unit;

	  printf("?");

	  // invoke deflate fcn only if the object has really been GC'd
	  if (CLAZ_OKAY(infl_obj))
	    infl_obj->hashunion.inflated->precise_deflate_obj(infl_obj, 
							      (ptroff_t)0);

	  // update links
	  if (prev == NULL)
	    h->inflated_objs = unit->next;
	  else
	    prev->next = unit->next;
	  // go to next
	  unit = unit->next;
	  // free unit
	  free(to_free);	  
	}
    }
  
  FLEX_MUTEX_UNLOCK(&inflated_objs_mutex);
}


/* effects: if the object resides in the given heap, it is added
   to the list of inflated objects that need to be deflated after the
   object has been garbage collected.
*/
void register_inflated_obj(jobject_unwrapped obj, struct copying_heap *h)
{
  struct obj_list *unit = (struct obj_list *)malloc(sizeof(struct obj_list));

  if (IN_FROM_SPACE(obj, *h))
    {
      unit->obj = obj;
      FLEX_MUTEX_LOCK(&inflated_objs_mutex);
      unit->next = h->inflated_objs;
      h->inflated_objs = unit;
      FLEX_MUTEX_UNLOCK(&inflated_objs_mutex); 
    }
}


