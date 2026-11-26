-- =====================================================
-- MacroTrack - Catálogo básico de alimentos (40 alimentos)
-- =====================================================
-- Valores por cada 100 g (aproximados, tomados de tablas nutricionales tipo USDA).
-- Se puede correr varias veces: solo agrega los alimentos que todavía no existen
-- (compara por nombre), así que no duplica lo que ya trae seed.sql.
-- Funciona en MySQL 8 y en MariaDB (XAMPP).
-- Importar en phpMyAdmin: Importar -> sql/catalogo.sql -> Importar.
-- =====================================================
USE macrotrack;

CREATE TEMPORARY TABLE tmp_catalogo (
  nombre             VARCHAR(120),
  kcal_100g          DECIMAL(6,2),
  proteina_100g      DECIMAL(5,2),
  carbohidratos_100g DECIMAL(5,2),
  grasas_100g        DECIMAL(5,2)
);

INSERT INTO tmp_catalogo VALUES
  ('Pechuga de pollo', 165, 31.0, 0.0, 3.6),
  ('Arroz blanco cocido', 130, 2.7, 28.2, 0.3),
  ('Huevo', 143, 12.6, 0.7, 9.5),
  ('Platano', 89, 1.1, 22.8, 0.3),
  ('Leche entera', 61, 3.2, 4.8, 3.3),
  ('Avena', 389, 16.9, 66.3, 6.9),
  ('Atun al natural', 116, 25.5, 0.0, 0.8),
  ('Aceite de oliva', 884, 0.0, 0.0, 100.0),
  ('Clara de huevo', 52, 10.9, 0.7, 0.2),
  ('Carne molida de res 90/10', 176, 20.0, 0.0, 10.0),
  ('Bistec de res', 158, 26.0, 0.0, 5.7),
  ('Salmón', 208, 20.4, 0.0, 13.4),
  ('Tilapia', 96, 20.1, 0.0, 1.7),
  ('Pechuga de pavo', 135, 30.0, 0.0, 1.0),
  ('Jamón de pavo', 110, 17.0, 3.0, 3.0),
  ('Queso panela', 250, 18.0, 3.0, 19.0),
  ('Queso cottage', 98, 11.1, 3.4, 4.3),
  ('Yogur griego natural sin grasa', 59, 10.2, 3.6, 0.4),
  ('Proteína whey (polvo)', 400, 80.0, 8.0, 6.0),
  ('Leche descremada', 34, 3.4, 5.0, 0.1),
  ('Frijoles negros cocidos', 132, 8.9, 23.7, 0.5),
  ('Lentejas cocidas', 116, 9.0, 20.1, 0.4),
  ('Garbanzos cocidos', 164, 8.9, 27.4, 2.6),
  ('Tortilla de maíz', 218, 5.7, 44.6, 2.9),
  ('Tortilla de harina', 304, 8.0, 50.0, 7.7),
  ('Pan integral', 247, 13.0, 41.0, 3.4),
  ('Pasta cocida', 158, 5.8, 30.9, 0.9),
  ('Arroz integral cocido', 123, 2.7, 25.6, 1.0),
  ('Papa cocida', 87, 1.9, 20.1, 0.1),
  ('Manzana', 52, 0.3, 13.8, 0.2),
  ('Naranja', 47, 0.9, 11.8, 0.1),
  ('Mango', 60, 0.8, 15.0, 0.4),
  ('Brócoli cocido', 35, 2.4, 7.2, 0.4),
  ('Espinaca', 23, 2.9, 3.6, 0.4),
  ('Jitomate', 18, 0.9, 3.9, 0.2),
  ('Lechuga', 15, 1.4, 2.9, 0.2),
  ('Zanahoria', 41, 0.9, 9.6, 0.2),
  ('Aguacate', 160, 2.0, 8.5, 14.7),
  ('Almendras', 579, 21.2, 21.6, 49.9),
  ('Crema de cacahuate', 588, 25.0, 20.0, 50.0);

INSERT INTO alimento (nombre, kcal_100g, proteina_100g, carbohidratos_100g, grasas_100g)
SELECT t.nombre, t.kcal_100g, t.proteina_100g, t.carbohidratos_100g, t.grasas_100g
FROM tmp_catalogo t
WHERE NOT EXISTS (SELECT 1 FROM alimento a WHERE a.nombre = t.nombre);

DROP TEMPORARY TABLE tmp_catalogo;
