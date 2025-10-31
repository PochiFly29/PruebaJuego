package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.imovimiento.IMovimientos;

public class PowerUpIman extends ObjetoCayendo {

    private final Sound powerupSound;

    public PowerUpIman(Texture textura, Rectangle hitbox, IMovimientos movimiento, Sound sound) {
        super(textura, hitbox, movimiento);
        this.powerupSound = sound;
        setCircleScale(0.40f);
    }

    @Override
    protected void alColisionar(Tarro tarro) {
        GameManager.getInstance().activarIman(5.0f);
        if (powerupSound != null) powerupSound.play();
    }

    @Override
    protected boolean esAtraible() { return false; }
}