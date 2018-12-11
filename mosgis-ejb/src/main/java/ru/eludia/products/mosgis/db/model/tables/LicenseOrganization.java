package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.EnTable;
import ru.eludia.products.mosgis.db.model.voc.VocOrganization;


public class LicenseOrganization extends EnTable{

    public enum Columns implements ColEnum {
        
        LEGAL              (VocOrganization.class, "Юридическое лицо"),
        ENTRP              (VocOrganization.class, "ИП"),
        LICENSEORGGUID     (Type.UUID,    null,    "UUID лицензиата в системе");

        @Override
        public Col getCol () {return col;}
        private Col col;        
        private Columns (Type type, Object... p) {col = new Col (this, type, p);}
        private Columns (Class c,   Object... p) {col = new Ref (this, c, p);} 
        
    }
        
    public LicenseOrganization () {
        super ("tb_licence_organizations", "Информация о лицензиате");
        
        cols   (Columns.class);
        
        key    ("license_org_guid", LicenseOrganization.Columns.LICENSEORGGUID);

    }
    
}
