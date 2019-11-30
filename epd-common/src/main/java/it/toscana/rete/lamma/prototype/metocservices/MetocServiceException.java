
package it.toscana.rete.lamma.prototype.metocservices;

import dk.dma.epd.common.prototype.communication.webservice.ShoreServiceException;

/**
 * Metoc service exception
 */
public class MetocServiceException extends ShoreServiceException {
    
    private static final long serialVersionUID = 1L;
    
    private int errroCode;
    private String extraMessage;
    
    public MetocServiceException(int errorCode, String extraMessage) {
        this(errorCode);
        this.extraMessage = extraMessage;
    }
    
    public MetocServiceException(int errorCode) {
        super(MetocServiceErrorCode.getErrorMessage(errorCode));
        this.errroCode = errorCode;
    }
    
    public int getErrroCode() {
        return errroCode;
    }
    
    public void setErrroCode(int errroCode) {
        this.errroCode = errroCode;
    }
    
    public void setExtraMessage(String extraMessage) {
        this.extraMessage = extraMessage;
    }
    
    public String getExtraMessage() {
        return extraMessage;
    }

}
