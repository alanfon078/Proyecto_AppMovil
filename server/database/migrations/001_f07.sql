-- Ejecutar una sola vez sobre la versión anterior del esquema y después de respaldarla.
ALTER TABLE aspirantes
  MODIFY folio VARCHAR(32) NOT NULL,
  ADD estado_residencia VARCHAR(100) NULL,
  ADD municipio_residencia VARCHAR(120) NULL;

UPDATE aspirantes a
LEFT JOIN estados e ON e.id_estado = a.id_estado
LEFT JOIN municipios m ON m.id_municipio = a.id_municipio
SET a.estado_residencia = e.nombre,
    a.municipio_residencia = m.nombre;

ALTER TABLE documentos ADD vigente BOOLEAN NOT NULL DEFAULT TRUE AFTER version;

UPDATE documentos d
JOIN (
  SELECT id_aspirante, id_tipo_documento, MAX(version) AS ultima
  FROM documentos
  GROUP BY id_aspirante, id_tipo_documento
) v ON v.id_aspirante = d.id_aspirante AND v.id_tipo_documento = d.id_tipo_documento
SET d.vigente = (d.version = v.ultima);

ALTER TABLE documentos
  ADD version_activa TINYINT AS (CASE WHEN vigente THEN 1 ELSE NULL END) VIRTUAL,
  ADD UNIQUE KEY uq_doc_vigente (id_aspirante, id_tipo_documento, version_activa);

DELETE FROM notificaciones WHERE tipo = 'pago';

ALTER TABLE notificaciones
  MODIFY tipo ENUM('documento', 'fecha', 'sistema') NOT NULL DEFAULT 'sistema',
  ADD KEY ix_not_usuario_leida (id_usuario, leida);

DROP TABLE IF EXISTS pagos, firmas_reglamento;

-- Las cuentas sembradas antes de F07 tenían contraseñas públicas. Se conservan sus datos.
UPDATE usuarios u LEFT JOIN aspirantes a ON a.id_usuario = u.id_usuario
SET u.activo = FALSE
WHERE (u.rol = 'escolar' AND u.id_usuario = 99 AND u.nombre_usuario = 'escolar.admin'
       AND u.correo = 'escolar.admin@itsur.edu.mx')
   OR (u.rol = 'aspirante' AND u.nombre_usuario = 'alan.fonseca'
       AND u.correo = 'alan.fonseca@alumnos.itsur.edu.mx' AND a.folio = 'ITSUR-2026-00001');

UPDATE refresh_tokens SET revocado = TRUE;
