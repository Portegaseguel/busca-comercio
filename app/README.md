Busca Comercio

1. Descripción General

Busca Comercio es una aplicación Android desarrollada en Kotlin que permite a los usuarios 
buscar comercios locales, evaluarlos y que los emprendedores administren sus propios negocios. 
Utiliza Firebase Authentication para el manejo de usuarios y Firebase Firestore para 
almacenar comercios y valoraciones.

2. Tecnologías

Kotlin
AndroidX / Navigation Component
Material Design
Firebase Authentication
Firebase Firestore

3. Arquitectura

Navegación basada en NavHostFragment y nav_graph.xml.
Pantallas implementadas como Fragments independientes.
MainActivity controla el BottomNavigationView y su comportamiento según sesión.

4. Manejo de Usuarios (FirebaseAuth)

Registro mediante email, contraseña y nombre.
El nombre se almacena en displayName.
Inicio de sesión persistente.
Cierre de sesión.
Eliminación de cuenta, incluyendo eliminación de comercios y valoraciones asociadas.

5. Manejo de Datos (Firestore)

Se utilizan colecciones separadas, sin documentos anidados:
businesses
Cada comercio contiene:
name, description, category, ownerId (uid del usuario)
ratings
Cada valoración contiene:
userId, businessId, score, comment
Relaciones gestionadas mediante IDs (ownerId, userId, businessId).

6. Funcionalidades Principales

Registro e inicio de sesión.
Buscador de comercios.
Valoraciones de usuarios.
Administración de comercios propios.
Eliminación completa de cuenta y datos.
Pantalla de cuenta personalizada.

7. Testing

La aplicación cuenta con pruebas automáticas que validan tanto la lógica como la interfaz.
Se implementaron 2 tests unitarios (validación de formato de email y formateo de nombre) 
y 2 tests de UI con Espresso (verificación del texto principal de la pantalla inicial y 
la presencia del botón de acceso a inicio de sesión). Todas las pruebas se ejecutan 
correctamente.

8. Conclusión

Busca Comercio implementa navegación moderna, manejo profesional de Firebase, diseño 
consistente y funcionalidades completas que cumplen y superan los requisitos del 
bootcamp: registro de usuarios, valoraciones y administración de comercios.