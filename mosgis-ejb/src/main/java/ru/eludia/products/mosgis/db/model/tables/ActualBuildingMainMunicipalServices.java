package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;

public class ActualBuildingMainMunicipalServices extends View {
    
    public static final String TABLE_NAME = "vw_bld_municipal_svc";

    public enum c implements ColEnum {
        
        ID                   (Type.UUID,     "id"),
	FIASHOUSEGUID        (VocBuilding.class,   null,   "Глобальный уникальный идентификатор дома по ФИАС"),
        UUID_M_M_SERVICE     (MainMunicipalService.class, null, "Коммунальная услуга"),
        STARTDATE            (Type.STRING,   "Начало"),
        ENDDATE              (Type.STRING,   "Окончание"),
        ;        

        @Override
        public Col getCol () {return col;}
        private Col col;
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}

    }
    

    public ActualBuildingMainMunicipalServices () {        
        
        super  (TABLE_NAME, "Коммунальные услуги, оказываемые по адресам в рамках действующих договоров управления");

        cols   (c.class);
        pk     (c.ID);        

    }

    @Override
    public final String getSQL () {

        return 
            "SELECT * FROM " + ActualContractMainMunicipalServices.TABLE_NAME
                + " UNION " +
            "SELECT * FROM " + ActualCharterMainMunicipalServices.TABLE_NAME
        ;

    }

}