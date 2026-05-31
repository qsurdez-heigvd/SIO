package ch.heig.sio.lab3.groupI;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

/**
 * SIO - 2025-2026, travail pratique n° 3
 * Modélisation et résolution d'un problème d'affectation de travaux de bachelor.'
 *
 * @author
 * @author
 */
public class TB_ProjectAssignment {
    static {
        // Chargement des bibliothèques natives
        Loader.loadNativeLibraries();
    }

    private static final double infinity = MPSolver.infinity();

    public static void main(String[] args) {

        int N_PROFS = 0; // nombre de profs
        int N_STUDENTS = 0; // nombre d'étudiants
        int N_PROJECTS = 0; // nombre de projets

        int MAX_ENCADREMENT = 3;  // valuers possibles : 4, 3, 2

        int[] prof_project = null;  // projectid -> profid
        TreeMap<String, Integer> prefs_Student = new TreeMap<>(); // studentid_projectid -> pref

        String FILENAME_PROJECTS = "./data/projects.txt";
        String FILENAME_PREFERENCES = "./data/preferences.txt";

        // lecture de projects.txt
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME_PROJECTS), StandardCharsets.UTF_8))) {

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] s = line.split("\\s+");
                if (count == 0) {
                    // première ligne : N_PROJETS N_PROFS
                    N_PROJECTS = Integer.parseInt(s[0]);
                    N_PROFS = Integer.parseInt(s[1]);
                    prof_project = new int[N_PROJECTS];
                    count++;
                    continue;
                }

                // stockage du couple (projet,prof) : prof_project[project_id] = prof_id
                prof_project[Integer.parseInt(s[0])] = Integer.parseInt(s[1]);
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // lecture de preferences.txt
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FILENAME_PREFERENCES), StandardCharsets.UTF_8))) {

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] s = line.split("\\s+");
                if (count == 0) {
                    // première ligne : N_STUDENTS
                    N_STUDENTS = Integer.parseInt(s[0]);
                    count++;
                    continue;
                }
                int mot1 = Integer.parseInt(s[0]); // studentid
                int mot2 = Integer.parseInt(s[1]); // projectid
                int mot3 = Integer.parseInt(s[2]); // preference
                String key = mot1 + "_" + mot2; // studentid_projectid
                // stockage de la clé studentid_projectid avec sa valeur preference
                prefs_Student.put(key, mot3);
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Nombre de projets  : " + N_PROJECTS);
        System.out.println("Nombre de profs    : " + N_PROFS);
        System.out.println("Nombre d'étudiants : " + N_STUDENTS);

        // Création d'un solveurMI
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Impossible de créer le solveur SCIP");
        }
        assert solver != null;

        // prefs_Student, préférence des étudiants pour chaque projet
        // stockage de la clé studentid_projectid avec sa valeur preference

        // prof_project, projet lead par prof
        // stockage du couple (projet,prof) : prof_project[project_id] = prof_id

        // Partie 1

        // Variables

        // Matrice de variable booléenne avec N_Etudiant en i et N_Projets en j
        MPVariable[][] xvars = new MPVariable[N_STUDENTS][N_PROJECTS];
        // Création d'une variable binaire (booléenne) pour chaque paire (étudiant, projet)
        // valant 1 si l'étudiant est assigné au projet, 0 sinon
        for (int i = 0; i < N_STUDENTS; i++) {
            for (int j = 0; j < N_PROJECTS; j++) {
                xvars[i][j] = solver.makeBoolVar(i + "_" + j);
            }
        }

        // Fonction objectif
        MPObjective objective = solver.objective();
        for (int i = 0; i < N_STUDENTS; i++) {
            for (int j = 0; j < N_PROJECTS; j++) {
                if (prefs_Student.get(xvars[i][j].name()) != null) {
                    objective.setCoefficient(xvars[i][j], prefs_Student.get(xvars[i][j].name()));
                }
            }
        }
        objective.setMaximization();

        // Contraintes

        // Unicité du projet assigné par étudiant
        for (int i = 0; i < N_STUDENTS; i++) {
            MPConstraint c1 = solver.makeConstraint(1, 1, "c_student" + i);
            for (int j = 0; j < N_PROJECTS; j++) {
                c1.setCoefficient(xvars[i][j], 1);
            }
        }

        // Projet peut être affecté au plus une fois
        for (int j = 0; j < N_PROJECTS; j++) {
            MPConstraint c2 = solver.makeConstraint(-infinity, 1, "c_project" + j);
            for (int i = 0; i < N_STUDENTS; i++) {
                c2.setCoefficient(xvars[i][j], 1);
            }
        }

        // Profs ne peuvent pas encadrer plus de MAX_ENCADREMENT projets
        for (int p = 0; p < N_PROFS; p++) {
            MPConstraint c3 = solver.makeConstraint(-infinity, MAX_ENCADREMENT, "c_prof" + p);
            for (int i = 0; i < N_STUDENTS; i++) {
                for (int j = 0; j < N_PROJECTS; j++) {
                    if (prof_project[j] == p) {
                        c3.setCoefficient(xvars[i][j], 1);
                    }
                }
            }
        }

        MPSolver.ResultStatus resultStatus = solver.solve();
        // Stock les assignements optimaux pour partie 2
        int[] optimalAssignments = new int[N_STUDENTS];

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.printf("Solution optimale trouvée pour max encadrement %d :\n", MAX_ENCADREMENT);
            System.out.printf("Somme maximale des préférences = %d%n", (int) objective.value());
            for (int i = 0; i < N_STUDENTS; i++) {
                for (int j = 0; j < N_PROJECTS; j++) {
                    if (xvars[i][j].solutionValue() > 0.5) {
                        System.out.printf("%s = %.3f%n", xvars[i][j].name(), xvars[i][j].solutionValue());
                        optimalAssignments[i] = j;
                    }
                }
            }

            try (PrintWriter writer = new PrintWriter("solution_enc" + MAX_ENCADREMENT + "_partie1.txt")) {
                for (int i = 0; i < N_STUDENTS; i++) {
                    for (int j = 0; j < N_PROJECTS; j++) {
                        if (xvars[i][j].solutionValue() > 0.5) {
                            writer.printf("%d %d\n", i, j);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Le solveur n’a pas réussi à résoudre le modèle (status = " + resultStatus + ").");
        }

        // Partie 2

        // Maximiser le nombre d'affectations de la partie 1 en sachant que de toute manière au moins
        // 3 étudiants vont voir leur affectation changée comme 3 projets ne sont plus disponibles !

        // Ajout des contraintes pour les projets retirés
        int[] unavailableProjects = {68, 123, 151};

        for (int projectId : unavailableProjects) {
            MPConstraint c_unavailable = solver.makeConstraint(0, 0, "c_unavailable" + projectId);
            for (int i = 0; i < N_STUDENTS; i++) {
                c_unavailable.setCoefficient(xvars[i][projectId], 1);
            }
        }

        // On ne peut assigné à un étudiant qu'un projet où il a émis une préférence
        for (int i = 0; i < N_STUDENTS; i++) {
            for (int j = 0; j < N_PROJECTS; j++) {
                if (prefs_Student.get(xvars[i][j].name()) == null) {
                    MPConstraint c_notAssignable = solver.makeConstraint(0, 0, "no_pref" + i + j);
                    c_notAssignable.setCoefficient(xvars[i][j], 1);
                }
            }
        }


        // Reset la fonction objectif
        objective.clear();

        // Nouvelle fonction objectif pour maximiser les affectations inchangées
        for (int i = 0; i < N_STUDENTS; i++) {
            int optimalAssignment = optimalAssignments[i];
            objective.setCoefficient(xvars[i][optimalAssignment], 1);
        }

        objective.setMaximization();

        resultStatus = solver.solve();
        int retainedAssignments = 0;

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            retainedAssignments = (int) objective.value();
            System.out.print("\nPartie 2 - Solution avec projets 68, 123, 151 indisponibles:\n");
            System.out.printf("Nombre d'étudiants conservant leur affectation: %d sur %d\n",
                    retainedAssignments, N_STUDENTS);

            // Write solution to file
            try (PrintWriter writer = new PrintWriter("solution_partie2.txt")) {
                for (int i = 0; i < N_STUDENTS; i++) {
                    for (int j = 0; j < N_PROJECTS; j++) {
                        if (xvars[i][j].solutionValue() > 0.5) {
                            writer.printf("%d %d\n", i, j);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Partie 3

        // Reset fonction objectif
        objective.clear();

        // Réutilisation de la fonction objectif part1
        for (int i = 0; i < N_STUDENTS; i++) {
            for (int j = 0; j < N_PROJECTS; j++) {
                if (prefs_Student.get(xvars[i][j].name()) != null) {
                    objective.setCoefficient(xvars[i][j], prefs_Student.get(xvars[i][j].name()));
                }
            }
        }

        objective.setMaximization();

        // Ajouter la contrainte pour garder le nombre optimal d'assignation
        MPConstraint c_retain = solver.makeConstraint(retainedAssignments, retainedAssignments, "c_retain_optimal");
        for (int i = 0; i < N_STUDENTS; i++) {
            int optimalAssignment = optimalAssignments[i];
            c_retain.setCoefficient(xvars[i][optimalAssignment], 1);
        }


        resultStatus = solver.solve();

        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.print("Solution optimale trouvée avec maintien du nombre max  :\n");
            System.out.printf("Nombre d'affectations conservées: %d sur %d\n", retainedAssignments, N_STUDENTS);
            System.out.printf("Somme maximale des préférences = %d%n", (int) objective.value());
            for (int i = 0; i < N_STUDENTS; i++) {
                for (int j = 0; j < N_PROJECTS; j++) {
                    if (xvars[i][j].solutionValue() > 0.5) {
                        System.out.printf("%s = %.3f%n", xvars[i][j].name(), xvars[i][j].solutionValue());
                        optimalAssignments[i] = j;
                    }
                }
            }

            try (PrintWriter writer = new PrintWriter("solution_partie3.txt")) {
                for (int i = 0; i < N_STUDENTS; i++) {
                    for (int j = 0; j < N_PROJECTS; j++) {
                        if (xvars[i][j].solutionValue() > 0.5) {
                            writer.printf("%d %d\n", i, j);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Le solveur n’a pas réussi à résoudre le modèle (status = " + resultStatus + ").");
        }




    }

}
