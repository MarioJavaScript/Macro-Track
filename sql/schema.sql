-- TODO: pegar aquí el esquema de la base de datos (CREATE DATABASE / CREATE TABLE)-- =====================================================
-- MacroTrack - Esquema de base de datos (MySQL 8)
-- =====================================================
CREATE DATABASE IF NOT EXISTS macrotrack
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE macrotrack;

-- ---------- Usuarios y metas ----------
CREATE TABLE usuario (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(80)  NOT NULL,
  email          VARCHAR(120) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  rol            ENUM('USUARIO','ADMIN') NOT NULL DEFAULT 'USUARIO',
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meta (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id      INT NOT NULL UNIQUE,
  kcal            DECIMAL(7,2) NOT NULL CHECK (kcal > 0),
  proteina_g      DECIMAL(6,2) NOT NULL CHECK (proteina_g >= 0),
  carbohidratos_g DECIMAL(6,2) NOT NULL CHECK (carbohidratos_g >= 0),
  grasas_g        DECIMAL(6,2) NOT NULL CHECK (grasas_g >= 0),
  actualizado_en  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_meta_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON DELETE CASCADE
);

-- ---------- Catálogo de alimentos ----------
CREATE TABLE alimento (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  nombre           VARCHAR(120) NOT NULL,
  kcal_100g        DECIMAL(6,2) NOT NULL CHECK (kcal_100g >= 0),
  proteina_100g    DECIMAL(5,2) NOT NULL CHECK (proteina_100g >= 0),
  carbohidratos_100g DECIMAL(5,2) NOT NULL CHECK (carbohidratos_100g >= 0),
  grasas_100g      DECIMAL(5,2) NOT NULL CHECK (grasas_100g >= 0),
  fuente           ENUM('MANUAL','API') NOT NULL DEFAULT 'MANUAL',
  id_externo       VARCHAR(50) NULL,           -- id en la API nutricional
  activo           BOOLEAN NOT NULL DEFAULT TRUE, -- baja lógica (admin)
  creado_por       INT NULL,
  creado_en        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_alimento_externo UNIQUE (fuente, id_externo),
  CONSTRAINT fk_alimento_usuario FOREIGN KEY (creado_por)
    REFERENCES usuario(id) ON DELETE SET NULL,
  INDEX idx_alimento_nombre (nombre)
);

-- ---------- Registro diario ----------
-- Los macros se guardan calculados (snapshot) para que el historial
-- no cambie si el admin edita el alimento después.
CREATE TABLE registro (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id      INT NOT NULL,
  alimento_id     INT NOT NULL,
  fecha           DATE NOT NULL,
  tipo_comida     ENUM('DESAYUNO','COMIDA','CENA','SNACK') NOT NULL,
  gramos          DECIMAL(6,1) NOT NULL CHECK (gramos > 0),
  kcal            DECIMAL(7,2) NOT NULL,
  proteina_g      DECIMAL(6,2) NOT NULL,
  carbohidratos_g DECIMAL(6,2) NOT NULL,
  grasas_g        DECIMAL(6,2) NOT NULL,
  creado_en       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_registro_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON DELETE CASCADE,
  CONSTRAINT fk_registro_alimento FOREIGN KEY (alimento_id)
    REFERENCES alimento(id) ON DELETE RESTRICT,
  INDEX idx_registro_usuario_fecha (usuario_id, fecha)
);

-- ---------- Opcional: recetas / comidas frecuentes ----------
CREATE TABLE receta (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id  INT NOT NULL,
  nombre      VARCHAR(80) NOT NULL,
  creado_en   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_receta_nombre UNIQUE (usuario_id, nombre),
  CONSTRAINT fk_receta_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE receta_alimento (
  receta_id   INT NOT NULL,
  alimento_id INT NOT NULL,
  gramos      DECIMAL(6,1) NOT NULL CHECK (gramos > 0),
  PRIMARY KEY (receta_id, alimento_id),
  CONSTRAINT fk_ra_receta FOREIGN KEY (receta_id)
    REFERENCES receta(id) ON DELETE CASCADE,
  CONSTRAINT fk_ra_alimento FOREIGN KEY (alimento_id)
    REFERENCES alimento(id) ON DELETE RESTRICT
);

-- ---------- Opcional: filtros por alérgenos ----------
CREATE TABLE alergeno (
  id      INT AUTO_INCREMENT PRIMARY KEY,
  nombre  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE alimento_alergeno (
  alimento_id INT NOT NULL,
  alergeno_id INT NOT NULL,
  PRIMARY KEY (alimento_id, alergeno_id),
  FOREIGN KEY (alimento_id) REFERENCES alimento(id) ON DELETE CASCADE,
  FOREIGN KEY (alergeno_id) REFERENCES alergeno(id) ON DELETE CASCADE
);

CREATE TABLE usuario_alergeno (
  usuario_id  INT NOT NULL,
  alergeno_id INT NOT NULL,
  PRIMARY KEY (usuario_id, alergeno_id),
  FOREIGN KEY (usuario_id)  REFERENCES usuario(id)  ON DELETE CASCADE,
  FOREIGN KEY (alergeno_id) REFERENCES alergeno(id) ON DELETE CASCADE
);

-- ---------- Vista útil: totales por día ----------
CREATE VIEW v_totales_diarios AS
SELECT usuario_id, fecha,
       SUM(kcal) AS kcal, SUM(proteina_g) AS proteina_g,
       SUM(carbohidratos_g) AS carbohidratos_g, SUM(grasas_g) AS grasas_g
FROM registro
GROUP BY usuario_id, fecha;