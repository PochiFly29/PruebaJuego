package com.mygdx.game.imovimiento;

import com.mygdx.game.ObjetoCayendo;

public interface IComportamientoMovimiento {
    void mover(ObjetoCayendo obj, float dt);

    IComportamientoMovimiento crearNueva();
    void initApariencia(ObjetoCayendo obj);
    void onStart();
    void onStop();

    float getDerivaHorizontal(float fallHeight, float fallSpeed);
    float getRotacion();
    float getVelocidadVertical();
}
