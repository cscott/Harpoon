
// Token values

// Used = 100
// Free = 101


foo_state * __repairstate0__=new foo_state();
__repairstate0__->d=(int)d;
__repairstate0__->doanalysis();
*((int*) &d)=__repairstate0__->d;
delete __repairstate0__;
