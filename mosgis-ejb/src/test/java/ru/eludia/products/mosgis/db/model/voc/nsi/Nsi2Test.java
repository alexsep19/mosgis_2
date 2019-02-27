package ru.eludia.products.mosgis.db.model.voc.nsi;

import org.junit.Test;
import static org.junit.Assert.*;
import ru.eludia.products.mosgis.db.model.voc.nsi.Nsi2.i;

public class Nsi2Test {
    
    public Nsi2Test () {
    }

    @Test
    public void test () {        
        
        assertEquals (i.HEAT_COLD_WATER.getCodes ().length, 2);
        assertEquals (i.HEAT_HOT_WATER.getCodes ().length, 2);
        assertEquals (i.HEAT_WATER.getCodes ().length, 3);        
        
        assertEquals (i.forId (19), i.HEAT_WATER);        
        
    }
    
}
