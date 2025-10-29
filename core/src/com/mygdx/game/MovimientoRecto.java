package com.mygdx.game;

import com.badlogic.gdx.Gdx;

// Implementación de Strategy: Caída recta
public class MovimientoRecto implements IComportamientoMovimiento {

    private float velocidadBase;

    public MovimientoRecto(float velocidad) {
        this.velocidadBase = velocidad;
    }

    @Override
    public void mover(ObjetoQueCae objeto, float delta) {
        objeto.hitbox.y -= velocidadBase * delta;
    }

    @Override
    public float getDerivaHorizontal(float fallHeight, float fallSpeed) {
        // No tiene deriva horizontal
        return 0f;
    }

    @Override
    public float getRotacion() {
        // No tiene rotación
        return 0f;
    }

    @Override
    public float getVelocidadVertical() {
        return this.velocidadBase;
    }
}