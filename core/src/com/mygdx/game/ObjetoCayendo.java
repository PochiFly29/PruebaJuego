package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.imovimiento.IComportamientoMovimiento;

public abstract class ObjetoCayendo {

    protected Rectangle hitbox;
    protected Texture textura;
    protected IComportamientoMovimiento movimiento;
    protected float rotacion = 0f;
    public boolean marcadoParaEliminar = false;

    protected float spawnX;
    protected float tiempoEnVida;

    private static final float VELOCIDAD_IMAN = 400f;
    private static final float DISTANCIA_IMAN_MAX = 200f;

    private boolean started = false;
    private boolean stopped = false;

    public ObjetoCayendo(Texture textura, Rectangle hitbox, IComportamientoMovimiento movimiento) {
        this.textura = textura;
        this.hitbox = hitbox;
        this.movimiento = movimiento;
        this.spawnX = hitbox.x;
        this.tiempoEnVida = 0f;
        if (movimiento != null) movimiento.initApariencia(this);
    }

    public ObjetoCayendo(Texture textura) {
        this(textura, new Rectangle(0, 0, 64, 64), null);
    }

    public final void update(float delta, Tarro tarro) {
        if (movimiento != null && !started) { movimiento.onStart(); started = true; }

        if (!tarro.estaHerido()) {
            tiempoEnVida += delta;
            aplicarMovimiento(delta, tarro);
        }

        if (hitbox.overlaps(tarro.getArea())) {
            alColisionar(tarro);
            marcadoParaEliminar = true;
        }

        if (hitbox.y + hitbox.height < 0) {
            marcadoParaEliminar = true;
        }

        if (marcadoParaEliminar && movimiento != null && !stopped) {
            movimiento.onStop();
            stopped = true;
        }
    }

    private void aplicarMovimiento(float delta, Tarro tarro) {
        boolean imanActivo = GameManager.getInstance().isImanActivo();

        if (imanActivo && esAtraible()) {
            Vector2 posTarro = new Vector2(
                    tarro.getArea().x + tarro.getArea().width / 2f,
                    tarro.getArea().y
            );
            Vector2 posObj = new Vector2(
                    hitbox.x + hitbox.width / 2f,
                    hitbox.y
            );
            if (posTarro.dst(posObj) < DISTANCIA_IMAN_MAX) {
                Vector2 dir = posTarro.sub(posObj).nor();
                hitbox.x += dir.x * VELOCIDAD_IMAN * delta;
                hitbox.y += dir.y * VELOCIDAD_IMAN * delta;
                return;
            }
        }
        if (movimiento != null) movimiento.mover(this, delta);
    }

    protected abstract void alColisionar(Tarro tarro);
    protected boolean esAtraible() { return false; }

    public void dibujar(SpriteBatch batch) {
        batch.draw(
                textura,
                hitbox.x, hitbox.y,
                hitbox.width / 2f, hitbox.height / 2f,
                hitbox.width, hitbox.height,
                1f, 1f,
                rotacion,
                0, 0,
                textura.getWidth(), textura.getHeight(),
                false, false
        );
    }

    public Rectangle getHitbox() { return hitbox; }
    public Rectangle getArea() { return hitbox; }
    public void setRotacion(float rotacion) { this.rotacion = rotacion; }

    public void setComportamiento(IComportamientoMovimiento movimiento) {
        this.movimiento = movimiento;
        if (movimiento != null) movimiento.initApariencia(this);
    }

    public float getTiempoEnVida() { return tiempoEnVida; }
    public float getSpawnX() { return spawnX; }
}