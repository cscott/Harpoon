BVAR=BASE RCHECK WCHECK WCHECKUNOPT RWCHECK RWCHECKUNOPT RWCHECKOPT
AVAR=BASE COPYALL SHALLOW RANDOM LOCKFREE

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
run-bench: all run-init-bench $(foreach v,$(BVAR),run-bench-$(v)) run-done-bench
run-array: all run-init-array $(foreach v,$(AVAR),run-array-$(v)) run-done-array
run-bench-%: bench-%
	-./bench-$* # warm up cache
	-/usr/bin/time -f $*" %U" -o results-bench.txt -a ./bench-$*
	tail -1 results-bench.txt
run-array-%: array-%
	-./array-$* # warm up cache
	-/usr/bin/time -f $*" 8 %U" -o results-array.txt -a ./array-$* 8
	-/usr/bin/time -f $*" 16 %U" -o results-array.txt -a ./array-$* 16
	-/usr/bin/time -f $*" 32 %U" -o results-array.txt -a ./array-$* 32
	-/usr/bin/time -f $*" 64 %U" -o results-array.txt -a ./array-$* 64
	-/usr/bin/time -f $*" 128 %U" -o results-array.txt -a ./array-$* 128
	-/usr/bin/time -f $*" 256 %U" -o results-array.txt -a ./array-$* 256
	-/usr/bin/time -f $*" 512 %U" -o results-array.txt -a ./array-$* 512
	-/usr/bin/time -f $*" 1024 %U" -o results-array.txt -a ./array-$* 1024
	-/usr/bin/time -f $*" 2048 %U" -o results-array.txt -a ./array-$* 2048
	-/usr/bin/time -f $*" 4096 %U" -o results-array.txt -a ./array-$* 4096
	tail -10 results-array.txt

run-init-%:
	touch results-$*.txt
	echo >> results-$*.txt
	echo -n "--- Run started: " >> results-$*.txt
	date >> results-$*.txt
run-done-%:
	echo "--- Run complete ---" >> results-$*.txt

####
print.ps:
	a2ps -o$@ -Mletter --prologue=color -E -g notes.txt results.txt Makefile bench.c bench-split.c array.c llsc-*.h rdtsc-*.h
