package com.mygdx.game;

import com.badlogic.gdx.utils.TimeUtils;

public class EscenarioTorrencial implements EscenarioStrat {
    private static final long INTERVALO = 80_000_000L;
    private static final int BURST = 2;
    private static final float DURACION = 5f;
    private long last;
    private float tiempoAcumulado = 0f;

    @Override
    public void init(Lluvia ctx) {
        last = TimeUtils.nanoTime();
        ctx.setSoloBuenas(true);
    }

    public void update(Lluvia ctx, Tarro tarro, float dt) {

        // contador de duración
        tiempoAcumulado += dt;
        if (tiempoAcumulado >= DURACION) {
            ctx.cambiarEscenarioNormal();
            return;
        }

        // spawn de gotas
        if (TimeUtils.nanoTime() - last > INTERVALO) {
            for (int i = 0; i < BURST; i++) ctx.crearGotaDeLluvia();
            last = TimeUtils.nanoTime();
        }
    }
}