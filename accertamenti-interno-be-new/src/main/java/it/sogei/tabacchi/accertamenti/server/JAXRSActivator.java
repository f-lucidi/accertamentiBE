package it.sogei.tabacchi.accertamenti.server;

import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@ApplicationPath("/api/v1")
@OpenAPIDefinition(
    info = @Info(
            title = "Accertamento prodotti tabacchi",
            version = "1.0.0"
    ),
    servers = @Server(url = Constants.Application.CONTEXT_ROOT)
)
@SecuritySchemes({
    @SecurityScheme(
            securitySchemeName = "microdepexample-auth",
            type = SecuritySchemeType.HTTP,
            description = "Basic http authentication to access the resource",
            scheme = "basic"
        )
    }
)
public class JAXRSActivator extends Application {
}
