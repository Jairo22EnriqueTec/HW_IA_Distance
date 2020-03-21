/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.helloworld.IA;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.helloworld.IA.OverlayView.DrawCallback;
import org.helloworld.IA.env.BorderedText;
import org.helloworld.IA.env.ImageUtils;
import org.helloworld.IA.env.Logger;
import org.helloworld.IA.tracking.MultiBoxTracker;
import org.helloworld.IA.R; // Explicit import needed for internal Google builds.

import static org.helloworld.IA.VariablesYDatos.coeficientes_ecuacion_distancia;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  // Configuration values for the prepackaged multibox model.
  private static final int MB_INPUT_SIZE = 224;
  private static final int MB_IMAGE_MEAN = 128;
  private static final float MB_IMAGE_STD = 128;
  private static final String MB_INPUT_NAME = "ResizeBilinear";
  private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
  private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
  private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
  private static final String MB_LOCATION_FILE =
      "file:///android_asset/multibox_location_priors.txt";

  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final String TF_OD_API_MODEL_FILE =
      "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

  // Configuration values for tiny-yolo-voc. Note that the graph is not included with TensorFlow and
  // must be manually placed in the assets/ directory by the user.
  // Graphs and models downloaded from http://pjreddie.com/darknet/yolo/ may be converted e.g. via
  // DarkFlow (https://github.com/thtrieu/darkflow). Sample command:
  // ./flow --model cfg/tiny-yolo-voc.cfg --load bin/tiny-yolo-voc.weights --savepb --verbalise
  private static final String YOLO_MODEL_FILE = "file:///android_asset/graph-tiny-yolo-voc.pb";
  private static final int YOLO_INPUT_SIZE = 416;
  private static final String YOLO_INPUT_NAME = "input";
  private static final String YOLO_OUTPUT_NAMES = "output";
  private static final int YOLO_BLOCK_SIZE = 32;
  private static double Distance=0;
  private static String Distance_String="";

  public TextToSpeech toSpeech;
  boolean SoloUno = true;
  //public static String PalabraActual="";
  public static double Palabraconfidense=0.0;
  String PalabraAnterior="";
  int result;
  public static ArrayList<String> LecturaObjetos = new ArrayList<>();
  public static ArrayList<String> DistanciaObjetos = new ArrayList<>();
  public static ArrayList<String> ColorLecturaObjetos = new ArrayList<>();
  public static boolean MandarMas=true;
  public static boolean PuedoEliminar=true;
   String Width_actual="";
   double Height_actual=0.0;
  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
  // or YOLO.
  private enum DetectorMode {
    TF_OD_API, MULTIBOX, YOLO;
  }
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;

  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
  private static final float MINIMUM_CONFIDENCE_YOLO = 0.25f;

  private static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.YOLO;

  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;

  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private byte[] luminanceCopy;

  private BorderedText borderedText;
  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;
    if (MODE == DetectorMode.YOLO) {
      detector =
          TensorFlowYoloDetector.create(
              getAssets(),
              YOLO_MODEL_FILE,
              YOLO_INPUT_SIZE,
              YOLO_INPUT_NAME,
              YOLO_OUTPUT_NAMES,
              YOLO_BLOCK_SIZE);
      cropSize = YOLO_INPUT_SIZE;
    } else if (MODE == DetectorMode.MULTIBOX) {
      detector =
          TensorFlowMultiBoxDetector.create(
              getAssets(),
              MB_MODEL_FILE,
              MB_LOCATION_FILE,
              MB_IMAGE_MEAN,
              MB_IMAGE_STD,
              MB_INPUT_NAME,
              MB_OUTPUT_LOCATIONS_NAME,
              MB_OUTPUT_SCORES_NAME);
      cropSize = MB_INPUT_SIZE;
    } else {
      try {
        detector = TensorFlowObjectDetectionAPIModel.create(
            getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        cropSize = TF_OD_API_INPUT_SIZE;
      } catch (final IOException e) {
        LOGGER.e("Exception initializing classifier!", e);
        Toast toast =
            Toast.makeText(
                getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
        toast.show();
        finish();
      }
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();
    Log.i("TAMANO este", String.valueOf(previewWidth));
    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            if (!isDebug()) {
              return;
            }
            final Bitmap copy = cropCopyBitmap;
            if (copy == null) {
              return;
            }

            final int backgroundColor = Color.argb(100, 0, 0, 0);
            canvas.drawColor(backgroundColor);

            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                canvas.getWidth() - copy.getWidth() * scaleFactor,
                canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (detector != null) {
              final String statString = detector.getStatString();
              final String[] statLines = statString.split("\n");
              for (final String line : statLines) {
                lines.add(line);
              }
            }
            lines.add("");

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
          }
        });
  }

  OverlayView trackingOverlay;

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    byte[] originalLuminance = getLuminance();
    tracker.onFrame(
        previewWidth,
        previewHeight,
        getLuminanceStride(),
        sensorOrientation,
        originalLuminance,
        timestamp);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");


    Log.i("Tamano este 2","processs men detector");//-------------------------------
    if(SoloUno){
      SacarPorVoz();
      SoloUno=false;
    }


    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
              case MULTIBOX:
                minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                break;
              case YOLO:
                minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
                break;
            }

            final List<Classifier.Recognition> mappedRecognitions =
                new LinkedList<Classifier.Recognition>();

            for (final Classifier.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {
                canvas.drawRect(location, paint);
                Log.i("Trabajar ex Dectector ", String.valueOf(location.width()));
                //AQUI ESTA TODO LO QUE NECESITAS!!!!!!!!!!!!!!
                //location.width()
                //location.height()
                Log.i("Trabajar ex", String.valueOf(location.height()));
                Log.i("Trabajar ex", String.valueOf(result.getTitle()));
                if(CameraActivity.Introducir_datos){
                    CameraActivity.Data_Set+="\n["+RecognitionScoreView.translate(result.getTitle().toString())+", "+String.valueOf(location.width())+", "
                            +String.valueOf(location.height())+", " +String.valueOf(result.getConfidence())+"]";
                }
                Width_actual=String.valueOf(location.width());
                cropToFrameTransform.mapRect(location);
                result.setLocation(location);
                mappedRecognitions.add(result);
              }
            }
            tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
            trackingOverlay.postInvalidate();

            requestRender();
            computingDetection = false;
          }
        });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onSetDebug(final boolean debug) {
    detector.enableStatLogging(debug);
  }

  //----------
  private void SacarPorVoz() {
    InitoSpeech();
    //----------------------
    final Handler handler = new Handler();
    final Timer timer = new Timer();
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        handler.post(new Runnable() {
          public void run() {
            try {
                String frase="veo ";


              if(LecturaObjetos.size()!=0) {
                  Log.i("DISTANCIA: ", "pal " + LecturaObjetos.get(0));
                  Log.i("DISTANCIA: ", "Width " + String.valueOf(Width_actual));
                  double Parametros[] = Encontrar_ecuacion_objeto(LecturaObjetos.get(0));
                  Distance = ((Parametros[0] / (Parametros[1] + Double.parseDouble(Width_actual))) + Parametros[2]) / Parametros[3];
                  //ecuacion de la forma p1/(q1+x)

                      if (Distance < 1) {
                          Distance *= 100;
                          Distance_String = String.format("%.0f", Distance) + " centimetros";
                          Log.i("DISTANCIA ", "Estimada: " + Distance_String);

                      } else {
                          Distance_String = String.format("%.1f", Distance) + " metros";
                          Log.i("DISTANCIA ", "Estimada: " + Distance_String);
                      }
                  }

              LecturaObjetos = ContabilizarObjetos(LecturaObjetos);
              MandarMas=false;
              PuedoEliminar=false;
              for (int i = 0;i<LecturaObjetos.size();i++) {
                if(LecturaObjetos.size()>1&&i==LecturaObjetos.size()-1){
                  frase="y veo ";
                }

                if(Distance!=0 && !Double.isNaN(Distance)) {
                  Hablar(frase + LecturaObjetos.get(i) + " a " + Distance_String);
                Distance=0;
                }else{
                  Hablar(frase + LecturaObjetos.get(i));
                }
                //Log.i("DISTANCIA: ","PArametro 0 "+String.valueOf(Parametros[0]));
               // Log.i("DISTANCIA: ",String.valueOf(Distance));


                //HEY HEY HEY!!!!!!!!!!!!!!!!!!!!!!
                //AQUI ESTÁ, DESCOMENTA ESTÁ LINEA PARA QUE HABLE
                while (toSpeech.isSpeaking()){}
              }
              MandarMas=true;
              PuedoEliminar=true;
              //Log.i("lecturap: ","Ejecutado 2.\nTamaño de la lista: "+LecturaObjetos.size()+"\nEstado de eliminar:"+PuedoEliminar
               //             +"\nEstado de Mandar mas:"+MandarMas+"\nancho: "+previewWidth);
            } catch (Exception e) {
              Log.i("lecturap", e.getMessage());
              MandarMas=true;
            }
          }
        });
      }
    };
    //Cambia de fragments cada tiempo
    timer.schedule(task, 0, 1009);

    final Handler handler2 = new Handler();
    Timer timer2 = new Timer();
    TimerTask task2 = new TimerTask() {
      @Override
      public void run() {
        handler2.post(new Runnable() {
          public void run() {
            try {
              if(PuedoEliminar){
              LecturaObjetos.clear();
              }
            } catch (Exception e) {
              Log.e("error", e.getMessage());
              Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
          }
        });
      }
    };
    //Cambia de fragments cada tiempo
    timer2.schedule(task2, 0, 479);
              //-----------------------
  }

  private ArrayList<String> ContabilizarColorObjetos(ArrayList<String> colorLecturaObjetos) {
    ArrayList<String> aux = new ArrayList<>();
    int [] ObjetosAContarrelacion= new int[COLORS.length];
    for(int i=0;i<colorLecturaObjetos.size();i++){
      for(int j=0;j<COLORS.length;j++) {
        if (colorLecturaObjetos.get(i).equalsIgnoreCase(String.valueOf(COLORS[j]))) {
          ObjetosAContarrelacion[j]++;
        }
      }
    }
    for(int i=0;i<ObjetosAContarrelacion.length;i++){
      if(ObjetosAContarrelacion[i]!=0) {
          aux.add(String.valueOf(COLORS[i]));
      }
    }

    return aux;
  }
  private static final int[] COLORS = {
          Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.WHITE,
          Color.parseColor("#55FF55"), Color.parseColor("#FFA500"), Color.parseColor("#FF8888"),
          Color.parseColor("#AAAAFF"), Color.parseColor("#FFFFAA"), Color.parseColor("#55AAAA"),
          Color.parseColor("#AA33AA"), Color.parseColor("#0D0068")
  };
  private ArrayList<String> ContabilizarObjetos(ArrayList<String> lecturaObjetos) {
    ArrayList<String> aux = new ArrayList<>();
    String [] ObjetosAContar={"persona","carro","celular","libro","laptop","taza","libro","control remoto","mesa","botella",
            "cuchara","Taza de baño","television","lavabo","silla","reloj","cepillo de dientes","teclado","cama","pastel",
            "dona","pizza","hot dog","sandwich","manzana","banana","copa","cuchillo","corbata","oso de peluche","tijeras",
            "mochila","perro","gato","señalizacion","semaforo","bus","motocicleta","tazón"};
      int [] ObjetosAContarrelacion= new int[ObjetosAContar.length];
      for(int i=0;i<lecturaObjetos.size();i++){
        for(int j=0;j<ObjetosAContar.length;j++) {
          if (lecturaObjetos.get(i).equalsIgnoreCase(ObjetosAContar[j])) {
              ObjetosAContarrelacion[j]++;
          }
        }
      }
    for(int i=0;i<ObjetosAContarrelacion.length;i++){
        if(ObjetosAContarrelacion[i]!=0) {
         if(ObjetosAContarrelacion[i]==1){
           String arti ="un";
           if(ObjetosAContar[i].endsWith("a")){
             arti="una";
           }
           aux.add(arti+ObjetosAContar[i]);
         }else{
           aux.add(ObjetosAContarrelacion[i]+" "+ObjetosAContar[i]+"s");
         }
        }
    }
    for (int i = 0;i<aux.size();i++){
                 Log.i("lectura: ", aux.get(i));
           }
    return aux;
  }

  private void Hablar(String query) {
    if(CameraActivity.Introducir_datos==false) {
      toSpeech.speak(query, TextToSpeech.QUEUE_FLUSH, null);
    }
  }

  private double[] Encontrar_ecuacion_objeto(String objeto){
    double Ecuacion[]=new double[4];
//[Numerador, denominador, factor de conversion]
    for (int i=0;i<coeficientes_ecuacion_distancia.length;i++){
      if(coeficientes_ecuacion_distancia[i][0].equalsIgnoreCase(objeto)){
        Ecuacion[0]=Double.parseDouble((coeficientes_ecuacion_distancia[i][1]));
        Ecuacion[1]=Double.parseDouble(coeficientes_ecuacion_distancia[i][2]);
        Ecuacion[2]=Double.parseDouble(coeficientes_ecuacion_distancia[i][3]);
        Ecuacion[3]=Double.parseDouble(coeficientes_ecuacion_distancia[i][4]);
        break;
      }
    }
    return Ecuacion;
  }

  private void InitoSpeech(){
    try {
      toSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
          if (status == TextToSpeech.SUCCESS) {
            result = toSpeech.setLanguage(Locale.getDefault());
          } else {
            Toast.makeText(getApplicationContext(), "Feature not supported in your device", Toast.LENGTH_SHORT).show();
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }
}
