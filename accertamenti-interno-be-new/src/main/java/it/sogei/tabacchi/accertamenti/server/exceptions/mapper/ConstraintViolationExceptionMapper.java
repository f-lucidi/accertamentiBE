package it.sogei.tabacchi.accertamenti.server.exceptions.mapper;

import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

// violazioni di Bean Validation (@Valid sui parametri dei service, @NotNull/@NotEmpty sui DTO): scattano prima del service.
// Stessa risposta di BusinessExceptionMapper (400, {"errorMessage": "..."}, messaggi uniti con SEPARATORE_ERRORI)
// così il frontend legge gli errori sempre allo stesso modo
@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    // stessa chiave di BusinessExceptionMapper: il frontend legge sempre errorMessage
    private static final String ERROR_MESSAGE_KEY = "errorMessage";

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // le violazioni arrivano in un Set senza ordine: ordinate per campo, il messaggio è sempre lo stesso a parità di errori
        String errorMessage = exception.getConstraintViolations()
                .stream()
                .sorted(Comparator.comparing(violazione -> violazione.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(Constants.ErrorMessages.SEPARATORE_ERRORI));

        log.info("Exception message: {}, sending Bad Request as status", errorMessage);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(ERROR_MESSAGE_KEY, errorMessage))
                .build();
    }
}
