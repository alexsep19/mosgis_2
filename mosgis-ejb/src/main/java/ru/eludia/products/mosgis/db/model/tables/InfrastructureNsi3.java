package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.STRING;
import ru.eludia.products.mosgis.db.model.EnColEnum;

public class InfrastructureNsi3 extends Table {

    public InfrastructureNsi3 () {
        
        super ("tb_infrastructures_nsi_3", "Виды коммунальных услуг коммунальных инфраструктур");
        
        pkref ("uuid",       Infrastructure.class,  "Объект коммунальной инфраструктуры");
        pk    ("code",       Type.STRING,  20,      "Ссылка на НСИ \"Вид коммунальной услуги\" (НСИ 3)");

    }

}