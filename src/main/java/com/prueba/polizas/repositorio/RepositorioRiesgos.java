package com.prueba.polizas.repositorio;

import com.prueba.polizas.entidad.Riesgo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioRiesgos extends JpaRepository<Riesgo, Long> {
  @Query("select riesgo from Riesgo riesgo where riesgo.poliza.id=:polizaId order by riesgo.id")
  List<Riesgo> listarPorPoliza(@Param("polizaId") Long polizaId);

  @Query("select r.poliza.id from Riesgo r where r.id=:id")
  Optional<Long> buscarIdPoliza(@Param("id") Long id);
}
