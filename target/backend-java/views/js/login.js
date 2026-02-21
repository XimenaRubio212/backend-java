document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.login-form');

    if (!form) return;

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const formData = new URLSearchParams(new FormData(form));

        try {
            const response = await fetch('http://localhost:8080/backend-java-1.0-SNAPSHOT/api/login', {
                method: 'POST',
                body: formData,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            const data = await response.json();

            if (data.status === 'success') {
                // 1. Separar el token de la URL que enviamos desde Java
                const [token, redirectUrl] = data.token.split('|');

                // 2. Guardar token en el navegador (Punto 6)
                localStorage.setItem('tastytap_token', token);
                localStorage.setItem('user_role', data.data.rol_id); // Guardamos el rol para validaciones rápidas

                alert("👋 " + data.message);

                // 3. Redirección Dinámica (Puntos 7 y 9)
                // Usamos la ruta que nos mandó el servidor
                window.location.href = `../../${redirectUrl}`; 
                
            } else {
                alert("❌ " + data.message);
            }
        } catch (error) {
            console.error("Detalle del error:", error);
            alert("Error: No se pudo conectar con el servidor Java.");
        }
    });
});