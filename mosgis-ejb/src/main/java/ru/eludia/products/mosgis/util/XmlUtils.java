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
import ru.gosuslugi.dom.schema.integration.base.OKTMORefType;
import ru.gosuslugi.dom.schema.integration.nsi_base.NsiRef;

public class XmlUtils {
   
    private static final Logger logger = java.util.logging.Logger.getLogger(XmlUtils.class.getName());
    
    public static final NsiRef createNsiRef(int regisryNumber, String code) throws SQLException {
        if (StringUtils.isBlank(code)) {
            return null;
        }

        Model model = ModelHolder.getModel();

        NsiTable nsiTable = NsiTable.getNsiTable(regisryNumber);

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

            return NsiTable.toDom(code, (UUID) nsiItem.get("guid"));
        }
    }
    
    public static final OKTMORefType createOKTMORef(Long oktmo) throws SQLException {
        if (oktmo == null) {
            return null;
        }
        
        OKTMORefType oktmoRef = new OKTMORefType();
        oktmoRef.setCode(oktmo.toString());
        return oktmoRef;
        
    }
}
