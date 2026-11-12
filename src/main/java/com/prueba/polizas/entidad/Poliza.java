package com.prueba.polizas.entidad;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
public class Poliza {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TipoPoliza tipo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Estado estado = Estado.ACTIVA;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal canon;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal prima;

  @Column(nullable = false)
  private int mesesVigencia;

  @Column(nullable = false)
  private LocalDate inicioVigencia;

  @Column(nullable = false)
  private LocalDate finVigencia;

  protected Poliza() {}

  public Poliza(TipoPoliza tipo, BigDecimal canon, int meses, LocalDate inicio) {
    this.tipo = tipo;
    this.canon = canon.setScale(2, RoundingMode.HALF_UP);
    this.mesesVigencia = meses;
    this.prima = this.canon.multiply(BigDecimal.valueOf(meses));
    this.inicioVigencia = inicio;
    this.finVigencia = inicio.plusMonths(meses);
  }

  public void renovar(BigDecimal ipc) {
    BigDecimal factor = BigDecimal.ONE.add(ipc.movePointLeft(2));
    canon = canon.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    prima = prima.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    inicioVigencia = finVigencia;
    finVigencia = inicioVigencia.plusMonths(mesesVigencia);
    estado = Estado.RENOVADA;
  }

  public void cancelar() {
    estado = Estado.CANCELADA;
  }

  public Long getId() {
    return id;
  }

  public TipoPoliza getTipo() {
    return tipo;
  }

  public Estado getEstado() {
    return estado;
  }

  public BigDecimal getCanon() {
    return canon;
  }

  public BigDecimal getPrima() {
    return prima;
  }

  public int getMesesVigencia() {
    return mesesVigencia;
  }

  public LocalDate getInicioVigencia() {
    return inicioVigencia;
  }

  public LocalDate getFinVigencia() {
    return finVigencia;
  }
}
