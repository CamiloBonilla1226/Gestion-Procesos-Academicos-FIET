-- =====================================================================
-- Script de creación — Extensión sobre la base de datos de Julián Camacho
-- Universidad del Cauca — FIET — Solicitudes Académicas
-- =====================================================================
-- Este script asume que la base de datos de Julián Camacho YA EXISTE y
-- contiene, sin modificarlas, al menos estas tablas reales (verificadas
-- contra el código fuente de back-fiet-sc, no contra nombres supuestos):
--   usuariosLivianos (uuidUsuario PK, nombres, apellidos, estado, fechaCreacion)
--   usuarios         (uuidUsuario PK/FK -> usuariosLivianos, tipoDocumento,
--                      numeroDocumento, telefono, correoElectronico,
--                      username, password, uuidTipoUsuario FK)
--   tiposUsuario     (uuidTipoUsuario PK, nombre)
-- Los nombres van en camelCase, tal como Hibernate los generó con
-- PhysicalNamingStrategyStandardImpl (sin snake_case). Ninguna de las
-- tres se modifica ni se le agrega una columna.
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. Programas académicos
-- ---------------------------------------------------------------------

-- Catálogo fijo de los cinco programas de la FIET, sembrado en
-- docs/database/seed-procesos-academicos.sql con UUID fijos. La sigla
-- forma el código de las asignaturas del programa (PIS-001, PIET-014, ...).
-- Siglas desde T12.34 (P61): PIET, PIS, PIAI, TTM y PIAC (antes IET, ISI,
-- IAI, TTE e IAC). Se cambiaron con UPDATE; el código de una asignatura se
-- guarda como texto al crearla, así que las que ya existían conservan el
-- código con la sigla anterior y solo las nuevas usan la sigla nueva.
-- ultimoConsecutivoAsignatura es el contador de ese código: el backend lo
-- lee con bloqueo de fila (SELECT ... FOR UPDATE), le suma uno por cada
-- asignatura nueva y lo guarda en la misma transacción. Nunca baja, así
-- que un código no se reutiliza aunque se borre la asignatura.
-- Una base con estudiantes y asignaturas anteriores a esta tabla se pone
-- al día con docs/database/migracion_programas_asignaturas.sql (ver el
-- diccionario de datos).
CREATE TABLE IF NOT EXISTS PROGRAMA_ACADEMICO (
    uuidProgramaAcademico        VARCHAR(100) NOT NULL,
    nombre                       VARCHAR(100) NOT NULL,
    sigla                        VARCHAR(10)  NOT NULL,
    ultimoConsecutivoAsignatura  INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (uuidProgramaAcademico),
    UNIQUE KEY uk_programa_nombre (nombre),
    UNIQUE KEY uk_programa_sigla (sigla)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 2. Usuarios especializados
-- ---------------------------------------------------------------------

-- El programa del estudiante se elige al crearlo y no se edita después.
-- Solo puede matricular asignaturas de ese mismo programa. La facultad
-- siempre es 'FIET': la asigna el backend al crear y al editar, y no llega
-- del cliente.
CREATE TABLE IF NOT EXISTS ESTUDIANTE (
    Usuario_uuid            VARCHAR(100) NOT NULL,
    codigoEstudiantil       VARCHAR(45)  NOT NULL,
    ProgramaAcademico_uuid  VARCHAR(100) NOT NULL,
    semestre                VARCHAR(10)  NOT NULL,
    facultad                VARCHAR(100) NOT NULL,
    PRIMARY KEY (Usuario_uuid),
    UNIQUE KEY uk_estudiante_codigo (codigoEstudiantil),
    CONSTRAINT fk_estudiante_usuario
        FOREIGN KEY (Usuario_uuid) REFERENCES usuarios (uuidUsuario),
    CONSTRAINT fk_estudiante_programa
        FOREIGN KEY (ProgramaAcademico_uuid) REFERENCES PROGRAMA_ACADEMICO (uuidProgramaAcademico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- FUNCIONARIO_ACADEMICO es una extensión propia de este trabajo de grado,
-- distinta del Funcionario/tabla "funcionarios" de Julián (ese es un rol
-- de comité de facultad). Esta tabla cubre solo al Funcionario Académico
-- (Técnico Administrativo de Procesos Académicos) que verifica la
-- información académica. La columna "cargo" es el cargo que ocupa (antes
-- se llamaba "dependencia"; la renombra
-- docs/database/migracion_cargo_situaciones_notas.sql); no distingue roles
-- (ver docs/database/diccionario-datos-extension.md).
CREATE TABLE IF NOT EXISTS FUNCIONARIO_ACADEMICO (
    Usuario_uuid        VARCHAR(100) NOT NULL,
    cargo               VARCHAR(100) NOT NULL,
    PRIMARY KEY (Usuario_uuid),
    CONSTRAINT fk_funcionarioacademico_usuario
        FOREIGN KEY (Usuario_uuid) REFERENCES usuarios (uuidUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Nota: el Decano no tiene fila en FUNCIONARIO_ACADEMICO ni tabla propia.
-- Es un usuario con el rol 'Decano' y cuyo uuidTipoUsuario apunta a la
-- fila de tiposUsuario con nombre = 'Maxima autoridad FIET - Decano'
-- (rol y tipo ya existen en el data.sql de Julián, no hace falta
-- insertarlos). Se identifica por ese rol.

-- ---------------------------------------------------------------------
-- 3. Asignaturas y matrícula
-- ---------------------------------------------------------------------

-- Cada asignatura pertenece a un solo programa, fijo desde su creación.
-- codigoAsignatura lo genera el backend (sigla, guion y consecutivo de al
-- menos tres dígitos) y no se edita; uk_asignatura_codigo es la última
-- defensa ante dos creaciones simultáneas. El nombre no se repite dentro
-- del mismo programa sin distinguir mayúsculas ni espacios sobrantes; esa
-- regla la valida el backend con el programa bloqueado, no hay índice.
CREATE TABLE IF NOT EXISTS ASIGNATURA (
    uuidAsignatura          VARCHAR(100) NOT NULL,
    codigoAsignatura        VARCHAR(45)  NOT NULL,
    nombreAsignatura        VARCHAR(150) NOT NULL,
    ProgramaAcademico_uuid  VARCHAR(100) NOT NULL,
    PRIMARY KEY (uuidAsignatura),
    UNIQUE KEY uk_asignatura_codigo (codigoAsignatura),
    CONSTRAINT fk_asignatura_programa
        FOREIGN KEY (ProgramaAcademico_uuid) REFERENCES PROGRAMA_ACADEMICO (uuidProgramaAcademico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ASIGNATURA_MATRICULADA (
    uuidAsignaturaMatriculada  VARCHAR(100) NOT NULL,
    Estudiante_uuid            VARCHAR(100) NOT NULL,
    Asignatura_uuid            VARCHAR(100) NOT NULL,
    grupo                      VARCHAR(20)  NOT NULL,
    estado                     VARCHAR(20)  NOT NULL DEFAULT 'activa',
    PRIMARY KEY (uuidAsignaturaMatriculada),
    CONSTRAINT fk_asigmat_estudiante
        FOREIGN KEY (Estudiante_uuid) REFERENCES ESTUDIANTE (Usuario_uuid),
    CONSTRAINT fk_asigmat_asignatura
        FOREIGN KEY (Asignatura_uuid) REFERENCES ASIGNATURA (uuidAsignatura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 4. Tipo de Solicitud Académica
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS TIPO_SOLICITUD_ACADEMICA (
    uuidTipoSolicitudAcademica  VARCHAR(100) NOT NULL,
    nombre                      VARCHAR(100) NOT NULL,
    descripcion                 VARCHAR(255) NULL,
    FuncionarioAcademico_uuid   VARCHAR(100) NOT NULL,
    PRIMARY KEY (uuidTipoSolicitudAcademica),
    CONSTRAINT fk_tiposolicitud_funcionario
        FOREIGN KEY (FuncionarioAcademico_uuid) REFERENCES FUNCIONARIO_ACADEMICO (Usuario_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 5. Máquina de etapas
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ETAPA_SOLICITUD_ACADEMICA (
    uuidEtapa                   VARCHAR(100) NOT NULL,
    codigo                      VARCHAR(60)  NOT NULL,
    TipoSolicitudAcademica_uuid VARCHAR(100) NULL,
    PRIMARY KEY (uuidEtapa),
    CONSTRAINT fk_etapa_tiposolicitud
        FOREIGN KEY (TipoSolicitudAcademica_uuid) REFERENCES TIPO_SOLICITUD_ACADEMICA (uuidTipoSolicitudAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ETAPA_ETIQUETA_ROL (
    Etapa_uuid   VARCHAR(100) NOT NULL,
    rol          ENUM('ESTUDIANTE', 'FUNCIONARIO', 'DECANO') NOT NULL,
    etiqueta     VARCHAR(60)  NOT NULL,
    PRIMARY KEY (Etapa_uuid, rol),
    CONSTRAINT fk_etiqueta_etapa
        FOREIGN KEY (Etapa_uuid) REFERENCES ETAPA_SOLICITUD_ACADEMICA (uuidEtapa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 6. Solicitud Académica y su especialización
-- ---------------------------------------------------------------------

-- radicado: identificador legible de la solicitud (AAAA-CM-0001,
-- AAAA-CA-0001 o AAAA-ES-0001: año, tipo y consecutivo por año y tipo).
-- Ver docs/database/etapas-por-proceso.md, sección 7. El uuid sigue siendo
-- la llave primaria interna.
CREATE TABLE IF NOT EXISTS SOLICITUD_ACADEMICA (
    uuidSolicitudAcademica       VARCHAR(100) NOT NULL,
    radicado                     VARCHAR(20)  NOT NULL,
    Estudiante_uuid              VARCHAR(100) NOT NULL,
    TipoSolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    fechaCreacion                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Etapa_uuid                   VARCHAR(100) NOT NULL,
    PRIMARY KEY (uuidSolicitudAcademica),
    UNIQUE KEY uk_solacad_radicado (radicado),
    CONSTRAINT fk_solacad_estudiante
        FOREIGN KEY (Estudiante_uuid) REFERENCES ESTUDIANTE (Usuario_uuid),
    CONSTRAINT fk_solacad_tiposolicitud
        FOREIGN KEY (TipoSolicitudAcademica_uuid) REFERENCES TIPO_SOLICITUD_ACADEMICA (uuidTipoSolicitudAcademica),
    CONSTRAINT fk_solacad_etapa
        FOREIGN KEY (Etapa_uuid) REFERENCES ETAPA_SOLICITUD_ACADEMICA (uuidEtapa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS SOLICITUD_CANCELACION_MATRICULA (
    SolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    motivoCancelacion        VARCHAR(255) NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_solcm_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS SOLICITUD_CANCELACION_ASIGNATURA (
    SolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    motivoCancelacion        VARCHAR(255) NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_solca_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- fechaAcordadaExamen está en desuso desde T12.28: el paso en que el
-- Funcionario aprueba el comprobante ya no pide fecha y nada la lee ni la
-- escribe. La columna se conserva (nullable) para no perder datos viejos.
-- Docente_uuid es el profesor (usuario con rol 'Docente') que el
-- Funcionario elige al aprobar el pago; queda en NULL hasta ese paso y
-- también si el pago se rechaza.
CREATE TABLE IF NOT EXISTS SOLICITUD_EXAMEN_SUPLETORIO (
    SolicitudAcademica_uuid    VARCHAR(100) NOT NULL,
    AsignaturaMatriculada_uuid VARCHAR(100) NOT NULL,
    fechaExamenNoPresentado    DATETIME     NOT NULL,
    fechaAcordadaExamen        DATETIME     NULL,
    tipoCausa                  ENUM('cruce', 'otra') NOT NULL,
    Docente_uuid               VARCHAR(100) NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_solsup_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_solsup_asigmat
        FOREIGN KEY (AsignaturaMatriculada_uuid) REFERENCES ASIGNATURA_MATRICULADA (uuidAsignaturaMatriculada),
    CONSTRAINT fk_solsup_docente
        FOREIGN KEY (Docente_uuid) REFERENCES usuarios (uuidUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS SOLICITUD_SUPLETORIO_CRUCE_ASIGNATURA (
    SolicitudAcademica_uuid           VARCHAR(100) NOT NULL,
    AsignaturaMatriculadaCruzada_uuid VARCHAR(100) NOT NULL,
    fechaExamenCruzada                DATETIME     NOT NULL,
    horaExamenCruzada                 VARCHAR(10)  NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_solcruce_solsup
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_EXAMEN_SUPLETORIO (SolicitudAcademica_uuid),
    CONSTRAINT fk_solcruce_asigmat
        FOREIGN KEY (AsignaturaMatriculadaCruzada_uuid) REFERENCES ASIGNATURA_MATRICULADA (uuidAsignaturaMatriculada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 7. Catálogo de situación académica (formato oficial PA-GA-4.2-FOR-9)
-- ---------------------------------------------------------------------

-- Desde T12.34 (P60) el catálogo tiene dos listas cerradas, separadas por
-- ambito: MATRICULA (situación de la asignatura) con R1 Cursada por primera
-- vez, R1R Recursada por primera vez, R2 Cursada por segunda vez y R3
-- Cursada por tercera vez; CANCELACION (situación al cancelar) con C1 No
-- vista y C2 Perdida. R0 No cursada se conserva con ambito NULL (retirada):
-- no se ofrece ni se acepta. Las filas de ASIGNATURA_SOLICITUD_ACADEMICA que
-- apuntaban a un valor fuera de su lista quedaron en NULL
-- (docs/database/migracion_situaciones_siglas.sql). Hibernate crea ambito
-- como ENUM desde el enum AmbitoSituacionAcademica.
CREATE TABLE IF NOT EXISTS SITUACION_ACADEMICA_ASIGNATURA (
    uuidSituacionAcademica  VARCHAR(100) NOT NULL,
    codigo                  VARCHAR(10)  NOT NULL,
    nombre                  VARCHAR(100) NOT NULL,
    ambito                  ENUM('CANCELACION', 'MATRICULA') NULL,
    PRIMARY KEY (uuidSituacionAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 8. Vinculación con asignaturas matriculadas (cancelaciones)
-- ---------------------------------------------------------------------

-- cumpleCondiciones, observacionEvaluacion, aprobadaPorDecano y
-- observacionDecision solo se usan en Cancelación de Asignatura, donde el
-- Decano decide cada asignatura por separado (decisión P14 de
-- docs/database/etapas-por-proceso.md). En Cancelación de Matrícula quedan
-- en NULL. BOOLEAN es TINYINT(1) en MySQL.
-- numeroFaltas y nota son texto de hasta 20 caracteres: si el valor es
-- numérico se valida (faltas entero >= 0; nota de 0.0 a 5.0 con un decimal,
-- con punto o coma, guardada con punto); si no, se guarda el texto tal cual.
-- observacionDecision es opcional y el Estudiante nunca la ve.
CREATE TABLE IF NOT EXISTS ASIGNATURA_SOLICITUD_ACADEMICA (
    uuidAsignaturaSolicitud     VARCHAR(100) NOT NULL,
    SolicitudAcademica_uuid     VARCHAR(100) NOT NULL,
    AsignaturaMatriculada_uuid  VARCHAR(100) NOT NULL,
    numeroFaltas                VARCHAR(20)  NULL,
    nota                        VARCHAR(20)  NULL,
    SituacionMatricula_uuid     VARCHAR(100) NULL,
    SituacionCancelar_uuid      VARCHAR(100) NULL,
    cumpleCondiciones           BOOLEAN      NULL,
    observacionEvaluacion       VARCHAR(255) NULL,
    aprobadaPorDecano           BOOLEAN      NULL,
    observacionDecision         VARCHAR(255) NULL,
    PRIMARY KEY (uuidAsignaturaSolicitud),
    CONSTRAINT fk_asigsol_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_asigsol_asigmat
        FOREIGN KEY (AsignaturaMatriculada_uuid) REFERENCES ASIGNATURA_MATRICULADA (uuidAsignaturaMatriculada),
    CONSTRAINT fk_asigsol_situmatricula
        FOREIGN KEY (SituacionMatricula_uuid) REFERENCES SITUACION_ACADEMICA_ASIGNATURA (uuidSituacionAcademica),
    CONSTRAINT fk_asigsol_situcancelar
        FOREIGN KEY (SituacionCancelar_uuid) REFERENCES SITUACION_ACADEMICA_ASIGNATURA (uuidSituacionAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 9. Anexos
-- ---------------------------------------------------------------------

-- vigente = 0 retira un tipo de anexo sin borrarlo: no se ofrece, no se exige
-- y no se puede subir, pero los anexos ya cargados con ese tipo se conservan.
-- Retirados: los seis anexos anteriores de Cancelación de Matrícula (T12.31,
-- P55) y el "Documento adicional de la respuesta" de Cancelación de
-- Asignatura (T12.33, P59). El seed los deja así con UPDATE, de modo que
-- restaurar los procesos no los vuelve a activar.
CREATE TABLE IF NOT EXISTS TIPO_ANEXO_ACADEMICO (
    uuidTipoAnexoAcademico       VARCHAR(100) NOT NULL,
    TipoSolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    nombre                       VARCHAR(150) NOT NULL,
    formatosPermitidos           VARCHAR(60)  NOT NULL,
    obligatorio                  TINYINT(1)   NOT NULL DEFAULT 1,
    vigente                      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (uuidTipoAnexoAcademico),
    CONSTRAINT fk_tipoanexo_tiposolicitud
        FOREIGN KEY (TipoSolicitudAcademica_uuid) REFERENCES TIPO_SOLICITUD_ACADEMICA (uuidTipoSolicitudAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- nombreArchivo es el nombre original que subió el usuario (solo para
-- mostrar y descargar); en disco el archivo se guarda con un nombre
-- generado por el sistema, que queda en urlArchivo. tipoArchivo es el
-- tipo de contenido deducido de la extensión validada, tamanioBytes el
-- tamaño real, Usuario_uuid quién lo subió y fechaSubida cuándo.
CREATE TABLE IF NOT EXISTS ANEXO_ACADEMICO (
    uuidAnexoAcademico       VARCHAR(100) NOT NULL,
    SolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    TipoAnexoAcademico_uuid  VARCHAR(100) NULL,
    nombreArchivo            VARCHAR(255) NOT NULL,
    urlArchivo               VARCHAR(400) NOT NULL,
    tipoArchivo              VARCHAR(100) NOT NULL,
    tamanioBytes             BIGINT       NOT NULL,
    Usuario_uuid             VARCHAR(100) NOT NULL,
    fechaSubida              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuidAnexoAcademico),
    CONSTRAINT fk_anexo_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_anexo_tipoanexo
        FOREIGN KEY (TipoAnexoAcademico_uuid) REFERENCES TIPO_ANEXO_ACADEMICO (uuidTipoAnexoAcademico),
    CONSTRAINT fk_anexo_usuario
        FOREIGN KEY (Usuario_uuid) REFERENCES usuarios (uuidUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 10. Resolución académica (escaneo del documento firmado físicamente)
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS RESOLUCION_ACADEMICA (
    SolicitudAcademica_uuid    VARCHAR(100) NOT NULL,
    urlArchivo                 VARCHAR(400) NOT NULL,
    nombreArchivo              VARCHAR(255) NOT NULL,
    fechaSubida                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FuncionarioAcademico_uuid  VARCHAR(100) NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_resolucion_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_resolucion_funcionario
        FOREIGN KEY (FuncionarioAcademico_uuid) REFERENCES FUNCIONARIO_ACADEMICO (Usuario_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 11. Historial de auditoría
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS HISTORIAL_SOLICITUD_ACADEMICA (
    uuidHistorial             VARCHAR(100) NOT NULL,
    SolicitudAcademica_uuid   VARCHAR(100) NOT NULL,
    Usuario_uuid              VARCHAR(100) NOT NULL,
    accion                    VARCHAR(150) NOT NULL,
    observaciones             VARCHAR(500) NULL,
    fecha                     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (uuidHistorial),
    CONSTRAINT fk_historial_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_historial_usuario
        FOREIGN KEY (Usuario_uuid) REFERENCES usuarios (uuidUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 12. Notificaciones al docente del examen supletorio
-- ---------------------------------------------------------------------

-- El sistema de Julián no tiene notificaciones; esta tabla es propia de la
-- extensión. Se crea una fila cuando el Funcionario aprueba el comprobante
-- de pago de un Examen Supletorio y elige al profesor, en la misma
-- transacción. titulo sale del estado final de la solicitud y su radicado
-- ("Examen supletorio aprobado, radicado 2026-ES-0001"). fechaLectura
-- queda en NULL hasta que el docente abre la notificación. Solo la ve el
-- docente destinatario.
CREATE TABLE IF NOT EXISTS NOTIFICACION_DOCENTE (
    idNotificacion            BIGINT       NOT NULL AUTO_INCREMENT,
    uuidNotificacion          VARCHAR(100) NOT NULL,
    SolicitudAcademica_uuid   VARCHAR(100) NOT NULL,
    Docente_uuid              VARCHAR(100) NOT NULL,
    titulo                    VARCHAR(255) NOT NULL,
    fechaCreacion             DATETIME     NOT NULL,
    fechaLectura              DATETIME     NULL,
    PRIMARY KEY (idNotificacion),
    CONSTRAINT uk_notifdoc_uuid UNIQUE (uuidNotificacion),
    CONSTRAINT fk_notifdoc_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_notifdoc_usuario
        FOREIGN KEY (Docente_uuid) REFERENCES usuarios (uuidUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 13. Cambios de T12.28 sobre una base que ya existía
-- ---------------------------------------------------------------------

-- Idempotentes: se pueden correr más de una vez. Agregan Docente_uuid a
-- SOLICITUD_EXAMEN_SUPLETORIO con su FK solo si falta, y dejan
-- fechaAcordadaExamen como NULL. La tabla NOTIFICACION_DOCENTE la crea el
-- CREATE TABLE IF NOT EXISTS de la sección 12. Las columnas de evaluación
-- de ASIGNATURA_SOLICITUD_ACADEMICA (numeroFaltas, nota,
-- SituacionMatricula_uuid, SituacionCancelar_uuid) ya eran NULL y no
-- cambian aunque desde T12.28 sean opcionales.
SET @falta_docente = (SELECT COUNT(*) = 0 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'SOLICITUD_EXAMEN_SUPLETORIO' AND column_name = 'Docente_uuid');
SET @sql_docente = IF(@falta_docente,
    'ALTER TABLE SOLICITUD_EXAMEN_SUPLETORIO ADD COLUMN Docente_uuid VARCHAR(100) NULL, ADD CONSTRAINT fk_solsup_docente FOREIGN KEY (Docente_uuid) REFERENCES usuarios (uuidUsuario)',
    'SELECT 1');
PREPARE cambio_docente FROM @sql_docente;
EXECUTE cambio_docente;
DEALLOCATE PREPARE cambio_docente;

ALTER TABLE SOLICITUD_EXAMEN_SUPLETORIO MODIFY fechaAcordadaExamen DATETIME NULL;

-- ---------------------------------------------------------------------
-- 14. Comunicación programada de la Resolución (T12.32)
-- ---------------------------------------------------------------------

-- Solo Cancelación de Asignatura que pasó por el Decano: una fila por
-- solicitud (1 a 0..1, PK = FK, como RESOLUCION_ACADEMICA), creada en la
-- misma transacción de funcionario/responder. fechaProgramada se interpreta
-- en hora de Colombia y debe ser futura. estado: PROGRAMADA (esperando la
-- fecha o el siguiente reintento), EN_PROCESO (la reclamó la tarea
-- programada con una actualización condicional por estado; si queda así
-- más de 15 minutos se vuelve a reclamar), ENVIADA o FALLIDA (tres intentos
-- sin éxito). firmada = 1 cuando el estudiante firmó la Resolución en la
-- Decanatura: no se le envía la comunicación y DARCA recibe solo el PDF de
-- la respuesta y la Resolución firmada. Desde T12.38 (P63) la firma cierra
-- la comunicación: se guarda con una actualización condicional (PROGRAMADA,
-- firmada = 0 y antes de la fecha) que también llena fechaLiberacion y
-- textoDarca; la tarea la reclama de inmediato aunque la fecha no haya
-- llegado (envía solo a DARCA) y después ya no se edita ni se vuelve a
-- firmar. fechaEnvioEstudiante y
-- fechaEnvioDarca son la marca de cada correo: el reintento repite solo el
-- que sigue nulo. fechaLiberacion es el momento en que la tarea reclamó la
-- fila por primera vez o en que se registró la firma. Desde T12.38 el
-- Estudiante ve la solicitud en trámite solo mientras fechaLiberacion sea
-- nula y fechaProgramada siga en el futuro; después no se vuelve a ocultar.
-- mensajeInicial guarda el texto del mensaje inmediato al estudiante; desde
-- T12.38 siempre se envía (las filas anteriores pueden tenerlo nulo).
-- textoComunicacion y textoDarca (T12.38) son los textos que editó el
-- Funcionario para el correo al estudiante y para DARCA: el primero con las
-- marcas {numero}, {anio}, {nombre} y {decision}, que se llenan al enviar
-- (en negrilla en el HTML); el segundo con {fecha}, la fecha de envío.
-- Nulos en las filas anteriores a T12.38, que usan la plantilla fija.
-- Hibernate crea estado como ENUM y firmada como BIT(1).
CREATE TABLE IF NOT EXISTS NOTIFICACION_RESOLUCION (
    SolicitudAcademica_uuid   VARCHAR(100)  NOT NULL,
    fechaProgramada           DATETIME      NOT NULL,
    estado                    ENUM('ENVIADA','EN_PROCESO','FALLIDA','PROGRAMADA') NOT NULL,
    firmada                   BIT(1)        NOT NULL DEFAULT b'0',
    numeroResolucion          VARCHAR(20)   NOT NULL,
    anioResolucion            INT           NOT NULL,
    decision                  VARCHAR(60)   NOT NULL,
    mensajeInicial            VARCHAR(2000) NULL,
    textoComunicacion         VARCHAR(4000) NULL,
    textoDarca                VARCHAR(4000) NULL,
    fechaEnvioEstudiante      DATETIME      NULL,
    fechaEnvioDarca           DATETIME      NULL,
    intentos                  INT           NOT NULL DEFAULT 0,
    ultimoError               VARCHAR(1000) NULL,
    fechaReclamo              DATETIME      NULL,
    fechaEnvio                DATETIME      NULL,
    fechaLiberacion           DATETIME      NULL,
    fechaCreacion             DATETIME      NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_notifres_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- Fin del script. Son 21 tablas nuevas en total. Orden de creación ya
-- resuelto para respetar FKs: ProgramaAcademico ->
-- Estudiante/FuncionarioAcademico ->
-- Asignatura/AsignaturaMatriculada -> TipoSolicitud -> Etapa/EtapaEtiqueta
-- -> SolicitudAcademica -> especializaciones -> SituacionAcademica ->
-- AsignaturaSolicitud -> Anexos -> Resolución -> Historial ->
-- NotificacionDocente -> NotificacionResolucion.
-- =====================================================================
