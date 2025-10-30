package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.iescenario.EscenarioStrat;
import com.mygdx.game.iescenario.EscenarioNormal;
import com.mygdx.game.iescenario.EscenarioTorrencial;
import com.mygdx.game.imovimiento.IComportamientoMovimiento;

import java.util.EnumMap;
import java.util.Map;

public class Lluvia {

    public enum TipoSpawn { BUENA, MALA, VIDA, ESCUDO, IMAN, TORMENTA }

    private interface Fabrica {
        ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov);
    }

    private final Array<ObjetoCayendo> objetos = new Array<ObjetoCayendo>();
    private long lastDropTime;

    private final Texture texBuena, texMala, texVida, texEscudo, texIman, texTormenta;
    private final com.badlogic.gdx.audio.Sound sndDrop, sndVida, sndPowerup;
    private final Music rainMusic;

    private int ancho = 64, alto = 64;
    private float pantallaAncho = 800f, pantallaAlto = 480f;

    private float pBuena = 0.70f;
    private float pMala  = 0.27f;
    private float pVida  = 0.02f;
    private float pRaras = 0.01f;

    private float pEscudo = 1f/3f, pIman = 1f/3f, pTormenta = 1f/3f;

    private boolean soloBuenas = false;

    private final Map<TipoSpawn, Fabrica> fabrica = new EnumMap<TipoSpawn, Fabrica>(TipoSpawn.class);

    private EscenarioStrat escenario;

    public Lluvia(Texture gotaBuena, Texture gotaMala, Texture vidaExtra,
                  Texture escudo, Texture iman, Texture tormenta,
                  com.badlogic.gdx.audio.Sound dropSound, com.badlogic.gdx.audio.Sound lifeSound,
                  com.badlogic.gdx.audio.Sound powerupSound, Music rainMusic) {

        this.texBuena = gotaBuena;
        this.texMala = gotaMala;
        this.texVida = vidaExtra;
        this.texEscudo = escudo;
        this.texIman = iman;
        this.texTormenta = tormenta;

        this.sndDrop = dropSound;
        this.sndVida = lifeSound;
        this.sndPowerup = powerupSound;
        this.rainMusic = rainMusic;

        fabrica.put(TipoSpawn.BUENA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov) {
                return new GotaBuena(texBuena, hb, mov, sndDrop);
            }
        });
        fabrica.put(TipoSpawn.MALA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov) {
                return new GotaMala(texMala, hb, mov);
            }
        });
        fabrica.put(TipoSpawn.VIDA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov) {
                return new VidaExtra(texVida, hb, mov, sndVida);
            }
        });
        fabrica.put(TipoSpawn.ESCUDO, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov) {
                return new PowerUpEscudo(texEscudo, hb, mov, sndPowerup);
            }
        });
        fabrica.put(TipoSpawn.IMAN, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov) {
                return new PowerUpIman(texIman, hb, mov, sndPowerup);
            }
        });
        fabrica.put(TipoSpawn.TORMENTA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IComportamientoMovimiento mov) {
                return new PowerUpTormenta(texTormenta, hb, mov, sndPowerup);
            }
        });
    }

    public Lluvia setTamañoHitbox(int ancho, int alto) { this.ancho = ancho; this.alto = alto; return this; }
    public Lluvia setDimensionesPantalla(float w, float h) { this.pantallaAncho = w; this.pantallaAlto = h; return this; }
    public Lluvia setProbabilidades(float pBuena, float pMala, float pVida, float pRaras) {
        this.pBuena = pBuena; this.pMala = pMala; this.pVida = pVida; this.pRaras = pRaras; return this;
    }
    public Lluvia setRarasDistrib(float pEscudo, float pIman, float pTormenta) {
        this.pEscudo = pEscudo; this.pIman = pIman; this.pTormenta = pTormenta; return this;
    }
    public void setSoloBuenas(boolean soloBuenas) { this.soloBuenas = soloBuenas; }

    public void setEscenario(EscenarioStrat esc) {
        this.escenario = esc;
        if (this.escenario != null) this.escenario.init(this);
    }

    public void crear() {
        objetos.clear();
        lastDropTime = TimeUtils.nanoTime();
        if (escenario == null) setEscenario(new EscenarioNormal());
        rainMusic.setLooping(true);
        rainMusic.play();
    }

    public void actualizarMovimiento(Tarro tarro) {
        GameManager gm = GameManager.getInstance();

        boolean enTormenta = (gm.getEstadoActual() == GameManager.EstadoJuego.TORMENTA_ESPECIAL);
        if (enTormenta && !(escenario instanceof EscenarioTorrencial)) {
            setEscenario(new EscenarioTorrencial());
        } else if (!enTormenta && (escenario instanceof EscenarioTorrencial)) {
            setEscenario(new EscenarioNormal());
        }

        float dt = Gdx.graphics.getDeltaTime();
        escenario.update(this, tarro, dt);

        for (int i = objetos.size - 1; i >= 0; i--) {
            ObjetoCayendo o = objetos.get(i);
            o.update(dt, tarro);
            if (o.marcadoParaEliminar) objetos.removeIndex(i);
        }
    }

    public void actualizarDibujoLluvia(SpriteBatch batch) {
        for (ObjetoCayendo o : objetos) o.dibujar(batch);
    }

    public void pausar()    { rainMusic.stop(); }
    public void continuar() { rainMusic.play(); }
    public void destruir()  { rainMusic.dispose(); }

    public void cambiarEscenarioNormal() { setEscenario(new EscenarioNormal()); }

    public void crearGotaDeLluvia() {
        GameManager gm = GameManager.getInstance();

        TipoSpawn tipo;
        if (soloBuenas || gm.getEstadoActual() == GameManager.EstadoJuego.TORMENTA_ESPECIAL) {
            tipo = TipoSpawn.BUENA;
        } else {
            float r = MathUtils.random();
            float tBuena = pBuena;
            float tMala  = tBuena + pMala;
            float tVida  = tMala  + pVida;
            if (r < tBuena)      tipo = TipoSpawn.BUENA;
            else if (r < tMala)  tipo = TipoSpawn.MALA;
            else if (r < tVida)  tipo = TipoSpawn.VIDA;
            else {
                float rr = MathUtils.random();
                float e = pEscudo, i = e + pIman;
                tipo = (rr < e) ? TipoSpawn.ESCUDO : (rr < i ? TipoSpawn.IMAN : TipoSpawn.TORMENTA);
            }
        }

        IComportamientoMovimiento base = (tipo == TipoSpawn.BUENA || tipo == TipoSpawn.MALA)
                ? gm.getMovimientoParaGota()
                : gm.getMovimientoParaPowerup();
        IComportamientoMovimiento mov = base != null ? base.crearNueva() : null;

        float velY = (mov != null) ? Math.max(1e-6f, mov.getVelocidadVertical()) : 300f;
        float deriva = (mov != null) ? mov.getDerivaHorizontal(pantallaAlto, velY) : 0f;
        float rot = (mov != null) ? mov.getRotacion() : 0f;

        float xSpawn = MathUtils.random(0f, Math.max(0f, pantallaAncho - ancho)) - deriva;
        Rectangle hb = new Rectangle(xSpawn, pantallaAlto, ancho, alto);

        Fabrica fab = fabrica.get(tipo);
        if (fab == null) fab = fabrica.get(TipoSpawn.BUENA);
        ObjetoCayendo obj = fab.crear(hb, mov);
        obj.setRotacion(rot);
        objetos.add(obj);

        lastDropTime = TimeUtils.nanoTime();
    }
}
