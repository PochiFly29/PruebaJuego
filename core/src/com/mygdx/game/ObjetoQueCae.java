package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

// Clase Abstracta con el Patrón Template Method
public abstract class ObjetoQueCae {

    protected Rectangle hitbox;     // Hitbox y posición
    protected Texture textura;      // Imagen
    protected float rotacion = 0f;       // Ángulo de rotación

    // Atributo para el Patrón Strategy
    protected IComportamientoMovimiento miMovimiento;

    // Flag para que GameScreen sepa cuándo eliminarlo
    public boolean marcadoParaEliminar = false;

    public ObjetoQueCae(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento) {
        this.textura = textura;
        this.hitbox = hitbox;
        this.miMovimiento = movimiento;
    }

    /**
     * MÉTODO PLANTILLA (Template Method).
     * Es 'final' para que las subclases no puedan alterarlo.
     * Define el algoritmo base: mover, chequear colisión, chequear límites.
     */
    public final void update(float delta, Tarro tarro) {

        // 1. Mover (¡MODIFICADO!)
        // Solo se mueve si el tarro NO está herido
        if (!tarro.estaHerido()) {
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

    protected abstract void aplicarEfecto(Tarro tarro);

    public void dibujar(SpriteBatch batch) {
        // Ya no usamos el batch.draw() simple
        // batch.draw(textura, hitbox.x, hitbox.y, hitbox.width, hitbox.height);

        // Usamos el batch.draw() completo que permite rotación
        batch.draw(textura,
                hitbox.x,
                hitbox.y,
                hitbox.width / 2,    // originX (centro del sprite)
                hitbox.height / 2,   // originY (centro del sprite)
                hitbox.width,
                hitbox.height,
                1.0f,                  // scaleX
                1.0f,                  // scaleY
                rotacion,              // ¡Aquí usamos la rotación!
                0,                     // srcX (región de la textura)
                0,                     // srcY (región de la textura)
                textura.getWidth(),    // srcWidth
                textura.getHeight(),   // srcHeight
                false,                 // flipX
                false);                // flipY
    }

    public void setComportamiento(IComportamientoMovimiento nuevoComportamiento) {
        this.miMovimiento = nuevoComportamiento;
    }

    public void setRotacion(float rotacion) {
        this.rotacion = rotacion;
    }
}