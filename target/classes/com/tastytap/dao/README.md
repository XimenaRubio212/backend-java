### 4. Transforma tu `dao/UsuarioDao.java`
*   **¿Por qué?** Aunque puedes seguir usándolo, lo profesional es usar **Spring Data JPA**.
*   **El cambio:** En lugar de escribir todo el código SQL a mano (`SELECT * FROM...`), solo creas una "Interface" y Spring hace las consultas por ti.