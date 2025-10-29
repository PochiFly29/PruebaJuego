package com.mygdx.game;

import com.badlogic.gdx.Gdx;

// Implementación de Strategy: Caída diagonal
public class MovimientoDiagonal implements IComportamientoMovimiento {

    private float velocidadVertical;
    private float velocidadHorizontal;
    private float anguloRotacion;

    public MovimientoDiagonal(float velocidadVertical, float velocidadHorizontal, float angulo) {
        this.velocidadVertical = velocidadVertical;
        this.velocidadHorizontal = velocidadHorizontal;
        this.anguloRotacion = angulo;
    }

    @Override
    public void mover(ObjetoQueCae objeto, float delta) {
        // Mover en ambos ejes
        objeto.hitbox.y -= velocidadVertical * delta;
        objeto.hitbox.x += velocidadHorizontal * delta;

        // Asignar rotación (por si cambia en tiempo real)
        objeto.setRotacion(anguloRotacion);
    }

    @Override
    public float getDerivaHorizontal(float fallHeight, float fallSpeed) {
        // Calcula la deriva total basado en el tiempo de caída
        float tiempoDeCaida = fallHeight / fallSpeed;
        return this.velocidadHorizontal * tiempoDeCaida;
    }

    @Override
    public float getRotacion() {
        // Informa su rotación inicial
        return this.anguloRotacion;
    }

    @Override
    public float getVelocidadVertical() {
        return this.velocidadVertical;
    }
}