package ru.eludia.products.mosgis.ws.soap.impl.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.gosuslugi.dom.schema.integration.base.CommonResultType;
import ru.gosuslugi.dom.schema.integration.base.ErrorMessageType;
import ru.gosuslugi.dom.schema.integration.base.Fault;

public enum Error {
    
    EXP001000 ("Внутренняя ошибка"),
    INT002013 ("Запрос не найден");

    String description;
    private static final ru.gosuslugi.dom.schema.integration.base.ObjectFactory of = new ru.gosuslugi.dom.schema.integration.base.ObjectFactory ();

    private Error (String description) {
        this.description = description;
    } 
    
    public final ErrorMessageType toErrorMessageType () {
        ErrorMessageType res = of.createErrorMessageType ();
        res.setErrorCode (this.name ());
        res.setDescription (this.description);
        return res;
    }        
    
    public final Fault toFault () {
        return toFault (null);
    }
    
    public final Fault toFault (Throwable x) {
        Fault res = of.createFault ();
        res.setErrorCode (this.name ());
        res.setErrorMessage (description);
        if (x != null) {
            UUID uuid = UUID.randomUUID ();
            Logger.getLogger (Error.class.getName ()).log (Level.SEVERE, uuid.toString (), x);
            StringWriter sw = new StringWriter ();
            PrintWriter pw = new PrintWriter (sw);
            pw.print (uuid.toString ());
            pw.print (' ');
            x.printStackTrace (pw);
            res.setStackTrace (sw.toString ().replaceAll ("ru\\.eludia", "(vendor)"));
        }
        return res;
    }
    
    public final CommonResultType.Error toCommonResultTypeError (String message) {
        CommonResultType.Error e = of.createCommonResultTypeError ();
        e.setErrorCode (this.name ());
        e.setDescription (message);
        return e;
    }

    
}