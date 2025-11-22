package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {

    final GameLluviaMenu game;
    private SpriteBatch batch;
    private OrthographicCamera camera;

    private Texture fondo;
    private Texture tarroLogo;

    private BitmapFont fontRecolecta;
    private BitmapFont fontGotas;
    private BitmapFont fontSubtitulo;

    private GlyphLayout layoutRecolecta;
    private GlyphLayout layoutGotas;
    private GlyphLayout layoutPresiona;

    private float tiempo = 0f;

    public MainMenuScreen(final GameLluviaMenu game) {
        this.game = game;
        this.batch = game.getBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        fondo = new Texture(Gdx.files.internal("fondo.png"));
        fondo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tarroLogo = new Texture(Gdx.files.internal("tarroLogo.png"));
        tarroLogo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Fuentes
        FreeTypeFontGenerator genMedium = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Medium.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter pMedium = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pMedium.size = 68;
        pMedium.minFilter = Texture.TextureFilter.Linear;
        pMedium.magFilter = Texture.TextureFilter.Linear;
        fontRecolecta = genMedium.generateFont(pMedium);
        genMedium.dispose();

        FreeTypeFontGenerator genBlack = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Black.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter pBlack = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pBlack.size = 68;
        pBlack.minFilter = Texture.TextureFilter.Linear;
        pBlack.magFilter = Texture.TextureFilter.Linear;
        fontGotas = genBlack.generateFont(pBlack);
        genBlack.dispose();

        FreeTypeFontGenerator genRegular = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter pReg = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pReg.size = 30;
        pReg.minFilter = Texture.TextureFilter.Linear;
        pReg.magFilter = Texture.TextureFilter.Linear;
        fontSubtitulo = genRegular.generateFont(pReg);
        genRegular.dispose();

        layoutRecolecta = new GlyphLayout(fontRecolecta, "RECOLECTA");
        layoutGotas = new GlyphLayout(fontGotas, "GOTAS");
        layoutPresiona = new GlyphLayout(fontSubtitulo, "PRESIONA PARA EMPEZAR");
    }

    @Override
    public void render(float delta) {
        tiempo += delta;

        ScreenUtils.clear(0, 0, 0.2f, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Fondo
        batch.draw(fondo, 0, 0, 800, 480);

        float cx = camera.viewportWidth / 2f;

        // Recolecta texto
        float y1 = camera.viewportHeight * 0.81f;
        float x1 = cx - layoutRecolecta.width / 1.7f;
        fontRecolecta.draw(batch, layoutRecolecta, x1, y1);

        // Gotas texto
        float y2 = camera.viewportHeight * 0.67f;
        float x2 = cx - layoutGotas.width / 15f;
        fontGotas.draw(batch, layoutGotas, x2, y2);

        // Logo de tarro
        float logoW = 130f;
        float logoH = 130f;

        float logoX = camera.viewportWidth * 0.63f;
        float logoYBase = camera.viewportHeight * 0.78f - logoH / 2f;

        batch.draw(tarroLogo, logoX, logoYBase, logoW, logoH);

        float ySub = camera.viewportHeight * 0.32f;
        float xSub = cx - layoutPresiona.width / 2f;

        float periodo = 1.0f;
        boolean mostrarTexto = ((int) (tiempo / (periodo / 2f)) % 2) == 0;

        if (mostrarTexto) {
            fontSubtitulo.draw(batch, layoutPresiona, xSub, ySub);
        }

        batch.end();

        // Entrar al juego
        if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        fondo.dispose();
        tarroLogo.dispose();
        fontRecolecta.dispose();
        fontGotas.dispose();
        fontSubtitulo.dispose();
    }
}
