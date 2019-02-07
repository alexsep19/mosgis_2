package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;

public class InfrastructureNsi3 extends Table {

    public InfrastructureNsi3 () {
        
        super ("tb_infrastructures_nsi_3", "Виды коммунальных услуг коммунальных инфраструктур");
        
        pkref ("uuid",       Infrastructure.class,  "Объект коммунальной инфраструктуры");
        pk    ("code",       Type.STRING,  20,      "Ссылка на НСИ \"Вид коммунальной услуги\" (НСИ 3)");
        
        trigger ("BEFORE INSERT OR UPDATE OR DELETE", ""
                + "DECLARE "
                    + "cnt NUMBER; "
                + "BEGIN "
                    + "SELECT COUNT(*) INTO cnt FROM tb_infrastructures infrastructure INNER JOIN vc_nsi_33 nsi33 ON (nsi33.code = infrastructure.code_vc_nsi_33 AND nsi33.is_object = 1); "
                    + "IF cnt > 0 THEN BEGIN "
                        + "SELECT COUNT(*) INTO cnt FROM tb_oki_resources res WHERE res.uuid_oki = :OLD.uuid; "
                        + "IF cnt > 0 THEN "
                            + "raise_application_error (-20000, 'К данному объекту привязаны характеристики мощностей объекта. Операция отменена'); "
                        + "END IF; END; "
                    + "ELSE BEGIN "
                        + "SELECT COUNT(*) INTO cnt FROM tb_oki_tr_resources res WHERE res.uuid_oki = :OLD.uuid; "
                        + "IF cnt > 0 THEN "
                            + "raise_application_error (-20000, 'К данному объекту привязаны характеристики передачи коммунальных ресурсов. Операция отменена'); "
                        + "END IF; "
                        + "SELECT COUNT(*) INTO cnt FROM tb_oki_net_pieces net WHERE net.uuid_oki = :OLD.uuid; "
                        + "IF cnt > 0 THEN "
                            + "raise_application_error (-20000, 'К данному объекту привязаны участки сети. Операция отменена'); "
                        + "END IF; "
                    + "END; END IF; "
                + "END; "
        );

    }

}