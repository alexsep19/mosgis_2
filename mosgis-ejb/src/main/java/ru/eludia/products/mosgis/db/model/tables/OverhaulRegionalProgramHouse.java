package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.EnColEnum;
import ru.eludia.products.mosgis.db.model.EnTable;

public class OverhaulRegionalProgramHouse extends EnTable {
    
    public enum c implements EnColEnum {
        
        PROGRAM_UUID        (OverhaulRegionalProgram.class, "Региональная программа"),
        
        
        
    }
    
}
