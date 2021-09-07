package fr.univ_lyon1.m2ia.coloration;

import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;


import java.util.ArrayList;
import java.util.Random;


public class ProblemeEnsembleDominantMinimal {

    public static ArrayList<Double> resolutionStratOn(UndirectedGraph inputGraph) {
        double nbSolfind = 0.0;
        double dureeStratOn = 0.0;
        long start = 0;
        ArrayList<Double> resultat = new ArrayList<>();

        GraphModel model = new GraphModel();

        // Variable graphe du model
        UndirectedGraph GUB = new UndirectedGraph(model, inputGraph.getNbMaxNodes(), SetType.BITSET, false);
        UndirectedGraph GLB = new UndirectedGraph(model, inputGraph.getNbMaxNodes(), SetType.BITSET, false);
        for (int i : inputGraph.getNodes()) {
            GUB.addNode(i);
            for (int j : inputGraph.getNeighOf(i)) {
                GUB.addEdge(i, j);
            }
        }
        UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
        model.connected(subgraph).post();

        // DOMINATING SET
        BoolVar[] domSet = model.nodeSetBool(subgraph); // permet d'avoir un liste Boolean de taille nbNoeud [0;1] qui représente les noeuds du graphe
        IntVar size = model.intVar(0, inputGraph.getNbMaxNodes()); // donne comme valeur le nombre de noeud dans le graphe
        model.setObjective(Model.MINIMIZE, size); // on force à minimiser
        model.sum(domSet, "=", size).post(); // la taille de notre ensemble dominant doit être comprise en 0 et le nombre de noeud du graphe
        for (int i = 0; i < inputGraph.getNbMaxNodes(); i++) {
            int idx = 0;
            BoolVar[] cluster = new BoolVar[1 + inputGraph.getNeighOf(i).size()];
            cluster[idx++] = domSet[i];
            for (int j : inputGraph.getNeighOf(i)){
                cluster[idx++] = domSet[j];

            }
            model.or(cluster).post(); // ici on veut que tout sois supérieur à 1 dans cluster qui est

        }

        //SEARCH (domset only -> then all edges can be used)
        model.getSolver().setSearch(Search.intVarSearch(variables -> {
            int min = -1;
            int size1 = Integer.MAX_VALUE / 10;
            for (int i = 0; i < inputGraph.getNbMaxNodes(); i++) {
                if (!variables[i].isInstantiated()) { //si le noeud indice i n'est pas instancié donc qu'il n'a pas encore de valeur entre 0 et 1
                    if (size1 > inputGraph.getNeighOf(i).size()) {
                        size1 = inputGraph.getNeighOf(i).size();
                        min = i;
                    }
                }
            }
            if(min == -1){
                return null;
            }
            else{
                return variables[min];
            }
        }, new IntDomainMin(), domSet));
        int res = 0;

        int rminStratOn = Integer.MAX_VALUE;
        start = System.nanoTime();
        Solver s = model.getSolver();
        s.limitTime("12000s");
        while (model.getSolver().solve()) {
            for (int i = 0; i < inputGraph.getNbMaxNodes(); i++) {
                if (domSet[i].isInstantiatedTo(1)) {
                    res++;
                }
            }
            if (rminStratOn > res) {
                rminStratOn = res;
                long duree = System.nanoTime() - start;
                dureeStratOn = (double) duree / 1_000_000_000;
                nbSolfind ++;

            }
            res =0;
        }
        resultat.add(dureeStratOn);
        resultat.add((double) rminStratOn);
        resultat.add(nbSolfind);
        return resultat;
    }


    public static ArrayList<Double> resolutionStratOff(UndirectedGraph inputGraph) {
        double nbSolfind = 0.0;
        double dureeStratOff = 1.0;
        long start = 0;
        ArrayList<Double> resultat = new ArrayList<>();

        GraphModel model = new GraphModel();

        // Variable graphe du model
        UndirectedGraph GUB = new UndirectedGraph(model, inputGraph.getNbMaxNodes(), SetType.BITSET, false);
        UndirectedGraph GLB = new UndirectedGraph(model, inputGraph.getNbMaxNodes(), SetType.BITSET, false);
        for (int i : inputGraph.getNodes()) {
            GUB.addNode(i);
            for (int j : inputGraph.getNeighOf(i)) {
                GUB.addEdge(i, j);
            }
        }
        UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
        model.connected(subgraph).post();

        // DOMINATING SET
        BoolVar[] domSet = model.nodeSetBool(subgraph); // permet d'avoir un liste Boolean de taille nbNoeud [0;1] qui représente les noeuds du graphe
        IntVar size = model.intVar(0, inputGraph.getNbMaxNodes()); // donne comme valeur le nombre de noeud dans le graphe
        model.setObjective(Model.MINIMIZE, size); // on force à minimiser
        model.sum(domSet, "=", size).post(); // la taille de notre ensemble dominant doit être comprise en 0 et le nombre de noeud du graphe
        for (int i = 0; i < inputGraph.getNbMaxNodes(); i++) {
            int idx = 0;
            BoolVar[] cluster = new BoolVar[1 + inputGraph.getNeighOf(i).size()];
            cluster[idx++] = domSet[i];
            for (int j : inputGraph.getNeighOf(i)){
                cluster[idx++] = domSet[j];

            }
            model.or(cluster).post(); // ici on veut que tout sois supérieur à 1 dans cluster qui est
        }

        int res = 0;

        int rminStratOff = Integer.MAX_VALUE;
        start = System.nanoTime();
        Solver s = model.getSolver();
        s.limitTime("12000s");

        while (model.getSolver().solve()) {
            for (int i = 0; i < inputGraph.getNbMaxNodes(); i++) {
                if (domSet[i].isInstantiatedTo(1)) {
                    res++;
                }
            }
            if (rminStratOff > res) {
                rminStratOff = res;
                long duree = System.nanoTime() - start;
                dureeStratOff = (double) duree / 1_000_000_000;
                nbSolfind ++;

            }
            res=0;

        }
        resultat.add(dureeStratOff);
        resultat.add((double) rminStratOff);
        resultat.add(nbSolfind);
        return resultat;
    }


    public static  UndirectedGraph constructionGraphe(int nNoeud, int pourcentAddNoeud){
        int NewPourcentAddNoeud;
        int NewMalus;
        Random randValue = new Random();
        UndirectedGraph inputGraph = new UndirectedGraph(nNoeud, SetType.BITSET, true);

        //ici on crée un graphe aléatoire de n noeud pour le moment n est choisi en dur,
        for (int i = 0; i < nNoeud; i++) {
            for (int j = i + 1; j < nNoeud; j++) {
                NewMalus = 10*inputGraph.getNeighOf(i).size();

                NewPourcentAddNoeud = Math.max((pourcentAddNoeud - NewMalus), 20);
                if (randValue.nextInt(100) < NewPourcentAddNoeud) {
                    if (!inputGraph.edgeExists(j, i)) {
                        inputGraph.addEdge(i, j);
                    }
                }
            }
        }
        return inputGraph;
    }



    //plus petit ensemble dominant d'un graphe
    public static void main(String[] args) {
        int nNoeud = 10;
        int pourcentAddNoeud = 80;
        int sizeDomSetStratOn = 0;
        int sizeDomSetStratOff = 0;
        double timeStratOn = 0.0;
        double timeStratOff = 0.0;
        double nbSolfindStratOn = 0.0;
        double nbSolfindStratOff = 0.0;
        int nExperience= 2;
        UndirectedGraph grapheExperience;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 20;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 25;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 30;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 35;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 40;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 45;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 50;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");



        nNoeud = 55;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 60;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 70;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 80;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 90;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 100;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");

        nNoeud = 150;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 300;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 500;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 700;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");


        nNoeud = 1000;
        pourcentAddNoeud = 80;
        sizeDomSetStratOn = 0;
        sizeDomSetStratOff = 0;
        timeStratOn = 0.0;
        timeStratOff = 0.0;
        nbSolfindStratOn = 0.0;
        nbSolfindStratOff = 0.0;
        for (int i = 0; i < nExperience; i++) {
            grapheExperience = constructionGraphe(nNoeud, pourcentAddNoeud);
            ArrayList<Double> resStratOn = resolutionStratOn(grapheExperience);
            ArrayList<Double> resStratOff = resolutionStratOff(grapheExperience);
            timeStratOn += resStratOn.get(0);
            timeStratOff += resStratOff.get(0);
            sizeDomSetStratOn += resStratOn.get(1);
            sizeDomSetStratOff += resStratOff.get(1);
            nbSolfindStratOn += resStratOn.get(2);
            nbSolfindStratOff += resStratOff.get(2);
        }
        System.out.println("nombre de noeud : " + nNoeud);
        System.out.println("taille du domSet moyen StratOn : " + sizeDomSetStratOn/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOn : " + timeStratOn/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOn : " + nbSolfindStratOn/nExperience);
        System.out.println("taille du domSet moyen StratOff : " + sizeDomSetStratOff/nExperience);
        System.out.println("durée moyenne pour trouver le domSet stratOff : " + timeStratOff/nExperience);
        System.out.println("nombre de solution moyenne par experience trouvé stratOff : " + nbSolfindStratOff/nExperience);
        System.out.println("");
    }
}

