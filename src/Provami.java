import gurobi.*;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;

public class Provami {

  public static void main(String[] args) {

    int M = 2;                  // Emittenti
    int K = 2;                  // Fasce orarie
    int S = 30;                 // Copertura giornaliera di spettatori
    double omega = 0.01;        // Percentuale di budget minimo per fascia sul totale
    int[] beta = {75, 35};      // Budget massimo per ogni emittente per ogni singola fascia

    int[][] tau = { {10, 10},
                    {10, 10} }; // Minuti massimi divisi per emittente e per fascia

    int[][] C = { {63, 72},
                  {32, 34} };   // Costo al minuto per emittente e per fascia

    int[][] P = { {11, 10},
                  { 5,  7} };   // Spettatori al minuto per emittente e per fascia

    try
    {
      GRBEnv env = new GRBEnv("pubblicita.log");
      impostaParametri(env);

      GRBModel model = new GRBModel(env);

      GRBVar[][] xij = aggiungiVariabili(model, C);

      //variabili per far risolvere a Gurobi direttamente la forma standard del problema
      GRBVar[] s = aggiungiVariabiliSlackSurplus(model, C);

      //variabili per far risolvere a Gurobi il problema artificiale della I fase
     GRBVar[] y = aggiungiVariabiliAusiliarie(model, C);

      aggiungiFunzioneObiettivoAusiliaria(model, y, C);

      aggiungiVincoliDiCopertura(model, xij, P, S, s, y);
      aggiungiVincoliDiConcorrenza(model, xij, C, beta, omega, s, y);
      aggiungiVincoliDiCosto(model, xij, C, beta, s, y);
      aggiungiVincoliDiTempo(model, xij, tau, s, y);

      model.optimize();

      rimuoviY(model, y);

      model.set(GRB.IntAttr.NumObj, 0);
      model.update();

      aggiungiFunzioneObiettivo(model, xij, P);

      model.optimize();

      risolvi(model);

    } catch (GRBException e)
    {
      e.printStackTrace();
    }
  }

  private static void rimuoviY(GRBModel model, GRBVar[] y) throws GRBException {

    for(GRBVar i : y) {

      model.remove(i);
    }
  }

  private static void aggiungiVincoliDiTempo(GRBModel model, GRBVar[][] xij, int[][] tau, GRBVar[] s, GRBVar[] y) throws GRBException {

    for(int i = 0; i < tau.length; i++){

      for(int j = 0; j < tau[0].length; j++){

        GRBLinExpr expr = new GRBLinExpr();

        expr.addTerm(1, xij[i][j]);

        expr.addTerm(1.0, s[i+j+2*(tau.length*tau[0].length)+1]);
        expr.addTerm(1.0, y[i+j+2*(tau.length*tau[0].length)+1]);

        model.addConstr(expr, GRB.LESS_EQUAL, tau[i][j], "vincolo di tempo" + i + ""+ j);
      }
    }
  }

  private static void aggiungiVincoliDiCosto(GRBModel model, GRBVar[][] xij, int[][] C, int[] beta, GRBVar[] s, GRBVar[] y) throws GRBException {

    for(int i = 0; i < C.length; i++){

      for(int j = 0; j < C[0].length; j++){

        GRBLinExpr expr = new GRBLinExpr();

        expr.addTerm(C[i][j], xij[i][j]);

        expr.addTerm(1.0, s[i+j+(C.length*C[0].length)+1]);
        expr.addTerm(1.0, y[i+j+(C.length*C[0].length)+1]);

        model.addConstr(expr, GRB.LESS_EQUAL, beta[i], "vincolo di costo" + i + ""+ j);
      }
    }
  }

  private static void aggiungiVincoliDiConcorrenza(GRBModel model, GRBVar[][] xij, int[][] C, int[] beta, double omega, GRBVar[] s, GRBVar[] y) throws GRBException {

    for(int i = 0; i < C.length; i++){

      for(int j = 0; j < C[0].length; j++){

        GRBLinExpr expr = new GRBLinExpr();

        expr.addTerm((double)C[i][j]/(double)(beta[i]*C[0].length), xij[i][j]);

        expr.addTerm(-1.0, s[i+j+1]);
        expr.addTerm(1.0, y[i+j+1]);

        model.addConstr(expr, GRB.GREATER_EQUAL, omega, "vincolo di concorrenza" + i + ""+ j);
      }
    }
  }

  private static void aggiungiVincoliDiCopertura(GRBModel model, GRBVar[][] xij, int[][] P, int S, GRBVar[] s, GRBVar[] y) throws GRBException {

    GRBLinExpr expr = new GRBLinExpr();

    for(int i = 0; i < P.length; i++){

      for(int j = 0; j < P[0].length; j++){

        expr.addTerm(P[i][j], xij[i][j]);
      }
    }

    expr.addTerm(-1.0, s[0]);
    expr.addTerm(1.0, y[0]);

    model.addConstr(expr, GRB.GREATER_EQUAL, S, "vincolo di copertura");
  }

  private static void impostaParametri(GRBEnv env) throws GRBException {
    env.set(GRB.IntParam.Method, 0);
    env.set(GRB.IntParam.Presolve, 0);
  }

  private static GRBVar[][] aggiungiVariabili(GRBModel model, int[][] C) throws GRBException {

    int righe = C.length;
    int colonne = C[0].length;

    GRBVar[][] xij = new GRBVar[righe][colonne];

    for (int i = 0; i < righe; i++) {
      for (int j = 0; j < colonne; j++) {
        xij[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "xij_"+i+"_"+j);
      }
    }

    return xij;
  }

  private static GRBVar[] aggiungiVariabiliSlackSurplus(GRBModel model, int[][] C) throws GRBException {

    int lunghezza = (C.length*C[0].length*3) + 1;

    GRBVar[] s = new GRBVar[lunghezza];

    for(int i = 0; i < lunghezza; i++) {
      s[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s_" + i);
    }

    return s;
  }

  private static GRBVar[] aggiungiVariabiliAusiliarie(GRBModel model, int[][] C) throws GRBException {

    int lunghezza = (C.length*C[0].length*3) + 1;

    GRBVar[] y = new GRBVar[lunghezza];

    for(int i = 0; i < lunghezza; i++) {
      y[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "y_" + i);
    }

    return y;
  }

  private static void aggiungiFunzioneObiettivoAusiliaria(GRBModel model, GRBVar[] y, int[][] C) throws GRBException {

    int lunghezza = (C.length*C[0].length) + 1;

    GRBLinExpr obj = new GRBLinExpr();

    //funzione obiettivo del problema artificiale
    for(int i = 0; i < lunghezza; i++) {
          obj.addTerm(1.0, y[i]);
    }

    model.setObjective(obj);
    model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
  }

  private static void aggiungiFunzioneObiettivo(GRBModel model, GRBVar[][] xij, int[][] P) throws GRBException {

    GRBLinExpr obj = new GRBLinExpr();

    for(int j = 0; j < P[0].length; j++){

      for(int i = 0; i < P.length; i++){

        if(j < P[0].length/2 ){
          obj.addTerm(P[i][j], xij[i][j]);
        }
        else{
          obj.addTerm(-P[i][j], xij[i][j]);
        }
      }
    }

    model.setObjective(obj);
    model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
  }

  private static void risolvi(GRBModel model) throws GRBException
  {

    int status = model.get(GRB.IntAttr.Status);
    GRBLinExpr obj = (GRBLinExpr) model.getObjective();

    System.out.println("\n\n\nStato Ottimizzazione: "+ status + "\n");
    // 2 soluzione ottima trovata
    // 3 non esiste soluzione ammissibile (infeasible)
    // 5 soluzione illimitata
    // 9 tempo limite raggiunto

    System.out.println("GRUPPO 81");
    System.out.println("Componenti: muscio brignoli");

    System.out.println("QUESITO 1:");
    System.out.println("funzione obiettivo = " + obj.getValue());
    System.out.println("copertura raggiunta totale (spettatori) = " );
    System.out.println("tempo acquistato (minuti) = " );
    System.out.println("budget inutilizzato = ");
    System.out.println("soluzione di base ottima = ");

    for(GRBVar var : model.getVars())
    {
      //stampo il valore delle variabili e i costi ridotti associati all'ottimo
      System.out.println(var.get(StringAttr.VarName)+ ": "+ var.get(DoubleAttr.X) + " RC = " + var.get(DoubleAttr.RC));
    }

    //per stamapre a video il valore ottimo delle slack/surplus del problema
//		for(GRBConstr c: model.getConstrs())
//		{
//			System.out.println(c.get(StringAttr.ConstrName)+ ": "+ c.get(DoubleAttr.Slack));
//			//Per gurobi SLACK vuol dire sia slack che surplus
//		}

  }

}
