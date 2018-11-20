package ru.eludia.products.mosgis.jms.base;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.logging.Level;
import javax.json.JsonObject;
import ru.eludia.base.DB;
import ru.eludia.base.model.Table;
import ru.eludia.base.db.sql.gen.Get;
import ru.eludia.base.db.util.TypeConverter;
import ru.eludia.products.mosgis.ejb.ModelHolder;

public abstract class JsonMDB<T extends Table> extends TextMDB {

    protected abstract void handleRecord(DB db, final JsonObject params, Map<String, Object> r) throws SQLException;

    private Class<Table> getTableClass() {
        return (Class<Table>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected final Table getTable() {
        return ModelHolder.getModel().get(getTableClass());
    }

    protected Get get(UUID uuid) {
        return (Get) ModelHolder.getModel().get(getTable(), uuid, "*");
    }

    /**
     * Метод принимает на вход сериализованный JSON, в котором обязательно
     * должны быть параметры:
     * <ul><li><b>uuid</b> - идентификатор сущности
     * <li><b>orgPPAGuid</b> - идентификатор поставщика информации</ul>
     *
     * @param message сериализованный JSON
     * @throws SQLException
     * @throws JMSException
     */
    @Override
    protected final void onTextMessage(TextMessage message) throws SQLException, JMSException {

        String txt = message.getText();
        JsonObject json = TypeConverter.JsonObject(txt);

        UUID uuid = null;

        UUID orgPPAGUID = null;

        try {
            uuid = UUID.fromString(json.getString("uuid"));
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Incorrect UUID: '" + json.getString("uuid") + "'", ex);
        }
        
        try {
            orgPPAGUID = UUID.fromString(json.getString("orgPPAGuid"));
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Incorrect orgPPAGUID: '" + json.getString("orgPPAGuid") + "'", ex);
        }

        if (uuid == null || orgPPAGUID == null) {
            return;
        }

        try (DB db = ModelHolder.getModel().getDb()) {

            Map<String, Object> r = null;

            try {
                r = db.getMap(get(uuid));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Cannot fetch '" + uuid + "'", e);
                return;
            }

            if (r == null) {
                logger.log(Level.SEVERE, "Record not found: '" + uuid + "'");
                return;
            }

            try {
                logger.log(Level.INFO, r.toString());
                handleRecord(db, json, r);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Cannot handle '" + uuid + "'", e);
            }

        }

    }

}
