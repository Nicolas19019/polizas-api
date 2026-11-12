package com.prueba.polizas.servicio;

import com.prueba.polizas.dto.RespuestaRiesgo;
import com.prueba.polizas.entidad.*;
import com.prueba.polizas.excepcion.ExcepcionApi;
import com.prueba.polizas.integracion.ClienteCore;
import com.prueba.polizas.repositorio.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicioPolizas {
  private final RepositorioPolizas polizas;
  private final RepositorioRiesgos riesgos;
  private final ClienteCore clienteCore;

  public ServicioPolizas(
      RepositorioPolizas repositorioPolizas,
      RepositorioRiesgos repositorioRiesgos,
      ClienteCore clienteCore) {
    polizas = repositorioPolizas;
    riesgos = repositorioRiesgos;
    this.clienteCore = clienteCore;
  }

  public List<Poliza> listar(TipoPoliza tipo, Estado estado) {
    return polizas.filtrar(tipo, estado);
  }

  public List<RespuestaRiesgo> listarRiesgos(Long id) {
    if (!polizas.existsById(id)) throw noEncontrada();
    return riesgos.listarPorPoliza(id).stream().map(RespuestaRiesgo::desdeEntidad).toList();
  }

  @Transactional
  public Poliza renovar(Long id, BigDecimal ipc) {
    Poliza poliza = buscarBloqueada(id);
    validarNoCancelada(poliza);
    poliza.renovar(ipc);
    polizas.flush();
    clienteCore.actualizar(id);
    return poliza;
  }

  @Transactional
  public Poliza cancelar(Long id) {
    Poliza poliza = buscarBloqueada(id);
    if (poliza.getEstado() == Estado.CANCELADA) return poliza;
    poliza.cancelar();
    riesgos.listarPorPoliza(id).forEach(Riesgo::cancelar);
    polizas.flush();
    clienteCore.actualizar(id);
    return poliza;
  }

  @Transactional
  public RespuestaRiesgo agregarRiesgo(Long id, String descripcion) {
    Poliza poliza = buscarBloqueada(id);
    validarNoCancelada(poliza);
    if (poliza.getTipo() != TipoPoliza.COLECTIVA)
      throw new ExcepcionApi(
          HttpStatus.CONFLICT,
          "Solo se pueden agregar riesgos a polizas COLECTIVAS; una INDIVIDUAL ya tiene su unico"
              + " riesgo.");
    Riesgo riesgo = riesgos.saveAndFlush(new Riesgo(poliza, descripcion.trim()));
    clienteCore.actualizar(id);
    return RespuestaRiesgo.desdeEntidad(riesgo);
  }

  @Transactional
  public RespuestaRiesgo cancelarRiesgo(Long id) {
    Long polizaId =
        riesgos
            .buscarIdPoliza(id)
            .orElseThrow(() -> new ExcepcionApi(HttpStatus.NOT_FOUND, "Riesgo no encontrado"));
    Poliza poliza = buscarBloqueada(polizaId);
    // Se consulta despues de bloquear la poliza para serializar cambios del agregado.
    Riesgo riesgo = riesgos.findById(id).orElseThrow();
    if (riesgo.getEstado() != Estado.CANCELADA) {
      riesgo.cancelar();
      riesgos.flush();
      clienteCore.actualizar(poliza.getId());
    }
    return RespuestaRiesgo.desdeEntidad(riesgo);
  }

  private Poliza buscarBloqueada(Long id) {
    return polizas.bloquear(id).orElseThrow(this::noEncontrada);
  }

  private ExcepcionApi noEncontrada() {
    return new ExcepcionApi(HttpStatus.NOT_FOUND, "Poliza no encontrada");
  }

  private void validarNoCancelada(Poliza poliza) {
    if (poliza.getEstado() == Estado.CANCELADA)
      throw new ExcepcionApi(HttpStatus.CONFLICT, "No se puede modificar una poliza cancelada");
  }
}
