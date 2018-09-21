package ru.eludia.products.mosgis.db.model.voc;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.datatype.XMLGregorianCalendar;
import ru.eludia.base.DB;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.products.mosgis.db.model.tables.OutSoap;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiItemInfoType;

public class VocNsiList extends Table {
    
    private static final XMLGregorianCalendar epoch = DB.to.XMLGregorianCalendar (new java.sql.Timestamp (0L));
    
    public VocNsiList () {
        
        super ("vc_nsi_list", "Перечень справочников НСИ ГИС ЖКХ");

        pk    ("registrynumber", Type.INTEGER,          "Реестровый номер справочника");        
        col   ("name",           Type.STRING,           "Наименование справочника");
        fk    ("listgroup",      VocNsiListGroup.class, "Группа");
        col   ("cols",           Type.TEXT, null,       "Список столбцов в виде JSON");
        col   ("ts_last_import", Type.DATETIME, null,   "Дата последнего импорта");
        fk    ("uuid_out_soap",  OutSoap.class, null,   "Последний запрос на импорт из ГИС ЖКХ");

        key   ("name", "name");
        
    }
    
    public static final NsiItemInfoType toDom (DB db, ResultSet rs) throws SQLException {
        
        final NsiItemInfoType i = new NsiItemInfoType ();
        
        i.setName           (rs.getString ("name"));
        i.setRegistryNumber (BigInteger.valueOf (rs.getLong ("registrynumber")));
        i.setModified       (epoch);
        
        return i;
        
    }    
        
}