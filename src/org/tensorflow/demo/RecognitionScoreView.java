/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import org.tensorflow.demo.Classifier.Recognition;

import java.util.List;

public class RecognitionScoreView extends View implements ResultsView {
  private static final float TEXT_SIZE_DIP = 24;
  private List<Recognition> results;
  private final float textSizePx;
  private final Paint fgPaint;
  private final Paint bgPaint;

  public RecognitionScoreView(final Context context, final AttributeSet set) {
    super(context, set);

    textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    fgPaint = new Paint();
    fgPaint.setTextSize(textSizePx);

    bgPaint = new Paint();
    bgPaint.setColor(0xcc4285f4);
  }

  @Override
  public void setResults(final List<Recognition> results) {
    this.results = results;
    postInvalidate();
  }

  @Override
  public void onDraw(final Canvas canvas) {
    final int x = 10;
    int y = (int) (fgPaint.getTextSize() * 1.5f);

    canvas.drawPaint(bgPaint);

    if (results != null) {
      for (final Recognition recog : results) {
        String objeto=recog.getTitle();
        objeto=translate(objeto);

        if(recog.getConfidence()>.20 || recog.getTitle().equalsIgnoreCase("street sign")) {
          ClassifierActivity.PalabraActual = objeto;
          ClassifierActivity.Palabraconfidense = recog.getConfidence();
        }
        if(recog.getTitle().equalsIgnoreCase("geyser")||recog.getTitle().equalsIgnoreCase("nematode")
                ||recog.getTitle().equalsIgnoreCase("great white shark")||
                recog.getTitle().equalsIgnoreCase("ala")
                ||recog.getTitle().equalsIgnoreCase("great white shark")){
          continue;
        }

        canvas.drawText( objeto+ ": " + recog.getConfidence(), x, y, fgPaint);
        Log.i("TAMANO exacto x", String.valueOf(x));
        Log.i("TAMANO exacto y", String.valueOf(y));
        y += fgPaint.getTextSize() * 1.5f;
      }
    }
  }

  public static String translate(String objeto) {
    String recogmine = objeto;
    recogmine=recogmine.replace("minibus", "autobús");
    recogmine=recogmine.replace("bathub", "bañera");
    recogmine=recogmine.replace("electric fan", " ventilador");
    recogmine=recogmine.replace("loafer", "zapatos");
    recogmine=recogmine.replace("packet", "paquete");
    recogmine=recogmine.replace("scoreboard", "marcador");
    recogmine=recogmine.replace("book jacket", "portada de libro");
    recogmine=recogmine.replace("racer", "automóvil");
    recogmine=recogmine.replace("mountain bike", "bicicleta");
    recogmine=recogmine.replace("beach bagon", "bagoneta");
    recogmine=recogmine.replace("street sign", "señalizacion");
    recogmine=recogmine.replace("piggy bank", " alcancia");
    recogmine=recogmine.replace("analogic clock", " reloj analógico");
    recogmine=recogmine.replace("remote control", " control remoto");
    recogmine=recogmine.replace("desk", " escritorio");
    recogmine=recogmine.replace("printer", "impresora");
    recogmine=recogmine.replace("cellular telephone", "Celular");
    recogmine=recogmine.replace("plastic bag", "bolsa de plastico");
    recogmine=recogmine.replace("sunscreen", "bloqueador solar");
    recogmine=recogmine.replace("screen", "pantalla");
    recogmine=recogmine.replace("pinwheel", "molino");
    recogmine=recogmine.replace("scale", "escala");
    recogmine=recogmine.replace("hamper", "cesto");
    recogmine=recogmine.replace("pot", "lata");
    recogmine=recogmine.replace("medicine chest", "botiquin");
    recogmine=recogmine.replace("paper toilet", "papel de baño");
    recogmine=recogmine.replace("water bottle", "botella de agua");
    recogmine=recogmine.replace("wine bottle", "botella de vino");
    recogmine=recogmine.replace("quill", "pluma");
    recogmine=recogmine.replace("nail", "clavo");
    recogmine=recogmine.replace("rule", "regla");
    recogmine=recogmine.replace("pill bottle", "medicina");
    recogmine=recogmine.replace("notebook", "libreta");
    recogmine=recogmine.replace("library", "biblioteca");
    recogmine=recogmine.replace("washbasin", "lavabo");
    recogmine=recogmine.replace("spotlight", "lampara");
    recogmine=recogmine.replace("toilet seat", "asiento de baño");
    recogmine=recogmine.replace("bathub", "bañera");
    recogmine=recogmine.replace("refrigerator", "refrigerador");
    recogmine=recogmine.replace("lotion", "locion");
    recogmine=recogmine.replace("water jug", "jarra de agua");
    recogmine=recogmine.replace("electric guitar", "guitarra");
    recogmine=recogmine.replace("soup bowl", "recipiente");
    recogmine=recogmine.replace("coffe mug", "taza de café");
    recogmine=recogmine.replace("mixing bowl", "recipiente grande");
    recogmine=recogmine.replace("bucket", "cubeta");
    recogmine=recogmine.replace("knee pad", "rodillera");
    recogmine=recogmine.replace("candle", "vela");
    recogmine=recogmine.replace("lipstick", "labial");
    recogmine=recogmine.replace("safe", "caja fuerte");
    recogmine=recogmine.replace("paper towel", "toalla de papel");

    recogmine=recogmine.replace("french loaf", "pan");
    recogmine=recogmine.replace("fountain pen", "pluma");
    //recogmine=recogmine.replace("ant", "hormiga");
    recogmine=recogmine.replace("envelope", "cartel");//--------------------
    recogmine=recogmine.replace("dishwater", "lavabajillas");
    recogmine=recogmine.replace("shopping car", "Carro de compras");
    recogmine=recogmine.replace("mesuare cup", "taza de medición");
    recogmine=recogmine.replace("sandal", "chancleta");
    recogmine=recogmine.replace("hair spray", "spray");
    recogmine=recogmine.replace("conch", "cacerola");
    recogmine=recogmine.replace("cleaver", "cuchillo");
    recogmine=recogmine.replace("crate", "caja");
    recogmine=recogmine.replace("teddy bear", "osito de peluche");
    recogmine=recogmine.replace("toy store", "juguetes");
    recogmine=recogmine.replace("toilet tissue", "papel de baño");
    recogmine=recogmine.replace("vase", "florero");
    recogmine=recogmine.replace("binder", "carpeta");
    recogmine=recogmine.replace("hammerhead", "martillo");
    recogmine=recogmine.replace("lighter", "encendedor");
    recogmine=recogmine.replace("digital clock", "reloj digital");
    recogmine=recogmine.replace("puck", "disco");
    recogmine=recogmine.replace("mousetrap", "ratonera");
    recogmine=recogmine.replace("wool", "lana");
    recogmine=recogmine.replace("backpack", "mochila");
    recogmine=recogmine.replace("computer keyboard", "teclado");
    recogmine=recogmine.replace("hand-held computer", "laptop");
    recogmine=recogmine.replace("space bar", "barra de espacio");
    recogmine=recogmine.replace("bath towel", "toalla");
    recogmine=recogmine.replace("sleeping bag", "cobertor");
    recogmine=recogmine.replace("holster", "funda");

    recogmine=recogmine.replace("humbrella", "sombrilla");
    recogmine=recogmine.replace("schooner", "tarro");
    recogmine=recogmine.replace("soccer ball", "balón");
    recogmine=recogmine.replace("balloon", "globo");
    recogmine=recogmine.replace("passenger car", "automóvil");
    recogmine=recogmine.replace("barrow", "carretilla");
    recogmine=recogmine.replace("shopping cart", "carro de compras");
    recogmine=recogmine.replace("ambulance", "ambulancia");
    recogmine=recogmine.replace("sports car", "Automóvil");
    recogmine=recogmine.replace("police van", "Automovil de policias");
    recogmine=recogmine.replace("streetcar", "automóvil");
    recogmine=recogmine.replace("four-poster", "mueble");
    recogmine=recogmine.replace("bookcase", "librero");
    recogmine=recogmine.replace("table lamp", "lampara de mesa");
    recogmine=recogmine.replace("file", "carpeta");
    recogmine=recogmine.replace("rocking chair", "mesedora");
    recogmine=recogmine.replace("folding chair", "silla");
    recogmine=recogmine.replace("studio couch", "sofá");
    recogmine=recogmine.replace("pool table", "mesa de billar");
    recogmine=recogmine.replace("dining table", "mesa de cenar");
    recogmine=recogmine.replace("lemon", "limon");
    recogmine=recogmine.replace("orange", "naranja");
    recogmine=recogmine.replace("banana", "platano");
    recogmine=recogmine.replace("pinapple", "piña");
    recogmine=recogmine.replace("jackfruit", "fruta");
    recogmine=recogmine.replace("custard apple", "manzana");
    recogmine=recogmine.replace("hip", "cadera");
    recogmine=recogmine.replace("ear", "oido");
    recogmine=recogmine.replace("corn", "maiz");
    recogmine=recogmine.replace("steel drum", "bateria");
    recogmine=recogmine.replace("drum", "bateria");
    recogmine=recogmine.replace("valley", "valle");
    recogmine=recogmine.replace("volcano", "volcan");
    recogmine=recogmine.replace("sandbar", "arena");
    recogmine=recogmine.replace("seashore", "costa");
    recogmine=recogmine.replace("hatchet", "hacha");
    recogmine=recogmine.replace("cleaver", "cuchillo");
    recogmine=recogmine.replace("hammer", "martillo");
    recogmine=recogmine.replace("can opener", "abre latas");
    recogmine=recogmine.replace("kite", "cometa");
    recogmine=recogmine.replace("wing", "ala");
    recogmine=recogmine.replace("tree frog", "rama de arbol");
    recogmine=recogmine.replace("paintbrush", "brocha");
    recogmine=recogmine.replace("loudspeaker", "bocina");
    recogmine=recogmine.replace("microphone", "microfono");
    recogmine=recogmine.replace("stove", "estufa");
    recogmine=recogmine.replace("hourglass", "reloj de arena");
    recogmine=recogmine.replace("digital watch", "reloj digital");
    recogmine=recogmine.replace("parking meter", "parquimetro");
    recogmine=recogmine.replace("projector", "proyector");
    recogmine=recogmine.replace("loupe", "lupa");
    recogmine=recogmine.replace("radio telescope", "telescopio");
    recogmine=recogmine.replace("crane", "grua");
    recogmine=recogmine.replace("cash machine", "cajero");
    recogmine=recogmine.replace("vending machine", "maquina dispensadora");
    recogmine=recogmine.replace("sewing machine", "máquina de coser");
    recogmine=recogmine.replace("joystick", "funda");
    recogmine=recogmine.replace("switch", "interrumptor");
    recogmine=recogmine.replace("car mirror", "espejo de carro");
    recogmine=recogmine.replace("remote control", "control remoto");
    recogmine=recogmine.replace("combination lock", "candado");
    recogmine=recogmine.replace("knot", "lazo");
    recogmine=recogmine.replace("padlock", "candado");
    recogmine=recogmine.replace("seat belt", "funda");
    recogmine=recogmine.replace("scorpion", "escorpion");
    recogmine=recogmine.replace("iron", "plancha");
    recogmine=recogmine.replace("microwave", "microondas");
    recogmine=recogmine.replace("cinema", "cine");
    recogmine=recogmine.replace("home theater", "teatro en casa");
    recogmine=recogmine.replace("prison", "prision");
    recogmine=recogmine.replace("barbershop", "barberia");
    recogmine=recogmine.replace("bookshop", "libreria");
    recogmine=recogmine.replace("shoe shop", "zapateria");
    recogmine=recogmine.replace("fur coat", "abrigo");
    recogmine=recogmine.replace("running shoe", "tenis para correr");
    recogmine=recogmine.replace("beer bottle", "botella de cerveza");
    recogmine=recogmine.replace("bookshop", "libreria");
    recogmine=recogmine.replace("bookshop", "libreria");
    recogmine=recogmine.replace("dummy", "maniquí");
    return recogmine;
  }
}
