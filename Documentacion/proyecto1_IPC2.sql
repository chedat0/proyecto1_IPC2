CREATE DATABASE proyecto1_IPC2;
USE proyecto1_IPC2;


CREATE TABLE usuario (
	id_usuario INT auto_increment PRIMARY KEY,
    usuario VARCHAR (50) NOT NULL UNIQUE,
    contra_hasheada VARCHAR (300) NOT NULL,
    nombre_completo VARCHAR (200) NOT NULL,
    rol INT NOT NULL COMMENT '1=Atencion al cliente, 2=Operaciones, 3=Administrador',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE destino (
	id_destino INT auto_increment PRIMARY KEY,
    nombre VARCHAR (200) NOT NULL UNIQUE,
    pais VARCHAR(100) NOT NULL,
    descripcion TEXT,
    clima VARCHAR (100),
    mejor_epoca VARCHAR (150),
    url_imagen VARCHAR (500),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE proveedor (
	id_proveedor INT auto_increment PRIMARY KEY,
    nombre VARCHAR (200) NOT NULL UNIQUE,
    tipo_servicio INT NOT NULL COMMENT '1=Aerolinea, 2=Hotel, 3=Tour, 4=Traslado, 5=Otro',
    pais_operacion VARCHAR (100),
    telefono VARCHAR (30),
    email VARCHAR (150),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE paquete (
	id_paquete INT auto_increment PRIMARY KEY,
    nombre VARCHAR (200) NOT NULL UNIQUE,
    id_destino INT NOT NULL,
    duracion_dias INT NOT NULL,
    descripcion TEXT,
    precio_venta DECIMAL (10,2) NOT NULL,
    capacidad_maxima INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_paquete_destino FOREIGN KEY (id_destino) REFERENCES destino(id_destino)
);

CREATE TABLE servicios_paquete (
	id_servicio INT auto_increment PRIMARY KEY,
    id_paquete INT NOT NULL,
    id_proveedor INT NOT NULL,
    descripcion VARCHAR (500) NOT NULL,
    costo_proveedor DECIMAL (10,2) NOT NULL,
    CONSTRAINT fk_servicio_paquete FOREIGN KEY (id_paquete) REFERENCES paquete(id_paquete),
    CONSTRAINT fk_servicio_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
);

CREATE TABLE cliente (
	id_cliente INT auto_increment PRIMARY KEY,
    dpi_pasaporte VARCHAR (30) NOT NULL UNIQUE,
    nombre_completo VARCHAR (200) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR (30),
    email VARCHAR (150),
    nacionalidad VARCHAR (100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME NOT NULL DEFAULT current_timestamp
);

CREATE TABLE reservacion (
	id_reservacion INT auto_increment PRIMARY KEY,
    numero_reservación VARCHAR (30) NOT NULL UNIQUE,
    id_paquete INT NOT NULL,
    id_agente INT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_viaje DATE NOT NULL,
    cantidad_pasajeros INT NOT NULL,
    costo_total DECIMAL (10,2) NOT NULL,
    estado ENUM ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_reservacion_paquete FOREIGN KEY (id_paquete) REFERENCES paquete (id_paquete),
    CONSTRAINT fk_reservacion_agente FOREIGN KEY (id_agente) REFERENCES usuario (id_usuario)
);

CREATE TABLE reservacion_pasajero (
	id_reservacion INT NOT NULL,
    id_cliente INT NOT NULL,
    PRIMARY KEY (id_reservacion, id_cliente),
    CONSTRAINT fk_rp_reservacion FOREIGN KEY (id_reservacion) REFERENCES reservacion (id_reservacion),
    CONSTRAINT fk_rp_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
);

CREATE TABLE pago (
	id_pago INT auto_increment PRIMARY KEY,
    id_reservacion INT NOT NULL,
    monto DECIMAL (10,2) NOT NULL,
    metodo_pago INT NOT NULL COMMENT '1=Efectivo, 2=Tarjeta, 3=Transferencia',
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_comprobante VARCHAR(50),
    CONSTRAINT fk_pago_reservacion FOREIGN KEY (id_reservacion) REFERENCES reservacion (id_reservacion)
);

CREATE TABLE cancelacion (
	id_cancelacion INT auto_increment PRIMARY KEY,
    id_reservacion INT NOT NULL UNIQUE,
    fecha_cancelacion INT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(1500),
    monto_pagado DECIMAL(10,2) NOT NULL,
    porcentaje_reembolso DECIMAL (5,2) NOT NULL,
    monto_reembolso DECIMAL (10,2) NOT NULL,
    perdida_agencia DECIMAL (10,2) NOT NULL,
    CONSTRAINT fk_cancelacion_reserva FOREIGN KEY (id_reservacion) REFERENCES reservacion (id_reservacion) 
);

CREATE TABLE log_carga (
	id_log INT auto_increment PRIMARY KEY,
    id_usuario INT NOT NULL,
    nombre_archivo VARCHAR(300),
    fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registros_procesados INT NOT NULL DEFAULT 0,
    registros_exitosos INT NOT NULL DEFAULT 0,
    registros_erroneos INT NOT NULL DEFAULT 0,
    errores_detalle TEXT,
    CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);


