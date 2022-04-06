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

/**
 * Classe principale contenete l'intero modello, non che il suo svolgimento
 */
public class App {
  //Variabili del problema
  /**
   * Variabile che rappresenta il numero di emittenti televisive
   */
  private static final int M = 10;
  /**
   * Variabile che rappresenta il numero di fasce orarie in una giornata
   */
  private static final int K = 8;
  /**
   * Variabile che rappresenta il minimo numero di spettatori giornalieri da raggiungere
   */
  private static final int S = 82110;
  /**
   * Variabile che rappresenta la percentuale minima di bilancio da spendere per emittente sul bilancio totale
   */
  private static final double omega = 0.02;
  /**
   * Variabile che rappresenta il massimo bilancio spendibile per emittente
   */
  private static final int[] beta = {3373, 3281, 3274, 3410, 2691, 2613, 3354, 2912, 3203, 2616};
  /**
   * Variabile che rappresenta il numero massimo di minuti acquistabili da ogni emittente per fascia oraria
   */
  private static final int[][] tau = {{2, 2, 2, 1, 2, 2, 1, 3},
                                      {2, 2, 1, 2, 2, 2, 2, 3},
                                      {2, 2, 2, 2, 3, 1, 2, 1},
                                      {2, 2, 2, 1, 1, 3, 2, 1},
                                      {2, 1, 3, 2, 3, 2, 2, 1},
                                      {2, 1, 2, 2, 2, 3, 3, 2},
                                      {3, 3, 1, 1, 2, 1, 2, 2},
                                      {3, 3, 2, 2, 2, 1, 3, 2},
                                      {3, 2, 2, 2, 3, 3, 1, 2},
                                      {3, 3, 3, 2, 2, 2, 3, 3}};
  /**
   * Variabile che rappresenta il costo, in €, al minuto per emittente in ogni fascia oraria
   */
  private static final int[][] C = {{1400, 1198, 1272, 1082,  936, 1130, 1280, 1249},
                                    {1069, 1358, 1194, 1227, 1344,  975, 1206, 1021},
                                    {1285,  964, 1342,  924, 1286, 1298, 1320,  925},
                                    { 911, 1052,  959, 1149, 1170, 1363, 1296, 1002},
                                    {1121, 1211,  988, 1168, 1175, 1037, 1066, 1221},
                                    { 929,  971, 1144, 1257, 1103, 1208, 1125, 1305},
                                    {1345, 1103, 1349, 1213, 1101, 1283, 1303,  928},
                                    {1385, 1136,  975, 1239, 1179, 1140, 1387, 1282},
                                    { 918, 1054, 1281, 1337,  935, 1119, 1210, 1132},
                                    {1133, 1302,  927, 1179, 1027, 1207, 1150, 1088}};
  /**
   * Variabile che rappresenta il numero di spettatori al minuto per emittente in ogni fascia oraria
   */
  private static final int[][] P = {{2890, 1584, 2905, 2465, 1128, 2285, 3204, 1009},
                                    {3399,  355, 2070,  905,  814,  772, 2502, 2780},
                                    { 590, 2861,  744, 3245, 2846, 2545, 2584,  633},
                                    {1332, 2682, 3264, 1558, 1162,  414, 1004, 1580},
                                    { 674, 1122, 1333, 1205, 3319, 2519, 2827, 1852},
                                    {2481, 1761, 2079, 1197, 3223, 3478, 2767, 1462},
                                    {1740, 3204, 2644, 3302,  474, 2765, 2296, 2376},
                                    {3471, 1593, 2726, 1921, 1841, 1191, 2294, 1642},
                                    { 900, 3035, 2951, 1440,  852, 1842,  307, 3189},
                                    {2104,  389, 3188,  365, 1931, 2563, 2770, 1844}};

  //Variabili di Gurobi
  /**
   * Variabile che rappresenta il modello del problema
   */
  private static GRBModel modello;
  /**
   * Variabile che rappresenta le incognite del modello
   */
  private static final GRBVar[] x = new GRBVar[M*K];
  /**
   * Variabile che rappresenta le slack/surplus del modello
   */
  private static final GRBVar[] s = new GRBVar[M+K+3+(M*K)];
  /**
   * Variabile che rappresenta le variabili ausiliarie del modello
   */
  private static final GRBVar[] y = new GRBVar[M+K+3+(M*K)];
  /**
   * Variabile che rappresenta la funzione obbiettivo del modello
   */
  private static GRBVar a;

  // Utilizzare per risolvere il modello in FORMA STANDARD
  /*private static int indiceSlack = 0;
  private static int indiceAusiliarie = 0;*/

  public static void main(String[] args) {
    try {
      GRBEnv ambiente = new GRBEnv("App.log"); // Creo l'ambiente di Gurobi impostando il file dei log del programma
      ambiente.set(GRB.IntParam.OutputFlag, 0); // Disattivo l'output di default di Gurobi
      ambiente.set(GRB.IntParam.Presolve, 0); // Disattivo gli algoritmi di presolve
      ambiente.set(GRB.IntParam.Method, 0); // Imposto l'utilizzo del simplesso semplice

      modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato
      
      inizializzaVariabili();

      impostaFunzioneObbiettivo();

      impostaVincoliDiModulo();
      impostaVincoliDiCopertura();
      impostaVincoliDiCosto();
      impostaVincoliDiBilancio();
      impostaVincoliDiTempo();

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
  private static void inizializzaVariabili() throws GRBException {
    // Inizializzazione delle incognite del modello
    for (int i = 0; i < x.length; i++) {
      x[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x"+(i+1));
    }

    // Inizializzazione delle slack/surplus e delle variabili ausiliarie del modello
    for (int i = 0; i < s.length; i++) {
      s[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s"+(i+1));
      y[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "y"+(i+1));
    }

    // Inizializzazione della funzione obbiettivo
    a = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");
  }

  /**
   * Metodo che crea un'espressione lineare che verrà impostata come funzione obbiettivo del modello
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaFunzioneObbiettivo() throws GRBException {
    GRBLinExpr funzioneObiettivo = new GRBLinExpr(); // Creo un'espressione lineare che andrà a rappresentare la mia funzione obbiettivo

    funzioneObiettivo.addTerm(1, a); // Aggiungo il termine 'a' alla funzione obbiettivo

    modello.setObjective(funzioneObiettivo, GRB.MINIMIZE); // Imposto come funzione obbiettivo del modello l'espressione lineare creata dicendo che voglio minimizzarla
  }

  /**
   * Metodo che crea e imposta le due espressioni lineari rappresentanti i due vincoli di moduli
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaVincoliDiModulo() throws GRBException {
    // Creo due espressioni lineari che andranno a rappresentare i vincoli di moduli
    GRBLinExpr vincoloDiModulo0 = new GRBLinExpr();
    GRBLinExpr vincoloDiModulo1 = new GRBLinExpr();

    // Aggiungo le variabili alle rispettive espressioni lineari
    for (int j = 0; j < K; j++) {
      for (int i = 0; i < M; i++) {
        vincoloDiModulo0.addTerm(j < K/2 ? P[i][j] : -P[i][j], x[i+j+(M-1)*j]);
        vincoloDiModulo1.addTerm(j < K/2 ? -P[i][j] : P[i][j], x[i+j+(M-1)*j]);
      }
    }

    // Aggiungo le variabili di slack/surplus e quelle ausiliarie alle rispettive espressioni lineari (FORMA STANDARD)
    /*vincoloModulo0.addTerm(1, s[indiceSlack++]);
    vincoloModulo0.addTerm(1, y[indiceAusiliarie++]);

    vincoloModulo1.addTerm(1, s[indiceSlack++]);
    vincoloModulo1.addTerm(1, y[indiceAusiliarie++]);*/

    // Aggiungo i vincoli con il termine noto al modello (FORMA NON STANDARD)
    modello.addConstr(vincoloDiModulo0, GRB.LESS_EQUAL, a, "Vincolo_di_modulo_0");
    modello.addConstr(vincoloDiModulo1, GRB.LESS_EQUAL, a, "Vincolo_di_modulo_1");

    // Aggiungo i vincoli con il termine noto al modello (FORMA STANDARD)
    /*modello.addConstr(vincoloModulo0, GRB.EQUAL, a, "Vincolo_di_modulo_0");
    modello.addConstr(vincoloModulo1, GRB.EQUAL, a, "Vincolo_di_modulo_1");*/
  }

  /**
   * Metodo che crea e imposta l'espressione lineare rappresentante il vincolo di copertura
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaVincoliDiCopertura() throws GRBException {
    // Creo un'espressioni lineare che andrà a rappresentare il vincolo di copertura
    GRBLinExpr vincoloDiCopertura = new GRBLinExpr();

    // Aggiungo le variabili all'espressione lineare
    for (int i = 0; i < M; i++) {
      for (int j = 0; j < K; j++) {
        vincoloDiCopertura.addTerm(P[i][j], x[i+j+(M-1)*j]);
      }
    }

    // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (FORMA STANDARD)
    /*vincoloCopertura.addTerm(-1, s[indiceSlack++]);
    vincoloCopertura.addTerm(1, y[indiceAusiliarie++]);*/

    // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
    modello.addConstr(vincoloDiCopertura, GRB.GREATER_EQUAL, S, "Vincolo_di_copertura");

    // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
    /*modello.addConstr(vincoloDiCopertura, GRB.EQUAL, S, "Vincolo_di_copertura");*/
  }

  /**
   * Metodo che crea e imposta ogni singola espressione lineare rappresentante il singolo vincolo di costo
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaVincoliDiCosto() throws GRBException {
    for (int i = 0; i < M; i++) {
      // Creo un'espressioni lineare che andrà a rappresentare il vincolo di costo
      GRBLinExpr vincoloDiCosto = new GRBLinExpr();

      // Aggiungo le variabili all'espressione lineare
      for (int j = 0; j < K; j++) {
        vincoloDiCosto.addTerm(C[i][j], x[i+j+(M-1)*j]);
      }

      // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (FORMA STANDARD)
      /*vincoloCosto.addTerm(1, s[indiceSlack++]);
      vincoloCosto.addTerm(1, y[indiceAusiliarie++]);*/

      // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
      modello.addConstr(vincoloDiCosto, GRB.LESS_EQUAL, beta[i], "Vincolo_di_costo_" + i);

      // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
      /*modello.addConstr(vincoloCosto, GRB.EQUAL, beta[i], "Vincolo_di_costo_" + i);*/
    }
  }

  /**
   * Metodo che crea e imposta ogni singola espressione lineare rappresentante il singolo vincolo di bilancio
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaVincoliDiBilancio() throws GRBException {
    // Creo un double che andrà a rappresentare il termine noto dei vincoli
    double termineNoto = 0.0;

    // Calcolo il termine noto dei vincoli di bilancio
    for (int i = 0; i < M; i++) {
      termineNoto += beta[i];
    }
    termineNoto *= omega;

    for (int j = 0; j < K; j++) {
      // Creo un'espressioni lineare che andrà a rappresentare il vincolo di bilancio
      GRBLinExpr vincoloDiBilancio= new GRBLinExpr();

      // Aggiungo le variabili all'espressione lineare
      for (int i = 0; i < M; i++) {
        vincoloDiBilancio.addTerm(C[i][j], x[i+j+(M-1)*j]);
      }

      // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (FORMA STANDARD)
      /*vincoloConcorrenza.addTerm(-1, s[indiceSlack++]);
      vincoloConcorrenza.addTerm(1, y[indiceAusiliarie++]);*/

      // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
      modello.addConstr(vincoloDiBilancio, GRB.GREATER_EQUAL, termineNoto, "Vincolo_di_budget_" + j);

      // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
      /*modello.addConstr(vincoloDiBilancio, GRB.EQUAL, termineNoto, "Vincolo_di_budget_" + j);*/
    }
  }

  /**
   * Metodo che crea e imposta ogni singola espressione lineare rappresentante il singolo vincolo di tempo
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaVincoliDiTempo() throws GRBException {
    for (int j = 0; j < K; j++) {
      for (int i = 0; i < M; i++) {
        // Creo un'espressione lineare che andrà a rappresentare il vincolo di tempo
        GRBLinExpr vincoloTempo = new GRBLinExpr();

        // Aggiungo la variabile all'espressione lineare
        vincoloTempo.addTerm(1, x[i+j+(M-1)*j]);

        // Aggiungo la variabile di slack/surplus e quella ausiliaria all'espressione lineare (FORMA STANDARD)
        /*vincoloTempo.addTerm(1, s[indiceSlack++]);
        vincoloTempo.addTerm(1, y[indiceAusiliarie++]);*/

        // Aggiungo il vincolo con il termine noto al modello (FORMA NON STANDARD)
        modello.addConstr(vincoloTempo, GRB.LESS_EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" + j);

        // Aggiungo il vincolo con il termine noto al modello (FORMA STANDARD)
        /*modello.addConstr(vincoloTempo, GRB.EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" + j);*/
      }
    }
  }

  private static void calcolaEStampa() throws GRBException {
    modello.update();
    modello.optimize();
    modello.write("App.lp");

    System.out.println("\n\nGRUPPO 81\nComponenti: Brignoli Muscio\n\nQUESITO I:");
    System.out.println("funzione obbiettivo = " + ottieniValoreFunzioneObbiettivo());
    System.out.println("copertura raggiunta totale (spettatori) = " + calcolaCoperturaRaggiuntaTotale());
    System.out.println("tempo acquistato (minuti) = " + calcolaTempoAcquistato());
    System.out.println("budget inutilizzato = " + calcolaBilancioInutilizzato());
    System.out.print("soluzione di base ottima:\n" + ottieniSoluzioneDiBaseOttima());
  }

  /**
   * Metodo per ottenere il valore della funzione obbiettivo
   *
   * @return Una <code>String</code> rappresentante il valore della funzione obbiettivo con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String ottieniValoreFunzioneObbiettivo() throws GRBException {
    return String.format("%.4f", modello.get(GRB.DoubleAttr.ObjVal));
  }

  /**
   * Metodo per calcolare e ottenere il valore della copertura totale di spettatori raggiunta
   *
   * @return Una <code>String</code> rappresentante il valore della copertura totale di spettatori raggiunta con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String calcolaCoperturaRaggiuntaTotale() throws GRBException {
    int i = 0, j = 0;
    double coperturaRaggiuntaTotale = 0.0;

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      if(nome.contains("x")) {
        coperturaRaggiuntaTotale += P[i][j]*valore;

        i++;
        if(i == M) {
          i = 0;
          j++;
        }
      }
    }

    return String.format("%.4f", coperturaRaggiuntaTotale);
  }

  /**
   * Metodo per calcolare e ottenere il valore del tempo acquistato dalle varie emittenti
   *
   * @return Una <code>String</code> rappresentante il valore del tempo acquistato con quattro cifre decimali
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String calcolaTempoAcquistato() throws GRBException {
    double tempoAcquistato = 0.0;

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      if(nome.contains("x")) {
        tempoAcquistato += valore;
      }
    }

    return String.format("%.4f", tempoAcquistato);
  }

  /**
   * Metodo per calcolare e ottenere il valore del bilancio inutilizzato
   *
   * @return Una <code>String</code> rappresentante il valore del bilancio inutilizzato con quattro cifre decimali
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

      if(name.contains("x")) {
        bilancioUtilizzato += C[i][j]*value;

        i++;
        if(i == M) {
          i = 0;
          j++;
        }
      }
    }

    return String.format("%d", bilancioTotale - bilancioUtilizzato);
  }

  /**
   * Metodo per calcolare e ottenere tutti i nomi e i valori delle variabili che formano la soluzione di base ottima
   *
   * @return Una <code>String</code> rappresentante tutti i nomi e i valori delle variabili che formano la soluzione di base ottima
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static String ottieniSoluzioneDiBaseOttima() throws GRBException {
    StringBuilder soluzioneDiBaseOttima = new StringBuilder();

    for (GRBVar x : modello.getVars()) {
      String nome = x.get(GRB.StringAttr.VarName);
      double valore = x.get(GRB.DoubleAttr.X);

      soluzioneDiBaseOttima.append(String.format("%s = %.4f\n", nome, valore));
    }

    return  soluzioneDiBaseOttima.toString();
  }
}