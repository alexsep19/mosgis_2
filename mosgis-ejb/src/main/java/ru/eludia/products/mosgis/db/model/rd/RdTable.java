package ru.eludia.products.mosgis.db.model.rd;

import java.sql.SQLException;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.abs.Roster;
import ru.eludia.base.db.sql.build.QP;
import ru.eludia.products.mosgis.db.model.rd.fields.RdField;

public class RdTable extends Table {
    
    int id;
    
    protected Logger logger = Logger.getLogger (getClass ().getName ());
    
    Roster <RdField> rdFields = new Roster <> ();
    
    public RdTable (DB db, int id) throws SQLException {

        super ("vc_rd_" + id, db.getString (new QP ("SELECT name FROM vc_rd_cols WHERE link_dictionary = ?", id)));

        this.id = id;

        pk  ("id",     Type.INTEGER,    "Идентификатор элемента справочника");
        col ("name",   Type.STRING,     "Наименование элемента справочника");
        col ("nsi_code", Type.STRING, null, "Код соответствующего элемента справочника ГИС ЖКХ");

        db.adjustTable (this);
        
    }

//    NsiStringField labelField = null;
/*    
    public RdTable (DB db, int n) throws SQLException {
        this (db.getMap (VocNsiList.class, n));
    }        

    public RdTable (DB db, ResultSet rs) throws SQLException {
        this (db.HASH (rs));
    }

    public Roster<NsiField> getNsiFields () {
        return rdFields;
    }
    
    public void addParentCol () {
        col ("parent", Type.UUID, null, "Ссылка на родительскую запись");
    }
    
    public Table [] getTables () {
        final int size = multipleRefTables.size ();
        Table [] result = new Table [1 + size];
        for (int i = 0; i < size; i ++) result [i] = multipleRefTables.get (i);
        result [size] = this;       
        return result;
    }

    public RdTable (Map<String, Object> record) {
        
        super  ("vc_nsi_" + record.get ("registrynumber"), record.get ("name").toString ());
        
        pk  ("guid",     Type.UUID, "Глобально-уникальный идентификатор элемента справочника");
        col ("code",     Type.STRING, 20, "Код элемента справочника, уникальный в пределах справочника");
        col ("isactual", Type.BOOLEAN, "Признак актуальности элемента справочника");
        
        try (StringReader stringReader = new StringReader (record.get ("cols").toString ())) {
            
            try (JsonReader jsonReader = Json.createReader (stringReader)) {
                
                jsonReader.readArray ().forEach (i -> {

                    final NsiField nsiField = NsiField.fromJson ((JsonObject) i);

                    rdFields.add (nsiField, nsiField.getRemark ());

                    if (nsiField.isMultiple ()) {
                        
                        if (!(nsiField instanceof NsiNsiRefField)) return;
                        
                        NsiNsiRefField targetField = (NsiNsiRefField) nsiField;
                        
                        if (multipleRefTables.isEmpty ()) multipleRefTables = new ArrayList<> ();
                        
                        multipleRefTables.add (new NsiMultipleRefTable (this, targetField));
                        
                    }
                    else {

                        nsiField.getCols ().forEach (col -> add (col));

                        if (nsiField instanceof NsiOkeiRefField) okeiField = (NsiOkeiRefField) nsiField;
                        
                        if (nsiField instanceof NsiStringField && labelField == null) labelField = (NsiStringField) nsiField;
                        
                    }

                });

            }

        }        

    }

    public List<NsiMultipleRefTable> getMultipleRefTables () {
        return multipleRefTables;
    }
    
    public void add (Map<String, Object> record, NsiElementFieldType f) {
        final NsiField nsiField = rdFields.get (f.getName ());        
        if (nsiField != null) nsiField.add (record, f);        
    }

    public NsiOkeiRefField getOkeiField () {
        return okeiField;
    }

    public NsiStringField getLabelField () {
        return labelField;
    }
    
    private static final PhysicalCol uu = new PhysicalCol (JDBCType.VARBINARY, "guid");
*/    

    public int getId () {
        return id;
    }

}