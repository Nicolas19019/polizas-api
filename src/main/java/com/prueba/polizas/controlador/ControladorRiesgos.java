package com.prueba.polizas.controlador;

import com.prueba.polizas.dto.RespuestaRiesgo;
import com.prueba.polizas.servicio.ServicioPolizas;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/riesgos")
public class ControladorRiesgos {
  private final ServicioPolizas servicioPolizas;

  public ControladorRiesgos(ServicioPolizas servicioPolizas) {
    this.servicioPolizas = servicioPolizas;
  }

  @PostMapping("/{id}/cancelar")
  public RespuestaRiesgo cancelar(@PathVariable Long id) {
    return servicioPolizas.cancelarRiesgo(id);
  }
}
