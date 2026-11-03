-- =============================================================================
-- ESQUEMA CONSOLIDADO DE BASE DE DATOS MYSQL 8 (InnoDB / utf8mb4)
-- Sistema de Inscripciones ITSUR (Nuevo Ingreso)
-- =============================================================================

CREATE DATABASE IF NOT EXISTS bd_inscripciones_itsur
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bd_inscripciones_itsur;

-- 1. Catálogo de Carreras ITSUR
CREATE TABLE IF NOT EXISTS carreras (
  id_carrera INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre_carrera VARCHAR(120) NOT NULL UNIQUE,
  activa BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

-- 2. Catálogo Geográfico: Estados
CREATE TABLE IF NOT EXISTS estados (
  id_estado SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 3. Catálogo Geográfico: Municipios
CREATE TABLE IF NOT EXISTS municipios (
  id_municipio SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_estado SMALLINT UNSIGNED NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  UNIQUE KEY uq_municipio (id_estado, nombre),
  CONSTRAINT fk_mun_estado FOREIGN KEY (id_estado) REFERENCES estados(id_estado)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 4. Usuarios (Unificado para roles 'aspirante' y 'escolar')
CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
  correo VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  rol ENUM('aspirante', 'escolar') NOT NULL DEFAULT 'aspirante',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,
  bloqueado_hasta DATETIME NULL,
  ultimo_acceso DATETIME NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 5. Aspirantes
CREATE TABLE IF NOT EXISTS aspirantes (
  id_aspirante BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario BIGINT UNSIGNED NOT NULL UNIQUE,
  folio VARCHAR(25) NOT NULL UNIQUE,
  curp CHAR(18) NOT NULL UNIQUE,
  telefono VARCHAR(15) NULL,
  direccion VARCHAR(255) NULL,
  id_estado SMALLINT UNSIGNED NULL,
  id_municipio SMALLINT UNSIGNED NULL,
  id_carrera INT UNSIGNED NOT NULL,
  promedio_bachillerato DECIMAL(4,2) NULL,
  estado_inscripcion ENUM(
    'en_captura',
    'en_revision',
    'con_observaciones',
    'documentacion_aprobada',
    'rechazado'
  ) NOT NULL DEFAULT 'en_captura',
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_asp_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_asp_estado FOREIGN KEY (id_estado) REFERENCES estados(id_estado)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_asp_municipio FOREIGN KEY (id_municipio) REFERENCES municipios(id_municipio)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_asp_carrera FOREIGN KEY (id_carrera) REFERENCES carreras(id_carrera)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_asp_promedio CHECK (promedio_bachillerato IS NULL OR promedio_bachillerato BETWEEN 0.00 AND 10.00)
) ENGINE=InnoDB;

-- 6. Catálogo de Tipos de Documento
CREATE TABLE IF NOT EXISTS tipos_documento (
  id_tipo_documento INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(40) NOT NULL UNIQUE,
  nombre_documento VARCHAR(100) NOT NULL,
  descripcion VARCHAR(255) NULL,
  obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
  formatos_permitidos VARCHAR(100) NOT NULL DEFAULT 'application/pdf',
  max_bytes INT UNSIGNED NOT NULL DEFAULT 5242880,
  orden TINYINT UNSIGNED NOT NULL DEFAULT 0
) ENGINE=InnoDB;

-- 7. Documentos y Archivos Multimedia
CREATE TABLE IF NOT EXISTS documentos (
  id_documento BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_aspirante BIGINT UNSIGNED NOT NULL,
  id_tipo_documento INT UNSIGNED NOT NULL,
  nombre_original VARCHAR(255) NOT NULL,
  ruta_archivo VARCHAR(500) NOT NULL,
  tipo_mime VARCHAR(80) NOT NULL DEFAULT 'application/pdf',
  tamano_bytes BIGINT UNSIGNED NOT NULL,
  hash_sha256 CHAR(64) NOT NULL,
  tamano_original_bytes BIGINT UNSIGNED NULL,
  comprimido BOOLEAN NOT NULL DEFAULT FALSE,
  num_paginas SMALLINT UNSIGNED NULL,
  version INT UNSIGNED NOT NULL DEFAULT 1,
  estado ENUM(
    'pendiente',
    'aprobado',
    'rechazado',
    'presencial',
    'requiere_correccion'
  ) NOT NULL DEFAULT 'pendiente',
  fecha_subida DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_doc_version (id_aspirante, id_tipo_documento, version),
  KEY ix_doc_hash (hash_sha256),
  CONSTRAINT fk_doc_aspirante FOREIGN KEY (id_aspirante) REFERENCES aspirantes(id_aspirante)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_doc_tipo FOREIGN KEY (id_tipo_documento) REFERENCES tipos_documento(id_tipo_documento)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 8. Revisiones de Control Escolar
CREATE TABLE IF NOT EXISTS revisiones (
  id_revision BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_documento BIGINT UNSIGNED NOT NULL,
  id_revisor BIGINT UNSIGNED NOT NULL,
  resultado ENUM('aprobado', 'rechazado', 'presencial', 'requiere_correccion') NOT NULL,
  observaciones TEXT NULL,
  fecha_revision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rev_documento FOREIGN KEY (id_documento) REFERENCES documentos(id_documento)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_rev_revisor FOREIGN KEY (id_revisor) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 9. Fechas Importantes del Proceso
CREATE TABLE IF NOT EXISTS fechas_importantes (
  id_fecha INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(150) NOT NULL,
  descripcion TEXT NULL,
  tipo ENUM('limite_entrega', 'correcciones', 'revision', 'publicacion', 'otro') NOT NULL,
  fecha_limite DATETIME NOT NULL,
  id_carrera INT UNSIGNED NULL,
  activa BOOLEAN NOT NULL DEFAULT TRUE,
  creada_por BIGINT UNSIGNED NOT NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_fecha_limite (activa, fecha_limite),
  CONSTRAINT fk_fecha_carrera FOREIGN KEY (id_carrera) REFERENCES carreras(id_carrera)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_fecha_usuario FOREIGN KEY (creada_por) REFERENCES usuarios(id_usuario)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 10. Notificaciones de Usuario
CREATE TABLE IF NOT EXISTS notificaciones (
  id_notificacion BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario BIGINT UNSIGNED NOT NULL,
  id_revision BIGINT UNSIGNED NULL,
  tipo ENUM('documento', 'fecha', 'pago', 'sistema') NOT NULL DEFAULT 'sistema',
  id_documento BIGINT UNSIGNED NULL,
  id_fecha INT UNSIGNED NULL,
  titulo VARCHAR(150) NOT NULL,
  mensaje TEXT NOT NULL,
  leida BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_lectura DATETIME NULL,
  CONSTRAINT fk_not_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_not_revision FOREIGN KEY (id_revision) REFERENCES revisiones(id_revision)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_not_documento FOREIGN KEY (id_documento) REFERENCES documentos(id_documento)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_not_fecha FOREIGN KEY (id_fecha) REFERENCES fechas_importantes(id_fecha)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 11. Comprobantes de Pago
CREATE TABLE IF NOT EXISTS pagos (
  id_pago BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_aspirante BIGINT UNSIGNED NOT NULL UNIQUE,
  referencia_asignada VARCHAR(50) NULL,
  monto_esperado DECIMAL(10,2) NULL,
  folio_pago VARCHAR(50) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  fecha_pago DATE NOT NULL,
  id_documento BIGINT UNSIGNED NULL,
  estado ENUM('pendiente', 'validado', 'rechazado') NOT NULL DEFAULT 'pendiente',
  observaciones TEXT NULL,
  id_validador BIGINT UNSIGNED NULL,
  fecha_validacion DATETIME NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pago_asp FOREIGN KEY (id_aspirante) REFERENCES aspirantes(id_aspirante)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_pago_doc FOREIGN KEY (id_documento) REFERENCES documentos(id_documento)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_pago_validador FOREIGN KEY (id_validador) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT chk_pago_monto CHECK (monto > 0)
) ENGINE=InnoDB;

-- 12. Firmas del Reglamento Institucional
CREATE TABLE IF NOT EXISTS firmas_reglamento (
  id_firma BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_aspirante BIGINT UNSIGNED NOT NULL,
  version_reglamento VARCHAR(20) NOT NULL,
  ruta_firma VARCHAR(500) NOT NULL,
  hash_sha256 CHAR(64) NOT NULL,
  fecha_firma DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_firma (id_aspirante, version_reglamento),
  CONSTRAINT fk_firma_asp FOREIGN KEY (id_aspirante) REFERENCES aspirantes(id_aspirante)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 13. Sesiones y Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id_token BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_usuario BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL UNIQUE,
  expira_en DATETIME NOT NULL,
  revocado BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_rt_usuario (id_usuario, revocado),
  CONSTRAINT fk_rt_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
