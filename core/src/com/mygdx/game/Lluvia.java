package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

// La "Fábrica" (Spawner)
public class Lluvia {

    private Array<ObjetoQueCae> objetosEnPantalla;
    private long lastDropTime;

    // Almacén de assets
    private Texture texGotaBuena;
    private Texture texGotaMala;
    private Texture texVidaExtra;
    private Sound sndDrop;
    private Sound sndVida;
    private Music rainMusic;

    public Lluvia(Texture gotaBuena, Texture gotaMala, Texture vidaExtra, Sound dropSound, Sound lifeSound, Music mm) {
        this.rainMusic = mm;
        this.sndDrop = dropSound;
        this.sndVida = lifeSound;
        this.texGotaBuena = gotaBuena;
        this.texGotaMala = gotaMala;
        this.texVidaExtra = vidaExtra;
    }

    public void crear() {
        objetosEnPantalla = new Array<ObjetoQueCae>();
        crearObjetoQueCae();

        rainMusic.setLooping(true);
        rainMusic.play();
    }

    // Lógica de la Fábrica
    private void crearObjetoQueCae() {

        // 1. Preguntar al Cerebro si estamos en pausa
        if (GameManager.getInstance().estaEnPausa()) {
            lastDropTime = TimeUtils.nanoTime();
            return;
        }

        // 2. Decidir qué crear (Powerup o Gota)
        boolean esPowerup = false;
        float chance = MathUtils.random();
        if (chance > 0.99f) { // 1% de chance de Powerup (VidaExtra)
            esPowerup = true;
        }

        // 3. Pedir al Cerebro la Estrategia de movimiento
        IComportamientoMovimiento movimiento;
        if (esPowerup) {
            movimiento = GameManager.getInstance().getMovimientoParaPowerup();
        } else {
            movimiento = GameManager.getInstance().getMovimientoParaGota();
        }

        // 4. Preguntar a la Estrategia sus propiedades
        float altoPantalla = 480;
        float velocidadVertical = movimiento.getVelocidadVertical();
        float derivaHorizontal = movimiento.getDerivaHorizontal(altoPantalla, velocidadVertical);
        float rotacion = movimiento.getRotacion();

        // 5. Calcular Spawn (Genérico, basado en la deriva)
        float anchoPantalla = 800;
        float anchoGota = 64;
        float spawnXBase = MathUtils.random(0, anchoPantalla - anchoGota);
        float spawnXFinal = spawnXBase - derivaHorizontal;

        // 6. Crear el Objeto
        Rectangle hitbox = new Rectangle();
        hitbox.x = spawnXFinal;
        hitbox.y = altoPantalla;
        hitbox.width = anchoGota;
        hitbox.height = 64;

        ObjetoQueCae nuevoObjeto;

        if (esPowerup) {
            nuevoObjeto = new VidaExtra(texVidaExtra, hitbox, movimiento, sndVida);
        } else {
            if (chance < 0.70f) { // 70% Gota Buena
                nuevoObjeto = new GotaBuena(texGotaBuena, hitbox, movimiento, sndDrop);
            } else { // 29% Gota Mala
                nuevoObjeto = new GotaMala(texGotaMala, hitbox, movimiento);
            }
        }

        nuevoObjeto.setRotacion(rotacion); // Aplicar rotación inicial
        objetosEnPantalla.add(nuevoObjeto);
        lastDropTime = TimeUtils.nanoTime();
    }

    // Actualiza todos los objetos
    public void actualizarMovimiento(Tarro tarro) {
        if (TimeUtils.nanoTime() - lastDropTime > 100000000)
            crearObjetoQueCae();

        for (int i = objetosEnPantalla.size - 1; i >= 0; i--) {
            ObjetoQueCae objeto = objetosEnPantalla.get(i);

            // Llamar al Template Method
            objeto.update(Gdx.graphics.getDeltaTime(), tarro);

            if (objeto.marcadoParaEliminar) {
                objetosEnPantalla.removeIndex(i);
            }
        }
    }

    // Dibuja todos los objetos
    public void actualizarDibujoLluvia(SpriteBatch batch) {
        for (ObjetoQueCae objeto : objetosEnPantalla) {
            objeto.dibujar(batch);
        }
    }

    public void destruir() {
        rainMusic.dispose();
    }

    public void pausar() {
        rainMusic.stop();
    }

    public void continuar() {
        rainMusic.play();
    }
}