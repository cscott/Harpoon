set term latex default
set title "Number of variables in large procedures"
set xlabel "Procedure size"
set ylabel '$V_{0}$' -1
set logscale
set mxtics 10
set mytics 10
set key spacing 5
# now line-fit
c=2.0
d=1.0/3.0
f(x)=c*(x**d)
fit f(x) 'Data/v0.data' using 2:5 via c
plot [1:10000] [1:10000] f(x) title "$y=cx^{1/3}$" with lines, "Data/v0.data" using 2:5 notitle with dots
