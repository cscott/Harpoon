all: tr-w-ten.eps tr-w-all.eps

copy: tr-w-ten.eps tr-w-all.eps
	cp $^ ..

update:
	scp ygg:Transact/Binaries/MEMTRACE_SZ/"{freq,trsize}.*.*.txt" .

%.eps: %.plo
	ploticus -tightcrop -eps $<

tr-w-all.eps: acc-freq-all.pl
tr-w-ten.eps: acc-freq-ten.pl
