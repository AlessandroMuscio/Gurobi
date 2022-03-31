package ro;

import gurobi.*;

public class Progetto {

    public static void main(String[] args){

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

        try {
            GRBEnv env = new GRBEnv("progetto.log");

            env.set(GRB.IntParam.Method, 0);
            env.set(GRB.IntParam.Presolve, 0);

            GRBModel model = new GRBModel(env);

            GRBVar[] x = new GRBVar[M*K];

            for (int i = 0; i < x.length; i++) {
                x[i] = model.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x"+(i+1));
            }
/*
            //variabili per far risolvere a Gurobi direttamente la forma standard del problema
            GRBVar[] s = aggiungiVariabiliSlackSurplus(model, C);

            //variabili per far risolvere a Gurobi il problema artificiale della I fase
            GRBVar[] y = aggiungiVariabiliAusiliarie(model, C);

            aggiungiFunzioneObiettivoAusiliaria(model, y, C);

            aggiungiVincoliDiCopertura(model, xij, P, S, s, y);
            aggiungiVincoliDiConcorrenza(model, xij, C, beta, omega, s, y);
            aggiungiVincoliDiCosto(model, xij, C, beta, s, y);
            aggiungiVincoliDiTempo(model, xij, tau, s, y);
*/
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    private static void aggiungiFunzioneObiettivo(GRBModel model, GRBVar[] x, int[][] P) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr();
        int temp = 0;

        for (int j = 0; j < P[0].length; j++) {
            for (int i = 0; i < P.length; i++) {
                expr.addTerm(j < P[0].length/2 ? P[i][j] : -P[i][j] , x[temp]);
                temp++;
            }
        }

        model.setObjective(expr, GRB.MINIMIZE);
    }

    public static void aggiungiVincoloDiCopertura(GRBModel model, GRBVar[] x, int[][] P, int S) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr();
        int temp = 0;

        for (int j = 0; j < P[0].length; j++) {
            for (int i = 0; i < P.length; i++) {
                expr.addTerm(P[i][j], x[temp]);
                temp++;
            }
        }

        model.addConstr(expr, GRB.GREATER_EQUAL, S, "VincoloDiCopertura");
    }
}
