import gurobi.*;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;

public class Prova {

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

    double[][] costi = { {11,          5,     10,      7},
                         {21/50.,      0,      0,      0},
                         {0,      16/35.,      0,      0},
                         {0,           0, 12/25.,      0},
                         {0,           0,      0, 17/35.},
                         {63,          0,      0,      0},
                         {0,          32,      0,      0},
                         {0,           0,     72,      0},
                         {0,           0,      0,     34},
                         {1,           0,      0,      0},
                         {0,           1,      0,      0},
                         {0,           0,      1,      0},
                         {0,           0,      0,      1} };

    double[] vincoli = {30, 1/50., 1/50., 1/50., 1/50., 75, 35, 75, 35, 10, 10, 10, 10};

    try
    {
      GRBEnv env = new GRBEnv("pubblicita.log");
      impostaParametri(env);

      GRBModel model = new GRBModel(env);

      GRBVar[][] xij = aggiungiVariabili(model, tau, C, K);

      //variabili per far risolvere a Gurobi direttamente la forma standard del problema
      GRBVar[] s = aggiungiVariabiliSlackSurplus(model, C, K);

      //variabili per far risolvere a Gurobi il problema artificiale della I fase
      GRBVar[] y = aggiungiVariabiliAusiliarie(model, C, K);

      aggiungiFunzioneObiettivo(model, xij, costi, y, C, tau, K);

      aggiungiVincoli(model, xij, costi, y, C, tau, K, vincoli);

      //aggiungiVincoliProduzione(model, xij, s, y, produzione);

      //aggiungiVincoliDomanda(model, xij, s, y, produzione, domanda);

      //model.addConstr(xij[0][1], GRB.GREATER_EQUAL, 1, "vincolo_aggiuntivo");
      /*
       * ATTENZIONE!
       * x12 >= 1 diventa
       * su carta: x12 + s = 1, s >= 0
       * Gurobi: x12 - s = 1, s <= 0
       */

      risolvi(model);

    } catch (GRBException e)
    {
      e.printStackTrace();
    }
  }

  private static void impostaParametri(GRBEnv env) throws GRBException {
    env.set(GRB.IntParam.Method, 0);
    env.set(GRB.IntParam.Presolve, 0);
  }

  private static GRBVar[][] aggiungiVariabili(GRBModel model, int[][] tau, int[][] C, int K) throws GRBException {

    int righe = (C.length*K*3) + 1;
    int colonne = tau.length*K;

    GRBVar[][] xij = new GRBVar[righe][colonne];

    for (int i = 0; i < righe; i++) {
      for (int j = 0; j < colonne; j++) {
        xij[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "xij_"+i+"_"+j);
      }
    }
    return xij;
  }


  private static GRBVar[] aggiungiVariabiliSlackSurplus(GRBModel model, int[][] C, int K) throws GRBException {

    int lunghezza = (C.length*K*3) + 1;

    GRBVar[] s = new GRBVar[lunghezza];

    for(int i = 0; i < lunghezza; i++) {
      s[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s_" + i);
    }

    return s;
  }



  private static GRBVar[] aggiungiVariabiliAusiliarie(GRBModel model, int[][] C, int K) throws GRBException {

    int lunghezza = (C.length*K) + 1;

    GRBVar[] y = new GRBVar[lunghezza];

    for(int i = 0; i < lunghezza; i++) {
      y[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "y_" + i);
    }

    return y;
  }


  private static void aggiungiFunzioneObiettivo(GRBModel model, GRBVar[][] xij, double[][] costi, GRBVar[] y, int[][] C, int[][] tau, int K) throws GRBException {

    int lunghezza = (C.length*C[0].length) + 1;
    //int righe = (C.length*K*3) + 1;
    //int colonne = tau.length*K;

    GRBLinExpr obj = new GRBLinExpr();

    //funzione obiettivo del problema artificiale
		for(int i = 0; i < lunghezza; i++) {
          obj.addTerm(1.0, y[i]);
        }

    /*for(int i = 0; i < righe; i++) {
      for(int j = 0; j < colonne; j++) {
        obj.addTerm(costi[i][j], xij[i][j]);
      }
    }*/

    model.setObjective(obj);
    model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
  }

  /*private static void aggiungiVincoliProduzione(GRBModel model, GRBVar[][] xij, GRBVar[] s, GRBVar [] y, int[] produzione) throws GRBException {
    for (int i = 0; i < produzione.length; i++) {
        GRBLinExpr expr = new GRBLinExpr();

        for (int j = 0; j < xij[0].length; j++) {
          expr.addTerm(1, xij[i][j]);
        }

      //se voglio risolvere la forma standard
      //expr.addTerm(1.0, s[i]);

      //se voglio risolvere il problema artificiale della I fase
      //expr.addTerm(1.0, y[i]);

      //vincolo forma standard
      //model.addConstr(expr, GRB.EQUAL, produzione[i], "vincolo_produzione_i_"+i);

      //vincolo no forma standard
      model.addConstr(expr, GRB.LESS_EQUAL, produzione[i], "vincolo_produzione_i_"+i);

    }
  }

  private static void aggiungiVincoliDomanda(GRBModel model, GRBVar[][] xij, GRBVar[] s, GRBVar [] y, int [] produzione, int[] domanda) throws GRBException
  {
    for (int j = 0; j < domanda.length; j++)
    {
      GRBLinExpr expr = new GRBLinExpr();

      for (int i = 0; i < xij.length; i++)
      {
        expr.addTerm(1, xij[i][j]);
      }

      //se voglio risolvere la forma standard
      //expr.addTerm(-1.0, s[produzione.length + j]);

      //se voglio risolvere il problema artificale della I fase
      //expr.addTerm(1.0, y[produzione.length + j]);

      //vincolo forma standard
      //model.addConstr(expr, GRB.EQUAL, domanda[j], "vincolo_domanda_j_"+j);

      //vincolo no forma standard
      model.addConstr(expr, GRB.GREATER_EQUAL, domanda[j], "vincolo_domanda_j_"+j);
    }
  }*/

  private static void aggiungiVincoli(GRBModel model, GRBVar[][] xij, double[][] costi, GRBVar[] y, int[][] C, int[][] tau, int K, double[] vincoli) throws GRBException  {

    int righe = (C.length*C[0].length*3) + 1;
    int colonne = tau.length*K;

    for (int i = 0; i < righe; i++) {

      GRBLinExpr expr = new GRBLinExpr();

      for (int j = 0; j < colonne; j++) {
        expr.addTerm(costi[i][j], xij[i][j]);
      }

      //se voglio risolvere la forma standard
      //expr.addTerm(-1.0, s[produzione.length + j]);

      //se voglio risolvere il problema artificale della I fase
      //expr.addTerm(1.0, y[produzione.length + j]);

      //vincolo forma standard
      //model.addConstr(expr, GRB.EQUAL, domanda[j], "vincolo_domanda_j_"+j);

      //vincolo no forma standard
      model.addConstr(expr, GRB.LESS_EQUAL, vincoli[i], "vincolo"+i);
    }
  }

  private static void risolvi(GRBModel model) throws GRBException
  {
    model.optimize();

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

    /*TODO
     * Una volta risolto il problema artificale P^ associato alla I fase, come posso sapere
     * quale è il valore della funzione obiettivo del problema originale in corrispondenza
     * dell'ottimo di P^? La soluzione è già ottima per il problema originale?
     */

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
