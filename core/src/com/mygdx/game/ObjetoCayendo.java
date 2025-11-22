package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.imovimiento.IMovimientos;

public abstract class ObjetoCayendo {

    protected Rectangle hitbox;
    protected Circle hitCircle;
    protected Polygon polyLocal;
    protected Polygon polyWorld;

    protected Texture textura;
    protected IMovimientos movimiento;
    protected float rotacion = 0f;
    public boolean marcadoParaEliminar = false;

    protected float spawnX;
    protected float tiempoEnVida;

    private static final float VELOCIDAD_IMAN = 400f;
    private static final float DISTANCIA_IMAN_MAX = 200f;

    private boolean started = false;
    private boolean stopped = false;

    protected float circleScale = 0.35f;

    public ObjetoCayendo(Texture textura, Rectangle hitbox, IMovimientos movimiento) {
        this.textura = textura;
        this.hitbox = hitbox;
        this.movimiento = movimiento;
        this.spawnX = hitbox.x;
        this.tiempoEnVida = 0f;

        refreshCircle();

        this.polyLocal = null;
        this.polyWorld = null;

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

        if (hitbox.overlaps(tarro.getAABB())) {
            if (tarro.colisionaCon(this)) {
                alColisionar(tarro);
                marcadoParaEliminar = true;
            }
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
            Vector2 posTarro = new Vector2(tarro.getAABB().x + tarro.getAABB().width / 2f, tarro.getAABB().y);
            Vector2 posObj = new Vector2(hitbox.x + hitbox.width / 2f, hitbox.y);
            if (posTarro.dst(posObj) < DISTANCIA_IMAN_MAX) {
                Vector2 dir = posTarro.sub(posObj).nor();
                hitbox.x += dir.x * VELOCIDAD_IMAN * delta;
                hitbox.y += dir.y * VELOCIDAD_IMAN * delta;
                syncTransforms();
                return;
            }
        }
        if (movimiento != null) movimiento.mover(this, delta);
        syncTransforms();
    }

    private void syncTransforms() {
        hitCircle.setPosition(hitbox.x + hitbox.width/2f, hitbox.y + hitbox.height/2f);
        hitCircle.setRadius(circleScale * Math.min(hitbox.width, hitbox.height));

        if (polyLocal != null) {
            if (polyWorld == null) {
                polyWorld = new Polygon(polyLocal.getVertices().clone());
            } else {
                polyWorld.setVertices(polyLocal.getVertices().clone());
            }
            polyWorld.setOrigin(hitbox.width/2f, hitbox.height/2f);
            polyWorld.setRotation(rotacion);
            polyWorld.setPosition(hitbox.x, hitbox.y);
        }
    }

    protected void setCircleScale(float s) {
        this.circleScale = s;
        refreshCircle();
    }

    private void refreshCircle() {
        float r = circleScale * Math.min(hitbox.width, hitbox.height);
        this.hitCircle = new Circle(hitbox.x + hitbox.width/2f, hitbox.y + hitbox.height/2f, r);
    }

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

    public Circle  getHitCircle() { return hitCircle; }
    public Polygon getHitPolygonWorld()  { return polyWorld; }
    public boolean usaPoligono() { return polyLocal != null; }

    public void setRotacion(float rotacion) { this.rotacion = rotacion; }

    protected abstract void alColisionar(Tarro tarro);
    protected boolean esAtraible() { return false; }
}