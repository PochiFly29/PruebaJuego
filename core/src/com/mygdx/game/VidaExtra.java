package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.imovimiento.IMovimientos;

public class VidaExtra extends ObjetoCayendo {

    private final Sound lifeSound;

    public VidaExtra(Texture textura, Rectangle hitbox, IMovimientos movimiento, Sound lifeSound) {
        super(textura, hitbox, movimiento);
        this.lifeSound = lifeSound;
        setCircleScale(0.40f);
    }

    @Override
    protected void alColisionar(Tarro tarro) {
        GameManager.getInstance().sumarVida();
        if (lifeSound != null) lifeSound.play();
    }

    @Override
    protected boolean esAtraible() { return false; }
}
