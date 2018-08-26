package ru.eludia.products.mosgis.db.model.incoming;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Num;
import ru.eludia.base.model.def.Virt;

public class InFias extends Table {

    public InFias () {
        
        super ("in_fias",     "Пакеты данных ФИАС");
        
        pk    ("uuid",        Type.UUID, NEW_UUID, "Ключ");
        
        col   ("dt",          Type.DATE,   "Дата выгрузки");
        col   ("dt_from",     Type.DATE, null,  "Дата начала процесса");
        col   ("dt_to_fact",  Type.DATE, null,  "Дата окончания процеса");

        col   ("uri_addrobj", Type.STRING, "Путь к файлу с адресными объектами");
        col   ("sz_addrobj",  Type.NUMERIC, 12, 0, Num.ZERO, "Размер XML-файла ADDROBJ, в байтах");
        col   ("rd_addrobj",  Type.NUMERIC, 12, 0, Num.ZERO, "Сколько прочитано XML-файла ADDROBJ, в байтах");
        
        col   ("uri_house",   Type.STRING, "Путь к файлу со зданиями/сооружениями");
        col   ("sz_house",    Type.NUMERIC, 12, 0, Num.ZERO, "Размер XML-файла HOUSE, в байтах");
        col   ("rd_house",    Type.NUMERIC, 12, 0, Num.ZERO, "Сколько прочитано XML-файла HOUSE, в байтах");

        col   ("uri_eststat", Type.STRING, "Путь к файлу с типами владения");
        col   ("sz_eststat",  Type.NUMERIC, 12, 0, Num.ZERO, "Размер XML-файла ESTSTAT, в байтах");
        col   ("rd_eststat",  Type.NUMERIC, 12, 0, Num.ZERO, "Сколько прочитано XML-файла ESTSTAT, в байтах");

        col   ("uri_strstat", Type.STRING, "Путь к файлу с типами строений");
        col   ("sz_strstat",  Type.NUMERIC, 12, 0, Num.ZERO, "Размер XML-файла STRSTAT, в байтах");
        col   ("rd_strstat",  Type.NUMERIC, 12, 0, Num.ZERO, "Сколько прочитано XML-файла STRSTAT, в байтах");
        
        col   ("prc", Type.NUMERIC, 3, 2, new Virt ("100*(RD_ADDROBJ+RD_HOUSE+RD_ESTSTAT+RD_STRSTAT)/(SZ_ADDROBJ+SZ_HOUSE+SZ_ESTSTAT+SZ_STRSTAT)"), "% прочитано");

    }
    
}