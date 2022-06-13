import gurobi.*;

import java.io.IOException;
import java.util.LinkedList;

public class AppProva {

  private static Reader reader;
  private static InputData dati;
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
  private static GRBVar[][] x;

  private static GRBVar verticiEF;
  private static GRBVar verticiGHI;
  private static GRBVar costoAggiuntivo;

  /**
   * Variabile che rappresenta le incognite del modello
   */
  private static GRBVar[] u;

  private static int[][] A;
  private static LinkedList<Integer> percorsoOttimo = new LinkedList<Integer>();

  /**
   * Metodo che inizializza le variabile, senza valorizzarle
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void inizializzaVariabili() throws GRBException {
    // Inizializzazione delle incognite del modello
    x = new GRBVar[dati.N][dati.N];
    u = new GRBVar[dati.N];

    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        x[i][j] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("x%02d_%02d", (i + 1), (j + 1)));
      }
    }

    for (int i = 0; i < u.length - 1; i++) {
      u[i] = modello.addVar(1, dati.N - 1, 0, GRB.INTEGER, String.format("u%02d", (i + 1)));
    }
  }

  /**
   * Metodo che crea un'espressione lineare che verrà impostata come funzione
   * obbiettivo del modello
   *
   * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
   */
  private static void impostaFunzioneObiettivo() throws GRBException {
    GRBLinExpr funzioneObiettivo = new GRBLinExpr();

    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        if (i != j)
          funzioneObiettivo.addTerm(dati.costi[i][j], x[i][j]);
      }
    }

    modello.setObjective(funzioneObiettivo, GRB.MINIMIZE);
  }

  private static void impostaVincoli() throws GRBException {
    // Vincoli di Uguaglianza -- Inizio
    GRBLinExpr vincoloDiUguaglianza0;
    GRBLinExpr vincoloDiUguaglianza1;

    for (int j = 0; j < x.length; j++) {
      vincoloDiUguaglianza0 = new GRBLinExpr();

      for (int i = 0; i < x.length; i++) {
        if (i != j)
          vincoloDiUguaglianza0.addTerm(1, x[i][j]);
      }
      modello.addConstr(vincoloDiUguaglianza0, GRB.EQUAL, 1, "Vincolo_di_uguaglianza_0_" + j);
    }

    for (int i = 0; i < x.length; i++) {
      vincoloDiUguaglianza1 = new GRBLinExpr();

      for (int j = 0; j < x[0].length; j++) {
        if (i != j)
          vincoloDiUguaglianza1.addTerm(1, x[i][j]);
      }
      modello.addConstr(vincoloDiUguaglianza1, GRB.EQUAL, 1, "Vincolo_di_uguaglianza_1_" + i);
    }
    // Vincoli di Uguaglianza -- Fine

    // Vincoli MTZ -- Inizio
    GRBLinExpr vincoliMTZ;

    for (int i = 2; i < u.length; i++) {
      for (int j = 2; j < u.length; j++) {
        if (i != j) {
          vincoliMTZ = new GRBLinExpr();

          vincoliMTZ.addTerm(1, u[i - 1]);
          vincoliMTZ.addTerm(-1, u[j - 1]);
          vincoliMTZ.addTerm((dati.N - 1), x[i][j]);

          modello.addConstr(vincoliMTZ, GRB.LESS_EQUAL, dati.N - 2, "vincolo_MTZ" + i + "_" + j);
        }
      }
    }
    // Vincoli MTZ -- Fine
  }

  private static void inizializzaVariabiliAggiuntive() throws GRBException {

    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        x[i][j] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("x%02d_%02d", (i + 1), (j + 1)));
      }
    }

    for (int i = 0; i < u.length - 1; i++) {
      u[i] = modello.addVar(1, dati.N - 1, 0, GRB.INTEGER, String.format("u%02d", (i + 1)));
    }

    verticiEF = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, "verticiEF");

    verticiGHI = modello.addVar(0.0, GRB.INFINITY, 0, GRB.INTEGER, "verticiGHI");

    costoAggiuntivo = modello.addVar(0.0, GRB.INFINITY, 0, GRB.INTEGER, "costoAggiuntivo");
  }

  private static void impostaFunzioneObiettivoAggiuntiva() throws GRBException {
    // Creo un'espressione lineare che andrà a rappresentare la mia funzione obiettivo
    GRBLinExpr funzioneObiettivo = new GRBLinExpr();

    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        if (i != j)
          funzioneObiettivo.addTerm(dati.costi[i][j], x[i][j]);
      }
    }

    funzioneObiettivo.addTerm(1, verticiGHI);

    modello.setObjective(funzioneObiettivo, GRB.MINIMIZE); // Imposto come funzione obiettivo del modello l'espressione lineare creata dicendo che voglio minimizzarla
  }

  private static void impostaVincoliAggiuntivi() throws GRBException {
    // VincoloA -- Inizio
    GRBLinExpr vincoloA = new GRBLinExpr();

    for (int i = 0; i < x.length; i++) {
      if (i != dati.v)
        vincoloA.addTerm(dati.costi[i][dati.v], x[i][dati.v]);
    }

    for (int j = 0; j < x[0].length; j++) {
      if (j != dati.v)
        vincoloA.addTerm(dati.costi[dati.v][j], x[dati.v][j]);
    }

    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        if (i != j)
          vincoloA.addTerm(-dati.costi[i][j] * ((double) dati.a / 100), x[i][j]);
      }
    }
    modello.addConstr(vincoloA, GRB.LESS_EQUAL, 0, "VincoloMinore_a%");
    // VincoloA -- Fine

    // VincoloB -- Inizio
    GRBLinExpr vincoloB = new GRBLinExpr();

    vincoloB.addTerm(dati.N * dati.N, x[dati.b1b2[0]][dati.b1b2[1]]);
    vincoloB.addTerm(-dati.c, x[dati.b1b2[0]][dati.b1b2[1]]);
    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < x[0].length; j++) {
        if (i != j)
          vincoloB.addTerm(dati.costi[i][j], x[i][j]);
      }
    }
    modello.addConstr(vincoloB, GRB.LESS_EQUAL, dati.N * dati.N, "VincoloB");
    // VincoloB -- Fine

    // VincoloC -- Inizio
    GRBLinExpr vincoloC = new GRBLinExpr();
    vincoloC.addTerm(1, verticiEF);
    modello.addConstr(vincoloC, GRB.GREATER_EQUAL, x[dati.d1d2[0]][dati.d1d2[1]], "VincoloC_00");

    vincoloC = new GRBLinExpr();
    vincoloC.addTerm(1, verticiEF);
    modello.addConstr(vincoloC, GRB.LESS_EQUAL, x[dati.e1e2[0]][dati.e1e2[1]], "VincoloC_01");

    vincoloC = new GRBLinExpr();
    vincoloC.addTerm(1, verticiEF);
    modello.addConstr(vincoloC, GRB.LESS_EQUAL, x[dati.f1f2[0]][dati.f1f2[1]], "VincoloC_02");

    vincoloC = new GRBLinExpr();
    vincoloC.addTerm(1, verticiEF);
    vincoloC.addTerm(-1, x[dati.e1e2[0]][dati.e1e2[1]]);
    vincoloC.addTerm(-1, x[dati.f1f2[0]][dati.f1f2[1]]);
    modello.addConstr(vincoloC, GRB.GREATER_EQUAL, -1, "VincoloC_03");
    // VincoloC -- Fine

    // VincoloD -- Inizio
    GRBLinExpr vincoloD = new GRBLinExpr();
    vincoloD.addTerm(dati.l, verticiGHI);
    modello.addConstr(vincoloD, GRB.EQUAL, costoAggiuntivo, "VincoloD_00");

    vincoloD = new GRBLinExpr();
    vincoloD.addTerm(1, verticiGHI);
    modello.addConstr(vincoloD, GRB.LESS_EQUAL, x[dati.g1g2[0]][dati.g1g2[1]], "VincoloD_01");

    vincoloD = new GRBLinExpr();
    vincoloD.addTerm(1, verticiGHI);
    modello.addConstr(vincoloD, GRB.LESS_EQUAL, x[dati.h1h2[0]][dati.h1h2[1]], "VincoloD_02");

    vincoloD = new GRBLinExpr();
    vincoloD.addTerm(1, verticiGHI);
    modello.addConstr(vincoloD, GRB.LESS_EQUAL, x[dati.i1i2[0]][dati.i1i2[1]], "VincoloD_03");

    vincoloD = new GRBLinExpr();
    vincoloD.addTerm(1, verticiGHI);
    vincoloD.addTerm(-1, x[dati.g1g2[0]][dati.g1g2[1]]);
    vincoloD.addTerm(-1, x[dati.h1h2[0]][dati.h1h2[1]]);
    vincoloD.addTerm(-1, x[dati.i1i2[0]][dati.i1i2[1]]);
    modello.addConstr(vincoloD, GRB.GREATER_EQUAL, -2, "VincoloD_04");
    // VincoloD -- Fine
  }

  private static void inizializzaMatricePercorsoOttimo() throws GRBException {
    A = new int[dati.N][dati.N];
    double[][] doubleA = modello.get(GRB.DoubleAttr.X, x);

    for (int i = 0; i < A.length; i++) {
      for (int j = 0; j < A[i].length; j++) {
        A[i][j] = (int) doubleA[i][j];
      }
    }
  }

  private static void percorsoOttimo(int col, LinkedList<Integer> percorsoOttimo) {
    for (int i = 0; i < dati.N; i++) {
      if (A[i][col] == 1) {
        percorsoOttimo.add(i);

        if (i == 0) {
          return;
        }

        percorsoOttimo(i, percorsoOttimo);
      }
    }
  }

  public static void main(String[] args) {
    try {
      reader = new Reader("assets/coppia81.txt");
      dati = reader.readFile();

      ambiente = new GRBEnv("App.log"); // Creo l'ambiente di Gurobi impostando il file dei log del programma
      ambiente.set(GRB.IntParam.OutputFlag, 0); // Disattivo l'output di default di Gurobi
      ambiente.set(GRB.IntParam.Presolve, 0); // Disattivo gli algoritmi di presolve

      // QUESITO I - Inizio
      modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato

      inizializzaVariabili();
      impostaFunzioneObiettivo();
      impostaVincoli();

      modello.update();
      modello.optimize();

      inizializzaMatricePercorsoOttimo();

      percorsoOttimo.add(0);
      percorsoOttimo(0, percorsoOttimo);

      System.out.println("GRUPPO 81\nComponenti: Brignoli Muscio\n");
      System.out.println("\nQUESITO I:");
      System.out.println(String.format("funzione obbiettivo = %.2f", modello.get(GRB.DoubleAttr.ObjVal)));
      System.out.println("ciclo ottimo 1 = " + percorsoOttimo);
      // QUESITO I - Fine

      // QUESITO II - Inizio
      int costo = (int) modello.get(GRB.DoubleAttr.ObjVal);

      modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato
      modello.set(GRB.IntParam.PoolSearchMode, 2);
      modello.set(GRB.IntParam.SolutionNumber, 1);

      inizializzaVariabili();
      impostaFunzioneObiettivo();
      impostaVincoli();

      modello.update();
      modello.optimize();

      if (costo == (int) modello.get(GRB.DoubleAttr.PoolObjVal)) {

        inizializzaMatricePercorsoOttimo();

        percorsoOttimo = new LinkedList<Integer>();
        percorsoOttimo.add(0);
        percorsoOttimo(0, percorsoOttimo);

        System.out.println("\nQUESITO II:");
        System.out.println("ciclo ottimo 2 = " + percorsoOttimo);
      } else {
        System.out.println("\nQUESITO II:");
        System.out.println("ciclo ottimo 2 = [Non esiste un ulteriore ciclo ottimo a costo uguale]");
      }
      // QUESITO II - Fine

      // QUESITO III - Inizio
      modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato
      modello.set(GRB.IntParam.PoolSearchMode, 2);
      modello.set(GRB.IntParam.SolutionNumber, 1);

      inizializzaVariabiliAggiuntive();
      impostaFunzioneObiettivoAggiuntiva();
      impostaVincoli();
      impostaVincoliAggiuntivi();

      modello.update();
      modello.optimize();

      inizializzaMatricePercorsoOttimo();

      percorsoOttimo = new LinkedList<Integer>();
      percorsoOttimo.add(0);
      percorsoOttimo(0, percorsoOttimo);

      System.out.println("\nQUESITO III:");
      System.out.println(String.format("funzione obbiettivo = %.2f", modello.get(GRB.DoubleAttr.ObjVal)));
      System.out.println("ciclo ottimo 3 = " + percorsoOttimo);
      // QUESITO III - Fine
    } catch (GRBException | IOException e) {
      e.printStackTrace();
    }
  }
}
