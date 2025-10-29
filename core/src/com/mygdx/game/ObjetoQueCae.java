package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

// Clase Abstracta (Patrón Template Method)
public abstract class ObjetoQueCae {

    protected Rectangle hitbox;
    protected Texture textura;
    protected IComportamientoMovimiento miMovimiento;
    protected float rotacion = 0f;
    public boolean marcadoParaEliminar = false;

    // Variables para cálculos de movimiento
    protected float spawnX; // Posición X inicial
    protected float tiempoEnVida = 0f; // Contador de tiempo

    public ObjetoQueCae(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento) {
        this.textura = textura;
        this.hitbox = hitbox;
        this.miMovimiento = movimiento;
        this.spawnX = hitbox.x; // Guardar la X inicial
    }

    /**
     * MÉTODO PLANTILLA (Template Method).
     * Define el algoritmo base: mover, chequear colisión, chequear límites.
     */
    public final void update(float delta, Tarro tarro) {

        // 1. Mover (solo si el tarro no está herido)
        if (!tarro.estaHerido()) {
            this.tiempoEnVida += delta; // Actualizar contador de tiempo
            miMovimiento.mover(this, delta);
        }

        // 2. Chequear Colisión con el tarro
        if (hitbox.overlaps(tarro.getArea())) {
            this.aplicarEfecto(tarro);
            this.marcadoParaEliminar = true;
        }

        // 3. Chequear Límites de pantalla
        if (this.hitbox.y + this.hitbox.height < 0) {
            this.marcadoParaEliminar = true;
        }
    }

    /**
     * PASO ABSTRACTO (Hook).
     * Las subclases deben implementar qué pasa al chocar.
     */
    protected abstract void aplicarEfecto(Tarro tarro);

    /**
     * Dibuja el objeto con su rotación.
     */
    public void dibujar(SpriteBatch batch) {
        batch.draw(textura,
                hitbox.x,
                hitbox.y,
                hitbox.width / 2,    // originX
                hitbox.height / 2,   // originY
                hitbox.width,
                hitbox.height,
                1.0f,                  // scaleX
                1.0f,                  // scaleY
                rotacion,              // rotación
                0,                     // srcX
                0,                     // srcY
                textura.getWidth(),    // srcWidth
                textura.getHeight(),   // srcHeight
                false,                 // flipX
                false);                // flipY
    }

    // --- Getters y Setters ---

    public void setRotacion(float rotacion) {
        this.rotacion = rotacion;
    }

    public void setComportamiento(IComportamientoMovimiento nuevoComportamiento) {
        this.miMovimiento = nuevoComportamiento;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getTiempoEnVida() {
        return tiempoEnVida;
    }
}