package ru.eludia.products.mosgis.db.model.voc;

import ru.eludia.base.db.sql.build.QP;
import ru.eludia.base.model.View;
import ru.eludia.base.model.Type;
import ru.eludia.products.mosgis.PassportKind;
import ru.eludia.products.mosgis.db.model.nsi.NsiTable;

public class VocPassportFields extends View {
    
    private static final int PASS_FLD_LAND       = 11098;	// Земельный участок
    private static final int PASS_FLD_CONDO_AREA = 20800;	// Площадь здания (многоквартирного дома), в том числе:
    
    /**
     * Поля МКД, ошибочно заявленные для ЖД
     */
    private static final int [] PASS_FLD_NOT_FOR_COTTAGE = {PASS_FLD_LAND, PASS_FLD_CONDO_AREA};

    private static final int PASS_FLD_PLUMBING      = 20146;	// Оснащенность водоразборными устройствами и санитарно-техническим оборудованием   
    private static final int PASS_FLD_DORM_PROPS    = 20901;	// Характеристики общежития
    private static final int PASS_FLD_HEAT_PURPOSE  = 20054;	// Направление использования тепловой энергии на нужды отопления
    private static final int PASS_FLD_GAS_PURPOSE   = 20053;	// Направления использования газа
    private static final int PASS_FLD_ELECTRO_EQUIP = 20056;	// Наличие электрооборудования
    private static final int PASS_FLD_WINDOW_MATERIAL = 13059;	// Материал окон

    /**
     * Поля со множественными значениями — что никак не обозначено в со стороны сервиса ГИС
     */
    private static final int [] PASS_FLD_MULTIPLE = {PASS_FLD_PLUMBING, PASS_FLD_DORM_PROPS, PASS_FLD_HEAT_PURPOSE, PASS_FLD_GAS_PURPOSE, PASS_FLD_ELECTRO_EQUIP, PASS_FLD_WINDOW_MATERIAL};
    
    
    /**
     * Волшебный номер, под которым в ГИС ЖКХ значится "Форма описания объектов ЖФ",
     * то есть список полей папортов МКД/ЖД/итд.
     */
    public static final int PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER = 197;
    
    public VocPassportFields () {

        super  ("vc_pass_fields", "Поля паспортов МКД/ЖД и пр. объектов, определённые согласно НСИ " + PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER);

        pk    ("id",      Type.STRING,                                         "Код поля");        
        col   ("label",   Type.STRING,                                         "Наименование поля");
        col   ("is_mandatory",   Type.BOOLEAN,                                 "1 для 'обязательных' полей");
        col   ("ord_src", Type.STRING,                                         "Порядок сортировки");
        col   ("unit",    Type.STRING,                                         "Единица измерения");
        col   ("voc",     Type.INTEGER,                                        "Код справочника НСИ (для перечислимых значений)");
        col   ("note",    Type.STRING,                                         "Описание");
        col   ("is_for_condo",   Type.BOOLEAN,                                 "1 для полей МКД, иначе 0");
        col   ("is_for_cottage", Type.BOOLEAN,                                 "1 для полей ЖД, иначе 0");
        col   ("is_for_premise_res", Type.BOOLEAN,                             "1 для полей жилых помещенй, иначе 0");
        col   ("is_for_premise_nrs", Type.BOOLEAN,                             "1 для полей нежилых помещенй, иначе 0");
        col   ("is_for_block", Type.BOOLEAN,                                   "1 для блоков ЖД, иначе 0");
        col   ("is_for_living_room", Type.BOOLEAN,                             "1 для комнат, иначе 0");
        col   ("id_type", Type.INTEGER,                                        "id типа (по vc_rd_col_types)");
        col   ("is_multiple", Type.BOOLEAN,                                    "1 для множественных полей, иначе 0");
        col   ("id_dt",      Type.STRING,                                      "Код поля даты (для документов)");        
        col   ("id_no",      Type.STRING,                                      "Код поля номера (для документов)");
        col   ("doc_label",  Type.STRING,                                      "Краткое наименование типа документа");
        col   ("is_for_uo",  Type.BOOLEAN,                                     "1 для полей, которые разрешено редактировать УО, иначе 0");
        col   ("is_for_oms", Type.BOOLEAN,                                     "1 для полей, которые разрешено редактировать ОМС, иначе 0");
        col   ("is_for_esp", Type.BOOLEAN,                                     "1 для полей, которые разрешено редактировать ЕСП, иначе 0");

    }

    @Override
    public final String getSQL () {
        
        QP sb = new QP ("SELECT " +
            "  f.code id" +
            "  , f.f_dfde1ffd13 label" +
            "  , f.f_ee615eea5b is_mandatory" +
            "  , f.f_d2fc7cb771 ord_src" +
            "  , u.national unit" +
            "  , f.f_f14793f679 voc" +
            "  , f.f_38ca0af80c note" +
            "  , d.id_dt" +
            "  , d.id_no" +
            "  , d.label doc_label" +
            "  , r.is_for_uo" +
            "  , r.is_for_oms" +
            "  , r.is_for_esp"
        );
        
        for (PassportKind i: PassportKind.values ()) {
            sb.append ("  , CASE WHEN (f_d121e7e83c = 0 OR f.f_d27ad7aa04 LIKE '%");
            sb.append (i.getLabel ());
            sb.append ("%')");
            if (PassportKind.COTTAGE.equals (i)) {
                sb.append (" AND f.code NOT IN (");
                for (int id: PASS_FLD_NOT_FOR_COTTAGE) {
                    sb.append (id);
                    sb.append (',');
                }
                sb.setLastChar (')');
            }
            sb.append (" THEN 1 ELSE 0 END ");
            sb.append (i.getFilterFieldName ());
        }

        sb.append (", CASE WHEN f.code IN (");
        for (int id: PASS_FLD_MULTIPLE) {
            sb.append (id);
            sb.append (',');
        }
        sb.setLastChar (')');
        sb.append (" THEN 1 ELSE 0 END is_multiple");
        
        sb.append (", t.id id_type" +
            " FROM " +
            getSrcTableName () +  " f" +
            "  LEFT JOIN " + getName (VocRdColType.class) +  " t ON f.f_82f23dfb08 = t.gislabel" +
            "  LEFT JOIN " + getName (VocOkei.class)      +  " u ON f.f_c6e5a29665 = u.code" +
            "  LEFT JOIN " + getName (VocPassportDocFields.class)  + " d ON f.code = d.id" +
            "  LEFT JOIN " + getName (VocNsi197Roles.class)  + " r ON f.code = r.code" +
            " WHERE " +
            "  f.isactual=1");
        
        return sb.toString ();

    }

    public static String getSrcTableName () {
        return NsiTable.getName (PASSPORT_FIELDS_LIST_NSI_REGISTRY_NUMBER);
    }  

}