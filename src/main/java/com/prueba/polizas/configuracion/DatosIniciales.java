package com.prueba.polizas.configuracion;

import com.prueba.polizas.entidad.*;
import com.prueba.polizas.repositorio.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatosIniciales implements CommandLineRunner {
  private final RepositorioPolizas polizas;
  private final RepositorioRiesgos riesgos;

  public DatosIniciales(
      RepositorioPolizas repositorioPolizas, RepositorioRiesgos repositorioRiesgos) {
    polizas = repositorioPolizas;
    riesgos = repositorioRiesgos;
  }

  @Override
  @Transactional
  public void run(String... argumentos) {
    if (polizas.count() > 0) return;
    Poliza polizaIndividual =
        polizas.save(
            new Poliza(
                TipoPoliza.INDIVIDUAL, new BigDecimal("1000000"), 12, LocalDate.of(2026, 1, 1)));
    Poliza polizaColectiva =
        polizas.save(
            new Poliza(
                TipoPoliza.COLECTIVA, new BigDecimal("2000000"), 12, LocalDate.of(2026, 1, 1)));
    riesgos.save(new Riesgo(polizaIndividual, "Apartamento 101"));
    riesgos.save(new Riesgo(polizaColectiva, "Local 201"));
    riesgos.save(new Riesgo(polizaColectiva, "Local 202"));
  }
}
