package com.mygdx.game;

// Implementación de Strategy: Caída en forma de "S"
public class MovimientoSerpenteante implements IComportamientoMovimiento {

    private float velocidadVertical;
    private float amplitud; // Qué tan ancha es la "S"
    private float frecuencia; // Qué tan rápida es la "S"

    public MovimientoSerpenteante(float velVertical, float amplitud, float frecuencia) {
        this.velocidadVertical = velVertical;
        this.amplitud = amplitud;
        this.frecuencia = frecuencia;
    }

    @Override
    public void mover(ObjetoQueCae objeto, float delta) {
        // 1. Mover verticalmente
        objeto.hitbox.y -= velocidadVertical * delta;

        // 2. Calcular la "S" usando la X inicial y el tiempo en vida
        float tiempo = objeto.getTiempoEnVida();
        float spawnX = objeto.getSpawnX();

        // Calcular oscilación con la función Seno
        float offsetX = (float)Math.sin(tiempo * frecuencia) * amplitud;

        // Aplicar la oscilación
        objeto.hitbox.x = spawnX + offsetX;

        // Opcional: Rotar el sprite según la curva (función Coseno)
        float rotacion = (float)Math.cos(tiempo * frecuencia) * -15; // Rota max 15 grados
        objeto.setRotacion(rotacion);
    }

    @Override
    public float getDerivaHorizontal(float fallHeight, float fallSpeed) {
        // Oscila en su eje, no tiene deriva neta
        return 0f;
    }

    @Override
    public float getRotacion() {
        // Inicia sin rotación
        return 0f;
    }

    @Override
    public float getVelocidadVertical() {
        return this.velocidadVertical;
    }
}