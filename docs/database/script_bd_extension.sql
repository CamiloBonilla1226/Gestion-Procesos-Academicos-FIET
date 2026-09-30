-- =====================================================================
-- Script de creación — Extensión sobre la base de datos de Julián Camacho
-- Universidad del Cauca — FIET — Solicitudes Académicas
-- =====================================================================
-- IMPORTANTE: este script asume que la base de datos de Julián Camacho
-- YA EXISTE y contiene, sin modificarlas, al menos estas tablas:
--   USUARIO_LIVIANO (uuidUsuario PK, nombres, apellidos, estado)
--   USUARIO         (UsuarioLiviano_uuid PK/FK -> USUARIO_LIVIANO, tipoDocumento,
--                     numeroDocumento, telefono, correoElectronico, estado,
--                     TipoUsuario_uuid FK, username, password)
--   TIPO_USUARIO    (uuidTipoUsuario PK, nombre)
-- Si en tu base física estos nombres de tabla/columna difieren, ajusta
-- las líneas marcadas con "AJUSTAR" antes de ejecutar.
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. Usuarios especializados
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ESTUDIANTE (
    Usuario_uuid        VARCHAR(100) NOT NULL,
    codigoEstudiantil   VARCHAR(45)  NOT NULL,
    programaAcademico   VARCHAR(100) NOT NULL,
    semestre            VARCHAR(10)  NOT NULL,
    facultad            VARCHAR(100) NOT NULL,
    PRIMARY KEY (Usuario_uuid),
    CONSTRAINT fk_estudiante_usuario
        FOREIGN KEY (Usuario_uuid) REFERENCES USUARIO (UsuarioLiviano_uuid) -- AJUSTAR si tu PK de USUARIO se llama distinto
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS FUNCIONARIO_ACADEMICO (
    Usuario_uuid        VARCHAR(100) NOT NULL,
    PRIMARY KEY (Usuario_uuid),
    CONSTRAINT fk_funcionarioacademico_usuario
        FOREIGN KEY (Usuario_uuid) REFERENCES USUARIO (UsuarioLiviano_uuid) -- AJUSTAR
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Nota: el Decano NO tiene tabla propia. Es un USUARIO cuyo TipoUsuario_uuid
-- apunta a una fila de TIPO_USUARIO (de Julián) con nombre = 'Decano'.
-- Inserta esa fila de catálogo si aún no existe:
-- INSERT INTO TIPO_USUARIO (uuidTipoUsuario, nombre) VALUES (UUID(), 'Decano');

-- ---------------------------------------------------------------------
-- 2. Asignaturas y matrícula
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ASIGNATURA (
    uuidAsignatura      VARCHAR(100) NOT NULL,
    codigoAsignatura    VARCHAR(45)  NOT NULL,
    nombreAsignatura    VARCHAR(150) NOT NULL,
    PRIMARY KEY (uuidAsignatura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ASIGNATURA_MATRICULADA (
    uuidAsignaturaMatriculada  VARCHAR(100) NOT NULL,
    Estudiante_uuid            VARCHAR(100) NOT NULL,
    Asignatura_uuid            VARCHAR(100) NOT NULL,
    grupo                      VARCHAR(20)  NOT NULL,
    estado                     TINYINT(1)   NOT NULL DEFAULT 1, -- 1 = activa, 0 = cancelada
    PRIMARY KEY (uuidAsignaturaMatriculada),
    CONSTRAINT fk_asigmat_estudiante
        FOREIGN KEY (Estudiante_uuid) REFERENCES ESTUDIANTE (Usuario_uuid),
    CONSTRAINT fk_asigmat_asignatura
        FOREIGN KEY (Asignatura_uuid) REFERENCES ASIGNATURA (uuidAsignatura)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 3. Tipo de Solicitud Académica
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
-- 4. Máquina de etapas
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ETAPA_SOLICITUD_ACADEMICA (
    uuidEtapa                   VARCHAR(100) NOT NULL,
    codigo                      VARCHAR(60)  NOT NULL,
    TipoSolicitudAcademica_uuid VARCHAR(100) NULL, -- NULO = etapa universal (aplica a los 3 procesos)
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
-- 5. Solicitud Académica y su especialización
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SOLICITUD_ACADEMICA (
    uuidSolicitudAcademica       VARCHAR(100) NOT NULL,
    Estudiante_uuid              VARCHAR(100) NOT NULL,
    TipoSolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    fechaCreacion                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Etapa_uuid                   VARCHAR(100) NOT NULL,
    PRIMARY KEY (uuidSolicitudAcademica),
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
    AsignaturaMatriculada_uuid VARCHAR(100) NOT NULL, -- la asignatura no presentada
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
-- 6. Vinculación con asignaturas matriculadas (cancelaciones)
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ASIGNATURA_SOLICITUD_ACADEMICA (
    uuidAsignaturaSolicitud     VARCHAR(100) NOT NULL,
    SolicitudAcademica_uuid     VARCHAR(100) NOT NULL,
    AsignaturaMatriculada_uuid  VARCHAR(100) NOT NULL,
    numeroFaltas                INT          NULL,
    nota                        DECIMAL(3,1) NULL,
    situacionMatricula          VARCHAR(60)  NULL,
    situacionCancelar           VARCHAR(60)  NULL,
    PRIMARY KEY (uuidAsignaturaSolicitud),
    CONSTRAINT fk_asigsol_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_asigsol_asigmat
        FOREIGN KEY (AsignaturaMatriculada_uuid) REFERENCES ASIGNATURA_MATRICULADA (uuidAsignaturaMatriculada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 7. Anexos
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS TIPO_ANEXO_ACADEMICO (
    uuidTipoAnexoAcademico       VARCHAR(100) NOT NULL,
    TipoSolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    nombre                       VARCHAR(150) NOT NULL,
    formatosPermitidos           VARCHAR(60)  NOT NULL, -- ej. 'pdf,jpg,png'
    obligatorio                  TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (uuidTipoAnexoAcademico),
    CONSTRAINT fk_tipoanexo_tiposolicitud
        FOREIGN KEY (TipoSolicitudAcademica_uuid) REFERENCES TIPO_SOLICITUD_ACADEMICA (uuidTipoSolicitudAcademica)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ANEXO_ACADEMICO (
    uuidAnexoAcademico       VARCHAR(100) NOT NULL,
    SolicitudAcademica_uuid  VARCHAR(100) NOT NULL,
    TipoAnexoAcademico_uuid  VARCHAR(100) NULL, -- NULO = soporte libre, sin requisito fijo
    nombreArchivo             VARCHAR(255) NOT NULL,
    urlArchivo                VARCHAR(400) NOT NULL,
    PRIMARY KEY (uuidAnexoAcademico),
    CONSTRAINT fk_anexo_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_anexo_tipoanexo
        FOREIGN KEY (TipoAnexoAcademico_uuid) REFERENCES TIPO_ANEXO_ACADEMICO (uuidTipoAnexoAcademico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 8. Resolución académica (escaneo del documento firmado físicamente)
-- ---------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS RESOLUCION_ACADEMICA (
    SolicitudAcademica_uuid    VARCHAR(100) NOT NULL, -- solo Cancelación de Matrícula y de Asignatura
    urlArchivo                 VARCHAR(400) NOT NULL,
    nombreArchivo               VARCHAR(255) NOT NULL,
    fechaSubida                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FuncionarioAcademico_uuid   VARCHAR(100) NOT NULL,
    PRIMARY KEY (SolicitudAcademica_uuid),
    CONSTRAINT fk_resolucion_solacad
        FOREIGN KEY (SolicitudAcademica_uuid) REFERENCES SOLICITUD_ACADEMICA (uuidSolicitudAcademica),
    CONSTRAINT fk_resolucion_funcionario
        FOREIGN KEY (FuncionarioAcademico_uuid) REFERENCES FUNCIONARIO_ACADEMICO (Usuario_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 9. Historial de auditoría
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
        FOREIGN KEY (Usuario_uuid) REFERENCES USUARIO (UsuarioLiviano_uuid) -- AJUSTAR si aplica
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- Fin del script. Orden de creación ya resuelto para respetar FKs:
-- Estudiante/Funcionario -> Asignatura/AsignaturaMatriculada ->
-- TipoSolicitud -> Etapa/EtapaEtiqueta -> SolicitudAcademica ->
-- especializaciones -> AsignaturaSolicitud -> Anexos -> Resolución ->
-- Historial.
-- =====================================================================
