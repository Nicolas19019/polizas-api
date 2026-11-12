package com.prueba.polizas.repositorio;

import com.prueba.polizas.entidad.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RepositorioPolizas extends JpaRepository<Poliza, Long> {
  @Query(
      "select p from Poliza p where (:tipo is null or p.tipo=:tipo) and (:estado is null or"
          + " p.estado=:estado) order by p.id")
  List<Poliza> filtrar(@Param("tipo") TipoPoliza tipo, @Param("estado") Estado estado);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Poliza p where p.id=:id")
  Optional<Poliza> bloquear(@Param("id") Long id);
}
