package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import static ru.eludia.base.model.Type.NUMERIC;
import static ru.eludia.base.model.Type.STRING;

public class Premise extends View {
    
    public enum c implements ColEnum {
        
        ID                        (Type.UUID,     null,           "Ключ"),        
        UUID_HOUSE                (House.class,                   "Дом"),
        LABEL                     (STRING,        null,           "Номер помещения"),
        CADASTRALNUMBER           (STRING,        null,           "Кадастровый номер"),
        TOTALAREA                 (NUMERIC,       25, 4, null,    "Общая площадь жилого помещения"),
        CLASS                     (STRING,        null,           "Класс")
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

    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid id, uuid_house, premisesnum label, cadastralnumber, totalarea, 'ResidentialPremise' class FROM " + getName (ResidentialPremise.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT uuid id, uuid_house, premisesnum label, cadastralnumber, totalarea, 'NonResidentialPremise' class FROM " + getName (NonResidentialPremise.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT uuid id, uuid_house, 'блок ' || blocknum label, cadastralnumber, totalarea, 'Block' class FROM " + getName (Block.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT r.uuid id, r.uuid_house, "
                + "CASE "
                + " WHEN b.blocknum IS NOT NULL THEN 'блок ' || b.blocknum "
                + " ELSE 'кв. ' || p.premisesnum "
                + "END || ' к. ' || roomnumber label"
                + ", r.cadastralnumber, r.square totalarea, 'LivingRoom' class "
                + "FROM " + getName (LivingRoom.class) + " r "
                + " LEFT JOIN " + getName (ResidentialPremise.class) + " p ON r.uuid_premise = p.uuid "
                + " LEFT JOIN " + getName (Block.class)              + " b ON r.uuid_block = b.uuid "
                + " WHERE r.is_deleted = 0 AND r.is_annuled = 0"
        ;

    }

}