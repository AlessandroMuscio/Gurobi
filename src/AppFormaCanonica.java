/*Consegna***********************************************************************************************************************************
 * Il responsabile marketing di una nota azienda di Matriciopoli è incaricato dell’acquisto di spazi pubblicitari su M emittenti televisive. *
 * La giornata televisiva è suddivisa in j = 1, 2, ..., K fasce orarie in cui è possibile acquistare minuti di pubblicità. In particolare,   *
 * in ciascuna fascia j dell’emittente i, i = 1, ..., M : l’azienda può finalizzare l’acquisto a un costo pari a Cij euro/minuto             *
 * garantendosi una copertura di Pij spettatori/minuto; non può acquistare più di τij minuti, per questioni di libera concorrenza. La        *
 * direzione stabilisce di voler investire non più di βi euro su una qualsivoglia emittente i, ma almeno Ω% del bilancio totale su ogni      *
 * fascia oraria j. Sapendo che l’azienda desidera ottenere una copertura giornaliera complessiva di almeno S spettatori, si aiuti il        *
 * responsabile a decidere la programmazione ideale in modo da minimizzare lo scarto tra le persone raggiunte nelle prime K/2 fasce orarie e *
 * le persone raggiunte nelle restanti.                                                                                                      *
 *********************************************************************************************************************************************/

import gurobi.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Classe principale contenete l'intero modello, non che il suo svolgimento
 */
public class AppFormaCanonica {
  // Variabili del problema
  /**
   * Variabile che rappresenta il numero di emittenti televisive
   */
  private static final int M = 10;
  /**
   * Variabile che rappresenta il numero di fasce orarie in una giornata
   */
  private static final int K = 8;
  /**
   * Variabile che rappresenta il minimo numero di spettatori giornalieri da
   * raggiungere
   */
  private static final int S = 82110;
  /**
   * Variabile che rappresenta la percentuale minima di bilancio da spendere per
   * emittente sul bilancio totale
   */
  private static final double omega = 0.02;
  /**
   * Variabile che rappresenta il massimo bilancio spendibile per emittente
   */
  private static final int[] beta = { 3373, 3281, 3274, 3410, 2691, 2613, 3354, 2912, 3203, 2616 };
  /**
   * Variabile che rappresenta il numero massimo di minuti acquistabili da ogni
   * emittente per fascia oraria
   */
  private static final int[][] tau = { { 2, 2, 2, 1, 2, 2, 1, 3 },
          { 2, 2, 1, 2, 2, 2, 2, 3 },
          { 2, 2, 2, 2, 3, 1, 2, 1 },
          { 2, 2, 2, 1, 1, 3, 2, 1 },
          { 2, 1, 3, 2, 3, 2, 2, 1 },
          { 2, 1, 2, 2, 2, 3, 3, 2 },
          { 3, 3, 1, 1, 2, 1, 2, 2 },
          { 3, 3, 2, 2, 2, 1, 3, 2 },
          { 3, 2, 2, 2, 3, 3, 1, 2 },
          { 3, 3, 3, 2, 2, 2, 3, 3 } };
  /**
   * Variabile che rappresenta il costo, in € al minuto, per emittente in ogni
   * fascia oraria
   */
  private static final int[][] C = { { 1400, 1198, 1272, 1082, 936, 1130, 1280, 1249 },
          { 1069, 1358, 1194, 1227, 1344, 975, 1206, 1021 },
          { 1285, 964, 1342, 924, 1286, 1298, 1320, 925 },
          { 911, 1052, 959, 1149, 1170, 1363, 1296, 1002 },
          { 1121, 1211, 988, 1168, 1175, 1037, 1066, 1221 },
          { 929, 971, 1144, 1257, 1103, 1208, 1125, 1305 },
          { 1345, 1103, 1349, 1213, 1101, 1283, 1303, 928 },
          { 1385, 1136, 975, 1239, 1179, 1140, 1387, 1282 },
          { 918, 1054, 1281, 1337, 935, 1119, 1210, 1132 },
          { 1133, 1302, 927, 1179, 1027, 1207, 1150, 1088 } };
  /**
   * Variabile che rappresenta il numero di spettatori al minuto per emittente in
   * ogni fascia oraria
   */
  private static final int[][] P = { { 2890, 1584, 2905, 2465, 1128, 2285, 3204, 1009 },
          { 3399, 355, 2070, 905, 814, 772, 2502, 2780 },
          { 590, 2861, 744, 3245, 2846, 2545, 2584, 633 },
          { 1332, 2682, 3264, 1558, 1162, 414, 1004, 1580 },
          { 674, 1122, 1333, 1205, 3319, 2519, 2827, 1852 },
          { 2481, 1761, 2079, 1197, 3223, 3478, 2767, 1462 },
          { 1740, 3204, 2644, 3302, 474, 2765, 2296, 2376 },
          { 3471, 1593, 2726, 1921, 1841, 1191, 2294, 1642 },
          { 900, 3035, 2951, 1440, 852, 1842, 307, 3189 },
          { 2104, 389, 3188, 365, 1931, 2563, 2770, 1844 } };

  // Variabili di Gurobi
  /**
   * Variabile che rappresenta l'ambiente in cui definire il modello'
   */
  private static GRBEnv ambiente;
  /**
   * Variabile che rappresenta il modello del problema
   */
  private static GRBModel modello;
  /**
   * Variabile che rappresenta le incognite del modello
   */
  private static final GRBVar[] x = new GRBVar[M * K];
  /**
   * Variabile che rappresenta le slack/surplus del modello
   */
  private static final GRBVar[] s = new GRBVar[M + K + 3 + (M * K)];
  /**
   * Variabile che rappresenta le variabili ausiliarie del modello
   */
  private static final GRBVar[] y = new GRBVar[M + K + 3 + (M * K)];
  /**
   * Variabile che rappresenta la funzione obiettivo del modello
   */
  private static GRBVar a;

  private static double[] soluzione3;

  // Utilizzare per risolvere il modello in FORMA STANDARD
  private static int indiceSlack = 0;
  private static int indiceAusiliarie = 0;

  // Variabili di utilità
  /**
   * Valore epsilon per i confronti tra <code>double</code>
   */
  private static final double epsilon = 1e-5;

  public static void main(String[] args) {
    try {
      ambiente = new GRBEnv("App.log"); // Creo l'ambiente di Gurobi impostando il file dei log del programma
      ambiente.set(GRB.IntParam.OutputFlag, 0); // Disattivo l'output di default di Gurobi
      ambiente.set(GRB.IntParam.Presolve, 0); // Disattivo gli algoritmi di presolve
      ambiente.set(GRB.IntParam.Method, 0); // Imposto l'utilizzo del simplesso semplice

      modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato

      inizializzaVariabili(0);

      impostaFunzioneObiettivo(0);

      impostaVincoli(0);

      calcolaEStampa();
    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
    }
  }

  /**
   * Metodo che inizializza le variabile, senza valorizzarle
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void inizializzaVariabili(int modalita) throws GRBException {
    // Inizializzazione delle incognite del modello
    for (int i = 0; i < x.length; i++) {
      x[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x" + (i + 1));
    }

    // Inizializzazione delle slack/surplus e delle variabili ausiliarie del modello
    if (modalita == 1) {
      for (int i = 0; i < s.length; i++) {
        s[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s" + (i + 1));
      }
    } else if (modalita == 2) {
      for (int i = 0; i < s.length; i++) {
        s[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s" + (i + 1));
        y[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "y" + (i + 1));
      }
    }

    // Aggiunge la variabile a per sciogliere il modulo
    a = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");
  }

  /**
   * Metodo che crea un'espressione lineare che verrà impostata come funzione
   * obbiettivo del modello
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaFunzioneObiettivo(int modalita) throws GRBException {
    // Creo un'espressione lineare che andrà a rappresentare la mia funzione
    // obiettivo
    GRBLinExpr funzioneObiettivo = new GRBLinExpr();

    if (modalita <= 1){
      funzioneObiettivo.addTerm(1, a); // Aggiungo il termine 'a' alla funzione obiettivo
    }
    else{
      for (int i = 0; i < y.length; i++) {
        funzioneObiettivo.addTerm(1, y[i]); // Aggiungo il termini y alla funzione obiettivo nel caso di risoluzione con metodo delle due fasi
      }
    }

    modello.setObjective(funzioneObiettivo, GRB.MINIMIZE); // Imposto come funzione obiettivo del modello l'espressione
    // lineare creata dicendo che voglio minimizzarla
  }

  /**
   * Metodo per impostare tutti i vincoli del modello:
   * - Vincoli di Modulo <br>
   * - Vincoli di Copertura <br>
   * - Vincoli di Costo <br>
   * - Vincoli di Bilancio <br>
   * - Vincoli di Tempo
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaVincoli(int modalita) throws GRBException {
    // VINCOLI DI MODULO //
    // Creo due espressioni lineari che andranno a rappresentare i vincoli di moduli
    GRBLinExpr vincoloDiModulo0 = new GRBLinExpr();
    GRBLinExpr vincoloDiModulo1 = new GRBLinExpr();

    // Aggiungo le variabili alle rispettive espressioni lineari
    for (int j = 0; j < K; j++) {
      for (int i = 0; i < M; i++) {
        vincoloDiModulo0.addTerm(j < K / 2 ? P[i][j] : -P[i][j], x[i + j + (M - 1) * j]);
        vincoloDiModulo1.addTerm(j < K / 2 ? -P[i][j] : P[i][j], x[i + j + (M - 1) * j]);
      }
    }

    // Aggiungo le variabili di slack/surplus e quelle ausiliarie alle rispettive espressioni lineari (solo FORMA STANDARD)
    if(modalita == 1) {
      vincoloDiModulo0.addTerm(1, s[indiceSlack++]);
      vincoloDiModulo1.addTerm(1, s[indiceSlack++]);
    } else if (modalita == 2) {
      vincoloDiModulo0.addTerm(1, s[indiceSlack++]);
      vincoloDiModulo0.addTerm(1, y[indiceAusiliarie++]);

      vincoloDiModulo1.addTerm(1, s[indiceSlack++]);
      vincoloDiModulo1.addTerm(1, y[indiceAusiliarie++]);
    }


    // Aggiungo i vincoli con il termine noto al modello (FORMA NON STANDARD)
    if(modalita == 0) {
      modello.addConstr(vincoloDiModulo0, GRB.LESS_EQUAL, a, "Vincolo_di_modulo_0");
      modello.addConstr(vincoloDiModulo1, GRB.LESS_EQUAL, a, "Vincolo_di_modulo_1");
    } else {
      // Aggiungo i vincoli con il termine noto al modello (solo FORMA STANDARD)
      modello.addConstr(vincoloDiModulo0, GRB.EQUAL, a, "Vincolo_di_modulo_0");
      modello.addConstr(vincoloDiModulo1, GRB.EQUAL, a, "Vincolo_di_modulo_1");
    }


    // VINCOLI DI COPERTURA //
    // Creo un'espressioni lineare che andrà a rappresentare il vincolo di copertura
    GRBLinExpr vincoloDiCopertura = new GRBLinExpr();

    // Aggiungo le variabili all'espressione lineare
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < K; j++) {
        vincoloDiCopertura.addTerm(P[i][j], x[i + j + (M - 1) * j]);
      }
    }

    // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (FORMA STANDARD)
    if (modalita == 1) {
      vincoloDiCopertura.addTerm(-1, s[indiceSlack++]);
    } else if (modalita == 2) {
      vincoloDiCopertura.addTerm(-1, s[indiceSlack++]);
      vincoloDiCopertura.addTerm(1, y[indiceAusiliarie++]);
    }


    // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
    if (modalita == 0) {
      modello.addConstr(vincoloDiCopertura, GRB.GREATER_EQUAL, S, "Vincolo_di_copertura");
    } else {
      // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
      modello.addConstr(vincoloDiCopertura, GRB.EQUAL, S, "Vincolo_di_copertura");
    }

    // VINCOLI DI COSTO //
    if (modalita == 0) {
      for (int i = 0; i < M; i++) {
        // Creo un'espressioni lineare che andrà a rappresentare il vincolo di costo
        GRBLinExpr vincoloDiCosto = new GRBLinExpr();

        // Aggiungo le variabili all'espressione lineare
        for (int j = 0; j < K; j++) {
          vincoloDiCosto.addTerm(C[i][j], x[i + j + (M - 1) * j]);
        }

        // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
        modello.addConstr(vincoloDiCosto, GRB.LESS_EQUAL, beta[i], "Vincolo_di_costo_" + i);
      }
    } else if (modalita == 1) {
      for (int i = 0; i < M; i++) {
        // Creo un'espressioni lineare che andrà a rappresentare il vincolo di costo
        GRBLinExpr vincoloDiCosto = new GRBLinExpr();

        // Aggiungo le variabili all'espressione lineare
        for (int j = 0; j < K; j++) {
          vincoloDiCosto.addTerm(C[i][j], x[i + j + (M - 1) * j]);
        }

        // Aggiungo la variabile di slack/surplus all'espressione lineare (FORMA STANDARD)
        vincoloDiCosto.addTerm(1, s[indiceSlack++]);

        // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
        modello.addConstr(vincoloDiCosto, GRB.EQUAL, beta[i], "Vincolo_di_costo_" + i);

      }
    } else {
      for (int i = 0; i < M; i++) {
        // Creo un'espressioni lineare che andrà a rappresentare il vincolo di costo
        GRBLinExpr vincoloDiCosto = new GRBLinExpr();

        // Aggiungo le variabili all'espressione lineare
        for (int j = 0; j < K; j++) {
          vincoloDiCosto.addTerm(C[i][j], x[i + j + (M - 1) * j]);
        }

        // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione
        // lineare (FORMA STANDARD)
        vincoloDiCosto.addTerm(1, s[indiceSlack++]);
        vincoloDiCosto.addTerm(1, y[indiceAusiliarie++]);


        // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
        modello.addConstr(vincoloDiCosto, GRB.EQUAL, beta[i], "Vincolo_di_costo_" + i);
      }
    }

    // VINCOLI DI BILANCIO //
    // Creo un double che andrà a rappresentare il termine noto dei vincoli
    double termineNoto = 0.0;

    // Calcolo il termine noto dei vincoli di bilancio
    for (int i = 0; i < M; i++) {
      termineNoto += beta[i];
    }
    termineNoto *= omega;

    if (modalita == 0) {
      for (int j = 0; j < K; j++) {
        // Creo un'espressioni lineare che andrà a rappresentare il vincolo di bilancio
        GRBLinExpr vincoloDiBilancio = new GRBLinExpr();

        // Aggiungo le variabili all'espressione lineare
        for (int i = 0; i < M; i++) {
          vincoloDiBilancio.addTerm(C[i][j], x[i + j + (M - 1) * j]);
        }

        // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
        modello.addConstr(vincoloDiBilancio, GRB.GREATER_EQUAL, termineNoto, "Vincolo_di_budget_" + j);
      }
    } else if (modalita == 1) {
      for (int j = 0; j < K; j++) {
        // Creo un'espressioni lineare che andrà a rappresentare il vincolo di bilancio
        GRBLinExpr vincoloDiBilancio = new GRBLinExpr();

        // Aggiungo le variabili all'espressione lineare
        for (int i = 0; i < M; i++) {
          vincoloDiBilancio.addTerm(C[i][j], x[i + j + (M - 1) * j]);
        }

        // Aggiungo la variabile di slack/surplus all'espressione lineare (FORMA STANDARD)
        vincoloDiBilancio.addTerm(-1, s[indiceSlack++]);

        // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
        modello.addConstr(vincoloDiBilancio, GRB.EQUAL, termineNoto, "Vincolo_di_budget_" + j);
      }
    } else {
      for (int j = 0; j < K; j++) {
        // Creo un'espressioni lineare che andrà a rappresentare il vincolo di bilancio
        GRBLinExpr vincoloDiBilancio = new GRBLinExpr();

        // Aggiungo le variabili all'espressione lineare
        for (int i = 0; i < M; i++) {
          vincoloDiBilancio.addTerm(C[i][j], x[i + j + (M - 1) * j]);
        }

        // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (FORMA STANDARD)
        vincoloDiBilancio.addTerm(-1, s[indiceSlack++]);
        vincoloDiBilancio.addTerm(1, y[indiceAusiliarie++]);

        // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
        modello.addConstr(vincoloDiBilancio, GRB.EQUAL, termineNoto, "Vincolo_di_budget_" + j);
      }
    }

    // VINCOLI DI TEMPO //
    if (modalita == 0) {
      for (int j = 0; j < K; j++) {
        for (int i = 0; i < M; i++) {
          // Creo un'espressione lineare che andrà a rappresentare il vincolo di tempo
          GRBLinExpr vincoloDiTempo = new GRBLinExpr();

          // Aggiungo la variabile all'espressione lineare
          vincoloDiTempo.addTerm(1, x[i + j + (M - 1) * j]);

          // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
          modello.addConstr(vincoloDiTempo, GRB.LESS_EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" + j);
        }
      }
    } else if (modalita == 1) {
      for (int j = 0; j < K; j++) {
        for (int i = 0; i < M; i++) {
          // Creo un'espressione lineare che andrà a rappresentare il vincolo di tempo
          GRBLinExpr vincoloDiTempo = new GRBLinExpr();

          // Aggiungo la variabile all'espressione lineare
          vincoloDiTempo.addTerm(1, x[i + j + (M - 1) * j]);

          // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (solo FORMA STANDARD)
          vincoloDiTempo.addTerm(1, s[indiceSlack++]);

          // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
          modello.addConstr(vincoloDiTempo, GRB.EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" + j);
        }
      }
    } else {
      for (int j = 0; j < K; j++) {
        for (int i = 0; i < M; i++) {
          // Creo un'espressione lineare che andrà a rappresentare il vincolo di tempo
          GRBLinExpr vincoloDiTempo = new GRBLinExpr();

          // Aggiungo la variabile all'espressione lineare
          vincoloDiTempo.addTerm(1, x[i + j + (M - 1) * j]);

          // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (solo FORMA STANDARD)
          vincoloDiTempo.addTerm(1, s[indiceSlack++]);
          vincoloDiTempo.addTerm(1, y[indiceAusiliarie++]);


          // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
          modello.addConstr(vincoloDiTempo, GRB.EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" + j);
        }
      }
    }
  }

  /**
   * Metodo che calcola l'ottimo del problema e stampa i risultati
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void calcolaEStampa() throws GRBException {
    modello.update();
    modello.optimize();
    modello.write("App.lp");

    System.out.println("\n\nGRUPPO 81\nComponenti: Brignoli Muscio");

    System.out.println("\nQUESITO I:");
    System.out.println("funzione obbiettivo = " + ottieniValoreFunzioneObbiettivo());
    System.out.println("copertura raggiunta totale (spettatori) = " + calcolaCoperturaRaggiuntaTotale());
    System.out.println("tempo acquistato (minuti) = " + calcolaTempoAcquistato());
    System.out.println("budget inutilizzato = " + calcolaBilancioInutilizzato());
    System.out.print("soluzione di base ottima:\n" + ottieniSoluzioneDiBaseOttima());

    System.out.println("\nQUESITO II:");
    System.out.println("variabili in base: " + ottieniVariabiliInBase());
    System.out.println("coefficienti di costo ridotto: " + formattaDouble(ottieniCCR()));
    System.out.println("soluzione ottima multipla: " + isMultipla());
    System.out.println("soluzione ottima degenere: " + isDegenere());
    System.out.println("vincoli vertice ottimo:" + ottieniVincoliVerticeOttimo());

    System.out.println("\nQUESITO III:");
    System.out.println("soluzione 1: \n" + soluzione1());
    System.out.println("soluzione 2: \n" + soluzione2());
    System.out.println("soluzione 3: \n" + soluzione3());
  }

  /**
   * Metodo per ottenere il valore della funzione obbiettivo
   *
   * @return Una <code>String</code> rappresentante il valore della funzione
   *         obbiettivo con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String ottieniValoreFunzioneObbiettivo() throws GRBException {
    return String.format("%.4f", modello.get(GRB.DoubleAttr.ObjVal));
  }

  /**
   * Metodo per calcolare e ottenere il valore della copertura totale di
   * spettatori raggiunta
   *
   * @return Una <code>String</code> rappresentante il valore della copertura
   *         totale di spettatori raggiunta con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String calcolaCoperturaRaggiuntaTotale() throws GRBException {
    int i = 0, j = 0;
    double coperturaRaggiuntaTotale = 0.0;

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      if (nome.contains("x")) {
        coperturaRaggiuntaTotale += P[i][j] * valore;

        i++;
        if (i == M) {
          i = 0;
          j++;
        }
      }
    }

    return String.format("%.4f", coperturaRaggiuntaTotale);
  }

  /**
   * Metodo per calcolare e ottenere il valore del tempo acquistato dalle varie
   * emittenti
   *
   * @return Una <code>String</code> rappresentante il valore del tempo acquistato
   *         con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String calcolaTempoAcquistato() throws GRBException {
    double tempoAcquistato = 0.0;

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      if (nome.contains("x")) {
        tempoAcquistato += valore;
      }
    }

    return String.format("%.4f", tempoAcquistato);
  }

  /**
   * Metodo per calcolare e ottenere il valore del bilancio inutilizzato
   *
   * @return Una <code>String</code> rappresentante il valore del bilancio
   *         inutilizzato con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String calcolaBilancioInutilizzato() throws GRBException {
    int bilancioTotale = 0, bilancioUtilizzato = 0, i = 0, j = 0;

    for (int bilancio : beta) {
      bilancioTotale += bilancio;
    }

    for (GRBVar x : modello.getVars()) {
      String name = x.get(GRB.StringAttr.VarName);
      double value = x.get(GRB.DoubleAttr.X);

      if (name.contains("x")) {
        bilancioUtilizzato += C[i][j] * value;

        i++;
        if (i == M) {
          i = 0;
          j++;
        }
      }
    }

    return String.format("%d", bilancioTotale - bilancioUtilizzato);
  }

  /**
   * Metodo per ottenere tutti i nomi e i valori delle variabili che formano la
   * soluzione di base ottima
   *
   * @return Una <code>String</code> rappresentante tutti i nomi e i valori delle
   *         variabili che formano la soluzione di base ottima
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String ottieniSoluzioneDiBaseOttima() throws GRBException {
    StringBuilder soluzioneDiBaseOttima = new StringBuilder();
    int i = 1, count = 0;
    soluzione3 = new double[modello.getVars().length + modello.getConstrs().length];

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      soluzioneDiBaseOttima.append(String.format("%s = %.4f\n", nome, valore));
      soluzione3[count++] = valore;
    }

    String nome = "s";

    for (GRBConstr x : modello.getConstrs()) {
      double valore = x.get(GRB.DoubleAttr.Slack);

      soluzioneDiBaseOttima.append(String.format("%s = %.4f\n", nome+i++, valore >= 0 ? valore : -valore));
      soluzione3[count++] = valore;
    }

    return soluzioneDiBaseOttima.toString();
  }

  /**
   * Metodo per ottenere tutte le variabili in base del modello
   *
   * @return Una <code>ArrayList</code> di <code>Integer</code> rappresentante
   *         tutte le variabili in base del modello
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static ArrayList<Integer> ottieniVariabiliInBase() throws GRBException {

    ArrayList<Integer> variabiliInBase = new ArrayList<Integer>();
    int inBase;

    for (GRBVar x : modello.getVars()) {
      inBase = x.get(GRB.IntAttr.VBasis);

      variabiliInBase.add(inBase == GRB.BASIC ? 1 : 0);
    }

    for (GRBConstr x : modello.getConstrs()) {
      inBase = x.get(GRB.IntAttr.CBasis);
      variabiliInBase.add(inBase == GRB.BASIC ? 1 : 0);
    }

    return variabiliInBase;
  }

  /**
   * Metodo per ottenere tutti i coefficienti di costo ridotto delle variabili del
   * modello
   *
   * @return Una <code>ArrayList</code> di <code>Double</code> rappresentante
   *         tutti i coefficienti di costo
   *         ridotto delle variabili del modello
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static ArrayList<Double> ottieniCCR() throws GRBException {

    ArrayList<Double> ccrValues = new ArrayList<>();

    for (GRBVar x : modello.getVars()) {
      double ccr = x.get(GRB.DoubleAttr.RC);

      ccrValues.add(ccr);
    }

    for (GRBConstr x : modello.getConstrs()) {
      double ccr = x.get(GRB.DoubleAttr.Pi);

      ccrValues.add(Math.abs(ccr));
    }
    return ccrValues;
  }

  /**
   * Metodo per ottenere se l'ottimo è multiplo oppure no
   *
   * @return Una <code>String</code> contenente "Si" se l'ottimo è multiplo
   *         altrimenti contenente "No"
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String isMultipla() throws GRBException {

    ArrayList<Integer> variabiliInBase = ottieniVariabiliInBase();

    for (int i = 0; i < modello.getVars().length; i++) {
      if (variabiliInBase.get(i) == 0 && Math.abs(modello.getVar(i).get(GRB.DoubleAttr.RC)) < epsilon) {
        return "Sì";
      }
    }

    for (int i = 0; i < modello.getConstrs().length; i++) {
      if (variabiliInBase.get(i) == 0 && Math.abs(modello.getVar(i).get(GRB.DoubleAttr.Pi)) < epsilon) {
        return "Sì";
      }
    }

    return "No";
  }

  /**
   * Metodo per ottenere se l'ottimo è degenere oppure no
   *
   * @return Una <code>String</code> contenente "Si" se l'ottimo è degenere
   *         altrimenti contenente "No"
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String isDegenere() throws GRBException {

    ArrayList<Integer> variabiliInBase = ottieniVariabiliInBase();
    ArrayList<Double> ccr = ottieniCCR();

    for (int i = 0; i < modello.getVars().length; i++) {
      if (variabiliInBase.get(i) == 1 && Math.abs(modello.getVar(i).get(GRB.DoubleAttr.X)) < epsilon) {
        return "Sì";
      }
    }

    for (int i = 0; i < modello.getConstrs().length; i++) {
      if (variabiliInBase.get(i) == 1 && Math.abs(modello.getVar(i).get(GRB.DoubleAttr.RHS)) < epsilon) {
        return "Sì";
      }
    }

    return "No";
  }

  /**
   * Metodo per ottenere tutti i vincoli che intersecati danno il vertice ottimo
   *
   * @return Una <code>ArrayList</code> di <code>String</code> rappresentante
   *         tutti i vincoli che intersecati danno il vertice ottimo
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static ArrayList<String> ottieniVincoliVerticeOttimo() throws GRBException {

    ArrayList<String> vincoliVerticeOttimo = new ArrayList<String>();

    for (GRBConstr vincolo : modello.getConstrs()) {
      if (Math.abs(vincolo.get(GRB.DoubleAttr.Slack)) < epsilon)
        vincoliVerticeOttimo.add(vincolo.get(GRB.StringAttr.ConstrName));
    }

    return vincoliVerticeOttimo;
  }

  /**
   * Formatta con 4 cifre decimali un <code>ArrayList</code> di
   * <code>Double</code>
   * 
   * @param daFormattare L'<code>ArrayList</code> di <code>Double</code> da
   *                     formattare
   * @return Un <code>ArrayList</code> di <code>String</code> formatta
   */
  private static ArrayList<String> formattaDouble(ArrayList<Double> daFormattare) {
    ArrayList<String> formattato = new ArrayList<String>();

    for (double valore : daFormattare) {
      formattato.add(String.format("%.4f", valore));
    }

    return formattato;
  }

  private static String soluzione2() throws GRBException {

    StringBuilder soluzione = new StringBuilder();
    modello = new GRBModel(ambiente);

    inizializzaVariabili(1);
    impostaFunzioneObiettivo(1);
    impostaVincoli(1);

    GRBLinExpr e = new GRBLinExpr();
    e.addTerm(1, s[0]);
    modello.addConstr(e,GRB.EQUAL, 100, "fasullo");

    modello.update();
    modello.optimize();

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      soluzione.append(String.format("%s = %.4f\n", nome, valore));
    }

    return soluzione.toString();
  }

  private static String soluzione3() throws GRBException {

    StringBuilder soluzione = new StringBuilder();
    modello = new GRBModel(ambiente);
    indiceSlack = 0;

    inizializzaVariabili(2);
    impostaFunzioneObiettivo(2);
    impostaVincoli(2);

    modello.update();
    modello.optimize();

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      if(!nome.contains("y")) {
        soluzione.append(String.format("%s = %.4f\n", nome, valore));
      }
    }

    return soluzione.toString();
  }

  private static String soluzione1() throws GRBException {

    StringBuilder soluzione = new StringBuilder();
    soluzione3 = combinazioneConvessa(soluzione3);
    int i = 0;

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = soluzione3[i++];

      soluzione.append(String.format("%s = %.4f\n", nome, valore));
    }

    return soluzione.toString();
  }

  /**
   * Formatta con 4 cifre decimali un <code>ArrayList</code> di
   * <code>Double</code>
   *
   * @param v  di <code>double</code> di cui fare la combinazione convessa
   * @return Un <code>double[]</code> contenente la combinazione convessa
   */
  private static double[] combinazioneConvessa(double v[]) {

    double alfa[] = new double[v.length];
    double counter = 0;
    Random rand = new Random();
    do {
      for (int i = 0; i < v.length - 1; i++) {
        alfa[i] = (rand.nextInt(1000) + 1) / 100000.;
        counter += alfa[i];
      }

      alfa[v.length-1] = 1-counter;
    }while(alfa[v.length-1] < 0);

    for (int i = 0; i < alfa.length; i++) {
      alfa[i] = alfa[i] * v[i];
    }

    return alfa;
  }
}