set term latex default
set title "SSI form linearity"
set xlabel "Procedure size"
set ylabel 'Number of \\ $\phi$- and $\sigma$- \\ functions' -2
# old plot command.
#plot [0:3000] "phisig.data-sorted" using 1:4 notitle with dots

#set grid
set logscale
set mxtics 10
set mytics 10
# now line-fit
a = 1#.13147572158139
b = 0.280604907987985
f(x)=exp(a*log(x)+b)
fit [0:3000] f(x) 'phisig.data-sorted' using 1:4 via b #a,b
plot [5:10000] [5:] f(x) notitle with lines, "phisig.data-sorted" using 1:4 notitle with dots

