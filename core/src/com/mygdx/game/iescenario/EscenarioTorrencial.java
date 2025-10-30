package com.mygdx.game.iescenario;

import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Lluvia;
import com.mygdx.game.Tarro;

public class EscenarioTorrencial implements EscenarioStrat {
    private static final long  INTERVALO = 30_000_000L; // 0.03s
    private static final int   BURST     = 3;
    private static final float DURACION  = 5f;

    private long  last;
    private float tiempoAcumulado = 0f;

    @Override
    public void init(Lluvia ctx) {
        last = TimeUtils.nanoTime();
        tiempoAcumulado = 0f;
        ctx.setSoloBuenas(true);
    }

    @Override
    public void update(Lluvia ctx, Tarro tarro, float dt) {
        tiempoAcumulado += dt;
        if (tiempoAcumulado >= DURACION) {
            ctx.cambiarEscenarioNormal();
            return;
        }
        if (TimeUtils.nanoTime() - last > INTERVALO) {
            for (int i = 0; i < BURST; i++) ctx.spawnAhoraBuena();
            last = TimeUtils.nanoTime();
        }
    }
}
