package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;

public class Premise extends View {

    public Premise () {
        
        super  ("vw_premises", "Помещения и блоки");
        
        pk     ("uuid",               Type.UUID,           null,       "Ключ");
        ref    ("uuid_house",         House.class,                     "Дом");
        col    ("premisesnum",        Type.STRING,         null,       "Номер помещения");
        col    ("cadastralnumber",    Type.STRING,         null,       "Кадастровый номер");
        col    ("totalarea",          Type.NUMERIC, 25, 4, null,       "Общая площадь жилого помещения");               
        
    }

    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid, uuid_house, premisesnum, cadastralnumber, totalarea, 'ResidentialPremise' class FROM " + getName (ResidentialPremise.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT uuid, uuid_house, premisesnum, cadastralnumber, totalarea, 'NonResidentialPremise' class FROM " + getName (NonResidentialPremise.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT uuid, uuid_house, 'блок ' || blocknum premisesnum, cadastralnumber, totalarea, 'Block' class FROM " + getName (Block.class) + " WHERE is_deleted = 0 AND is_annuled = 0"
            + " UNION "
            + "SELECT r.uuid, r.uuid_house, "
                + "CASE "
                + " WHEN b.blocknum IS NOT NULL THEN 'блок ' || b.blocknum "
                + " ELSE 'кв. ' || p.premisesnum "
                + "END || ' к. ' || roomnumber premisesnum"
                + ", r.cadastralnumber, r.square totalarea, 'LivingRoom' class "
                + "FROM " + getName (LivingRoom.class) + " r "
                + " LEFT JOIN " + getName (ResidentialPremise.class) + " p ON r.uuid_premise = p.uuid "
                + " LEFT JOIN " + getName (Block.class)              + " b ON r.uuid_block = b.uuid "
                + " WHERE r.is_deleted = 0 AND r.is_annuled = 0"
        ;

    }

}