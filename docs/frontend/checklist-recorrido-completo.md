# Checklist de recorrido completo del frontend

Recorrido manual de las pantallas de los procesos académicos y de la
administración académica, rol por rol. Los textos de botones, diálogos y
avisos son los que implementa el frontend. La etiqueta que se ve en la
insignia de estado depende del rol y la entrega el backend (ver tabla de
etapas al final).

## Antes de empezar

- Backend y base arriba, con `data.sql` y `docs/database/seed-procesos-academicos.sql`
  cargados (ver `README.md`). La base tiene los cinco programas académicos (del
  seed o de `docs/database/migracion_programas_asignaturas.sql` si ya tenía
  datos). Las asignaturas se crean antes que los estudiantes: un estudiante
  solo matricula asignaturas que ya existen en su programa.
- Frontend con `npm start` en `http://localhost:4200`.
- Usuarios: el Secretario General `rootfiet`, un Decano, al menos dos
  Funcionarios Académicos (uno responsable de los tres procesos) y dos
  Estudiantes con asignaturas activas. Los estudiantes y funcionarios se pueden
  crear en el paso A de este recorrido.
- Archivos de prueba: PDF válidos de menos de 5 MB, un JPG o PNG, un archivo de
  más de 5 MB, un `.docx` y los Excel de `backend/pruebas/plantilla-*.xlsx`.
- Ten a mano la columna "Resultado esperado": cada fila se marca como cumplida
  solo si el resultado coincide.

## 1. Login y redirección por rol

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 1.1 | Secretario General | `/login` | Entrar con `rootfiet` | Llega a `/sec-general`; el menú incluye "Administración académica" | |
| 1.2 | Decano | `/login` | Entrar con el Decano | Llega a `/decano`; menú: Mi Usuario, Solicitudes académicas, Solicitudes del Consejo, Administración académica | |
| 1.3 | Funcionario Académico | `/login` | Entrar | Llega a `/funcionario-academico`; menú: Mi Usuario, Solicitudes | |
| 1.4 | Estudiante | `/login` | Entrar | Llega a `/estudiante`; menú: Mi Usuario, Mis solicitudes, Cancelar matrícula, Cancelar asignatura, Examen supletorio | |
| 1.5 | Cualquiera | `/login` | Contraseña errada | Error de credenciales; no hay aviso de sesión vencida | |

## 2. Administración académica

Ruta `/sec-general/administracion-academica` o `/decano/administracion-academica`.
Las dos muestran las mismas cuatro pestañas.

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 2.1 | Secretario General | Pestaña Asignaturas | Abrir la pestaña | Tabla con Código, Nombre y Programa, paginador y total; filtros de texto y de programa (con "Todos") y botón "Buscar" | |
| 2.2 | Secretario General | Asignaturas | Escribir parte de un código o nombre, elegir un programa y pulsar "Buscar" | La tabla se filtra por los dos criterios y vuelve a la página 1; con "Todos" se ven los cinco programas | |
| 2.3 | Secretario General | Asignaturas | "Nueva asignatura" | El diálogo pide programa (select con los cinco programas) y nombre; no hay campo de código | |
| 2.4 | Secretario General | Asignaturas | Elegir Ingeniería de Sistemas, escribir el nombre y "Crear" | Toast "Asignatura creada con el código ISI-<n>"; aparece en la tabla con ese código | |
| 2.5 | Secretario General | Asignaturas | Crear otra con el mismo nombre (en otras mayúsculas) en el mismo programa | El diálogo sigue abierto y muestra "La asignatura ... ya existe en el programa ... con el código ...". Con el mismo nombre en otro programa sí se crea | |
| 2.6 | Decano | Asignaturas | "Editar" en una fila, cambiar el nombre, "Guardar cambios" | Código y programa se ven de solo lectura; toast "La asignatura quedó actualizada." y el código no cambia | |
| 2.7 | Secretario General | Asignaturas | "Cargar Excel" | El diálogo lista los encabezados `nombreAsignatura` y `programaAcademico`, los cinco nombres válidos de programa y la regla de que el código se genera solo | |
| 2.8 | Secretario General | Asignaturas | Cargar `plantilla-asignaturas.xlsx` llena con "Cargar archivo" | "Se crearon N asignaturas."; la tabla se recarga con los códigos nuevos | |
| 2.9 | Secretario General | Asignaturas | Cargar un Excel con un programa que no existe o un nombre repetido | Lista de errores dentro del diálogo con fila y columna; "No se creó ningún registro" | |
| 2.10 | Secretario General | Pestaña Estudiantes | Filtrar por nombre, apellido o código con "Buscar" | La tabla muestra Código estudiantil, Nombres, Apellidos, Programa (nombre), Semestre | |
| 2.11 | Secretario General | Estudiantes | "Nuevo estudiante" | El primer campo es el select de programa; la sección de asignaturas está deshabilitada con el aviso "Elige primero el programa..." | |
| 2.12 | Secretario General | Estudiantes, alta | Elegir un programa sin asignaturas | Aviso "No hay asignaturas registradas para el programa ... Deben crearse primero en la pestaña Asignaturas."; "Crear" no envía nada | |
| 2.13 | Secretario General | Estudiantes, alta | Elegir Ingeniería de Sistemas | Cada fila trae un select "código - nombre" con las asignaturas de ese programa y el grupo; una asignatura elegida en una fila no aparece en las demás | |
| 2.14 | Secretario General | Estudiantes, alta | Con filas llenas, cambiar a otro programa | Aviso de confirmación: "Conservar el anterior" deja todo igual; "Cambiar programa" limpia las filas y carga las asignaturas del nuevo | |
| 2.15 | Secretario General | Estudiantes, alta | Llenar datos personales, de acceso, académicos y dos asignaturas, "Crear" | Toast "El estudiante quedó creado."; aparece en la tabla con su programa | |
| 2.16 | Secretario General | Estudiantes | Repetir con contraseña de 4 caracteres y correo sin arroba | Errores bajo cada campo; no se envía nada | |
| 2.17 | Secretario General | Estudiantes | "Cargar Excel" | El diálogo lista los 14 encabezados en orden (sin `nombreAsignatura`), los cinco programas válidos y la regla de que cada `codigoAsignatura` debe existir y ser del programa | |
| 2.18 | Secretario General | Estudiantes | Cargar `plantilla-estudiantes.xlsx` llena con "Cargar archivo" | "Se crearon N estudiantes."; la tabla se recarga | |
| 2.19 | Secretario General | Estudiantes | Cargar un Excel con un código de asignatura de otro programa o un encabezado cambiado | Lista de errores dentro del diálogo, con fila y columna (columna M para la asignatura); "No se creó ningún registro" | |
| 2.20 | Decano | Estudiantes | "Editar", escribir solo el semestre, "Guardar cambios" | El programa se ve de solo lectura; solo cambia el semestre y los campos vacíos se conservan | |
| 2.21 | Decano | Estudiantes | "Asignaturas" de un estudiante | Lista de matrículas con su estado; solo las activas tienen "Nuevo estado" y "Cambiar" | |
| 2.22 | Decano | Estudiantes, diálogo de asignaturas | Elegir "cancelada" en una activa y "Cambiar" | Toast "La asignatura ... quedó cancelada."; la fila ya no se puede cambiar | |
| 2.23 | Decano | Estudiantes, diálogo de asignaturas | Elegir una asignatura del select (solo las del programa del estudiante), escribir el grupo y "Agregar" | Toast "La asignatura quedó matriculada."; aparece activa. Si ya la tiene activa, el error del backend sale dentro del diálogo | |
| 2.24 | Secretario General | Pestaña Funcionarios académicos | Filtrar por nombre, apellido o dependencia | La tabla muestra la columna "Procesos a cargo" | |
| 2.25 | Secretario General | Funcionarios | "Nuevo funcionario" con dependencia, botón "Crear" | Toast "El funcionario académico quedó creado." | |
| 2.26 | Secretario General | Funcionarios | "Cargar Excel" con la plantilla de funcionarios | El diálogo lista los 9 encabezados; "Se crearon N funcionarios académicos." | |
| 2.27 | Decano | Funcionarios | "Editar dependencia", "Guardar cambios" | Toast "La dependencia quedó actualizada." | |
| 2.28 | Decano | Pestaña Responsable por proceso | Abrir la pestaña | Los tres procesos con su responsable actual o "Sin responsable" | |
| 2.29 | Decano | Responsable por proceso | "Cambiar responsable" en un proceso | Aviso de que las solicitudes pasan a la bandeja del nuevo funcionario; select con "Apellidos Nombres - dependencia" | |
| 2.30 | Decano | Responsable por proceso | Elegir al mismo responsable y "Cambiar responsable" | Error "Ese funcionario ya es el responsable de este proceso." | |
| 2.31 | Decano | Responsable por proceso | Elegir otro y "Cambiar responsable" | Toast "<proceso> quedó a cargo de <nombre>."; la tabla se recarga | |
| 2.32 | Funcionario Académico | `/funcionario-academico/solicitudes` | Entrar con el responsable anterior y luego con el nuevo | El anterior ya no ve las solicitudes de ese proceso; el nuevo sí | |

Antes de seguir, deja un solo Funcionario Académico como responsable de los
tres procesos.

## 3. Cancelación de Matrícula

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 3.1 | Estudiante | `/estudiante/cancelacion-matricula` | Abrir | Lista de asignaturas que se cancelan, motivo, seis anexos obligatorios, soportes opcionales y la advertencia de primer periodo con casilla | |
| 3.2 | Estudiante | Radicación | "Radicar solicitud" sin llenar nada | Toast "Revisa el formulario"; errores en motivo, los seis anexos y la advertencia | |
| 3.3 | Estudiante | Radicación | Subir un archivo de más de 5 MB o un `.docx` en un anexo | Error bajo el anexo; el archivo no queda | |
| 3.4 | Estudiante | Radicación | Motivo, los seis anexos, marcar "Leí y entiendo la advertencia.", "Radicar solicitud" | Toast "Solicitud radicada" con el radicado; la pantalla muestra "Tu solicitud quedó radicada con el número ..." y navega al detalle | RADICADA |
| 3.5 | Estudiante | `/estudiante/cancelacion-matricula` | Radicar otra igual | Error del backend "Ya tienes una solicitud de Cancelación de Matrícula en curso (...)" | |
| 3.6 | Funcionario Académico | Detalle de la solicitud | Abrir | Motivo, tabla de asignaturas, anexos descargables; botones "Rechazar solicitud" y "Remitir al Decano" | RADICADA |
| 3.7 | Funcionario Académico | Detalle | "Rechazar solicitud" sin observación ni escaneo, botón "Rechazar solicitud" | Errores de observación y "Debes subir el escaneo de la Resolución firmada (PDF)." | |
| 3.8 | Funcionario Académico | Detalle | Observación y escaneo PDF, "Rechazar solicitud" | Toast "La solicitud quedó rechazada."; el detalle se recarga sin botones | RECHAZADA |
| 3.9 | Estudiante | Detalle | Abrir la solicitud rechazada | Botón "Descargar Resolución"; descarga el PDF | RECHAZADA |
| 3.10 | Estudiante | Radicación | Radicar una nueva solicitud | Queda radicada | RADICADA |
| 3.11 | Funcionario Académico | Detalle | "Remitir al Decano": faltas, nota y situación de cada asignatura, "Remitir al Decano" | Toast "La solicitud fue remitida al Decano." | EN_REVISION_DECANO |
| 3.12 | Decano | `/decano/solicitudes`, detalle | Abrir | Botones "Aprobar" y "Rechazar" | EN_REVISION_DECANO |
| 3.13 | Decano | Detalle | "Aprobar" | El diálogo abre después de cargar las situaciones; un select "situación al cancelar" por asignatura | |
| 3.14 | Decano | Detalle | Elegir situaciones y "Aprobar solicitud" | Toast "La cancelación quedó aprobada." | APROBADA_POR_DECANO |
| 3.15 | Funcionario Académico | Detalle | "Enviar respuesta" con el escaneo PDF | Toast "La respuesta fue enviada al estudiante."; las asignaturas activas quedan canceladas | APROBADA |
| 3.16 | Estudiante | Detalle | Abrir | "Descargar Resolución" disponible; la tabla muestra la situación al cancelar | APROBADA |
| 3.17 | Decano | Detalle de otra solicitud remitida | "Rechazar" con observación, botón "Rechazar solicitud" | Toast "La cancelación quedó rechazada." | RECHAZADA_POR_DECANO |
| 3.18 | Funcionario Académico | Detalle | "Enviar respuesta" con escaneo | La solicitud termina | RECHAZADA |

## 4. Cancelación de Asignatura

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 4.1 | Estudiante | `/estudiante/cancelacion-asignatura` | Abrir | Casillas por asignatura activa con su grupo, motivo y soportes opcionales; sin advertencia de primer periodo | |
| 4.2 | Estudiante | Radicación | "Radicar solicitud" sin asignaturas | Error "Elige al menos una asignatura para cancelar." | |
| 4.3 | Estudiante | Radicación | Marcar dos asignaturas, motivo, "Radicar solicitud" | Radicado y "Ver solicitud"; navega al detalle | RADICADA |
| 4.4 | Estudiante | Detalle | Abrir | Tabla solo con Código y Asignatura; sin botones | RADICADA |
| 4.5 | Funcionario Académico | Detalle | "Remitir al Decano": en una asignatura nota 2.5 y "Cumple condiciones" Sí | Error "Con nota menor a 3.0 no puede marcarse que cumple las condiciones." | |
| 4.6 | Funcionario Académico | Detalle | Marcar las dos como "No" | Aviso "Ninguna asignatura cumple las condiciones: en lugar de remitir la solicitud al Decano debes rechazarla."; no se envía | |
| 4.7 | Funcionario Académico | Detalle | Una que cumple (nota 3.5, Sí) y otra que no (nota 2.5, No, con observación), "Remitir al Decano" | Toast "La solicitud fue remitida al Decano." | EN_REVISION_DECANO |
| 4.8 | Decano | Detalle | "Decidir por asignatura" | Cada asignatura muestra la evaluación del Funcionario; la que no cumple arranca en "No" | |
| 4.9 | Decano | Detalle | Aprobar la que cumple con situación al cancelar, "No" en la otra con observación, "Registrar decisiones" | Toast "La decisión por asignatura quedó registrada." | APROBADA_POR_DECANO |
| 4.10 | Decano | Detalle de otra solicitud | Marcar todas en "No" | Aviso "No se aprobó ninguna asignatura: para negar la solicitud completa usa Rechazar solicitud completa." | |
| 4.11 | Funcionario Académico | Detalle | "Enviar respuesta" con escaneo | Solo se cancela la asignatura aprobada | APROBADA |
| 4.12 | Estudiante | Detalle | Abrir | Tabla con Código, Asignatura, "Decisión del Decano" (Aprobada / No aprobada) y la observación; "Descargar Resolución" | APROBADA |
| 4.13 | Decano | Detalle de otra solicitud remitida | "Rechazar solicitud completa" con observación | Toast "La solicitud quedó rechazada." | RECHAZADA_POR_DECANO |
| 4.14 | Funcionario Académico | Detalle | "Enviar respuesta" con escaneo | | RECHAZADA |

## 5. Examen Supletorio

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 5.1 | Estudiante | `/estudiante/examen-supletorio` | Abrir | Aviso "Tienes 3 días hábiles después del examen para radicar la solicitud..."; antes de elegir causa solo aparece el FOR-23 | |
| 5.2 | Estudiante | Radicación | Elegir "Cruce con el examen de otra asignatura" | Aparecen asignatura cruzada (sin la del examen), fecha y hora (HH:mm), y el formato del docente cruzado | |
| 5.3 | Estudiante | Radicación | Fecha del examen futura u hora "2pm" | "La fecha del examen no presentado no puede ser posterior a hoy." / "La hora del examen cruzado debe tener el formato HH:mm..." | |
| 5.4 | Estudiante | Radicación | Cambiar la causa a "Otra causa justificada" | Desaparecen los datos de cruce y el archivo del docente; aparece el soporte de la justificación; el FOR-23 se conserva | |
| 5.5 | Estudiante | Radicación | Con cruce completo, FOR-23 y formato del docente, "Radicar solicitud" | Radicado y "Ver solicitud"; el detalle muestra la asignatura, la causa y el cruce con fechas dd/MM/aaaa | RADICADA |
| 5.6 | Estudiante | Radicación | Radicar con un examen de hace más de 3 días hábiles | Error del backend "El plazo para pedir el supletorio del examen del ... venció..." | |
| 5.7 | Funcionario Académico | Detalle | "Rechazar solicitud" con observación (sin Resolución) | Toast "La solicitud quedó rechazada." | RECHAZADA |
| 5.8 | Estudiante | Radicación | Radicar una nueva con causa otra | | RADICADA |
| 5.9 | Funcionario Académico | Detalle | "Remitir al Decano" sin marcar "Verifiqué los requisitos del examen supletorio." | Error "Confirma que verificaste los requisitos..." | |
| 5.10 | Funcionario Académico | Detalle | Marcar la casilla y "Remitir al Decano" | Toast "La solicitud fue remitida al Decano." | EN_REVISION_DECANO |
| 5.11 | Decano | Detalle | "Aprobar" con observación opcional, botón "Aprobar solicitud" | Toast "El examen supletorio quedó aprobado." | APROBADA_POR_DECANO |
| 5.12 | Funcionario Académico | Detalle | "Enviar recibo de pago" | El diálogo abre después de cargar los tipos de anexo; pide el recibo en PDF | |
| 5.13 | Funcionario Académico | Detalle | Subir el recibo y "Enviar recibo" | Toast "El recibo de pago fue enviado al estudiante."; el recibo aparece en Anexos | PENDIENTE_PAGO |
| 5.14 | Estudiante | Detalle | Abrir | Insignia amarilla; aviso "Descarga el recibo de pago en la sección Anexos, realiza el pago y sube el comprobante..."; descarga el recibo | PENDIENTE_PAGO |
| 5.15 | Estudiante | Detalle | "Subir comprobante de pago", archivo PDF/JPG/PNG, "Subir comprobante" | Toast "El comprobante de pago fue enviado."; aviso "El Funcionario Académico está verificando tu comprobante de pago." | EN_VERIFICACION_PAGO |
| 5.16 | Funcionario Académico | Detalle | "Aprobar comprobante" con fecha acordada anterior al examen | Error "La fecha acordada no puede ser anterior a la fecha del examen no presentado (...)" | |
| 5.17 | Funcionario Académico | Detalle | Fecha acordada válida y "Aprobar comprobante" | Toast "El comprobante quedó aprobado."; el detalle muestra la fecha acordada | APROBADA |
| 5.18 | Estudiante y Funcionario | Detalle de otra solicitud en EN_VERIFICACION_PAGO | El Funcionario usa "Rechazar comprobante" con observación | Toast "El comprobante quedó rechazado."; el Estudiante ve "Tu comprobante de pago fue rechazado... debes radicar una solicitud nueva." | RECHAZADA |
| 5.19 | Estudiante | `/estudiante/examen-supletorio` | Radicar de nuevo | Se acepta la nueva radicación | RADICADA |
| 5.20 | Decano | Detalle de otra solicitud remitida | "Rechazar" con observación, botón "Rechazar solicitud" | Toast "El examen supletorio quedó rechazado." | RECHAZADA_POR_DECANO |
| 5.21 | Funcionario Académico | Detalle | "Enviar respuesta", botón "Enviar respuesta" | Toast "La respuesta fue enviada al estudiante." | RECHAZADA |

## 6. Sesión vencida (401)

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 6.1 | Cualquiera | Una bandeja o un detalle | Con la sesión abierta, cambiar `authToken` en el almacenamiento local del navegador por un valor inválido y recargar los datos (por ejemplo "Reintentar" o cambiar de página) | Un solo toast "Tu sesión venció, inicia sesión de nuevo"; redirige a `/login`; `authToken` y `currentUser` quedan vacíos | |
| 6.2 | Cualquiera | Detalle (pide solicitud, historial y proceso a la vez) | Repetir el paso 6.1 | Aunque fallen varias peticiones, hay un solo aviso y una sola redirección | |
| 6.3 | Cualquiera | `/login` | Credenciales erradas | No aparece el aviso de sesión vencida | |

## 7. Permisos negados

| Paso | Rol | Pantalla o ruta | Acción | Resultado esperado | Etapa resultante |
| --- | --- | --- | --- | --- | --- |
| 7.1 | Estudiante | `/decano/solicitudes`, `/funcionario-academico/solicitudes`, `/decano/administracion-academica` | Escribir la ruta | `RoleGuard` redirige a `/login` (la sesión no se limpia) | |
| 7.2 | Funcionario Académico | `/estudiante/cancelacion-matricula`, `/sec-general/administracion-academica` | Escribir la ruta | Redirige a `/login` | |
| 7.3 | Decano | `/sec-general/administracion-academica` | Escribir la ruta | Redirige a `/login`; el Decano usa su propia ruta `/decano/administracion-academica` | |
| 7.4 | Secretario General | `/decano/administracion-academica`, `/estudiante/solicitudes` | Escribir la ruta | Redirige a `/login` | |
| 7.5 | Funcionario Académico no responsable | Detalle de una solicitud de un proceso que no tiene a cargo | Abrir por URL | "Solicitud no encontrada" con enlace a la bandeja | |
| 7.6 | Sin sesión | Cualquier ruta protegida | Escribir la ruta | `AuthGuard` redirige a `/login` | |

## Etapas y etiquetas

La insignia del detalle y la columna Estado de las bandejas muestran la
etiqueta del rol. El historial muestra el texto de la etapa del frontend.

| etapaCodigo | Estudiante | Funcionario | Decano | Texto en el historial | Color de la insignia |
| --- | --- | --- | --- | --- | --- |
| RADICADA | En trámite | Pendiente | (no la ve) | Radicada | Gris |
| EN_REVISION_DECANO | En trámite | En Gestión | Pendiente | En revisión del Decano | Gris |
| APROBADA_POR_DECANO | En trámite | Pendiente de Respuesta | Respondida | Aprobada por el Decano | Gris |
| RECHAZADA_POR_DECANO | En trámite | Pendiente de Respuesta | Respondida | Rechazada por el Decano | Gris |
| PENDIENTE_PAGO | Pendiente de pago | En Gestión | Respondida | Pendiente de pago | Amarillo |
| EN_VERIFICACION_PAGO | En verificación de pago | Pendiente de Verificación | Respondida | En verificación de pago | Amarillo |
| APROBADA | Aprobada | Respondida | Respondida | Aprobada | Verde |
| RECHAZADA | Rechazada | Respondida | Respondida | Rechazada | Rojo |
