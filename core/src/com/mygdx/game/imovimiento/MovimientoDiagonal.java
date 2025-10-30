package com.mygdx.game.imovimiento;

import com.badlogic.gdx.audio.Music;
import com.mygdx.game.ObjetoCayendo;

public class MovimientoDiagonal implements IComportamientoMovimiento {

    private final float velY;
    private final float velX;
    private final float rotDeg;
    private final Music wind; // opcional

    public MovimientoDiagonal(float velY, float velX, float rotDeg, Music wind) {
        this.velY = velY;
        this.velX = velX;
        this.rotDeg = rotDeg;
        this.wind = wind;
    }

    @Override
    public void mover(ObjetoCayendo obj, float dt) {
        obj.getHitbox().y -= velY * dt;
        obj.getHitbox().x += velX * dt;
        obj.setRotacion(rotDeg);
    }

    @Override
    public IComportamientoMovimiento crearNueva() {
        return new MovimientoDiagonal(velY, velX, rotDeg, wind);
    }

    @Override
    public void initApariencia(ObjetoCayendo obj) {
        obj.setRotacion(rotDeg);
    }

    @Override
    public void onStart() {
        if (wind != null) {
            wind.setLooping(true);
            wind.play();
        }
    }

    @Override
    public void onStop() {
        if (wind != null) {
            wind.stop();
            wind.setPosition(0f);
        }
    }

    @Override
    public float getDerivaHorizontal(float fallHeight, float fallSpeed) {
        float t = fallHeight / fallSpeed;
        return velX * t;
    }

    @Override
    public float getRotacion() {
        return rotDeg;
    }

    @Override
    public float getVelocidadVertical() {
        return velY;
    }
}
