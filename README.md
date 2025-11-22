## Instrucciones de instalación y ejecución

Proyecto desarrollado en **Java**, utilizando **LibGDX** y **Gradle**  
(ambos vienen incluidos en la carpeta del proyecto).

### 1. Instalación

1. Descargar y descomprimir el archivo:  
   `GameLluvia (Recolecta Gotas).zip`

2. Abrir un IDE de preferencia (Realizado en Intellij IDEA).

3. Seleccionar **Open / Import Project** y abrir la carpeta del proyecto.

4. El IDE detectará el archivo `build.gradle`.  
   De ser necesario, permitir que Gradle descargue las dependencias.

---

### 2. Ejecución del juego

1. Ir a: desktop/src/com.mygdx.game/Lwjgl3Launcher.java

2. Hacer clic derecho → **Run "Lwjgl3Launcher"**

El juego iniciará en una ventana de **800×480**.

---

## Mecánicas y funciones del juego

### Controles

Flechas izquierda o derecha: Mover el tarro

**ESC**: Pausar/reanudar el juego

**F3**: Mostrar/ocultar visualización de hitbox (colisiones)

**R**: Reiniciado rapido

---

### Eventos

El juego aumenta su dificultad en función del puntaje y de los power-ups obtenidos:

**Etapa 2 — 1000 puntos**: Aumenta velocidad de caída + las gotas buenas/malas caen con patrón **diagonal**

**Etapa 3 — 2500 puntos**: Cae lluvia en patrón **diagonal + serpentenante para otros objetos**

**Escenario Bonus Tormenta** (Al recolectar **3 power-ups Tormenta**): **lluvia torrencial solo de gotas buenas**, puntaje rápido asegurado por tiempo limitado

---
