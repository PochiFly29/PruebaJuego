package com.mygdx.game;

public interface MovimientoStrat {
    void mover(Objeto obj, float dt);
    MovimientoStrat crearNueva();
    void initApariencia(Objeto obj);
    void onStart();
    void onStop();
}
