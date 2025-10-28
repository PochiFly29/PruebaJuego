package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

/**
 * Gota dañina — al colisionar, resta una vida al jugador.
 */
public class GotaDaño extends Objeto {
    private static final float VELOCIDAD = 300;

    public GotaDaño(Texture imagen) {
        super(imagen);
    }

    @Override
    protected void definirMovimiento(float delta) {
        // Movimiento vertical descendente
        area.y -= VELOCIDAD * delta;
    }

    @Override
    public boolean aplicarEfecto(Tarro tarro) {
        tarro.dañar();
        // Retorna true si aún tiene vidas (continúa el juego)
        // Retorna false si se quedó sin vidas → Game Over
        return tarro.getVidas() > 0;
    }
}
