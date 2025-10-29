package com.mygdx.game.imovimiento;

import com.mygdx.game.Objeto;

public interface MovimientoStrat {
    void mover(Objeto obj, float dt);
    MovimientoStrat crearNueva();
    void initApariencia(Objeto obj);
    void onStart();
    void onStop();
}
