package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import jdk.nashorn.internal.runtime.Debug;

public class MyGdxGame extends ApplicationAdapter {

	//iniciando todas as variaveis

	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture[] moedas;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Circle circuloMoeda;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Rectangle retanguloTopo;
	private Rectangle retanguloFundo;

	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float variacaoMoeda = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float posicaoMoedaHorizontal;
	private float posicaoMoedaVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;
	Sound somMoeda;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;



	@Override
	public void create () {

		inicializarTexturas();
		inicializaObjetos();
	}

	@Override
	//renderizar o jogo para o usuario ver
	//
	public void render () {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();


	}


	private void inicializarTexturas(){

		//gerenciando texturas

		passaros = new Texture[3];
		passaros[0] = new Texture("Kirby1 (1).png");
		passaros[1] = new Texture("Kirby2 (1).png");
		passaros[2] = new Texture("Kirby2 (1).png");

		moedas = new Texture[2];
		moedas[0] = new Texture("gold_coin (1).png");
		moedas[1] = new Texture("silver_coin (1).png");

		fundo = new Texture("fundoAtt.png");
		canoBaixo = new Texture("PilarG.png");
		canoTopo = new Texture("PilarTopo.png");
		gameOver = new Texture("game_over.png");

	}

	private void inicializaObjetos(){
		//aplicando tamanho dos assets, colisores em forma geometrica, camera, fontes, viewport e atrelando valores a variaveis
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		posicaoMoedaHorizontal = larguraDispositivo+100;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		circuloMoeda = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		retanguloTopo = new Rectangle();
		retanguloFundo = new Rectangle();

		somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav") );
		somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav") );
		somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav") );
		somMoeda = Gdx.audio.newSound( Gdx.files.internal("coin_sound.wav") );


		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}

	private void verificarEstadoJogo() {

		//a força pra cima é aplicada no passaro caso toquem na tela.
		//Caso o jogo esteja ativo, os canos e moedas se movem até o player

		boolean toqueTela = Gdx.input.justTouched();
		if(estadoJogo == 0) {
			if(toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}

		}else if(estadoJogo ==1) {
			if(toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if( posicaoCanoHorizontal < -canoTopo.getWidth() ){
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			if( posicaoInicialVerticalPassaro > 0 || toqueTela )
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			posicaoMoedaHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if( posicaoMoedaHorizontal < -canoTopo.getWidth() ){
				posicaoMoedaHorizontal = larguraDispositivo+100;
				posicaoMoedaVertical = random.nextInt((int) alturaDispositivo);
				variacaoMoeda++;
			}
			//a
			if(pontos>=1){
				posicaoMoedaHorizontal -= Gdx.graphics.getDeltaTime() * 200;
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			}
			if(pontos>=5){
				posicaoMoedaHorizontal -= Gdx.graphics.getDeltaTime() * 200;
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			}
			if(pontos>=15){
				posicaoMoedaHorizontal -= Gdx.graphics.getDeltaTime() * 200;
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			}
			gravidade++;
		}else if(estadoJogo ==2) {
			if( pontos > pontuacaoMaxima ) {
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}

			if(toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoMoedaHorizontal = larguraDispositivo+100;
				posicaoMoedaVertical = random.nextInt((int) alturaDispositivo);
			}
		}
	}


	public void detectarColisoes() {

		//aplicando colisores

		circuloPassaro.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
				passaros[0].getWidth() / 2
		);


		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		circuloMoeda.set(
				posicaoMoedaHorizontal + moedas[0].getWidth() / 2,
				alturaDispositivo/2 + posicaoMoedaVertical + moedas[0].getHeight() / 2,
				moedas[0].getWidth() / 2
		);
		retanguloTopo.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2, alturaDispositivo,
				passaros[0].getWidth() / 2, 10
		);
		retanguloFundo.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2, 0,
				passaros[0].getWidth() / 2, 10
		);

		//Checando se o passaro bateu no cano de cima ou de baixo ou nas moedas de Prata ou Ouro.
		//Toca um som caso encoste no cano e leva ao game over e aumenta a pontuacao caso toque na moeda

		boolean pegouMoeda = Intersector.overlaps(circuloPassaro, circuloMoeda);
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuTopo = Intersector.overlaps(circuloPassaro, retanguloTopo);
		boolean colidiuFundo = Intersector.overlaps(circuloPassaro, retanguloFundo);


		if (colidiuCanoCima || colidiuCanoBaixo || colidiuTopo || colidiuFundo) {
			if(estadoJogo ==1) {
				somColisao.play();
				estadoJogo = 2;
			}
		}

		if(pegouMoeda && variacaoMoeda==0){
			pontos+=10;
			posicaoMoedaVertical = alturaDispositivo;
		}
		if(pegouMoeda && variacaoMoeda==1) {
			pontos += 5;
			posicaoMoedaVertical = alturaDispositivo;
		}

	}


	private void desenharTexturas() {
		//desenhando texturas do passaro, moedas e canos a partir dos assets

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo,0,0,larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao] ,
				50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(moedas[(int) variacaoMoeda], posicaoMoedaHorizontal,
				alturaDispositivo / 2 + posicaoMoedaVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos),larguraDispositivo / 2,
				alturaDispositivo - 110);

		//desenhar tela de game over
		if (estadoJogo == 2) {
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2,
					alturaDispositivo / 2);
			textoReiniciar.draw(batch,
					"Toque para reiniciar!", larguraDispositivo / 2 - 140,
					alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch,
					"Seu record é: " + pontuacaoMaxima + " pontos",
					larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		if(variacaoMoeda>1)
			variacaoMoeda=0;

		batch.end();


	}


	public void validarPontos() {


		//verifica se o passaro passou pelos canos ou pegou as moedas e aumenta a pontuacao

		if(posicaoCanoHorizontal < 50 - passaros[0].getWidth()) {
			if(!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3)
			variacao = 0;
	}


	@Override
	//alterando o tamanho do viewPort
	public void resize(int width, int height) {
		viewport.update(width, height);
	}



	@Override
	public void dispose () {

	}
}





