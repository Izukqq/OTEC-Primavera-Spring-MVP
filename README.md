MVP OTEC PRIMAVERA - SISTEMA DE GESTIÓN ACADÉMICA
1. DESCRIPCIÓN DEL PROYECTO
Este proyecto es un Producto Mínimo Viable (MVP) diseñado para la OTEC "Primavera". La plataforma permite la digitalización de la gestión de programas de capacitación, ofreciendo trazabilidad en tiempo real del progreso académico de los estudiantes y centralizando la administración educativa en un entorno web robusto.

2. STACK TECNOLÓGICO
El sistema ha sido construido bajo los estándares modernos de desarrollo en Java, garantizando escalabilidad y mantenibilidad:

Lenguaje: Java 21

Framework Principal: Spring Boot 3.x

Gestión de Dependencias: Maven

Persistencia de Datos: PostgreSQL + Spring Data JPA

Motor de Plantillas: Thymeleaf (Vistas dinámicas y fragmentos)

Seguridad: Spring Security (Control de acceso por roles)

Interoperabilidad: API REST (Intercambio de datos en formato JSON)

3. ARQUITECTURA DEL SISTEMA
Se ha implementado una arquitectura desacoplada basada en el patrón Modelo-Vista-Controlador (MVC) y el principio de responsabilidad única:

Capa de Modelo (Entities): Mapeo de objetos a tablas relacionales (Estudiante, Curso, Evaluacion, Nota).

Capa de Repositorio: Interfaces de persistencia que gestionan las consultas a PostgreSQL.

Capa de Servicio (@Service): Implementación de la lógica de negocio y gestión de transacciones.

Capa de Controlador: Manejo de peticiones web y endpoints de la API.

4. SEGURIDAD Y CONTROL DE ACCESO
La seguridad es un pilar fundamental del sistema, gestionada mediante Spring Security:

Rol Administrador (ADMIN): Gestión total de la oferta educativa, creación de cursos, registro de evaluaciones y asignación de calificaciones. Acceso completo a la API REST.

Rol Estudiante (USER): Acceso exclusivo a un portal de autogestión donde puede visualizar su matrícula, su barra de progreso dinámica y el detalle de sus notas.

Cifrado de Datos: Las credenciales de acceso se almacenan utilizando algoritmos de hashing BCrypt, cumpliendo con las mejores prácticas de seguridad.

5. LÓGICA DE NEGOCIO: TRAZABILIDAD DINÁMICA
El sistema ofrece un cálculo de progreso académico en tiempo real. El avance del estudiante no es un valor estático, sino que se calcula bajo la siguiente lógica:

Progreso = (Cantidad de Notas Registradas / Total de Evaluaciones del Curso) * 100

Este enfoque asegura que el progreso refleje fielmente los hitos cumplidos por el alumno. Si el administrador añade una nueva evaluación al programa, el progreso se ajusta automáticamente para todos los alumnos matriculados.

6. INTEROPERABILIDAD (API REST)
El sistema está preparado para futuras integraciones (como aplicaciones móviles) mediante los siguientes endpoints protegidos (solo acceso ADMIN):

GET /api/estudiantes: Retorna la lista completa de alumnos en JSON.

GET /api/estudiantes/{id}/evaluaciones: Retorna el detalle de notas de un estudiante específico en JSON.

7. INSTALACIÓN Y EJECUCIÓN
Requisitos: Tener instalado Java 21, Maven y PostgreSQL.

Base de Datos: Crear una base de datos denominada db_otec_primavera.

Configuración: Actualizar las credenciales de base de datos en el archivo src/main/resources/application.properties.

Compilación y Ejecución:

Bash
mvn clean install
mvn spring-boot:run
