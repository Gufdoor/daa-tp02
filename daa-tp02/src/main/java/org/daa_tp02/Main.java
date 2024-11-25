/**
 * Solution for DAA Project 02.
 *
 * @author  Daniel Lucas Murta
 * @author  Gabriel Luna dos Anjos
 * @version 1.0.0
 */

package org.daa_tp02;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

public class Main {
    // region Objects
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
         * Handles a Gantt chart plot, managing its data and also the window in which
         * it'll be rendered.
         *
         * @param charts list of build Gantt charts
         */
        public static void plotGanttChart(List<String> titles, List<ChartPanel> charts) {
            SwingUtilities.invokeLater(() -> {
                final JFrame frame = new JFrame("DAA TP02");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1000, 600);
                frame.setLocationRelativeTo(null);

                final JTabbedPane tabbedPane = new JTabbedPane();

                for (int i = 0; i < titles.size(); i++)
                    tabbedPane.addTab(titles.get(i), charts.get(i));

                frame.add(tabbedPane);
                frame.setVisible(true);
            });
        }

        /**
         * Handles a Gantt chart plot and returns its ChartPanel for embedding.
         *
         * @param title    The title of the chart
         * @param solution The solution containing an ordered exercise object list
         * @return A ChartPanel containing the Gantt chart
         */
        public static ChartPanel handleGanttChart(String title, List<Exercise> solution) {
            final IntervalCategoryDataset dataset = createDataset(solution);
            final JFreeChart chart = createChart(title, dataset);

            customizeChart(chart);

            return new ChartPanel(chart);
        }

        /**
         * Manage an exercise object list to create the necessary data to render a Gantt
         * chart.
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
                    dataset);
        }

        /**
         * Receives a Gantt and customizes its series colours. This way we can
         * represents each student by different
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
         * Parse any minutes count to Date type starting from 0. This is expected to
         * create a date that represents
         * an elapsed time in minutes.
         *
         * @param minutes minutes count
         * @return minutes converted into Date (year 0, month 0, day 0, x hour, x
         *         minute)
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
    // endregion

    // region File reading

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
    // endregion

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

    // region Brute Force Permutation

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
    private static void generateBruteForcePermutations(List<Student> students, List<Exercise> stepPermutation,
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
            generateBruteForcePermutations(remainingStudents, stepPermutation, permutations);
            // Remove changes to current permutation and continue to next student
            stepPermutation.subList(stepPermutation.size() - exercises.size(), stepPermutation.size()).clear();
        }
    }

    /**
     * Controls brute-force permutation processing
     *
     * @param academy receives an Academy object generated from the read file
     * @return a list of exercises that represents the optimal solution
     */
    private static List<Exercise> handleBruteForcePermutation(Academy academy) {
        final List<List<Exercise>> permutations = new ArrayList<>();
        generateBruteForcePermutations(academy.students, new ArrayList<>(), permutations);
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

        System.out.printf("%nLowest time: %.2f minutes%n", minTime);
        System.out.println("%nOptimal Brute Force sequence solution:");

        for (Exercise exercise : optimalBruteForceSolution)
            System.out.println(exercise);

        return optimalBruteForceSolution;
    }
    // endregion

    // region Branch and Bound

    /**
     * Controls Branch-And-Bound processing
     *
     * @param academy receives an Academy object generated from the read file
     * @return a list of exercises that represents the optimal solution
     */
    private static List<Exercise> handleBranchAndBound(Academy academy) {
        final List<Exercise> optimalBranchAndBoundSolution = new ArrayList<>();
        final List<Exercise> currentSolution = new ArrayList<>();
        final double[] minTime = { Double.MAX_VALUE };
        final double[] equipmentFreeTimes = new double[academy.M];
        final Map<Integer, Double> studentsElapsedTimes = new HashMap<>();
        final Map<Integer, Integer> studentProgress = new HashMap<>();

        for (Student student : academy.students) {
            studentProgress.put(student.studentId, 0);
        }

        exploreBranch(academy, currentSolution, optimalBranchAndBoundSolution, minTime, equipmentFreeTimes,
                studentsElapsedTimes, studentProgress);

        System.out.printf("%nBranch-and-Bound Lowest time: %.2f minutes%n", minTime[0]);
        System.out.println("Optimal Branch-and-Bound sequence solution:");

        for (Exercise exercise : optimalBranchAndBoundSolution) {
            System.out.println(exercise);
        }

        return optimalBranchAndBoundSolution;
    }

    /**
     * Recursively explores branches of the solution tree for the Branch-And-Bound
     * algorithm.
     *
     * @param academy                       receives an Academy object generated
     *                                      from the read file
     * @param currentSolution               the current partial solution being
     *                                      explored
     * @param optimalBranchAndBoundSolution the list that stores the best solution
     *                                      found so far
     * @param minTime                       an array containing the minimum time
     *                                      found so far, updated during the process
     * @param equipmentFreeTimes            an array representing the availability
     *                                      of each piece of equipment
     * @param studentsElapsedTimes          a map tracking the elapsed time for each
     *                                      student
     * @param studentProgress               a map tracking the current progress of
     *                                      each student (index of their next
     *                                      exercise)
     */
    private static void exploreBranch(Academy academy, List<Exercise> currentSolution,
            List<Exercise> optimalBranchAndBoundSolution,
            double[] minTime, double[] equipmentFreeTimes, Map<Integer, Double> studentsElapsedTimes,
            Map<Integer, Integer> studentProgress) {
        final double currentElapsed = Arrays.stream(equipmentFreeTimes).max().orElse(0.0);
        final double lowerBound = calculateLowerBound(
                academy, currentSolution, equipmentFreeTimes, studentsElapsedTimes, studentProgress);

        if (lowerBound >= minTime[0])
            return;

        if (currentSolution.size() == academy.n) {
            if (currentElapsed < minTime[0]) {
                minTime[0] = currentElapsed;
                optimalBranchAndBoundSolution.clear();
                optimalBranchAndBoundSolution.addAll(currentSolution);
            }

            return;
        }

        for (Student student : academy.students) {
            final int progressIndex = studentProgress.get(student.studentId);

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
                    academy, currentSolution, optimalBranchAndBoundSolution, minTime,
                    equipmentFreeTimes, studentsElapsedTimes, studentProgress);

            // Execute backtracking
            equipmentFreeTimes[equipmentIdx] = prevEquipmentTime;
            studentsElapsedTimes.put(nextExercise.studentId, prevStudentTime);
            currentSolution.remove(currentSolution.size() - 1);
            studentProgress.put(student.studentId, progressIndex);
        }
    }

    /**
     * Calculates an improved lower bound for the Branch-And-Bound algorithm
     *
     * @param academy              receives an Academy object generated from the
     *                             read file
     * @param currentSolution      the current partial solution being evaluated
     * @param equipmentFreeTimes   an array representing the availability of each
     *                             piece of equipment
     * @param studentsElapsedTimes a map tracking the elapsed time for each student
     * @param studentProgress      a map tracking the current progress of each
     *                             student (index of their next exercise)
     * @return the lower bound estimate as a double value
     */
    private static double calculateLowerBound(Academy academy, List<Exercise> currentSolution,
            double[] equipmentFreeTimes, Map<Integer, Double> studentsElapsedTimes,
            Map<Integer, Integer> studentProgress) {

        double lowerBound = Arrays.stream(equipmentFreeTimes).max().orElse(0.0);

        for (Student student : academy.students) {
            final int progressIndex = studentProgress.get(student.studentId);

            for (int i = progressIndex; i < student.exercises.size(); i++) {
                final Exercise exercise = student.exercises.get(i);
                final int equipmentIdx = exercise.equipmentId - 1;
                final double equipmentTime = equipmentFreeTimes[equipmentIdx];
                final double studentTime = studentsElapsedTimes.getOrDefault(student.studentId, 0.0);
                final double estimatedStartTime = Math.max(equipmentTime, studentTime);

                lowerBound = Math.max(lowerBound, estimatedStartTime + exercise.duration);
            }
        }

        return lowerBound;
    }
    // endregion

    // region Approximate Heuristic

    /**
     * Simulates the scheduling of exercises for all students and equipment.
     * It calculates the total time taken for a given schedule of exercises,
     * considering the availability
     * of each equipment and the completion times for each student.
     *
     * @param M               the number of academy equipments
     * @param currentSchedule a list of exercises representing the sequence in which
     *                        tasks
     *                        are performed
     * @param nextExercise    next exercise object on the queue to be processed
     * @return the total duration in minutes (double) when all exercises are
     *         completed
     */
    private static double handleApproximateHeuristicSchedule(int M, List<Exercise> currentSchedule,
            Exercise nextExercise) {
        final List<Exercise> timeSchedule = new ArrayList<>(currentSchedule);

        timeSchedule.add(nextExercise);

        final double timeSimulated = simulateSchedule(M, timeSchedule);

        return timeSimulated - nextExercise.duration;
    }

    /**
     * Controls approximate heuristic processing
     *
     * @param academy receives an Academy object generated from the read file
     * @return a list of exercises that represents the optimal solution
     */
    private static List<Exercise> approximateHeuristic(Academy academy) {
        final List<List<Exercise>> studentsExercises = new ArrayList<>();

        for (Student student : academy.students) {
            studentsExercises.add(new ArrayList<>(student.exercises));
        }

        final List<Exercise> approximateSolution = new ArrayList<>();
        double minTime = 0;

        while (!studentsExercises.isEmpty()) {
            Exercise nextExercise = null;
            minTime = Double.MAX_VALUE;

            for (List<Exercise> studentExercises : studentsExercises) {
                if (studentExercises.isEmpty())
                    continue;

                final Exercise exercise = studentExercises.get(0);
                final double elapsed = handleApproximateHeuristicSchedule(academy.M, approximateSolution, exercise);

                if (elapsed < minTime) {
                    minTime = elapsed;
                    nextExercise = exercise;
                }
            }

            if (nextExercise != null) {
                approximateSolution.add(nextExercise);

                for (List<Exercise> studentExercises : studentsExercises) {
                    if (!studentExercises.isEmpty() && studentExercises.get(0).equals(nextExercise)) {
                        studentExercises.remove(0);

                        break;
                    }
                }

                studentsExercises.removeIf(List::isEmpty);
            }
        }

        System.out.printf("%nLowest time: %.2f minutes%n", minTime);
        System.out.println("Optimal Approximate Heuristic sequence solution:");

        for (Exercise exercise : approximateSolution)
            System.out.println(exercise);

        return approximateSolution;
    }
    // endregion

    public static void main(String[] args) {
        final String filePath = "exercises.txt";

        try {
            final Academy academy = readAcademyDataFromFile(filePath);

            if (academy.M <= 0) {
                throw new IllegalArgumentException("The number of equipment must be greater than 0");
            }

            final List<Exercise> bruteForceSolution = handleBruteForcePermutation(academy);
            final List<Exercise> approximateHeuristicSolution = approximateHeuristic(academy);
            final List<Exercise> branchAndBoundSolution = handleBranchAndBound(academy);
            final ChartPanel bruteForceChart = ChartPlotter.handleGanttChart("Brute-Force Solution",
                    bruteForceSolution);
            final ChartPanel heuristicChart = ChartPlotter.handleGanttChart("Approximate Heuristic Solution",
                    approximateHeuristicSolution);
            final ChartPanel branchBoundChart = ChartPlotter.handleGanttChart("Branch and Bound Solution",
                    branchAndBoundSolution);
            final List<String> chartTabTitles = Arrays.asList("Brute-Force", "Approximate Heuristic",
                    "Branch and Bound");
            final List<ChartPanel> chartPanels = Arrays.asList(bruteForceChart, heuristicChart, branchBoundChart);

            ChartPlotter.plotGanttChart(chartTabTitles, chartPanels);
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
}
