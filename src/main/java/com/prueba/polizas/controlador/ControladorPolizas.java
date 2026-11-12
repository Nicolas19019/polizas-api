package com.prueba.polizas.controlador;

import com.prueba.polizas.dto.*;
import com.prueba.polizas.dto.Solicitudes.*;
import com.prueba.polizas.entidad.*;
import com.prueba.polizas.servicio.ServicioPolizas;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/polizas")
public class ControladorPolizas {
  private final ServicioPolizas servicioPolizas;

  public ControladorPolizas(ServicioPolizas servicioPolizas) {
    this.servicioPolizas = servicioPolizas;
  }

  @GetMapping
  public List<Poliza> listar(
      @RequestParam(required = false) TipoPoliza tipo,
      @RequestParam(required = false) Estado estado) {
    return servicioPolizas.listar(tipo, estado);
  }

  @GetMapping("/{id}/riesgos")
  public List<RespuestaRiesgo> riesgos(@PathVariable Long id) {
    return servicioPolizas.listarRiesgos(id);
  }

  @PostMapping("/{id}/renovar")
  public Poliza renovar(@PathVariable Long id, @Valid @RequestBody Renovar cuerpo) {
    return servicioPolizas.renovar(id, cuerpo.ipc());
  }

  @PostMapping("/{id}/cancelar")
  public Poliza cancelar(@PathVariable Long id) {
    return servicioPolizas.cancelar(id);
  }

  @PostMapping("/{id}/riesgos")
  @ResponseStatus(HttpStatus.CREATED)
  public RespuestaRiesgo agregar(@PathVariable Long id, @Valid @RequestBody AgregarRiesgo cuerpo) {
    return servicioPolizas.agregarRiesgo(id, cuerpo.descripcion());
  }
}
