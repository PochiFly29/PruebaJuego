package com.mygdx.game.iescenario;

import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Lluvia;
import com.mygdx.game.Tarro;

public class EscenarioNormal implements EscenarioStrat {
    private static final long INTERVALO = 100_000_000L;
    private long last;

    @Override
    public void init(Lluvia ctx) {
        last = TimeUtils.nanoTime();
        ctx.setSoloBuenas(false);
    }

    @Override public void update(Lluvia ctx, Tarro tarro, float dt) {
        if (TimeUtils.nanoTime() - last > INTERVALO) {
            ctx.crearGotaDeLluvia();
            last = TimeUtils.nanoTime();
        }
    }
}