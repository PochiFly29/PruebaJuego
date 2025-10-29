package com.mygdx.game.imovimiento;

import com.mygdx.game.Objeto;

public class CaidaVertical implements MovimientoStrat {
    private float velY = 300f;

    @Override public void mover(Objeto obj, float dt) {
        obj.getArea().y -= velY * dt;
    }

    @Override public MovimientoStrat crearNueva() { return this; }

    @Override public void initApariencia(Objeto obj) {
        obj.setRotacion(0f);
    }

    @Override public void onStart() {}
    @Override public void onStop()  {}
}
