

===== ARCHIVO: docs/database/script_bd_extension.sql =====

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
-- forma el código de las asignaturas del programa (ISI-001, IET-014, ...).
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

CREATE TABLE IF NOT EXISTS SOLICITUD_EXAMEN_SUPLETORIO (
    SolicitudAcademica_uuid    VARCHAR(100) NOT NULL,
    AsignaturaMatriculada_uuid VARCHAR(100) NOT NULL,
    fechaExamenNoPresentado    DATETIME     NOT NULL,
    fechaAcordadaExamen        DATETIME     NULL,
    tipoCausa                  ENUM('cruce', 'otra') NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_solsup_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_solsup_asigmat
        FOREIGN KEY (AsignaturaMatriculada_uuid) REFERENCES ASIGNATURA_MATRICULADA (uuidAsignaturaMatriculada)
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

-- Filas sembradas: R0 No cursada, R1 Cursada por primera vez, R2 Cursada
-- por segunda vez, R3 Cursada por tercera vez.
CREATE TABLE IF NOT EXISTS SITUACION_ACADEMICA_ASIGNATURA (
    uuidSituacionAcademica  VARCHAR(100) NOT NULL,
    codigo                  VARCHAR(10)  NOT NULL,
    nombre                  VARCHAR(100) NOT NULL,
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

CREATE TABLE IF NOT EXISTS TIPO_ANEXO_ACADEMICO (
    uuidTipoAnexoAcademico       VARCHAR(100) NOT NULL,
    TipoSolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    nombre                       VARCHAR(150) NOT NULL,
    formatosPermitidos           VARCHAR(60)  NOT NULL,
    obligatorio                  TINYINT(1)   NOT NULL DEFAULT 1,
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

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- Fin del script. Son 19 tablas nuevas en total. Orden de creación ya
-- resuelto para respetar FKs: ProgramaAcademico ->
-- Estudiante/FuncionarioAcademico ->
-- Asignatura/AsignaturaMatriculada -> TipoSolicitud -> Etapa/EtapaEtiqueta
-- -> SolicitudAcademica -> especializaciones -> SituacionAcademica ->
-- AsignaturaSolicitud -> Anexos -> Resolución -> Historial.
-- =====================================================================



===== ARCHIVO: docs/database/seed-procesos-academicos.sql =====

-- Uso: con el cliente mysql en utf8mb4 sobre la base cfiet, asignar primero el
-- uuid del Funcionario Académico a cargo de los tres procesos y luego cargar
-- este archivo. Se puede correr más de una vez (INSERT IGNORE con UUID fijos).
--   SET @funcionario_uuid = '<Usuario_uuid de FUNCIONARIO_ACADEMICO>';
--   source docs/database/seed-procesos-academicos.sql
-- Con Docker, desde backend/ (PowerShell):
--   docker compose cp ../docs/database/seed-procesos-academicos.sql cfiet_database:/tmp/seed.sql
--   docker compose exec -T cfiet_database mysql -u root -p<REDACTADO> --default-character-set=utf8mb4 cfiet -e "SET @funcionario_uuid='<uuid>'; source /tmp/seed.sql"

INSERT IGNORE INTO PROGRAMA_ACADEMICO (uuidProgramaAcademico, nombre, sigla, ultimoConsecutivoAsignatura) VALUES
('14299e0f-8da8-4c73-ad2b-b61e0c52b7f5', 'Ingeniería Electrónica y Telecomunicaciones', 'IET', 0),
('5d73d656-2504-44cc-ab6e-5cd10b500abf', 'Ingeniería de Sistemas', 'ISI', 0),
('8606cf01-c111-4aef-92cd-a111cb7b7a38', 'Ingeniería en Automática Industrial', 'IAI', 0),
('7ebedec6-aeab-403b-8a97-9312061bd4b7', 'Tecnología en Telemática', 'TTE', 0),
('d21ec0bf-cee1-4a06-8ae7-00477e88cfe6', 'Ingeniería en Inteligencia Artificial y Ciencia de Datos', 'IAC', 0);

INSERT IGNORE INTO TIPO_SOLICITUD_ACADEMICA (uuidTipoSolicitudAcademica, nombre, descripcion, FuncionarioAcademico_uuid) VALUES
('3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Cancelación de Matrícula', 'Cancelación de todas las asignaturas matriculadas en el periodo académico', @funcionario_uuid),
('7b3d4347-3525-4ab7-a2b4-723b1ced80c8', 'Cancelación de Asignatura', 'Cancelación de una o más asignaturas matriculadas en el periodo académico', @funcionario_uuid),
('459cde47-f3a3-4a74-9b44-1063a516326c', 'Examen Supletorio', 'Solicitud de examen supletorio por un examen no presentado', @funcionario_uuid);

INSERT IGNORE INTO ETAPA_SOLICITUD_ACADEMICA (uuidEtapa, codigo, TipoSolicitudAcademica_uuid) VALUES
('653e4772-562b-4d3c-becf-85f19a6dbdc9', 'RADICADA', NULL),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'EN_REVISION_DECANO', NULL),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'APROBADA_POR_DECANO', NULL),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'RECHAZADA_POR_DECANO', NULL),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'APROBADA', NULL),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'RECHAZADA', NULL),
('6e4561a7-b354-4f21-9825-f35263882b36', 'PENDIENTE_PAGO', '459cde47-f3a3-4a74-9b44-1063a516326c'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'EN_VERIFICACION_PAGO', '459cde47-f3a3-4a74-9b44-1063a516326c');

INSERT IGNORE INTO ETAPA_ETIQUETA_ROL (Etapa_uuid, rol, etiqueta) VALUES
('653e4772-562b-4d3c-becf-85f19a6dbdc9', 'ESTUDIANTE', 'En trámite'),
('653e4772-562b-4d3c-becf-85f19a6dbdc9', 'FUNCIONARIO', 'Pendiente'),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'ESTUDIANTE', 'En trámite'),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'FUNCIONARIO', 'En Gestión'),
('28ee2417-f100-4972-82d3-a4d267a1a10f', 'DECANO', 'Pendiente'),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'ESTUDIANTE', 'En trámite'),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'FUNCIONARIO', 'Pendiente Notificar'),
('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'DECANO', 'Respondida'),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'ESTUDIANTE', 'En trámite'),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'FUNCIONARIO', 'Pendiente Notificar'),
('fa3c502f-dc81-46eb-bace-88deb194564b', 'DECANO', 'Respondida'),
('6e4561a7-b354-4f21-9825-f35263882b36', 'ESTUDIANTE', 'Pendiente de pago'),
('6e4561a7-b354-4f21-9825-f35263882b36', 'FUNCIONARIO', 'En Gestión'),
('6e4561a7-b354-4f21-9825-f35263882b36', 'DECANO', 'Respondida'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'ESTUDIANTE', 'En verificación de pago'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'FUNCIONARIO', 'Recibo Pagado'),
('ac289652-cf4e-4142-bec4-8809cbcb74fc', 'DECANO', 'Respondida'),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'ESTUDIANTE', 'Aprobada'),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'FUNCIONARIO', 'Respondida'),
('6e6b1b7e-f7db-4fb8-8c4a-3d67c18dbe5a', 'DECANO', 'Respondida'),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'ESTUDIANTE', 'Rechazada'),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'FUNCIONARIO', 'Respondida'),
('475e51b3-f854-4b4e-818e-a792ef59f64c', 'DECANO', 'Respondida');

INSERT IGNORE INTO TIPO_ANEXO_ACADEMICO (uuidTipoAnexoAcademico, TipoSolicitudAcademica_uuid, nombre, formatosPermitidos, obligatorio) VALUES
('0870fd29-220d-4226-92b1-590804ac0974', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División de Bibliotecas', 'pdf', 1),
('fc630c85-6dff-4def-a072-ea26bd942d6e', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División de Deportes y Recreación', 'pdf', 1),
('7b26091a-d12e-48d2-85b1-10a578ef198e', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División de Salud Integral', 'pdf', 1),
('6898eb98-f150-4784-8255-0eab0eb61edd', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Cupón de Confirmación de la Intervención Psicosocial - División de Salud Integral', 'pdf', 1),
('c756365a-f0c4-4e9a-9326-2946aeacd638', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Paz y salvo - División Financiera', 'pdf', 1),
('22ca9f7d-967d-44ba-87fa-103aa63b297f', '3b6dccc9-ff78-48ef-8e66-05fb9932e302', 'Carné estudiantil o constancia de no trámite - DARCA', 'pdf,jpg,jpeg,png', 1),
('ee145c67-0f6e-4971-84b7-42679747ed49', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Formato firmado por el docente que orienta la asignatura', 'pdf,jpg,png', 1),
('2bfc61be-7787-4772-8b86-ca64488f2578', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Soporte de la justificación de la no presentación', 'pdf,jpg,png', 0),
('3cba6336-4de6-492b-838d-602f28a9efbe', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Formato firmado por el docente de la asignatura con la que se cruza', 'pdf,jpg,png', 0),
('422163e2-81b8-48f8-a054-2c40033de17c', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Recibo de pago', 'pdf', 0),
('8408772b-8b61-472e-ac44-a83923dea147', '459cde47-f3a3-4a74-9b44-1063a516326c', 'Comprobante de pago', 'pdf,jpg,png', 0);

INSERT IGNORE INTO SITUACION_ACADEMICA_ASIGNATURA (uuidSituacionAcademica, codigo, nombre) VALUES
('6eec86b1-53da-4c65-baa9-96bf67669c13', 'R0', 'No cursada'),
('62aa0705-f878-408e-8111-78691c1a871a', 'R1', 'Cursada por primera vez'),
('fa14cee3-6e4f-4178-955c-ba73312e08cb', 'R2', 'Cursada por segunda vez'),
('a6077d43-d5fc-4e8d-9424-18c549515c24', 'R3', 'Cursada por tercera vez');

UPDATE ETAPA_ETIQUETA_ROL SET etiqueta = 'Pendiente Notificar' WHERE rol = 'FUNCIONARIO' AND Etapa_uuid IN ('1dfc0089-f1b0-486d-bfc5-2025081d800f', 'fa3c502f-dc81-46eb-bace-88deb194564b');
UPDATE ETAPA_ETIQUETA_ROL SET etiqueta = 'Recibo Pagado' WHERE rol = 'FUNCIONARIO' AND Etapa_uuid = 'ac289652-cf4e-4142-bec4-8809cbcb74fc';
UPDATE TIPO_ANEXO_ACADEMICO SET nombre = 'Formato firmado por el docente que orienta la asignatura' WHERE uuidTipoAnexoAcademico = 'ee145c67-0f6e-4971-84b7-42679747ed49';



===== ARCHIVO: docs/database/seed-roles-extension.sql =====

INSERT IGNORE INTO roles (uuidRol, nombre, descripcion, estado) VALUES
('b77e970f-9c82-4c4b-9570-fe79840245a7', 'Estudiante', 'Estudiante de la FIET', true),
('996f4a2d-e051-4367-a702-a3e6a0a91873', 'Funcionario Académico', 'Técnico Administrativo de Procesos Académicos de la FIET', true);

INSERT IGNORE INTO tiposUsuario (uuidTipoUsuario, nombre) VALUES
('5725e5e5-aa04-494c-b347-be14f619fd97', 'Estudiante');



===== ARCHIVO: docs/database/migracion_cargo_situaciones_notas.sql =====

SET NAMES utf8mb4;

SET @tiene_dependencia = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'FUNCIONARIO_ACADEMICO' AND column_name = 'dependencia');
SET @tiene_cargo = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'FUNCIONARIO_ACADEMICO' AND column_name = 'cargo');

SET @sql = IF(@tiene_dependencia = 1 AND @tiene_cargo = 0,
    'ALTER TABLE FUNCIONARIO_ACADEMICO RENAME COLUMN dependencia TO cargo',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = IF(@tiene_dependencia = 1 AND @tiene_cargo = 1,
    'UPDATE FUNCIONARIO_ACADEMICO SET cargo = dependencia WHERE cargo IS NULL OR TRIM(cargo) = ''''',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = IF(@tiene_dependencia = 1 AND @tiene_cargo = 1,
    'ALTER TABLE FUNCIONARIO_ACADEMICO DROP COLUMN dependencia',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

ALTER TABLE FUNCIONARIO_ACADEMICO MODIFY cargo VARCHAR(100) NOT NULL;

UPDATE ESTUDIANTE SET facultad = 'FIET' WHERE facultad IS NULL OR facultad <> 'FIET';

ALTER TABLE ASIGNATURA_SOLICITUD_ACADEMICA
    MODIFY numeroFaltas VARCHAR(20) NULL,
    MODIFY nota VARCHAR(20) NULL;

UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'No cursada' WHERE codigo = 'R0';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por primera vez' WHERE codigo = 'R1';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por segunda vez' WHERE codigo = 'R2';
UPDATE SITUACION_ACADEMICA_ASIGNATURA SET nombre = 'Cursada por tercera vez' WHERE codigo = 'R3';

UPDATE ETAPA_ETIQUETA_ROL r
JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid
SET r.etiqueta = 'Aprobada por el Decano'
WHERE r.rol = 'FUNCIONARIO' AND e.codigo = 'APROBADA_POR_DECANO';

UPDATE ETAPA_ETIQUETA_ROL r
JOIN ETAPA_SOLICITUD_ACADEMICA e ON e.uuidEtapa = r.Etapa_uuid
SET r.etiqueta = 'Rechazada por el Decano'
WHERE r.rol = 'FUNCIONARIO' AND e.codigo = 'RECHAZADA_POR_DECANO';



===== ARCHIVO: docs/database/migracion_programas_asignaturas.sql =====

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS PROGRAMA_ACADEMICO (
    uuidProgramaAcademico        VARCHAR(100) NOT NULL,
    nombre                       VARCHAR(100) NOT NULL,
    sigla                        VARCHAR(10)  NOT NULL,
    ultimoConsecutivoAsignatura  INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (uuidProgramaAcademico),
    UNIQUE KEY uk_programa_nombre (nombre),
    UNIQUE KEY uk_programa_sigla (sigla)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO PROGRAMA_ACADEMICO (uuidProgramaAcademico, nombre, sigla, ultimoConsecutivoAsignatura) VALUES
('14299e0f-8da8-4c73-ad2b-b61e0c52b7f5', 'Ingeniería Electrónica y Telecomunicaciones', 'IET', 0),
('5d73d656-2504-44cc-ab6e-5cd10b500abf', 'Ingeniería de Sistemas', 'ISI', 0),
('8606cf01-c111-4aef-92cd-a111cb7b7a38', 'Ingeniería en Automática Industrial', 'IAI', 0),
('7ebedec6-aeab-403b-8a97-9312061bd4b7', 'Tecnología en Telemática', 'TTE', 0),
('d21ec0bf-cee1-4a06-8ae7-00477e88cfe6', 'Ingeniería en Inteligencia Artificial y Ciencia de Datos', 'IAC', 0);

SET @isi = '5d73d656-2504-44cc-ab6e-5cd10b500abf';

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ESTUDIANTE ADD COLUMN ProgramaAcademico_uuid VARCHAR(100) NULL',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND column_name = 'ProgramaAcademico_uuid');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = (SELECT IF(COUNT(*) = 1,
    'UPDATE ESTUDIANTE e LEFT JOIN PROGRAMA_ACADEMICO p ON p.nombre = TRIM(e.programaAcademico) SET e.ProgramaAcademico_uuid = COALESCE(p.uuidProgramaAcademico, @isi) WHERE e.ProgramaAcademico_uuid IS NULL OR e.ProgramaAcademico_uuid NOT IN (SELECT uuidProgramaAcademico FROM PROGRAMA_ACADEMICO)',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND column_name = 'programaAcademico');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

UPDATE ESTUDIANTE SET ProgramaAcademico_uuid = @isi
WHERE ProgramaAcademico_uuid IS NULL
   OR ProgramaAcademico_uuid NOT IN (SELECT uuidProgramaAcademico FROM PROGRAMA_ACADEMICO);

ALTER TABLE ESTUDIANTE MODIFY ProgramaAcademico_uuid VARCHAR(100) NOT NULL;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ESTUDIANTE ADD CONSTRAINT fk_estudiante_programa FOREIGN KEY (ProgramaAcademico_uuid) REFERENCES PROGRAMA_ACADEMICO (uuidProgramaAcademico)',
    'DO 0')
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND constraint_name = 'fk_estudiante_programa');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = (SELECT IF(COUNT(*) = 1,
    'ALTER TABLE ESTUDIANTE DROP COLUMN programaAcademico',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ESTUDIANTE' AND column_name = 'programaAcademico');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ASIGNATURA ADD COLUMN ProgramaAcademico_uuid VARCHAR(100) NULL',
    'DO 0')
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'ASIGNATURA' AND column_name = 'ProgramaAcademico_uuid');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

DROP TEMPORARY TABLE IF EXISTS migracion_asignatura;

CREATE TEMPORARY TABLE migracion_asignatura AS
SELECT uuidAsignatura,
       ROW_NUMBER() OVER (ORDER BY codigoAsignatura, uuidAsignatura) AS posicion
FROM ASIGNATURA
WHERE ProgramaAcademico_uuid IS NULL
   OR ProgramaAcademico_uuid NOT IN (SELECT uuidProgramaAcademico FROM PROGRAMA_ACADEMICO);

SET @base = (SELECT CAST(GREATEST(
    (SELECT ultimoConsecutivoAsignatura FROM PROGRAMA_ACADEMICO WHERE uuidProgramaAcademico = @isi),
    COALESCE((SELECT MAX(CAST(SUBSTRING(codigoAsignatura, 5) AS UNSIGNED)) FROM ASIGNATURA
              WHERE ProgramaAcademico_uuid = @isi AND codigoAsignatura REGEXP '^ISI-[0-9]+$'), 0)) AS UNSIGNED));

UPDATE ASIGNATURA a JOIN migracion_asignatura m ON m.uuidAsignatura = a.uuidAsignatura
SET a.codigoAsignatura = CONCAT('MIG-', a.uuidAsignatura);

UPDATE ASIGNATURA a JOIN migracion_asignatura m ON m.uuidAsignatura = a.uuidAsignatura
SET a.ProgramaAcademico_uuid = @isi,
    a.codigoAsignatura = CONCAT('ISI-', LPAD(CAST(m.posicion + @base AS CHAR), GREATEST(3, LENGTH(CAST(m.posicion + @base AS CHAR))), '0'));

DROP TEMPORARY TABLE IF EXISTS migracion_asignatura;

ALTER TABLE ASIGNATURA MODIFY ProgramaAcademico_uuid VARCHAR(100) NOT NULL;

SET @sql = (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE ASIGNATURA ADD CONSTRAINT fk_asignatura_programa FOREIGN KEY (ProgramaAcademico_uuid) REFERENCES PROGRAMA_ACADEMICO (uuidProgramaAcademico)',
    'DO 0')
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'ASIGNATURA' AND constraint_name = 'fk_asignatura_programa');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

UPDATE PROGRAMA_ACADEMICO p
SET p.ultimoConsecutivoAsignatura = GREATEST(p.ultimoConsecutivoAsignatura, COALESCE((
    SELECT MAX(CAST(SUBSTRING(a.codigoAsignatura, LENGTH(p.sigla) + 2) AS UNSIGNED))
    FROM ASIGNATURA a
    WHERE a.ProgramaAcademico_uuid = p.uuidProgramaAcademico
      AND a.codigoAsignatura REGEXP CONCAT('^', p.sigla, '-[0-9]+$')), 0));



===== ARCHIVO: backend/solicitudes/src/main/resources/data.sql =====

INSERT INTO roles (uuidRol, nombre, descripcion, estado) VALUES
('0f76e194-eda2-49c3-adb0-c16abcfbfa4d', 'Secretario General', 'Administrador del sistema', true),
('48e3a5ed-709a-4dcb-9434-3bcb43ef9413', 'Funcionario', 'Empleado de la FIET', true),
('e7051231-8c01-4fb9-a301-9d5760663f40', 'Secretaria Decanatura FIET', 'Secretaria de la Decanatura de la FIET', true),
('11a1536f-215a-40bf-b1e6-86097a6887e9', 'Coordinador Pregrado', 'Coordinador de Pregrado', true),
('ba7ba649-d691-467d-b496-159a9ad4aef5', 'Coordinador Posgrados', 'Coordinador de Posgrados', true),
('f7b8052e-776b-49f3-9d92-7d178e0aaef1', 'Jefe de Departamento', 'Jefe de Departamento', true),
('0f9f5523-56c7-4e50-a0ca-3eae4025f700', 'Solicitante Publico', 'Solicitante Publico', true),
('1b08e82e-4617-4cf5-96b6-7ace7ed1bfbd', 'Decano', 'Autoridad maxima de la decanatura de la FIET', true),
('1b08e82e-4617-4cf5-9646-7ace7ed1bfbd', 'Docente', 'Docente FIET', true);

INSERT INTO tiposUsuario (uuidTipoUsuario, nombre) VALUES
('cf4858d4-77b4-4e70-ac47-bc650a34584b', 'Empleado FIET - Secretario General'),
('79105584-1091-4a4e-ba8e-9cbdd1c85b91', 'Empleado FIET - Funcionario'),
('9b25c043-9ade-40a1-9a76-4b8346525e83', 'Empleado FIET - Secretaria Decanatura FIET'),
('b3855d44-29fc-45d5-9221-b2d41734abc1', 'Coordinador'),
('e8c9848b-d494-4e50-bf0b-2c22ed1faa3f', 'Jefe de Departamento'),
('0955dbf8-f180-44a4-8137-04ff338e6427', 'Docente'),
('9d4ddd15-925e-444e-a60e-4b0f0bdba16a', 'Maxima autoridad FIET - Decano');

ALTER TABLE usuariosLivianos
MODIFY fechaCreacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

INSERT INTO usuariosLivianos (uuidUsuario, nombres, apellidos, estado)
VALUES ('uuid-root-0001', 'root', 'root', TRUE);

INSERT INTO usuarios (
  uuidUsuario, tipoDocumento, numeroDocumento, telefono, correoElectronico, username, password, uuidTipoUsuario
) VALUES (
  'uuid-root-0001',
  'Cédula de ciudadanía',
  '0000000000',
  '+5700000000',
  'root@unicauca.edu.co',
  'rootfiet',
  '$2a$12$fRuV1RR6f0Y/iku2NsfAGeVLnZGuvSYeXX8htnDav2qa4TRxxrTSG',
  'cf4858d4-77b4-4e70-ac47-bc650a34584b'
);

INSERT INTO Usuario_has_Roles (uuidRol, uuidUsuario) VALUES ('0f76e194-eda2-49c3-adb0-c16abcfbfa4d', 'uuid-root-0001');

INSERT INTO roles (uuidRol, nombre, descripcion, estado) VALUES
('b77e970f-9c82-4c4b-9570-fe79840245a7', 'Estudiante', 'Estudiante de la FIET', true),
('996f4a2d-e051-4367-a702-a3e6a0a91873', 'Funcionario Académico', 'Técnico Administrativo de Procesos Académicos de la FIET', true);

INSERT INTO tiposUsuario (uuidTipoUsuario, nombre) VALUES
('5725e5e5-aa04-494c-b347-be14f619fd97', 'Estudiante');
