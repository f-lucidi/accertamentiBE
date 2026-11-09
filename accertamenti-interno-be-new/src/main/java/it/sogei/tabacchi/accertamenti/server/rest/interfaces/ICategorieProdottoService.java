package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


public interface ICategorieProdottoService {
    @GET
    @Path("/{sigla}/descrizione")
    @Produces(MediaType.TEXT_PLAIN)
    Response getDescrizioneCategoria(@PathParam("sigla") String siglaCategoria);
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllCategorieProdotto(
            @QueryParam("sigla") String sigla,
            @QueryParam("descrizione") String descrizione);
}
