# GeoEventosDesktop

Cliente de escritorio del ecosistema **GeoEventos** — plataforma B2B de gestión de eventos geolocalizados.

Aplicación nativa construida con **Java 23 + Swing + JavaFX**, con mapa interactivo embebido via Leaflet/OpenStreetMap y una suite completa de tests automatizados.

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 23+ |
| UI | Swing + FlatLaf |
| Mapa | Leaflet.js + OpenStreetMap (embebido via JavaFX WebView) |
| HTTP | Java HttpClient |
| Imágenes | ImgBB API |
| Build | Maven |
| Tests | JUnit 5 + Mockito + WireMock + AssertJ Swing |

---

## Funcionalidades

* Visualizar eventos en tabla con ID, nombre, valor y ubicación
* Buscar eventos por nombre o ubicación
* Crear eventos con nombre, descripción, vigencia, valor, ubicación, coordenadas y foto
* Modificar y eliminar eventos con confirmación
* Ver detalles de evento en modo lector
* Subir fotos a ImgBB y obtener enlace directo
* **Vista de mapa** con marcadores geolocalizados para cada evento
* Click en un marcador para ver detalles del evento
* Click en el mapa para asignar coordenadas a un nuevo evento

---

## Cómo correr el proyecto

### Requisitos

* JDK 23+
* Maven 3.8+
* [GeoEventosAPI](https://github.com/AlfredoSWDev/GeoEventosAPI) corriendo localmente (o apuntando a producción)

### Pasos

**1. Clonar el repositorio:**
```bash
git clone https://github.com/AlfredoSWDev/GeoEventosDesktop.git
```

**2. Compilar y ejecutar:**
```bash
cd GeoEventosDesktop
mvn javafx:run
```

---

## Tests

27 tests cubriendo las capas principales de la aplicación.

```bash
mvn test
```

En entornos sin pantalla gráfica (servidores, CI/CD):

```bash
Xvfb :99 &
export DISPLAY=:99
mvn test
```

| Capa | Herramientas |
|------|-------------|
| Unitarios | JUnit 5 + Mockito |
| Integración HTTP | WireMock |
| UI Swing | AssertJ Swing |

---

## Roadmap

- [x] CRUD completo de eventos
- [x] Vista de mapa con Leaflet/OpenStreetMap
- [x] Click en marcador para ver detalles
- [x] Click en mapa para asignar coordenadas
- [x] Subida de fotos a ImgBB
- [x] 27 tests (JUnit 5 + Mockito + WireMock + AssertJ Swing)
- [ ] Filtrar eventos por categoría, fecha y distancia
- [ ] Panel de estadísticas del cliente
- [ ] Despliegue en AWS

---

## Parte del ecosistema GeoEventos

| Repositorio | Descripción |
|-------------|-------------|
| [GeoEventosAPI](https://github.com/AlfredoSWDev/GeoEventosAPI) | Spring Boot 4 + Java 21 + PostgreSQL |
| [GeoEventosWeb](https://github.com/AlfredoSWDev/GeoEventosWeb) | Kotlin/Wasm + Compose for Web |
| [GeoEventosAndroid](https://github.com/AlfredoSWDev/GeoEventosAndroid) | Kotlin + Jetpack Compose + OSMDroid |
| **GeoEventosDesktop** | Java 23 + Swing + JavaFX ← aquí |
| [GeoEventosDB](https://github.com/AlfredoSWDev/GeoEventosDB) | Schema y migraciones PostgreSQL |

---

## Licencia

Este proyecto está licenciado bajo la licencia MIT. Ver el archivo [LICENSE](LICENSE) para detalles.

---

## Autor

**Alfredo Sanchez** — [@AlfredoSWDev](https://github.com/AlfredoSWDev)

📺 Stream de desarrollo en [Twitch](https://twitch.tv/AlfredoSWDev) · [YouTube](https://youtube.com/@AlfredoSWDev)
