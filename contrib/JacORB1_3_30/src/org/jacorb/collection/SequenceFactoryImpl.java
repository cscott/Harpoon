package org.jacorb.collection;

import org.omg.CosCollection.*;

public class  SequenceFactoryImpl extends SequenceFactoryPOA implements IteratorFactory {
    public final static String IMPL_CATEGORY = "ArrayBased";
    private org.omg.PortableServer.POA poa;
    public SequenceFactoryImpl( org.omg.PortableServer.POA poa ){
        this.poa = poa;
        try {
            poa.servant_to_reference(this);
        } catch( Exception e ){
            System.out.println( "Internal error: Can not activate factory" );
            e.printStackTrace();
            throw new org.omg.CORBA.INTERNAL();
        }
    };
    public CSequence create( Operations ops, int expected_size ){
        return create( ops, expected_size, poa );
    };
    public CSequence create( String ops_class, int expected_size ){
        OperationsOperations ops = null;
        try {
            Class operation_class = Class.forName( ops_class );
            ops = (OperationsOperations)operation_class.newInstance();
        } catch ( Exception e ){
            System.out.println( "Internal error: Can not instantiate object of class \""+ops_class+"\"" );
            throw new org.omg.CORBA.INTERNAL();
        };
        return create( ops, expected_size, poa );
    };
    public CSequence create( OperationsOperations ops, int expected_size, org.omg.PortableServer.POA poa ){
        SequenceImpl collection = new SequenceImpl( ops, poa, this, expected_size );
        CSequence collection_ref = null;
        CSequencePOATie srvnt = new CSequencePOATie( collection );
        try {
            collection_ref = CSequenceHelper.narrow(poa.servant_to_reference(srvnt));
            collection.set_servant( srvnt );
        } catch(Exception e) {
            System.out.println("Internal error: Can not Activate collection");
            e.printStackTrace();
            throw new org.omg.CORBA.INTERNAL();
        }
        return collection_ref;
    };
    public Collection generic_create( NVPair[] parameters) throws ParameterInvalid{
        NVPairManager pm = new NVPairManager( parameters );
        String collection_interface     = pm.find_string_param( CollectionService.COL_INTRF );
        String implementation_interface = pm.find_string_param( CollectionService.IMPL_INTRF );
        String implementation_category  = pm.find_string_param( CollectionService.IMPL_CAT );
        if( implementation_category != null && !implementation_category.equals( IMPL_CATEGORY ) ) {
            throw new ParameterInvalid( pm.find_param_idx( CollectionService.IMPL_CAT ), "CollectionFactory : not support implementation category "+implementation_category );
        }
        Integer   size = pm.find_ulong_param( CollectionService.EXP_SIZE );
        if ( size == null ) {
            size = new Integer(10);
        }
        Operations ops = pm.find_operations_param( CollectionService.OPERATIONS );
        if ( ops == null ) {
            String ops_class = pm.find_string_param( CollectionService.OPERATIONS_CLASS );
            if( ops_class == null ){
                throw new ParameterInvalid( pm.find_param_idx(CollectionService.OPERATIONS), "CollectionFactory: OPERATION object not defined" );
            }
            return create( ops_class, size.intValue() );
        } else {
            return create( ops, size.intValue() );
        }
    };
    public PositionalIteratorImpl create_iterator( CollectionImpl collection, boolean read_only ){
        return create_iterator( collection, read_only, false );
    };
    public PositionalIteratorImpl create_iterator( CollectionImpl collection, boolean read_only, boolean reverse ){
        return new SequentialIteratorImpl( (SequentialCollectionImpl)collection, read_only, reverse );
    };
}





