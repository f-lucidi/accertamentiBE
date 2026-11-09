package it.sogei.tabacchi.accertamenti.server.exceptions.enums;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {

    INTERNAL_SERVER_ERROR("error.internal", Response.Status.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_USER("user.unauthorized", Response.Status.UNAUTHORIZED),
    UNAUTHENTICATED_USER("user.unauthenticated", Response.Status.UNAUTHORIZED),
    OPERATION_NOT_ALLOWED("operation.not.allowed", Response.Status.FORBIDDEN),
    PERSISTENCE_ERROR("persistence.error", Response.Status.INTERNAL_SERVER_ERROR),
    GENERIC_FIELD_VALUE_NOT_VALID("generic.field.value.not.valid", Response.Status.BAD_REQUEST),
    NOT_FOUND("resource.not.found", Response.Status.NOT_FOUND),
    EXPORT_ERROR("export.error", Response.Status.INTERNAL_SERVER_ERROR),
    ALIQUOTA_FUTURA_ALREADY_CREATED("aliquota.futura.already.created", Response.Status.FORBIDDEN),
    SANZIONE_FUTURA_ALREADY_CREATED("sanzione.futura.already.created", Response.Status.FORBIDDEN),

    /* Messaggi che richiedono formattazione: * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    GIORNI_RITARDO_NOT_VALID("giorni.ritardo.not.valid", Response.Status.BAD_REQUEST),
    FIELD_VALUE_NOT_VALID("field.value.not.valid", Response.Status.BAD_REQUEST),
    TASSO_INTERESSE_ALREADY_CREATED("tasso.interesse.already.created.today", Response.Status.FORBIDDEN),
    ;

    private final String messageKey;
    private final Response.Status status;

}
