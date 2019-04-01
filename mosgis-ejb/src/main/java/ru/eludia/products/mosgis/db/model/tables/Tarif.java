package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;

public class Tarif extends View {
    
    public enum c implements ColEnum {
        
	ID                        (Type.UUID,     null, "Ключ"),
	LABEL                     (Type.STRING,   null, "Наименование"),
	PRICE                     (Type.NUMERIC, 15, 3, null, "Величина"),
	CLASS                     (Type.STRING,   null, "Класс")
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, Object... p) {col = new Col (this, type, p);}
        private c (Class c,   Object... p) {col = new Ref (this, c, p);}
                
    }    

    public Tarif () {
        super  ("vw_tarifs", "Все тарифы");
        cols   (c.class);
        pk     (c.ID);
    }

    @Override
    public final String getSQL () {

        return ""
            + "SELECT uuid id, name label, price, 'PremiseUsageTarif' class FROM " + getName (PremiseUsageTarif.class) + " WHERE is_deleted = 0 AND is_annuled = 0 "
//            + " UNION "
        ;

    }

}