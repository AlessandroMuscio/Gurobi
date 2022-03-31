import gurobi.*;

public class Progetto {

  private static final int M = 2;                  // Emittenti
  private static final int K = 2;                  // Fasce orarie
  private static final int S = 30;                 // Copertura giornaliera di spettatori
  private static final double omega = 0.01;        // Percentuale di budget minimo per fascia sul totale
  private static final int[] beta = {75, 35};      // Budget massimo per ogni emittente per ogni singola fascia

  private static final int[][] tau = { {10, 10},
                                       {10, 10} }; // Minuti massimi divisi per emittente e per fascia

  private static final int[][] C = { {63, 72},
                                     {32, 34} };   // Costo al minuto per emittente e per fascia

  private static final int[][] P = { {11, 10},
                                     { 5,  7} };   // Spettatori al minuto per emittente e per fascia

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

      // Aggiungo la funzione obbiettivo
      GRBLinExpr expr0 = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          expr0.addTerm(j < P[0].length/2 ? P[i][j] : -P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      model.setObjective(expr0, GRB.MINIMIZE);

      // Aggiungo il vincolo di copertura
      GRBLinExpr expr1 = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          expr0.addTerm(P[i][j], x[i+j+((P.length-1)*j)]);
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
    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
    }
  }
}
