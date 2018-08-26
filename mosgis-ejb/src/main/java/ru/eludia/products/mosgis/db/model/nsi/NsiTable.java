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
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.abs.Roster;
import ru.eludia.base.model.phys.PhysicalCol;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiNsiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiOkeiRefField;
import ru.eludia.products.mosgis.db.model.nsi.fields.NsiStringField;
import ru.eludia.products.mosgis.db.model.voc.VocNsiList;
import static ru.eludia.products.mosgis.db.model.voc.VocPassportFields.PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiElementFieldType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class NsiTable extends Table {
    
    protected Logger logger = Logger.getLogger (getClass ().getName ());
    
    Roster <NsiField> nsiFields = new Roster <> ();
    
    NsiOkeiRefField okeiField = null;
    NsiStringField labelField = null;
    List<NsiMultipleRefTable> multipleRefTables = Collections.EMPTY_LIST;
    
    public static NsiRef toDom (String code, UUID uuid) {
        NsiRef nsiRef = new NsiRef ();
        nsiRef.setCode (code);
        nsiRef.setGUID (uuid.toString ());
        return nsiRef;
    }    
    
    public Select getVocSelect () {
        
        final String label = getLabelField ().getfName ();
        
        return model.select (this, "code AS id", label + " AS label")
            .where ("isactual", 1)
            .orderBy (label)
        ;
        
    }
    
    public static final NsiTable getPassportFieldsTable (DB db) throws SQLException {
        
        return getNsiTable (db, PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER);
        
    }

    public static final NsiTable getNsiTable (DB db, int n) throws SQLException {
        
        NsiTable t = new NsiTable (db, n);
        
        db.adjustTable (t);
        
        return t;
        
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
        col ("parent", Type.UUID, null, "Ссылка на родительскую запись");
    }
    
    public final String getColNameByRemarkPrefix (String prefix) {
        return getColumns ().values ().stream ().filter (f -> f.getRemark ().startsWith (prefix)).findFirst ().get ().getName ();
    }
    
    public Table [] getTables () {
        final int size = multipleRefTables.size ();
        Table [] result = new Table [1 + size];
        for (int i = 0; i < size; i ++) result [i] = multipleRefTables.get (i);
        result [size] = this;       
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
        
        try (StringReader stringReader = new StringReader (record.get ("cols").toString ())) {
            
            try (JsonReader jsonReader = Json.createReader (stringReader)) {
                
                jsonReader.readArray ().forEach (i -> {

                    final NsiField nsiField = NsiField.fromJson ((JsonObject) i);

                    nsiFields.add (nsiField, nsiField.getRemark ());

                    if (nsiField.isMultiple ()) {
                        
                        if (!(nsiField instanceof NsiNsiRefField)) return;
                        
                        NsiNsiRefField targetField = (NsiNsiRefField) nsiField;
                        
                        if (multipleRefTables.isEmpty ()) multipleRefTables = new ArrayList<> ();
                        
                        multipleRefTables.add (new NsiMultipleRefTable (this, targetField));
                        
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