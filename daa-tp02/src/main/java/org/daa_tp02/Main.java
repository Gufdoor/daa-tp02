package org.daa_tp02;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            return "Exercise: " +
                    "studentId = " + studentId +
                    ", equipmentId = " + equipmentId +
                    ", duration = " + duration;
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
            Student student = academy.students.stream()
                    .filter(auxStudent -> auxStudent.studentId == exercise.studentId)
                    .findFirst()
                    .orElse(null);

            if (student == null) {
                student = new Student(exercise.studentId);
                academy.students.add(student);
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

    public static void main(String[] args) {
        final String filePath = "exercises.txt";

        try {
            final Academy academy = readAcademyDataFromFile(filePath);

            for (Student student : academy.students)
                for (Exercise exercise : student.exercises)
                    System.out.println(exercise.toString());
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
