#!/usr/bin/env perl

while (<>) {
    my ($op,$addr,$sz,$objsize) = split;
    if ($op eq "x") {
	$txr = $txw = 0;
    } elsif ($op eq "r") {
	$txr++;
    } elsif ($op eq "w") {
	$txw++;
    } elsif ($op eq "X") {
	$count{$txr}{"r"}++;
	$count{$txw}{"w"}++;
	$count{$txr+$txw}{"T"}++;
    }
}
foreach my $sz (sort {$a <=> $b} keys %count) {
    foreach my $op (sort keys %{$count{$sz}}) {
	print $sz."\t".$op."\t".$count{$sz}{$op}."\n";
    }
}
