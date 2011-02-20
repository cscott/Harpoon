set term latex default 7
set title "Average space reduction as a function of pointer size"
set xlabel "Pointer size (bits)"
set ylabel "Reduction of \\\\ dynamically \\\\ alloc'd \\\\ bytes \\\\ (\\% total \\\\ alloc'd)" -4
set logscale 8
set xtics 2
#set mytics 10
set size 0.7,0.7

#plot [8:32] 'ptrsize.data' using 1:2 notitle with lines 
f(x)=(1-(x/32))*20.0
plot [7.75:32] f(x) title "bit alignment" with lines, 'ptrsize.data' using 1:2 title "byte alignment" with steps
