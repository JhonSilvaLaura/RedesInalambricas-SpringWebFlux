# 📚 DOCUMENTACIÓN COMPLETA - API MICROSERVICIOS

## 📋 Índice
1. [Arquitectura](#arquitectura)
2. [Microservicio Productos](#ms-productos)
3. [Microservicio Pedidos](#ms-pedidos)
4. [Flujos de Negocio](#flujos-de-negocio)
5. [Manejo de Errores](#manejo-de-errores)
6. [Base de Datos](#base-de-datos)

---

## 🏗️ Arquitectura

### Visión General
Sistema de microservicios reactivo construido con Spring Boot WebFlux y R2DBC para gestionar productos y pedidos.

### Tecnologías
- **Framework**: Spring Boot 4.1.0
- **Programación Reactiva**: Project Reactor
- **Base de Datos**: PostgreSQL (Neon)
- **Driver BD**: R2DBC PostgreSQL
- **Comunicación**: WebClient (HTTP Reactivo)
- **Arquitectura**: Hexagonal (Ports & Adapters)

### Puertos
- **ms-productos**: `http://localhost:8081`
- **ms-pedidos**: `http://localhost:8082`

---

## 📦 MS-PRODUCTOS

### Descripción
Microservicio encargado de la gestión del catálogo de productos, incluyendo operaciones CRUD y control de inventario.

### Endpoints

#### 1️⃣ GET - Listar Todos los Productos
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos -Method Get
```

**Descripción:** Obtiene la lista completa de productos en el sistema.

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "name": "Laptop Dell XPS 13",
    "price": 1299.99,
    "stock": 15,
    "active": true
  },
  {
    "id": 2,
    "name": "Mouse Logitech MX Master 3",
    "price": 99.99,
    "stock": 50,
    "active": true
  }
]
```

**Casos de Uso:**
- Mostrar catálogo completo en la interfaz
- Sincronización de inventario
- Reportes y análisis

---

#### 2️⃣ GET - Obtener Producto por ID
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos/1 -Method Get
```

**Descripción:** Obtiene los detalles de un producto específico por su ID.

**Parámetros:**
- `id` (path): ID del producto (Long)

**Respuesta Exitosa (200):**
```json
{
  "id": 1,
  "name": "Laptop Dell XPS 13",
  "price": 1299.99,
  "stock": 15,
  "active": true
}
```

**Respuesta Error (404):**
```json
{
  "timestamp": "2026-07-02T18:00:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado"
}
```

**Casos de Uso:**
- Vista detalle de producto
- Validación antes de crear pedido
- Verificación de disponibilidad

---

#### 3️⃣ POST - Crear Producto
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos -Method Post -ContentType "application/json" -Body '{"name":"Teclado Mecánico","price":89.99,"stock":25,"active":true}'
```

**Descripción:** Crea un nuevo producto en el catálogo.

**Body (JSON):**
```json
{
  "name": "Teclado Mecánico",
  "price": 89.99,
  "stock": 25,
  "active": true
}
```

**Campos:**
- `name` (String, requerido): Nombre del producto
- `price` (Double, requerido): Precio unitario
- `stock` (Integer, requerido): Cantidad disponible
- `active` (Boolean, requerido): Estado del producto

**Respuesta Exitosa (201):**
```json
{
  "id": 16,
  "name": "Teclado Mecánico",
  "price": 89.99,
  "stock": 25,
  "active": true
}
```

**Validaciones:**
- Precio debe ser mayor a 0
- Stock debe ser 0 o mayor
- Nombre no puede estar vacío

**Casos de Uso:**
- Alta de nuevos productos
- Importación de catálogo
- Registro de nuevo inventario

---

#### 4️⃣ PUT - Actualizar Producto
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos/1 -Method Put -ContentType "application/json" -Body '{"name":"Laptop Dell XPS 13 Plus","price":1399.99,"stock":12,"active":true}'
```

**Descripción:** Actualiza completamente un producto existente.

**Parámetros:**
- `id` (path): ID del producto a actualizar

**Body (JSON):**
```json
{
  "name": "Laptop Dell XPS 13 Plus",
  "price": 1399.99,
  "stock": 12,
  "active": true
}
```

**Respuesta Exitosa (200):**
```json
{
  "id": 1,
  "name": "Laptop Dell XPS 13 Plus",
  "price": 1399.99,
  "stock": 12,
  "active": true
}
```

**Respuesta Error (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado"
}
```

**Casos de Uso:**
- Actualización de precios
- Corrección de información
- Ajuste manual de inventario
- Cambio de estado (activar/desactivar)

---

#### 5️⃣ PATCH - Disminuir Stock
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/productos/2/decreaseStock?quantity=3" -Method Patch
```

**Descripción:** Disminuye el stock de un producto de forma atómica. Solo se ejecuta si hay stock suficiente.

**Parámetros:**
- `id` (path): ID del producto
- `quantity` (query): Cantidad a descontar

**Query SQL Interno:**
```sql
UPDATE productos 
SET stock = stock - :quantity 
WHERE id = :id AND stock >= :quantity 
RETURNING *
```

**Respuesta Exitosa (200):**
```json
{
  "id": 2,
  "name": "Mouse Logitech MX Master 3",
  "price": 99.99,
  "stock": 47,
  "active": true
}
```

**Respuesta Error (400):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente"
}
```

**Características:**
- ✅ Operación atómica (no permite stock negativo)
- ✅ Thread-safe (sin race conditions)
- ✅ Validación a nivel de base de datos
- ✅ Devuelve el producto actualizado

**Casos de Uso:**
- Reserva de stock al crear pedido
- Ajustes de inventario
- Procesos automáticos de venta

---

#### 6️⃣ DELETE - Eliminar Producto
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos/15 -Method Delete
```

**Descripción:** Realiza un soft delete del producto (cambia active a false).

**Parámetros:**
- `id` (path): ID del producto

**Respuesta Exitosa (204):**
```
No Content
```

**Respuesta Error (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado"
}
```

**Notas Importantes:**
- ⚠️ Es un soft delete (no borra físicamente)
- ⚠️ El producto permanece en BD con active=false
- ⚠️ No afecta pedidos existentes

**Casos de Uso:**
- Descontinuar productos
- Ocultar productos temporalmente
- Gestión de catálogo

---

## 🛒 MS-PEDIDOS

### Descripción
Microservicio encargado de la gestión de pedidos, validación de stock y comunicación con el microservicio de productos.

### Endpoints

#### 1️⃣ GET - Listar Todos los Pedidos
```powershell
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos -Method Get
```

**Descripción:** Obtiene la lista completa de pedidos del sistema.

**Respuesta Exitosa (200):**
```json
[
  {
    "id": 1,
    "productId": "1",
    "quantity": 2,
    "total": 2599.98,
    "price": 1299.99,
    "status": "CONFIRMADO",
    "fecha": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "productId": "2",
    "quantity": 5,
    "total": 499.95,
    "price": 99.99,
    "status": "CANCELADO",
    "fecha": "2024-01-16T14:20:00"
  }
]
```

**Estados de Pedido:**
- `CONFIRMADO`: Pedido creado exitosamente, stock descontado
- `CANCELADO`: Pedido cancelado, stock devuelto
- `COMPLETED`: Pedido completado (datos de prueba)
- `SHIPPED`: Pedido enviado (datos de prueba)
- `PENDING`: Pedido pendiente (datos de prueba)

**Casos de Uso:**
- Dashboard de pedidos
- Reportes de ventas
- Historial de transacciones

---

#### 2️⃣ GET - Obtener Pedido por ID
```powershell
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos/1 -Method Get
```

**Descripción:** Obtiene los detalles de un pedido específico.

**Parámetros:**
- `id` (path): ID del pedido

**Respuesta Exitosa (200):**
```json
{
  "id": 1,
  "productId": "1",
  "quantity": 2,
  "total": 2599.98,
  "price": 1299.99,
  "status": "CONFIRMADO",
  "fecha": "2024-01-15T10:30:00"
}
```

**Respuesta Error (404):**
```json
{
  "status": 404,
  "error": "Not Found"
}
```

**Casos de Uso:**
- Detalle de pedido
- Seguimiento de orden
- Auditoría

---

#### 3️⃣ POST - Crear Pedido
```powershell
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos -Method Post -ContentType "application/json" -Body '{"productId":"1","quantity":2}'
```

**Descripción:** Crea un nuevo pedido. Valida stock y lo descuenta automáticamente.

**Body (JSON):**
```json
{
  "productId": "1",
  "quantity": 2
}
```

**Campos:**
- `productId` (String, requerido): ID del producto a pedir
- `quantity` (Integer, requerido): Cantidad a pedir
- `price` (Double, opcional): Se obtiene automáticamente del producto

**Proceso Interno:**
1. ✅ Valida que el producto existe
2. ✅ Verifica que hay stock suficiente
3. ✅ Descuenta el stock (`decreaseStock`)
4. ✅ Calcula el total automáticamente
5. ✅ Establece status como "CONFIRMADO"
6. ✅ Guarda el pedido con fecha actual

**Respuesta Exitosa (201):**
```json
{
  "id": 16,
  "productId": "1",
  "quantity": 2,
  "total": 2599.98,
  "price": 1299.99,
  "status": "CONFIRMADO",
  "fecha": "2026-07-02T18:35:00"
}
```

**Respuesta Error - Producto no existe (404):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado"
}
```

**Respuesta Error - Stock insuficiente (400):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente"
}
```

**Comunicación entre Microservicios:**
```
ms-pedidos → GET /api/productos/{id} → ms-productos
ms-pedidos → PATCH /api/productos/{id}/decreaseStock?quantity=X → ms-productos
```

**Casos de Uso:**
- Proceso de checkout
- Reserva de productos
- Generación de orden de compra

**⚠️ Importante:**
- La operación es atómica a nivel de stock
- Si falla el descuento, no se crea el pedido
- El precio se congela al momento del pedido

---

#### 4️⃣ DELETE - Cancelar Pedido
```powershell
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos/1 -Method Delete
```

**Descripción:** Cancela un pedido y devuelve el stock al inventario.

**Parámetros:**
- `id` (path): ID del pedido

**Proceso Interno:**
1. ✅ Busca el pedido por ID
2. ✅ Valida que el status sea "CONFIRMADO"
3. ✅ Devuelve el stock al producto
4. ✅ Cambia el status a "CANCELADO"
5. ✅ Guarda el pedido actualizado

**Respuesta Exitosa (200):**
```json
{
  "id": 1,
  "productId": "1",
  "quantity": 2,
  "total": 2599.98,
  "price": 1299.99,
  "status": "CANCELADO",
  "fecha": "2024-01-15T10:30:00"
}
```

**Respuesta Error - Pedido no encontrado (404):**
```json
{
  "status": 404,
  "error": "Not Found"
}
```

**Respuesta Error - Status incorrecto (400):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Solo se pueden cancelar pedidos confirmados"
}
```

**Restricciones:**
- ❌ No se pueden cancelar pedidos con status "COMPLETED"
- ❌ No se pueden cancelar pedidos con status "SHIPPED"
- ❌ No se pueden cancelar pedidos con status "PENDING"
- ❌ No se pueden cancelar pedidos con status "CANCELADO"
- ✅ Solo se pueden cancelar pedidos con status "CONFIRMADO"

**Comunicación entre Microservicios:**
```
ms-pedidos → (implementación pendiente) increaseStock → ms-productos
```

**Casos de Uso:**
- Cancelación de orden
- Devolución de productos
- Corrección de errores

---

## 🔄 Flujos de Negocio

### Flujo 1: Creación Exitosa de Pedido

```
1. Cliente solicita crear pedido
   POST /api/pedidos
   {
     "productId": "1",
     "quantity": 3
   }

2. ms-pedidos consulta producto
   GET http://localhost:8081/api/productos/1
   
   Respuesta:
   {
     "id": 1,
     "name": "Laptop Dell XPS 13",
     "price": 1299.99,
     "stock": 15,  ← Hay stock suficiente
     "active": true
   }

3. ms-pedidos valida stock
   15 >= 3 ✅ Stock suficiente

4. ms-pedidos descuenta stock
   PATCH http://localhost:8081/api/productos/1/decreaseStock?quantity=3
   
   Respuesta:
   {
     "id": 1,
     "name": "Laptop Dell XPS 13",
     "price": 1299.99,
     "stock": 12,  ← Stock actualizado
     "active": true
   }

5. ms-pedidos guarda pedido
   Pedido creado con:
   - total: 1299.99 * 3 = 3899.97
   - status: "CONFIRMADO"
   - fecha: Timestamp actual

6. Respuesta al cliente
   201 Created
   {
     "id": 16,
     "productId": "1",
     "quantity": 3,
     "total": 3899.97,
     "price": 1299.99,
     "status": "CONFIRMADO",
     "fecha": "2026-07-02T18:35:00"
   }
```

**Resultado:**
- ✅ Pedido creado
- ✅ Stock descontado
- ✅ Transacción completa

---

### Flujo 2: Cancelación de Pedido

```
1. Cliente solicita cancelar pedido
   DELETE /api/pedidos/16

2. ms-pedidos busca el pedido
   SELECT * FROM pedidos WHERE id = 16
   
   Resultado:
   {
     "id": 16,
     "productId": "1",
     "quantity": 3,
     "status": "CONFIRMADO"  ← Válido para cancelar
   }

3. ms-pedidos valida status
   status == "CONFIRMADO" ✅

4. ms-pedidos devuelve stock
   (implementación pendiente)
   Debería llamar a: increaseStock(productId=1, quantity=3)

5. ms-pedidos actualiza pedido
   UPDATE pedidos SET status = 'CANCELADO' WHERE id = 16

6. Respuesta al cliente
   200 OK
   {
     "id": 16,
     "productId": "1",
     "quantity": 3,
     "total": 3899.97,
     "price": 1299.99,
     "status": "CANCELADO",
     "fecha": "2026-07-02T18:35:00"
   }
```

**Resultado:**
- ✅ Pedido cancelado
- ✅ Stock devuelto (cuando se implemente)
- ✅ Historial mantenido

---

### Flujo 3: Pedido Rechazado - Stock Insuficiente

```
1. Cliente solicita crear pedido
   POST /api/pedidos
   {
     "productId": "9",
     "quantity": 20
   }

2. ms-pedidos consulta producto
   GET http://localhost:8081/api/productos/9
   
   Respuesta:
   {
     "id": 9,
     "name": "Tablet Samsung Galaxy Tab S8",
     "price": 699.99,
     "stock": 12,  ← Stock insuficiente
     "active": true
   }

3. ms-pedidos valida stock
   12 < 20 ❌ Stock insuficiente

4. ms-pedidos rechaza pedido
   No se ejecuta decreaseStock
   No se crea pedido en BD

5. Respuesta al cliente
   400 Bad Request
   {
     "status": 400,
     "error": "Bad Request",
     "message": "Stock insuficiente"
   }
```

**Resultado:**
- ❌ Pedido no creado
- ✅ Stock no modificado
- ✅ Sistema consistente

---

### Flujo 4: Actualización Manual de Inventario

```
1. Administrador actualiza producto
   PUT /api/productos/1
   {
     "name": "Laptop Dell XPS 13 Plus",
     "price": 1499.99,
     "stock": 20,
     "active": true
   }

2. Sistema valida y actualiza
   UPDATE productos SET 
     name = 'Laptop Dell XPS 13 Plus',
     price = 1499.99,
     stock = 20,
     active = true
   WHERE id = 1

3. Respuesta
   200 OK
   {
     "id": 1,
     "name": "Laptop Dell XPS 13 Plus",
     "price": 1499.99,
     "stock": 20,
     "active": true
   }
```

**Notas:**
- Los pedidos anteriores mantienen el precio histórico
- El nuevo precio aplica solo a pedidos futuros

---

## ⚠️ Manejo de Errores

### Códigos de Estado HTTP

| Código | Significado | Cuándo Ocurre |
|--------|-------------|---------------|
| 200 | OK | Operación exitosa (GET, PUT, PATCH, DELETE) |
| 201 | Created | Recurso creado (POST) |
| 204 | No Content | Eliminación exitosa |
| 400 | Bad Request | Validación fallida, stock insuficiente |
| 404 | Not Found | Recurso no encontrado |
| 500 | Internal Server Error | Error del servidor, BD no disponible |

### Ejemplos de Errores Comunes

#### Error 1: Producto No Encontrado
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos/999 -Method Get
```

```json
{
  "timestamp": "2026-07-02T18:40:00.000Z",
  "path": "/api/productos/999",
  "status": 404,
  "error": "Not Found",
  "message": null
}
```

**Solución:** Verificar que el ID del producto existe

---

#### Error 2: Stock Insuficiente
```powershell
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos -Method Post -ContentType "application/json" -Body '{"productId":"1","quantity":100}'
```

```json
{
  "timestamp": "2026-07-02T18:40:00.000Z",
  "path": "/api/pedidos",
  "status": 400,
  "error": "Bad Request",
  "message": "Stock insuficiente"
}
```

**Solución:** Reducir la cantidad o esperar reposición de inventario

---

#### Error 3: Cancelar Pedido con Status Incorrecto
```powershell
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos/1 -Method Delete
```

```json
{
  "timestamp": "2026-07-02T18:40:00.000Z",
  "path": "/api/pedidos/1",
  "status": 400,
  "error": "Bad Request",
  "message": "Solo se pueden cancelar pedidos confirmados"
}
```

**Solución:** Solo cancelar pedidos con status "CONFIRMADO"

---

#### Error 4: JSON Malformado
```powershell
Invoke-RestMethod -Uri http://localhost:8081/api/productos -Method Post -ContentType "application/json" -Body '{"name":"Test"price":99.99}'
```

```json
{
  "timestamp": "2026-07-02T18:40:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Failed to read HTTP message"
}
```

**Solución:** Verificar sintaxis JSON correcta

---

## 💾 Base de Datos

### Conexión
```yaml
Host: ep-rough-violet-adi6ri0k-pooler.c-2.us-east-1.aws.neon.tech
Puerto: 5432
Base de datos: redesinalambricas
Usuario: neondb_owner
Password: npg_pHT4NJx3gnSG
SSL Mode: require
```

### Esquema de Tablas

#### Tabla: productos
```sql
CREATE TABLE productos (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_productos_active ON productos(active);
CREATE INDEX idx_productos_name ON productos(name);
```

#### Tabla: pedidos
```sql
CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pedidos_product_id ON pedidos(product_id);
CREATE INDEX idx_pedidos_status ON pedidos(status);
CREATE INDEX idx_pedidos_fecha ON pedidos(fecha DESC);
```

### Consultas Útiles

#### Productos con Bajo Stock
```sql
SELECT * FROM productos 
WHERE stock < 10 AND active = true 
ORDER BY stock ASC;
```

#### Total de Ventas por Producto
```sql
SELECT 
    product_id, 
    COUNT(*) as total_pedidos,
    SUM(quantity) as unidades_vendidas,
    SUM(total) as ingresos_totales
FROM pedidos 
WHERE status = 'CONFIRMADO'
GROUP BY product_id 
ORDER BY ingresos_totales DESC;
```

#### Pedidos del Día
```sql
SELECT * FROM pedidos 
WHERE DATE(fecha) = CURRENT_DATE
ORDER BY fecha DESC;
```

#### Stock Disponible vs Reservado
```sql
SELECT 
    p.id,
    p.name,
    p.stock as stock_actual,
    COALESCE(SUM(ped.quantity), 0) as reservado
FROM productos p
LEFT JOIN pedidos ped ON p.id::text = ped.product_id 
    AND ped.status = 'CONFIRMADO'
GROUP BY p.id, p.name, p.stock;
```

---

## 🚀 Inicio Rápido

### 1. Iniciar Servicios

```bash
# Terminal 1 - ms-productos
cd c:\Users\USER\Documents\RedesInalambricas\ms-productos
mvn spring-boot:run

# Terminal 2 - ms-pedidos
cd c:\Users\USER\Documents\RedesInalambricas\ms-pedidos
mvn spring-boot:run
```

### 2. Verificar que Funcionan

```powershell
# Verificar ms-productos
Invoke-RestMethod -Uri http://localhost:8081/api/productos -Method Get

# Verificar ms-pedidos
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos -Method Get
```

### 3. Prueba Completa

```powershell
# 1. Ver producto
Invoke-RestMethod -Uri http://localhost:8081/api/productos/1 -Method Get

# 2. Crear pedido
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos -Method Post -ContentType "application/json" -Body '{"productId":"1","quantity":1}'

# 3. Verificar stock disminuyó
Invoke-RestMethod -Uri http://localhost:8081/api/productos/1 -Method Get

# 4. Ver pedidos
Invoke-RestMethod -Uri http://localhost:8082/api/pedidos -Method Get
```

---

## 📝 Notas Finales

### Consideraciones de Producción

1. **Seguridad:**
   - Implementar autenticación (JWT)
   - Agregar rate limiting
   - Validar inputs con Bean Validation

2. **Resiliencia:**
   - Circuit breaker entre microservicios
   - Retry con backoff exponencial
   - Timeouts configurables

3. **Monitoreo:**
   - Logs estructurados
   - Métricas con Micrometer
   - Health checks

4. **Testing:**
   - Tests unitarios con JUnit
   - Tests de integración con TestContainers
   - Tests de carga con JMeter

### Mejoras Pendientes

- [ ] Implementar `increaseStock` en ms-productos
- [ ] Agregar paginación en listados
- [ ] Implementar búsqueda y filtros
- [ ] Agregar validación de precios negativos
- [ ] Implementar soft delete en pedidos
- [ ] Agregar auditoría de cambios
- [ ] Implementar caché con Redis
- [ ] Agregar documentación con Swagger/OpenAPI

---

## 📞 Soporte

Para dudas o problemas:
1. Revisar logs de los servicios
2. Verificar conectividad a base de datos
3. Validar formato de JSON en requests
4. Comprobar que ambos servicios están corriendo

---

**Versión:** 1.0.0  
**Última actualización:** 2 de Julio, 2026  
**Autor:** Equipo de Desarrollo
