package io.mosip.print.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ObjectDoesnotExistsException extends BaseUncheckedException {
    public ObjectDoesnotExistsException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
