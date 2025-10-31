package com.mygdx.game.iescenario;

import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.GameManager;
import com.mygdx.game.Lluvia;
import com.mygdx.game.Tarro;
import com.mygdx.game.imovimiento.IMovimientos;

public class EscenarioTorrencial implements IEscenarios {
    private static final long INTERVALO = 50_000_000L;
    private static final int BURST = 2;
    private static final float DURACION = 5f;

    private long last;
    private float tiempoAcumulado = 0f;
    private IMovimientos movDuranteTormenta;

    @Override
    public void init(Lluvia ctx) {
        last = TimeUtils.nanoTime();
        tiempoAcumulado = 0f;
        ctx.setSoloBuenas(true);
        movDuranteTormenta = GameManager.getInstance().getMovimientoGotaPrevioATormenta();
    }

    @Override
    public void update(Lluvia ctx, Tarro tarro, float dt) {
        tiempoAcumulado += dt;
        if (tiempoAcumulado >= DURACION) {
            ctx.cambiarEscenarioNormal();
            return;
        }
        if (TimeUtils.nanoTime() - last > INTERVALO) {
            for (int i = 0; i < BURST; i++) {
                ctx.spawnAhora(Lluvia.TipoSpawn.BUENA, movDuranteTormenta);
            }
            last = TimeUtils.nanoTime();
        }
    }
}
