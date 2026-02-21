/**
 * registro.js
 * Maneja el envío del formulario de registro.
 *
 * Validaciones que hace el JS (ANTES de enviar al servidor):
 *  1. Que las contraseñas coincidan → muestra mensaje de error en rojo
 *  2. Que la contraseña tenga mínimo 6 caracteres
 *
 * El servidor (RegistroServlet.java) vuelve a validar todo esto también.
 * Siempre se valida en ambos lados: JS para UX rápida, Java para seguridad.
 */

document.addEventListener('DOMContentLoaded', () => {

    const form       = document.getElementById('form-registro');
    const errorPass  = document.getElementById('error-pass');
    const passInput  = document.getElementById('password');
    const confirmInput = document.getElementById('confirm-password');

    if (!form) return;

    // ----------------------------------------------------------
    // Validación en tiempo real de contraseñas
    // Muestra el mensaje de error mientras el usuario escribe
    // ----------------------------------------------------------
    confirmInput.addEventListener('input', () => {
        if (passInput.value !== confirmInput.value) {
            errorPass.style.display = 'block';   // Mostrar error
            confirmInput.setCustomValidity('Las contraseñas no coinciden.');
        } else {
            errorPass.style.display = 'none';    // Ocultar error
            confirmInput.setCustomValidity('');  // Limpiar error nativo
        }
    });

    // ----------------------------------------------------------
    // Envío del formulario
    // ----------------------------------------------------------
    form.addEventListener('submit', async (e) => {
        e.preventDefault(); // Evitar recarga de página

        // Validar contraseñas antes de enviar
        if (passInput.value !== confirmInput.value) {
            errorPass.style.display = 'block';
            confirmInput.focus();
            return; // Detener el envío
        }

        // Recoger todos los datos del formulario
        // FormData toma automáticamente todos los campos con name=""
        const formData = new URLSearchParams(new FormData(form));

        // Deshabilitar el botón para evitar doble envío
        const btnSubmit = form.querySelector('.btn-login-submit');
        btnSubmit.disabled = true;
        btnSubmit.textContent = 'Registrando...';

        try {
            // Enviar al nuevo endpoint del backend
            const response = await fetch(
                'http://localhost:8080/backend-java/api/registro',
                {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                }
            );

            const data = await response.json();

            if (data.status === 'success') {
                alert('✅ ' + data.message);
                // Redirigir al login para que el usuario inicie sesión
                window.location.href = 'login.html';
            } else {
                // Mostrar el mensaje de error del servidor
                alert('❌ ' + data.message);
            }

        } catch (error) {
            console.error('Error completo:', error);
            alert('Error: No se pudo conectar con el servidor. ¿Está encendido Tomcat?');
        } finally {
            // Rehabilitar el botón siempre, haya error o no
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Registrarme';
        }
    });
});