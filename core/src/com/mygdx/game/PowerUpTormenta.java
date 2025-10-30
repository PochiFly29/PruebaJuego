package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.imovimiento.IComportamientoMovimiento;

public class PowerUpTormenta extends ObjetoCayendo {

    private final Sound powerupSound;

    public PowerUpTormenta(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound sound) {
        super(textura, hitbox, movimiento);
        this.powerupSound = sound;
    }

    @Override
    protected void alColisionar(Tarro tarro) {
        GameManager.getInstance().incrementarContadorTormenta();
        if (powerupSound != null) powerupSound.play();
    }

    @Override
    protected boolean esAtraible() { return false; }
}