
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
SimpleHash* __referencecount___hashinv = new SimpleHash();
SimpleHash* __filesize___hash = new SimpleHash();
SimpleHash* __filesize___hashinv = new SimpleHash();
SimpleHash* __inodeof___hash = new SimpleHash();
SimpleHash* __inodeof___hashinv = new SimpleHash();
SimpleHash* __contents___hash = new SimpleHash();
SimpleHash* __contents___hashinv = new SimpleHash();
SimpleHash* __inodestatus___hash = new SimpleHash();
SimpleHash* __inodestatus___hashinv = new SimpleHash();
SimpleHash* __blockstatus___hash = new SimpleHash();
SimpleHash* __blockstatus___hashinv = new SimpleHash();


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


// build rule5
{
  //d.g.BlockBitmapBlock < d.s.NumberofBlocks
  // __left164__ <-- d.g
  // __left165__ <-- d
  int __left165__ = (int) d;
  // __left165__ = d
  // __offsetinbits166__ <-- 0 + 8 * d.s.blocksize + 0 * 1
  int __leftop167__ = 0;
  int __leftop171__ = 8;
  // __left173__ <-- d.s
  // __left174__ <-- d
  int __left174__ = (int) d;
  // __left174__ = d
  int __left173__ = (__left174__ + 0);
  // __left173__ = d.s
  // __offsetinbits175__ <-- 32 + 32 + 32 + 32 + 32 + 0
  int __leftop176__ = 32;
  int __leftop178__ = 32;
  int __leftop180__ = 32;
  int __leftop182__ = 32;
  int __leftop184__ = 32;
  int __rightop185__ = 0;
  int __rightop183__ = __leftop184__ + __rightop185__;
  int __rightop181__ = __leftop182__ + __rightop183__;
  int __rightop179__ = __leftop180__ + __rightop181__;
  int __rightop177__ = __leftop178__ + __rightop179__;
  int __offsetinbits175__ = __leftop176__ + __rightop177__;
  // __offsetinbits175__ = 32 + 32 + 32 + 32 + 32 + 0
  int __offset186__ = __offsetinbits175__ >> 3;
  int __shift187__ = __offsetinbits175__ - (__offset186__ << 3);
  int __rightop172__ = ((*(int *)(__left173__ + __offset186__))  >> __shift187__) & 0xffffffff;
  int __leftop170__ = __leftop171__ * __rightop172__;
  int __rightop188__ = 0;
  int __leftop169__ = __leftop170__ + __rightop188__;
  int __rightop189__ = 1;
  int __rightop168__ = __leftop169__ * __rightop189__;
  int __offsetinbits166__ = __leftop167__ + __rightop168__;
  // __offsetinbits166__ = 0 + 8 * d.s.blocksize + 0 * 1
  int __offset190__ = __offsetinbits166__ >> 3;
  int __left164__ = (__left165__ + __offset190__);
  // __left164__ = d.g
  int __leftop163__ = ((*(int *)(__left164__ + 0))  >> 0) & 0xffffffff;
  // __left192__ <-- d.s
  // __left193__ <-- d
  int __left193__ = (int) d;
  // __left193__ = d
  int __left192__ = (__left193__ + 0);
  // __left192__ = d.s
  // __offsetinbits194__ <-- 32 + 32 + 0
  int __leftop195__ = 32;
  int __leftop197__ = 32;
  int __rightop198__ = 0;
  int __rightop196__ = __leftop197__ + __rightop198__;
  int __offsetinbits194__ = __leftop195__ + __rightop196__;
  // __offsetinbits194__ = 32 + 32 + 0
  int __offset199__ = __offsetinbits194__ >> 3;
  int __shift200__ = __offsetinbits194__ - (__offset199__ << 3);
  int __rightop191__ = ((*(int *)(__left192__ + __offset199__))  >> __shift200__) & 0xffffffff;
  int __tempvar162__ = __leftop163__ < __rightop191__;
  if (__tempvar162__) {
    // __left202__ <-- d.g
    // __left203__ <-- d
    int __left203__ = (int) d;
    // __left203__ = d
    // __offsetinbits204__ <-- 0 + 8 * d.s.blocksize + 0 * 1
    int __leftop205__ = 0;
    int __leftop209__ = 8;
    // __left211__ <-- d.s
    // __left212__ <-- d
    int __left212__ = (int) d;
    // __left212__ = d
    int __left211__ = (__left212__ + 0);
    // __left211__ = d.s
    // __offsetinbits213__ <-- 32 + 32 + 32 + 32 + 32 + 0
    int __leftop214__ = 32;
    int __leftop216__ = 32;
    int __leftop218__ = 32;
    int __leftop220__ = 32;
    int __leftop222__ = 32;
    int __rightop223__ = 0;
    int __rightop221__ = __leftop222__ + __rightop223__;
    int __rightop219__ = __leftop220__ + __rightop221__;
    int __rightop217__ = __leftop218__ + __rightop219__;
    int __rightop215__ = __leftop216__ + __rightop217__;
    int __offsetinbits213__ = __leftop214__ + __rightop215__;
    // __offsetinbits213__ = 32 + 32 + 32 + 32 + 32 + 0
    int __offset224__ = __offsetinbits213__ >> 3;
    int __shift225__ = __offsetinbits213__ - (__offset224__ << 3);
    int __rightop210__ = ((*(int *)(__left211__ + __offset224__))  >> __shift225__) & 0xffffffff;
    int __leftop208__ = __leftop209__ * __rightop210__;
    int __rightop226__ = 0;
    int __leftop207__ = __leftop208__ + __rightop226__;
    int __rightop227__ = 1;
    int __rightop206__ = __leftop207__ * __rightop227__;
    int __offsetinbits204__ = __leftop205__ + __rightop206__;
    // __offsetinbits204__ = 0 + 8 * d.s.blocksize + 0 * 1
    int __offset228__ = __offsetinbits204__ >> 3;
    int __left202__ = (__left203__ + __offset228__);
    // __left202__ = d.g
    int __element201__ = ((*(int *)(__left202__ + 0))  >> 0) & 0xffffffff;
    __BlockBitmapBlock___hash->add((int)__element201__, (int)__element201__);
  }
}


// build rule6
{
  //d.s.RootDirectoryInode < d.s.NumberofInodes
  // __left231__ <-- d.s
  // __left232__ <-- d
  int __left232__ = (int) d;
  // __left232__ = d
  int __left231__ = (__left232__ + 0);
  // __left231__ = d.s
  // __offsetinbits233__ <-- 32 + 32 + 32 + 32 + 0
  int __leftop234__ = 32;
  int __leftop236__ = 32;
  int __leftop238__ = 32;
  int __leftop240__ = 32;
  int __rightop241__ = 0;
  int __rightop239__ = __leftop240__ + __rightop241__;
  int __rightop237__ = __leftop238__ + __rightop239__;
  int __rightop235__ = __leftop236__ + __rightop237__;
  int __offsetinbits233__ = __leftop234__ + __rightop235__;
  // __offsetinbits233__ = 32 + 32 + 32 + 32 + 0
  int __offset242__ = __offsetinbits233__ >> 3;
  int __shift243__ = __offsetinbits233__ - (__offset242__ << 3);
  int __leftop230__ = ((*(int *)(__left231__ + __offset242__))  >> __shift243__) & 0xffffffff;
  // __left245__ <-- d.s
  // __left246__ <-- d
  int __left246__ = (int) d;
  // __left246__ = d
  int __left245__ = (__left246__ + 0);
  // __left245__ = d.s
  // __offsetinbits247__ <-- 32 + 32 + 32 + 0
  int __leftop248__ = 32;
  int __leftop250__ = 32;
  int __leftop252__ = 32;
  int __rightop253__ = 0;
  int __rightop251__ = __leftop252__ + __rightop253__;
  int __rightop249__ = __leftop250__ + __rightop251__;
  int __offsetinbits247__ = __leftop248__ + __rightop249__;
  // __offsetinbits247__ = 32 + 32 + 32 + 0
  int __offset254__ = __offsetinbits247__ >> 3;
  int __shift255__ = __offsetinbits247__ - (__offset254__ << 3);
  int __rightop244__ = ((*(int *)(__left245__ + __offset254__))  >> __shift255__) & 0xffffffff;
  int __tempvar229__ = __leftop230__ < __rightop244__;
  if (__tempvar229__) {
    // __left257__ <-- d.s
    // __left258__ <-- d
    int __left258__ = (int) d;
    // __left258__ = d
    int __left257__ = (__left258__ + 0);
    // __left257__ = d.s
    // __offsetinbits259__ <-- 32 + 32 + 32 + 32 + 0
    int __leftop260__ = 32;
    int __leftop262__ = 32;
    int __leftop264__ = 32;
    int __leftop266__ = 32;
    int __rightop267__ = 0;
    int __rightop265__ = __leftop266__ + __rightop267__;
    int __rightop263__ = __leftop264__ + __rightop265__;
    int __rightop261__ = __leftop262__ + __rightop263__;
    int __offsetinbits259__ = __leftop260__ + __rightop261__;
    // __offsetinbits259__ = 32 + 32 + 32 + 32 + 0
    int __offset268__ = __offsetinbits259__ >> 3;
    int __shift269__ = __offsetinbits259__ - (__offset268__ << 3);
    int __element256__ = ((*(int *)(__left257__ + __offset268__))  >> __shift269__) & 0xffffffff;
    __RootDirectoryInode___hash->add((int)__element256__, (int)__element256__);
  }
}


// build rule9
{
  for (SimpleIterator* __di___iterator = __DirectoryInode___hash->iterator(); __di___iterator->hasNext(); ) {
    int __di__ = (int) __di___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar270__ = 0;
      // __left274__ <-- d.s
      // __left275__ <-- d
      int __left275__ = (int) d;
      // __left275__ = d
      int __left274__ = (__left275__ + 0);
      // __left274__ = d.s
      // __offsetinbits276__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop277__ = 32;
      int __leftop279__ = 32;
      int __leftop281__ = 32;
      int __leftop283__ = 32;
      int __leftop285__ = 32;
      int __rightop286__ = 0;
      int __rightop284__ = __leftop285__ + __rightop286__;
      int __rightop282__ = __leftop283__ + __rightop284__;
      int __rightop280__ = __leftop281__ + __rightop282__;
      int __rightop278__ = __leftop279__ + __rightop280__;
      int __offsetinbits276__ = __leftop277__ + __rightop278__;
      // __offsetinbits276__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset287__ = __offsetinbits276__ >> 3;
      int __shift288__ = __offsetinbits276__ - (__offset287__ << 3);
      int __leftop273__ = ((*(int *)(__left274__ + __offset287__))  >> __shift288__) & 0xffffffff;
      int __rightop289__ = 128;
      int __leftop272__ = __leftop273__ / __rightop289__;
      int __rightop290__ = 1;
      int __tempvar271__ = __leftop272__ - __rightop290__;
      for (int __j__ = __tempvar270__; __j__ <= __tempvar271__; __j__++) {
        int __tempvar291__ = 0;
        int __tempvar292__ = 11;
        for (int __k__ = __tempvar291__; __k__ <= __tempvar292__; __k__++) {
          //cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k] < d.s.NumberofBlocks
          // __left295__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
          // __left296__ <-- cast(__InodeTable__, d.b[itb])
          // __left298__ <-- d
          int __left298__ = (int) d;
          // __left298__ = d
          // __offsetinbits299__ <-- 0 + 8 * d.s.blocksize + 0 * itb
          int __leftop300__ = 0;
          int __leftop304__ = 8;
          // __left306__ <-- d.s
          // __left307__ <-- d
          int __left307__ = (int) d;
          // __left307__ = d
          int __left306__ = (__left307__ + 0);
          // __left306__ = d.s
          // __offsetinbits308__ <-- 32 + 32 + 32 + 32 + 32 + 0
          int __leftop309__ = 32;
          int __leftop311__ = 32;
          int __leftop313__ = 32;
          int __leftop315__ = 32;
          int __leftop317__ = 32;
          int __rightop318__ = 0;
          int __rightop316__ = __leftop317__ + __rightop318__;
          int __rightop314__ = __leftop315__ + __rightop316__;
          int __rightop312__ = __leftop313__ + __rightop314__;
          int __rightop310__ = __leftop311__ + __rightop312__;
          int __offsetinbits308__ = __leftop309__ + __rightop310__;
          // __offsetinbits308__ = 32 + 32 + 32 + 32 + 32 + 0
          int __offset319__ = __offsetinbits308__ >> 3;
          int __shift320__ = __offsetinbits308__ - (__offset319__ << 3);
          int __rightop305__ = ((*(int *)(__left306__ + __offset319__))  >> __shift320__) & 0xffffffff;
          int __leftop303__ = __leftop304__ * __rightop305__;
          int __rightop321__ = 0;
          int __leftop302__ = __leftop303__ + __rightop321__;
          int __rightop322__ = (int) __itb__;
          int __rightop301__ = __leftop302__ * __rightop322__;
          int __offsetinbits299__ = __leftop300__ + __rightop301__;
          // __offsetinbits299__ = 0 + 8 * d.s.blocksize + 0 * itb
          int __offset323__ = __offsetinbits299__ >> 3;
          int __expr297__ = (__left298__ + __offset323__);
          int __left296__ = (int) __expr297__;
          // __left296__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits324__ <-- 0 + 32 + 32 * 12 + 32 + 0 * di
          int __leftop325__ = 0;
          int __leftop328__ = 32;
          int __leftop331__ = 32;
          int __rightop332__ = 12;
          int __leftop330__ = __leftop331__ * __rightop332__;
          int __leftop334__ = 32;
          int __rightop335__ = 0;
          int __rightop333__ = __leftop334__ + __rightop335__;
          int __rightop329__ = __leftop330__ + __rightop333__;
          int __leftop327__ = __leftop328__ + __rightop329__;
          int __rightop336__ = (int) __di__;
          int __rightop326__ = __leftop327__ * __rightop336__;
          int __offsetinbits324__ = __leftop325__ + __rightop326__;
          // __offsetinbits324__ = 0 + 32 + 32 * 12 + 32 + 0 * di
          int __offset337__ = __offsetinbits324__ >> 3;
          int __left295__ = (__left296__ + __offset337__);
          // __left295__ = cast(__InodeTable__, d.b[itb]).itable[di]
          // __offsetinbits338__ <-- 32 + 0 + 32 * k
          int __leftop340__ = 32;
          int __rightop341__ = 0;
          int __leftop339__ = __leftop340__ + __rightop341__;
          int __leftop343__ = 32;
          int __rightop344__ = (int) __k__;
          int __rightop342__ = __leftop343__ * __rightop344__;
          int __offsetinbits338__ = __leftop339__ + __rightop342__;
          // __offsetinbits338__ = 32 + 0 + 32 * k
          int __offset345__ = __offsetinbits338__ >> 3;
          int __shift346__ = __offsetinbits338__ - (__offset345__ << 3);
          int __leftop294__ = ((*(int *)(__left295__ + __offset345__))  >> __shift346__) & 0xffffffff;
          // __left348__ <-- d.s
          // __left349__ <-- d
          int __left349__ = (int) d;
          // __left349__ = d
          int __left348__ = (__left349__ + 0);
          // __left348__ = d.s
          // __offsetinbits350__ <-- 32 + 32 + 0
          int __leftop351__ = 32;
          int __leftop353__ = 32;
          int __rightop354__ = 0;
          int __rightop352__ = __leftop353__ + __rightop354__;
          int __offsetinbits350__ = __leftop351__ + __rightop352__;
          // __offsetinbits350__ = 32 + 32 + 0
          int __offset355__ = __offsetinbits350__ >> 3;
          int __shift356__ = __offsetinbits350__ - (__offset355__ << 3);
          int __rightop347__ = ((*(int *)(__left348__ + __offset355__))  >> __shift356__) & 0xffffffff;
          int __tempvar293__ = __leftop294__ < __rightop347__;
          if (__tempvar293__) {
            // __left358__ <-- cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __left360__ <-- d
            int __left360__ = (int) d;
            // __left360__ = d
            // __offsetinbits361__ <-- 0 + 8 * d.s.blocksize + 0 * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]
            int __leftop362__ = 0;
            int __leftop366__ = 8;
            // __left368__ <-- d.s
            // __left369__ <-- d
            int __left369__ = (int) d;
            // __left369__ = d
            int __left368__ = (__left369__ + 0);
            // __left368__ = d.s
            // __offsetinbits370__ <-- 32 + 32 + 32 + 32 + 32 + 0
            int __leftop371__ = 32;
            int __leftop373__ = 32;
            int __leftop375__ = 32;
            int __leftop377__ = 32;
            int __leftop379__ = 32;
            int __rightop380__ = 0;
            int __rightop378__ = __leftop379__ + __rightop380__;
            int __rightop376__ = __leftop377__ + __rightop378__;
            int __rightop374__ = __leftop375__ + __rightop376__;
            int __rightop372__ = __leftop373__ + __rightop374__;
            int __offsetinbits370__ = __leftop371__ + __rightop372__;
            // __offsetinbits370__ = 32 + 32 + 32 + 32 + 32 + 0
            int __offset381__ = __offsetinbits370__ >> 3;
            int __shift382__ = __offsetinbits370__ - (__offset381__ << 3);
            int __rightop367__ = ((*(int *)(__left368__ + __offset381__))  >> __shift382__) & 0xffffffff;
            int __leftop365__ = __leftop366__ * __rightop367__;
            int __rightop383__ = 0;
            int __leftop364__ = __leftop365__ + __rightop383__;
            // __left385__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
            // __left386__ <-- cast(__InodeTable__, d.b[itb])
            // __left388__ <-- d
            int __left388__ = (int) d;
            // __left388__ = d
            // __offsetinbits389__ <-- 0 + 8 * d.s.blocksize + 0 * itb
            int __leftop390__ = 0;
            int __leftop394__ = 8;
            // __left396__ <-- d.s
            // __left397__ <-- d
            int __left397__ = (int) d;
            // __left397__ = d
            int __left396__ = (__left397__ + 0);
            // __left396__ = d.s
            // __offsetinbits398__ <-- 32 + 32 + 32 + 32 + 32 + 0
            int __leftop399__ = 32;
            int __leftop401__ = 32;
            int __leftop403__ = 32;
            int __leftop405__ = 32;
            int __leftop407__ = 32;
            int __rightop408__ = 0;
            int __rightop406__ = __leftop407__ + __rightop408__;
            int __rightop404__ = __leftop405__ + __rightop406__;
            int __rightop402__ = __leftop403__ + __rightop404__;
            int __rightop400__ = __leftop401__ + __rightop402__;
            int __offsetinbits398__ = __leftop399__ + __rightop400__;
            // __offsetinbits398__ = 32 + 32 + 32 + 32 + 32 + 0
            int __offset409__ = __offsetinbits398__ >> 3;
            int __shift410__ = __offsetinbits398__ - (__offset409__ << 3);
            int __rightop395__ = ((*(int *)(__left396__ + __offset409__))  >> __shift410__) & 0xffffffff;
            int __leftop393__ = __leftop394__ * __rightop395__;
            int __rightop411__ = 0;
            int __leftop392__ = __leftop393__ + __rightop411__;
            int __rightop412__ = (int) __itb__;
            int __rightop391__ = __leftop392__ * __rightop412__;
            int __offsetinbits389__ = __leftop390__ + __rightop391__;
            // __offsetinbits389__ = 0 + 8 * d.s.blocksize + 0 * itb
            int __offset413__ = __offsetinbits389__ >> 3;
            int __expr387__ = (__left388__ + __offset413__);
            int __left386__ = (int) __expr387__;
            // __left386__ = cast(__InodeTable__, d.b[itb])
            // __offsetinbits414__ <-- 0 + 32 + 32 * 12 + 32 + 0 * di
            int __leftop415__ = 0;
            int __leftop418__ = 32;
            int __leftop421__ = 32;
            int __rightop422__ = 12;
            int __leftop420__ = __leftop421__ * __rightop422__;
            int __leftop424__ = 32;
            int __rightop425__ = 0;
            int __rightop423__ = __leftop424__ + __rightop425__;
            int __rightop419__ = __leftop420__ + __rightop423__;
            int __leftop417__ = __leftop418__ + __rightop419__;
            int __rightop426__ = (int) __di__;
            int __rightop416__ = __leftop417__ * __rightop426__;
            int __offsetinbits414__ = __leftop415__ + __rightop416__;
            // __offsetinbits414__ = 0 + 32 + 32 * 12 + 32 + 0 * di
            int __offset427__ = __offsetinbits414__ >> 3;
            int __left385__ = (__left386__ + __offset427__);
            // __left385__ = cast(__InodeTable__, d.b[itb]).itable[di]
            // __offsetinbits428__ <-- 32 + 0 + 32 * k
            int __leftop430__ = 32;
            int __rightop431__ = 0;
            int __leftop429__ = __leftop430__ + __rightop431__;
            int __leftop433__ = 32;
            int __rightop434__ = (int) __k__;
            int __rightop432__ = __leftop433__ * __rightop434__;
            int __offsetinbits428__ = __leftop429__ + __rightop432__;
            // __offsetinbits428__ = 32 + 0 + 32 * k
            int __offset435__ = __offsetinbits428__ >> 3;
            int __shift436__ = __offsetinbits428__ - (__offset435__ << 3);
            int __rightop384__ = ((*(int *)(__left385__ + __offset435__))  >> __shift436__) & 0xffffffff;
            int __rightop363__ = __leftop364__ * __rightop384__;
            int __offsetinbits361__ = __leftop362__ + __rightop363__;
            // __offsetinbits361__ = 0 + 8 * d.s.blocksize + 0 * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]
            int __offset437__ = __offsetinbits361__ >> 3;
            int __expr359__ = (__left360__ + __offset437__);
            int __left358__ = (int) __expr359__;
            // __left358__ = cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __offsetinbits438__ <-- 0 + 32 + 8 * 124 + 0 * j
            int __leftop439__ = 0;
            int __leftop442__ = 32;
            int __leftop445__ = 8;
            int __rightop446__ = 124;
            int __leftop444__ = __leftop445__ * __rightop446__;
            int __rightop447__ = 0;
            int __rightop443__ = __leftop444__ + __rightop447__;
            int __leftop441__ = __leftop442__ + __rightop443__;
            int __rightop448__ = (int) __j__;
            int __rightop440__ = __leftop441__ * __rightop448__;
            int __offsetinbits438__ = __leftop439__ + __rightop440__;
            // __offsetinbits438__ = 0 + 32 + 8 * 124 + 0 * j
            int __offset449__ = __offsetinbits438__ >> 3;
            int __element357__ = (__left358__ + __offset449__);
            __DirectoryEntry___hash->add((int)__element357__, (int)__element357__);
          }
        }
      }
    }
  }
}


// build rule14
{
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); ) {
    int __de__ = (int) __de___iterator->next();
    //de.inodenumber < d.s.NumberofInodes && !de.inodenumber == 0
    // __left453__ <-- de
    int __left453__ = (int) __de__;
    // __left453__ = de
    // __offsetinbits454__ <-- 8 * 124 + 0
    int __leftop456__ = 8;
    int __rightop457__ = 124;
    int __leftop455__ = __leftop456__ * __rightop457__;
    int __rightop458__ = 0;
    int __offsetinbits454__ = __leftop455__ + __rightop458__;
    // __offsetinbits454__ = 8 * 124 + 0
    int __offset459__ = __offsetinbits454__ >> 3;
    int __shift460__ = __offsetinbits454__ - (__offset459__ << 3);
    int __leftop452__ = ((*(int *)(__left453__ + __offset459__))  >> __shift460__) & 0xffffffff;
    // __left462__ <-- d.s
    // __left463__ <-- d
    int __left463__ = (int) d;
    // __left463__ = d
    int __left462__ = (__left463__ + 0);
    // __left462__ = d.s
    // __offsetinbits464__ <-- 32 + 32 + 32 + 0
    int __leftop465__ = 32;
    int __leftop467__ = 32;
    int __leftop469__ = 32;
    int __rightop470__ = 0;
    int __rightop468__ = __leftop469__ + __rightop470__;
    int __rightop466__ = __leftop467__ + __rightop468__;
    int __offsetinbits464__ = __leftop465__ + __rightop466__;
    // __offsetinbits464__ = 32 + 32 + 32 + 0
    int __offset471__ = __offsetinbits464__ >> 3;
    int __shift472__ = __offsetinbits464__ - (__offset471__ << 3);
    int __rightop461__ = ((*(int *)(__left462__ + __offset471__))  >> __shift472__) & 0xffffffff;
    int __leftop451__ = __leftop452__ < __rightop461__;
    // __left476__ <-- de
    int __left476__ = (int) __de__;
    // __left476__ = de
    // __offsetinbits477__ <-- 8 * 124 + 0
    int __leftop479__ = 8;
    int __rightop480__ = 124;
    int __leftop478__ = __leftop479__ * __rightop480__;
    int __rightop481__ = 0;
    int __offsetinbits477__ = __leftop478__ + __rightop481__;
    // __offsetinbits477__ = 8 * 124 + 0
    int __offset482__ = __offsetinbits477__ >> 3;
    int __shift483__ = __offsetinbits477__ - (__offset482__ << 3);
    int __leftop475__ = ((*(int *)(__left476__ + __offset482__))  >> __shift483__) & 0xffffffff;
    int __rightop484__ = 0;
    int __leftop474__ = __leftop475__ == __rightop484__;
    int __rightop473__ = !__leftop474__;
    int __tempvar450__ = __leftop451__ && __rightop473__;
    if (__tempvar450__) {
      // __left486__ <-- de
      int __left486__ = (int) __de__;
      // __left486__ = de
      // __offsetinbits487__ <-- 8 * 124 + 0
      int __leftop489__ = 8;
      int __rightop490__ = 124;
      int __leftop488__ = __leftop489__ * __rightop490__;
      int __rightop491__ = 0;
      int __offsetinbits487__ = __leftop488__ + __rightop491__;
      // __offsetinbits487__ = 8 * 124 + 0
      int __offset492__ = __offsetinbits487__ >> 3;
      int __shift493__ = __offsetinbits487__ - (__offset492__ << 3);
      int __element485__ = ((*(int *)(__left486__ + __offset492__))  >> __shift493__) & 0xffffffff;
      __FileInode___hash->add((int)__element485__, (int)__element485__);
    }
  }
}


// build rule15
{
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); ) {
    int __de__ = (int) __de___iterator->next();
    //de.inodenumber < d.s.NumberofInodes
    // __left496__ <-- de
    int __left496__ = (int) __de__;
    // __left496__ = de
    // __offsetinbits497__ <-- 8 * 124 + 0
    int __leftop499__ = 8;
    int __rightop500__ = 124;
    int __leftop498__ = __leftop499__ * __rightop500__;
    int __rightop501__ = 0;
    int __offsetinbits497__ = __leftop498__ + __rightop501__;
    // __offsetinbits497__ = 8 * 124 + 0
    int __offset502__ = __offsetinbits497__ >> 3;
    int __shift503__ = __offsetinbits497__ - (__offset502__ << 3);
    int __leftop495__ = ((*(int *)(__left496__ + __offset502__))  >> __shift503__) & 0xffffffff;
    // __left505__ <-- d.s
    // __left506__ <-- d
    int __left506__ = (int) d;
    // __left506__ = d
    int __left505__ = (__left506__ + 0);
    // __left505__ = d.s
    // __offsetinbits507__ <-- 32 + 32 + 32 + 0
    int __leftop508__ = 32;
    int __leftop510__ = 32;
    int __leftop512__ = 32;
    int __rightop513__ = 0;
    int __rightop511__ = __leftop512__ + __rightop513__;
    int __rightop509__ = __leftop510__ + __rightop511__;
    int __offsetinbits507__ = __leftop508__ + __rightop509__;
    // __offsetinbits507__ = 32 + 32 + 32 + 0
    int __offset514__ = __offsetinbits507__ >> 3;
    int __shift515__ = __offsetinbits507__ - (__offset514__ << 3);
    int __rightop504__ = ((*(int *)(__left505__ + __offset514__))  >> __shift515__) & 0xffffffff;
    int __tempvar494__ = __leftop495__ < __rightop504__;
    if (__tempvar494__) {
      int __leftele516__ = (int) __de__;
      // __left518__ <-- de
      int __left518__ = (int) __de__;
      // __left518__ = de
      // __offsetinbits519__ <-- 8 * 124 + 0
      int __leftop521__ = 8;
      int __rightop522__ = 124;
      int __leftop520__ = __leftop521__ * __rightop522__;
      int __rightop523__ = 0;
      int __offsetinbits519__ = __leftop520__ + __rightop523__;
      // __offsetinbits519__ = 8 * 124 + 0
      int __offset524__ = __offsetinbits519__ >> 3;
      int __shift525__ = __offsetinbits519__ - (__offset524__ << 3);
      int __rightele517__ = ((*(int *)(__left518__ + __offset524__))  >> __shift525__) & 0xffffffff;
      __inodeof___hash->add((int)__leftele516__, (int)__rightele517__);
      __inodeof___hashinv->add((int)__rightele517__, (int)__leftele516__);
    }
  }
}


// build rule11
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar526__ = 0;
      int __tempvar527__ = 11;
      for (int __j__ = __tempvar526__; __j__ <= __tempvar527__; __j__++) {
        //cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] < d.s.NumberofBlocks && !cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0
        // __left531__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left532__ <-- cast(__InodeTable__, d.b[itb])
        // __left534__ <-- d
        int __left534__ = (int) d;
        // __left534__ = d
        // __offsetinbits535__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop536__ = 0;
        int __leftop540__ = 8;
        // __left542__ <-- d.s
        // __left543__ <-- d
        int __left543__ = (int) d;
        // __left543__ = d
        int __left542__ = (__left543__ + 0);
        // __left542__ = d.s
        // __offsetinbits544__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop545__ = 32;
        int __leftop547__ = 32;
        int __leftop549__ = 32;
        int __leftop551__ = 32;
        int __leftop553__ = 32;
        int __rightop554__ = 0;
        int __rightop552__ = __leftop553__ + __rightop554__;
        int __rightop550__ = __leftop551__ + __rightop552__;
        int __rightop548__ = __leftop549__ + __rightop550__;
        int __rightop546__ = __leftop547__ + __rightop548__;
        int __offsetinbits544__ = __leftop545__ + __rightop546__;
        // __offsetinbits544__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset555__ = __offsetinbits544__ >> 3;
        int __shift556__ = __offsetinbits544__ - (__offset555__ << 3);
        int __rightop541__ = ((*(int *)(__left542__ + __offset555__))  >> __shift556__) & 0xffffffff;
        int __leftop539__ = __leftop540__ * __rightop541__;
        int __rightop557__ = 0;
        int __leftop538__ = __leftop539__ + __rightop557__;
        int __rightop558__ = (int) __itb__;
        int __rightop537__ = __leftop538__ * __rightop558__;
        int __offsetinbits535__ = __leftop536__ + __rightop537__;
        // __offsetinbits535__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset559__ = __offsetinbits535__ >> 3;
        int __expr533__ = (__left534__ + __offset559__);
        int __left532__ = (int) __expr533__;
        // __left532__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits560__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
        int __leftop561__ = 0;
        int __leftop564__ = 32;
        int __leftop567__ = 32;
        int __rightop568__ = 12;
        int __leftop566__ = __leftop567__ * __rightop568__;
        int __leftop570__ = 32;
        int __rightop571__ = 0;
        int __rightop569__ = __leftop570__ + __rightop571__;
        int __rightop565__ = __leftop566__ + __rightop569__;
        int __leftop563__ = __leftop564__ + __rightop565__;
        int __rightop572__ = (int) __i__;
        int __rightop562__ = __leftop563__ * __rightop572__;
        int __offsetinbits560__ = __leftop561__ + __rightop562__;
        // __offsetinbits560__ = 0 + 32 + 32 * 12 + 32 + 0 * i
        int __offset573__ = __offsetinbits560__ >> 3;
        int __left531__ = (__left532__ + __offset573__);
        // __left531__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits574__ <-- 32 + 0 + 32 * j
        int __leftop576__ = 32;
        int __rightop577__ = 0;
        int __leftop575__ = __leftop576__ + __rightop577__;
        int __leftop579__ = 32;
        int __rightop580__ = (int) __j__;
        int __rightop578__ = __leftop579__ * __rightop580__;
        int __offsetinbits574__ = __leftop575__ + __rightop578__;
        // __offsetinbits574__ = 32 + 0 + 32 * j
        int __offset581__ = __offsetinbits574__ >> 3;
        int __shift582__ = __offsetinbits574__ - (__offset581__ << 3);
        int __leftop530__ = ((*(int *)(__left531__ + __offset581__))  >> __shift582__) & 0xffffffff;
        // __left584__ <-- d.s
        // __left585__ <-- d
        int __left585__ = (int) d;
        // __left585__ = d
        int __left584__ = (__left585__ + 0);
        // __left584__ = d.s
        // __offsetinbits586__ <-- 32 + 32 + 0
        int __leftop587__ = 32;
        int __leftop589__ = 32;
        int __rightop590__ = 0;
        int __rightop588__ = __leftop589__ + __rightop590__;
        int __offsetinbits586__ = __leftop587__ + __rightop588__;
        // __offsetinbits586__ = 32 + 32 + 0
        int __offset591__ = __offsetinbits586__ >> 3;
        int __shift592__ = __offsetinbits586__ - (__offset591__ << 3);
        int __rightop583__ = ((*(int *)(__left584__ + __offset591__))  >> __shift592__) & 0xffffffff;
        int __leftop529__ = __leftop530__ < __rightop583__;
        // __left596__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left597__ <-- cast(__InodeTable__, d.b[itb])
        // __left599__ <-- d
        int __left599__ = (int) d;
        // __left599__ = d
        // __offsetinbits600__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop601__ = 0;
        int __leftop605__ = 8;
        // __left607__ <-- d.s
        // __left608__ <-- d
        int __left608__ = (int) d;
        // __left608__ = d
        int __left607__ = (__left608__ + 0);
        // __left607__ = d.s
        // __offsetinbits609__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop610__ = 32;
        int __leftop612__ = 32;
        int __leftop614__ = 32;
        int __leftop616__ = 32;
        int __leftop618__ = 32;
        int __rightop619__ = 0;
        int __rightop617__ = __leftop618__ + __rightop619__;
        int __rightop615__ = __leftop616__ + __rightop617__;
        int __rightop613__ = __leftop614__ + __rightop615__;
        int __rightop611__ = __leftop612__ + __rightop613__;
        int __offsetinbits609__ = __leftop610__ + __rightop611__;
        // __offsetinbits609__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset620__ = __offsetinbits609__ >> 3;
        int __shift621__ = __offsetinbits609__ - (__offset620__ << 3);
        int __rightop606__ = ((*(int *)(__left607__ + __offset620__))  >> __shift621__) & 0xffffffff;
        int __leftop604__ = __leftop605__ * __rightop606__;
        int __rightop622__ = 0;
        int __leftop603__ = __leftop604__ + __rightop622__;
        int __rightop623__ = (int) __itb__;
        int __rightop602__ = __leftop603__ * __rightop623__;
        int __offsetinbits600__ = __leftop601__ + __rightop602__;
        // __offsetinbits600__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset624__ = __offsetinbits600__ >> 3;
        int __expr598__ = (__left599__ + __offset624__);
        int __left597__ = (int) __expr598__;
        // __left597__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits625__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
        int __leftop626__ = 0;
        int __leftop629__ = 32;
        int __leftop632__ = 32;
        int __rightop633__ = 12;
        int __leftop631__ = __leftop632__ * __rightop633__;
        int __leftop635__ = 32;
        int __rightop636__ = 0;
        int __rightop634__ = __leftop635__ + __rightop636__;
        int __rightop630__ = __leftop631__ + __rightop634__;
        int __leftop628__ = __leftop629__ + __rightop630__;
        int __rightop637__ = (int) __i__;
        int __rightop627__ = __leftop628__ * __rightop637__;
        int __offsetinbits625__ = __leftop626__ + __rightop627__;
        // __offsetinbits625__ = 0 + 32 + 32 * 12 + 32 + 0 * i
        int __offset638__ = __offsetinbits625__ >> 3;
        int __left596__ = (__left597__ + __offset638__);
        // __left596__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits639__ <-- 32 + 0 + 32 * j
        int __leftop641__ = 32;
        int __rightop642__ = 0;
        int __leftop640__ = __leftop641__ + __rightop642__;
        int __leftop644__ = 32;
        int __rightop645__ = (int) __j__;
        int __rightop643__ = __leftop644__ * __rightop645__;
        int __offsetinbits639__ = __leftop640__ + __rightop643__;
        // __offsetinbits639__ = 32 + 0 + 32 * j
        int __offset646__ = __offsetinbits639__ >> 3;
        int __shift647__ = __offsetinbits639__ - (__offset646__ << 3);
        int __leftop595__ = ((*(int *)(__left596__ + __offset646__))  >> __shift647__) & 0xffffffff;
        int __rightop648__ = 0;
        int __leftop594__ = __leftop595__ == __rightop648__;
        int __rightop593__ = !__leftop594__;
        int __tempvar528__ = __leftop529__ && __rightop593__;
        if (__tempvar528__) {
          // __left650__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left651__ <-- cast(__InodeTable__, d.b[itb])
          // __left653__ <-- d
          int __left653__ = (int) d;
          // __left653__ = d
          // __offsetinbits654__ <-- 0 + 8 * d.s.blocksize + 0 * itb
          int __leftop655__ = 0;
          int __leftop659__ = 8;
          // __left661__ <-- d.s
          // __left662__ <-- d
          int __left662__ = (int) d;
          // __left662__ = d
          int __left661__ = (__left662__ + 0);
          // __left661__ = d.s
          // __offsetinbits663__ <-- 32 + 32 + 32 + 32 + 32 + 0
          int __leftop664__ = 32;
          int __leftop666__ = 32;
          int __leftop668__ = 32;
          int __leftop670__ = 32;
          int __leftop672__ = 32;
          int __rightop673__ = 0;
          int __rightop671__ = __leftop672__ + __rightop673__;
          int __rightop669__ = __leftop670__ + __rightop671__;
          int __rightop667__ = __leftop668__ + __rightop669__;
          int __rightop665__ = __leftop666__ + __rightop667__;
          int __offsetinbits663__ = __leftop664__ + __rightop665__;
          // __offsetinbits663__ = 32 + 32 + 32 + 32 + 32 + 0
          int __offset674__ = __offsetinbits663__ >> 3;
          int __shift675__ = __offsetinbits663__ - (__offset674__ << 3);
          int __rightop660__ = ((*(int *)(__left661__ + __offset674__))  >> __shift675__) & 0xffffffff;
          int __leftop658__ = __leftop659__ * __rightop660__;
          int __rightop676__ = 0;
          int __leftop657__ = __leftop658__ + __rightop676__;
          int __rightop677__ = (int) __itb__;
          int __rightop656__ = __leftop657__ * __rightop677__;
          int __offsetinbits654__ = __leftop655__ + __rightop656__;
          // __offsetinbits654__ = 0 + 8 * d.s.blocksize + 0 * itb
          int __offset678__ = __offsetinbits654__ >> 3;
          int __expr652__ = (__left653__ + __offset678__);
          int __left651__ = (int) __expr652__;
          // __left651__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits679__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
          int __leftop680__ = 0;
          int __leftop683__ = 32;
          int __leftop686__ = 32;
          int __rightop687__ = 12;
          int __leftop685__ = __leftop686__ * __rightop687__;
          int __leftop689__ = 32;
          int __rightop690__ = 0;
          int __rightop688__ = __leftop689__ + __rightop690__;
          int __rightop684__ = __leftop685__ + __rightop688__;
          int __leftop682__ = __leftop683__ + __rightop684__;
          int __rightop691__ = (int) __i__;
          int __rightop681__ = __leftop682__ * __rightop691__;
          int __offsetinbits679__ = __leftop680__ + __rightop681__;
          // __offsetinbits679__ = 0 + 32 + 32 * 12 + 32 + 0 * i
          int __offset692__ = __offsetinbits679__ >> 3;
          int __left650__ = (__left651__ + __offset692__);
          // __left650__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits693__ <-- 32 + 0 + 32 * j
          int __leftop695__ = 32;
          int __rightop696__ = 0;
          int __leftop694__ = __leftop695__ + __rightop696__;
          int __leftop698__ = 32;
          int __rightop699__ = (int) __j__;
          int __rightop697__ = __leftop698__ * __rightop699__;
          int __offsetinbits693__ = __leftop694__ + __rightop697__;
          // __offsetinbits693__ = 32 + 0 + 32 * j
          int __offset700__ = __offsetinbits693__ >> 3;
          int __shift701__ = __offsetinbits693__ - (__offset700__ << 3);
          int __element649__ = ((*(int *)(__left650__ + __offset700__))  >> __shift701__) & 0xffffffff;
          __FileBlock___hash->add((int)__element649__, (int)__element649__);
        }
      }
    }
  }
}


// build rule8
{
  int __tempvar702__ = 0;
  // __left705__ <-- d.s
  // __left706__ <-- d
  int __left706__ = (int) d;
  // __left706__ = d
  int __left705__ = (__left706__ + 0);
  // __left705__ = d.s
  // __offsetinbits707__ <-- 32 + 32 + 0
  int __leftop708__ = 32;
  int __leftop710__ = 32;
  int __rightop711__ = 0;
  int __rightop709__ = __leftop710__ + __rightop711__;
  int __offsetinbits707__ = __leftop708__ + __rightop709__;
  // __offsetinbits707__ = 32 + 32 + 0
  int __offset712__ = __offsetinbits707__ >> 3;
  int __shift713__ = __offsetinbits707__ - (__offset712__ << 3);
  int __leftop704__ = ((*(int *)(__left705__ + __offset712__))  >> __shift713__) & 0xffffffff;
  int __rightop714__ = 1;
  int __tempvar703__ = __leftop704__ - __rightop714__;
  for (int __j__ = __tempvar702__; __j__ <= __tempvar703__; __j__++) {
    //!j in? __UsedBlock__
    int __element717__ = (int) __j__;
    int __leftop716__ = __UsedBlock___hash->contains(__element717__);
    int __tempvar715__ = !__leftop716__;
    if (__tempvar715__) {
      int __element718__ = (int) __j__;
      __FreeBlock___hash->add((int)__element718__, (int)__element718__);
    }
  }
}


// build rule10
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); ) {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar719__ = 0;
      int __tempvar720__ = 11;
      for (int __j__ = __tempvar719__; __j__ <= __tempvar720__; __j__++) {
        //!cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0
        // __left724__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left725__ <-- cast(__InodeTable__, d.b[itb])
        // __left727__ <-- d
        int __left727__ = (int) d;
        // __left727__ = d
        // __offsetinbits728__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop729__ = 0;
        int __leftop733__ = 8;
        // __left735__ <-- d.s
        // __left736__ <-- d
        int __left736__ = (int) d;
        // __left736__ = d
        int __left735__ = (__left736__ + 0);
        // __left735__ = d.s
        // __offsetinbits737__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop738__ = 32;
        int __leftop740__ = 32;
        int __leftop742__ = 32;
        int __leftop744__ = 32;
        int __leftop746__ = 32;
        int __rightop747__ = 0;
        int __rightop745__ = __leftop746__ + __rightop747__;
        int __rightop743__ = __leftop744__ + __rightop745__;
        int __rightop741__ = __leftop742__ + __rightop743__;
        int __rightop739__ = __leftop740__ + __rightop741__;
        int __offsetinbits737__ = __leftop738__ + __rightop739__;
        // __offsetinbits737__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset748__ = __offsetinbits737__ >> 3;
        int __shift749__ = __offsetinbits737__ - (__offset748__ << 3);
        int __rightop734__ = ((*(int *)(__left735__ + __offset748__))  >> __shift749__) & 0xffffffff;
        int __leftop732__ = __leftop733__ * __rightop734__;
        int __rightop750__ = 0;
        int __leftop731__ = __leftop732__ + __rightop750__;
        int __rightop751__ = (int) __itb__;
        int __rightop730__ = __leftop731__ * __rightop751__;
        int __offsetinbits728__ = __leftop729__ + __rightop730__;
        // __offsetinbits728__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset752__ = __offsetinbits728__ >> 3;
        int __expr726__ = (__left727__ + __offset752__);
        int __left725__ = (int) __expr726__;
        // __left725__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits753__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
        int __leftop754__ = 0;
        int __leftop757__ = 32;
        int __leftop760__ = 32;
        int __rightop761__ = 12;
        int __leftop759__ = __leftop760__ * __rightop761__;
        int __leftop763__ = 32;
        int __rightop764__ = 0;
        int __rightop762__ = __leftop763__ + __rightop764__;
        int __rightop758__ = __leftop759__ + __rightop762__;
        int __leftop756__ = __leftop757__ + __rightop758__;
        int __rightop765__ = (int) __i__;
        int __rightop755__ = __leftop756__ * __rightop765__;
        int __offsetinbits753__ = __leftop754__ + __rightop755__;
        // __offsetinbits753__ = 0 + 32 + 32 * 12 + 32 + 0 * i
        int __offset766__ = __offsetinbits753__ >> 3;
        int __left724__ = (__left725__ + __offset766__);
        // __left724__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits767__ <-- 32 + 0 + 32 * j
        int __leftop769__ = 32;
        int __rightop770__ = 0;
        int __leftop768__ = __leftop769__ + __rightop770__;
        int __leftop772__ = 32;
        int __rightop773__ = (int) __j__;
        int __rightop771__ = __leftop772__ * __rightop773__;
        int __offsetinbits767__ = __leftop768__ + __rightop771__;
        // __offsetinbits767__ = 32 + 0 + 32 * j
        int __offset774__ = __offsetinbits767__ >> 3;
        int __shift775__ = __offsetinbits767__ - (__offset774__ << 3);
        int __leftop723__ = ((*(int *)(__left724__ + __offset774__))  >> __shift775__) & 0xffffffff;
        int __rightop776__ = 0;
        int __leftop722__ = __leftop723__ == __rightop776__;
        int __tempvar721__ = !__leftop722__;
        if (__tempvar721__) {
          int __leftele777__ = (int) __i__;
          // __left779__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left780__ <-- cast(__InodeTable__, d.b[itb])
          // __left782__ <-- d
          int __left782__ = (int) d;
          // __left782__ = d
          // __offsetinbits783__ <-- 0 + 8 * d.s.blocksize + 0 * itb
          int __leftop784__ = 0;
          int __leftop788__ = 8;
          // __left790__ <-- d.s
          // __left791__ <-- d
          int __left791__ = (int) d;
          // __left791__ = d
          int __left790__ = (__left791__ + 0);
          // __left790__ = d.s
          // __offsetinbits792__ <-- 32 + 32 + 32 + 32 + 32 + 0
          int __leftop793__ = 32;
          int __leftop795__ = 32;
          int __leftop797__ = 32;
          int __leftop799__ = 32;
          int __leftop801__ = 32;
          int __rightop802__ = 0;
          int __rightop800__ = __leftop801__ + __rightop802__;
          int __rightop798__ = __leftop799__ + __rightop800__;
          int __rightop796__ = __leftop797__ + __rightop798__;
          int __rightop794__ = __leftop795__ + __rightop796__;
          int __offsetinbits792__ = __leftop793__ + __rightop794__;
          // __offsetinbits792__ = 32 + 32 + 32 + 32 + 32 + 0
          int __offset803__ = __offsetinbits792__ >> 3;
          int __shift804__ = __offsetinbits792__ - (__offset803__ << 3);
          int __rightop789__ = ((*(int *)(__left790__ + __offset803__))  >> __shift804__) & 0xffffffff;
          int __leftop787__ = __leftop788__ * __rightop789__;
          int __rightop805__ = 0;
          int __leftop786__ = __leftop787__ + __rightop805__;
          int __rightop806__ = (int) __itb__;
          int __rightop785__ = __leftop786__ * __rightop806__;
          int __offsetinbits783__ = __leftop784__ + __rightop785__;
          // __offsetinbits783__ = 0 + 8 * d.s.blocksize + 0 * itb
          int __offset807__ = __offsetinbits783__ >> 3;
          int __expr781__ = (__left782__ + __offset807__);
          int __left780__ = (int) __expr781__;
          // __left780__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits808__ <-- 0 + 32 + 32 * 12 + 32 + 0 * i
          int __leftop809__ = 0;
          int __leftop812__ = 32;
          int __leftop815__ = 32;
          int __rightop816__ = 12;
          int __leftop814__ = __leftop815__ * __rightop816__;
          int __leftop818__ = 32;
          int __rightop819__ = 0;
          int __rightop817__ = __leftop818__ + __rightop819__;
          int __rightop813__ = __leftop814__ + __rightop817__;
          int __leftop811__ = __leftop812__ + __rightop813__;
          int __rightop820__ = (int) __i__;
          int __rightop810__ = __leftop811__ * __rightop820__;
          int __offsetinbits808__ = __leftop809__ + __rightop810__;
          // __offsetinbits808__ = 0 + 32 + 32 * 12 + 32 + 0 * i
          int __offset821__ = __offsetinbits808__ >> 3;
          int __left779__ = (__left780__ + __offset821__);
          // __left779__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits822__ <-- 32 + 0 + 32 * j
          int __leftop824__ = 32;
          int __rightop825__ = 0;
          int __leftop823__ = __leftop824__ + __rightop825__;
          int __leftop827__ = 32;
          int __rightop828__ = (int) __j__;
          int __rightop826__ = __leftop827__ * __rightop828__;
          int __offsetinbits822__ = __leftop823__ + __rightop826__;
          // __offsetinbits822__ = 32 + 0 + 32 * j
          int __offset829__ = __offsetinbits822__ >> 3;
          int __shift830__ = __offsetinbits822__ - (__offset829__ << 3);
          int __rightele778__ = ((*(int *)(__left779__ + __offset829__))  >> __shift830__) & 0xffffffff;
          __contents___hash->add((int)__leftele777__, (int)__rightele778__);
          __contents___hashinv->add((int)__rightele778__, (int)__leftele777__);
        }
      }
    }
  }
}


// build rule7
{
  int __tempvar831__ = 0;
  // __left834__ <-- d.s
  // __left835__ <-- d
  int __left835__ = (int) d;
  // __left835__ = d
  int __left834__ = (__left835__ + 0);
  // __left834__ = d.s
  // __offsetinbits836__ <-- 32 + 32 + 32 + 0
  int __leftop837__ = 32;
  int __leftop839__ = 32;
  int __leftop841__ = 32;
  int __rightop842__ = 0;
  int __rightop840__ = __leftop841__ + __rightop842__;
  int __rightop838__ = __leftop839__ + __rightop840__;
  int __offsetinbits836__ = __leftop837__ + __rightop838__;
  // __offsetinbits836__ = 32 + 32 + 32 + 0
  int __offset843__ = __offsetinbits836__ >> 3;
  int __shift844__ = __offsetinbits836__ - (__offset843__ << 3);
  int __leftop833__ = ((*(int *)(__left834__ + __offset843__))  >> __shift844__) & 0xffffffff;
  int __rightop845__ = 1;
  int __tempvar832__ = __leftop833__ - __rightop845__;
  for (int __j__ = __tempvar831__; __j__ <= __tempvar832__; __j__++) {
    //!j in? __UsedInode__
    int __element848__ = (int) __j__;
    int __leftop847__ = __UsedInode___hash->contains(__element848__);
    int __tempvar846__ = !__leftop847__;
    if (__tempvar846__) {
      int __element849__ = (int) __j__;
      __FreeInode___hash->add((int)__element849__, (int)__element849__);
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
      int __tempvar850__ = 1;
      if (__tempvar850__) {
        int __leftele851__ = (int) __j__;
        // __left853__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left854__ <-- cast(__InodeTable__, d.b[itb])
        // __left856__ <-- d
        int __left856__ = (int) d;
        // __left856__ = d
        // __offsetinbits857__ <-- 0 + 8 * d.s.blocksize + 0 * itb
        int __leftop858__ = 0;
        int __leftop862__ = 8;
        // __left864__ <-- d.s
        // __left865__ <-- d
        int __left865__ = (int) d;
        // __left865__ = d
        int __left864__ = (__left865__ + 0);
        // __left864__ = d.s
        // __offsetinbits866__ <-- 32 + 32 + 32 + 32 + 32 + 0
        int __leftop867__ = 32;
        int __leftop869__ = 32;
        int __leftop871__ = 32;
        int __leftop873__ = 32;
        int __leftop875__ = 32;
        int __rightop876__ = 0;
        int __rightop874__ = __leftop875__ + __rightop876__;
        int __rightop872__ = __leftop873__ + __rightop874__;
        int __rightop870__ = __leftop871__ + __rightop872__;
        int __rightop868__ = __leftop869__ + __rightop870__;
        int __offsetinbits866__ = __leftop867__ + __rightop868__;
        // __offsetinbits866__ = 32 + 32 + 32 + 32 + 32 + 0
        int __offset877__ = __offsetinbits866__ >> 3;
        int __shift878__ = __offsetinbits866__ - (__offset877__ << 3);
        int __rightop863__ = ((*(int *)(__left864__ + __offset877__))  >> __shift878__) & 0xffffffff;
        int __leftop861__ = __leftop862__ * __rightop863__;
        int __rightop879__ = 0;
        int __leftop860__ = __leftop861__ + __rightop879__;
        int __rightop880__ = (int) __itb__;
        int __rightop859__ = __leftop860__ * __rightop880__;
        int __offsetinbits857__ = __leftop858__ + __rightop859__;
        // __offsetinbits857__ = 0 + 8 * d.s.blocksize + 0 * itb
        int __offset881__ = __offsetinbits857__ >> 3;
        int __expr855__ = (__left856__ + __offset881__);
        int __left854__ = (int) __expr855__;
        // __left854__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits882__ <-- 0 + 32 + 32 * 12 + 32 + 0 * j
        int __leftop883__ = 0;
        int __leftop886__ = 32;
        int __leftop889__ = 32;
        int __rightop890__ = 12;
        int __leftop888__ = __leftop889__ * __rightop890__;
        int __leftop892__ = 32;
        int __rightop893__ = 0;
        int __rightop891__ = __leftop892__ + __rightop893__;
        int __rightop887__ = __leftop888__ + __rightop891__;
        int __leftop885__ = __leftop886__ + __rightop887__;
        int __rightop894__ = (int) __j__;
        int __rightop884__ = __leftop885__ * __rightop894__;
        int __offsetinbits882__ = __leftop883__ + __rightop884__;
        // __offsetinbits882__ = 0 + 32 + 32 * 12 + 32 + 0 * j
        int __offset895__ = __offsetinbits882__ >> 3;
        int __left853__ = (__left854__ + __offset895__);
        // __left853__ = cast(__InodeTable__, d.b[itb]).itable[j]
        // __offsetinbits896__ <-- 32 * 12 + 32 + 0
        int __leftop898__ = 32;
        int __rightop899__ = 12;
        int __leftop897__ = __leftop898__ * __rightop899__;
        int __leftop901__ = 32;
        int __rightop902__ = 0;
        int __rightop900__ = __leftop901__ + __rightop902__;
        int __offsetinbits896__ = __leftop897__ + __rightop900__;
        // __offsetinbits896__ = 32 * 12 + 32 + 0
        int __offset903__ = __offsetinbits896__ >> 3;
        int __shift904__ = __offsetinbits896__ - (__offset903__ << 3);
        int __rightele852__ = ((*(int *)(__left853__ + __offset903__))  >> __shift904__) & 0xffffffff;
        __referencecount___hash->add((int)__leftele851__, (int)__rightele852__);
        __referencecount___hashinv->add((int)__rightele852__, (int)__leftele851__);
      }
    }
  }
}


// build rule13
{
  int __tempvar905__ = 0;
  // __left908__ <-- d.s
  // __left909__ <-- d
  int __left909__ = (int) d;
  // __left909__ = d
  int __left908__ = (__left909__ + 0);
  // __left908__ = d.s
  // __offsetinbits910__ <-- 32 + 32 + 32 + 0
  int __leftop911__ = 32;
  int __leftop913__ = 32;
  int __leftop915__ = 32;
  int __rightop916__ = 0;
  int __rightop914__ = __leftop915__ + __rightop916__;
  int __rightop912__ = __leftop913__ + __rightop914__;
  int __offsetinbits910__ = __leftop911__ + __rightop912__;
  // __offsetinbits910__ = 32 + 32 + 32 + 0
  int __offset917__ = __offsetinbits910__ >> 3;
  int __shift918__ = __offsetinbits910__ - (__offset917__ << 3);
  int __leftop907__ = ((*(int *)(__left908__ + __offset917__))  >> __shift918__) & 0xffffffff;
  int __rightop919__ = 1;
  int __tempvar906__ = __leftop907__ - __rightop919__;
  for (int __j__ = __tempvar905__; __j__ <= __tempvar906__; __j__++) {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); ) {
      int __ibb__ = (int) __ibb___iterator->next();
      //cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == true
      // __left922__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left924__ <-- d
      int __left924__ = (int) d;
      // __left924__ = d
      // __offsetinbits925__ <-- 0 + 8 * d.s.blocksize + 0 * ibb
      int __leftop926__ = 0;
      int __leftop930__ = 8;
      // __left932__ <-- d.s
      // __left933__ <-- d
      int __left933__ = (int) d;
      // __left933__ = d
      int __left932__ = (__left933__ + 0);
      // __left932__ = d.s
      // __offsetinbits934__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop935__ = 32;
      int __leftop937__ = 32;
      int __leftop939__ = 32;
      int __leftop941__ = 32;
      int __leftop943__ = 32;
      int __rightop944__ = 0;
      int __rightop942__ = __leftop943__ + __rightop944__;
      int __rightop940__ = __leftop941__ + __rightop942__;
      int __rightop938__ = __leftop939__ + __rightop940__;
      int __rightop936__ = __leftop937__ + __rightop938__;
      int __offsetinbits934__ = __leftop935__ + __rightop936__;
      // __offsetinbits934__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset945__ = __offsetinbits934__ >> 3;
      int __shift946__ = __offsetinbits934__ - (__offset945__ << 3);
      int __rightop931__ = ((*(int *)(__left932__ + __offset945__))  >> __shift946__) & 0xffffffff;
      int __leftop929__ = __leftop930__ * __rightop931__;
      int __rightop947__ = 0;
      int __leftop928__ = __leftop929__ + __rightop947__;
      int __rightop948__ = (int) __ibb__;
      int __rightop927__ = __leftop928__ * __rightop948__;
      int __offsetinbits925__ = __leftop926__ + __rightop927__;
      // __offsetinbits925__ = 0 + 8 * d.s.blocksize + 0 * ibb
      int __offset949__ = __offsetinbits925__ >> 3;
      int __expr923__ = (__left924__ + __offset949__);
      int __left922__ = (int) __expr923__;
      // __left922__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits950__ <-- 0 + 1 * j
      int __leftop951__ = 0;
      int __leftop953__ = 1;
      int __rightop954__ = (int) __j__;
      int __rightop952__ = __leftop953__ * __rightop954__;
      int __offsetinbits950__ = __leftop951__ + __rightop952__;
      // __offsetinbits950__ = 0 + 1 * j
      int __offset955__ = __offsetinbits950__ >> 3;
      int __shift956__ = __offsetinbits950__ - (__offset955__ << 3);
      int __leftop921__ = ((*(int *)(__left922__ + __offset955__))  >> __shift956__) & 0x1;
      int __rightop957__ = 1;
      int __tempvar920__ = __leftop921__ == __rightop957__;
      if (__tempvar920__) {
        int __leftele958__ = (int) __j__;
        int __rightele959__ = 100;
        __inodestatus___hash->add((int)__leftele958__, (int)__rightele959__);
        __inodestatus___hashinv->add((int)__rightele959__, (int)__leftele958__);
      }
    }
  }
}


// build rule12
{
  int __tempvar960__ = 0;
  // __left963__ <-- d.s
  // __left964__ <-- d
  int __left964__ = (int) d;
  // __left964__ = d
  int __left963__ = (__left964__ + 0);
  // __left963__ = d.s
  // __offsetinbits965__ <-- 32 + 32 + 32 + 0
  int __leftop966__ = 32;
  int __leftop968__ = 32;
  int __leftop970__ = 32;
  int __rightop971__ = 0;
  int __rightop969__ = __leftop970__ + __rightop971__;
  int __rightop967__ = __leftop968__ + __rightop969__;
  int __offsetinbits965__ = __leftop966__ + __rightop967__;
  // __offsetinbits965__ = 32 + 32 + 32 + 0
  int __offset972__ = __offsetinbits965__ >> 3;
  int __shift973__ = __offsetinbits965__ - (__offset972__ << 3);
  int __leftop962__ = ((*(int *)(__left963__ + __offset972__))  >> __shift973__) & 0xffffffff;
  int __rightop974__ = 1;
  int __tempvar961__ = __leftop962__ - __rightop974__;
  for (int __j__ = __tempvar960__; __j__ <= __tempvar961__; __j__++) {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); ) {
      int __ibb__ = (int) __ibb___iterator->next();
      //cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == false
      // __left977__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left979__ <-- d
      int __left979__ = (int) d;
      // __left979__ = d
      // __offsetinbits980__ <-- 0 + 8 * d.s.blocksize + 0 * ibb
      int __leftop981__ = 0;
      int __leftop985__ = 8;
      // __left987__ <-- d.s
      // __left988__ <-- d
      int __left988__ = (int) d;
      // __left988__ = d
      int __left987__ = (__left988__ + 0);
      // __left987__ = d.s
      // __offsetinbits989__ <-- 32 + 32 + 32 + 32 + 32 + 0
      int __leftop990__ = 32;
      int __leftop992__ = 32;
      int __leftop994__ = 32;
      int __leftop996__ = 32;
      int __leftop998__ = 32;
      int __rightop999__ = 0;
      int __rightop997__ = __leftop998__ + __rightop999__;
      int __rightop995__ = __leftop996__ + __rightop997__;
      int __rightop993__ = __leftop994__ + __rightop995__;
      int __rightop991__ = __leftop992__ + __rightop993__;
      int __offsetinbits989__ = __leftop990__ + __rightop991__;
      // __offsetinbits989__ = 32 + 32 + 32 + 32 + 32 + 0
      int __offset1000__ = __offsetinbits989__ >> 3;
      int __shift1001__ = __offsetinbits989__ - (__offset1000__ << 3);
      int __rightop986__ = ((*(int *)(__left987__ + __offset1000__))  >> __shift1001__) & 0xffffffff;
      int __leftop984__ = __leftop985__ * __rightop986__;
      int __rightop1002__ = 0;
      int __leftop983__ = __leftop984__ + __rightop1002__;
      int __rightop1003__ = (int) __ibb__;
      int __rightop982__ = __leftop983__ * __rightop1003__;
      int __offsetinbits980__ = __leftop981__ + __rightop982__;
      // __offsetinbits980__ = 0 + 8 * d.s.blocksize + 0 * ibb
      int __offset1004__ = __offsetinbits980__ >> 3;
      int __expr978__ = (__left979__ + __offset1004__);
      int __left977__ = (int) __expr978__;
      // __left977__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits1005__ <-- 0 + 1 * j
      int __leftop1006__ = 0;
      int __leftop1008__ = 1;
      int __rightop1009__ = (int) __j__;
      int __rightop1007__ = __leftop1008__ * __rightop1009__;
      int __offsetinbits1005__ = __leftop1006__ + __rightop1007__;
      // __offsetinbits1005__ = 0 + 1 * j
      int __offset1010__ = __offsetinbits1005__ >> 3;
      int __shift1011__ = __offsetinbits1005__ - (__offset1010__ << 3);
      int __leftop976__ = ((*(int *)(__left977__ + __offset1010__))  >> __shift1011__) & 0x1;
      int __rightop1012__ = 0;
      int __tempvar975__ = __leftop976__ == __rightop1012__;
      if (__tempvar975__) {
        int __leftele1013__ = (int) __j__;
        int __rightele1014__ = 101;
        __inodestatus___hash->add((int)__leftele1013__, (int)__rightele1014__);
        __inodestatus___hashinv->add((int)__rightele1014__, (int)__leftele1013__);
      }
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
        __filesize___hashinv->add((int)__rightele1017__, (int)__leftele1016__);
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
      //cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == false
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
      int __rightop1111__ = 0;
      int __tempvar1074__ = __leftop1075__ == __rightop1111__;
      if (__tempvar1074__) {
        int __leftele1112__ = (int) __j__;
        int __rightele1113__ = 101;
        __blockstatus___hash->add((int)__leftele1112__, (int)__rightele1113__);
        __blockstatus___hashinv->add((int)__rightele1113__, (int)__leftele1112__);
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
        int __rightele1166__ = 100;
        __blockstatus___hash->add((int)__leftele1165__, (int)__rightele1166__);
        __blockstatus___hashinv->add((int)__rightele1166__, (int)__leftele1165__);
      }
    }
  }
}




// checking c1
{
  for (SimpleIterator* __u___iterator = __UsedInode___hash->iterator(); __u___iterator->hasNext(); ) {
    int __u__ = (int) __u___iterator->next();
    int __relval1168__ = __inodestatus___hash->get(__u__);
    int __exprval1169__ = 100;
    int __constraintboolean1167__ = __relval1168__==__exprval1169__;
    if (!__constraintboolean1167__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c2
{
  for (SimpleIterator* __f___iterator = __FreeInode___hash->iterator(); __f___iterator->hasNext(); ) {
    int __f__ = (int) __f___iterator->next();
    int __relval1171__ = __inodestatus___hash->get(__f__);
    int __exprval1172__ = 101;
    int __constraintboolean1170__ = __relval1171__==__exprval1172__;
    if (!__constraintboolean1170__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c3
{
  for (SimpleIterator* __u___iterator = __UsedBlock___hash->iterator(); __u___iterator->hasNext(); ) {
    int __u__ = (int) __u___iterator->next();
    int __relval1174__ = __blockstatus___hash->get(__u__);
    int __exprval1175__ = 100;
    int __constraintboolean1173__ = __relval1174__==__exprval1175__;
    if (!__constraintboolean1173__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c4
{
  for (SimpleIterator* __f___iterator = __FreeBlock___hash->iterator(); __f___iterator->hasNext(); ) {
    int __f__ = (int) __f___iterator->next();
    int __relval1177__ = __blockstatus___hash->get(__f__);
    int __exprval1178__ = 101;
    int __constraintboolean1176__ = __relval1177__==__exprval1178__;
    if (!__constraintboolean1176__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c5
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    int __relval1180__ = __referencecount___hash->get(__i__);
    int __exprval1181__ = __inodeof___hashinv->count(__i__);
    int __constraintboolean1179__ = __relval1180__==__exprval1181__;
    if (!__constraintboolean1179__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c6
{
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); ) {
    int __i__ = (int) __i___iterator->next();
    int __relval1183__ = __filesize___hash->get(__i__);
    int __leftop1185__ = __contents___hash->count(__i__);
    int __rightop1186__ = 8192;
    int __exprval1184__ = __leftop1185__ * __rightop1186__;
    int __constraintboolean1182__ = __relval1183__<=__exprval1184__;
    if (!__constraintboolean1182__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c7
{
  for (SimpleIterator* __b___iterator = __FileDirectoryBlock___hash->iterator(); __b___iterator->hasNext(); ) {
    int __b__ = (int) __b___iterator->next();
    int __size1188__ = __contents___hashinv->count(__b__);
    int __constraintboolean1187__ = __size1188__==1;
    if (!__constraintboolean1187__) {
      __Success = 0;
      printf("fail. ");
    }
  }
}


// checking c8
{
  int __size1190__ = __SuperBlock___hash->count();
  int __constraintboolean1189__ = __size1190__==1;
  if (!__constraintboolean1189__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c9
{
  int __size1192__ = __GroupBlock___hash->count();
  int __constraintboolean1191__ = __size1192__==1;
  if (!__constraintboolean1191__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c10
{
  int __size1194__ = __InodeTableBlock___hash->count();
  int __constraintboolean1193__ = __size1194__==1;
  if (!__constraintboolean1193__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c11
{
  int __size1196__ = __InodeBitmapBlock___hash->count();
  int __constraintboolean1195__ = __size1196__==1;
  if (!__constraintboolean1195__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c12
{
  int __size1198__ = __BlockBitmapBlock___hash->count();
  int __constraintboolean1197__ = __size1198__==1;
  if (!__constraintboolean1197__) {
    __Success = 0;
    printf("fail. ");
  }
}


// checking c13
{
  int __size1200__ = __RootDirectoryInode___hash->count();
  int __constraintboolean1199__ = __size1200__==1;
  if (!__constraintboolean1199__) {
    __Success = 0;
    printf("fail. ");
  }
}


if (__Success) { printf("all tests passed"); }
