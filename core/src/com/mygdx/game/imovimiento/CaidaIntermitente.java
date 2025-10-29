package com.mygdx.game.imovimiento;

import com.mygdx.game.Objeto;

public class CaidaIntermitente implements MovimientoStrat {
    private float velY = 300f;
    private float tOn = 1f, tOff = 2f;
    private float reloj = 0f;

    @Override public void mover(Objeto obj, float dt) {
        reloj += dt;
        if ((reloj % (tOn + tOff)) < tOn) obj.getArea().y -= velY * dt;
    }

    @Override public MovimientoStrat crearNueva() { return new CaidaIntermitente(); }

    @Override public void initApariencia(Objeto obj) {
        obj.setRotacion(0f);
    }

    @Override public void onStart() {}
    @Override public void onStop()  {}
}
