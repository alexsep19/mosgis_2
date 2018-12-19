package ru.eludia.products.mosgis.rest.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import ru.eludia.base.db.sql.gen.Select;
import ru.eludia.products.mosgis.db.model.voc.VocOksm;
import ru.eludia.products.mosgis.rest.api.VocOksmLocal;
import ru.eludia.products.mosgis.rest.impl.base.Base;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class VocOksmImpl extends Base implements VocOksmLocal {

    private static final Logger logger = Logger.getLogger (VocOksmImpl.class.getName ());

    private static final Pattern RE_ALFA2_CODE = Pattern.compile ("[A-Z]{2}");

    @Override
    public JsonObject select (JsonObject p) {return fetchData ((db, job) -> {

        Select select = db.getModel ()
            .select  (VocOksm.class, "code AS id", "fullname AS label", "alfa2")
            .orderBy ("alfa2, fullname")
            .limit (0, 50);

        String q = p.getString("search", "").replace('Ё', 'Е');

        if (RE_ALFA2_CODE.matcher(q).matches()) {
            select.and("alfa2", q);
        } else {
            select.andEither("shortname LIKE %?%", q.toUpperCase()) // "БРИТ" не найдет GB СОЕДИНЕННОЕ КОРОЛЕВСТВО
                .or("fullname LIKE %?%", q);
        }

        db.addJsonArrays (job, select);

    });}

}