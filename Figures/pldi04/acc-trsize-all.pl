#!/usr/bin/env perl

# missing: trsize.{201,209,222}.100

@benchmarks = ( 201, 202, 205, 209, 213, 222, 228 );
%fullname = ( 200 => "check", 201 => "compress", 202 => "jess",
	      205 => "raytrace", 209 => "db", 213 => "javac",
	      222 => "mpegaudio", 227 => "mtrt", 228 => "jess" );
%runsize = ( 201=>10, 202 => 100, 205 => 100, 209 => 10, 213 => 100, 222 => 10, 228 => 100);

foreach my $run (keys %runsize) {
    my $file = "trsize.".$run.".".$runsize{$run}.".txt";
    open(FH, "< $file") or die "Can't open $file for reading.\n";
    while (<FH>) {
        my ($sz, $op, $freq) = split;
	$count{$run}{$sz}{$op} = $freq;
	$total{$run}{$op} += $freq*$sz;
	$allsizes{$sz} = 1; # keep track of all sizes seen.
    }
    close FH;
}
$allsizes{1 + (sort {$b <=> $a} keys %allsizes)[0]} = 1;
@allops = ( "r", "w", "T" );
# now accumulate in reverse size order
# (so we get "number of accesses to objects larger than X")
print "SIZE";
foreach my $run (sort keys %runsize) {
    foreach my $op ( @allops ) {
	print " ".$fullname{$run}."_".$op;
    }
}
print "\n";
foreach my $sz (sort {$b <=> $a} keys %allsizes) {
    print "$sz";
    foreach my $run (sort keys %runsize) {
	foreach my $op ( @allops ) {
	    $acc{$run}{$op} += $count{$run}{$sz}{$op}*$sz;
	    print " ".(100.0*$acc{$run}{$op}/$total{$run}{$op});
	}
    }
    print "\n";
}
