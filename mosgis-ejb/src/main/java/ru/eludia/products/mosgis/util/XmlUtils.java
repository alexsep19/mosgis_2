package ru.eludia.products.mosgis.util;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.eludia.base.DB;
import ru.eludia.base.Model;
import ru.eludia.base.model.def.Bool;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;
import ru.eludia.products.mosgis.ejb.ModelHolder;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class XmlUtils {
   
    private static final Logger logger = java.util.logging.Logger.getLogger(XmlUtils.class.getName());
    
    public static String printRubKop (java.math.BigDecimal v) {
        String s = v.toString ();
        if (v.scale () == 1) return s + '0';
        return s;
    }    
      
    public static final NsiRef createNsiRef(int regisryNumber, String code) throws SQLException {
        Map<String, Object> nsiItem = getNsiItem(regisryNumber, code);
        if (nsiItem == null)
            return null;
        return NsiTable.toDom(code, (UUID) nsiItem.get("guid"));
    }
    
    public static final ru.mos.gkh.gis.schema.integration.nsi_base.NsiRef createWsNsiRef(int regisryNumber, String code) throws SQLException {
        Map<String, Object> nsiItem = getNsiItem(regisryNumber, code);
        if (nsiItem == null)
            return null;
        
        ru.mos.gkh.gis.schema.integration.nsi_base.NsiRef nsiRef = new ru.mos.gkh.gis.schema.integration.nsi_base.NsiRef ();
        nsiRef.setCode (code);
        nsiRef.setGUID (nsiItem.get("guid").toString());
        return nsiRef;
    }
    
    private static Map<String, Object> getNsiItem(int regisryNumber, String code) throws SQLException {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        Model model = ModelHolder.getModel();

        NsiTable nsiTable = NsiTable.getNsiTable(regisryNumber);
        if (nsiTable == null) {
            logger.log(Level.SEVERE, "Cannot find nsi by registryNumber '" + regisryNumber);
            return null;
        }
        
        try (DB db = ModelHolder.getModel().getDb()) {

            Map<String, Object> nsiItem = null;
            try {
                nsiItem = db.getMap(model.select(nsiTable, "guid")
                        .where("code", code)
                        .and("is_actual", Bool.TRUE));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Cannot fetch nsi '" + regisryNumber + "' by code '" + code + "'", e);
                return null;
            }

            if (nsiItem == null) {
                logger.log(Level.SEVERE, "Cannot find nsi '" + regisryNumber + "' by code '" + code + "'");
                return null;
            }

            return nsiItem;
        } 
    }
}
