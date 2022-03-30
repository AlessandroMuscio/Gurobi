import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
public class App {

  public static void main(String[] args) {

    int M = 10;           // Emittenti
    int K = 8;            // Fasce orarie
    int S = 84070;        // Copertura giornaliera di spettatori
    double omega = 0.02;  // Percentuale di budget minimo per fascia sul totale
    int[] beta = {3176, 2804, 3011, 3486, 2606, 2887, 3132, 3211, 3033, 2721};      // Budget massimo per ogni emittente per ogni singola fascia

    int[][] tau = { {3, 1, 1, 2, 2, 2, 2, 2},
                    {2, 2, 2, 2, 1, 1, 2, 3},
                    {1, 2, 2, 3, 1, 2, 2, 1},
                    {2, 2, 1, 1, 2, 1, 1, 1},
                    {2, 2, 3, 2, 1, 1, 3, 3},
                    {2, 2, 2, 3, 3, 1, 2, 2},
                    {2, 2, 2, 3, 2, 1, 3, 1},
                    {3, 3, 3, 2, 1, 3, 1, 3},
                    {2, 2, 2, 2, 1, 1, 3, 2},
                    {1, 2, 2, 2, 3, 2, 1, 1} };    // Minuti massimi divisi per emittente e per fascia

    int[][] C = { {1146,  950, 1354, 1385, 1301, 1363, 1112, 1151},
                  {1026, 1293, 1107,  935, 1259, 1229, 1097, 1176},
                  { 935, 1383, 1387, 1021, 1359,  919,  900, 1021},
                  {1153, 1129,  994, 1133, 1099, 1372, 1055, 1003},
                  {1376, 1096, 1356, 1139, 1061, 1007, 1095, 1094},
                  { 957, 1248, 1055, 1332, 1336, 1100,  996, 1332},
                  { 928, 1045, 1237,  908, 1036, 1368,  903, 1379},
                  {1372,  919, 1394, 1268, 1010, 1352, 1088, 1343},
                  {1185,  906, 1113, 1119,  923, 1335, 1075, 1284},
                  {1269, 1089, 1198, 1008, 1016, 1289, 1373, 1105} };        // Costo al minuto per emittente e per fascia

    int[][] P = { { 553, 3444, 1098, 2171, 2145, 1429, 1932,  611},
                  { 944,  998, 2601,  495,  431, 1807, 1334, 2080},
                  {2674,  666, 3239,  583,  902, 2109, 1226, 1187},
                  {1384,  905, 1206, 2178, 2571, 2573, 3380, 2904},
                  {1333, 1114,  663, 1196, 1247, 3264, 3006, 2705},
                  {1342, 3414, 1399, 2325, 1791, 3362, 3359, 1078},
                  {1195, 3143, 2001, 3489, 2882, 2853,  527, 1682},
                  {1930, 2842, 2184, 3205, 1968, 1955, 1607,  648},
                  {3128, 1174, 3179, 2326, 2529,  313, 1210, 2380},
                  { 521, 1357, 1848,  876, 2090, 2752, 1386, 2122} };        // Spettatori al minuto per emittente e per fascia

    int[] produzione = {10, 15, 25, 5};
    int[] domanda = {8, 25, 18};
    int[][] costi = {{10, 5, 15}, {12, 10, 13}, {15, 13, 13}, {10, 5, 5}};

    try
    {
      GRBEnv env = new GRBEnv("pubblicita.log");
      impostaParametri(env);

      GRBModel model = new GRBModel(env);

      GRBVar[][] xij = aggiungiVariabili(model, produzione, domanda);

      //variabili per far risolvere a Gurobi direttamente la forma standard del problema
      GRBVar[] s = aggiungiVariabiliSlackSurplus(model, produzione, domanda);

      //variabili per far risolvere a Gurobi il problema artificiale della I fase
      GRBVar[] y = aggiungiVariabiliAusiliarie(model, produzione, domanda);

      aggiungiFunzioneObiettivo(model, xij, costi, y, produzione, domanda);

      aggiungiVincoliProduzione(model, xij, s, y, produzione);

      aggiungiVincoliDomanda(model, xij, s, y, produzione, domanda);

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

  private static GRBVar[][] aggiungiVariabili(GRBModel model, int[] produzione, int[] domanda) throws GRBException {

    GRBVar[][] xij = new GRBVar[produzione.length][domanda.length];

    for (int i = 0; i < produzione.length; i++) {
      for (int j = 0; j < domanda.length; j++) {
        xij[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "xij_"+i+"_"+j);
      }
    }
    return xij;
  }


  private static GRBVar[] aggiungiVariabiliSlackSurplus(GRBModel model, int[] produzione, int[] domanda) throws GRBException
  {
    GRBVar[] s = new GRBVar[produzione.length + domanda.length];

    for(int i = 0; i < produzione.length + domanda.length; i++) {
      s[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s_" + i);
    }

    return s;
  }



  private static GRBVar[] aggiungiVariabiliAusiliarie(GRBModel model, int[] produzione, int[] domanda) throws GRBException
  {
    GRBVar[] s = new GRBVar[produzione.length + domanda.length];

    for(int i = 0; i < produzione.length + domanda.length; i++) {
      s[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s_" + i);
    }

    return s;
  }


  private static void aggiungiFunzioneObiettivo(GRBModel model, GRBVar[][] xij, int[][] costi, GRBVar[] y, int[] produzione, int[] domanda) throws GRBException
  {
    GRBLinExpr obj = new GRBLinExpr();

    //funzione obiettivo del problema artificiale
//		for(int i = 0; i < produzione.length + domanda.length; i++) {
//			obj.addTerm(1.0, y[i]);
//		}

    for(int i = 0; i < produzione.length; i++) {
      for(int j = 0; j < domanda.length; j++) {
        obj.addTerm(costi[i][j], xij[i][j]);
      }
    }


    model.setObjective(obj);
    model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
  }

  private static void aggiungiVincoliProduzione(GRBModel model, GRBVar[][] xij, GRBVar[] s, GRBVar [] y, int[] produzione) throws GRBException
  {
    for (int i = 0; i < produzione.length; i++)
    {
      GRBLinExpr expr = new GRBLinExpr();

      for (int j = 0; j < xij[0].length; j++)
      {
        expr.addTerm(1, xij[i][j]);
      }

      //se voglio risolvere la forma standard
      //expr.addTerm(1.0, s[i]);

      //se voglio risolvere il problema artificale della I fase
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

      //vincolo no forma standard
      //model.addConstr(expr, GRB.EQUAL, domanda[j], "vincolo_domanda_j_"+j);

      //vincolo no forma standard
      model.addConstr(expr, GRB.GREATER_EQUAL, domanda[j], "vincolo_domanda_j_"+j);
    }
  }

  private static void risolvi(GRBModel model) throws GRBException
  {
    model.optimize();

    int status = model.get(GRB.IntAttr.Status);

    System.out.println("\n\n\nStato Ottimizzazione: "+ status + "\n");
    // 2 soluzione ottima trovata
    // 3 non esiste soluzione ammissibile (infeasible)
    // 5 soluzione illimitata
    // 9 tempo limite raggiunto

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
