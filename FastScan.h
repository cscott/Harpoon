#ifndef FASTSCAN
#define FASTSCAN
#include "Names.h"


struct methodchain{
    struct methodname *method;
    struct methodchain *caller;
};
void outputinfo(struct namer* namer, struct genhashtable *calltable);
void fastscan();
#endif
