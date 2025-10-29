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
    private Sound sndPowerup;
    private Music rainMusic;
    private Texture texEscudo;
    private Texture texIman;
    private Texture texTormenta;

    private final long SPAWN_TIMER_NORMAL = 100000000; // 0.1s
    private final long SPAWN_TIMER_TORMENTA = 30000000; // 0.03s

    public Lluvia(Texture gotaBuena, Texture gotaMala, Texture vidaExtra,
                  Texture texEscudo, Texture texIman, Texture texTormenta,
                  Sound dropSound, Sound lifeSound, Sound powerupSound, Music mm) {
        this.rainMusic = mm;
        this.sndDrop = dropSound;
        this.sndVida = lifeSound;
        this.sndPowerup = powerupSound; // Asignar sonido
        this.texGotaBuena = gotaBuena;
        this.texGotaMala = gotaMala;
        this.texVidaExtra = vidaExtra;
        this.texEscudo = texEscudo; // Asignar texturas
        this.texIman = texIman;
        this.texTormenta = texTormenta;
    }

    public void crear() {
        objetosEnPantalla = new Array<ObjetoQueCae>();
        crearObjetoQueCae();
        rainMusic.setLooping(true);
        rainMusic.play();
    }

    // Lógica de la Fábrica
    private void crearObjetoQueCae() {

        GameManager gm = GameManager.getInstance();

        // 1. Preguntar al Cerebro si estamos en pausa
        if (gm.estaEnPausa()) {
            lastDropTime = TimeUtils.nanoTime();
            return;
        }

        // 2. Lógica de Spawneo (Normal vs Tormenta)
        if (gm.getEstadoActual() == GameManager.EstadoJuego.TORMENTA_ESPECIAL) {
            // --- MODO TORMENTA ---
            // Solo crea Gotas Buenas muy rápido
            IComportamientoMovimiento mov = gm.getMovimientoParaGota();
            float deriva = mov.getDerivaHorizontal(480, mov.getVelocidadVertical());
            float rot = mov.getRotacion();
            float spawnX = MathUtils.random(0, 800 - 64) - deriva;

            Rectangle hitbox = new Rectangle(spawnX, 480, 64, 64);
            ObjetoQueCae nuevaGota = new GotaBuena(texGotaBuena, hitbox, mov, sndDrop);
            nuevaGota.setRotacion(rot);
            objetosEnPantalla.add(nuevaGota);

        } else {
            // --- MODO NORMAL ---
            float chance = MathUtils.random(); // Valor entre 0.0 y 1.0
            boolean esPowerup = false;
            int tipoPowerup = 0; // 1:Vida, 2:Escudo, 3:Iman, 4:Tormenta

            // --- LÓGICA DE PROBABILIDAD REVISADA ---
            // 70% Gota Buena (chance < 0.70)
            // 27% Gota Mala (chance >= 0.70 y < 0.97)
            // 2% Vida Extra (chance >= 0.97 y < 0.99)
            // 1% Poderes Raros (Escudo, Imán, Tormenta) (chance >= 0.99)
            // -------------------------------------------

            if (chance < 0.70f) {
                // Es Gota Buena (no hacer nada aquí)
            } else if (chance < 0.97f) {
                // Es Gota Mala (no hacer nada aquí)
            } else if (chance < 0.99f) {
                // Es Vida Extra (2% de chance)
                esPowerup = true;
                tipoPowerup = 1; // 1 = Vida
            } else {
                // Es un Poder Raro (1% de chance total)
                esPowerup = true;
                // Asignar aleatoriamente entre Escudo (2), Imán (3) y Tormenta (4)
                tipoPowerup = MathUtils.random(2, 4);
            }
            // --- FIN DE LÓGICA REVISADA ---

            // 4. Pedir al Cerebro la Estrategia
            IComportamientoMovimiento movimiento;
            if (esPowerup) {
                movimiento = gm.getMovimientoParaPowerup();
            } else {
                movimiento = gm.getMovimientoParaGota();
            }

            // 5. Preguntar a la Estrategia
            float derivaHorizontal = movimiento.getDerivaHorizontal(480, movimiento.getVelocidadVertical());
            float rotacion = movimiento.getRotacion();

            // 6. Calcular Spawn
            float spawnXFinal = MathUtils.random(0, 800 - 64) - derivaHorizontal;
            Rectangle hitbox = new Rectangle(spawnXFinal, 480, 64, 64);

            // 7. Crear el Objeto
            ObjetoQueCae nuevoObjeto;

            if (esPowerup) {
                switch (tipoPowerup) {
                    case 1:
                        nuevoObjeto = new VidaExtra(texVidaExtra, hitbox, movimiento, sndVida);
                        break;
                    case 2:
                        nuevoObjeto = new PowerUpEscudo(texEscudo, hitbox, movimiento, sndPowerup);
                        break;
                    case 3:
                        nuevoObjeto = new PowerUpIman(texIman, hitbox, movimiento, sndPowerup);
                        break;
                    case 4:
                        nuevoObjeto = new PowerUpTormenta(texTormenta, hitbox, movimiento, sndPowerup);
                        break;
                    default:
                        // Fallback a Gota Buena
                        nuevoObjeto = new GotaBuena(texGotaBuena, hitbox, movimiento, sndDrop);
                }
            } else {
                // Gota Buena vs Gota Mala
                if (chance < 0.70f) { // 70% Gota Buena
                    nuevoObjeto = new GotaBuena(texGotaBuena, hitbox, movimiento, sndDrop);
                } else { // Gota Mala
                    nuevoObjeto = new GotaMala(texGotaMala, hitbox, movimiento);
                }
            }

            nuevoObjeto.setRotacion(rotacion);
            objetosEnPantalla.add(nuevoObjeto);
        }

        lastDropTime = TimeUtils.nanoTime();
    }

    // Actualiza todos los objetos
    public void actualizarMovimiento(Tarro tarro) {

        // --- TIMER DE SPAWN MODIFICADO ---
        long timerSpawnRequerido = (GameManager.getInstance().getEstadoActual() == GameManager.EstadoJuego.TORMENTA_ESPECIAL)
                ? SPAWN_TIMER_TORMENTA
                : SPAWN_TIMER_NORMAL;

        if (TimeUtils.nanoTime() - lastDropTime > timerSpawnRequerido)
            crearObjetoQueCae();

        // (Iteración y update de objetos sin cambios)
        for (int i = objetosEnPantalla.size - 1; i >= 0; i--) {
            ObjetoQueCae objeto = objetosEnPantalla.get(i);
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