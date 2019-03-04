package ru.eludia.products.mosgis.db.model.tables;

import ru.eludia.products.mosgis.db.model.tables.dyn.MultipleRefTable;
import java.sql.SQLException;
import ru.eludia.base.DB;
import ru.eludia.base.model.Col;
import ru.eludia.base.model.ColEnum;
import ru.eludia.base.model.Ref;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;
import ru.eludia.products.mosgis.db.model.incoming.xl.InXlFile;
import ru.eludia.products.mosgis.db.model.voc.VocBuilding;
import ru.eludia.products.mosgis.db.model.voc.VocGisStatus;
import ru.eludia.products.mosgis.db.model.voc.VocHouseStatus;
import ru.eludia.products.mosgis.db.model.voc.VocPassportFields;
import ru.eludia.products.mosgis.db.model.voc.VocRdColType;

public class House extends Passport {
    
    public static final String TABLE_NAME = "tb_houses";
    
    public enum c implements ColEnum {
        
        UUID (Type.UUID,   NEW_UUID,            "Ключ"),
        
        UUID_XL (InXlFile.class,        null,           "Источник импорта"),
        
        UNOM (Type.NUMERIC, 12, null,           "UNOM (код дома в московских ИС)"),
        
        FIASHOUSEGUID (VocBuilding.class,  null,                     "Глобальный уникальный идентификатор дома по ФИАС"),
        ID_STATUS_GIS (VocGisStatus.class, null,                     "Статус обмена с ГИС ЖКХ"),
        ID_STATUS (VocHouseStatus.class, VocHouseStatus.DEFAULT, "Статус размещения в ГИС ЖКХ"),
        ID_LOG  (HouseLog.class,     null ,                    "Последнее событие редактирования"),

        ADDRESS (Type.STRING,                      "Адрес"),
        ADDRESS_UC  (Type.STRING,  new Virt ("UPPER(\"ADDRESS\")"),  "АДРЕС В ВЕРХНЕМ РЕГИСТРЕ"),

        TOTALSQUARE (Type.NUMERIC, 25, 4, null,        "Общая площадь"),
        USEDYEAR    (Type.NUMERIC,  4, null,           "Год ввода в эксплуатацию"),
        CULTURALHERITAGE    (Type.BOOLEAN,     Bool.FALSE,     "Наличие у дома статуса объекта культурного наследия"),
        FLOORCOUNT  (Type.NUMERIC,  3, null,           "Количество этажей"),
        MINFLOORCOUNT   (Type.NUMERIC,  3, null,           "Количество этажей, наименьшее"),
        UNDERGROUNDFLOORCOUNT   (Type.NUMERIC,  3, null,           "Количество подземных этажей"),

        IS_CONDO    (Type.BOOLEAN, null,                "1 для МКД, 0 для ЖД"),
        HASBLOCKS   (Type.BOOLEAN, Bool.FALSE, "1 для жилых домов блокированной застройки, иначе 0"),
        HASMULTIPLEHOUSESWITHSAMEADRES  (Type.BOOLEAN, Bool.FALSE, "1, если есть несколько жилых домов с одинаковым адресом, иначе 0"),
        
        F_20819   (Type.BOOLEAN, null,       "Наличие подземного паркинга"),
        F_20140  (Type.NUMERIC, 10, null,        "Количество проживающих"),
        
        ID_VC_RD_1240   (Type.INTEGER, null,               "Вид жилого фонда"),
        
        CODE_VC_NSI_24  (Type.STRING, 20, null,            "Состояние дома"),
        KAD_N   (Type.STRING, null,                "Кадастровый номер"),
        
        GIS_GUID    (Type.UUID,           null,       "Идентификатор паспорта дома из ГИС ЖКХ"),
        GIS_UNIQUE_NUMBER   (Type.STRING,         null,       "Уникальный номер дома из ГИС ЖКХ"),
        GIS_MODIFICATION_DATE   (Type.TIMESTAMP,      null,       "Дата модификации данных дома в ГИС ЖКХ"),
        TERMINATIONDATE (Type.DATE,           null,       "Дата аннулирования объекта в ГИС ЖКХ"),
        CODE_VC_NSI_330 (Type.STRING,  20,    null,       "Причина аннулирования объекта жилищного фонда (НСИ 330)"),
        ANNULMENTINFO   (Type.STRING,         null,       "Причина аннулирования.Дополнительная информация"),
        CODE_VC_NSI_241 (Type.STRING,  20,    null,       "Способ формирования фонда капитального ремонта (НСИ 241)"),
        CODE_VC_NSI_25  (Type.STRING,  20,    null,       "Способ управления домом (НСИ 25)"),
        CODE_VC_NSI_338 (Type.STRING,  20,    null,       "Стадия жизненного цикла (НСИ 338)"),
        
        TS              (Type.TIMESTAMP,                  "Дата/время последнего изменения в БД")
//      UUID_IN_OPEN_DATA   (InOpenData.class,                 "Последний пакет импорта")
        
        ;
        
        @Override
        public Col getCol () {return col;}
        private Col col;        
        private c (Type type, java.lang.Object... p) {col = new Col (this, type, p);}
        private c (Class c,   java.lang.Object... p) {col = new Ref (this, c, p);}
        
        public boolean isToXlImport () {
            
            switch (this) {
                case UUID_XL:
                case ADDRESS:
                case UNOM:
                case FIASHOUSEGUID:
                case HASBLOCKS:
                case HASMULTIPLEHOUSESWITHSAMEADRES:
                case CODE_VC_NSI_24:
                case CODE_VC_NSI_338:
                case TOTALSQUARE:
                case USEDYEAR:
                case FLOORCOUNT:
                case CULTURALHERITAGE:
                case KAD_N:
                case F_20140:
                case F_20819:
                    return true;
                default:
                    return false;
            }
            
        }
        
    }
    
    public House () {
        
        super  (TABLE_NAME,             "МКД/ЖД");
        
        cols   (c.class);
        
        pk     (c.UUID);

        key    ("fiashouseguid1", "fiashouseguid");

        key    ("address1", "address");
        key    ("unom", "unom");

        trigger ("BEFORE INSERT OR UPDATE", ""
                + "BEGIN "
                    + ":NEW.ts := CURRENT_TIMESTAMP(); "                                    
                + "END; ");

    }

    @Override
    public void addNsiFields (DB db) throws SQLException {
        
        boolean isVirgin = refTables.isEmpty ();

            db.forEach (model.select (VocPassportFields.class, "id", "label", "id_type", "is_for_condo", "is_for_cottage", "is_multiple"), rs -> {

                if (rs.getInt ("is_for_condo") != 1 && rs.getInt ("is_for_cottage") != 1) return;
                
                if (rs.getInt ("is_multiple") == 1) {
                    
                    if (!isVirgin) return;

                    MultipleRefTable refTable = new MultipleRefTable (this, rs.getString ("id"), remark + ": " + rs.getString ("label"));
                    
                    db.adjustTable (refTable);
                    
                    refTables.add (refTable);
                    
                }
                else {

                    Col col = VocRdColType.i.forId (rs.getInt ("id_type")).getColDef ("f_" + rs.getString ("id"), rs.getString ("label"));

                    if (col == null) return;
                    if (columns.containsKey (col.getName ())) return;

                    add (col);

                }

            });
            
            db.adjustTable (this);
            
    }
    
    public enum Action {
        EDITING     (VocGisStatus.i.PENDING_RP_PLACING,   VocGisStatus.i.FAILED_PLACING),
        RELOADING   (VocGisStatus.i.PENDING_RP_RELOAD,    VocGisStatus.i.FAILED_STATE);
        
        VocGisStatus.i nextStatus;
        VocGisStatus.i failStatus;

        private Action (VocGisStatus.i nextStatus, VocGisStatus.i failStatus) {
            this.nextStatus = nextStatus;
            this.failStatus = failStatus;
        }

        public VocGisStatus.i getNextStatus () {
            return nextStatus;
        }

        public VocGisStatus.i getFailStatus () {
            return failStatus;
        }
        
        public static Action forStatus (VocGisStatus.i status) {
            switch (status) {
                case PENDING_RQ_PLACING:   return EDITING;
                case PENDING_RQ_RELOAD:    return RELOADING;
                default: return null;
            }
        }
    };
    
    public enum Object {
        HOUSE(House.class, null, "gis_guid"),
        ENTRANCE(Entrance.class, "Подъезд", "entranceguid"),
        LIFT(Lift.class, "Лифт", "liftguid"),
        RESIDENTIAL_PREMISE(ResidentialPremise.class, "Квартира", "premisesguid"),
        NON_RESIDENTIAL_PREMISE(NonResidentialPremise.class, "Нежилое помещение", "premisesguid"),
        BLOCK(Block.class, "Блок", "blockguid"),
        LIVING_ROOM(LivingRoom.class, "Комната", "livingroomguid");
        
        private Class clazz;
        private String name;
        private String gisKey;
        
        private Object (Class clazz, String name, String gisKey) {
            this.clazz = clazz;
            this.name = name;
            this.gisKey = gisKey;
        }
        
        public Class getClazz() {
            return clazz;
        }
        
        public String getName() {
            return name;
        }
        
        public String getGisKey() {
            return gisKey;
        }
    }

}