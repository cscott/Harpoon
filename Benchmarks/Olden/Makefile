#
# A makefile to compile and/or run all the benchmarks
#
BMARKS = bh bisort em3d health mst perimeter power treeadd tsp voronoi

compile: 
	@for dir in $(BMARKS); \
	do \
	  if test -d $$dir ; \
	  then \
	    (cd $$dir; $(MAKE);); \
	  fi; \
	done	

run: 
	@for dir in $(BMARKS); \
	do \
	  if test -d $$dir ; \
	  then \
	    (cd $$dir; $(MAKE) run;); \
	  fi; \
	done

doc:
	@for dir in $(BMARKS); \
	do \
	  if test -d $$dir ; \
	  then \
	    (cd $$dir; $(MAKE) doc;); \
	  fi; \
	done	

clean:
	@for dir in $(BMARKS); \
	do \
	  if test -d $$dir ; \
	  then \
	    (cd $$dir; $(MAKE) clean;); \
	  fi; \
	done	
