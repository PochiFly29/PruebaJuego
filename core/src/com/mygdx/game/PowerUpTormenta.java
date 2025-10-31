package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.iescenario.EscenarioTorrencial;
import com.mygdx.game.imovimiento.IMovimientos;

public class PowerUpTormenta extends ObjetoCayendo {

    private final Sound sfx;
    private final Lluvia lluvia;

    public PowerUpTormenta(Lluvia lluvia, Texture textura, Rectangle hitbox, IMovimientos movimiento, Sound sound) {
        super(textura, hitbox, movimiento);
        this.sfx = sound;
        this.lluvia = lluvia;
        setCircleScale(0.40f);
    }

    @Override
    protected void alColisionar(Tarro tarro) {
        if (sfx != null) sfx.play();

        GameManager gm = GameManager.getInstance();
        gm.incrementarContadorTormenta();

        if (gm.getEstadoActual() == GameManager.EstadoJuego.TORMENTA_ESPECIAL && lluvia != null) {
            lluvia.setEscenario(new EscenarioTorrencial());
        }
    }

    @Override
    protected boolean esAtraible() {
        return false;
    }
}
