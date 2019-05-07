package ru.eludia.products.mosgis.db.model.voc;

import java.util.logging.Logger;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;

public class VocRdColType extends Table {
    
    protected static Logger logger = java.util.logging.Logger.getLogger (VocRdColType.class.getName ());

    public VocRdColType () {
        
        super ("vc_rd_col_types", "Типы реквизитов ГИС РД");
        
        pk    ("id",       Type.INTEGER,        "Код");
        col   ("label",    Type.STRING,         "Наименование");
        col   ("gislabel", Type.STRING,   null, "Наименование в ГИС ЖКХ");

        col   ("name",      Type.STRING,  null, "Физическое имя типа");
        col   ("length",    Type.INTEGER, null, "Длина поля");
        col   ("precision", Type.INTEGER, null, "Число десятичных знаков");
        
        data  (i.class);

    }
        
    public enum i {

        INT    (0, "INT",        "Целое",                   "NUMBER",   10,   0),
        FLOAT  (1, "FLOAT",      "Вещественное",            "NUMBER",   19,   5),
        TEXT   (2, "Текст",      "Строка",                  "VARCHAR2", 4000, 0),
        DATA   (3, "DATA",       "Файл",                    "CHAR",     1,    0),
        BOOL   (4, "BOOL",       "Логическое",              "NUMBER",   1,    0),

        REF    (6, "Справочник", "Перечислимый",            "NUMBER",   18,   0),
        YEAR   (7, "Год",        "Дата (год)",              "NUMBER",   4,    0),
        TIME   (8, "Время",      "Дата (день, месяц, год)", "DATE",     0,    0);

        int id;
        String label;
        String gislabel;
        String name;
        int length;
        int precision;

        public int getId () {
            return id;
        }

        public String getLabel () {
            return label;
        }

        public String getGislabel () {
            return gislabel;
        }

        public String getName () {
            return name;
        }

        public int getLength () {
            return length;
        }
        
        public int getPrecision () {
            return precision;
        }

        private i (int id, String label, String gislabel, String name, int length, int precision) {
            this.id = id;
            this.label = label;
            this.gislabel = gislabel;
            this.name = name;
            this.length = length;
            this.precision = precision;
        }
        
        public final static i forId (int id) {
            
            logger.info ("VocRdColType.forId: id=" + id);

            for (i i: values ()) if (i.getId () == id) return i;
            
            return null;
            
        }
        
        public Col getColDef (String name, String remark) {
            
            Col col = _getColDef (name, remark);
            
            logger.info ("VocRdColType.getColDef: name=" + name + ", this = " + this + ", col = " + col);
            
            return col;
            
        }
        
        public Col _getColDef (String name, String remark) {
            
            switch (this) {
                case TEXT:  return new Col (name, Type.STRING,   null, remark);
                case INT:   return new Col (name, Type.INTEGER, null,  remark);
                case BOOL:  return new Col (name, Type.BOOLEAN, null, remark);
                case FLOAT: return new Col (name, Type.NUMERIC, 19, 4, null, remark);
                case YEAR:  return new Col (name, Type.INTEGER, 4, null, remark);
                case TIME:  return new Col (name, Type.DATE, null, remark);
                case REF:   return new Col (name, Type.INTEGER, null, remark);
                default: return null;
            }
            
        }

    }
    
}