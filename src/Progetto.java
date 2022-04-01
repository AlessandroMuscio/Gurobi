import gurobi.*;

public class Progetto {

  private static final int M = 10;           // Emittenti
  private static final int K = 8;            // Fasce orarie
  private static final int S = 84070;        // Copertura giornaliera di spettatori
  private static final double omega = 0.02;  // Percentuale di budget minimo per fascia sul totale
  private static final int[] beta = {3176, 2804, 3011, 3486, 2606, 2887, 3132, 3211, 3033, 2721};      // Budget massimo per ogni emittente per ogni singola fascia

  private static final int[][] tau = { {3, 1, 1, 2, 2, 2, 2, 2},
          {2, 2, 2, 2, 1, 1, 2, 3},
          {1, 2, 2, 3, 1, 2, 2, 1},
          {2, 2, 1, 1, 2, 1, 1, 1},
          {2, 2, 3, 2, 1, 1, 3, 3},
          {2, 2, 2, 3, 3, 1, 2, 2},
          {2, 2, 2, 3, 2, 1, 3, 1},
          {3, 3, 3, 2, 1, 3, 1, 3},
          {2, 2, 2, 2, 1, 1, 3, 2},
          {1, 2, 2, 2, 3, 2, 1, 1} };    // Minuti massimi divisi per emittente e per fascia

  private static final int[][] C = { {1146,  950, 1354, 1385, 1301, 1363, 1112, 1151},
          {1026, 1293, 1107,  935, 1259, 1229, 1097, 1176},
          { 935, 1383, 1387, 1021, 1359,  919,  900, 1021},
          {1153, 1129,  994, 1133, 1099, 1372, 1055, 1003},
          {1376, 1096, 1356, 1139, 1061, 1007, 1095, 1094},
          { 957, 1248, 1055, 1332, 1336, 1100,  996, 1332},
          { 928, 1045, 1237,  908, 1036, 1368,  903, 1379},
          {1372,  919, 1394, 1268, 1010, 1352, 1088, 1343},
          {1185,  906, 1113, 1119,  923, 1335, 1075, 1284},
          {1269, 1089, 1198, 1008, 1016, 1289, 1373, 1105} };        // Costo al minuto per emittente e per fascia

  private static final int[][] P = { { 553, 3444, 1098, 2171, 2145, 1429, 1932,  611},
          { 944,  998, 2601,  495,  431, 1807, 1334, 2080},
          {2674,  666, 3239,  583,  902, 2109, 1226, 1187},
          {1384,  905, 1206, 2178, 2571, 2573, 3380, 2904},
          {1333, 1114,  663, 1196, 1247, 3264, 3006, 2705},
          {1342, 3414, 1399, 2325, 1791, 3362, 3359, 1078},
          {1195, 3143, 2001, 3489, 2882, 2853,  527, 1682},
          {1930, 2842, 2184, 3205, 1968, 1955, 1607,  648},
          {3128, 1174, 3179, 2326, 2529,  313, 1210, 2380},
          { 521, 1357, 1848,  876, 2090, 2752, 1386, 2122} };        // Spettatori al minuto per emittente e per fascia

  private static final GRBVar[] x = new GRBVar[M*K];

  public static void main(String[] args){
    try {
      // Setto l'ambiente e creo il modello
      GRBEnv env = new GRBEnv(false);
      env.set(GRB.IntParam.Presolve, 0); // Disattivo il presolve
      env.set(GRB.IntParam.Method, 0); // Utilizzo il simplesso primale
      env.set(GRB.DoubleParam.Heuristics, 0); // Non ho ancora ben capito cosa faccia pero nel dubbio

      GRBModel model = new GRBModel(env);

      // Aggiungo le variabili, saranno una per ogni cella delle matrici, quindi M*K
      for (int i = 0; i < x.length; i++) {
        x[i] = model.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x"+(i+1));
      }

      //Aggiungo la variabile ausiliaria per sciogliere il modulo
      GRBVar a = model.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");
/*
      // Aggiungo la funzione obbiettivo
      GRBLinExpr expr0 = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          expr0.addTerm(j < P[0].length/2 ? P[i][j] : -P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      model.setObjective(expr0, GRB.MINIMIZE);*/

      // Aggiungo la funzione obbiettivo
      GRBLinExpr expr = new GRBLinExpr();
      expr.addTerm(1, a);
      model.setObjective(expr, GRB.MINIMIZE);

      // Aggiungo i vincoli di modulo
      GRBLinExpr expr0 = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          expr0.addTerm(j < P[0].length/2 ? P[i][j] : -P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      model.addConstr(expr0, GRB.LESS_EQUAL, a, "VincoloDiModulo");

      expr0 = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          expr0.addTerm(j < P[0].length/2 ? -P[i][j] : P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      model.addConstr(expr0, GRB.LESS_EQUAL, a, "VincoloDiModulo");

      // Aggiungo il vincolo di copertura
      GRBLinExpr expr1 = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          expr1.addTerm(P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      model.addConstr(expr1, GRB.GREATER_EQUAL, S, "VincoloDiCopertura");

      // Aggiungo i vincoli di concorrenza e di costo
      for (int j = 0; j < C[0].length; j++) {
        for (int i = 0; i < C.length; i++) {
          GRBLinExpr expr2 = new GRBLinExpr();
          expr2.addTerm(C[i][j], x[i+j+((P.length-1)*j)]);
          model.addConstr(expr2, GRB.GREATER_EQUAL, omega*(beta[i]*K), "VincoloDiConcorrenza_" + i + "" + j);
          model.addConstr(expr2, GRB.LESS_EQUAL, beta[i], "VincoloDiCosto_" + i + "" + j);
        }
      }

      // Nel farlo ho notato che l'espressione lineare era la stessa cosa
      /*for (int j = 0; j < C[0].length; j++) {
        for (int i = 0; i < C.length; i++) {
          GRBLinExpr expr3 = new GRBLinExpr();
          expr3.addTerm(C[i][j], x[i+j+((P.length-1)*j)]);
          model.addConstr(expr3, GRB.LESS_EQUAL, beta[i], "VincoloDiCosto_" + i + "" + j);
        }
      }*/
      
      // Aggiungo i vincoli di tempo
      for (int j = 0; j < C[0].length; j++) {
        for (int i = 0; i < C.length; i++) {
          GRBLinExpr expr3 = new GRBLinExpr();
          expr3.addTerm(1.0, x[i+j+((P.length-1)*j)]);
          model.addConstr(expr3, GRB.LESS_EQUAL, tau[i][j], "VincoloDiTempo_" + i + "" + j);
        }
      }

      // Ottimizzazione
      model.update();
      model.optimize();
      model.write("progetto.lp");

      stampa(model);

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
    }
  }

  private static void stampa(GRBModel model) throws GRBException{

    GRBLinExpr obbiettivo = (GRBLinExpr) model.getObjective();

    int status = model.get(GRB.IntAttr.Status);
    System.out.println("\n\n\nStato Ottimizzazione: "+ status + "\n");

    System.out.println("GRUPPO 81\nComponenti: Brignoli Muscio\n\nQUESITO I:");
    System.out.println(String.format("funzione obbiettivo = %.4f", ((GRBLinExpr) model.getObjective()).getValue()));
    System.out.println("copertura raggiunta totale (spettatori) = ");
    System.out.println("tempo acquistato (minuti) = " );
    System.out.println("budget inutilizzato = ");
    System.out.println("soluzione di base ottima:");

    for (GRBVar x : model.getVars()) {
      System.out.println(String.format("%s = %.4f", x.get(GRB.StringAttr.VarName), x.get(GRB.DoubleAttr.X)));
    }
  }
}
