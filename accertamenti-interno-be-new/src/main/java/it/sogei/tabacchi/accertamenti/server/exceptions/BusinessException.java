package it.sogei.tabacchi.accertamenti.server.exceptions;

import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import lombok.Getter;

import java.io.Serial;

@Getter
public final class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4027807724673720152L;

    private ExceptionType exceptionType;
    private Object[] parameters;
    private String message;

    /**
     * Se l'{@code Exception} è un istanza di {@code BusinessException} restituisce l'istanza,
     * altrimenti ne crea una nuova con l'{@code ExceptionType} passato come parametro.
     */
    public static BusinessException getOrElse(ExceptionType exceptionType, Exception e) {
        return e instanceof BusinessException be ? be : new BusinessException(exceptionType);
    }

    public BusinessException(ExceptionType exceptionType) {
        this(exceptionType, (Object[]) null);
    }

    public BusinessException(ExceptionType exceptionType, Object... parameters) {
        this.exceptionType = exceptionType;
        this.parameters = parameters;
    }

    public BusinessException(String message) {
        this.message = message;
    }

    public boolean hasParameters() {
        return parameters != null && parameters.length > 0;
    }
}
