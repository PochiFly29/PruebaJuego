package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Interfaz (Contrato) para el Patrón Strategy
public interface IComportamientoMovimiento {

    // Cada estrategia debe saber cómo moverse
    // Recibe el objeto que debe mover y el tiempo delta
    void mover(ObjetoQueCae objeto, float delta);
}