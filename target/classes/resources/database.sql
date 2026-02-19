create database Tastytap;
use Tastytap;

-- 1. ROLES
create table roles (
    id int primary key,
    nombre varchar(50) not null,
    descripcion text
);

-- 2. USUARIOS
create table usuarios (
    id int primary key auto_increment,
    nombre varchar(100) not null,
    edad int,
    contrasena varchar(255) not null,
    rol_id int not null,
    foreign key (rol_id) references roles(id)
);

-- 3. CORREOS
create table correos (
    id int primary key auto_increment,
    usuario_id int not null,
    email varchar(155) not null,
    tipo varchar(20) not null,
    verificado tinyint not null,
    foreign key (usuario_id) references usuarios(id)
);

-- 4. TELEFONOS
create table telefonos (
    id int primary key auto_increment,
    usuario_id int not null,
    numero varchar(20) not null,
    tipo varchar(20) not null,
    es_principal tinyint not null,
    verificado tinyint not null,
    foreign key (usuario_id) references usuarios(id)
);

-- 5. EMPRENDIMIENTOS (Depende de usuario - Generalmente el Proveedor)
create table emprendimientos (
    id int primary key auto_increment,
    usuario_id int not null,
    nombre varchar(100) not null,
    descripcion text,
    ubicacion varchar(255),
    activo tinyint not null,
    foreign key (usuario_id) references usuarios(id)
);

-- 6. CATEGORIAS
create table categorias (
    id int primary key,
    nombre varchar(50) not null
);

-- 7. PRODUCTOS
create table productos (
    id int primary key auto_increment,
    emprendimiento_id int not null,
    categoria_id int not null,
    nombre varchar(150) not null,
    descripcion text,
    precio decimal(10,2) not null,
    activo tinyint not null,
    capacidad_diaria int not null,
    vendido_hoy int not null,
    foreign key (emprendimiento_id) references emprendimientos(id),
    foreign key (categoria_id) references categorias(id)
);

-- 8. IMAGENES PRODUCTOS
create table imagenes_productos (
    id int primary key auto_increment,
    producto_id int not null,
    ruta varchar(255) not null,
    orden int not null,
    foreign key (producto_id) references productos(id)
);

-- 9. PROMOCIONES
create table promociones (
    id int primary key auto_increment,
    nombre varchar(100) not null,
    descripcion text,
    descuento_porcentaje decimal(5,2),
    fecha_inicio datetime not null,
    fecha_fin datetime not null,
    activo tinyint not null
);

-- 10. PROMOCION PRODUCTO
create table promocion_producto (
    promocion_id int not null,
    producto_id int not null,
    primary key (promocion_id, producto_id),
    foreign key (promocion_id) references promociones(id),
    foreign key (producto_id) references productos(id)
);

-- 11. PEDIDOS
create table pedidos (
    id int primary key auto_increment,
    cliente_id int not null,
    registrador_id int not null,
    fecha_hora datetime not null,
    estado varchar(50) not null,
    total decimal(10,2) not null,
    foreign key (cliente_id) references usuarios(id),
    foreign key (registrador_id) references usuarios(id)
);

create table consulta (
id int primary key auto_increment,
id_pedido int not null,
id_emprendimiento int not null,
vendido_hoy int not null,
foreign key (id_pedido) references pedidos(id),
foreign key (id_emprendimiento) references emprendimientos(id)
);

-- 12. DETALLES PEDIDOS
create table detalles_pedidos (
    id int primary key auto_increment,
    pedido_id int not null,
    producto_id int not null,
    promocion_id int,
    cantidad int not null,
    descuento_aplicado decimal(10,2) not null,
    subtotal decimal(10,2) not null,
    foreign key (pedido_id) references pedidos(id),
    foreign key (producto_id) references productos(id),
    foreign key (promocion_id) references promociones(id)
);

-- 13. PAGOS
create table pagos (
    id int primary key auto_increment,
    pedido_id int not null,
    metodo_id int not null,
    fecha_pago datetime not null,
    estado varchar(50) not null,
    validado_por int,
    fecha_validacion datetime,
    foreign key (pedido_id) references pedidos(id),
    foreign key (validado_por) references usuarios(id)
);

-- 14. NOTIFICACIONES
create table notificaciones (
    id int primary key auto_increment,
    usuario_id int not null,
    titulo varchar(150) not null,
    mensaje text not null,
    tipo varchar(50) not null,
    prioridad varchar(20) not null,
    requiere_confirmacion tinyint not null,
    fecha_envio datetime not null,
    foreign key (usuario_id) references usuarios(id)
);

-- 15. PERMISOS
create table permisos (
    id int primary key auto_increment,
    codigo varchar(50) not null,
    descripcion varchar(255) not null
);

-- 16. ROL PERMISO
create table rol_permiso (
    rol_id int not null,
    permiso_id int not null,
    primary key (rol_id, permiso_id),
    foreign key (rol_id) references roles(id),
    foreign key (permiso_id) references permisos(id)
);

-- Índices
create index idx_usuario_rol on usuarios(rol_id);
create index idx_correo_usuario on correos(usuario_id);
create index idx_telefono_usuario on telefonos(usuario_id);
create index idx_producto_categoria on productos(categoria_id);
create index idx_pedido_cliente on pedidos(cliente_id);
create index idx_consulta_pedido on consulta(id_pedido);
create index idx_consulta_emprendimiento on consulta(id_emprendimiento);
create index idx_pago_pedido on pagos(pedido_id);

-- INSERCIÓN DE DATOS INICIALES

insert into roles (id, nombre, descripcion) values
(1, 'administrador', 'Gestiona todo el sistema'),
(2, 'cliente', 'Realiza compras y consulta historial'),
(3, 'proveedor', 'Gestiona sus propios productos y emprendimiento');

insert into categorias (id, nombre) values
(1, 'hamburguesa'), (2, 'pasta'), (3, 'sushi'), (4, 'pizza');

-- Usuarios
insert into usuarios (nombre, edad, contrasena, rol_id) values
('Admin TastyTap', 28, 'admin123', 1),
('Carlos Proveedor', 35, 'prov123', 3),
('Ana Cliente', 22, 'ana123', 2);

-- esto es para que muestre una tabla de usuarios
select * from usuarios;

-- este es para eliminar un usuario en especifico
delete from usuarios where id IN (30);

-- este funciona para eliminar varios usuarios desde un rango en especifico
DELETE FROM usuarios WHERE id between 5 AND 29;

-- El emprendimiento ahora depende de un usuario (en este caso, el proveedor Carlos ID: 2)
insert into emprendimientos (usuario_id, nombre, descripcion, ubicacion, activo) values
(2, 'Tasty Tap Central', 'Venta exclusiva de hamburguesas, pastas, sushi y pizza', 'Bucaramanga, Santander', 1);

insert into permisos (codigo, descripcion) values
('ver_balance', 'Ver reportes financieros'),
('editar_productos', 'Crear/modificar productos'),
('gestionar_promociones', 'Crear y asociar promociones'),
('validar_pagos', 'Confirmar/rechazar pagos'),
('gestionar_emprendimiento', 'Activar/desactivar el negocio'),
('exportar_pdf', 'Descargar reportes'),
('ver_historial_ajeno', 'Ver pedidos de otros usuarios'),
('realizar_pedido', 'Permite comprar productos');

-- Permisos para el Administrador
insert into rol_permiso (rol_id, permiso_id) values (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7);
-- Permisos para el Proveedor (Solo puede gestionar lo suyo)
insert into rol_permiso (rol_id, permiso_id) values (3, 2), (3, 3), (3, 4), (3, 5);
-- Permisos para el Cliente
insert into rol_permiso (rol_id, permiso_id) values (2, 8);