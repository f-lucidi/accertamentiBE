package it.sogei.tabacchi.accertamenti.server.filter;

import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
public class ClientBuilderFilter implements ClientRequestFilter, ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        log.info("Service URI: {}", requestContext.getUri());
        log.info("Request Entity: {}", requestContext.getEntity());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        log.info("Response Status Code: {}", responseContext.getStatus());

        byte[] response = IOUtils.toByteArray(responseContext.getEntityStream());
        responseContext.setEntityStream(new ByteArrayInputStream(response));

        String responseEntity = Utility.bytesToString(response);

        log.info("Response Entity: {}", responseEntity);

        if (responseContext.getStatus() != 200) {
            throw new BusinessException(responseEntity);
        }
    }
}
