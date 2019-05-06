package ru.eludia.products.mosgis.db.model.tables;

import java.util.Map;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.ModelHolder;
import ru.eludia.products.mosgis.db.model.MosGisModel;
import ru.eludia.products.mosgis.db.model.voc.VocPerson;

public class Premise extends View {
    
    public enum c implements ColEnum {
        
        ID                        (Type.UUID,     null,           "Ключ"),        
        UUID_HOUSE                (House.class,                   "Дом"),
        LABEL                     (Type.STRING,   null,           "Номер помещения"),
        CADASTRALNUMBER           (Type.STRING,   null,           "Кадастровый номер"),
	APARTMENTNUMBER           (Type.STRING,   null,           "Номер помещения / Номер блока"),
	ROOMNUMBER                (Type.STRING,   null,           "Номер комнаты"),
        TOTALAREA                 (Type.NUMERIC,  25, 4, null,    "Общая площадь жилого помещения"),
        PREMISESGUID              (Type.UUID,     null,           "Идентификатор помещения"),       
        LIVINGROOMGUID            (Type.UUID,     null,           "Идентификатор комнаты"),
        CODE_VC_NSI_30            (Type.STRING,   null,           "Характеристика помещения"),
        CLASS                     (Type.STRING,   null,           "Класс")
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Premise () {        
        super  ("vw_premises", "Помещения и блоки");
        cols   (c.class);
        pk     (c.ID);
    }

    public static String getUuidPremiseByApartmentNumber (Map<String, Object> r, Object fiashouseguid) {

	return null;

//	final MosGisModel m = ModelHolder.getModel ();
//
//        try (DB db = m.getDb ()) {
//
//            return db.upsertId (ResidentialPremise.class, r
//		, "uuid_house"
//		, "premisenum"
//	    );
//
//        }
    }    
    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid id, uuid_house, premisesnum label, premisesnum apartmentnumber, NULL roomnumber, cadastralnumber, totalarea, premisesguid, NULL livingroomguid, code_vc_nsi_30, 'ResidentialPremise' class FROM " + getName (ResidentialPremise.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT uuid id, uuid_house, premisesnum label,  premisesnum apartmentnumber, NULL roomnumber, cadastralnumber, totalarea, premisesguid, NULL livingroomguid, NULL code_vc_nsi_30, 'NonResidentialPremise' class FROM " + getName (NonResidentialPremise.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT uuid id, uuid_house, 'блок ' || blocknum label,  blocknum apartmentnumber, NULL roomnumber, cadastralnumber, totalarea, blockguid premisesguid, NULL livingroomguid, NULL code_vc_nsi_30, 'Block' class FROM " + getName (Block.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT r.uuid id, r.uuid_house, "
                + "CASE "
                + " WHEN b.blocknum IS NOT NULL THEN 'блок ' || b.blocknum "
                + " ELSE 'кв. ' || p.premisesnum "
                + "END || ' к. ' || roomnumber label"
		+ ", CASE "
		+ " WHEN b.blocknum IS NOT NULL THEN b.blocknum "
		+ " ELSE p.premisesnum "
		+ "END apartmentnumber "
		+ ", r.roomnumber "
                + ", r.cadastralnumber, r.square totalarea, NULL premisesguid, livingroomguid, NULL code_vc_nsi_30, 'LivingRoom' class "
                + "FROM " + getName (LivingRoom.class) + " r "
                + " LEFT JOIN " + getName (ResidentialPremise.class) + " p ON r.uuid_premise = p.uuid "
                + " LEFT JOIN " + getName (Block.class)              + " b ON r.uuid_block = b.uuid "
                + " WHERE r.is_deleted = 0 AND r.is_annuled = 0"
        ;

    }

}