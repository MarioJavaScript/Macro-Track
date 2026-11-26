-- =====================================================
-- MacroTrack - datos de ejemplo
-- =====================================================
-- ojo: esto NO es idempotente, correlo una sola vez
-- si lo corres de nuevo se te duplican las filas
USE macrotrack;

-- admin de prueba, la contrasena es: admin123
INSERT INTO usuario (nombre, email, password_hash, rol, activo)
VALUES ('Admin', 'admin@macrotrack.com',
        'nPilgDzZI3t2YRUBQReOOA==:/jKdtw7xTru8BmtLH2F6h5c5TROpiuUT07bQksDrOPo=',
        'ADMIN', TRUE);

-- catalogo tipico, macros por 100 g
INSERT INTO alimento (nombre, kcal_100g, proteina_100g, carbohidratos_100g, grasas_100g) VALUES
  ('Pechuga de pollo', 165, 31.0, 0.0, 3.6),
  ('Arroz blanco cocido', 130, 2.7, 28.2, 0.3),
  ('Huevo', 143, 12.6, 0.7, 9.5),
  ('Platano', 89, 1.1, 22.8, 0.3),
  ('Leche entera', 61, 3.2, 4.8, 3.3),
  ('Avena', 389, 16.9, 66.3, 6.9),
  ('Atun al natural', 116, 25.5, 0.0, 0.8),
  ('Aceite de oliva', 884, 0.0, 0.0, 100.0);
