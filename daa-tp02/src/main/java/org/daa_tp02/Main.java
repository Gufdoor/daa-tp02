package org.daa_tp02;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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
        double startTime;

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

    private static class ChartPlotter {
        /**
         * Handles a Gantt chart plot, managing its data and also the window in which it'll be rendered.
         *
         * @param title    JFrame window title
         * @param solution solution containing an ordered exercise object list
         */
        public static void plotGanttChart(String title, List<Exercise> solution) {
            SwingUtilities.invokeLater(() -> {
                final JFrame chartFrame = new JFrame("DAA TP02");
                final IntervalCategoryDataset dataset = createDataset(solution);
                final JFreeChart chart = createChart(title, dataset);

                customizeChart(chart);

                final ChartPanel panel = new ChartPanel(chart);

                chartFrame.setContentPane(panel);
                chartFrame.setSize(800, 400);
                chartFrame.setLocationRelativeTo(null);
                chartFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                chartFrame.setVisible(true);
            });
        }

        /**
         * Manage an exercise object list to create the necessary data to render a Gantt chart.
         *
         * @param solution solution containing an ordered exercise object list
         */
        private static IntervalCategoryDataset createDataset(List<Exercise> solution) {
            final TaskSeriesCollection dataset = new TaskSeriesCollection();
            final Map<Integer, TaskSeries> studentsSeriesMap = new HashMap<>();

            for (Exercise exercise : solution) {
                studentsSeriesMap.putIfAbsent(exercise.studentId, new TaskSeries("Student " + exercise.studentId));

                final TaskSeries series = studentsSeriesMap.get(exercise.studentId);
                final Date startDate = parseMinutesToDate(exercise.startTime);
                final Date endDate = parseMinutesToDate(exercise.startTime + exercise.duration);

                series.add(new Task("Equipment " + exercise.equipmentId, startDate, endDate));
            }

            studentsSeriesMap.values().forEach(dataset::add);

            return dataset;
        }

        /**
         * Creates a Gantt chart with the provided arguments.
         *
         * @param title   defined chart title
         * @param dataset chart data to be rendered
         */
        private static JFreeChart createChart(String title, IntervalCategoryDataset dataset) {
            return ChartFactory.createGanttChart(
                    title,
                    "Equipments",
                    "Elapsed Time (minutes)",
                    dataset
            );
        }

        /**
         * Receives a Gantt and customizes its series colours. This way we can represents each student by different
         * colours.
         *
         * @param chart Gantt chart already created
         */
        private static void customizeChart(JFreeChart chart) {
            final int seriesCount = chart.getCategoryPlot().getDataset().getRowCount();
            final GanttRenderer renderer = new GanttRenderer();

            for (int i = 0; i < seriesCount; i++) {
                renderer.setSeriesPaint(i, generateRandomColor());
            }

            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRenderer(renderer);
        }

        /**
         * Generates a random color.
         *
         * @return a random Color object
         */
        private static Color generateRandomColor() {
            final Random random = new Random();
            final int bound = 256;

            final int red = random.nextInt(bound);
            final int green = random.nextInt(bound);
            final int blue = random.nextInt(bound);

            return new Color(red, green, blue);
        }

        /**
         * Parse any minutes count to Date type starting from 0. This is expected to create a date that represents
         * an elapsed time in minutes.
         *
         * @param minutes minutes count
         * @return minutes converted into Date (year 0, month 0, day 0, x hour, x minute)
         */
        private static Date parseMinutesToDate(double minutes) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.MINUTE, (int) minutes);

            return calendar.getTime();
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
        System.out.println("%nOptimal sequence solution:");

        for (Exercise exercise : optimalBruteForceSolution)
            System.out.println(exercise);

        return optimalBruteForceSolution;
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
        final double[] equipmentFreeTimes = new double[M];
        final Map<Integer, Double> studentsElapsedTimes = new HashMap<>();

        for (Exercise exercise : schedule) {
            final int equipmentIdIndex = exercise.equipmentId - 1;
            final double equipmentFreeTime = equipmentFreeTimes[equipmentIdIndex];
            final double studentsElapsedTime = studentsElapsedTimes.getOrDefault(exercise.studentId, 0.0);
            final double startTime = Math.max(equipmentFreeTime, studentsElapsedTime);
            exercise.startTime = startTime;
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
            final List<Exercise> optimalBruteForceSolution = handleBruteForcePermutation(academy);
            final List<Exercise> approximateHeuristicSolution = approximateHeuristic(academy);
            ChartPlotter.plotGanttChart("Academy Brute-Force Solution", optimalBruteForceSolution);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
