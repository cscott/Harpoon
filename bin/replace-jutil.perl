#!/usr/bin/perl

# in harpoon.Util.Collections
@incoll = (
	   AbstractHeap,
	   AbstractMapEntry,
	   AggregateMapFactory,
	   AggregateSetFactory,
	   BinaryHeap,
	   BinaryTree,
	   BinomialHeap,
	   BitSetFactory,
	   CollectionFactory,
	   CollectionWrapper,
	   DisjointSet,
	   Environment,
	   Factories,
	   FibonacciHeap,
	   GenericInvertibleMap,
	   GenericInvertibleMultiMap,
	   GenericMultiMap,
	   HashEnvironment,
	   Heap,
	   IntervalTree,
	   InvertibleMap,
	   InvertibleMultiMap,
	   LinearMap,
	   LinearSet,
	   ListFactory,
	   ListWrapper,
	   MapFactory,
	   MapSet,
	   MapWrapper,
	   MultiMap,
	   MultiMapFactory,
	   MultiMapSet,
	   PairMapEntry,
	   PersistentEnvironment,
	   PersistentMap,
	   PersistentMapFactory,
	   PersistentSet,
	   PersistentSetFactory,
	   PersistentTreeNode,
	   RedBlackTree,
	   ReverseIterator,
	   ReverseListIterator,
	   SetFactory,
	   SetWrapper,
	   SnapshotIterator,
	   TestMap,
	   TestSet,
	   UniqueStack,
	   UniqueVector,
	   UnmodifiableIterator,
	   UnmodifiableListIterator,
	   UnmodifiableMultiMap,
#	   WorkSet # Worklist issues.
	);

# in harpoon.Util
@inutil = (
	   BitString,
	   CombineIterator, # array issues
	   Default,
	   FilterIterator,
	   Indexer,
	   IteratorEnumerator,
	   ReferenceUnique,
#	   Util,
	   );

# initialize map of old names to new names.
%map = ();
foreach my $cls (@inutil) {
    $map{"harpoon.Util.$cls"}="net.cscott.jutil.$cls";
}
foreach my $cls (@incoll) {
    $map{"harpoon.Util.Collections.$cls"}="net.cscott.jutil.$cls";
}
# substitution function
sub dosubst {
    my $str = shift @_;
    return $map{$str} if exists $map{$str};
    return $str; # otherwise
}

while (<>) {
    # replace old assert with assert
    s/\b(harpoon\.Util\.(?:Collections\.)?\w+)/&dosubst($1)/ge;
    print $_;
}
