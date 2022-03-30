package ro;

import gurobi.*;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;

public class ProblemaDelTrasporto
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

            aggiungiFunzioneObiettivo(model, xij, costi);

            aggiungiVincoliProduzione(model, xij, produzione);

            aggiungiVincoliDomanda(model, xij, domanda);

            //model.addConstr(xij[0][0], GRB.GREATER_EQUAL , 10000, "test");
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

    private static void aggiungiFunzioneObiettivo(GRBModel model, GRBVar[][] xij, int[][] costi) throws GRBException
    {
        GRBLinExpr obj = new GRBLinExpr();

        for (int i = 0; i < costi.length; i++)
        {
            for (int j = 0; j < costi[0].length; j++)
            {
                obj.addTerm(costi[i][j], xij[i][j]);
            }
        }
        model.setObjective(obj);
        model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);
    }

    private static void aggiungiVincoliProduzione(GRBModel model, GRBVar[][] xij, int[] produzione) throws GRBException
    {
        for (int i = 0; i < produzione.length; i++)
        {
            GRBLinExpr expr = new GRBLinExpr();

            for (int j = 0; j < xij[0].length; j++)
            {
                expr.addTerm(1, xij[i][j]);
            }
            model.addConstr(expr, GRB.LESS_EQUAL, produzione[i], "vincolo_produzione_i_"+i);
        }
    }

    private static void aggiungiVincoliDomanda(GRBModel model, GRBVar[][] xij, int[] domanda) throws GRBException
    {
        for (int j = 0; j < domanda.length; j++)
        {
            GRBLinExpr expr = new GRBLinExpr();

            for (int i = 0; i < xij.length; i++)
            {
                expr.addTerm(1, xij[i][j]);
            }
            model.addConstr(expr, GRB.GREATER_EQUAL, domanda[j], "vincolo_domanda_j_"+j);
        }
    }

    private static void risolvi(GRBModel model) throws GRBException
    {
        model.optimize();

        int status = model.get(GRB.IntAttr.Status);

        System.out.println("\n\n\nStato Ottimizzazione: "+ status);
        // 2 soluzione ottima trovata
        // 3 non esiste soluzione ammissibile (infeasible)
        // 5 soluzione illimitata
        // 9 tempo limite raggiunto


        for(GRBVar var : model.getVars())
        {
            System.out.println(var.get(StringAttr.VarName)+ ": "+ var.get(DoubleAttr.X));
        }
    }
}