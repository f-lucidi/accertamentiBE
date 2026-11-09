package it.sogei.tabacchi.accertamenti.server.exceptions.mapper;

import it.sogei.tabacchi.accertamenti.configurations.qualifiers.ExceptionMessages;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

@Slf4j
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    private static final String ERROR_MESSAGE_KEY = "errorMessage";

    @Inject
    @ExceptionMessages
    private ResourceBundle exceptionMessages;

    @Override
    public Response toResponse(BusinessException exception) {

        if (exception != null) {
            return prepareResponse(exception);
        }
        return prepareDefaultResponse();
    }


    private Response prepareDefaultResponse() {
        return prepareResponse(new BusinessException(ExceptionType.INTERNAL_SERVER_ERROR));
    }

    private Response prepareResponse(BusinessException businessException) {
        if (StringUtils.isNotBlank(businessException.getMessage())) {
            log.info("Exception message: {}, sending Bad Request as status", businessException.getMessage());
            return this.buildReponse(Response.Status.BAD_REQUEST, businessException.getMessage());

        } else {
            String errorMessage = exceptionMessages.getString(businessException.getExceptionType().getMessageKey());

            if(businessException.hasParameters()){
                errorMessage = MessageFormat.format(errorMessage, businessException.getParameters());
            }

            log.info("Exception message: {}, sending {} as status", errorMessage, businessException.getExceptionType().getStatus().toString());

            return this.buildReponse(businessException.getExceptionType().getStatus(), errorMessage);
        }
    }

    private Response buildReponse(Response.Status status, String message) {
        return Response.status(status)
                .entity(Map.of(ERROR_MESSAGE_KEY, message))
                .build();
    }
}
