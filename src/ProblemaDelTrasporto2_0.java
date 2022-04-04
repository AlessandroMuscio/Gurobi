import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class ProblemaDelTrasporto2_0
{
    public static void main(String[] args)
    {
        int[] produzione = {10, 15, 25, 5};
        int[] domanda = {8, 25, 18};
        int[][] costi = {{10, 5, 15}, {12, 10, 13}, {15, 13, 13}, {10, 5, 5}};

        try
        {
            GRBEnv env = new GRBEnv("trasporto.log");
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

    private static void impostaParametri(GRBEnv env) throws GRBException
    {
        env.set(GRB.IntParam.Method, 0);
        env.set(GRB.IntParam.Presolve, 0);
    }

    private static GRBVar[][] aggiungiVariabili(GRBModel model, int[] produzione, int[] domanda) throws GRBException
    {
        GRBVar[][] xij = new GRBVar[produzione.length][domanda.length];

        for (int i = 0; i < produzione.length; i++)
        {
            for (int j = 0; j < domanda.length; j++)
            {
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
            s[i] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "y_" + i);
        }

        return s;
    }


    private static void aggiungiFunzioneObiettivo(GRBModel model, GRBVar[][] xij, int[][] costi, GRBVar[] y, int[] produzione, int[] domanda) throws GRBException
    {
        GRBLinExpr obj = new GRBLinExpr();
        GRBLinExpr obj1 = new GRBLinExpr();
        //GRBVar a = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "a");

        //funzione obiettivo del problema artificiale
		for(int i = 0; i < produzione.length + domanda.length; i++) {
			obj.addTerm(1.0, y[i]);
		}

       /* for(int i = 0; i < produzione.length; i++) {
            for(int j = 0; j < domanda.length; j++) {
                obj.addTerm(costi[i][j], xij[i][j]);
            }
        }

        model.addConstr(obj,GRB.LESS_EQUAL, a, "Vincolo fittizio");
        obj1.addTerm(1, a);
        model.setObjective(obj1);*/

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
        model.write("Trasporto2.lp");

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