package com.tastytap.dao;

import com.tastytap.config.ConexionDB;
import com.tastytap.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * ============================================================
 *  UsuarioDao — Capa de acceso a datos para la tabla `usuarios`
 * ============================================================
 *
 *  DAO = Data Access Object.
 *  Esta clase es la ÚNICA que habla con la base de datos para
 *  todo lo relacionado con usuarios.
 *
 *  Los Servlets llaman a estos métodos y reciben objetos Java,
 *  sin preocuparse por SQL ni conexiones.
 *
 *  IMPORTANTE — BCrypt:
 *  Las contraseñas NUNCA se guardan en texto plano.
 *  Al registrar:  BCrypt.hashpw(pass, BCrypt.gensalt())
 *  Al verificar:  BCrypt.checkpw(passPlana, hashGuardado)
 * ============================================================
 */
public class UsuarioDao {

    // -------------------------------------------------------
    // VALIDAR LOGIN
    // -------------------------------------------------------
    /**
     * Verifica las credenciales del usuario para el login.
     *
     * Flujo:
     *  1. Busca el usuario por nombre en la BD
     *  2. Si existe, compara la contraseña plana con el hash BCrypt
     *  3. Si coincide, retorna el objeto Usuario completo (con email/teléfono)
     *  4. Si no coincide o no existe, retorna null
     *
     * NOTA: No se busca por contraseña en SQL porque BCrypt
     * genera un hash diferente cada vez. La comparación se hace en Java.
     *
     * @param nombre    Nombre de usuario del formulario
     * @param passPlana Contraseña en texto plano del formulario
     * @return Usuario encontrado, o null si las credenciales son incorrectas
     */
    public static Usuario validarLogin(String nombre, String passPlana) {
        // Solo buscamos por nombre; la contraseña la verificamos en Java con BCrypt
        String sql = """
            SELECT u.*, c.email, t.numero AS telefono
            FROM usuarios u
            LEFT JOIN correos c ON c.usuario_id = u.id AND c.tipo = 'principal'
            LEFT JOIN telefonos t ON t.usuario_id = u.id AND t.es_principal = 1
            WHERE u.nombre = ?
            LIMIT 1
            """;

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contrasena");

                    // BCrypt.checkpw compara la contraseña plana con el hash de la BD
                    // Retorna true solo si coinciden
                    if (BCrypt.checkpw(passPlana, hashGuardado)) {
                        Usuario u = new Usuario();
                        u.setId(rs.getInt("id"));
                        u.setNombre(rs.getString("nombre"));
                        u.setEdad(rs.getInt("edad"));
                        u.setRol_id(rs.getInt("rol_id"));
                        u.setEmail(rs.getString("email"));
                        u.setTelefono(rs.getString("telefono"));
                        // NOTA: NO incluimos el hash de la contraseña en el objeto
                        // que se envía al frontend. Nunca se expone.
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en validarLogin: " + e.getMessage());
        }
        return null; // Credenciales incorrectas
    }

    // -------------------------------------------------------
    // REGISTRAR NUEVO USUARIO
    // -------------------------------------------------------
    /**
     * Inserta un nuevo usuario en la BD con la contraseña hasheada.
     *
     * Flujo:
     *  1. Hashea la contraseña con BCrypt antes de guardarla
     *  2. Inserta en la tabla `usuarios`
     *  3. Si tiene email, inserta en la tabla `correos`
     *  4. Si tiene teléfono, inserta en la tabla `telefonos`
     *
     * Se usan transacciones para que si falla el email/teléfono,
     * no quede un usuario incompleto en la BD.
     *
     * @param u     Objeto Usuario con los datos del formulario
     * @param email Email opcional (puede ser null)
     * @param tel   Teléfono opcional (puede ser null)
     * @return true si se registró exitosamente, false si hubo error
     */
    public static boolean registrar(Usuario u, String email, String tel) {
        String sqlUsuario = "INSERT INTO usuarios (nombre, edad, contrasena, rol_id) VALUES (?, ?, ?, ?)";
        String sqlCorreo  = "INSERT INTO correos (usuario_id, email, tipo, verificado) VALUES (?, ?, 'principal', 0)";
        String sqlTel     = "INSERT INTO telefonos (usuario_id, numero, tipo, es_principal, verificado) VALUES (?, ?, 'celular', 1, 0)";

        Connection conn = null;
        try {
            conn = ConexionDB.MetodoConectar();
            conn.setAutoCommit(false); // Iniciar transacción manual

            // 1. Hashear la contraseña
            // BCrypt.gensalt() genera una sal aleatoria cada vez
            // El hash resultante incluye la sal, por eso no hay que guardarla aparte
            String hashPass = BCrypt.hashpw(u.getPass(), BCrypt.gensalt());

            // 2. Insertar en la tabla usuarios
            int nuevoId;
            try (PreparedStatement ps = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNombre());
                ps.setInt(2, u.getEdad());
                ps.setString(3, hashPass);    // Guardamos el hash, NO la contraseña
                ps.setInt(4, u.getRol_id());
                ps.executeUpdate();

                // Obtener el ID auto-generado del nuevo usuario
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        nuevoId = rs.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 3. Insertar email si se proporcionó
            if (email != null && !email.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlCorreo)) {
                    ps.setInt(1, nuevoId);
                    ps.setString(2, email.trim());
                    ps.executeUpdate();
                }
            }

            // 4. Insertar teléfono si se proporcionó
            if (tel != null && !tel.isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlTel)) {
                    ps.setInt(1, nuevoId);
                    ps.setString(2, tel.trim());
                    ps.executeUpdate();
                }
            }

            conn.commit(); // Confirmar todos los cambios juntos
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Nombre de usuario ya existe (índice único o lógica de negocio)
            System.err.println("⚠️ El usuario ya existe: " + e.getMessage());
            rollback(conn);
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Error en registrar: " + e.getMessage());
            rollback(conn);
            return false;
        } finally {
            cerrarConexion(conn);
        }
    }

    // -------------------------------------------------------
    // OBTENER PERFIL POR ID
    // -------------------------------------------------------
    /**
     * Obtiene todos los datos del perfil de un usuario.
     * Se usa en la pantalla de "Mi Perfil".
     *
     * @param id  ID del usuario (viene del token JWT)
     * @return    Objeto Usuario con todos sus datos, o null si no existe
     */
    public static Usuario obtenerPorId(int id) {
        String sql = """
            SELECT u.*, c.email, t.numero AS telefono
            FROM usuarios u
            LEFT JOIN correos c ON c.usuario_id = u.id AND c.tipo = 'principal'
            LEFT JOIN telefonos t ON t.usuario_id = u.id AND t.es_principal = 1
            WHERE u.id = ?
            """;

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setEdad(rs.getInt("edad"));
                    u.setRol_id(rs.getInt("rol_id"));
                    u.setEmail(rs.getString("email"));
                    u.setTelefono(rs.getString("telefono"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error en obtenerPorId: " + e.getMessage());
        }
        return null;
    }

    // -------------------------------------------------------
    // ACTUALIZAR PERFIL
    // -------------------------------------------------------
    /**
     * Actualiza los datos personales del usuario desde la pantalla
     * "Editar Perfil". Actualiza nombre, edad, email y teléfono.
     *
     * Usa transacciones para asegurar que todas las tablas se
     * actualicen juntas o no se actualice ninguna.
     *
     * @param u     Usuario con los nuevos datos (id obligatorio)
     * @return      true si se actualizó correctamente
     */
    public static boolean actualizarPerfil(Usuario u) {
        String sqlUser = "UPDATE usuarios SET nombre = ?, edad = ? WHERE id = ?";
        String sqlEmail = """
            INSERT INTO correos (usuario_id, email, tipo, verificado) VALUES (?, ?, 'principal', 0)
            ON DUPLICATE KEY UPDATE email = VALUES(email)
            """;
        String sqlTel = """
            INSERT INTO telefonos (usuario_id, numero, tipo, es_principal, verificado) VALUES (?, ?, 'celular', 1, 0)
            ON DUPLICATE KEY UPDATE numero = VALUES(numero)
            """;

        Connection conn = null;
        try {
            conn = ConexionDB.MetodoConectar();
            conn.setAutoCommit(false);

            // Actualizar nombre y edad
            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                ps.setString(1, u.getNombre());
                ps.setInt(2, u.getEdad());
                ps.setInt(3, u.getId());
                ps.executeUpdate();
            }

            // Actualizar/insertar email
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlEmail)) {
                    ps.setInt(1, u.getId());
                    ps.setString(2, u.getEmail().trim());
                    ps.executeUpdate();
                }
            }

            // Actualizar/insertar teléfono
            if (u.getTelefono() != null && !u.getTelefono().isBlank()) {
                try (PreparedStatement ps = conn.prepareStatement(sqlTel)) {
                    ps.setInt(1, u.getId());
                    ps.setString(2, u.getTelefono().trim());
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error en actualizarPerfil: " + e.getMessage());
            rollback(conn);
            return false;
        } finally {
            cerrarConexion(conn);
        }
    }

    // -------------------------------------------------------
    // CAMBIAR CONTRASEÑA
    // -------------------------------------------------------
    /**
     * Permite al usuario cambiar su contraseña desde el perfil.
     *
     * Flujo:
     *  1. Obtiene el hash actual de la BD
     *  2. Verifica que la contraseña actual sea correcta
     *  3. Hashea la nueva contraseña
     *  4. Actualiza en la BD
     *
     * @param usuarioId    ID del usuario (del token JWT)
     * @param passActual   Contraseña actual (en texto plano)
     * @param passNueva    Nueva contraseña (en texto plano)
     * @return "ok" si se cambió, "incorrecta" si la contraseña actual no coincide,
     *         "error" si hubo un problema de BD
     */
    public static String cambiarContrasena(int usuarioId, String passActual, String passNueva) {
        // 1. Obtener el hash actual
        String sqlGet = "SELECT contrasena FROM usuarios WHERE id = ?";
        String sqlUpdate = "UPDATE usuarios SET contrasena = ? WHERE id = ?";

        try (Connection conn = ConexionDB.MetodoConectar()) {

            String hashActual;
            try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return "error";
                    hashActual = rs.getString("contrasena");
                }
            }

            // 2. Verificar que la contraseña actual sea correcta
            if (!BCrypt.checkpw(passActual, hashActual)) {
                return "incorrecta"; // La contraseña actual no coincide
            }

            // 3. Hashear la nueva contraseña y actualizar
            String nuevoHash = BCrypt.hashpw(passNueva, BCrypt.gensalt());
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, nuevoHash);
                ps.setInt(2, usuarioId);
                ps.executeUpdate();
            }

            return "ok";

        } catch (SQLException e) {
            System.err.println("❌ Error en cambiarContrasena: " + e.getMessage());
            return "error";
        }
    }

    // -------------------------------------------------------
    // OBTENER TODOS LOS USUARIOS (para el Admin)
    // -------------------------------------------------------
    /**
     * Retorna todos los usuarios como texto formateado.
     * Método heredado, útil para el panel de administración.
     *
     * @return String con todos los usuarios, uno por línea
     */
    public static String obtenerTodosComoTexto() {
        StringBuilder resultado = new StringBuilder();

        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM usuarios");
             ResultSet rs = ps.executeQuery()) {

            boolean hay = false;
            while (rs.next()) {
                hay = true;
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEdad(rs.getInt("edad"));
                u.setRol_id(rs.getInt("rol_id"));
                resultado.append(u.toString()).append("\n");
            }

            if (!hay) return "No hay usuarios registrados.";

        } catch (SQLException e) {
            return "Error al consultar usuarios: " + e.getMessage();
        }

        return resultado.toString().trim();
    }

    // -------------------------------------------------------
    // ELIMINAR USUARIO (para el Admin)
    // -------------------------------------------------------
    /**
     * Elimina un usuario por su ID.
     * Solo el administrador puede hacer esto.
     *
     * @param id  ID del usuario a eliminar
     * @return true si se eliminó correctamente
     */
    public static boolean eliminarPorId(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error en eliminarPorId: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------
    // VERIFICAR SI NOMBRE YA EXISTE (para validación en registro)
    // -------------------------------------------------------
    /**
     * Comprueba si ya hay un usuario con ese nombre.
     * Se llama en el registro para dar un mensaje claro.
     *
     * @param nombre  Nombre a verificar
     * @return true si ya existe ese nombre
     */
    public static boolean existeNombre(String nombre) {
        String sql = "SELECT id FROM usuarios WHERE nombre = ? LIMIT 1";
        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true si encontró al menos uno
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // -------------------------------------------------------
    // MÉTODOS HEREDADOS (compatibilidad con código anterior)
    // -------------------------------------------------------

    /** @deprecated Usar registrar(Usuario, String, String) */
    @Deprecated
    public static boolean insertar(Usuario u) {
        return registrar(u, null, null);
    }

    /** @deprecated Usar eliminarPorId(int) */
    @Deprecated
    public static boolean eliminar(String nombre) {
        String sql = "DELETE FROM usuarios WHERE nombre = ?";
        try (Connection conn = ConexionDB.MetodoConectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // -------------------------------------------------------
    // MÉTODOS AUXILIARES PRIVADOS
    // -------------------------------------------------------

    /** Hace rollback ignorando excepciones. Llamado cuando algo falla. */
    private static void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); }
            catch (SQLException ignored) {}
        }
    }

    /** Cierra la conexión y restaura el autoCommit. */
    private static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ignored) {}
        }
    }
}