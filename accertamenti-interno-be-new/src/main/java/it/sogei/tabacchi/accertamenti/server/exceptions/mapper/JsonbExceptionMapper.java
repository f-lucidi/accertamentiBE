package it.sogei.tabacchi.accertamenti.server.exceptions.mapper;

import it.sogei.tabacchi.accertamenti.configurations.qualifiers.ExceptionMessages;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.ResourceBundle;

// body assente o non è un JSON valido: JSON-B fallisce prima del service.
// Senza questo mapper diventava un 500, invece è un errore del client (400)
@Slf4j
@Provider
public class JsonbExceptionMapper implements ExceptionMapper<JsonbException> {

    // stessa chiave di BusinessExceptionMapper: il frontend legge sempre errorMessage
    private static final String ERROR_MESSAGE_KEY = "errorMessage";

    @Inject
    @ExceptionMessages
    private ResourceBundle exceptionMessages;

    @Override
    public Response toResponse(JsonbException exception) {
        log.warn("Body della richiesta non leggibile: {}", exception.getMessage());
        ExceptionType tipoErrore = ExceptionType.REQUEST_BODY_NOT_VALID;
        return Response.status(tipoErrore.getStatus())
                .entity(Map.of(ERROR_MESSAGE_KEY, exceptionMessages.getString(tipoErrore.getMessageKey())))
                .build();
    }
}
