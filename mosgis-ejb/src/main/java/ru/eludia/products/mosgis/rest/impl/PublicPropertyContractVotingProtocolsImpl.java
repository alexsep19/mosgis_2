package ru.eludia.products.mosgis.rest.impl;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.Model;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.tables.PublicPropertyContractVotingProtocol;
import ru.eludia.products.mosgis.db.model.tables.VotingProtocol;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.eludia.products.mosgis.rest.User;
import ru.eludia.products.mosgis.rest.api.PublicPropertyContractVotingProtocolsLocal;
import ru.eludia.products.mosgis.rest.impl.base.BaseCRUD;
import ru.eludia.products.mosgis.web.base.ComplexSearch;
import ru.eludia.products.mosgis.web.base.Search;
import ru.eludia.products.mosgis.web.base.SimpleSearch;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PublicPropertyContractVotingProtocolsImpl extends BaseCRUD<PublicPropertyContractVotingProtocol> implements PublicPropertyContractVotingProtocolsLocal {

    private void filterOffDeleted (Select select) {
        select.and ("is_deleted", 0);
    }
    
    private void applyComplexSearch (final ComplexSearch search, Select select) {

        search.filter (select, "");
        
        if (!search.getFilters ().containsKey ("is_deleted")) filterOffDeleted (select);

    }
    
    private void applySimpleSearch (final SimpleSearch search, Select select) {

        filterOffDeleted (select);

        final String searchString = search.getSearchString ();
        
        if (searchString == null || searchString.isEmpty ()) return;

        select.and ("label_uc LIKE %?%", searchString.toUpperCase ());
        
    }
    
    private void applySearch (final Search search, Select select) {        

        if (search instanceof SimpleSearch) {
            applySimpleSearch  ((SimpleSearch) search, select);
        }
        else if (search instanceof ComplexSearch) {
            applyComplexSearch ((ComplexSearch) search, select);
        }
        else {
            filterOffDeleted (select);
        }

    }
    
    private static final String PARENT_REF = PublicPropertyContractVotingProtocol.c.UUID_CTR.lc ();

    @Override
    public JsonObject select (JsonObject p, User user) {return fetchData ((db, job) -> {
                
        Model m = ModelHolder.getModel ();
        
        Select select = m.select (VotingProtocol.class, "AS root", "*", "uuid AS id")
            .orderBy (VotingProtocol.c.PROTOCOLDATE.lc ())
            .limit (p.getInt ("offset"), p.getInt ("limit"))
            .toOne (PublicPropertyContractVotingProtocol.class, "AS m2m", "uuid")
                .where (PARENT_REF, p.getJsonObject ("data").getString (PARENT_REF))
                .and ("is_deleted", 0)
                .on ("root.uuid=m2m." + PublicPropertyContractVotingProtocol.c.UUID_VP.lc ())
            ;

        applySearch (Search.from (p), select);

        db.addJsonArrayCnt (job, select);

    });}

    @Override
    public JsonObject getItem (String id) {
        throw new UnsupportedOperationException ("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
