package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class GotaMala extends ObjetoQueCae {

    public GotaMala(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento) {
        super(textura, hitbox, movimiento);
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {

        // Chequear si el escudo está activo
        if (GameManager.getInstance().isEscudoActivo()) {
            // El escudo para el golpe y se consume
            GameManager.getInstance().consumirEscudo();
            // (Opcional: añadir sonido de "escudo roto")
            return; // No hace daño
        }

        // Comportamiento normal (si no hay escudo)
        if (!tarro.estaHerido()) {
            tarro.dañar();
        }
    }


    @Override
    protected boolean esAtraible() {
        return false;
    }
}