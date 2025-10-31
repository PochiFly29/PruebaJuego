package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.imovimiento.IMovimientos;

public class GotaMala extends ObjetoCayendo {

    public GotaMala(Texture textura, Rectangle hitbox, IMovimientos movimiento) {
        super(textura, hitbox, movimiento);

        setCircleScale(0.40f);
    }

    @Override
    protected void alColisionar(Tarro tarro) {
        if (GameManager.getInstance().isEscudoActivo()) {
            GameManager.getInstance().consumirEscudo();
            return;
        }
        tarro.dañar();
    }

    @Override
    protected boolean esAtraible() { return false; }
}