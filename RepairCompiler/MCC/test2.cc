
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
  //(d.g.InodeTableBlock < d.s.NumberofBlocks)
  // __left6__ <-- d.g
  // __left7__ <-- d
  int __left7__ = (int) d;
  // __left7__ = d
  // __offsetinbits8__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop9__ = 0;
  int __leftop13__ = 8;
  // __left15__ <-- d.s
  // __left16__ <-- d
  int __left16__ = (int) d;
  // __left16__ = d
  int __left15__ = (__left16__ + 0);
  // __left15__ = d.s
  // __offsetinbits17__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
  // __offsetinbits17__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset28__ = __offsetinbits17__ >> 3;
  int __shift29__ = __offsetinbits17__ - (__offset28__ << 3);
  int __rightop14__ = ((*(int *)(__left15__ + __offset28__))  >> __shift29__) & 0xffffffff;
  int __leftop12__ = __leftop13__ * __rightop14__;
  int __rightop30__ = 0;
  int __leftop11__ = __leftop12__ + __rightop30__;
  int __rightop31__ = 1;
  int __rightop10__ = __leftop11__ * __rightop31__;
  int __offsetinbits8__ = __leftop9__ + __rightop10__;
  // __offsetinbits8__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset32__ = __offsetinbits8__ >> 3;
  int __left6__ = (__left7__ + __offset32__);
  // __left6__ = d.g
  // __offsetinbits33__ <-- (32 + (32 + 0))
  int __leftop34__ = 32;
  int __leftop36__ = 32;
  int __rightop37__ = 0;
  int __rightop35__ = __leftop36__ + __rightop37__;
  int __offsetinbits33__ = __leftop34__ + __rightop35__;
  // __offsetinbits33__ = (32 + (32 + 0))
  int __offset38__ = __offsetinbits33__ >> 3;
  int __shift39__ = __offsetinbits33__ - (__offset38__ << 3);
  int __leftop5__ = ((*(int *)(__left6__ + __offset38__))  >> __shift39__) & 0xffffffff;
  // __left41__ <-- d.s
  // __left42__ <-- d
  int __left42__ = (int) d;
  // __left42__ = d
  int __left41__ = (__left42__ + 0);
  // __left41__ = d.s
  // __offsetinbits43__ <-- (32 + (32 + 0))
  int __leftop44__ = 32;
  int __leftop46__ = 32;
  int __rightop47__ = 0;
  int __rightop45__ = __leftop46__ + __rightop47__;
  int __offsetinbits43__ = __leftop44__ + __rightop45__;
  // __offsetinbits43__ = (32 + (32 + 0))
  int __offset48__ = __offsetinbits43__ >> 3;
  int __shift49__ = __offsetinbits43__ - (__offset48__ << 3);
  int __rightop40__ = ((*(int *)(__left41__ + __offset48__))  >> __shift49__) & 0xffffffff;
  int __tempvar4__ = __leftop5__ < __rightop40__;
  if (__tempvar4__) {
    // __left51__ <-- d.g
    // __left52__ <-- d
    int __left52__ = (int) d;
    // __left52__ = d
    // __offsetinbits53__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop54__ = 0;
    int __leftop58__ = 8;
    // __left60__ <-- d.s
    // __left61__ <-- d
    int __left61__ = (int) d;
    // __left61__ = d
    int __left60__ = (__left61__ + 0);
    // __left60__ = d.s
    // __offsetinbits62__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
    // __offsetinbits62__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset73__ = __offsetinbits62__ >> 3;
    int __shift74__ = __offsetinbits62__ - (__offset73__ << 3);
    int __rightop59__ = ((*(int *)(__left60__ + __offset73__))  >> __shift74__) & 0xffffffff;
    int __leftop57__ = __leftop58__ * __rightop59__;
    int __rightop75__ = 0;
    int __leftop56__ = __leftop57__ + __rightop75__;
    int __rightop76__ = 1;
    int __rightop55__ = __leftop56__ * __rightop76__;
    int __offsetinbits53__ = __leftop54__ + __rightop55__;
    // __offsetinbits53__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset77__ = __offsetinbits53__ >> 3;
    int __left51__ = (__left52__ + __offset77__);
    // __left51__ = d.g
    // __offsetinbits78__ <-- (32 + (32 + 0))
    int __leftop79__ = 32;
    int __leftop81__ = 32;
    int __rightop82__ = 0;
    int __rightop80__ = __leftop81__ + __rightop82__;
    int __offsetinbits78__ = __leftop79__ + __rightop80__;
    // __offsetinbits78__ = (32 + (32 + 0))
    int __offset83__ = __offsetinbits78__ >> 3;
    int __shift84__ = __offsetinbits78__ - (__offset83__ << 3);
    int __element50__ = ((*(int *)(__left51__ + __offset83__))  >> __shift84__) & 0xffffffff;
    __InodeTableBlock___hash->add((int)__element50__, (int)__element50__);
  }
}


// build rule4
{
  //(d.g.InodeBitmapBlock < d.s.NumberofBlocks)
  // __left87__ <-- d.g
  // __left88__ <-- d
  int __left88__ = (int) d;
  // __left88__ = d
  // __offsetinbits89__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop90__ = 0;
  int __leftop94__ = 8;
  // __left96__ <-- d.s
  // __left97__ <-- d
  int __left97__ = (int) d;
  // __left97__ = d
  int __left96__ = (__left97__ + 0);
  // __left96__ = d.s
  // __offsetinbits98__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
  // __offsetinbits98__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset109__ = __offsetinbits98__ >> 3;
  int __shift110__ = __offsetinbits98__ - (__offset109__ << 3);
  int __rightop95__ = ((*(int *)(__left96__ + __offset109__))  >> __shift110__) & 0xffffffff;
  int __leftop93__ = __leftop94__ * __rightop95__;
  int __rightop111__ = 0;
  int __leftop92__ = __leftop93__ + __rightop111__;
  int __rightop112__ = 1;
  int __rightop91__ = __leftop92__ * __rightop112__;
  int __offsetinbits89__ = __leftop90__ + __rightop91__;
  // __offsetinbits89__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset113__ = __offsetinbits89__ >> 3;
  int __left87__ = (__left88__ + __offset113__);
  // __left87__ = d.g
  // __offsetinbits114__ <-- (32 + 0)
  int __leftop115__ = 32;
  int __rightop116__ = 0;
  int __offsetinbits114__ = __leftop115__ + __rightop116__;
  // __offsetinbits114__ = (32 + 0)
  int __offset117__ = __offsetinbits114__ >> 3;
  int __shift118__ = __offsetinbits114__ - (__offset117__ << 3);
  int __leftop86__ = ((*(int *)(__left87__ + __offset117__))  >> __shift118__) & 0xffffffff;
  // __left120__ <-- d.s
  // __left121__ <-- d
  int __left121__ = (int) d;
  // __left121__ = d
  int __left120__ = (__left121__ + 0);
  // __left120__ = d.s
  // __offsetinbits122__ <-- (32 + (32 + 0))
  int __leftop123__ = 32;
  int __leftop125__ = 32;
  int __rightop126__ = 0;
  int __rightop124__ = __leftop125__ + __rightop126__;
  int __offsetinbits122__ = __leftop123__ + __rightop124__;
  // __offsetinbits122__ = (32 + (32 + 0))
  int __offset127__ = __offsetinbits122__ >> 3;
  int __shift128__ = __offsetinbits122__ - (__offset127__ << 3);
  int __rightop119__ = ((*(int *)(__left120__ + __offset127__))  >> __shift128__) & 0xffffffff;
  int __tempvar85__ = __leftop86__ < __rightop119__;
  if (__tempvar85__) {
    // __left130__ <-- d.g
    // __left131__ <-- d
    int __left131__ = (int) d;
    // __left131__ = d
    // __offsetinbits132__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop133__ = 0;
    int __leftop137__ = 8;
    // __left139__ <-- d.s
    // __left140__ <-- d
    int __left140__ = (int) d;
    // __left140__ = d
    int __left139__ = (__left140__ + 0);
    // __left139__ = d.s
    // __offsetinbits141__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
    // __offsetinbits141__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset152__ = __offsetinbits141__ >> 3;
    int __shift153__ = __offsetinbits141__ - (__offset152__ << 3);
    int __rightop138__ = ((*(int *)(__left139__ + __offset152__))  >> __shift153__) & 0xffffffff;
    int __leftop136__ = __leftop137__ * __rightop138__;
    int __rightop154__ = 0;
    int __leftop135__ = __leftop136__ + __rightop154__;
    int __rightop155__ = 1;
    int __rightop134__ = __leftop135__ * __rightop155__;
    int __offsetinbits132__ = __leftop133__ + __rightop134__;
    // __offsetinbits132__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset156__ = __offsetinbits132__ >> 3;
    int __left130__ = (__left131__ + __offset156__);
    // __left130__ = d.g
    // __offsetinbits157__ <-- (32 + 0)
    int __leftop158__ = 32;
    int __rightop159__ = 0;
    int __offsetinbits157__ = __leftop158__ + __rightop159__;
    // __offsetinbits157__ = (32 + 0)
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
  // __offsetinbits167__ <-- (32 + (32 + (32 + 0)))
  int __leftop168__ = 32;
  int __leftop170__ = 32;
  int __leftop172__ = 32;
  int __rightop173__ = 0;
  int __rightop171__ = __leftop172__ + __rightop173__;
  int __rightop169__ = __leftop170__ + __rightop171__;
  int __offsetinbits167__ = __leftop168__ + __rightop169__;
  // __offsetinbits167__ = (32 + (32 + (32 + 0)))
  int __offset174__ = __offsetinbits167__ >> 3;
  int __shift175__ = __offsetinbits167__ - (__offset174__ << 3);
  int __leftop164__ = ((*(int *)(__left165__ + __offset174__))  >> __shift175__) & 0xffffffff;
  int __rightop176__ = 1;
  int __tempvar163__ = __leftop164__ - __rightop176__;
  for (int __j__ = __tempvar162__; __j__ <= __tempvar163__; __j__++) {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); ) {
      int __ibb__ = (int) __ibb___iterator->next();
      //(cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == false)
      // __left179__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left181__ <-- d
      int __left181__ = (int) d;
      // __left181__ = d
      // __offsetinbits182__ <-- (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __leftop183__ = 0;
      int __leftop187__ = 8;
      // __left189__ <-- d.s
      // __left190__ <-- d
      int __left190__ = (int) d;
      // __left190__ = d
      int __left189__ = (__left190__ + 0);
      // __left189__ = d.s
      // __offsetinbits191__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
      // __offsetinbits191__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset202__ = __offsetinbits191__ >> 3;
      int __shift203__ = __offsetinbits191__ - (__offset202__ << 3);
      int __rightop188__ = ((*(int *)(__left189__ + __offset202__))  >> __shift203__) & 0xffffffff;
      int __leftop186__ = __leftop187__ * __rightop188__;
      int __rightop204__ = 0;
      int __leftop185__ = __leftop186__ + __rightop204__;
      int __rightop205__ = (int) __ibb__;
      int __rightop184__ = __leftop185__ * __rightop205__;
      int __offsetinbits182__ = __leftop183__ + __rightop184__;
      // __offsetinbits182__ = (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __offset206__ = __offsetinbits182__ >> 3;
      int __expr180__ = (__left181__ + __offset206__);
      int __left179__ = (int) __expr180__;
      // __left179__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits207__ <-- (0 + (1 * j))
      int __leftop208__ = 0;
      int __leftop210__ = 1;
      int __rightop211__ = (int) __j__;
      int __rightop209__ = __leftop210__ * __rightop211__;
      int __offsetinbits207__ = __leftop208__ + __rightop209__;
      // __offsetinbits207__ = (0 + (1 * j))
      int __offset212__ = __offsetinbits207__ >> 3;
      int __shift213__ = __offsetinbits207__ - (__offset212__ << 3);
      int __leftop178__ = ((*(int *)(__left179__ + __offset212__))  >> __shift213__) & 0x1;
      int __rightop214__ = 0;
      int __tempvar177__ = __leftop178__ == __rightop214__;
      if (__tempvar177__) {
        int __leftele215__ = (int) __j__;
        int __rightele216__ = 101;
        __inodestatus___hash->add((int)__leftele215__, (int)__rightele216__);
      }
    }
  }
}


// build rule5
{
  //(d.g.BlockBitmapBlock < d.s.NumberofBlocks)
  // __left220__ <-- d.g
  // __left221__ <-- d
  int __left221__ = (int) d;
  // __left221__ = d
  // __offsetinbits222__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop223__ = 0;
  int __leftop227__ = 8;
  // __left229__ <-- d.s
  // __left230__ <-- d
  int __left230__ = (int) d;
  // __left230__ = d
  int __left229__ = (__left230__ + 0);
  // __left229__ = d.s
  // __offsetinbits231__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop232__ = 32;
  int __leftop234__ = 32;
  int __leftop236__ = 32;
  int __leftop238__ = 32;
  int __leftop240__ = 32;
  int __rightop241__ = 0;
  int __rightop239__ = __leftop240__ + __rightop241__;
  int __rightop237__ = __leftop238__ + __rightop239__;
  int __rightop235__ = __leftop236__ + __rightop237__;
  int __rightop233__ = __leftop234__ + __rightop235__;
  int __offsetinbits231__ = __leftop232__ + __rightop233__;
  // __offsetinbits231__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset242__ = __offsetinbits231__ >> 3;
  int __shift243__ = __offsetinbits231__ - (__offset242__ << 3);
  int __rightop228__ = ((*(int *)(__left229__ + __offset242__))  >> __shift243__) & 0xffffffff;
  int __leftop226__ = __leftop227__ * __rightop228__;
  int __rightop244__ = 0;
  int __leftop225__ = __leftop226__ + __rightop244__;
  int __rightop245__ = 1;
  int __rightop224__ = __leftop225__ * __rightop245__;
  int __offsetinbits222__ = __leftop223__ + __rightop224__;
  // __offsetinbits222__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset246__ = __offsetinbits222__ >> 3;
  int __left220__ = (__left221__ + __offset246__);
  // __left220__ = d.g
  int __leftop219__ = ((*(int *)(__left220__ + 0))  >> 0) & 0xffffffff;
  // __left248__ <-- d.s
  // __left249__ <-- d
  int __left249__ = (int) d;
  // __left249__ = d
  int __left248__ = (__left249__ + 0);
  // __left248__ = d.s
  // __offsetinbits250__ <-- (32 + (32 + 0))
  int __leftop251__ = 32;
  int __leftop253__ = 32;
  int __rightop254__ = 0;
  int __rightop252__ = __leftop253__ + __rightop254__;
  int __offsetinbits250__ = __leftop251__ + __rightop252__;
  // __offsetinbits250__ = (32 + (32 + 0))
  int __offset255__ = __offsetinbits250__ >> 3;
  int __shift256__ = __offsetinbits250__ - (__offset255__ << 3);
  int __rightop247__ = ((*(int *)(__left248__ + __offset255__))  >> __shift256__) & 0xffffffff;
  int __tempvar218__ = __leftop219__ < __rightop247__;
  if (__tempvar218__) {
    // __left258__ <-- d.g
    // __left259__ <-- d
    int __left259__ = (int) d;
    // __left259__ = d
    // __offsetinbits260__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop261__ = 0;
    int __leftop265__ = 8;
    // __left267__ <-- d.s
    // __left268__ <-- d
    int __left268__ = (int) d;
    // __left268__ = d
    int __left267__ = (__left268__ + 0);
    // __left267__ = d.s
    // __offsetinbits269__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop270__ = 32;
    int __leftop272__ = 32;
    int __leftop274__ = 32;
    int __leftop276__ = 32;
    int __leftop278__ = 32;
    int __rightop279__ = 0;
    int __rightop277__ = __leftop278__ + __rightop279__;
    int __rightop275__ = __leftop276__ + __rightop277__;
    int __rightop273__ = __leftop274__ + __rightop275__;
    int __rightop271__ = __leftop272__ + __rightop273__;
    int __offsetinbits269__ = __leftop270__ + __rightop271__;
    // __offsetinbits269__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset280__ = __offsetinbits269__ >> 3;
    int __shift281__ = __offsetinbits269__ - (__offset280__ << 3);
    int __rightop266__ = ((*(int *)(__left267__ + __offset280__))  >> __shift281__) & 0xffffffff;
    int __leftop264__ = __leftop265__ * __rightop266__;
    int __rightop282__ = 0;
    int __leftop263__ = __leftop264__ + __rightop282__;
    int __rightop283__ = 1;
    int __rightop262__ = __leftop263__ * __rightop283__;
    int __offsetinbits260__ = __leftop261__ + __rightop262__;
    // __offsetinbits260__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset284__ = __offsetinbits260__ >> 3;
    int __left258__ = (__left259__ + __offset284__);
    // __left258__ = d.g
    int __element257__ = ((*(int *)(__left258__ + 0))  >> 0) & 0xffffffff;
    __BlockBitmapBlock___hash->add((int)__element257__, (int)__element257__);
  }
}


// build rule13
{
  int __tempvar285__ = 0;
  // __left288__ <-- d.s
  // __left289__ <-- d
  int __left289__ = (int) d;
  // __left289__ = d
  int __left288__ = (__left289__ + 0);
  // __left288__ = d.s
  // __offsetinbits290__ <-- (32 + (32 + (32 + 0)))
  int __leftop291__ = 32;
  int __leftop293__ = 32;
  int __leftop295__ = 32;
  int __rightop296__ = 0;
  int __rightop294__ = __leftop295__ + __rightop296__;
  int __rightop292__ = __leftop293__ + __rightop294__;
  int __offsetinbits290__ = __leftop291__ + __rightop292__;
  // __offsetinbits290__ = (32 + (32 + (32 + 0)))
  int __offset297__ = __offsetinbits290__ >> 3;
  int __shift298__ = __offsetinbits290__ - (__offset297__ << 3);
  int __leftop287__ = ((*(int *)(__left288__ + __offset297__))  >> __shift298__) & 0xffffffff;
  int __rightop299__ = 1;
  int __tempvar286__ = __leftop287__ - __rightop299__;
  for (int __j__ = __tempvar285__; __j__ <= __tempvar286__; __j__++) {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); ) {
      int __ibb__ = (int) __ibb___iterator->next();
      //(cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == true)
      // __left302__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left304__ <-- d
      int __left304__ = (int) d;
      // __left304__ = d
      // __offsetinbits305__ <-- (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __leftop306__ = 0;
      int __leftop310__ = 8;
      // __left312__ <-- d.s
      // __left313__ <-- d
      int __left313__ = (int) d;
      // __left313__ = d
      int __left312__ = (__left313__ + 0);
      // __left312__ = d.s
      // __offsetinbits314__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop315__ = 32;
      int __leftop317__ = 32;
      int __leftop319__ = 32;
      int __leftop321__ = 32;
      int __leftop323__ = 32;
      int __rightop324__ = 0;
      int __rightop322__ = __leftop323__ + __rightop324__;
      int __rightop320__ = __leftop321__ + __rightop322__;
      int __rightop318__ = __leftop319__ + __rightop320__;
      int __rightop316__ = __leftop317__ + __rightop318__;
      int __offsetinbits314__ = __leftop315__ + __rightop316__;
      // __offsetinbits314__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset325__ = __offsetinbits314__ >> 3;
      int __shift326__ = __offsetinbits314__ - (__offset325__ << 3);
      int __rightop311__ = ((*(int *)(__left312__ + __offset325__))  >> __shift326__) & 0xffffffff;
      int __leftop309__ = __leftop310__ * __rightop311__;
      int __rightop327__ = 0;
      int __leftop308__ = __leftop309__ + __rightop327__;
      int __rightop328__ = (int) __ibb__;
      int __rightop307__ = __leftop308__ * __rightop328__;
      int __offsetinbits305__ = __leftop306__ + __rightop307__;
      // __offsetinbits305__ = (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __offset329__ = __offsetinbits305__ >> 3;
      int __expr303__ = (__left304__ + __offset329__);
      int __left302__ = (int) __expr303__;
      // __left302__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits330__ <-- (0 + (1 * j))
      int __leftop331__ = 0;
      int __leftop333__ = 1;
      int __rightop334__ = (int) __j__;
      int __rightop332__ = __leftop333__ * __rightop334__;
      int __offsetinbits330__ = __leftop331__ + __rightop332__;
      // __offsetinbits330__ = (0 + (1 * j))
      int __offset335__ = __offsetinbits330__ >> 3;
      int __shift336__ = __offsetinbits330__ - (__offset335__ << 3);
      int __leftop301__ = ((*(int *)(__left302__ + __offset335__))  >> __shift336__) & 0x1;
      int __rightop337__ = 1;
      int __tempvar300__ = __leftop301__ == __rightop337__;
      if (__tempvar300__) {
        int __leftele338__ = (int) __j__;
        int __rightele339__ = 100;
        __inodestatus___hash->add((int)__leftele338__, (int)__rightele339__);
      }
    }
  }
}


// build rule6
{
  //(d.s.RootDirectoryInode < d.s.NumberofInodes)
  // __left343__ <-- d.s
  // __left344__ <-- d
  int __left344__ = (int) d;
  // __left344__ = d
  int __left343__ = (__left344__ + 0);
  // __left343__ = d.s
  // __offsetinbits345__ <-- (32 + (32 + (32 + (32 + 0))))
  int __leftop346__ = 32;
  int __leftop348__ = 32;
  int __leftop350__ = 32;
  int __leftop352__ = 32;
  int __rightop353__ = 0;
  int __rightop351__ = __leftop352__ + __rightop353__;
  int __rightop349__ = __leftop350__ + __rightop351__;
  int __rightop347__ = __leftop348__ + __rightop349__;
  int __offsetinbits345__ = __leftop346__ + __rightop347__;
  // __offsetinbits345__ = (32 + (32 + (32 + (32 + 0))))
  int __offset354__ = __offsetinbits345__ >> 3;
  int __shift355__ = __offsetinbits345__ - (__offset354__ << 3);
  int __leftop342__ = ((*(int *)(__left343__ + __offset354__))  >> __shift355__) & 0xffffffff;
  // __left357__ <-- d.s
  // __left358__ <-- d
  int __left358__ = (int) d;
  // __left358__ = d
  int __left357__ = (__left358__ + 0);
  // __left357__ = d.s
  // __offsetinbits359__ <-- (32 + (32 + (32 + 0)))
  int __leftop360__ = 32;
  int __leftop362__ = 32;
  int __leftop364__ = 32;
  int __rightop365__ = 0;
  int __rightop363__ = __leftop364__ + __rightop365__;
  int __rightop361__ = __leftop362__ + __rightop363__;
  int __offsetinbits359__ = __leftop360__ + __rightop361__;
  // __offsetinbits359__ = (32 + (32 + (32 + 0)))
  int __offset366__ = __offsetinbits359__ >> 3;
  int __shift367__ = __offsetinbits359__ - (__offset366__ << 3);
  int __rightop356__ = ((*(int *)(__left357__ + __offset366__))  >> __shift367__) & 0xffffffff;
  int __tempvar341__ = __leftop342__ < __rightop356__;
  if (__tempvar341__) {
    // __left369__ <-- d.s
    // __left370__ <-- d
    int __left370__ = (int) d;
    // __left370__ = d
    int __left369__ = (__left370__ + 0);
    // __left369__ = d.s
    // __offsetinbits371__ <-- (32 + (32 + (32 + (32 + 0))))
    int __leftop372__ = 32;
    int __leftop374__ = 32;
    int __leftop376__ = 32;
    int __leftop378__ = 32;
    int __rightop379__ = 0;
    int __rightop377__ = __leftop378__ + __rightop379__;
    int __rightop375__ = __leftop376__ + __rightop377__;
    int __rightop373__ = __leftop374__ + __rightop375__;
    int __offsetinbits371__ = __leftop372__ + __rightop373__;
    // __offsetinbits371__ = (32 + (32 + (32 + (32 + 0))))
    int __offset380__ = __offsetinbits371__ >> 3;
    int __shift381__ = __offsetinbits371__ - (__offset380__ << 3);
    int __element368__ = ((*(int *)(__left369__ + __offset380__))  >> __shift381__) & 0xffffffff;
    __RootDirectoryInode___hash->add((int)__element368__, (int)__element368__);
  }
}


// build rule9
{
  for (SimpleIterator* __di___iterator = __DirectoryInode___hash->iterator(); __di___iterator->hasNext(); ) {
    int __di__ = (int) __di___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar382__ = 0;
      // __left386__ <-- d.s
      // __left387__ <-- d
      int __left387__ = (int) d;
      // __left387__ = d
      int __left386__ = (__left387__ + 0);
      // __left386__ = d.s
      // __offsetinbits388__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop389__ = 32;
      int __leftop391__ = 32;
      int __leftop393__ = 32;
      int __leftop395__ = 32;
      int __leftop397__ = 32;
      int __rightop398__ = 0;
      int __rightop396__ = __leftop397__ + __rightop398__;
      int __rightop394__ = __leftop395__ + __rightop396__;
      int __rightop392__ = __leftop393__ + __rightop394__;
      int __rightop390__ = __leftop391__ + __rightop392__;
      int __offsetinbits388__ = __leftop389__ + __rightop390__;
      // __offsetinbits388__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset399__ = __offsetinbits388__ >> 3;
      int __shift400__ = __offsetinbits388__ - (__offset399__ << 3);
      int __leftop385__ = ((*(int *)(__left386__ + __offset399__))  >> __shift400__) & 0xffffffff;
      int __rightop401__ = 128;
      int __leftop384__ = __leftop385__ / __rightop401__;
      int __rightop402__ = 1;
      int __tempvar383__ = __leftop384__ - __rightop402__;
      for (int __j__ = __tempvar382__; __j__ <= __tempvar383__; __j__++) {
        int __tempvar403__ = 0;
        int __tempvar404__ = 11;
        for (int __k__ = __tempvar403__; __k__ <= __tempvar404__; __k__++) {
          //(cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k] < d.s.NumberofBlocks)
          // __left407__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
          // __left408__ <-- cast(__InodeTable__, d.b[itb])
          // __left410__ <-- d
          int __left410__ = (int) d;
          // __left410__ = d
          // __offsetinbits411__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop412__ = 0;
          int __leftop416__ = 8;
          // __left418__ <-- d.s
          // __left419__ <-- d
          int __left419__ = (int) d;
          // __left419__ = d
          int __left418__ = (__left419__ + 0);
          // __left418__ = d.s
          // __offsetinbits420__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop421__ = 32;
          int __leftop423__ = 32;
          int __leftop425__ = 32;
          int __leftop427__ = 32;
          int __leftop429__ = 32;
          int __rightop430__ = 0;
          int __rightop428__ = __leftop429__ + __rightop430__;
          int __rightop426__ = __leftop427__ + __rightop428__;
          int __rightop424__ = __leftop425__ + __rightop426__;
          int __rightop422__ = __leftop423__ + __rightop424__;
          int __offsetinbits420__ = __leftop421__ + __rightop422__;
          // __offsetinbits420__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset431__ = __offsetinbits420__ >> 3;
          int __shift432__ = __offsetinbits420__ - (__offset431__ << 3);
          int __rightop417__ = ((*(int *)(__left418__ + __offset431__))  >> __shift432__) & 0xffffffff;
          int __leftop415__ = __leftop416__ * __rightop417__;
          int __rightop433__ = 0;
          int __leftop414__ = __leftop415__ + __rightop433__;
          int __rightop434__ = (int) __itb__;
          int __rightop413__ = __leftop414__ * __rightop434__;
          int __offsetinbits411__ = __leftop412__ + __rightop413__;
          // __offsetinbits411__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset435__ = __offsetinbits411__ >> 3;
          int __expr409__ = (__left410__ + __offset435__);
          int __left408__ = (int) __expr409__;
          // __left408__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits436__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
          int __leftop437__ = 0;
          int __leftop440__ = 32;
          int __leftop443__ = 32;
          int __rightop444__ = 12;
          int __leftop442__ = __leftop443__ * __rightop444__;
          int __leftop446__ = 32;
          int __rightop447__ = 0;
          int __rightop445__ = __leftop446__ + __rightop447__;
          int __rightop441__ = __leftop442__ + __rightop445__;
          int __leftop439__ = __leftop440__ + __rightop441__;
          int __rightop448__ = (int) __di__;
          int __rightop438__ = __leftop439__ * __rightop448__;
          int __offsetinbits436__ = __leftop437__ + __rightop438__;
          // __offsetinbits436__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
          int __offset449__ = __offsetinbits436__ >> 3;
          int __left407__ = (__left408__ + __offset449__);
          // __left407__ = cast(__InodeTable__, d.b[itb]).itable[di]
          // __offsetinbits450__ <-- ((32 + 0) + (32 * k))
          int __leftop452__ = 32;
          int __rightop453__ = 0;
          int __leftop451__ = __leftop452__ + __rightop453__;
          int __leftop455__ = 32;
          int __rightop456__ = (int) __k__;
          int __rightop454__ = __leftop455__ * __rightop456__;
          int __offsetinbits450__ = __leftop451__ + __rightop454__;
          // __offsetinbits450__ = ((32 + 0) + (32 * k))
          int __offset457__ = __offsetinbits450__ >> 3;
          int __shift458__ = __offsetinbits450__ - (__offset457__ << 3);
          int __leftop406__ = ((*(int *)(__left407__ + __offset457__))  >> __shift458__) & 0xffffffff;
          // __left460__ <-- d.s
          // __left461__ <-- d
          int __left461__ = (int) d;
          // __left461__ = d
          int __left460__ = (__left461__ + 0);
          // __left460__ = d.s
          // __offsetinbits462__ <-- (32 + (32 + 0))
          int __leftop463__ = 32;
          int __leftop465__ = 32;
          int __rightop466__ = 0;
          int __rightop464__ = __leftop465__ + __rightop466__;
          int __offsetinbits462__ = __leftop463__ + __rightop464__;
          // __offsetinbits462__ = (32 + (32 + 0))
          int __offset467__ = __offsetinbits462__ >> 3;
          int __shift468__ = __offsetinbits462__ - (__offset467__ << 3);
          int __rightop459__ = ((*(int *)(__left460__ + __offset467__))  >> __shift468__) & 0xffffffff;
          int __tempvar405__ = __leftop406__ < __rightop459__;
          if (__tempvar405__) {
            // __left470__ <-- cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __left472__ <-- d
            int __left472__ = (int) d;
            // __left472__ = d
            // __offsetinbits473__ <-- (0 + (((8 * d.s.blocksize) + 0) * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]))
            int __leftop474__ = 0;
            int __leftop478__ = 8;
            // __left480__ <-- d.s
            // __left481__ <-- d
            int __left481__ = (int) d;
            // __left481__ = d
            int __left480__ = (__left481__ + 0);
            // __left480__ = d.s
            // __offsetinbits482__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop483__ = 32;
            int __leftop485__ = 32;
            int __leftop487__ = 32;
            int __leftop489__ = 32;
            int __leftop491__ = 32;
            int __rightop492__ = 0;
            int __rightop490__ = __leftop491__ + __rightop492__;
            int __rightop488__ = __leftop489__ + __rightop490__;
            int __rightop486__ = __leftop487__ + __rightop488__;
            int __rightop484__ = __leftop485__ + __rightop486__;
            int __offsetinbits482__ = __leftop483__ + __rightop484__;
            // __offsetinbits482__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset493__ = __offsetinbits482__ >> 3;
            int __shift494__ = __offsetinbits482__ - (__offset493__ << 3);
            int __rightop479__ = ((*(int *)(__left480__ + __offset493__))  >> __shift494__) & 0xffffffff;
            int __leftop477__ = __leftop478__ * __rightop479__;
            int __rightop495__ = 0;
            int __leftop476__ = __leftop477__ + __rightop495__;
            // __left497__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
            // __left498__ <-- cast(__InodeTable__, d.b[itb])
            // __left500__ <-- d
            int __left500__ = (int) d;
            // __left500__ = d
            // __offsetinbits501__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
            int __leftop502__ = 0;
            int __leftop506__ = 8;
            // __left508__ <-- d.s
            // __left509__ <-- d
            int __left509__ = (int) d;
            // __left509__ = d
            int __left508__ = (__left509__ + 0);
            // __left508__ = d.s
            // __offsetinbits510__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop511__ = 32;
            int __leftop513__ = 32;
            int __leftop515__ = 32;
            int __leftop517__ = 32;
            int __leftop519__ = 32;
            int __rightop520__ = 0;
            int __rightop518__ = __leftop519__ + __rightop520__;
            int __rightop516__ = __leftop517__ + __rightop518__;
            int __rightop514__ = __leftop515__ + __rightop516__;
            int __rightop512__ = __leftop513__ + __rightop514__;
            int __offsetinbits510__ = __leftop511__ + __rightop512__;
            // __offsetinbits510__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset521__ = __offsetinbits510__ >> 3;
            int __shift522__ = __offsetinbits510__ - (__offset521__ << 3);
            int __rightop507__ = ((*(int *)(__left508__ + __offset521__))  >> __shift522__) & 0xffffffff;
            int __leftop505__ = __leftop506__ * __rightop507__;
            int __rightop523__ = 0;
            int __leftop504__ = __leftop505__ + __rightop523__;
            int __rightop524__ = (int) __itb__;
            int __rightop503__ = __leftop504__ * __rightop524__;
            int __offsetinbits501__ = __leftop502__ + __rightop503__;
            // __offsetinbits501__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
            int __offset525__ = __offsetinbits501__ >> 3;
            int __expr499__ = (__left500__ + __offset525__);
            int __left498__ = (int) __expr499__;
            // __left498__ = cast(__InodeTable__, d.b[itb])
            // __offsetinbits526__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
            int __leftop527__ = 0;
            int __leftop530__ = 32;
            int __leftop533__ = 32;
            int __rightop534__ = 12;
            int __leftop532__ = __leftop533__ * __rightop534__;
            int __leftop536__ = 32;
            int __rightop537__ = 0;
            int __rightop535__ = __leftop536__ + __rightop537__;
            int __rightop531__ = __leftop532__ + __rightop535__;
            int __leftop529__ = __leftop530__ + __rightop531__;
            int __rightop538__ = (int) __di__;
            int __rightop528__ = __leftop529__ * __rightop538__;
            int __offsetinbits526__ = __leftop527__ + __rightop528__;
            // __offsetinbits526__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
            int __offset539__ = __offsetinbits526__ >> 3;
            int __left497__ = (__left498__ + __offset539__);
            // __left497__ = cast(__InodeTable__, d.b[itb]).itable[di]
            // __offsetinbits540__ <-- ((32 + 0) + (32 * k))
            int __leftop542__ = 32;
            int __rightop543__ = 0;
            int __leftop541__ = __leftop542__ + __rightop543__;
            int __leftop545__ = 32;
            int __rightop546__ = (int) __k__;
            int __rightop544__ = __leftop545__ * __rightop546__;
            int __offsetinbits540__ = __leftop541__ + __rightop544__;
            // __offsetinbits540__ = ((32 + 0) + (32 * k))
            int __offset547__ = __offsetinbits540__ >> 3;
            int __shift548__ = __offsetinbits540__ - (__offset547__ << 3);
            int __rightop496__ = ((*(int *)(__left497__ + __offset547__))  >> __shift548__) & 0xffffffff;
            int __rightop475__ = __leftop476__ * __rightop496__;
            int __offsetinbits473__ = __leftop474__ + __rightop475__;
            // __offsetinbits473__ = (0 + (((8 * d.s.blocksize) + 0) * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]))
            int __offset549__ = __offsetinbits473__ >> 3;
            int __expr471__ = (__left472__ + __offset549__);
            int __left470__ = (int) __expr471__;
            // __left470__ = cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __offsetinbits550__ <-- (0 + ((32 + ((8 * 124) + 0)) * j))
            int __leftop551__ = 0;
            int __leftop554__ = 32;
            int __leftop557__ = 8;
            int __rightop558__ = 124;
            int __leftop556__ = __leftop557__ * __rightop558__;
            int __rightop559__ = 0;
            int __rightop555__ = __leftop556__ + __rightop559__;
            int __leftop553__ = __leftop554__ + __rightop555__;
            int __rightop560__ = (int) __j__;
            int __rightop552__ = __leftop553__ * __rightop560__;
            int __offsetinbits550__ = __leftop551__ + __rightop552__;
            // __offsetinbits550__ = (0 + ((32 + ((8 * 124) + 0)) * j))
            int __offset561__ = __offsetinbits550__ >> 3;
            int __element469__ = (__left470__ + __offset561__);
            __DirectoryEntry___hash->add((int)__element469__, (int)__element469__);
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
    //(de.inodenumber < d.s.NumberofInodes)
    // __left564__ <-- de
    int __left564__ = (int) __de__;
    // __left564__ = de
    // __offsetinbits565__ <-- ((8 * 124) + 0)
    int __leftop567__ = 8;
    int __rightop568__ = 124;
    int __leftop566__ = __leftop567__ * __rightop568__;
    int __rightop569__ = 0;
    int __offsetinbits565__ = __leftop566__ + __rightop569__;
    // __offsetinbits565__ = ((8 * 124) + 0)
    int __offset570__ = __offsetinbits565__ >> 3;
    int __shift571__ = __offsetinbits565__ - (__offset570__ << 3);
    int __leftop563__ = ((*(int *)(__left564__ + __offset570__))  >> __shift571__) & 0xffffffff;
    // __left573__ <-- d.s
    // __left574__ <-- d
    int __left574__ = (int) d;
    // __left574__ = d
    int __left573__ = (__left574__ + 0);
    // __left573__ = d.s
    // __offsetinbits575__ <-- (32 + (32 + (32 + 0)))
    int __leftop576__ = 32;
    int __leftop578__ = 32;
    int __leftop580__ = 32;
    int __rightop581__ = 0;
    int __rightop579__ = __leftop580__ + __rightop581__;
    int __rightop577__ = __leftop578__ + __rightop579__;
    int __offsetinbits575__ = __leftop576__ + __rightop577__;
    // __offsetinbits575__ = (32 + (32 + (32 + 0)))
    int __offset582__ = __offsetinbits575__ >> 3;
    int __shift583__ = __offsetinbits575__ - (__offset582__ << 3);
    int __rightop572__ = ((*(int *)(__left573__ + __offset582__))  >> __shift583__) & 0xffffffff;
    int __tempvar562__ = __leftop563__ < __rightop572__;
    if (__tempvar562__) {
      int __leftele584__ = (int) __de__;
      // __left586__ <-- de
      int __left586__ = (int) __de__;
      // __left586__ = de
      // __offsetinbits587__ <-- ((8 * 124) + 0)
      int __leftop589__ = 8;
      int __rightop590__ = 124;
      int __leftop588__ = __leftop589__ * __rightop590__;
      int __rightop591__ = 0;
      int __offsetinbits587__ = __leftop588__ + __rightop591__;
      // __offsetinbits587__ = ((8 * 124) + 0)
      int __offset592__ = __offsetinbits587__ >> 3;
      int __shift593__ = __offsetinbits587__ - (__offset592__ << 3);
      int __rightele585__ = ((*(int *)(__left586__ + __offset592__))  >> __shift593__) & 0xffffffff;
      __inodeof___hashinv->add((int)__rightele585__, (int)__leftele584__);
    }
  }
}


// build rule14
{
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); ) {
    int __de__ = (int) __de___iterator->next();
    //((de.inodenumber < d.s.NumberofInodes) && ((de.inodenumber == 0)))
    // __left598__ <-- de
    int __left598__ = (int) __de__;
    // __left598__ = de
    // __offsetinbits599__ <-- ((8 * 124) + 0)
    int __leftop601__ = 8;
    int __rightop602__ = 124;
    int __leftop600__ = __leftop601__ * __rightop602__;
    int __rightop603__ = 0;
    int __offsetinbits599__ = __leftop600__ + __rightop603__;
    // __offsetinbits599__ = ((8 * 124) + 0)
    int __offset604__ = __offsetinbits599__ >> 3;
    int __shift605__ = __offsetinbits599__ - (__offset604__ << 3);
    int __leftop597__ = ((*(int *)(__left598__ + __offset604__))  >> __shift605__) & 0xffffffff;
    // __left607__ <-- d.s
    // __left608__ <-- d
    int __left608__ = (int) d;
    // __left608__ = d
    int __left607__ = (__left608__ + 0);
    // __left607__ = d.s
    // __offsetinbits609__ <-- (32 + (32 + (32 + 0)))
    int __leftop610__ = 32;
    int __leftop612__ = 32;
    int __leftop614__ = 32;
    int __rightop615__ = 0;
    int __rightop613__ = __leftop614__ + __rightop615__;
    int __rightop611__ = __leftop612__ + __rightop613__;
    int __offsetinbits609__ = __leftop610__ + __rightop611__;
    // __offsetinbits609__ = (32 + (32 + (32 + 0)))
    int __offset616__ = __offsetinbits609__ >> 3;
    int __shift617__ = __offsetinbits609__ - (__offset616__ << 3);
    int __rightop606__ = ((*(int *)(__left607__ + __offset616__))  >> __shift617__) & 0xffffffff;
    int __leftop596__ = __leftop597__ < __rightop606__;
    // __left621__ <-- de
    int __left621__ = (int) __de__;
    // __left621__ = de
    // __offsetinbits622__ <-- ((8 * 124) + 0)
    int __leftop624__ = 8;
    int __rightop625__ = 124;
    int __leftop623__ = __leftop624__ * __rightop625__;
    int __rightop626__ = 0;
    int __offsetinbits622__ = __leftop623__ + __rightop626__;
    // __offsetinbits622__ = ((8 * 124) + 0)
    int __offset627__ = __offsetinbits622__ >> 3;
    int __shift628__ = __offsetinbits622__ - (__offset627__ << 3);
    int __leftop620__ = ((*(int *)(__left621__ + __offset627__))  >> __shift628__) & 0xffffffff;
    int __rightop629__ = 0;
    int __leftop619__ = __leftop620__ == __rightop629__;
    int __rightop618__ = !__leftop619__;
    int __tempvar595__ = __leftop596__ && __rightop618__;
    if (__tempvar595__) {
      // __left631__ <-- de
      int __left631__ = (int) __de__;
      // __left631__ = de
      // __offsetinbits632__ <-- ((8 * 124) + 0)
      int __leftop634__ = 8;
      int __rightop635__ = 124;
      int __leftop633__ = __leftop634__ * __rightop635__;
      int __rightop636__ = 0;
      int __offsetinbits632__ = __leftop633__ + __rightop636__;
      // __offsetinbits632__ = ((8 * 124) + 0)
      int __offset637__ = __offsetinbits632__ >> 3;
      int __shift638__ = __offsetinbits632__ - (__offset637__ << 3);
      int __element630__ = ((*(int *)(__left631__ + __offset637__))  >> __shift638__) & 0xffffffff;
      __FileInode___hash->add((int)__element630__, (int)__element630__);
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
      int __tempvar639__ = 1;
      if (__tempvar639__) {
        int __leftele640__ = (int) __j__;
        // __left642__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left643__ <-- cast(__InodeTable__, d.b[itb])
        // __left645__ <-- d
        int __left645__ = (int) d;
        // __left645__ = d
        // __offsetinbits646__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop647__ = 0;
        int __leftop651__ = 8;
        // __left653__ <-- d.s
        // __left654__ <-- d
        int __left654__ = (int) d;
        // __left654__ = d
        int __left653__ = (__left654__ + 0);
        // __left653__ = d.s
        // __offsetinbits655__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop656__ = 32;
        int __leftop658__ = 32;
        int __leftop660__ = 32;
        int __leftop662__ = 32;
        int __leftop664__ = 32;
        int __rightop665__ = 0;
        int __rightop663__ = __leftop664__ + __rightop665__;
        int __rightop661__ = __leftop662__ + __rightop663__;
        int __rightop659__ = __leftop660__ + __rightop661__;
        int __rightop657__ = __leftop658__ + __rightop659__;
        int __offsetinbits655__ = __leftop656__ + __rightop657__;
        // __offsetinbits655__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset666__ = __offsetinbits655__ >> 3;
        int __shift667__ = __offsetinbits655__ - (__offset666__ << 3);
        int __rightop652__ = ((*(int *)(__left653__ + __offset666__))  >> __shift667__) & 0xffffffff;
        int __leftop650__ = __leftop651__ * __rightop652__;
        int __rightop668__ = 0;
        int __leftop649__ = __leftop650__ + __rightop668__;
        int __rightop669__ = (int) __itb__;
        int __rightop648__ = __leftop649__ * __rightop669__;
        int __offsetinbits646__ = __leftop647__ + __rightop648__;
        // __offsetinbits646__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset670__ = __offsetinbits646__ >> 3;
        int __expr644__ = (__left645__ + __offset670__);
        int __left643__ = (int) __expr644__;
        // __left643__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits671__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __leftop672__ = 0;
        int __leftop675__ = 32;
        int __leftop678__ = 32;
        int __rightop679__ = 12;
        int __leftop677__ = __leftop678__ * __rightop679__;
        int __leftop681__ = 32;
        int __rightop682__ = 0;
        int __rightop680__ = __leftop681__ + __rightop682__;
        int __rightop676__ = __leftop677__ + __rightop680__;
        int __leftop674__ = __leftop675__ + __rightop676__;
        int __rightop683__ = (int) __j__;
        int __rightop673__ = __leftop674__ * __rightop683__;
        int __offsetinbits671__ = __leftop672__ + __rightop673__;
        // __offsetinbits671__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __offset684__ = __offsetinbits671__ >> 3;
        int __left642__ = (__left643__ + __offset684__);
        // __left642__ = cast(__InodeTable__, d.b[itb]).itable[j]
        // __offsetinbits685__ <-- ((32 * 12) + (32 + 0))
        int __leftop687__ = 32;
        int __rightop688__ = 12;
        int __leftop686__ = __leftop687__ * __rightop688__;
        int __leftop690__ = 32;
        int __rightop691__ = 0;
        int __rightop689__ = __leftop690__ + __rightop691__;
        int __offsetinbits685__ = __leftop686__ + __rightop689__;
        // __offsetinbits685__ = ((32 * 12) + (32 + 0))
        int __offset692__ = __offsetinbits685__ >> 3;
        int __shift693__ = __offsetinbits685__ - (__offset692__ << 3);
        int __rightele641__ = ((*(int *)(__left642__ + __offset692__))  >> __shift693__) & 0xffffffff;
        __referencecount___hash->add((int)__leftele640__, (int)__rightele641__);
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
      int __tempvar695__ = 0;
      int __tempvar696__ = 11;
      for (int __j__ = __tempvar695__; __j__ <= __tempvar696__; __j__++) {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] < d.s.NumberofBlocks) && ((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0)))
        // __left700__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left701__ <-- cast(__InodeTable__, d.b[itb])
        // __left703__ <-- d
        int __left703__ = (int) d;
        // __left703__ = d
        // __offsetinbits704__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop705__ = 0;
        int __leftop709__ = 8;
        // __left711__ <-- d.s
        // __left712__ <-- d
        int __left712__ = (int) d;
        // __left712__ = d
        int __left711__ = (__left712__ + 0);
        // __left711__ = d.s
        // __offsetinbits713__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop714__ = 32;
        int __leftop716__ = 32;
        int __leftop718__ = 32;
        int __leftop720__ = 32;
        int __leftop722__ = 32;
        int __rightop723__ = 0;
        int __rightop721__ = __leftop722__ + __rightop723__;
        int __rightop719__ = __leftop720__ + __rightop721__;
        int __rightop717__ = __leftop718__ + __rightop719__;
        int __rightop715__ = __leftop716__ + __rightop717__;
        int __offsetinbits713__ = __leftop714__ + __rightop715__;
        // __offsetinbits713__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset724__ = __offsetinbits713__ >> 3;
        int __shift725__ = __offsetinbits713__ - (__offset724__ << 3);
        int __rightop710__ = ((*(int *)(__left711__ + __offset724__))  >> __shift725__) & 0xffffffff;
        int __leftop708__ = __leftop709__ * __rightop710__;
        int __rightop726__ = 0;
        int __leftop707__ = __leftop708__ + __rightop726__;
        int __rightop727__ = (int) __itb__;
        int __rightop706__ = __leftop707__ * __rightop727__;
        int __offsetinbits704__ = __leftop705__ + __rightop706__;
        // __offsetinbits704__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset728__ = __offsetinbits704__ >> 3;
        int __expr702__ = (__left703__ + __offset728__);
        int __left701__ = (int) __expr702__;
        // __left701__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits729__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop730__ = 0;
        int __leftop733__ = 32;
        int __leftop736__ = 32;
        int __rightop737__ = 12;
        int __leftop735__ = __leftop736__ * __rightop737__;
        int __leftop739__ = 32;
        int __rightop740__ = 0;
        int __rightop738__ = __leftop739__ + __rightop740__;
        int __rightop734__ = __leftop735__ + __rightop738__;
        int __leftop732__ = __leftop733__ + __rightop734__;
        int __rightop741__ = (int) __i__;
        int __rightop731__ = __leftop732__ * __rightop741__;
        int __offsetinbits729__ = __leftop730__ + __rightop731__;
        // __offsetinbits729__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset742__ = __offsetinbits729__ >> 3;
        int __left700__ = (__left701__ + __offset742__);
        // __left700__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits743__ <-- ((32 + 0) + (32 * j))
        int __leftop745__ = 32;
        int __rightop746__ = 0;
        int __leftop744__ = __leftop745__ + __rightop746__;
        int __leftop748__ = 32;
        int __rightop749__ = (int) __j__;
        int __rightop747__ = __leftop748__ * __rightop749__;
        int __offsetinbits743__ = __leftop744__ + __rightop747__;
        // __offsetinbits743__ = ((32 + 0) + (32 * j))
        int __offset750__ = __offsetinbits743__ >> 3;
        int __shift751__ = __offsetinbits743__ - (__offset750__ << 3);
        int __leftop699__ = ((*(int *)(__left700__ + __offset750__))  >> __shift751__) & 0xffffffff;
        // __left753__ <-- d.s
        // __left754__ <-- d
        int __left754__ = (int) d;
        // __left754__ = d
        int __left753__ = (__left754__ + 0);
        // __left753__ = d.s
        // __offsetinbits755__ <-- (32 + (32 + 0))
        int __leftop756__ = 32;
        int __leftop758__ = 32;
        int __rightop759__ = 0;
        int __rightop757__ = __leftop758__ + __rightop759__;
        int __offsetinbits755__ = __leftop756__ + __rightop757__;
        // __offsetinbits755__ = (32 + (32 + 0))
        int __offset760__ = __offsetinbits755__ >> 3;
        int __shift761__ = __offsetinbits755__ - (__offset760__ << 3);
        int __rightop752__ = ((*(int *)(__left753__ + __offset760__))  >> __shift761__) & 0xffffffff;
        int __leftop698__ = __leftop699__ < __rightop752__;
        // __left765__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left766__ <-- cast(__InodeTable__, d.b[itb])
        // __left768__ <-- d
        int __left768__ = (int) d;
        // __left768__ = d
        // __offsetinbits769__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop770__ = 0;
        int __leftop774__ = 8;
        // __left776__ <-- d.s
        // __left777__ <-- d
        int __left777__ = (int) d;
        // __left777__ = d
        int __left776__ = (__left777__ + 0);
        // __left776__ = d.s
        // __offsetinbits778__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop779__ = 32;
        int __leftop781__ = 32;
        int __leftop783__ = 32;
        int __leftop785__ = 32;
        int __leftop787__ = 32;
        int __rightop788__ = 0;
        int __rightop786__ = __leftop787__ + __rightop788__;
        int __rightop784__ = __leftop785__ + __rightop786__;
        int __rightop782__ = __leftop783__ + __rightop784__;
        int __rightop780__ = __leftop781__ + __rightop782__;
        int __offsetinbits778__ = __leftop779__ + __rightop780__;
        // __offsetinbits778__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset789__ = __offsetinbits778__ >> 3;
        int __shift790__ = __offsetinbits778__ - (__offset789__ << 3);
        int __rightop775__ = ((*(int *)(__left776__ + __offset789__))  >> __shift790__) & 0xffffffff;
        int __leftop773__ = __leftop774__ * __rightop775__;
        int __rightop791__ = 0;
        int __leftop772__ = __leftop773__ + __rightop791__;
        int __rightop792__ = (int) __itb__;
        int __rightop771__ = __leftop772__ * __rightop792__;
        int __offsetinbits769__ = __leftop770__ + __rightop771__;
        // __offsetinbits769__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset793__ = __offsetinbits769__ >> 3;
        int __expr767__ = (__left768__ + __offset793__);
        int __left766__ = (int) __expr767__;
        // __left766__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits794__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop795__ = 0;
        int __leftop798__ = 32;
        int __leftop801__ = 32;
        int __rightop802__ = 12;
        int __leftop800__ = __leftop801__ * __rightop802__;
        int __leftop804__ = 32;
        int __rightop805__ = 0;
        int __rightop803__ = __leftop804__ + __rightop805__;
        int __rightop799__ = __leftop800__ + __rightop803__;
        int __leftop797__ = __leftop798__ + __rightop799__;
        int __rightop806__ = (int) __i__;
        int __rightop796__ = __leftop797__ * __rightop806__;
        int __offsetinbits794__ = __leftop795__ + __rightop796__;
        // __offsetinbits794__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset807__ = __offsetinbits794__ >> 3;
        int __left765__ = (__left766__ + __offset807__);
        // __left765__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits808__ <-- ((32 + 0) + (32 * j))
        int __leftop810__ = 32;
        int __rightop811__ = 0;
        int __leftop809__ = __leftop810__ + __rightop811__;
        int __leftop813__ = 32;
        int __rightop814__ = (int) __j__;
        int __rightop812__ = __leftop813__ * __rightop814__;
        int __offsetinbits808__ = __leftop809__ + __rightop812__;
        // __offsetinbits808__ = ((32 + 0) + (32 * j))
        int __offset815__ = __offsetinbits808__ >> 3;
        int __shift816__ = __offsetinbits808__ - (__offset815__ << 3);
        int __leftop764__ = ((*(int *)(__left765__ + __offset815__))  >> __shift816__) & 0xffffffff;
        int __rightop817__ = 0;
        int __leftop763__ = __leftop764__ == __rightop817__;
        int __rightop762__ = !__leftop763__;
        int __tempvar697__ = __leftop698__ && __rightop762__;
        if (__tempvar697__) {
          // __left819__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left820__ <-- cast(__InodeTable__, d.b[itb])
          // __left822__ <-- d
          int __left822__ = (int) d;
          // __left822__ = d
          // __offsetinbits823__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop824__ = 0;
          int __leftop828__ = 8;
          // __left830__ <-- d.s
          // __left831__ <-- d
          int __left831__ = (int) d;
          // __left831__ = d
          int __left830__ = (__left831__ + 0);
          // __left830__ = d.s
          // __offsetinbits832__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop833__ = 32;
          int __leftop835__ = 32;
          int __leftop837__ = 32;
          int __leftop839__ = 32;
          int __leftop841__ = 32;
          int __rightop842__ = 0;
          int __rightop840__ = __leftop841__ + __rightop842__;
          int __rightop838__ = __leftop839__ + __rightop840__;
          int __rightop836__ = __leftop837__ + __rightop838__;
          int __rightop834__ = __leftop835__ + __rightop836__;
          int __offsetinbits832__ = __leftop833__ + __rightop834__;
          // __offsetinbits832__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset843__ = __offsetinbits832__ >> 3;
          int __shift844__ = __offsetinbits832__ - (__offset843__ << 3);
          int __rightop829__ = ((*(int *)(__left830__ + __offset843__))  >> __shift844__) & 0xffffffff;
          int __leftop827__ = __leftop828__ * __rightop829__;
          int __rightop845__ = 0;
          int __leftop826__ = __leftop827__ + __rightop845__;
          int __rightop846__ = (int) __itb__;
          int __rightop825__ = __leftop826__ * __rightop846__;
          int __offsetinbits823__ = __leftop824__ + __rightop825__;
          // __offsetinbits823__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset847__ = __offsetinbits823__ >> 3;
          int __expr821__ = (__left822__ + __offset847__);
          int __left820__ = (int) __expr821__;
          // __left820__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits848__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop849__ = 0;
          int __leftop852__ = 32;
          int __leftop855__ = 32;
          int __rightop856__ = 12;
          int __leftop854__ = __leftop855__ * __rightop856__;
          int __leftop858__ = 32;
          int __rightop859__ = 0;
          int __rightop857__ = __leftop858__ + __rightop859__;
          int __rightop853__ = __leftop854__ + __rightop857__;
          int __leftop851__ = __leftop852__ + __rightop853__;
          int __rightop860__ = (int) __i__;
          int __rightop850__ = __leftop851__ * __rightop860__;
          int __offsetinbits848__ = __leftop849__ + __rightop850__;
          // __offsetinbits848__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset861__ = __offsetinbits848__ >> 3;
          int __left819__ = (__left820__ + __offset861__);
          // __left819__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits862__ <-- ((32 + 0) + (32 * j))
          int __leftop864__ = 32;
          int __rightop865__ = 0;
          int __leftop863__ = __leftop864__ + __rightop865__;
          int __leftop867__ = 32;
          int __rightop868__ = (int) __j__;
          int __rightop866__ = __leftop867__ * __rightop868__;
          int __offsetinbits862__ = __leftop863__ + __rightop866__;
          // __offsetinbits862__ = ((32 + 0) + (32 * j))
          int __offset869__ = __offsetinbits862__ >> 3;
          int __shift870__ = __offsetinbits862__ - (__offset869__ << 3);
          int __element818__ = ((*(int *)(__left819__ + __offset869__))  >> __shift870__) & 0xffffffff;
          __FileBlock___hash->add((int)__element818__, (int)__element818__);
        }
      }
    }
  }
}


// build rule8
{
  int __tempvar871__ = 0;
  // __left874__ <-- d.s
  // __left875__ <-- d
  int __left875__ = (int) d;
  // __left875__ = d
  int __left874__ = (__left875__ + 0);
  // __left874__ = d.s
  // __offsetinbits876__ <-- (32 + (32 + 0))
  int __leftop877__ = 32;
  int __leftop879__ = 32;
  int __rightop880__ = 0;
  int __rightop878__ = __leftop879__ + __rightop880__;
  int __offsetinbits876__ = __leftop877__ + __rightop878__;
  // __offsetinbits876__ = (32 + (32 + 0))
  int __offset881__ = __offsetinbits876__ >> 3;
  int __shift882__ = __offsetinbits876__ - (__offset881__ << 3);
  int __leftop873__ = ((*(int *)(__left874__ + __offset881__))  >> __shift882__) & 0xffffffff;
  int __rightop883__ = 1;
  int __tempvar872__ = __leftop873__ - __rightop883__;
  for (int __j__ = __tempvar871__; __j__ <= __tempvar872__; __j__++) {
    //(j in? __UsedBlock__)
    int __element886__ = (int) __j__;
    int __leftop885__ = __UsedBlock___hash->contains(__element886__);
    int __tempvar884__ = !__leftop885__;
    if (__tempvar884__) {
      int __element887__ = (int) __j__;
      __FreeBlock___hash->add((int)__element887__, (int)__element887__);
    }
  }
}


// build rule10
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar888__ = 0;
      int __tempvar889__ = 11;
      for (int __j__ = __tempvar888__; __j__ <= __tempvar889__; __j__++) {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0))
        // __left893__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left894__ <-- cast(__InodeTable__, d.b[itb])
        // __left896__ <-- d
        int __left896__ = (int) d;
        // __left896__ = d
        // __offsetinbits897__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop898__ = 0;
        int __leftop902__ = 8;
        // __left904__ <-- d.s
        // __left905__ <-- d
        int __left905__ = (int) d;
        // __left905__ = d
        int __left904__ = (__left905__ + 0);
        // __left904__ = d.s
        // __offsetinbits906__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop907__ = 32;
        int __leftop909__ = 32;
        int __leftop911__ = 32;
        int __leftop913__ = 32;
        int __leftop915__ = 32;
        int __rightop916__ = 0;
        int __rightop914__ = __leftop915__ + __rightop916__;
        int __rightop912__ = __leftop913__ + __rightop914__;
        int __rightop910__ = __leftop911__ + __rightop912__;
        int __rightop908__ = __leftop909__ + __rightop910__;
        int __offsetinbits906__ = __leftop907__ + __rightop908__;
        // __offsetinbits906__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset917__ = __offsetinbits906__ >> 3;
        int __shift918__ = __offsetinbits906__ - (__offset917__ << 3);
        int __rightop903__ = ((*(int *)(__left904__ + __offset917__))  >> __shift918__) & 0xffffffff;
        int __leftop901__ = __leftop902__ * __rightop903__;
        int __rightop919__ = 0;
        int __leftop900__ = __leftop901__ + __rightop919__;
        int __rightop920__ = (int) __itb__;
        int __rightop899__ = __leftop900__ * __rightop920__;
        int __offsetinbits897__ = __leftop898__ + __rightop899__;
        // __offsetinbits897__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset921__ = __offsetinbits897__ >> 3;
        int __expr895__ = (__left896__ + __offset921__);
        int __left894__ = (int) __expr895__;
        // __left894__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits922__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop923__ = 0;
        int __leftop926__ = 32;
        int __leftop929__ = 32;
        int __rightop930__ = 12;
        int __leftop928__ = __leftop929__ * __rightop930__;
        int __leftop932__ = 32;
        int __rightop933__ = 0;
        int __rightop931__ = __leftop932__ + __rightop933__;
        int __rightop927__ = __leftop928__ + __rightop931__;
        int __leftop925__ = __leftop926__ + __rightop927__;
        int __rightop934__ = (int) __i__;
        int __rightop924__ = __leftop925__ * __rightop934__;
        int __offsetinbits922__ = __leftop923__ + __rightop924__;
        // __offsetinbits922__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset935__ = __offsetinbits922__ >> 3;
        int __left893__ = (__left894__ + __offset935__);
        // __left893__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits936__ <-- ((32 + 0) + (32 * j))
        int __leftop938__ = 32;
        int __rightop939__ = 0;
        int __leftop937__ = __leftop938__ + __rightop939__;
        int __leftop941__ = 32;
        int __rightop942__ = (int) __j__;
        int __rightop940__ = __leftop941__ * __rightop942__;
        int __offsetinbits936__ = __leftop937__ + __rightop940__;
        // __offsetinbits936__ = ((32 + 0) + (32 * j))
        int __offset943__ = __offsetinbits936__ >> 3;
        int __shift944__ = __offsetinbits936__ - (__offset943__ << 3);
        int __leftop892__ = ((*(int *)(__left893__ + __offset943__))  >> __shift944__) & 0xffffffff;
        int __rightop945__ = 0;
        int __leftop891__ = __leftop892__ == __rightop945__;
        int __tempvar890__ = !__leftop891__;
        if (__tempvar890__) {
          int __leftele946__ = (int) __i__;
          // __left948__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left949__ <-- cast(__InodeTable__, d.b[itb])
          // __left951__ <-- d
          int __left951__ = (int) d;
          // __left951__ = d
          // __offsetinbits952__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop953__ = 0;
          int __leftop957__ = 8;
          // __left959__ <-- d.s
          // __left960__ <-- d
          int __left960__ = (int) d;
          // __left960__ = d
          int __left959__ = (__left960__ + 0);
          // __left959__ = d.s
          // __offsetinbits961__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop962__ = 32;
          int __leftop964__ = 32;
          int __leftop966__ = 32;
          int __leftop968__ = 32;
          int __leftop970__ = 32;
          int __rightop971__ = 0;
          int __rightop969__ = __leftop970__ + __rightop971__;
          int __rightop967__ = __leftop968__ + __rightop969__;
          int __rightop965__ = __leftop966__ + __rightop967__;
          int __rightop963__ = __leftop964__ + __rightop965__;
          int __offsetinbits961__ = __leftop962__ + __rightop963__;
          // __offsetinbits961__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset972__ = __offsetinbits961__ >> 3;
          int __shift973__ = __offsetinbits961__ - (__offset972__ << 3);
          int __rightop958__ = ((*(int *)(__left959__ + __offset972__))  >> __shift973__) & 0xffffffff;
          int __leftop956__ = __leftop957__ * __rightop958__;
          int __rightop974__ = 0;
          int __leftop955__ = __leftop956__ + __rightop974__;
          int __rightop975__ = (int) __itb__;
          int __rightop954__ = __leftop955__ * __rightop975__;
          int __offsetinbits952__ = __leftop953__ + __rightop954__;
          // __offsetinbits952__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset976__ = __offsetinbits952__ >> 3;
          int __expr950__ = (__left951__ + __offset976__);
          int __left949__ = (int) __expr950__;
          // __left949__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits977__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop978__ = 0;
          int __leftop981__ = 32;
          int __leftop984__ = 32;
          int __rightop985__ = 12;
          int __leftop983__ = __leftop984__ * __rightop985__;
          int __leftop987__ = 32;
          int __rightop988__ = 0;
          int __rightop986__ = __leftop987__ + __rightop988__;
          int __rightop982__ = __leftop983__ + __rightop986__;
          int __leftop980__ = __leftop981__ + __rightop982__;
          int __rightop989__ = (int) __i__;
          int __rightop979__ = __leftop980__ * __rightop989__;
          int __offsetinbits977__ = __leftop978__ + __rightop979__;
          // __offsetinbits977__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset990__ = __offsetinbits977__ >> 3;
          int __left948__ = (__left949__ + __offset990__);
          // __left948__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits991__ <-- ((32 + 0) + (32 * j))
          int __leftop993__ = 32;
          int __rightop994__ = 0;
          int __leftop992__ = __leftop993__ + __rightop994__;
          int __leftop996__ = 32;
          int __rightop997__ = (int) __j__;
          int __rightop995__ = __leftop996__ * __rightop997__;
          int __offsetinbits991__ = __leftop992__ + __rightop995__;
          // __offsetinbits991__ = ((32 + 0) + (32 * j))
          int __offset998__ = __offsetinbits991__ >> 3;
          int __shift999__ = __offsetinbits991__ - (__offset998__ << 3);
          int __rightele947__ = ((*(int *)(__left948__ + __offset998__))  >> __shift999__) & 0xffffffff;
          __contents___hash->add((int)__leftele946__, (int)__rightele947__);
          __contents___hashinv->add((int)__rightele947__, (int)__leftele946__);
        }
      }
    }
  }
}


// build rule7
{
  int __tempvar1001__ = 0;
  // __left1004__ <-- d.s
  // __left1005__ <-- d
  int __left1005__ = (int) d;
  // __left1005__ = d
  int __left1004__ = (__left1005__ + 0);
  // __left1004__ = d.s
  // __offsetinbits1006__ <-- (32 + (32 + (32 + 0)))
  int __leftop1007__ = 32;
  int __leftop1009__ = 32;
  int __leftop1011__ = 32;
  int __rightop1012__ = 0;
  int __rightop1010__ = __leftop1011__ + __rightop1012__;
  int __rightop1008__ = __leftop1009__ + __rightop1010__;
  int __offsetinbits1006__ = __leftop1007__ + __rightop1008__;
  // __offsetinbits1006__ = (32 + (32 + (32 + 0)))
  int __offset1013__ = __offsetinbits1006__ >> 3;
  int __shift1014__ = __offsetinbits1006__ - (__offset1013__ << 3);
  int __leftop1003__ = ((*(int *)(__left1004__ + __offset1013__))  >> __shift1014__) & 0xffffffff;
  int __rightop1015__ = 1;
  int __tempvar1002__ = __leftop1003__ - __rightop1015__;
  for (int __j__ = __tempvar1001__; __j__ <= __tempvar1002__; __j__++) {
    //(j in? __UsedInode__)
    int __element1018__ = (int) __j__;
    int __leftop1017__ = __UsedInode___hash->contains(__element1018__);
    int __tempvar1016__ = !__leftop1017__;
    if (__tempvar1016__) {
      int __element1019__ = (int) __j__;
      __FreeInode___hash->add((int)__element1019__, (int)__element1019__);
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
      int __tempvar1020__ = 1;
      if (__tempvar1020__) {
        int __leftele1021__ = (int) __j__;
        // __left1023__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left1024__ <-- cast(__InodeTable__, d.b[itb])
        // __left1026__ <-- d
        int __left1026__ = (int) d;
        // __left1026__ = d
        // __offsetinbits1027__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1028__ = 0;
        int __leftop1032__ = 8;
        // __left1034__ <-- d.s
        // __left1035__ <-- d
        int __left1035__ = (int) d;
        // __left1035__ = d
        int __left1034__ = (__left1035__ + 0);
        // __left1034__ = d.s
        // __offsetinbits1036__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1037__ = 32;
        int __leftop1039__ = 32;
        int __leftop1041__ = 32;
        int __leftop1043__ = 32;
        int __leftop1045__ = 32;
        int __rightop1046__ = 0;
        int __rightop1044__ = __leftop1045__ + __rightop1046__;
        int __rightop1042__ = __leftop1043__ + __rightop1044__;
        int __rightop1040__ = __leftop1041__ + __rightop1042__;
        int __rightop1038__ = __leftop1039__ + __rightop1040__;
        int __offsetinbits1036__ = __leftop1037__ + __rightop1038__;
        // __offsetinbits1036__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1047__ = __offsetinbits1036__ >> 3;
        int __shift1048__ = __offsetinbits1036__ - (__offset1047__ << 3);
        int __rightop1033__ = ((*(int *)(__left1034__ + __offset1047__))  >> __shift1048__) & 0xffffffff;
        int __leftop1031__ = __leftop1032__ * __rightop1033__;
        int __rightop1049__ = 0;
        int __leftop1030__ = __leftop1031__ + __rightop1049__;
        int __rightop1050__ = (int) __itb__;
        int __rightop1029__ = __leftop1030__ * __rightop1050__;
        int __offsetinbits1027__ = __leftop1028__ + __rightop1029__;
        // __offsetinbits1027__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1051__ = __offsetinbits1027__ >> 3;
        int __expr1025__ = (__left1026__ + __offset1051__);
        int __left1024__ = (int) __expr1025__;
        // __left1024__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1052__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __leftop1053__ = 0;
        int __leftop1056__ = 32;
        int __leftop1059__ = 32;
        int __rightop1060__ = 12;
        int __leftop1058__ = __leftop1059__ * __rightop1060__;
        int __leftop1062__ = 32;
        int __rightop1063__ = 0;
        int __rightop1061__ = __leftop1062__ + __rightop1063__;
        int __rightop1057__ = __leftop1058__ + __rightop1061__;
        int __leftop1055__ = __leftop1056__ + __rightop1057__;
        int __rightop1064__ = (int) __j__;
        int __rightop1054__ = __leftop1055__ * __rightop1064__;
        int __offsetinbits1052__ = __leftop1053__ + __rightop1054__;
        // __offsetinbits1052__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __offset1065__ = __offsetinbits1052__ >> 3;
        int __left1023__ = (__left1024__ + __offset1065__);
        // __left1023__ = cast(__InodeTable__, d.b[itb]).itable[j]
        int __rightele1022__ = ((*(int *)(__left1023__ + 0))  >> 0) & 0xffffffff;
        __filesize___hash->add((int)__leftele1021__, (int)__rightele1022__);
      }
    }
  }
}


// build rule18
{
  int __tempvar1067__ = 0;
  // __left1070__ <-- d.s
  // __left1071__ <-- d
  int __left1071__ = (int) d;
  // __left1071__ = d
  int __left1070__ = (__left1071__ + 0);
  // __left1070__ = d.s
  // __offsetinbits1072__ <-- (32 + (32 + 0))
  int __leftop1073__ = 32;
  int __leftop1075__ = 32;
  int __rightop1076__ = 0;
  int __rightop1074__ = __leftop1075__ + __rightop1076__;
  int __offsetinbits1072__ = __leftop1073__ + __rightop1074__;
  // __offsetinbits1072__ = (32 + (32 + 0))
  int __offset1077__ = __offsetinbits1072__ >> 3;
  int __shift1078__ = __offsetinbits1072__ - (__offset1077__ << 3);
  int __leftop1069__ = ((*(int *)(__left1070__ + __offset1077__))  >> __shift1078__) & 0xffffffff;
  int __rightop1079__ = 1;
  int __tempvar1068__ = __leftop1069__ - __rightop1079__;
  for (int __j__ = __tempvar1067__; __j__ <= __tempvar1068__; __j__++) {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); ) {
      int __bbb__ = (int) __bbb___iterator->next();
      //(cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == false)
      // __left1082__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left1084__ <-- d
      int __left1084__ = (int) d;
      // __left1084__ = d
      // __offsetinbits1085__ <-- (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __leftop1086__ = 0;
      int __leftop1090__ = 8;
      // __left1092__ <-- d.s
      // __left1093__ <-- d
      int __left1093__ = (int) d;
      // __left1093__ = d
      int __left1092__ = (__left1093__ + 0);
      // __left1092__ = d.s
      // __offsetinbits1094__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop1095__ = 32;
      int __leftop1097__ = 32;
      int __leftop1099__ = 32;
      int __leftop1101__ = 32;
      int __leftop1103__ = 32;
      int __rightop1104__ = 0;
      int __rightop1102__ = __leftop1103__ + __rightop1104__;
      int __rightop1100__ = __leftop1101__ + __rightop1102__;
      int __rightop1098__ = __leftop1099__ + __rightop1100__;
      int __rightop1096__ = __leftop1097__ + __rightop1098__;
      int __offsetinbits1094__ = __leftop1095__ + __rightop1096__;
      // __offsetinbits1094__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset1105__ = __offsetinbits1094__ >> 3;
      int __shift1106__ = __offsetinbits1094__ - (__offset1105__ << 3);
      int __rightop1091__ = ((*(int *)(__left1092__ + __offset1105__))  >> __shift1106__) & 0xffffffff;
      int __leftop1089__ = __leftop1090__ * __rightop1091__;
      int __rightop1107__ = 0;
      int __leftop1088__ = __leftop1089__ + __rightop1107__;
      int __rightop1108__ = (int) __bbb__;
      int __rightop1087__ = __leftop1088__ * __rightop1108__;
      int __offsetinbits1085__ = __leftop1086__ + __rightop1087__;
      // __offsetinbits1085__ = (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __offset1109__ = __offsetinbits1085__ >> 3;
      int __expr1083__ = (__left1084__ + __offset1109__);
      int __left1082__ = (int) __expr1083__;
      // __left1082__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits1110__ <-- (0 + (1 * j))
      int __leftop1111__ = 0;
      int __leftop1113__ = 1;
      int __rightop1114__ = (int) __j__;
      int __rightop1112__ = __leftop1113__ * __rightop1114__;
      int __offsetinbits1110__ = __leftop1111__ + __rightop1112__;
      // __offsetinbits1110__ = (0 + (1 * j))
      int __offset1115__ = __offsetinbits1110__ >> 3;
      int __shift1116__ = __offsetinbits1110__ - (__offset1115__ << 3);
      int __leftop1081__ = ((*(int *)(__left1082__ + __offset1115__))  >> __shift1116__) & 0x1;
      int __rightop1117__ = 0;
      int __tempvar1080__ = __leftop1081__ == __rightop1117__;
      if (__tempvar1080__) {
        int __leftele1118__ = (int) __j__;
        int __rightele1119__ = 101;
        __blockstatus___hash->add((int)__leftele1118__, (int)__rightele1119__);
      }
    }
  }
}


// build rule19
{
  int __tempvar1121__ = 0;
  // __left1124__ <-- d.s
  // __left1125__ <-- d
  int __left1125__ = (int) d;
  // __left1125__ = d
  int __left1124__ = (__left1125__ + 0);
  // __left1124__ = d.s
  // __offsetinbits1126__ <-- (32 + (32 + 0))
  int __leftop1127__ = 32;
  int __leftop1129__ = 32;
  int __rightop1130__ = 0;
  int __rightop1128__ = __leftop1129__ + __rightop1130__;
  int __offsetinbits1126__ = __leftop1127__ + __rightop1128__;
  // __offsetinbits1126__ = (32 + (32 + 0))
  int __offset1131__ = __offsetinbits1126__ >> 3;
  int __shift1132__ = __offsetinbits1126__ - (__offset1131__ << 3);
  int __leftop1123__ = ((*(int *)(__left1124__ + __offset1131__))  >> __shift1132__) & 0xffffffff;
  int __rightop1133__ = 1;
  int __tempvar1122__ = __leftop1123__ - __rightop1133__;
  for (int __j__ = __tempvar1121__; __j__ <= __tempvar1122__; __j__++) {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); ) {
      int __bbb__ = (int) __bbb___iterator->next();
      //(cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == true)
      // __left1136__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left1138__ <-- d
      int __left1138__ = (int) d;
      // __left1138__ = d
      // __offsetinbits1139__ <-- (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __leftop1140__ = 0;
      int __leftop1144__ = 8;
      // __left1146__ <-- d.s
      // __left1147__ <-- d
      int __left1147__ = (int) d;
      // __left1147__ = d
      int __left1146__ = (__left1147__ + 0);
      // __left1146__ = d.s
      // __offsetinbits1148__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop1149__ = 32;
      int __leftop1151__ = 32;
      int __leftop1153__ = 32;
      int __leftop1155__ = 32;
      int __leftop1157__ = 32;
      int __rightop1158__ = 0;
      int __rightop1156__ = __leftop1157__ + __rightop1158__;
      int __rightop1154__ = __leftop1155__ + __rightop1156__;
      int __rightop1152__ = __leftop1153__ + __rightop1154__;
      int __rightop1150__ = __leftop1151__ + __rightop1152__;
      int __offsetinbits1148__ = __leftop1149__ + __rightop1150__;
      // __offsetinbits1148__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset1159__ = __offsetinbits1148__ >> 3;
      int __shift1160__ = __offsetinbits1148__ - (__offset1159__ << 3);
      int __rightop1145__ = ((*(int *)(__left1146__ + __offset1159__))  >> __shift1160__) & 0xffffffff;
      int __leftop1143__ = __leftop1144__ * __rightop1145__;
      int __rightop1161__ = 0;
      int __leftop1142__ = __leftop1143__ + __rightop1161__;
      int __rightop1162__ = (int) __bbb__;
      int __rightop1141__ = __leftop1142__ * __rightop1162__;
      int __offsetinbits1139__ = __leftop1140__ + __rightop1141__;
      // __offsetinbits1139__ = (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __offset1163__ = __offsetinbits1139__ >> 3;
      int __expr1137__ = (__left1138__ + __offset1163__);
      int __left1136__ = (int) __expr1137__;
      // __left1136__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits1164__ <-- (0 + (1 * j))
      int __leftop1165__ = 0;
      int __leftop1167__ = 1;
      int __rightop1168__ = (int) __j__;
      int __rightop1166__ = __leftop1167__ * __rightop1168__;
      int __offsetinbits1164__ = __leftop1165__ + __rightop1166__;
      // __offsetinbits1164__ = (0 + (1 * j))
      int __offset1169__ = __offsetinbits1164__ >> 3;
      int __shift1170__ = __offsetinbits1164__ - (__offset1169__ << 3);
      int __leftop1135__ = ((*(int *)(__left1136__ + __offset1169__))  >> __shift1170__) & 0x1;
      int __rightop1171__ = 1;
      int __tempvar1134__ = __leftop1135__ == __rightop1171__;
      if (__tempvar1134__) {
        int __leftele1172__ = (int) __j__;
        int __rightele1173__ = 100;
        __blockstatus___hash->add((int)__leftele1172__, (int)__rightele1173__);
      }
    }
  }
}


// checking c1
{
  for (SimpleIterator* __u___iterator = __UsedInode___hash->iterator(); __u___iterator->hasNext(); ) {
    int __u__ = (int) __u___iterator->next();
    int __relval1176__ = __inodestatus___hash->get(__u__);
    int __exprval1177__ = 100;
    int __constraintboolean1175__ = __relval1176__==__exprval1177__;
    if (!__constraintboolean1175__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c2
{
  for (SimpleIterator* __f___iterator = __FreeInode___hash->iterator(); __f___iterator->hasNext(); ) {
    int __f__ = (int) __f___iterator->next();
    int __relval1179__ = __inodestatus___hash->get(__f__);
    int __exprval1180__ = 101;
    int __constraintboolean1178__ = __relval1179__==__exprval1180__;
    if (!__constraintboolean1178__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c3
{
  for (SimpleIterator* __u___iterator = __UsedBlock___hash->iterator(); __u___iterator->hasNext(); ) {
    int __u__ = (int) __u___iterator->next();
    int __relval1182__ = __blockstatus___hash->get(__u__);
    int __exprval1183__ = 100;
    int __constraintboolean1181__ = __relval1182__==__exprval1183__;
    if (!__constraintboolean1181__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c4
{
  for (SimpleIterator* __f___iterator = __FreeBlock___hash->iterator(); __f___iterator->hasNext(); ) {
    int __f__ = (int) __f___iterator->next();
    int __relval1185__ = __blockstatus___hash->get(__f__);
    int __exprval1186__ = 101;
    int __constraintboolean1184__ = __relval1185__==__exprval1186__;
    if (!__constraintboolean1184__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c5
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    int __relval1188__ = __referencecount___hash->get(__i__);
    int __exprval1189__ = __inodeof___hashinv->count(__i__);
    int __constraintboolean1187__ = __relval1188__==__exprval1189__;
    if (!__constraintboolean1187__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c6
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    int __relval1191__ = __filesize___hash->get(__i__);
    int __leftop1193__ = __contents___hash->count(__i__);
    int __rightop1194__ = 8192;
    int __exprval1192__ = __leftop1193__ * __rightop1194__;
    int __constraintboolean1190__ = __relval1191__<=__exprval1192__;
    if (!__constraintboolean1190__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c7
{
  for (SimpleIterator* __b___iterator = __FileDirectoryBlock___hash->iterator(); __b___iterator->hasNext(); ) {
    int __b__ = (int) __b___iterator->next();
    int __size1196__ = __contents___hashinv->count(__b__);
    int __constraintboolean1195__ = __size1196__==1;
    if (!__constraintboolean1195__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c8
{
  int __size1198__ = __SuperBlock___hash->count();
  int __constraintboolean1197__ = __size1198__==1;
  if (!__constraintboolean1197__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c9
{
  int __size1200__ = __GroupBlock___hash->count();
  int __constraintboolean1199__ = __size1200__==1;
  if (!__constraintboolean1199__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c10
{
  int __size1202__ = __InodeTableBlock___hash->count();
  int __constraintboolean1201__ = __size1202__==1;
  if (!__constraintboolean1201__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c11
{
  int __size1204__ = __InodeBitmapBlock___hash->count();
  int __constraintboolean1203__ = __size1204__==1;
  if (!__constraintboolean1203__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c12
{
  int __size1206__ = __BlockBitmapBlock___hash->count();
  int __constraintboolean1205__ = __size1206__==1;
  if (!__constraintboolean1205__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c13
{
  int __size1208__ = __RootDirectoryInode___hash->count();
  int __constraintboolean1207__ = __size1208__==1;
  if (!__constraintboolean1207__) {
    __Success = 0;
    printf("fail. ");
  }
}


if (__Success) { printf("all tests passed"); }
