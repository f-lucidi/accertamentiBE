package it.sogei.tabacchi.accertamenti.server.exceptions.mapper;

import it.sogei.tabacchi.accertamenti.business.model.ErrorMessageDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ErrorMessageDto> errorList = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .map(ErrorMessageDto::new)
                .toList();

        log.error("Si e' verificata una ConstraintViolationException per i seguenti motivi: {}", errorList);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorList)
                .build();
    }
}
