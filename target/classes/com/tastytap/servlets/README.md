### 1. Elimina (o deja de usar) la carpeta `servlets`
*   **¿Por qué?** En Spring Boot no usamos Servlets (`HttpServlet`). Usamos **Controllers**.
*   **El cambio:** Todas esas clases (`LoginServlet`, `RegistroServlet`, etc.) se convertirán en una carpeta llamada `controller`. Es mucho más fácil porque no tienes que configurar rutas en archivos externos y evitas los errores 404 de Tomcat.


Aun estoy en duda si eliminarlo o no asi que lo dejare pero sin utilizar