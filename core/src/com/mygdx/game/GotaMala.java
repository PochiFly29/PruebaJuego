package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class GotaMala extends ObjetoQueCae {

    public GotaMala(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento) {
        super(textura, hitbox, movimiento);
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {
        // El tarro se encarga de su sonido y estado "herido"
        if (!tarro.estaHerido()) {
            // 1. Herir al tarro
            tarro.dañar();
        }
    }
}