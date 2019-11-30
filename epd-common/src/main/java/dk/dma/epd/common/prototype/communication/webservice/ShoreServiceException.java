/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.epd.common.prototype.communication.webservice;

/**
 * Shore service exception 
 */
public class ShoreServiceException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private int errroCode;
    private String extraMessage;
    
    public ShoreServiceException(int errorCode, String extraMessage) {
        this(errorCode);
        this.extraMessage = extraMessage;
    }
    
    public ShoreServiceException(int errorCode) {
        super(ShoreServiceErrorCode.getErrorMessage(errorCode));
        this.errroCode = errorCode;
    }
    
    public ShoreServiceException(String message) {
        super(message);
    
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
