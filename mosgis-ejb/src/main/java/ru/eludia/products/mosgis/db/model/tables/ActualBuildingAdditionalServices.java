package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;

public class ActualBuildingAdditionalServices extends View {
    
    public static final String TABLE_NAME = "vw_bld_add_svc";

    public enum c implements ColEnum {
        
        ID                   (Type.UUID,     "id"),
	FIASHOUSEGUID        (VocBuilding.class,   null,   "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_ADD_SERVICE      (AdditionalService.class, null,    "Дополнительная услуга"),
        STARTDATE            (Type.STRING,   "Начало"),
        ENDDATE              (Type.STRING,   "Окончание"),
        UUID_ORG             (VocOrganization.class, null, "Организация - исполнитель"),
        ;        

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }
    

    public ActualBuildingAdditionalServices () {        
        
        super  (TABLE_NAME, "Коммунальные услуги, оказываемые по адресам в рамках действующих договоров управления");

        cols   (c.class);
        pk     (c.ID);        

    }

    @Override
    public final String getSQL () {
        
        QP qp = new QP ("SELECT ");
        for (c c: c.values ()) {
            qp.append (c.name ());
            qp.append (',');
        }
        qp.setLastChar (' ');
        qp.append ("FROM ");

        String select = qp.toString ();

        return 
            select + ActualContractAdditionalServices.TABLE_NAME
                + " UNION " +
            select + ActualCharterAdditionalServices.TABLE_NAME
        ;

    }

}