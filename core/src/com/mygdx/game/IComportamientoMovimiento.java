package com.mygdx.game;

public interface IComportamientoMovimiento {

    /**
     * Actualiza la posición del objeto que cae.
     * @param objeto La instancia de ObjetoQueCae a mover.
     * @param delta El tiempo transcurrido desde el último frame.
     */
    void mover(ObjetoQueCae objeto, float delta);

    /**
     * Calcula la desviación horizontal total que tendrá el objeto
     * durante su caída.
     * @param fallHeight La altura total de la caída (ej: 480).
     * @param fallSpeed La velocidad vertical base.
     * @return El offset horizontal total (ej: 160 para diagonal, 0 para recto).
     */
    float getDerivaHorizontal(float fallHeight, float fallSpeed);

    /**
     * Devuelve el ángulo de rotación inicial que debe tener el sprite.
     * @return El ángulo en grados.
     */
    float getRotacion();

    /**
     * Devuelve la velocidad vertical base de esta estrategia.
     * @return La velocidad vertical en píxeles/segundo.
     */
    float getVelocidadVertical();
}