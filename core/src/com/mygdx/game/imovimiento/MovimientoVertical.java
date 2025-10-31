package com.mygdx.game.imovimiento;

import com.mygdx.game.ObjetoCayendo;

public class MovimientoVertical implements IMovimientos {

    private final float velY;

    public MovimientoVertical(float velY) {
        this.velY = velY;
    }

    @Override
    public void mover(ObjetoCayendo obj, float dt) {
        obj.getHitbox().y -= velY * dt;
    }

    @Override
    public IMovimientos crearNueva() {
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
