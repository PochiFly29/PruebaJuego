package com.mygdx.game.imovimiento;

import com.mygdx.game.ObjetoCayendo;

public class MovimientoVertical implements IComportamientoMovimiento {

    private final float velY;

    public MovimientoVertical(float velY) {
        this.velY = velY;
    }

    @Override
    public void mover(ObjetoCayendo obj, float dt) {
        obj.getHitbox().y -= velY * dt;
    }

    @Override
    public IComportamientoMovimiento crearNueva() {
        return new MovimientoVertical(velY);
    }

    @Override
    public void initApariencia(ObjetoCayendo obj) {
        obj.setRotacion(0f);
    }

    @Override
    public void onStart() { }

    @Override
    public void onStop() { }

    @Override
    public float getDerivaHorizontal(float fallHeight, float fallSpeed) {
        return 0f;
    }

    @Override
    public float getRotacion() {
        return 0f;
    }

    @Override
    public float getVelocidadVertical() {
        return velY;
    }
}
