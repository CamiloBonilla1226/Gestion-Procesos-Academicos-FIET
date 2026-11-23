SET NAMES utf8mb4;

SET @tiene_texto_comunicacion = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'NOTIFICACION_RESOLUCION' AND column_name = 'textoComunicacion');

SET @sql = IF(@tiene_texto_comunicacion = 0,
    'ALTER TABLE NOTIFICACION_RESOLUCION ADD COLUMN textoComunicacion VARCHAR(4000) NULL AFTER mensajeInicial',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SET @tiene_texto_darca = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'NOTIFICACION_RESOLUCION' AND column_name = 'textoDarca');

SET @sql = IF(@tiene_texto_darca = 0,
    'ALTER TABLE NOTIFICACION_RESOLUCION ADD COLUMN textoDarca VARCHAR(4000) NULL AFTER textoComunicacion',
    'DO 0');
PREPARE sentencia FROM @sql;
EXECUTE sentencia;
DEALLOCATE PREPARE sentencia;

SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'NOTIFICACION_RESOLUCION' AND column_name IN ('textoComunicacion', 'textoDarca');
