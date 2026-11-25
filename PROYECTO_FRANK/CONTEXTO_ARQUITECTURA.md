# Contexto de arquitectura para desarrollar en otro proyecto

Usa esta guía para decidir dónde ubicar el código y cómo conectar sus partes. Obtén los nombres, las operaciones, los datos y los contratos públicos de los requisitos del proyecto destino. Si ya existe código, identifica primero sus módulos, su vocabulario y sus convenciones.

## Cómo se organiza la referencia

El repositorio agrupa tres servicios por capacidad de negocio. Cada directorio de servicio contiene una aplicación Spring Boot ejecutable, su propio `pom.xml`, Maven Wrapper, configuración y pruebas. La compilación se realiza desde cada servicio. Dos servicios administran datos en bases PostgreSQL propias. El tercero resuelve una integración externa y prescinde de persistencia. La comunicación entre servicios que colaboran se realiza por HTTP.

Dentro de cada servicio se repite una arquitectura de **puertos y adaptadores**. El dominio define qué operación necesita del exterior mediante una interfaz o *puerto*. Un adaptador implementa esa interfaz con la tecnología correspondiente. Así, un cambio de cliente HTTP o de almacenamiento queda localizado mientras se conserve el contrato que usa el dominio. Esta organización interna también se puede aplicar a un módulo dentro de una sola aplicación.

## Mapa de carpetas dentro de una unidad

El árbol reúne las carpetas observadas. Cada unidad incorpora las que corresponden a sus responsabilidades.

```text
<unidad>/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
└── src/
    ├── main/
    │   ├── java/<paquete-base>/
    │   │   ├── <clase de arranque>
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   └── gateway/
    │   │   │   ├── usecase/
    │   │   │   └── exception/
    │   │   ├── application/           # Configuración directa o en config/
    │   │   └── infraestructure/
    │   │       ├── entry_points/
    │   │       ├── driver_adapters/
    │   │       └── mapper/
    │   └── resources/application.properties
    └── test/java/<paquete-base>/
```

`infraestructure` es la grafía del repositorio de referencia. En el proyecto destino usa la convención de nombres que ya tenga.

| Ubicación | Responsabilidad |
| --- | --- |
| `domain/model` y `domain/exception` | Expresan los conceptos, estados, resultados y errores con el vocabulario del negocio. |
| `domain/usecase` | Implementa operaciones completas. Aquí viven las validaciones de negocio, los cálculos y la coordinación de dependencias. |
| `domain/model/gateway` | Declara las operaciones de salida que necesitan los casos de uso. Sus firmas expresan lo que necesita el negocio en términos propios. |
| `application` | Ensambla la aplicación. Las clases de configuración de Spring crean los casos de uso mediante métodos `@Bean` y les pasan por constructor las implementaciones de sus puertos. |
| `entry_points` | Recibe solicitudes, invoca el caso de uso y traduce su resultado o error a una respuesta HTTP. |
| `driver_adapters` | Implementa los puertos. En persistencia agrupa entidades JPA, repositorios Spring Data e implementaciones de los puertos. En integraciones agrupa el cliente, sus tipos de intercambio y la lógica de adaptación. Cuando crece el servicio, subdivide por tecnología y concepto. |
| `mapper` | Convierte explícitamente entre el modelo de dominio y la representación de persistencia cuando ambos tienen estructuras o ciclos de vida propios. |
| `resources` y `test` | La configuración pertenece a cada aplicación. Las pruebas se organizan bajo el paquete del componente que ejercitan. |

## Cómo recorre el código una operación

Un controlador recibe la entrada y llama a un caso de uso. Este comprueba las reglas y, si necesita leer, guardar o consultar otro sistema, llama al puerto que representa esa necesidad. En ejecución, Spring le ha inyectado un adaptador concreto. El adaptador realiza la operación técnica y devuelve un resultado que el caso de uso puede interpretar. El controlador construye la respuesta final.

La dirección de las dependencias en el código importa tanto como el recorrido en ejecución. El caso de uso conoce la interfaz del puerto y el adaptador también la conoce para implementarla. Los detalles de conexión, las entidades de base de datos y los formatos del proveedor quedan en el borde correspondiente. Una prueba puede construir ese mismo caso de uso con un sustituto del puerto y verificar sus reglas, incluso cuando los sistemas externos están desconectados.

## Dependencias y herramientas observadas

| Alcance en la referencia | Tecnologías y función |
| --- | --- |
| Todos los servicios | Java 21, Maven con Wrapper y Spring Boot 4.1.0. Spring Web MVC expone las entradas HTTP. Lombok genera código repetitivo durante la compilación. El plugin Maven de Spring Boot empaqueta la aplicación. |
| Servicios con almacenamiento relacional | Spring Data JPA implementa repositorios sobre entidades de Jakarta Persistence. El controlador JDBC de PostgreSQL se carga en ejecución. |
| Adaptadores HTTP de salida | Un servicio usa `RestTemplate` y otro usa Spring Cloud OpenFeign como cliente declarativo. El segundo gestiona las versiones de Spring Cloud mediante su BOM 2025.1.4. Son elecciones locales a cada integración. |
| Adaptador de seguridad | Un servicio declara `spring-security-web` y usa `BCryptPasswordEncoder` para calcular y verificar hashes de contraseñas detrás de un puerto. |
| Pruebas y cobertura | JUnit Jupiter ejecuta las pruebas y Mockito sustituye colaboradores. Los servicios declaran soporte de pruebas de Spring Boot para Web MVC y, donde corresponde, para JPA. Un servicio configura JaCoCo 0.8.13 para generar el informe de cobertura. |

Las versiones documentan la referencia. En el proyecto destino, conserva las versiones compatibles con su plataforma y añade cada dependencia al módulo que utiliza su capacidad.

## Cómo aplicar el patrón en el proyecto destino

1. **Identifica una operación real.** Describe qué recibe, qué regla debe cumplir y qué resultado espera quien la solicita. Usa el lenguaje de los requisitos para nombrar el caso de uso y sus modelos.
2. **Ubícala en la unidad que posee esa regla.** Si el proyecto ya tiene módulos o servicios, aprovecha sus límites. Decide la separación en procesos según sus necesidades de despliegue y propiedad de datos. La cantidad de servicios de la referencia es circunstancial.
3. **Define los puertos a partir de las dependencias de la operación.** Si basta un cálculo local, impleméntalo en el dominio. Si la operación requiere un efecto externo, expresa la capacidad en una interfaz e implementa el detalle en un adaptador.
4. **Conecta el recorrido.** Usa el mecanismo de entrada previsto en el proyecto, registra las dependencias en su punto de configuración y traduce modelos en los límites que tengan formatos propios. Crea las carpetas a medida que tengan código que alojar.
5. **Comprueba la separación con pruebas.** Ejercita el resultado esperado y los errores de negocio sustituyendo los puertos. Para cambios en un adaptador, prueba la conversión, consulta o llamada que ese adaptador implementa.

Al terminar una funcionalidad, debería ser posible señalar dónde se decide su regla, qué contrato expresa cada dependencia externa y qué pieza concreta habría que cambiar si cambia esa tecnología.
