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
SimpleHash* __inodeof___hash = new SimpleHash();
SimpleHash* __contents___hash = new SimpleHash();
SimpleHash* __inodestatus___hash = new SimpleHash();
SimpleHash* __blockstatus___hash = new SimpleHash();


// build rule1
{
  //true
  int __tempvar0__ = 1;
  if (__tempvar0__) {
    int __element1__ = 0;
    __SuperBlock___hash->add((int)__element1__, (int)__element1__);
  }
}


// build rule2
{
  //true
  int __tempvar2__ = 1;
  if (__tempvar2__) {
    int __element3__ = 1;
    __GroupBlock___hash->add((int)__element3__, (int)__element3__);
  }
}


// build rule3
{
  //d.g.InodeTableBlock < d.s.NumberofBlocks
  // __left6__ <-- d.g
  // __left7__ <-- d
  int __left7__ = (int) d;
  // __left7__ = d
  // __offsetinbits8__ <-- 0 + 8 * d.s.blocksize + 0 * 1
  int __leftop9__ = 0;
  int __leftop13__ = 8;
  // __left15__ <-- d.s
  // __left16__ <-- d
  int __left16__ = (int) d;
  // __left16__ = d
  int __left15__ = (__left16__ + 0);
  // __left15__ = d.s
  // __offsetinbits17__ <-- 32 + 32 + 32 + 32 + 32 + 0
  int __leftop18__ = 32;
  int __leftop20__ = 32;
  int __leftop22__ = 32;
  int __leftop24__ = 32;
  int __leftop26__ = 32;
  int __rightop27__ = 0;
  int __rightop25__ = __leftop26__ + __rightop27__;
  int __rightop23__ = __leftop24__ + __rightop25__;
  int __rightop21__ = __leftop22__ + __rightop23__;
  int __rightop19__ = __leftop20__ + __rightop21__;
  int __offsetinbits17__ = __leftop18__ + __rightop19__;
  // __offsetinbits17__ = 32 + 32 + 32 + 32 + 32 + 0
  int __offset28__ = __offsetinbits17__ >> 3;
  int __shift29__ = __offsetinbits17__ - (__offset28__ << 3);
  int __rightop14__ = ((*(int *)(__left15__ + __offset28__))  >> __shift29__) & 0xffffffff;
  int __leftop12__ = __leftop13__ * __rightop14__;
  int __rightop30__ = 0;
  int __leftop11__ = __leftop12__ + __rightop30__;
  int __rightop31__ = 1;
  int __rightop10__ = __leftop11__ * __rightop31__;
  int __offsetinbits8__ = __leftop9__ + __rightop10__;
  // __offsetinbits8__ = 0 + 8 * d.s.blocksize + 0 * 1
  int __offset32__ = __offsetinbits8__ >> 3;
  int __left6__ = (__left7__ + __offset32__);
  // __left6__ = d.g
  // __offsetinbits33__ <-- 32 + 32 + 0
  int __leftop34__ = 32;
  int __leftop36__ = 32;
  int __rightop37__ = 0;
  int __rightop35__ = __leftop36__ + __rightop37__;
  int __offsetinbits33__ = __leftop34__ + __rightop35__;
  // __offsetinbits33__ = 32 + 32 + 0
  int __offset38__ = __offsetinbits33__ >> 3;
  int __shift39__ = __offsetinbits33__ - (__offset38__ << 3);
  int __leftop5__ = ((*(int *)(__left6__ + __offset38__))  >> __shift39__) & 0xffffffff;
  // __left41__ <-- d.s
  // __left42__ <-- d
  int __left42__ = (int) d;
  // __left42__ = d
  int __left41__ = (__left42__ + 0);
  // __left41__ = d.s
  // __offsetinbits43__ <-- 32 + 32 + 0
  int __leftop44__ = 32;
  int __leftop46__ = 32;
  int __rightop47__ = 0;
  int __rightop45__ = __leftop46__ + __rightop47__;
  int __offsetinbits43__ = __leftop44__ + __rightop45__;
  // __offsetinbits43__ = 32 + 32 + 0
  int __offset48__ = __offsetinbits43__ >> 3;
  int __shift49__ = __offsetinbits43__ - (__offset48__ << 3);
  int __rightop40__ = ((*(int *)(__left41__ + __offset48__))  >> __shift49__) & 0xffffffff;
  int __tempvar4__ = __leftop5__ < __rightop40__;
  if (__tempvar4__) {
    // __left51__ <-- d.g
    // __left52__ <-- d
    int __left52__ = (int) d;
    // __left52__ = d
    // __offsetinbits53__ <-- 0 + 8 * d.s.blocksize + 0 * 1
    int __leftop54__ = 0;
    int __leftop58__ = 8;
    // __left60__ <-- d.s
    // __left61__ <-- d
    int __left61__ = (int) d;
    // __left61__ = d
    int __left60__ = (__left61__ + 0);
    // __left60__ = d.s
    // __offsetinbits62__ <-- 32 + 32 + 32 + 32 + 32 + 0
    int __leftop63__ = 32;
    int __leftop65__ = 32;
    int __leftop67__ = 32;
    int __leftop69__ = 32;
    int __leftop71__ = 32;
    int __rightop72__ = 0;
    int __rightop70__ = __leftop71__ + __rightop72__;
    int __rightop68__ = __leftop69__ + __rightop70__;
    int __rightop66__ = __leftop67__ + __rightop68__;
    int __rightop64__ = __leftop65__ + __rightop66__;
    int __offsetinbits62__ = __leftop63__ + __rightop64__;
    // __offsetinbits62__ = 32 + 32 + 32 + 32 + 32 + 0
    int __offset73__ = __offsetinbits62__ >> 3;
    int __shift74__ = __offsetinbits62__ - (__offset73__ << 3);
    int __rightop59__ = ((*(int *)(__left60__ + __offset73__))  >> __shift74__) & 0xffffffff;
    int __leftop57__ = __leftop58__ * __rightop59__;
    int __rightop75__ = 0;
    int __leftop56__ = __leftop57__ + __rightop75__;
    int __rightop76__ = 1;
    int __rightop55__ = __leftop56__ * __rightop76__;
    int __offsetinbits53__ = __leftop54__ + __rightop55__;
    // __offsetinbits53__ = 0 + 8 * d.s.blocksize + 0 * 1
    int __offset77__ = __offsetinbits53__ >> 3;
    int __left51__ = (__left52__ + __offset77__);
    // __left51__ = d.g
    // __offsetinbits78__ <-- 32 + 32 + 0
    int __leftop79__ = 32;
    int __leftop81__ = 32;
    int __rightop82__ = 0;
    int __rightop80__ = __leftop81__ + __rightop82__;
    int __offsetinbits78__ = __leftop79__ + __rightop80__;
    // __offsetinbits78__ = 32 + 32 + 0
    int __offset83__ = __offsetinbits78__ >> 3;
    int __shift84__ = __offsetinbits78__ - (__offset83__ << 3);
    int __element50__ = ((*(int *)(__left51__ + __offset83__))  >> __shift84__) & 0xffffffff;
    __InodeTableBlock___hash->add((int)__element50__, (int)__element50__);
  }
}


// build rule4
{
  //d.g.InodeBitmapBlock < d.s.NumberofBlocks
  // __left87__ <-- d.g
  // __left88__ <-- d
  int __left88__ = (int) d;
  // __left88__ = d
  // __offsetinbits89__ <-- 0 + 8 * d.s.blocksize + 0 * 1
  int __leftop90__ = 0;
  int __leftop94__ = 8;
  // __left96__ <-- d.s
  // __left97__ <-- d
  int __left97__ = (int) d;
  // __left97__ = d
  int __left96__ = (__left97__ + 0);
  // __left96__ = d.s
  // __offsetinbits98__ <-- 32 + 32 + 32 + 32 + 32 + 0
  int __leftop99__ = 32;
  int __leftop101__ = 32;
  int __leftop103__ = 32;
  int __leftop105__ = 32;
  int __leftop107__ = 32;
  int __rightop108__ = 0;
  int __rightop106__ = __leftop107__ + __rightop108__;
  int __rightop104__ = __leftop105__ + __rightop106__;
  int __rightop102__ = __leftop103__ + __rightop104__;
  int __rightop100__ = __leftop101__ + __rightop102__;
  int __offsetinbits98__ = __leftop99__ + __rightop100__;
  // __offsetinbits98__ = 32 + 32 + 32 + 32 + 32 + 0
  int __offset109__ = __offsetinbits98__ >> 3;
  int __shift110__ = __offsetinbits98__ - (__offset109__ << 3);
  int __rightop95__ = ((*(int *)(__left96__ + __offset109__))  >> __shift110__) & 0xffffffff;
  int __leftop93__ = __leftop94__ * __rightop95__;
  int __rightop111__ = 0;
  int __leftop92__ = __leftop93__ + __rightop111__;
  int __rightop112__ = 1;
  int __rightop91__ = __leftop92__ * __rightop112__;
  int __offsetinbits89__ = __leftop90__ + __rightop91__;
  // __offsetinbits89__ = 0 + 8 * d.s.blocksize + 0 * 1
  int __offset113__ = __offsetinbits89__ >> 3;
  int __left87__ = (__left88__ + __offset113__);
  // __left87__ = d.g
  // __offsetinbits114__ <-- 32 + 0
  int __leftop115__ = 32;
  int __rightop116__ = 0;
  int __offsetinbits114__ = __leftop115__ + __rightop116__;
  // __offsetinbits114__ = 32 + 0
  int __offset117__ = __offsetinbits114__ >> 3;
  int __shift118__ = __offsetinbits114__ - (__offset117__ << 3);
  int __leftop86__ = ((*(int *)(__left87__ + __offset117__))  >> __shift118__) & 0xffffffff;
  // __left120__ <-- d.s
  // __left121__ <-- d
  int __left121__ = (int) d;
  // __left121__ = d
  int __left120__ = (__left121__ + 0);
  // __left120__ = d.s
  // __offsetinbits122__ <-- 32 + 32 + 0
  int __leftop123__ = 32;
  int __leftop125__ = 32;
  int __rightop126__ = 0;
  int __rightop124__ = __leftop125__ + __rightop126__;
  int __offsetinbits122__ = __leftop123__ + __rightop124__;
  // __offsetinbits122__ = 32 + 32 + 0
  int __offset127__ = __offsetinbits122__ >> 3;
  int __shift128__ = __offsetinbits122__ - (__offset127__ << 3);
  int __rightop119__ = ((*(int *)(__left120__ + __offset127__))  >> __shift128__) & 0xffffffff;
  int __tempvar85__ = __leftop86__ < __rightop119__;
  if (__tempvar85__) {
    // __left130__ <-- d.g
    // __left131__ <-- d
    int __left131__ = (int) d;
    // __left131__ = d
    // __offsetinbits132__ <-- 0 + 8 * d.s.blocksize + 0 * 1
    int __leftop133__ = 0;
    int __leftop137__ = 8;
    // __left139__ <-- d.s
    // __left140__ <-- d
    int __left140__ = (int) d;
    // __left140__ = d
    int __left139__ = (__left140__ + 0);
    // __left139__ = d.s
    // __offsetinbits141__ <-- 32 + 32 + 32 + 32 + 32 + 0
    int __leftop142__ = 32;
    int __leftop144__ = 32;
    int __leftop146__ = 32;
    int __leftop148__ = 32;
    int __leftop150__ = 32;
    int __rightop151__ = 0;
    int __rightop149__ = __leftop150__ + __rightop151__;
    int __rightop147__ = __leftop148__ + __rightop149__;
    int __rightop145__ = __leftop146__ + __rightop147__;
    int __rightop143__ = __leftop144__ + __rightop145__;
    int __offsetinbits141__ = __leftop142__ + __rightop143__;
    // __offsetinbits141__ = 32 + 32 + 32 + 32 + 32 + 0
    int __offset152__ = __offsetinbits141__ >> 3;
    int __shift153__ = __offsetinbits141__ - (__offset152__ << 3);
    int __rightop138__ = ((*(int *)(__left139__ + __offset152__))  >> __shift153__) & 0xffffffff;
    int __leftop136__ = __leftop137__ * __rightop138__;
    int __rightop154__ = 0;
    int __leftop135__ = __leftop136__ + __rightop154__;
    int __rightop155__ = 1;
    int __rightop134__ = __leftop135__ * __rightop155__;
    int __offsetinbits132__ = __leftop133__ + __rightop134__;
    // __offsetinbits132__ = 0 + 8 * d.s.blocksize + 0 * 1
    int __offset156__ = __offsetinbits132__ >> 3;
    int __left130__ = (__left131__ + __offset156__);
    // __left130__ = d.g
    // __offsetinbits157__ <-- 32 + 0
    int __leftop158__ = 32;
    int __rightop159__ = 0;
    int __offsetinbits157__ = __leftop158__ + __rightop159__;
    // __offsetinbits157__ = 32 + 0
    int __offset160__ = __offsetinbits157__ >> 3;
    int __shift161__ = __offsetinbits157__ - (__offset160__ << 3);
    int __element129__ = ((*(int *)(__left130__ + __offset160__))  >> __shift161__) & 0xffffffff;
    __InodeBitmapBlock___hash->add((int)__element129__, (int)__element129__);
  }
}


// build rule12
{
  int __tempvar162__ = 0;
  // __left165__ <-- d.s
  // __left166__ <-- d
  int __left166__ = (int) d;
  // __left166__ = d
  int __left165__ = (__left166__ + 0);
  // __left165__ = d.s
  // __offsetinbits167__ <-- 32 + 32 + 32 + 0
  int __leftop168__ = 32;
  int __leftop170__ = 32;
  int __leftop172__ = 32;
  int __rightop173__ = 0;
  int __rightop171__ = __leftop172__ + __rightop173__;
  int __rightop169__ = __leftop170__ + __rightop171__;
  int __offsetinbits167__ = __leftop168__ + __rightop169__;
  // __offsetinbits167__ = 32 + 32 + 32 + 0
  int __offset174__ = __offsetinbits167__ >> 3;
  int __shift175__ = __offsetinbits167__ - (__offset174__ << 3);
  int __leftop164__ = ((*(int *)(__left165__ + __offset174__))  >> __shift175__) & 0xffffffff;
  int __rightop176__ = 1;
  int __tempvar163__ = __leftop164__ - __rightop176__;
  for (int __j__ = __tempvar162__; __j__ <= __tempvar163__; __j__++) {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); ) {
      int __ibb__ = (int) __ibb___iterator->next();
      //cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == {false = 104}
      // __left179__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left181__ <-- d
      int __left181__ = (int) d;
      // __left181__ = d
      // __offsetinbits182__ <-- 0 + 8 * d.s.blocksize + 0 * ibb
      int __leftop183__ = 0;
      int __leftop187__ = 8;
      // __left189__ <-- d.s
      // __left190__ <-- d
      int __left190__ = (int) d;
      // __left190__ = d
      int __left189__ = (__left190__ + 0);
      // __left189__ = d.s
      // __offsetinbits191__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop192__ = 32;
      int __leftop194__ = 32;
      int __leftop196__ = 32;
      int __leftop198__ = 32;
      int __leftop200__ = 32;
      int __rightop201__ = 0;
      int __rightop199__ = __leftop200__ + __rightop201__;
      int __rightop197__ = __leftop198__ + __rightop199__;
      int __rightop195__ = __leftop196__ + __rightop197__;
      int __rightop193__ = __leftop194__ + __rightop195__;
      int __offsetinbits191__ = __leftop192__ + __rightop193__;
      // __offsetinbits191__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset202__ = __offsetinbits191__ >> 3;
      int __shift203__ = __offsetinbits191__ - (__offset202__ << 3);
      int __rightop188__ = ((*(int *)(__left189__ + __offset202__))  >> __shift203__) & 0xffffffff;
      int __leftop186__ = __leftop187__ * __rightop188__;
      int __rightop204__ = 0;
      int __leftop185__ = __leftop186__ + __rightop204__;
      int __rightop205__ = (int) __ibb__;
      int __rightop184__ = __leftop185__ * __rightop205__;
      int __offsetinbits182__ = __leftop183__ + __rightop184__;
      // __offsetinbits182__ = 0 + 8 * d.s.blocksize + 0 * ibb
      int __offset206__ = __offsetinbits182__ >> 3;
      int __expr180__ = (__left181__ + __offset206__);
      int __left179__ = (int) __expr180__;
      // __left179__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits207__ <-- 0 + 1 * j
      int __leftop208__ = 0;
      int __leftop210__ = 1;
      int __rightop211__ = (int) __j__;
      int __rightop209__ = __leftop210__ * __rightop211__;
      int __offsetinbits207__ = __leftop208__ + __rightop209__;
      // __offsetinbits207__ = 0 + 1 * j
      int __offset212__ = __offsetinbits207__ >> 3;
      int __shift213__ = __offsetinbits207__ - (__offset212__ << 3);
      int __leftop178__ = ((*(int *)(__left179__ + __offset212__))  >> __shift213__) & 0x1;
      int __rightop214__ = 104;
      int __tempvar177__ = __leftop178__ == __rightop214__;
      if (__tempvar177__) {
        int __leftele215__ = (int) __j__;
        int __rightele216__ = 105;
        __inodestatus___hash->add((int)__leftele215__, (int)__rightele216__);
      }
    }
  }
}


// build rule5
{
  //d.g.BlockBitmapBlock < d.s.NumberofBlocks
  // __left219__ <-- d.g
  // __left220__ <-- d
  int __left220__ = (int) d;
  // __left220__ = d
  // __offsetinbits221__ <-- 0 + 8 * d.s.blocksize + 0 * 1
  int __leftop222__ = 0;
  int __leftop226__ = 8;
  // __left228__ <-- d.s
  // __left229__ <-- d
  int __left229__ = (int) d;
  // __left229__ = d
  int __left228__ = (__left229__ + 0);
  // __left228__ = d.s
  // __offsetinbits230__ <-- 32 + 32 + 32 + 32 + 32 + 0
  int __leftop231__ = 32;
  int __leftop233__ = 32;
  int __leftop235__ = 32;
  int __leftop237__ = 32;
  int __leftop239__ = 32;
  int __rightop240__ = 0;
  int __rightop238__ = __leftop239__ + __rightop240__;
  int __rightop236__ = __leftop237__ + __rightop238__;
  int __rightop234__ = __leftop235__ + __rightop236__;
  int __rightop232__ = __leftop233__ + __rightop234__;
  int __offsetinbits230__ = __leftop231__ + __rightop232__;
  // __offsetinbits230__ = 32 + 32 + 32 + 32 + 32 + 0
  int __offset241__ = __offsetinbits230__ >> 3;
  int __shift242__ = __offsetinbits230__ - (__offset241__ << 3);
  int __rightop227__ = ((*(int *)(__left228__ + __offset241__))  >> __shift242__) & 0xffffffff;
  int __leftop225__ = __leftop226__ * __rightop227__;
  int __rightop243__ = 0;
  int __leftop224__ = __leftop225__ + __rightop243__;
  int __rightop244__ = 1;
  int __rightop223__ = __leftop224__ * __rightop244__;
  int __offsetinbits221__ = __leftop222__ + __rightop223__;
  // __offsetinbits221__ = 0 + 8 * d.s.blocksize + 0 * 1
  int __offset245__ = __offsetinbits221__ >> 3;
  int __left219__ = (__left220__ + __offset245__);
  // __left219__ = d.g
  int __leftop218__ = ((*(int *)(__left219__ + 0))  >> 0) & 0xffffffff;
  // __left247__ <-- d.s
  // __left248__ <-- d
  int __left248__ = (int) d;
  // __left248__ = d
  int __left247__ = (__left248__ + 0);
  // __left247__ = d.s
  // __offsetinbits249__ <-- 32 + 32 + 0
  int __leftop250__ = 32;
  int __leftop252__ = 32;
  int __rightop253__ = 0;
  int __rightop251__ = __leftop252__ + __rightop253__;
  int __offsetinbits249__ = __leftop250__ + __rightop251__;
  // __offsetinbits249__ = 32 + 32 + 0
  int __offset254__ = __offsetinbits249__ >> 3;
  int __shift255__ = __offsetinbits249__ - (__offset254__ << 3);
  int __rightop246__ = ((*(int *)(__left247__ + __offset254__))  >> __shift255__) & 0xffffffff;
  int __tempvar217__ = __leftop218__ < __rightop246__;
  if (__tempvar217__) {
    // __left257__ <-- d.g
    // __left258__ <-- d
    int __left258__ = (int) d;
    // __left258__ = d
    // __offsetinbits259__ <-- 0 + 8 * d.s.blocksize + 0 * 1
    int __leftop260__ = 0;
    int __leftop264__ = 8;
    // __left266__ <-- d.s
    // __left267__ <-- d
    int __left267__ = (int) d;
    // __left267__ = d
    int __left266__ = (__left267__ + 0);
    // __left266__ = d.s
    // __offsetinbits268__ <-- 32 + 32 + 32 + 32 + 32 + 0
    int __leftop269__ = 32;
    int __leftop271__ = 32;
    int __leftop273__ = 32;
    int __leftop275__ = 32;
    int __leftop277__ = 32;
    int __rightop278__ = 0;
    int __rightop276__ = __leftop277__ + __rightop278__;
    int __rightop274__ = __leftop275__ + __rightop276__;
    int __rightop272__ = __leftop273__ + __rightop274__;
    int __rightop270__ = __leftop271__ + __rightop272__;
    int __offsetinbits268__ = __leftop269__ + __rightop270__;
    // __offsetinbits268__ = 32 + 32 + 32 + 32 + 32 + 0
    int __offset279__ = __offsetinbits268__ >> 3;
    int __shift280__ = __offsetinbits268__ - (__offset279__ << 3);
    int __rightop265__ = ((*(int *)(__left266__ + __offset279__))  >> __shift280__) & 0xffffffff;
    int __leftop263__ = __leftop264__ * __rightop265__;
    int __rightop281__ = 0;
    int __leftop262__ = __leftop263__ + __rightop281__;
    int __rightop282__ = 1;
    int __rightop261__ = __leftop262__ * __rightop282__;
    int __offsetinbits259__ = __leftop260__ + __rightop261__;
    // __offsetinbits259__ = 0 + 8 * d.s.blocksize + 0 * 1
    int __offset283__ = __offsetinbits259__ >> 3;
    int __left257__ = (__left258__ + __offset283__);
    // __left257__ = d.g
    int __element256__ = ((*(int *)(__left257__ + 0))  >> 0) & 0xffffffff;
    __BlockBitmapBlock___hash->add((int)__element256__, (int)__element256__);
  }
}


// build rule13
{
  int __tempvar284__ = 0;
  // __left287__ <-- d.s
  // __left288__ <-- d
  int __left288__ = (int) d;
  // __left288__ = d
  int __left287__ = (__left288__ + 0);
  // __left287__ = d.s
  // __offsetinbits289__ <-- 32 + 32 + 32 + 0
  int __leftop290__ = 32;
  int __leftop292__ = 32;
  int __leftop294__ = 32;
  int __rightop295__ = 0;
  int __rightop293__ = __leftop294__ + __rightop295__;
  int __rightop291__ = __leftop292__ + __rightop293__;
  int __offsetinbits289__ = __leftop290__ + __rightop291__;
  // __offsetinbits289__ = 32 + 32 + 32 + 0
  int __offset296__ = __offsetinbits289__ >> 3;
  int __shift297__ = __offsetinbits289__ - (__offset296__ << 3);
  int __leftop286__ = ((*(int *)(__left287__ + __offset296__))  >> __shift297__) & 0xffffffff;
  int __rightop298__ = 1;
  int __tempvar285__ = __leftop286__ - __rightop298__;
  for (int __j__ = __tempvar284__; __j__ <= __tempvar285__; __j__++) {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); ) {
      int __ibb__ = (int) __ibb___iterator->next();
      //cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == true
      // __left301__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left303__ <-- d
      int __left303__ = (int) d;
      // __left303__ = d
      // __offsetinbits304__ <-- 0 + 8 * d.s.blocksize + 0 * ibb
      int __leftop305__ = 0;
      int __leftop309__ = 8;
      // __left311__ <-- d.s
      // __left312__ <-- d
      int __left312__ = (int) d;
      // __left312__ = d
      int __left311__ = (__left312__ + 0);
      // __left311__ = d.s
      // __offsetinbits313__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop314__ = 32;
      int __leftop316__ = 32;
      int __leftop318__ = 32;
      int __leftop320__ = 32;
      int __leftop322__ = 32;
      int __rightop323__ = 0;
      int __rightop321__ = __leftop322__ + __rightop323__;
      int __rightop319__ = __leftop320__ + __rightop321__;
      int __rightop317__ = __leftop318__ + __rightop319__;
      int __rightop315__ = __leftop316__ + __rightop317__;
      int __offsetinbits313__ = __leftop314__ + __rightop315__;
      // __offsetinbits313__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset324__ = __offsetinbits313__ >> 3;
      int __shift325__ = __offsetinbits313__ - (__offset324__ << 3);
      int __rightop310__ = ((*(int *)(__left311__ + __offset324__))  >> __shift325__) & 0xffffffff;
      int __leftop308__ = __leftop309__ * __rightop310__;
      int __rightop326__ = 0;
      int __leftop307__ = __leftop308__ + __rightop326__;
      int __rightop327__ = (int) __ibb__;
      int __rightop306__ = __leftop307__ * __rightop327__;
      int __offsetinbits304__ = __leftop305__ + __rightop306__;
      // __offsetinbits304__ = 0 + 8 * d.s.blocksize + 0 * ibb
      int __offset328__ = __offsetinbits304__ >> 3;
      int __expr302__ = (__left303__ + __offset328__);
      int __left301__ = (int) __expr302__;
      // __left301__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits329__ <-- 0 + 1 * j
      int __leftop330__ = 0;
      int __leftop332__ = 1;
      int __rightop333__ = (int) __j__;
      int __rightop331__ = __leftop332__ * __rightop333__;
      int __offsetinbits329__ = __leftop330__ + __rightop331__;
      // __offsetinbits329__ = 0 + 1 * j
      int __offset334__ = __offsetinbits329__ >> 3;
      int __shift335__ = __offsetinbits329__ - (__offset334__ << 3);
      int __leftop300__ = ((*(int *)(__left301__ + __offset334__))  >> __shift335__) & 0x1;
      int __rightop336__ = 1;
      int __tempvar299__ = __leftop300__ == __rightop336__;
      if (__tempvar299__) {
        int __leftele337__ = (int) __j__;
        int __rightele338__ = 106;
        __inodestatus___hash->add((int)__leftele337__, (int)__rightele338__);
      }
    }
  }
}


// build rule6
{
  //d.s.RootDirectoryInode < d.s.NumberofInodes
  // __left341__ <-- d.s
  // __left342__ <-- d
  int __left342__ = (int) d;
  // __left342__ = d
  int __left341__ = (__left342__ + 0);
  // __left341__ = d.s
  // __offsetinbits343__ <-- 32 + 32 + 32 + 32 + 0
  int __leftop344__ = 32;
  int __leftop346__ = 32;
  int __leftop348__ = 32;
  int __leftop350__ = 32;
  int __rightop351__ = 0;
  int __rightop349__ = __leftop350__ + __rightop351__;
  int __rightop347__ = __leftop348__ + __rightop349__;
  int __rightop345__ = __leftop346__ + __rightop347__;
  int __offsetinbits343__ = __leftop344__ + __rightop345__;
  // __offsetinbits343__ = 32 + 32 + 32 + 32 + 0
  int __offset352__ = __offsetinbits343__ >> 3;
  int __shift353__ = __offsetinbits343__ - (__offset352__ << 3);
  int __leftop340__ = ((*(int *)(__left341__ + __offset352__))  >> __shift353__) & 0xffffffff;
  // __left355__ <-- d.s
  // __left356__ <-- d
  int __left356__ = (int) d;
  // __left356__ = d
  int __left355__ = (__left356__ + 0);
  // __left355__ = d.s
  // __offsetinbits357__ <-- 32 + 32 + 32 + 0
  int __leftop358__ = 32;
  int __leftop360__ = 32;
  int __leftop362__ = 32;
  int __rightop363__ = 0;
  int __rightop361__ = __leftop362__ + __rightop363__;
  int __rightop359__ = __leftop360__ + __rightop361__;
  int __offsetinbits357__ = __leftop358__ + __rightop359__;
  // __offsetinbits357__ = 32 + 32 + 32 + 0
  int __offset364__ = __offsetinbits357__ >> 3;
  int __shift365__ = __offsetinbits357__ - (__offset364__ << 3);
  int __rightop354__ = ((*(int *)(__left355__ + __offset364__))  >> __shift365__) & 0xffffffff;
  int __tempvar339__ = __leftop340__ < __rightop354__;
  if (__tempvar339__) {
    // __left367__ <-- d.s
    // __left368__ <-- d
    int __left368__ = (int) d;
    // __left368__ = d
    int __left367__ = (__left368__ + 0);
    // __left367__ = d.s
    // __offsetinbits369__ <-- 32 + 32 + 32 + 32 + 0
    int __leftop370__ = 32;
    int __leftop372__ = 32;
    int __leftop374__ = 32;
    int __leftop376__ = 32;
    int __rightop377__ = 0;
    int __rightop375__ = __leftop376__ + __rightop377__;
    int __rightop373__ = __leftop374__ + __rightop375__;
    int __rightop371__ = __leftop372__ + __rightop373__;
    int __offsetinbits369__ = __leftop370__ + __rightop371__;
    // __offsetinbits369__ = 32 + 32 + 32 + 32 + 0
    int __offset378__ = __offsetinbits369__ >> 3;
    int __shift379__ = __offsetinbits369__ - (__offset378__ << 3);
    int __element366__ = ((*(int *)(__left367__ + __offset378__))  >> __shift379__) & 0xffffffff;
    __RootDirectoryInode___hash->add((int)__element366__, (int)__element366__);
  }
}


// build rule9
{
  for (SimpleIterator* __di___iterator = __DirectoryInode___hash->iterator(); __di___iterator->hasNext(); ) {
    int __di__ = (int) __di___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar380__ = 0;
      // __left384__ <-- d.s
      // __left385__ <-- d
      int __left385__ = (int) d;
      // __left385__ = d
      int __left384__ = (__left385__ + 0);
      // __left384__ = d.s
      // __offsetinbits386__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop387__ = 32;
      int __leftop389__ = 32;
      int __leftop391__ = 32;
      int __leftop393__ = 32;
      int __leftop395__ = 32;
      int __rightop396__ = 0;
      int __rightop394__ = __leftop395__ + __rightop396__;
      int __rightop392__ = __leftop393__ + __rightop394__;
      int __rightop390__ = __leftop391__ + __rightop392__;
      int __rightop388__ = __leftop389__ + __rightop390__;
      int __offsetinbits386__ = __leftop387__ + __rightop388__;
      // __offsetinbits386__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset397__ = __offsetinbits386__ >> 3;
      int __shift398__ = __offsetinbits386__ - (__offset397__ << 3);
      int __leftop383__ = ((*(int *)(__left384__ + __offset397__))  >> __shift398__) & 0xffffffff;
      int __rightop399__ = 128;
      int __leftop382__ = __leftop383__ / __rightop399__;
      int __rightop400__ = 1;
      int __tempvar381__ = __leftop382__ - __rightop400__;
      for (int __j__ = __tempvar380__; __j__ <= __tempvar381__; __j__++) {
        int __tempvar401__ = 0;
        int __tempvar402__ = 11;
        for (int __k__ = __tempvar401__; __k__ <= __tempvar402__; __k__++) {
          //cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k] < d.s.NumberofBlocks
          // __left405__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
          // __left406__ <-- cast(__InodeTable__, d.b[itb])
          // __left408__ <-- d
          int __left408__ = (int) d;
          // __left408__ = d
          // __offsetinbits409__ <-- 0 + 8 * d.s.blocksize + 0 * itb
          int __leftop410__ = 0;
          int __leftop414__ = 8;
          // __left416__ <-- d.s
          // __left417__ <-- d
          int __left417__ = (int) d;
          // __left417__ = d
          int __left416__ = (__left417__ + 0);
          // __left416__ = d.s
          // __offsetinbits418__ <-- 32 + 32 + 32 + 32 + 32 + 0
          int __leftop419__ = 32;
          int __leftop421__ = 32;
          int __leftop423__ = 32;
          int __leftop425__ = 32;
          int __leftop427__ = 32;
          int __rightop428__ = 0;
          int __rightop426__ = __leftop427__ + __rightop428__;
          int __rightop424__ = __leftop425__ + __rightop426__;
          int __rightop422__ = __leftop423__ + __rightop424__;
          int __rightop420__ = __leftop421__ + __rightop422__;
          int __offsetinbits418__ = __leftop419__ + __rightop420__;
          // __offsetinbits418__ = 32 + 32 + 32 + 32 + 32 + 0
          int __offset429__ = __offsetinbits418__ >> 3;
          int __shift430__ = __offsetinbits418__ - (__offset429__ << 3);
          int __rightop415__ = ((*(int *)(__left416__ + __offset429__))  >> __shift430__) & 0xffffffff;
          int __leftop413__ = __leftop414__ * __rightop415__;
          int __rightop431__ = 0;
          int __leftop412__ = __leftop413__ + __rightop431__;
          int __rightop432__ = (int) __itb__;
          int __rightop411__ = __leftop412__ * __rightop432__;
          int __offsetinbits409__ = __leftop410__ + __rightop411__;
          // __offsetinbits409__ = 0 + 8 * d.s.blocksize + 0 * itb
          int __offset433__ = __offsetinbits409__ >> 3;
          int __expr407__ = (__left408__ + __offset433__);
          int __left406__ = (int) __expr407__;
          // __left406__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits434__ <-- 0 + 32 + 32 * 12 + 32 + 0 * di
          int __leftop435__ = 0;
          int __leftop438__ = 32;
          int __leftop441__ = 32;
          int __rightop442__ = 12;
          int __leftop440__ = __leftop441__ * __rightop442__;
          int __leftop444__ = 32;
          int __rightop445__ = 0;
          int __rightop443__ = __leftop444__ + __rightop445__;
          int __rightop439__ = __leftop440__ + __rightop443__;
          int __leftop437__ = __leftop438__ + __rightop439__;
          int __rightop446__ = (int) __di__;
          int __rightop436__ = __leftop437__ * __rightop446__;
          int __offsetinbits434__ = __leftop435__ + __rightop436__;
          // __offsetinbits434__ = 0 + 32 + 32 * 12 + 32 + 0 * di
          int __offset447__ = __offsetinbits434__ >> 3;
          int __left405__ = (__left406__ + __offset447__);
          // __left405__ = cast(__InodeTable__, d.b[itb]).itable[di]
          // __offsetinbits448__ <-- 32 + 0 + 32 * k
          int __leftop450__ = 32;
          int __rightop451__ = 0;
          int __leftop449__ = __leftop450__ + __rightop451__;
          int __leftop453__ = 32;
          int __rightop454__ = (int) __k__;
          int __rightop452__ = __leftop453__ * __rightop454__;
          int __offsetinbits448__ = __leftop449__ + __rightop452__;
          // __offsetinbits448__ = 32 + 0 + 32 * k
          int __offset455__ = __offsetinbits448__ >> 3;
          int __shift456__ = __offsetinbits448__ - (__offset455__ << 3);
          int __leftop404__ = ((*(int *)(__left405__ + __offset455__))  >> __shift456__) & 0xffffffff;
          // __left458__ <-- d.s
          // __left459__ <-- d
          int __left459__ = (int) d;
          // __left459__ = d
          int __left458__ = (__left459__ + 0);
          // __left458__ = d.s
          // __offsetinbits460__ <-- 32 + 32 + 0
          int __leftop461__ = 32;
          int __leftop463__ = 32;
          int __rightop464__ = 0;
          int __rightop462__ = __leftop463__ + __rightop464__;
          int __offsetinbits460__ = __leftop461__ + __rightop462__;
          // __offsetinbits460__ = 32 + 32 + 0
          int __offset465__ = __offsetinbits460__ >> 3;
          int __shift466__ = __offsetinbits460__ - (__offset465__ << 3);
          int __rightop457__ = ((*(int *)(__left458__ + __offset465__))  >> __shift466__) & 0xffffffff;
          int __tempvar403__ = __leftop404__ < __rightop457__;
          if (__tempvar403__) {
            // __left468__ <-- cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __left470__ <-- d
            int __left470__ = (int) d;
            // __left470__ = d
            // __offsetinbits471__ <-- 0 + 8 * d.s.blocksize + 0 * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]
            int __leftop472__ = 0;
            int __leftop476__ = 8;
            // __left478__ <-- d.s
            // __left479__ <-- d
            int __left479__ = (int) d;
            // __left479__ = d
            int __left478__ = (__left479__ + 0);
            // __left478__ = d.s
            // __offsetinbits480__ <-- 32 + 32 + 32 + 32 + 32 + 0
            int __leftop481__ = 32;
            int __leftop483__ = 32;
            int __leftop485__ = 32;
            int __leftop487__ = 32;
            int __leftop489__ = 32;
            int __rightop490__ = 0;
            int __rightop488__ = __leftop489__ + __rightop490__;
            int __rightop486__ = __leftop487__ + __rightop488__;
            int __rightop484__ = __leftop485__ + __rightop486__;
            int __rightop482__ = __leftop483__ + __rightop484__;
            int __offsetinbits480__ = __leftop481__ + __rightop482__;
            // __offsetinbits480__ = 32 + 32 + 32 + 32 + 32 + 0
            int __offset491__ = __offsetinbits480__ >> 3;
            int __shift492__ = __offsetinbits480__ - (__offset491__ << 3);
            int __rightop477__ = ((*(int *)(__left478__ + __offset491__))  >> __shift492__) & 0xffffffff;
            int __leftop475__ = __leftop476__ * __rightop477__;
            int __rightop493__ = 0;
            int __leftop474__ = __leftop475__ + __rightop493__;
            // __left495__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
            // __left496__ <-- cast(__InodeTable__, d.b[itb])
            // __left498__ <-- d
            int __left498__ = (int) d;
            // __left498__ = d
            // __offsetinbits499__ <-- 0 + 8 * d.s.blocksize + 0 * itb
            int __leftop500__ = 0;
            int __leftop504__ = 8;
            // __left506__ <-- d.s
            // __left507__ <-- d
            int __left507__ = (int) d;
            // __left507__ = d
            int __left506__ = (__left507__ + 0);
            // __left506__ = d.s
            // __offsetinbits508__ <-- 32 + 32 + 32 + 32 + 32 + 0
            int __leftop509__ = 32;
            int __leftop511__ = 32;
            int __leftop513__ = 32;
            int __leftop515__ = 32;
            int __leftop517__ = 32;
            int __rightop518__ = 0;
            int __rightop516__ = __leftop517__ + __rightop518__;
            int __rightop514__ = __leftop515__ + __rightop516__;
            int __rightop512__ = __leftop513__ + __rightop514__;
            int __rightop510__ = __leftop511__ + __rightop512__;
            int __offsetinbits508__ = __leftop509__ + __rightop510__;
            // __offsetinbits508__ = 32 + 32 + 32 + 32 + 32 + 0
            int __offset519__ = __offsetinbits508__ >> 3;
            int __shift520__ = __offsetinbits508__ - (__offset519__ << 3);
            int __rightop505__ = ((*(int *)(__left506__ + __offset519__))  >> __shift520__) & 0xffffffff;
            int __leftop503__ = __leftop504__ * __rightop505__;
            int __rightop521__ = 0;
            int __leftop502__ = __leftop503__ + __rightop521__;
            int __rightop522__ = (int) __itb__;
            int __rightop501__ = __leftop502__ * __rightop522__;
            int __offsetinbits499__ = __leftop500__ + __rightop501__;
            // __offsetinbits499__ = 0 + 8 * d.s.blocksize + 0 * itb
            int __offset523__ = __offsetinbits499__ >> 3;
            int __expr497__ = (__left498__ + __offset523__);
            int __left496__ = (int) __expr497__;
            // __left496__ = cast(__InodeTable__, d.b[itb])
            // __offsetinbits524__ <-- 0 + 32 + 32 * 12 + 32 + 0 * di
            int __leftop525__ = 0;
            int __leftop528__ = 32;
            int __leftop531__ = 32;
            int __rightop532__ = 12;
            int __leftop530__ = __leftop531__ * __rightop532__;
            int __leftop534__ = 32;
            int __rightop535__ = 0;
            int __rightop533__ = __leftop534__ + __rightop535__;
            int __rightop529__ = __leftop530__ + __rightop533__;
            int __leftop527__ = __leftop528__ + __rightop529__;
            int __rightop536__ = (int) __di__;
            int __rightop526__ = __leftop527__ * __rightop536__;
            int __offsetinbits524__ = __leftop525__ + __rightop526__;
            // __offsetinbits524__ = 0 + 32 + 32 * 12 + 32 + 0 * di
            int __offset537__ = __offsetinbits524__ >> 3;
            int __left495__ = (__left496__ + __offset537__);
            // __left495__ = cast(__InodeTable__, d.b[itb]).itable[di]
            // __offsetinbits538__ <-- 32 + 0 + 32 * k
            int __leftop540__ = 32;
            int __rightop541__ = 0;
            int __leftop539__ = __leftop540__ + __rightop541__;
            int __leftop543__ = 32;
            int __rightop544__ = (int) __k__;
            int __rightop542__ = __leftop543__ * __rightop544__;
            int __offsetinbits538__ = __leftop539__ + __rightop542__;
            // __offsetinbits538__ = 32 + 0 + 32 * k
            int __offset545__ = __offsetinbits538__ >> 3;
            int __shift546__ = __offsetinbits538__ - (__offset545__ << 3);
            int __rightop494__ = ((*(int *)(__left495__ + __offset545__))  >> __shift546__) & 0xffffffff;
            int __rightop473__ = __leftop474__ * __rightop494__;
            int __offsetinbits471__ = __leftop472__ + __rightop473__;
            // __offsetinbits471__ = 0 + 8 * d.s.blocksize + 0 * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]
            int __offset547__ = __offsetinbits471__ >> 3;
            int __expr469__ = (__left470__ + __offset547__);
            int __left468__ = (int) __expr469__;
            // __left468__ = cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __offsetinbits548__ <-- 0 + 32 + 8 * 124 + 0 * j
            int __leftop549__ = 0;
            int __leftop552__ = 32;
            int __leftop555__ = 8;
            int __rightop556__ = 124;
            int __leftop554__ = __leftop555__ * __rightop556__;
            int __rightop557__ = 0;
            int __rightop553__ = __leftop554__ + __rightop557__;
            int __leftop551__ = __leftop552__ + __rightop553__;
            int __rightop558__ = (int) __j__;
            int __rightop550__ = __leftop551__ * __rightop558__;
            int __offsetinbits548__ = __leftop549__ + __rightop550__;
            // __offsetinbits548__ = 0 + 32 + 8 * 124 + 0 * j
            int __offset559__ = __offsetinbits548__ >> 3;
            int __element467__ = (__left468__ + __offset559__);
            __DirectoryEntry___hash->add((int)__element467__, (int)__element467__);
          }
        }
      }
    }
  }
}


// build rule15
{
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); ) {
    int __de__ = (int) __de___iterator->next();
    //de.inodenumber < d.s.NumberofInodes
    // __left562__ <-- de
    int __left562__ = (int) __de__;
    // __left562__ = de
    // __offsetinbits563__ <-- 8 * 124 + 0
    int __leftop565__ = 8;
    int __rightop566__ = 124;
    int __leftop564__ = __leftop565__ * __rightop566__;
    int __rightop567__ = 0;
    int __offsetinbits563__ = __leftop564__ + __rightop567__;
    // __offsetinbits563__ = 8 * 124 + 0
    int __offset568__ = __offsetinbits563__ >> 3;
    int __shift569__ = __offsetinbits563__ - (__offset568__ << 3);
    int __leftop561__ = ((*(int *)(__left562__ + __offset568__))  >> __shift569__) & 0xffffffff;
    // __left571__ <-- d.s
    // __left572__ <-- d
    int __left572__ = (int) d;
    // __left572__ = d
    int __left571__ = (__left572__ + 0);
    // __left571__ = d.s
    // __offsetinbits573__ <-- 32 + 32 + 32 + 0
    int __leftop574__ = 32;
    int __leftop576__ = 32;
    int __leftop578__ = 32;
    int __rightop579__ = 0;
    int __rightop577__ = __leftop578__ + __rightop579__;
    int __rightop575__ = __leftop576__ + __rightop577__;
    int __offsetinbits573__ = __leftop574__ + __rightop575__;
    // __offsetinbits573__ = 32 + 32 + 32 + 0
    int __offset580__ = __offsetinbits573__ >> 3;
    int __shift581__ = __offsetinbits573__ - (__offset580__ << 3);
    int __rightop570__ = ((*(int *)(__left571__ + __offset580__))  >> __shift581__) & 0xffffffff;
    int __tempvar560__ = __leftop561__ < __rightop570__;
    if (__tempvar560__) {
      int __leftele582__ = (int) __de__;
      // __left584__ <-- de
      int __left584__ = (int) __de__;
      // __left584__ = de
      // __offsetinbits585__ <-- 8 * 124 + 0
      int __leftop587__ = 8;
      int __rightop588__ = 124;
      int __leftop586__ = __leftop587__ * __rightop588__;
      int __rightop589__ = 0;
      int __offsetinbits585__ = __leftop586__ + __rightop589__;
      // __offsetinbits585__ = 8 * 124 + 0
      int __offset590__ = __offsetinbits585__ >> 3;
      int __shift591__ = __offsetinbits585__ - (__offset590__ << 3);
      int __rightele583__ = ((*(int *)(__left584__ + __offset590__))  >> __shift591__) & 0xffffffff;
      __inodeof___hash->add((int)__leftele582__, (int)__rightele583__);
    }
  }
}


// build rule14
{
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); ) {
    int __de__ = (int) __de___iterator->next();
    //de.inodenumber < d.s.NumberofInodes && !de.inodenumber == 0
    // __left595__ <-- de
    int __left595__ = (int) __de__;
    // __left595__ = de
    // __offsetinbits596__ <-- 8 * 124 + 0
    int __leftop598__ = 8;
    int __rightop599__ = 124;
    int __leftop597__ = __leftop598__ * __rightop599__;
    int __rightop600__ = 0;
    int __offsetinbits596__ = __leftop597__ + __rightop600__;
    // __offsetinbits596__ = 8 * 124 + 0
    int __offset601__ = __offsetinbits596__ >> 3;
    int __shift602__ = __offsetinbits596__ - (__offset601__ << 3);
    int __leftop594__ = ((*(int *)(__left595__ + __offset601__))  >> __shift602__) & 0xffffffff;
    // __left604__ <-- d.s
    // __left605__ <-- d
    int __left605__ = (int) d;
    // __left605__ = d
    int __left604__ = (__left605__ + 0);
    // __left604__ = d.s
    // __offsetinbits606__ <-- 32 + 32 + 32 + 0
    int __leftop607__ = 32;
    int __leftop609__ = 32;
    int __leftop611__ = 32;
    int __rightop612__ = 0;
    int __rightop610__ = __leftop611__ + __rightop612__;
    int __rightop608__ = __leftop609__ + __rightop610__;
    int __offsetinbits606__ = __leftop607__ + __rightop608__;
    // __offsetinbits606__ = 32 + 32 + 32 + 0
    int __offset613__ = __offsetinbits606__ >> 3;
    int __shift614__ = __offsetinbits606__ - (__offset613__ << 3);
    int __rightop603__ = ((*(int *)(__left604__ + __offset613__))  >> __shift614__) & 0xffffffff;
    int __leftop593__ = __leftop594__ < __rightop603__;
    // __left618__ <-- de
    int __left618__ = (int) __de__;
    // __left618__ = de
    // __offsetinbits619__ <-- 8 * 124 + 0
    int __leftop621__ = 8;
    int __rightop622__ = 124;
    int __leftop620__ = __leftop621__ * __rightop622__;
    int __rightop623__ = 0;
    int __offsetinbits619__ = __leftop620__ + __rightop623__;
    // __offsetinbits619__ = 8 * 124 + 0
    int __offset624__ = __offsetinbits619__ >> 3;
    int __shift625__ = __offsetinbits619__ - (__offset624__ << 3);
    int __leftop617__ = ((*(int *)(__left618__ + __offset624__))  >> __shift625__) & 0xffffffff;
    int __rightop626__ = 0;
    int __leftop616__ = __leftop617__ == __rightop626__;
    int __rightop615__ = !__leftop616__;
    int __tempvar592__ = __leftop593__ && __rightop615__;
    if (__tempvar592__) {
      // __left628__ <-- de
      int __left628__ = (int) __de__;
      // __left628__ = de
      // __offsetinbits629__ <-- 8 * 124 + 0
      int __leftop631__ = 8;
      int __rightop632__ = 124;
      int __leftop630__ = __leftop631__ * __rightop632__;
      int __rightop633__ = 0;
      int __offsetinbits629__ = __leftop630__ + __rightop633__;
      // __offsetinbits629__ = 8 * 124 + 0
      int __offset634__ = __offsetinbits629__ >> 3;
      int __shift635__ = __offsetinbits629__ - (__offset634__ << 3);
      int __element627__ = ((*(int *)(__left628__ + __offset634__))  >> __shift635__) & 0xffffffff;
      __FileInode___hash->add((int)__element627__, (int)__element627__);
    }
  }
}


// build rule16
{
  for (SimpleIterator* __j___iterator = __UsedInode___hash->iterator(); __j___iterator->hasNext(); ) {
    int __j__ = (int) __j___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      //true
      int __tempvar636__ = 1;
      if (__tempvar636__) {
        int __leftele637__ = (int) __j__;
        // __left639__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left640__ <-- cast(__InodeTable__, d.b[itb])
        // __left642__ <-- d
        int __left642__ = (int) d;
        // __left642__ = d
        // __offsetinbits643__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop644__ = 0;
        int __leftop648__ = 8;
        // __left650__ <-- d.s
        // __left651__ <-- d
        int __left651__ = (int) d;
        // __left651__ = d
        int __left650__ = (__left651__ + 0);
        // __left650__ = d.s
        // __offsetinbits652__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop653__ = 32;
        int __leftop655__ = 32;
        int __leftop657__ = 32;
        int __leftop659__ = 32;
        int __leftop661__ = 32;
        int __rightop662__ = 0;
        int __rightop660__ = __leftop661__ + __rightop662__;
        int __rightop658__ = __leftop659__ + __rightop660__;
        int __rightop656__ = __leftop657__ + __rightop658__;
        int __rightop654__ = __leftop655__ + __rightop656__;
        int __offsetinbits652__ = __leftop653__ + __rightop654__;
        // __offsetinbits652__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset663__ = __offsetinbits652__ >> 3;
        int __shift664__ = __offsetinbits652__ - (__offset663__ << 3);
        int __rightop649__ = ((*(int *)(__left650__ + __offset663__))  >> __shift664__) & 0xffffffff;
        int __leftop647__ = __leftop648__ * __rightop649__;
        int __rightop665__ = 0;
        int __leftop646__ = __leftop647__ + __rightop665__;
        int __rightop666__ = (int) __itb__;
        int __rightop645__ = __leftop646__ * __rightop666__;
        int __offsetinbits643__ = __leftop644__ + __rightop645__;
        // __offsetinbits643__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset667__ = __offsetinbits643__ >> 3;
        int __expr641__ = (__left642__ + __offset667__);
        int __left640__ = (int) __expr641__;
        // __left640__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits668__ <-- 0 + 32 + 32 * 12 + 32 + 0 * j
        int __leftop669__ = 0;
        int __leftop672__ = 32;
        int __leftop675__ = 32;
        int __rightop676__ = 12;
        int __leftop674__ = __leftop675__ * __rightop676__;
        int __leftop678__ = 32;
        int __rightop679__ = 0;
        int __rightop677__ = __leftop678__ + __rightop679__;
        int __rightop673__ = __leftop674__ + __rightop677__;
        int __leftop671__ = __leftop672__ + __rightop673__;
        int __rightop680__ = (int) __j__;
        int __rightop670__ = __leftop671__ * __rightop680__;
        int __offsetinbits668__ = __leftop669__ + __rightop670__;
        // __offsetinbits668__ = 0 + 32 + 32 * 12 + 32 + 0 * j
        int __offset681__ = __offsetinbits668__ >> 3;
        int __left639__ = (__left640__ + __offset681__);
        // __left639__ = cast(__InodeTable__, d.b[itb]).itable[j]
        // __offsetinbits682__ <-- 32 * 12 + 32 + 0
        int __leftop684__ = 32;
        int __rightop685__ = 12;
        int __leftop683__ = __leftop684__ * __rightop685__;
        int __leftop687__ = 32;
        int __rightop688__ = 0;
        int __rightop686__ = __leftop687__ + __rightop688__;
        int __offsetinbits682__ = __leftop683__ + __rightop686__;
        // __offsetinbits682__ = 32 * 12 + 32 + 0
        int __offset689__ = __offsetinbits682__ >> 3;
        int __shift690__ = __offsetinbits682__ - (__offset689__ << 3);
        int __rightele638__ = ((*(int *)(__left639__ + __offset689__))  >> __shift690__) & 0xffffffff;
        __referencecount___hash->add((int)__leftele637__, (int)__rightele638__);
      }
    }
  }
}


// build rule11
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar691__ = 0;
      int __tempvar692__ = 11;
      for (int __j__ = __tempvar691__; __j__ <= __tempvar692__; __j__++) {
        //cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] < d.s.NumberofBlocks && !cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0
        // __left696__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left697__ <-- cast(__InodeTable__, d.b[itb])
        // __left699__ <-- d
        int __left699__ = (int) d;
        // __left699__ = d
        // __offsetinbits700__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop701__ = 0;
        int __leftop705__ = 8;
        // __left707__ <-- d.s
        // __left708__ <-- d
        int __left708__ = (int) d;
        // __left708__ = d
        int __left707__ = (__left708__ + 0);
        // __left707__ = d.s
        // __offsetinbits709__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop710__ = 32;
        int __leftop712__ = 32;
        int __leftop714__ = 32;
        int __leftop716__ = 32;
        int __leftop718__ = 32;
        int __rightop719__ = 0;
        int __rightop717__ = __leftop718__ + __rightop719__;
        int __rightop715__ = __leftop716__ + __rightop717__;
        int __rightop713__ = __leftop714__ + __rightop715__;
        int __rightop711__ = __leftop712__ + __rightop713__;
        int __offsetinbits709__ = __leftop710__ + __rightop711__;
        // __offsetinbits709__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset720__ = __offsetinbits709__ >> 3;
        int __shift721__ = __offsetinbits709__ - (__offset720__ << 3);
        int __rightop706__ = ((*(int *)(__left707__ + __offset720__))  >> __shift721__) & 0xffffffff;
        int __leftop704__ = __leftop705__ * __rightop706__;
        int __rightop722__ = 0;
        int __leftop703__ = __leftop704__ + __rightop722__;
        int __rightop723__ = (int) __itb__;
        int __rightop702__ = __leftop703__ * __rightop723__;
        int __offsetinbits700__ = __leftop701__ + __rightop702__;
        // __offsetinbits700__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset724__ = __offsetinbits700__ >> 3;
        int __expr698__ = (__left699__ + __offset724__);
        int __left697__ = (int) __expr698__;
        // __left697__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits725__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
        int __leftop726__ = 0;
        int __leftop729__ = 32;
        int __leftop732__ = 32;
        int __rightop733__ = 12;
        int __leftop731__ = __leftop732__ * __rightop733__;
        int __leftop735__ = 32;
        int __rightop736__ = 0;
        int __rightop734__ = __leftop735__ + __rightop736__;
        int __rightop730__ = __leftop731__ + __rightop734__;
        int __leftop728__ = __leftop729__ + __rightop730__;
        int __rightop737__ = (int) __i__;
        int __rightop727__ = __leftop728__ * __rightop737__;
        int __offsetinbits725__ = __leftop726__ + __rightop727__;
        // __offsetinbits725__ = 0 + 32 + 32 * 12 + 32 + 0 * i
        int __offset738__ = __offsetinbits725__ >> 3;
        int __left696__ = (__left697__ + __offset738__);
        // __left696__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits739__ <-- 32 + 0 + 32 * j
        int __leftop741__ = 32;
        int __rightop742__ = 0;
        int __leftop740__ = __leftop741__ + __rightop742__;
        int __leftop744__ = 32;
        int __rightop745__ = (int) __j__;
        int __rightop743__ = __leftop744__ * __rightop745__;
        int __offsetinbits739__ = __leftop740__ + __rightop743__;
        // __offsetinbits739__ = 32 + 0 + 32 * j
        int __offset746__ = __offsetinbits739__ >> 3;
        int __shift747__ = __offsetinbits739__ - (__offset746__ << 3);
        int __leftop695__ = ((*(int *)(__left696__ + __offset746__))  >> __shift747__) & 0xffffffff;
        // __left749__ <-- d.s
        // __left750__ <-- d
        int __left750__ = (int) d;
        // __left750__ = d
        int __left749__ = (__left750__ + 0);
        // __left749__ = d.s
        // __offsetinbits751__ <-- 32 + 32 + 0
        int __leftop752__ = 32;
        int __leftop754__ = 32;
        int __rightop755__ = 0;
        int __rightop753__ = __leftop754__ + __rightop755__;
        int __offsetinbits751__ = __leftop752__ + __rightop753__;
        // __offsetinbits751__ = 32 + 32 + 0
        int __offset756__ = __offsetinbits751__ >> 3;
        int __shift757__ = __offsetinbits751__ - (__offset756__ << 3);
        int __rightop748__ = ((*(int *)(__left749__ + __offset756__))  >> __shift757__) & 0xffffffff;
        int __leftop694__ = __leftop695__ < __rightop748__;
        // __left761__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left762__ <-- cast(__InodeTable__, d.b[itb])
        // __left764__ <-- d
        int __left764__ = (int) d;
        // __left764__ = d
        // __offsetinbits765__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop766__ = 0;
        int __leftop770__ = 8;
        // __left772__ <-- d.s
        // __left773__ <-- d
        int __left773__ = (int) d;
        // __left773__ = d
        int __left772__ = (__left773__ + 0);
        // __left772__ = d.s
        // __offsetinbits774__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop775__ = 32;
        int __leftop777__ = 32;
        int __leftop779__ = 32;
        int __leftop781__ = 32;
        int __leftop783__ = 32;
        int __rightop784__ = 0;
        int __rightop782__ = __leftop783__ + __rightop784__;
        int __rightop780__ = __leftop781__ + __rightop782__;
        int __rightop778__ = __leftop779__ + __rightop780__;
        int __rightop776__ = __leftop777__ + __rightop778__;
        int __offsetinbits774__ = __leftop775__ + __rightop776__;
        // __offsetinbits774__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset785__ = __offsetinbits774__ >> 3;
        int __shift786__ = __offsetinbits774__ - (__offset785__ << 3);
        int __rightop771__ = ((*(int *)(__left772__ + __offset785__))  >> __shift786__) & 0xffffffff;
        int __leftop769__ = __leftop770__ * __rightop771__;
        int __rightop787__ = 0;
        int __leftop768__ = __leftop769__ + __rightop787__;
        int __rightop788__ = (int) __itb__;
        int __rightop767__ = __leftop768__ * __rightop788__;
        int __offsetinbits765__ = __leftop766__ + __rightop767__;
        // __offsetinbits765__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset789__ = __offsetinbits765__ >> 3;
        int __expr763__ = (__left764__ + __offset789__);
        int __left762__ = (int) __expr763__;
        // __left762__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits790__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
        int __leftop791__ = 0;
        int __leftop794__ = 32;
        int __leftop797__ = 32;
        int __rightop798__ = 12;
        int __leftop796__ = __leftop797__ * __rightop798__;
        int __leftop800__ = 32;
        int __rightop801__ = 0;
        int __rightop799__ = __leftop800__ + __rightop801__;
        int __rightop795__ = __leftop796__ + __rightop799__;
        int __leftop793__ = __leftop794__ + __rightop795__;
        int __rightop802__ = (int) __i__;
        int __rightop792__ = __leftop793__ * __rightop802__;
        int __offsetinbits790__ = __leftop791__ + __rightop792__;
        // __offsetinbits790__ = 0 + 32 + 32 * 12 + 32 + 0 * i
        int __offset803__ = __offsetinbits790__ >> 3;
        int __left761__ = (__left762__ + __offset803__);
        // __left761__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits804__ <-- 32 + 0 + 32 * j
        int __leftop806__ = 32;
        int __rightop807__ = 0;
        int __leftop805__ = __leftop806__ + __rightop807__;
        int __leftop809__ = 32;
        int __rightop810__ = (int) __j__;
        int __rightop808__ = __leftop809__ * __rightop810__;
        int __offsetinbits804__ = __leftop805__ + __rightop808__;
        // __offsetinbits804__ = 32 + 0 + 32 * j
        int __offset811__ = __offsetinbits804__ >> 3;
        int __shift812__ = __offsetinbits804__ - (__offset811__ << 3);
        int __leftop760__ = ((*(int *)(__left761__ + __offset811__))  >> __shift812__) & 0xffffffff;
        int __rightop813__ = 0;
        int __leftop759__ = __leftop760__ == __rightop813__;
        int __rightop758__ = !__leftop759__;
        int __tempvar693__ = __leftop694__ && __rightop758__;
        if (__tempvar693__) {
          // __left815__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left816__ <-- cast(__InodeTable__, d.b[itb])
          // __left818__ <-- d
          int __left818__ = (int) d;
          // __left818__ = d
          // __offsetinbits819__ <-- 0 + 8 * d.s.blocksize + 0 * itb
          int __leftop820__ = 0;
          int __leftop824__ = 8;
          // __left826__ <-- d.s
          // __left827__ <-- d
          int __left827__ = (int) d;
          // __left827__ = d
          int __left826__ = (__left827__ + 0);
          // __left826__ = d.s
          // __offsetinbits828__ <-- 32 + 32 + 32 + 32 + 32 + 0
          int __leftop829__ = 32;
          int __leftop831__ = 32;
          int __leftop833__ = 32;
          int __leftop835__ = 32;
          int __leftop837__ = 32;
          int __rightop838__ = 0;
          int __rightop836__ = __leftop837__ + __rightop838__;
          int __rightop834__ = __leftop835__ + __rightop836__;
          int __rightop832__ = __leftop833__ + __rightop834__;
          int __rightop830__ = __leftop831__ + __rightop832__;
          int __offsetinbits828__ = __leftop829__ + __rightop830__;
          // __offsetinbits828__ = 32 + 32 + 32 + 32 + 32 + 0
          int __offset839__ = __offsetinbits828__ >> 3;
          int __shift840__ = __offsetinbits828__ - (__offset839__ << 3);
          int __rightop825__ = ((*(int *)(__left826__ + __offset839__))  >> __shift840__) & 0xffffffff;
          int __leftop823__ = __leftop824__ * __rightop825__;
          int __rightop841__ = 0;
          int __leftop822__ = __leftop823__ + __rightop841__;
          int __rightop842__ = (int) __itb__;
          int __rightop821__ = __leftop822__ * __rightop842__;
          int __offsetinbits819__ = __leftop820__ + __rightop821__;
          // __offsetinbits819__ = 0 + 8 * d.s.blocksize + 0 * itb
          int __offset843__ = __offsetinbits819__ >> 3;
          int __expr817__ = (__left818__ + __offset843__);
          int __left816__ = (int) __expr817__;
          // __left816__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits844__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
          int __leftop845__ = 0;
          int __leftop848__ = 32;
          int __leftop851__ = 32;
          int __rightop852__ = 12;
          int __leftop850__ = __leftop851__ * __rightop852__;
          int __leftop854__ = 32;
          int __rightop855__ = 0;
          int __rightop853__ = __leftop854__ + __rightop855__;
          int __rightop849__ = __leftop850__ + __rightop853__;
          int __leftop847__ = __leftop848__ + __rightop849__;
          int __rightop856__ = (int) __i__;
          int __rightop846__ = __leftop847__ * __rightop856__;
          int __offsetinbits844__ = __leftop845__ + __rightop846__;
          // __offsetinbits844__ = 0 + 32 + 32 * 12 + 32 + 0 * i
          int __offset857__ = __offsetinbits844__ >> 3;
          int __left815__ = (__left816__ + __offset857__);
          // __left815__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits858__ <-- 32 + 0 + 32 * j
          int __leftop860__ = 32;
          int __rightop861__ = 0;
          int __leftop859__ = __leftop860__ + __rightop861__;
          int __leftop863__ = 32;
          int __rightop864__ = (int) __j__;
          int __rightop862__ = __leftop863__ * __rightop864__;
          int __offsetinbits858__ = __leftop859__ + __rightop862__;
          // __offsetinbits858__ = 32 + 0 + 32 * j
          int __offset865__ = __offsetinbits858__ >> 3;
          int __shift866__ = __offsetinbits858__ - (__offset865__ << 3);
          int __element814__ = ((*(int *)(__left815__ + __offset865__))  >> __shift866__) & 0xffffffff;
          __FileBlock___hash->add((int)__element814__, (int)__element814__);
        }
      }
    }
  }
}


// build rule8
{
  int __tempvar867__ = 0;
  // __left870__ <-- d.s
  // __left871__ <-- d
  int __left871__ = (int) d;
  // __left871__ = d
  int __left870__ = (__left871__ + 0);
  // __left870__ = d.s
  // __offsetinbits872__ <-- 32 + 32 + 0
  int __leftop873__ = 32;
  int __leftop875__ = 32;
  int __rightop876__ = 0;
  int __rightop874__ = __leftop875__ + __rightop876__;
  int __offsetinbits872__ = __leftop873__ + __rightop874__;
  // __offsetinbits872__ = 32 + 32 + 0
  int __offset877__ = __offsetinbits872__ >> 3;
  int __shift878__ = __offsetinbits872__ - (__offset877__ << 3);
  int __leftop869__ = ((*(int *)(__left870__ + __offset877__))  >> __shift878__) & 0xffffffff;
  int __rightop879__ = 1;
  int __tempvar868__ = __leftop869__ - __rightop879__;
  for (int __j__ = __tempvar867__; __j__ <= __tempvar868__; __j__++) {
    //!j in? __UsedBlock__
    int __element882__ = (int) __j__;
    int __leftop881__ = __UsedBlock___hash->contains(__element882__);
    int __tempvar880__ = !__leftop881__;
    if (__tempvar880__) {
      int __element883__ = (int) __j__;
      __FreeBlock___hash->add((int)__element883__, (int)__element883__);
    }
  }
}


// build rule10
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar884__ = 0;
      int __tempvar885__ = 11;
      for (int __j__ = __tempvar884__; __j__ <= __tempvar885__; __j__++) {
        //!cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0
        // __left889__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left890__ <-- cast(__InodeTable__, d.b[itb])
        // __left892__ <-- d
        int __left892__ = (int) d;
        // __left892__ = d
        // __offsetinbits893__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop894__ = 0;
        int __leftop898__ = 8;
        // __left900__ <-- d.s
        // __left901__ <-- d
        int __left901__ = (int) d;
        // __left901__ = d
        int __left900__ = (__left901__ + 0);
        // __left900__ = d.s
        // __offsetinbits902__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop903__ = 32;
        int __leftop905__ = 32;
        int __leftop907__ = 32;
        int __leftop909__ = 32;
        int __leftop911__ = 32;
        int __rightop912__ = 0;
        int __rightop910__ = __leftop911__ + __rightop912__;
        int __rightop908__ = __leftop909__ + __rightop910__;
        int __rightop906__ = __leftop907__ + __rightop908__;
        int __rightop904__ = __leftop905__ + __rightop906__;
        int __offsetinbits902__ = __leftop903__ + __rightop904__;
        // __offsetinbits902__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset913__ = __offsetinbits902__ >> 3;
        int __shift914__ = __offsetinbits902__ - (__offset913__ << 3);
        int __rightop899__ = ((*(int *)(__left900__ + __offset913__))  >> __shift914__) & 0xffffffff;
        int __leftop897__ = __leftop898__ * __rightop899__;
        int __rightop915__ = 0;
        int __leftop896__ = __leftop897__ + __rightop915__;
        int __rightop916__ = (int) __itb__;
        int __rightop895__ = __leftop896__ * __rightop916__;
        int __offsetinbits893__ = __leftop894__ + __rightop895__;
        // __offsetinbits893__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset917__ = __offsetinbits893__ >> 3;
        int __expr891__ = (__left892__ + __offset917__);
        int __left890__ = (int) __expr891__;
        // __left890__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits918__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
        int __leftop919__ = 0;
        int __leftop922__ = 32;
        int __leftop925__ = 32;
        int __rightop926__ = 12;
        int __leftop924__ = __leftop925__ * __rightop926__;
        int __leftop928__ = 32;
        int __rightop929__ = 0;
        int __rightop927__ = __leftop928__ + __rightop929__;
        int __rightop923__ = __leftop924__ + __rightop927__;
        int __leftop921__ = __leftop922__ + __rightop923__;
        int __rightop930__ = (int) __i__;
        int __rightop920__ = __leftop921__ * __rightop930__;
        int __offsetinbits918__ = __leftop919__ + __rightop920__;
        // __offsetinbits918__ = 0 + 32 + 32 * 12 + 32 + 0 * i
        int __offset931__ = __offsetinbits918__ >> 3;
        int __left889__ = (__left890__ + __offset931__);
        // __left889__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits932__ <-- 32 + 0 + 32 * j
        int __leftop934__ = 32;
        int __rightop935__ = 0;
        int __leftop933__ = __leftop934__ + __rightop935__;
        int __leftop937__ = 32;
        int __rightop938__ = (int) __j__;
        int __rightop936__ = __leftop937__ * __rightop938__;
        int __offsetinbits932__ = __leftop933__ + __rightop936__;
        // __offsetinbits932__ = 32 + 0 + 32 * j
        int __offset939__ = __offsetinbits932__ >> 3;
        int __shift940__ = __offsetinbits932__ - (__offset939__ << 3);
        int __leftop888__ = ((*(int *)(__left889__ + __offset939__))  >> __shift940__) & 0xffffffff;
        int __rightop941__ = 0;
        int __leftop887__ = __leftop888__ == __rightop941__;
        int __tempvar886__ = !__leftop887__;
        if (__tempvar886__) {
          int __leftele942__ = (int) __i__;
          // __left944__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left945__ <-- cast(__InodeTable__, d.b[itb])
          // __left947__ <-- d
          int __left947__ = (int) d;
          // __left947__ = d
          // __offsetinbits948__ <-- 0 + 8 * d.s.blocksize + 0 * itb
          int __leftop949__ = 0;
          int __leftop953__ = 8;
          // __left955__ <-- d.s
          // __left956__ <-- d
          int __left956__ = (int) d;
          // __left956__ = d
          int __left955__ = (__left956__ + 0);
          // __left955__ = d.s
          // __offsetinbits957__ <-- 32 + 32 + 32 + 32 + 32 + 0
          int __leftop958__ = 32;
          int __leftop960__ = 32;
          int __leftop962__ = 32;
          int __leftop964__ = 32;
          int __leftop966__ = 32;
          int __rightop967__ = 0;
          int __rightop965__ = __leftop966__ + __rightop967__;
          int __rightop963__ = __leftop964__ + __rightop965__;
          int __rightop961__ = __leftop962__ + __rightop963__;
          int __rightop959__ = __leftop960__ + __rightop961__;
          int __offsetinbits957__ = __leftop958__ + __rightop959__;
          // __offsetinbits957__ = 32 + 32 + 32 + 32 + 32 + 0
          int __offset968__ = __offsetinbits957__ >> 3;
          int __shift969__ = __offsetinbits957__ - (__offset968__ << 3);
          int __rightop954__ = ((*(int *)(__left955__ + __offset968__))  >> __shift969__) & 0xffffffff;
          int __leftop952__ = __leftop953__ * __rightop954__;
          int __rightop970__ = 0;
          int __leftop951__ = __leftop952__ + __rightop970__;
          int __rightop971__ = (int) __itb__;
          int __rightop950__ = __leftop951__ * __rightop971__;
          int __offsetinbits948__ = __leftop949__ + __rightop950__;
          // __offsetinbits948__ = 0 + 8 * d.s.blocksize + 0 * itb
          int __offset972__ = __offsetinbits948__ >> 3;
          int __expr946__ = (__left947__ + __offset972__);
          int __left945__ = (int) __expr946__;
          // __left945__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits973__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
          int __leftop974__ = 0;
          int __leftop977__ = 32;
          int __leftop980__ = 32;
          int __rightop981__ = 12;
          int __leftop979__ = __leftop980__ * __rightop981__;
          int __leftop983__ = 32;
          int __rightop984__ = 0;
          int __rightop982__ = __leftop983__ + __rightop984__;
          int __rightop978__ = __leftop979__ + __rightop982__;
          int __leftop976__ = __leftop977__ + __rightop978__;
          int __rightop985__ = (int) __i__;
          int __rightop975__ = __leftop976__ * __rightop985__;
          int __offsetinbits973__ = __leftop974__ + __rightop975__;
          // __offsetinbits973__ = 0 + 32 + 32 * 12 + 32 + 0 * i
          int __offset986__ = __offsetinbits973__ >> 3;
          int __left944__ = (__left945__ + __offset986__);
          // __left944__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits987__ <-- 32 + 0 + 32 * j
          int __leftop989__ = 32;
          int __rightop990__ = 0;
          int __leftop988__ = __leftop989__ + __rightop990__;
          int __leftop992__ = 32;
          int __rightop993__ = (int) __j__;
          int __rightop991__ = __leftop992__ * __rightop993__;
          int __offsetinbits987__ = __leftop988__ + __rightop991__;
          // __offsetinbits987__ = 32 + 0 + 32 * j
          int __offset994__ = __offsetinbits987__ >> 3;
          int __shift995__ = __offsetinbits987__ - (__offset994__ << 3);
          int __rightele943__ = ((*(int *)(__left944__ + __offset994__))  >> __shift995__) & 0xffffffff;
          __contents___hash->add((int)__leftele942__, (int)__rightele943__);
        }
      }
    }
  }
}


// build rule7
{
  int __tempvar996__ = 0;
  // __left999__ <-- d.s
  // __left1000__ <-- d
  int __left1000__ = (int) d;
  // __left1000__ = d
  int __left999__ = (__left1000__ + 0);
  // __left999__ = d.s
  // __offsetinbits1001__ <-- 32 + 32 + 32 + 0
  int __leftop1002__ = 32;
  int __leftop1004__ = 32;
  int __leftop1006__ = 32;
  int __rightop1007__ = 0;
  int __rightop1005__ = __leftop1006__ + __rightop1007__;
  int __rightop1003__ = __leftop1004__ + __rightop1005__;
  int __offsetinbits1001__ = __leftop1002__ + __rightop1003__;
  // __offsetinbits1001__ = 32 + 32 + 32 + 0
  int __offset1008__ = __offsetinbits1001__ >> 3;
  int __shift1009__ = __offsetinbits1001__ - (__offset1008__ << 3);
  int __leftop998__ = ((*(int *)(__left999__ + __offset1008__))  >> __shift1009__) & 0xffffffff;
  int __rightop1010__ = 1;
  int __tempvar997__ = __leftop998__ - __rightop1010__;
  for (int __j__ = __tempvar996__; __j__ <= __tempvar997__; __j__++) {
    //!j in? __UsedInode__
    int __element1013__ = (int) __j__;
    int __leftop1012__ = __UsedInode___hash->contains(__element1013__);
    int __tempvar1011__ = !__leftop1012__;
    if (__tempvar1011__) {
      int __element1014__ = (int) __j__;
      __FreeInode___hash->add((int)__element1014__, (int)__element1014__);
    }
  }
}


// build rule17
{
  for (SimpleIterator* __j___iterator = __UsedInode___hash->iterator(); __j___iterator->hasNext(); ) {
    int __j__ = (int) __j___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      //true
      int __tempvar1015__ = 1;
      if (__tempvar1015__) {
        int __leftele1016__ = (int) __j__;
        // __left1018__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left1019__ <-- cast(__InodeTable__, d.b[itb])
        // __left1021__ <-- d
        int __left1021__ = (int) d;
        // __left1021__ = d
        // __offsetinbits1022__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop1023__ = 0;
        int __leftop1027__ = 8;
        // __left1029__ <-- d.s
        // __left1030__ <-- d
        int __left1030__ = (int) d;
        // __left1030__ = d
        int __left1029__ = (__left1030__ + 0);
        // __left1029__ = d.s
        // __offsetinbits1031__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop1032__ = 32;
        int __leftop1034__ = 32;
        int __leftop1036__ = 32;
        int __leftop1038__ = 32;
        int __leftop1040__ = 32;
        int __rightop1041__ = 0;
        int __rightop1039__ = __leftop1040__ + __rightop1041__;
        int __rightop1037__ = __leftop1038__ + __rightop1039__;
        int __rightop1035__ = __leftop1036__ + __rightop1037__;
        int __rightop1033__ = __leftop1034__ + __rightop1035__;
        int __offsetinbits1031__ = __leftop1032__ + __rightop1033__;
        // __offsetinbits1031__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset1042__ = __offsetinbits1031__ >> 3;
        int __shift1043__ = __offsetinbits1031__ - (__offset1042__ << 3);
        int __rightop1028__ = ((*(int *)(__left1029__ + __offset1042__))  >> __shift1043__) & 0xffffffff;
        int __leftop1026__ = __leftop1027__ * __rightop1028__;
        int __rightop1044__ = 0;
        int __leftop1025__ = __leftop1026__ + __rightop1044__;
        int __rightop1045__ = (int) __itb__;
        int __rightop1024__ = __leftop1025__ * __rightop1045__;
        int __offsetinbits1022__ = __leftop1023__ + __rightop1024__;
        // __offsetinbits1022__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset1046__ = __offsetinbits1022__ >> 3;
        int __expr1020__ = (__left1021__ + __offset1046__);
        int __left1019__ = (int) __expr1020__;
        // __left1019__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1047__ <-- 0 + 32 + 32 * 12 + 32 + 0 * j
        int __leftop1048__ = 0;
        int __leftop1051__ = 32;
        int __leftop1054__ = 32;
        int __rightop1055__ = 12;
        int __leftop1053__ = __leftop1054__ * __rightop1055__;
        int __leftop1057__ = 32;
        int __rightop1058__ = 0;
        int __rightop1056__ = __leftop1057__ + __rightop1058__;
        int __rightop1052__ = __leftop1053__ + __rightop1056__;
        int __leftop1050__ = __leftop1051__ + __rightop1052__;
        int __rightop1059__ = (int) __j__;
        int __rightop1049__ = __leftop1050__ * __rightop1059__;
        int __offsetinbits1047__ = __leftop1048__ + __rightop1049__;
        // __offsetinbits1047__ = 0 + 32 + 32 * 12 + 32 + 0 * j
        int __offset1060__ = __offsetinbits1047__ >> 3;
        int __left1018__ = (__left1019__ + __offset1060__);
        // __left1018__ = cast(__InodeTable__, d.b[itb]).itable[j]
        int __rightele1017__ = ((*(int *)(__left1018__ + 0))  >> 0) & 0xffffffff;
        __filesize___hash->add((int)__leftele1016__, (int)__rightele1017__);
      }
    }
  }
}


// build rule18
{
  int __tempvar1061__ = 0;
  // __left1064__ <-- d.s
  // __left1065__ <-- d
  int __left1065__ = (int) d;
  // __left1065__ = d
  int __left1064__ = (__left1065__ + 0);
  // __left1064__ = d.s
  // __offsetinbits1066__ <-- 32 + 32 + 0
  int __leftop1067__ = 32;
  int __leftop1069__ = 32;
  int __rightop1070__ = 0;
  int __rightop1068__ = __leftop1069__ + __rightop1070__;
  int __offsetinbits1066__ = __leftop1067__ + __rightop1068__;
  // __offsetinbits1066__ = 32 + 32 + 0
  int __offset1071__ = __offsetinbits1066__ >> 3;
  int __shift1072__ = __offsetinbits1066__ - (__offset1071__ << 3);
  int __leftop1063__ = ((*(int *)(__left1064__ + __offset1071__))  >> __shift1072__) & 0xffffffff;
  int __rightop1073__ = 1;
  int __tempvar1062__ = __leftop1063__ - __rightop1073__;
  for (int __j__ = __tempvar1061__; __j__ <= __tempvar1062__; __j__++) {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); ) {
      int __bbb__ = (int) __bbb___iterator->next();
      //cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == {false = 107}
      // __left1076__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left1078__ <-- d
      int __left1078__ = (int) d;
      // __left1078__ = d
      // __offsetinbits1079__ <-- 0 + 8 * d.s.blocksize + 0 * bbb
      int __leftop1080__ = 0;
      int __leftop1084__ = 8;
      // __left1086__ <-- d.s
      // __left1087__ <-- d
      int __left1087__ = (int) d;
      // __left1087__ = d
      int __left1086__ = (__left1087__ + 0);
      // __left1086__ = d.s
      // __offsetinbits1088__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop1089__ = 32;
      int __leftop1091__ = 32;
      int __leftop1093__ = 32;
      int __leftop1095__ = 32;
      int __leftop1097__ = 32;
      int __rightop1098__ = 0;
      int __rightop1096__ = __leftop1097__ + __rightop1098__;
      int __rightop1094__ = __leftop1095__ + __rightop1096__;
      int __rightop1092__ = __leftop1093__ + __rightop1094__;
      int __rightop1090__ = __leftop1091__ + __rightop1092__;
      int __offsetinbits1088__ = __leftop1089__ + __rightop1090__;
      // __offsetinbits1088__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset1099__ = __offsetinbits1088__ >> 3;
      int __shift1100__ = __offsetinbits1088__ - (__offset1099__ << 3);
      int __rightop1085__ = ((*(int *)(__left1086__ + __offset1099__))  >> __shift1100__) & 0xffffffff;
      int __leftop1083__ = __leftop1084__ * __rightop1085__;
      int __rightop1101__ = 0;
      int __leftop1082__ = __leftop1083__ + __rightop1101__;
      int __rightop1102__ = (int) __bbb__;
      int __rightop1081__ = __leftop1082__ * __rightop1102__;
      int __offsetinbits1079__ = __leftop1080__ + __rightop1081__;
      // __offsetinbits1079__ = 0 + 8 * d.s.blocksize + 0 * bbb
      int __offset1103__ = __offsetinbits1079__ >> 3;
      int __expr1077__ = (__left1078__ + __offset1103__);
      int __left1076__ = (int) __expr1077__;
      // __left1076__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits1104__ <-- 0 + 1 * j
      int __leftop1105__ = 0;
      int __leftop1107__ = 1;
      int __rightop1108__ = (int) __j__;
      int __rightop1106__ = __leftop1107__ * __rightop1108__;
      int __offsetinbits1104__ = __leftop1105__ + __rightop1106__;
      // __offsetinbits1104__ = 0 + 1 * j
      int __offset1109__ = __offsetinbits1104__ >> 3;
      int __shift1110__ = __offsetinbits1104__ - (__offset1109__ << 3);
      int __leftop1075__ = ((*(int *)(__left1076__ + __offset1109__))  >> __shift1110__) & 0x1;
      int __rightop1111__ = 107;
      int __tempvar1074__ = __leftop1075__ == __rightop1111__;
      if (__tempvar1074__) {
        int __leftele1112__ = (int) __j__;
        int __rightele1113__ = 108;
        __blockstatus___hash->add((int)__leftele1112__, (int)__rightele1113__);
      }
    }
  }
}


// build rule19
{
  int __tempvar1114__ = 0;
  // __left1117__ <-- d.s
  // __left1118__ <-- d
  int __left1118__ = (int) d;
  // __left1118__ = d
  int __left1117__ = (__left1118__ + 0);
  // __left1117__ = d.s
  // __offsetinbits1119__ <-- 32 + 32 + 0
  int __leftop1120__ = 32;
  int __leftop1122__ = 32;
  int __rightop1123__ = 0;
  int __rightop1121__ = __leftop1122__ + __rightop1123__;
  int __offsetinbits1119__ = __leftop1120__ + __rightop1121__;
  // __offsetinbits1119__ = 32 + 32 + 0
  int __offset1124__ = __offsetinbits1119__ >> 3;
  int __shift1125__ = __offsetinbits1119__ - (__offset1124__ << 3);
  int __leftop1116__ = ((*(int *)(__left1117__ + __offset1124__))  >> __shift1125__) & 0xffffffff;
  int __rightop1126__ = 1;
  int __tempvar1115__ = __leftop1116__ - __rightop1126__;
  for (int __j__ = __tempvar1114__; __j__ <= __tempvar1115__; __j__++) {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); ) {
      int __bbb__ = (int) __bbb___iterator->next();
      //cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == true
      // __left1129__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left1131__ <-- d
      int __left1131__ = (int) d;
      // __left1131__ = d
      // __offsetinbits1132__ <-- 0 + 8 * d.s.blocksize + 0 * bbb
      int __leftop1133__ = 0;
      int __leftop1137__ = 8;
      // __left1139__ <-- d.s
      // __left1140__ <-- d
      int __left1140__ = (int) d;
      // __left1140__ = d
      int __left1139__ = (__left1140__ + 0);
      // __left1139__ = d.s
      // __offsetinbits1141__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop1142__ = 32;
      int __leftop1144__ = 32;
      int __leftop1146__ = 32;
      int __leftop1148__ = 32;
      int __leftop1150__ = 32;
      int __rightop1151__ = 0;
      int __rightop1149__ = __leftop1150__ + __rightop1151__;
      int __rightop1147__ = __leftop1148__ + __rightop1149__;
      int __rightop1145__ = __leftop1146__ + __rightop1147__;
      int __rightop1143__ = __leftop1144__ + __rightop1145__;
      int __offsetinbits1141__ = __leftop1142__ + __rightop1143__;
      // __offsetinbits1141__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset1152__ = __offsetinbits1141__ >> 3;
      int __shift1153__ = __offsetinbits1141__ - (__offset1152__ << 3);
      int __rightop1138__ = ((*(int *)(__left1139__ + __offset1152__))  >> __shift1153__) & 0xffffffff;
      int __leftop1136__ = __leftop1137__ * __rightop1138__;
      int __rightop1154__ = 0;
      int __leftop1135__ = __leftop1136__ + __rightop1154__;
      int __rightop1155__ = (int) __bbb__;
      int __rightop1134__ = __leftop1135__ * __rightop1155__;
      int __offsetinbits1132__ = __leftop1133__ + __rightop1134__;
      // __offsetinbits1132__ = 0 + 8 * d.s.blocksize + 0 * bbb
      int __offset1156__ = __offsetinbits1132__ >> 3;
      int __expr1130__ = (__left1131__ + __offset1156__);
      int __left1129__ = (int) __expr1130__;
      // __left1129__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits1157__ <-- 0 + 1 * j
      int __leftop1158__ = 0;
      int __leftop1160__ = 1;
      int __rightop1161__ = (int) __j__;
      int __rightop1159__ = __leftop1160__ * __rightop1161__;
      int __offsetinbits1157__ = __leftop1158__ + __rightop1159__;
      // __offsetinbits1157__ = 0 + 1 * j
      int __offset1162__ = __offsetinbits1157__ >> 3;
      int __shift1163__ = __offsetinbits1157__ - (__offset1162__ << 3);
      int __leftop1128__ = ((*(int *)(__left1129__ + __offset1162__))  >> __shift1163__) & 0x1;
      int __rightop1164__ = 1;
      int __tempvar1127__ = __leftop1128__ == __rightop1164__;
      if (__tempvar1127__) {
        int __leftele1165__ = (int) __j__;
        int __rightele1166__ = 109;
        __blockstatus___hash->add((int)__leftele1165__, (int)__rightele1166__);
      }
    }
  }
}


