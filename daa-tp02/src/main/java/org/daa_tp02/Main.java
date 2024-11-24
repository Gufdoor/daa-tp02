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

        @Override
        public String toString() {
            return "Exercise: " +
                    "studentId = " + studentId +
                    ", equipmentId = " + equipmentId +
                    ", duration = " + duration;
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
        final BufferedReader reader = new BufferedReader(new FileReader(filePath));
        final List<Exercise> exercises = new ArrayList<>();

        final int equipmentCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        final int studentCount = Integer.parseInt(reader.readLine().split("=")[1].trim());
        final int exerciseCount = Integer.parseInt(reader.readLine().split("=")[1].trim());

        System.out.println("Equipment count: " + equipmentCount);
        System.out.println("Student count: " + studentCount);
        System.out.println("Total exercises: " + exerciseCount);

        boolean isNotEOF = true;

        while (isNotEOF) {
            final String line = reader.readLine();

            if (line != null)
                exercises.add(parseReadLineToExercise(line));
            else isNotEOF = false;
        }

        reader.close();

        return exercises;
    }

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
            List<Exercise> exercises = readExercisesFromFile(filePath);

            for (Exercise exercise : exercises)
                System.out.println(exercise.toString());
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
