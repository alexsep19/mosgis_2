package ru.eludia.products.mosgis.db.model.nsi;

import java.io.StringReader;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.datatype.XMLGregorianCalendar;
import ru.eludia.base.DB;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.abs.Roster;
import ru.eludia.base.model.phys.PhysicalCol;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiNsiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiOkeiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiScalarField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiStringField;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import static ru.eludia.products.mosgis.db.model.voc.VocPassportFields.PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class NsiTable extends Table {

    public static final String CHILDREN = "children";
    
    protected static Logger logger = Logger.getLogger (NsiTable.class.getName ());
    
    Roster <NsiField> nsiFields = new Roster <> ();
    
    NsiOkeiRefField okeiField = null;
    NsiStringField labelField = null;
    List<NsiMultipleRefTable> multipleRefTables = Collections.EMPTY_LIST;
    List<NsiMultipleScalarTable> multipleScalarTables = Collections.EMPTY_LIST;
    private static final XMLGregorianCalendar epoch = DB.to.XMLGregorianCalendar (new java.sql.Timestamp (0L));

    public static NsiRef toDom (Map<String, Object> r, String alias) {
        return toDom (
            (String) r.get (alias + ".code"), 
            (UUID)   r.get (alias + ".guid")
        );
    }

    public static NsiRef toDom (String code, UUID uuid) {
        if (code == null) return null;
        NsiRef nsiRef = new NsiRef ();
        nsiRef.setCode (code);
        nsiRef.setGUID (uuid.toString ());
        return nsiRef;
    }    
    
    public NsiElementType toDom (Map<String, Object> r) throws SQLException {

        NsiElementType result = (NsiElementType) DB.to.javaBean (NsiElementType.class, r);
        result.setModified (epoch);
        
        List<NsiElementFieldType> nsiElementField = result.getNsiElementField ();
        
        for (NsiField field: nsiFields.values ()) {
            
            if (field.isMultiple ()) continue;
            
            if (field instanceof NsiNsiRefField) {
                
                NsiNsiRefField nrf = (NsiNsiRefField) field;
                
                NsiElementFieldType f = nrf.toDom (
                    r.get (nrf.getfName ().toLowerCase ()), 
                    r.get ("nsi_" + nrf.getRegistryNumber () + ".code").toString ()
                );
                
                if (f != null) nsiElementField.add (f);
                
            }
            else if (field instanceof NsiScalarField) {
                
                NsiScalarField sf = (NsiScalarField) field;
                
                NsiElementFieldType f = sf.toDom (r.get (sf.getfName ().toLowerCase ()));
                
                if (f != null) nsiElementField.add (f);
                
            }
            
        }
        
        for (NsiMultipleScalarTable mst: getMultipleScalarTables ()) {
            
            NsiScalarField vf = mst.getValueField ();
            
            List<Map<String, Object>> values = (List) r.get (vf.getName ());

            if (values == null || values.isEmpty ()) continue;

            for (Object value: values) {
                
                NsiElementFieldType f = vf.toDom (value);
                
                if (f != null) nsiElementField.add (f);
                
            }
            
        }        
        
        for (NsiMultipleRefTable mrt: getMultipleRefTables ()) {
            
            NsiNsiRefField nrf = mrt.getTargetField ();
            
            List<Map<String, Object>> values = (List) r.get (nrf.getName ());
            
            if (values == null || values.isEmpty ()) continue;
            
            for (Map<String, Object> value: values) {
                                
                NsiElementFieldType f = nrf.toDom (
                    (UUID) value.get ("ref.guid"),
                    value.get ("ref.code").toString ()
                );
                
                if (f != null) nsiElementField.add (f);
                
            }

        }
        
        List<Map<String, Object>> children = (List) r.get (CHILDREN);
        
        if (children != null) {
            
            List<NsiElementType> childElement = result.getChildElement ();
            
            for (Map<String, Object> c: children) childElement.add (toDom (c));
            
        }
        
        return result;
        
    }
       
    public Select getVocSelect () {
        
        final String label = getLabelField ().getfName ();
        
        return model.select (this, "code AS id", label + " AS label")
            .where ("isactual", 1)
            .orderBy (label)
        ;
        
    }
    
    public static final NsiTable getPassportFieldsTable (DB db) throws SQLException {
        
        return getNsiTable (PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER);
        
    }

    public static final NsiTable getNsiTable (int n) {
        return (NsiTable) ModelHolder.getModel ().get(getName (n));
    }

    public NsiTable (DB db, int n) throws SQLException {
        
        this (db.getMap (VocNsiList.class, n));
        
    }        

    public NsiTable (DB db, ResultSet rs) throws SQLException {
        this (db.HASH (rs));
    }

    public Roster<NsiField> getNsiFields () {
        return nsiFields;
    }
    
    public void addParentCol () {
        if (!columns.containsKey ("parent")) col ("parent", Type.UUID, null, "Ссылка на родительскую запись");
    }
    
    public final String getColNameByRemarkPrefix (String prefix) {
        return getColumns ().values ().stream ().filter (f -> f.getRemark ().startsWith (prefix)).findFirst ().get ().getName ();
    }
    
    public Table [] getTables () {
        final int rSize = multipleRefTables.size ();
        final int sSize = multipleScalarTables.size ();
        Table [] result = new Table [1 + rSize + sSize];
        for (int i = 0; i < rSize; i ++) result [i] = multipleRefTables.get (i);
        for (int i = 0; i < sSize; i ++) result [i + rSize] = multipleScalarTables.get (i);
        result [rSize + sSize] = this;       
        return result;
    }
    
    public static final String getName (Object registrynumber) {
        return "vc_nsi_" + registrynumber;
    }

    public NsiTable (Map<String, Object> record) {
        
        super  (getName (record.get ("registrynumber")), record.get ("name").toString ());
        
        pk  ("guid",     Type.UUID, "Глобально-уникальный идентификатор элемента справочника");
        col ("code",     Type.STRING, 20, "Код элемента справочника, уникальный в пределах справочника");
        col ("isactual", Type.BOOLEAN, "Признак актуальности элемента справочника");
        
        if (Bool.TRUE.equals (record.get ("is_nested"))) { this.addParentCol (); }
        
        try (StringReader stringReader = new StringReader (record.get ("cols").toString ())) {
            
            try (JsonReader jsonReader = Json.createReader (stringReader)) {
                
                jsonReader.readArray ().forEach (i -> {

                    final NsiField nsiField = NsiField.fromJson ((JsonObject) i);

                    nsiFields.add (nsiField, nsiField.getRemark ());

                    if (nsiField.isMultiple ()) {
                        
                        if (nsiField instanceof NsiNsiRefField) {
                            
                            NsiNsiRefField targetField = (NsiNsiRefField) nsiField;

                            if (multipleRefTables.isEmpty ()) multipleRefTables = new ArrayList<> ();

                            multipleRefTables.add (new NsiMultipleRefTable (this, targetField));
                            
                        }
                        else if (nsiField instanceof NsiScalarField) {
                            
                            NsiScalarField valueField = (NsiScalarField) nsiField;

                            if (multipleScalarTables.isEmpty ()) multipleScalarTables = new ArrayList<> ();

                            multipleScalarTables.add (new NsiMultipleScalarTable (this, valueField));
                            
                        }
                        else {
                            
                            logger.warning ("Bizarre NSI field definition: " + i);
                            
                        }
                        
                    }
                    else {

                        nsiField.getCols ().forEach (col -> add (col));

                        if (nsiField instanceof NsiOkeiRefField) okeiField = (NsiOkeiRefField) nsiField;
                        
                        trySetLabelField (nsiField);
                                                
                    }

                });

            }

        }        

    }

    private void trySetLabelField (NsiField nsiField) {

        if (labelField != null) return;

        if (nsiField instanceof NsiOkeiRefField) return;
        
        if (!(nsiField instanceof NsiStringField)) return;

        NsiStringField field = (NsiStringField) nsiField;
        
        String rem = field.getRemark ();

        if ("Примечание".equals (rem)) return;
        if (rem.startsWith ("Сокращенное")) return;

        labelField = field;

    }

    public List<NsiMultipleRefTable> getMultipleRefTables () {
        return multipleRefTables;
    }

    public List<NsiMultipleScalarTable> getMultipleScalarTables () {
        return multipleScalarTables;
    }
    
    public void add (Map<String, Object> record, NsiElementFieldType f) {
        final NsiField nsiField = nsiFields.get (f.getName ());        
        if (nsiField != null) nsiField.add (record, f);        
    }

    public NsiOkeiRefField getOkeiField () {
        return okeiField;
    }

    public NsiStringField getLabelField () {
        return labelField;
    }
    
    private static final PhysicalCol uu = new PhysicalCol (JDBCType.VARBINARY, "guid");
    
}