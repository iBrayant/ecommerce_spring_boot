# Cómo diseñar pruebas unitarias por comportamiento

Cada prueba responde una pregunta concreta: **dada una condición, ¿qué debería ocurrir al ejecutar una operación?** El estilo consiste en dedicar un método de prueba a cada escenario y comprobar su resultado observable. Una misma operación puede tener varios escenarios según las reglas del proyecto.

## Cómo organizar las pruebas

En Java, las pruebas suelen ubicarse en `src/test/java`, bajo el paquete correspondiente a la clase que se quiere probar. Una convención es nombrar la clase de prueba con el nombre de la clase real seguido de `Test`. Dentro de ella, los métodos se agrupan por operación y sus nombres describen la condición y el resultado esperado. Así, el nombre de una prueba que falla ya orienta sobre el comportamiento afectado.

La responsabilidad de la clase determina qué conviene comprobar. Si toma una decisión de negocio, interesa el resultado de esa decisión. Si convierte datos, se pueden construir objetos reales y comparar sus campos después de la conversión. Si traduce un resultado a una respuesta HTTP, se pueden comprobar el estado y el cuerpo que devuelve su método. Estas posibilidades dependen de las responsabilidades presentes en el proyecto.

## Cómo escribir cada escenario

1. **Arrange · preparar.** Elige los datos que activan el comportamiento que quieres observar. Si la clase consulta una dependencia, prepara la respuesta que debe recibir en ese escenario.
2. **Act · ejecutar.** Llama a la operación real con esos datos. Si devuelve un valor, guárdalo. Si esperas una excepción, pasa la llamada como lambda a la aserción para que pueda capturarla.
3. **Assert · comprobar.** Compara lo observado con lo que exige la regla. Puede ser un valor, un cambio de estado, una excepción o una interacción con una dependencia. Un escenario puede necesitar varias comprobaciones cuando todas describen el mismo comportamiento.

### Un ejemplo breve

Supongamos que una regla admite un valor igual al mínimo permitido. La prueba elige exactamente el límite porque ese dato permite detectar un error concreto: si la implementación usara `>` donde corresponde `>=`, la prueba fallaría. Los nombres y valores de este fragmento son ilustrativos.

```java
@Test
void aceptaUnValorIgualAlMinimo() {
    // Arrange
    int minimo = 5;
    ReglaDeMinimo regla = new ReglaDeMinimo(minimo);

    // Act
    boolean aceptado = regla.acepta(5);

    // Assert
    assertTrue(aceptado);
}
```

## Cuándo usar JUnit y Mockito

En este estilo de pruebas para Java, JUnit reconoce los métodos marcados con `@Test` y evalúa las aserciones. Por ejemplo, `assertEquals(esperado, obtenido)` compara un resultado y `assertThrows(TipoEsperado.class, accion)` comprueba que la operación lance la excepción prevista.

Mockito permite controlar una dependencia cuando su respuesta influye en el escenario. Con `@ExtendWith(MockitoExtension.class)`, se puede declarar el colaborador con `@Mock` e inyectarlo en la clase real mediante `@InjectMocks`. En **Arrange**, `when(...).thenReturn(...)` prepara lo que responderá ese colaborador. En **Assert**, `verify(...)` comprueba una llamada cuando esa colaboración forma parte del comportamiento esperado. Por ejemplo, si una regla exige rechazar una entrada antes de guardarla, tiene sentido comprobar tanto el rechazo como la ausencia de escritura con `never()`. Para una regla que opera directamente sobre sus entradas, como la del ejemplo, basta con construir la clase y observar el resultado.

## Cómo aplicar el estilo en otro proyecto

Empieza por una operación real y consulta sus requisitos, su contrato y las convenciones de prueba existentes. Identifica el escenario habitual y añade casos que distingan decisiones relevantes, como un límite exacto, una entrada inválida o la ausencia de un dato, cuando esas condiciones formen parte del contrato. Para cada caso, completa esta frase antes de escribir código: **«Dado ___, cuando ejecuto ___, espero ___»**. La última parte indica qué aserción necesitas.

Si compartes esta guía con una IA, puedes darle la siguiente instrucción:

> Revisa el código y los requisitos del proyecto actual. Usa sus nombres, paquetes, dependencias y herramientas de prueba. Para cada escenario, indica qué regla lo justifica y escribe la prueba con Arrange, Act y Assert. Usa el ejemplo de esta guía únicamente como referencia de estructura. Si el resultado esperado es ambiguo, señala qué decisión falta antes de convertirla en una aserción.