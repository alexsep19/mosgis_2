package ru.eludia.products.mosgis.db.model.voc;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.UUID;
import ru.eludia.base.DB;
import ru.eludia.base.model.Type;
import ru.eludia.base.model.Table;
import ru.eludia.base.model.def.Bool;
import static ru.eludia.base.model.def.Def.NEW_UUID;
import ru.eludia.base.model.def.Virt;

public class VocUser extends Table {

    public static String encrypt (UUID salt, String password) {

        try {            
            MessageDigest md = MessageDigest.getInstance ("SHA-1");
            ByteBuffer bb = ByteBuffer.wrap (new byte [16]);
            bb.putLong (salt.getMostSignificantBits ());
            bb.putLong (salt.getLeastSignificantBits ());
            md.update (bb.array ());
            md.update (password.getBytes ("UTF-8"));
            String result = DB.to.hex (md.digest ());
            return result;
        }
        catch (Exception ex) {
            throw new IllegalStateException (ex);
        }

    }

    public VocUser () {

        super ("vc_users", "Пользователи Web-интерфейса системы");

        pk    ("uuid",         Type.UUID,   NEW_UUID,   "Ключ");
        ref   ("uuid_org",     VocOrganization.class, null, "Организация");

        col   ("label",        Type.STRING,             "ФИО");
        col   ("label_uc",     Type.STRING,  new Virt ("UPPER(\"LABEL\")"),  "ФИО В ВЕРХНЕМ РЕГИСТРЕ");
        
        col   ("f",            Type.STRING,             "Фамилия");
        col   ("i",            Type.STRING,             "Имя");
        col   ("o",            Type.STRING, null,       "Отчество");
        
        col   ("login",        Type.STRING, null,       "Login");
        col   ("login_uc",     Type.STRING,  new Virt ("UPPER(\"LOGIN\")"),  "LOGIN В ВЕРХНЕМ РЕГИСТРЕ");
        
        col   ("salt",         Type.UUID, null,         "Соль для пароля");
        col   ("sha1",         Type.BINARY, 20, null,   "SHA1 от пароля с учётом salt");

        col   ("is_admin",     Type.BOOLEAN,        Bool.FALSE, "1, для УЗ администратора системы; иначе 0");
//        col   ("is_blocked",   Type.BOOLEAN,        Bool.FALSE, "1, если УЗ заблокирована; иначе 0");
        col   ("is_deleted",   Type.BOOLEAN,        Bool.FALSE, "1, если УЗ удалена; иначе 0");
        
        col   ("is_locked",   Type.BOOLEAN, null,   Bool.FALSE, "1, если УЗ заблокирована; иначе 0");
        col   ("lockreason",   Type.STRING, null,        "Причина блокировки");

        key   ("label",    "label");
        key   ("label_uc", "label_uc");
        
        unique ("login", "login");
        
        trigger ("BEFORE INSERT OR UPDATE", 
                
            "BEGIN "
                    
                + ":NEW.label := TRIM   (:NEW.f || ' ' || :NEW.i || ' ' || :NEW.o);"

                + "IF :NEW.uuid_org IS NULL THEN :NEW.is_admin   := 1; ELSE :NEW.is_admin   := 0; END IF;"
//                + "IF :NEW.sha1     IS NULL THEN :NEW.is_blocked := 1; ELSE :NEW.is_blocked := 0; END IF;"
                    
            + "END;"
                
        );

    }

}