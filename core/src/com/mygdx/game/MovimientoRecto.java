package com.mygdx.game;

import com.badlogic.gdx.Gdx;

// Implementación concreta del patrón Strategy
public class MovimientoRecto implements IComportamientoMovimiento {

    private float velocidadBase;

    public MovimientoRecto(float velocidad) {
        this.velocidadBase = velocidad;
    }

    @Override
    public void mover(ObjetoQueCae objeto, float delta) {
        // Lógica de movimiento de caída recta
        objeto.hitbox.y -= velocidadBase * delta;
    }
}