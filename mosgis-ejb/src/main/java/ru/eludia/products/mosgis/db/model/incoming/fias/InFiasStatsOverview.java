package ru.eludia.products.mosgis.db.model.incoming.fias;

import ru.eludia.base.model.Type;
import ru.eludia.base.model.View;
import ru.eludia.products.mosgis.db.model.incoming.InFias;

public class InFiasStatsOverview extends View {
    
    public InFiasStatsOverview () {
        
        super  ("in_fias_stats_overview",     "Статистика импорта справочников ФИАС");

        col   ("started",        Type.TIMESTAMP,   null, "Дата/время запуска импорта");
        
        col   ("total_read",     Type.INTEGER,     null, "Количество считанных байтов (всего)");
        col   ("total_size",     Type.INTEGER,     null, "Оставшиеся байты для считывания (общие)");
        
        col   ("addrobj_read",   Type.INTEGER,     null, "Количество считанных байтов (ADDR)");
        col   ("addrobj_size",   Type.INTEGER,     null, "Оставшиеся байты для считывания (ADDR)");
        
        col   ("house_read",     Type.INTEGER,     null, "Количество считанных байтов (HOUSE)");
        col   ("house_size",     Type.INTEGER,     null, "Оставшиеся байты для считывания (HOUSE)");
        
        col   ("eststat_read",   Type.INTEGER,     null, "Количество считанных байтов (ESTSTAT)");
        col   ("eststat_size",   Type.INTEGER,     null, "Оставшиеся байты для считывания (ESTSTAT)");
        
        col   ("strstat_read",   Type.INTEGER,     null, "Количество считанных байтов (STRSTAT)");
        col   ("strstat_size",   Type.INTEGER,     null, "Оставшиеся байты для считывания (STRSTAT)");
    }
    
    @Override
    public String getSQL () {
        
        return "WITH parts AS "
                + "( "
                    + "SELECT "
                        + "if.dt starting, "
                        + "if.sz_addrobj sz_addr, "
                        + "if.rd_addrobj rd_addr, "
                        + "if.sz_house sz_house,  "
                        + "if.rd_house rd_house,  "
                        + "if.sz_eststat sz_est,  "
                        + "if.rd_eststat rd_est,  "
                        + "if.sz_strstat sz_str,  "
                        + "if.rd_strstat rd_str,  "
                        + "if.sz_total sz, "
                        + "if.rd_total rd  "
                    + "FROM " + getName (InFias.class) + " if, "
                        + "( "
                            + "SELECT "
                                + "MAX(dt) AS maxdt "
                            + "FROM " + getName (InFias.class) + " "
                            + "GROUP BY dt "
                        + ") maxdate "
                    + "WHERE "
                        + "if.dt = maxdate.maxdt "
                + ") "
                + "SELECT "
                    + "starting started, "
                    + "rd total_done, "
                    + "sz total_size, "
                    + "sz_addr addrobj_size, "
                    + "rd_addr addrobj_read, "
                    + "sz_house house_size,  "
                    + "rd_house house_read,  "
                    + "sz_est eststat_size,  "
                    + "rd_est eststat_read,  "
                    + "sz_str strstat_size,  "
                    + "rd_str strstat_read   "
                + "FROM parts";                

    }
    
}
