package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.db.model.tables.InsuranceProduct;

public class VocOrganizationInsurance extends View {

    public VocOrganizationInsurance () {
        
        super  ("vc_orgs_ins", "Страховые компании");

        pk     ("id",    Type.UUID,              "Ключ");        
        col    ("label", Type.STRING,            "Наименование");
        col    ("ogrn",  Type.NUMERIC, 15, null, "ОГРН");
        col    ("inn",   Type.NUMERIC, 12, null, "ИНН");
        col    ("kpp",   Type.NUMERIC,  9, null, "КПП");
        
    }

    @Override
    public final String getSQL () {

        return "SELECT " +
            " t.uuid id" +
            " , t.label" +
            " , t.ogrn"  +
            " , t.inn"   +
            " , t.kpp"   +
            " FROM "      + getName (VocOrganization.class)   + " t " + 
            " WHERE uuid IN (SELECT DISTINCT insuranceorg FROM  " + getName (InsuranceProduct.class)/* + " WHERE is_deleted = 0)"*/ + ")";

    }

}