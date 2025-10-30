package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.imovimiento.IComportamientoMovimiento;

public class GotaBuena extends ObjetoCayendo {

    private final Sound dropSound;

    public GotaBuena(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento, Sound dropSound) {
        super(textura, hitbox, movimiento);
        this.dropSound = dropSound;
    }

    @Override
    protected void alColisionar(Tarro tarro) {
        GameManager.getInstance().sumarPuntos(1);
        if (dropSound != null) dropSound.play();
    }

    @Override
    protected boolean esAtraible() { return true; }
}