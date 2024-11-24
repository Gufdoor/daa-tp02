import java.io.*;
import java.util.*;

public class Academia {

    static class Exercicio{
        int alunoID;
        int equipamentoID;
        double tempo;
        
        public Exercicio(int alunoID, int equipamentoID, double tempo){
            this.alunoID = alunoID;
            this.equipamentoID = equipamentoID;
            this.tempo = tempo;
        }

        @Override
        public String toString(){
            return " " + alunoID + " " + equipamentoID + " " + tempo;
        }
    }

    public static void main(String[] args) throws IOException {
        String arquivo = "exercicios.txt";
        int qtdEquipamento, qtdAluno, somatorioN;
        List<Exercicio> exercicios = new ArrayList<Exercicio>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            qtdEquipamento = Integer.parseInt(br.readLine().split("=")[1].trim());
            qtdAluno = Integer.parseInt(br.readLine().split("=")[1].trim());
            somatorioN = Integer.parseInt(br.readLine().split("=")[1].trim());

            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ");
                int idAluno = Integer.parseInt(split[0]);
                int idEquipamento = Integer.parseInt(split[1]);
                double tempo = Double.parseDouble(split[2]);
                
                exercicios.add(new Exercicio(idAluno, idEquipamento, tempo));
            }
        }

        // Agrupar os exercícios por aluno para respeitar a ordem
        Map<Integer, List<Exercicio>> studentSequences = new HashMap<>();
        for (Exercicio e : exercicios) {
            studentSequences.computeIfAbsent(e.alunoID, k -> new ArrayList<>()).add(e);
        }

        // Gerar todas as permutações possíveis
        List<List<Exercicio>> allPermutations = new ArrayList<>();
        generatePermutations(new ArrayList<>(studentSequences.values()), new ArrayList<>(), allPermutations);
        System.out.println(allPermutations.size());

        // Simular todas as permutações e encontrar o menor tempo
        double minTime = Double.MAX_VALUE;
        List<Exercicio> bestSchedule = null;

        // for (List<Exercise> perm : allPermutations) {
        double time = simulateSchedule(qtdEquipamento, allPermutations.get(0));   
        if (time < minTime) {
            minTime = time;
            bestSchedule = allPermutations.get(0);
        }
        // }

        // Exibir resultados
        System.out.printf("Menor tempo total: %.2f minutos%n", minTime);
        System.out.println("Melhor sequência:");
        for (Exercicio e : bestSchedule) {
            System.out.println(e);
        }
    }

    // Método para simular o cronograma de uma sequência
    private static double simulateSchedule(int M, List<Exercicio> schedule) {
        double[] equipmentFreeTime = new double[M]; // Tempo livre de cada equipamento
        Map<Integer, Double> studentFinishTime = new HashMap<>(); // Tempo de término de cada aluno

        for (Exercicio e : schedule) {
            int equipmentIdx = e.equipamentoID - 1;
            double startTime = Math.max(equipmentFreeTime[equipmentIdx],
                    studentFinishTime.getOrDefault(e.alunoID, 0.0));
            double finishTime = startTime + e.tempo;

            // Atualizar tempos
            equipmentFreeTime[equipmentIdx] = finishTime;
            System.out.println(
                    "Tempo Equipamento: " + equipmentFreeTime[equipmentIdx] + " Equipamento: " + e.equipamentoID);
            studentFinishTime.put(e.alunoID, finishTime);
        }

        // Retornar o maior tempo de término entre os alunos
        return Collections.max(studentFinishTime.values());
    }

    private static void simulacaoExercicios(int M, List<Exercicio> combinacaoExercicio){
        boolean[] equipamentoLivre = new boolean[M];
        // Inicializando todos os elementos como true
        Arrays.fill(equipamentoLivre, true);

        for(Exercicio e : combinacaoExercicio){
            if(equipamentoLivre[e.equipamentoID-1]){
                
            } else {
                
            }
        }

    }

    private static void generatePermutations(List<List<Exercicio>> sequences, List<Exercicio> current, List<List<Exercicio>> result) {
        if (sequences.isEmpty()) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 0; i < sequences.size(); i++) {
            List<Exercicio> sequence = sequences.get(i);

            // Fixar a sequência atual e gerar permutações com o restante
            List<List<Exercicio>> remaining = new ArrayList<>(sequences);
            remaining.remove(i);

            current.addAll(sequence);
            generatePermutations(remaining, current, result);
            current.subList(current.size() - sequence.size(), current.size()).clear(); // Reverter a adição
        }
    }
}