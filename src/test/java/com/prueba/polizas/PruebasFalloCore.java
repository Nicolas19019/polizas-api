package com.prueba.polizas;

import static org.junit.jupiter.api.Assertions.*;

import com.prueba.polizas.entidad.*;
import com.prueba.polizas.excepcion.ExcepcionApi;
import com.prueba.polizas.repositorio.*;
import com.prueba.polizas.servicio.ServicioPolizas;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
      "core.url-base=http://localhost:1",
      "spring.datasource.url=jdbc:h2:mem:falloCore;DB_CLOSE_DELAY=-1"
    })
class PruebasFalloCore {
  @Autowired ServicioPolizas servicioPolizas;
  @Autowired RepositorioPolizas polizas;
  @Autowired RepositorioRiesgos riesgos;
  Long polizaId, riesgoId;

  @BeforeEach
  void preparar() {
    riesgos.deleteAll();
    polizas.deleteAll();
    var poliza =
        polizas.save(
            new Poliza(
                TipoPoliza.COLECTIVA, new BigDecimal("1000000"), 12, LocalDate.of(2026, 1, 1)));
    polizaId = poliza.getId();
    riesgoId = riesgos.save(new Riesgo(poliza, "Local")).getId();
  }

  @Test
  void revierteRenovacionSiCoreFalla() {
    var excepcion =
        assertThrows(
            ExcepcionApi.class, () -> servicioPolizas.renovar(polizaId, new BigDecimal("5")));
    assertEquals(HttpStatus.BAD_GATEWAY, excepcion.getEstadoHttp());
    var poliza = polizas.findById(polizaId).orElseThrow();
    assertEquals(Estado.ACTIVA, poliza.getEstado());
    assertEquals(0, new BigDecimal("1000000").compareTo(poliza.getCanon()));
    assertEquals(LocalDate.of(2027, 1, 1), poliza.getFinVigencia());
  }

  @Test
  void revierteCancelacionDePolizaYRiesgos() {
    assertThrows(ExcepcionApi.class, () -> servicioPolizas.cancelar(polizaId));
    assertEquals(Estado.ACTIVA, polizas.findById(polizaId).orElseThrow().getEstado());
    assertEquals(Estado.ACTIVA, riesgos.findById(riesgoId).orElseThrow().getEstado());
  }

  @Test
  void revierteNuevoRiesgo() {
    assertThrows(ExcepcionApi.class, () -> servicioPolizas.agregarRiesgo(polizaId, "Otro"));
    assertEquals(1, riesgos.count());
  }

  @Test
  void revierteCancelacionDeRiesgo() {
    assertThrows(ExcepcionApi.class, () -> servicioPolizas.cancelarRiesgo(riesgoId));
    assertEquals(Estado.ACTIVA, riesgos.findById(riesgoId).orElseThrow().getEstado());
  }
}
