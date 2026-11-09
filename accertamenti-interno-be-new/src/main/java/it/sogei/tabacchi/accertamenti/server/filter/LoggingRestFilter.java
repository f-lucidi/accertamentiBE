package it.sogei.tabacchi.accertamenti.server.filter;

import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Provider
@Slf4j
public class LoggingRestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final int MAX_LOG_SIZE = 10000;

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (Utility.isAnnotationPresent(resourceInfo, NoLogging.class)) {
            return;
        }
        log.info("Start request URI:{} - {},", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
        if (!requestContext.getMethod().equals("GET") && requestContext.hasEntity()) {
            byte[] request = IOUtils.toByteArray(requestContext.getEntityStream());
            requestContext.setEntityStream(new ByteArrayInputStream(request));
            if(request.length < MAX_LOG_SIZE) {
                log.info("Request Entity: {}", Utility.bytesToString(request));
            } else {
                log.warn("Request Entity too large to log");
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (Utility.isAnnotationPresent(resourceInfo, NoLogging.class)) {
            return;
        }
        log.info("Response Status: {}", responseContext.getStatus());
        if (responseContext.hasEntity()) {
            Object responseEntity = responseContext.getEntity();
            if(responseEntity instanceof byte[] bytes) {
                log.info("Binary file - bytes: {}, base64: {}", bytes.length, Base64.getEncoder().encodeToString(bytes));
            } else {
                log.info("Response Entity: {}", responseEntity);
            }
        }
        log.info("End request * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
    }

}
