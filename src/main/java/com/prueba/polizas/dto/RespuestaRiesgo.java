package com.prueba.polizas.dto;

import com.prueba.polizas.entidad.*;

public record RespuestaRiesgo(Long id, Long polizaId, String descripcion, Estado estado) {
  public static RespuestaRiesgo desdeEntidad(Riesgo riesgo) {
    return new RespuestaRiesgo(
        riesgo.getId(), riesgo.getPoliza().getId(), riesgo.getDescripcion(), riesgo.getEstado());
  }
}
