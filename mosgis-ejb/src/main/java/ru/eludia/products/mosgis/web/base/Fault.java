package ru.eludia.products.mosgis.web.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.ws.WebFault;

/**
 *
 * @author Aleksei
 */
@WebFault(name = "Fault", targetNamespace = "http://gis.gkh.mos.ru/schema/integration/base/")
public class Fault extends Exception {

    private ru.mos.gkh.gis.schema.integration.base.Fault faultInfo;

    public Fault(String message) {      
        super(message);
    }
    
    public Fault(String message, Throwable cause) {      
        super(message, cause);
    }
    
    public Fault(String message, Errors error) {      
        super(error.name() + ": " + message);
        this.faultInfo = generateDetail(error.name(), message, null);
    }
    
    public Fault(String message, Errors error, Throwable cause) {      
        super(error.name() + ": " + message, cause);
        this.faultInfo = generateDetail(error.name(), message, cause);
    }
    
    public Fault(Errors error) {      
        super(error.getMessageWithCode());
        this.faultInfo = generateDetail(error, null);
    }

    public Fault(Errors error, Throwable cause) {
        super(error.getMessageWithCode(), cause);
        this.faultInfo = generateDetail(error, cause);
    }
    
    private ru.mos.gkh.gis.schema.integration.base.Fault generateDetail(Errors error, Throwable cause) {
        if (error == null)
            return null;
        return generateDetail(error.name(), error.getMessage(), cause);
    }
    
    private ru.mos.gkh.gis.schema.integration.base.Fault generateDetail(String errorCode, String errorMessage, Throwable cause) {
        if (errorCode == null)
            return null;
        ru.mos.gkh.gis.schema.integration.base.Fault detail = new ru.mos.gkh.gis.schema.integration.base.Fault();
        detail.setErrorCode(errorCode);
        detail.setErrorMessage(errorMessage);
        if (cause != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            cause.printStackTrace(pw);
            sw.getBuffer().toString();
            detail.setStackTrace(sw.getBuffer().toString());
        }
        return detail;
    }

}
