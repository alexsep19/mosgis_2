package ru.eludia.products.mosgis.ws.soap.impl.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.xml.ws.WebFault;

/**
 *
 * @author Aleksei
 */
@WebFault(name = "Fault", targetNamespace = "http://gis.gkh.mos.ru/schema/integration/base/")
public class Fault extends Exception {

	private static final long serialVersionUID = -1818905802359262315L;
	
	private ru.gosuslugi.dom.schema.integration.base.Fault faultInfo;
    private String faultCode;
    private String faultMessage;
    
    
    public Fault(String message) {      
        super(message);
    }
    
    public Fault(String message, Throwable cause) {
        super(message, cause);
        this.faultMessage = message;
    }
    
    public Fault(String message, Errors error) {      
        super(error.name() + ": " + message);
        this.faultCode = error.name();
        this.faultMessage = message;
        this.faultInfo = generateDetail(error.name(), message, null);
    }
    
    public Fault(String message, Errors error, Throwable cause) {      
        super(error.name() + ": " + message, cause);
        this.faultCode = error.name();
        this.faultMessage = message;
        this.faultInfo = generateDetail(error.name(), message, cause);
    }
    
    public Fault(Errors error) {      
        super(error.getMessageWithCode());
        this.faultCode = error.name();
        this.faultMessage = error.getMessage();
        this.faultInfo = generateDetail(error, null);
    }

    public Fault(Errors error, Throwable cause) {
        super(error.getMessageWithCode(), cause);
        this.faultCode = error.name();
        this.faultMessage = error.getMessage();
        this.faultInfo = generateDetail(error, cause);
    }
    
    public Fault(ru.gosuslugi.dom.schema.integration.base.Fault faultInfo) {
    	super(faultInfo.getErrorCode() + ": " + faultInfo.getErrorCode());
    	this.faultCode = faultInfo.getErrorCode();
    	this.faultMessage = faultInfo.getErrorMessage();
        this.faultInfo = faultInfo;
    }
    
    private ru.gosuslugi.dom.schema.integration.base.Fault generateDetail(Errors error, Throwable cause) {
        if (error == null)
            return null;
        return generateDetail(error.name(), error.getMessage(), cause);
    }
    
    private ru.gosuslugi.dom.schema.integration.base.Fault generateDetail(String errorCode, String errorMessage, Throwable cause) {
        if (errorCode == null)
            return null;
        ru.gosuslugi.dom.schema.integration.base.Fault detail = new ru.gosuslugi.dom.schema.integration.base.Fault();
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

    public String getFaultCode() {
        return faultCode;
    }
    
    public String getFaultMessage() {
        return faultMessage;
    }
    
    public ru.gosuslugi.dom.schema.integration.base.Fault getFaultInfo() {
        return faultInfo;
    }
}
