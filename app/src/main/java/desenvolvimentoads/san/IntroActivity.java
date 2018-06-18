package desenvolvimentoads.san;

import android.*;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import agency.tango.materialintroscreen.MaterialIntroActivity;
import agency.tango.materialintroscreen.MessageButtonBehaviour;
import agency.tango.materialintroscreen.SlideFragmentBuilder;
import agency.tango.materialintroscreen.animations.IViewTranslation;

/**
 * Created by jeanf on 14/06/2018.
 */

public class IntroActivity extends MaterialIntroActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLastSlideAlphaExitTransition(true);

        getBackButtonTranslationWrapper()
                .setEnterTranslation(new IViewTranslation() {
                    @Override
                    public void translate(View view, @FloatRange(from = 0, to = 1.0) float percentage) {
                        view.setAlpha(percentage);
                    }
                });

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.third_slide_background)
                        .buttonsColor(R.color.third_slide_buttons)
                        .image(R.mipmap.ic_logo_san)
                        .title("Bem Vindo Ao SAN!")
                        .description("O SAN irá te acompanhar a partir de agora!\nFique atento aos alertas de alagamentos!")
                        .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.first_slide_background)
                .buttonsColor(R.color.first_slide_buttons)
                .image(R.drawable.status)
                .title("Atenção aos Status")
                .description("O seu avatar indicará se há um alagamento próximo!\nVermelho fique atento!")
                .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.custom3_slide_background)
                .buttonsColor(R.color.custom3_slide_buttons)
                .image(R.drawable.ferramentas)
                .title("Ferramentas de Zoom")
                .description("Utilize as ferramentas do Maps!\nElas facilitam a navegação!")
                .build());

        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.custom_slide_background)
                        .buttonsColor(R.color.custom_slide_buttons)
                        .image(R.drawable.cadastrar)
                        .title("Cadastrando Marcadores")
                        .description("Veja como é simples cadastrar um marcador!\nBasta selecionar o icone ou pressionar o dedo sobre o local! ")
                        .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("Se continuar com alguma dúvida o tutorial estará sempre disponível!");
                    }
                }, "Mais Dicas!"));

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.second_slide_background)
                .buttonsColor(R.color.second_slide_buttons)
                .image(R.drawable.validar)
                .title("Validando Marcadores")
                .description("Para validar marcadores de outros usuários.\nÉ só clicar com dedo sobre ele!")
                .build(),
                new MessageButtonBehaviour(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMessage("A validação adiciona ou subtrai o tempo dos marcadores!");
                    }
                }, "Mais Dicas!"));


        addSlide(new SlideFragmentBuilder()
                        .backgroundColor(R.color.custom2_slide_background)
                        .buttonsColor(R.color.custom2_slide_buttons)
                        .image(R.drawable.denunciar)
                        .title("Denunciando Marcadores")
                        .description("Denunciar marcadores para sinalizar um falso alerta.\nÉ só clicar sobre Denunciar e depois no marcador!")
                        .build());

        addSlide(new SlideFragmentBuilder()
                .backgroundColor(R.color.fourth_slide_background)
                .buttonsColor(R.color.fourth_slide_buttons)
                .image(R.drawable.historico)
                .title("Histórico de Marcadores")
                .description("Veja os históricos de seus marcadores!")
                .build());




        //Exemplo de Termos de Uso
        //addSlide(new CustomSlide());

    }

    @Override
    public void onFinish() {
        super.onFinish();
        Toast.makeText(this, "Bem vindo ao SAN :)", Toast.LENGTH_SHORT).show();
    }
}
