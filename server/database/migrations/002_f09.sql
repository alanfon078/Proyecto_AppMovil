-- Migración idempotente F09: preguntas/dudas y avisos generales

CREATE TABLE IF NOT EXISTS preguntas_dudas (
  id_pregunta BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  id_aspirante BIGINT UNSIGNED NOT NULL,
  categoria ENUM('fechas', 'documentos') NOT NULL,
  pregunta TEXT NOT NULL,
  estado ENUM('pendiente', 'respondida') NOT NULL DEFAULT 'pendiente',
  respuesta TEXT NULL,
  id_admin_respuesta BIGINT UNSIGNED NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_respuesta DATETIME NULL,
  KEY ix_preg_filtros (categoria, estado, fecha_creacion),
  KEY ix_preg_aspirante (id_aspirante),
  CONSTRAINT fk_preg_aspirante FOREIGN KEY (id_aspirante) REFERENCES aspirantes(id_aspirante)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_preg_admin FOREIGN KEY (id_admin_respuesta) REFERENCES usuarios(id_usuario)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

ALTER TABLE notificaciones
  MODIFY tipo ENUM('documento', 'fecha', 'duda', 'aviso_general', 'sistema') NOT NULL DEFAULT 'sistema';

SET @notificaciones_pregunta_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notificaciones' AND COLUMN_NAME = 'id_pregunta'
);
SET @sql = IF(@notificaciones_pregunta_exists = 0,
  'ALTER TABLE notificaciones ADD COLUMN id_pregunta BIGINT UNSIGNED NULL AFTER id_fecha',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @notificaciones_pregunta_index_exists = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notificaciones' AND INDEX_NAME = 'ix_not_pregunta'
);
SET @sql = IF(@notificaciones_pregunta_index_exists = 0,
  'ALTER TABLE notificaciones ADD INDEX ix_not_pregunta (id_pregunta)',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @notificaciones_pregunta_fk_exists = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'notificaciones'
    AND CONSTRAINT_NAME = 'fk_not_pregunta'
);
SET @sql = IF(@notificaciones_pregunta_fk_exists = 0,
  'ALTER TABLE notificaciones ADD CONSTRAINT fk_not_pregunta FOREIGN KEY (id_pregunta) REFERENCES preguntas_dudas(id_pregunta) ON DELETE SET NULL ON UPDATE CASCADE',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
