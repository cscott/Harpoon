
// Token values

// Used = 100
// Free = 101


int __Success = 1;

// creating hashtables 
SimpleHash* __int___hash = new SimpleHash();
SimpleHash* __FileBlock___hash = new SimpleHash();
SimpleHash* __UsedBlock___hash = new SimpleHash();
SimpleHash* __FileInode___hash = new SimpleHash();
SimpleHash* __SuperBlock___hash = new SimpleHash();
SimpleHash* __UsedInode___hash = new SimpleHash();
SimpleHash* __InodeBitmapBlock___hash = new SimpleHash();
SimpleHash* __BlockBitmapBlock___hash = new SimpleHash();
SimpleHash* __FileDirectoryBlock___hash = new SimpleHash();
SimpleHash* __token___hash = new SimpleHash();
SimpleHash* __DirectoryEntry___hash = new SimpleHash();
SimpleHash* __RootDirectoryInode___hash = new SimpleHash();
SimpleHash* __FreeBlock___hash = new SimpleHash();
SimpleHash* __GroupBlock___hash = new SimpleHash();
SimpleHash* __Block___hash = new SimpleHash();
SimpleHash* __DirectoryBlock___hash = new SimpleHash();
SimpleHash* __FreeInode___hash = new SimpleHash();
SimpleHash* __Inode___hash = new SimpleHash();
SimpleHash* __DirectoryInode___hash = new SimpleHash();
SimpleHash* __InodeTableBlock___hash = new SimpleHash();
__SuperBlock___hash->addParent(__UsedBlock___hash);
__GroupBlock___hash->addParent(__UsedBlock___hash);
__FileDirectoryBlock___hash->addParent(__UsedBlock___hash);
__InodeTableBlock___hash->addParent(__UsedBlock___hash);
__InodeBitmapBlock___hash->addParent(__UsedBlock___hash);
__BlockBitmapBlock___hash->addParent(__UsedBlock___hash);
__FileInode___hash->addParent(__UsedInode___hash);
__DirectoryInode___hash->addParent(__UsedInode___hash);
__DirectoryBlock___hash->addParent(__FileDirectoryBlock___hash);
__FileBlock___hash->addParent(__FileDirectoryBlock___hash);
__UsedBlock___hash->addParent(__Block___hash);
__FreeBlock___hash->addParent(__Block___hash);
__UsedInode___hash->addParent(__Inode___hash);
__FreeInode___hash->addParent(__Inode___hash);
__RootDirectoryInode___hash->addParent(__DirectoryInode___hash);
SimpleHash* __referencecount___hash = new SimpleHash();
SimpleHash* __filesize___hash = new SimpleHash();
SimpleHash* __inodeof___hashinv = new SimpleHash();
SimpleHash* __contents___hash = new SimpleHash();
SimpleHash* __contents___hashinv = new SimpleHash();
SimpleHash* __inodestatus___hash = new SimpleHash();
SimpleHash* __blockstatus___hash = new SimpleHash();


// build rule1
  {
  //true
  int __tempvar0__ = 1;
  if (__tempvar0__)
    {
    int __element1__ = 0;
    __SuperBlock___hash->add((int)__element1__, (int)__element1__);
    }
  }


// build rule2
  {
  //true
  int __tempvar2__ = 1;
  if (__tempvar2__)
    {
    int __element3__ = 1;
    __GroupBlock___hash->add((int)__element3__, (int)__element3__);
    }
  }


// build rule3
  {
  //(d.g.InodeTableBlock < d.s.NumberofBlocks)
  // __left6__ <-- d.g
  // __left7__ <-- d
  int __left7__ = (int) d; //varexpr
  // __left7__ = d
  // __offsetinbits8__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop9__ = 0;
  int __leftop13__ = 8;
  // __left15__ <-- d.s
  // __left16__ <-- d
  int __left16__ = (int) d; //varexpr
  // __left16__ = d
  int __left15__ = (__left16__ + 0);
  int __leftop18__ = 32;
  int __leftop20__ = 32;
  int __leftop22__ = 32;
  int __leftop24__ = 32;
  int __leftop26__ = 32;
  int __leftop28__ = 32;
  int __rightop29__ = 0;
  int __rightop27__ = __leftop28__ + __rightop29__;
  int __rightop25__ = __leftop26__ + __rightop27__;
  int __rightop23__ = __leftop24__ + __rightop25__;
  int __rightop21__ = __leftop22__ + __rightop23__;
  int __rightop19__ = __leftop20__ + __rightop21__;
  int __sizeof17__ = __leftop18__ + __rightop19__;
  int __high30__ = __left15__ + __sizeof17__;
  assertvalidmemory(__left15__, __high30__);
  // __left15__ = d.s
  // __offsetinbits31__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop32__ = 32;
  int __leftop34__ = 32;
  int __leftop36__ = 32;
  int __leftop38__ = 32;
  int __leftop40__ = 32;
  int __rightop41__ = 0;
  int __rightop39__ = __leftop40__ + __rightop41__;
  int __rightop37__ = __leftop38__ + __rightop39__;
  int __rightop35__ = __leftop36__ + __rightop37__;
  int __rightop33__ = __leftop34__ + __rightop35__;
  int __offsetinbits31__ = __leftop32__ + __rightop33__;
  // __offsetinbits31__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset42__ = __offsetinbits31__ >> 3;
  int __shift43__ = __offsetinbits31__ - (__offset42__ << 3);
  int __rightop14__ = ((*(int *)(__left15__ + __offset42__))  >> __shift43__) & 0xffffffff;
  int __leftop12__ = __leftop13__ * __rightop14__;
  int __rightop44__ = 0;
  int __leftop11__ = __leftop12__ + __rightop44__;
  int __rightop45__ = 1;
  int __rightop10__ = __leftop11__ * __rightop45__;
  int __offsetinbits8__ = __leftop9__ + __rightop10__;
  // __offsetinbits8__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset46__ = __offsetinbits8__ >> 3;
  int __left6__ = (__left7__ + __offset46__);
  int __leftop48__ = 32;
  int __leftop50__ = 32;
  int __leftop52__ = 32;
  int __leftop54__ = 32;
  int __leftop56__ = 32;
  int __rightop57__ = 0;
  int __rightop55__ = __leftop56__ + __rightop57__;
  int __rightop53__ = __leftop54__ + __rightop55__;
  int __rightop51__ = __leftop52__ + __rightop53__;
  int __rightop49__ = __leftop50__ + __rightop51__;
  int __sizeof47__ = __leftop48__ + __rightop49__;
  int __high58__ = __left6__ + __sizeof47__;
  assertvalidmemory(__left6__, __high58__);
  // __left6__ = d.g
  // __offsetinbits59__ <-- (32 + (32 + 0))
  int __leftop60__ = 32;
  int __leftop62__ = 32;
  int __rightop63__ = 0;
  int __rightop61__ = __leftop62__ + __rightop63__;
  int __offsetinbits59__ = __leftop60__ + __rightop61__;
  // __offsetinbits59__ = (32 + (32 + 0))
  int __offset64__ = __offsetinbits59__ >> 3;
  int __shift65__ = __offsetinbits59__ - (__offset64__ << 3);
  int __leftop5__ = ((*(int *)(__left6__ + __offset64__))  >> __shift65__) & 0xffffffff;
  // __left67__ <-- d.s
  // __left68__ <-- d
  int __left68__ = (int) d; //varexpr
  // __left68__ = d
  int __left67__ = (__left68__ + 0);
  int __leftop70__ = 32;
  int __leftop72__ = 32;
  int __leftop74__ = 32;
  int __leftop76__ = 32;
  int __leftop78__ = 32;
  int __leftop80__ = 32;
  int __rightop81__ = 0;
  int __rightop79__ = __leftop80__ + __rightop81__;
  int __rightop77__ = __leftop78__ + __rightop79__;
  int __rightop75__ = __leftop76__ + __rightop77__;
  int __rightop73__ = __leftop74__ + __rightop75__;
  int __rightop71__ = __leftop72__ + __rightop73__;
  int __sizeof69__ = __leftop70__ + __rightop71__;
  int __high82__ = __left67__ + __sizeof69__;
  assertvalidmemory(__left67__, __high82__);
  // __left67__ = d.s
  // __offsetinbits83__ <-- (32 + (32 + 0))
  int __leftop84__ = 32;
  int __leftop86__ = 32;
  int __rightop87__ = 0;
  int __rightop85__ = __leftop86__ + __rightop87__;
  int __offsetinbits83__ = __leftop84__ + __rightop85__;
  // __offsetinbits83__ = (32 + (32 + 0))
  int __offset88__ = __offsetinbits83__ >> 3;
  int __shift89__ = __offsetinbits83__ - (__offset88__ << 3);
  int __rightop66__ = ((*(int *)(__left67__ + __offset88__))  >> __shift89__) & 0xffffffff;
  int __tempvar4__ = __leftop5__ < __rightop66__;
  if (__tempvar4__)
    {
    // __left91__ <-- d.g
    // __left92__ <-- d
    int __left92__ = (int) d; //varexpr
    // __left92__ = d
    // __offsetinbits93__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop94__ = 0;
    int __leftop98__ = 8;
    // __left100__ <-- d.s
    // __left101__ <-- d
    int __left101__ = (int) d; //varexpr
    // __left101__ = d
    int __left100__ = (__left101__ + 0);
    int __leftop103__ = 32;
    int __leftop105__ = 32;
    int __leftop107__ = 32;
    int __leftop109__ = 32;
    int __leftop111__ = 32;
    int __leftop113__ = 32;
    int __rightop114__ = 0;
    int __rightop112__ = __leftop113__ + __rightop114__;
    int __rightop110__ = __leftop111__ + __rightop112__;
    int __rightop108__ = __leftop109__ + __rightop110__;
    int __rightop106__ = __leftop107__ + __rightop108__;
    int __rightop104__ = __leftop105__ + __rightop106__;
    int __sizeof102__ = __leftop103__ + __rightop104__;
    int __high115__ = __left100__ + __sizeof102__;
    assertvalidmemory(__left100__, __high115__);
    // __left100__ = d.s
    // __offsetinbits116__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop117__ = 32;
    int __leftop119__ = 32;
    int __leftop121__ = 32;
    int __leftop123__ = 32;
    int __leftop125__ = 32;
    int __rightop126__ = 0;
    int __rightop124__ = __leftop125__ + __rightop126__;
    int __rightop122__ = __leftop123__ + __rightop124__;
    int __rightop120__ = __leftop121__ + __rightop122__;
    int __rightop118__ = __leftop119__ + __rightop120__;
    int __offsetinbits116__ = __leftop117__ + __rightop118__;
    // __offsetinbits116__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset127__ = __offsetinbits116__ >> 3;
    int __shift128__ = __offsetinbits116__ - (__offset127__ << 3);
    int __rightop99__ = ((*(int *)(__left100__ + __offset127__))  >> __shift128__) & 0xffffffff;
    int __leftop97__ = __leftop98__ * __rightop99__;
    int __rightop129__ = 0;
    int __leftop96__ = __leftop97__ + __rightop129__;
    int __rightop130__ = 1;
    int __rightop95__ = __leftop96__ * __rightop130__;
    int __offsetinbits93__ = __leftop94__ + __rightop95__;
    // __offsetinbits93__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset131__ = __offsetinbits93__ >> 3;
    int __left91__ = (__left92__ + __offset131__);
    int __leftop133__ = 32;
    int __leftop135__ = 32;
    int __leftop137__ = 32;
    int __leftop139__ = 32;
    int __leftop141__ = 32;
    int __rightop142__ = 0;
    int __rightop140__ = __leftop141__ + __rightop142__;
    int __rightop138__ = __leftop139__ + __rightop140__;
    int __rightop136__ = __leftop137__ + __rightop138__;
    int __rightop134__ = __leftop135__ + __rightop136__;
    int __sizeof132__ = __leftop133__ + __rightop134__;
    int __high143__ = __left91__ + __sizeof132__;
    assertvalidmemory(__left91__, __high143__);
    // __left91__ = d.g
    // __offsetinbits144__ <-- (32 + (32 + 0))
    int __leftop145__ = 32;
    int __leftop147__ = 32;
    int __rightop148__ = 0;
    int __rightop146__ = __leftop147__ + __rightop148__;
    int __offsetinbits144__ = __leftop145__ + __rightop146__;
    // __offsetinbits144__ = (32 + (32 + 0))
    int __offset149__ = __offsetinbits144__ >> 3;
    int __shift150__ = __offsetinbits144__ - (__offset149__ << 3);
    int __element90__ = ((*(int *)(__left91__ + __offset149__))  >> __shift150__) & 0xffffffff;
    __InodeTableBlock___hash->add((int)__element90__, (int)__element90__);
    }
  }


// build rule4
  {
  //(d.g.InodeBitmapBlock < d.s.NumberofBlocks)
  // __left153__ <-- d.g
  // __left154__ <-- d
  int __left154__ = (int) d; //varexpr
  // __left154__ = d
  // __offsetinbits155__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop156__ = 0;
  int __leftop160__ = 8;
  // __left162__ <-- d.s
  // __left163__ <-- d
  int __left163__ = (int) d; //varexpr
  // __left163__ = d
  int __left162__ = (__left163__ + 0);
  int __leftop165__ = 32;
  int __leftop167__ = 32;
  int __leftop169__ = 32;
  int __leftop171__ = 32;
  int __leftop173__ = 32;
  int __leftop175__ = 32;
  int __rightop176__ = 0;
  int __rightop174__ = __leftop175__ + __rightop176__;
  int __rightop172__ = __leftop173__ + __rightop174__;
  int __rightop170__ = __leftop171__ + __rightop172__;
  int __rightop168__ = __leftop169__ + __rightop170__;
  int __rightop166__ = __leftop167__ + __rightop168__;
  int __sizeof164__ = __leftop165__ + __rightop166__;
  int __high177__ = __left162__ + __sizeof164__;
  assertvalidmemory(__left162__, __high177__);
  // __left162__ = d.s
  // __offsetinbits178__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop179__ = 32;
  int __leftop181__ = 32;
  int __leftop183__ = 32;
  int __leftop185__ = 32;
  int __leftop187__ = 32;
  int __rightop188__ = 0;
  int __rightop186__ = __leftop187__ + __rightop188__;
  int __rightop184__ = __leftop185__ + __rightop186__;
  int __rightop182__ = __leftop183__ + __rightop184__;
  int __rightop180__ = __leftop181__ + __rightop182__;
  int __offsetinbits178__ = __leftop179__ + __rightop180__;
  // __offsetinbits178__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset189__ = __offsetinbits178__ >> 3;
  int __shift190__ = __offsetinbits178__ - (__offset189__ << 3);
  int __rightop161__ = ((*(int *)(__left162__ + __offset189__))  >> __shift190__) & 0xffffffff;
  int __leftop159__ = __leftop160__ * __rightop161__;
  int __rightop191__ = 0;
  int __leftop158__ = __leftop159__ + __rightop191__;
  int __rightop192__ = 1;
  int __rightop157__ = __leftop158__ * __rightop192__;
  int __offsetinbits155__ = __leftop156__ + __rightop157__;
  // __offsetinbits155__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset193__ = __offsetinbits155__ >> 3;
  int __left153__ = (__left154__ + __offset193__);
  int __leftop195__ = 32;
  int __leftop197__ = 32;
  int __leftop199__ = 32;
  int __leftop201__ = 32;
  int __leftop203__ = 32;
  int __rightop204__ = 0;
  int __rightop202__ = __leftop203__ + __rightop204__;
  int __rightop200__ = __leftop201__ + __rightop202__;
  int __rightop198__ = __leftop199__ + __rightop200__;
  int __rightop196__ = __leftop197__ + __rightop198__;
  int __sizeof194__ = __leftop195__ + __rightop196__;
  int __high205__ = __left153__ + __sizeof194__;
  assertvalidmemory(__left153__, __high205__);
  // __left153__ = d.g
  // __offsetinbits206__ <-- (32 + 0)
  int __leftop207__ = 32;
  int __rightop208__ = 0;
  int __offsetinbits206__ = __leftop207__ + __rightop208__;
  // __offsetinbits206__ = (32 + 0)
  int __offset209__ = __offsetinbits206__ >> 3;
  int __shift210__ = __offsetinbits206__ - (__offset209__ << 3);
  int __leftop152__ = ((*(int *)(__left153__ + __offset209__))  >> __shift210__) & 0xffffffff;
  // __left212__ <-- d.s
  // __left213__ <-- d
  int __left213__ = (int) d; //varexpr
  // __left213__ = d
  int __left212__ = (__left213__ + 0);
  int __leftop215__ = 32;
  int __leftop217__ = 32;
  int __leftop219__ = 32;
  int __leftop221__ = 32;
  int __leftop223__ = 32;
  int __leftop225__ = 32;
  int __rightop226__ = 0;
  int __rightop224__ = __leftop225__ + __rightop226__;
  int __rightop222__ = __leftop223__ + __rightop224__;
  int __rightop220__ = __leftop221__ + __rightop222__;
  int __rightop218__ = __leftop219__ + __rightop220__;
  int __rightop216__ = __leftop217__ + __rightop218__;
  int __sizeof214__ = __leftop215__ + __rightop216__;
  int __high227__ = __left212__ + __sizeof214__;
  assertvalidmemory(__left212__, __high227__);
  // __left212__ = d.s
  // __offsetinbits228__ <-- (32 + (32 + 0))
  int __leftop229__ = 32;
  int __leftop231__ = 32;
  int __rightop232__ = 0;
  int __rightop230__ = __leftop231__ + __rightop232__;
  int __offsetinbits228__ = __leftop229__ + __rightop230__;
  // __offsetinbits228__ = (32 + (32 + 0))
  int __offset233__ = __offsetinbits228__ >> 3;
  int __shift234__ = __offsetinbits228__ - (__offset233__ << 3);
  int __rightop211__ = ((*(int *)(__left212__ + __offset233__))  >> __shift234__) & 0xffffffff;
  int __tempvar151__ = __leftop152__ < __rightop211__;
  if (__tempvar151__)
    {
    // __left236__ <-- d.g
    // __left237__ <-- d
    int __left237__ = (int) d; //varexpr
    // __left237__ = d
    // __offsetinbits238__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop239__ = 0;
    int __leftop243__ = 8;
    // __left245__ <-- d.s
    // __left246__ <-- d
    int __left246__ = (int) d; //varexpr
    // __left246__ = d
    int __left245__ = (__left246__ + 0);
    int __leftop248__ = 32;
    int __leftop250__ = 32;
    int __leftop252__ = 32;
    int __leftop254__ = 32;
    int __leftop256__ = 32;
    int __leftop258__ = 32;
    int __rightop259__ = 0;
    int __rightop257__ = __leftop258__ + __rightop259__;
    int __rightop255__ = __leftop256__ + __rightop257__;
    int __rightop253__ = __leftop254__ + __rightop255__;
    int __rightop251__ = __leftop252__ + __rightop253__;
    int __rightop249__ = __leftop250__ + __rightop251__;
    int __sizeof247__ = __leftop248__ + __rightop249__;
    int __high260__ = __left245__ + __sizeof247__;
    assertvalidmemory(__left245__, __high260__);
    // __left245__ = d.s
    // __offsetinbits261__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop262__ = 32;
    int __leftop264__ = 32;
    int __leftop266__ = 32;
    int __leftop268__ = 32;
    int __leftop270__ = 32;
    int __rightop271__ = 0;
    int __rightop269__ = __leftop270__ + __rightop271__;
    int __rightop267__ = __leftop268__ + __rightop269__;
    int __rightop265__ = __leftop266__ + __rightop267__;
    int __rightop263__ = __leftop264__ + __rightop265__;
    int __offsetinbits261__ = __leftop262__ + __rightop263__;
    // __offsetinbits261__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset272__ = __offsetinbits261__ >> 3;
    int __shift273__ = __offsetinbits261__ - (__offset272__ << 3);
    int __rightop244__ = ((*(int *)(__left245__ + __offset272__))  >> __shift273__) & 0xffffffff;
    int __leftop242__ = __leftop243__ * __rightop244__;
    int __rightop274__ = 0;
    int __leftop241__ = __leftop242__ + __rightop274__;
    int __rightop275__ = 1;
    int __rightop240__ = __leftop241__ * __rightop275__;
    int __offsetinbits238__ = __leftop239__ + __rightop240__;
    // __offsetinbits238__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset276__ = __offsetinbits238__ >> 3;
    int __left236__ = (__left237__ + __offset276__);
    int __leftop278__ = 32;
    int __leftop280__ = 32;
    int __leftop282__ = 32;
    int __leftop284__ = 32;
    int __leftop286__ = 32;
    int __rightop287__ = 0;
    int __rightop285__ = __leftop286__ + __rightop287__;
    int __rightop283__ = __leftop284__ + __rightop285__;
    int __rightop281__ = __leftop282__ + __rightop283__;
    int __rightop279__ = __leftop280__ + __rightop281__;
    int __sizeof277__ = __leftop278__ + __rightop279__;
    int __high288__ = __left236__ + __sizeof277__;
    assertvalidmemory(__left236__, __high288__);
    // __left236__ = d.g
    // __offsetinbits289__ <-- (32 + 0)
    int __leftop290__ = 32;
    int __rightop291__ = 0;
    int __offsetinbits289__ = __leftop290__ + __rightop291__;
    // __offsetinbits289__ = (32 + 0)
    int __offset292__ = __offsetinbits289__ >> 3;
    int __shift293__ = __offsetinbits289__ - (__offset292__ << 3);
    int __element235__ = ((*(int *)(__left236__ + __offset292__))  >> __shift293__) & 0xffffffff;
    __InodeBitmapBlock___hash->add((int)__element235__, (int)__element235__);
    }
  }


// build rule12
  {
  int __tempvar294__ = 0;
  // __left297__ <-- d.s
  // __left298__ <-- d
  int __left298__ = (int) d; //varexpr
  // __left298__ = d
  int __left297__ = (__left298__ + 0);
  int __leftop300__ = 32;
  int __leftop302__ = 32;
  int __leftop304__ = 32;
  int __leftop306__ = 32;
  int __leftop308__ = 32;
  int __leftop310__ = 32;
  int __rightop311__ = 0;
  int __rightop309__ = __leftop310__ + __rightop311__;
  int __rightop307__ = __leftop308__ + __rightop309__;
  int __rightop305__ = __leftop306__ + __rightop307__;
  int __rightop303__ = __leftop304__ + __rightop305__;
  int __rightop301__ = __leftop302__ + __rightop303__;
  int __sizeof299__ = __leftop300__ + __rightop301__;
  int __high312__ = __left297__ + __sizeof299__;
  assertvalidmemory(__left297__, __high312__);
  // __left297__ = d.s
  // __offsetinbits313__ <-- (32 + (32 + (32 + 0)))
  int __leftop314__ = 32;
  int __leftop316__ = 32;
  int __leftop318__ = 32;
  int __rightop319__ = 0;
  int __rightop317__ = __leftop318__ + __rightop319__;
  int __rightop315__ = __leftop316__ + __rightop317__;
  int __offsetinbits313__ = __leftop314__ + __rightop315__;
  // __offsetinbits313__ = (32 + (32 + (32 + 0)))
  int __offset320__ = __offsetinbits313__ >> 3;
  int __shift321__ = __offsetinbits313__ - (__offset320__ << 3);
  int __leftop296__ = ((*(int *)(__left297__ + __offset320__))  >> __shift321__) & 0xffffffff;
  int __rightop322__ = 1;
  int __tempvar295__ = __leftop296__ - __rightop322__;
  for (int __j__ = __tempvar294__; __j__ <= __tempvar295__; __j__++)
    {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); )
      {
      int __ibb__ = (int) __ibb___iterator->next();
      //(cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == false)
      // __left325__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left327__ <-- d
      int __left327__ = (int) d; //varexpr
      // __left327__ = d
      // __offsetinbits328__ <-- (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __leftop329__ = 0;
      int __leftop333__ = 8;
      // __left335__ <-- d.s
      // __left336__ <-- d
      int __left336__ = (int) d; //varexpr
      // __left336__ = d
      int __left335__ = (__left336__ + 0);
      int __leftop338__ = 32;
      int __leftop340__ = 32;
      int __leftop342__ = 32;
      int __leftop344__ = 32;
      int __leftop346__ = 32;
      int __leftop348__ = 32;
      int __rightop349__ = 0;
      int __rightop347__ = __leftop348__ + __rightop349__;
      int __rightop345__ = __leftop346__ + __rightop347__;
      int __rightop343__ = __leftop344__ + __rightop345__;
      int __rightop341__ = __leftop342__ + __rightop343__;
      int __rightop339__ = __leftop340__ + __rightop341__;
      int __sizeof337__ = __leftop338__ + __rightop339__;
      int __high350__ = __left335__ + __sizeof337__;
      assertvalidmemory(__left335__, __high350__);
      // __left335__ = d.s
      // __offsetinbits351__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop352__ = 32;
      int __leftop354__ = 32;
      int __leftop356__ = 32;
      int __leftop358__ = 32;
      int __leftop360__ = 32;
      int __rightop361__ = 0;
      int __rightop359__ = __leftop360__ + __rightop361__;
      int __rightop357__ = __leftop358__ + __rightop359__;
      int __rightop355__ = __leftop356__ + __rightop357__;
      int __rightop353__ = __leftop354__ + __rightop355__;
      int __offsetinbits351__ = __leftop352__ + __rightop353__;
      // __offsetinbits351__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset362__ = __offsetinbits351__ >> 3;
      int __shift363__ = __offsetinbits351__ - (__offset362__ << 3);
      int __rightop334__ = ((*(int *)(__left335__ + __offset362__))  >> __shift363__) & 0xffffffff;
      int __leftop332__ = __leftop333__ * __rightop334__;
      int __rightop364__ = 0;
      int __leftop331__ = __leftop332__ + __rightop364__;
      int __rightop365__ = (int) __ibb__; //varexpr
      int __rightop330__ = __leftop331__ * __rightop365__;
      int __offsetinbits328__ = __leftop329__ + __rightop330__;
      // __offsetinbits328__ = (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __offset366__ = __offsetinbits328__ >> 3;
      int __expr326__ = (__left327__ + __offset366__);
      int __leftop369__ = 8;
      // __left371__ <-- d.s
      // __left372__ <-- d
      int __left372__ = (int) d; //varexpr
      // __left372__ = d
      int __left371__ = (__left372__ + 0);
      int __leftop374__ = 32;
      int __leftop376__ = 32;
      int __leftop378__ = 32;
      int __leftop380__ = 32;
      int __leftop382__ = 32;
      int __leftop384__ = 32;
      int __rightop385__ = 0;
      int __rightop383__ = __leftop384__ + __rightop385__;
      int __rightop381__ = __leftop382__ + __rightop383__;
      int __rightop379__ = __leftop380__ + __rightop381__;
      int __rightop377__ = __leftop378__ + __rightop379__;
      int __rightop375__ = __leftop376__ + __rightop377__;
      int __sizeof373__ = __leftop374__ + __rightop375__;
      int __high386__ = __left371__ + __sizeof373__;
      assertvalidmemory(__left371__, __high386__);
      // __left371__ = d.s
      // __offsetinbits387__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop388__ = 32;
      int __leftop390__ = 32;
      int __leftop392__ = 32;
      int __leftop394__ = 32;
      int __leftop396__ = 32;
      int __rightop397__ = 0;
      int __rightop395__ = __leftop396__ + __rightop397__;
      int __rightop393__ = __leftop394__ + __rightop395__;
      int __rightop391__ = __leftop392__ + __rightop393__;
      int __rightop389__ = __leftop390__ + __rightop391__;
      int __offsetinbits387__ = __leftop388__ + __rightop389__;
      // __offsetinbits387__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset398__ = __offsetinbits387__ >> 3;
      int __shift399__ = __offsetinbits387__ - (__offset398__ << 3);
      int __rightop370__ = ((*(int *)(__left371__ + __offset398__))  >> __shift399__) & 0xffffffff;
      int __leftop368__ = __leftop369__ * __rightop370__;
      int __rightop400__ = 0;
      int __sizeof367__ = __leftop368__ + __rightop400__;
      int __high401__ = __expr326__ + __sizeof367__;
      assertvalidmemory(__expr326__, __high401__);
      int __left325__ = (int) __expr326__;
      // __left325__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits402__ <-- (0 + (1 * j))
      int __leftop403__ = 0;
      int __leftop405__ = 1;
      int __rightop406__ = (int) __j__; //varexpr
      int __rightop404__ = __leftop405__ * __rightop406__;
      int __offsetinbits402__ = __leftop403__ + __rightop404__;
      // __offsetinbits402__ = (0 + (1 * j))
      int __offset407__ = __offsetinbits402__ >> 3;
      int __shift408__ = __offsetinbits402__ - (__offset407__ << 3);
      int __leftop324__ = ((*(int *)(__left325__ + __offset407__))  >> __shift408__) & 0x1;
      int __rightop409__ = 0;
      int __tempvar323__ = __leftop324__ == __rightop409__;
      if (__tempvar323__)
        {
        int __leftele410__ = (int) __j__; //varexpr
        int __rightele411__ = 101;
        __inodestatus___hash->add((int)__leftele410__, (int)__rightele411__);
        }
      }
    }
  }


// build rule5
  {
  //(d.g.BlockBitmapBlock < d.s.NumberofBlocks)
  // __left415__ <-- d.g
  // __left416__ <-- d
  int __left416__ = (int) d; //varexpr
  // __left416__ = d
  // __offsetinbits417__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop418__ = 0;
  int __leftop422__ = 8;
  // __left424__ <-- d.s
  // __left425__ <-- d
  int __left425__ = (int) d; //varexpr
  // __left425__ = d
  int __left424__ = (__left425__ + 0);
  int __leftop427__ = 32;
  int __leftop429__ = 32;
  int __leftop431__ = 32;
  int __leftop433__ = 32;
  int __leftop435__ = 32;
  int __leftop437__ = 32;
  int __rightop438__ = 0;
  int __rightop436__ = __leftop437__ + __rightop438__;
  int __rightop434__ = __leftop435__ + __rightop436__;
  int __rightop432__ = __leftop433__ + __rightop434__;
  int __rightop430__ = __leftop431__ + __rightop432__;
  int __rightop428__ = __leftop429__ + __rightop430__;
  int __sizeof426__ = __leftop427__ + __rightop428__;
  int __high439__ = __left424__ + __sizeof426__;
  assertvalidmemory(__left424__, __high439__);
  // __left424__ = d.s
  // __offsetinbits440__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop441__ = 32;
  int __leftop443__ = 32;
  int __leftop445__ = 32;
  int __leftop447__ = 32;
  int __leftop449__ = 32;
  int __rightop450__ = 0;
  int __rightop448__ = __leftop449__ + __rightop450__;
  int __rightop446__ = __leftop447__ + __rightop448__;
  int __rightop444__ = __leftop445__ + __rightop446__;
  int __rightop442__ = __leftop443__ + __rightop444__;
  int __offsetinbits440__ = __leftop441__ + __rightop442__;
  // __offsetinbits440__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset451__ = __offsetinbits440__ >> 3;
  int __shift452__ = __offsetinbits440__ - (__offset451__ << 3);
  int __rightop423__ = ((*(int *)(__left424__ + __offset451__))  >> __shift452__) & 0xffffffff;
  int __leftop421__ = __leftop422__ * __rightop423__;
  int __rightop453__ = 0;
  int __leftop420__ = __leftop421__ + __rightop453__;
  int __rightop454__ = 1;
  int __rightop419__ = __leftop420__ * __rightop454__;
  int __offsetinbits417__ = __leftop418__ + __rightop419__;
  // __offsetinbits417__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset455__ = __offsetinbits417__ >> 3;
  int __left415__ = (__left416__ + __offset455__);
  int __leftop457__ = 32;
  int __leftop459__ = 32;
  int __leftop461__ = 32;
  int __leftop463__ = 32;
  int __leftop465__ = 32;
  int __rightop466__ = 0;
  int __rightop464__ = __leftop465__ + __rightop466__;
  int __rightop462__ = __leftop463__ + __rightop464__;
  int __rightop460__ = __leftop461__ + __rightop462__;
  int __rightop458__ = __leftop459__ + __rightop460__;
  int __sizeof456__ = __leftop457__ + __rightop458__;
  int __high467__ = __left415__ + __sizeof456__;
  assertvalidmemory(__left415__, __high467__);
  // __left415__ = d.g
  int __leftop414__ = ((*(int *)(__left415__ + 0))  >> 0) & 0xffffffff;
  // __left469__ <-- d.s
  // __left470__ <-- d
  int __left470__ = (int) d; //varexpr
  // __left470__ = d
  int __left469__ = (__left470__ + 0);
  int __leftop472__ = 32;
  int __leftop474__ = 32;
  int __leftop476__ = 32;
  int __leftop478__ = 32;
  int __leftop480__ = 32;
  int __leftop482__ = 32;
  int __rightop483__ = 0;
  int __rightop481__ = __leftop482__ + __rightop483__;
  int __rightop479__ = __leftop480__ + __rightop481__;
  int __rightop477__ = __leftop478__ + __rightop479__;
  int __rightop475__ = __leftop476__ + __rightop477__;
  int __rightop473__ = __leftop474__ + __rightop475__;
  int __sizeof471__ = __leftop472__ + __rightop473__;
  int __high484__ = __left469__ + __sizeof471__;
  assertvalidmemory(__left469__, __high484__);
  // __left469__ = d.s
  // __offsetinbits485__ <-- (32 + (32 + 0))
  int __leftop486__ = 32;
  int __leftop488__ = 32;
  int __rightop489__ = 0;
  int __rightop487__ = __leftop488__ + __rightop489__;
  int __offsetinbits485__ = __leftop486__ + __rightop487__;
  // __offsetinbits485__ = (32 + (32 + 0))
  int __offset490__ = __offsetinbits485__ >> 3;
  int __shift491__ = __offsetinbits485__ - (__offset490__ << 3);
  int __rightop468__ = ((*(int *)(__left469__ + __offset490__))  >> __shift491__) & 0xffffffff;
  int __tempvar413__ = __leftop414__ < __rightop468__;
  if (__tempvar413__)
    {
    // __left493__ <-- d.g
    // __left494__ <-- d
    int __left494__ = (int) d; //varexpr
    // __left494__ = d
    // __offsetinbits495__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop496__ = 0;
    int __leftop500__ = 8;
    // __left502__ <-- d.s
    // __left503__ <-- d
    int __left503__ = (int) d; //varexpr
    // __left503__ = d
    int __left502__ = (__left503__ + 0);
    int __leftop505__ = 32;
    int __leftop507__ = 32;
    int __leftop509__ = 32;
    int __leftop511__ = 32;
    int __leftop513__ = 32;
    int __leftop515__ = 32;
    int __rightop516__ = 0;
    int __rightop514__ = __leftop515__ + __rightop516__;
    int __rightop512__ = __leftop513__ + __rightop514__;
    int __rightop510__ = __leftop511__ + __rightop512__;
    int __rightop508__ = __leftop509__ + __rightop510__;
    int __rightop506__ = __leftop507__ + __rightop508__;
    int __sizeof504__ = __leftop505__ + __rightop506__;
    int __high517__ = __left502__ + __sizeof504__;
    assertvalidmemory(__left502__, __high517__);
    // __left502__ = d.s
    // __offsetinbits518__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop519__ = 32;
    int __leftop521__ = 32;
    int __leftop523__ = 32;
    int __leftop525__ = 32;
    int __leftop527__ = 32;
    int __rightop528__ = 0;
    int __rightop526__ = __leftop527__ + __rightop528__;
    int __rightop524__ = __leftop525__ + __rightop526__;
    int __rightop522__ = __leftop523__ + __rightop524__;
    int __rightop520__ = __leftop521__ + __rightop522__;
    int __offsetinbits518__ = __leftop519__ + __rightop520__;
    // __offsetinbits518__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset529__ = __offsetinbits518__ >> 3;
    int __shift530__ = __offsetinbits518__ - (__offset529__ << 3);
    int __rightop501__ = ((*(int *)(__left502__ + __offset529__))  >> __shift530__) & 0xffffffff;
    int __leftop499__ = __leftop500__ * __rightop501__;
    int __rightop531__ = 0;
    int __leftop498__ = __leftop499__ + __rightop531__;
    int __rightop532__ = 1;
    int __rightop497__ = __leftop498__ * __rightop532__;
    int __offsetinbits495__ = __leftop496__ + __rightop497__;
    // __offsetinbits495__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset533__ = __offsetinbits495__ >> 3;
    int __left493__ = (__left494__ + __offset533__);
    int __leftop535__ = 32;
    int __leftop537__ = 32;
    int __leftop539__ = 32;
    int __leftop541__ = 32;
    int __leftop543__ = 32;
    int __rightop544__ = 0;
    int __rightop542__ = __leftop543__ + __rightop544__;
    int __rightop540__ = __leftop541__ + __rightop542__;
    int __rightop538__ = __leftop539__ + __rightop540__;
    int __rightop536__ = __leftop537__ + __rightop538__;
    int __sizeof534__ = __leftop535__ + __rightop536__;
    int __high545__ = __left493__ + __sizeof534__;
    assertvalidmemory(__left493__, __high545__);
    // __left493__ = d.g
    int __element492__ = ((*(int *)(__left493__ + 0))  >> 0) & 0xffffffff;
    __BlockBitmapBlock___hash->add((int)__element492__, (int)__element492__);
    }
  }


// build rule13
  {
  int __tempvar546__ = 0;
  // __left549__ <-- d.s
  // __left550__ <-- d
  int __left550__ = (int) d; //varexpr
  // __left550__ = d
  int __left549__ = (__left550__ + 0);
  int __leftop552__ = 32;
  int __leftop554__ = 32;
  int __leftop556__ = 32;
  int __leftop558__ = 32;
  int __leftop560__ = 32;
  int __leftop562__ = 32;
  int __rightop563__ = 0;
  int __rightop561__ = __leftop562__ + __rightop563__;
  int __rightop559__ = __leftop560__ + __rightop561__;
  int __rightop557__ = __leftop558__ + __rightop559__;
  int __rightop555__ = __leftop556__ + __rightop557__;
  int __rightop553__ = __leftop554__ + __rightop555__;
  int __sizeof551__ = __leftop552__ + __rightop553__;
  int __high564__ = __left549__ + __sizeof551__;
  assertvalidmemory(__left549__, __high564__);
  // __left549__ = d.s
  // __offsetinbits565__ <-- (32 + (32 + (32 + 0)))
  int __leftop566__ = 32;
  int __leftop568__ = 32;
  int __leftop570__ = 32;
  int __rightop571__ = 0;
  int __rightop569__ = __leftop570__ + __rightop571__;
  int __rightop567__ = __leftop568__ + __rightop569__;
  int __offsetinbits565__ = __leftop566__ + __rightop567__;
  // __offsetinbits565__ = (32 + (32 + (32 + 0)))
  int __offset572__ = __offsetinbits565__ >> 3;
  int __shift573__ = __offsetinbits565__ - (__offset572__ << 3);
  int __leftop548__ = ((*(int *)(__left549__ + __offset572__))  >> __shift573__) & 0xffffffff;
  int __rightop574__ = 1;
  int __tempvar547__ = __leftop548__ - __rightop574__;
  for (int __j__ = __tempvar546__; __j__ <= __tempvar547__; __j__++)
    {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); )
      {
      int __ibb__ = (int) __ibb___iterator->next();
      //(cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == true)
      // __left577__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left579__ <-- d
      int __left579__ = (int) d; //varexpr
      // __left579__ = d
      // __offsetinbits580__ <-- (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __leftop581__ = 0;
      int __leftop585__ = 8;
      // __left587__ <-- d.s
      // __left588__ <-- d
      int __left588__ = (int) d; //varexpr
      // __left588__ = d
      int __left587__ = (__left588__ + 0);
      int __leftop590__ = 32;
      int __leftop592__ = 32;
      int __leftop594__ = 32;
      int __leftop596__ = 32;
      int __leftop598__ = 32;
      int __leftop600__ = 32;
      int __rightop601__ = 0;
      int __rightop599__ = __leftop600__ + __rightop601__;
      int __rightop597__ = __leftop598__ + __rightop599__;
      int __rightop595__ = __leftop596__ + __rightop597__;
      int __rightop593__ = __leftop594__ + __rightop595__;
      int __rightop591__ = __leftop592__ + __rightop593__;
      int __sizeof589__ = __leftop590__ + __rightop591__;
      int __high602__ = __left587__ + __sizeof589__;
      assertvalidmemory(__left587__, __high602__);
      // __left587__ = d.s
      // __offsetinbits603__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop604__ = 32;
      int __leftop606__ = 32;
      int __leftop608__ = 32;
      int __leftop610__ = 32;
      int __leftop612__ = 32;
      int __rightop613__ = 0;
      int __rightop611__ = __leftop612__ + __rightop613__;
      int __rightop609__ = __leftop610__ + __rightop611__;
      int __rightop607__ = __leftop608__ + __rightop609__;
      int __rightop605__ = __leftop606__ + __rightop607__;
      int __offsetinbits603__ = __leftop604__ + __rightop605__;
      // __offsetinbits603__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset614__ = __offsetinbits603__ >> 3;
      int __shift615__ = __offsetinbits603__ - (__offset614__ << 3);
      int __rightop586__ = ((*(int *)(__left587__ + __offset614__))  >> __shift615__) & 0xffffffff;
      int __leftop584__ = __leftop585__ * __rightop586__;
      int __rightop616__ = 0;
      int __leftop583__ = __leftop584__ + __rightop616__;
      int __rightop617__ = (int) __ibb__; //varexpr
      int __rightop582__ = __leftop583__ * __rightop617__;
      int __offsetinbits580__ = __leftop581__ + __rightop582__;
      // __offsetinbits580__ = (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __offset618__ = __offsetinbits580__ >> 3;
      int __expr578__ = (__left579__ + __offset618__);
      int __leftop621__ = 8;
      // __left623__ <-- d.s
      // __left624__ <-- d
      int __left624__ = (int) d; //varexpr
      // __left624__ = d
      int __left623__ = (__left624__ + 0);
      int __leftop626__ = 32;
      int __leftop628__ = 32;
      int __leftop630__ = 32;
      int __leftop632__ = 32;
      int __leftop634__ = 32;
      int __leftop636__ = 32;
      int __rightop637__ = 0;
      int __rightop635__ = __leftop636__ + __rightop637__;
      int __rightop633__ = __leftop634__ + __rightop635__;
      int __rightop631__ = __leftop632__ + __rightop633__;
      int __rightop629__ = __leftop630__ + __rightop631__;
      int __rightop627__ = __leftop628__ + __rightop629__;
      int __sizeof625__ = __leftop626__ + __rightop627__;
      int __high638__ = __left623__ + __sizeof625__;
      assertvalidmemory(__left623__, __high638__);
      // __left623__ = d.s
      // __offsetinbits639__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop640__ = 32;
      int __leftop642__ = 32;
      int __leftop644__ = 32;
      int __leftop646__ = 32;
      int __leftop648__ = 32;
      int __rightop649__ = 0;
      int __rightop647__ = __leftop648__ + __rightop649__;
      int __rightop645__ = __leftop646__ + __rightop647__;
      int __rightop643__ = __leftop644__ + __rightop645__;
      int __rightop641__ = __leftop642__ + __rightop643__;
      int __offsetinbits639__ = __leftop640__ + __rightop641__;
      // __offsetinbits639__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset650__ = __offsetinbits639__ >> 3;
      int __shift651__ = __offsetinbits639__ - (__offset650__ << 3);
      int __rightop622__ = ((*(int *)(__left623__ + __offset650__))  >> __shift651__) & 0xffffffff;
      int __leftop620__ = __leftop621__ * __rightop622__;
      int __rightop652__ = 0;
      int __sizeof619__ = __leftop620__ + __rightop652__;
      int __high653__ = __expr578__ + __sizeof619__;
      assertvalidmemory(__expr578__, __high653__);
      int __left577__ = (int) __expr578__;
      // __left577__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits654__ <-- (0 + (1 * j))
      int __leftop655__ = 0;
      int __leftop657__ = 1;
      int __rightop658__ = (int) __j__; //varexpr
      int __rightop656__ = __leftop657__ * __rightop658__;
      int __offsetinbits654__ = __leftop655__ + __rightop656__;
      // __offsetinbits654__ = (0 + (1 * j))
      int __offset659__ = __offsetinbits654__ >> 3;
      int __shift660__ = __offsetinbits654__ - (__offset659__ << 3);
      int __leftop576__ = ((*(int *)(__left577__ + __offset659__))  >> __shift660__) & 0x1;
      int __rightop661__ = 1;
      int __tempvar575__ = __leftop576__ == __rightop661__;
      if (__tempvar575__)
        {
        int __leftele662__ = (int) __j__; //varexpr
        int __rightele663__ = 100;
        __inodestatus___hash->add((int)__leftele662__, (int)__rightele663__);
        }
      }
    }
  }


// build rule6
  {
  //(d.s.RootDirectoryInode < d.s.NumberofInodes)
  // __left667__ <-- d.s
  // __left668__ <-- d
  int __left668__ = (int) d; //varexpr
  // __left668__ = d
  int __left667__ = (__left668__ + 0);
  int __leftop670__ = 32;
  int __leftop672__ = 32;
  int __leftop674__ = 32;
  int __leftop676__ = 32;
  int __leftop678__ = 32;
  int __leftop680__ = 32;
  int __rightop681__ = 0;
  int __rightop679__ = __leftop680__ + __rightop681__;
  int __rightop677__ = __leftop678__ + __rightop679__;
  int __rightop675__ = __leftop676__ + __rightop677__;
  int __rightop673__ = __leftop674__ + __rightop675__;
  int __rightop671__ = __leftop672__ + __rightop673__;
  int __sizeof669__ = __leftop670__ + __rightop671__;
  int __high682__ = __left667__ + __sizeof669__;
  assertvalidmemory(__left667__, __high682__);
  // __left667__ = d.s
  // __offsetinbits683__ <-- (32 + (32 + (32 + (32 + 0))))
  int __leftop684__ = 32;
  int __leftop686__ = 32;
  int __leftop688__ = 32;
  int __leftop690__ = 32;
  int __rightop691__ = 0;
  int __rightop689__ = __leftop690__ + __rightop691__;
  int __rightop687__ = __leftop688__ + __rightop689__;
  int __rightop685__ = __leftop686__ + __rightop687__;
  int __offsetinbits683__ = __leftop684__ + __rightop685__;
  // __offsetinbits683__ = (32 + (32 + (32 + (32 + 0))))
  int __offset692__ = __offsetinbits683__ >> 3;
  int __shift693__ = __offsetinbits683__ - (__offset692__ << 3);
  int __leftop666__ = ((*(int *)(__left667__ + __offset692__))  >> __shift693__) & 0xffffffff;
  // __left695__ <-- d.s
  // __left696__ <-- d
  int __left696__ = (int) d; //varexpr
  // __left696__ = d
  int __left695__ = (__left696__ + 0);
  int __leftop698__ = 32;
  int __leftop700__ = 32;
  int __leftop702__ = 32;
  int __leftop704__ = 32;
  int __leftop706__ = 32;
  int __leftop708__ = 32;
  int __rightop709__ = 0;
  int __rightop707__ = __leftop708__ + __rightop709__;
  int __rightop705__ = __leftop706__ + __rightop707__;
  int __rightop703__ = __leftop704__ + __rightop705__;
  int __rightop701__ = __leftop702__ + __rightop703__;
  int __rightop699__ = __leftop700__ + __rightop701__;
  int __sizeof697__ = __leftop698__ + __rightop699__;
  int __high710__ = __left695__ + __sizeof697__;
  assertvalidmemory(__left695__, __high710__);
  // __left695__ = d.s
  // __offsetinbits711__ <-- (32 + (32 + (32 + 0)))
  int __leftop712__ = 32;
  int __leftop714__ = 32;
  int __leftop716__ = 32;
  int __rightop717__ = 0;
  int __rightop715__ = __leftop716__ + __rightop717__;
  int __rightop713__ = __leftop714__ + __rightop715__;
  int __offsetinbits711__ = __leftop712__ + __rightop713__;
  // __offsetinbits711__ = (32 + (32 + (32 + 0)))
  int __offset718__ = __offsetinbits711__ >> 3;
  int __shift719__ = __offsetinbits711__ - (__offset718__ << 3);
  int __rightop694__ = ((*(int *)(__left695__ + __offset718__))  >> __shift719__) & 0xffffffff;
  int __tempvar665__ = __leftop666__ < __rightop694__;
  if (__tempvar665__)
    {
    // __left721__ <-- d.s
    // __left722__ <-- d
    int __left722__ = (int) d; //varexpr
    // __left722__ = d
    int __left721__ = (__left722__ + 0);
    int __leftop724__ = 32;
    int __leftop726__ = 32;
    int __leftop728__ = 32;
    int __leftop730__ = 32;
    int __leftop732__ = 32;
    int __leftop734__ = 32;
    int __rightop735__ = 0;
    int __rightop733__ = __leftop734__ + __rightop735__;
    int __rightop731__ = __leftop732__ + __rightop733__;
    int __rightop729__ = __leftop730__ + __rightop731__;
    int __rightop727__ = __leftop728__ + __rightop729__;
    int __rightop725__ = __leftop726__ + __rightop727__;
    int __sizeof723__ = __leftop724__ + __rightop725__;
    int __high736__ = __left721__ + __sizeof723__;
    assertvalidmemory(__left721__, __high736__);
    // __left721__ = d.s
    // __offsetinbits737__ <-- (32 + (32 + (32 + (32 + 0))))
    int __leftop738__ = 32;
    int __leftop740__ = 32;
    int __leftop742__ = 32;
    int __leftop744__ = 32;
    int __rightop745__ = 0;
    int __rightop743__ = __leftop744__ + __rightop745__;
    int __rightop741__ = __leftop742__ + __rightop743__;
    int __rightop739__ = __leftop740__ + __rightop741__;
    int __offsetinbits737__ = __leftop738__ + __rightop739__;
    // __offsetinbits737__ = (32 + (32 + (32 + (32 + 0))))
    int __offset746__ = __offsetinbits737__ >> 3;
    int __shift747__ = __offsetinbits737__ - (__offset746__ << 3);
    int __element720__ = ((*(int *)(__left721__ + __offset746__))  >> __shift747__) & 0xffffffff;
    __RootDirectoryInode___hash->add((int)__element720__, (int)__element720__);
    }
  }


// build rule9
  {
  for (SimpleIterator* __di___iterator = __DirectoryInode___hash->iterator(); __di___iterator->hasNext(); )
    {
    int __di__ = (int) __di___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); )
      {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar748__ = 0;
      // __left752__ <-- d.s
      // __left753__ <-- d
      int __left753__ = (int) d; //varexpr
      // __left753__ = d
      int __left752__ = (__left753__ + 0);
      int __leftop755__ = 32;
      int __leftop757__ = 32;
      int __leftop759__ = 32;
      int __leftop761__ = 32;
      int __leftop763__ = 32;
      int __leftop765__ = 32;
      int __rightop766__ = 0;
      int __rightop764__ = __leftop765__ + __rightop766__;
      int __rightop762__ = __leftop763__ + __rightop764__;
      int __rightop760__ = __leftop761__ + __rightop762__;
      int __rightop758__ = __leftop759__ + __rightop760__;
      int __rightop756__ = __leftop757__ + __rightop758__;
      int __sizeof754__ = __leftop755__ + __rightop756__;
      int __high767__ = __left752__ + __sizeof754__;
      assertvalidmemory(__left752__, __high767__);
      // __left752__ = d.s
      // __offsetinbits768__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop769__ = 32;
      int __leftop771__ = 32;
      int __leftop773__ = 32;
      int __leftop775__ = 32;
      int __leftop777__ = 32;
      int __rightop778__ = 0;
      int __rightop776__ = __leftop777__ + __rightop778__;
      int __rightop774__ = __leftop775__ + __rightop776__;
      int __rightop772__ = __leftop773__ + __rightop774__;
      int __rightop770__ = __leftop771__ + __rightop772__;
      int __offsetinbits768__ = __leftop769__ + __rightop770__;
      // __offsetinbits768__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset779__ = __offsetinbits768__ >> 3;
      int __shift780__ = __offsetinbits768__ - (__offset779__ << 3);
      int __leftop751__ = ((*(int *)(__left752__ + __offset779__))  >> __shift780__) & 0xffffffff;
      int __rightop781__ = 128;
      int __leftop750__ = __leftop751__ / __rightop781__;
      int __rightop782__ = 1;
      int __tempvar749__ = __leftop750__ - __rightop782__;
      for (int __j__ = __tempvar748__; __j__ <= __tempvar749__; __j__++)
        {
        int __tempvar783__ = 0;
        int __tempvar784__ = 11;
        for (int __k__ = __tempvar783__; __k__ <= __tempvar784__; __k__++)
          {
          //(cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k] < d.s.NumberofBlocks)
          // __left787__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
          // __left788__ <-- cast(__InodeTable__, d.b[itb])
          // __left790__ <-- d
          int __left790__ = (int) d; //varexpr
          // __left790__ = d
          // __offsetinbits791__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop792__ = 0;
          int __leftop796__ = 8;
          // __left798__ <-- d.s
          // __left799__ <-- d
          int __left799__ = (int) d; //varexpr
          // __left799__ = d
          int __left798__ = (__left799__ + 0);
          int __leftop801__ = 32;
          int __leftop803__ = 32;
          int __leftop805__ = 32;
          int __leftop807__ = 32;
          int __leftop809__ = 32;
          int __leftop811__ = 32;
          int __rightop812__ = 0;
          int __rightop810__ = __leftop811__ + __rightop812__;
          int __rightop808__ = __leftop809__ + __rightop810__;
          int __rightop806__ = __leftop807__ + __rightop808__;
          int __rightop804__ = __leftop805__ + __rightop806__;
          int __rightop802__ = __leftop803__ + __rightop804__;
          int __sizeof800__ = __leftop801__ + __rightop802__;
          int __high813__ = __left798__ + __sizeof800__;
          assertvalidmemory(__left798__, __high813__);
          // __left798__ = d.s
          // __offsetinbits814__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop815__ = 32;
          int __leftop817__ = 32;
          int __leftop819__ = 32;
          int __leftop821__ = 32;
          int __leftop823__ = 32;
          int __rightop824__ = 0;
          int __rightop822__ = __leftop823__ + __rightop824__;
          int __rightop820__ = __leftop821__ + __rightop822__;
          int __rightop818__ = __leftop819__ + __rightop820__;
          int __rightop816__ = __leftop817__ + __rightop818__;
          int __offsetinbits814__ = __leftop815__ + __rightop816__;
          // __offsetinbits814__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset825__ = __offsetinbits814__ >> 3;
          int __shift826__ = __offsetinbits814__ - (__offset825__ << 3);
          int __rightop797__ = ((*(int *)(__left798__ + __offset825__))  >> __shift826__) & 0xffffffff;
          int __leftop795__ = __leftop796__ * __rightop797__;
          int __rightop827__ = 0;
          int __leftop794__ = __leftop795__ + __rightop827__;
          int __rightop828__ = (int) __itb__; //varexpr
          int __rightop793__ = __leftop794__ * __rightop828__;
          int __offsetinbits791__ = __leftop792__ + __rightop793__;
          // __offsetinbits791__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset829__ = __offsetinbits791__ >> 3;
          int __expr789__ = (__left790__ + __offset829__);
          int __leftop832__ = 8;
          // __left834__ <-- d.s
          // __left835__ <-- d
          int __left835__ = (int) d; //varexpr
          // __left835__ = d
          int __left834__ = (__left835__ + 0);
          int __leftop837__ = 32;
          int __leftop839__ = 32;
          int __leftop841__ = 32;
          int __leftop843__ = 32;
          int __leftop845__ = 32;
          int __leftop847__ = 32;
          int __rightop848__ = 0;
          int __rightop846__ = __leftop847__ + __rightop848__;
          int __rightop844__ = __leftop845__ + __rightop846__;
          int __rightop842__ = __leftop843__ + __rightop844__;
          int __rightop840__ = __leftop841__ + __rightop842__;
          int __rightop838__ = __leftop839__ + __rightop840__;
          int __sizeof836__ = __leftop837__ + __rightop838__;
          int __high849__ = __left834__ + __sizeof836__;
          assertvalidmemory(__left834__, __high849__);
          // __left834__ = d.s
          // __offsetinbits850__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop851__ = 32;
          int __leftop853__ = 32;
          int __leftop855__ = 32;
          int __leftop857__ = 32;
          int __leftop859__ = 32;
          int __rightop860__ = 0;
          int __rightop858__ = __leftop859__ + __rightop860__;
          int __rightop856__ = __leftop857__ + __rightop858__;
          int __rightop854__ = __leftop855__ + __rightop856__;
          int __rightop852__ = __leftop853__ + __rightop854__;
          int __offsetinbits850__ = __leftop851__ + __rightop852__;
          // __offsetinbits850__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset861__ = __offsetinbits850__ >> 3;
          int __shift862__ = __offsetinbits850__ - (__offset861__ << 3);
          int __rightop833__ = ((*(int *)(__left834__ + __offset861__))  >> __shift862__) & 0xffffffff;
          int __leftop831__ = __leftop832__ * __rightop833__;
          int __rightop863__ = 0;
          int __sizeof830__ = __leftop831__ + __rightop863__;
          int __high864__ = __expr789__ + __sizeof830__;
          assertvalidmemory(__expr789__, __high864__);
          int __left788__ = (int) __expr789__;
          // __left788__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits865__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
          int __leftop866__ = 0;
          int __leftop869__ = 32;
          int __leftop872__ = 32;
          int __rightop873__ = 12;
          int __leftop871__ = __leftop872__ * __rightop873__;
          int __leftop875__ = 32;
          int __rightop876__ = 0;
          int __rightop874__ = __leftop875__ + __rightop876__;
          int __rightop870__ = __leftop871__ + __rightop874__;
          int __leftop868__ = __leftop869__ + __rightop870__;
          int __rightop877__ = (int) __di__; //varexpr
          int __rightop867__ = __leftop868__ * __rightop877__;
          int __offsetinbits865__ = __leftop866__ + __rightop867__;
          // __offsetinbits865__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
          int __offset878__ = __offsetinbits865__ >> 3;
          int __left787__ = (__left788__ + __offset878__);
          int __leftop880__ = 32;
          int __leftop883__ = 32;
          int __rightop884__ = 12;
          int __leftop882__ = __leftop883__ * __rightop884__;
          int __leftop886__ = 32;
          int __rightop887__ = 0;
          int __rightop885__ = __leftop886__ + __rightop887__;
          int __rightop881__ = __leftop882__ + __rightop885__;
          int __sizeof879__ = __leftop880__ + __rightop881__;
          int __high888__ = __left787__ + __sizeof879__;
          assertvalidmemory(__left787__, __high888__);
          // __left787__ = cast(__InodeTable__, d.b[itb]).itable[di]
          // __offsetinbits889__ <-- ((32 + 0) + (32 * k))
          int __leftop891__ = 32;
          int __rightop892__ = 0;
          int __leftop890__ = __leftop891__ + __rightop892__;
          int __leftop894__ = 32;
          int __rightop895__ = (int) __k__; //varexpr
          int __rightop893__ = __leftop894__ * __rightop895__;
          int __offsetinbits889__ = __leftop890__ + __rightop893__;
          // __offsetinbits889__ = ((32 + 0) + (32 * k))
          int __offset896__ = __offsetinbits889__ >> 3;
          int __shift897__ = __offsetinbits889__ - (__offset896__ << 3);
          int __leftop786__ = ((*(int *)(__left787__ + __offset896__))  >> __shift897__) & 0xffffffff;
          // __left899__ <-- d.s
          // __left900__ <-- d
          int __left900__ = (int) d; //varexpr
          // __left900__ = d
          int __left899__ = (__left900__ + 0);
          int __leftop902__ = 32;
          int __leftop904__ = 32;
          int __leftop906__ = 32;
          int __leftop908__ = 32;
          int __leftop910__ = 32;
          int __leftop912__ = 32;
          int __rightop913__ = 0;
          int __rightop911__ = __leftop912__ + __rightop913__;
          int __rightop909__ = __leftop910__ + __rightop911__;
          int __rightop907__ = __leftop908__ + __rightop909__;
          int __rightop905__ = __leftop906__ + __rightop907__;
          int __rightop903__ = __leftop904__ + __rightop905__;
          int __sizeof901__ = __leftop902__ + __rightop903__;
          int __high914__ = __left899__ + __sizeof901__;
          assertvalidmemory(__left899__, __high914__);
          // __left899__ = d.s
          // __offsetinbits915__ <-- (32 + (32 + 0))
          int __leftop916__ = 32;
          int __leftop918__ = 32;
          int __rightop919__ = 0;
          int __rightop917__ = __leftop918__ + __rightop919__;
          int __offsetinbits915__ = __leftop916__ + __rightop917__;
          // __offsetinbits915__ = (32 + (32 + 0))
          int __offset920__ = __offsetinbits915__ >> 3;
          int __shift921__ = __offsetinbits915__ - (__offset920__ << 3);
          int __rightop898__ = ((*(int *)(__left899__ + __offset920__))  >> __shift921__) & 0xffffffff;
          int __tempvar785__ = __leftop786__ < __rightop898__;
          if (__tempvar785__)
            {
            // __left923__ <-- cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __left925__ <-- d
            int __left925__ = (int) d; //varexpr
            // __left925__ = d
            // __offsetinbits926__ <-- (0 + (((8 * d.s.blocksize) + 0) * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]))
            int __leftop927__ = 0;
            int __leftop931__ = 8;
            // __left933__ <-- d.s
            // __left934__ <-- d
            int __left934__ = (int) d; //varexpr
            // __left934__ = d
            int __left933__ = (__left934__ + 0);
            int __leftop936__ = 32;
            int __leftop938__ = 32;
            int __leftop940__ = 32;
            int __leftop942__ = 32;
            int __leftop944__ = 32;
            int __leftop946__ = 32;
            int __rightop947__ = 0;
            int __rightop945__ = __leftop946__ + __rightop947__;
            int __rightop943__ = __leftop944__ + __rightop945__;
            int __rightop941__ = __leftop942__ + __rightop943__;
            int __rightop939__ = __leftop940__ + __rightop941__;
            int __rightop937__ = __leftop938__ + __rightop939__;
            int __sizeof935__ = __leftop936__ + __rightop937__;
            int __high948__ = __left933__ + __sizeof935__;
            assertvalidmemory(__left933__, __high948__);
            // __left933__ = d.s
            // __offsetinbits949__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop950__ = 32;
            int __leftop952__ = 32;
            int __leftop954__ = 32;
            int __leftop956__ = 32;
            int __leftop958__ = 32;
            int __rightop959__ = 0;
            int __rightop957__ = __leftop958__ + __rightop959__;
            int __rightop955__ = __leftop956__ + __rightop957__;
            int __rightop953__ = __leftop954__ + __rightop955__;
            int __rightop951__ = __leftop952__ + __rightop953__;
            int __offsetinbits949__ = __leftop950__ + __rightop951__;
            // __offsetinbits949__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset960__ = __offsetinbits949__ >> 3;
            int __shift961__ = __offsetinbits949__ - (__offset960__ << 3);
            int __rightop932__ = ((*(int *)(__left933__ + __offset960__))  >> __shift961__) & 0xffffffff;
            int __leftop930__ = __leftop931__ * __rightop932__;
            int __rightop962__ = 0;
            int __leftop929__ = __leftop930__ + __rightop962__;
            // __left964__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
            // __left965__ <-- cast(__InodeTable__, d.b[itb])
            // __left967__ <-- d
            int __left967__ = (int) d; //varexpr
            // __left967__ = d
            // __offsetinbits968__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
            int __leftop969__ = 0;
            int __leftop973__ = 8;
            // __left975__ <-- d.s
            // __left976__ <-- d
            int __left976__ = (int) d; //varexpr
            // __left976__ = d
            int __left975__ = (__left976__ + 0);
            int __leftop978__ = 32;
            int __leftop980__ = 32;
            int __leftop982__ = 32;
            int __leftop984__ = 32;
            int __leftop986__ = 32;
            int __leftop988__ = 32;
            int __rightop989__ = 0;
            int __rightop987__ = __leftop988__ + __rightop989__;
            int __rightop985__ = __leftop986__ + __rightop987__;
            int __rightop983__ = __leftop984__ + __rightop985__;
            int __rightop981__ = __leftop982__ + __rightop983__;
            int __rightop979__ = __leftop980__ + __rightop981__;
            int __sizeof977__ = __leftop978__ + __rightop979__;
            int __high990__ = __left975__ + __sizeof977__;
            assertvalidmemory(__left975__, __high990__);
            // __left975__ = d.s
            // __offsetinbits991__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop992__ = 32;
            int __leftop994__ = 32;
            int __leftop996__ = 32;
            int __leftop998__ = 32;
            int __leftop1000__ = 32;
            int __rightop1001__ = 0;
            int __rightop999__ = __leftop1000__ + __rightop1001__;
            int __rightop997__ = __leftop998__ + __rightop999__;
            int __rightop995__ = __leftop996__ + __rightop997__;
            int __rightop993__ = __leftop994__ + __rightop995__;
            int __offsetinbits991__ = __leftop992__ + __rightop993__;
            // __offsetinbits991__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset1002__ = __offsetinbits991__ >> 3;
            int __shift1003__ = __offsetinbits991__ - (__offset1002__ << 3);
            int __rightop974__ = ((*(int *)(__left975__ + __offset1002__))  >> __shift1003__) & 0xffffffff;
            int __leftop972__ = __leftop973__ * __rightop974__;
            int __rightop1004__ = 0;
            int __leftop971__ = __leftop972__ + __rightop1004__;
            int __rightop1005__ = (int) __itb__; //varexpr
            int __rightop970__ = __leftop971__ * __rightop1005__;
            int __offsetinbits968__ = __leftop969__ + __rightop970__;
            // __offsetinbits968__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
            int __offset1006__ = __offsetinbits968__ >> 3;
            int __expr966__ = (__left967__ + __offset1006__);
            int __leftop1009__ = 8;
            // __left1011__ <-- d.s
            // __left1012__ <-- d
            int __left1012__ = (int) d; //varexpr
            // __left1012__ = d
            int __left1011__ = (__left1012__ + 0);
            int __leftop1014__ = 32;
            int __leftop1016__ = 32;
            int __leftop1018__ = 32;
            int __leftop1020__ = 32;
            int __leftop1022__ = 32;
            int __leftop1024__ = 32;
            int __rightop1025__ = 0;
            int __rightop1023__ = __leftop1024__ + __rightop1025__;
            int __rightop1021__ = __leftop1022__ + __rightop1023__;
            int __rightop1019__ = __leftop1020__ + __rightop1021__;
            int __rightop1017__ = __leftop1018__ + __rightop1019__;
            int __rightop1015__ = __leftop1016__ + __rightop1017__;
            int __sizeof1013__ = __leftop1014__ + __rightop1015__;
            int __high1026__ = __left1011__ + __sizeof1013__;
            assertvalidmemory(__left1011__, __high1026__);
            // __left1011__ = d.s
            // __offsetinbits1027__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop1028__ = 32;
            int __leftop1030__ = 32;
            int __leftop1032__ = 32;
            int __leftop1034__ = 32;
            int __leftop1036__ = 32;
            int __rightop1037__ = 0;
            int __rightop1035__ = __leftop1036__ + __rightop1037__;
            int __rightop1033__ = __leftop1034__ + __rightop1035__;
            int __rightop1031__ = __leftop1032__ + __rightop1033__;
            int __rightop1029__ = __leftop1030__ + __rightop1031__;
            int __offsetinbits1027__ = __leftop1028__ + __rightop1029__;
            // __offsetinbits1027__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset1038__ = __offsetinbits1027__ >> 3;
            int __shift1039__ = __offsetinbits1027__ - (__offset1038__ << 3);
            int __rightop1010__ = ((*(int *)(__left1011__ + __offset1038__))  >> __shift1039__) & 0xffffffff;
            int __leftop1008__ = __leftop1009__ * __rightop1010__;
            int __rightop1040__ = 0;
            int __sizeof1007__ = __leftop1008__ + __rightop1040__;
            int __high1041__ = __expr966__ + __sizeof1007__;
            assertvalidmemory(__expr966__, __high1041__);
            int __left965__ = (int) __expr966__;
            // __left965__ = cast(__InodeTable__, d.b[itb])
            // __offsetinbits1042__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
            int __leftop1043__ = 0;
            int __leftop1046__ = 32;
            int __leftop1049__ = 32;
            int __rightop1050__ = 12;
            int __leftop1048__ = __leftop1049__ * __rightop1050__;
            int __leftop1052__ = 32;
            int __rightop1053__ = 0;
            int __rightop1051__ = __leftop1052__ + __rightop1053__;
            int __rightop1047__ = __leftop1048__ + __rightop1051__;
            int __leftop1045__ = __leftop1046__ + __rightop1047__;
            int __rightop1054__ = (int) __di__; //varexpr
            int __rightop1044__ = __leftop1045__ * __rightop1054__;
            int __offsetinbits1042__ = __leftop1043__ + __rightop1044__;
            // __offsetinbits1042__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
            int __offset1055__ = __offsetinbits1042__ >> 3;
            int __left964__ = (__left965__ + __offset1055__);
            int __leftop1057__ = 32;
            int __leftop1060__ = 32;
            int __rightop1061__ = 12;
            int __leftop1059__ = __leftop1060__ * __rightop1061__;
            int __leftop1063__ = 32;
            int __rightop1064__ = 0;
            int __rightop1062__ = __leftop1063__ + __rightop1064__;
            int __rightop1058__ = __leftop1059__ + __rightop1062__;
            int __sizeof1056__ = __leftop1057__ + __rightop1058__;
            int __high1065__ = __left964__ + __sizeof1056__;
            assertvalidmemory(__left964__, __high1065__);
            // __left964__ = cast(__InodeTable__, d.b[itb]).itable[di]
            // __offsetinbits1066__ <-- ((32 + 0) + (32 * k))
            int __leftop1068__ = 32;
            int __rightop1069__ = 0;
            int __leftop1067__ = __leftop1068__ + __rightop1069__;
            int __leftop1071__ = 32;
            int __rightop1072__ = (int) __k__; //varexpr
            int __rightop1070__ = __leftop1071__ * __rightop1072__;
            int __offsetinbits1066__ = __leftop1067__ + __rightop1070__;
            // __offsetinbits1066__ = ((32 + 0) + (32 * k))
            int __offset1073__ = __offsetinbits1066__ >> 3;
            int __shift1074__ = __offsetinbits1066__ - (__offset1073__ << 3);
            int __rightop963__ = ((*(int *)(__left964__ + __offset1073__))  >> __shift1074__) & 0xffffffff;
            int __rightop928__ = __leftop929__ * __rightop963__;
            int __offsetinbits926__ = __leftop927__ + __rightop928__;
            // __offsetinbits926__ = (0 + (((8 * d.s.blocksize) + 0) * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]))
            int __offset1075__ = __offsetinbits926__ >> 3;
            int __expr924__ = (__left925__ + __offset1075__);
            int __leftop1078__ = 8;
            // __left1080__ <-- d.s
            // __left1081__ <-- d
            int __left1081__ = (int) d; //varexpr
            // __left1081__ = d
            int __left1080__ = (__left1081__ + 0);
            int __leftop1083__ = 32;
            int __leftop1085__ = 32;
            int __leftop1087__ = 32;
            int __leftop1089__ = 32;
            int __leftop1091__ = 32;
            int __leftop1093__ = 32;
            int __rightop1094__ = 0;
            int __rightop1092__ = __leftop1093__ + __rightop1094__;
            int __rightop1090__ = __leftop1091__ + __rightop1092__;
            int __rightop1088__ = __leftop1089__ + __rightop1090__;
            int __rightop1086__ = __leftop1087__ + __rightop1088__;
            int __rightop1084__ = __leftop1085__ + __rightop1086__;
            int __sizeof1082__ = __leftop1083__ + __rightop1084__;
            int __high1095__ = __left1080__ + __sizeof1082__;
            assertvalidmemory(__left1080__, __high1095__);
            // __left1080__ = d.s
            // __offsetinbits1096__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop1097__ = 32;
            int __leftop1099__ = 32;
            int __leftop1101__ = 32;
            int __leftop1103__ = 32;
            int __leftop1105__ = 32;
            int __rightop1106__ = 0;
            int __rightop1104__ = __leftop1105__ + __rightop1106__;
            int __rightop1102__ = __leftop1103__ + __rightop1104__;
            int __rightop1100__ = __leftop1101__ + __rightop1102__;
            int __rightop1098__ = __leftop1099__ + __rightop1100__;
            int __offsetinbits1096__ = __leftop1097__ + __rightop1098__;
            // __offsetinbits1096__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset1107__ = __offsetinbits1096__ >> 3;
            int __shift1108__ = __offsetinbits1096__ - (__offset1107__ << 3);
            int __rightop1079__ = ((*(int *)(__left1080__ + __offset1107__))  >> __shift1108__) & 0xffffffff;
            int __leftop1077__ = __leftop1078__ * __rightop1079__;
            int __rightop1109__ = 0;
            int __sizeof1076__ = __leftop1077__ + __rightop1109__;
            int __high1110__ = __expr924__ + __sizeof1076__;
            assertvalidmemory(__expr924__, __high1110__);
            int __left923__ = (int) __expr924__;
            // __left923__ = cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __offsetinbits1111__ <-- (0 + ((32 + ((8 * 124) + 0)) * j))
            int __leftop1112__ = 0;
            int __leftop1115__ = 32;
            int __leftop1118__ = 8;
            int __rightop1119__ = 124;
            int __leftop1117__ = __leftop1118__ * __rightop1119__;
            int __rightop1120__ = 0;
            int __rightop1116__ = __leftop1117__ + __rightop1120__;
            int __leftop1114__ = __leftop1115__ + __rightop1116__;
            int __rightop1121__ = (int) __j__; //varexpr
            int __rightop1113__ = __leftop1114__ * __rightop1121__;
            int __offsetinbits1111__ = __leftop1112__ + __rightop1113__;
            // __offsetinbits1111__ = (0 + ((32 + ((8 * 124) + 0)) * j))
            int __offset1122__ = __offsetinbits1111__ >> 3;
            int __element922__ = (__left923__ + __offset1122__);
            int __leftop1124__ = 32;
            int __leftop1127__ = 8;
            int __rightop1128__ = 124;
            int __leftop1126__ = __leftop1127__ * __rightop1128__;
            int __rightop1129__ = 0;
            int __rightop1125__ = __leftop1126__ + __rightop1129__;
            int __sizeof1123__ = __leftop1124__ + __rightop1125__;
            int __high1130__ = __element922__ + __sizeof1123__;
            assertvalidmemory(__element922__, __high1130__);
            __DirectoryEntry___hash->add((int)__element922__, (int)__element922__);
            }
          }
        }
      }
    }
  }


// build rule15
  {
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); )
    {
    int __de__ = (int) __de___iterator->next();
    //(de.inodenumber < d.s.NumberofInodes)
    // __left1133__ <-- de
    int __left1133__ = (int) __de__; //varexpr
    // __left1133__ = de
    // __offsetinbits1134__ <-- ((8 * 124) + 0)
    int __leftop1136__ = 8;
    int __rightop1137__ = 124;
    int __leftop1135__ = __leftop1136__ * __rightop1137__;
    int __rightop1138__ = 0;
    int __offsetinbits1134__ = __leftop1135__ + __rightop1138__;
    // __offsetinbits1134__ = ((8 * 124) + 0)
    int __offset1139__ = __offsetinbits1134__ >> 3;
    int __shift1140__ = __offsetinbits1134__ - (__offset1139__ << 3);
    int __leftop1132__ = ((*(int *)(__left1133__ + __offset1139__))  >> __shift1140__) & 0xffffffff;
    // __left1142__ <-- d.s
    // __left1143__ <-- d
    int __left1143__ = (int) d; //varexpr
    // __left1143__ = d
    int __left1142__ = (__left1143__ + 0);
    int __leftop1145__ = 32;
    int __leftop1147__ = 32;
    int __leftop1149__ = 32;
    int __leftop1151__ = 32;
    int __leftop1153__ = 32;
    int __leftop1155__ = 32;
    int __rightop1156__ = 0;
    int __rightop1154__ = __leftop1155__ + __rightop1156__;
    int __rightop1152__ = __leftop1153__ + __rightop1154__;
    int __rightop1150__ = __leftop1151__ + __rightop1152__;
    int __rightop1148__ = __leftop1149__ + __rightop1150__;
    int __rightop1146__ = __leftop1147__ + __rightop1148__;
    int __sizeof1144__ = __leftop1145__ + __rightop1146__;
    int __high1157__ = __left1142__ + __sizeof1144__;
    assertvalidmemory(__left1142__, __high1157__);
    // __left1142__ = d.s
    // __offsetinbits1158__ <-- (32 + (32 + (32 + 0)))
    int __leftop1159__ = 32;
    int __leftop1161__ = 32;
    int __leftop1163__ = 32;
    int __rightop1164__ = 0;
    int __rightop1162__ = __leftop1163__ + __rightop1164__;
    int __rightop1160__ = __leftop1161__ + __rightop1162__;
    int __offsetinbits1158__ = __leftop1159__ + __rightop1160__;
    // __offsetinbits1158__ = (32 + (32 + (32 + 0)))
    int __offset1165__ = __offsetinbits1158__ >> 3;
    int __shift1166__ = __offsetinbits1158__ - (__offset1165__ << 3);
    int __rightop1141__ = ((*(int *)(__left1142__ + __offset1165__))  >> __shift1166__) & 0xffffffff;
    int __tempvar1131__ = __leftop1132__ < __rightop1141__;
    if (__tempvar1131__)
      {
      int __leftele1167__ = (int) __de__; //varexpr
      // __left1169__ <-- de
      int __left1169__ = (int) __de__; //varexpr
      // __left1169__ = de
      // __offsetinbits1170__ <-- ((8 * 124) + 0)
      int __leftop1172__ = 8;
      int __rightop1173__ = 124;
      int __leftop1171__ = __leftop1172__ * __rightop1173__;
      int __rightop1174__ = 0;
      int __offsetinbits1170__ = __leftop1171__ + __rightop1174__;
      // __offsetinbits1170__ = ((8 * 124) + 0)
      int __offset1175__ = __offsetinbits1170__ >> 3;
      int __shift1176__ = __offsetinbits1170__ - (__offset1175__ << 3);
      int __rightele1168__ = ((*(int *)(__left1169__ + __offset1175__))  >> __shift1176__) & 0xffffffff;
      __inodeof___hashinv->add((int)__rightele1168__, (int)__leftele1167__);
      }
    }
  }


// build rule14
  {
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); )
    {
    int __de__ = (int) __de___iterator->next();
    //((de.inodenumber < d.s.NumberofInodes) && ((de.inodenumber == 0)))
    // __left1181__ <-- de
    int __left1181__ = (int) __de__; //varexpr
    // __left1181__ = de
    // __offsetinbits1182__ <-- ((8 * 124) + 0)
    int __leftop1184__ = 8;
    int __rightop1185__ = 124;
    int __leftop1183__ = __leftop1184__ * __rightop1185__;
    int __rightop1186__ = 0;
    int __offsetinbits1182__ = __leftop1183__ + __rightop1186__;
    // __offsetinbits1182__ = ((8 * 124) + 0)
    int __offset1187__ = __offsetinbits1182__ >> 3;
    int __shift1188__ = __offsetinbits1182__ - (__offset1187__ << 3);
    int __leftop1180__ = ((*(int *)(__left1181__ + __offset1187__))  >> __shift1188__) & 0xffffffff;
    // __left1190__ <-- d.s
    // __left1191__ <-- d
    int __left1191__ = (int) d; //varexpr
    // __left1191__ = d
    int __left1190__ = (__left1191__ + 0);
    int __leftop1193__ = 32;
    int __leftop1195__ = 32;
    int __leftop1197__ = 32;
    int __leftop1199__ = 32;
    int __leftop1201__ = 32;
    int __leftop1203__ = 32;
    int __rightop1204__ = 0;
    int __rightop1202__ = __leftop1203__ + __rightop1204__;
    int __rightop1200__ = __leftop1201__ + __rightop1202__;
    int __rightop1198__ = __leftop1199__ + __rightop1200__;
    int __rightop1196__ = __leftop1197__ + __rightop1198__;
    int __rightop1194__ = __leftop1195__ + __rightop1196__;
    int __sizeof1192__ = __leftop1193__ + __rightop1194__;
    int __high1205__ = __left1190__ + __sizeof1192__;
    assertvalidmemory(__left1190__, __high1205__);
    // __left1190__ = d.s
    // __offsetinbits1206__ <-- (32 + (32 + (32 + 0)))
    int __leftop1207__ = 32;
    int __leftop1209__ = 32;
    int __leftop1211__ = 32;
    int __rightop1212__ = 0;
    int __rightop1210__ = __leftop1211__ + __rightop1212__;
    int __rightop1208__ = __leftop1209__ + __rightop1210__;
    int __offsetinbits1206__ = __leftop1207__ + __rightop1208__;
    // __offsetinbits1206__ = (32 + (32 + (32 + 0)))
    int __offset1213__ = __offsetinbits1206__ >> 3;
    int __shift1214__ = __offsetinbits1206__ - (__offset1213__ << 3);
    int __rightop1189__ = ((*(int *)(__left1190__ + __offset1213__))  >> __shift1214__) & 0xffffffff;
    int __leftop1179__ = __leftop1180__ < __rightop1189__;
    // __left1218__ <-- de
    int __left1218__ = (int) __de__; //varexpr
    // __left1218__ = de
    // __offsetinbits1219__ <-- ((8 * 124) + 0)
    int __leftop1221__ = 8;
    int __rightop1222__ = 124;
    int __leftop1220__ = __leftop1221__ * __rightop1222__;
    int __rightop1223__ = 0;
    int __offsetinbits1219__ = __leftop1220__ + __rightop1223__;
    // __offsetinbits1219__ = ((8 * 124) + 0)
    int __offset1224__ = __offsetinbits1219__ >> 3;
    int __shift1225__ = __offsetinbits1219__ - (__offset1224__ << 3);
    int __leftop1217__ = ((*(int *)(__left1218__ + __offset1224__))  >> __shift1225__) & 0xffffffff;
    int __rightop1226__ = 0;
    int __leftop1216__ = __leftop1217__ == __rightop1226__;
    int __rightop1215__ = !__leftop1216__;
    int __tempvar1178__ = __leftop1179__ && __rightop1215__;
    if (__tempvar1178__)
      {
      // __left1228__ <-- de
      int __left1228__ = (int) __de__; //varexpr
      // __left1228__ = de
      // __offsetinbits1229__ <-- ((8 * 124) + 0)
      int __leftop1231__ = 8;
      int __rightop1232__ = 124;
      int __leftop1230__ = __leftop1231__ * __rightop1232__;
      int __rightop1233__ = 0;
      int __offsetinbits1229__ = __leftop1230__ + __rightop1233__;
      // __offsetinbits1229__ = ((8 * 124) + 0)
      int __offset1234__ = __offsetinbits1229__ >> 3;
      int __shift1235__ = __offsetinbits1229__ - (__offset1234__ << 3);
      int __element1227__ = ((*(int *)(__left1228__ + __offset1234__))  >> __shift1235__) & 0xffffffff;
      __FileInode___hash->add((int)__element1227__, (int)__element1227__);
      }
    }
  }


// build rule16
  {
  for (SimpleIterator* __j___iterator = __UsedInode___hash->iterator(); __j___iterator->hasNext(); )
    {
    int __j__ = (int) __j___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); )
      {
      int __itb__ = (int) __itb___iterator->next();
      //true
      int __tempvar1236__ = 1;
      if (__tempvar1236__)
        {
        int __leftele1237__ = (int) __j__; //varexpr
        // __left1239__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left1240__ <-- cast(__InodeTable__, d.b[itb])
        // __left1242__ <-- d
        int __left1242__ = (int) d; //varexpr
        // __left1242__ = d
        // __offsetinbits1243__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1244__ = 0;
        int __leftop1248__ = 8;
        // __left1250__ <-- d.s
        // __left1251__ <-- d
        int __left1251__ = (int) d; //varexpr
        // __left1251__ = d
        int __left1250__ = (__left1251__ + 0);
        int __leftop1253__ = 32;
        int __leftop1255__ = 32;
        int __leftop1257__ = 32;
        int __leftop1259__ = 32;
        int __leftop1261__ = 32;
        int __leftop1263__ = 32;
        int __rightop1264__ = 0;
        int __rightop1262__ = __leftop1263__ + __rightop1264__;
        int __rightop1260__ = __leftop1261__ + __rightop1262__;
        int __rightop1258__ = __leftop1259__ + __rightop1260__;
        int __rightop1256__ = __leftop1257__ + __rightop1258__;
        int __rightop1254__ = __leftop1255__ + __rightop1256__;
        int __sizeof1252__ = __leftop1253__ + __rightop1254__;
        int __high1265__ = __left1250__ + __sizeof1252__;
        assertvalidmemory(__left1250__, __high1265__);
        // __left1250__ = d.s
        // __offsetinbits1266__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1267__ = 32;
        int __leftop1269__ = 32;
        int __leftop1271__ = 32;
        int __leftop1273__ = 32;
        int __leftop1275__ = 32;
        int __rightop1276__ = 0;
        int __rightop1274__ = __leftop1275__ + __rightop1276__;
        int __rightop1272__ = __leftop1273__ + __rightop1274__;
        int __rightop1270__ = __leftop1271__ + __rightop1272__;
        int __rightop1268__ = __leftop1269__ + __rightop1270__;
        int __offsetinbits1266__ = __leftop1267__ + __rightop1268__;
        // __offsetinbits1266__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1277__ = __offsetinbits1266__ >> 3;
        int __shift1278__ = __offsetinbits1266__ - (__offset1277__ << 3);
        int __rightop1249__ = ((*(int *)(__left1250__ + __offset1277__))  >> __shift1278__) & 0xffffffff;
        int __leftop1247__ = __leftop1248__ * __rightop1249__;
        int __rightop1279__ = 0;
        int __leftop1246__ = __leftop1247__ + __rightop1279__;
        int __rightop1280__ = (int) __itb__; //varexpr
        int __rightop1245__ = __leftop1246__ * __rightop1280__;
        int __offsetinbits1243__ = __leftop1244__ + __rightop1245__;
        // __offsetinbits1243__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1281__ = __offsetinbits1243__ >> 3;
        int __expr1241__ = (__left1242__ + __offset1281__);
        int __leftop1284__ = 8;
        // __left1286__ <-- d.s
        // __left1287__ <-- d
        int __left1287__ = (int) d; //varexpr
        // __left1287__ = d
        int __left1286__ = (__left1287__ + 0);
        int __leftop1289__ = 32;
        int __leftop1291__ = 32;
        int __leftop1293__ = 32;
        int __leftop1295__ = 32;
        int __leftop1297__ = 32;
        int __leftop1299__ = 32;
        int __rightop1300__ = 0;
        int __rightop1298__ = __leftop1299__ + __rightop1300__;
        int __rightop1296__ = __leftop1297__ + __rightop1298__;
        int __rightop1294__ = __leftop1295__ + __rightop1296__;
        int __rightop1292__ = __leftop1293__ + __rightop1294__;
        int __rightop1290__ = __leftop1291__ + __rightop1292__;
        int __sizeof1288__ = __leftop1289__ + __rightop1290__;
        int __high1301__ = __left1286__ + __sizeof1288__;
        assertvalidmemory(__left1286__, __high1301__);
        // __left1286__ = d.s
        // __offsetinbits1302__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1303__ = 32;
        int __leftop1305__ = 32;
        int __leftop1307__ = 32;
        int __leftop1309__ = 32;
        int __leftop1311__ = 32;
        int __rightop1312__ = 0;
        int __rightop1310__ = __leftop1311__ + __rightop1312__;
        int __rightop1308__ = __leftop1309__ + __rightop1310__;
        int __rightop1306__ = __leftop1307__ + __rightop1308__;
        int __rightop1304__ = __leftop1305__ + __rightop1306__;
        int __offsetinbits1302__ = __leftop1303__ + __rightop1304__;
        // __offsetinbits1302__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1313__ = __offsetinbits1302__ >> 3;
        int __shift1314__ = __offsetinbits1302__ - (__offset1313__ << 3);
        int __rightop1285__ = ((*(int *)(__left1286__ + __offset1313__))  >> __shift1314__) & 0xffffffff;
        int __leftop1283__ = __leftop1284__ * __rightop1285__;
        int __rightop1315__ = 0;
        int __sizeof1282__ = __leftop1283__ + __rightop1315__;
        int __high1316__ = __expr1241__ + __sizeof1282__;
        assertvalidmemory(__expr1241__, __high1316__);
        int __left1240__ = (int) __expr1241__;
        // __left1240__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1317__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __leftop1318__ = 0;
        int __leftop1321__ = 32;
        int __leftop1324__ = 32;
        int __rightop1325__ = 12;
        int __leftop1323__ = __leftop1324__ * __rightop1325__;
        int __leftop1327__ = 32;
        int __rightop1328__ = 0;
        int __rightop1326__ = __leftop1327__ + __rightop1328__;
        int __rightop1322__ = __leftop1323__ + __rightop1326__;
        int __leftop1320__ = __leftop1321__ + __rightop1322__;
        int __rightop1329__ = (int) __j__; //varexpr
        int __rightop1319__ = __leftop1320__ * __rightop1329__;
        int __offsetinbits1317__ = __leftop1318__ + __rightop1319__;
        // __offsetinbits1317__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __offset1330__ = __offsetinbits1317__ >> 3;
        int __left1239__ = (__left1240__ + __offset1330__);
        int __leftop1332__ = 32;
        int __leftop1335__ = 32;
        int __rightop1336__ = 12;
        int __leftop1334__ = __leftop1335__ * __rightop1336__;
        int __leftop1338__ = 32;
        int __rightop1339__ = 0;
        int __rightop1337__ = __leftop1338__ + __rightop1339__;
        int __rightop1333__ = __leftop1334__ + __rightop1337__;
        int __sizeof1331__ = __leftop1332__ + __rightop1333__;
        int __high1340__ = __left1239__ + __sizeof1331__;
        assertvalidmemory(__left1239__, __high1340__);
        // __left1239__ = cast(__InodeTable__, d.b[itb]).itable[j]
        // __offsetinbits1341__ <-- ((32 * 12) + (32 + 0))
        int __leftop1343__ = 32;
        int __rightop1344__ = 12;
        int __leftop1342__ = __leftop1343__ * __rightop1344__;
        int __leftop1346__ = 32;
        int __rightop1347__ = 0;
        int __rightop1345__ = __leftop1346__ + __rightop1347__;
        int __offsetinbits1341__ = __leftop1342__ + __rightop1345__;
        // __offsetinbits1341__ = ((32 * 12) + (32 + 0))
        int __offset1348__ = __offsetinbits1341__ >> 3;
        int __shift1349__ = __offsetinbits1341__ - (__offset1348__ << 3);
        int __rightele1238__ = ((*(int *)(__left1239__ + __offset1348__))  >> __shift1349__) & 0xffffffff;
        __referencecount___hash->add((int)__leftele1237__, (int)__rightele1238__);
        }
      }
    }
  }


// build rule11
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); )
      {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar1351__ = 0;
      int __tempvar1352__ = 11;
      for (int __j__ = __tempvar1351__; __j__ <= __tempvar1352__; __j__++)
        {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] < d.s.NumberofBlocks) && ((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0)))
        // __left1356__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left1357__ <-- cast(__InodeTable__, d.b[itb])
        // __left1359__ <-- d
        int __left1359__ = (int) d; //varexpr
        // __left1359__ = d
        // __offsetinbits1360__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1361__ = 0;
        int __leftop1365__ = 8;
        // __left1367__ <-- d.s
        // __left1368__ <-- d
        int __left1368__ = (int) d; //varexpr
        // __left1368__ = d
        int __left1367__ = (__left1368__ + 0);
        int __leftop1370__ = 32;
        int __leftop1372__ = 32;
        int __leftop1374__ = 32;
        int __leftop1376__ = 32;
        int __leftop1378__ = 32;
        int __leftop1380__ = 32;
        int __rightop1381__ = 0;
        int __rightop1379__ = __leftop1380__ + __rightop1381__;
        int __rightop1377__ = __leftop1378__ + __rightop1379__;
        int __rightop1375__ = __leftop1376__ + __rightop1377__;
        int __rightop1373__ = __leftop1374__ + __rightop1375__;
        int __rightop1371__ = __leftop1372__ + __rightop1373__;
        int __sizeof1369__ = __leftop1370__ + __rightop1371__;
        int __high1382__ = __left1367__ + __sizeof1369__;
        assertvalidmemory(__left1367__, __high1382__);
        // __left1367__ = d.s
        // __offsetinbits1383__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1384__ = 32;
        int __leftop1386__ = 32;
        int __leftop1388__ = 32;
        int __leftop1390__ = 32;
        int __leftop1392__ = 32;
        int __rightop1393__ = 0;
        int __rightop1391__ = __leftop1392__ + __rightop1393__;
        int __rightop1389__ = __leftop1390__ + __rightop1391__;
        int __rightop1387__ = __leftop1388__ + __rightop1389__;
        int __rightop1385__ = __leftop1386__ + __rightop1387__;
        int __offsetinbits1383__ = __leftop1384__ + __rightop1385__;
        // __offsetinbits1383__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1394__ = __offsetinbits1383__ >> 3;
        int __shift1395__ = __offsetinbits1383__ - (__offset1394__ << 3);
        int __rightop1366__ = ((*(int *)(__left1367__ + __offset1394__))  >> __shift1395__) & 0xffffffff;
        int __leftop1364__ = __leftop1365__ * __rightop1366__;
        int __rightop1396__ = 0;
        int __leftop1363__ = __leftop1364__ + __rightop1396__;
        int __rightop1397__ = (int) __itb__; //varexpr
        int __rightop1362__ = __leftop1363__ * __rightop1397__;
        int __offsetinbits1360__ = __leftop1361__ + __rightop1362__;
        // __offsetinbits1360__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1398__ = __offsetinbits1360__ >> 3;
        int __expr1358__ = (__left1359__ + __offset1398__);
        int __leftop1401__ = 8;
        // __left1403__ <-- d.s
        // __left1404__ <-- d
        int __left1404__ = (int) d; //varexpr
        // __left1404__ = d
        int __left1403__ = (__left1404__ + 0);
        int __leftop1406__ = 32;
        int __leftop1408__ = 32;
        int __leftop1410__ = 32;
        int __leftop1412__ = 32;
        int __leftop1414__ = 32;
        int __leftop1416__ = 32;
        int __rightop1417__ = 0;
        int __rightop1415__ = __leftop1416__ + __rightop1417__;
        int __rightop1413__ = __leftop1414__ + __rightop1415__;
        int __rightop1411__ = __leftop1412__ + __rightop1413__;
        int __rightop1409__ = __leftop1410__ + __rightop1411__;
        int __rightop1407__ = __leftop1408__ + __rightop1409__;
        int __sizeof1405__ = __leftop1406__ + __rightop1407__;
        int __high1418__ = __left1403__ + __sizeof1405__;
        assertvalidmemory(__left1403__, __high1418__);
        // __left1403__ = d.s
        // __offsetinbits1419__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1420__ = 32;
        int __leftop1422__ = 32;
        int __leftop1424__ = 32;
        int __leftop1426__ = 32;
        int __leftop1428__ = 32;
        int __rightop1429__ = 0;
        int __rightop1427__ = __leftop1428__ + __rightop1429__;
        int __rightop1425__ = __leftop1426__ + __rightop1427__;
        int __rightop1423__ = __leftop1424__ + __rightop1425__;
        int __rightop1421__ = __leftop1422__ + __rightop1423__;
        int __offsetinbits1419__ = __leftop1420__ + __rightop1421__;
        // __offsetinbits1419__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1430__ = __offsetinbits1419__ >> 3;
        int __shift1431__ = __offsetinbits1419__ - (__offset1430__ << 3);
        int __rightop1402__ = ((*(int *)(__left1403__ + __offset1430__))  >> __shift1431__) & 0xffffffff;
        int __leftop1400__ = __leftop1401__ * __rightop1402__;
        int __rightop1432__ = 0;
        int __sizeof1399__ = __leftop1400__ + __rightop1432__;
        int __high1433__ = __expr1358__ + __sizeof1399__;
        assertvalidmemory(__expr1358__, __high1433__);
        int __left1357__ = (int) __expr1358__;
        // __left1357__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1434__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop1435__ = 0;
        int __leftop1438__ = 32;
        int __leftop1441__ = 32;
        int __rightop1442__ = 12;
        int __leftop1440__ = __leftop1441__ * __rightop1442__;
        int __leftop1444__ = 32;
        int __rightop1445__ = 0;
        int __rightop1443__ = __leftop1444__ + __rightop1445__;
        int __rightop1439__ = __leftop1440__ + __rightop1443__;
        int __leftop1437__ = __leftop1438__ + __rightop1439__;
        int __rightop1446__ = (int) __i__; //varexpr
        int __rightop1436__ = __leftop1437__ * __rightop1446__;
        int __offsetinbits1434__ = __leftop1435__ + __rightop1436__;
        // __offsetinbits1434__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset1447__ = __offsetinbits1434__ >> 3;
        int __left1356__ = (__left1357__ + __offset1447__);
        int __leftop1449__ = 32;
        int __leftop1452__ = 32;
        int __rightop1453__ = 12;
        int __leftop1451__ = __leftop1452__ * __rightop1453__;
        int __leftop1455__ = 32;
        int __rightop1456__ = 0;
        int __rightop1454__ = __leftop1455__ + __rightop1456__;
        int __rightop1450__ = __leftop1451__ + __rightop1454__;
        int __sizeof1448__ = __leftop1449__ + __rightop1450__;
        int __high1457__ = __left1356__ + __sizeof1448__;
        assertvalidmemory(__left1356__, __high1457__);
        // __left1356__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits1458__ <-- ((32 + 0) + (32 * j))
        int __leftop1460__ = 32;
        int __rightop1461__ = 0;
        int __leftop1459__ = __leftop1460__ + __rightop1461__;
        int __leftop1463__ = 32;
        int __rightop1464__ = (int) __j__; //varexpr
        int __rightop1462__ = __leftop1463__ * __rightop1464__;
        int __offsetinbits1458__ = __leftop1459__ + __rightop1462__;
        // __offsetinbits1458__ = ((32 + 0) + (32 * j))
        int __offset1465__ = __offsetinbits1458__ >> 3;
        int __shift1466__ = __offsetinbits1458__ - (__offset1465__ << 3);
        int __leftop1355__ = ((*(int *)(__left1356__ + __offset1465__))  >> __shift1466__) & 0xffffffff;
        // __left1468__ <-- d.s
        // __left1469__ <-- d
        int __left1469__ = (int) d; //varexpr
        // __left1469__ = d
        int __left1468__ = (__left1469__ + 0);
        int __leftop1471__ = 32;
        int __leftop1473__ = 32;
        int __leftop1475__ = 32;
        int __leftop1477__ = 32;
        int __leftop1479__ = 32;
        int __leftop1481__ = 32;
        int __rightop1482__ = 0;
        int __rightop1480__ = __leftop1481__ + __rightop1482__;
        int __rightop1478__ = __leftop1479__ + __rightop1480__;
        int __rightop1476__ = __leftop1477__ + __rightop1478__;
        int __rightop1474__ = __leftop1475__ + __rightop1476__;
        int __rightop1472__ = __leftop1473__ + __rightop1474__;
        int __sizeof1470__ = __leftop1471__ + __rightop1472__;
        int __high1483__ = __left1468__ + __sizeof1470__;
        assertvalidmemory(__left1468__, __high1483__);
        // __left1468__ = d.s
        // __offsetinbits1484__ <-- (32 + (32 + 0))
        int __leftop1485__ = 32;
        int __leftop1487__ = 32;
        int __rightop1488__ = 0;
        int __rightop1486__ = __leftop1487__ + __rightop1488__;
        int __offsetinbits1484__ = __leftop1485__ + __rightop1486__;
        // __offsetinbits1484__ = (32 + (32 + 0))
        int __offset1489__ = __offsetinbits1484__ >> 3;
        int __shift1490__ = __offsetinbits1484__ - (__offset1489__ << 3);
        int __rightop1467__ = ((*(int *)(__left1468__ + __offset1489__))  >> __shift1490__) & 0xffffffff;
        int __leftop1354__ = __leftop1355__ < __rightop1467__;
        // __left1494__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left1495__ <-- cast(__InodeTable__, d.b[itb])
        // __left1497__ <-- d
        int __left1497__ = (int) d; //varexpr
        // __left1497__ = d
        // __offsetinbits1498__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1499__ = 0;
        int __leftop1503__ = 8;
        // __left1505__ <-- d.s
        // __left1506__ <-- d
        int __left1506__ = (int) d; //varexpr
        // __left1506__ = d
        int __left1505__ = (__left1506__ + 0);
        int __leftop1508__ = 32;
        int __leftop1510__ = 32;
        int __leftop1512__ = 32;
        int __leftop1514__ = 32;
        int __leftop1516__ = 32;
        int __leftop1518__ = 32;
        int __rightop1519__ = 0;
        int __rightop1517__ = __leftop1518__ + __rightop1519__;
        int __rightop1515__ = __leftop1516__ + __rightop1517__;
        int __rightop1513__ = __leftop1514__ + __rightop1515__;
        int __rightop1511__ = __leftop1512__ + __rightop1513__;
        int __rightop1509__ = __leftop1510__ + __rightop1511__;
        int __sizeof1507__ = __leftop1508__ + __rightop1509__;
        int __high1520__ = __left1505__ + __sizeof1507__;
        assertvalidmemory(__left1505__, __high1520__);
        // __left1505__ = d.s
        // __offsetinbits1521__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1522__ = 32;
        int __leftop1524__ = 32;
        int __leftop1526__ = 32;
        int __leftop1528__ = 32;
        int __leftop1530__ = 32;
        int __rightop1531__ = 0;
        int __rightop1529__ = __leftop1530__ + __rightop1531__;
        int __rightop1527__ = __leftop1528__ + __rightop1529__;
        int __rightop1525__ = __leftop1526__ + __rightop1527__;
        int __rightop1523__ = __leftop1524__ + __rightop1525__;
        int __offsetinbits1521__ = __leftop1522__ + __rightop1523__;
        // __offsetinbits1521__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1532__ = __offsetinbits1521__ >> 3;
        int __shift1533__ = __offsetinbits1521__ - (__offset1532__ << 3);
        int __rightop1504__ = ((*(int *)(__left1505__ + __offset1532__))  >> __shift1533__) & 0xffffffff;
        int __leftop1502__ = __leftop1503__ * __rightop1504__;
        int __rightop1534__ = 0;
        int __leftop1501__ = __leftop1502__ + __rightop1534__;
        int __rightop1535__ = (int) __itb__; //varexpr
        int __rightop1500__ = __leftop1501__ * __rightop1535__;
        int __offsetinbits1498__ = __leftop1499__ + __rightop1500__;
        // __offsetinbits1498__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1536__ = __offsetinbits1498__ >> 3;
        int __expr1496__ = (__left1497__ + __offset1536__);
        int __leftop1539__ = 8;
        // __left1541__ <-- d.s
        // __left1542__ <-- d
        int __left1542__ = (int) d; //varexpr
        // __left1542__ = d
        int __left1541__ = (__left1542__ + 0);
        int __leftop1544__ = 32;
        int __leftop1546__ = 32;
        int __leftop1548__ = 32;
        int __leftop1550__ = 32;
        int __leftop1552__ = 32;
        int __leftop1554__ = 32;
        int __rightop1555__ = 0;
        int __rightop1553__ = __leftop1554__ + __rightop1555__;
        int __rightop1551__ = __leftop1552__ + __rightop1553__;
        int __rightop1549__ = __leftop1550__ + __rightop1551__;
        int __rightop1547__ = __leftop1548__ + __rightop1549__;
        int __rightop1545__ = __leftop1546__ + __rightop1547__;
        int __sizeof1543__ = __leftop1544__ + __rightop1545__;
        int __high1556__ = __left1541__ + __sizeof1543__;
        assertvalidmemory(__left1541__, __high1556__);
        // __left1541__ = d.s
        // __offsetinbits1557__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1558__ = 32;
        int __leftop1560__ = 32;
        int __leftop1562__ = 32;
        int __leftop1564__ = 32;
        int __leftop1566__ = 32;
        int __rightop1567__ = 0;
        int __rightop1565__ = __leftop1566__ + __rightop1567__;
        int __rightop1563__ = __leftop1564__ + __rightop1565__;
        int __rightop1561__ = __leftop1562__ + __rightop1563__;
        int __rightop1559__ = __leftop1560__ + __rightop1561__;
        int __offsetinbits1557__ = __leftop1558__ + __rightop1559__;
        // __offsetinbits1557__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1568__ = __offsetinbits1557__ >> 3;
        int __shift1569__ = __offsetinbits1557__ - (__offset1568__ << 3);
        int __rightop1540__ = ((*(int *)(__left1541__ + __offset1568__))  >> __shift1569__) & 0xffffffff;
        int __leftop1538__ = __leftop1539__ * __rightop1540__;
        int __rightop1570__ = 0;
        int __sizeof1537__ = __leftop1538__ + __rightop1570__;
        int __high1571__ = __expr1496__ + __sizeof1537__;
        assertvalidmemory(__expr1496__, __high1571__);
        int __left1495__ = (int) __expr1496__;
        // __left1495__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1572__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop1573__ = 0;
        int __leftop1576__ = 32;
        int __leftop1579__ = 32;
        int __rightop1580__ = 12;
        int __leftop1578__ = __leftop1579__ * __rightop1580__;
        int __leftop1582__ = 32;
        int __rightop1583__ = 0;
        int __rightop1581__ = __leftop1582__ + __rightop1583__;
        int __rightop1577__ = __leftop1578__ + __rightop1581__;
        int __leftop1575__ = __leftop1576__ + __rightop1577__;
        int __rightop1584__ = (int) __i__; //varexpr
        int __rightop1574__ = __leftop1575__ * __rightop1584__;
        int __offsetinbits1572__ = __leftop1573__ + __rightop1574__;
        // __offsetinbits1572__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset1585__ = __offsetinbits1572__ >> 3;
        int __left1494__ = (__left1495__ + __offset1585__);
        int __leftop1587__ = 32;
        int __leftop1590__ = 32;
        int __rightop1591__ = 12;
        int __leftop1589__ = __leftop1590__ * __rightop1591__;
        int __leftop1593__ = 32;
        int __rightop1594__ = 0;
        int __rightop1592__ = __leftop1593__ + __rightop1594__;
        int __rightop1588__ = __leftop1589__ + __rightop1592__;
        int __sizeof1586__ = __leftop1587__ + __rightop1588__;
        int __high1595__ = __left1494__ + __sizeof1586__;
        assertvalidmemory(__left1494__, __high1595__);
        // __left1494__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits1596__ <-- ((32 + 0) + (32 * j))
        int __leftop1598__ = 32;
        int __rightop1599__ = 0;
        int __leftop1597__ = __leftop1598__ + __rightop1599__;
        int __leftop1601__ = 32;
        int __rightop1602__ = (int) __j__; //varexpr
        int __rightop1600__ = __leftop1601__ * __rightop1602__;
        int __offsetinbits1596__ = __leftop1597__ + __rightop1600__;
        // __offsetinbits1596__ = ((32 + 0) + (32 * j))
        int __offset1603__ = __offsetinbits1596__ >> 3;
        int __shift1604__ = __offsetinbits1596__ - (__offset1603__ << 3);
        int __leftop1493__ = ((*(int *)(__left1494__ + __offset1603__))  >> __shift1604__) & 0xffffffff;
        int __rightop1605__ = 0;
        int __leftop1492__ = __leftop1493__ == __rightop1605__;
        int __rightop1491__ = !__leftop1492__;
        int __tempvar1353__ = __leftop1354__ && __rightop1491__;
        if (__tempvar1353__)
          {
          // __left1607__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left1608__ <-- cast(__InodeTable__, d.b[itb])
          // __left1610__ <-- d
          int __left1610__ = (int) d; //varexpr
          // __left1610__ = d
          // __offsetinbits1611__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop1612__ = 0;
          int __leftop1616__ = 8;
          // __left1618__ <-- d.s
          // __left1619__ <-- d
          int __left1619__ = (int) d; //varexpr
          // __left1619__ = d
          int __left1618__ = (__left1619__ + 0);
          int __leftop1621__ = 32;
          int __leftop1623__ = 32;
          int __leftop1625__ = 32;
          int __leftop1627__ = 32;
          int __leftop1629__ = 32;
          int __leftop1631__ = 32;
          int __rightop1632__ = 0;
          int __rightop1630__ = __leftop1631__ + __rightop1632__;
          int __rightop1628__ = __leftop1629__ + __rightop1630__;
          int __rightop1626__ = __leftop1627__ + __rightop1628__;
          int __rightop1624__ = __leftop1625__ + __rightop1626__;
          int __rightop1622__ = __leftop1623__ + __rightop1624__;
          int __sizeof1620__ = __leftop1621__ + __rightop1622__;
          int __high1633__ = __left1618__ + __sizeof1620__;
          assertvalidmemory(__left1618__, __high1633__);
          // __left1618__ = d.s
          // __offsetinbits1634__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1635__ = 32;
          int __leftop1637__ = 32;
          int __leftop1639__ = 32;
          int __leftop1641__ = 32;
          int __leftop1643__ = 32;
          int __rightop1644__ = 0;
          int __rightop1642__ = __leftop1643__ + __rightop1644__;
          int __rightop1640__ = __leftop1641__ + __rightop1642__;
          int __rightop1638__ = __leftop1639__ + __rightop1640__;
          int __rightop1636__ = __leftop1637__ + __rightop1638__;
          int __offsetinbits1634__ = __leftop1635__ + __rightop1636__;
          // __offsetinbits1634__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1645__ = __offsetinbits1634__ >> 3;
          int __shift1646__ = __offsetinbits1634__ - (__offset1645__ << 3);
          int __rightop1617__ = ((*(int *)(__left1618__ + __offset1645__))  >> __shift1646__) & 0xffffffff;
          int __leftop1615__ = __leftop1616__ * __rightop1617__;
          int __rightop1647__ = 0;
          int __leftop1614__ = __leftop1615__ + __rightop1647__;
          int __rightop1648__ = (int) __itb__; //varexpr
          int __rightop1613__ = __leftop1614__ * __rightop1648__;
          int __offsetinbits1611__ = __leftop1612__ + __rightop1613__;
          // __offsetinbits1611__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset1649__ = __offsetinbits1611__ >> 3;
          int __expr1609__ = (__left1610__ + __offset1649__);
          int __leftop1652__ = 8;
          // __left1654__ <-- d.s
          // __left1655__ <-- d
          int __left1655__ = (int) d; //varexpr
          // __left1655__ = d
          int __left1654__ = (__left1655__ + 0);
          int __leftop1657__ = 32;
          int __leftop1659__ = 32;
          int __leftop1661__ = 32;
          int __leftop1663__ = 32;
          int __leftop1665__ = 32;
          int __leftop1667__ = 32;
          int __rightop1668__ = 0;
          int __rightop1666__ = __leftop1667__ + __rightop1668__;
          int __rightop1664__ = __leftop1665__ + __rightop1666__;
          int __rightop1662__ = __leftop1663__ + __rightop1664__;
          int __rightop1660__ = __leftop1661__ + __rightop1662__;
          int __rightop1658__ = __leftop1659__ + __rightop1660__;
          int __sizeof1656__ = __leftop1657__ + __rightop1658__;
          int __high1669__ = __left1654__ + __sizeof1656__;
          assertvalidmemory(__left1654__, __high1669__);
          // __left1654__ = d.s
          // __offsetinbits1670__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1671__ = 32;
          int __leftop1673__ = 32;
          int __leftop1675__ = 32;
          int __leftop1677__ = 32;
          int __leftop1679__ = 32;
          int __rightop1680__ = 0;
          int __rightop1678__ = __leftop1679__ + __rightop1680__;
          int __rightop1676__ = __leftop1677__ + __rightop1678__;
          int __rightop1674__ = __leftop1675__ + __rightop1676__;
          int __rightop1672__ = __leftop1673__ + __rightop1674__;
          int __offsetinbits1670__ = __leftop1671__ + __rightop1672__;
          // __offsetinbits1670__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1681__ = __offsetinbits1670__ >> 3;
          int __shift1682__ = __offsetinbits1670__ - (__offset1681__ << 3);
          int __rightop1653__ = ((*(int *)(__left1654__ + __offset1681__))  >> __shift1682__) & 0xffffffff;
          int __leftop1651__ = __leftop1652__ * __rightop1653__;
          int __rightop1683__ = 0;
          int __sizeof1650__ = __leftop1651__ + __rightop1683__;
          int __high1684__ = __expr1609__ + __sizeof1650__;
          assertvalidmemory(__expr1609__, __high1684__);
          int __left1608__ = (int) __expr1609__;
          // __left1608__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits1685__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop1686__ = 0;
          int __leftop1689__ = 32;
          int __leftop1692__ = 32;
          int __rightop1693__ = 12;
          int __leftop1691__ = __leftop1692__ * __rightop1693__;
          int __leftop1695__ = 32;
          int __rightop1696__ = 0;
          int __rightop1694__ = __leftop1695__ + __rightop1696__;
          int __rightop1690__ = __leftop1691__ + __rightop1694__;
          int __leftop1688__ = __leftop1689__ + __rightop1690__;
          int __rightop1697__ = (int) __i__; //varexpr
          int __rightop1687__ = __leftop1688__ * __rightop1697__;
          int __offsetinbits1685__ = __leftop1686__ + __rightop1687__;
          // __offsetinbits1685__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset1698__ = __offsetinbits1685__ >> 3;
          int __left1607__ = (__left1608__ + __offset1698__);
          int __leftop1700__ = 32;
          int __leftop1703__ = 32;
          int __rightop1704__ = 12;
          int __leftop1702__ = __leftop1703__ * __rightop1704__;
          int __leftop1706__ = 32;
          int __rightop1707__ = 0;
          int __rightop1705__ = __leftop1706__ + __rightop1707__;
          int __rightop1701__ = __leftop1702__ + __rightop1705__;
          int __sizeof1699__ = __leftop1700__ + __rightop1701__;
          int __high1708__ = __left1607__ + __sizeof1699__;
          assertvalidmemory(__left1607__, __high1708__);
          // __left1607__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits1709__ <-- ((32 + 0) + (32 * j))
          int __leftop1711__ = 32;
          int __rightop1712__ = 0;
          int __leftop1710__ = __leftop1711__ + __rightop1712__;
          int __leftop1714__ = 32;
          int __rightop1715__ = (int) __j__; //varexpr
          int __rightop1713__ = __leftop1714__ * __rightop1715__;
          int __offsetinbits1709__ = __leftop1710__ + __rightop1713__;
          // __offsetinbits1709__ = ((32 + 0) + (32 * j))
          int __offset1716__ = __offsetinbits1709__ >> 3;
          int __shift1717__ = __offsetinbits1709__ - (__offset1716__ << 3);
          int __element1606__ = ((*(int *)(__left1607__ + __offset1716__))  >> __shift1717__) & 0xffffffff;
          __FileBlock___hash->add((int)__element1606__, (int)__element1606__);
          }
        }
      }
    }
  }


// build rule8
  {
  int __tempvar1718__ = 0;
  // __left1721__ <-- d.s
  // __left1722__ <-- d
  int __left1722__ = (int) d; //varexpr
  // __left1722__ = d
  int __left1721__ = (__left1722__ + 0);
  int __leftop1724__ = 32;
  int __leftop1726__ = 32;
  int __leftop1728__ = 32;
  int __leftop1730__ = 32;
  int __leftop1732__ = 32;
  int __leftop1734__ = 32;
  int __rightop1735__ = 0;
  int __rightop1733__ = __leftop1734__ + __rightop1735__;
  int __rightop1731__ = __leftop1732__ + __rightop1733__;
  int __rightop1729__ = __leftop1730__ + __rightop1731__;
  int __rightop1727__ = __leftop1728__ + __rightop1729__;
  int __rightop1725__ = __leftop1726__ + __rightop1727__;
  int __sizeof1723__ = __leftop1724__ + __rightop1725__;
  int __high1736__ = __left1721__ + __sizeof1723__;
  assertvalidmemory(__left1721__, __high1736__);
  // __left1721__ = d.s
  // __offsetinbits1737__ <-- (32 + (32 + 0))
  int __leftop1738__ = 32;
  int __leftop1740__ = 32;
  int __rightop1741__ = 0;
  int __rightop1739__ = __leftop1740__ + __rightop1741__;
  int __offsetinbits1737__ = __leftop1738__ + __rightop1739__;
  // __offsetinbits1737__ = (32 + (32 + 0))
  int __offset1742__ = __offsetinbits1737__ >> 3;
  int __shift1743__ = __offsetinbits1737__ - (__offset1742__ << 3);
  int __leftop1720__ = ((*(int *)(__left1721__ + __offset1742__))  >> __shift1743__) & 0xffffffff;
  int __rightop1744__ = 1;
  int __tempvar1719__ = __leftop1720__ - __rightop1744__;
  for (int __j__ = __tempvar1718__; __j__ <= __tempvar1719__; __j__++)
    {
    //(j in? __UsedBlock__)
    int __element1747__ = (int) __j__; //varexpr
    int __leftop1746__ = __UsedBlock___hash->contains(__element1747__);
    int __tempvar1745__ = !__leftop1746__;
    if (__tempvar1745__)
      {
      int __element1748__ = (int) __j__; //varexpr
      __FreeBlock___hash->add((int)__element1748__, (int)__element1748__);
      }
    }
  }


// build rule10
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); )
      {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar1749__ = 0;
      int __tempvar1750__ = 11;
      for (int __j__ = __tempvar1749__; __j__ <= __tempvar1750__; __j__++)
        {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0))
        // __left1754__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left1755__ <-- cast(__InodeTable__, d.b[itb])
        // __left1757__ <-- d
        int __left1757__ = (int) d; //varexpr
        // __left1757__ = d
        // __offsetinbits1758__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1759__ = 0;
        int __leftop1763__ = 8;
        // __left1765__ <-- d.s
        // __left1766__ <-- d
        int __left1766__ = (int) d; //varexpr
        // __left1766__ = d
        int __left1765__ = (__left1766__ + 0);
        int __leftop1768__ = 32;
        int __leftop1770__ = 32;
        int __leftop1772__ = 32;
        int __leftop1774__ = 32;
        int __leftop1776__ = 32;
        int __leftop1778__ = 32;
        int __rightop1779__ = 0;
        int __rightop1777__ = __leftop1778__ + __rightop1779__;
        int __rightop1775__ = __leftop1776__ + __rightop1777__;
        int __rightop1773__ = __leftop1774__ + __rightop1775__;
        int __rightop1771__ = __leftop1772__ + __rightop1773__;
        int __rightop1769__ = __leftop1770__ + __rightop1771__;
        int __sizeof1767__ = __leftop1768__ + __rightop1769__;
        int __high1780__ = __left1765__ + __sizeof1767__;
        assertvalidmemory(__left1765__, __high1780__);
        // __left1765__ = d.s
        // __offsetinbits1781__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1782__ = 32;
        int __leftop1784__ = 32;
        int __leftop1786__ = 32;
        int __leftop1788__ = 32;
        int __leftop1790__ = 32;
        int __rightop1791__ = 0;
        int __rightop1789__ = __leftop1790__ + __rightop1791__;
        int __rightop1787__ = __leftop1788__ + __rightop1789__;
        int __rightop1785__ = __leftop1786__ + __rightop1787__;
        int __rightop1783__ = __leftop1784__ + __rightop1785__;
        int __offsetinbits1781__ = __leftop1782__ + __rightop1783__;
        // __offsetinbits1781__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1792__ = __offsetinbits1781__ >> 3;
        int __shift1793__ = __offsetinbits1781__ - (__offset1792__ << 3);
        int __rightop1764__ = ((*(int *)(__left1765__ + __offset1792__))  >> __shift1793__) & 0xffffffff;
        int __leftop1762__ = __leftop1763__ * __rightop1764__;
        int __rightop1794__ = 0;
        int __leftop1761__ = __leftop1762__ + __rightop1794__;
        int __rightop1795__ = (int) __itb__; //varexpr
        int __rightop1760__ = __leftop1761__ * __rightop1795__;
        int __offsetinbits1758__ = __leftop1759__ + __rightop1760__;
        // __offsetinbits1758__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1796__ = __offsetinbits1758__ >> 3;
        int __expr1756__ = (__left1757__ + __offset1796__);
        int __leftop1799__ = 8;
        // __left1801__ <-- d.s
        // __left1802__ <-- d
        int __left1802__ = (int) d; //varexpr
        // __left1802__ = d
        int __left1801__ = (__left1802__ + 0);
        int __leftop1804__ = 32;
        int __leftop1806__ = 32;
        int __leftop1808__ = 32;
        int __leftop1810__ = 32;
        int __leftop1812__ = 32;
        int __leftop1814__ = 32;
        int __rightop1815__ = 0;
        int __rightop1813__ = __leftop1814__ + __rightop1815__;
        int __rightop1811__ = __leftop1812__ + __rightop1813__;
        int __rightop1809__ = __leftop1810__ + __rightop1811__;
        int __rightop1807__ = __leftop1808__ + __rightop1809__;
        int __rightop1805__ = __leftop1806__ + __rightop1807__;
        int __sizeof1803__ = __leftop1804__ + __rightop1805__;
        int __high1816__ = __left1801__ + __sizeof1803__;
        assertvalidmemory(__left1801__, __high1816__);
        // __left1801__ = d.s
        // __offsetinbits1817__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1818__ = 32;
        int __leftop1820__ = 32;
        int __leftop1822__ = 32;
        int __leftop1824__ = 32;
        int __leftop1826__ = 32;
        int __rightop1827__ = 0;
        int __rightop1825__ = __leftop1826__ + __rightop1827__;
        int __rightop1823__ = __leftop1824__ + __rightop1825__;
        int __rightop1821__ = __leftop1822__ + __rightop1823__;
        int __rightop1819__ = __leftop1820__ + __rightop1821__;
        int __offsetinbits1817__ = __leftop1818__ + __rightop1819__;
        // __offsetinbits1817__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1828__ = __offsetinbits1817__ >> 3;
        int __shift1829__ = __offsetinbits1817__ - (__offset1828__ << 3);
        int __rightop1800__ = ((*(int *)(__left1801__ + __offset1828__))  >> __shift1829__) & 0xffffffff;
        int __leftop1798__ = __leftop1799__ * __rightop1800__;
        int __rightop1830__ = 0;
        int __sizeof1797__ = __leftop1798__ + __rightop1830__;
        int __high1831__ = __expr1756__ + __sizeof1797__;
        assertvalidmemory(__expr1756__, __high1831__);
        int __left1755__ = (int) __expr1756__;
        // __left1755__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1832__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop1833__ = 0;
        int __leftop1836__ = 32;
        int __leftop1839__ = 32;
        int __rightop1840__ = 12;
        int __leftop1838__ = __leftop1839__ * __rightop1840__;
        int __leftop1842__ = 32;
        int __rightop1843__ = 0;
        int __rightop1841__ = __leftop1842__ + __rightop1843__;
        int __rightop1837__ = __leftop1838__ + __rightop1841__;
        int __leftop1835__ = __leftop1836__ + __rightop1837__;
        int __rightop1844__ = (int) __i__; //varexpr
        int __rightop1834__ = __leftop1835__ * __rightop1844__;
        int __offsetinbits1832__ = __leftop1833__ + __rightop1834__;
        // __offsetinbits1832__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset1845__ = __offsetinbits1832__ >> 3;
        int __left1754__ = (__left1755__ + __offset1845__);
        int __leftop1847__ = 32;
        int __leftop1850__ = 32;
        int __rightop1851__ = 12;
        int __leftop1849__ = __leftop1850__ * __rightop1851__;
        int __leftop1853__ = 32;
        int __rightop1854__ = 0;
        int __rightop1852__ = __leftop1853__ + __rightop1854__;
        int __rightop1848__ = __leftop1849__ + __rightop1852__;
        int __sizeof1846__ = __leftop1847__ + __rightop1848__;
        int __high1855__ = __left1754__ + __sizeof1846__;
        assertvalidmemory(__left1754__, __high1855__);
        // __left1754__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits1856__ <-- ((32 + 0) + (32 * j))
        int __leftop1858__ = 32;
        int __rightop1859__ = 0;
        int __leftop1857__ = __leftop1858__ + __rightop1859__;
        int __leftop1861__ = 32;
        int __rightop1862__ = (int) __j__; //varexpr
        int __rightop1860__ = __leftop1861__ * __rightop1862__;
        int __offsetinbits1856__ = __leftop1857__ + __rightop1860__;
        // __offsetinbits1856__ = ((32 + 0) + (32 * j))
        int __offset1863__ = __offsetinbits1856__ >> 3;
        int __shift1864__ = __offsetinbits1856__ - (__offset1863__ << 3);
        int __leftop1753__ = ((*(int *)(__left1754__ + __offset1863__))  >> __shift1864__) & 0xffffffff;
        int __rightop1865__ = 0;
        int __leftop1752__ = __leftop1753__ == __rightop1865__;
        int __tempvar1751__ = !__leftop1752__;
        if (__tempvar1751__)
          {
          int __leftele1866__ = (int) __i__; //varexpr
          // __left1868__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left1869__ <-- cast(__InodeTable__, d.b[itb])
          // __left1871__ <-- d
          int __left1871__ = (int) d; //varexpr
          // __left1871__ = d
          // __offsetinbits1872__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop1873__ = 0;
          int __leftop1877__ = 8;
          // __left1879__ <-- d.s
          // __left1880__ <-- d
          int __left1880__ = (int) d; //varexpr
          // __left1880__ = d
          int __left1879__ = (__left1880__ + 0);
          int __leftop1882__ = 32;
          int __leftop1884__ = 32;
          int __leftop1886__ = 32;
          int __leftop1888__ = 32;
          int __leftop1890__ = 32;
          int __leftop1892__ = 32;
          int __rightop1893__ = 0;
          int __rightop1891__ = __leftop1892__ + __rightop1893__;
          int __rightop1889__ = __leftop1890__ + __rightop1891__;
          int __rightop1887__ = __leftop1888__ + __rightop1889__;
          int __rightop1885__ = __leftop1886__ + __rightop1887__;
          int __rightop1883__ = __leftop1884__ + __rightop1885__;
          int __sizeof1881__ = __leftop1882__ + __rightop1883__;
          int __high1894__ = __left1879__ + __sizeof1881__;
          assertvalidmemory(__left1879__, __high1894__);
          // __left1879__ = d.s
          // __offsetinbits1895__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1896__ = 32;
          int __leftop1898__ = 32;
          int __leftop1900__ = 32;
          int __leftop1902__ = 32;
          int __leftop1904__ = 32;
          int __rightop1905__ = 0;
          int __rightop1903__ = __leftop1904__ + __rightop1905__;
          int __rightop1901__ = __leftop1902__ + __rightop1903__;
          int __rightop1899__ = __leftop1900__ + __rightop1901__;
          int __rightop1897__ = __leftop1898__ + __rightop1899__;
          int __offsetinbits1895__ = __leftop1896__ + __rightop1897__;
          // __offsetinbits1895__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1906__ = __offsetinbits1895__ >> 3;
          int __shift1907__ = __offsetinbits1895__ - (__offset1906__ << 3);
          int __rightop1878__ = ((*(int *)(__left1879__ + __offset1906__))  >> __shift1907__) & 0xffffffff;
          int __leftop1876__ = __leftop1877__ * __rightop1878__;
          int __rightop1908__ = 0;
          int __leftop1875__ = __leftop1876__ + __rightop1908__;
          int __rightop1909__ = (int) __itb__; //varexpr
          int __rightop1874__ = __leftop1875__ * __rightop1909__;
          int __offsetinbits1872__ = __leftop1873__ + __rightop1874__;
          // __offsetinbits1872__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset1910__ = __offsetinbits1872__ >> 3;
          int __expr1870__ = (__left1871__ + __offset1910__);
          int __leftop1913__ = 8;
          // __left1915__ <-- d.s
          // __left1916__ <-- d
          int __left1916__ = (int) d; //varexpr
          // __left1916__ = d
          int __left1915__ = (__left1916__ + 0);
          int __leftop1918__ = 32;
          int __leftop1920__ = 32;
          int __leftop1922__ = 32;
          int __leftop1924__ = 32;
          int __leftop1926__ = 32;
          int __leftop1928__ = 32;
          int __rightop1929__ = 0;
          int __rightop1927__ = __leftop1928__ + __rightop1929__;
          int __rightop1925__ = __leftop1926__ + __rightop1927__;
          int __rightop1923__ = __leftop1924__ + __rightop1925__;
          int __rightop1921__ = __leftop1922__ + __rightop1923__;
          int __rightop1919__ = __leftop1920__ + __rightop1921__;
          int __sizeof1917__ = __leftop1918__ + __rightop1919__;
          int __high1930__ = __left1915__ + __sizeof1917__;
          assertvalidmemory(__left1915__, __high1930__);
          // __left1915__ = d.s
          // __offsetinbits1931__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1932__ = 32;
          int __leftop1934__ = 32;
          int __leftop1936__ = 32;
          int __leftop1938__ = 32;
          int __leftop1940__ = 32;
          int __rightop1941__ = 0;
          int __rightop1939__ = __leftop1940__ + __rightop1941__;
          int __rightop1937__ = __leftop1938__ + __rightop1939__;
          int __rightop1935__ = __leftop1936__ + __rightop1937__;
          int __rightop1933__ = __leftop1934__ + __rightop1935__;
          int __offsetinbits1931__ = __leftop1932__ + __rightop1933__;
          // __offsetinbits1931__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1942__ = __offsetinbits1931__ >> 3;
          int __shift1943__ = __offsetinbits1931__ - (__offset1942__ << 3);
          int __rightop1914__ = ((*(int *)(__left1915__ + __offset1942__))  >> __shift1943__) & 0xffffffff;
          int __leftop1912__ = __leftop1913__ * __rightop1914__;
          int __rightop1944__ = 0;
          int __sizeof1911__ = __leftop1912__ + __rightop1944__;
          int __high1945__ = __expr1870__ + __sizeof1911__;
          assertvalidmemory(__expr1870__, __high1945__);
          int __left1869__ = (int) __expr1870__;
          // __left1869__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits1946__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop1947__ = 0;
          int __leftop1950__ = 32;
          int __leftop1953__ = 32;
          int __rightop1954__ = 12;
          int __leftop1952__ = __leftop1953__ * __rightop1954__;
          int __leftop1956__ = 32;
          int __rightop1957__ = 0;
          int __rightop1955__ = __leftop1956__ + __rightop1957__;
          int __rightop1951__ = __leftop1952__ + __rightop1955__;
          int __leftop1949__ = __leftop1950__ + __rightop1951__;
          int __rightop1958__ = (int) __i__; //varexpr
          int __rightop1948__ = __leftop1949__ * __rightop1958__;
          int __offsetinbits1946__ = __leftop1947__ + __rightop1948__;
          // __offsetinbits1946__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset1959__ = __offsetinbits1946__ >> 3;
          int __left1868__ = (__left1869__ + __offset1959__);
          int __leftop1961__ = 32;
          int __leftop1964__ = 32;
          int __rightop1965__ = 12;
          int __leftop1963__ = __leftop1964__ * __rightop1965__;
          int __leftop1967__ = 32;
          int __rightop1968__ = 0;
          int __rightop1966__ = __leftop1967__ + __rightop1968__;
          int __rightop1962__ = __leftop1963__ + __rightop1966__;
          int __sizeof1960__ = __leftop1961__ + __rightop1962__;
          int __high1969__ = __left1868__ + __sizeof1960__;
          assertvalidmemory(__left1868__, __high1969__);
          // __left1868__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits1970__ <-- ((32 + 0) + (32 * j))
          int __leftop1972__ = 32;
          int __rightop1973__ = 0;
          int __leftop1971__ = __leftop1972__ + __rightop1973__;
          int __leftop1975__ = 32;
          int __rightop1976__ = (int) __j__; //varexpr
          int __rightop1974__ = __leftop1975__ * __rightop1976__;
          int __offsetinbits1970__ = __leftop1971__ + __rightop1974__;
          // __offsetinbits1970__ = ((32 + 0) + (32 * j))
          int __offset1977__ = __offsetinbits1970__ >> 3;
          int __shift1978__ = __offsetinbits1970__ - (__offset1977__ << 3);
          int __rightele1867__ = ((*(int *)(__left1868__ + __offset1977__))  >> __shift1978__) & 0xffffffff;
          __contents___hash->add((int)__leftele1866__, (int)__rightele1867__);
          __contents___hashinv->add((int)__rightele1867__, (int)__leftele1866__);
          }
        }
      }
    }
  }


// build rule7
  {
  int __tempvar1980__ = 0;
  // __left1983__ <-- d.s
  // __left1984__ <-- d
  int __left1984__ = (int) d; //varexpr
  // __left1984__ = d
  int __left1983__ = (__left1984__ + 0);
  int __leftop1986__ = 32;
  int __leftop1988__ = 32;
  int __leftop1990__ = 32;
  int __leftop1992__ = 32;
  int __leftop1994__ = 32;
  int __leftop1996__ = 32;
  int __rightop1997__ = 0;
  int __rightop1995__ = __leftop1996__ + __rightop1997__;
  int __rightop1993__ = __leftop1994__ + __rightop1995__;
  int __rightop1991__ = __leftop1992__ + __rightop1993__;
  int __rightop1989__ = __leftop1990__ + __rightop1991__;
  int __rightop1987__ = __leftop1988__ + __rightop1989__;
  int __sizeof1985__ = __leftop1986__ + __rightop1987__;
  int __high1998__ = __left1983__ + __sizeof1985__;
  assertvalidmemory(__left1983__, __high1998__);
  // __left1983__ = d.s
  // __offsetinbits1999__ <-- (32 + (32 + (32 + 0)))
  int __leftop2000__ = 32;
  int __leftop2002__ = 32;
  int __leftop2004__ = 32;
  int __rightop2005__ = 0;
  int __rightop2003__ = __leftop2004__ + __rightop2005__;
  int __rightop2001__ = __leftop2002__ + __rightop2003__;
  int __offsetinbits1999__ = __leftop2000__ + __rightop2001__;
  // __offsetinbits1999__ = (32 + (32 + (32 + 0)))
  int __offset2006__ = __offsetinbits1999__ >> 3;
  int __shift2007__ = __offsetinbits1999__ - (__offset2006__ << 3);
  int __leftop1982__ = ((*(int *)(__left1983__ + __offset2006__))  >> __shift2007__) & 0xffffffff;
  int __rightop2008__ = 1;
  int __tempvar1981__ = __leftop1982__ - __rightop2008__;
  for (int __j__ = __tempvar1980__; __j__ <= __tempvar1981__; __j__++)
    {
    //(j in? __UsedInode__)
    int __element2011__ = (int) __j__; //varexpr
    int __leftop2010__ = __UsedInode___hash->contains(__element2011__);
    int __tempvar2009__ = !__leftop2010__;
    if (__tempvar2009__)
      {
      int __element2012__ = (int) __j__; //varexpr
      __FreeInode___hash->add((int)__element2012__, (int)__element2012__);
      }
    }
  }


// build rule17
  {
  for (SimpleIterator* __j___iterator = __UsedInode___hash->iterator(); __j___iterator->hasNext(); )
    {
    int __j__ = (int) __j___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); )
      {
      int __itb__ = (int) __itb___iterator->next();
      //true
      int __tempvar2013__ = 1;
      if (__tempvar2013__)
        {
        int __leftele2014__ = (int) __j__; //varexpr
        // __left2016__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left2017__ <-- cast(__InodeTable__, d.b[itb])
        // __left2019__ <-- d
        int __left2019__ = (int) d; //varexpr
        // __left2019__ = d
        // __offsetinbits2020__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop2021__ = 0;
        int __leftop2025__ = 8;
        // __left2027__ <-- d.s
        // __left2028__ <-- d
        int __left2028__ = (int) d; //varexpr
        // __left2028__ = d
        int __left2027__ = (__left2028__ + 0);
        int __leftop2030__ = 32;
        int __leftop2032__ = 32;
        int __leftop2034__ = 32;
        int __leftop2036__ = 32;
        int __leftop2038__ = 32;
        int __leftop2040__ = 32;
        int __rightop2041__ = 0;
        int __rightop2039__ = __leftop2040__ + __rightop2041__;
        int __rightop2037__ = __leftop2038__ + __rightop2039__;
        int __rightop2035__ = __leftop2036__ + __rightop2037__;
        int __rightop2033__ = __leftop2034__ + __rightop2035__;
        int __rightop2031__ = __leftop2032__ + __rightop2033__;
        int __sizeof2029__ = __leftop2030__ + __rightop2031__;
        int __high2042__ = __left2027__ + __sizeof2029__;
        assertvalidmemory(__left2027__, __high2042__);
        // __left2027__ = d.s
        // __offsetinbits2043__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop2044__ = 32;
        int __leftop2046__ = 32;
        int __leftop2048__ = 32;
        int __leftop2050__ = 32;
        int __leftop2052__ = 32;
        int __rightop2053__ = 0;
        int __rightop2051__ = __leftop2052__ + __rightop2053__;
        int __rightop2049__ = __leftop2050__ + __rightop2051__;
        int __rightop2047__ = __leftop2048__ + __rightop2049__;
        int __rightop2045__ = __leftop2046__ + __rightop2047__;
        int __offsetinbits2043__ = __leftop2044__ + __rightop2045__;
        // __offsetinbits2043__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset2054__ = __offsetinbits2043__ >> 3;
        int __shift2055__ = __offsetinbits2043__ - (__offset2054__ << 3);
        int __rightop2026__ = ((*(int *)(__left2027__ + __offset2054__))  >> __shift2055__) & 0xffffffff;
        int __leftop2024__ = __leftop2025__ * __rightop2026__;
        int __rightop2056__ = 0;
        int __leftop2023__ = __leftop2024__ + __rightop2056__;
        int __rightop2057__ = (int) __itb__; //varexpr
        int __rightop2022__ = __leftop2023__ * __rightop2057__;
        int __offsetinbits2020__ = __leftop2021__ + __rightop2022__;
        // __offsetinbits2020__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset2058__ = __offsetinbits2020__ >> 3;
        int __expr2018__ = (__left2019__ + __offset2058__);
        int __leftop2061__ = 8;
        // __left2063__ <-- d.s
        // __left2064__ <-- d
        int __left2064__ = (int) d; //varexpr
        // __left2064__ = d
        int __left2063__ = (__left2064__ + 0);
        int __leftop2066__ = 32;
        int __leftop2068__ = 32;
        int __leftop2070__ = 32;
        int __leftop2072__ = 32;
        int __leftop2074__ = 32;
        int __leftop2076__ = 32;
        int __rightop2077__ = 0;
        int __rightop2075__ = __leftop2076__ + __rightop2077__;
        int __rightop2073__ = __leftop2074__ + __rightop2075__;
        int __rightop2071__ = __leftop2072__ + __rightop2073__;
        int __rightop2069__ = __leftop2070__ + __rightop2071__;
        int __rightop2067__ = __leftop2068__ + __rightop2069__;
        int __sizeof2065__ = __leftop2066__ + __rightop2067__;
        int __high2078__ = __left2063__ + __sizeof2065__;
        assertvalidmemory(__left2063__, __high2078__);
        // __left2063__ = d.s
        // __offsetinbits2079__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop2080__ = 32;
        int __leftop2082__ = 32;
        int __leftop2084__ = 32;
        int __leftop2086__ = 32;
        int __leftop2088__ = 32;
        int __rightop2089__ = 0;
        int __rightop2087__ = __leftop2088__ + __rightop2089__;
        int __rightop2085__ = __leftop2086__ + __rightop2087__;
        int __rightop2083__ = __leftop2084__ + __rightop2085__;
        int __rightop2081__ = __leftop2082__ + __rightop2083__;
        int __offsetinbits2079__ = __leftop2080__ + __rightop2081__;
        // __offsetinbits2079__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset2090__ = __offsetinbits2079__ >> 3;
        int __shift2091__ = __offsetinbits2079__ - (__offset2090__ << 3);
        int __rightop2062__ = ((*(int *)(__left2063__ + __offset2090__))  >> __shift2091__) & 0xffffffff;
        int __leftop2060__ = __leftop2061__ * __rightop2062__;
        int __rightop2092__ = 0;
        int __sizeof2059__ = __leftop2060__ + __rightop2092__;
        int __high2093__ = __expr2018__ + __sizeof2059__;
        assertvalidmemory(__expr2018__, __high2093__);
        int __left2017__ = (int) __expr2018__;
        // __left2017__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits2094__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __leftop2095__ = 0;
        int __leftop2098__ = 32;
        int __leftop2101__ = 32;
        int __rightop2102__ = 12;
        int __leftop2100__ = __leftop2101__ * __rightop2102__;
        int __leftop2104__ = 32;
        int __rightop2105__ = 0;
        int __rightop2103__ = __leftop2104__ + __rightop2105__;
        int __rightop2099__ = __leftop2100__ + __rightop2103__;
        int __leftop2097__ = __leftop2098__ + __rightop2099__;
        int __rightop2106__ = (int) __j__; //varexpr
        int __rightop2096__ = __leftop2097__ * __rightop2106__;
        int __offsetinbits2094__ = __leftop2095__ + __rightop2096__;
        // __offsetinbits2094__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __offset2107__ = __offsetinbits2094__ >> 3;
        int __left2016__ = (__left2017__ + __offset2107__);
        int __leftop2109__ = 32;
        int __leftop2112__ = 32;
        int __rightop2113__ = 12;
        int __leftop2111__ = __leftop2112__ * __rightop2113__;
        int __leftop2115__ = 32;
        int __rightop2116__ = 0;
        int __rightop2114__ = __leftop2115__ + __rightop2116__;
        int __rightop2110__ = __leftop2111__ + __rightop2114__;
        int __sizeof2108__ = __leftop2109__ + __rightop2110__;
        int __high2117__ = __left2016__ + __sizeof2108__;
        assertvalidmemory(__left2016__, __high2117__);
        // __left2016__ = cast(__InodeTable__, d.b[itb]).itable[j]
        int __rightele2015__ = ((*(int *)(__left2016__ + 0))  >> 0) & 0xffffffff;
        __filesize___hash->add((int)__leftele2014__, (int)__rightele2015__);
        }
      }
    }
  }


// build rule18
  {
  int __tempvar2119__ = 0;
  // __left2122__ <-- d.s
  // __left2123__ <-- d
  int __left2123__ = (int) d; //varexpr
  // __left2123__ = d
  int __left2122__ = (__left2123__ + 0);
  int __leftop2125__ = 32;
  int __leftop2127__ = 32;
  int __leftop2129__ = 32;
  int __leftop2131__ = 32;
  int __leftop2133__ = 32;
  int __leftop2135__ = 32;
  int __rightop2136__ = 0;
  int __rightop2134__ = __leftop2135__ + __rightop2136__;
  int __rightop2132__ = __leftop2133__ + __rightop2134__;
  int __rightop2130__ = __leftop2131__ + __rightop2132__;
  int __rightop2128__ = __leftop2129__ + __rightop2130__;
  int __rightop2126__ = __leftop2127__ + __rightop2128__;
  int __sizeof2124__ = __leftop2125__ + __rightop2126__;
  int __high2137__ = __left2122__ + __sizeof2124__;
  assertvalidmemory(__left2122__, __high2137__);
  // __left2122__ = d.s
  // __offsetinbits2138__ <-- (32 + (32 + 0))
  int __leftop2139__ = 32;
  int __leftop2141__ = 32;
  int __rightop2142__ = 0;
  int __rightop2140__ = __leftop2141__ + __rightop2142__;
  int __offsetinbits2138__ = __leftop2139__ + __rightop2140__;
  // __offsetinbits2138__ = (32 + (32 + 0))
  int __offset2143__ = __offsetinbits2138__ >> 3;
  int __shift2144__ = __offsetinbits2138__ - (__offset2143__ << 3);
  int __leftop2121__ = ((*(int *)(__left2122__ + __offset2143__))  >> __shift2144__) & 0xffffffff;
  int __rightop2145__ = 1;
  int __tempvar2120__ = __leftop2121__ - __rightop2145__;
  for (int __j__ = __tempvar2119__; __j__ <= __tempvar2120__; __j__++)
    {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); )
      {
      int __bbb__ = (int) __bbb___iterator->next();
      //(cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == false)
      // __left2148__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left2150__ <-- d
      int __left2150__ = (int) d; //varexpr
      // __left2150__ = d
      // __offsetinbits2151__ <-- (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __leftop2152__ = 0;
      int __leftop2156__ = 8;
      // __left2158__ <-- d.s
      // __left2159__ <-- d
      int __left2159__ = (int) d; //varexpr
      // __left2159__ = d
      int __left2158__ = (__left2159__ + 0);
      int __leftop2161__ = 32;
      int __leftop2163__ = 32;
      int __leftop2165__ = 32;
      int __leftop2167__ = 32;
      int __leftop2169__ = 32;
      int __leftop2171__ = 32;
      int __rightop2172__ = 0;
      int __rightop2170__ = __leftop2171__ + __rightop2172__;
      int __rightop2168__ = __leftop2169__ + __rightop2170__;
      int __rightop2166__ = __leftop2167__ + __rightop2168__;
      int __rightop2164__ = __leftop2165__ + __rightop2166__;
      int __rightop2162__ = __leftop2163__ + __rightop2164__;
      int __sizeof2160__ = __leftop2161__ + __rightop2162__;
      int __high2173__ = __left2158__ + __sizeof2160__;
      assertvalidmemory(__left2158__, __high2173__);
      // __left2158__ = d.s
      // __offsetinbits2174__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2175__ = 32;
      int __leftop2177__ = 32;
      int __leftop2179__ = 32;
      int __leftop2181__ = 32;
      int __leftop2183__ = 32;
      int __rightop2184__ = 0;
      int __rightop2182__ = __leftop2183__ + __rightop2184__;
      int __rightop2180__ = __leftop2181__ + __rightop2182__;
      int __rightop2178__ = __leftop2179__ + __rightop2180__;
      int __rightop2176__ = __leftop2177__ + __rightop2178__;
      int __offsetinbits2174__ = __leftop2175__ + __rightop2176__;
      // __offsetinbits2174__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2185__ = __offsetinbits2174__ >> 3;
      int __shift2186__ = __offsetinbits2174__ - (__offset2185__ << 3);
      int __rightop2157__ = ((*(int *)(__left2158__ + __offset2185__))  >> __shift2186__) & 0xffffffff;
      int __leftop2155__ = __leftop2156__ * __rightop2157__;
      int __rightop2187__ = 0;
      int __leftop2154__ = __leftop2155__ + __rightop2187__;
      int __rightop2188__ = (int) __bbb__; //varexpr
      int __rightop2153__ = __leftop2154__ * __rightop2188__;
      int __offsetinbits2151__ = __leftop2152__ + __rightop2153__;
      // __offsetinbits2151__ = (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __offset2189__ = __offsetinbits2151__ >> 3;
      int __expr2149__ = (__left2150__ + __offset2189__);
      int __leftop2192__ = 8;
      // __left2194__ <-- d.s
      // __left2195__ <-- d
      int __left2195__ = (int) d; //varexpr
      // __left2195__ = d
      int __left2194__ = (__left2195__ + 0);
      int __leftop2197__ = 32;
      int __leftop2199__ = 32;
      int __leftop2201__ = 32;
      int __leftop2203__ = 32;
      int __leftop2205__ = 32;
      int __leftop2207__ = 32;
      int __rightop2208__ = 0;
      int __rightop2206__ = __leftop2207__ + __rightop2208__;
      int __rightop2204__ = __leftop2205__ + __rightop2206__;
      int __rightop2202__ = __leftop2203__ + __rightop2204__;
      int __rightop2200__ = __leftop2201__ + __rightop2202__;
      int __rightop2198__ = __leftop2199__ + __rightop2200__;
      int __sizeof2196__ = __leftop2197__ + __rightop2198__;
      int __high2209__ = __left2194__ + __sizeof2196__;
      assertvalidmemory(__left2194__, __high2209__);
      // __left2194__ = d.s
      // __offsetinbits2210__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2211__ = 32;
      int __leftop2213__ = 32;
      int __leftop2215__ = 32;
      int __leftop2217__ = 32;
      int __leftop2219__ = 32;
      int __rightop2220__ = 0;
      int __rightop2218__ = __leftop2219__ + __rightop2220__;
      int __rightop2216__ = __leftop2217__ + __rightop2218__;
      int __rightop2214__ = __leftop2215__ + __rightop2216__;
      int __rightop2212__ = __leftop2213__ + __rightop2214__;
      int __offsetinbits2210__ = __leftop2211__ + __rightop2212__;
      // __offsetinbits2210__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2221__ = __offsetinbits2210__ >> 3;
      int __shift2222__ = __offsetinbits2210__ - (__offset2221__ << 3);
      int __rightop2193__ = ((*(int *)(__left2194__ + __offset2221__))  >> __shift2222__) & 0xffffffff;
      int __leftop2191__ = __leftop2192__ * __rightop2193__;
      int __rightop2223__ = 0;
      int __sizeof2190__ = __leftop2191__ + __rightop2223__;
      int __high2224__ = __expr2149__ + __sizeof2190__;
      assertvalidmemory(__expr2149__, __high2224__);
      int __left2148__ = (int) __expr2149__;
      // __left2148__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits2225__ <-- (0 + (1 * j))
      int __leftop2226__ = 0;
      int __leftop2228__ = 1;
      int __rightop2229__ = (int) __j__; //varexpr
      int __rightop2227__ = __leftop2228__ * __rightop2229__;
      int __offsetinbits2225__ = __leftop2226__ + __rightop2227__;
      // __offsetinbits2225__ = (0 + (1 * j))
      int __offset2230__ = __offsetinbits2225__ >> 3;
      int __shift2231__ = __offsetinbits2225__ - (__offset2230__ << 3);
      int __leftop2147__ = ((*(int *)(__left2148__ + __offset2230__))  >> __shift2231__) & 0x1;
      int __rightop2232__ = 0;
      int __tempvar2146__ = __leftop2147__ == __rightop2232__;
      if (__tempvar2146__)
        {
        int __leftele2233__ = (int) __j__; //varexpr
        int __rightele2234__ = 101;
        __blockstatus___hash->add((int)__leftele2233__, (int)__rightele2234__);
        }
      }
    }
  }


// build rule19
  {
  int __tempvar2236__ = 0;
  // __left2239__ <-- d.s
  // __left2240__ <-- d
  int __left2240__ = (int) d; //varexpr
  // __left2240__ = d
  int __left2239__ = (__left2240__ + 0);
  int __leftop2242__ = 32;
  int __leftop2244__ = 32;
  int __leftop2246__ = 32;
  int __leftop2248__ = 32;
  int __leftop2250__ = 32;
  int __leftop2252__ = 32;
  int __rightop2253__ = 0;
  int __rightop2251__ = __leftop2252__ + __rightop2253__;
  int __rightop2249__ = __leftop2250__ + __rightop2251__;
  int __rightop2247__ = __leftop2248__ + __rightop2249__;
  int __rightop2245__ = __leftop2246__ + __rightop2247__;
  int __rightop2243__ = __leftop2244__ + __rightop2245__;
  int __sizeof2241__ = __leftop2242__ + __rightop2243__;
  int __high2254__ = __left2239__ + __sizeof2241__;
  assertvalidmemory(__left2239__, __high2254__);
  // __left2239__ = d.s
  // __offsetinbits2255__ <-- (32 + (32 + 0))
  int __leftop2256__ = 32;
  int __leftop2258__ = 32;
  int __rightop2259__ = 0;
  int __rightop2257__ = __leftop2258__ + __rightop2259__;
  int __offsetinbits2255__ = __leftop2256__ + __rightop2257__;
  // __offsetinbits2255__ = (32 + (32 + 0))
  int __offset2260__ = __offsetinbits2255__ >> 3;
  int __shift2261__ = __offsetinbits2255__ - (__offset2260__ << 3);
  int __leftop2238__ = ((*(int *)(__left2239__ + __offset2260__))  >> __shift2261__) & 0xffffffff;
  int __rightop2262__ = 1;
  int __tempvar2237__ = __leftop2238__ - __rightop2262__;
  for (int __j__ = __tempvar2236__; __j__ <= __tempvar2237__; __j__++)
    {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); )
      {
      int __bbb__ = (int) __bbb___iterator->next();
      //(cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == true)
      // __left2265__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left2267__ <-- d
      int __left2267__ = (int) d; //varexpr
      // __left2267__ = d
      // __offsetinbits2268__ <-- (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __leftop2269__ = 0;
      int __leftop2273__ = 8;
      // __left2275__ <-- d.s
      // __left2276__ <-- d
      int __left2276__ = (int) d; //varexpr
      // __left2276__ = d
      int __left2275__ = (__left2276__ + 0);
      int __leftop2278__ = 32;
      int __leftop2280__ = 32;
      int __leftop2282__ = 32;
      int __leftop2284__ = 32;
      int __leftop2286__ = 32;
      int __leftop2288__ = 32;
      int __rightop2289__ = 0;
      int __rightop2287__ = __leftop2288__ + __rightop2289__;
      int __rightop2285__ = __leftop2286__ + __rightop2287__;
      int __rightop2283__ = __leftop2284__ + __rightop2285__;
      int __rightop2281__ = __leftop2282__ + __rightop2283__;
      int __rightop2279__ = __leftop2280__ + __rightop2281__;
      int __sizeof2277__ = __leftop2278__ + __rightop2279__;
      int __high2290__ = __left2275__ + __sizeof2277__;
      assertvalidmemory(__left2275__, __high2290__);
      // __left2275__ = d.s
      // __offsetinbits2291__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2292__ = 32;
      int __leftop2294__ = 32;
      int __leftop2296__ = 32;
      int __leftop2298__ = 32;
      int __leftop2300__ = 32;
      int __rightop2301__ = 0;
      int __rightop2299__ = __leftop2300__ + __rightop2301__;
      int __rightop2297__ = __leftop2298__ + __rightop2299__;
      int __rightop2295__ = __leftop2296__ + __rightop2297__;
      int __rightop2293__ = __leftop2294__ + __rightop2295__;
      int __offsetinbits2291__ = __leftop2292__ + __rightop2293__;
      // __offsetinbits2291__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2302__ = __offsetinbits2291__ >> 3;
      int __shift2303__ = __offsetinbits2291__ - (__offset2302__ << 3);
      int __rightop2274__ = ((*(int *)(__left2275__ + __offset2302__))  >> __shift2303__) & 0xffffffff;
      int __leftop2272__ = __leftop2273__ * __rightop2274__;
      int __rightop2304__ = 0;
      int __leftop2271__ = __leftop2272__ + __rightop2304__;
      int __rightop2305__ = (int) __bbb__; //varexpr
      int __rightop2270__ = __leftop2271__ * __rightop2305__;
      int __offsetinbits2268__ = __leftop2269__ + __rightop2270__;
      // __offsetinbits2268__ = (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __offset2306__ = __offsetinbits2268__ >> 3;
      int __expr2266__ = (__left2267__ + __offset2306__);
      int __leftop2309__ = 8;
      // __left2311__ <-- d.s
      // __left2312__ <-- d
      int __left2312__ = (int) d; //varexpr
      // __left2312__ = d
      int __left2311__ = (__left2312__ + 0);
      int __leftop2314__ = 32;
      int __leftop2316__ = 32;
      int __leftop2318__ = 32;
      int __leftop2320__ = 32;
      int __leftop2322__ = 32;
      int __leftop2324__ = 32;
      int __rightop2325__ = 0;
      int __rightop2323__ = __leftop2324__ + __rightop2325__;
      int __rightop2321__ = __leftop2322__ + __rightop2323__;
      int __rightop2319__ = __leftop2320__ + __rightop2321__;
      int __rightop2317__ = __leftop2318__ + __rightop2319__;
      int __rightop2315__ = __leftop2316__ + __rightop2317__;
      int __sizeof2313__ = __leftop2314__ + __rightop2315__;
      int __high2326__ = __left2311__ + __sizeof2313__;
      assertvalidmemory(__left2311__, __high2326__);
      // __left2311__ = d.s
      // __offsetinbits2327__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2328__ = 32;
      int __leftop2330__ = 32;
      int __leftop2332__ = 32;
      int __leftop2334__ = 32;
      int __leftop2336__ = 32;
      int __rightop2337__ = 0;
      int __rightop2335__ = __leftop2336__ + __rightop2337__;
      int __rightop2333__ = __leftop2334__ + __rightop2335__;
      int __rightop2331__ = __leftop2332__ + __rightop2333__;
      int __rightop2329__ = __leftop2330__ + __rightop2331__;
      int __offsetinbits2327__ = __leftop2328__ + __rightop2329__;
      // __offsetinbits2327__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2338__ = __offsetinbits2327__ >> 3;
      int __shift2339__ = __offsetinbits2327__ - (__offset2338__ << 3);
      int __rightop2310__ = ((*(int *)(__left2311__ + __offset2338__))  >> __shift2339__) & 0xffffffff;
      int __leftop2308__ = __leftop2309__ * __rightop2310__;
      int __rightop2340__ = 0;
      int __sizeof2307__ = __leftop2308__ + __rightop2340__;
      int __high2341__ = __expr2266__ + __sizeof2307__;
      assertvalidmemory(__expr2266__, __high2341__);
      int __left2265__ = (int) __expr2266__;
      // __left2265__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits2342__ <-- (0 + (1 * j))
      int __leftop2343__ = 0;
      int __leftop2345__ = 1;
      int __rightop2346__ = (int) __j__; //varexpr
      int __rightop2344__ = __leftop2345__ * __rightop2346__;
      int __offsetinbits2342__ = __leftop2343__ + __rightop2344__;
      // __offsetinbits2342__ = (0 + (1 * j))
      int __offset2347__ = __offsetinbits2342__ >> 3;
      int __shift2348__ = __offsetinbits2342__ - (__offset2347__ << 3);
      int __leftop2264__ = ((*(int *)(__left2265__ + __offset2347__))  >> __shift2348__) & 0x1;
      int __rightop2349__ = 1;
      int __tempvar2263__ = __leftop2264__ == __rightop2349__;
      if (__tempvar2263__)
        {
        int __leftele2350__ = (int) __j__; //varexpr
        int __rightele2351__ = 100;
        __blockstatus___hash->add((int)__leftele2350__, (int)__rightele2351__);
        }
      }
    }
  }


// checking c1
  {
  for (SimpleIterator* __u___iterator = __UsedInode___hash->iterator(); __u___iterator->hasNext(); )
    {
    int __u__ = (int) __u___iterator->next();
    int maybe = 0;
    int __domain2355__ = (int) __u__; //varexpr
    int __leftop2354__;
    int __found2356__ = __inodestatus___hash->get(__domain2355__, __leftop2354__);
    if (!__found2356__) { maybe = 1; }
    int __rightop2357__ = 100;
    int __constraintboolean2353__ = __leftop2354__ == __rightop2357__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 1. ");
      }
    else if (!__constraintboolean2353__)
      {
      __Success = 0;
      printf("fail 1. ");
      }
    }
  }


// checking c2
  {
  for (SimpleIterator* __f___iterator = __FreeInode___hash->iterator(); __f___iterator->hasNext(); )
    {
    int __f__ = (int) __f___iterator->next();
    int maybe = 0;
    int __domain2360__ = (int) __f__; //varexpr
    int __leftop2359__;
    int __found2361__ = __inodestatus___hash->get(__domain2360__, __leftop2359__);
    if (!__found2361__) { maybe = 1; }
    int __rightop2362__ = 101;
    int __constraintboolean2358__ = __leftop2359__ == __rightop2362__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 2. ");
      }
    else if (!__constraintboolean2358__)
      {
      __Success = 0;
      printf("fail 2. ");
      }
    }
  }


// checking c3
  {
  for (SimpleIterator* __u___iterator = __UsedBlock___hash->iterator(); __u___iterator->hasNext(); )
    {
    int __u__ = (int) __u___iterator->next();
    int maybe = 0;
    int __domain2365__ = (int) __u__; //varexpr
    int __leftop2364__;
    int __found2366__ = __blockstatus___hash->get(__domain2365__, __leftop2364__);
    if (!__found2366__) { maybe = 1; }
    int __rightop2367__ = 100;
    int __constraintboolean2363__ = __leftop2364__ == __rightop2367__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 3. ");
      }
    else if (!__constraintboolean2363__)
      {
      __Success = 0;
      printf("fail 3. ");
      }
    }
  }


// checking c4
  {
  for (SimpleIterator* __f___iterator = __FreeBlock___hash->iterator(); __f___iterator->hasNext(); )
    {
    int __f__ = (int) __f___iterator->next();
    int maybe = 0;
    int __domain2370__ = (int) __f__; //varexpr
    int __leftop2369__;
    int __found2371__ = __blockstatus___hash->get(__domain2370__, __leftop2369__);
    if (!__found2371__) { maybe = 1; }
    int __rightop2372__ = 101;
    int __constraintboolean2368__ = __leftop2369__ == __rightop2372__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 4. ");
      }
    else if (!__constraintboolean2368__)
      {
      __Success = 0;
      printf("fail 4. ");
      }
    }
  }


// checking c5
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    int maybe = 0;
    int __domain2375__ = (int) __i__; //varexpr
    int __leftop2374__;
    int __found2376__ = __referencecount___hash->get(__domain2375__, __leftop2374__);
    if (!__found2376__) { maybe = 1; }
    int __rightop2377__ = __inodeof___hashinv->count(__i__);
    int __constraintboolean2373__ = __leftop2374__ == __rightop2377__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 5. ");
      }
    else if (!__constraintboolean2373__)
      {
      __Success = 0;
      printf("fail 5. ");
      }
    }
  }


// checking c6
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    int maybe = 0;
    int __domain2380__ = (int) __i__; //varexpr
    int __leftop2379__;
    int __found2381__ = __filesize___hash->get(__domain2380__, __leftop2379__);
    if (!__found2381__) { maybe = 1; }
    int __leftop2383__ = __contents___hash->count(__i__);
    int __rightop2384__ = 8192;
    int __rightop2382__ = __leftop2383__ * __rightop2384__;
    int __constraintboolean2378__ = __leftop2379__ <= __rightop2382__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 6. ");
      }
    else if (!__constraintboolean2378__)
      {
      __Success = 0;
      printf("fail 6. ");
      }
    }
  }


// checking c7
  {
  for (SimpleIterator* __b___iterator = __FileDirectoryBlock___hash->iterator(); __b___iterator->hasNext(); )
    {
    int __b__ = (int) __b___iterator->next();
    int maybe = 0;
    int __leftop2386__ = __contents___hashinv->count(__b__);
    int __rightop2387__ = 1;
    int __constraintboolean2385__ = __leftop2386__ == __rightop2387__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 7. ");
      }
    else if (!__constraintboolean2385__)
      {
      __Success = 0;
      printf("fail 7. ");
      }
    }
  }


// checking c8
  {
  int maybe = 0;
  int __leftop2389__ = __SuperBlock___hash->count();
  int __rightop2390__ = 1;
  int __constraintboolean2388__ = __leftop2389__ == __rightop2390__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 8. ");
    }
  else if (!__constraintboolean2388__)
    {
    __Success = 0;
    printf("fail 8. ");
    }
  }


// checking c9
  {
  int maybe = 0;
  int __leftop2392__ = __GroupBlock___hash->count();
  int __rightop2393__ = 1;
  int __constraintboolean2391__ = __leftop2392__ == __rightop2393__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 9. ");
    }
  else if (!__constraintboolean2391__)
    {
    __Success = 0;
    printf("fail 9. ");
    }
  }


// checking c10
  {
  int maybe = 0;
  int __leftop2395__ = __InodeTableBlock___hash->count();
  int __rightop2396__ = 1;
  int __constraintboolean2394__ = __leftop2395__ == __rightop2396__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 10. ");
    }
  else if (!__constraintboolean2394__)
    {
    __Success = 0;
    printf("fail 10. ");
    }
  }


// checking c11
  {
  int maybe = 0;
  int __leftop2398__ = __InodeBitmapBlock___hash->count();
  int __rightop2399__ = 1;
  int __constraintboolean2397__ = __leftop2398__ == __rightop2399__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 11. ");
    }
  else if (!__constraintboolean2397__)
    {
    __Success = 0;
    printf("fail 11. ");
    }
  }


// checking c12
  {
  int maybe = 0;
  int __leftop2401__ = __BlockBitmapBlock___hash->count();
  int __rightop2402__ = 1;
  int __constraintboolean2400__ = __leftop2401__ == __rightop2402__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 12. ");
    }
  else if (!__constraintboolean2400__)
    {
    __Success = 0;
    printf("fail 12. ");
    }
  }


// checking c13
  {
  int maybe = 0;
  int __leftop2404__ = __RootDirectoryInode___hash->count();
  int __rightop2405__ = 1;
  int __constraintboolean2403__ = __leftop2404__ == __rightop2405__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 13. ");
    }
  else if (!__constraintboolean2403__)
    {
    __Success = 0;
    printf("fail 13. ");
    }
  }


if (__Success) { //printf("all tests passed"); 
}
