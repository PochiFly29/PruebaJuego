package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class GotaPuntos extends Objeto {
    private Sound dropSound;
    private static final float VELOCIDAD = 300;

    public GotaPuntos(Texture imagen, Sound dropSound) {
        super(imagen);
        this.dropSound = dropSound;
    }

    @Override
    protected void definirMovimiento(float delta) {
        // Movimiento vertical descendente
        area.y -= VELOCIDAD * delta;
    }


    @Override
    public boolean aplicarEfecto(Tarro tarro) {
        tarro.sumarPuntos(10);
        if (dropSound != null) dropSound.play();

        return false;
    }
}
