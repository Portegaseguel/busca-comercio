# BuscaComercio

Aplicación móvil Android que permite buscar comercios locales, ver información detallada y que los emprendedores administren sus propios negocios dentro de la aplicación. Este proyecto fue desarrollado como parte del bootcamp, 
complementado por un informe completo de análisis y diseño.

---

## Descripción del proyecto

BuscaComercio es una aplicación que conecta a usuarios con pequeños comercios locales.  
Permite:

- Buscar comercios por nombre o rubro.
- Ver información detallada: dirección, contacto, horario, redes sociales.
- Crear, editar y eliminar comercios (para usuarios emprendedores).
- Valorar negocios.
- Eliminar la cuenta con limpieza de datos asociada.
- Autenticación segura con Firebase.

El proyecto incluye datos de comercios demo precargados para facilitar la revisión.

---

## Tecnologías utilizadas

- **Kotlin**
- **Android Jetpack**
  - Navigation Component  
  - ViewBinding  
  - Material Design  
- **Firebase Authentication**
- **Firebase Firestore**
- **JUnit** (tests unitarios)
- **Espresso** (tests UI)

---

## Descargar APK

Instala la aplicación en tu dispositivo Android desde el enlace oficial:

**[Descargar APK – BuscaComercio v1.0](https://github.com/Portegaseguel/busca-comercio/releases/download/v1.0/app-debug.apk)**

---

## Instalación y ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Portegaseguel/busca-comercio.git
2. Abrir el proyecto en Android Studio.
3. Agregar tu archivo google-services.json dentro de:
    app/
4. Sincronizar el proyecto con Gradle.
5. Ejecutar en un emulador o dispositivo real.

---

## Estructura del proyecto

- app/src/main/java/... → Código fuente de la aplicación
- app/src/main/res/ → Layouts, colores, y recursos gráficos
- app/src/test/ → Tests unitarios
- app/src/androidTest/ → Tests UI con Espresso
- DemoDataSeeder.kt → Script que genera comercios demo en Firestore
- MainActivity.kt → Integración de navegación y bottom menu

---

## Estado del proyecto

Funcionalidades completas
Tests unitarios y UI
APK publicada
Datos demo cargados
Proyecto documentado

---

## Autora

Paulina Ortega Seguel
GitHub: https://github.com/Portegaseguel

