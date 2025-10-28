package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class GotaBuena extends ObjetoQueCae {

    private Sound dropSound;
    private int puntosQueDa = 10;

    public GotaBuena(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound dropSound) {
        super(textura, hitbox, movimiento);
        this.dropSound = dropSound;
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {
        // Llama al Singleton
        GameManager.getInstance().sumarPuntos(puntosQueDa);
        dropSound.play();
    }
}