package com.prueba.polizas.controlador;

import com.prueba.polizas.dto.Solicitudes.EventoCore;
import jakarta.validation.Valid;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ControladorSimuladorCore {
  private static final Logger registro = LoggerFactory.getLogger(ControladorSimuladorCore.class);

  @PostMapping("/core-mock/evento")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void evento(@Valid @RequestBody EventoCore cuerpo) {
    registro.info(
        "Intento de envio al CORE: evento={}, polizaId={}", cuerpo.evento(), cuerpo.polizaId());
  }
}
