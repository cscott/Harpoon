
// Token values



foo_state * __repairstate0__=new foo_state();
__repairstate0__->head=(int)head;
__repairstate0__->doanalysis();
*((int*) &head)=__repairstate0__->head;
delete __repairstate0__;
