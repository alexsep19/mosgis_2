package ru.eludia.products.mosgis.jms.ws.sr_ctr;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.tables.SupplyResourceContract;
import ru.eludia.products.mosgis.db.model.voc.VocGisSupplyResourceContractCustomerType;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;
import ru.gosuslugi.dom.schema.integration.house_management.DRSOIndType;
import ru.gosuslugi.dom.schema.integration.house_management.DRSORegOrgType;
import ru.gosuslugi.dom.schema.integration.house_management.SupplyResourceContractType;

public class CustomerSetter {

    private static Object setCustomerOrg (final Map<String, Object> r, DRSORegOrgType regOrg) {
        final String orgRootEntityGUID = regOrg.getOrgRootEntityGUID ();
        if (orgRootEntityGUID == null) throw new IllegalArgumentException ("Не указан orgRootEntityGUID для DRSORegOrgType");
        return r.put (SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc (), orgRootEntityGUID);
    }    
    
    private static Object setCustomerInd (Map<String, Object> r, DRSOIndType ind) throws Exception {
        
        Map<String, Object> person = DB.to.Map (ind);
        
        person.put (EnTable.c.IS_DELETED.lc (), 0);
        person.put (VocPerson.c.UUID_ORG.lc (), r.get (SupplyResourceContract.c.UUID_ORG.lc ()));
        person.put (VocPerson.c.BIRTHDATE.lc (), ind.getDateOfBirth ());
        person.put (VocPerson.c.IS_FEMALE.lc (), "F".equals (ind.getSex ()));
            
        String snils = ind.getSNILS ();
        
        return r.put (SupplyResourceContract.c.UUID_PERSON_CUSTOMER.lc (), snils == null ? 
            VocPerson.getCustomerUuidByLegalId  (person, ind.getID ()) : 
            VocPerson.getCustomerUuidBySnils    (person, snils)
        );
        
    }
    
    private static Object setCustomer (final Map<String, Object> r, DRSORegOrgType regOrg, DRSOIndType ind) throws Exception {        
        if (regOrg != null) return setCustomerOrg (r, regOrg);
        if (ind    != null) return setCustomerInd (r, ind);
        throw new IllegalArgumentException ("No regOrg nor ind provided");
    }
    
    public static void setCustomer (final Map<String, Object> r, SupplyResourceContractType src) throws Exception {
        
        if (DB.ok (src.isOffer ())) {
            r.put (SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.OFFER.getId ());
            return;
        }
        
        final SupplyResourceContractType.ApartmentBuildingOwner abo = src.getApartmentBuildingOwner ();        
        if (abo != null) {
            r.put (SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.OWNER.getId ());
            if (!DB.ok (abo.isNoData ())) setCustomer (r, abo.getRegOrg (), abo.getInd ());
            return;
        }

        final SupplyResourceContractType.ApartmentBuildingRepresentativeOwner abro = src.getApartmentBuildingRepresentativeOwner ();
        if (abro != null) {
            r.put (SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.REPRESENTATIVEOWNER.getId ());
            if (!DB.ok (abro.isNoData ())) setCustomer (r, abro.getRegOrg (), abro.getInd ());
        }

        final SupplyResourceContractType.ApartmentBuildingSoleOwner abso = src.getApartmentBuildingSoleOwner ();        
        if (abso != null) {
            r.put (SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.SOLEOWNER.getId ());            
            if (!DB.ok (abso.isNoData ())) setCustomer (r, abso.getRegOrg (), abso.getInd ());
        }
        
        final SupplyResourceContractType.LivingHouseOwner lho = src.getLivingHouseOwner ();        
        if (lho != null) {
            r.put (SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.LIVINGHOUSEOWNER.getId ());
            if (!DB.ok (lho.isNoData ())) setCustomer (r, lho.getRegOrg (), lho.getInd ());
        }
                
        if (DB.ok (src.getOrganization ())) {
            r.put (SupplyResourceContract.c.ID_CUSTOMER_TYPE.lc (), VocGisSupplyResourceContractCustomerType.i.ORGANIZATION.getId ());
            r.put (SupplyResourceContract.c.UUID_ORG_CUSTOMER.lc (), src.getOrganization ().getOrgRootEntityGUID ());
        }
        
    }    
    
}
