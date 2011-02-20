#!/usr/bin/env perl

while (<>) {
    my @fields = split;
    next if $fields[0] eq "x" or $fields[0] eq "X";
    $count{$fields[3]}{$fields[0]}++;
}
foreach my $sz (sort {$a <=> $b} keys %count) {
    foreach my $op (sort keys %{$count{$sz}}) {
	print $sz."\t".$op."\t".$count{$sz}{$op}."\n";
    }
}
