package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

// Power-up que suma al contador de la "Súper Tormenta"
public class PowerUpTormenta extends ObjetoQueCae {

    private Sound powerupSound;

    public PowerUpTormenta(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound sound) {
        super(textura, hitbox, movimiento);
        this.powerupSound = sound;
    }

    @Override
    protected void aplicarEfecto(Tarro tarro) {
        // Le dice al GameManager que sume 1 al contador
        GameManager.getInstance().incrementarContadorTormenta();
        powerupSound.play();
    }

    @Override
    protected boolean esAtraible() {
        return false;
    }
}