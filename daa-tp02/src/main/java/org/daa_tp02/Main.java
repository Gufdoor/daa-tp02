package org.daa_tp02;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    private static class Academy {
        int M; // Equipment count
        int N; // Student count
        int n; // Exercise count
        List<Student> students;

        public Academy(int M, int N, int n) {
            this.M = M;
            this.N = N;
            this.n = n;
            this.students = new ArrayList<>();
        }

        public void addStudent(Student student) {
            students.add(student);
        }
    }

    private static class Student {
        int studentId;
        List<Exercise> exercises;

        public Student(int studentId) {
            this.studentId = studentId;
            this.exercises = new ArrayList<>();
        }

        public void addExercise(Exercise exercise) {
            exercises.add(exercise);
        }
    }

    private static class Exercise {
        int studentId;
        int equipmentId;
        double duration; // In minutes

        public Exercise(int studentId, int equipmentId, double duration) {
            this.studentId = studentId;
            this.equipmentId = equipmentId;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "Exercise: " + "studentId = " + studentId + ", equipmentId = " + equipmentId + ", duration = "
                    + duration;
        }
    }

    /**
     * Reads the input file and parses it into an Academy object.
     *
     * @param filePath the path to the input file
     * @return a list of exercises parsed from the file
     * @throws IOException if the file cannot be read
     */
    private static Academy readAcademyDataFromFile(String filePath) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(filePath));
        final int equipmentCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        final int studentCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        final int exerciseCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        final Academy academy = new Academy(equipmentCount, studentCount, exerciseCount);

        while (true) {
            final String line = reader.readLine();

            if (line == null)
                break;

            final Exercise exercise = parseReadLineToExercise(line);
            Student student = academy.students.stream().filter(auxStudent -> auxStudent.studentId == exercise.studentId)
                    .findFirst().orElse(null);

            if (student == null) {
                student = new Student(exercise.studentId);
                academy.addStudent(student);
            }

            student.addExercise(exercise);
        }

        reader.close();

        return academy;
    }

    /**
     * Parses a read line into an Exercise object.
     *
     * @param line read line with an exercise data
     * @return an Exercise object
     */
    private static Exercise parseReadLineToExercise(String line) {
        final String[] parts = line.split(" ");
        final int studentId = Integer.parseInt(parts[0]);
        final int equipmentId = Integer.parseInt(parts[1]);
        final double duration = Double.parseDouble(parts[2]);

        return new Exercise(studentId, equipmentId, duration);
    }

    /**
     * Controls brute-force permutation processing
     *
     * @param academy receives an Academy object generated from the read file
     * @return a list of exercises that represents the optimal solution
     */
    private static List<Exercise> handleBruteForcePermutation(Academy academy) {
        final List<List<Exercise>> permutations = new ArrayList<>();
        generatePermutations(academy.students, new ArrayList<>(), permutations);
        System.out.println("Generate permutations count: " + permutations.size());

        double minTime = Double.MAX_VALUE;
        List<Exercise> optimalBruteForceSolution = new ArrayList<>();

        for (List<Exercise> permutation : permutations) {
            double elapsed = simulateSchedule(academy.M, permutation);

            if (elapsed < minTime) {
                minTime = elapsed;
                optimalBruteForceSolution = permutation;
            }
        }

        System.out.printf("Lowest time: %.2f minutes%n", minTime);
        System.out.println("Optimal sequence solution:");

        for (Exercise exercise : optimalBruteForceSolution)
            System.out.println(exercise);

        return optimalBruteForceSolution;
    }

    private static List<Exercise> branchAndBound(Academy academy) {
        final List<Exercise> optimalSolution = new ArrayList<>();
        final List<Exercise> currentSolution = new ArrayList<>();
        final double[] minTime = {Double.MAX_VALUE};
    
        // Helper function to recursively explore branches
        exploreBranch(academy, currentSolution, optimalSolution, minTime);
        
        System.out.printf("Branch-and-Bound Lowest time: %.2f minutes%n", minTime[0]);
        System.out.println("Optimal sequence solution:");
        for (Exercise exercise : optimalSolution) {
            System.out.println(exercise);
        }
        
        return optimalSolution;
    }
    
    private static void exploreBranch(Academy academy, List<Exercise> currentSolution, List<Exercise> optimalSolution, double[] minTime) {
        // Calcular o tempo decorrido atual para a solução parcial
        double currentElapsed = simulateSchedule(academy.M, currentSolution);
    
        // Calcular o lower bound para a solução parcial
        double lowerBound = currentElapsed + calculateImprovedLowerBound(academy, currentSolution);
    
        // Poda: se o lower bound for maior ou igual ao melhor tempo conhecido, não explorar este ramo
        if (lowerBound >= minTime[0]) {
            return;
        }
    
        // Se a solução parcial está completa, verifique se ela é ótima
        if (currentSolution.size() == academy.n) {
            if (currentElapsed < minTime[0]) {
                minTime[0] = currentElapsed;
                optimalSolution.clear();
                optimalSolution.addAll(currentSolution);
            }
            return;
        }
    
        // Obter todos os exercícios disponíveis que ainda não foram alocados na solução
        List<Exercise> availableExercises = getAvailableExercises(academy, currentSolution);
    
        // Ordenar os exercícios com base em uma heurística composta (duração, equipamentos, etc.)
        Collections.sort(availableExercises, (ex1, ex2) -> {
            // Critério de ordenação: Duração primeiro, depois equipamento (aluno e equipamento)
            int durationComparison = Double.compare(ex1.duration, ex2.duration);
            if (durationComparison != 0) return durationComparison;
    
            // Se as durações forem iguais, ordenar pela disponibilidade do equipamento (exemplo: o equipamento mais utilizado primeiro)
            return Integer.compare(ex1.equipmentId, ex2.equipmentId);
        });
    
        // Explorar os próximos exercícios de cada aluno, ordenados por heurística
        for (Exercise nextExercise : availableExercises) {
            // Adiciona o exercício à solução atual
            currentSolution.add(nextExercise);
    
            // Chamada recursiva para explorar o próximo nível
            exploreBranch(academy, currentSolution, optimalSolution, minTime);
    
            // Retroceder: remover o exercício e continuar a exploração
            currentSolution.remove(currentSolution.size() - 1);
        }
    }
    
    private static double calculateImprovedLowerBound(Academy academy, List<Exercise> currentSolution) {
        // Estratégia de lower bound melhorada: soma dos tempos mínimos dos exercícios restantes,
        // levando em conta a disponibilidade dos equipamentos e a sequência dos alunos
    
        double lowerBound = 0.0;
    
        // Considerar a disponibilidade dos equipamentos
        double[] equipmentFreeTimes = new double[academy.M];
    
        // Itera sobre os alunos para calcular o tempo mínimo dos exercícios restantes
        for (Student student : academy.students) {
            List<Exercise> remainingExercises = new ArrayList<>(student.exercises);
            remainingExercises.removeAll(currentSolution);
    
            // Para cada exercício, calcular o tempo mínimo possível considerando os equipamentos
            for (Exercise exercise : remainingExercises) {
                int equipmentId = exercise.equipmentId - 1; // Ajustando o índice do equipamento
                lowerBound += exercise.duration; // Somando o tempo de duração do exercício
                equipmentFreeTimes[equipmentId] += exercise.duration; // Simulando a utilização do equipamento
            }
        }
    
        // Considera o tempo total de uso dos equipamentos como uma parte da estimativa de lower bound
        double maxEquipmentTime = Arrays.stream(equipmentFreeTimes).max().orElse(0.0);
        lowerBound += maxEquipmentTime;
    
        return lowerBound;
    }
    
    private static List<Exercise> getAvailableExercises(Academy academy, List<Exercise> currentSolution) {
        List<Exercise> availableExercises = new ArrayList<>();
    
        // Itera sobre os alunos para pegar os exercícios disponíveis
        for (Student student : academy.students) {
            for (Exercise exercise : student.exercises) {
                if (!currentSolution.contains(exercise)) {
                    availableExercises.add(exercise);
                }
            }
        }
    
        return availableExercises;
    }     

    /**
     * Controls approximate heuristic processing
     *
     * @param academy receives an Academy object generated from the read file
     * @return a list of exercises that represents the optimal solution
     */
    private static List<Exercise> approximateHeuristic(Academy academy) {
        List<List<Exercise>> studentsExercices = new ArrayList<>();
        for (Student student : academy.students) {
            studentsExercices.add(new ArrayList<>(student.exercises));
        }

        List<Exercise> approximateSolution = new ArrayList<>();
        double minTime = 0;

        while (!studentsExercices.isEmpty()) {
            Exercise nextExercise = null;
            minTime = Double.MAX_VALUE;

            for (List<Exercise> studentExercises : studentsExercices) {
                if (studentExercises.isEmpty())
                    continue;

                Exercise exercise = studentExercises.get(0);
                double elapsed = simulateScheduleForExercise(academy.M, approximateSolution, exercise);
                if (elapsed < minTime) {
                    minTime = elapsed;
                    nextExercise = exercise;
                }
            }

            if (nextExercise != null) {
                approximateSolution.add(nextExercise);

                for (List<Exercise> studentExercises : studentsExercices) {
                    if (!studentExercises.isEmpty() && studentExercises.get(0).equals(nextExercise)) {
                        studentExercises.remove(0);
                        break;
                    }
                }

                studentsExercices.removeIf(List::isEmpty);
            }
        }

        System.out.printf("Lowest time: %.2f minutes%n", minTime);
        System.out.println("Optimal sequence solution:");
        for (Exercise exercise : approximateSolution)
            System.out.println(exercise);

        return approximateSolution;
    }

    /**
     * Simulates the scheduling of exercises for all students and equipment.
     * It calculates the total time taken for a given schedule of exercises,
     * considering the availability
     * of each equipment and the completion times for each student.
     *
     * @param M        the number of academy equipments
     * @param schedule a list of exercises representing the sequence in which tasks
     *                 are performed
     * @return the total duration in minutes (double) when all exercises are
     *         completed
     */
    private static double simulateScheduleForExercise(int M, List<Exercise> currentSchedule, Exercise nextExercise) {
        if (M <= 0) {
            throw new IllegalArgumentException("The number of equipment must be greater than 0");
        }

        List<Exercise> timeSchedule = new ArrayList<>(currentSchedule);
        timeSchedule.add(nextExercise);
        final double timeSimulated = simulateSchedule(M, timeSchedule);

        return timeSimulated - nextExercise.duration;
    }

    /**
     * For a given group of exercises, process all combinations possible, respecting
     * that each student must complete
     * its exercises sequence in order.
     *
     * @param students        a list of Students objects containing all exercises
     *                        sequences
     * @param stepPermutation current permutation recursion step
     * @param permutations    result permutations
     */
    private static void generatePermutations(List<Student> students, List<Exercise> stepPermutation,
            List<List<Exercise>> permutations) {
        if (students.isEmpty()) {
            permutations.add(new ArrayList<>(stepPermutation));

            return;
        }

        for (Student student : students) {
            final List<Exercise> exercises = student.exercises;
            // Remaining students exercises to process
            final List<Student> remainingStudents = new ArrayList<>(students);

            remainingStudents.remove(student);
            stepPermutation.addAll(exercises);
            generatePermutations(remainingStudents, stepPermutation, permutations);
            // Remove changes to current permutation and continue to next student
            stepPermutation.subList(stepPermutation.size() - exercises.size(), stepPermutation.size()).clear();
        }
    }

    /**
     * Calculates the duration time for a given permutation.
     *
     * @param M        an academy equipments count
     * @param schedule exercises list from a permutation
     * @return minutes duration in double
     */
    private static double simulateSchedule(int M, List<Exercise> schedule) {
        if (schedule.isEmpty()) {
            return 0.0; // Caso não existam exercícios, o tempo total é zero
        }
    
        final double[] equipmentFreeTimes = new double[M];
        final Map<Integer, Double> studentsElapsedTimes = new HashMap<>();
    
        for (Exercise exercise : schedule) {
            final int equipmentIdIndex = exercise.equipmentId - 1;
            final double equipmentFreeTime = equipmentFreeTimes[equipmentIdIndex];
            final double studentsElapsedTime = studentsElapsedTimes.getOrDefault(exercise.studentId, 0.0);
            final double startTime = Math.max(equipmentFreeTime, studentsElapsedTime);
            final double finishTime = startTime + exercise.duration;
    
            equipmentFreeTimes[equipmentIdIndex] = finishTime;
            studentsElapsedTimes.put(exercise.studentId, finishTime);
        }
    
        // Get max elapsed time from students
        return Collections.max(studentsElapsedTimes.values());
    }
    

    public static void main(String[] args) {
        final String filePath = "exercises.txt";

        try {
            final Academy academy = readAcademyDataFromFile(filePath);
            final List<Exercise> optimalBruteForceSolution = handleBruteForcePermutation(academy);
            final List<Exercise> approximateHeuristicSolution = approximateHeuristic(academy);
            final List<Exercise> branchAndBoundSolution = branchAndBound(academy);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
