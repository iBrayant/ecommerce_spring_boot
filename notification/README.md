# Microservicio de Notificaciones (`notification`)

Envía **notificaciones por SMS** a los usuarios del e-commerce usando **Twilio**, un proveedor externo.
Es un *microservicio de integración*: no tiene lógica de negocio pesada ni base de datos. Recibe una petición y se la pasa a una API externa.

> Se basa en la clase del **31 de octubre** de Profundización de Software.

---

## 1. ¿Cómo funciona? (la idea en 30 segundos)

```
  Postman / otro microservicio
            │
            │  POST /api/ecommerce/notificacion   { telefono, mensaje, canal: "SMS" }
            ▼
  ┌──────────────────────────────┐
  │  notification  (puerto 8082) │
  │                              │
  │  1. Controller recibe el JSON│
  │  2. UseCase valida los datos │
  │  3. Adaptador Twilio envía   │
  └──────────────┬───────────────┘
                 │  HTTP (OpenFeign)
                 ▼
          API de Twilio  ──────►  📱 SMS al celular
                 │
                 └── devuelve un ID del mensaje (SM...)
```

1. Alguien (Postman, o más adelante `auth` o `catalogo`) envía un JSON con el teléfono, el mensaje y el canal.
2. El **caso de uso** revisa que el mensaje no esté vacío, que el canal exista y que el teléfono tenga un formato válido.
3. El **adaptador de Twilio** llama a la API de Twilio por internet.
4. Twilio envía el SMS y devuelve un **ID** (`SM...`), que sirve para rastrear el mensaje.
5. El servicio responde con ese ID y el estado `ACEPTADA_POR_PROVEEDOR`.

---

## 2. Arquitectura hexagonal (¿qué va en cada carpeta?)

La regla de oro: **el dominio no sabe que existe Twilio**. Solo conoce un "puerto" (`SmsGateway`) que dice *"necesito enviar un SMS con un teléfono y un mensaje"*.
Si mañana se cambia Twilio por Brevo, AWS u otro proveedor, **solo cambia la carpeta `infraestructure`**.

```
notification/src/main/java/com/ecommerce/notification/
│
├── NotificationApplication.java        ← Clase main (+ @EnableFeignClients)
│
├── domain/                             ← 🧠 LÓGICA (no depende de nada externo)
│   ├── model/
│   │   ├── Notificacion.java           ← Lo que se quiere enviar
│   │   ├── CanalNotificacion.java      ← enum: SMS, EMAIL
│   │   ├── ResultadoNotificacion.java  ← Lo que se responde
│   │   └── gateway/
│   │       └── SmsGateway.java         ← 🔌 PUERTO (interfaz)
│   ├── usecase/
│   │   └── NotificacionUseCase.java    ← Validaciones y decisión del canal
│   └── exception/
│       └── ServicioExternoException.java
│
├── infraestructure/                    ← 🔧 DETALLES TÉCNICOS
│   ├── driver_adapters/notificacion_sms/
│   │   ├── TwilioClient.java           ← Cliente Feign (la llamada HTTP)
│   │   ├── TwilioFeignConfig.java      ← Usuario y contraseña para Twilio
│   │   ├── TwilioResponse.java         ← Lo que responde Twilio
│   │   └── TwilioSmsGatewayImpl.java   ← 🔌 ADAPTADOR (implementa el puerto)
│   └── entry_points/
│       └── NotificacionController.java ← Endpoint REST
│
└── application/config/
    └── UseCaseConfig.java              ← Crea el bean del caso de uso
```

### ¿Qué hace cada clase?

| Clase | Capa | Qué hace |
|---|---|---|
| `Notificacion` | dominio | Datos de entrada: `tipo`, `email`, `telefono`, `mensaje`, `canal` |
| `CanalNotificacion` | dominio | Lista fija de canales (`SMS`, `EMAIL`). Evita escribir "SMS" como texto suelto |
| `ResultadoNotificacion` | dominio | Respuesta: `estado`, `canal`, `idProveedor`, `detalle` |
| `SmsGateway` | dominio | **Puerto**: `String enviarSms(telefono, mensaje)` |
| `NotificacionUseCase` | dominio | Valida los datos, limpia el teléfono y escoge el canal |
| `TwilioClient` | infraestructura | Interfaz Feign que hace el `POST` a Twilio |
| `TwilioFeignConfig` | infraestructura | Agrega la autenticación (Account SID + Auth Token) |
| `TwilioResponse` | infraestructura | Toma de la respuesta de Twilio solo lo que se necesita (`sid`, `status`) |
| `TwilioSmsGatewayImpl` | infraestructura | **Adaptador**: une el puerto con Twilio y traduce sus errores |
| `NotificacionController` | infraestructura | Recibe el JSON y devuelve 201, 400 o 502 |
| `UseCaseConfig` | aplicación | Le dice a Spring cómo crear `NotificacionUseCase` |

---

## 3. Reglas del caso de uso

`NotificacionUseCase.enviarNotificacion(...)` hace lo siguiente, en orden:

1. ❌ Si el mensaje viene vacío → `"El mensaje es obligatorio"`.
2. ❌ Si no viene canal → `"El canal de notificacion es obligatorio"`.
3. Si el canal es **SMS**:
   - **Normaliza el teléfono**: quita espacios, guiones y paréntesis (`"+57 300-123 4567"` → `"+573001234567"`).
   - ❌ Si no empieza por `+` y el indicativo → error. Twilio exige ese formato.
   - ✅ Llama al puerto `SmsGateway` y devuelve el resultado.
4. ❌ Cualquier otro canal (por ahora `EMAIL`) → `"Canal de notificacion invalido"`.

> Hay **un solo método para todos los canales**, como lo explicó el profe. Para agregar EMAIL se crea otro puerto (`EmailGateway`) y su adaptador.

---

## 4. Configuración

### `pom.xml`: dependencias nuevas

| Dependencia | Para qué |
|---|---|
| `spring-boot-starter-webmvc` | Exponer la API REST |
| `spring-cloud-starter-openfeign` | Consumir la API de Twilio de forma sencilla |
| `lombok` | Generar getters, setters y constructores |
| `spring-cloud-dependencies` (en `dependencyManagement`) | **BOM** que fija las versiones de Spring Cloud. Sin esto sale el error que tuvo el profe en clase |

### `application.properties`

```properties
server.port=8082
twilio.api-url=https://api.twilio.com
twilio.account-sid=AC...           # Twilio → Account SID
twilio.auth-token=...              # Twilio → Auth Token (¡secreto!)
twilio.from-number=+1...           # Número de Twilio que envía el SMS
```

**Dónde se sacan en Twilio:**
- **Account SID** y **Auth Token**: página de inicio de la consola (*Account details*).
- **From number**: *Send an SMS → Try for free*, el campo **From**.

> ⚠️ **No subir el Auth Token a GitHub.** Antes de hacer commit, cambia el valor por `PON_AQUI_TU_AUTH_TOKEN`. Si se llega a subir, genera un token nuevo en Twilio.

---

## 5. Cómo probarlo

1. Abre la carpeta `notification` en **su propia ventana** de IntelliJ.
2. Ejecuta `NotificationApplication`.
3. En Postman:

```
POST http://localhost:8082/api/ecommerce/notificacion
Content-Type: application/json
```
```json
{
  "tipo": "REGISTRO",
  "telefono": "+573001234567",
  "mensaje": "sms_order_confirmation",
  "canal": "SMS"
}
```

**Respuesta correcta (201 Created):**
```json
{
  "estado": "ACEPTADA_POR_PROVEEDOR",
  "canal": "SMS",
  "idProveedor": "SMe652437a1efd3a54eeda23a0248b9b6e",
  "detalle": "El proveedor acepto el mensaje"
}
```

### Posibles respuestas

| Caso | Código | Mensaje |
|---|---|---|
| Todo bien | **201** | JSON con el `idProveedor` |
| Falta el mensaje | **400** | `El mensaje es obligatorio` |
| Teléfono sin `+57` | **400** | `El telefono debe tener indicativo...` |
| Canal `EMAIL` | **400** | `Canal de notificacion invalido: EMAIL` |
| Twilio rechaza (credenciales, plantilla, número) | **502** | El motivo exacto que da Twilio |

---

## 6. ⚠️ Limitación de la cuenta gratuita de Twilio

Con la cuenta **Trial**:
- **No se puede escribir texto libre.** En `"mensaje"` solo se acepta el **nombre de una plantilla** de Twilio. Si se envía otro texto, responde el error **572006**.
- Las plantillas están **en inglés** y no se pueden editar. Su texto se ve en la consola (*Send an SMS → Template → Preview*).
- Solo envía a **números verificados**, con un máximo de **100 SMS gratis**.

**Plantillas disponibles:**
`sms_2fa` · `sms_appointment_reminders` · `sms_order_confirmation` · `sms_delivery_updates` · `sms_customer_support` · `sms_marketing_promotions` · `sms_event_notifications` · `sms_account_alerts` · `sms_feedback_surveys` · `sms_internal_alerts`

> Con una cuenta paga (*Upgrade*), `"mensaje"` acepta cualquier texto en español **sin cambiar el código**.

---

## 7. Pruebas automáticas

`NotificacionUseCaseTest` (JUnit + Mockito) prueba el caso de uso sin llamar a Twilio:
- ✅ Envía el SMS con el teléfono normalizado.
- ❌ Rechaza un mensaje vacío o nulo.
- ❌ Rechaza un teléfono vacío o sin indicativo.
- ❌ Rechaza canales sin implementar.

Para correrlas: `./mvnw test`, o el botón ▶ de IntelliJ sobre la clase de prueba.

---

## 8. Próximos pasos (ideas)

- **Conectar `auth`**: al registrar un usuario, enviar `sms_account_alerts`.
- **Conectar `catalogo`**: al confirmar una compra, enviar `sms_order_confirmation`.
  > Si falla la notificación, el registro o la compra **no** deben fallar.
- **Agregar EMAIL**: crear `EmailGateway` y un adaptador (Brevo, Gmail, Twilio SendGrid…).
