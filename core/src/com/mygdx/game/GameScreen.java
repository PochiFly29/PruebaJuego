package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {

    private final GameLluviaMenu game;
    private OrthographicCamera camera;

    private BitmapFont fontHUD;
    private PausaScreen pausaScreen;

    private Tarro tarro;
    private Lluvia lluvia;

    private boolean debugHitbox = false;
    private ShapeRenderer shapeRenderer;

    // Texturas
    private Texture texTarro, texGotaBuena, texGotaMala, texVidaExtra, texEscudo, texIman, texTormenta, texTrueno, imgFondo;
    // Sonidos
    private Sound sndHurt, sndDrop, sndVida, sndPowerup, sndTrueno;
    private Music rainMusic, windMusic;

    // Trueno con parpadeo
    private boolean truenoActivo = false;
    private float truenoTimer = 0f;
    private static final float TRUENO_DURACION = 0.25f; // 250 ms

    public GameScreen(final GameLluviaMenu game) {
        this.game = game;

        // ----- FUENTES -----
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto.ttf"));

        // HUD
        FreeTypeFontGenerator.FreeTypeFontParameter pHUD = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pHUD.size = 28;
        pHUD.minFilter = Texture.TextureFilter.Linear;
        pHUD.magFilter = Texture.TextureFilter.Linear;
        this.fontHUD = gen.generateFont(pHUD);

        // Título de pausa
        FreeTypeFontGenerator.FreeTypeFontParameter pTitulo = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pTitulo.size = 60;
        BitmapFont fontTitulo = gen.generateFont(pTitulo);

        // Subtítulo de pausa
        FreeTypeFontGenerator.FreeTypeFontParameter pSub = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pSub.size = 22;
        BitmapFont fontSubtitulo = gen.generateFont(pSub);

        gen.dispose();

        pausaScreen = new PausaScreen(fontTitulo, fontSubtitulo);

        shapeRenderer = new ShapeRenderer();

        // ----- TEXTURAS -----
        imgFondo = new Texture(Gdx.files.internal("fondo.png"));
        texTarro = new Texture(Gdx.files.internal("bucket.png"));
        texGotaBuena = new Texture(Gdx.files.internal("drop.png"));
        texGotaMala = new Texture(Gdx.files.internal("dropBad.png"));
        texVidaExtra = new Texture(Gdx.files.internal("vidaExtra.png"));
        texEscudo = new Texture(Gdx.files.internal("shield.png"));
        texIman = new Texture(Gdx.files.internal("magnet.png"));
        texTormenta = new Texture(Gdx.files.internal("storm_pickup.png"));
        texTrueno = new Texture(Gdx.files.internal("trueno.png"));

        setLinear(texTarro, texGotaBuena, texGotaMala, texVidaExtra, texEscudo, texIman, texTormenta, texTrueno, imgFondo);

        // ----- SONIDOS -----
        sndHurt = Gdx.audio.newSound(Gdx.files.internal("hurt.mp3"));
        sndDrop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        sndVida = Gdx.audio.newSound(Gdx.files.internal("life.wav"));
        sndPowerup = Gdx.audio.newSound(Gdx.files.internal("powerup.wav"));
        sndTrueno = Gdx.audio.newSound(Gdx.files.internal("trueno.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        windMusic = Gdx.audio.newMusic(Gdx.files.internal("wind.mp3"));

        GameManager.getInstance().setWindMusic(windMusic);

        // ----- ENTIDADES -----
        tarro = new Tarro(texTarro, sndHurt);
        tarro.setEscudoTexture(texEscudo);

        lluvia = new Lluvia(
                texGotaBuena, texGotaMala, texVidaExtra,
                texEscudo, texIman, texTormenta,
                sndDrop, sndVida, sndPowerup, rainMusic
        );

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        GameManager.getInstance().resetJuego();
        tarro.crear();
        lluvia.crear();
    }

    private static void setLinear(Texture... textures) {
        for (Texture t : textures) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    @Override
    public void render(float delta) {

        // ---- INPUT QUE SIEMPRE FUNCIONA ----

        // ESC → Pausa / Reanudar
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            boolean estabaActiva = pausaScreen.isActivo();
            pausaScreen.toggle();

            GameManager gm = GameManager.getInstance();

            if (!estabaActiva) {
                // Entrando en pausa
                lluvia.pausar();              // detiene rain.mp3
                gm.getAudioBus().stopAll();   // detiene viento y otros Music del bus
            } else {
                // Saliendo de pausa
                lluvia.continuar();           // reanuda rain.mp3

                GameManager.EstadoJuego estado = gm.getEstadoActual();
                if (estado == GameManager.EstadoJuego.ETAPA_2 ||
                        estado == GameManager.EstadoJuego.ETAPA_3) {
                    gm.startWind();          // vuelve a encender viento si corresponde
                }
            }
        }

        // R → Reiniciar juego SIEMPRE
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        }

        // F3 → Toggle debug hitbox SIEMPRE
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            debugHitbox = !debugHitbox;
        }

        // ---- UPDATE SOLO SI NO ESTÁ EN PAUSA ----
        if (!pausaScreen.isActivo()) {
            GameManager.getInstance().update(delta);

            if (!tarro.estaHerido()) {
                tarro.actualizarMovimiento();
            }
            lluvia.actualizarMovimiento(tarro);
        }

        // GAME OVER (solo si no está en pausa)
        if (GameManager.getInstance().getVidas() <= 0 && !pausaScreen.isActivo()) {
            GameManager.getInstance().actualizarHighscore();
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        // ---- DIBUJO ----
        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);

        game.getBatch().begin();

        // Fondo
        game.getBatch().draw(imgFondo, 0, 0, 800, 480);

        // HUD
        fontHUD.draw(game.getBatch(), "HighScore: " + GameManager.getInstance().getHighscore(), 5, 475);
        fontHUD.draw(game.getBatch(), "Tormenta: " + GameManager.getInstance().getContadorTormenta() + " / 3", 600, 475);
        fontHUD.draw(game.getBatch(), "Gotas: " + GameManager.getInstance().getPuntos(), 5, 40);
        fontHUD.draw(game.getBatch(), "Vidas: " + GameManager.getInstance().getVidas(), 670, 40);

        // Entidades
        tarro.dibujar(game.getBatch());
        lluvia.actualizarDibujoLluvia(game.getBatch());

        // ---- TRUENO: disparo + parpadeo ----
        if (GameManager.getInstance().debeMostrarTrueno()) {
            sndTrueno.play();
            truenoActivo = true;
            truenoTimer = TRUENO_DURACION;
        }

        if (truenoActivo) {
            truenoTimer -= delta;
            if (truenoTimer <= 0f) {
                truenoActivo = false;
            } else {
                float intensidad = MathUtils.random(0.4f, 1f); // parpadeo tipo ruido
                game.getBatch().setColor(1f, 1f, 1f, intensidad);

                game.getBatch().draw(
                        texTrueno,
                        camera.viewportWidth / 2f - texTrueno.getWidth() / 2f,
                        camera.viewportHeight / 2f - texTrueno.getHeight() / 2f
                );

                game.getBatch().setColor(1f, 1f, 1f, 1f);
            }
        }

        // Overlay de pausa (encima de todo lo anterior)
        pausaScreen.render(game.getBatch(), camera);

        game.getBatch().end();

        // ---- DEBUG HITBOX (F3) ----
        if (debugHitbox) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            // Boca del tarro
            Polygon p = tarro.getPolyBoca();
            if (p != null) {
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.polygon(p.getTransformedVertices());
            }

            // Hitboxes de objetos cayendo
            for (ObjetoCayendo o : lluvia.getObjetos()) {
                if (o.usaPoligono()) {
                    shapeRenderer.setColor(1, 0, 0, 1);
                    shapeRenderer.polygon(o.getHitPolygonWorld().getTransformedVertices());
                } else {
                    shapeRenderer.setColor(0, 0, 1, 1);
                    Circle c = o.getHitCircle();
                    shapeRenderer.circle(c.x, c.y, c.radius);
                }
            }

            shapeRenderer.end();
        }
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void resize(int width, int height) {}

    @Override
    public void dispose() {
        pausaScreen.dispose();
        fontHUD.dispose();

        tarro.destruir();
        lluvia.destruir();

        texTarro.dispose();
        texGotaBuena.dispose();
        texGotaMala.dispose();
        texVidaExtra.dispose();
        texEscudo.dispose();
        texIman.dispose();
        texTormenta.dispose();
        texTrueno.dispose();
        imgFondo.dispose();

        sndHurt.dispose();
        sndDrop.dispose();
        sndVida.dispose();
        sndPowerup.dispose();
        sndTrueno.dispose();
        rainMusic.dispose();
        windMusic.dispose();

        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}