package com.mygdx.game.imovimiento;

import com.mygdx.game.ObjetoCayendo;

public class MovimientoSerpenteante implements IMovimientos {

    private final float velocidadVertical;
    private final float amplitud;
    private final float frecuencia;

    private float tAccum = 0f;
    private Float spawnX = null;

    public MovimientoSerpenteante(float velVertical, float amplitud, float frecuencia) {
        this.velocidadVertical = velVertical;
        this.amplitud = amplitud;
        this.frecuencia = frecuencia;
    }

    public void mover(ObjetoCayendo obj, float dt) {
        tAccum += dt;

        obj.getHitbox().y -= velocidadVertical * dt;

        if (spawnX == null) spawnX = obj.getHitbox().x;

        float offsetX = (float) Math.sin(tAccum * frecuencia) * amplitud;
        obj.getHitbox().x = spawnX + offsetX;

        float rot = (float) Math.cos(tAccum * frecuencia) * -15f;
        obj.setRotacion(rot);
    }

    public IMovimientos crearNueva() {
        return new MovimientoSerpenteante(velocidadVertical, amplitud, frecuencia);
    }

    public void initApariencia(ObjetoCayendo obj) { obj.setRotacion(0f); }
    public void onStart() {}
    public void onStop()  {}

    public float getDerivaHorizontal(float fallHeight, float fallSpeed) { return 0f; }
    public float getRotacion() { return 0f; }
    public float getVelocidadVertical() { return velocidadVertical; }
}
