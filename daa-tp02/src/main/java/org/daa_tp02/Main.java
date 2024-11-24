package org.daa_tp02;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static class Exercise {
        int studentId;
        int equipmentId;
        // In minutes
        double duration;

        public Exercise(int studentId, int equipmentId, double duration) {
            this.studentId = studentId;
            this.equipmentId = equipmentId;
            this.duration = duration;
        }
    }

    /**
     * Reads the input file and parses it into a list of exercises.
     *
     * @param filePath the path to the input file
     * @return a list of exercises parsed from the file
     * @throws IOException if the file cannot be read
     */
    private static List<Exercise> readExercisesFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        List<Exercise> exercises = new ArrayList<>();

        // Read the initial metadata (M, N, n)
        int equipmentCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        int studentCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        int exerciseCount = Integer.parseInt(reader.readLine().split("=")[1].trim());

        System.out.println("Equipment count: " + equipmentCount);
        System.out.println("Student count: " + studentCount);
        System.out.println("Total exercises: " + exerciseCount);

        // Read the exercises
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            int studentId = Integer.parseInt(parts[0]);
            int equipmentId = Integer.parseInt(parts[1]);
            double duration = Double.parseDouble(parts[2]);
            exercises.add(new Exercise(studentId, equipmentId, duration));
        }

        reader.close();
        return exercises;
    }

    private static Exercise parseReadLineToExercise(String line) {
        final String[] parts = line.split(" ");
        int studentId = Integer.parseInt(parts[0]);
        int equipmentId = Integer.parseInt(parts[1]);
        double duration = Double.parseDouble(parts[2]);

        return new Exercise(studentId, equipmentId, duration);
    }

    public static void main(String[] args) {
    }
}
