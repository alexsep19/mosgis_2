package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;

public class VocUserOktmo extends View {

    public VocUserOktmo () {        
        super  ("vw_user_oktmo", "ОКТМО для операторов ОМС");
        pk     ("uuid_user",  Type.UUID,   "Пользователь");
        col    ("oktmo",      Type.STRING, "ОКТМО");
    }

    @Override
    public final String getSQL () {

        return "SELECT "
            + " u.uuid uuid_user"
            + " , o.code oktmo"
            + " FROM "       + getName (VocUser.class) + " u"
            + " INNER JOIN " + getName (VocOrganizationTerritory.class) + " t ON (u.uuid_org = t.uuid_org AND t.is_deleted = 0)"
            + " INNER JOIN " + getName (VocOktmo.class) + " o ON t.oktmo = o.id"
        ;

    }

}