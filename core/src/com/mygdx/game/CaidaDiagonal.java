package com.mygdx.game;

import com.badlogic.gdx.audio.Music;

public class CaidaDiagonal implements MovimientoStrat {
    private final float velY;
    private final float velX;
    private final float tiltDeg;
    private final Music wind;

    public CaidaDiagonal(float velY, float velX, Music wind) {
        this.velY = velY;
        this.velX = velX;
        this.wind = wind;
        this.tiltDeg = velX >= 0 ? 13f : -13f;
    }

    @Override public void mover(Objeto obj, float dt) {
        obj.getArea().y -= velY * dt;
        obj.getArea().x += velX * dt;
    }

    @Override public MovimientoStrat crearNueva() {
        return new CaidaDiagonal(velY, velX, wind);
    }

    @Override public void initApariencia(Objeto obj) {
        obj.setRotacion(tiltDeg);
    }

    @Override public void onStart() {
        if (wind != null) { wind.setLooping(true); wind.play(); }
    }

    @Override public void onStop() {
        if (wind != null) { wind.stop(); wind.setPosition(0f); }
    }
}
