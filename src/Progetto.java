import gurobi.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Progetto {

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

  private static final GRBVar[] x = new GRBVar[M*K]; // Incognite del modello

  static int dimVarSlack = 0;

  public static void main(String[] args){


    try {

      // Setto l'ambiente e creo il modello
      GRBEnv ambiente = new GRBEnv(false); // Ho messo false invece che "Progetto.log" perché Gurobi non andava se lo facevo
      ambiente.set(GRB.IntParam.Presolve, 0); // Disattivo il presolve
      ambiente.set(GRB.IntParam.Method, 0); // Utilizzo il simplesso primale
      //ambiente.set(GRB.DoubleParam.Heuristics, 0); // Non ho ancora ben capito cosa faccia pero nel dubbio

      GRBModel modello = new GRBModel(ambiente);

      // Aggiungo le variabili, saranno una per ogni cella delle matrici, quindi M*K
      for (int i = 0; i < x.length; i++) {
        x[i] = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "x"+(i+1));
      }

      //Aggiungo la variabile ausiliaria per sciogliere il modulo
      GRBVar a = modello.addVar(0.0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");

      // Aggiungo la funzione obbiettivo
      GRBLinExpr obbiettivo = new GRBLinExpr();
      obbiettivo.addTerm(1, a);
      modello.setObjective(obbiettivo, GRB.MINIMIZE);

      // Aggiungo i vincoli di modulo
      GRBLinExpr vincoloModuloZero = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          vincoloModuloZero.addTerm(j < P[0].length/2 ? P[i][j] : -P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      modello.addConstr(vincoloModuloZero, GRB.LESS_EQUAL, a, "VincoloDiModulo_0");

      GRBLinExpr vincoloModuloUno = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          vincoloModuloUno.addTerm(j < P[0].length/2 ? -P[i][j] : P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      modello.addConstr(vincoloModuloUno, GRB.LESS_EQUAL, a, "VincoloDiModulo_1");

      // Aggiungo il vincolo di copertura
      GRBLinExpr vincoloCopertura = new GRBLinExpr();
      for (int j = 0; j < P[0].length; j++) {
        for (int i = 0; i < P.length; i++) {
          vincoloCopertura.addTerm(P[i][j], x[i+j+((P.length-1)*j)]);
        }
      }
      modello.addConstr(vincoloCopertura, GRB.GREATER_EQUAL, S, "VincoloDiCopertura");

      // Aggiungo i vincoli di concorrenza e di costo
      for (int j = 0; j < C[0].length; j++) {
        for (int i = 0; i < C.length; i++) {
          GRBLinExpr vincoliConcorrenza_Costo = new GRBLinExpr();
          vincoliConcorrenza_Costo.addTerm(C[i][j], x[i+j+((P.length-1)*j)]);
          modello.addConstr(vincoliConcorrenza_Costo, GRB.GREATER_EQUAL, omega*(beta[i]*K), "VincoloDiConcorrenza_" + i + "" + j);
          modello.addConstr(vincoliConcorrenza_Costo, GRB.LESS_EQUAL, beta[i], "VincoloDiCosto_" + i + "" + j);
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
          GRBLinExpr vincoliTempo = new GRBLinExpr();
          vincoliTempo.addTerm(1.0, x[i+j+((P.length-1)*j)]);
          modello.addConstr(vincoliTempo, GRB.LESS_EQUAL, tau[i][j], "VincoloDiTempo_" + i + "" + j);
        }
      }

      // Ottimizzazione
      modello.update();
      dimVarSlack= modello.getConstrs().length + modello.getVars().length;
      modello.optimize();
      modello.write("progetto.lp");

      stampa(modello);

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
    }
  }

  private static void stampa(GRBModel model) throws GRBException{

    System.out.println("GRUPPO 81\nComponenti: Brignoli Muscio\n\nQUESITO I:");
    printObj(model);
    System.out.println("copertura raggiunta totale (spettatori) = ");
    System.out.println("tempo acquistato (minuti) = " );
    System.out.println("budget inutilizzato = ");
    printOttimo(model);

    System.out.println("\nQUESITO II:");
    printInBasis(model);
    printCCR(model);
    printMultiplaDegenere(model);
    verticeOttimo(model);

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

/*  for(int i=0; i<model.getConstrs().length; i++) {
      str.append(String.format(String.format("\n<%s> = <%.4f> ", constrs[i].get(GRB.StringAttr.VarName), constrs[i].get(GRB.DoubleAttr.X))));
    }*/

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

    str.append("]");
    System.out.println(str);
  }

  public static void printCCR(GRBModel model) throws GRBException {

    StringBuilder str = new StringBuilder();
    double sp[] = new double[model.getConstrs().length];

    str.append("coefficienti di costo ridotto: [");

    for(var v: model.getVars()) {
      str.append(String.format("<%.4f> ", v.get(GRB.DoubleAttr.RC)));
    }

    for(int i=0; i<model.getConstrs().length;i++) {
      str.append(String.format("<%.4f> ", model.getConstr(i).get(GRB.DoubleAttr.Pi)));
    }

    str.append("]");
    System.out.println(str);
  }

  public static void printMultiplaDegenere(GRBModel model) throws GRBException {

    StringBuilder str = new StringBuilder();
    boolean isMultipla= soluzioneMultiplaCheck(model);
    boolean isDegenere= soluzioneDegenereCheck(model);

    System.out.println(String.format("soluzione ottima multipla: %s\nsoluzione ottima degenere: %s",
            isMultipla ? "<Sì>" : "<No>",
            isDegenere ? "<Sì>" : "<No>")
    );
  }

  public static void verticeOttimo(GRBModel model) throws GRBException {

    StringBuilder str = new StringBuilder();
    double epsilon = 1e-5;

    ArrayList<String> vincoli_ottimo = new ArrayList<>();
    for(var v : model.getConstrs()) {
      if(Math.abs(v.get(GRB.DoubleAttr.Slack)) < epsilon)
        vincoli_ottimo.add(v.get(GRB.StringAttr.ConstrName));
    }

    str.append(String.format("vincoli vertice ottimo: %s\n\n", Arrays.toString(vincoli_ottimo.toArray())));
    System.out.println(str);
  }

  public static boolean soluzioneMultiplaCheck(GRBModel model) throws GRBException {

    double epsilon = 1e-5;
    boolean multipla = false;
    int[] var_in_base = new int[Progetto.dimVarSlack];

    int i =0;
    int index = 0;

    int count =0;

    for(i=0; i<model.getVars().length;i++) {
      if(var_in_base[i] ==1 && Math.abs(model.getVar(i).get(GRB.DoubleAttr.RC))<epsilon) {
        count++;
      }
    }

    for(index=i; index<model.getConstrs().length;index++) {
      if(var_in_base[index] ==1 && Math.abs(model.getConstr(index).get(GRB.DoubleAttr.Pi))<epsilon) {
        count++;
      }
    }

    if(count>0) {        //ho delle var/slack in base a 0
      multipla = true;
    }

    return multipla;
  }

  public static boolean soluzioneDegenereCheck(GRBModel model) throws GRBException {

    boolean degenere = false;
    double epsilon = 1e-5;
    int[] var_in_base = getInBaseVars(model);
    int i;
    int index;
    int count =0;

    for(i=0; i<model.getVars().length;i++) {
      if (var_in_base[i] == 0 && Math.abs(model.getVar(i).get(GRB.DoubleAttr.X)) < epsilon){
        count++;
      }
    }

    for(index=i; index<model.getConstrs().length;index++) {
      if(var_in_base[index] ==0 && Math.abs(model.getConstr(index).get(GRB.DoubleAttr.RHS))<epsilon) {
        count++;
      }
    }

    if(count>0) {               //ho delle var/slack in base a 0
      degenere = true;
    }

    return degenere;
  }

  public static int[] getInBaseVars(GRBModel model) throws GRBException {

    int[] var_in_base = new int[model.getVars().length + model.getConstrs().length];
    int index = 0;

    //variabili
    for (var v : model.getVars()){

      var_in_base[index++] = v.get(GRB.IntAttr.VBasis) == GRB.BASIC ? 1 : 0;
    }

    //slack
    for (var v : model.getConstrs()) {

      var_in_base[index++] = v.get(GRB.IntAttr.CBasis) == GRB.BASIC ? 1 : 0;
    }

    return var_in_base;
  }
}
