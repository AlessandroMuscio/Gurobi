import gurobi.*;

import java.util.ArrayList;
import java.util.Arrays;

public class QuelloBello {
/*
    private static final int M = 10; // N° emittenti televisive
    private static final int K = 8; // N° fasce orarie
    private static final int S = 84070; // Minima copertura giornaliera di spettatori da raggiungere
    private static final double omega = 0.02; // Percentuale di budget minimo spendibile per fascia
    private static final int[] beta = {3176, 2804, 3011, 3486, 2606, 2887, 3132, 3211, 3033, 2721}; // Budget massimo che ogni emittente può spendere per ogni fascia

    private static final int[][] tau = { {3, 1, 1, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 1, 1, 2, 3},
            {1, 2, 2, 3, 1, 2, 2, 1},
            {2, 2, 1, 1, 2, 1, 1, 1},
            {2, 2, 3, 2, 1, 1, 3, 3},
            {2, 2, 2, 3, 3, 1, 2, 2},
            {2, 2, 2, 3, 2, 1, 3, 1},
            {3, 3, 3, 2, 1, 3, 1, 3},
            {2, 2, 2, 2, 1, 1, 3, 2},
            {1, 2, 2, 2, 3, 2, 1, 1} }; // Minuti massimi che ogni emittente può acquistare per ogni fascia

    private static final int[][] C = { {1146,  950, 1354, 1385, 1301, 1363, 1112, 1151},
            {1026, 1293, 1107,  935, 1259, 1229, 1097, 1176},
            { 935, 1383, 1387, 1021, 1359,  919,  900, 1021},
            {1153, 1129,  994, 1133, 1099, 1372, 1055, 1003},
            {1376, 1096, 1356, 1139, 1061, 1007, 1095, 1094},
            { 957, 1248, 1055, 1332, 1336, 1100,  996, 1332},
            { 928, 1045, 1237,  908, 1036, 1368,  903, 1379},
            {1372,  919, 1394, 1268, 1010, 1352, 1088, 1343},
            {1185,  906, 1113, 1119,  923, 1335, 1075, 1284},
            {1269, 1089, 1198, 1008, 1016, 1289, 1373, 1105} }; // Costo al minuto di ogni emittente per ogni fascia

    private static final int[][] P = { { 553, 3444, 1098, 2171, 2145, 1429, 1932,  611},
            { 944,  998, 2601,  495,  431, 1807, 1334, 2080},
            {2674,  666, 3239,  583,  902, 2109, 1226, 1187},
            {1384,  905, 1206, 2178, 2571, 2573, 3380, 2904},
            {1333, 1114,  663, 1196, 1247, 3264, 3006, 2705},
            {1342, 3414, 1399, 2325, 1791, 3362, 3359, 1078},
            {1195, 3143, 2001, 3489, 2882, 2853,  527, 1682},
            {1930, 2842, 2184, 3205, 1968, 1955, 1607,  648},
            {3128, 1174, 3179, 2326, 2529,  313, 1210, 2380},
            { 521, 1357, 1848,  876, 2090, 2752, 1386, 2122} }; // Spettatori al minuto di ogni emittente per ogni fascia
*/

/*
    private static int M = 3;                  // Emittenti
    private static int K = 2;                  // Fasce orarie
    private static int S = 100;                 // Copertura giornaliera di spettatori
    private static double omega = 0.01;        // Percentuale di budget minimo per fascia sul totale
    private static int[] beta = {22, 15, 25};      // Budget massimo per ogni emittente per ogni singola fascia

    private static int[][] tau = { {5, 5},
            {5, 5},
            {5, 5}}; // Minuti massimi divisi per emittente e per fascia

    private static int[][] C = { {4, 6},
            {3, 2},
            {5, 3}};   // Costo al minuto per emittente e per fascia

    private static int[][] P = { {20, 5},
            {25, 11},
            {18, 22}};   // Spettatori al minuto per emittente e per fascia
 */

//*
    private static int M = 2;                  // Emittenti
    private static int K = 2;                  // Fasce orarie
    private static int S = 30;                 // Copertura giornaliera di spettatori
    private static double omega = 0.01;        // Percentuale di budget minimo per fascia sul totale
    private static int[] beta = {75, 35};      // Budget massimo per ogni emittente per ogni singola fascia

    private static int[][] tau = { {10, 10},
            {10, 10} }; // Minuti massimi divisi per emittente e per fascia

    private static int[][] C = { {63, 72},
            {32, 34} };   // Costo al minuto per emittente e per fascia

    private static int[][] P = { {11, 10},
            { 5,  7} };   // Spettatori al minuto per emittente e per fascia
//*/
    private static final GRBVar[] x = new GRBVar[M*K]; // Incognite del modello
    private static final GRBVar[] s = new GRBVar[M+K+3+M*K]; // Slack/Surplus per la forma standard
    private static final GRBVar[] y = new GRBVar[M+K+3+M*K]; // Variabili ausiliarie per il metodo delle II fasi

    private static GRBVar a;

    private static int indiceSlack = 0;
    private static int indiceAusiliarie = 0;

    public static void main(String[] args) {

        GRBEnv ambiente = null; // Ho messo false invece che "Progetto.log" perché Gurobi non andava se lo facevo
        try {
            ambiente = new GRBEnv(false);
            set(ambiente);

            GRBModel modello = new GRBModel(ambiente);

            aggiungiVariabili(modello);
            aggiungiFunzioneObiettivo(modello);
            aggiungiVincoli(modello);

            modello.update();
            modello.optimize();
            modello.write("QuelloBello0.lp");

            stampa(modello);

        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

    private static void set(GRBEnv ambiente) throws GRBException {
        ambiente.set(GRB.IntParam.Presolve, 0); // Disattivo il presolve
        ambiente.set(GRB.IntParam.Method, 0); // Utilizzo il simplesso primale
        ambiente.set(GRB.DoubleParam.Heuristics, 0); // Non ho ancora ben capito cosa faccia pero nel dubbio
    }

    private static void stampa(GRBModel modello) throws GRBException{

        System.out.println("GRUPPO 81\nComponenti: Brignoli Muscio\n\nQUESITO I:");
        printObj(modello);
        printOttimo(modello);

        System.out.println("\nQUESITO II:");
        printInBasis(modello);
        printCCR(modello);
    }

    private static void aggiungiVariabili(GRBModel modello) throws GRBException {

        // Aggiungo le variabili, saranno una per ogni cella delle matrici, quindi M*K
        for (int i = 0; i < x.length; i++) {
            x[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x"+(i+1));
        }

        // Aggiungo la variabile per sciogliere il modulo
        // a = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");

        // Aggiungo le variabili di slack
        for (int i = 0; i < s.length; i++) {
            s[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "s"+(i+1));
        }

        // Aggiungo le variabili ausiliarie
        for (int i = 0; i < y.length; i++) {
            y[i] = modello.addVar(0,GRB.INFINITY, 0, GRB.CONTINUOUS, "y" +(i+1));
        }
    }

    private static void aggiungiVincoli(GRBModel modello) throws GRBException {

        //Vincoli di MODULO
        GRBLinExpr vincoloModulo0 = new GRBLinExpr();
        GRBLinExpr vincoloModulo1 = new GRBLinExpr();
        for (int j = 0; j < K; j++) {
            for (int i = 0; i < M; i++) {
                vincoloModulo0.addTerm(j < K/2 ? P[i][j] : -P[i][j], x[i+j+(M-1)*j]);
                vincoloModulo1.addTerm(j < K/2 ? -P[i][j] : P[i][j], x[i+j+(M-1)*j]);
            }
        }
        /*vincoloModulo0.addTerm(1, s[indiceSlack++]);
        vincoloModulo0.addTerm(1, y[indiceAusiliarie++]);
        vincoloModulo1.addTerm(1, s[indiceSlack++]);
        vincoloModulo1.addTerm(1, y[indiceAusiliarie++]);*/
        modello.addConstr(vincoloModulo0,GRB.LESS_EQUAL, a, "Vincolo_di_modulo_0");
        modello.addConstr(vincoloModulo1,GRB.LESS_EQUAL, a, "Vincolo_di_modulo_1");

        // Vincolo di COPERTURA
        GRBLinExpr vincoloCopertura = new GRBLinExpr();
        for (int j = 0; j < K; j++) {
            for (int i = 0; i < M; i++) {
                vincoloCopertura.addTerm(P[i][j], x[i+j+(M-1)*j]);
            }
        }
        /*vincoloCopertura.addTerm(-1, s[indiceSlack++]);
        vincoloCopertura.addTerm(1, y[indiceAusiliarie++]);

         */
        modello.addConstr(vincoloCopertura,GRB.GREATER_EQUAL, S, "Vincolo_di_copertura");

        //Vincoli di COSTO
        for (int i = 0; i < M; i++) {
            GRBLinExpr vincoloCosto = new GRBLinExpr();

            for (int j = 0; j < K; j++) {
                vincoloCosto.addTerm(C[i][j], x[i+j+(M-1)*j]);
            }
            /*vincoloCosto.addTerm(1, s[indiceSlack++]);
            vincoloCosto.addTerm(1, y[indiceAusiliarie++]);

             */
            modello.addConstr(vincoloCosto,GRB.LESS_EQUAL, beta[i], "Vincolo_di_costo_" + i);
        }

        //Vincoli di CONCORRENZA
        double termineNoto = 0.0;
        for (int i = 0; i < M; i++) {
            termineNoto += beta[i];
        }
        termineNoto *= omega;

        for (int j = 0; j < K; j++) {
            GRBLinExpr vincoloConcorrenza = new GRBLinExpr();

            for (int i = 0; i < M; i++) {
                vincoloConcorrenza.addTerm(C[i][j], x[i+j+(M-1)*j]);
            }
            /*vincoloConcorrenza.addTerm(-1, s[indiceSlack++]);
            vincoloConcorrenza.addTerm(1, y[indiceAusiliarie++]);*/
            modello.addConstr(vincoloConcorrenza,GRB.GREATER_EQUAL, termineNoto, "Vincolo_di_concorrenza_" + j);
        }

        //Vincoli di TEMPO
        for (int j = 0; j < K; j++) {
            for (int i = 0; i < M; i++) {
                GRBLinExpr vincoloTempo = new GRBLinExpr();
                vincoloTempo.addTerm(1, x[i+j+(M-1)*j]);
                /*vincoloTempo.addTerm(1, s[indiceSlack++]);
                vincoloTempo.addTerm(1, y[indiceAusiliarie++]);

                 */
                modello.addConstr(vincoloTempo,GRB.LESS_EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" +j);
            }
        }

    }

    private static void aggiungiFunzioneObiettivo(GRBModel modello) throws GRBException {

        GRBLinExpr funzioneObiettivo = new GRBLinExpr();
/*
        for (int i = 0; i < y.length; i++) {
            funzioneObiettivo.addTerm(1, y[i]);
        }

 */

        a = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");
        funzioneObiettivo.addTerm(1, a);
        modello.setObjective(funzioneObiettivo, GRB.MINIMIZE);
    }

    public static void printObj(GRBModel model) throws GRBException {
        //stampo funzione obiettivo
        System.out.println(String.format("funzione obiettivo = <%.4f>", model.get(GRB.DoubleAttr.ObjVal)));
    }

    public static void printOttimo(GRBModel model) throws GRBException {

        StringBuilder str = new StringBuilder();
        GRBConstr[] constrs = model.getConstrs().clone();

        str.append("soluzione di base ottima:");

        for(GRBVar v: model.getVars()) {
            str.append(String.format("\n<%s> = <%.4f> ", v.get(GRB.StringAttr.VarName), v.get(GRB.DoubleAttr.X)));
        }

    /*
    for(int i=0; i<model.getConstrs().length; i++) {
      str.append(String.format(String.format("\n<%s> = <%.4f> ", constrs[i].get(GRB.StringAttr.VarName), constrs[i].get(GRB.DoubleAttr.X))));
    }
    */

        // CONTROLLARE PERCHÈ NON STAMPA LE SLACK

        System.out.println(str);
    }

    public static void printInBasis(GRBModel model) throws GRBException {

        StringBuilder str = new StringBuilder();
        int [] varInBase;
        varInBase = getInBaseVars(model);

        str.append("variabili in base: [");

        for(int i=0; i<varInBase.length;i++) {

            str.append(String.format("<%d> ", varInBase[i]));
        }

        str.append("]\n");
        str.append("variabili in base: [");

        String[] varInBaseName = getInBaseVarsName(model);

        for(int i=0; i<varInBaseName.length;i++) {

            str.append(String.format("<%s> ", varInBaseName[i]));
        }

        str.append("]");
        System.out.println(str);
    }

    public static void printCCR(GRBModel model) throws GRBException {

        StringBuilder str = new StringBuilder();
        double sp[] = new double[model.getConstrs().length];

        str.append("coefficienti di costo ridotto: [");

        for(var v: model.getVars()) {
            str.append(String.format("<%s = %.4f> ", v.get(GRB.StringAttr.VarName) ,v.get(GRB.DoubleAttr.RC)));
        }

    /*    for(int i=0; i<model.getConstrs().length;i++) {
            str.append(String.format("<%.4f> ", model.getConstr(i).get(GRB.DoubleAttr.Pi)));
        }*/

        str.append("]");
        System.out.println(str);
    }

    public static int[] getInBaseVars(GRBModel model) throws GRBException {

        int[] var_in_base = new int[model.getVars().length + model.getConstrs().length];
        String[] var_in_baseN = new String[model.getVars().length + model.getConstrs().length];
        int index = 0;

        //variabili
        for (var v : model.getVars()){

            var_in_base[index] = v.get(GRB.IntAttr.VBasis) == GRB.BASIC ? 1 : 0;
        }

        //slack
        for (var v : model.getConstrs()) {

            var_in_base[index] = v.get(GRB.IntAttr.CBasis) == GRB.BASIC ? 1 : 0;
        }

        return var_in_base;
    }

    public static String[] getInBaseVarsName(GRBModel model) throws GRBException {

        String[] var_in_base = new String[model.getVars().length + model.getConstrs().length];
        int index = 0;

        //variabili
        for (var v : model.getVars()){

            var_in_base[index++] = v.get(GRB.IntAttr.VBasis) == GRB.BASIC ? v.get(GRB.StringAttr.VarName) : "";
        }

        //slack
        for (var v : model.getConstrs()) {

            //var_in_base[index++] = v.get(GRB.IntAttr.CBasis) == GRB.BASIC ? v.get(GRB.StringAttr.VarName) : "";
        }

        return var_in_base;
    }
}
