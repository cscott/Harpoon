
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
    int __addeditem2__ = 1;
    __addeditem2__ = __SuperBlock___hash->add((int)__element1__, (int)__element1__);
    }
  }


// build rule2
  {
  //true
  int __tempvar3__ = 1;
  if (__tempvar3__)
    {
    int __element4__ = 1;
    int __addeditem5__ = 1;
    __addeditem5__ = __GroupBlock___hash->add((int)__element4__, (int)__element4__);
    }
  }


// build rule3
  {
  //(d.g.InodeTableBlock < d.s.NumberofBlocks)
  // __left8__ <-- d.g
  // __left9__ <-- d
  int __left9__ = (int) d; //varexpr
  // __left9__ = d
  // __offsetinbits10__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop11__ = 0;
  int __leftop15__ = 8;
  // __left17__ <-- d.s
  // __left18__ <-- d
  int __left18__ = (int) d; //varexpr
  // __left18__ = d
  int __left17__ = (__left18__ + 0);
  int __leftop20__ = 32;
  int __leftop22__ = 32;
  int __leftop24__ = 32;
  int __leftop26__ = 32;
  int __leftop28__ = 32;
  int __leftop30__ = 32;
  int __rightop31__ = 0;
  int __rightop29__ = __leftop30__ + __rightop31__;
  int __rightop27__ = __leftop28__ + __rightop29__;
  int __rightop25__ = __leftop26__ + __rightop27__;
  int __rightop23__ = __leftop24__ + __rightop25__;
  int __rightop21__ = __leftop22__ + __rightop23__;
  int __sizeof19__ = __leftop20__ + __rightop21__;
  int __high32__ = __left17__ + __sizeof19__;
  assertvalidmemory(__left17__, __high32__);
  // __left17__ = d.s
  // __offsetinbits33__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop34__ = 32;
  int __leftop36__ = 32;
  int __leftop38__ = 32;
  int __leftop40__ = 32;
  int __leftop42__ = 32;
  int __rightop43__ = 0;
  int __rightop41__ = __leftop42__ + __rightop43__;
  int __rightop39__ = __leftop40__ + __rightop41__;
  int __rightop37__ = __leftop38__ + __rightop39__;
  int __rightop35__ = __leftop36__ + __rightop37__;
  int __offsetinbits33__ = __leftop34__ + __rightop35__;
  // __offsetinbits33__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset44__ = __offsetinbits33__ >> 3;
  int __shift45__ = __offsetinbits33__ - (__offset44__ << 3);
  int __rightop16__ = ((*(int *)(__left17__ + __offset44__))  >> __shift45__) & 0xffffffff;
  int __leftop14__ = __leftop15__ * __rightop16__;
  int __rightop46__ = 0;
  int __leftop13__ = __leftop14__ + __rightop46__;
  int __rightop47__ = 1;
  int __rightop12__ = __leftop13__ * __rightop47__;
  int __offsetinbits10__ = __leftop11__ + __rightop12__;
  // __offsetinbits10__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset48__ = __offsetinbits10__ >> 3;
  int __left8__ = (__left9__ + __offset48__);
  int __leftop50__ = 32;
  int __leftop52__ = 32;
  int __leftop54__ = 32;
  int __leftop56__ = 32;
  int __leftop58__ = 32;
  int __rightop59__ = 0;
  int __rightop57__ = __leftop58__ + __rightop59__;
  int __rightop55__ = __leftop56__ + __rightop57__;
  int __rightop53__ = __leftop54__ + __rightop55__;
  int __rightop51__ = __leftop52__ + __rightop53__;
  int __sizeof49__ = __leftop50__ + __rightop51__;
  int __high60__ = __left8__ + __sizeof49__;
  assertvalidmemory(__left8__, __high60__);
  // __left8__ = d.g
  // __offsetinbits61__ <-- (32 + (32 + 0))
  int __leftop62__ = 32;
  int __leftop64__ = 32;
  int __rightop65__ = 0;
  int __rightop63__ = __leftop64__ + __rightop65__;
  int __offsetinbits61__ = __leftop62__ + __rightop63__;
  // __offsetinbits61__ = (32 + (32 + 0))
  int __offset66__ = __offsetinbits61__ >> 3;
  int __shift67__ = __offsetinbits61__ - (__offset66__ << 3);
  int __leftop7__ = ((*(int *)(__left8__ + __offset66__))  >> __shift67__) & 0xffffffff;
  // __left69__ <-- d.s
  // __left70__ <-- d
  int __left70__ = (int) d; //varexpr
  // __left70__ = d
  int __left69__ = (__left70__ + 0);
  int __leftop72__ = 32;
  int __leftop74__ = 32;
  int __leftop76__ = 32;
  int __leftop78__ = 32;
  int __leftop80__ = 32;
  int __leftop82__ = 32;
  int __rightop83__ = 0;
  int __rightop81__ = __leftop82__ + __rightop83__;
  int __rightop79__ = __leftop80__ + __rightop81__;
  int __rightop77__ = __leftop78__ + __rightop79__;
  int __rightop75__ = __leftop76__ + __rightop77__;
  int __rightop73__ = __leftop74__ + __rightop75__;
  int __sizeof71__ = __leftop72__ + __rightop73__;
  int __high84__ = __left69__ + __sizeof71__;
  assertvalidmemory(__left69__, __high84__);
  // __left69__ = d.s
  // __offsetinbits85__ <-- (32 + (32 + 0))
  int __leftop86__ = 32;
  int __leftop88__ = 32;
  int __rightop89__ = 0;
  int __rightop87__ = __leftop88__ + __rightop89__;
  int __offsetinbits85__ = __leftop86__ + __rightop87__;
  // __offsetinbits85__ = (32 + (32 + 0))
  int __offset90__ = __offsetinbits85__ >> 3;
  int __shift91__ = __offsetinbits85__ - (__offset90__ << 3);
  int __rightop68__ = ((*(int *)(__left69__ + __offset90__))  >> __shift91__) & 0xffffffff;
  int __tempvar6__ = __leftop7__ < __rightop68__;
  if (__tempvar6__)
    {
    // __left93__ <-- d.g
    // __left94__ <-- d
    int __left94__ = (int) d; //varexpr
    // __left94__ = d
    // __offsetinbits95__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop96__ = 0;
    int __leftop100__ = 8;
    // __left102__ <-- d.s
    // __left103__ <-- d
    int __left103__ = (int) d; //varexpr
    // __left103__ = d
    int __left102__ = (__left103__ + 0);
    int __leftop105__ = 32;
    int __leftop107__ = 32;
    int __leftop109__ = 32;
    int __leftop111__ = 32;
    int __leftop113__ = 32;
    int __leftop115__ = 32;
    int __rightop116__ = 0;
    int __rightop114__ = __leftop115__ + __rightop116__;
    int __rightop112__ = __leftop113__ + __rightop114__;
    int __rightop110__ = __leftop111__ + __rightop112__;
    int __rightop108__ = __leftop109__ + __rightop110__;
    int __rightop106__ = __leftop107__ + __rightop108__;
    int __sizeof104__ = __leftop105__ + __rightop106__;
    int __high117__ = __left102__ + __sizeof104__;
    assertvalidmemory(__left102__, __high117__);
    // __left102__ = d.s
    // __offsetinbits118__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop119__ = 32;
    int __leftop121__ = 32;
    int __leftop123__ = 32;
    int __leftop125__ = 32;
    int __leftop127__ = 32;
    int __rightop128__ = 0;
    int __rightop126__ = __leftop127__ + __rightop128__;
    int __rightop124__ = __leftop125__ + __rightop126__;
    int __rightop122__ = __leftop123__ + __rightop124__;
    int __rightop120__ = __leftop121__ + __rightop122__;
    int __offsetinbits118__ = __leftop119__ + __rightop120__;
    // __offsetinbits118__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset129__ = __offsetinbits118__ >> 3;
    int __shift130__ = __offsetinbits118__ - (__offset129__ << 3);
    int __rightop101__ = ((*(int *)(__left102__ + __offset129__))  >> __shift130__) & 0xffffffff;
    int __leftop99__ = __leftop100__ * __rightop101__;
    int __rightop131__ = 0;
    int __leftop98__ = __leftop99__ + __rightop131__;
    int __rightop132__ = 1;
    int __rightop97__ = __leftop98__ * __rightop132__;
    int __offsetinbits95__ = __leftop96__ + __rightop97__;
    // __offsetinbits95__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset133__ = __offsetinbits95__ >> 3;
    int __left93__ = (__left94__ + __offset133__);
    int __leftop135__ = 32;
    int __leftop137__ = 32;
    int __leftop139__ = 32;
    int __leftop141__ = 32;
    int __leftop143__ = 32;
    int __rightop144__ = 0;
    int __rightop142__ = __leftop143__ + __rightop144__;
    int __rightop140__ = __leftop141__ + __rightop142__;
    int __rightop138__ = __leftop139__ + __rightop140__;
    int __rightop136__ = __leftop137__ + __rightop138__;
    int __sizeof134__ = __leftop135__ + __rightop136__;
    int __high145__ = __left93__ + __sizeof134__;
    assertvalidmemory(__left93__, __high145__);
    // __left93__ = d.g
    // __offsetinbits146__ <-- (32 + (32 + 0))
    int __leftop147__ = 32;
    int __leftop149__ = 32;
    int __rightop150__ = 0;
    int __rightop148__ = __leftop149__ + __rightop150__;
    int __offsetinbits146__ = __leftop147__ + __rightop148__;
    // __offsetinbits146__ = (32 + (32 + 0))
    int __offset151__ = __offsetinbits146__ >> 3;
    int __shift152__ = __offsetinbits146__ - (__offset151__ << 3);
    int __element92__ = ((*(int *)(__left93__ + __offset151__))  >> __shift152__) & 0xffffffff;
    int __addeditem153__ = 1;
    __addeditem153__ = __InodeTableBlock___hash->add((int)__element92__, (int)__element92__);
    }
  }


// build rule4
  {
  //(d.g.InodeBitmapBlock < d.s.NumberofBlocks)
  // __left156__ <-- d.g
  // __left157__ <-- d
  int __left157__ = (int) d; //varexpr
  // __left157__ = d
  // __offsetinbits158__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop159__ = 0;
  int __leftop163__ = 8;
  // __left165__ <-- d.s
  // __left166__ <-- d
  int __left166__ = (int) d; //varexpr
  // __left166__ = d
  int __left165__ = (__left166__ + 0);
  int __leftop168__ = 32;
  int __leftop170__ = 32;
  int __leftop172__ = 32;
  int __leftop174__ = 32;
  int __leftop176__ = 32;
  int __leftop178__ = 32;
  int __rightop179__ = 0;
  int __rightop177__ = __leftop178__ + __rightop179__;
  int __rightop175__ = __leftop176__ + __rightop177__;
  int __rightop173__ = __leftop174__ + __rightop175__;
  int __rightop171__ = __leftop172__ + __rightop173__;
  int __rightop169__ = __leftop170__ + __rightop171__;
  int __sizeof167__ = __leftop168__ + __rightop169__;
  int __high180__ = __left165__ + __sizeof167__;
  assertvalidmemory(__left165__, __high180__);
  // __left165__ = d.s
  // __offsetinbits181__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop182__ = 32;
  int __leftop184__ = 32;
  int __leftop186__ = 32;
  int __leftop188__ = 32;
  int __leftop190__ = 32;
  int __rightop191__ = 0;
  int __rightop189__ = __leftop190__ + __rightop191__;
  int __rightop187__ = __leftop188__ + __rightop189__;
  int __rightop185__ = __leftop186__ + __rightop187__;
  int __rightop183__ = __leftop184__ + __rightop185__;
  int __offsetinbits181__ = __leftop182__ + __rightop183__;
  // __offsetinbits181__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset192__ = __offsetinbits181__ >> 3;
  int __shift193__ = __offsetinbits181__ - (__offset192__ << 3);
  int __rightop164__ = ((*(int *)(__left165__ + __offset192__))  >> __shift193__) & 0xffffffff;
  int __leftop162__ = __leftop163__ * __rightop164__;
  int __rightop194__ = 0;
  int __leftop161__ = __leftop162__ + __rightop194__;
  int __rightop195__ = 1;
  int __rightop160__ = __leftop161__ * __rightop195__;
  int __offsetinbits158__ = __leftop159__ + __rightop160__;
  // __offsetinbits158__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset196__ = __offsetinbits158__ >> 3;
  int __left156__ = (__left157__ + __offset196__);
  int __leftop198__ = 32;
  int __leftop200__ = 32;
  int __leftop202__ = 32;
  int __leftop204__ = 32;
  int __leftop206__ = 32;
  int __rightop207__ = 0;
  int __rightop205__ = __leftop206__ + __rightop207__;
  int __rightop203__ = __leftop204__ + __rightop205__;
  int __rightop201__ = __leftop202__ + __rightop203__;
  int __rightop199__ = __leftop200__ + __rightop201__;
  int __sizeof197__ = __leftop198__ + __rightop199__;
  int __high208__ = __left156__ + __sizeof197__;
  assertvalidmemory(__left156__, __high208__);
  // __left156__ = d.g
  // __offsetinbits209__ <-- (32 + 0)
  int __leftop210__ = 32;
  int __rightop211__ = 0;
  int __offsetinbits209__ = __leftop210__ + __rightop211__;
  // __offsetinbits209__ = (32 + 0)
  int __offset212__ = __offsetinbits209__ >> 3;
  int __shift213__ = __offsetinbits209__ - (__offset212__ << 3);
  int __leftop155__ = ((*(int *)(__left156__ + __offset212__))  >> __shift213__) & 0xffffffff;
  // __left215__ <-- d.s
  // __left216__ <-- d
  int __left216__ = (int) d; //varexpr
  // __left216__ = d
  int __left215__ = (__left216__ + 0);
  int __leftop218__ = 32;
  int __leftop220__ = 32;
  int __leftop222__ = 32;
  int __leftop224__ = 32;
  int __leftop226__ = 32;
  int __leftop228__ = 32;
  int __rightop229__ = 0;
  int __rightop227__ = __leftop228__ + __rightop229__;
  int __rightop225__ = __leftop226__ + __rightop227__;
  int __rightop223__ = __leftop224__ + __rightop225__;
  int __rightop221__ = __leftop222__ + __rightop223__;
  int __rightop219__ = __leftop220__ + __rightop221__;
  int __sizeof217__ = __leftop218__ + __rightop219__;
  int __high230__ = __left215__ + __sizeof217__;
  assertvalidmemory(__left215__, __high230__);
  // __left215__ = d.s
  // __offsetinbits231__ <-- (32 + (32 + 0))
  int __leftop232__ = 32;
  int __leftop234__ = 32;
  int __rightop235__ = 0;
  int __rightop233__ = __leftop234__ + __rightop235__;
  int __offsetinbits231__ = __leftop232__ + __rightop233__;
  // __offsetinbits231__ = (32 + (32 + 0))
  int __offset236__ = __offsetinbits231__ >> 3;
  int __shift237__ = __offsetinbits231__ - (__offset236__ << 3);
  int __rightop214__ = ((*(int *)(__left215__ + __offset236__))  >> __shift237__) & 0xffffffff;
  int __tempvar154__ = __leftop155__ < __rightop214__;
  if (__tempvar154__)
    {
    // __left239__ <-- d.g
    // __left240__ <-- d
    int __left240__ = (int) d; //varexpr
    // __left240__ = d
    // __offsetinbits241__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop242__ = 0;
    int __leftop246__ = 8;
    // __left248__ <-- d.s
    // __left249__ <-- d
    int __left249__ = (int) d; //varexpr
    // __left249__ = d
    int __left248__ = (__left249__ + 0);
    int __leftop251__ = 32;
    int __leftop253__ = 32;
    int __leftop255__ = 32;
    int __leftop257__ = 32;
    int __leftop259__ = 32;
    int __leftop261__ = 32;
    int __rightop262__ = 0;
    int __rightop260__ = __leftop261__ + __rightop262__;
    int __rightop258__ = __leftop259__ + __rightop260__;
    int __rightop256__ = __leftop257__ + __rightop258__;
    int __rightop254__ = __leftop255__ + __rightop256__;
    int __rightop252__ = __leftop253__ + __rightop254__;
    int __sizeof250__ = __leftop251__ + __rightop252__;
    int __high263__ = __left248__ + __sizeof250__;
    assertvalidmemory(__left248__, __high263__);
    // __left248__ = d.s
    // __offsetinbits264__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop265__ = 32;
    int __leftop267__ = 32;
    int __leftop269__ = 32;
    int __leftop271__ = 32;
    int __leftop273__ = 32;
    int __rightop274__ = 0;
    int __rightop272__ = __leftop273__ + __rightop274__;
    int __rightop270__ = __leftop271__ + __rightop272__;
    int __rightop268__ = __leftop269__ + __rightop270__;
    int __rightop266__ = __leftop267__ + __rightop268__;
    int __offsetinbits264__ = __leftop265__ + __rightop266__;
    // __offsetinbits264__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset275__ = __offsetinbits264__ >> 3;
    int __shift276__ = __offsetinbits264__ - (__offset275__ << 3);
    int __rightop247__ = ((*(int *)(__left248__ + __offset275__))  >> __shift276__) & 0xffffffff;
    int __leftop245__ = __leftop246__ * __rightop247__;
    int __rightop277__ = 0;
    int __leftop244__ = __leftop245__ + __rightop277__;
    int __rightop278__ = 1;
    int __rightop243__ = __leftop244__ * __rightop278__;
    int __offsetinbits241__ = __leftop242__ + __rightop243__;
    // __offsetinbits241__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset279__ = __offsetinbits241__ >> 3;
    int __left239__ = (__left240__ + __offset279__);
    int __leftop281__ = 32;
    int __leftop283__ = 32;
    int __leftop285__ = 32;
    int __leftop287__ = 32;
    int __leftop289__ = 32;
    int __rightop290__ = 0;
    int __rightop288__ = __leftop289__ + __rightop290__;
    int __rightop286__ = __leftop287__ + __rightop288__;
    int __rightop284__ = __leftop285__ + __rightop286__;
    int __rightop282__ = __leftop283__ + __rightop284__;
    int __sizeof280__ = __leftop281__ + __rightop282__;
    int __high291__ = __left239__ + __sizeof280__;
    assertvalidmemory(__left239__, __high291__);
    // __left239__ = d.g
    // __offsetinbits292__ <-- (32 + 0)
    int __leftop293__ = 32;
    int __rightop294__ = 0;
    int __offsetinbits292__ = __leftop293__ + __rightop294__;
    // __offsetinbits292__ = (32 + 0)
    int __offset295__ = __offsetinbits292__ >> 3;
    int __shift296__ = __offsetinbits292__ - (__offset295__ << 3);
    int __element238__ = ((*(int *)(__left239__ + __offset295__))  >> __shift296__) & 0xffffffff;
    int __addeditem297__ = 1;
    __addeditem297__ = __InodeBitmapBlock___hash->add((int)__element238__, (int)__element238__);
    }
  }


// build rule12
  {
  int __tempvar298__ = 0;
  // __left301__ <-- d.s
  // __left302__ <-- d
  int __left302__ = (int) d; //varexpr
  // __left302__ = d
  int __left301__ = (__left302__ + 0);
  int __leftop304__ = 32;
  int __leftop306__ = 32;
  int __leftop308__ = 32;
  int __leftop310__ = 32;
  int __leftop312__ = 32;
  int __leftop314__ = 32;
  int __rightop315__ = 0;
  int __rightop313__ = __leftop314__ + __rightop315__;
  int __rightop311__ = __leftop312__ + __rightop313__;
  int __rightop309__ = __leftop310__ + __rightop311__;
  int __rightop307__ = __leftop308__ + __rightop309__;
  int __rightop305__ = __leftop306__ + __rightop307__;
  int __sizeof303__ = __leftop304__ + __rightop305__;
  int __high316__ = __left301__ + __sizeof303__;
  assertvalidmemory(__left301__, __high316__);
  // __left301__ = d.s
  // __offsetinbits317__ <-- (32 + (32 + (32 + 0)))
  int __leftop318__ = 32;
  int __leftop320__ = 32;
  int __leftop322__ = 32;
  int __rightop323__ = 0;
  int __rightop321__ = __leftop322__ + __rightop323__;
  int __rightop319__ = __leftop320__ + __rightop321__;
  int __offsetinbits317__ = __leftop318__ + __rightop319__;
  // __offsetinbits317__ = (32 + (32 + (32 + 0)))
  int __offset324__ = __offsetinbits317__ >> 3;
  int __shift325__ = __offsetinbits317__ - (__offset324__ << 3);
  int __leftop300__ = ((*(int *)(__left301__ + __offset324__))  >> __shift325__) & 0xffffffff;
  int __rightop326__ = 1;
  int __tempvar299__ = __leftop300__ - __rightop326__;
  for (int __j__ = __tempvar298__; __j__ <= __tempvar299__; __j__++)
    {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); )
      {
      int __ibb__ = (int) __ibb___iterator->next();
      //(cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == false)
      // __left329__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left331__ <-- d
      int __left331__ = (int) d; //varexpr
      // __left331__ = d
      // __offsetinbits332__ <-- (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __leftop333__ = 0;
      int __leftop337__ = 8;
      // __left339__ <-- d.s
      // __left340__ <-- d
      int __left340__ = (int) d; //varexpr
      // __left340__ = d
      int __left339__ = (__left340__ + 0);
      int __leftop342__ = 32;
      int __leftop344__ = 32;
      int __leftop346__ = 32;
      int __leftop348__ = 32;
      int __leftop350__ = 32;
      int __leftop352__ = 32;
      int __rightop353__ = 0;
      int __rightop351__ = __leftop352__ + __rightop353__;
      int __rightop349__ = __leftop350__ + __rightop351__;
      int __rightop347__ = __leftop348__ + __rightop349__;
      int __rightop345__ = __leftop346__ + __rightop347__;
      int __rightop343__ = __leftop344__ + __rightop345__;
      int __sizeof341__ = __leftop342__ + __rightop343__;
      int __high354__ = __left339__ + __sizeof341__;
      assertvalidmemory(__left339__, __high354__);
      // __left339__ = d.s
      // __offsetinbits355__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop356__ = 32;
      int __leftop358__ = 32;
      int __leftop360__ = 32;
      int __leftop362__ = 32;
      int __leftop364__ = 32;
      int __rightop365__ = 0;
      int __rightop363__ = __leftop364__ + __rightop365__;
      int __rightop361__ = __leftop362__ + __rightop363__;
      int __rightop359__ = __leftop360__ + __rightop361__;
      int __rightop357__ = __leftop358__ + __rightop359__;
      int __offsetinbits355__ = __leftop356__ + __rightop357__;
      // __offsetinbits355__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset366__ = __offsetinbits355__ >> 3;
      int __shift367__ = __offsetinbits355__ - (__offset366__ << 3);
      int __rightop338__ = ((*(int *)(__left339__ + __offset366__))  >> __shift367__) & 0xffffffff;
      int __leftop336__ = __leftop337__ * __rightop338__;
      int __rightop368__ = 0;
      int __leftop335__ = __leftop336__ + __rightop368__;
      int __rightop369__ = (int) __ibb__; //varexpr
      int __rightop334__ = __leftop335__ * __rightop369__;
      int __offsetinbits332__ = __leftop333__ + __rightop334__;
      // __offsetinbits332__ = (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __offset370__ = __offsetinbits332__ >> 3;
      int __expr330__ = (__left331__ + __offset370__);
      int __leftop373__ = 8;
      // __left375__ <-- d.s
      // __left376__ <-- d
      int __left376__ = (int) d; //varexpr
      // __left376__ = d
      int __left375__ = (__left376__ + 0);
      int __leftop378__ = 32;
      int __leftop380__ = 32;
      int __leftop382__ = 32;
      int __leftop384__ = 32;
      int __leftop386__ = 32;
      int __leftop388__ = 32;
      int __rightop389__ = 0;
      int __rightop387__ = __leftop388__ + __rightop389__;
      int __rightop385__ = __leftop386__ + __rightop387__;
      int __rightop383__ = __leftop384__ + __rightop385__;
      int __rightop381__ = __leftop382__ + __rightop383__;
      int __rightop379__ = __leftop380__ + __rightop381__;
      int __sizeof377__ = __leftop378__ + __rightop379__;
      int __high390__ = __left375__ + __sizeof377__;
      assertvalidmemory(__left375__, __high390__);
      // __left375__ = d.s
      // __offsetinbits391__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop392__ = 32;
      int __leftop394__ = 32;
      int __leftop396__ = 32;
      int __leftop398__ = 32;
      int __leftop400__ = 32;
      int __rightop401__ = 0;
      int __rightop399__ = __leftop400__ + __rightop401__;
      int __rightop397__ = __leftop398__ + __rightop399__;
      int __rightop395__ = __leftop396__ + __rightop397__;
      int __rightop393__ = __leftop394__ + __rightop395__;
      int __offsetinbits391__ = __leftop392__ + __rightop393__;
      // __offsetinbits391__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset402__ = __offsetinbits391__ >> 3;
      int __shift403__ = __offsetinbits391__ - (__offset402__ << 3);
      int __rightop374__ = ((*(int *)(__left375__ + __offset402__))  >> __shift403__) & 0xffffffff;
      int __leftop372__ = __leftop373__ * __rightop374__;
      int __rightop404__ = 0;
      int __sizeof371__ = __leftop372__ + __rightop404__;
      int __high405__ = __expr330__ + __sizeof371__;
      assertvalidmemory(__expr330__, __high405__);
      int __left329__ = (int) __expr330__;
      // __left329__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits406__ <-- (0 + (1 * j))
      int __leftop407__ = 0;
      int __leftop409__ = 1;
      int __rightop410__ = (int) __j__; //varexpr
      int __rightop408__ = __leftop409__ * __rightop410__;
      int __offsetinbits406__ = __leftop407__ + __rightop408__;
      // __offsetinbits406__ = (0 + (1 * j))
      int __offset411__ = __offsetinbits406__ >> 3;
      int __shift412__ = __offsetinbits406__ - (__offset411__ << 3);
      int __leftop328__ = ((*(int *)(__left329__ + __offset411__))  >> __shift412__) & 0x1;
      int __rightop413__ = 0;
      int __tempvar327__ = __leftop328__ == __rightop413__;
      if (__tempvar327__)
        {
        int __leftele414__ = (int) __j__; //varexpr
        int __rightele415__ = 101;
        int __addeditem417__;
        __addeditem417__ = __inodestatus___hash->add((int)__leftele414__, (int)__rightele415__);
        }
      }
    }
  }


// build rule5
  {
  //(d.g.BlockBitmapBlock < d.s.NumberofBlocks)
  // __left420__ <-- d.g
  // __left421__ <-- d
  int __left421__ = (int) d; //varexpr
  // __left421__ = d
  // __offsetinbits422__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop423__ = 0;
  int __leftop427__ = 8;
  // __left429__ <-- d.s
  // __left430__ <-- d
  int __left430__ = (int) d; //varexpr
  // __left430__ = d
  int __left429__ = (__left430__ + 0);
  int __leftop432__ = 32;
  int __leftop434__ = 32;
  int __leftop436__ = 32;
  int __leftop438__ = 32;
  int __leftop440__ = 32;
  int __leftop442__ = 32;
  int __rightop443__ = 0;
  int __rightop441__ = __leftop442__ + __rightop443__;
  int __rightop439__ = __leftop440__ + __rightop441__;
  int __rightop437__ = __leftop438__ + __rightop439__;
  int __rightop435__ = __leftop436__ + __rightop437__;
  int __rightop433__ = __leftop434__ + __rightop435__;
  int __sizeof431__ = __leftop432__ + __rightop433__;
  int __high444__ = __left429__ + __sizeof431__;
  assertvalidmemory(__left429__, __high444__);
  // __left429__ = d.s
  // __offsetinbits445__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop446__ = 32;
  int __leftop448__ = 32;
  int __leftop450__ = 32;
  int __leftop452__ = 32;
  int __leftop454__ = 32;
  int __rightop455__ = 0;
  int __rightop453__ = __leftop454__ + __rightop455__;
  int __rightop451__ = __leftop452__ + __rightop453__;
  int __rightop449__ = __leftop450__ + __rightop451__;
  int __rightop447__ = __leftop448__ + __rightop449__;
  int __offsetinbits445__ = __leftop446__ + __rightop447__;
  // __offsetinbits445__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset456__ = __offsetinbits445__ >> 3;
  int __shift457__ = __offsetinbits445__ - (__offset456__ << 3);
  int __rightop428__ = ((*(int *)(__left429__ + __offset456__))  >> __shift457__) & 0xffffffff;
  int __leftop426__ = __leftop427__ * __rightop428__;
  int __rightop458__ = 0;
  int __leftop425__ = __leftop426__ + __rightop458__;
  int __rightop459__ = 1;
  int __rightop424__ = __leftop425__ * __rightop459__;
  int __offsetinbits422__ = __leftop423__ + __rightop424__;
  // __offsetinbits422__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset460__ = __offsetinbits422__ >> 3;
  int __left420__ = (__left421__ + __offset460__);
  int __leftop462__ = 32;
  int __leftop464__ = 32;
  int __leftop466__ = 32;
  int __leftop468__ = 32;
  int __leftop470__ = 32;
  int __rightop471__ = 0;
  int __rightop469__ = __leftop470__ + __rightop471__;
  int __rightop467__ = __leftop468__ + __rightop469__;
  int __rightop465__ = __leftop466__ + __rightop467__;
  int __rightop463__ = __leftop464__ + __rightop465__;
  int __sizeof461__ = __leftop462__ + __rightop463__;
  int __high472__ = __left420__ + __sizeof461__;
  assertvalidmemory(__left420__, __high472__);
  // __left420__ = d.g
  int __leftop419__ = ((*(int *)(__left420__ + 0))  >> 0) & 0xffffffff;
  // __left474__ <-- d.s
  // __left475__ <-- d
  int __left475__ = (int) d; //varexpr
  // __left475__ = d
  int __left474__ = (__left475__ + 0);
  int __leftop477__ = 32;
  int __leftop479__ = 32;
  int __leftop481__ = 32;
  int __leftop483__ = 32;
  int __leftop485__ = 32;
  int __leftop487__ = 32;
  int __rightop488__ = 0;
  int __rightop486__ = __leftop487__ + __rightop488__;
  int __rightop484__ = __leftop485__ + __rightop486__;
  int __rightop482__ = __leftop483__ + __rightop484__;
  int __rightop480__ = __leftop481__ + __rightop482__;
  int __rightop478__ = __leftop479__ + __rightop480__;
  int __sizeof476__ = __leftop477__ + __rightop478__;
  int __high489__ = __left474__ + __sizeof476__;
  assertvalidmemory(__left474__, __high489__);
  // __left474__ = d.s
  // __offsetinbits490__ <-- (32 + (32 + 0))
  int __leftop491__ = 32;
  int __leftop493__ = 32;
  int __rightop494__ = 0;
  int __rightop492__ = __leftop493__ + __rightop494__;
  int __offsetinbits490__ = __leftop491__ + __rightop492__;
  // __offsetinbits490__ = (32 + (32 + 0))
  int __offset495__ = __offsetinbits490__ >> 3;
  int __shift496__ = __offsetinbits490__ - (__offset495__ << 3);
  int __rightop473__ = ((*(int *)(__left474__ + __offset495__))  >> __shift496__) & 0xffffffff;
  int __tempvar418__ = __leftop419__ < __rightop473__;
  if (__tempvar418__)
    {
    // __left498__ <-- d.g
    // __left499__ <-- d
    int __left499__ = (int) d; //varexpr
    // __left499__ = d
    // __offsetinbits500__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop501__ = 0;
    int __leftop505__ = 8;
    // __left507__ <-- d.s
    // __left508__ <-- d
    int __left508__ = (int) d; //varexpr
    // __left508__ = d
    int __left507__ = (__left508__ + 0);
    int __leftop510__ = 32;
    int __leftop512__ = 32;
    int __leftop514__ = 32;
    int __leftop516__ = 32;
    int __leftop518__ = 32;
    int __leftop520__ = 32;
    int __rightop521__ = 0;
    int __rightop519__ = __leftop520__ + __rightop521__;
    int __rightop517__ = __leftop518__ + __rightop519__;
    int __rightop515__ = __leftop516__ + __rightop517__;
    int __rightop513__ = __leftop514__ + __rightop515__;
    int __rightop511__ = __leftop512__ + __rightop513__;
    int __sizeof509__ = __leftop510__ + __rightop511__;
    int __high522__ = __left507__ + __sizeof509__;
    assertvalidmemory(__left507__, __high522__);
    // __left507__ = d.s
    // __offsetinbits523__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop524__ = 32;
    int __leftop526__ = 32;
    int __leftop528__ = 32;
    int __leftop530__ = 32;
    int __leftop532__ = 32;
    int __rightop533__ = 0;
    int __rightop531__ = __leftop532__ + __rightop533__;
    int __rightop529__ = __leftop530__ + __rightop531__;
    int __rightop527__ = __leftop528__ + __rightop529__;
    int __rightop525__ = __leftop526__ + __rightop527__;
    int __offsetinbits523__ = __leftop524__ + __rightop525__;
    // __offsetinbits523__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset534__ = __offsetinbits523__ >> 3;
    int __shift535__ = __offsetinbits523__ - (__offset534__ << 3);
    int __rightop506__ = ((*(int *)(__left507__ + __offset534__))  >> __shift535__) & 0xffffffff;
    int __leftop504__ = __leftop505__ * __rightop506__;
    int __rightop536__ = 0;
    int __leftop503__ = __leftop504__ + __rightop536__;
    int __rightop537__ = 1;
    int __rightop502__ = __leftop503__ * __rightop537__;
    int __offsetinbits500__ = __leftop501__ + __rightop502__;
    // __offsetinbits500__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset538__ = __offsetinbits500__ >> 3;
    int __left498__ = (__left499__ + __offset538__);
    int __leftop540__ = 32;
    int __leftop542__ = 32;
    int __leftop544__ = 32;
    int __leftop546__ = 32;
    int __leftop548__ = 32;
    int __rightop549__ = 0;
    int __rightop547__ = __leftop548__ + __rightop549__;
    int __rightop545__ = __leftop546__ + __rightop547__;
    int __rightop543__ = __leftop544__ + __rightop545__;
    int __rightop541__ = __leftop542__ + __rightop543__;
    int __sizeof539__ = __leftop540__ + __rightop541__;
    int __high550__ = __left498__ + __sizeof539__;
    assertvalidmemory(__left498__, __high550__);
    // __left498__ = d.g
    int __element497__ = ((*(int *)(__left498__ + 0))  >> 0) & 0xffffffff;
    int __addeditem551__ = 1;
    __addeditem551__ = __BlockBitmapBlock___hash->add((int)__element497__, (int)__element497__);
    }
  }


// build rule13
  {
  int __tempvar552__ = 0;
  // __left555__ <-- d.s
  // __left556__ <-- d
  int __left556__ = (int) d; //varexpr
  // __left556__ = d
  int __left555__ = (__left556__ + 0);
  int __leftop558__ = 32;
  int __leftop560__ = 32;
  int __leftop562__ = 32;
  int __leftop564__ = 32;
  int __leftop566__ = 32;
  int __leftop568__ = 32;
  int __rightop569__ = 0;
  int __rightop567__ = __leftop568__ + __rightop569__;
  int __rightop565__ = __leftop566__ + __rightop567__;
  int __rightop563__ = __leftop564__ + __rightop565__;
  int __rightop561__ = __leftop562__ + __rightop563__;
  int __rightop559__ = __leftop560__ + __rightop561__;
  int __sizeof557__ = __leftop558__ + __rightop559__;
  int __high570__ = __left555__ + __sizeof557__;
  assertvalidmemory(__left555__, __high570__);
  // __left555__ = d.s
  // __offsetinbits571__ <-- (32 + (32 + (32 + 0)))
  int __leftop572__ = 32;
  int __leftop574__ = 32;
  int __leftop576__ = 32;
  int __rightop577__ = 0;
  int __rightop575__ = __leftop576__ + __rightop577__;
  int __rightop573__ = __leftop574__ + __rightop575__;
  int __offsetinbits571__ = __leftop572__ + __rightop573__;
  // __offsetinbits571__ = (32 + (32 + (32 + 0)))
  int __offset578__ = __offsetinbits571__ >> 3;
  int __shift579__ = __offsetinbits571__ - (__offset578__ << 3);
  int __leftop554__ = ((*(int *)(__left555__ + __offset578__))  >> __shift579__) & 0xffffffff;
  int __rightop580__ = 1;
  int __tempvar553__ = __leftop554__ - __rightop580__;
  for (int __j__ = __tempvar552__; __j__ <= __tempvar553__; __j__++)
    {
    for (SimpleIterator* __ibb___iterator = __InodeBitmapBlock___hash->iterator(); __ibb___iterator->hasNext(); )
      {
      int __ibb__ = (int) __ibb___iterator->next();
      //(cast(__InodeBitmap__, d.b[ibb]).inodebitmap[j] == true)
      // __left583__ <-- cast(__InodeBitmap__, d.b[ibb])
      // __left585__ <-- d
      int __left585__ = (int) d; //varexpr
      // __left585__ = d
      // __offsetinbits586__ <-- (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __leftop587__ = 0;
      int __leftop591__ = 8;
      // __left593__ <-- d.s
      // __left594__ <-- d
      int __left594__ = (int) d; //varexpr
      // __left594__ = d
      int __left593__ = (__left594__ + 0);
      int __leftop596__ = 32;
      int __leftop598__ = 32;
      int __leftop600__ = 32;
      int __leftop602__ = 32;
      int __leftop604__ = 32;
      int __leftop606__ = 32;
      int __rightop607__ = 0;
      int __rightop605__ = __leftop606__ + __rightop607__;
      int __rightop603__ = __leftop604__ + __rightop605__;
      int __rightop601__ = __leftop602__ + __rightop603__;
      int __rightop599__ = __leftop600__ + __rightop601__;
      int __rightop597__ = __leftop598__ + __rightop599__;
      int __sizeof595__ = __leftop596__ + __rightop597__;
      int __high608__ = __left593__ + __sizeof595__;
      assertvalidmemory(__left593__, __high608__);
      // __left593__ = d.s
      // __offsetinbits609__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
      // __offsetinbits609__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset620__ = __offsetinbits609__ >> 3;
      int __shift621__ = __offsetinbits609__ - (__offset620__ << 3);
      int __rightop592__ = ((*(int *)(__left593__ + __offset620__))  >> __shift621__) & 0xffffffff;
      int __leftop590__ = __leftop591__ * __rightop592__;
      int __rightop622__ = 0;
      int __leftop589__ = __leftop590__ + __rightop622__;
      int __rightop623__ = (int) __ibb__; //varexpr
      int __rightop588__ = __leftop589__ * __rightop623__;
      int __offsetinbits586__ = __leftop587__ + __rightop588__;
      // __offsetinbits586__ = (0 + (((8 * d.s.blocksize) + 0) * ibb))
      int __offset624__ = __offsetinbits586__ >> 3;
      int __expr584__ = (__left585__ + __offset624__);
      int __leftop627__ = 8;
      // __left629__ <-- d.s
      // __left630__ <-- d
      int __left630__ = (int) d; //varexpr
      // __left630__ = d
      int __left629__ = (__left630__ + 0);
      int __leftop632__ = 32;
      int __leftop634__ = 32;
      int __leftop636__ = 32;
      int __leftop638__ = 32;
      int __leftop640__ = 32;
      int __leftop642__ = 32;
      int __rightop643__ = 0;
      int __rightop641__ = __leftop642__ + __rightop643__;
      int __rightop639__ = __leftop640__ + __rightop641__;
      int __rightop637__ = __leftop638__ + __rightop639__;
      int __rightop635__ = __leftop636__ + __rightop637__;
      int __rightop633__ = __leftop634__ + __rightop635__;
      int __sizeof631__ = __leftop632__ + __rightop633__;
      int __high644__ = __left629__ + __sizeof631__;
      assertvalidmemory(__left629__, __high644__);
      // __left629__ = d.s
      // __offsetinbits645__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop646__ = 32;
      int __leftop648__ = 32;
      int __leftop650__ = 32;
      int __leftop652__ = 32;
      int __leftop654__ = 32;
      int __rightop655__ = 0;
      int __rightop653__ = __leftop654__ + __rightop655__;
      int __rightop651__ = __leftop652__ + __rightop653__;
      int __rightop649__ = __leftop650__ + __rightop651__;
      int __rightop647__ = __leftop648__ + __rightop649__;
      int __offsetinbits645__ = __leftop646__ + __rightop647__;
      // __offsetinbits645__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset656__ = __offsetinbits645__ >> 3;
      int __shift657__ = __offsetinbits645__ - (__offset656__ << 3);
      int __rightop628__ = ((*(int *)(__left629__ + __offset656__))  >> __shift657__) & 0xffffffff;
      int __leftop626__ = __leftop627__ * __rightop628__;
      int __rightop658__ = 0;
      int __sizeof625__ = __leftop626__ + __rightop658__;
      int __high659__ = __expr584__ + __sizeof625__;
      assertvalidmemory(__expr584__, __high659__);
      int __left583__ = (int) __expr584__;
      // __left583__ = cast(__InodeBitmap__, d.b[ibb])
      // __offsetinbits660__ <-- (0 + (1 * j))
      int __leftop661__ = 0;
      int __leftop663__ = 1;
      int __rightop664__ = (int) __j__; //varexpr
      int __rightop662__ = __leftop663__ * __rightop664__;
      int __offsetinbits660__ = __leftop661__ + __rightop662__;
      // __offsetinbits660__ = (0 + (1 * j))
      int __offset665__ = __offsetinbits660__ >> 3;
      int __shift666__ = __offsetinbits660__ - (__offset665__ << 3);
      int __leftop582__ = ((*(int *)(__left583__ + __offset665__))  >> __shift666__) & 0x1;
      int __rightop667__ = 1;
      int __tempvar581__ = __leftop582__ == __rightop667__;
      if (__tempvar581__)
        {
        int __leftele668__ = (int) __j__; //varexpr
        int __rightele669__ = 100;
        int __addeditem671__;
        __addeditem671__ = __inodestatus___hash->add((int)__leftele668__, (int)__rightele669__);
        }
      }
    }
  }


// build rule6
  {
  //(d.s.RootDirectoryInode < d.s.NumberofInodes)
  // __left674__ <-- d.s
  // __left675__ <-- d
  int __left675__ = (int) d; //varexpr
  // __left675__ = d
  int __left674__ = (__left675__ + 0);
  int __leftop677__ = 32;
  int __leftop679__ = 32;
  int __leftop681__ = 32;
  int __leftop683__ = 32;
  int __leftop685__ = 32;
  int __leftop687__ = 32;
  int __rightop688__ = 0;
  int __rightop686__ = __leftop687__ + __rightop688__;
  int __rightop684__ = __leftop685__ + __rightop686__;
  int __rightop682__ = __leftop683__ + __rightop684__;
  int __rightop680__ = __leftop681__ + __rightop682__;
  int __rightop678__ = __leftop679__ + __rightop680__;
  int __sizeof676__ = __leftop677__ + __rightop678__;
  int __high689__ = __left674__ + __sizeof676__;
  assertvalidmemory(__left674__, __high689__);
  // __left674__ = d.s
  // __offsetinbits690__ <-- (32 + (32 + (32 + (32 + 0))))
  int __leftop691__ = 32;
  int __leftop693__ = 32;
  int __leftop695__ = 32;
  int __leftop697__ = 32;
  int __rightop698__ = 0;
  int __rightop696__ = __leftop697__ + __rightop698__;
  int __rightop694__ = __leftop695__ + __rightop696__;
  int __rightop692__ = __leftop693__ + __rightop694__;
  int __offsetinbits690__ = __leftop691__ + __rightop692__;
  // __offsetinbits690__ = (32 + (32 + (32 + (32 + 0))))
  int __offset699__ = __offsetinbits690__ >> 3;
  int __shift700__ = __offsetinbits690__ - (__offset699__ << 3);
  int __leftop673__ = ((*(int *)(__left674__ + __offset699__))  >> __shift700__) & 0xffffffff;
  // __left702__ <-- d.s
  // __left703__ <-- d
  int __left703__ = (int) d; //varexpr
  // __left703__ = d
  int __left702__ = (__left703__ + 0);
  int __leftop705__ = 32;
  int __leftop707__ = 32;
  int __leftop709__ = 32;
  int __leftop711__ = 32;
  int __leftop713__ = 32;
  int __leftop715__ = 32;
  int __rightop716__ = 0;
  int __rightop714__ = __leftop715__ + __rightop716__;
  int __rightop712__ = __leftop713__ + __rightop714__;
  int __rightop710__ = __leftop711__ + __rightop712__;
  int __rightop708__ = __leftop709__ + __rightop710__;
  int __rightop706__ = __leftop707__ + __rightop708__;
  int __sizeof704__ = __leftop705__ + __rightop706__;
  int __high717__ = __left702__ + __sizeof704__;
  assertvalidmemory(__left702__, __high717__);
  // __left702__ = d.s
  // __offsetinbits718__ <-- (32 + (32 + (32 + 0)))
  int __leftop719__ = 32;
  int __leftop721__ = 32;
  int __leftop723__ = 32;
  int __rightop724__ = 0;
  int __rightop722__ = __leftop723__ + __rightop724__;
  int __rightop720__ = __leftop721__ + __rightop722__;
  int __offsetinbits718__ = __leftop719__ + __rightop720__;
  // __offsetinbits718__ = (32 + (32 + (32 + 0)))
  int __offset725__ = __offsetinbits718__ >> 3;
  int __shift726__ = __offsetinbits718__ - (__offset725__ << 3);
  int __rightop701__ = ((*(int *)(__left702__ + __offset725__))  >> __shift726__) & 0xffffffff;
  int __tempvar672__ = __leftop673__ < __rightop701__;
  if (__tempvar672__)
    {
    // __left728__ <-- d.s
    // __left729__ <-- d
    int __left729__ = (int) d; //varexpr
    // __left729__ = d
    int __left728__ = (__left729__ + 0);
    int __leftop731__ = 32;
    int __leftop733__ = 32;
    int __leftop735__ = 32;
    int __leftop737__ = 32;
    int __leftop739__ = 32;
    int __leftop741__ = 32;
    int __rightop742__ = 0;
    int __rightop740__ = __leftop741__ + __rightop742__;
    int __rightop738__ = __leftop739__ + __rightop740__;
    int __rightop736__ = __leftop737__ + __rightop738__;
    int __rightop734__ = __leftop735__ + __rightop736__;
    int __rightop732__ = __leftop733__ + __rightop734__;
    int __sizeof730__ = __leftop731__ + __rightop732__;
    int __high743__ = __left728__ + __sizeof730__;
    assertvalidmemory(__left728__, __high743__);
    // __left728__ = d.s
    // __offsetinbits744__ <-- (32 + (32 + (32 + (32 + 0))))
    int __leftop745__ = 32;
    int __leftop747__ = 32;
    int __leftop749__ = 32;
    int __leftop751__ = 32;
    int __rightop752__ = 0;
    int __rightop750__ = __leftop751__ + __rightop752__;
    int __rightop748__ = __leftop749__ + __rightop750__;
    int __rightop746__ = __leftop747__ + __rightop748__;
    int __offsetinbits744__ = __leftop745__ + __rightop746__;
    // __offsetinbits744__ = (32 + (32 + (32 + (32 + 0))))
    int __offset753__ = __offsetinbits744__ >> 3;
    int __shift754__ = __offsetinbits744__ - (__offset753__ << 3);
    int __element727__ = ((*(int *)(__left728__ + __offset753__))  >> __shift754__) & 0xffffffff;
    int __addeditem755__ = 1;
    __addeditem755__ = __RootDirectoryInode___hash->add((int)__element727__, (int)__element727__);
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
      int __tempvar756__ = 0;
      // __left760__ <-- d.s
      // __left761__ <-- d
      int __left761__ = (int) d; //varexpr
      // __left761__ = d
      int __left760__ = (__left761__ + 0);
      int __leftop763__ = 32;
      int __leftop765__ = 32;
      int __leftop767__ = 32;
      int __leftop769__ = 32;
      int __leftop771__ = 32;
      int __leftop773__ = 32;
      int __rightop774__ = 0;
      int __rightop772__ = __leftop773__ + __rightop774__;
      int __rightop770__ = __leftop771__ + __rightop772__;
      int __rightop768__ = __leftop769__ + __rightop770__;
      int __rightop766__ = __leftop767__ + __rightop768__;
      int __rightop764__ = __leftop765__ + __rightop766__;
      int __sizeof762__ = __leftop763__ + __rightop764__;
      int __high775__ = __left760__ + __sizeof762__;
      assertvalidmemory(__left760__, __high775__);
      // __left760__ = d.s
      // __offsetinbits776__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop777__ = 32;
      int __leftop779__ = 32;
      int __leftop781__ = 32;
      int __leftop783__ = 32;
      int __leftop785__ = 32;
      int __rightop786__ = 0;
      int __rightop784__ = __leftop785__ + __rightop786__;
      int __rightop782__ = __leftop783__ + __rightop784__;
      int __rightop780__ = __leftop781__ + __rightop782__;
      int __rightop778__ = __leftop779__ + __rightop780__;
      int __offsetinbits776__ = __leftop777__ + __rightop778__;
      // __offsetinbits776__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset787__ = __offsetinbits776__ >> 3;
      int __shift788__ = __offsetinbits776__ - (__offset787__ << 3);
      int __leftop759__ = ((*(int *)(__left760__ + __offset787__))  >> __shift788__) & 0xffffffff;
      int __rightop789__ = 128;
      int __leftop758__ = __leftop759__ / __rightop789__;
      int __rightop790__ = 1;
      int __tempvar757__ = __leftop758__ - __rightop790__;
      for (int __j__ = __tempvar756__; __j__ <= __tempvar757__; __j__++)
        {
        int __tempvar791__ = 0;
        int __tempvar792__ = 11;
        for (int __k__ = __tempvar791__; __k__ <= __tempvar792__; __k__++)
          {
          //(cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k] < d.s.NumberofBlocks)
          // __left795__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
          // __left796__ <-- cast(__InodeTable__, d.b[itb])
          // __left798__ <-- d
          int __left798__ = (int) d; //varexpr
          // __left798__ = d
          // __offsetinbits799__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop800__ = 0;
          int __leftop804__ = 8;
          // __left806__ <-- d.s
          // __left807__ <-- d
          int __left807__ = (int) d; //varexpr
          // __left807__ = d
          int __left806__ = (__left807__ + 0);
          int __leftop809__ = 32;
          int __leftop811__ = 32;
          int __leftop813__ = 32;
          int __leftop815__ = 32;
          int __leftop817__ = 32;
          int __leftop819__ = 32;
          int __rightop820__ = 0;
          int __rightop818__ = __leftop819__ + __rightop820__;
          int __rightop816__ = __leftop817__ + __rightop818__;
          int __rightop814__ = __leftop815__ + __rightop816__;
          int __rightop812__ = __leftop813__ + __rightop814__;
          int __rightop810__ = __leftop811__ + __rightop812__;
          int __sizeof808__ = __leftop809__ + __rightop810__;
          int __high821__ = __left806__ + __sizeof808__;
          assertvalidmemory(__left806__, __high821__);
          // __left806__ = d.s
          // __offsetinbits822__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop823__ = 32;
          int __leftop825__ = 32;
          int __leftop827__ = 32;
          int __leftop829__ = 32;
          int __leftop831__ = 32;
          int __rightop832__ = 0;
          int __rightop830__ = __leftop831__ + __rightop832__;
          int __rightop828__ = __leftop829__ + __rightop830__;
          int __rightop826__ = __leftop827__ + __rightop828__;
          int __rightop824__ = __leftop825__ + __rightop826__;
          int __offsetinbits822__ = __leftop823__ + __rightop824__;
          // __offsetinbits822__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset833__ = __offsetinbits822__ >> 3;
          int __shift834__ = __offsetinbits822__ - (__offset833__ << 3);
          int __rightop805__ = ((*(int *)(__left806__ + __offset833__))  >> __shift834__) & 0xffffffff;
          int __leftop803__ = __leftop804__ * __rightop805__;
          int __rightop835__ = 0;
          int __leftop802__ = __leftop803__ + __rightop835__;
          int __rightop836__ = (int) __itb__; //varexpr
          int __rightop801__ = __leftop802__ * __rightop836__;
          int __offsetinbits799__ = __leftop800__ + __rightop801__;
          // __offsetinbits799__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset837__ = __offsetinbits799__ >> 3;
          int __expr797__ = (__left798__ + __offset837__);
          int __leftop840__ = 8;
          // __left842__ <-- d.s
          // __left843__ <-- d
          int __left843__ = (int) d; //varexpr
          // __left843__ = d
          int __left842__ = (__left843__ + 0);
          int __leftop845__ = 32;
          int __leftop847__ = 32;
          int __leftop849__ = 32;
          int __leftop851__ = 32;
          int __leftop853__ = 32;
          int __leftop855__ = 32;
          int __rightop856__ = 0;
          int __rightop854__ = __leftop855__ + __rightop856__;
          int __rightop852__ = __leftop853__ + __rightop854__;
          int __rightop850__ = __leftop851__ + __rightop852__;
          int __rightop848__ = __leftop849__ + __rightop850__;
          int __rightop846__ = __leftop847__ + __rightop848__;
          int __sizeof844__ = __leftop845__ + __rightop846__;
          int __high857__ = __left842__ + __sizeof844__;
          assertvalidmemory(__left842__, __high857__);
          // __left842__ = d.s
          // __offsetinbits858__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop859__ = 32;
          int __leftop861__ = 32;
          int __leftop863__ = 32;
          int __leftop865__ = 32;
          int __leftop867__ = 32;
          int __rightop868__ = 0;
          int __rightop866__ = __leftop867__ + __rightop868__;
          int __rightop864__ = __leftop865__ + __rightop866__;
          int __rightop862__ = __leftop863__ + __rightop864__;
          int __rightop860__ = __leftop861__ + __rightop862__;
          int __offsetinbits858__ = __leftop859__ + __rightop860__;
          // __offsetinbits858__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset869__ = __offsetinbits858__ >> 3;
          int __shift870__ = __offsetinbits858__ - (__offset869__ << 3);
          int __rightop841__ = ((*(int *)(__left842__ + __offset869__))  >> __shift870__) & 0xffffffff;
          int __leftop839__ = __leftop840__ * __rightop841__;
          int __rightop871__ = 0;
          int __sizeof838__ = __leftop839__ + __rightop871__;
          int __high872__ = __expr797__ + __sizeof838__;
          assertvalidmemory(__expr797__, __high872__);
          int __left796__ = (int) __expr797__;
          // __left796__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits873__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
          int __leftop874__ = 0;
          int __leftop877__ = 32;
          int __leftop880__ = 32;
          int __rightop881__ = 12;
          int __leftop879__ = __leftop880__ * __rightop881__;
          int __leftop883__ = 32;
          int __rightop884__ = 0;
          int __rightop882__ = __leftop883__ + __rightop884__;
          int __rightop878__ = __leftop879__ + __rightop882__;
          int __leftop876__ = __leftop877__ + __rightop878__;
          int __rightop885__ = (int) __di__; //varexpr
          int __rightop875__ = __leftop876__ * __rightop885__;
          int __offsetinbits873__ = __leftop874__ + __rightop875__;
          // __offsetinbits873__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
          int __offset886__ = __offsetinbits873__ >> 3;
          int __left795__ = (__left796__ + __offset886__);
          int __leftop888__ = 32;
          int __leftop891__ = 32;
          int __rightop892__ = 12;
          int __leftop890__ = __leftop891__ * __rightop892__;
          int __leftop894__ = 32;
          int __rightop895__ = 0;
          int __rightop893__ = __leftop894__ + __rightop895__;
          int __rightop889__ = __leftop890__ + __rightop893__;
          int __sizeof887__ = __leftop888__ + __rightop889__;
          int __high896__ = __left795__ + __sizeof887__;
          assertvalidmemory(__left795__, __high896__);
          // __left795__ = cast(__InodeTable__, d.b[itb]).itable[di]
          // __offsetinbits897__ <-- ((32 + 0) + (32 * k))
          int __leftop899__ = 32;
          int __rightop900__ = 0;
          int __leftop898__ = __leftop899__ + __rightop900__;
          int __leftop902__ = 32;
          int __rightop903__ = (int) __k__; //varexpr
          int __rightop901__ = __leftop902__ * __rightop903__;
          int __offsetinbits897__ = __leftop898__ + __rightop901__;
          // __offsetinbits897__ = ((32 + 0) + (32 * k))
          int __offset904__ = __offsetinbits897__ >> 3;
          int __shift905__ = __offsetinbits897__ - (__offset904__ << 3);
          int __leftop794__ = ((*(int *)(__left795__ + __offset904__))  >> __shift905__) & 0xffffffff;
          // __left907__ <-- d.s
          // __left908__ <-- d
          int __left908__ = (int) d; //varexpr
          // __left908__ = d
          int __left907__ = (__left908__ + 0);
          int __leftop910__ = 32;
          int __leftop912__ = 32;
          int __leftop914__ = 32;
          int __leftop916__ = 32;
          int __leftop918__ = 32;
          int __leftop920__ = 32;
          int __rightop921__ = 0;
          int __rightop919__ = __leftop920__ + __rightop921__;
          int __rightop917__ = __leftop918__ + __rightop919__;
          int __rightop915__ = __leftop916__ + __rightop917__;
          int __rightop913__ = __leftop914__ + __rightop915__;
          int __rightop911__ = __leftop912__ + __rightop913__;
          int __sizeof909__ = __leftop910__ + __rightop911__;
          int __high922__ = __left907__ + __sizeof909__;
          assertvalidmemory(__left907__, __high922__);
          // __left907__ = d.s
          // __offsetinbits923__ <-- (32 + (32 + 0))
          int __leftop924__ = 32;
          int __leftop926__ = 32;
          int __rightop927__ = 0;
          int __rightop925__ = __leftop926__ + __rightop927__;
          int __offsetinbits923__ = __leftop924__ + __rightop925__;
          // __offsetinbits923__ = (32 + (32 + 0))
          int __offset928__ = __offsetinbits923__ >> 3;
          int __shift929__ = __offsetinbits923__ - (__offset928__ << 3);
          int __rightop906__ = ((*(int *)(__left907__ + __offset928__))  >> __shift929__) & 0xffffffff;
          int __tempvar793__ = __leftop794__ < __rightop906__;
          if (__tempvar793__)
            {
            // __left931__ <-- cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __left933__ <-- d
            int __left933__ = (int) d; //varexpr
            // __left933__ = d
            // __offsetinbits934__ <-- (0 + (((8 * d.s.blocksize) + 0) * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]))
            int __leftop935__ = 0;
            int __leftop939__ = 8;
            // __left941__ <-- d.s
            // __left942__ <-- d
            int __left942__ = (int) d; //varexpr
            // __left942__ = d
            int __left941__ = (__left942__ + 0);
            int __leftop944__ = 32;
            int __leftop946__ = 32;
            int __leftop948__ = 32;
            int __leftop950__ = 32;
            int __leftop952__ = 32;
            int __leftop954__ = 32;
            int __rightop955__ = 0;
            int __rightop953__ = __leftop954__ + __rightop955__;
            int __rightop951__ = __leftop952__ + __rightop953__;
            int __rightop949__ = __leftop950__ + __rightop951__;
            int __rightop947__ = __leftop948__ + __rightop949__;
            int __rightop945__ = __leftop946__ + __rightop947__;
            int __sizeof943__ = __leftop944__ + __rightop945__;
            int __high956__ = __left941__ + __sizeof943__;
            assertvalidmemory(__left941__, __high956__);
            // __left941__ = d.s
            // __offsetinbits957__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
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
            // __offsetinbits957__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset968__ = __offsetinbits957__ >> 3;
            int __shift969__ = __offsetinbits957__ - (__offset968__ << 3);
            int __rightop940__ = ((*(int *)(__left941__ + __offset968__))  >> __shift969__) & 0xffffffff;
            int __leftop938__ = __leftop939__ * __rightop940__;
            int __rightop970__ = 0;
            int __leftop937__ = __leftop938__ + __rightop970__;
            // __left972__ <-- cast(__InodeTable__, d.b[itb]).itable[di]
            // __left973__ <-- cast(__InodeTable__, d.b[itb])
            // __left975__ <-- d
            int __left975__ = (int) d; //varexpr
            // __left975__ = d
            // __offsetinbits976__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
            int __leftop977__ = 0;
            int __leftop981__ = 8;
            // __left983__ <-- d.s
            // __left984__ <-- d
            int __left984__ = (int) d; //varexpr
            // __left984__ = d
            int __left983__ = (__left984__ + 0);
            int __leftop986__ = 32;
            int __leftop988__ = 32;
            int __leftop990__ = 32;
            int __leftop992__ = 32;
            int __leftop994__ = 32;
            int __leftop996__ = 32;
            int __rightop997__ = 0;
            int __rightop995__ = __leftop996__ + __rightop997__;
            int __rightop993__ = __leftop994__ + __rightop995__;
            int __rightop991__ = __leftop992__ + __rightop993__;
            int __rightop989__ = __leftop990__ + __rightop991__;
            int __rightop987__ = __leftop988__ + __rightop989__;
            int __sizeof985__ = __leftop986__ + __rightop987__;
            int __high998__ = __left983__ + __sizeof985__;
            assertvalidmemory(__left983__, __high998__);
            // __left983__ = d.s
            // __offsetinbits999__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop1000__ = 32;
            int __leftop1002__ = 32;
            int __leftop1004__ = 32;
            int __leftop1006__ = 32;
            int __leftop1008__ = 32;
            int __rightop1009__ = 0;
            int __rightop1007__ = __leftop1008__ + __rightop1009__;
            int __rightop1005__ = __leftop1006__ + __rightop1007__;
            int __rightop1003__ = __leftop1004__ + __rightop1005__;
            int __rightop1001__ = __leftop1002__ + __rightop1003__;
            int __offsetinbits999__ = __leftop1000__ + __rightop1001__;
            // __offsetinbits999__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset1010__ = __offsetinbits999__ >> 3;
            int __shift1011__ = __offsetinbits999__ - (__offset1010__ << 3);
            int __rightop982__ = ((*(int *)(__left983__ + __offset1010__))  >> __shift1011__) & 0xffffffff;
            int __leftop980__ = __leftop981__ * __rightop982__;
            int __rightop1012__ = 0;
            int __leftop979__ = __leftop980__ + __rightop1012__;
            int __rightop1013__ = (int) __itb__; //varexpr
            int __rightop978__ = __leftop979__ * __rightop1013__;
            int __offsetinbits976__ = __leftop977__ + __rightop978__;
            // __offsetinbits976__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
            int __offset1014__ = __offsetinbits976__ >> 3;
            int __expr974__ = (__left975__ + __offset1014__);
            int __leftop1017__ = 8;
            // __left1019__ <-- d.s
            // __left1020__ <-- d
            int __left1020__ = (int) d; //varexpr
            // __left1020__ = d
            int __left1019__ = (__left1020__ + 0);
            int __leftop1022__ = 32;
            int __leftop1024__ = 32;
            int __leftop1026__ = 32;
            int __leftop1028__ = 32;
            int __leftop1030__ = 32;
            int __leftop1032__ = 32;
            int __rightop1033__ = 0;
            int __rightop1031__ = __leftop1032__ + __rightop1033__;
            int __rightop1029__ = __leftop1030__ + __rightop1031__;
            int __rightop1027__ = __leftop1028__ + __rightop1029__;
            int __rightop1025__ = __leftop1026__ + __rightop1027__;
            int __rightop1023__ = __leftop1024__ + __rightop1025__;
            int __sizeof1021__ = __leftop1022__ + __rightop1023__;
            int __high1034__ = __left1019__ + __sizeof1021__;
            assertvalidmemory(__left1019__, __high1034__);
            // __left1019__ = d.s
            // __offsetinbits1035__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop1036__ = 32;
            int __leftop1038__ = 32;
            int __leftop1040__ = 32;
            int __leftop1042__ = 32;
            int __leftop1044__ = 32;
            int __rightop1045__ = 0;
            int __rightop1043__ = __leftop1044__ + __rightop1045__;
            int __rightop1041__ = __leftop1042__ + __rightop1043__;
            int __rightop1039__ = __leftop1040__ + __rightop1041__;
            int __rightop1037__ = __leftop1038__ + __rightop1039__;
            int __offsetinbits1035__ = __leftop1036__ + __rightop1037__;
            // __offsetinbits1035__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset1046__ = __offsetinbits1035__ >> 3;
            int __shift1047__ = __offsetinbits1035__ - (__offset1046__ << 3);
            int __rightop1018__ = ((*(int *)(__left1019__ + __offset1046__))  >> __shift1047__) & 0xffffffff;
            int __leftop1016__ = __leftop1017__ * __rightop1018__;
            int __rightop1048__ = 0;
            int __sizeof1015__ = __leftop1016__ + __rightop1048__;
            int __high1049__ = __expr974__ + __sizeof1015__;
            assertvalidmemory(__expr974__, __high1049__);
            int __left973__ = (int) __expr974__;
            // __left973__ = cast(__InodeTable__, d.b[itb])
            // __offsetinbits1050__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
            int __leftop1051__ = 0;
            int __leftop1054__ = 32;
            int __leftop1057__ = 32;
            int __rightop1058__ = 12;
            int __leftop1056__ = __leftop1057__ * __rightop1058__;
            int __leftop1060__ = 32;
            int __rightop1061__ = 0;
            int __rightop1059__ = __leftop1060__ + __rightop1061__;
            int __rightop1055__ = __leftop1056__ + __rightop1059__;
            int __leftop1053__ = __leftop1054__ + __rightop1055__;
            int __rightop1062__ = (int) __di__; //varexpr
            int __rightop1052__ = __leftop1053__ * __rightop1062__;
            int __offsetinbits1050__ = __leftop1051__ + __rightop1052__;
            // __offsetinbits1050__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * di))
            int __offset1063__ = __offsetinbits1050__ >> 3;
            int __left972__ = (__left973__ + __offset1063__);
            int __leftop1065__ = 32;
            int __leftop1068__ = 32;
            int __rightop1069__ = 12;
            int __leftop1067__ = __leftop1068__ * __rightop1069__;
            int __leftop1071__ = 32;
            int __rightop1072__ = 0;
            int __rightop1070__ = __leftop1071__ + __rightop1072__;
            int __rightop1066__ = __leftop1067__ + __rightop1070__;
            int __sizeof1064__ = __leftop1065__ + __rightop1066__;
            int __high1073__ = __left972__ + __sizeof1064__;
            assertvalidmemory(__left972__, __high1073__);
            // __left972__ = cast(__InodeTable__, d.b[itb]).itable[di]
            // __offsetinbits1074__ <-- ((32 + 0) + (32 * k))
            int __leftop1076__ = 32;
            int __rightop1077__ = 0;
            int __leftop1075__ = __leftop1076__ + __rightop1077__;
            int __leftop1079__ = 32;
            int __rightop1080__ = (int) __k__; //varexpr
            int __rightop1078__ = __leftop1079__ * __rightop1080__;
            int __offsetinbits1074__ = __leftop1075__ + __rightop1078__;
            // __offsetinbits1074__ = ((32 + 0) + (32 * k))
            int __offset1081__ = __offsetinbits1074__ >> 3;
            int __shift1082__ = __offsetinbits1074__ - (__offset1081__ << 3);
            int __rightop971__ = ((*(int *)(__left972__ + __offset1081__))  >> __shift1082__) & 0xffffffff;
            int __rightop936__ = __leftop937__ * __rightop971__;
            int __offsetinbits934__ = __leftop935__ + __rightop936__;
            // __offsetinbits934__ = (0 + (((8 * d.s.blocksize) + 0) * cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]))
            int __offset1083__ = __offsetinbits934__ >> 3;
            int __expr932__ = (__left933__ + __offset1083__);
            int __leftop1086__ = 8;
            // __left1088__ <-- d.s
            // __left1089__ <-- d
            int __left1089__ = (int) d; //varexpr
            // __left1089__ = d
            int __left1088__ = (__left1089__ + 0);
            int __leftop1091__ = 32;
            int __leftop1093__ = 32;
            int __leftop1095__ = 32;
            int __leftop1097__ = 32;
            int __leftop1099__ = 32;
            int __leftop1101__ = 32;
            int __rightop1102__ = 0;
            int __rightop1100__ = __leftop1101__ + __rightop1102__;
            int __rightop1098__ = __leftop1099__ + __rightop1100__;
            int __rightop1096__ = __leftop1097__ + __rightop1098__;
            int __rightop1094__ = __leftop1095__ + __rightop1096__;
            int __rightop1092__ = __leftop1093__ + __rightop1094__;
            int __sizeof1090__ = __leftop1091__ + __rightop1092__;
            int __high1103__ = __left1088__ + __sizeof1090__;
            assertvalidmemory(__left1088__, __high1103__);
            // __left1088__ = d.s
            // __offsetinbits1104__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
            int __leftop1105__ = 32;
            int __leftop1107__ = 32;
            int __leftop1109__ = 32;
            int __leftop1111__ = 32;
            int __leftop1113__ = 32;
            int __rightop1114__ = 0;
            int __rightop1112__ = __leftop1113__ + __rightop1114__;
            int __rightop1110__ = __leftop1111__ + __rightop1112__;
            int __rightop1108__ = __leftop1109__ + __rightop1110__;
            int __rightop1106__ = __leftop1107__ + __rightop1108__;
            int __offsetinbits1104__ = __leftop1105__ + __rightop1106__;
            // __offsetinbits1104__ = (32 + (32 + (32 + (32 + (32 + 0)))))
            int __offset1115__ = __offsetinbits1104__ >> 3;
            int __shift1116__ = __offsetinbits1104__ - (__offset1115__ << 3);
            int __rightop1087__ = ((*(int *)(__left1088__ + __offset1115__))  >> __shift1116__) & 0xffffffff;
            int __leftop1085__ = __leftop1086__ * __rightop1087__;
            int __rightop1117__ = 0;
            int __sizeof1084__ = __leftop1085__ + __rightop1117__;
            int __high1118__ = __expr932__ + __sizeof1084__;
            assertvalidmemory(__expr932__, __high1118__);
            int __left931__ = (int) __expr932__;
            // __left931__ = cast(__DirectoryBlock__, d.b[cast(__InodeTable__, d.b[itb]).itable[di].Blockptr[k]])
            // __offsetinbits1119__ <-- (0 + ((32 + ((8 * 124) + 0)) * j))
            int __leftop1120__ = 0;
            int __leftop1123__ = 32;
            int __leftop1126__ = 8;
            int __rightop1127__ = 124;
            int __leftop1125__ = __leftop1126__ * __rightop1127__;
            int __rightop1128__ = 0;
            int __rightop1124__ = __leftop1125__ + __rightop1128__;
            int __leftop1122__ = __leftop1123__ + __rightop1124__;
            int __rightop1129__ = (int) __j__; //varexpr
            int __rightop1121__ = __leftop1122__ * __rightop1129__;
            int __offsetinbits1119__ = __leftop1120__ + __rightop1121__;
            // __offsetinbits1119__ = (0 + ((32 + ((8 * 124) + 0)) * j))
            int __offset1130__ = __offsetinbits1119__ >> 3;
            int __element930__ = (__left931__ + __offset1130__);
            int __leftop1132__ = 32;
            int __leftop1135__ = 8;
            int __rightop1136__ = 124;
            int __leftop1134__ = __leftop1135__ * __rightop1136__;
            int __rightop1137__ = 0;
            int __rightop1133__ = __leftop1134__ + __rightop1137__;
            int __sizeof1131__ = __leftop1132__ + __rightop1133__;
            int __high1138__ = __element930__ + __sizeof1131__;
            assertvalidmemory(__element930__, __high1138__);
            int __addeditem1139__ = 1;
            __addeditem1139__ = __DirectoryEntry___hash->add((int)__element930__, (int)__element930__);
            }
          }
        }
      }
    }
  }


// build rule27
  {
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); )
    {
    int __de__ = (int) __de___iterator->next();
    //((de.inodenumber < d.s.NumberofInodes) && ((de.inodenumber == 0)))
    // __left1143__ <-- de
    int __left1143__ = (int) __de__; //varexpr
    // __left1143__ = de
    // __offsetinbits1144__ <-- ((8 * 124) + 0)
    int __leftop1146__ = 8;
    int __rightop1147__ = 124;
    int __leftop1145__ = __leftop1146__ * __rightop1147__;
    int __rightop1148__ = 0;
    int __offsetinbits1144__ = __leftop1145__ + __rightop1148__;
    // __offsetinbits1144__ = ((8 * 124) + 0)
    int __offset1149__ = __offsetinbits1144__ >> 3;
    int __shift1150__ = __offsetinbits1144__ - (__offset1149__ << 3);
    int __leftop1142__ = ((*(int *)(__left1143__ + __offset1149__))  >> __shift1150__) & 0xffffffff;
    // __left1152__ <-- d.s
    // __left1153__ <-- d
    int __left1153__ = (int) d; //varexpr
    // __left1153__ = d
    int __left1152__ = (__left1153__ + 0);
    int __leftop1155__ = 32;
    int __leftop1157__ = 32;
    int __leftop1159__ = 32;
    int __leftop1161__ = 32;
    int __leftop1163__ = 32;
    int __leftop1165__ = 32;
    int __rightop1166__ = 0;
    int __rightop1164__ = __leftop1165__ + __rightop1166__;
    int __rightop1162__ = __leftop1163__ + __rightop1164__;
    int __rightop1160__ = __leftop1161__ + __rightop1162__;
    int __rightop1158__ = __leftop1159__ + __rightop1160__;
    int __rightop1156__ = __leftop1157__ + __rightop1158__;
    int __sizeof1154__ = __leftop1155__ + __rightop1156__;
    int __high1167__ = __left1152__ + __sizeof1154__;
    assertvalidmemory(__left1152__, __high1167__);
    // __left1152__ = d.s
    // __offsetinbits1168__ <-- (32 + (32 + (32 + 0)))
    int __leftop1169__ = 32;
    int __leftop1171__ = 32;
    int __leftop1173__ = 32;
    int __rightop1174__ = 0;
    int __rightop1172__ = __leftop1173__ + __rightop1174__;
    int __rightop1170__ = __leftop1171__ + __rightop1172__;
    int __offsetinbits1168__ = __leftop1169__ + __rightop1170__;
    // __offsetinbits1168__ = (32 + (32 + (32 + 0)))
    int __offset1175__ = __offsetinbits1168__ >> 3;
    int __shift1176__ = __offsetinbits1168__ - (__offset1175__ << 3);
    int __rightop1151__ = ((*(int *)(__left1152__ + __offset1175__))  >> __shift1176__) & 0xffffffff;
    int __leftop1141__ = __leftop1142__ < __rightop1151__;
    // __left1180__ <-- de
    int __left1180__ = (int) __de__; //varexpr
    // __left1180__ = de
    // __offsetinbits1181__ <-- ((8 * 124) + 0)
    int __leftop1183__ = 8;
    int __rightop1184__ = 124;
    int __leftop1182__ = __leftop1183__ * __rightop1184__;
    int __rightop1185__ = 0;
    int __offsetinbits1181__ = __leftop1182__ + __rightop1185__;
    // __offsetinbits1181__ = ((8 * 124) + 0)
    int __offset1186__ = __offsetinbits1181__ >> 3;
    int __shift1187__ = __offsetinbits1181__ - (__offset1186__ << 3);
    int __leftop1179__ = ((*(int *)(__left1180__ + __offset1186__))  >> __shift1187__) & 0xffffffff;
    int __rightop1188__ = 0;
    int __leftop1178__ = __leftop1179__ == __rightop1188__;
    int __rightop1177__ = !__leftop1178__;
    int __tempvar1140__ = __leftop1141__ && __rightop1177__;
    if (__tempvar1140__)
      {
      // __left1190__ <-- de
      int __left1190__ = (int) __de__; //varexpr
      // __left1190__ = de
      // __offsetinbits1191__ <-- ((8 * 124) + 0)
      int __leftop1193__ = 8;
      int __rightop1194__ = 124;
      int __leftop1192__ = __leftop1193__ * __rightop1194__;
      int __rightop1195__ = 0;
      int __offsetinbits1191__ = __leftop1192__ + __rightop1195__;
      // __offsetinbits1191__ = ((8 * 124) + 0)
      int __offset1196__ = __offsetinbits1191__ >> 3;
      int __shift1197__ = __offsetinbits1191__ - (__offset1196__ << 3);
      int __element1189__ = ((*(int *)(__left1190__ + __offset1196__))  >> __shift1197__) & 0xffffffff;
      int __addeditem1198__ = 1;
      __addeditem1198__ = __Inode___hash->add((int)__element1189__, (int)__element1189__);
      }
    }
  }


// build rule15
  {
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); )
    {
    int __de__ = (int) __de___iterator->next();
    //(de.inodenumber < d.s.NumberofInodes)
    // __left1201__ <-- de
    int __left1201__ = (int) __de__; //varexpr
    // __left1201__ = de
    // __offsetinbits1202__ <-- ((8 * 124) + 0)
    int __leftop1204__ = 8;
    int __rightop1205__ = 124;
    int __leftop1203__ = __leftop1204__ * __rightop1205__;
    int __rightop1206__ = 0;
    int __offsetinbits1202__ = __leftop1203__ + __rightop1206__;
    // __offsetinbits1202__ = ((8 * 124) + 0)
    int __offset1207__ = __offsetinbits1202__ >> 3;
    int __shift1208__ = __offsetinbits1202__ - (__offset1207__ << 3);
    int __leftop1200__ = ((*(int *)(__left1201__ + __offset1207__))  >> __shift1208__) & 0xffffffff;
    // __left1210__ <-- d.s
    // __left1211__ <-- d
    int __left1211__ = (int) d; //varexpr
    // __left1211__ = d
    int __left1210__ = (__left1211__ + 0);
    int __leftop1213__ = 32;
    int __leftop1215__ = 32;
    int __leftop1217__ = 32;
    int __leftop1219__ = 32;
    int __leftop1221__ = 32;
    int __leftop1223__ = 32;
    int __rightop1224__ = 0;
    int __rightop1222__ = __leftop1223__ + __rightop1224__;
    int __rightop1220__ = __leftop1221__ + __rightop1222__;
    int __rightop1218__ = __leftop1219__ + __rightop1220__;
    int __rightop1216__ = __leftop1217__ + __rightop1218__;
    int __rightop1214__ = __leftop1215__ + __rightop1216__;
    int __sizeof1212__ = __leftop1213__ + __rightop1214__;
    int __high1225__ = __left1210__ + __sizeof1212__;
    assertvalidmemory(__left1210__, __high1225__);
    // __left1210__ = d.s
    // __offsetinbits1226__ <-- (32 + (32 + (32 + 0)))
    int __leftop1227__ = 32;
    int __leftop1229__ = 32;
    int __leftop1231__ = 32;
    int __rightop1232__ = 0;
    int __rightop1230__ = __leftop1231__ + __rightop1232__;
    int __rightop1228__ = __leftop1229__ + __rightop1230__;
    int __offsetinbits1226__ = __leftop1227__ + __rightop1228__;
    // __offsetinbits1226__ = (32 + (32 + (32 + 0)))
    int __offset1233__ = __offsetinbits1226__ >> 3;
    int __shift1234__ = __offsetinbits1226__ - (__offset1233__ << 3);
    int __rightop1209__ = ((*(int *)(__left1210__ + __offset1233__))  >> __shift1234__) & 0xffffffff;
    int __tempvar1199__ = __leftop1200__ < __rightop1209__;
    if (__tempvar1199__)
      {
      int __leftele1235__ = (int) __de__; //varexpr
      // __left1237__ <-- de
      int __left1237__ = (int) __de__; //varexpr
      // __left1237__ = de
      // __offsetinbits1238__ <-- ((8 * 124) + 0)
      int __leftop1240__ = 8;
      int __rightop1241__ = 124;
      int __leftop1239__ = __leftop1240__ * __rightop1241__;
      int __rightop1242__ = 0;
      int __offsetinbits1238__ = __leftop1239__ + __rightop1242__;
      // __offsetinbits1238__ = ((8 * 124) + 0)
      int __offset1243__ = __offsetinbits1238__ >> 3;
      int __shift1244__ = __offsetinbits1238__ - (__offset1243__ << 3);
      int __rightele1236__ = ((*(int *)(__left1237__ + __offset1243__))  >> __shift1244__) & 0xffffffff;
      int __addeditem1246__;
      __addeditem1246__ = __inodeof___hashinv->add((int)__rightele1236__, (int)__leftele1235__);
      }
    }
  }


// build rule14
  {
  for (SimpleIterator* __de___iterator = __DirectoryEntry___hash->iterator(); __de___iterator->hasNext(); )
    {
    int __de__ = (int) __de___iterator->next();
    //((de.inodenumber < d.s.NumberofInodes) && ((de.inodenumber == 0)))
    // __left1250__ <-- de
    int __left1250__ = (int) __de__; //varexpr
    // __left1250__ = de
    // __offsetinbits1251__ <-- ((8 * 124) + 0)
    int __leftop1253__ = 8;
    int __rightop1254__ = 124;
    int __leftop1252__ = __leftop1253__ * __rightop1254__;
    int __rightop1255__ = 0;
    int __offsetinbits1251__ = __leftop1252__ + __rightop1255__;
    // __offsetinbits1251__ = ((8 * 124) + 0)
    int __offset1256__ = __offsetinbits1251__ >> 3;
    int __shift1257__ = __offsetinbits1251__ - (__offset1256__ << 3);
    int __leftop1249__ = ((*(int *)(__left1250__ + __offset1256__))  >> __shift1257__) & 0xffffffff;
    // __left1259__ <-- d.s
    // __left1260__ <-- d
    int __left1260__ = (int) d; //varexpr
    // __left1260__ = d
    int __left1259__ = (__left1260__ + 0);
    int __leftop1262__ = 32;
    int __leftop1264__ = 32;
    int __leftop1266__ = 32;
    int __leftop1268__ = 32;
    int __leftop1270__ = 32;
    int __leftop1272__ = 32;
    int __rightop1273__ = 0;
    int __rightop1271__ = __leftop1272__ + __rightop1273__;
    int __rightop1269__ = __leftop1270__ + __rightop1271__;
    int __rightop1267__ = __leftop1268__ + __rightop1269__;
    int __rightop1265__ = __leftop1266__ + __rightop1267__;
    int __rightop1263__ = __leftop1264__ + __rightop1265__;
    int __sizeof1261__ = __leftop1262__ + __rightop1263__;
    int __high1274__ = __left1259__ + __sizeof1261__;
    assertvalidmemory(__left1259__, __high1274__);
    // __left1259__ = d.s
    // __offsetinbits1275__ <-- (32 + (32 + (32 + 0)))
    int __leftop1276__ = 32;
    int __leftop1278__ = 32;
    int __leftop1280__ = 32;
    int __rightop1281__ = 0;
    int __rightop1279__ = __leftop1280__ + __rightop1281__;
    int __rightop1277__ = __leftop1278__ + __rightop1279__;
    int __offsetinbits1275__ = __leftop1276__ + __rightop1277__;
    // __offsetinbits1275__ = (32 + (32 + (32 + 0)))
    int __offset1282__ = __offsetinbits1275__ >> 3;
    int __shift1283__ = __offsetinbits1275__ - (__offset1282__ << 3);
    int __rightop1258__ = ((*(int *)(__left1259__ + __offset1282__))  >> __shift1283__) & 0xffffffff;
    int __leftop1248__ = __leftop1249__ < __rightop1258__;
    // __left1287__ <-- de
    int __left1287__ = (int) __de__; //varexpr
    // __left1287__ = de
    // __offsetinbits1288__ <-- ((8 * 124) + 0)
    int __leftop1290__ = 8;
    int __rightop1291__ = 124;
    int __leftop1289__ = __leftop1290__ * __rightop1291__;
    int __rightop1292__ = 0;
    int __offsetinbits1288__ = __leftop1289__ + __rightop1292__;
    // __offsetinbits1288__ = ((8 * 124) + 0)
    int __offset1293__ = __offsetinbits1288__ >> 3;
    int __shift1294__ = __offsetinbits1288__ - (__offset1293__ << 3);
    int __leftop1286__ = ((*(int *)(__left1287__ + __offset1293__))  >> __shift1294__) & 0xffffffff;
    int __rightop1295__ = 0;
    int __leftop1285__ = __leftop1286__ == __rightop1295__;
    int __rightop1284__ = !__leftop1285__;
    int __tempvar1247__ = __leftop1248__ && __rightop1284__;
    if (__tempvar1247__)
      {
      // __left1297__ <-- de
      int __left1297__ = (int) __de__; //varexpr
      // __left1297__ = de
      // __offsetinbits1298__ <-- ((8 * 124) + 0)
      int __leftop1300__ = 8;
      int __rightop1301__ = 124;
      int __leftop1299__ = __leftop1300__ * __rightop1301__;
      int __rightop1302__ = 0;
      int __offsetinbits1298__ = __leftop1299__ + __rightop1302__;
      // __offsetinbits1298__ = ((8 * 124) + 0)
      int __offset1303__ = __offsetinbits1298__ >> 3;
      int __shift1304__ = __offsetinbits1298__ - (__offset1303__ << 3);
      int __element1296__ = ((*(int *)(__left1297__ + __offset1303__))  >> __shift1304__) & 0xffffffff;
      int __addeditem1305__ = 1;
      __addeditem1305__ = __FileInode___hash->add((int)__element1296__, (int)__element1296__);
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
      int __tempvar1306__ = 1;
      if (__tempvar1306__)
        {
        int __leftele1307__ = (int) __j__; //varexpr
        // __left1309__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left1310__ <-- cast(__InodeTable__, d.b[itb])
        // __left1312__ <-- d
        int __left1312__ = (int) d; //varexpr
        // __left1312__ = d
        // __offsetinbits1313__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1314__ = 0;
        int __leftop1318__ = 8;
        // __left1320__ <-- d.s
        // __left1321__ <-- d
        int __left1321__ = (int) d; //varexpr
        // __left1321__ = d
        int __left1320__ = (__left1321__ + 0);
        int __leftop1323__ = 32;
        int __leftop1325__ = 32;
        int __leftop1327__ = 32;
        int __leftop1329__ = 32;
        int __leftop1331__ = 32;
        int __leftop1333__ = 32;
        int __rightop1334__ = 0;
        int __rightop1332__ = __leftop1333__ + __rightop1334__;
        int __rightop1330__ = __leftop1331__ + __rightop1332__;
        int __rightop1328__ = __leftop1329__ + __rightop1330__;
        int __rightop1326__ = __leftop1327__ + __rightop1328__;
        int __rightop1324__ = __leftop1325__ + __rightop1326__;
        int __sizeof1322__ = __leftop1323__ + __rightop1324__;
        int __high1335__ = __left1320__ + __sizeof1322__;
        assertvalidmemory(__left1320__, __high1335__);
        // __left1320__ = d.s
        // __offsetinbits1336__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1337__ = 32;
        int __leftop1339__ = 32;
        int __leftop1341__ = 32;
        int __leftop1343__ = 32;
        int __leftop1345__ = 32;
        int __rightop1346__ = 0;
        int __rightop1344__ = __leftop1345__ + __rightop1346__;
        int __rightop1342__ = __leftop1343__ + __rightop1344__;
        int __rightop1340__ = __leftop1341__ + __rightop1342__;
        int __rightop1338__ = __leftop1339__ + __rightop1340__;
        int __offsetinbits1336__ = __leftop1337__ + __rightop1338__;
        // __offsetinbits1336__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1347__ = __offsetinbits1336__ >> 3;
        int __shift1348__ = __offsetinbits1336__ - (__offset1347__ << 3);
        int __rightop1319__ = ((*(int *)(__left1320__ + __offset1347__))  >> __shift1348__) & 0xffffffff;
        int __leftop1317__ = __leftop1318__ * __rightop1319__;
        int __rightop1349__ = 0;
        int __leftop1316__ = __leftop1317__ + __rightop1349__;
        int __rightop1350__ = (int) __itb__; //varexpr
        int __rightop1315__ = __leftop1316__ * __rightop1350__;
        int __offsetinbits1313__ = __leftop1314__ + __rightop1315__;
        // __offsetinbits1313__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1351__ = __offsetinbits1313__ >> 3;
        int __expr1311__ = (__left1312__ + __offset1351__);
        int __leftop1354__ = 8;
        // __left1356__ <-- d.s
        // __left1357__ <-- d
        int __left1357__ = (int) d; //varexpr
        // __left1357__ = d
        int __left1356__ = (__left1357__ + 0);
        int __leftop1359__ = 32;
        int __leftop1361__ = 32;
        int __leftop1363__ = 32;
        int __leftop1365__ = 32;
        int __leftop1367__ = 32;
        int __leftop1369__ = 32;
        int __rightop1370__ = 0;
        int __rightop1368__ = __leftop1369__ + __rightop1370__;
        int __rightop1366__ = __leftop1367__ + __rightop1368__;
        int __rightop1364__ = __leftop1365__ + __rightop1366__;
        int __rightop1362__ = __leftop1363__ + __rightop1364__;
        int __rightop1360__ = __leftop1361__ + __rightop1362__;
        int __sizeof1358__ = __leftop1359__ + __rightop1360__;
        int __high1371__ = __left1356__ + __sizeof1358__;
        assertvalidmemory(__left1356__, __high1371__);
        // __left1356__ = d.s
        // __offsetinbits1372__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1373__ = 32;
        int __leftop1375__ = 32;
        int __leftop1377__ = 32;
        int __leftop1379__ = 32;
        int __leftop1381__ = 32;
        int __rightop1382__ = 0;
        int __rightop1380__ = __leftop1381__ + __rightop1382__;
        int __rightop1378__ = __leftop1379__ + __rightop1380__;
        int __rightop1376__ = __leftop1377__ + __rightop1378__;
        int __rightop1374__ = __leftop1375__ + __rightop1376__;
        int __offsetinbits1372__ = __leftop1373__ + __rightop1374__;
        // __offsetinbits1372__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1383__ = __offsetinbits1372__ >> 3;
        int __shift1384__ = __offsetinbits1372__ - (__offset1383__ << 3);
        int __rightop1355__ = ((*(int *)(__left1356__ + __offset1383__))  >> __shift1384__) & 0xffffffff;
        int __leftop1353__ = __leftop1354__ * __rightop1355__;
        int __rightop1385__ = 0;
        int __sizeof1352__ = __leftop1353__ + __rightop1385__;
        int __high1386__ = __expr1311__ + __sizeof1352__;
        assertvalidmemory(__expr1311__, __high1386__);
        int __left1310__ = (int) __expr1311__;
        // __left1310__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1387__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __leftop1388__ = 0;
        int __leftop1391__ = 32;
        int __leftop1394__ = 32;
        int __rightop1395__ = 12;
        int __leftop1393__ = __leftop1394__ * __rightop1395__;
        int __leftop1397__ = 32;
        int __rightop1398__ = 0;
        int __rightop1396__ = __leftop1397__ + __rightop1398__;
        int __rightop1392__ = __leftop1393__ + __rightop1396__;
        int __leftop1390__ = __leftop1391__ + __rightop1392__;
        int __rightop1399__ = (int) __j__; //varexpr
        int __rightop1389__ = __leftop1390__ * __rightop1399__;
        int __offsetinbits1387__ = __leftop1388__ + __rightop1389__;
        // __offsetinbits1387__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __offset1400__ = __offsetinbits1387__ >> 3;
        int __left1309__ = (__left1310__ + __offset1400__);
        int __leftop1402__ = 32;
        int __leftop1405__ = 32;
        int __rightop1406__ = 12;
        int __leftop1404__ = __leftop1405__ * __rightop1406__;
        int __leftop1408__ = 32;
        int __rightop1409__ = 0;
        int __rightop1407__ = __leftop1408__ + __rightop1409__;
        int __rightop1403__ = __leftop1404__ + __rightop1407__;
        int __sizeof1401__ = __leftop1402__ + __rightop1403__;
        int __high1410__ = __left1309__ + __sizeof1401__;
        assertvalidmemory(__left1309__, __high1410__);
        // __left1309__ = cast(__InodeTable__, d.b[itb]).itable[j]
        // __offsetinbits1411__ <-- ((32 * 12) + (32 + 0))
        int __leftop1413__ = 32;
        int __rightop1414__ = 12;
        int __leftop1412__ = __leftop1413__ * __rightop1414__;
        int __leftop1416__ = 32;
        int __rightop1417__ = 0;
        int __rightop1415__ = __leftop1416__ + __rightop1417__;
        int __offsetinbits1411__ = __leftop1412__ + __rightop1415__;
        // __offsetinbits1411__ = ((32 * 12) + (32 + 0))
        int __offset1418__ = __offsetinbits1411__ >> 3;
        int __shift1419__ = __offsetinbits1411__ - (__offset1418__ << 3);
        int __rightele1308__ = ((*(int *)(__left1309__ + __offset1418__))  >> __shift1419__) & 0xffffffff;
        int __addeditem1421__;
        __addeditem1421__ = __referencecount___hash->add((int)__leftele1307__, (int)__rightele1308__);
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
      int __tempvar1422__ = 0;
      int __tempvar1423__ = 11;
      for (int __j__ = __tempvar1422__; __j__ <= __tempvar1423__; __j__++)
        {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] < d.s.NumberofBlocks) && ((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0)))
        // __left1427__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left1428__ <-- cast(__InodeTable__, d.b[itb])
        // __left1430__ <-- d
        int __left1430__ = (int) d; //varexpr
        // __left1430__ = d
        // __offsetinbits1431__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1432__ = 0;
        int __leftop1436__ = 8;
        // __left1438__ <-- d.s
        // __left1439__ <-- d
        int __left1439__ = (int) d; //varexpr
        // __left1439__ = d
        int __left1438__ = (__left1439__ + 0);
        int __leftop1441__ = 32;
        int __leftop1443__ = 32;
        int __leftop1445__ = 32;
        int __leftop1447__ = 32;
        int __leftop1449__ = 32;
        int __leftop1451__ = 32;
        int __rightop1452__ = 0;
        int __rightop1450__ = __leftop1451__ + __rightop1452__;
        int __rightop1448__ = __leftop1449__ + __rightop1450__;
        int __rightop1446__ = __leftop1447__ + __rightop1448__;
        int __rightop1444__ = __leftop1445__ + __rightop1446__;
        int __rightop1442__ = __leftop1443__ + __rightop1444__;
        int __sizeof1440__ = __leftop1441__ + __rightop1442__;
        int __high1453__ = __left1438__ + __sizeof1440__;
        assertvalidmemory(__left1438__, __high1453__);
        // __left1438__ = d.s
        // __offsetinbits1454__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1455__ = 32;
        int __leftop1457__ = 32;
        int __leftop1459__ = 32;
        int __leftop1461__ = 32;
        int __leftop1463__ = 32;
        int __rightop1464__ = 0;
        int __rightop1462__ = __leftop1463__ + __rightop1464__;
        int __rightop1460__ = __leftop1461__ + __rightop1462__;
        int __rightop1458__ = __leftop1459__ + __rightop1460__;
        int __rightop1456__ = __leftop1457__ + __rightop1458__;
        int __offsetinbits1454__ = __leftop1455__ + __rightop1456__;
        // __offsetinbits1454__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1465__ = __offsetinbits1454__ >> 3;
        int __shift1466__ = __offsetinbits1454__ - (__offset1465__ << 3);
        int __rightop1437__ = ((*(int *)(__left1438__ + __offset1465__))  >> __shift1466__) & 0xffffffff;
        int __leftop1435__ = __leftop1436__ * __rightop1437__;
        int __rightop1467__ = 0;
        int __leftop1434__ = __leftop1435__ + __rightop1467__;
        int __rightop1468__ = (int) __itb__; //varexpr
        int __rightop1433__ = __leftop1434__ * __rightop1468__;
        int __offsetinbits1431__ = __leftop1432__ + __rightop1433__;
        // __offsetinbits1431__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1469__ = __offsetinbits1431__ >> 3;
        int __expr1429__ = (__left1430__ + __offset1469__);
        int __leftop1472__ = 8;
        // __left1474__ <-- d.s
        // __left1475__ <-- d
        int __left1475__ = (int) d; //varexpr
        // __left1475__ = d
        int __left1474__ = (__left1475__ + 0);
        int __leftop1477__ = 32;
        int __leftop1479__ = 32;
        int __leftop1481__ = 32;
        int __leftop1483__ = 32;
        int __leftop1485__ = 32;
        int __leftop1487__ = 32;
        int __rightop1488__ = 0;
        int __rightop1486__ = __leftop1487__ + __rightop1488__;
        int __rightop1484__ = __leftop1485__ + __rightop1486__;
        int __rightop1482__ = __leftop1483__ + __rightop1484__;
        int __rightop1480__ = __leftop1481__ + __rightop1482__;
        int __rightop1478__ = __leftop1479__ + __rightop1480__;
        int __sizeof1476__ = __leftop1477__ + __rightop1478__;
        int __high1489__ = __left1474__ + __sizeof1476__;
        assertvalidmemory(__left1474__, __high1489__);
        // __left1474__ = d.s
        // __offsetinbits1490__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1491__ = 32;
        int __leftop1493__ = 32;
        int __leftop1495__ = 32;
        int __leftop1497__ = 32;
        int __leftop1499__ = 32;
        int __rightop1500__ = 0;
        int __rightop1498__ = __leftop1499__ + __rightop1500__;
        int __rightop1496__ = __leftop1497__ + __rightop1498__;
        int __rightop1494__ = __leftop1495__ + __rightop1496__;
        int __rightop1492__ = __leftop1493__ + __rightop1494__;
        int __offsetinbits1490__ = __leftop1491__ + __rightop1492__;
        // __offsetinbits1490__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1501__ = __offsetinbits1490__ >> 3;
        int __shift1502__ = __offsetinbits1490__ - (__offset1501__ << 3);
        int __rightop1473__ = ((*(int *)(__left1474__ + __offset1501__))  >> __shift1502__) & 0xffffffff;
        int __leftop1471__ = __leftop1472__ * __rightop1473__;
        int __rightop1503__ = 0;
        int __sizeof1470__ = __leftop1471__ + __rightop1503__;
        int __high1504__ = __expr1429__ + __sizeof1470__;
        assertvalidmemory(__expr1429__, __high1504__);
        int __left1428__ = (int) __expr1429__;
        // __left1428__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1505__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop1506__ = 0;
        int __leftop1509__ = 32;
        int __leftop1512__ = 32;
        int __rightop1513__ = 12;
        int __leftop1511__ = __leftop1512__ * __rightop1513__;
        int __leftop1515__ = 32;
        int __rightop1516__ = 0;
        int __rightop1514__ = __leftop1515__ + __rightop1516__;
        int __rightop1510__ = __leftop1511__ + __rightop1514__;
        int __leftop1508__ = __leftop1509__ + __rightop1510__;
        int __rightop1517__ = (int) __i__; //varexpr
        int __rightop1507__ = __leftop1508__ * __rightop1517__;
        int __offsetinbits1505__ = __leftop1506__ + __rightop1507__;
        // __offsetinbits1505__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset1518__ = __offsetinbits1505__ >> 3;
        int __left1427__ = (__left1428__ + __offset1518__);
        int __leftop1520__ = 32;
        int __leftop1523__ = 32;
        int __rightop1524__ = 12;
        int __leftop1522__ = __leftop1523__ * __rightop1524__;
        int __leftop1526__ = 32;
        int __rightop1527__ = 0;
        int __rightop1525__ = __leftop1526__ + __rightop1527__;
        int __rightop1521__ = __leftop1522__ + __rightop1525__;
        int __sizeof1519__ = __leftop1520__ + __rightop1521__;
        int __high1528__ = __left1427__ + __sizeof1519__;
        assertvalidmemory(__left1427__, __high1528__);
        // __left1427__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits1529__ <-- ((32 + 0) + (32 * j))
        int __leftop1531__ = 32;
        int __rightop1532__ = 0;
        int __leftop1530__ = __leftop1531__ + __rightop1532__;
        int __leftop1534__ = 32;
        int __rightop1535__ = (int) __j__; //varexpr
        int __rightop1533__ = __leftop1534__ * __rightop1535__;
        int __offsetinbits1529__ = __leftop1530__ + __rightop1533__;
        // __offsetinbits1529__ = ((32 + 0) + (32 * j))
        int __offset1536__ = __offsetinbits1529__ >> 3;
        int __shift1537__ = __offsetinbits1529__ - (__offset1536__ << 3);
        int __leftop1426__ = ((*(int *)(__left1427__ + __offset1536__))  >> __shift1537__) & 0xffffffff;
        // __left1539__ <-- d.s
        // __left1540__ <-- d
        int __left1540__ = (int) d; //varexpr
        // __left1540__ = d
        int __left1539__ = (__left1540__ + 0);
        int __leftop1542__ = 32;
        int __leftop1544__ = 32;
        int __leftop1546__ = 32;
        int __leftop1548__ = 32;
        int __leftop1550__ = 32;
        int __leftop1552__ = 32;
        int __rightop1553__ = 0;
        int __rightop1551__ = __leftop1552__ + __rightop1553__;
        int __rightop1549__ = __leftop1550__ + __rightop1551__;
        int __rightop1547__ = __leftop1548__ + __rightop1549__;
        int __rightop1545__ = __leftop1546__ + __rightop1547__;
        int __rightop1543__ = __leftop1544__ + __rightop1545__;
        int __sizeof1541__ = __leftop1542__ + __rightop1543__;
        int __high1554__ = __left1539__ + __sizeof1541__;
        assertvalidmemory(__left1539__, __high1554__);
        // __left1539__ = d.s
        // __offsetinbits1555__ <-- (32 + (32 + 0))
        int __leftop1556__ = 32;
        int __leftop1558__ = 32;
        int __rightop1559__ = 0;
        int __rightop1557__ = __leftop1558__ + __rightop1559__;
        int __offsetinbits1555__ = __leftop1556__ + __rightop1557__;
        // __offsetinbits1555__ = (32 + (32 + 0))
        int __offset1560__ = __offsetinbits1555__ >> 3;
        int __shift1561__ = __offsetinbits1555__ - (__offset1560__ << 3);
        int __rightop1538__ = ((*(int *)(__left1539__ + __offset1560__))  >> __shift1561__) & 0xffffffff;
        int __leftop1425__ = __leftop1426__ < __rightop1538__;
        // __left1565__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left1566__ <-- cast(__InodeTable__, d.b[itb])
        // __left1568__ <-- d
        int __left1568__ = (int) d; //varexpr
        // __left1568__ = d
        // __offsetinbits1569__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1570__ = 0;
        int __leftop1574__ = 8;
        // __left1576__ <-- d.s
        // __left1577__ <-- d
        int __left1577__ = (int) d; //varexpr
        // __left1577__ = d
        int __left1576__ = (__left1577__ + 0);
        int __leftop1579__ = 32;
        int __leftop1581__ = 32;
        int __leftop1583__ = 32;
        int __leftop1585__ = 32;
        int __leftop1587__ = 32;
        int __leftop1589__ = 32;
        int __rightop1590__ = 0;
        int __rightop1588__ = __leftop1589__ + __rightop1590__;
        int __rightop1586__ = __leftop1587__ + __rightop1588__;
        int __rightop1584__ = __leftop1585__ + __rightop1586__;
        int __rightop1582__ = __leftop1583__ + __rightop1584__;
        int __rightop1580__ = __leftop1581__ + __rightop1582__;
        int __sizeof1578__ = __leftop1579__ + __rightop1580__;
        int __high1591__ = __left1576__ + __sizeof1578__;
        assertvalidmemory(__left1576__, __high1591__);
        // __left1576__ = d.s
        // __offsetinbits1592__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1593__ = 32;
        int __leftop1595__ = 32;
        int __leftop1597__ = 32;
        int __leftop1599__ = 32;
        int __leftop1601__ = 32;
        int __rightop1602__ = 0;
        int __rightop1600__ = __leftop1601__ + __rightop1602__;
        int __rightop1598__ = __leftop1599__ + __rightop1600__;
        int __rightop1596__ = __leftop1597__ + __rightop1598__;
        int __rightop1594__ = __leftop1595__ + __rightop1596__;
        int __offsetinbits1592__ = __leftop1593__ + __rightop1594__;
        // __offsetinbits1592__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1603__ = __offsetinbits1592__ >> 3;
        int __shift1604__ = __offsetinbits1592__ - (__offset1603__ << 3);
        int __rightop1575__ = ((*(int *)(__left1576__ + __offset1603__))  >> __shift1604__) & 0xffffffff;
        int __leftop1573__ = __leftop1574__ * __rightop1575__;
        int __rightop1605__ = 0;
        int __leftop1572__ = __leftop1573__ + __rightop1605__;
        int __rightop1606__ = (int) __itb__; //varexpr
        int __rightop1571__ = __leftop1572__ * __rightop1606__;
        int __offsetinbits1569__ = __leftop1570__ + __rightop1571__;
        // __offsetinbits1569__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1607__ = __offsetinbits1569__ >> 3;
        int __expr1567__ = (__left1568__ + __offset1607__);
        int __leftop1610__ = 8;
        // __left1612__ <-- d.s
        // __left1613__ <-- d
        int __left1613__ = (int) d; //varexpr
        // __left1613__ = d
        int __left1612__ = (__left1613__ + 0);
        int __leftop1615__ = 32;
        int __leftop1617__ = 32;
        int __leftop1619__ = 32;
        int __leftop1621__ = 32;
        int __leftop1623__ = 32;
        int __leftop1625__ = 32;
        int __rightop1626__ = 0;
        int __rightop1624__ = __leftop1625__ + __rightop1626__;
        int __rightop1622__ = __leftop1623__ + __rightop1624__;
        int __rightop1620__ = __leftop1621__ + __rightop1622__;
        int __rightop1618__ = __leftop1619__ + __rightop1620__;
        int __rightop1616__ = __leftop1617__ + __rightop1618__;
        int __sizeof1614__ = __leftop1615__ + __rightop1616__;
        int __high1627__ = __left1612__ + __sizeof1614__;
        assertvalidmemory(__left1612__, __high1627__);
        // __left1612__ = d.s
        // __offsetinbits1628__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1629__ = 32;
        int __leftop1631__ = 32;
        int __leftop1633__ = 32;
        int __leftop1635__ = 32;
        int __leftop1637__ = 32;
        int __rightop1638__ = 0;
        int __rightop1636__ = __leftop1637__ + __rightop1638__;
        int __rightop1634__ = __leftop1635__ + __rightop1636__;
        int __rightop1632__ = __leftop1633__ + __rightop1634__;
        int __rightop1630__ = __leftop1631__ + __rightop1632__;
        int __offsetinbits1628__ = __leftop1629__ + __rightop1630__;
        // __offsetinbits1628__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1639__ = __offsetinbits1628__ >> 3;
        int __shift1640__ = __offsetinbits1628__ - (__offset1639__ << 3);
        int __rightop1611__ = ((*(int *)(__left1612__ + __offset1639__))  >> __shift1640__) & 0xffffffff;
        int __leftop1609__ = __leftop1610__ * __rightop1611__;
        int __rightop1641__ = 0;
        int __sizeof1608__ = __leftop1609__ + __rightop1641__;
        int __high1642__ = __expr1567__ + __sizeof1608__;
        assertvalidmemory(__expr1567__, __high1642__);
        int __left1566__ = (int) __expr1567__;
        // __left1566__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1643__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop1644__ = 0;
        int __leftop1647__ = 32;
        int __leftop1650__ = 32;
        int __rightop1651__ = 12;
        int __leftop1649__ = __leftop1650__ * __rightop1651__;
        int __leftop1653__ = 32;
        int __rightop1654__ = 0;
        int __rightop1652__ = __leftop1653__ + __rightop1654__;
        int __rightop1648__ = __leftop1649__ + __rightop1652__;
        int __leftop1646__ = __leftop1647__ + __rightop1648__;
        int __rightop1655__ = (int) __i__; //varexpr
        int __rightop1645__ = __leftop1646__ * __rightop1655__;
        int __offsetinbits1643__ = __leftop1644__ + __rightop1645__;
        // __offsetinbits1643__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset1656__ = __offsetinbits1643__ >> 3;
        int __left1565__ = (__left1566__ + __offset1656__);
        int __leftop1658__ = 32;
        int __leftop1661__ = 32;
        int __rightop1662__ = 12;
        int __leftop1660__ = __leftop1661__ * __rightop1662__;
        int __leftop1664__ = 32;
        int __rightop1665__ = 0;
        int __rightop1663__ = __leftop1664__ + __rightop1665__;
        int __rightop1659__ = __leftop1660__ + __rightop1663__;
        int __sizeof1657__ = __leftop1658__ + __rightop1659__;
        int __high1666__ = __left1565__ + __sizeof1657__;
        assertvalidmemory(__left1565__, __high1666__);
        // __left1565__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits1667__ <-- ((32 + 0) + (32 * j))
        int __leftop1669__ = 32;
        int __rightop1670__ = 0;
        int __leftop1668__ = __leftop1669__ + __rightop1670__;
        int __leftop1672__ = 32;
        int __rightop1673__ = (int) __j__; //varexpr
        int __rightop1671__ = __leftop1672__ * __rightop1673__;
        int __offsetinbits1667__ = __leftop1668__ + __rightop1671__;
        // __offsetinbits1667__ = ((32 + 0) + (32 * j))
        int __offset1674__ = __offsetinbits1667__ >> 3;
        int __shift1675__ = __offsetinbits1667__ - (__offset1674__ << 3);
        int __leftop1564__ = ((*(int *)(__left1565__ + __offset1674__))  >> __shift1675__) & 0xffffffff;
        int __rightop1676__ = 0;
        int __leftop1563__ = __leftop1564__ == __rightop1676__;
        int __rightop1562__ = !__leftop1563__;
        int __tempvar1424__ = __leftop1425__ && __rightop1562__;
        if (__tempvar1424__)
          {
          // __left1678__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left1679__ <-- cast(__InodeTable__, d.b[itb])
          // __left1681__ <-- d
          int __left1681__ = (int) d; //varexpr
          // __left1681__ = d
          // __offsetinbits1682__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop1683__ = 0;
          int __leftop1687__ = 8;
          // __left1689__ <-- d.s
          // __left1690__ <-- d
          int __left1690__ = (int) d; //varexpr
          // __left1690__ = d
          int __left1689__ = (__left1690__ + 0);
          int __leftop1692__ = 32;
          int __leftop1694__ = 32;
          int __leftop1696__ = 32;
          int __leftop1698__ = 32;
          int __leftop1700__ = 32;
          int __leftop1702__ = 32;
          int __rightop1703__ = 0;
          int __rightop1701__ = __leftop1702__ + __rightop1703__;
          int __rightop1699__ = __leftop1700__ + __rightop1701__;
          int __rightop1697__ = __leftop1698__ + __rightop1699__;
          int __rightop1695__ = __leftop1696__ + __rightop1697__;
          int __rightop1693__ = __leftop1694__ + __rightop1695__;
          int __sizeof1691__ = __leftop1692__ + __rightop1693__;
          int __high1704__ = __left1689__ + __sizeof1691__;
          assertvalidmemory(__left1689__, __high1704__);
          // __left1689__ = d.s
          // __offsetinbits1705__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1706__ = 32;
          int __leftop1708__ = 32;
          int __leftop1710__ = 32;
          int __leftop1712__ = 32;
          int __leftop1714__ = 32;
          int __rightop1715__ = 0;
          int __rightop1713__ = __leftop1714__ + __rightop1715__;
          int __rightop1711__ = __leftop1712__ + __rightop1713__;
          int __rightop1709__ = __leftop1710__ + __rightop1711__;
          int __rightop1707__ = __leftop1708__ + __rightop1709__;
          int __offsetinbits1705__ = __leftop1706__ + __rightop1707__;
          // __offsetinbits1705__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1716__ = __offsetinbits1705__ >> 3;
          int __shift1717__ = __offsetinbits1705__ - (__offset1716__ << 3);
          int __rightop1688__ = ((*(int *)(__left1689__ + __offset1716__))  >> __shift1717__) & 0xffffffff;
          int __leftop1686__ = __leftop1687__ * __rightop1688__;
          int __rightop1718__ = 0;
          int __leftop1685__ = __leftop1686__ + __rightop1718__;
          int __rightop1719__ = (int) __itb__; //varexpr
          int __rightop1684__ = __leftop1685__ * __rightop1719__;
          int __offsetinbits1682__ = __leftop1683__ + __rightop1684__;
          // __offsetinbits1682__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset1720__ = __offsetinbits1682__ >> 3;
          int __expr1680__ = (__left1681__ + __offset1720__);
          int __leftop1723__ = 8;
          // __left1725__ <-- d.s
          // __left1726__ <-- d
          int __left1726__ = (int) d; //varexpr
          // __left1726__ = d
          int __left1725__ = (__left1726__ + 0);
          int __leftop1728__ = 32;
          int __leftop1730__ = 32;
          int __leftop1732__ = 32;
          int __leftop1734__ = 32;
          int __leftop1736__ = 32;
          int __leftop1738__ = 32;
          int __rightop1739__ = 0;
          int __rightop1737__ = __leftop1738__ + __rightop1739__;
          int __rightop1735__ = __leftop1736__ + __rightop1737__;
          int __rightop1733__ = __leftop1734__ + __rightop1735__;
          int __rightop1731__ = __leftop1732__ + __rightop1733__;
          int __rightop1729__ = __leftop1730__ + __rightop1731__;
          int __sizeof1727__ = __leftop1728__ + __rightop1729__;
          int __high1740__ = __left1725__ + __sizeof1727__;
          assertvalidmemory(__left1725__, __high1740__);
          // __left1725__ = d.s
          // __offsetinbits1741__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1742__ = 32;
          int __leftop1744__ = 32;
          int __leftop1746__ = 32;
          int __leftop1748__ = 32;
          int __leftop1750__ = 32;
          int __rightop1751__ = 0;
          int __rightop1749__ = __leftop1750__ + __rightop1751__;
          int __rightop1747__ = __leftop1748__ + __rightop1749__;
          int __rightop1745__ = __leftop1746__ + __rightop1747__;
          int __rightop1743__ = __leftop1744__ + __rightop1745__;
          int __offsetinbits1741__ = __leftop1742__ + __rightop1743__;
          // __offsetinbits1741__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1752__ = __offsetinbits1741__ >> 3;
          int __shift1753__ = __offsetinbits1741__ - (__offset1752__ << 3);
          int __rightop1724__ = ((*(int *)(__left1725__ + __offset1752__))  >> __shift1753__) & 0xffffffff;
          int __leftop1722__ = __leftop1723__ * __rightop1724__;
          int __rightop1754__ = 0;
          int __sizeof1721__ = __leftop1722__ + __rightop1754__;
          int __high1755__ = __expr1680__ + __sizeof1721__;
          assertvalidmemory(__expr1680__, __high1755__);
          int __left1679__ = (int) __expr1680__;
          // __left1679__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits1756__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop1757__ = 0;
          int __leftop1760__ = 32;
          int __leftop1763__ = 32;
          int __rightop1764__ = 12;
          int __leftop1762__ = __leftop1763__ * __rightop1764__;
          int __leftop1766__ = 32;
          int __rightop1767__ = 0;
          int __rightop1765__ = __leftop1766__ + __rightop1767__;
          int __rightop1761__ = __leftop1762__ + __rightop1765__;
          int __leftop1759__ = __leftop1760__ + __rightop1761__;
          int __rightop1768__ = (int) __i__; //varexpr
          int __rightop1758__ = __leftop1759__ * __rightop1768__;
          int __offsetinbits1756__ = __leftop1757__ + __rightop1758__;
          // __offsetinbits1756__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset1769__ = __offsetinbits1756__ >> 3;
          int __left1678__ = (__left1679__ + __offset1769__);
          int __leftop1771__ = 32;
          int __leftop1774__ = 32;
          int __rightop1775__ = 12;
          int __leftop1773__ = __leftop1774__ * __rightop1775__;
          int __leftop1777__ = 32;
          int __rightop1778__ = 0;
          int __rightop1776__ = __leftop1777__ + __rightop1778__;
          int __rightop1772__ = __leftop1773__ + __rightop1776__;
          int __sizeof1770__ = __leftop1771__ + __rightop1772__;
          int __high1779__ = __left1678__ + __sizeof1770__;
          assertvalidmemory(__left1678__, __high1779__);
          // __left1678__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits1780__ <-- ((32 + 0) + (32 * j))
          int __leftop1782__ = 32;
          int __rightop1783__ = 0;
          int __leftop1781__ = __leftop1782__ + __rightop1783__;
          int __leftop1785__ = 32;
          int __rightop1786__ = (int) __j__; //varexpr
          int __rightop1784__ = __leftop1785__ * __rightop1786__;
          int __offsetinbits1780__ = __leftop1781__ + __rightop1784__;
          // __offsetinbits1780__ = ((32 + 0) + (32 * j))
          int __offset1787__ = __offsetinbits1780__ >> 3;
          int __shift1788__ = __offsetinbits1780__ - (__offset1787__ << 3);
          int __element1677__ = ((*(int *)(__left1678__ + __offset1787__))  >> __shift1788__) & 0xffffffff;
          int __addeditem1789__ = 1;
          __addeditem1789__ = __FileBlock___hash->add((int)__element1677__, (int)__element1677__);
          }
        }
      }
    }
  }


// build rule8
  {
  int __tempvar1790__ = 0;
  // __left1793__ <-- d.s
  // __left1794__ <-- d
  int __left1794__ = (int) d; //varexpr
  // __left1794__ = d
  int __left1793__ = (__left1794__ + 0);
  int __leftop1796__ = 32;
  int __leftop1798__ = 32;
  int __leftop1800__ = 32;
  int __leftop1802__ = 32;
  int __leftop1804__ = 32;
  int __leftop1806__ = 32;
  int __rightop1807__ = 0;
  int __rightop1805__ = __leftop1806__ + __rightop1807__;
  int __rightop1803__ = __leftop1804__ + __rightop1805__;
  int __rightop1801__ = __leftop1802__ + __rightop1803__;
  int __rightop1799__ = __leftop1800__ + __rightop1801__;
  int __rightop1797__ = __leftop1798__ + __rightop1799__;
  int __sizeof1795__ = __leftop1796__ + __rightop1797__;
  int __high1808__ = __left1793__ + __sizeof1795__;
  assertvalidmemory(__left1793__, __high1808__);
  // __left1793__ = d.s
  // __offsetinbits1809__ <-- (32 + (32 + 0))
  int __leftop1810__ = 32;
  int __leftop1812__ = 32;
  int __rightop1813__ = 0;
  int __rightop1811__ = __leftop1812__ + __rightop1813__;
  int __offsetinbits1809__ = __leftop1810__ + __rightop1811__;
  // __offsetinbits1809__ = (32 + (32 + 0))
  int __offset1814__ = __offsetinbits1809__ >> 3;
  int __shift1815__ = __offsetinbits1809__ - (__offset1814__ << 3);
  int __leftop1792__ = ((*(int *)(__left1793__ + __offset1814__))  >> __shift1815__) & 0xffffffff;
  int __rightop1816__ = 1;
  int __tempvar1791__ = __leftop1792__ - __rightop1816__;
  for (int __j__ = __tempvar1790__; __j__ <= __tempvar1791__; __j__++)
    {
    //(j in? __UsedBlock__)
    int __element1819__ = (int) __j__; //varexpr
    int __leftop1818__ = __UsedBlock___hash->contains(__element1819__);
    int __tempvar1817__ = !__leftop1818__;
    if (__tempvar1817__)
      {
      int __element1820__ = (int) __j__; //varexpr
      int __addeditem1821__ = 1;
      __addeditem1821__ = __FreeBlock___hash->add((int)__element1820__, (int)__element1820__);
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
      int __tempvar1822__ = 0;
      int __tempvar1823__ = 11;
      for (int __j__ = __tempvar1822__; __j__ <= __tempvar1823__; __j__++)
        {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0))
        // __left1827__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left1828__ <-- cast(__InodeTable__, d.b[itb])
        // __left1830__ <-- d
        int __left1830__ = (int) d; //varexpr
        // __left1830__ = d
        // __offsetinbits1831__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop1832__ = 0;
        int __leftop1836__ = 8;
        // __left1838__ <-- d.s
        // __left1839__ <-- d
        int __left1839__ = (int) d; //varexpr
        // __left1839__ = d
        int __left1838__ = (__left1839__ + 0);
        int __leftop1841__ = 32;
        int __leftop1843__ = 32;
        int __leftop1845__ = 32;
        int __leftop1847__ = 32;
        int __leftop1849__ = 32;
        int __leftop1851__ = 32;
        int __rightop1852__ = 0;
        int __rightop1850__ = __leftop1851__ + __rightop1852__;
        int __rightop1848__ = __leftop1849__ + __rightop1850__;
        int __rightop1846__ = __leftop1847__ + __rightop1848__;
        int __rightop1844__ = __leftop1845__ + __rightop1846__;
        int __rightop1842__ = __leftop1843__ + __rightop1844__;
        int __sizeof1840__ = __leftop1841__ + __rightop1842__;
        int __high1853__ = __left1838__ + __sizeof1840__;
        assertvalidmemory(__left1838__, __high1853__);
        // __left1838__ = d.s
        // __offsetinbits1854__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1855__ = 32;
        int __leftop1857__ = 32;
        int __leftop1859__ = 32;
        int __leftop1861__ = 32;
        int __leftop1863__ = 32;
        int __rightop1864__ = 0;
        int __rightop1862__ = __leftop1863__ + __rightop1864__;
        int __rightop1860__ = __leftop1861__ + __rightop1862__;
        int __rightop1858__ = __leftop1859__ + __rightop1860__;
        int __rightop1856__ = __leftop1857__ + __rightop1858__;
        int __offsetinbits1854__ = __leftop1855__ + __rightop1856__;
        // __offsetinbits1854__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1865__ = __offsetinbits1854__ >> 3;
        int __shift1866__ = __offsetinbits1854__ - (__offset1865__ << 3);
        int __rightop1837__ = ((*(int *)(__left1838__ + __offset1865__))  >> __shift1866__) & 0xffffffff;
        int __leftop1835__ = __leftop1836__ * __rightop1837__;
        int __rightop1867__ = 0;
        int __leftop1834__ = __leftop1835__ + __rightop1867__;
        int __rightop1868__ = (int) __itb__; //varexpr
        int __rightop1833__ = __leftop1834__ * __rightop1868__;
        int __offsetinbits1831__ = __leftop1832__ + __rightop1833__;
        // __offsetinbits1831__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset1869__ = __offsetinbits1831__ >> 3;
        int __expr1829__ = (__left1830__ + __offset1869__);
        int __leftop1872__ = 8;
        // __left1874__ <-- d.s
        // __left1875__ <-- d
        int __left1875__ = (int) d; //varexpr
        // __left1875__ = d
        int __left1874__ = (__left1875__ + 0);
        int __leftop1877__ = 32;
        int __leftop1879__ = 32;
        int __leftop1881__ = 32;
        int __leftop1883__ = 32;
        int __leftop1885__ = 32;
        int __leftop1887__ = 32;
        int __rightop1888__ = 0;
        int __rightop1886__ = __leftop1887__ + __rightop1888__;
        int __rightop1884__ = __leftop1885__ + __rightop1886__;
        int __rightop1882__ = __leftop1883__ + __rightop1884__;
        int __rightop1880__ = __leftop1881__ + __rightop1882__;
        int __rightop1878__ = __leftop1879__ + __rightop1880__;
        int __sizeof1876__ = __leftop1877__ + __rightop1878__;
        int __high1889__ = __left1874__ + __sizeof1876__;
        assertvalidmemory(__left1874__, __high1889__);
        // __left1874__ = d.s
        // __offsetinbits1890__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop1891__ = 32;
        int __leftop1893__ = 32;
        int __leftop1895__ = 32;
        int __leftop1897__ = 32;
        int __leftop1899__ = 32;
        int __rightop1900__ = 0;
        int __rightop1898__ = __leftop1899__ + __rightop1900__;
        int __rightop1896__ = __leftop1897__ + __rightop1898__;
        int __rightop1894__ = __leftop1895__ + __rightop1896__;
        int __rightop1892__ = __leftop1893__ + __rightop1894__;
        int __offsetinbits1890__ = __leftop1891__ + __rightop1892__;
        // __offsetinbits1890__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset1901__ = __offsetinbits1890__ >> 3;
        int __shift1902__ = __offsetinbits1890__ - (__offset1901__ << 3);
        int __rightop1873__ = ((*(int *)(__left1874__ + __offset1901__))  >> __shift1902__) & 0xffffffff;
        int __leftop1871__ = __leftop1872__ * __rightop1873__;
        int __rightop1903__ = 0;
        int __sizeof1870__ = __leftop1871__ + __rightop1903__;
        int __high1904__ = __expr1829__ + __sizeof1870__;
        assertvalidmemory(__expr1829__, __high1904__);
        int __left1828__ = (int) __expr1829__;
        // __left1828__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits1905__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop1906__ = 0;
        int __leftop1909__ = 32;
        int __leftop1912__ = 32;
        int __rightop1913__ = 12;
        int __leftop1911__ = __leftop1912__ * __rightop1913__;
        int __leftop1915__ = 32;
        int __rightop1916__ = 0;
        int __rightop1914__ = __leftop1915__ + __rightop1916__;
        int __rightop1910__ = __leftop1911__ + __rightop1914__;
        int __leftop1908__ = __leftop1909__ + __rightop1910__;
        int __rightop1917__ = (int) __i__; //varexpr
        int __rightop1907__ = __leftop1908__ * __rightop1917__;
        int __offsetinbits1905__ = __leftop1906__ + __rightop1907__;
        // __offsetinbits1905__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset1918__ = __offsetinbits1905__ >> 3;
        int __left1827__ = (__left1828__ + __offset1918__);
        int __leftop1920__ = 32;
        int __leftop1923__ = 32;
        int __rightop1924__ = 12;
        int __leftop1922__ = __leftop1923__ * __rightop1924__;
        int __leftop1926__ = 32;
        int __rightop1927__ = 0;
        int __rightop1925__ = __leftop1926__ + __rightop1927__;
        int __rightop1921__ = __leftop1922__ + __rightop1925__;
        int __sizeof1919__ = __leftop1920__ + __rightop1921__;
        int __high1928__ = __left1827__ + __sizeof1919__;
        assertvalidmemory(__left1827__, __high1928__);
        // __left1827__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits1929__ <-- ((32 + 0) + (32 * j))
        int __leftop1931__ = 32;
        int __rightop1932__ = 0;
        int __leftop1930__ = __leftop1931__ + __rightop1932__;
        int __leftop1934__ = 32;
        int __rightop1935__ = (int) __j__; //varexpr
        int __rightop1933__ = __leftop1934__ * __rightop1935__;
        int __offsetinbits1929__ = __leftop1930__ + __rightop1933__;
        // __offsetinbits1929__ = ((32 + 0) + (32 * j))
        int __offset1936__ = __offsetinbits1929__ >> 3;
        int __shift1937__ = __offsetinbits1929__ - (__offset1936__ << 3);
        int __leftop1826__ = ((*(int *)(__left1827__ + __offset1936__))  >> __shift1937__) & 0xffffffff;
        int __rightop1938__ = 0;
        int __leftop1825__ = __leftop1826__ == __rightop1938__;
        int __tempvar1824__ = !__leftop1825__;
        if (__tempvar1824__)
          {
          int __leftele1939__ = (int) __i__; //varexpr
          // __left1941__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left1942__ <-- cast(__InodeTable__, d.b[itb])
          // __left1944__ <-- d
          int __left1944__ = (int) d; //varexpr
          // __left1944__ = d
          // __offsetinbits1945__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop1946__ = 0;
          int __leftop1950__ = 8;
          // __left1952__ <-- d.s
          // __left1953__ <-- d
          int __left1953__ = (int) d; //varexpr
          // __left1953__ = d
          int __left1952__ = (__left1953__ + 0);
          int __leftop1955__ = 32;
          int __leftop1957__ = 32;
          int __leftop1959__ = 32;
          int __leftop1961__ = 32;
          int __leftop1963__ = 32;
          int __leftop1965__ = 32;
          int __rightop1966__ = 0;
          int __rightop1964__ = __leftop1965__ + __rightop1966__;
          int __rightop1962__ = __leftop1963__ + __rightop1964__;
          int __rightop1960__ = __leftop1961__ + __rightop1962__;
          int __rightop1958__ = __leftop1959__ + __rightop1960__;
          int __rightop1956__ = __leftop1957__ + __rightop1958__;
          int __sizeof1954__ = __leftop1955__ + __rightop1956__;
          int __high1967__ = __left1952__ + __sizeof1954__;
          assertvalidmemory(__left1952__, __high1967__);
          // __left1952__ = d.s
          // __offsetinbits1968__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop1969__ = 32;
          int __leftop1971__ = 32;
          int __leftop1973__ = 32;
          int __leftop1975__ = 32;
          int __leftop1977__ = 32;
          int __rightop1978__ = 0;
          int __rightop1976__ = __leftop1977__ + __rightop1978__;
          int __rightop1974__ = __leftop1975__ + __rightop1976__;
          int __rightop1972__ = __leftop1973__ + __rightop1974__;
          int __rightop1970__ = __leftop1971__ + __rightop1972__;
          int __offsetinbits1968__ = __leftop1969__ + __rightop1970__;
          // __offsetinbits1968__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset1979__ = __offsetinbits1968__ >> 3;
          int __shift1980__ = __offsetinbits1968__ - (__offset1979__ << 3);
          int __rightop1951__ = ((*(int *)(__left1952__ + __offset1979__))  >> __shift1980__) & 0xffffffff;
          int __leftop1949__ = __leftop1950__ * __rightop1951__;
          int __rightop1981__ = 0;
          int __leftop1948__ = __leftop1949__ + __rightop1981__;
          int __rightop1982__ = (int) __itb__; //varexpr
          int __rightop1947__ = __leftop1948__ * __rightop1982__;
          int __offsetinbits1945__ = __leftop1946__ + __rightop1947__;
          // __offsetinbits1945__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset1983__ = __offsetinbits1945__ >> 3;
          int __expr1943__ = (__left1944__ + __offset1983__);
          int __leftop1986__ = 8;
          // __left1988__ <-- d.s
          // __left1989__ <-- d
          int __left1989__ = (int) d; //varexpr
          // __left1989__ = d
          int __left1988__ = (__left1989__ + 0);
          int __leftop1991__ = 32;
          int __leftop1993__ = 32;
          int __leftop1995__ = 32;
          int __leftop1997__ = 32;
          int __leftop1999__ = 32;
          int __leftop2001__ = 32;
          int __rightop2002__ = 0;
          int __rightop2000__ = __leftop2001__ + __rightop2002__;
          int __rightop1998__ = __leftop1999__ + __rightop2000__;
          int __rightop1996__ = __leftop1997__ + __rightop1998__;
          int __rightop1994__ = __leftop1995__ + __rightop1996__;
          int __rightop1992__ = __leftop1993__ + __rightop1994__;
          int __sizeof1990__ = __leftop1991__ + __rightop1992__;
          int __high2003__ = __left1988__ + __sizeof1990__;
          assertvalidmemory(__left1988__, __high2003__);
          // __left1988__ = d.s
          // __offsetinbits2004__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop2005__ = 32;
          int __leftop2007__ = 32;
          int __leftop2009__ = 32;
          int __leftop2011__ = 32;
          int __leftop2013__ = 32;
          int __rightop2014__ = 0;
          int __rightop2012__ = __leftop2013__ + __rightop2014__;
          int __rightop2010__ = __leftop2011__ + __rightop2012__;
          int __rightop2008__ = __leftop2009__ + __rightop2010__;
          int __rightop2006__ = __leftop2007__ + __rightop2008__;
          int __offsetinbits2004__ = __leftop2005__ + __rightop2006__;
          // __offsetinbits2004__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset2015__ = __offsetinbits2004__ >> 3;
          int __shift2016__ = __offsetinbits2004__ - (__offset2015__ << 3);
          int __rightop1987__ = ((*(int *)(__left1988__ + __offset2015__))  >> __shift2016__) & 0xffffffff;
          int __leftop1985__ = __leftop1986__ * __rightop1987__;
          int __rightop2017__ = 0;
          int __sizeof1984__ = __leftop1985__ + __rightop2017__;
          int __high2018__ = __expr1943__ + __sizeof1984__;
          assertvalidmemory(__expr1943__, __high2018__);
          int __left1942__ = (int) __expr1943__;
          // __left1942__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits2019__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop2020__ = 0;
          int __leftop2023__ = 32;
          int __leftop2026__ = 32;
          int __rightop2027__ = 12;
          int __leftop2025__ = __leftop2026__ * __rightop2027__;
          int __leftop2029__ = 32;
          int __rightop2030__ = 0;
          int __rightop2028__ = __leftop2029__ + __rightop2030__;
          int __rightop2024__ = __leftop2025__ + __rightop2028__;
          int __leftop2022__ = __leftop2023__ + __rightop2024__;
          int __rightop2031__ = (int) __i__; //varexpr
          int __rightop2021__ = __leftop2022__ * __rightop2031__;
          int __offsetinbits2019__ = __leftop2020__ + __rightop2021__;
          // __offsetinbits2019__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset2032__ = __offsetinbits2019__ >> 3;
          int __left1941__ = (__left1942__ + __offset2032__);
          int __leftop2034__ = 32;
          int __leftop2037__ = 32;
          int __rightop2038__ = 12;
          int __leftop2036__ = __leftop2037__ * __rightop2038__;
          int __leftop2040__ = 32;
          int __rightop2041__ = 0;
          int __rightop2039__ = __leftop2040__ + __rightop2041__;
          int __rightop2035__ = __leftop2036__ + __rightop2039__;
          int __sizeof2033__ = __leftop2034__ + __rightop2035__;
          int __high2042__ = __left1941__ + __sizeof2033__;
          assertvalidmemory(__left1941__, __high2042__);
          // __left1941__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits2043__ <-- ((32 + 0) + (32 * j))
          int __leftop2045__ = 32;
          int __rightop2046__ = 0;
          int __leftop2044__ = __leftop2045__ + __rightop2046__;
          int __leftop2048__ = 32;
          int __rightop2049__ = (int) __j__; //varexpr
          int __rightop2047__ = __leftop2048__ * __rightop2049__;
          int __offsetinbits2043__ = __leftop2044__ + __rightop2047__;
          // __offsetinbits2043__ = ((32 + 0) + (32 * j))
          int __offset2050__ = __offsetinbits2043__ >> 3;
          int __shift2051__ = __offsetinbits2043__ - (__offset2050__ << 3);
          int __rightele1940__ = ((*(int *)(__left1941__ + __offset2050__))  >> __shift2051__) & 0xffffffff;
          int __addeditem2053__;
          __addeditem2053__ = __contents___hash->add((int)__leftele1939__, (int)__rightele1940__);
          __addeditem2053__ = __contents___hashinv->add((int)__rightele1940__, (int)__leftele1939__);
          }
        }
      }
    }
  }


// build rule7
  {
  int __tempvar2054__ = 0;
  // __left2057__ <-- d.s
  // __left2058__ <-- d
  int __left2058__ = (int) d; //varexpr
  // __left2058__ = d
  int __left2057__ = (__left2058__ + 0);
  int __leftop2060__ = 32;
  int __leftop2062__ = 32;
  int __leftop2064__ = 32;
  int __leftop2066__ = 32;
  int __leftop2068__ = 32;
  int __leftop2070__ = 32;
  int __rightop2071__ = 0;
  int __rightop2069__ = __leftop2070__ + __rightop2071__;
  int __rightop2067__ = __leftop2068__ + __rightop2069__;
  int __rightop2065__ = __leftop2066__ + __rightop2067__;
  int __rightop2063__ = __leftop2064__ + __rightop2065__;
  int __rightop2061__ = __leftop2062__ + __rightop2063__;
  int __sizeof2059__ = __leftop2060__ + __rightop2061__;
  int __high2072__ = __left2057__ + __sizeof2059__;
  assertvalidmemory(__left2057__, __high2072__);
  // __left2057__ = d.s
  // __offsetinbits2073__ <-- (32 + (32 + (32 + 0)))
  int __leftop2074__ = 32;
  int __leftop2076__ = 32;
  int __leftop2078__ = 32;
  int __rightop2079__ = 0;
  int __rightop2077__ = __leftop2078__ + __rightop2079__;
  int __rightop2075__ = __leftop2076__ + __rightop2077__;
  int __offsetinbits2073__ = __leftop2074__ + __rightop2075__;
  // __offsetinbits2073__ = (32 + (32 + (32 + 0)))
  int __offset2080__ = __offsetinbits2073__ >> 3;
  int __shift2081__ = __offsetinbits2073__ - (__offset2080__ << 3);
  int __leftop2056__ = ((*(int *)(__left2057__ + __offset2080__))  >> __shift2081__) & 0xffffffff;
  int __rightop2082__ = 1;
  int __tempvar2055__ = __leftop2056__ - __rightop2082__;
  for (int __j__ = __tempvar2054__; __j__ <= __tempvar2055__; __j__++)
    {
    //(j in? __UsedInode__)
    int __element2085__ = (int) __j__; //varexpr
    int __leftop2084__ = __UsedInode___hash->contains(__element2085__);
    int __tempvar2083__ = !__leftop2084__;
    if (__tempvar2083__)
      {
      int __element2086__ = (int) __j__; //varexpr
      int __addeditem2087__ = 1;
      __addeditem2087__ = __FreeInode___hash->add((int)__element2086__, (int)__element2086__);
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
      int __tempvar2088__ = 1;
      if (__tempvar2088__)
        {
        int __leftele2089__ = (int) __j__; //varexpr
        // __left2091__ <-- cast(__InodeTable__, d.b[itb]).itable[j]
        // __left2092__ <-- cast(__InodeTable__, d.b[itb])
        // __left2094__ <-- d
        int __left2094__ = (int) d; //varexpr
        // __left2094__ = d
        // __offsetinbits2095__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop2096__ = 0;
        int __leftop2100__ = 8;
        // __left2102__ <-- d.s
        // __left2103__ <-- d
        int __left2103__ = (int) d; //varexpr
        // __left2103__ = d
        int __left2102__ = (__left2103__ + 0);
        int __leftop2105__ = 32;
        int __leftop2107__ = 32;
        int __leftop2109__ = 32;
        int __leftop2111__ = 32;
        int __leftop2113__ = 32;
        int __leftop2115__ = 32;
        int __rightop2116__ = 0;
        int __rightop2114__ = __leftop2115__ + __rightop2116__;
        int __rightop2112__ = __leftop2113__ + __rightop2114__;
        int __rightop2110__ = __leftop2111__ + __rightop2112__;
        int __rightop2108__ = __leftop2109__ + __rightop2110__;
        int __rightop2106__ = __leftop2107__ + __rightop2108__;
        int __sizeof2104__ = __leftop2105__ + __rightop2106__;
        int __high2117__ = __left2102__ + __sizeof2104__;
        assertvalidmemory(__left2102__, __high2117__);
        // __left2102__ = d.s
        // __offsetinbits2118__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop2119__ = 32;
        int __leftop2121__ = 32;
        int __leftop2123__ = 32;
        int __leftop2125__ = 32;
        int __leftop2127__ = 32;
        int __rightop2128__ = 0;
        int __rightop2126__ = __leftop2127__ + __rightop2128__;
        int __rightop2124__ = __leftop2125__ + __rightop2126__;
        int __rightop2122__ = __leftop2123__ + __rightop2124__;
        int __rightop2120__ = __leftop2121__ + __rightop2122__;
        int __offsetinbits2118__ = __leftop2119__ + __rightop2120__;
        // __offsetinbits2118__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset2129__ = __offsetinbits2118__ >> 3;
        int __shift2130__ = __offsetinbits2118__ - (__offset2129__ << 3);
        int __rightop2101__ = ((*(int *)(__left2102__ + __offset2129__))  >> __shift2130__) & 0xffffffff;
        int __leftop2099__ = __leftop2100__ * __rightop2101__;
        int __rightop2131__ = 0;
        int __leftop2098__ = __leftop2099__ + __rightop2131__;
        int __rightop2132__ = (int) __itb__; //varexpr
        int __rightop2097__ = __leftop2098__ * __rightop2132__;
        int __offsetinbits2095__ = __leftop2096__ + __rightop2097__;
        // __offsetinbits2095__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset2133__ = __offsetinbits2095__ >> 3;
        int __expr2093__ = (__left2094__ + __offset2133__);
        int __leftop2136__ = 8;
        // __left2138__ <-- d.s
        // __left2139__ <-- d
        int __left2139__ = (int) d; //varexpr
        // __left2139__ = d
        int __left2138__ = (__left2139__ + 0);
        int __leftop2141__ = 32;
        int __leftop2143__ = 32;
        int __leftop2145__ = 32;
        int __leftop2147__ = 32;
        int __leftop2149__ = 32;
        int __leftop2151__ = 32;
        int __rightop2152__ = 0;
        int __rightop2150__ = __leftop2151__ + __rightop2152__;
        int __rightop2148__ = __leftop2149__ + __rightop2150__;
        int __rightop2146__ = __leftop2147__ + __rightop2148__;
        int __rightop2144__ = __leftop2145__ + __rightop2146__;
        int __rightop2142__ = __leftop2143__ + __rightop2144__;
        int __sizeof2140__ = __leftop2141__ + __rightop2142__;
        int __high2153__ = __left2138__ + __sizeof2140__;
        assertvalidmemory(__left2138__, __high2153__);
        // __left2138__ = d.s
        // __offsetinbits2154__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop2155__ = 32;
        int __leftop2157__ = 32;
        int __leftop2159__ = 32;
        int __leftop2161__ = 32;
        int __leftop2163__ = 32;
        int __rightop2164__ = 0;
        int __rightop2162__ = __leftop2163__ + __rightop2164__;
        int __rightop2160__ = __leftop2161__ + __rightop2162__;
        int __rightop2158__ = __leftop2159__ + __rightop2160__;
        int __rightop2156__ = __leftop2157__ + __rightop2158__;
        int __offsetinbits2154__ = __leftop2155__ + __rightop2156__;
        // __offsetinbits2154__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset2165__ = __offsetinbits2154__ >> 3;
        int __shift2166__ = __offsetinbits2154__ - (__offset2165__ << 3);
        int __rightop2137__ = ((*(int *)(__left2138__ + __offset2165__))  >> __shift2166__) & 0xffffffff;
        int __leftop2135__ = __leftop2136__ * __rightop2137__;
        int __rightop2167__ = 0;
        int __sizeof2134__ = __leftop2135__ + __rightop2167__;
        int __high2168__ = __expr2093__ + __sizeof2134__;
        assertvalidmemory(__expr2093__, __high2168__);
        int __left2092__ = (int) __expr2093__;
        // __left2092__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits2169__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __leftop2170__ = 0;
        int __leftop2173__ = 32;
        int __leftop2176__ = 32;
        int __rightop2177__ = 12;
        int __leftop2175__ = __leftop2176__ * __rightop2177__;
        int __leftop2179__ = 32;
        int __rightop2180__ = 0;
        int __rightop2178__ = __leftop2179__ + __rightop2180__;
        int __rightop2174__ = __leftop2175__ + __rightop2178__;
        int __leftop2172__ = __leftop2173__ + __rightop2174__;
        int __rightop2181__ = (int) __j__; //varexpr
        int __rightop2171__ = __leftop2172__ * __rightop2181__;
        int __offsetinbits2169__ = __leftop2170__ + __rightop2171__;
        // __offsetinbits2169__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * j))
        int __offset2182__ = __offsetinbits2169__ >> 3;
        int __left2091__ = (__left2092__ + __offset2182__);
        int __leftop2184__ = 32;
        int __leftop2187__ = 32;
        int __rightop2188__ = 12;
        int __leftop2186__ = __leftop2187__ * __rightop2188__;
        int __leftop2190__ = 32;
        int __rightop2191__ = 0;
        int __rightop2189__ = __leftop2190__ + __rightop2191__;
        int __rightop2185__ = __leftop2186__ + __rightop2189__;
        int __sizeof2183__ = __leftop2184__ + __rightop2185__;
        int __high2192__ = __left2091__ + __sizeof2183__;
        assertvalidmemory(__left2091__, __high2192__);
        // __left2091__ = cast(__InodeTable__, d.b[itb]).itable[j]
        int __rightele2090__ = ((*(int *)(__left2091__ + 0))  >> 0) & 0xffffffff;
        int __addeditem2194__;
        __addeditem2194__ = __filesize___hash->add((int)__leftele2089__, (int)__rightele2090__);
        }
      }
    }
  }


// build rule18
  {
  int __tempvar2195__ = 0;
  // __left2198__ <-- d.s
  // __left2199__ <-- d
  int __left2199__ = (int) d; //varexpr
  // __left2199__ = d
  int __left2198__ = (__left2199__ + 0);
  int __leftop2201__ = 32;
  int __leftop2203__ = 32;
  int __leftop2205__ = 32;
  int __leftop2207__ = 32;
  int __leftop2209__ = 32;
  int __leftop2211__ = 32;
  int __rightop2212__ = 0;
  int __rightop2210__ = __leftop2211__ + __rightop2212__;
  int __rightop2208__ = __leftop2209__ + __rightop2210__;
  int __rightop2206__ = __leftop2207__ + __rightop2208__;
  int __rightop2204__ = __leftop2205__ + __rightop2206__;
  int __rightop2202__ = __leftop2203__ + __rightop2204__;
  int __sizeof2200__ = __leftop2201__ + __rightop2202__;
  int __high2213__ = __left2198__ + __sizeof2200__;
  assertvalidmemory(__left2198__, __high2213__);
  // __left2198__ = d.s
  // __offsetinbits2214__ <-- (32 + (32 + 0))
  int __leftop2215__ = 32;
  int __leftop2217__ = 32;
  int __rightop2218__ = 0;
  int __rightop2216__ = __leftop2217__ + __rightop2218__;
  int __offsetinbits2214__ = __leftop2215__ + __rightop2216__;
  // __offsetinbits2214__ = (32 + (32 + 0))
  int __offset2219__ = __offsetinbits2214__ >> 3;
  int __shift2220__ = __offsetinbits2214__ - (__offset2219__ << 3);
  int __leftop2197__ = ((*(int *)(__left2198__ + __offset2219__))  >> __shift2220__) & 0xffffffff;
  int __rightop2221__ = 1;
  int __tempvar2196__ = __leftop2197__ - __rightop2221__;
  for (int __j__ = __tempvar2195__; __j__ <= __tempvar2196__; __j__++)
    {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); )
      {
      int __bbb__ = (int) __bbb___iterator->next();
      //(cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == false)
      // __left2224__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left2226__ <-- d
      int __left2226__ = (int) d; //varexpr
      // __left2226__ = d
      // __offsetinbits2227__ <-- (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __leftop2228__ = 0;
      int __leftop2232__ = 8;
      // __left2234__ <-- d.s
      // __left2235__ <-- d
      int __left2235__ = (int) d; //varexpr
      // __left2235__ = d
      int __left2234__ = (__left2235__ + 0);
      int __leftop2237__ = 32;
      int __leftop2239__ = 32;
      int __leftop2241__ = 32;
      int __leftop2243__ = 32;
      int __leftop2245__ = 32;
      int __leftop2247__ = 32;
      int __rightop2248__ = 0;
      int __rightop2246__ = __leftop2247__ + __rightop2248__;
      int __rightop2244__ = __leftop2245__ + __rightop2246__;
      int __rightop2242__ = __leftop2243__ + __rightop2244__;
      int __rightop2240__ = __leftop2241__ + __rightop2242__;
      int __rightop2238__ = __leftop2239__ + __rightop2240__;
      int __sizeof2236__ = __leftop2237__ + __rightop2238__;
      int __high2249__ = __left2234__ + __sizeof2236__;
      assertvalidmemory(__left2234__, __high2249__);
      // __left2234__ = d.s
      // __offsetinbits2250__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2251__ = 32;
      int __leftop2253__ = 32;
      int __leftop2255__ = 32;
      int __leftop2257__ = 32;
      int __leftop2259__ = 32;
      int __rightop2260__ = 0;
      int __rightop2258__ = __leftop2259__ + __rightop2260__;
      int __rightop2256__ = __leftop2257__ + __rightop2258__;
      int __rightop2254__ = __leftop2255__ + __rightop2256__;
      int __rightop2252__ = __leftop2253__ + __rightop2254__;
      int __offsetinbits2250__ = __leftop2251__ + __rightop2252__;
      // __offsetinbits2250__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2261__ = __offsetinbits2250__ >> 3;
      int __shift2262__ = __offsetinbits2250__ - (__offset2261__ << 3);
      int __rightop2233__ = ((*(int *)(__left2234__ + __offset2261__))  >> __shift2262__) & 0xffffffff;
      int __leftop2231__ = __leftop2232__ * __rightop2233__;
      int __rightop2263__ = 0;
      int __leftop2230__ = __leftop2231__ + __rightop2263__;
      int __rightop2264__ = (int) __bbb__; //varexpr
      int __rightop2229__ = __leftop2230__ * __rightop2264__;
      int __offsetinbits2227__ = __leftop2228__ + __rightop2229__;
      // __offsetinbits2227__ = (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __offset2265__ = __offsetinbits2227__ >> 3;
      int __expr2225__ = (__left2226__ + __offset2265__);
      int __leftop2268__ = 8;
      // __left2270__ <-- d.s
      // __left2271__ <-- d
      int __left2271__ = (int) d; //varexpr
      // __left2271__ = d
      int __left2270__ = (__left2271__ + 0);
      int __leftop2273__ = 32;
      int __leftop2275__ = 32;
      int __leftop2277__ = 32;
      int __leftop2279__ = 32;
      int __leftop2281__ = 32;
      int __leftop2283__ = 32;
      int __rightop2284__ = 0;
      int __rightop2282__ = __leftop2283__ + __rightop2284__;
      int __rightop2280__ = __leftop2281__ + __rightop2282__;
      int __rightop2278__ = __leftop2279__ + __rightop2280__;
      int __rightop2276__ = __leftop2277__ + __rightop2278__;
      int __rightop2274__ = __leftop2275__ + __rightop2276__;
      int __sizeof2272__ = __leftop2273__ + __rightop2274__;
      int __high2285__ = __left2270__ + __sizeof2272__;
      assertvalidmemory(__left2270__, __high2285__);
      // __left2270__ = d.s
      // __offsetinbits2286__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2287__ = 32;
      int __leftop2289__ = 32;
      int __leftop2291__ = 32;
      int __leftop2293__ = 32;
      int __leftop2295__ = 32;
      int __rightop2296__ = 0;
      int __rightop2294__ = __leftop2295__ + __rightop2296__;
      int __rightop2292__ = __leftop2293__ + __rightop2294__;
      int __rightop2290__ = __leftop2291__ + __rightop2292__;
      int __rightop2288__ = __leftop2289__ + __rightop2290__;
      int __offsetinbits2286__ = __leftop2287__ + __rightop2288__;
      // __offsetinbits2286__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2297__ = __offsetinbits2286__ >> 3;
      int __shift2298__ = __offsetinbits2286__ - (__offset2297__ << 3);
      int __rightop2269__ = ((*(int *)(__left2270__ + __offset2297__))  >> __shift2298__) & 0xffffffff;
      int __leftop2267__ = __leftop2268__ * __rightop2269__;
      int __rightop2299__ = 0;
      int __sizeof2266__ = __leftop2267__ + __rightop2299__;
      int __high2300__ = __expr2225__ + __sizeof2266__;
      assertvalidmemory(__expr2225__, __high2300__);
      int __left2224__ = (int) __expr2225__;
      // __left2224__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits2301__ <-- (0 + (1 * j))
      int __leftop2302__ = 0;
      int __leftop2304__ = 1;
      int __rightop2305__ = (int) __j__; //varexpr
      int __rightop2303__ = __leftop2304__ * __rightop2305__;
      int __offsetinbits2301__ = __leftop2302__ + __rightop2303__;
      // __offsetinbits2301__ = (0 + (1 * j))
      int __offset2306__ = __offsetinbits2301__ >> 3;
      int __shift2307__ = __offsetinbits2301__ - (__offset2306__ << 3);
      int __leftop2223__ = ((*(int *)(__left2224__ + __offset2306__))  >> __shift2307__) & 0x1;
      int __rightop2308__ = 0;
      int __tempvar2222__ = __leftop2223__ == __rightop2308__;
      if (__tempvar2222__)
        {
        int __leftele2309__ = (int) __j__; //varexpr
        int __rightele2310__ = 101;
        int __addeditem2312__;
        __addeditem2312__ = __blockstatus___hash->add((int)__leftele2309__, (int)__rightele2310__);
        }
      }
    }
  }


// build rule19
  {
  int __tempvar2313__ = 0;
  // __left2316__ <-- d.s
  // __left2317__ <-- d
  int __left2317__ = (int) d; //varexpr
  // __left2317__ = d
  int __left2316__ = (__left2317__ + 0);
  int __leftop2319__ = 32;
  int __leftop2321__ = 32;
  int __leftop2323__ = 32;
  int __leftop2325__ = 32;
  int __leftop2327__ = 32;
  int __leftop2329__ = 32;
  int __rightop2330__ = 0;
  int __rightop2328__ = __leftop2329__ + __rightop2330__;
  int __rightop2326__ = __leftop2327__ + __rightop2328__;
  int __rightop2324__ = __leftop2325__ + __rightop2326__;
  int __rightop2322__ = __leftop2323__ + __rightop2324__;
  int __rightop2320__ = __leftop2321__ + __rightop2322__;
  int __sizeof2318__ = __leftop2319__ + __rightop2320__;
  int __high2331__ = __left2316__ + __sizeof2318__;
  assertvalidmemory(__left2316__, __high2331__);
  // __left2316__ = d.s
  // __offsetinbits2332__ <-- (32 + (32 + 0))
  int __leftop2333__ = 32;
  int __leftop2335__ = 32;
  int __rightop2336__ = 0;
  int __rightop2334__ = __leftop2335__ + __rightop2336__;
  int __offsetinbits2332__ = __leftop2333__ + __rightop2334__;
  // __offsetinbits2332__ = (32 + (32 + 0))
  int __offset2337__ = __offsetinbits2332__ >> 3;
  int __shift2338__ = __offsetinbits2332__ - (__offset2337__ << 3);
  int __leftop2315__ = ((*(int *)(__left2316__ + __offset2337__))  >> __shift2338__) & 0xffffffff;
  int __rightop2339__ = 1;
  int __tempvar2314__ = __leftop2315__ - __rightop2339__;
  for (int __j__ = __tempvar2313__; __j__ <= __tempvar2314__; __j__++)
    {
    for (SimpleIterator* __bbb___iterator = __BlockBitmapBlock___hash->iterator(); __bbb___iterator->hasNext(); )
      {
      int __bbb__ = (int) __bbb___iterator->next();
      //(cast(__BlockBitmap__, d.b[bbb]).blockbitmap[j] == true)
      // __left2342__ <-- cast(__BlockBitmap__, d.b[bbb])
      // __left2344__ <-- d
      int __left2344__ = (int) d; //varexpr
      // __left2344__ = d
      // __offsetinbits2345__ <-- (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __leftop2346__ = 0;
      int __leftop2350__ = 8;
      // __left2352__ <-- d.s
      // __left2353__ <-- d
      int __left2353__ = (int) d; //varexpr
      // __left2353__ = d
      int __left2352__ = (__left2353__ + 0);
      int __leftop2355__ = 32;
      int __leftop2357__ = 32;
      int __leftop2359__ = 32;
      int __leftop2361__ = 32;
      int __leftop2363__ = 32;
      int __leftop2365__ = 32;
      int __rightop2366__ = 0;
      int __rightop2364__ = __leftop2365__ + __rightop2366__;
      int __rightop2362__ = __leftop2363__ + __rightop2364__;
      int __rightop2360__ = __leftop2361__ + __rightop2362__;
      int __rightop2358__ = __leftop2359__ + __rightop2360__;
      int __rightop2356__ = __leftop2357__ + __rightop2358__;
      int __sizeof2354__ = __leftop2355__ + __rightop2356__;
      int __high2367__ = __left2352__ + __sizeof2354__;
      assertvalidmemory(__left2352__, __high2367__);
      // __left2352__ = d.s
      // __offsetinbits2368__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2369__ = 32;
      int __leftop2371__ = 32;
      int __leftop2373__ = 32;
      int __leftop2375__ = 32;
      int __leftop2377__ = 32;
      int __rightop2378__ = 0;
      int __rightop2376__ = __leftop2377__ + __rightop2378__;
      int __rightop2374__ = __leftop2375__ + __rightop2376__;
      int __rightop2372__ = __leftop2373__ + __rightop2374__;
      int __rightop2370__ = __leftop2371__ + __rightop2372__;
      int __offsetinbits2368__ = __leftop2369__ + __rightop2370__;
      // __offsetinbits2368__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2379__ = __offsetinbits2368__ >> 3;
      int __shift2380__ = __offsetinbits2368__ - (__offset2379__ << 3);
      int __rightop2351__ = ((*(int *)(__left2352__ + __offset2379__))  >> __shift2380__) & 0xffffffff;
      int __leftop2349__ = __leftop2350__ * __rightop2351__;
      int __rightop2381__ = 0;
      int __leftop2348__ = __leftop2349__ + __rightop2381__;
      int __rightop2382__ = (int) __bbb__; //varexpr
      int __rightop2347__ = __leftop2348__ * __rightop2382__;
      int __offsetinbits2345__ = __leftop2346__ + __rightop2347__;
      // __offsetinbits2345__ = (0 + (((8 * d.s.blocksize) + 0) * bbb))
      int __offset2383__ = __offsetinbits2345__ >> 3;
      int __expr2343__ = (__left2344__ + __offset2383__);
      int __leftop2386__ = 8;
      // __left2388__ <-- d.s
      // __left2389__ <-- d
      int __left2389__ = (int) d; //varexpr
      // __left2389__ = d
      int __left2388__ = (__left2389__ + 0);
      int __leftop2391__ = 32;
      int __leftop2393__ = 32;
      int __leftop2395__ = 32;
      int __leftop2397__ = 32;
      int __leftop2399__ = 32;
      int __leftop2401__ = 32;
      int __rightop2402__ = 0;
      int __rightop2400__ = __leftop2401__ + __rightop2402__;
      int __rightop2398__ = __leftop2399__ + __rightop2400__;
      int __rightop2396__ = __leftop2397__ + __rightop2398__;
      int __rightop2394__ = __leftop2395__ + __rightop2396__;
      int __rightop2392__ = __leftop2393__ + __rightop2394__;
      int __sizeof2390__ = __leftop2391__ + __rightop2392__;
      int __high2403__ = __left2388__ + __sizeof2390__;
      assertvalidmemory(__left2388__, __high2403__);
      // __left2388__ = d.s
      // __offsetinbits2404__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
      int __leftop2405__ = 32;
      int __leftop2407__ = 32;
      int __leftop2409__ = 32;
      int __leftop2411__ = 32;
      int __leftop2413__ = 32;
      int __rightop2414__ = 0;
      int __rightop2412__ = __leftop2413__ + __rightop2414__;
      int __rightop2410__ = __leftop2411__ + __rightop2412__;
      int __rightop2408__ = __leftop2409__ + __rightop2410__;
      int __rightop2406__ = __leftop2407__ + __rightop2408__;
      int __offsetinbits2404__ = __leftop2405__ + __rightop2406__;
      // __offsetinbits2404__ = (32 + (32 + (32 + (32 + (32 + 0)))))
      int __offset2415__ = __offsetinbits2404__ >> 3;
      int __shift2416__ = __offsetinbits2404__ - (__offset2415__ << 3);
      int __rightop2387__ = ((*(int *)(__left2388__ + __offset2415__))  >> __shift2416__) & 0xffffffff;
      int __leftop2385__ = __leftop2386__ * __rightop2387__;
      int __rightop2417__ = 0;
      int __sizeof2384__ = __leftop2385__ + __rightop2417__;
      int __high2418__ = __expr2343__ + __sizeof2384__;
      assertvalidmemory(__expr2343__, __high2418__);
      int __left2342__ = (int) __expr2343__;
      // __left2342__ = cast(__BlockBitmap__, d.b[bbb])
      // __offsetinbits2419__ <-- (0 + (1 * j))
      int __leftop2420__ = 0;
      int __leftop2422__ = 1;
      int __rightop2423__ = (int) __j__; //varexpr
      int __rightop2421__ = __leftop2422__ * __rightop2423__;
      int __offsetinbits2419__ = __leftop2420__ + __rightop2421__;
      // __offsetinbits2419__ = (0 + (1 * j))
      int __offset2424__ = __offsetinbits2419__ >> 3;
      int __shift2425__ = __offsetinbits2419__ - (__offset2424__ << 3);
      int __leftop2341__ = ((*(int *)(__left2342__ + __offset2424__))  >> __shift2425__) & 0x1;
      int __rightop2426__ = 1;
      int __tempvar2340__ = __leftop2341__ == __rightop2426__;
      if (__tempvar2340__)
        {
        int __leftele2427__ = (int) __j__; //varexpr
        int __rightele2428__ = 100;
        int __addeditem2430__;
        __addeditem2430__ = __blockstatus___hash->add((int)__leftele2427__, (int)__rightele2428__);
        }
      }
    }
  }


// build rule20
  {
  //true
  int __tempvar2431__ = 1;
  if (__tempvar2431__)
    {
    int __element2432__ = 0;
    int __addeditem2433__ = 1;
    __addeditem2433__ = __Block___hash->add((int)__element2432__, (int)__element2432__);
    }
  }


// build rule21
  {
  //true
  int __tempvar2434__ = 1;
  if (__tempvar2434__)
    {
    int __element2435__ = 1;
    int __addeditem2436__ = 1;
    __addeditem2436__ = __Block___hash->add((int)__element2435__, (int)__element2435__);
    }
  }


// build rule22
  {
  //(d.g.InodeTableBlock < d.s.NumberofBlocks)
  // __left2439__ <-- d.g
  // __left2440__ <-- d
  int __left2440__ = (int) d; //varexpr
  // __left2440__ = d
  // __offsetinbits2441__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop2442__ = 0;
  int __leftop2446__ = 8;
  // __left2448__ <-- d.s
  // __left2449__ <-- d
  int __left2449__ = (int) d; //varexpr
  // __left2449__ = d
  int __left2448__ = (__left2449__ + 0);
  int __leftop2451__ = 32;
  int __leftop2453__ = 32;
  int __leftop2455__ = 32;
  int __leftop2457__ = 32;
  int __leftop2459__ = 32;
  int __leftop2461__ = 32;
  int __rightop2462__ = 0;
  int __rightop2460__ = __leftop2461__ + __rightop2462__;
  int __rightop2458__ = __leftop2459__ + __rightop2460__;
  int __rightop2456__ = __leftop2457__ + __rightop2458__;
  int __rightop2454__ = __leftop2455__ + __rightop2456__;
  int __rightop2452__ = __leftop2453__ + __rightop2454__;
  int __sizeof2450__ = __leftop2451__ + __rightop2452__;
  int __high2463__ = __left2448__ + __sizeof2450__;
  assertvalidmemory(__left2448__, __high2463__);
  // __left2448__ = d.s
  // __offsetinbits2464__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop2465__ = 32;
  int __leftop2467__ = 32;
  int __leftop2469__ = 32;
  int __leftop2471__ = 32;
  int __leftop2473__ = 32;
  int __rightop2474__ = 0;
  int __rightop2472__ = __leftop2473__ + __rightop2474__;
  int __rightop2470__ = __leftop2471__ + __rightop2472__;
  int __rightop2468__ = __leftop2469__ + __rightop2470__;
  int __rightop2466__ = __leftop2467__ + __rightop2468__;
  int __offsetinbits2464__ = __leftop2465__ + __rightop2466__;
  // __offsetinbits2464__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset2475__ = __offsetinbits2464__ >> 3;
  int __shift2476__ = __offsetinbits2464__ - (__offset2475__ << 3);
  int __rightop2447__ = ((*(int *)(__left2448__ + __offset2475__))  >> __shift2476__) & 0xffffffff;
  int __leftop2445__ = __leftop2446__ * __rightop2447__;
  int __rightop2477__ = 0;
  int __leftop2444__ = __leftop2445__ + __rightop2477__;
  int __rightop2478__ = 1;
  int __rightop2443__ = __leftop2444__ * __rightop2478__;
  int __offsetinbits2441__ = __leftop2442__ + __rightop2443__;
  // __offsetinbits2441__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset2479__ = __offsetinbits2441__ >> 3;
  int __left2439__ = (__left2440__ + __offset2479__);
  int __leftop2481__ = 32;
  int __leftop2483__ = 32;
  int __leftop2485__ = 32;
  int __leftop2487__ = 32;
  int __leftop2489__ = 32;
  int __rightop2490__ = 0;
  int __rightop2488__ = __leftop2489__ + __rightop2490__;
  int __rightop2486__ = __leftop2487__ + __rightop2488__;
  int __rightop2484__ = __leftop2485__ + __rightop2486__;
  int __rightop2482__ = __leftop2483__ + __rightop2484__;
  int __sizeof2480__ = __leftop2481__ + __rightop2482__;
  int __high2491__ = __left2439__ + __sizeof2480__;
  assertvalidmemory(__left2439__, __high2491__);
  // __left2439__ = d.g
  // __offsetinbits2492__ <-- (32 + (32 + 0))
  int __leftop2493__ = 32;
  int __leftop2495__ = 32;
  int __rightop2496__ = 0;
  int __rightop2494__ = __leftop2495__ + __rightop2496__;
  int __offsetinbits2492__ = __leftop2493__ + __rightop2494__;
  // __offsetinbits2492__ = (32 + (32 + 0))
  int __offset2497__ = __offsetinbits2492__ >> 3;
  int __shift2498__ = __offsetinbits2492__ - (__offset2497__ << 3);
  int __leftop2438__ = ((*(int *)(__left2439__ + __offset2497__))  >> __shift2498__) & 0xffffffff;
  // __left2500__ <-- d.s
  // __left2501__ <-- d
  int __left2501__ = (int) d; //varexpr
  // __left2501__ = d
  int __left2500__ = (__left2501__ + 0);
  int __leftop2503__ = 32;
  int __leftop2505__ = 32;
  int __leftop2507__ = 32;
  int __leftop2509__ = 32;
  int __leftop2511__ = 32;
  int __leftop2513__ = 32;
  int __rightop2514__ = 0;
  int __rightop2512__ = __leftop2513__ + __rightop2514__;
  int __rightop2510__ = __leftop2511__ + __rightop2512__;
  int __rightop2508__ = __leftop2509__ + __rightop2510__;
  int __rightop2506__ = __leftop2507__ + __rightop2508__;
  int __rightop2504__ = __leftop2505__ + __rightop2506__;
  int __sizeof2502__ = __leftop2503__ + __rightop2504__;
  int __high2515__ = __left2500__ + __sizeof2502__;
  assertvalidmemory(__left2500__, __high2515__);
  // __left2500__ = d.s
  // __offsetinbits2516__ <-- (32 + (32 + 0))
  int __leftop2517__ = 32;
  int __leftop2519__ = 32;
  int __rightop2520__ = 0;
  int __rightop2518__ = __leftop2519__ + __rightop2520__;
  int __offsetinbits2516__ = __leftop2517__ + __rightop2518__;
  // __offsetinbits2516__ = (32 + (32 + 0))
  int __offset2521__ = __offsetinbits2516__ >> 3;
  int __shift2522__ = __offsetinbits2516__ - (__offset2521__ << 3);
  int __rightop2499__ = ((*(int *)(__left2500__ + __offset2521__))  >> __shift2522__) & 0xffffffff;
  int __tempvar2437__ = __leftop2438__ < __rightop2499__;
  if (__tempvar2437__)
    {
    // __left2524__ <-- d.g
    // __left2525__ <-- d
    int __left2525__ = (int) d; //varexpr
    // __left2525__ = d
    // __offsetinbits2526__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop2527__ = 0;
    int __leftop2531__ = 8;
    // __left2533__ <-- d.s
    // __left2534__ <-- d
    int __left2534__ = (int) d; //varexpr
    // __left2534__ = d
    int __left2533__ = (__left2534__ + 0);
    int __leftop2536__ = 32;
    int __leftop2538__ = 32;
    int __leftop2540__ = 32;
    int __leftop2542__ = 32;
    int __leftop2544__ = 32;
    int __leftop2546__ = 32;
    int __rightop2547__ = 0;
    int __rightop2545__ = __leftop2546__ + __rightop2547__;
    int __rightop2543__ = __leftop2544__ + __rightop2545__;
    int __rightop2541__ = __leftop2542__ + __rightop2543__;
    int __rightop2539__ = __leftop2540__ + __rightop2541__;
    int __rightop2537__ = __leftop2538__ + __rightop2539__;
    int __sizeof2535__ = __leftop2536__ + __rightop2537__;
    int __high2548__ = __left2533__ + __sizeof2535__;
    assertvalidmemory(__left2533__, __high2548__);
    // __left2533__ = d.s
    // __offsetinbits2549__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop2550__ = 32;
    int __leftop2552__ = 32;
    int __leftop2554__ = 32;
    int __leftop2556__ = 32;
    int __leftop2558__ = 32;
    int __rightop2559__ = 0;
    int __rightop2557__ = __leftop2558__ + __rightop2559__;
    int __rightop2555__ = __leftop2556__ + __rightop2557__;
    int __rightop2553__ = __leftop2554__ + __rightop2555__;
    int __rightop2551__ = __leftop2552__ + __rightop2553__;
    int __offsetinbits2549__ = __leftop2550__ + __rightop2551__;
    // __offsetinbits2549__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset2560__ = __offsetinbits2549__ >> 3;
    int __shift2561__ = __offsetinbits2549__ - (__offset2560__ << 3);
    int __rightop2532__ = ((*(int *)(__left2533__ + __offset2560__))  >> __shift2561__) & 0xffffffff;
    int __leftop2530__ = __leftop2531__ * __rightop2532__;
    int __rightop2562__ = 0;
    int __leftop2529__ = __leftop2530__ + __rightop2562__;
    int __rightop2563__ = 1;
    int __rightop2528__ = __leftop2529__ * __rightop2563__;
    int __offsetinbits2526__ = __leftop2527__ + __rightop2528__;
    // __offsetinbits2526__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset2564__ = __offsetinbits2526__ >> 3;
    int __left2524__ = (__left2525__ + __offset2564__);
    int __leftop2566__ = 32;
    int __leftop2568__ = 32;
    int __leftop2570__ = 32;
    int __leftop2572__ = 32;
    int __leftop2574__ = 32;
    int __rightop2575__ = 0;
    int __rightop2573__ = __leftop2574__ + __rightop2575__;
    int __rightop2571__ = __leftop2572__ + __rightop2573__;
    int __rightop2569__ = __leftop2570__ + __rightop2571__;
    int __rightop2567__ = __leftop2568__ + __rightop2569__;
    int __sizeof2565__ = __leftop2566__ + __rightop2567__;
    int __high2576__ = __left2524__ + __sizeof2565__;
    assertvalidmemory(__left2524__, __high2576__);
    // __left2524__ = d.g
    // __offsetinbits2577__ <-- (32 + (32 + 0))
    int __leftop2578__ = 32;
    int __leftop2580__ = 32;
    int __rightop2581__ = 0;
    int __rightop2579__ = __leftop2580__ + __rightop2581__;
    int __offsetinbits2577__ = __leftop2578__ + __rightop2579__;
    // __offsetinbits2577__ = (32 + (32 + 0))
    int __offset2582__ = __offsetinbits2577__ >> 3;
    int __shift2583__ = __offsetinbits2577__ - (__offset2582__ << 3);
    int __element2523__ = ((*(int *)(__left2524__ + __offset2582__))  >> __shift2583__) & 0xffffffff;
    int __addeditem2584__ = 1;
    __addeditem2584__ = __Block___hash->add((int)__element2523__, (int)__element2523__);
    }
  }


// build rule23
  {
  //(d.g.InodeBitmapBlock < d.s.NumberofBlocks)
  // __left2587__ <-- d.g
  // __left2588__ <-- d
  int __left2588__ = (int) d; //varexpr
  // __left2588__ = d
  // __offsetinbits2589__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop2590__ = 0;
  int __leftop2594__ = 8;
  // __left2596__ <-- d.s
  // __left2597__ <-- d
  int __left2597__ = (int) d; //varexpr
  // __left2597__ = d
  int __left2596__ = (__left2597__ + 0);
  int __leftop2599__ = 32;
  int __leftop2601__ = 32;
  int __leftop2603__ = 32;
  int __leftop2605__ = 32;
  int __leftop2607__ = 32;
  int __leftop2609__ = 32;
  int __rightop2610__ = 0;
  int __rightop2608__ = __leftop2609__ + __rightop2610__;
  int __rightop2606__ = __leftop2607__ + __rightop2608__;
  int __rightop2604__ = __leftop2605__ + __rightop2606__;
  int __rightop2602__ = __leftop2603__ + __rightop2604__;
  int __rightop2600__ = __leftop2601__ + __rightop2602__;
  int __sizeof2598__ = __leftop2599__ + __rightop2600__;
  int __high2611__ = __left2596__ + __sizeof2598__;
  assertvalidmemory(__left2596__, __high2611__);
  // __left2596__ = d.s
  // __offsetinbits2612__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop2613__ = 32;
  int __leftop2615__ = 32;
  int __leftop2617__ = 32;
  int __leftop2619__ = 32;
  int __leftop2621__ = 32;
  int __rightop2622__ = 0;
  int __rightop2620__ = __leftop2621__ + __rightop2622__;
  int __rightop2618__ = __leftop2619__ + __rightop2620__;
  int __rightop2616__ = __leftop2617__ + __rightop2618__;
  int __rightop2614__ = __leftop2615__ + __rightop2616__;
  int __offsetinbits2612__ = __leftop2613__ + __rightop2614__;
  // __offsetinbits2612__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset2623__ = __offsetinbits2612__ >> 3;
  int __shift2624__ = __offsetinbits2612__ - (__offset2623__ << 3);
  int __rightop2595__ = ((*(int *)(__left2596__ + __offset2623__))  >> __shift2624__) & 0xffffffff;
  int __leftop2593__ = __leftop2594__ * __rightop2595__;
  int __rightop2625__ = 0;
  int __leftop2592__ = __leftop2593__ + __rightop2625__;
  int __rightop2626__ = 1;
  int __rightop2591__ = __leftop2592__ * __rightop2626__;
  int __offsetinbits2589__ = __leftop2590__ + __rightop2591__;
  // __offsetinbits2589__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset2627__ = __offsetinbits2589__ >> 3;
  int __left2587__ = (__left2588__ + __offset2627__);
  int __leftop2629__ = 32;
  int __leftop2631__ = 32;
  int __leftop2633__ = 32;
  int __leftop2635__ = 32;
  int __leftop2637__ = 32;
  int __rightop2638__ = 0;
  int __rightop2636__ = __leftop2637__ + __rightop2638__;
  int __rightop2634__ = __leftop2635__ + __rightop2636__;
  int __rightop2632__ = __leftop2633__ + __rightop2634__;
  int __rightop2630__ = __leftop2631__ + __rightop2632__;
  int __sizeof2628__ = __leftop2629__ + __rightop2630__;
  int __high2639__ = __left2587__ + __sizeof2628__;
  assertvalidmemory(__left2587__, __high2639__);
  // __left2587__ = d.g
  // __offsetinbits2640__ <-- (32 + 0)
  int __leftop2641__ = 32;
  int __rightop2642__ = 0;
  int __offsetinbits2640__ = __leftop2641__ + __rightop2642__;
  // __offsetinbits2640__ = (32 + 0)
  int __offset2643__ = __offsetinbits2640__ >> 3;
  int __shift2644__ = __offsetinbits2640__ - (__offset2643__ << 3);
  int __leftop2586__ = ((*(int *)(__left2587__ + __offset2643__))  >> __shift2644__) & 0xffffffff;
  // __left2646__ <-- d.s
  // __left2647__ <-- d
  int __left2647__ = (int) d; //varexpr
  // __left2647__ = d
  int __left2646__ = (__left2647__ + 0);
  int __leftop2649__ = 32;
  int __leftop2651__ = 32;
  int __leftop2653__ = 32;
  int __leftop2655__ = 32;
  int __leftop2657__ = 32;
  int __leftop2659__ = 32;
  int __rightop2660__ = 0;
  int __rightop2658__ = __leftop2659__ + __rightop2660__;
  int __rightop2656__ = __leftop2657__ + __rightop2658__;
  int __rightop2654__ = __leftop2655__ + __rightop2656__;
  int __rightop2652__ = __leftop2653__ + __rightop2654__;
  int __rightop2650__ = __leftop2651__ + __rightop2652__;
  int __sizeof2648__ = __leftop2649__ + __rightop2650__;
  int __high2661__ = __left2646__ + __sizeof2648__;
  assertvalidmemory(__left2646__, __high2661__);
  // __left2646__ = d.s
  // __offsetinbits2662__ <-- (32 + (32 + 0))
  int __leftop2663__ = 32;
  int __leftop2665__ = 32;
  int __rightop2666__ = 0;
  int __rightop2664__ = __leftop2665__ + __rightop2666__;
  int __offsetinbits2662__ = __leftop2663__ + __rightop2664__;
  // __offsetinbits2662__ = (32 + (32 + 0))
  int __offset2667__ = __offsetinbits2662__ >> 3;
  int __shift2668__ = __offsetinbits2662__ - (__offset2667__ << 3);
  int __rightop2645__ = ((*(int *)(__left2646__ + __offset2667__))  >> __shift2668__) & 0xffffffff;
  int __tempvar2585__ = __leftop2586__ < __rightop2645__;
  if (__tempvar2585__)
    {
    // __left2670__ <-- d.g
    // __left2671__ <-- d
    int __left2671__ = (int) d; //varexpr
    // __left2671__ = d
    // __offsetinbits2672__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop2673__ = 0;
    int __leftop2677__ = 8;
    // __left2679__ <-- d.s
    // __left2680__ <-- d
    int __left2680__ = (int) d; //varexpr
    // __left2680__ = d
    int __left2679__ = (__left2680__ + 0);
    int __leftop2682__ = 32;
    int __leftop2684__ = 32;
    int __leftop2686__ = 32;
    int __leftop2688__ = 32;
    int __leftop2690__ = 32;
    int __leftop2692__ = 32;
    int __rightop2693__ = 0;
    int __rightop2691__ = __leftop2692__ + __rightop2693__;
    int __rightop2689__ = __leftop2690__ + __rightop2691__;
    int __rightop2687__ = __leftop2688__ + __rightop2689__;
    int __rightop2685__ = __leftop2686__ + __rightop2687__;
    int __rightop2683__ = __leftop2684__ + __rightop2685__;
    int __sizeof2681__ = __leftop2682__ + __rightop2683__;
    int __high2694__ = __left2679__ + __sizeof2681__;
    assertvalidmemory(__left2679__, __high2694__);
    // __left2679__ = d.s
    // __offsetinbits2695__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop2696__ = 32;
    int __leftop2698__ = 32;
    int __leftop2700__ = 32;
    int __leftop2702__ = 32;
    int __leftop2704__ = 32;
    int __rightop2705__ = 0;
    int __rightop2703__ = __leftop2704__ + __rightop2705__;
    int __rightop2701__ = __leftop2702__ + __rightop2703__;
    int __rightop2699__ = __leftop2700__ + __rightop2701__;
    int __rightop2697__ = __leftop2698__ + __rightop2699__;
    int __offsetinbits2695__ = __leftop2696__ + __rightop2697__;
    // __offsetinbits2695__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset2706__ = __offsetinbits2695__ >> 3;
    int __shift2707__ = __offsetinbits2695__ - (__offset2706__ << 3);
    int __rightop2678__ = ((*(int *)(__left2679__ + __offset2706__))  >> __shift2707__) & 0xffffffff;
    int __leftop2676__ = __leftop2677__ * __rightop2678__;
    int __rightop2708__ = 0;
    int __leftop2675__ = __leftop2676__ + __rightop2708__;
    int __rightop2709__ = 1;
    int __rightop2674__ = __leftop2675__ * __rightop2709__;
    int __offsetinbits2672__ = __leftop2673__ + __rightop2674__;
    // __offsetinbits2672__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset2710__ = __offsetinbits2672__ >> 3;
    int __left2670__ = (__left2671__ + __offset2710__);
    int __leftop2712__ = 32;
    int __leftop2714__ = 32;
    int __leftop2716__ = 32;
    int __leftop2718__ = 32;
    int __leftop2720__ = 32;
    int __rightop2721__ = 0;
    int __rightop2719__ = __leftop2720__ + __rightop2721__;
    int __rightop2717__ = __leftop2718__ + __rightop2719__;
    int __rightop2715__ = __leftop2716__ + __rightop2717__;
    int __rightop2713__ = __leftop2714__ + __rightop2715__;
    int __sizeof2711__ = __leftop2712__ + __rightop2713__;
    int __high2722__ = __left2670__ + __sizeof2711__;
    assertvalidmemory(__left2670__, __high2722__);
    // __left2670__ = d.g
    // __offsetinbits2723__ <-- (32 + 0)
    int __leftop2724__ = 32;
    int __rightop2725__ = 0;
    int __offsetinbits2723__ = __leftop2724__ + __rightop2725__;
    // __offsetinbits2723__ = (32 + 0)
    int __offset2726__ = __offsetinbits2723__ >> 3;
    int __shift2727__ = __offsetinbits2723__ - (__offset2726__ << 3);
    int __element2669__ = ((*(int *)(__left2670__ + __offset2726__))  >> __shift2727__) & 0xffffffff;
    int __addeditem2728__ = 1;
    __addeditem2728__ = __Block___hash->add((int)__element2669__, (int)__element2669__);
    }
  }


// build rule24
  {
  //(d.g.BlockBitmapBlock < d.s.NumberofBlocks)
  // __left2731__ <-- d.g
  // __left2732__ <-- d
  int __left2732__ = (int) d; //varexpr
  // __left2732__ = d
  // __offsetinbits2733__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __leftop2734__ = 0;
  int __leftop2738__ = 8;
  // __left2740__ <-- d.s
  // __left2741__ <-- d
  int __left2741__ = (int) d; //varexpr
  // __left2741__ = d
  int __left2740__ = (__left2741__ + 0);
  int __leftop2743__ = 32;
  int __leftop2745__ = 32;
  int __leftop2747__ = 32;
  int __leftop2749__ = 32;
  int __leftop2751__ = 32;
  int __leftop2753__ = 32;
  int __rightop2754__ = 0;
  int __rightop2752__ = __leftop2753__ + __rightop2754__;
  int __rightop2750__ = __leftop2751__ + __rightop2752__;
  int __rightop2748__ = __leftop2749__ + __rightop2750__;
  int __rightop2746__ = __leftop2747__ + __rightop2748__;
  int __rightop2744__ = __leftop2745__ + __rightop2746__;
  int __sizeof2742__ = __leftop2743__ + __rightop2744__;
  int __high2755__ = __left2740__ + __sizeof2742__;
  assertvalidmemory(__left2740__, __high2755__);
  // __left2740__ = d.s
  // __offsetinbits2756__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
  int __leftop2757__ = 32;
  int __leftop2759__ = 32;
  int __leftop2761__ = 32;
  int __leftop2763__ = 32;
  int __leftop2765__ = 32;
  int __rightop2766__ = 0;
  int __rightop2764__ = __leftop2765__ + __rightop2766__;
  int __rightop2762__ = __leftop2763__ + __rightop2764__;
  int __rightop2760__ = __leftop2761__ + __rightop2762__;
  int __rightop2758__ = __leftop2759__ + __rightop2760__;
  int __offsetinbits2756__ = __leftop2757__ + __rightop2758__;
  // __offsetinbits2756__ = (32 + (32 + (32 + (32 + (32 + 0)))))
  int __offset2767__ = __offsetinbits2756__ >> 3;
  int __shift2768__ = __offsetinbits2756__ - (__offset2767__ << 3);
  int __rightop2739__ = ((*(int *)(__left2740__ + __offset2767__))  >> __shift2768__) & 0xffffffff;
  int __leftop2737__ = __leftop2738__ * __rightop2739__;
  int __rightop2769__ = 0;
  int __leftop2736__ = __leftop2737__ + __rightop2769__;
  int __rightop2770__ = 1;
  int __rightop2735__ = __leftop2736__ * __rightop2770__;
  int __offsetinbits2733__ = __leftop2734__ + __rightop2735__;
  // __offsetinbits2733__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
  int __offset2771__ = __offsetinbits2733__ >> 3;
  int __left2731__ = (__left2732__ + __offset2771__);
  int __leftop2773__ = 32;
  int __leftop2775__ = 32;
  int __leftop2777__ = 32;
  int __leftop2779__ = 32;
  int __leftop2781__ = 32;
  int __rightop2782__ = 0;
  int __rightop2780__ = __leftop2781__ + __rightop2782__;
  int __rightop2778__ = __leftop2779__ + __rightop2780__;
  int __rightop2776__ = __leftop2777__ + __rightop2778__;
  int __rightop2774__ = __leftop2775__ + __rightop2776__;
  int __sizeof2772__ = __leftop2773__ + __rightop2774__;
  int __high2783__ = __left2731__ + __sizeof2772__;
  assertvalidmemory(__left2731__, __high2783__);
  // __left2731__ = d.g
  int __leftop2730__ = ((*(int *)(__left2731__ + 0))  >> 0) & 0xffffffff;
  // __left2785__ <-- d.s
  // __left2786__ <-- d
  int __left2786__ = (int) d; //varexpr
  // __left2786__ = d
  int __left2785__ = (__left2786__ + 0);
  int __leftop2788__ = 32;
  int __leftop2790__ = 32;
  int __leftop2792__ = 32;
  int __leftop2794__ = 32;
  int __leftop2796__ = 32;
  int __leftop2798__ = 32;
  int __rightop2799__ = 0;
  int __rightop2797__ = __leftop2798__ + __rightop2799__;
  int __rightop2795__ = __leftop2796__ + __rightop2797__;
  int __rightop2793__ = __leftop2794__ + __rightop2795__;
  int __rightop2791__ = __leftop2792__ + __rightop2793__;
  int __rightop2789__ = __leftop2790__ + __rightop2791__;
  int __sizeof2787__ = __leftop2788__ + __rightop2789__;
  int __high2800__ = __left2785__ + __sizeof2787__;
  assertvalidmemory(__left2785__, __high2800__);
  // __left2785__ = d.s
  // __offsetinbits2801__ <-- (32 + (32 + 0))
  int __leftop2802__ = 32;
  int __leftop2804__ = 32;
  int __rightop2805__ = 0;
  int __rightop2803__ = __leftop2804__ + __rightop2805__;
  int __offsetinbits2801__ = __leftop2802__ + __rightop2803__;
  // __offsetinbits2801__ = (32 + (32 + 0))
  int __offset2806__ = __offsetinbits2801__ >> 3;
  int __shift2807__ = __offsetinbits2801__ - (__offset2806__ << 3);
  int __rightop2784__ = ((*(int *)(__left2785__ + __offset2806__))  >> __shift2807__) & 0xffffffff;
  int __tempvar2729__ = __leftop2730__ < __rightop2784__;
  if (__tempvar2729__)
    {
    // __left2809__ <-- d.g
    // __left2810__ <-- d
    int __left2810__ = (int) d; //varexpr
    // __left2810__ = d
    // __offsetinbits2811__ <-- (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __leftop2812__ = 0;
    int __leftop2816__ = 8;
    // __left2818__ <-- d.s
    // __left2819__ <-- d
    int __left2819__ = (int) d; //varexpr
    // __left2819__ = d
    int __left2818__ = (__left2819__ + 0);
    int __leftop2821__ = 32;
    int __leftop2823__ = 32;
    int __leftop2825__ = 32;
    int __leftop2827__ = 32;
    int __leftop2829__ = 32;
    int __leftop2831__ = 32;
    int __rightop2832__ = 0;
    int __rightop2830__ = __leftop2831__ + __rightop2832__;
    int __rightop2828__ = __leftop2829__ + __rightop2830__;
    int __rightop2826__ = __leftop2827__ + __rightop2828__;
    int __rightop2824__ = __leftop2825__ + __rightop2826__;
    int __rightop2822__ = __leftop2823__ + __rightop2824__;
    int __sizeof2820__ = __leftop2821__ + __rightop2822__;
    int __high2833__ = __left2818__ + __sizeof2820__;
    assertvalidmemory(__left2818__, __high2833__);
    // __left2818__ = d.s
    // __offsetinbits2834__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
    int __leftop2835__ = 32;
    int __leftop2837__ = 32;
    int __leftop2839__ = 32;
    int __leftop2841__ = 32;
    int __leftop2843__ = 32;
    int __rightop2844__ = 0;
    int __rightop2842__ = __leftop2843__ + __rightop2844__;
    int __rightop2840__ = __leftop2841__ + __rightop2842__;
    int __rightop2838__ = __leftop2839__ + __rightop2840__;
    int __rightop2836__ = __leftop2837__ + __rightop2838__;
    int __offsetinbits2834__ = __leftop2835__ + __rightop2836__;
    // __offsetinbits2834__ = (32 + (32 + (32 + (32 + (32 + 0)))))
    int __offset2845__ = __offsetinbits2834__ >> 3;
    int __shift2846__ = __offsetinbits2834__ - (__offset2845__ << 3);
    int __rightop2817__ = ((*(int *)(__left2818__ + __offset2845__))  >> __shift2846__) & 0xffffffff;
    int __leftop2815__ = __leftop2816__ * __rightop2817__;
    int __rightop2847__ = 0;
    int __leftop2814__ = __leftop2815__ + __rightop2847__;
    int __rightop2848__ = 1;
    int __rightop2813__ = __leftop2814__ * __rightop2848__;
    int __offsetinbits2811__ = __leftop2812__ + __rightop2813__;
    // __offsetinbits2811__ = (0 + (((8 * d.s.blocksize) + 0) * 1))
    int __offset2849__ = __offsetinbits2811__ >> 3;
    int __left2809__ = (__left2810__ + __offset2849__);
    int __leftop2851__ = 32;
    int __leftop2853__ = 32;
    int __leftop2855__ = 32;
    int __leftop2857__ = 32;
    int __leftop2859__ = 32;
    int __rightop2860__ = 0;
    int __rightop2858__ = __leftop2859__ + __rightop2860__;
    int __rightop2856__ = __leftop2857__ + __rightop2858__;
    int __rightop2854__ = __leftop2855__ + __rightop2856__;
    int __rightop2852__ = __leftop2853__ + __rightop2854__;
    int __sizeof2850__ = __leftop2851__ + __rightop2852__;
    int __high2861__ = __left2809__ + __sizeof2850__;
    assertvalidmemory(__left2809__, __high2861__);
    // __left2809__ = d.g
    int __element2808__ = ((*(int *)(__left2809__ + 0))  >> 0) & 0xffffffff;
    int __addeditem2862__ = 1;
    __addeditem2862__ = __Block___hash->add((int)__element2808__, (int)__element2808__);
    }
  }


// build rule25
  {
  //(d.s.RootDirectoryInode < d.s.NumberofInodes)
  // __left2865__ <-- d.s
  // __left2866__ <-- d
  int __left2866__ = (int) d; //varexpr
  // __left2866__ = d
  int __left2865__ = (__left2866__ + 0);
  int __leftop2868__ = 32;
  int __leftop2870__ = 32;
  int __leftop2872__ = 32;
  int __leftop2874__ = 32;
  int __leftop2876__ = 32;
  int __leftop2878__ = 32;
  int __rightop2879__ = 0;
  int __rightop2877__ = __leftop2878__ + __rightop2879__;
  int __rightop2875__ = __leftop2876__ + __rightop2877__;
  int __rightop2873__ = __leftop2874__ + __rightop2875__;
  int __rightop2871__ = __leftop2872__ + __rightop2873__;
  int __rightop2869__ = __leftop2870__ + __rightop2871__;
  int __sizeof2867__ = __leftop2868__ + __rightop2869__;
  int __high2880__ = __left2865__ + __sizeof2867__;
  assertvalidmemory(__left2865__, __high2880__);
  // __left2865__ = d.s
  // __offsetinbits2881__ <-- (32 + (32 + (32 + (32 + 0))))
  int __leftop2882__ = 32;
  int __leftop2884__ = 32;
  int __leftop2886__ = 32;
  int __leftop2888__ = 32;
  int __rightop2889__ = 0;
  int __rightop2887__ = __leftop2888__ + __rightop2889__;
  int __rightop2885__ = __leftop2886__ + __rightop2887__;
  int __rightop2883__ = __leftop2884__ + __rightop2885__;
  int __offsetinbits2881__ = __leftop2882__ + __rightop2883__;
  // __offsetinbits2881__ = (32 + (32 + (32 + (32 + 0))))
  int __offset2890__ = __offsetinbits2881__ >> 3;
  int __shift2891__ = __offsetinbits2881__ - (__offset2890__ << 3);
  int __leftop2864__ = ((*(int *)(__left2865__ + __offset2890__))  >> __shift2891__) & 0xffffffff;
  // __left2893__ <-- d.s
  // __left2894__ <-- d
  int __left2894__ = (int) d; //varexpr
  // __left2894__ = d
  int __left2893__ = (__left2894__ + 0);
  int __leftop2896__ = 32;
  int __leftop2898__ = 32;
  int __leftop2900__ = 32;
  int __leftop2902__ = 32;
  int __leftop2904__ = 32;
  int __leftop2906__ = 32;
  int __rightop2907__ = 0;
  int __rightop2905__ = __leftop2906__ + __rightop2907__;
  int __rightop2903__ = __leftop2904__ + __rightop2905__;
  int __rightop2901__ = __leftop2902__ + __rightop2903__;
  int __rightop2899__ = __leftop2900__ + __rightop2901__;
  int __rightop2897__ = __leftop2898__ + __rightop2899__;
  int __sizeof2895__ = __leftop2896__ + __rightop2897__;
  int __high2908__ = __left2893__ + __sizeof2895__;
  assertvalidmemory(__left2893__, __high2908__);
  // __left2893__ = d.s
  // __offsetinbits2909__ <-- (32 + (32 + (32 + 0)))
  int __leftop2910__ = 32;
  int __leftop2912__ = 32;
  int __leftop2914__ = 32;
  int __rightop2915__ = 0;
  int __rightop2913__ = __leftop2914__ + __rightop2915__;
  int __rightop2911__ = __leftop2912__ + __rightop2913__;
  int __offsetinbits2909__ = __leftop2910__ + __rightop2911__;
  // __offsetinbits2909__ = (32 + (32 + (32 + 0)))
  int __offset2916__ = __offsetinbits2909__ >> 3;
  int __shift2917__ = __offsetinbits2909__ - (__offset2916__ << 3);
  int __rightop2892__ = ((*(int *)(__left2893__ + __offset2916__))  >> __shift2917__) & 0xffffffff;
  int __tempvar2863__ = __leftop2864__ < __rightop2892__;
  if (__tempvar2863__)
    {
    // __left2919__ <-- d.s
    // __left2920__ <-- d
    int __left2920__ = (int) d; //varexpr
    // __left2920__ = d
    int __left2919__ = (__left2920__ + 0);
    int __leftop2922__ = 32;
    int __leftop2924__ = 32;
    int __leftop2926__ = 32;
    int __leftop2928__ = 32;
    int __leftop2930__ = 32;
    int __leftop2932__ = 32;
    int __rightop2933__ = 0;
    int __rightop2931__ = __leftop2932__ + __rightop2933__;
    int __rightop2929__ = __leftop2930__ + __rightop2931__;
    int __rightop2927__ = __leftop2928__ + __rightop2929__;
    int __rightop2925__ = __leftop2926__ + __rightop2927__;
    int __rightop2923__ = __leftop2924__ + __rightop2925__;
    int __sizeof2921__ = __leftop2922__ + __rightop2923__;
    int __high2934__ = __left2919__ + __sizeof2921__;
    assertvalidmemory(__left2919__, __high2934__);
    // __left2919__ = d.s
    // __offsetinbits2935__ <-- (32 + (32 + (32 + (32 + 0))))
    int __leftop2936__ = 32;
    int __leftop2938__ = 32;
    int __leftop2940__ = 32;
    int __leftop2942__ = 32;
    int __rightop2943__ = 0;
    int __rightop2941__ = __leftop2942__ + __rightop2943__;
    int __rightop2939__ = __leftop2940__ + __rightop2941__;
    int __rightop2937__ = __leftop2938__ + __rightop2939__;
    int __offsetinbits2935__ = __leftop2936__ + __rightop2937__;
    // __offsetinbits2935__ = (32 + (32 + (32 + (32 + 0))))
    int __offset2944__ = __offsetinbits2935__ >> 3;
    int __shift2945__ = __offsetinbits2935__ - (__offset2944__ << 3);
    int __element2918__ = ((*(int *)(__left2919__ + __offset2944__))  >> __shift2945__) & 0xffffffff;
    int __addeditem2946__ = 1;
    __addeditem2946__ = __Inode___hash->add((int)__element2918__, (int)__element2918__);
    }
  }


// build rule26
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    for (SimpleIterator* __itb___iterator = __InodeTableBlock___hash->iterator(); __itb___iterator->hasNext(); )
      {
      int __itb__ = (int) __itb___iterator->next();
      int __tempvar2947__ = 0;
      int __tempvar2948__ = 11;
      for (int __j__ = __tempvar2947__; __j__ <= __tempvar2948__; __j__++)
        {
        //((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] < d.s.NumberofBlocks) && ((cast(__InodeTable__, d.b[itb]).itable[i].Blockptr[j] == 0)))
        // __left2952__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left2953__ <-- cast(__InodeTable__, d.b[itb])
        // __left2955__ <-- d
        int __left2955__ = (int) d; //varexpr
        // __left2955__ = d
        // __offsetinbits2956__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop2957__ = 0;
        int __leftop2961__ = 8;
        // __left2963__ <-- d.s
        // __left2964__ <-- d
        int __left2964__ = (int) d; //varexpr
        // __left2964__ = d
        int __left2963__ = (__left2964__ + 0);
        int __leftop2966__ = 32;
        int __leftop2968__ = 32;
        int __leftop2970__ = 32;
        int __leftop2972__ = 32;
        int __leftop2974__ = 32;
        int __leftop2976__ = 32;
        int __rightop2977__ = 0;
        int __rightop2975__ = __leftop2976__ + __rightop2977__;
        int __rightop2973__ = __leftop2974__ + __rightop2975__;
        int __rightop2971__ = __leftop2972__ + __rightop2973__;
        int __rightop2969__ = __leftop2970__ + __rightop2971__;
        int __rightop2967__ = __leftop2968__ + __rightop2969__;
        int __sizeof2965__ = __leftop2966__ + __rightop2967__;
        int __high2978__ = __left2963__ + __sizeof2965__;
        assertvalidmemory(__left2963__, __high2978__);
        // __left2963__ = d.s
        // __offsetinbits2979__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop2980__ = 32;
        int __leftop2982__ = 32;
        int __leftop2984__ = 32;
        int __leftop2986__ = 32;
        int __leftop2988__ = 32;
        int __rightop2989__ = 0;
        int __rightop2987__ = __leftop2988__ + __rightop2989__;
        int __rightop2985__ = __leftop2986__ + __rightop2987__;
        int __rightop2983__ = __leftop2984__ + __rightop2985__;
        int __rightop2981__ = __leftop2982__ + __rightop2983__;
        int __offsetinbits2979__ = __leftop2980__ + __rightop2981__;
        // __offsetinbits2979__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset2990__ = __offsetinbits2979__ >> 3;
        int __shift2991__ = __offsetinbits2979__ - (__offset2990__ << 3);
        int __rightop2962__ = ((*(int *)(__left2963__ + __offset2990__))  >> __shift2991__) & 0xffffffff;
        int __leftop2960__ = __leftop2961__ * __rightop2962__;
        int __rightop2992__ = 0;
        int __leftop2959__ = __leftop2960__ + __rightop2992__;
        int __rightop2993__ = (int) __itb__; //varexpr
        int __rightop2958__ = __leftop2959__ * __rightop2993__;
        int __offsetinbits2956__ = __leftop2957__ + __rightop2958__;
        // __offsetinbits2956__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset2994__ = __offsetinbits2956__ >> 3;
        int __expr2954__ = (__left2955__ + __offset2994__);
        int __leftop2997__ = 8;
        // __left2999__ <-- d.s
        // __left3000__ <-- d
        int __left3000__ = (int) d; //varexpr
        // __left3000__ = d
        int __left2999__ = (__left3000__ + 0);
        int __leftop3002__ = 32;
        int __leftop3004__ = 32;
        int __leftop3006__ = 32;
        int __leftop3008__ = 32;
        int __leftop3010__ = 32;
        int __leftop3012__ = 32;
        int __rightop3013__ = 0;
        int __rightop3011__ = __leftop3012__ + __rightop3013__;
        int __rightop3009__ = __leftop3010__ + __rightop3011__;
        int __rightop3007__ = __leftop3008__ + __rightop3009__;
        int __rightop3005__ = __leftop3006__ + __rightop3007__;
        int __rightop3003__ = __leftop3004__ + __rightop3005__;
        int __sizeof3001__ = __leftop3002__ + __rightop3003__;
        int __high3014__ = __left2999__ + __sizeof3001__;
        assertvalidmemory(__left2999__, __high3014__);
        // __left2999__ = d.s
        // __offsetinbits3015__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop3016__ = 32;
        int __leftop3018__ = 32;
        int __leftop3020__ = 32;
        int __leftop3022__ = 32;
        int __leftop3024__ = 32;
        int __rightop3025__ = 0;
        int __rightop3023__ = __leftop3024__ + __rightop3025__;
        int __rightop3021__ = __leftop3022__ + __rightop3023__;
        int __rightop3019__ = __leftop3020__ + __rightop3021__;
        int __rightop3017__ = __leftop3018__ + __rightop3019__;
        int __offsetinbits3015__ = __leftop3016__ + __rightop3017__;
        // __offsetinbits3015__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset3026__ = __offsetinbits3015__ >> 3;
        int __shift3027__ = __offsetinbits3015__ - (__offset3026__ << 3);
        int __rightop2998__ = ((*(int *)(__left2999__ + __offset3026__))  >> __shift3027__) & 0xffffffff;
        int __leftop2996__ = __leftop2997__ * __rightop2998__;
        int __rightop3028__ = 0;
        int __sizeof2995__ = __leftop2996__ + __rightop3028__;
        int __high3029__ = __expr2954__ + __sizeof2995__;
        assertvalidmemory(__expr2954__, __high3029__);
        int __left2953__ = (int) __expr2954__;
        // __left2953__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits3030__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop3031__ = 0;
        int __leftop3034__ = 32;
        int __leftop3037__ = 32;
        int __rightop3038__ = 12;
        int __leftop3036__ = __leftop3037__ * __rightop3038__;
        int __leftop3040__ = 32;
        int __rightop3041__ = 0;
        int __rightop3039__ = __leftop3040__ + __rightop3041__;
        int __rightop3035__ = __leftop3036__ + __rightop3039__;
        int __leftop3033__ = __leftop3034__ + __rightop3035__;
        int __rightop3042__ = (int) __i__; //varexpr
        int __rightop3032__ = __leftop3033__ * __rightop3042__;
        int __offsetinbits3030__ = __leftop3031__ + __rightop3032__;
        // __offsetinbits3030__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset3043__ = __offsetinbits3030__ >> 3;
        int __left2952__ = (__left2953__ + __offset3043__);
        int __leftop3045__ = 32;
        int __leftop3048__ = 32;
        int __rightop3049__ = 12;
        int __leftop3047__ = __leftop3048__ * __rightop3049__;
        int __leftop3051__ = 32;
        int __rightop3052__ = 0;
        int __rightop3050__ = __leftop3051__ + __rightop3052__;
        int __rightop3046__ = __leftop3047__ + __rightop3050__;
        int __sizeof3044__ = __leftop3045__ + __rightop3046__;
        int __high3053__ = __left2952__ + __sizeof3044__;
        assertvalidmemory(__left2952__, __high3053__);
        // __left2952__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits3054__ <-- ((32 + 0) + (32 * j))
        int __leftop3056__ = 32;
        int __rightop3057__ = 0;
        int __leftop3055__ = __leftop3056__ + __rightop3057__;
        int __leftop3059__ = 32;
        int __rightop3060__ = (int) __j__; //varexpr
        int __rightop3058__ = __leftop3059__ * __rightop3060__;
        int __offsetinbits3054__ = __leftop3055__ + __rightop3058__;
        // __offsetinbits3054__ = ((32 + 0) + (32 * j))
        int __offset3061__ = __offsetinbits3054__ >> 3;
        int __shift3062__ = __offsetinbits3054__ - (__offset3061__ << 3);
        int __leftop2951__ = ((*(int *)(__left2952__ + __offset3061__))  >> __shift3062__) & 0xffffffff;
        // __left3064__ <-- d.s
        // __left3065__ <-- d
        int __left3065__ = (int) d; //varexpr
        // __left3065__ = d
        int __left3064__ = (__left3065__ + 0);
        int __leftop3067__ = 32;
        int __leftop3069__ = 32;
        int __leftop3071__ = 32;
        int __leftop3073__ = 32;
        int __leftop3075__ = 32;
        int __leftop3077__ = 32;
        int __rightop3078__ = 0;
        int __rightop3076__ = __leftop3077__ + __rightop3078__;
        int __rightop3074__ = __leftop3075__ + __rightop3076__;
        int __rightop3072__ = __leftop3073__ + __rightop3074__;
        int __rightop3070__ = __leftop3071__ + __rightop3072__;
        int __rightop3068__ = __leftop3069__ + __rightop3070__;
        int __sizeof3066__ = __leftop3067__ + __rightop3068__;
        int __high3079__ = __left3064__ + __sizeof3066__;
        assertvalidmemory(__left3064__, __high3079__);
        // __left3064__ = d.s
        // __offsetinbits3080__ <-- (32 + (32 + 0))
        int __leftop3081__ = 32;
        int __leftop3083__ = 32;
        int __rightop3084__ = 0;
        int __rightop3082__ = __leftop3083__ + __rightop3084__;
        int __offsetinbits3080__ = __leftop3081__ + __rightop3082__;
        // __offsetinbits3080__ = (32 + (32 + 0))
        int __offset3085__ = __offsetinbits3080__ >> 3;
        int __shift3086__ = __offsetinbits3080__ - (__offset3085__ << 3);
        int __rightop3063__ = ((*(int *)(__left3064__ + __offset3085__))  >> __shift3086__) & 0xffffffff;
        int __leftop2950__ = __leftop2951__ < __rightop3063__;
        // __left3090__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
        // __left3091__ <-- cast(__InodeTable__, d.b[itb])
        // __left3093__ <-- d
        int __left3093__ = (int) d; //varexpr
        // __left3093__ = d
        // __offsetinbits3094__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __leftop3095__ = 0;
        int __leftop3099__ = 8;
        // __left3101__ <-- d.s
        // __left3102__ <-- d
        int __left3102__ = (int) d; //varexpr
        // __left3102__ = d
        int __left3101__ = (__left3102__ + 0);
        int __leftop3104__ = 32;
        int __leftop3106__ = 32;
        int __leftop3108__ = 32;
        int __leftop3110__ = 32;
        int __leftop3112__ = 32;
        int __leftop3114__ = 32;
        int __rightop3115__ = 0;
        int __rightop3113__ = __leftop3114__ + __rightop3115__;
        int __rightop3111__ = __leftop3112__ + __rightop3113__;
        int __rightop3109__ = __leftop3110__ + __rightop3111__;
        int __rightop3107__ = __leftop3108__ + __rightop3109__;
        int __rightop3105__ = __leftop3106__ + __rightop3107__;
        int __sizeof3103__ = __leftop3104__ + __rightop3105__;
        int __high3116__ = __left3101__ + __sizeof3103__;
        assertvalidmemory(__left3101__, __high3116__);
        // __left3101__ = d.s
        // __offsetinbits3117__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop3118__ = 32;
        int __leftop3120__ = 32;
        int __leftop3122__ = 32;
        int __leftop3124__ = 32;
        int __leftop3126__ = 32;
        int __rightop3127__ = 0;
        int __rightop3125__ = __leftop3126__ + __rightop3127__;
        int __rightop3123__ = __leftop3124__ + __rightop3125__;
        int __rightop3121__ = __leftop3122__ + __rightop3123__;
        int __rightop3119__ = __leftop3120__ + __rightop3121__;
        int __offsetinbits3117__ = __leftop3118__ + __rightop3119__;
        // __offsetinbits3117__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset3128__ = __offsetinbits3117__ >> 3;
        int __shift3129__ = __offsetinbits3117__ - (__offset3128__ << 3);
        int __rightop3100__ = ((*(int *)(__left3101__ + __offset3128__))  >> __shift3129__) & 0xffffffff;
        int __leftop3098__ = __leftop3099__ * __rightop3100__;
        int __rightop3130__ = 0;
        int __leftop3097__ = __leftop3098__ + __rightop3130__;
        int __rightop3131__ = (int) __itb__; //varexpr
        int __rightop3096__ = __leftop3097__ * __rightop3131__;
        int __offsetinbits3094__ = __leftop3095__ + __rightop3096__;
        // __offsetinbits3094__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
        int __offset3132__ = __offsetinbits3094__ >> 3;
        int __expr3092__ = (__left3093__ + __offset3132__);
        int __leftop3135__ = 8;
        // __left3137__ <-- d.s
        // __left3138__ <-- d
        int __left3138__ = (int) d; //varexpr
        // __left3138__ = d
        int __left3137__ = (__left3138__ + 0);
        int __leftop3140__ = 32;
        int __leftop3142__ = 32;
        int __leftop3144__ = 32;
        int __leftop3146__ = 32;
        int __leftop3148__ = 32;
        int __leftop3150__ = 32;
        int __rightop3151__ = 0;
        int __rightop3149__ = __leftop3150__ + __rightop3151__;
        int __rightop3147__ = __leftop3148__ + __rightop3149__;
        int __rightop3145__ = __leftop3146__ + __rightop3147__;
        int __rightop3143__ = __leftop3144__ + __rightop3145__;
        int __rightop3141__ = __leftop3142__ + __rightop3143__;
        int __sizeof3139__ = __leftop3140__ + __rightop3141__;
        int __high3152__ = __left3137__ + __sizeof3139__;
        assertvalidmemory(__left3137__, __high3152__);
        // __left3137__ = d.s
        // __offsetinbits3153__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
        int __leftop3154__ = 32;
        int __leftop3156__ = 32;
        int __leftop3158__ = 32;
        int __leftop3160__ = 32;
        int __leftop3162__ = 32;
        int __rightop3163__ = 0;
        int __rightop3161__ = __leftop3162__ + __rightop3163__;
        int __rightop3159__ = __leftop3160__ + __rightop3161__;
        int __rightop3157__ = __leftop3158__ + __rightop3159__;
        int __rightop3155__ = __leftop3156__ + __rightop3157__;
        int __offsetinbits3153__ = __leftop3154__ + __rightop3155__;
        // __offsetinbits3153__ = (32 + (32 + (32 + (32 + (32 + 0)))))
        int __offset3164__ = __offsetinbits3153__ >> 3;
        int __shift3165__ = __offsetinbits3153__ - (__offset3164__ << 3);
        int __rightop3136__ = ((*(int *)(__left3137__ + __offset3164__))  >> __shift3165__) & 0xffffffff;
        int __leftop3134__ = __leftop3135__ * __rightop3136__;
        int __rightop3166__ = 0;
        int __sizeof3133__ = __leftop3134__ + __rightop3166__;
        int __high3167__ = __expr3092__ + __sizeof3133__;
        assertvalidmemory(__expr3092__, __high3167__);
        int __left3091__ = (int) __expr3092__;
        // __left3091__ = cast(__InodeTable__, d.b[itb])
        // __offsetinbits3168__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __leftop3169__ = 0;
        int __leftop3172__ = 32;
        int __leftop3175__ = 32;
        int __rightop3176__ = 12;
        int __leftop3174__ = __leftop3175__ * __rightop3176__;
        int __leftop3178__ = 32;
        int __rightop3179__ = 0;
        int __rightop3177__ = __leftop3178__ + __rightop3179__;
        int __rightop3173__ = __leftop3174__ + __rightop3177__;
        int __leftop3171__ = __leftop3172__ + __rightop3173__;
        int __rightop3180__ = (int) __i__; //varexpr
        int __rightop3170__ = __leftop3171__ * __rightop3180__;
        int __offsetinbits3168__ = __leftop3169__ + __rightop3170__;
        // __offsetinbits3168__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
        int __offset3181__ = __offsetinbits3168__ >> 3;
        int __left3090__ = (__left3091__ + __offset3181__);
        int __leftop3183__ = 32;
        int __leftop3186__ = 32;
        int __rightop3187__ = 12;
        int __leftop3185__ = __leftop3186__ * __rightop3187__;
        int __leftop3189__ = 32;
        int __rightop3190__ = 0;
        int __rightop3188__ = __leftop3189__ + __rightop3190__;
        int __rightop3184__ = __leftop3185__ + __rightop3188__;
        int __sizeof3182__ = __leftop3183__ + __rightop3184__;
        int __high3191__ = __left3090__ + __sizeof3182__;
        assertvalidmemory(__left3090__, __high3191__);
        // __left3090__ = cast(__InodeTable__, d.b[itb]).itable[i]
        // __offsetinbits3192__ <-- ((32 + 0) + (32 * j))
        int __leftop3194__ = 32;
        int __rightop3195__ = 0;
        int __leftop3193__ = __leftop3194__ + __rightop3195__;
        int __leftop3197__ = 32;
        int __rightop3198__ = (int) __j__; //varexpr
        int __rightop3196__ = __leftop3197__ * __rightop3198__;
        int __offsetinbits3192__ = __leftop3193__ + __rightop3196__;
        // __offsetinbits3192__ = ((32 + 0) + (32 * j))
        int __offset3199__ = __offsetinbits3192__ >> 3;
        int __shift3200__ = __offsetinbits3192__ - (__offset3199__ << 3);
        int __leftop3089__ = ((*(int *)(__left3090__ + __offset3199__))  >> __shift3200__) & 0xffffffff;
        int __rightop3201__ = 0;
        int __leftop3088__ = __leftop3089__ == __rightop3201__;
        int __rightop3087__ = !__leftop3088__;
        int __tempvar2949__ = __leftop2950__ && __rightop3087__;
        if (__tempvar2949__)
          {
          // __left3203__ <-- cast(__InodeTable__, d.b[itb]).itable[i]
          // __left3204__ <-- cast(__InodeTable__, d.b[itb])
          // __left3206__ <-- d
          int __left3206__ = (int) d; //varexpr
          // __left3206__ = d
          // __offsetinbits3207__ <-- (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __leftop3208__ = 0;
          int __leftop3212__ = 8;
          // __left3214__ <-- d.s
          // __left3215__ <-- d
          int __left3215__ = (int) d; //varexpr
          // __left3215__ = d
          int __left3214__ = (__left3215__ + 0);
          int __leftop3217__ = 32;
          int __leftop3219__ = 32;
          int __leftop3221__ = 32;
          int __leftop3223__ = 32;
          int __leftop3225__ = 32;
          int __leftop3227__ = 32;
          int __rightop3228__ = 0;
          int __rightop3226__ = __leftop3227__ + __rightop3228__;
          int __rightop3224__ = __leftop3225__ + __rightop3226__;
          int __rightop3222__ = __leftop3223__ + __rightop3224__;
          int __rightop3220__ = __leftop3221__ + __rightop3222__;
          int __rightop3218__ = __leftop3219__ + __rightop3220__;
          int __sizeof3216__ = __leftop3217__ + __rightop3218__;
          int __high3229__ = __left3214__ + __sizeof3216__;
          assertvalidmemory(__left3214__, __high3229__);
          // __left3214__ = d.s
          // __offsetinbits3230__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop3231__ = 32;
          int __leftop3233__ = 32;
          int __leftop3235__ = 32;
          int __leftop3237__ = 32;
          int __leftop3239__ = 32;
          int __rightop3240__ = 0;
          int __rightop3238__ = __leftop3239__ + __rightop3240__;
          int __rightop3236__ = __leftop3237__ + __rightop3238__;
          int __rightop3234__ = __leftop3235__ + __rightop3236__;
          int __rightop3232__ = __leftop3233__ + __rightop3234__;
          int __offsetinbits3230__ = __leftop3231__ + __rightop3232__;
          // __offsetinbits3230__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset3241__ = __offsetinbits3230__ >> 3;
          int __shift3242__ = __offsetinbits3230__ - (__offset3241__ << 3);
          int __rightop3213__ = ((*(int *)(__left3214__ + __offset3241__))  >> __shift3242__) & 0xffffffff;
          int __leftop3211__ = __leftop3212__ * __rightop3213__;
          int __rightop3243__ = 0;
          int __leftop3210__ = __leftop3211__ + __rightop3243__;
          int __rightop3244__ = (int) __itb__; //varexpr
          int __rightop3209__ = __leftop3210__ * __rightop3244__;
          int __offsetinbits3207__ = __leftop3208__ + __rightop3209__;
          // __offsetinbits3207__ = (0 + (((8 * d.s.blocksize) + 0) * itb))
          int __offset3245__ = __offsetinbits3207__ >> 3;
          int __expr3205__ = (__left3206__ + __offset3245__);
          int __leftop3248__ = 8;
          // __left3250__ <-- d.s
          // __left3251__ <-- d
          int __left3251__ = (int) d; //varexpr
          // __left3251__ = d
          int __left3250__ = (__left3251__ + 0);
          int __leftop3253__ = 32;
          int __leftop3255__ = 32;
          int __leftop3257__ = 32;
          int __leftop3259__ = 32;
          int __leftop3261__ = 32;
          int __leftop3263__ = 32;
          int __rightop3264__ = 0;
          int __rightop3262__ = __leftop3263__ + __rightop3264__;
          int __rightop3260__ = __leftop3261__ + __rightop3262__;
          int __rightop3258__ = __leftop3259__ + __rightop3260__;
          int __rightop3256__ = __leftop3257__ + __rightop3258__;
          int __rightop3254__ = __leftop3255__ + __rightop3256__;
          int __sizeof3252__ = __leftop3253__ + __rightop3254__;
          int __high3265__ = __left3250__ + __sizeof3252__;
          assertvalidmemory(__left3250__, __high3265__);
          // __left3250__ = d.s
          // __offsetinbits3266__ <-- (32 + (32 + (32 + (32 + (32 + 0)))))
          int __leftop3267__ = 32;
          int __leftop3269__ = 32;
          int __leftop3271__ = 32;
          int __leftop3273__ = 32;
          int __leftop3275__ = 32;
          int __rightop3276__ = 0;
          int __rightop3274__ = __leftop3275__ + __rightop3276__;
          int __rightop3272__ = __leftop3273__ + __rightop3274__;
          int __rightop3270__ = __leftop3271__ + __rightop3272__;
          int __rightop3268__ = __leftop3269__ + __rightop3270__;
          int __offsetinbits3266__ = __leftop3267__ + __rightop3268__;
          // __offsetinbits3266__ = (32 + (32 + (32 + (32 + (32 + 0)))))
          int __offset3277__ = __offsetinbits3266__ >> 3;
          int __shift3278__ = __offsetinbits3266__ - (__offset3277__ << 3);
          int __rightop3249__ = ((*(int *)(__left3250__ + __offset3277__))  >> __shift3278__) & 0xffffffff;
          int __leftop3247__ = __leftop3248__ * __rightop3249__;
          int __rightop3279__ = 0;
          int __sizeof3246__ = __leftop3247__ + __rightop3279__;
          int __high3280__ = __expr3205__ + __sizeof3246__;
          assertvalidmemory(__expr3205__, __high3280__);
          int __left3204__ = (int) __expr3205__;
          // __left3204__ = cast(__InodeTable__, d.b[itb])
          // __offsetinbits3281__ <-- (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __leftop3282__ = 0;
          int __leftop3285__ = 32;
          int __leftop3288__ = 32;
          int __rightop3289__ = 12;
          int __leftop3287__ = __leftop3288__ * __rightop3289__;
          int __leftop3291__ = 32;
          int __rightop3292__ = 0;
          int __rightop3290__ = __leftop3291__ + __rightop3292__;
          int __rightop3286__ = __leftop3287__ + __rightop3290__;
          int __leftop3284__ = __leftop3285__ + __rightop3286__;
          int __rightop3293__ = (int) __i__; //varexpr
          int __rightop3283__ = __leftop3284__ * __rightop3293__;
          int __offsetinbits3281__ = __leftop3282__ + __rightop3283__;
          // __offsetinbits3281__ = (0 + ((32 + ((32 * 12) + (32 + 0))) * i))
          int __offset3294__ = __offsetinbits3281__ >> 3;
          int __left3203__ = (__left3204__ + __offset3294__);
          int __leftop3296__ = 32;
          int __leftop3299__ = 32;
          int __rightop3300__ = 12;
          int __leftop3298__ = __leftop3299__ * __rightop3300__;
          int __leftop3302__ = 32;
          int __rightop3303__ = 0;
          int __rightop3301__ = __leftop3302__ + __rightop3303__;
          int __rightop3297__ = __leftop3298__ + __rightop3301__;
          int __sizeof3295__ = __leftop3296__ + __rightop3297__;
          int __high3304__ = __left3203__ + __sizeof3295__;
          assertvalidmemory(__left3203__, __high3304__);
          // __left3203__ = cast(__InodeTable__, d.b[itb]).itable[i]
          // __offsetinbits3305__ <-- ((32 + 0) + (32 * j))
          int __leftop3307__ = 32;
          int __rightop3308__ = 0;
          int __leftop3306__ = __leftop3307__ + __rightop3308__;
          int __leftop3310__ = 32;
          int __rightop3311__ = (int) __j__; //varexpr
          int __rightop3309__ = __leftop3310__ * __rightop3311__;
          int __offsetinbits3305__ = __leftop3306__ + __rightop3309__;
          // __offsetinbits3305__ = ((32 + 0) + (32 * j))
          int __offset3312__ = __offsetinbits3305__ >> 3;
          int __shift3313__ = __offsetinbits3305__ - (__offset3312__ << 3);
          int __element3202__ = ((*(int *)(__left3203__ + __offset3312__))  >> __shift3313__) & 0xffffffff;
          int __addeditem3314__ = 1;
          __addeditem3314__ = __Block___hash->add((int)__element3202__, (int)__element3202__);
          }
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
    int __domain3317__ = (int) __u__; //varexpr
    int __leftop3316__;
    int __found3318__ = __inodestatus___hash->get(__domain3317__, __leftop3316__);
    if (!__found3318__) { maybe = 1; }
    int __rightop3319__ = 100;
    int __constraintboolean3315__ = __leftop3316__ == __rightop3319__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 1. ");
      exit(1);
      }
    else if (!__constraintboolean3315__)
      {
      __Success = 0;
      printf("fail 1. ");
      exit(1);
      }
    }
  }


// checking c2
  {
  for (SimpleIterator* __f___iterator = __FreeInode___hash->iterator(); __f___iterator->hasNext(); )
    {
    int __f__ = (int) __f___iterator->next();
    int maybe = 0;
    int __domain3322__ = (int) __f__; //varexpr
    int __leftop3321__;
    int __found3323__ = __inodestatus___hash->get(__domain3322__, __leftop3321__);
    if (!__found3323__) { maybe = 1; }
    int __rightop3324__ = 101;
    int __constraintboolean3320__ = __leftop3321__ == __rightop3324__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 2. ");
      exit(1);
      }
    else if (!__constraintboolean3320__)
      {
      __Success = 0;
      printf("fail 2. ");
      exit(1);
      }
    }
  }


// checking c3
  {
  for (SimpleIterator* __u___iterator = __UsedBlock___hash->iterator(); __u___iterator->hasNext(); )
    {
    int __u__ = (int) __u___iterator->next();
    int maybe = 0;
    int __domain3327__ = (int) __u__; //varexpr
    int __leftop3326__;
    int __found3328__ = __blockstatus___hash->get(__domain3327__, __leftop3326__);
    if (!__found3328__) { maybe = 1; }
    int __rightop3329__ = 100;
    int __constraintboolean3325__ = __leftop3326__ == __rightop3329__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 3. ");
      exit(1);
      }
    else if (!__constraintboolean3325__)
      {
      __Success = 0;
      printf("fail 3. ");
      exit(1);
      }
    }
  }


// checking c4
  {
  for (SimpleIterator* __f___iterator = __FreeBlock___hash->iterator(); __f___iterator->hasNext(); )
    {
    int __f__ = (int) __f___iterator->next();
    int maybe = 0;
    int __domain3332__ = (int) __f__; //varexpr
    int __leftop3331__;
    int __found3333__ = __blockstatus___hash->get(__domain3332__, __leftop3331__);
    if (!__found3333__) { maybe = 1; }
    int __rightop3334__ = 101;
    int __constraintboolean3330__ = __leftop3331__ == __rightop3334__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 4. ");
      exit(1);
      }
    else if (!__constraintboolean3330__)
      {
      __Success = 0;
      printf("fail 4. ");
      exit(1);
      }
    }
  }


// checking c5
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    int maybe = 0;
    int __domain3337__ = (int) __i__; //varexpr
    int __leftop3336__;
    int __found3338__ = __referencecount___hash->get(__domain3337__, __leftop3336__);
    if (!__found3338__) { maybe = 1; }
    int __rightop3339__ = __inodeof___hashinv->count(__i__);
    int __constraintboolean3335__ = __leftop3336__ == __rightop3339__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 5. ");
      exit(1);
      }
    else if (!__constraintboolean3335__)
      {
      __Success = 0;
      printf("fail 5. ");
      exit(1);
      }
    }
  }


// checking c6
  {
  for (SimpleIterator* __i___iterator = __UsedInode___hash->iterator(); __i___iterator->hasNext(); )
    {
    int __i__ = (int) __i___iterator->next();
    int maybe = 0;
    int __domain3342__ = (int) __i__; //varexpr
    int __leftop3341__;
    int __found3343__ = __filesize___hash->get(__domain3342__, __leftop3341__);
    if (!__found3343__) { maybe = 1; }
    int __leftop3345__ = __contents___hash->count(__i__);
    int __rightop3346__ = 8192;
    int __rightop3344__ = __leftop3345__ * __rightop3346__;
    int __constraintboolean3340__ = __leftop3341__ <= __rightop3344__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 6. ");
      exit(1);
      }
    else if (!__constraintboolean3340__)
      {
      __Success = 0;
      printf("fail 6. ");
      exit(1);
      }
    }
  }


// checking c7
  {
  for (SimpleIterator* __b___iterator = __FileDirectoryBlock___hash->iterator(); __b___iterator->hasNext(); )
    {
    int __b__ = (int) __b___iterator->next();
    int maybe = 0;
    int __leftop3348__ = __contents___hashinv->count(__b__);
    int __rightop3349__ = 1;
    int __constraintboolean3347__ = __leftop3348__ == __rightop3349__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 7. ");
      exit(1);
      }
    else if (!__constraintboolean3347__)
      {
      __Success = 0;
      printf("fail 7. ");
      exit(1);
      }
    }
  }


// checking c8
  {
  int maybe = 0;
  int __leftop3351__ = __SuperBlock___hash->count();
  int __rightop3352__ = 1;
  int __constraintboolean3350__ = __leftop3351__ == __rightop3352__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 8. ");
    exit(1);
    }
  else if (!__constraintboolean3350__)
    {
    __Success = 0;
    printf("fail 8. ");
    exit(1);
    }
  }


// checking c9
  {
  int maybe = 0;
  int __leftop3354__ = __GroupBlock___hash->count();
  int __rightop3355__ = 1;
  int __constraintboolean3353__ = __leftop3354__ == __rightop3355__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 9. ");
    exit(1);
    }
  else if (!__constraintboolean3353__)
    {
    __Success = 0;
    printf("fail 9. ");
    exit(1);
    }
  }


// checking c10
  {
  int maybe = 0;
  int __leftop3357__ = __InodeTableBlock___hash->count();
  int __rightop3358__ = 1;
  int __constraintboolean3356__ = __leftop3357__ == __rightop3358__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 10. ");
    exit(1);
    }
  else if (!__constraintboolean3356__)
    {
    __Success = 0;
    printf("fail 10. ");
    exit(1);
    }
  }


// checking c11
  {
  int maybe = 0;
  int __leftop3360__ = __InodeBitmapBlock___hash->count();
  int __rightop3361__ = 1;
  int __constraintboolean3359__ = __leftop3360__ == __rightop3361__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 11. ");
    exit(1);
    }
  else if (!__constraintboolean3359__)
    {
    __Success = 0;
    printf("fail 11. ");
    exit(1);
    }
  }


// checking c12
  {
  int maybe = 0;
  int __leftop3363__ = __BlockBitmapBlock___hash->count();
  int __rightop3364__ = 1;
  int __constraintboolean3362__ = __leftop3363__ == __rightop3364__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 12. ");
    exit(1);
    }
  else if (!__constraintboolean3362__)
    {
    __Success = 0;
    printf("fail 12. ");
    exit(1);
    }
  }


// checking c13
  {
  int maybe = 0;
  int __leftop3366__ = __RootDirectoryInode___hash->count();
  int __rightop3367__ = 1;
  int __constraintboolean3365__ = __leftop3366__ == __rightop3367__;
  if (maybe)
    {
    __Success = 0;
    printf("maybe fail 13. ");
    exit(1);
    }
  else if (!__constraintboolean3365__)
    {
    __Success = 0;
    printf("fail 13. ");
    exit(1);
    }
  }


// checking c14
  {
  for (SimpleIterator* __partitionvar___iterator = __UsedBlock___hash->iterator(); __partitionvar___iterator->hasNext(); )
    {
    int __partitionvar__ = (int) __partitionvar___iterator->next();
    int maybe = 0;
    int __constraintboolean3368__;
    int __leftboolean3369__;
    int __leftboolean3371__;
    int __leftboolean3373__;
    int __leftboolean3375__;
    int __leftboolean3377__;
    int __leftboolean3379__;
    int __leftboolean3381__;
    int __leftboolean3383__;
    int __leftboolean3385__;
    int __exprval3389__ = (int) __partitionvar__; //varexpr
    int __leftboolean3387__ = __SuperBlock___hash->contains(__exprval3389__);
    int __leftmaybe3388__ = maybe;
    int __rightboolean3390__;
    int __exprval3393__ = (int) __partitionvar__; //varexpr
    int __leftboolean3392__ = __GroupBlock___hash->contains(__exprval3393__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3390__ =  !__leftboolean3392__;
      }
    int __rightmaybe3391__ = maybe;
    maybe = (__leftboolean3387__ && __rightmaybe3391__) || (__rightboolean3390__ && __leftmaybe3388__) || (__leftmaybe3388__ && __rightmaybe3391__);
    __leftboolean3385__ = __leftboolean3387__ && __rightboolean3390__;
    int __leftmaybe3386__ = maybe;
    int __rightboolean3394__;
    int __exprval3397__ = (int) __partitionvar__; //varexpr
    int __leftboolean3396__ = __FileDirectoryBlock___hash->contains(__exprval3397__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3394__ =  !__leftboolean3396__;
      }
    int __rightmaybe3395__ = maybe;
    maybe = (__leftboolean3385__ && __rightmaybe3395__) || (__rightboolean3394__ && __leftmaybe3386__) || (__leftmaybe3386__ && __rightmaybe3395__);
    __leftboolean3383__ = __leftboolean3385__ && __rightboolean3394__;
    int __leftmaybe3384__ = maybe;
    int __rightboolean3398__;
    int __exprval3401__ = (int) __partitionvar__; //varexpr
    int __leftboolean3400__ = __InodeTableBlock___hash->contains(__exprval3401__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3398__ =  !__leftboolean3400__;
      }
    int __rightmaybe3399__ = maybe;
    maybe = (__leftboolean3383__ && __rightmaybe3399__) || (__rightboolean3398__ && __leftmaybe3384__) || (__leftmaybe3384__ && __rightmaybe3399__);
    __leftboolean3381__ = __leftboolean3383__ && __rightboolean3398__;
    int __leftmaybe3382__ = maybe;
    int __rightboolean3402__;
    int __exprval3405__ = (int) __partitionvar__; //varexpr
    int __leftboolean3404__ = __InodeBitmapBlock___hash->contains(__exprval3405__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3402__ =  !__leftboolean3404__;
      }
    int __rightmaybe3403__ = maybe;
    maybe = (__leftboolean3381__ && __rightmaybe3403__) || (__rightboolean3402__ && __leftmaybe3382__) || (__leftmaybe3382__ && __rightmaybe3403__);
    __leftboolean3379__ = __leftboolean3381__ && __rightboolean3402__;
    int __leftmaybe3380__ = maybe;
    int __rightboolean3406__;
    int __exprval3409__ = (int) __partitionvar__; //varexpr
    int __leftboolean3408__ = __BlockBitmapBlock___hash->contains(__exprval3409__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3406__ =  !__leftboolean3408__;
      }
    int __rightmaybe3407__ = maybe;
    maybe = (__leftboolean3379__ && __rightmaybe3407__) || (__rightboolean3406__ && __leftmaybe3380__) || (__leftmaybe3380__ && __rightmaybe3407__);
    __leftboolean3377__ = __leftboolean3379__ && __rightboolean3406__;
    int __leftmaybe3378__ = maybe;
    int __rightboolean3410__;
    int __leftboolean3412__;
    int __leftboolean3414__;
    int __leftboolean3416__;
    int __leftboolean3418__;
    int __leftboolean3420__;
    int __exprval3423__ = (int) __partitionvar__; //varexpr
    int __leftboolean3422__ = __SuperBlock___hash->contains(__exprval3423__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3420__ =  !__leftboolean3422__;
      }
    int __leftmaybe3421__ = maybe;
    int __exprval3426__ = (int) __partitionvar__; //varexpr
    int __rightboolean3424__ = __GroupBlock___hash->contains(__exprval3426__);
    int __rightmaybe3425__ = maybe;
    maybe = (__leftboolean3420__ && __rightmaybe3425__) || (__rightboolean3424__ && __leftmaybe3421__) || (__leftmaybe3421__ && __rightmaybe3425__);
    __leftboolean3418__ = __leftboolean3420__ && __rightboolean3424__;
    int __leftmaybe3419__ = maybe;
    int __rightboolean3427__;
    int __exprval3430__ = (int) __partitionvar__; //varexpr
    int __leftboolean3429__ = __FileDirectoryBlock___hash->contains(__exprval3430__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3427__ =  !__leftboolean3429__;
      }
    int __rightmaybe3428__ = maybe;
    maybe = (__leftboolean3418__ && __rightmaybe3428__) || (__rightboolean3427__ && __leftmaybe3419__) || (__leftmaybe3419__ && __rightmaybe3428__);
    __leftboolean3416__ = __leftboolean3418__ && __rightboolean3427__;
    int __leftmaybe3417__ = maybe;
    int __rightboolean3431__;
    int __exprval3434__ = (int) __partitionvar__; //varexpr
    int __leftboolean3433__ = __InodeTableBlock___hash->contains(__exprval3434__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3431__ =  !__leftboolean3433__;
      }
    int __rightmaybe3432__ = maybe;
    maybe = (__leftboolean3416__ && __rightmaybe3432__) || (__rightboolean3431__ && __leftmaybe3417__) || (__leftmaybe3417__ && __rightmaybe3432__);
    __leftboolean3414__ = __leftboolean3416__ && __rightboolean3431__;
    int __leftmaybe3415__ = maybe;
    int __rightboolean3435__;
    int __exprval3438__ = (int) __partitionvar__; //varexpr
    int __leftboolean3437__ = __InodeBitmapBlock___hash->contains(__exprval3438__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3435__ =  !__leftboolean3437__;
      }
    int __rightmaybe3436__ = maybe;
    maybe = (__leftboolean3414__ && __rightmaybe3436__) || (__rightboolean3435__ && __leftmaybe3415__) || (__leftmaybe3415__ && __rightmaybe3436__);
    __leftboolean3412__ = __leftboolean3414__ && __rightboolean3435__;
    int __leftmaybe3413__ = maybe;
    int __rightboolean3439__;
    int __exprval3442__ = (int) __partitionvar__; //varexpr
    int __leftboolean3441__ = __BlockBitmapBlock___hash->contains(__exprval3442__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3439__ =  !__leftboolean3441__;
      }
    int __rightmaybe3440__ = maybe;
    maybe = (__leftboolean3412__ && __rightmaybe3440__) || (__rightboolean3439__ && __leftmaybe3413__) || (__leftmaybe3413__ && __rightmaybe3440__);
    __rightboolean3410__ = __leftboolean3412__ && __rightboolean3439__;
    int __rightmaybe3411__ = maybe;
    maybe = (!__leftboolean3377__ && __rightmaybe3411__) || (!__rightboolean3410__ && __leftmaybe3378__) || (__leftmaybe3378__ && __rightmaybe3411__);
    __leftboolean3375__ = __leftboolean3377__ || __rightboolean3410__;
    int __leftmaybe3376__ = maybe;
    int __rightboolean3443__;
    int __leftboolean3445__;
    int __leftboolean3447__;
    int __leftboolean3449__;
    int __leftboolean3451__;
    int __leftboolean3453__;
    int __exprval3456__ = (int) __partitionvar__; //varexpr
    int __leftboolean3455__ = __SuperBlock___hash->contains(__exprval3456__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3453__ =  !__leftboolean3455__;
      }
    int __leftmaybe3454__ = maybe;
    int __rightboolean3457__;
    int __exprval3460__ = (int) __partitionvar__; //varexpr
    int __leftboolean3459__ = __GroupBlock___hash->contains(__exprval3460__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3457__ =  !__leftboolean3459__;
      }
    int __rightmaybe3458__ = maybe;
    maybe = (__leftboolean3453__ && __rightmaybe3458__) || (__rightboolean3457__ && __leftmaybe3454__) || (__leftmaybe3454__ && __rightmaybe3458__);
    __leftboolean3451__ = __leftboolean3453__ && __rightboolean3457__;
    int __leftmaybe3452__ = maybe;
    int __exprval3463__ = (int) __partitionvar__; //varexpr
    int __rightboolean3461__ = __FileDirectoryBlock___hash->contains(__exprval3463__);
    int __rightmaybe3462__ = maybe;
    maybe = (__leftboolean3451__ && __rightmaybe3462__) || (__rightboolean3461__ && __leftmaybe3452__) || (__leftmaybe3452__ && __rightmaybe3462__);
    __leftboolean3449__ = __leftboolean3451__ && __rightboolean3461__;
    int __leftmaybe3450__ = maybe;
    int __rightboolean3464__;
    int __exprval3467__ = (int) __partitionvar__; //varexpr
    int __leftboolean3466__ = __InodeTableBlock___hash->contains(__exprval3467__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3464__ =  !__leftboolean3466__;
      }
    int __rightmaybe3465__ = maybe;
    maybe = (__leftboolean3449__ && __rightmaybe3465__) || (__rightboolean3464__ && __leftmaybe3450__) || (__leftmaybe3450__ && __rightmaybe3465__);
    __leftboolean3447__ = __leftboolean3449__ && __rightboolean3464__;
    int __leftmaybe3448__ = maybe;
    int __rightboolean3468__;
    int __exprval3471__ = (int) __partitionvar__; //varexpr
    int __leftboolean3470__ = __InodeBitmapBlock___hash->contains(__exprval3471__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3468__ =  !__leftboolean3470__;
      }
    int __rightmaybe3469__ = maybe;
    maybe = (__leftboolean3447__ && __rightmaybe3469__) || (__rightboolean3468__ && __leftmaybe3448__) || (__leftmaybe3448__ && __rightmaybe3469__);
    __leftboolean3445__ = __leftboolean3447__ && __rightboolean3468__;
    int __leftmaybe3446__ = maybe;
    int __rightboolean3472__;
    int __exprval3475__ = (int) __partitionvar__; //varexpr
    int __leftboolean3474__ = __BlockBitmapBlock___hash->contains(__exprval3475__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3472__ =  !__leftboolean3474__;
      }
    int __rightmaybe3473__ = maybe;
    maybe = (__leftboolean3445__ && __rightmaybe3473__) || (__rightboolean3472__ && __leftmaybe3446__) || (__leftmaybe3446__ && __rightmaybe3473__);
    __rightboolean3443__ = __leftboolean3445__ && __rightboolean3472__;
    int __rightmaybe3444__ = maybe;
    maybe = (!__leftboolean3375__ && __rightmaybe3444__) || (!__rightboolean3443__ && __leftmaybe3376__) || (__leftmaybe3376__ && __rightmaybe3444__);
    __leftboolean3373__ = __leftboolean3375__ || __rightboolean3443__;
    int __leftmaybe3374__ = maybe;
    int __rightboolean3476__;
    int __leftboolean3478__;
    int __leftboolean3480__;
    int __leftboolean3482__;
    int __leftboolean3484__;
    int __leftboolean3486__;
    int __exprval3489__ = (int) __partitionvar__; //varexpr
    int __leftboolean3488__ = __SuperBlock___hash->contains(__exprval3489__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3486__ =  !__leftboolean3488__;
      }
    int __leftmaybe3487__ = maybe;
    int __rightboolean3490__;
    int __exprval3493__ = (int) __partitionvar__; //varexpr
    int __leftboolean3492__ = __GroupBlock___hash->contains(__exprval3493__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3490__ =  !__leftboolean3492__;
      }
    int __rightmaybe3491__ = maybe;
    maybe = (__leftboolean3486__ && __rightmaybe3491__) || (__rightboolean3490__ && __leftmaybe3487__) || (__leftmaybe3487__ && __rightmaybe3491__);
    __leftboolean3484__ = __leftboolean3486__ && __rightboolean3490__;
    int __leftmaybe3485__ = maybe;
    int __rightboolean3494__;
    int __exprval3497__ = (int) __partitionvar__; //varexpr
    int __leftboolean3496__ = __FileDirectoryBlock___hash->contains(__exprval3497__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3494__ =  !__leftboolean3496__;
      }
    int __rightmaybe3495__ = maybe;
    maybe = (__leftboolean3484__ && __rightmaybe3495__) || (__rightboolean3494__ && __leftmaybe3485__) || (__leftmaybe3485__ && __rightmaybe3495__);
    __leftboolean3482__ = __leftboolean3484__ && __rightboolean3494__;
    int __leftmaybe3483__ = maybe;
    int __exprval3500__ = (int) __partitionvar__; //varexpr
    int __rightboolean3498__ = __InodeTableBlock___hash->contains(__exprval3500__);
    int __rightmaybe3499__ = maybe;
    maybe = (__leftboolean3482__ && __rightmaybe3499__) || (__rightboolean3498__ && __leftmaybe3483__) || (__leftmaybe3483__ && __rightmaybe3499__);
    __leftboolean3480__ = __leftboolean3482__ && __rightboolean3498__;
    int __leftmaybe3481__ = maybe;
    int __rightboolean3501__;
    int __exprval3504__ = (int) __partitionvar__; //varexpr
    int __leftboolean3503__ = __InodeBitmapBlock___hash->contains(__exprval3504__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3501__ =  !__leftboolean3503__;
      }
    int __rightmaybe3502__ = maybe;
    maybe = (__leftboolean3480__ && __rightmaybe3502__) || (__rightboolean3501__ && __leftmaybe3481__) || (__leftmaybe3481__ && __rightmaybe3502__);
    __leftboolean3478__ = __leftboolean3480__ && __rightboolean3501__;
    int __leftmaybe3479__ = maybe;
    int __rightboolean3505__;
    int __exprval3508__ = (int) __partitionvar__; //varexpr
    int __leftboolean3507__ = __BlockBitmapBlock___hash->contains(__exprval3508__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3505__ =  !__leftboolean3507__;
      }
    int __rightmaybe3506__ = maybe;
    maybe = (__leftboolean3478__ && __rightmaybe3506__) || (__rightboolean3505__ && __leftmaybe3479__) || (__leftmaybe3479__ && __rightmaybe3506__);
    __rightboolean3476__ = __leftboolean3478__ && __rightboolean3505__;
    int __rightmaybe3477__ = maybe;
    maybe = (!__leftboolean3373__ && __rightmaybe3477__) || (!__rightboolean3476__ && __leftmaybe3374__) || (__leftmaybe3374__ && __rightmaybe3477__);
    __leftboolean3371__ = __leftboolean3373__ || __rightboolean3476__;
    int __leftmaybe3372__ = maybe;
    int __rightboolean3509__;
    int __leftboolean3511__;
    int __leftboolean3513__;
    int __leftboolean3515__;
    int __leftboolean3517__;
    int __leftboolean3519__;
    int __exprval3522__ = (int) __partitionvar__; //varexpr
    int __leftboolean3521__ = __SuperBlock___hash->contains(__exprval3522__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3519__ =  !__leftboolean3521__;
      }
    int __leftmaybe3520__ = maybe;
    int __rightboolean3523__;
    int __exprval3526__ = (int) __partitionvar__; //varexpr
    int __leftboolean3525__ = __GroupBlock___hash->contains(__exprval3526__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3523__ =  !__leftboolean3525__;
      }
    int __rightmaybe3524__ = maybe;
    maybe = (__leftboolean3519__ && __rightmaybe3524__) || (__rightboolean3523__ && __leftmaybe3520__) || (__leftmaybe3520__ && __rightmaybe3524__);
    __leftboolean3517__ = __leftboolean3519__ && __rightboolean3523__;
    int __leftmaybe3518__ = maybe;
    int __rightboolean3527__;
    int __exprval3530__ = (int) __partitionvar__; //varexpr
    int __leftboolean3529__ = __FileDirectoryBlock___hash->contains(__exprval3530__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3527__ =  !__leftboolean3529__;
      }
    int __rightmaybe3528__ = maybe;
    maybe = (__leftboolean3517__ && __rightmaybe3528__) || (__rightboolean3527__ && __leftmaybe3518__) || (__leftmaybe3518__ && __rightmaybe3528__);
    __leftboolean3515__ = __leftboolean3517__ && __rightboolean3527__;
    int __leftmaybe3516__ = maybe;
    int __rightboolean3531__;
    int __exprval3534__ = (int) __partitionvar__; //varexpr
    int __leftboolean3533__ = __InodeTableBlock___hash->contains(__exprval3534__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3531__ =  !__leftboolean3533__;
      }
    int __rightmaybe3532__ = maybe;
    maybe = (__leftboolean3515__ && __rightmaybe3532__) || (__rightboolean3531__ && __leftmaybe3516__) || (__leftmaybe3516__ && __rightmaybe3532__);
    __leftboolean3513__ = __leftboolean3515__ && __rightboolean3531__;
    int __leftmaybe3514__ = maybe;
    int __exprval3537__ = (int) __partitionvar__; //varexpr
    int __rightboolean3535__ = __InodeBitmapBlock___hash->contains(__exprval3537__);
    int __rightmaybe3536__ = maybe;
    maybe = (__leftboolean3513__ && __rightmaybe3536__) || (__rightboolean3535__ && __leftmaybe3514__) || (__leftmaybe3514__ && __rightmaybe3536__);
    __leftboolean3511__ = __leftboolean3513__ && __rightboolean3535__;
    int __leftmaybe3512__ = maybe;
    int __rightboolean3538__;
    int __exprval3541__ = (int) __partitionvar__; //varexpr
    int __leftboolean3540__ = __BlockBitmapBlock___hash->contains(__exprval3541__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3538__ =  !__leftboolean3540__;
      }
    int __rightmaybe3539__ = maybe;
    maybe = (__leftboolean3511__ && __rightmaybe3539__) || (__rightboolean3538__ && __leftmaybe3512__) || (__leftmaybe3512__ && __rightmaybe3539__);
    __rightboolean3509__ = __leftboolean3511__ && __rightboolean3538__;
    int __rightmaybe3510__ = maybe;
    maybe = (!__leftboolean3371__ && __rightmaybe3510__) || (!__rightboolean3509__ && __leftmaybe3372__) || (__leftmaybe3372__ && __rightmaybe3510__);
    __leftboolean3369__ = __leftboolean3371__ || __rightboolean3509__;
    int __leftmaybe3370__ = maybe;
    int __rightboolean3542__;
    int __leftboolean3544__;
    int __leftboolean3546__;
    int __leftboolean3548__;
    int __leftboolean3550__;
    int __leftboolean3552__;
    int __exprval3555__ = (int) __partitionvar__; //varexpr
    int __leftboolean3554__ = __SuperBlock___hash->contains(__exprval3555__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3552__ =  !__leftboolean3554__;
      }
    int __leftmaybe3553__ = maybe;
    int __rightboolean3556__;
    int __exprval3559__ = (int) __partitionvar__; //varexpr
    int __leftboolean3558__ = __GroupBlock___hash->contains(__exprval3559__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3556__ =  !__leftboolean3558__;
      }
    int __rightmaybe3557__ = maybe;
    maybe = (__leftboolean3552__ && __rightmaybe3557__) || (__rightboolean3556__ && __leftmaybe3553__) || (__leftmaybe3553__ && __rightmaybe3557__);
    __leftboolean3550__ = __leftboolean3552__ && __rightboolean3556__;
    int __leftmaybe3551__ = maybe;
    int __rightboolean3560__;
    int __exprval3563__ = (int) __partitionvar__; //varexpr
    int __leftboolean3562__ = __FileDirectoryBlock___hash->contains(__exprval3563__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3560__ =  !__leftboolean3562__;
      }
    int __rightmaybe3561__ = maybe;
    maybe = (__leftboolean3550__ && __rightmaybe3561__) || (__rightboolean3560__ && __leftmaybe3551__) || (__leftmaybe3551__ && __rightmaybe3561__);
    __leftboolean3548__ = __leftboolean3550__ && __rightboolean3560__;
    int __leftmaybe3549__ = maybe;
    int __rightboolean3564__;
    int __exprval3567__ = (int) __partitionvar__; //varexpr
    int __leftboolean3566__ = __InodeTableBlock___hash->contains(__exprval3567__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3564__ =  !__leftboolean3566__;
      }
    int __rightmaybe3565__ = maybe;
    maybe = (__leftboolean3548__ && __rightmaybe3565__) || (__rightboolean3564__ && __leftmaybe3549__) || (__leftmaybe3549__ && __rightmaybe3565__);
    __leftboolean3546__ = __leftboolean3548__ && __rightboolean3564__;
    int __leftmaybe3547__ = maybe;
    int __rightboolean3568__;
    int __exprval3571__ = (int) __partitionvar__; //varexpr
    int __leftboolean3570__ = __InodeBitmapBlock___hash->contains(__exprval3571__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3568__ =  !__leftboolean3570__;
      }
    int __rightmaybe3569__ = maybe;
    maybe = (__leftboolean3546__ && __rightmaybe3569__) || (__rightboolean3568__ && __leftmaybe3547__) || (__leftmaybe3547__ && __rightmaybe3569__);
    __leftboolean3544__ = __leftboolean3546__ && __rightboolean3568__;
    int __leftmaybe3545__ = maybe;
    int __exprval3574__ = (int) __partitionvar__; //varexpr
    int __rightboolean3572__ = __BlockBitmapBlock___hash->contains(__exprval3574__);
    int __rightmaybe3573__ = maybe;
    maybe = (__leftboolean3544__ && __rightmaybe3573__) || (__rightboolean3572__ && __leftmaybe3545__) || (__leftmaybe3545__ && __rightmaybe3573__);
    __rightboolean3542__ = __leftboolean3544__ && __rightboolean3572__;
    int __rightmaybe3543__ = maybe;
    maybe = (!__leftboolean3369__ && __rightmaybe3543__) || (!__rightboolean3542__ && __leftmaybe3370__) || (__leftmaybe3370__ && __rightmaybe3543__);
    __constraintboolean3368__ = __leftboolean3369__ || __rightboolean3542__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 14. ");
      exit(1);
      }
    else if (!__constraintboolean3368__)
      {
      __Success = 0;
      printf("fail 14. ");
      exit(1);
      }
    }
  }


// checking c15
  {
  for (SimpleIterator* __partitionvar___iterator = __UsedInode___hash->iterator(); __partitionvar___iterator->hasNext(); )
    {
    int __partitionvar__ = (int) __partitionvar___iterator->next();
    int maybe = 0;
    int __constraintboolean3575__;
    int __leftboolean3576__;
    int __exprval3580__ = (int) __partitionvar__; //varexpr
    int __leftboolean3578__ = __FileInode___hash->contains(__exprval3580__);
    int __leftmaybe3579__ = maybe;
    int __rightboolean3581__;
    int __exprval3584__ = (int) __partitionvar__; //varexpr
    int __leftboolean3583__ = __DirectoryInode___hash->contains(__exprval3584__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3581__ =  !__leftboolean3583__;
      }
    int __rightmaybe3582__ = maybe;
    maybe = (__leftboolean3578__ && __rightmaybe3582__) || (__rightboolean3581__ && __leftmaybe3579__) || (__leftmaybe3579__ && __rightmaybe3582__);
    __leftboolean3576__ = __leftboolean3578__ && __rightboolean3581__;
    int __leftmaybe3577__ = maybe;
    int __rightboolean3585__;
    int __leftboolean3587__;
    int __exprval3590__ = (int) __partitionvar__; //varexpr
    int __leftboolean3589__ = __FileInode___hash->contains(__exprval3590__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3587__ =  !__leftboolean3589__;
      }
    int __leftmaybe3588__ = maybe;
    int __exprval3593__ = (int) __partitionvar__; //varexpr
    int __rightboolean3591__ = __DirectoryInode___hash->contains(__exprval3593__);
    int __rightmaybe3592__ = maybe;
    maybe = (__leftboolean3587__ && __rightmaybe3592__) || (__rightboolean3591__ && __leftmaybe3588__) || (__leftmaybe3588__ && __rightmaybe3592__);
    __rightboolean3585__ = __leftboolean3587__ && __rightboolean3591__;
    int __rightmaybe3586__ = maybe;
    maybe = (!__leftboolean3576__ && __rightmaybe3586__) || (!__rightboolean3585__ && __leftmaybe3577__) || (__leftmaybe3577__ && __rightmaybe3586__);
    __constraintboolean3575__ = __leftboolean3576__ || __rightboolean3585__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 15. ");
      exit(1);
      }
    else if (!__constraintboolean3575__)
      {
      __Success = 0;
      printf("fail 15. ");
      exit(1);
      }
    }
  }


// checking c16
  {
  for (SimpleIterator* __partitionvar___iterator = __Block___hash->iterator(); __partitionvar___iterator->hasNext(); )
    {
    int __partitionvar__ = (int) __partitionvar___iterator->next();
    int maybe = 0;
    int __constraintboolean3594__;
    int __leftboolean3595__;
    int __exprval3599__ = (int) __partitionvar__; //varexpr
    int __leftboolean3597__ = __UsedBlock___hash->contains(__exprval3599__);
    int __leftmaybe3598__ = maybe;
    int __rightboolean3600__;
    int __exprval3603__ = (int) __partitionvar__; //varexpr
    int __leftboolean3602__ = __FreeBlock___hash->contains(__exprval3603__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3600__ =  !__leftboolean3602__;
      }
    int __rightmaybe3601__ = maybe;
    maybe = (__leftboolean3597__ && __rightmaybe3601__) || (__rightboolean3600__ && __leftmaybe3598__) || (__leftmaybe3598__ && __rightmaybe3601__);
    __leftboolean3595__ = __leftboolean3597__ && __rightboolean3600__;
    int __leftmaybe3596__ = maybe;
    int __rightboolean3604__;
    int __leftboolean3606__;
    int __exprval3609__ = (int) __partitionvar__; //varexpr
    int __leftboolean3608__ = __UsedBlock___hash->contains(__exprval3609__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3606__ =  !__leftboolean3608__;
      }
    int __leftmaybe3607__ = maybe;
    int __exprval3612__ = (int) __partitionvar__; //varexpr
    int __rightboolean3610__ = __FreeBlock___hash->contains(__exprval3612__);
    int __rightmaybe3611__ = maybe;
    maybe = (__leftboolean3606__ && __rightmaybe3611__) || (__rightboolean3610__ && __leftmaybe3607__) || (__leftmaybe3607__ && __rightmaybe3611__);
    __rightboolean3604__ = __leftboolean3606__ && __rightboolean3610__;
    int __rightmaybe3605__ = maybe;
    maybe = (!__leftboolean3595__ && __rightmaybe3605__) || (!__rightboolean3604__ && __leftmaybe3596__) || (__leftmaybe3596__ && __rightmaybe3605__);
    __constraintboolean3594__ = __leftboolean3595__ || __rightboolean3604__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 16. ");
      exit(1);
      }
    else if (!__constraintboolean3594__)
      {
      __Success = 0;
      printf("fail 16. ");
      exit(1);
      }
    }
  }


// checking c17
  {
  for (SimpleIterator* __partitionvar___iterator = __Inode___hash->iterator(); __partitionvar___iterator->hasNext(); )
    {
    int __partitionvar__ = (int) __partitionvar___iterator->next();
    int maybe = 0;
    int __constraintboolean3613__;
    int __leftboolean3614__;
    int __exprval3618__ = (int) __partitionvar__; //varexpr
    int __leftboolean3616__ = __UsedInode___hash->contains(__exprval3618__);
    int __leftmaybe3617__ = maybe;
    int __rightboolean3619__;
    int __exprval3622__ = (int) __partitionvar__; //varexpr
    int __leftboolean3621__ = __FreeInode___hash->contains(__exprval3622__);
    // 3-valued NOT
    if (!maybe)
      {
      __rightboolean3619__ =  !__leftboolean3621__;
      }
    int __rightmaybe3620__ = maybe;
    maybe = (__leftboolean3616__ && __rightmaybe3620__) || (__rightboolean3619__ && __leftmaybe3617__) || (__leftmaybe3617__ && __rightmaybe3620__);
    __leftboolean3614__ = __leftboolean3616__ && __rightboolean3619__;
    int __leftmaybe3615__ = maybe;
    int __rightboolean3623__;
    int __leftboolean3625__;
    int __exprval3628__ = (int) __partitionvar__; //varexpr
    int __leftboolean3627__ = __UsedInode___hash->contains(__exprval3628__);
    // 3-valued NOT
    if (!maybe)
      {
      __leftboolean3625__ =  !__leftboolean3627__;
      }
    int __leftmaybe3626__ = maybe;
    int __exprval3631__ = (int) __partitionvar__; //varexpr
    int __rightboolean3629__ = __FreeInode___hash->contains(__exprval3631__);
    int __rightmaybe3630__ = maybe;
    maybe = (__leftboolean3625__ && __rightmaybe3630__) || (__rightboolean3629__ && __leftmaybe3626__) || (__leftmaybe3626__ && __rightmaybe3630__);
    __rightboolean3623__ = __leftboolean3625__ && __rightboolean3629__;
    int __rightmaybe3624__ = maybe;
    maybe = (!__leftboolean3614__ && __rightmaybe3624__) || (!__rightboolean3623__ && __leftmaybe3615__) || (__leftmaybe3615__ && __rightmaybe3624__);
    __constraintboolean3613__ = __leftboolean3614__ || __rightboolean3623__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 17. ");
      exit(1);
      }
    else if (!__constraintboolean3613__)
      {
      __Success = 0;
      printf("fail 17. ");
      exit(1);
      }
    }
  }


// checking c18
  {
  for (SimpleIterator* __partitionvar1___iterator = __referencecount___hash->iterator(); __partitionvar1___iterator->hasNext(); )
    {
    int __partitionvar2__ = (int) __partitionvar1___iterator->next();
    int __partitionvar1__ = (int) __partitionvar1___iterator->key();
    int maybe = 0;
    int __constraintboolean3632__;
    int __exprval3635__ = (int) __partitionvar1__; //varexpr
    int __leftboolean3633__ = __Inode___hash->contains(__exprval3635__);
    int __leftmaybe3634__ = maybe;
    int __exprval3638__ = (int) __partitionvar2__; //varexpr
    int __rightboolean3636__ = __int___hash->contains(__exprval3638__);
    int __rightmaybe3637__ = maybe;
    maybe = (__leftboolean3633__ && __rightmaybe3637__) || (__rightboolean3636__ && __leftmaybe3634__) || (__leftmaybe3634__ && __rightmaybe3637__);
    __constraintboolean3632__ = __leftboolean3633__ && __rightboolean3636__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 18. ");
      exit(1);
      }
    else if (!__constraintboolean3632__)
      {
      __Success = 0;
      printf("fail 18. ");
      exit(1);
      }
    }
  }


// checking c19
  {
  for (SimpleIterator* __partitionvar1___iterator = __filesize___hash->iterator(); __partitionvar1___iterator->hasNext(); )
    {
    int __partitionvar2__ = (int) __partitionvar1___iterator->next();
    int __partitionvar1__ = (int) __partitionvar1___iterator->key();
    int maybe = 0;
    int __constraintboolean3639__;
    int __exprval3642__ = (int) __partitionvar1__; //varexpr
    int __leftboolean3640__ = __Inode___hash->contains(__exprval3642__);
    int __leftmaybe3641__ = maybe;
    int __exprval3645__ = (int) __partitionvar2__; //varexpr
    int __rightboolean3643__ = __int___hash->contains(__exprval3645__);
    int __rightmaybe3644__ = maybe;
    maybe = (__leftboolean3640__ && __rightmaybe3644__) || (__rightboolean3643__ && __leftmaybe3641__) || (__leftmaybe3641__ && __rightmaybe3644__);
    __constraintboolean3639__ = __leftboolean3640__ && __rightboolean3643__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 19. ");
      exit(1);
      }
    else if (!__constraintboolean3639__)
      {
      __Success = 0;
      printf("fail 19. ");
      exit(1);
      }
    }
  }


// checking c20
  {
  for (SimpleIterator* __partitionvar1___iterator = __inodeof___hash->iterator(); __partitionvar1___iterator->hasNext(); )
    {
    int __partitionvar2__ = (int) __partitionvar1___iterator->next();
    __DirectoryEntry__ __partitionvar1__ = (__DirectoryEntry__) __partitionvar1___iterator->key();
    int maybe = 0;
    int __constraintboolean3646__;
    int __exprval3649__ = (int) __partitionvar1__; //varexpr
    int __leftboolean3647__ = __DirectoryEntry___hash->contains(__exprval3649__);
    int __leftmaybe3648__ = maybe;
    int __exprval3652__ = (int) __partitionvar2__; //varexpr
    int __rightboolean3650__ = __UsedInode___hash->contains(__exprval3652__);
    int __rightmaybe3651__ = maybe;
    maybe = (__leftboolean3647__ && __rightmaybe3651__) || (__rightboolean3650__ && __leftmaybe3648__) || (__leftmaybe3648__ && __rightmaybe3651__);
    __constraintboolean3646__ = __leftboolean3647__ && __rightboolean3650__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 20. ");
      exit(1);
      }
    else if (!__constraintboolean3646__)
      {
      __Success = 0;
      printf("fail 20. ");
      exit(1);
      }
    }
  }


// checking c21
  {
  for (SimpleIterator* __partitionvar1___iterator = __contents___hash->iterator(); __partitionvar1___iterator->hasNext(); )
    {
    int __partitionvar2__ = (int) __partitionvar1___iterator->next();
    int __partitionvar1__ = (int) __partitionvar1___iterator->key();
    int maybe = 0;
    int __constraintboolean3653__;
    int __exprval3656__ = (int) __partitionvar1__; //varexpr
    int __leftboolean3654__ = __UsedInode___hash->contains(__exprval3656__);
    int __leftmaybe3655__ = maybe;
    int __exprval3659__ = (int) __partitionvar2__; //varexpr
    int __rightboolean3657__ = __FileDirectoryBlock___hash->contains(__exprval3659__);
    int __rightmaybe3658__ = maybe;
    maybe = (__leftboolean3654__ && __rightmaybe3658__) || (__rightboolean3657__ && __leftmaybe3655__) || (__leftmaybe3655__ && __rightmaybe3658__);
    __constraintboolean3653__ = __leftboolean3654__ && __rightboolean3657__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 21. ");
      exit(1);
      }
    else if (!__constraintboolean3653__)
      {
      __Success = 0;
      printf("fail 21. ");
      exit(1);
      }
    }
  }


// checking c22
  {
  for (SimpleIterator* __partitionvar1___iterator = __inodestatus___hash->iterator(); __partitionvar1___iterator->hasNext(); )
    {
    int __partitionvar2__ = (int) __partitionvar1___iterator->next();
    int __partitionvar1__ = (int) __partitionvar1___iterator->key();
    int maybe = 0;
    int __constraintboolean3660__;
    int __exprval3663__ = (int) __partitionvar1__; //varexpr
    int __leftboolean3661__ = __Inode___hash->contains(__exprval3663__);
    int __leftmaybe3662__ = maybe;
    int __exprval3666__ = (int) __partitionvar2__; //varexpr
    int __rightboolean3664__ = __token___hash->contains(__exprval3666__);
    int __rightmaybe3665__ = maybe;
    maybe = (__leftboolean3661__ && __rightmaybe3665__) || (__rightboolean3664__ && __leftmaybe3662__) || (__leftmaybe3662__ && __rightmaybe3665__);
    __constraintboolean3660__ = __leftboolean3661__ && __rightboolean3664__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 22. ");
      exit(1);
      }
    else if (!__constraintboolean3660__)
      {
      __Success = 0;
      printf("fail 22. ");
      exit(1);
      }
    }
  }


// checking c23
  {
  for (SimpleIterator* __partitionvar1___iterator = __blockstatus___hash->iterator(); __partitionvar1___iterator->hasNext(); )
    {
    int __partitionvar2__ = (int) __partitionvar1___iterator->next();
    int __partitionvar1__ = (int) __partitionvar1___iterator->key();
    int maybe = 0;
    int __constraintboolean3667__;
    int __exprval3670__ = (int) __partitionvar1__; //varexpr
    int __leftboolean3668__ = __Block___hash->contains(__exprval3670__);
    int __leftmaybe3669__ = maybe;
    int __exprval3673__ = (int) __partitionvar2__; //varexpr
    int __rightboolean3671__ = __token___hash->contains(__exprval3673__);
    int __rightmaybe3672__ = maybe;
    maybe = (__leftboolean3668__ && __rightmaybe3672__) || (__rightboolean3671__ && __leftmaybe3669__) || (__leftmaybe3669__ && __rightmaybe3672__);
    __constraintboolean3667__ = __leftboolean3668__ && __rightboolean3671__;
    if (maybe)
      {
      __Success = 0;
      printf("maybe fail 23. ");
      exit(1);
      }
    else if (!__constraintboolean3667__)
      {
      __Success = 0;
      printf("fail 23. ");
      exit(1);
      }
    }
  }


//if (__Success) { printf("all tests passed"); }
