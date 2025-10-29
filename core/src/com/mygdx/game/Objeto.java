package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.imovimiento.MovimientoStrat;

public abstract class Objeto {
    protected Rectangle area;
    protected Texture imagen;
    private MovimientoStrat movimiento;
    protected float rotacion = 0f;

    public void setRotacion(float rotacion) {
        this.rotacion = rotacion;
    }

    public Objeto(Texture imagen) {
        this.imagen = imagen;
        this.area = new Rectangle();
        this.area.width = 64;
        this.area.height = 64;
    }

    public void setMovimiento(MovimientoStrat m) {
        this.movimiento = m;
        if (this.movimiento != null) this.movimiento.initApariencia(this);
    }

    // --- MÉTODOS ABSTRACTOS ---
    /** Define cómo se mueve el objeto (implementado en las subclases). */
    protected abstract void definirMovimiento(float delta);

    /** Aplica el efecto particular al colisionar con el tarro. */
    public abstract boolean aplicarEfecto(Tarro tarro);

    // --- METODO TEMPLATE ---
    public final boolean actualizarYVerificar(Tarro tarro, float delta) {
        definirMovimiento(delta);

        if (movimiento != null) movimiento.mover(this, delta);

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
        batch.draw(
                imagen,
                area.x, area.y,
                area.width / 2f, area.height / 2f, // origen de rotación (centro)
                area.width, area.height,
                1f, 1f, // escala
                rotacion, // grados
                0, 0, // srcX, srcY
                imagen.getWidth(),
                imagen.getHeight(),
                false, false
        );
    }

    public Rectangle getArea() {
        return area;
    }
}
