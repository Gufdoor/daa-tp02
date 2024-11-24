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
            return "Exercise: " + "studentId = " + studentId + ", equipmentId = " + equipmentId + ", duration = " + duration;
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

            if (line == null) break;

            final Exercise exercise = parseReadLineToExercise(line);
            Student student = academy.students.stream().filter(auxStudent -> auxStudent.studentId == exercise.studentId).findFirst().orElse(null);

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
     * For a given group of exercises, process all combinations possible, respecting that each student must complete
     * its exercises sequence in order.
     *
     * @param students a list of Students objects containing all exercises sequences
     * @param stepPermutation current permutation recursion step
     * @param permutations result permutations
     */
    private static void generatePermutations(List<Student> students, List<Exercise> stepPermutation, List<List<Exercise>> permutations) {
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
     * @param M an academy equipments count
     * @param schedule exercises list from a permutation
     * @return minutes duration in double
     */
    private static double simulateSchedule(int M, List<Exercise> schedule) {
        final double[] equipmentFreeTimes = new double[M];
        final Map<Integer, Double> studentsElapsedTimes = new HashMap<>();

        for (Exercise exercise : schedule) {
            final int equipmentIdIndex = exercise.equipmentId - 1;
            final double equipmentFreeTime = equipmentFreeTimes[equipmentIdIndex];
            final double studentsElapsedTime = studentsElapsedTimes.getOrDefault(exercise.studentId, 0.0);
            final double startTime = Math.max(equipmentFreeTime, studentsElapsedTime);
            final double finishTime = startTime + exercise.duration;

            equipmentFreeTimes[equipmentIdIndex] = finishTime;
            System.out.println("Equipment " + exercise.equipmentId + " time: " + finishTime + " minutes");
            studentsElapsedTimes.put(exercise.studentId, finishTime);
        }

        // Get max elapsed time from students
        return Collections.max(studentsElapsedTimes.values());
    }

    public static void main(String[] args) {
        final String filePath = "exercises.txt";

        try {
            final Academy academy = readAcademyDataFromFile(filePath);
            final List<List<Exercise>> permutations = new ArrayList<>();
            generatePermutations(academy.students, new ArrayList<>(), permutations);
            System.out.println("Generate permutations count: " + permutations.size());

            double minTime = Double.MAX_VALUE;
            List<Exercise> optimalExercisesOrder = new ArrayList<>();

            for (List<Exercise> permutation : permutations) {
                double elapsed = simulateSchedule(academy.M, permutation);

                if (elapsed < minTime) {
                    minTime = elapsed;
                    optimalExercisesOrder = permutation;
                }
            }

            System.out.printf("Menor tempo total: %.2f minutos%n", minTime);
            System.out.println("Melhor sequência:");

            for (Exercise exercise : optimalExercisesOrder) {
                System.out.println(exercise);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
