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
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    private final GameLluviaMenu game;
    private OrthographicCamera camera;
    private BitmapFont font;
    private Tarro tarro;
    private Lluvia lluvia;

    private boolean debugHitbox = false;
    private ShapeRenderer shapeRenderer;

    private Texture texTarro, texGotaBuena, texGotaMala, texVidaExtra, texEscudo, texIman, texTormenta, texTrueno, imgFondo;
    private Sound sndHurt, sndDrop, sndVida, sndPowerup, sndTrueno;
    private Music rainMusic, windMusic;

    public GameScreen(final GameLluviaMenu game) {
        this.game = game;

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 28;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        this.font = gen.generateFont(p);
        gen.dispose();

        shapeRenderer = new ShapeRenderer();

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

        sndHurt = Gdx.audio.newSound(Gdx.files.internal("hurt.mp3"));
        sndDrop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        sndVida = Gdx.audio.newSound(Gdx.files.internal("life.wav"));
        sndPowerup = Gdx.audio.newSound(Gdx.files.internal("powerup.wav"));
        sndTrueno = Gdx.audio.newSound(Gdx.files.internal("trueno.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        windMusic = Gdx.audio.newMusic(Gdx.files.internal("wind.mp3"));

        GameManager.getInstance().setWindMusic(windMusic);

        tarro = new Tarro(texTarro, sndHurt);
        tarro.setEscudoTexture(texEscudo);

        lluvia = new Lluvia(texGotaBuena, texGotaMala, texVidaExtra,texEscudo, texIman, texTormenta,sndDrop, sndVida, sndPowerup, rainMusic);

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pause();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            debugHitbox = !debugHitbox;
        }

        GameManager.getInstance().update(delta);
        if (GameManager.getInstance().debeMostrarTrueno()) sndTrueno.play();

        if (!tarro.estaHerido()) tarro.actualizarMovimiento();
        lluvia.actualizarMovimiento(tarro);

        if (GameManager.getInstance().getVidas() <= 0) {
            GameManager.getInstance().actualizarHighscore();
            stopAllAudio();
            if (game.getBatch().isDrawing()) game.getBatch().end();
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);

        game.getBatch().begin();

        game.getBatch().draw(imgFondo, 0, 0, 800, 480);

        font.draw(game.getBatch(), "HighScore: " + GameManager.getInstance().getHighscore(), 5, 475);
        font.draw(game.getBatch(), "Tormenta: " + GameManager.getInstance().getContadorTormenta() + " / 3", 600, 475);
        font.draw(game.getBatch(), "Gotas: " + GameManager.getInstance().getPuntos(), 5, 40);
        font.draw(game.getBatch(), "Vidas: " + GameManager.getInstance().getVidas(), 670, 40);

        tarro.dibujar(game.getBatch());
        lluvia.actualizarDibujoLluvia(game.getBatch());

        if (GameManager.getInstance().estaEnPausaDeTransicion()) {
            game.getBatch().draw(
                    texTrueno,
                    camera.viewportWidth / 2f - texTrueno.getWidth() / 2f,
                    camera.viewportHeight / 2f - texTrueno.getHeight() / 2f
            );
        }
        game.getBatch().end();

        if (debugHitbox) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            Polygon p = tarro.getPolyBoca();
            if (p != null) {
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.polygon(p.getTransformedVertices());
            }

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

    @Override public void show()   { lluvia.continuar(); }
    @Override public void hide()   {}
    @Override public void pause() {
        lluvia.pausar();
        game.setScreen(new PausaScreen(game, this));
    }
    @Override public void resume() {}
    @Override public void resize(int width, int height) {}

    @Override
    public void dispose() {
        stopAllAudio();

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

        if (font != null) font.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

    private void stopAllAudio() {
        GameManager.getInstance().getAudioBus().stopAll();
    }
}
