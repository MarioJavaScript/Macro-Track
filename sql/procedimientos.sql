-- =====================================================
-- MacroTrack - Procedimientos almacenados (Fase 2, MySQL 8)
-- =====================================================
-- CONTRATO:
--   Los NOMBRES y PARÁMETROS de estos procedimientos no se cambian sin
--   avisar al backend (los DAOs en com.macrotrack.dao los llaman tal cual).
--   El SQL interno sí se puede mejorar libremente.
--
-- Se puede ejecutar el archivo varias veces (DROP PROCEDURE IF EXISTS).
--   mysql -u root -p macrotrack < sql/procedimientos.sql
-- =====================================================
USE macrotrack;

DELIMITER $$

-- =====================================================
-- USUARIO
-- =====================================================

DROP PROCEDURE IF EXISTS sp_usuario_insertar$$
CREATE PROCEDURE sp_usuario_insertar(
    IN  p_nombre        VARCHAR(80),
    IN  p_email         VARCHAR(120),
    IN  p_password_hash VARCHAR(255),
    OUT p_id            INT)
BEGIN
    -- rol y activo toman sus valores por defecto (USUARIO, TRUE)
    INSERT INTO usuario (nombre, email, password_hash)
    VALUES (p_nombre, p_email, p_password_hash);

    SET p_id = LAST_INSERT_ID();
END$$

DROP PROCEDURE IF EXISTS sp_usuario_buscar_por_email$$
CREATE PROCEDURE sp_usuario_buscar_por_email(IN p_email VARCHAR(120))
BEGIN
    -- No filtra por activo: el service decide qué hacer con un usuario dado de baja
    SELECT id, nombre, email, password_hash, rol, activo
    FROM usuario
    WHERE email = p_email;
END$$

-- =====================================================
-- META
-- =====================================================

DROP PROCEDURE IF EXISTS sp_meta_guardar$$
CREATE PROCEDURE sp_meta_guardar(
    IN p_usuario_id    INT,
    IN p_kcal          DECIMAL(7,2),
    IN p_proteina      DECIMAL(6,2),
    IN p_carbohidratos DECIMAL(6,2),
    IN p_grasas        DECIMAL(6,2))
BEGIN
    -- usuario_id es UNIQUE: si ya tiene meta, se actualiza
    INSERT INTO meta (usuario_id, kcal, proteina_g, carbohidratos_g, grasas_g)
    VALUES (p_usuario_id, p_kcal, p_proteina, p_carbohidratos, p_grasas)
    ON DUPLICATE KEY UPDATE
        kcal            = VALUES(kcal),
        proteina_g      = VALUES(proteina_g),
        carbohidratos_g = VALUES(carbohidratos_g),
        grasas_g        = VALUES(grasas_g);
END$$

DROP PROCEDURE IF EXISTS sp_meta_buscar$$
CREATE PROCEDURE sp_meta_buscar(IN p_usuario_id INT)
BEGIN
    SELECT id, usuario_id, kcal, proteina_g, carbohidratos_g, grasas_g
    FROM meta
    WHERE usuario_id = p_usuario_id;
END$$

-- =====================================================
-- ALIMENTO
-- =====================================================

DROP PROCEDURE IF EXISTS sp_alimento_buscar$$
CREATE PROCEDURE sp_alimento_buscar(IN p_texto VARCHAR(120))
BEGIN
    -- Solo activos, por nombre, máximo 50 resultados
    SELECT id, nombre, kcal_100g, proteina_100g, carbohidratos_100g,
           grasas_100g, id_externo, activo
    FROM alimento
    WHERE activo = TRUE
      AND nombre LIKE CONCAT('%', p_texto, '%')
    ORDER BY nombre
    LIMIT 50;
END$$

DROP PROCEDURE IF EXISTS sp_alimento_buscar_por_id$$
CREATE PROCEDURE sp_alimento_buscar_por_id(IN p_id INT)
BEGIN
    -- No filtra por activo: un registro viejo puede apuntar a un alimento dado de baja
    SELECT id, nombre, kcal_100g, proteina_100g, carbohidratos_100g,
           grasas_100g, id_externo, activo
    FROM alimento
    WHERE id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_alimento_insertar$$
CREATE PROCEDURE sp_alimento_insertar(
    IN  p_nombre        VARCHAR(120),
    IN  p_kcal          DECIMAL(6,2),
    IN  p_proteina      DECIMAL(5,2),
    IN  p_carbohidratos DECIMAL(5,2),
    IN  p_grasas        DECIMAL(5,2),
    IN  p_creado_por    INT,
    OUT p_id            INT)
BEGIN
    -- fuente = 'MANUAL' y activo = TRUE por defecto
    INSERT INTO alimento (nombre, kcal_100g, proteina_100g, carbohidratos_100g,
                          grasas_100g, creado_por)
    VALUES (p_nombre, p_kcal, p_proteina, p_carbohidratos, p_grasas, p_creado_por);

    SET p_id = LAST_INSERT_ID();
END$$

DROP PROCEDURE IF EXISTS sp_alimento_actualizar$$
CREATE PROCEDURE sp_alimento_actualizar(
    IN p_id            INT,
    IN p_nombre        VARCHAR(120),
    IN p_kcal          DECIMAL(6,2),
    IN p_proteina      DECIMAL(5,2),
    IN p_carbohidratos DECIMAL(5,2),
    IN p_grasas        DECIMAL(5,2))
BEGIN
    UPDATE alimento
    SET nombre             = p_nombre,
        kcal_100g          = p_kcal,
        proteina_100g      = p_proteina,
        carbohidratos_100g = p_carbohidratos,
        grasas_100g        = p_grasas
    WHERE id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_alimento_desactivar$$
CREATE PROCEDURE sp_alimento_desactivar(IN p_id INT)
BEGIN
    -- Baja lógica: el alimento no se borra porque hay registros que lo usan
    UPDATE alimento SET activo = FALSE WHERE id = p_id;
END$$

-- =====================================================
-- REGISTRO
-- =====================================================
-- Los SELECT de registro traen también el alimento (datos por 100 g).

DROP PROCEDURE IF EXISTS sp_registro_insertar$$
CREATE PROCEDURE sp_registro_insertar(
    IN  p_usuario_id    INT,
    IN  p_alimento_id   INT,
    IN  p_fecha         DATE,
    IN  p_tipo_comida   VARCHAR(10),
    IN  p_gramos        DECIMAL(6,1),
    IN  p_kcal          DECIMAL(7,2),
    IN  p_proteina      DECIMAL(6,2),
    IN  p_carbohidratos DECIMAL(6,2),
    IN  p_grasas        DECIMAL(6,2),
    OUT p_id            INT)
BEGIN
    -- Los macros llegan ya calculados (snapshot de la porción)
    INSERT INTO registro (usuario_id, alimento_id, fecha, tipo_comida, gramos,
                          kcal, proteina_g, carbohidratos_g, grasas_g)
    VALUES (p_usuario_id, p_alimento_id, p_fecha, p_tipo_comida, p_gramos,
            p_kcal, p_proteina, p_carbohidratos, p_grasas);

    SET p_id = LAST_INSERT_ID();
END$$

DROP PROCEDURE IF EXISTS sp_registro_por_fecha$$
CREATE PROCEDURE sp_registro_por_fecha(IN p_usuario_id INT, IN p_fecha DATE)
BEGIN
    SELECT r.id, r.usuario_id, r.fecha, r.tipo_comida, r.gramos,
           r.kcal, r.proteina_g, r.carbohidratos_g, r.grasas_g,
           a.id AS alimento_id, a.nombre AS alimento_nombre,
           a.kcal_100g, a.proteina_100g, a.carbohidratos_100g, a.grasas_100g,
           a.id_externo AS alimento_id_externo, a.activo AS alimento_activo
    FROM registro r
    JOIN alimento a ON a.id = r.alimento_id
    WHERE r.usuario_id = p_usuario_id
      AND r.fecha = p_fecha
    ORDER BY r.tipo_comida, r.id;   -- ENUM ordena por posición: desayuno, comida, cena, snack
END$$

DROP PROCEDURE IF EXISTS sp_registro_buscar_por_id$$
CREATE PROCEDURE sp_registro_buscar_por_id(IN p_id INT)
BEGIN
    SELECT r.id, r.usuario_id, r.fecha, r.tipo_comida, r.gramos,
           r.kcal, r.proteina_g, r.carbohidratos_g, r.grasas_g,
           a.id AS alimento_id, a.nombre AS alimento_nombre,
           a.kcal_100g, a.proteina_100g, a.carbohidratos_100g, a.grasas_100g,
           a.id_externo AS alimento_id_externo, a.activo AS alimento_activo
    FROM registro r
    JOIN alimento a ON a.id = r.alimento_id
    WHERE r.id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_registro_actualizar$$
CREATE PROCEDURE sp_registro_actualizar(
    IN p_id            INT,
    IN p_gramos        DECIMAL(6,1),
    IN p_kcal          DECIMAL(7,2),
    IN p_proteina      DECIMAL(6,2),
    IN p_carbohidratos DECIMAL(6,2),
    IN p_grasas        DECIMAL(6,2))
BEGIN
    UPDATE registro
    SET gramos          = p_gramos,
        kcal            = p_kcal,
        proteina_g      = p_proteina,
        carbohidratos_g = p_carbohidratos,
        grasas_g        = p_grasas
    WHERE id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_registro_eliminar$$
CREATE PROCEDURE sp_registro_eliminar(IN p_id INT)
BEGIN
    DELETE FROM registro WHERE id = p_id;
END$$

DROP PROCEDURE IF EXISTS sp_registro_totales_rango$$
CREATE PROCEDURE sp_registro_totales_rango(
    IN p_usuario_id INT,
    IN p_inicio     DATE,
    IN p_fin        DATE)
BEGIN
    -- Un renglón por día que tenga registros (los días vacíos no aparecen)
    SELECT fecha, kcal, proteina_g, carbohidratos_g, grasas_g
    FROM v_totales_diarios
    WHERE usuario_id = p_usuario_id
      AND fecha BETWEEN p_inicio AND p_fin
    ORDER BY fecha;
END$$

DELIMITER ;
