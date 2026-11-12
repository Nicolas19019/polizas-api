package com.prueba.polizas.configuracion;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FiltroClaveApi extends OncePerRequestFilter {
  private final byte[] claveApi;

  public FiltroClaveApi(@Value("${aplicacion.clave-api}") String claveApi) {
    this.claveApi = claveApi.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest peticion) {
    String ruta = peticion.getServletPath();
    return ruta.equals("/swagger-ui.html")
        || ruta.startsWith("/swagger-ui/")
        || ruta.equals("/v3/api-docs")
        || ruta.startsWith("/v3/api-docs/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadenaFiltros)
      throws ServletException, IOException {
    String claveRecibida = peticion.getHeader("x-api-key");
    if (claveRecibida == null
        || !MessageDigest.isEqual(claveApi, claveRecibida.getBytes(StandardCharsets.UTF_8))) {
      respuesta.setStatus(401);
      respuesta.setContentType("application/problem+json");
      respuesta.setCharacterEncoding("UTF-8");
      respuesta
          .getWriter()
          .write(
              "{\"title\":\"No autorizado\",\"status\":401,\"detail\":\"x-api-key ausente o"
                  + " invalida\"}");
      return;
    }
    cadenaFiltros.doFilter(peticion, respuesta);
  }
}
