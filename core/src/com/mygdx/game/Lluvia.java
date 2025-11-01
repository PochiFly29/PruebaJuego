package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.iescenario.IEscenarios;
import com.mygdx.game.iescenario.EscenarioNormal;

import java.util.EnumMap;
import java.util.Map;

import com.mygdx.game.imovimiento.IMovimientos;

public class Lluvia {

    public enum TipoSpawn {
        BUENA, MALA, VIDA, ESCUDO, IMAN, TORMENTA
    }

    private interface Fabrica {
        ObjetoCayendo crear(Rectangle hb, IMovimientos mov);
    }

    private final Array<ObjetoCayendo> objetos = new Array<ObjetoCayendo>();
    private long lastDropTime;
    private IEscenarios escenario;

    private final Texture texBuena, texMala, texVida, texEscudo, texIman, texTormenta;
    private final com.badlogic.gdx.audio.Sound sndDrop, sndVida, sndPowerup;
    private final Music rainMusic;

    private int ancho = 64, alto = 64;
    private float pantallaAncho = 800f, pantallaAlto = 480f;

    private long spawnNormalNs = 100_000_000L;
    private long spawnTormentaNs = 30_000_000L;

    private float pBuena = 0.70f;
    private float pMala  = 0.27f;
    private float pVida  = 0.01f;

    private float pEscudo = 1f/3f, pIman = 1f/3f, pTormenta = 1f/3f;

    private boolean soloBuenas = false;

    private final Map<TipoSpawn, Fabrica> fabrica = new EnumMap<TipoSpawn, Fabrica>(TipoSpawn.class);

    public Iterable<ObjetoCayendo> getObjetos() {
        return objetos;
    }

    public Lluvia(Texture gotaBuena, Texture gotaMala, Texture vidaExtra, Texture texEscudo, Texture texIman, final Texture texTormenta, com.badlogic.gdx.audio.Sound dropSound, com.badlogic.gdx.audio.Sound lifeSound, com.badlogic.gdx.audio.Sound powerupSound, Music rainMusic) {

        this.texBuena = gotaBuena;
        this.texMala = gotaMala;
        this.texVida = vidaExtra;
        this.texEscudo = texEscudo;
        this.texIman = texIman;
        this.texTormenta = texTormenta;
        this.sndDrop = dropSound;
        this.sndVida = lifeSound;
        this.sndPowerup = powerupSound;
        this.rainMusic = rainMusic;

        fabrica.put(TipoSpawn.BUENA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IMovimientos mov) {
                return new GotaBuena(Lluvia.this.texBuena, hb, mov, Lluvia.this.sndDrop);
            }
        });
        fabrica.put(TipoSpawn.MALA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IMovimientos mov) {
                return new GotaMala(Lluvia.this.texMala, hb, mov);
            }
        });
        fabrica.put(TipoSpawn.VIDA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IMovimientos mov) {
                return new VidaExtra(Lluvia.this.texVida, hb, mov, Lluvia.this.sndVida);
            }
        });
        fabrica.put(TipoSpawn.ESCUDO, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IMovimientos mov) {
                return new PowerUpEscudo(Lluvia.this.texEscudo, hb, mov, Lluvia.this.sndPowerup);
            }
        });
        fabrica.put(TipoSpawn.IMAN, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IMovimientos mov) {
                return new PowerUpIman(Lluvia.this.texIman, hb, mov, Lluvia.this.sndPowerup);
            }
        });
        fabrica.put(TipoSpawn.TORMENTA, new Fabrica() {
            public ObjetoCayendo crear(Rectangle hb, IMovimientos mov) {
                return new PowerUpTormenta(Lluvia.this, texTormenta, hb, mov, sndPowerup);
            }
        });

        this.escenario = new EscenarioNormal();
        this.escenario.init(this);
    }

    public Lluvia setTamañoHitbox(int ancho, int alto) { this.ancho = ancho; this.alto = alto; return this; }
    public Lluvia setDimensionesPantalla(float w, float h) { this.pantallaAncho = w; this.pantallaAlto = h; return this; }
    public Lluvia setTimers(long normalNs, long tormentaNs) { this.spawnNormalNs = normalNs; this.spawnTormentaNs = tormentaNs; return this; }
    public Lluvia setProbabilidades(float pBuena, float pMala, float pVida, float pRaras) {
        this.pBuena = pBuena; this.pMala = pMala; this.pVida = pVida;
        return this;
    }
    public Lluvia setRarasDistrib(float pEscudo, float pIman, float pTormenta) {
        this.pEscudo = pEscudo; this.pIman = pIman; this.pTormenta = pTormenta; return this;
    }
    public void setSoloBuenas(boolean soloBuenas) { this.soloBuenas = soloBuenas; }

    public void crear() {
        objetos.clear();
        lastDropTime = TimeUtils.nanoTime();
        rainMusic.setLooping(true);
        rainMusic.play();
    }

    public void actualizarMovimiento(Tarro tarro) {
        float dt = Gdx.graphics.getDeltaTime();

        boolean enPausa = GameManager.getInstance().estaEnPausa();

        if (escenario != null) {
            if (!enPausa) {
                escenario.update(this, tarro, dt);
            }
        } else {
            if (!enPausa) {
                long spawnTarget = (GameManager.getInstance().getEstadoActual() == GameManager.EstadoJuego.TORMENTA_ESPECIAL) ? spawnTormentaNs : spawnNormalNs;
                if (TimeUtils.nanoTime() - lastDropTime > spawnTarget) {
                    TipoSpawn tipo = elegirTipo(false);
                    spawnAhora(tipo);
                    lastDropTime = TimeUtils.nanoTime();
                }
            }
        }

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

    public void setEscenario(IEscenarios nuevo) {
        this.escenario = nuevo;
        if (this.escenario != null) this.escenario.init(this);
    }

    public void cambiarEscenarioNormal() {
        setEscenario(new EscenarioNormal());
    }

    public void spawnAhora(TipoSpawn tipo) {
        GameManager gm = GameManager.getInstance();
        IMovimientos base = (tipo == TipoSpawn.BUENA || tipo == TipoSpawn.MALA) ? gm.getMovimientoParaGota() : gm.getMovimientoParaPowerup();
        IMovimientos mov = base.crearNueva();
        spawnCore(tipo, mov);
    }

    public void spawnAhora(TipoSpawn tipo, IMovimientos movOverride) {
        IMovimientos mov = (movOverride != null) ? movOverride.crearNueva() : null;
        if (mov == null) {
            spawnAhora(tipo);
            return;
        }
        spawnCore(tipo, mov);
    }

    private void spawnCore(TipoSpawn tipo, IMovimientos mov) {
        float velY   = Math.max(1e-6f, mov.getVelocidadVertical());
        float deriva = mov.getDerivaHorizontal(pantallaAlto, velY);
        float rot    = mov.getRotacion();

        float xSpawn = MathUtils.random(0f, Math.max(0f, pantallaAncho - ancho)) - deriva;
        Rectangle hb = new Rectangle(xSpawn, pantallaAlto, ancho, alto);

        Fabrica fab = fabrica.get(tipo);
        if (fab == null) fab = fabrica.get(TipoSpawn.BUENA);
        ObjetoCayendo obj = fab.crear(hb, mov);
        obj.setRotacion(rot);
        objetos.add(obj);
    }

    public TipoSpawn elegirTipoNormal() {
        return elegirTipo(false);
    }

    private TipoSpawn elegirTipo(boolean enTormenta) {
        if (soloBuenas || enTormenta) return TipoSpawn.BUENA;

        float pVidaActual = this.pVida;
        switch (GameManager.getInstance().getEstadoActual()) {
            case ETAPA_1:
                pVidaActual = 0.005f; // menos vidas en nivel 1
                break;
            default:
                pVidaActual = this.pVida;
        }

        float r = MathUtils.random();
        float tBuena = pBuena;
        float tMala  = tBuena + pMala;
        float tVida  = tMala  + pVidaActual;

        if (r < tBuena)  return TipoSpawn.BUENA;
        if (r < tMala)   return TipoSpawn.MALA;
        if (r < tVida)   return TipoSpawn.VIDA;

        float rr = MathUtils.random();
        float e = pEscudo;
        float i = e + pIman;
        return (rr < e) ? TipoSpawn.ESCUDO : (rr < i ? TipoSpawn.IMAN : TipoSpawn.TORMENTA);
    }
}
