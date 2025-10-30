package com.mygdx.game.imovimiento;

import com.mygdx.game.ObjetoCayendo;
import com.mygdx.game.GameManager;

public class MovimientoDiagonal implements IComportamientoMovimiento {
    private final float velY, velX, rotDeg;

    public MovimientoDiagonal(float velY, float velX, float rotDeg) {
        this.velY = velY;
        this.velX = velX;
        this.rotDeg = rotDeg;
    }

    @Override
    public void mover(ObjetoCayendo obj, float dt) {
        obj.getHitbox().y -= velY * dt;
        obj.getHitbox().x += velX * dt;
        obj.setRotacion(rotDeg);
    }

    @Override public void initApariencia(ObjetoCayendo obj) { obj.setRotacion(rotDeg); }

    @Override public void onStart() { GameManager.getInstance().startWind(); }
    @Override public void onStop()  { GameManager.getInstance().stopWind();  }

    @Override public float getDerivaHorizontal(float fallHeight, float fallSpeed) {
        return velX * (fallHeight / fallSpeed);
    }
    @Override public float getRotacion() { return rotDeg; }
    @Override public float getVelocidadVertical() { return velY; }

    @Override
    public IComportamientoMovimiento crearNueva() {
        return new MovimientoDiagonal(velY, velX, rotDeg);
    }
}
