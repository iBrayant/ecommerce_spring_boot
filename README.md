# Ecommerce Spring Boot

Proyecto de e-commerce construido con microservicios en **Java 21** y **Spring Boot**, con persistencia en **PostgreSQL**.

> 🚧 **Proyecto en desarrollo.** Las funcionalidades y la estructura pueden cambiar.

## Servicios

- **`auth`**: gestión de usuarios. Registro, inicio de sesión, consulta, actualización y eliminación de usuarios, y manejo del saldo de cada uno (consultar, recargar y descontar). Las contraseñas se almacenan cifradas.
- **`catalogo`**: catálogo de productos y carrito de compras. Permite administrar productos, agregar, modificar y quitar ítems del carrito y realizar la compra. Para validar y descontar el saldo del comprador se comunica con el servicio `auth`.

## Características

- Arquitectura de microservicios independientes, cada uno con su propia base de datos.
- Arquitectura limpia (hexagonal): dominio, casos de uso, y adaptadores de entrada y salida separados.
- API REST.
- Comunicación entre servicios mediante HTTP.
- Pruebas unitarias de los casos de uso.

## Tecnologías

Java 21 · Spring Boot · Spring Data JPA · Spring Security · PostgreSQL · Lombok · Maven
