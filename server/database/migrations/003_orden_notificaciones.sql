-- Mantener el orden de confirmación de los avisos de cada destinatario.
-- Ejecutar con la API detenida, después de 002_f09.sql.
DROP TRIGGER IF EXISTS ordenar_notificacion_destinatario;
CREATE TRIGGER ordenar_notificacion_destinatario
  BEFORE INSERT ON notificaciones FOR EACH ROW
  SET NEW.id_usuario = COALESCE(
    (SELECT u.id_usuario FROM usuarios u WHERE u.id_usuario = NEW.id_usuario FOR UPDATE),
    NEW.id_usuario
  );
