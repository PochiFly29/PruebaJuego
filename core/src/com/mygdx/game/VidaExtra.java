package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class VidaExtra extends ObjetoQueCae {

    private Sound lifeSound;

    public VidaExtra(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound lifeSound) {
        super(textura, hitbox, movimiento);
        this.lifeSound = lifeSound;
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {
        GameManager.getInstance().sumarVida();
        lifeSound.play();
    }
}