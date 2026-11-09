package it.sogei.tabacchi.accertamenti.server.rest;

import it.sogei.tabacchi.accertamenti.business.model.dto.AllegatoDto;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.Predicate;

@Slf4j
public abstract class AbstractService {

    @Context
    protected HttpServletRequest httpRequest;

    protected Response ok() {
        return Response.ok().build();
    }

    protected Response ok(Object body) {
        return Response.ok(body).build();
    }

    protected Response attachment(AllegatoDto allegatoDto) {
        return attachment(allegatoDto.getFilename(), allegatoDto.getMediaType(), allegatoDto.getBytes());
    }

    protected Response attachment(String filename, String mediaType, byte[] bytes) {
        return Response.ok(bytes, mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .build();
    }

    protected Object getSessionAttribute(String key) {
        return this.getSessionAttribute(key, Object.class);
    }

    protected <T> T getSessionAttribute(String key, Class<T> type) {
        if (httpRequest.getSession() != null) {
            Object sessionObject = httpRequest.getSession().getAttribute(key);
            return type.cast(sessionObject);
        }

        return null;
    }

    protected void setSessionAttribute(String key, Object object) {
        if (httpRequest.getSession() != null) {
            httpRequest.getSession().setAttribute(key, object);
        }
    }

    /**
     * Recupera una lista presente in sessione e controlla che essa rispetti il predicate in input.
     *
     * @param key       La chiave della lista in sessione
     * @param predicate il controllo da effettuare <b>(true -> OK, false -> ERRORE)</b>
     * @throws BusinessException di tipo {@link ExceptionType#OPERATION_NOT_ALLOWED} se il predicato restituisce false
     */
    @SuppressWarnings("unchecked")
    protected <S> void validateAgainstSessionObjList(String key, Predicate<List<S>> predicate) {
        List<S> sessionObjList = (List<S>) this.getSessionAttribute(key);

        if (CollectionUtils.isEmpty(sessionObjList) || predicate.negate().test(sessionObjList)) {
            log.error("validateAgainstSessionObjList: Operation not allowed - La lista ({}) in sessione non contiene riferimenti all'oggetto in parametro", key);
            throw new BusinessException(ExceptionType.OPERATION_NOT_ALLOWED);
        }
    }

//    protected <S> void validateAgainstSessionObj(String key, Class<S> sessionObjType, Predicate<S> predicate) {
//        S sessionObj = this.getSessionAttribute(key, sessionObjType);
//
//        if(Objects.isNull(sessionObj) || predicate.negate().test(sessionObj)) {
//            throw new BusinessException(ExceptionType.OPERATION_NOT_ALLOWED);
//        }
//    }
}
