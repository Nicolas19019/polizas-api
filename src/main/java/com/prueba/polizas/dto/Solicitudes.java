package com.prueba.polizas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class Solicitudes {
  private Solicitudes() {}

  public record Renovar(
      @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Schema(example = "5") BigDecimal ipc) {}

  public record AgregarRiesgo(
      @NotBlank @Size(max = 250) @Schema(example = "Apartamento 301") String descripcion) {}

  public record EventoCore(
      @NotBlank @Pattern(regexp = "ACTUALIZACION") @Schema(example = "ACTUALIZACION") String evento,
      @NotNull @Positive @Schema(example = "555") Long polizaId) {}
}
