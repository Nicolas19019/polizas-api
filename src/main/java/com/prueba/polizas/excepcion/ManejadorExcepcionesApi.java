package com.prueba.polizas.excepcion;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ManejadorExcepcionesApi {
  @ExceptionHandler(ExcepcionApi.class)
  ResponseEntity<ProblemDetail> manejarErrorNegocio(ExcepcionApi excepcion) {
    return ResponseEntity.status(excepcion.getEstadoHttp())
        .body(ProblemDetail.forStatusAndDetail(excepcion.getEstadoHttp(), excepcion.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetail> manejarValidacion(MethodArgumentNotValidException excepcion) {
    String detalle =
        excepcion.getBindingResult().getFieldErrors().stream()
            .map(errorCampo -> errorCampo.getField() + ": " + errorCampo.getDefaultMessage())
            .sorted()
            .reduce((primerMensaje, segundoMensaje) -> primerMensaje + "; " + segundoMensaje)
            .orElse("Solicitud invalida");
    return ResponseEntity.badRequest()
        .body(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalle));
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  ResponseEntity<ProblemDetail> manejarFormatoInvalido(Exception excepcion) {
    return ResponseEntity.badRequest()
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Formato invalido. Revise el JSON, los identificadores y los valores de"
                    + " tipo/estado."));
  }
}
