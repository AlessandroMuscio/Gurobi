import gurobi.*;
import gurobi.GRB.DoubleAttr;

public class Finale {

    private static final int M = 10; // N° emittenti televisive
    private static final int K = 8; // N° fasce orarie
    private static final int S = 82110; // Minima copertura giornaliera di spettatori da raggiungere
    private static final double omega = 0.02; // Percentuale di budget minimo spendibile per fascia
    private static final int[] beta = {3373, 3281, 3274, 3410, 2691, 2613, 3354, 2912, 3203, 2616}; // Budget massimo che ogni emittente può spendere per ogni fascia

    private static final int[][] tau = {    {2, 2, 2, 1, 2, 2, 1, 3},
                                            {2, 2, 1, 2, 2, 2, 2, 3},
                                            {2, 2, 2, 2, 3, 1, 2, 1},
                                            {2, 2, 2, 1, 1, 3, 2, 1},
                                            {2, 1, 3, 2, 3, 2, 2, 1},
                                            {2, 1, 2, 2, 2, 3, 3, 2},
                                            {3, 3, 1, 1, 2, 1, 2, 2},
                                            {3, 3, 2, 2, 2, 1, 3, 2},
                                            {3, 2, 2, 2, 3, 3, 1, 2},
                                            {3, 3, 3, 2, 2, 2, 3, 3}}; // Minuti massimi che ogni emittente può acquistare per ogni fascia

    private static final int[][] C = {      {1400, 1198, 1272, 1082, 936, 1130, 1280, 1249},
                                            {1069, 1358, 1194, 1227, 1344, 975, 1206, 1021},
                                            {1285, 964, 1342, 924, 1286, 1298, 1320, 925},
                                            {911, 1052, 959, 1149, 1170, 1363, 1296, 1002},
                                            {1121, 1211, 988, 1168, 1175, 1037, 1066, 1221},
                                            {929, 971, 1144, 1257, 1103, 1208, 1125, 1305},
                                            {1345, 1103, 1349, 1213, 1101, 1283, 1303, 928},
                                            {1385, 1136, 975, 1239, 1179, 1140, 1387, 1282},
                                            {918, 1054, 1281, 1337, 935, 1119, 1210, 1132},
                                            {1133, 1302, 927, 1179, 1027, 1207, 1150, 1088}}; // Costo al minuto di ogni emittente per ogni fascia

    private static final int[][] P = {      {2890, 1584, 2905, 2465, 1128, 2285, 3204, 1009},
                                            {3399, 355, 2070, 905, 814, 772, 2502, 2780},
                                            {590, 2861, 744, 3245, 2846, 2545, 2584, 633},
                                            {1332, 2682, 3264, 1558, 1162, 414, 1004, 1580},
                                            {674, 1122, 1333, 1205, 3319, 2519, 2827, 1852},
                                            {2481, 1761, 2079, 1197, 3223, 3478, 2767, 1462},
                                            {1740, 3204, 2644, 3302, 474, 2765, 2296, 2376},
                                            {3471, 1593, 2726, 1921, 1841, 1191, 2294, 1642},
                                            {900, 3035, 2951, 1440, 852, 1842, 307, 3189},
                                            {2104, 389, 3188, 365, 1931, 2563, 2770, 1844 } };

    private static final GRBVar[] x = new GRBVar[M*K]; // Incognite del modello
    private static final GRBVar[] s = new GRBVar[M+K+3+(M*K)]; // Slack/Surplus per la forma standard
    private static final GRBVar[] y = new GRBVar[M+K+3+(M*K)]; // Variabili ausiliarie per il metodo delle II fasi

    private static GRBLinExpr vincoloModulo0 = new GRBLinExpr();
    private static GRBLinExpr vincoloModulo1 = new GRBLinExpr();
    private static GRBLinExpr vincoloCopertura = new GRBLinExpr();
    private static GRBLinExpr[] vincoloCosto = new GRBLinExpr[M];
    private static GRBLinExpr[] vincoloConcorrenza = new GRBLinExpr[K];
    private static GRBLinExpr[][] vincoloTempo= new GRBLinExpr[M][K];

    private static GRBVar a;
    private static int dimVarSlack;

    private static int indiceSlack = 0;
    private static int indiceAusiliarie = 0;

    public static void main(String[] args) {

        GRBEnv ambiente = null; // Ho messo false invece che "Progetto.log" perché Gurobi non andava se lo facevo
        try {
            ambiente = new GRBEnv("QuelloBello.log");
            set(ambiente);

            GRBModel modello = new GRBModel(ambiente);

            aggiungiVariabili(modello);
            aggiungiFunzioneObiettivo(modello);
            aggiungiVincoli(modello);

            modello.write ("modello.lp");//stampo il file lp per verificare la coerenza del programma con il modello matematico

            stampa(modello);

            modello.write ("modello.sol");

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

        modello.update();
        modello.optimize();
        System.out.println("GRUPPO 81\nComponenti: Brignoli Muscio\n\nQUESITO I:");
        printObj(modello);
        printOttimo(modello);

        System.out.println("\nQUESITO II:");
        printInBasis(modello);
        printCCR(modello);

        dimVarSlack = modello.getVars().length;// assegno il n�di vincoli+ n� variabili per comodit�
      //estraggo la matrice A
        double[][] A= new double[modello.getConstrs().length][dimVarSlack];
        EstraiMatrA(modello, A);

        // estraggo b (i termini noti)
        double[][]b = new double[modello.getConstrs().length][1];
        estraiTerminiNoti(modello,b);

        System.out.println();

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                System.out.print(A[i][j] + "     \t");
            }
            System.out.println();
        }


        System.out.println();

        for(GRBVar var : modello.getVars()) {
            //stampo il valore delle variabili e i costi ridotti associati all'ottimo
            System.out.print( var.get(DoubleAttr.RC) + "       \t");
        }
    }

    private static void aggiungiVariabili(GRBModel modello) throws GRBException {

        // Aggiungo le variabili, saranno una per ogni cella delle matrici, quindi M*K
        for (int i = 0; i < x.length; i++) {
            x[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x"+(i+1));
        }

        a = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");

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
        for (int j = 0; j < K; j++) {
            for (int i = 0; i < M; i++) {
                vincoloModulo0.addTerm(j < K/2 ? P[i][j] : -P[i][j], x[i+j+(M-1)*j]);
                vincoloModulo1.addTerm(j < K/2 ? -P[i][j] : P[i][j], x[i+j+(M-1)*j]);
            }
        }
        /*vincoloModulo0.addTerm(1, s[indiceSlack++]);
        vincoloModulo0.addTerm(1, y[indiceAusiliarie++]);

        vincoloModulo1.addTerm(1, s[indiceSlack++]);
        vincoloModulo1.addTerm(1, y[indiceAusiliarie++]);

         */

        modello.addConstr(vincoloModulo0,GRB.LESS_EQUAL, a, "Vincolo_di_modulo_0");
        modello.addConstr(vincoloModulo1,GRB.LESS_EQUAL, a, "Vincolo_di_modulo_1");

        // Vincolo di COPERTURA
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < K; j++) {
                vincoloCopertura.addTerm(P[i][j], x[i+j+(M-1)*j]);
            }
        }
        /*vincoloCopertura.addTerm(-1, s[indiceSlack++]);
        vincoloCopertura.addTerm(1, y[indiceAusiliarie++]);

         */

        modello.addConstr(vincoloCopertura,GRB.GREATER_EQUAL, S, "Vincolo_di_copertura");

        //Vincoli di COSTO
        for (int i = 0; i < M; i++) {
            vincoloCosto[i] = new GRBLinExpr();

            for (int j = 0; j < K; j++) {
                vincoloCosto[i].addTerm(C[i][j], x[i+j+(M-1)*j]);
            }
            /*vincoloCosto[i].addTerm(1, s[indiceSlack++]);
            vincoloCosto[i].addTerm(1, y[indiceAusiliarie++]);


             */
            modello.addConstr(vincoloCosto[i],GRB.LESS_EQUAL, beta[i], "Vincolo_di_costo_" + i);
        }

        //Vincoli di BUDGET
        double termineNoto = 0.0;
        for (int i = 0; i < M; i++) {
            termineNoto += beta[i];
        }
        termineNoto *= omega;

        for (int j = 0; j < K; j++) {
            vincoloConcorrenza[j] = new GRBLinExpr();

            for (int i = 0; i < M; i++) {
                vincoloConcorrenza[j].addTerm(C[i][j], x[i+j+(M-1)*j]);
            }
            /*vincoloConcorrenza[j].addTerm(-1, s[indiceSlack++]);
            vincoloConcorrenza[j].addTerm(1, y[indiceAusiliarie++]);


             */
            modello.addConstr(vincoloConcorrenza[j],GRB.GREATER_EQUAL, termineNoto, "Vincolo_di_budget_" + j);
        }


        //Vincoli di TEMPO
        for (int j = 0; j < K; j++) {
            for (int i = 0; i < M; i++) {
                vincoloTempo[i][j] = new GRBLinExpr();
                vincoloTempo[i][j].addTerm(1, x[i+j+(M-1)*j]);
                /*vincoloTempo[i][j].addTerm(1, s[indiceSlack++]);
                vincoloTempo[i][j].addTerm(1, y[indiceAusiliarie++]);


                 */
                modello.addConstr(vincoloTempo[i][j],GRB.LESS_EQUAL, tau[i][j], "Vincolo_di_tempo_" + i + "" +j);
            }
        }

    }

    private static void aggiungiFunzioneObiettivo(GRBModel modello) throws GRBException {

        GRBLinExpr funzioneObiettivo = new GRBLinExpr();

        /*for (int i = 0; i < y.length ; i++) {
            funzioneObiettivo.addTerm(1, y[i]);
        }

         */

        funzioneObiettivo.addTerm(1, a);

        modello.setObjective(funzioneObiettivo, GRB.MINIMIZE);
    }

    public static void printObj(GRBModel model) throws GRBException {
        //stampo funzione obiettivo
        System.out.println(String.format("funzione obiettivo = <%.4f>", model.get(DoubleAttr.ObjVal)));
    }

    public static void printOttimo(GRBModel model) throws GRBException {

        StringBuilder str = new StringBuilder();
        GRBConstr[] constrs = model.getConstrs().clone();

        str.append("soluzione di base ottima:");

        for(GRBVar v: model.getVars()) {
            str.append(String.format("\n<%s> = <%.4f> ", v.get(GRB.StringAttr.VarName), v.get(DoubleAttr.X)));
        }

        System.out.println(str);
    }

    public static void printInBasis(GRBModel model) throws GRBException {

        StringBuilder str = new StringBuilder();
        int [] varInBase = getInBaseVars(model);
        String[] varInBaseName = getInBaseVarsName(model);

        str.append("variabili in base: [");

        for(int i=0; i<varInBase.length;i++) {

            if(varInBase[i] != 0){
                str.append(String.format("<%s> ", varInBaseName[i]));
                str.append(String.format("<%d> ", varInBase[i]));
            }
        }

        str.append("]");
        System.out.println(str);
    }

    public static void printCCR(GRBModel model) throws GRBException {

        StringBuilder str = new StringBuilder();

        str.append("coefficienti di costo ridotto: [");

        for(var v: model.getVars()) {
            str.append(String.format("<%s = %.4f> ", v.get(GRB.StringAttr.VarName) ,v.get(DoubleAttr.RC)));
        }

        str.append("]");
        System.out.println(str);
    }

    public static int[] getInBaseVars(GRBModel model) throws GRBException {

        int[] var_in_base = new int[model.getVars().length];
        int index = 0;

        //variabili
        for (var v : model.getVars()){

            var_in_base[index++] =  v.get(GRB.IntAttr.VBasis) == GRB.BASIC ? 1 : 0;
        }

        return var_in_base;
    }

    public static String[] getInBaseVarsName(GRBModel model) throws GRBException {

        String[] var_in_base = new String[model.getVars().length];
        int index = 0;

        //variabili
        for (var v : model.getVars()){

            var_in_base[index++] = v.get(GRB.StringAttr.VarName);
        }

        return var_in_base;
    }

    public static void EstraiMatrA(GRBModel model, double[][] A) throws GRBException {

        int rw = 0;
        int cl = 0;

        for (var c : model.getConstrs()) {//righe
            for (var v : model.getVars()) {//colonne
                A[rw][cl++] = model.getCoeff(c, v);
            }
            A[rw][model.getVars().length + rw] = 1;
            //termini_noti[rw] = c.get(GRB.DoubleAttr.RHS);
            rw++;
            cl = 0;
        }

    }

    public static void estraiTerminiNoti(GRBModel model, double[][] b) throws GRBException {
        for(int i =0; i< model.getConstrs().length;i++) {
            b[i] [0]= model.getConstr(i).get(DoubleAttr.RHS);
        }
    }
}
