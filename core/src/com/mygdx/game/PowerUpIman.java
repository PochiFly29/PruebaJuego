package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

// Power-up que activa un imán
public class PowerUpIman extends ObjetoQueCae {

    private Sound powerupSound;

    public PowerUpIman(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound sound) {
        super(textura, hitbox, movimiento);
        this.powerupSound = sound;
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {
        // Le dice al GameManager que active el imán
        GameManager.getInstance().activarIman(5.0f); // 5 segundos
        powerupSound.play();
    }

    @Override
    protected boolean esAtraible() {
        return false;
    }
}