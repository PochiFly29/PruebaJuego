package com.mygdx.game;

// Importamos Gdx para el delta time
import com.badlogic.gdx.Gdx;

// Implementación concreta del patrón Strategy
public class MovimientoDiagonal implements IComportamientoMovimiento {

    private float velocidadVertical;
    private float velocidadHorizontal;
    private float anguloRotacion;

    public MovimientoDiagonal(float velocidadVertical, float velocidadHorizontal, float angulo) {
        this.velocidadVertical = velocidadVertical;
        this.velocidadHorizontal = velocidadHorizontal; // Positivo para derecha, negativo para izquierda
        this.anguloRotacion = angulo;
    }

    @Override
    public void mover(ObjetoQueCae objeto, float delta) {
        // 1. Mover el hitbox en ambos ejes
        objeto.hitbox.y -= velocidadVertical * delta;
        objeto.hitbox.x += velocidadHorizontal * delta;

        // 2. Asignar la rotación al objeto
        // El objeto ahora tendrá este ángulo al dibujarse
        objeto.setRotacion(anguloRotacion);
    }
}