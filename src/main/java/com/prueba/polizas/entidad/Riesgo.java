package com.prueba.polizas.entidad;

import jakarta.persistence.*;

@Entity
public class Riesgo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "poliza_id", nullable = false)
  private Poliza poliza;

  @Column(nullable = false, length = 250)
  private String descripcion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Estado estado = Estado.ACTIVA;

  protected Riesgo() {}

  public Riesgo(Poliza poliza, String descripcion) {
    this.poliza = poliza;
    this.descripcion = descripcion;
  }

  public void cancelar() {
    estado = Estado.CANCELADA;
  }

  public Long getId() {
    return id;
  }

  public Poliza getPoliza() {
    return poliza;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public Estado getEstado() {
    return estado;
  }
}
