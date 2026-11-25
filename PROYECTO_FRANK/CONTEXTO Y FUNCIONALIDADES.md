# Contexto y Especificación Funcional del Backend: Lunara

## 1. Visión General del Proyecto
**Lunara** (evolución multiusuario de un prototipo personal) es una plataforma backend distribuida enfocada en la **salud menstrual, el bienestar sexual preventivo y la privacidad por diseño (*Privacy by Design*)**.

A diferencia de un calendario menstrual convencional, Lunara permite a cualquier usuaria registrarse, gestionar su ciclo de forma personalizada, proyectar fases hormonales y ventanas fértiles, evaluar riesgos asociados a la actividad sexual y al uso de anticonceptivos, detectar interacciones farmacológicas reales, recibir análisis personalizados mediante Inteligencia Artificial y gestionar alertas en **Modo Discreto** (camuflando información sensible en notificaciones).

**Alcance:** Exclusivamente **Backend** (arquitectura de microservicios con APIs RESTful documentadas). No se desarrollará interfaz gráfica (Frontend).

---

## 2. Requerimientos Obligatorios (Rúbrica Académica)
El diseño y la implementación deben cumplir estrictamente con:
1. **Arquitectura:** Mínimo **3 microservicios** independientes.
2. **Base de Datos:** **1 sola base de datos** relacional (**PostgreSQL**), organizada lógicamente (ej. esquemas separados por dominio) para evitar acoplamiento indebido entre servicios.
3. **Autenticación y Multiusuario:** Sistema de **registro e inicio de sesión** (Login/Register) para soportar múltiples usuarias, además de un segundo factor de privacidad interno (PIN de bloqueo para registros íntimos).
4. **Endpoints de Lógica de Negocio:** Cada microservicio debe exponer **5 APIs diferentes al CRUD** (15 endpoints de procesamiento, cálculo o integración en total), además de los endpoints CRUD básicos necesarios para operar la plataforma.
5. **Sistema de Notificaciones:** Envío real de alertas al usuario (preferiblemente vía correo electrónico REST como Resend o similar) con soporte para **Modo Discreto**.
6. **Consumo de APIs Externas (Mínimo 2 — Usaremos 3):**
   * **OpenAI API (modelo económico como `gpt-4o-mini`):** Análisis inteligente de síntomas, tendencias del ciclo y traducción de advertencias médicas a lenguaje empático.
   * **OpenFDA API (`api.fda.gov`):** Consulta oficial y gratuita de interacciones farmacológicas entre medicamentos registrados y anticonceptivos hormonales.
   * **API de Notificaciones (ej. Resend Email API):** Despacho de alertas discretas por correo electrónico.
7. **Calidad y Testing:** Pruebas unitarias automatizadas con una cobertura mínima del **90%**.
8. **Rendimiento (SLA):** En pruebas de carga y estrés, el tiempo de respuesta de las APIs **no debe exceder los 500 ms**.
9. **Fundamento Teórico:** Aplicación estricta de principios **SOLID** y patrones de arquitectura justificados para un artículo académico en formato IEEE.
10. **Despliegue:** Todo el sistema debe quedar desplegado y accesible en la nube (plataforma y estrategia a definir en conjunto).

---

## 3. Base Tecnológica Base (Abierta a Decisiones de Implementación)
* **Lenguaje y Framework:** Java + Spring Boot.
* **Base de Datos:** PostgreSQL (instancia única).
* **Decisiones abiertas para evaluar juntos:** Plataforma de despliegue en la nube, estrategia de caché (ej. Caffeine/Redis), manejo de asincronía para llamadas lentas a OpenAI/Email (ej. `@Async` o colas ligeras) y librerías de seguridad/JWT.

---

## 4. Funcionalidades Principales de la Aplicación (Multiusuario)

### A. Gestión de Cuenta, Autenticación y Privacidad
* **Registro y Login:** Creación de cuenta con correo y contraseña encriptada, emisión de tokens de sesión (JWT) para aislar los datos de cada usuaria.
* **Configuración Personalizada:** Cada usuaria define la duración base de su ciclo (ej. 28 días), duración de sangrado, método anticonceptivo habitual y su lista personalizada de síntomas a monitorear.
* **PIN de Seguridad:** Bloqueo adicional para consultar o modificar registros sexuales y notas privadas, con protección ante intentos fallidos.
* **Modo Discreto:** Opción que transforma notificaciones íntimas (ej. *"Ventana fértil alta"* o *"Riesgo sin protección"*) en mensajes neutros configurados por la usuaria (ej. *"Recordatorio de rutina semanal"*).
* * **Vinculación de Pareja (Invitación Única y Permisos Granulares):**
  * Cada miembro tiene su propia cuenta (Login/Registro).
  * La usuaria genera un **código de invitación de un solo uso** (ej. `LUNA-94`) para vincular permanentemente la cuenta de su pareja.
  * Una vez vinculadas las cuentas, la usuaria decide qué información recibe su pareja (ej. alertas preventivas de fase premenstrual, estado de ánimo o días de alta fertilidad), manteniendo **totalmente bloqueados y privados** sus notas personales, síntomas detallados y registros sexuales.
* **Exportación Clínica Anonimizada:** Generación de un resumen médico limpio para llevar a consulta ginecológica (fechas, duración media, síntomas físicos), omitiendo automáticamente notas privadas y detalles sexuales explícitos.

### B. Calendario Menstrual y Motor Predictivo
* **Registro de Períodos:** Marcado de inicio y fin de menstruación.
* **Algoritmo de Proyección Adaptativa:** Toma como referencia el inicio del último período. Si hay menos de 2 períodos registrados, usa la duración configurada por la usuaria; cuando existen 2 o más, promedia únicamente los intervalos válidos de **20 a 45 días** entre inicios consecutivos. Proyecta la próxima menstruación, sitúa la ovulación estimada **14 días antes** y define la ventana fértil desde **5 días antes hasta 1 día después** de la ovulación.
* **Detección de Irregularidades:** Análisis estadístico de variación entre ciclos históricos para alertar si existen cambios bruscos o ciclos fuera del rango saludable.
* **Simulador de Escenarios / Viajes:** Permite consultar una fecha futura (ej. vacaciones) y simular cómo se desplazarían los próximos períodos si el ciclo actual se adelanta o retrasa $N$ días.

### C. Sexualidad, Salud Preventiva e Inteligencia Artificial
* **Diario Íntimo:** Registro diario de síntomas (con severidad), notas personales, toma de anticonceptivos y actividad sexual (indicando si hubo protección).
* **Evaluación de Riesgo Reproductivo:** Cruce automático entre relaciones sexuales sin protección y la ventana fértil proyectada para calcular el nivel de riesgo y disparar alertas preventivas.
* **Control de Olvido de Anticonceptivos:** Cálculo de ventana de seguridad cuando se registra un retraso u olvido en la toma de la píldora anticonceptiva.
* **Verificación Farmacológica (OpenFDA):** Al registrar un medicamento (ej. antibiótico o analgésico), consulta la API oficial de la FDA para detectar si disminuye la eficacia del método anticonceptivo configurado.
* **Insights y Bienestar con IA (OpenAI):** Análisis periódico de los últimos ciclos y síntomas para identificar patrones recurrentes y generar recomendaciones personalizadas de bienestar hormonal y sexual.

---

## 5. Propuesta de Arquitectura: 3 Microservicios y sus 15 APIs (No-CRUD)

### Microservicio 1: `ms-cycle-core` (Motor Matemático y Predicción Biométrica)
*Responsabilidad:* Cálculos de calendario, proyección de fechas, fases hormonales y análisis estadístico del ciclo.
* **CRUD base:** Gestión de períodos menstruales y configuración base del ciclo por usuaria.
* **5 APIs de Lógica de Negocio (No-CRUD):**
  1. `POST /api/v1/cycle/calculate-projection`: Ejecuta el algoritmo de filtrado (20–45 días), calcula el promedio dinámico y proyecta próxima menstruación, ovulación (-14 días) y ventana fértil (-5 a +1 día).
  2. `GET /api/v1/cycle/current-status`: Determina el día exacto del ciclo actual, la fase hormonal (*Menstrual, Folicular, Ovulatoria, Lútea*), días faltantes y porcentaje de progreso.
  3. `GET /api/v1/cycle/irregularity-analysis`: Calcula desviación estándar y varianza de los últimos ciclos para detectar irregularidades o ciclos anovulatorios.
  4. `POST /api/v1/cycle/simulate-scenario`: Simula fechas de próximos ciclos ante un adelanto o retraso hipotético de $\pm N$ días (útil para planear viajes o eventos).
  5. `GET /api/v1/cycle/fertility-probability`: Calcula el porcentaje estimado de fertilidad y nivel de riesgo (*Bajo, Medio, Alto*) para una fecha específica del calendario.

### Microservicio 2: `ms-intimacy-health` (Sexualidad, Síntomas e Integraciones Médicas/IA)
*Responsabilidad:* Registro íntimo, evaluación de riesgo sexual, correlación de síntomas y consumo de **OpenFDA** y **OpenAI**.
* **CRUD base:** Gestión de registros diarios (síntomas, actividad sexual, medicamentos y notas).
* **5 APIs de Lógica de Negocio (No-CRUD):**
  1. `POST /api/v1/intimacy/assess-conception-risk`: Cruza una relación sexual sin protección con la ventana fértil proyectada (consultando a `ms-cycle-core`) y determina el nivel de riesgo; si es alto, dispara una alerta al servicio de notificaciones.
  2. `POST /api/v1/intimacy/contraceptive-delay-check`: Evalúa las horas de retraso en la toma de un anticonceptivo según su tipo y determina si requiere respaldo de barrera.
  3. `GET /api/v1/intimacy/symptom-patterns`: Agrupa el historial de síntomas y libido por fase del ciclo para calcular frecuencias y detectar patrones recurrentes.
  4. `POST /api/v1/intimacy/medication-interaction` *(Consume **OpenFDA API**)*: Consulta la base oficial de la FDA para verificar si un medicamento registrado tiene interacciones o reduce la eficacia del anticonceptivo de la usuaria.
  5. `POST /api/v1/intimacy/ai-wellness-insights` *(Consume **OpenAI API**)*: Procesa métricas agregadas de los últimos ciclos y genera un informe empático de salud hormonal y sexual mediante IA.

### Microservicio 3: `ms-auth-privacy-notifications` (Identidad, Seguridad, Modo Discreto y Alertas)
*Responsabilidad:* Autenticación multiusuario (Registro/Login), seguridad por PIN, privacidad, modo pareja y envío real de notificaciones (**Resend API**).
* **CRUD base:** Registro de usuarias, login (JWT) y gestión de preferencias de privacidad.
* **5 APIs de Lógica de Negocio (No-CRUD):**
  1. `POST /api/v1/privacy/verify-pin`: Valida el PIN secundario encriptado para desbloquear datos íntimos, aplicando bloqueo temporal tras múltiples intentos fallidos.
  2. `POST /api/v1/notifications/dispatch-discreet` *(Consume **Resend Email API**)*: Enmascara mensajes íntimos/sensibles si el **Modo Discreto** está activo y envía la notificación real por correo electrónico registrando auditoría de entrega.
  3. `GET /api/v1/privacy/clinical-export`: Construye un reporte médico limpio para consulta ginecológica consolidando datos de los otros servicios, purificando notas personales y registros sexuales explícitos.
  4. `POST /api/v1/privacy/partner-invite`: Genera un código de invitación de un solo uso (`LUNA-XX`) y configura los permisos de privacidad de la usuaria (qué tipo de alertas o estados autoriza compartir).
  5. `POST /api/v1/privacy/partner-link`: Permite a la cuenta de la pareja ingresar el código de invitación para quedar vinculada permanentemente y recibir únicamente las notificaciones autorizadas por la usuaria.
---

## 6. Consideraciones Clave para la Implementación
* **Cumplimiento del SLA (< 500 ms):** Las operaciones que invoquen APIs externas lentas (OpenAI o envío de correos) deben diseñarse con estrategias de **ejecución asíncrona** (ej. responder `202 Accepted` inmediatamente mientras se procesa en segundo plano) o **caché** (ej. cachear respuestas de OpenFDA y proyecciones de ciclo) para garantizar tiempos de respuesta muy inferiores a 500 ms durante las pruebas de carga y estrés.
* **Cobertura Unitaria (≥ 90%):** La lógica matemática (cálculo de intervalos de 20–45 días, fases, desviación estándar, evaluación de riesgo y enmascaramiento discreto) debe estar desacoplada de la infraestructura siguiendo principios **SOLID**, facilitando pruebas unitarias exhaustivas.