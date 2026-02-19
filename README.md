# TastyTap - API Backend 🍔🥤

Este es el repositorio del **Backend** para el proyecto **TastyTap**, un sistema de gestión de pedidos para emprendimientos gastronómicos. Desarrollado como parte de la formación en el SENA.

El proyecto está construido en Java utilizando una arquitectura multicapa (DAO, Modelo, Servlets) y se comunica con una base de datos MySQL.

## 🚀 Tecnologías Utilizadas

*   **Java 17**: Lenguaje de programación principal.
*   **Maven**: Gestión de dependencias y construcción del proyecto.
*   **Jakarta Servlet API 6.0**: Para el manejo de peticiones HTTP.
*   **MySQL 8.0**: Sistema de gestión de base de datos.
*   **Tomcat 10+**: Servidor de aplicaciones (gestionado mediante el plugin Cargo).
*   **Gson**: Librería para la conversión de objetos Java a formato JSON.

## 📋 Requisitos Previos

Antes de empezar, asegúrate de tener instalado:
*   [Java JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
*   [Apache Maven](https://maven.apache.org/download.cgi)
*   [MySQL Server 8.x](https://dev.mysql.com/downloads/mysql/)

## 🗄️ Configuración de la Base de Datos

1. El script de la base de datos se encuentra en la carpeta: `/database/tastytap.sql`.
2. Abre tu cliente de MySQL (Workbench, Terminal o phpMyAdmin).
3. Crea la base de datos:
   ```sql
   CREATE DATABASE Tastytap;
   USE Tastytap;
   ```
4. Ejecuta el script SQL para crear las tablas y cargar los datos iniciales (roles, categorías y permisos).

> **Nota:** Recuerda actualizar las credenciales de conexión (usuario y contraseña) en tu clase de configuración de base de datos en `src/main/java/com/tastytap/config/`.

## 🛠️ Instalación y Ejecución

Sigue estos pasos para levantar el servidor:

1. Clona este repositorio:
   ```bash
   git clone https://github.com/tu-usuario/backend-java.git
   ```
2. Entra a la carpeta del proyecto:
   ```bash
   cd backend-java
   ```
3. Ejecuta el servidor embebido con Maven:
   ```bash
   mvn cargo:run
   ```
4. Una vez que la terminal indique que el servidor está listo, la API estará disponible en:
   `http://localhost:8080/backend-java/`

## 💻 Ejecución en otros equipos (Sin instalar Tomcat)

Una de las ventajas de este proyecto es que utiliza un **Servidor Tomcat Embebido**. Esto significa que cualquier persona que clone el repositorio puede ponerlo en marcha sin necesidad de instalar, configurar o mover carpetas de servidores externos.

### Pasos para ejecutar en un equipo nuevo:

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/tu-usuario/backend-java.git
    ```

2.  **Requisitos mínimos:**
    Solo es necesario que el equipo tenga instalado:
    *   **Java JDK 17** (Configurado en las variables de entorno).
    *   **Maven** (O usar el ejecutable de Maven si el IDE lo incluye).

3.  **Lanzar el servidor:**
    Desde la terminal, en la raíz del proyecto, ejecuta:
    ```bash
    mvn cargo:run
    ```

4.  **¿Qué hará Maven automáticamente?**
    *   Descargará las librerías necesarias (MySQL Connector, Gson, etc.).
    *   **Descargará una instancia limpia de Tomcat 10** específica para este proyecto.
    *   Compilará el código Java.
    *   Desplegará la aplicación en el puerto `8080`.

> **IMPORTANTE:** La primera vez que se ejecute en un equipo nuevo, puede tardar un par de minutos mientras Maven descarga el servidor Tomcat por única vez. Una vez que veas el mensaje `Tomcat 10.x Embedded started`, la API estará lista para recibir peticiones del Frontend.

## 📁 Estructura del Proyecto

*   `src/main/java/com/tastytap/`
    *   `config/`: Clases de configuración (Conexión BD).
    *   `dao/`: Objetos de Acceso a Datos (Consultas SQL).
    *   `modelo/`: Clases POJO (Entidades).
    *   `servlets/`: Controladores que reciben las peticiones del Frontend.
*   `database/`: Contiene el script `.sql` de la base de datos.
*   `pom.xml`: Configuración de dependencias de Maven.

## 🛡️ CORS (Importante para el Frontend)

Este backend está configurado para aceptar peticiones desde el frontend. Si tienes problemas de conexión, asegúrate de que los Servlets tengan habilitados los encabezados de `Access-Control-Allow-Origin`.