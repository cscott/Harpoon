BVAR=BASE RCHECK WCHECK WCHECKUNOPT RWCHECK RWCHECKUNOPT RWCHECKOPT
AVAR=BASE COPYALL SHALLOW RANDOM

all: $(foreach v,$(BVAR),bench-$(v).s bench-$(v)) \
     $(foreach v,$(AVAR),array-$(v).s array-$(v))

bench-%.s: bench.c llsc-unimpl.h llsc-ppc32.h
	gcc -D$* -S -O9 $<
	mv bench.s bench-$*.s
bench-%: bench-%.s
	gcc -o $@ -O9 $<

array-%.s: array.c llsc-unimpl.h llsc-ppc32.h
	gcc -D$* -S -O9 $<
	mv array.s array-$*.s
array-%: array-%.s
	gcc -o $@ -O9 $< -lgc

clean:
	$(RM) bench-*.s array-*.s
	$(RM) bench-*[A-Z] array-*[A-Z]
run: all run-init $(foreach v,$(BVAR),run-$(v)) run-done
run-init:
	touch results.txt
	echo >> results.txt
	echo -n "--- Run started: " >> results.txt
	date >> results.txt
run-done:
	echo "--- Run complete ---" >> results.txt
run-%: bench-%
	-./bench-$* # warm up cache
	-/usr/bin/time -f $*" %U" -o results.txt -a ./bench-$*
	tail -1 results.txt

####
print.ps:
	a2ps -o$@ -Mletter --prologue=color -E -g notes.txt results.txt Makefile bench.c bench-split.c array.c llsc-*.h rdtsc-*.h
