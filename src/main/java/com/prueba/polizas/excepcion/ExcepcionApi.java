package com.prueba.polizas.excepcion;

import org.springframework.http.HttpStatus;

public class ExcepcionApi extends RuntimeException {
  private final HttpStatus estadoHttp;

  public ExcepcionApi(HttpStatus estadoHttp, String mensaje) {
    super(mensaje);
    this.estadoHttp = estadoHttp;
  }

  public HttpStatus getEstadoHttp() {
    return estadoHttp;
  }
}
