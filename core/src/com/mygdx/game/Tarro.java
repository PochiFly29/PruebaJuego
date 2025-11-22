package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

public class Tarro {
    private Rectangle bucket;
    private Texture bucketImage;
    private Sound sonidoHerido;
    private int velx = 400;
    private boolean herido = false;
    private int tiempoHeridoMax = 50;
    private int tiempoHerido;
    private Texture escudoImage;

    private Texture imanImage;
    private boolean mirandoDerecha = true;

    private Polygon polyBoca;

    public Tarro(Texture tex, Sound ss) {
        bucketImage = tex;
        sonidoHerido = ss;
    }

    public void setEscudoTexture(Texture tex) {
        this.escudoImage = tex;
    }

    public void setImanTexture(Texture tex) {
        this.imanImage = tex;
    }

    public Rectangle getAABB() {
        return bucket;
    }

    public void crear() {
        bucket = new Rectangle();
        bucket.x = 800 / 2f - 64 / 2f;
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;
        defineBocaPoligonal();
        syncHitboxes();
    }

    public void dañar() {
        GameManager.getInstance().perderVida();
        herido = true;
        tiempoHerido = tiempoHeridoMax;
        sonidoHerido.play();
    }

    public void dibujar(SpriteBatch batch) {
        float drawX = bucket.x;
        float drawY = bucket.y;
        float drawW = bucket.width;
        float drawH = bucket.height;

        if (!mirandoDerecha) {
            drawX = bucket.x + bucket.width;
            drawW = -bucket.width;
        }

        if (!herido) {
            batch.draw(bucketImage, drawX, drawY, drawW, drawH);
        } else {
            float shakeY = drawY + MathUtils.random(-5, 5);
            batch.draw(bucketImage, drawX, shakeY, drawW, drawH);
            tiempoHerido--;
            if (tiempoHerido <= 0)
                herido = false;
        }

        if (GameManager.getInstance().isEscudoActivo() && escudoImage != null) {
            batch.draw(escudoImage, bucket.x - 8, bucket.y - 8, 80, 80);
        }

        if (GameManager.getInstance().isImanActivo() && imanImage != null) {
            float imanWidth = 60f;
            float imanHeight = 60f;
            float x = bucket.x + bucket.width / 2f - imanWidth / 2f;
            float y = bucket.y + bucket.height - 22f;
            batch.draw(imanImage, x, y, imanWidth, imanHeight);
        }
    }

    public void actualizarMovimiento() {
        float dt = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= velx * dt;
            mirandoDerecha = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += velx * dt;
            mirandoDerecha = true;
        }

        if (bucket.x < 0) bucket.x = 0;
        if (bucket.x > 800 - bucket.width) bucket.x = 800 - bucket.width;
        syncHitboxes();
    }

    public void destruir() {
        bucketImage.dispose();
    }

    public Polygon getPolyBoca() {
        return polyBoca;
    }

    public boolean estaHerido() {
        return herido;
    }

    public boolean colisionaCon(ObjetoCayendo o) {
        if (o.usaPoligono()) {
            return Intersector.overlapConvexPolygons(polyBoca, o.getHitPolygonWorld());
        } else {
            Polygon p = circleToPolygon(o.getHitCircle().x, o.getHitCircle().y, o.getHitCircle().radius, 14);
            return Intersector.overlapConvexPolygons(polyBoca, p);
        }
    }

    private void defineBocaPoligonal() {
        float w = bucket.width, h = bucket.height;
        float[] verts = new float[] {
                0.10f * w, 0.75f * h,
                0.90f * w, 0.75f * h,
                0.80f * w, 0.20f * h,
                0.20f * w, 0.20f * h
        };
        polyBoca = new Polygon(verts);
        polyBoca.setOrigin(0, 0);
        polyBoca.setPosition(bucket.x, bucket.y);
    }

    private void syncHitboxes() {
        if (polyBoca != null) {
            polyBoca.setPosition(bucket.x, bucket.y);
        }
    }

    private static Polygon circleToPolygon(float cx, float cy, float r, int segments) {
        float[] v = new float[segments * 2];
        for (int i = 0; i < segments; i++) {
            float a = (float) (i * Math.PI * 2 / segments);
            v[2 * i] = cx + r * (float) Math.cos(a);
            v[2 * i + 1] = cy + r * (float) Math.sin(a);
        }
        return new Polygon(v);
    }
}
