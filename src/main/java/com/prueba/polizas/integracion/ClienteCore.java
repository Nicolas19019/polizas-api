package com.prueba.polizas.integracion;

import com.prueba.polizas.dto.Solicitudes.EventoCore;
import com.prueba.polizas.excepcion.ExcepcionApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class ClienteCore {
  private final RestClient clienteHttp;

  public ClienteCore(
      @Value("${core.url-base}") String url, @Value("${aplicacion.clave-api}") String claveApi) {
    var fabricaPeticiones = new SimpleClientHttpRequestFactory();
    fabricaPeticiones.setConnectTimeout(2000);
    fabricaPeticiones.setReadTimeout(3000);
    clienteHttp =
        RestClient.builder()
            .baseUrl(url)
            .requestFactory(fabricaPeticiones)
            .defaultHeader("x-api-key", claveApi)
            .build();
  }

  public void actualizar(Long polizaId) {
    try {
      clienteHttp
          .post()
          .uri("/core-mock/evento")
          .body(new EventoCore("ACTUALIZACION", polizaId))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException excepcion) {
      throw new ExcepcionApi(
          HttpStatus.BAD_GATEWAY,
          "No fue posible notificar al CORE; la operacion local se revierte.");
    }
  }
}
