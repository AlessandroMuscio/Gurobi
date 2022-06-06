import gurobi.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public class AppProva {

    private static int vCosti[] = {3, 11, 10, 10, 9, 11, 7, 8, 8,  9,  5,  11,  7,  11,  10,  11,  10,  7,  8,  7,  11,  10,  8,  5,  13,  7,  11,  9,  11,  9,  8,  8,  5,  3,  9,  7,  8,  6,  9,  9, 9, 8, 8, 8, 8, 6, 6, 10,  8,  2,  10,  7,  8,  10,  8,  8,  4,  6,  10,  8,  8,  8,  8,  11,  4,  9,  8,  10,  8,  5,  8,  8,  6,  9,  4,  5,  4,  6,  8, 4, 8, 2, 9, 4, 7, 8,  8,  8,  6,  8,  6,  8,  4,  8,  7,  10,  10,  8,  8,  6,  6,  7,  9,  9,  10,  7,  6,  5,  4,  6,  8,  7,  10,  7,  8,  6,  6, 8, 2, 6, 4, 3, 8,  8,  6,  6,  7,  8,  4,  7,  6,  8,  6,  7,  5,  7,  2,  6,  3,  6,  6,  9,  5,  6,  9,  7,  5,  7,  4,  7,  7,  4,  7,  4, 9, 6, 7, 8, 8,  4,  7,  7,  9,  9,  6,  5,  5,  5,  9,  3,  5,  5,  6,  8,  9,  9,  6,  5,  9,  6,  7,  4,  7,  7,  4,  7,  5,  9,  3,  7, 8, 2, 5, 6,  9,  6,  4,  6,  8,  6,  6,  6,  8,  8,  9,  7,  6,  4,  4,  5,  8,  8,  8,  5,  4,  7,  5,  7,  9,  6,  8,  5,  6,  7,  4, 6, 9, 8,  2,  7,  8,  8,  8,  7,  5,  6,  6,  10,  6,  6,  6,  5,  8,  3,  4,  9,  5,  8,  6,  8,  6,  6,  8,  7,  7,  4,  9,  4,  4, 7, 4,  7,  4,  4,  4,  6,  6,  7,  4,  6,  8,  7,  7,  6,  6,  2,  7,  8,  8,  6,  7,  2,  6,  3,  6,  8,  6,  6,  6,  6,  5,  2, 11,  8,  4,  8,  5,  9,  7,  8,  8,  6,  4,  10,  7,  6,  5,  9,  6,  5,  7,  9,  8,  9,  8,  4,  7,  8,  7,  6,  8,  2,  6,  7,  9,  8,  4,  8,  4,  8,  9,  6,  8,  9,  8,  7,  5,  8,  3,  7,  6,  6,  6,  10,  2,  6,  7,  7,  5,  6,  10,  9,  9,  7,  4, 6, 6, 6, 10, 6, 4, 4, 4, 8, 4, 4, 4, 7, 9, 5, 6, 8, 6, 8, 7, 6, 4, 4, 6, 6, 7, 4, 8, 2, 6, 8, 5, 10, 8, 6, 6, 2, 4, 8, 6, 6, 7, 6, 9, 6, 7, 6, 9, 6, 4, 6, 7, 7, 8, 2, 6, 2, 4, 6, 8, 8, 8, 6, 6, 6, 8, 6, 4, 2, 7, 6, 7, 6, 8, 6, 9, 2, 6, 6, 9, 9, 6, 9, 6, 6, 4, 4, 8, 7, 9, 7, 7, 4, 6, 6, 6, 5, 6, 10, 4, 7, 4, 8, 6, 7, 5, 2, 4, 7, 7, 7, 3, 7, 5, 4, 10, 6, 8, 10, 6, 9, 8, 6, 7, 8, 4, 10, 4, 9, 6, 8, 9, 9, 9, 8, 11, 10, 9, 8, 8, 6, 2, 6, 6, 3, 5, 6, 2, 8, 4, 6, 6, 5, 5, 6, 8, 6, 5, 7, 4, 7, 6, 8, 4, 4, 4, 4, 8, 8, 4, 4, 5, 7, 8, 8, 5, 8, 4, 7, 6, 4, 7, 9, 3, 7, 4, 8, 2, 6, 4, 8, 5, 4, 4, 4, 6, 6, 8, 8, 7, 7, 4, 6, 4, 5, 7, 6, 7, 4, 8, 2, 2, 6, 6, 4, 4, 7, 8, 9, 6, 8, 4, 7, 6, 2, 4, 7, 5, 6, 4, 4, 4, 2, 6, 9, 7, 6, 6, 9, 9, 7, 3, 8, 9, 9, 8, 7, 4, 6, 5, 6, 9, 2, 8, 9, 6, 4, 5, 5, 7, 8, 9, 2, 7, 6, 8, 6, 6, 4, 7, 10, 8, 8, 6, 7, 2, 3, 6, 8, 6, 4, 8, 6, 6, 6, 4, 6, 8, 2, 7, 4, 6, 2, 6, 5, 8, 9, 4, 6, 6, 7, 4, 6, 4, 7, 8, 4, 7, 4, 4, 2, 6, 6, 5, 4, 4, 7, 3, 6, 8, 6, 3, 5, 2, 5, 6, 6, 5, 6, 9, 9, 6, 7, 9, 4, 8, 5, 8, 8, 4, 8, 8, 8, 7, 4, 7, 9, 7, 8, 9, 11, 9, 8, 10, 7, 10, 6, 7, 7, 7, 8, 8, 7, 8, 4, 8, 6, 8, 6, 8, 8, 5, 6, 8, 9, 7, 6, 8, 8, 7, 9, 2, 9, 6, 5, 6, 6, 5, 4, 6, 8, 6, 6, 7, 8, 8, 7, 6, 6, 9, 5, 8, 6, 8, 5, 8, 8, 9, 6, 9, 4, 5, 8, 7, 4, 8, 7, 8, 5, 2, 6, 5, 7, 6, 6, 6, 6, 4, 6, 3, 5, 6, 6, 4, 6, 2, 5, 2, 5, 8, 7, 5, 5, 7, 7, 9, 9, 7, 7, 9, 7, 4, 7, 4, 6, 7, 4, 5, 8, 8, 2, 6, 6, 8, 4};
    //private static int vCosti[] = {1, 2, 100, 100, 100, 100, 1, 1, 100, 3};
    private static int N = 41;
    private static int[][] C = new int[N][N];
    private static double[][] A = new double[N][N];
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
    private static final GRBVar[][] x = new GRBVar[N][N];
    private static GRBVar w;
    private static GRBVar f;
    private static GRBVar k;
    private static GRBVar temp;
    /**
     * Variabile che rappresenta le incognite del modello
     */
    private static final GRBVar[] u = new GRBVar[N];

    private static void inizializzaMatrice(){
        int k = 0;

        for(int i = 0; i < N; i++){
            C[i][i] = 0;
        }

        for(int j = 0; j < N; j++){
            for(int i = j+1; i < N; i++){
                C[i][j] = vCosti[k];
                C[j][i] = vCosti[k++];
            }
        }

        /*System.out.println("Matrice costi");
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                System.out.printf("%d\t",C[i][j]);
            }
            System.out.println();
        }*/
    }

    /**
     * Metodo che inizializza le variabile, senza valorizzarle
     *
     * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
     */
    private static void inizializzaVariabili() throws GRBException {
        // Inizializzazione delle incognite del modello
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                x[i][j] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("x%02d_%02d", (i + 1), (j + 1)));
            }
        }

        for (int i = 0; i < u.length-1; i++) {
            u[i] = modello.addVar(1, N-1, 0, GRB.INTEGER, String.format("u%02d", (i + 1)));
        }
    }

    /**
     * Metodo che crea un'espressione lineare che verrà impostata come funzione
     * obbiettivo del modello
     *
     * @throws GRBException Eccezione di Gurobi, lanciata quando qualcosa va storto
     */
    private static void impostaFunzioneObiettivo() throws GRBException {
        // Creo un'espressione lineare che andrà a rappresentare la mia funzione
        // obiettivo
        GRBLinExpr funzioneObiettivo = new GRBLinExpr();

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if(i!=j)
                    funzioneObiettivo.addTerm(C[i][j], x[i][j]);
            }
        }

        modello.setObjective(funzioneObiettivo, GRB.MINIMIZE);  // Imposto come funzione obiettivo del modello l'espressione
                                                                // lineare creata dicendo che voglio minimizzarla
    }

    private static void impostaVincoli() throws GRBException {
        // VINCOLI DI MODULO //
        // Creo due espressioni lineari che andranno a rappresentare i vincoli di moduli
        GRBLinExpr vincoloDiUguaglianza0;
        GRBLinExpr vincoloDiUguaglianza1;

        // Aggiungo le variabili alle rispettive espressioni lineari
        for (int j = 0; j < x[0].length; j++) {

            vincoloDiUguaglianza0 = new GRBLinExpr();

            for(int i = 0; i < x.length; i++){
                if(i!=j)
                    vincoloDiUguaglianza0.addTerm(1, x[i][j]);
            }
            modello.addConstr(vincoloDiUguaglianza0, GRB.EQUAL, 1, "Vincolo_di_uguaglianza_0_" + j);
        }


        // Aggiungo le variabili alle rispettive espressioni lineari
        for (int i = 0; i < x.length; i++) {

            vincoloDiUguaglianza1 = new GRBLinExpr();

            for(int j = 0; j < x[0].length; j++){
                if(i!=j)
                    vincoloDiUguaglianza1.addTerm(1, x[i][j]);
            }
            modello.addConstr(vincoloDiUguaglianza1, GRB.EQUAL, 1, "Vincolo_di_uguaglianza_1_" + i);
        }

        GRBLinExpr vincoloStoCazzo;

        for(int i = 2; i < u.length; i++){

            for(int j = 2; j < u.length; j++){

                if( i != j ){

                    vincoloStoCazzo = new GRBLinExpr();

                    vincoloStoCazzo.addTerm(1, u[i-1]);
                    vincoloStoCazzo.addTerm(-1, u[j-1]);
                    vincoloStoCazzo.addTerm((N-1), x[i][j]);

                    modello.addConstr(vincoloStoCazzo, GRB.LESS_EQUAL, N-2, "Vincolo_di_stocazzo_" + i + "_" + j);

                }
            }
        }
    }

    private static void inizializzaVariabiliAggiuntive() throws GRBException {

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                x[i][j] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("x%02d_%02d", (i + 1), (j + 1)));
            }
        }

        for (int i = 0; i < u.length; i++) {
            u[i] = modello.addVar(1, N-1, 0, GRB.INTEGER, String.format("u%02d", (i + 1)));
        }

        w = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("w"));

        k = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("k"));

        f = modello.addVar(0.0, GRB.INFINITY, 0, GRB.BINARY, String.format("f"));

        temp = modello.addVar(0.0, GRB.INFINITY, 0, GRB.INTEGER, String.format("temp"));
    }

    private static void impostaFunzioneObiettivoAggiuntiva() throws GRBException {
        // Creo un'espressione lineare che andrà a rappresentare la mia funzione
        // obiettivo
        GRBLinExpr funzioneObiettivo = new GRBLinExpr();

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if(i!=j)
                    funzioneObiettivo.addTerm(C[i][j], x[i][j]);
            }
        }

        funzioneObiettivo.addTerm(1, k);

        modello.setObjective(funzioneObiettivo, GRB.MINIMIZE);  // Imposto come funzione obiettivo del modello l'espressione
        // lineare creata dicendo che voglio minimizzarla
    }

    private static void impostaVincoliAggiuntivi() throws GRBException{
        GRBLinExpr vincoloA = new GRBLinExpr();

        for (int i = 0; i < x.length; i++) {
            if(i!=dati.v)
            vincoloA.addTerm(C[i][dati.v], x[i][dati.v]);
        }

        for (int j = 0; j < x[0].length; j++) {
            if(j!=dati.v)
            vincoloA.addTerm(C[dati.v][j], x[dati.v][j]);
        }

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if(i!=j)
                    vincoloA.addTerm(-C[i][j]*((double)dati.a/100), x[i][j]);
            }
        }

        modello.addConstr(vincoloA, GRB.LESS_EQUAL, 0, "VincoloMinoreA%");


        GRBLinExpr vincoloB = new GRBLinExpr();
        vincoloB.addTerm(1, w);
        modello.addConstr(vincoloB, GRB.LESS_EQUAL, dati.c, "VincoloB");

        vincoloB = new GRBLinExpr();
        vincoloB.addTerm(1, w);
        modello.addConstr(vincoloB, GRB.LESS_EQUAL, x[dati.b1b2[0]][dati.b1b2[1]], "VincoloB_00");

        vincoloB = new GRBLinExpr();
        vincoloB.addTerm(1, w);
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if(i!=j)
                    vincoloB.addTerm(-C[i][j], x[i][j]);
            }
        }
        modello.addConstr(vincoloB, GRB.LESS_EQUAL, 0, "VincoloB_01");

        vincoloB = new GRBLinExpr();
        vincoloB.addTerm(1, w);
        vincoloB.addTerm(-1, x[dati.b1b2[0]][dati.b1b2[1]]);
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if(i!=j)
                    vincoloB.addTerm(-C[i][j], x[i][j]);
            }
        }
        modello.addConstr(vincoloB, GRB.GREATER_EQUAL, -N*N, "VincoloB_02");


        GRBLinExpr vincoloC = new GRBLinExpr();
        vincoloC.addTerm(1, f);
        modello.addConstr(vincoloC, GRB.GREATER_EQUAL, x[dati.d1d2[0]][dati.d1d2[1]], "VincoloC");

        vincoloC = new GRBLinExpr();
        vincoloC.addTerm(1, f);
        modello.addConstr(vincoloC, GRB.LESS_EQUAL, x[dati.e1e2[0]][dati.e1e2[1]], "VincoloC_00");

        vincoloC = new GRBLinExpr();
        vincoloC.addTerm(1, f);
        modello.addConstr(vincoloC, GRB.LESS_EQUAL, x[dati.f1f2[0]][dati.f1f2[1]], "VincoloC_01");

        vincoloC = new GRBLinExpr();
        vincoloC.addTerm(1, f);
        vincoloC.addTerm(-1, x[dati.e1e2[0]][dati.e1e2[1]]);
        vincoloC.addTerm(-1, x[dati.f1f2[0]][dati.f1f2[1]]);
        modello.addConstr(vincoloC, GRB.GREATER_EQUAL, -1, "VincoloC_02");


        GRBLinExpr vincoloD = new GRBLinExpr();
        vincoloD.addTerm(dati.l, k);
        modello.addConstr(vincoloD, GRB.EQUAL, temp, "VincoloD");

        vincoloD = new GRBLinExpr();
        vincoloD.addTerm(1, k);
        modello.addConstr(vincoloD, GRB.LESS_EQUAL, x[dati.g1g2[0]][dati.g1g2[1]], "VincoloD_00");

        vincoloD = new GRBLinExpr();
        vincoloD.addTerm(1, k);
        modello.addConstr(vincoloD, GRB.LESS_EQUAL, x[dati.h1h2[0]][dati.h1h2[1]], "VincoloD_01");

        vincoloD = new GRBLinExpr();
        vincoloD.addTerm(1, k);
        modello.addConstr(vincoloD, GRB.LESS_EQUAL, x[dati.i1i2[0]][dati.i1i2[1]], "VincoloD_02");

        vincoloD = new GRBLinExpr();
        vincoloD.addTerm(1, k);
        vincoloD.addTerm(-1, x[dati.g1g2[0]][dati.g1g2[1]]);
        vincoloD.addTerm(-1, x[dati.h1h2[0]][dati.h1h2[1]]);
        vincoloD.addTerm(-1, x[dati.i1i2[0]][dati.i1i2[1]]);
        modello.addConstr(vincoloD, GRB.GREATER_EQUAL, -2, "VincoloD_03");
    }

    private static void percorsoOttimo(int col, LinkedList<Integer> percorsoOttimo) {
        for (int i = 0; i < N; i++) {
            if (A[i][col] == 1){
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

            modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato

            inizializzaMatrice();
            inizializzaVariabili();
            impostaFunzioneObiettivo();
            impostaVincoli();

            modello.update();
            modello.optimize();
            modello.write("App.lp");

            A = modello.get(GRB.DoubleAttr.X, x);

            LinkedList<Integer> percorsoOttimo = new LinkedList<Integer>();
            percorsoOttimo.add(0);
            percorsoOttimo(0, percorsoOttimo);

            System.out.println(modello.get(GRB.DoubleAttr.ObjVal));
            System.out.println(percorsoOttimo);

            modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato
            modello.set(GRB.IntParam.PoolSearchMode, 2);
            modello.set(GRB.IntParam.SolutionNumber, 1);

            inizializzaMatrice();
            inizializzaVariabili();
            impostaFunzioneObiettivo();
            impostaVincoli();

            modello.update();
            modello.optimize();
            modello.write("App.lp");

            A = modello.get(GRB.DoubleAttr.X, x);

            percorsoOttimo = new LinkedList<Integer>();
            percorsoOttimo.add(0);
            percorsoOttimo(0, percorsoOttimo);

            System.out.println(percorsoOttimo);

            modello = new GRBModel(ambiente); // Creo un modello vuoto utilizzando l'ambiente precedentemente creato
            modello.set(GRB.IntParam.PoolSearchMode, 2);
            modello.set(GRB.IntParam.SolutionNumber, 1);

            inizializzaMatrice();
            inizializzaVariabiliAggiuntive();
            impostaFunzioneObiettivoAggiuntiva();
            impostaVincoli();
            impostaVincoliAggiuntivi();

            modello.update();
            modello.optimize();
            modello.write("App.lp");

            A = modello.get(GRB.DoubleAttr.X, x);

            percorsoOttimo = new LinkedList<Integer>();
            percorsoOttimo.add(0);
            percorsoOttimo(0, percorsoOttimo);

            System.out.println(percorsoOttimo);
            System.out.println(modello.get(GRB.DoubleAttr.ObjVal));

        } catch (GRBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
