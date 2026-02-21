package com.tastytap.controller;

import com.tastytap.dao.UsuarioDao;
import com.tastytap.modelo.Usuario;
import com.tastytap.util.TokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Map<String, String> body) {
        Usuario u = new Usuario();
        u.setNombre(body.get("nombre"));
        u.setPass(body.get("pass"));
        u.setEdad(Integer.parseInt(body.get("edad")));
        u.setRol_id(2); 

        boolean exito = UsuarioDao.registrar(u, body.get("email"), body.get("telefono"));
        
        Map<String, Object> res = new HashMap<>();
        res.put("success", exito);
        res.put("mensaje", exito ? "Usuario creado" : "Error al registrar");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String nombre = body.get("nombre");
        String pass = body.get("pass");

        Usuario u = UsuarioDao.validar(nombre, pass);
        Map<String, Object> res = new HashMap<>();

        if (u != null) {
            String token = TokenUtil.createToken(u.getNombre(), String.valueOf(u.getRol_id()));
            res.put("success", true);
            res.put("token", token);
            res.put("rol", u.getRol_id());
            return ResponseEntity.ok(res);
        } else {
            res.put("success", false);
            res.put("mensaje", "Credenciales incorrectas");
            return ResponseEntity.status(401).body(res);
        }
    }
}