package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Objeto {
    protected Rectangle area;
    protected Texture imagen;

    public Objeto(Texture imagen) {
        this.imagen = imagen;
        this.area = new Rectangle();
        this.area.width = 64;
        this.area.height = 64;
    }

    // --- MÉTODOS ABSTRACTOS ---
    /** Define cómo se mueve el objeto (implementado en las subclases). */
    protected abstract void definirMovimiento(float delta);

    /** Aplica el efecto particular al colisionar con el tarro. */
    public abstract boolean aplicarEfecto(Tarro tarro);

    // --- METODO TEMPLATE ---
    public final boolean actualizarYVerificar(Tarro tarro, float delta) {
        definirMovimiento(delta);

        if (area.y + area.height < 0) {
            return false;
        }

        if (area.overlaps(tarro.getArea())) {
            System.out.println("Colisión con " + this.getClass().getSimpleName());
            return aplicarEfecto(tarro);
        }

        return true; // Sigue activo
    }

    // --- MÉTODOS CONCRETOS ---
    public void dibujar(SpriteBatch batch) {
        batch.draw(imagen, area.x, area.y);
    }

    public Rectangle getArea() {
        return area;
    }
}
