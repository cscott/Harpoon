set term latex default
set title "Linearity of uses in SSI form"
set xlabel "Procedure size"
set ylabel '$U_{SSI}$' -1
# old plot command.
#plot [0:3000] "Data/ussi.data" using 2:3 notitle with dots

#set grid
set logscale
set mxtics 10
set mytics 10
set key bottom
set key spacing 5
# now line-fit
c = 1.0
f(x)=c*x
fit [1:1000] f(x) 'Data/ussi.data' using 2:3 via c
plot [2:] [2:] f(x) title "$y=cx$" with lines, "Data/ussi.data" using 2:3 notitle with dots
