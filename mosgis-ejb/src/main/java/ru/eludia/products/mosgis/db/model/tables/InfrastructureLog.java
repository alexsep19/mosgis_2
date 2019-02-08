package ru.eludia.products.mosgis.db.model.tables;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.GisWsLogTable;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;
import ru.eludia.products.mosgis.db.model.voc.VocOrganizationNsi20;
import ru.gosuslugi.dom.schema.integration.base.OKTMORefType;
import ru.gosuslugi.dom.schema.integration.infrastructure.ImportOKIRequest;
import ru.gosuslugi.dom.schema.integration.infrastructure.InfrastructureType;
import ru.gosuslugi.dom.schema.integration.infrastructure.ManagerOKIType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class InfrastructureLog extends GisWsLogTable {

    public InfrastructureLog () {
        
        super ("tb_infrastructures__log", "История редактирования объектов коммунальной инфраструктуры", Infrastructure.class, EnTable.c.class, Infrastructure.c.class);
        
    }
    
    public static ImportOKIRequest toImportOKIRequest (Map<String, Object> r) {
        final ImportOKIRequest result = new ImportOKIRequest ();
        result.getRKIItem ().add (toRKIItem (r));
        return result;            
    }
    
    private static ImportOKIRequest.RKIItem toRKIItem (Map<String, Object> r) {
        final ImportOKIRequest.RKIItem result = DB.to.javaBean (ImportOKIRequest.RKIItem.class, r);
        result.setTransportGUID(UUID.randomUUID ().toString ());
        result.setOKI(toOKI (r));
        return result;
    }
    
    private static ImportOKIRequest.RKIItem.OKI toOKI (Map<String, Object> r) {
        final ImportOKIRequest.RKIItem.OKI result = DB.to.javaBean (ImportOKIRequest.RKIItem.OKI.class, r);
        result.setBase (NsiTable.toDom (r, "vc_nsi_39"));
        result.setOKIType (toOKIType (r));
        result.setManagerOKI (toManagerOKIType (r));
        if ((long) r.get ("indefinitemanagement") == 1) result.setEndManagmentDate(null);
        else result.setIndefiniteManagement(null);
        result.getServices ().clear();
        for (Map <String, Object> x: (List <Map <String, Object>>) r.get ("services"))
            result.getServices ().add (
                NsiTable.toDom (
                    x.get("code").toString (), 
                    (UUID) x.get("guid")
                )
            );
        
        OKTMORefType oktmo = new OKTMORefType ();
        oktmo.setCode(r.get ("oktmo_code").toString ());
        result.setOKTMO(oktmo);
        
        result.setCommissioningYear(Short.valueOf (r.get ("comissioningyear").toString ()));
        
        if (r.getOrDefault ("independentsource", null) != null && (long) r.get ("independentsource") == 0) result.setIndependentSource(null);
        if (r.getOrDefault ("deterioration", null) == null) result.setDeterioration(BigDecimal.ZERO);
        
        result.setObjectProperty (toObjectProperty (r));
        
        return result;
    }
    
    private static InfrastructureType.OKIType toOKIType(Map<String, Object> r) {
        final ImportOKIRequest.RKIItem.OKI.OKIType result = DB.to.javaBean (ImportOKIRequest.RKIItem.OKI.OKIType.class, r);
        NsiRef nsi_ref_33 = NsiTable.toDom (r, "vc_nsi_33");
        result.setCode (nsi_ref_33.getCode ());
        result.setGUID (nsi_ref_33.getGUID ());
        result.setName (r.get ("vc_nsi_33").toString ());
        if (r.containsKey ("vc_nsi_37")) result.setESubstationType (NsiTable.toDom (r, "vc_nsi_37"));
        if (r.containsKey ("vc_nsi_38")) result.setPowerPlantType  (NsiTable.toDom (r, "vc_nsi_38"));
        if (r.containsKey ("vc_nsi_34")) result.setWaterIntakeType (NsiTable.toDom (r, "vc_nsi_34"));
        if (r.containsKey ("vc_nsi_40")) result.setFuelType        (NsiTable.toDom (r, "vc_nsi_40"));
        if (r.containsKey ("vc_nsi_35")) result.setGasNetworkType  (NsiTable.toDom (r, "vc_nsi_35"));        
        return result;
    }
    
    private static ManagerOKIType toManagerOKIType(Map<String, Object> r) {
        final ManagerOKIType result = new ManagerOKIType ();
        if ("".equals (r.get("is_rso"))) result.setMunicipalities(Boolean.TRUE);
        else {
            ManagerOKIType.RSO rso = new ManagerOKIType.RSO ();
            rso.setRSOOrganizationGUID (r.get ("org.orgppaguid").toString ());
            result.setRSO (rso);
        }
        return result;
    }
    
    private static InfrastructureType.ObjectProperty toObjectProperty(Map<String, Object> r) {
        final InfrastructureType.ObjectProperty result = DB.to.javaBean (InfrastructureType.ObjectProperty.class, r);
        result.getResources ().clear ();
        result.getTransportationResources ().clear ();
        result.getNetPieces ().clear ();
        if ((long) r.get ("is_object") == 1) {
            result.setCountAccidents (null);
            for (Map <String, Object> x: (List <Map <String, Object>>) r.get ("resources")) {
                InfrastructureType.ObjectProperty.Resources resource = DB.to.javaBean (InfrastructureType.ObjectProperty.Resources.class, x);
                resource.setMunicipalResource (NsiTable.toDom (x.get ("code_vc_nsi_2").toString (), (UUID) x.get ("guid_vc_nsi_2")));
                result.getResources ().add (resource);
            }
        }
        else {
            for (Map <String, Object> x: (List <Map <String, Object>>) r.get ("transportation_resources")) {
                InfrastructureType.ObjectProperty.TransportationResources resource = DB.to.javaBean (InfrastructureType.ObjectProperty.TransportationResources.class, x);
                resource.setMunicipalResource (NsiTable.toDom (x.get ("code_vc_nsi_2").toString (), (UUID) x.get ("guid_vc_nsi_2")));
                resource.setCoolantType       (NsiTable.toDom (x.get ("code_vc_nsi_41").toString (), (UUID) x.get ("guid_vc_nsi_41")));
                result.getTransportationResources ().add (resource);
            }
            for (Map <String, Object> x: (List <Map <String, Object>>) r.get ("net_pieces")) {
                InfrastructureType.ObjectProperty.NetPieces netPiece = DB.to.javaBean (InfrastructureType.ObjectProperty.NetPieces.class, x);
                netPiece.setPressureType (NsiTable.toDom (x.get ("code_vc_nsi_36").toString (), (UUID) x.get ("guid_vc_nsi_36")));
                netPiece.setVoltageType  (NsiTable.toDom (x.get ("code_vc_nsi_45").toString (), (UUID) x.get ("guid_vc_nsi_45")));
                result.getNetPieces ().add (netPiece);
            }
        }
        return result;
    }
    
    public static void addServicesForImport (DB db, Map<String, Object> r) throws SQLException {
        
        r.put ("services", db.getList (db.getModel ()
                .select  (InfrastructureNsi3.class, "AS ref", "code")
                .toOne   (NsiTable.getNsiTable (3), "AS nsi3", "guid AS guid").on ("(ref.code = nsi3.code)")
                .where   ("uuid", r.get("uuid_object"))
        ));
        
    }
    
    public static void addPropertiesForImport (DB db, Map<String, Object> r) throws SQLException {
        
        r.put ("resources", db.getList (db.getModel ()
                .select  (InfrastructureResource.class, "AS res", "*")
                .toOne   (NsiTable.getNsiTable (2), "AS nsi2", "guid AS guid_vc_nsi_2").on ("(res.code_vc_nsi_2 = nsi2.code)")
                .where   ("uuid_oki", r.get("uuid_object"))
                .and     ("is_deleted", 0)
                .orderBy (InfrastructureResource.c.CODE_VC_NSI_2.lc ())
        ));
        
        r.put ("transportation_resources", db.getList (db.getModel ()
                .select     (InfrastructureTransportationResource.class, "AS res", "*")
                .toOne      (NsiTable.getNsiTable (2), "AS nsi2", "guid AS guid_vc_nsi_2").on ("(res.code_vc_nsi_2 = nsi2.code)")
                .toMaybeOne (NsiTable.getNsiTable (41), "AS nsi41", "guid AS guid_vc_nsi_41").on ("(res.code_vc_nsi_41 = nsi41.code)")
                .where      ("uuid_oki", r.get("uuid_object"))
                .and        ("is_deleted", 0)
                .orderBy    (InfrastructureTransportationResource.c.CODE_VC_NSI_2.lc ())
        ));
        
        r.put ("net_pieces", db.getList (db.getModel ()
                .select  (InfrastructureNetPiece.class, "AS res", "*")
                .toMaybeOne   (NsiTable.getNsiTable (36), "AS nsi36", "guid AS guid_vc_nsi_36").on ("(res.code_vc_nsi_36 = nsi36.code)")
                .toMaybeOne   (NsiTable.getNsiTable (45), "AS nsi45", "guid AS guid_vc_nsi_45").on ("(res.code_vc_nsi_45 = nsi45.code)")
                .where   ("uuid_oki", r.get("uuid_object"))
                .and     ("is_deleted", 0)
                .orderBy (InfrastructureNetPiece.c.LENGTH)
        ));
        
    }
    
    public Get getForExport (String id) {
        
        final VocNsi33 nsi_33 = (VocNsi33) getModel ().get (VocNsi33.class);
        final NsiTable nsi_34 = NsiTable.getNsiTable (34);
        final NsiTable nsi_35 = NsiTable.getNsiTable (35);
        final NsiTable nsi_37 = NsiTable.getNsiTable (37);
        final VocNsi38 nsi_38 = (VocNsi38) getModel ().get (VocNsi38.class);
        final NsiTable nsi_39 = NsiTable.getNsiTable (39);
        final VocNsi40 nsi_40 = (VocNsi40) getModel ().get (VocNsi40.class);
        
        return (Get) getModel ()
            .get        (this, id, "*")
            .toOne      (Infrastructure.class, "AS r", Infrastructure.c.ID_IS_STATUS.lc ()).on ()
            .toOne      (VocOrganization.class, "AS org", "orgppaguid").on ()
            .toOne      (nsi_33, "label AS vc_nsi_33", "code", "guid", "is_object AS is_object").on ("(r.code_vc_nsi_33 = vc_nsi_33.code AND vc_nsi_33.isactual=1)")
            .toOne      (nsi_39, nsi_39.getLabelField ().getfName () + " AS vc_nsi_39", "code", "guid").on ("(r.code_vc_nsi_39 = vc_nsi_39.code AND vc_nsi_39.isactual=1)")
            .toMaybeOne (VocOrganizationNsi20.class, "AS org_perms", "code AS is_rso").on ("r.manageroki=org_perms.uuid AND org_perms.code=2 AND org_perms.is_deleted=0")
            .toMaybeOne (nsi_37, nsi_37.getLabelField ().getfName () + " AS vc_nsi_37", "code", "guid").on ("(r.code_vc_nsi_37 = vc_nsi_37.code AND vc_nsi_37.isactual=1)")
            .toMaybeOne (nsi_38, "label AS vc_nsi_38", "code", "guid").on ("(r.code_vc_nsi_38 = vc_nsi_38.code AND vc_nsi_38.isactual=1)")
            .toMaybeOne (nsi_34, nsi_34.getLabelField ().getfName () + " AS vc_nsi_34", "code", "guid").on ("(r.code_vc_nsi_34 = vc_nsi_34.code AND vc_nsi_34.isactual=1)")
            .toMaybeOne (nsi_40, "label AS vc_nsi_40", "code", "guid").on ("(r.code_vc_nsi_40 = vc_nsi_40.code AND vc_nsi_40.isactual=1)")
            .toMaybeOne (nsi_35, nsi_35.getLabelField ().getfName () + " AS vc_nsi_35", "code", "guid").on ("(r.code_vc_nsi_35 = vc_nsi_35.code AND vc_nsi_35.isactual=1)");
    }
    
}
