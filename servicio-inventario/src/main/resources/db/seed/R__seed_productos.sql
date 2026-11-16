-- Archivo R__: Migracion repetible con Flyway.

-- Datos demo para insertar productos en la tabla productos.
INSERT INTO productos (sku, nombre, cantidad_disponible) VALUES
('SKU001', 'Teclado inalambrico', 100),
('SKU002', 'Notebook ', 200),
('SKU003', 'Webcam', 150),
('SKU004', 'Mouse inalambrico', 300),
('SKU005', 'Auriculares', 250)
ON CONFLICT (sku) DO NOTHING;