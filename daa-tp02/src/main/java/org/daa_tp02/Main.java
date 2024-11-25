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

    /**
     * Controls Branch-And-Bound processing
     *
     * @param academy receives an Academy object generated from the read file
     * @return a list of exercises that represents the optimal solution
     */
    private static List<Exercise> handlebranchAndBound(Academy academy) {
        List<Exercise> optimalBranchAndBoundSolution = new ArrayList<>();
        List<Exercise> currentSolution = new ArrayList<>();
        double[] minTime = { Double.MAX_VALUE };
        double[] equipmentFreeTimes = new double[academy.M];

        Map<Integer, Double> studentsElapsedTimes = new HashMap<>();

        Map<Integer, Integer> studentProgress = new HashMap<>();
        for (Student student : academy.students) {
            studentProgress.put(student.studentId, 0); 
        }

        exploreBranch(academy, currentSolution, optimalBranchAndBoundSolution, minTime, equipmentFreeTimes, studentsElapsedTimes, studentProgress);

        System.out.printf("Branch-and-Bound Lowest time: %.2f minutes%n", minTime[0]);
        System.out.println("Optimal sequence solution:");
        for (Exercise exercise : optimalBranchAndBoundSolution) {
            System.out.println(exercise);
        }

        return optimalBranchAndBoundSolution;
    }

    /**
     * Controls Branch-And-Bound processing
     *
     * @param academy         receives an Academy object generated from the read file
     * @param currentSolution the current partial solution being explored
     * @param optimalBranchAndBoundSolution the list that stores the best solution found so far
     * @param minTime         an array containing the minimum time found so far, updated during the process
     */
    private static void exploreBranch( Academy academy, List<Exercise> currentSolution, List<Exercise> optimalSolution, 
                            double[] minTime,double[] equipmentFreeTimes, Map<Integer, Double> studentsElapsedTimes, 
                            Map<Integer, Integer> studentProgress) {
        double currentElapsed = Arrays.stream(equipmentFreeTimes).max().orElse(0.0);

        double lowerBound = calculateLowerBound(
                academy, currentSolution, equipmentFreeTimes, studentsElapsedTimes, studentProgress);

        // Poda: se o lower bound for maior ou igual ao melhor tempo conhecido, não
        // explorar este ramo
        if (lowerBound >= minTime[0]) {
            return;
        }

        // Verificar se a solução está completa
        if (currentSolution.size() == academy.n) {
            if (currentElapsed < minTime[0]) {
                minTime[0] = currentElapsed;
                optimalSolution.clear();
                optimalSolution.addAll(currentSolution);
            }
            return;
        }

        for (Student student : academy.students) {
            int progressIndex = studentProgress.get(student.studentId);

            if (progressIndex >= student.exercises.size()) {
                continue;
            }

            Exercise nextExercise = student.exercises.get(progressIndex);

            int equipmentIdx = nextExercise.equipmentId - 1;

            double prevEquipmentTime = equipmentFreeTimes[equipmentIdx];
            double prevStudentTime = studentsElapsedTimes.getOrDefault(nextExercise.studentId, 0.0);

            double startTime = Math.max(prevEquipmentTime, prevStudentTime);
            double finishTime = startTime + nextExercise.duration;

            equipmentFreeTimes[equipmentIdx] = finishTime;
            studentsElapsedTimes.put(nextExercise.studentId, finishTime);
            currentSolution.add(nextExercise);
            studentProgress.put(student.studentId, progressIndex + 1);

            exploreBranch(
                    academy, currentSolution, optimalSolution, minTime,
                    equipmentFreeTimes, studentsElapsedTimes, studentProgress);

            // Retroceder estado (backtracking)
            equipmentFreeTimes[equipmentIdx] = prevEquipmentTime;
            studentsElapsedTimes.put(nextExercise.studentId, prevStudentTime);
            currentSolution.remove(currentSolution.size() - 1);
            studentProgress.put(student.studentId, progressIndex);
        }
    }

    /**
     * Controls Branch-And-Bound processing
     *
     * @param academy         receives an Academy object generated from the read file
     * @param currentSolution the current partial solution being evaluated
     * @return the lower bound estimate as a double value
     */
    private static double calculateLowerBound( Academy academy, List<Exercise> currentSolution,
                                    double[] equipmentFreeTimes, Map<Integer, Double> studentsElapsedTimes,
                                    Map<Integer, Integer> studentProgress) {

        double lowerBound = Arrays.stream(equipmentFreeTimes).max().orElse(0.0);

        for (Student student : academy.students) {
            int progressIndex = studentProgress.get(student.studentId);

            for (int i = progressIndex; i < student.exercises.size(); i++) {
                Exercise exercise = student.exercises.get(i);

                int equipmentIdx = exercise.equipmentId - 1;
                double equipmentTime = equipmentFreeTimes[equipmentIdx];
                double studentTime = studentsElapsedTimes.getOrDefault(student.studentId, 0.0);

                double estimatedStartTime = Math.max(equipmentTime, studentTime);
                lowerBound = Math.max(lowerBound, estimatedStartTime + exercise.duration);
            }
        }

        return lowerBound;
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
            final List<Exercise> branchAndBoundSolution = handlebranchAndBound(academy);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
