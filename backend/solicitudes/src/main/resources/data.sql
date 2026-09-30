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

INSERT INTO Usuario_has_Roles (uuidRol, uuidUsuario) VALUES ('0f76e194-eda2-49c3-adb0-c16abcfbfa4d', 'uuid-root-0001')