package ru.eludia.products.mosgis;

public enum PassportKind {
    
    
    LIVING_ROOM ("Комната"),
    BLOCK       ("илое помещение"),
    PREMISE_RES ("Жилое помещение"),
    PREMISE_NRS ("Нежилое помещение"),
    CONDO       ("МКД"),
    COTTAGE     ("ЖД");
            
    String label;

    private PassportKind (String label) {
        this.label = label;
    }

    public String getLabel () {
        return label;
    }    
    
    public String getFilterFieldName () {
        return "is_for_" + name ().toLowerCase ();
    }    
    
}
