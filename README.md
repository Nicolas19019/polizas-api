# API de Pólizas

## Requisitos

JDK 17 o superior compatible. Maven Wrapper está incluido.

## Ejecutar

Desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Probar

1. Abrir http://localhost:8080/swagger-ui/index.html.
2. Pulsar **Authorize**, ingresar `123456`, confirmar y cerrar el cuadro.
3. Seleccionar un endpoint, pulsar **Try it out**, completar los datos y pulsar **Execute**.

Las peticiones requieren el encabezado `x-api-key: 123456`.

| Método | Ruta | Parámetros o cuerpo |
|---|---|---|
| GET | /polizas | Filtros opcionales: `tipo` y `estado` |
| GET | /polizas/{id}/riesgos | ID de póliza |
| POST | /polizas/{id}/renovar | `{"ipc":5}` (5 %) |
| POST | /polizas/{id}/cancelar | Sin cuerpo |
| POST | /polizas/{id}/riesgos | `{"descripcion":"Apartamento 301"}` |
| POST | /riesgos/{id}/cancelar | Sin cuerpo |
| POST | /core-mock/evento | `{"evento":"ACTUALIZACION","polizaId":555}` |

Tipos: `INDIVIDUAL`, `COLECTIVA`. Estados: `ACTIVA`, `RENOVADA`, `CANCELADA`.

Al iniciar se cargan la póliza individual `1` con el riesgo `1`, y la colectiva `2` con los riesgos `2` y `3`. Los datos se reinician al detener la aplicación. El mock del CORE registra los eventos en los logs de la aplicación.

## Pruebas automáticas

```powershell
.\mvnw.cmd test
```
