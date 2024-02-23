# Laboratorio de Software - Grupo_7

*Desarrollo de app Android para la gestión de visitas a Sistemas Participativos de Garantía*

## Proyecto realizado para la materia Laboratorio de Software 2023, UNLP

Desarrolladores:
- Facundo Miglierini
- Facundo Tomatis

Contenido: 
- La aplicación se encuentra dentro de la carpeta "spgunlp"
- Esquema de la aplicación en la carpeta "esquema"
- Documento de análisis preliminar y diseño
- Archivo de Docker Compose para probar la aplicación con mockup data

Cómo probar la app:
1. Ejecutar el archivo compose con el comando `docker-compose up`.
2. Obtener la IP privada de la PC donde se ejecutará la aplicación.
3. Dentro de Android Studio, abrir la carpeta "spgunlp". 
4. Modificar la variable `BASE_URL` ubicada dentro de "local.properties" con el siguiente valor:
`BASE_URL = http://ip:9090`, donde `ip` corresponde a la IP obtenida en el segundo paso.
5. Ejecutar la aplicación con el botón "Run" presente en Android Studio. 
6. Es posible iniciar sesión con las siguientes credenciales:
 - Mail: grupo7@tecnico.com 
 - Contraseña: grupo7

Para ver los esquemas ofrecemos el [link de figma](https://www.figma.com/proto/ztDfI0YQqjZ6CviSy24Jxl/Laboratorio-de-Software?type=design&node-id=0-1&t=bRLwStAl59l3Umno-0&scaling=min-zoom&page-id=0%3A1&starting-point-node-id=2%3A88&show-proto-sidebar=1) o como alternativa subimos las imagenes dentro de la carpeta de esquemas, el link tiene animaciones (recomendamos este).
