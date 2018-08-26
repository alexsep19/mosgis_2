package ru.eludia.products.mosgis.db.model.nsi.fields;

import java.util.Collections;
import java.util.List;
import javax.json.JsonObject;
import ru.eludia.base.model.Col;

public abstract class NsiScalarField extends NsiField {
    
    Col col;
    String fName;
        
    public NsiScalarField (JsonObject o) {
        super (o);
        fName = "F_" + name;
    }

    @Override
    public final List<Col> getCols () {
        return Collections.singletonList (col);
    }

    /**
     * Физическое имя поля в таблице БД. 
     * Отличается от getName () префиксом "F_".
     * @return "F_..."
     */
    public String getfName () {
        return fName;
    }
    
}