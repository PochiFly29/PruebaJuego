package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

// Power-up que da un escudo temporal
public class PowerUpEscudo extends ObjetoQueCae {

    private Sound powerupSound; // Sonido al tomarlo

    public PowerUpEscudo(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound sound) {
        super(textura, hitbox, movimiento);
        this.powerupSound = sound;
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {
        // Le dice al GameManager que active el escudo
        GameManager.getInstance().activarEscudo(7.0f); // 7 segundos
        powerupSound.play();
    }

    @Override
    protected boolean esAtraible() {
        // El imán también atrae este power-up
        return false;
    }
}