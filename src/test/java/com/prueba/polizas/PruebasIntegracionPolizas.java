package com.prueba.polizas;

import static org.junit.jupiter.api.Assertions.*;

import com.prueba.polizas.entidad.*;
import com.prueba.polizas.repositorio.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = {"server.port=18081", "core.url-base=http://localhost:18081"})
class PruebasIntegracionPolizas {
  @Autowired RepositorioPolizas polizas;
  @Autowired RepositorioRiesgos riesgos;
  final HttpClient http = HttpClient.newHttpClient();
  final ObjectMapper json = new ObjectMapper();
  Long idPolizaIndividual, idPolizaColectiva, idRiesgo;

  @BeforeEach
  void preparar() {
    riesgos.deleteAll();
    polizas.deleteAll();
    var polizaIndividual =
        polizas.save(
            new Poliza(
                TipoPoliza.INDIVIDUAL, new BigDecimal("1000000"), 12, LocalDate.of(2026, 1, 1)));
    var polizaColectiva =
        polizas.save(
            new Poliza(
                TipoPoliza.COLECTIVA, new BigDecimal("2000000"), 12, LocalDate.of(2026, 1, 1)));
    idPolizaIndividual = polizaIndividual.getId();
    idPolizaColectiva = polizaColectiva.getId();
    riesgos.save(new Riesgo(polizaIndividual, "Apartamento"));
    idRiesgo = riesgos.save(new Riesgo(polizaColectiva, "Local")).getId();
  }

  HttpResponse<String> peticion(String metodo, String ruta, String cuerpo, String claveApi)
      throws Exception {
    var constructorPeticion =
        HttpRequest.newBuilder(URI.create("http://localhost:18081" + ruta))
            .header("Content-Type", "application/json");
    if (claveApi != null) constructorPeticion.header("x-api-key", claveApi);
    constructorPeticion.method(
        metodo,
        cuerpo == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(cuerpo));
    return http.send(constructorPeticion.build(), HttpResponse.BodyHandlers.ofString());
  }

  HttpResponse<String> consultar(String ruta) throws Exception {
    return peticion("GET", ruta, null, "123456");
  }

  HttpResponse<String> enviar(String ruta, String cuerpo) throws Exception {
    return peticion("POST", ruta, cuerpo, "123456");
  }

  JsonNode leerJson(HttpResponse<String> respuesta) {
    return json.readTree(respuesta.body());
  }

  @Test
  void exigeClaveEnTodaLaApi() throws Exception {
    assertEquals(401, peticion("GET", "/polizas", null, null).statusCode());
    assertEquals(401, peticion("GET", "/polizas", null, "incorrecta").statusCode());
    assertEquals(401, peticion("POST", "/core-mock/evento", "{}", null).statusCode());
  }

  @Test
  void listaYFiltra() throws Exception {
    assertEquals(2, leerJson(consultar("/polizas")).size());
    var respuesta = consultar("/polizas?tipo=COLECTIVA&estado=ACTIVA");
    assertEquals(200, respuesta.statusCode());
    assertEquals(1, leerJson(respuesta).size());
    assertEquals(idPolizaColectiva.longValue(), leerJson(respuesta).get(0).path("id").asLong());
    assertEquals(400, consultar("/polizas?tipo=OTRA").statusCode());
  }

  @Test
  void listaRiesgos() throws Exception {
    var respuesta = consultar("/polizas/" + idPolizaIndividual + "/riesgos");
    assertEquals(200, respuesta.statusCode());
    assertEquals(1, leerJson(respuesta).size());
    assertEquals(404, consultar("/polizas/999999/riesgos").statusCode());
  }

  @Test
  void renuevaValoresYVigencia() throws Exception {
    var respuesta = enviar("/polizas/" + idPolizaIndividual + "/renovar", "{\"ipc\":5}");
    assertEquals(200, respuesta.statusCode(), respuesta.body());
    var contenido = leerJson(respuesta);
    assertEquals(0, new BigDecimal("1050000.00").compareTo(contenido.path("canon").decimalValue()));
    assertEquals(
        0, new BigDecimal("12600000.00").compareTo(contenido.path("prima").decimalValue()));
    assertEquals("RENOVADA", contenido.path("estado").asText());
    assertEquals("2027-01-01", contenido.path("inicioVigencia").asText());
    assertEquals("2028-01-01", contenido.path("finVigencia").asText());
  }

  @Test
  void cancelaTodosLosRiesgosYRechazaRenovacion() throws Exception {
    assertEquals(
        201,
        enviar("/polizas/" + idPolizaColectiva + "/riesgos", "{\"descripcion\":\"Otro local\"}")
            .statusCode());
    assertEquals(200, enviar("/polizas/" + idPolizaColectiva + "/cancelar", null).statusCode());
    for (var respuesta : leerJson(consultar("/polizas/" + idPolizaColectiva + "/riesgos")))
      assertEquals("CANCELADA", respuesta.path("estado").asText());
    assertEquals(
        409, enviar("/polizas/" + idPolizaColectiva + "/renovar", "{\"ipc\":5}").statusCode());
    assertEquals(
        409,
        enviar("/polizas/" + idPolizaColectiva + "/riesgos", "{\"descripcion\":\"Nuevo\"}")
            .statusCode());
    assertEquals(200, enviar("/polizas/" + idPolizaColectiva + "/cancelar", null).statusCode());
  }

  @Test
  void soloAgregaRiesgoAColectiva() throws Exception {
    assertEquals(
        409,
        enviar("/polizas/" + idPolizaIndividual + "/riesgos", "{\"descripcion\":\"Otro\"}")
            .statusCode());
    assertEquals(1, leerJson(consultar("/polizas/" + idPolizaIndividual + "/riesgos")).size());
    var respuesta =
        enviar("/polizas/" + idPolizaColectiva + "/riesgos", "{\"descripcion\":\"Otro\"}");
    assertEquals(201, respuesta.statusCode(), respuesta.body());
    assertEquals(idPolizaColectiva.longValue(), leerJson(respuesta).path("polizaId").asLong());
  }

  @Test
  void cancelaUnRiesgoSinCancelarLaPoliza() throws Exception {
    var respuesta = enviar("/riesgos/" + idRiesgo + "/cancelar", null);
    assertEquals(200, respuesta.statusCode());
    assertEquals("CANCELADA", leerJson(respuesta).path("estado").asText());
    assertEquals(Estado.ACTIVA, polizas.findById(idPolizaColectiva).orElseThrow().getEstado());
    assertEquals(200, enviar("/riesgos/" + idRiesgo + "/cancelar", null).statusCode());
  }

  @Test
  void validaEntradasYNoEncontrados() throws Exception {
    for (String cuerpo : new String[] {"{}", "{\"ipc\":-1}", "{\"ipc\":101}", "no-json"})
      assertEquals(400, enviar("/polizas/" + idPolizaIndividual + "/renovar", cuerpo).statusCode());
    assertEquals(
        400,
        enviar("/polizas/" + idPolizaColectiva + "/riesgos", "{\"descripcion\":\" \"}")
            .statusCode());
    assertEquals(404, enviar("/polizas/999999/cancelar", null).statusCode());
    assertEquals(404, enviar("/riesgos/999999/cancelar", null).statusCode());
  }

  @Test
  void simuladorAceptaEventoValido() throws Exception {
    assertEquals(
        202,
        enviar("/core-mock/evento", "{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}")
            .statusCode());
    assertEquals(
        400, enviar("/core-mock/evento", "{\"evento\":\"OTRO\",\"polizaId\":555}").statusCode());
  }

  @Test
  void swaggerPublicoConAutenticacionDocumentada() throws Exception {
    var interfaz = peticion("GET", "/swagger-ui/index.html", null, null);
    assertEquals(200, interfaz.statusCode());
    assertTrue(interfaz.body().contains("Swagger UI"));
    var documentacion = peticion("GET", "/v3/api-docs", null, null);
    assertEquals(200, documentacion.statusCode());
    var especificacion = leerJson(documentacion);
    assertEquals(
        "x-api-key",
        especificacion
            .path("components")
            .path("securitySchemes")
            .path("ClaveApi")
            .path("name")
            .asText());
    assertEquals(6, especificacion.path("paths").size());
    assertTrue(especificacion.path("paths").has("/polizas/{id}/renovar"));
    assertEquals(401, peticion("GET", "/polizas", null, null).statusCode());
  }
}
