package com.example.lab3_3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View v) {
        EditText Equals = findViewById(R.id.equals);
        EditText populationNumber = findViewById(R.id.populationNumber);
        EditText A = findViewById(R.id.editA);
        EditText B = findViewById(R.id.editB);
        EditText C = findViewById(R.id.editC);
        EditText D = findViewById(R.id.editD);
        EditText MinText = findViewById(R.id.minText);
        EditText MaxText = findViewById(R.id.maxText);

        TextView resultText = findViewById(R.id.resultText);
        TextView bestPercent = findViewById(R.id.bestPercent);

        if (A.getText().toString().trim().equals("")
                || B.getText().toString().trim().equals("")
                || C.getText().toString().trim().equals("")
                || D.getText().toString().trim().equals("")
                || Equals.getText().toString().trim().equals("")
                || populationNumber.getText().toString().trim().equals("")
                || MinText.getText().toString().trim().equals("")
                || MaxText.getText().toString().trim().equals("")) {
            resultText.setText("Введіть вірні дані!");
            return;
        }
        if (populationNumber.getText().toString().trim().equals("1")) {
            resultText.setText("Кількість популяцій повинна бути більшою за 1");
            return;
        }

        int FINAL_VALUE = Integer.parseInt(Equals.getText().toString());
        int POPULATION_SIZE = Integer.parseInt(populationNumber.getText().toString());
        int a = Integer.parseInt(A.getText().toString());
        int b = Integer.parseInt(B.getText().toString());
        int c = Integer.parseInt(C.getText().toString());
        int d = Integer.parseInt(D.getText().toString());
        int GENE_MIN = Integer.parseInt(MinText.getText().toString());
        int GENE_MAX = Integer.parseInt(MaxText.getText().toString());

        if (GENE_MIN >= GENE_MAX) {
            resultText.setText("Виникла помилка: мінімум не може перевищувати максимум");
            return;
        }

        Chromosome chromosome = null;
        long iterationMin = Integer.MAX_VALUE;
        float bestPercentValue = 0.0F;

        for (float mutationPercent = 1.0F; mutationPercent < 100.0f; mutationPercent += 0.5F) {
            Algorithm diofant = new Algorithm(FINAL_VALUE, POPULATION_SIZE, GENE_MIN, GENE_MAX, a, b, c, d);
            diofant.initiatePopulation(FINAL_VALUE, GENE_MIN, GENE_MAX, a, b, c, d, mutationPercent);
            long iterationsNumber = 0;

            do {
                int fitnessFilling = diofant.fillChromosomesWithFitnesses();

                if (fitnessFilling != -2) {
                    chromosome = diofant.getPopulation()[fitnessFilling];
                    break;
                }

                diofant.fillLikelihoods();

                int[][] pairs = diofant.getPairsForCrossover();
                diofant.analyzePairs(pairs);

                Chromosome nextGeneration[];
                nextGeneration = diofant.nextGenerationWithCrossoverAndMutation(pairs);

                diofant.setPopulation(nextGeneration);

                iterationsNumber++;
            } while (iterationsNumber < 10000);

            if (iterationsNumber < iterationMin) {
                iterationMin = iterationsNumber;
                bestPercentValue = mutationPercent;
            }
        }


        if (chromosome != null) {
            resultText.setText("Результат: " + chromosome);
            bestPercent.setText("Найкращий відсоток: " + bestPercentValue);
        } else {
            resultText.setText("Рішення не знайдено");
        }

        return;
    }
}

class Chromosome {

    int targetValue, a, b, c, d;
    public final int TARGET_IS_REACHED_FLAG = -1;
    public int geneMin, geneMax;
    public float mutationPercent;

    public Chromosome(int FINAL_VALUE, int GENE_MIN, int GENE_MAX, int a, int b, int c, int d, float mutationPercent) {
        this.targetValue = FINAL_VALUE;
        this.geneMin = GENE_MIN;
        this.geneMax = GENE_MAX;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.mutationPercent = mutationPercent;
    }

    private int genes[] = new int[Algorithm.GENES_SIZE];

    private float fitness;

    private float likelihood;

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public int[] getGenes() {
        return genes;
    }

    public void setGenes(int[] genes) {
        this.genes = genes;
    }

    public float getLikelihood() {
        return likelihood;
    }

    public void setLikelihood(float likelihood) {
        this.likelihood = likelihood;
    }

    public int getRandomInt(int min, int max) {
        Random randomGenerator;
        randomGenerator = new Random();
        return randomGenerator.nextInt(max + 1) + min;
    }

    public float getRandomFloat(float min, float max) {
        return (float) (Math.random() * max + min);
    }

    public int getRandomGene() {
        return getRandomInt(geneMin, geneMax);
    }

    public int function(int x1, int x2, int x3, int x4) {
        return a * x1 + b * x2 + c * x3 + d * x4;
    }

    public float calculateFitness() {
        int a = genes[0];
        int b = genes[1];
        int c = genes[2];
        int d = genes[3];
        int closeness = Math.abs(targetValue - function(a, b, c, d));
        return 0 != closeness ? 1 / (float) closeness : TARGET_IS_REACHED_FLAG;
    }

    public Chromosome mutateWithGivenLikelihood() {

        Chromosome result = (Chromosome) this.clone();

        for (int i = 0; i < 4; ++i) {

            float randomPercent = getRandomFloat(0, 100);
            if (randomPercent < mutationPercent) {
                int newValue = getRandomGene();
                result.getGenes()[i] = newValue;
            }
        }
        return result;
    }

    public Chromosome[] doubleCrossover(Chromosome chromosome) {

        int crossoverline = getRandomCrossoverLine();
        Chromosome[] result = new Chromosome[2];
        result[0] = new Chromosome(targetValue, geneMin, geneMax, a, b, c, d, mutationPercent);
        result[1] = new Chromosome(targetValue, geneMin, geneMax, a, b, c, d, mutationPercent);

        for (int i = 0; i < 4; ++i) {
            if (i <= crossoverline) {
                result[0].getGenes()[i] = this.getGenes()[i];
                result[1].getGenes()[i] = chromosome.getGenes()[i];
            } else {
                result[0].getGenes()[i] = chromosome.getGenes()[i];
                result[1].getGenes()[i] = this.getGenes()[i];
            }
        }
        return result;
    }

    public Chromosome singleCrossover(Chromosome chromosome) {
        Chromosome[] children = doubleCrossover(chromosome);
        int childNumber = getRandomInt(0, 1);
        return children[childNumber];
    }

    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append(" (");
        for (int i = 0; i < 4; ++i) {
            result.append("" + genes[i]);
            result.append(i < 4 - 1 ? ", " : "");
        }
        result.append(")\n");
        return result.toString();
    }

    private int getRandomCrossoverLine() {
        int line = getRandomInt(0, 4 - 2);
        return line;
    }

    protected Object clone() {
        Chromosome resultChromosome = new Chromosome(targetValue, geneMin, geneMax, a, b, c, d, mutationPercent);
        resultChromosome.setFitness(this.getFitness());
        resultChromosome.setLikelihood(this.getLikelihood());
        int resultGenes[];
        resultGenes = this.genes.clone();
        resultChromosome.setGenes(resultGenes);
        return resultChromosome;
    }
}


class Algorithm {

    public final int TARGET_IS_REACHED_FLAG = -1;
    private final int TARGET_NOT_REACHED_FLAG = -2;
    int FINAL_VALUE, POPULATION_SIZE, a, b, c, d;
    public static final int GENES_SIZE = 4;
    public int GENE_MIN;
    public int GENE_MAX;

    public Algorithm(int FINAL_VALUE, int POPULATION_SIZE, int GENE_MIN, int GENE_MAX, int a, int b, int c, int d) {
        this.FINAL_VALUE = FINAL_VALUE;
        this.POPULATION_SIZE = POPULATION_SIZE;
        this.GENE_MIN = GENE_MIN;
        this.GENE_MAX = GENE_MAX;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        population = new Chromosome[POPULATION_SIZE];
    }

    private Chromosome population[];

    public int fillChromosomesWithFitnesses() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            float currentFitness = population[i].calculateFitness();
            population[i].setFitness(currentFitness);

            if (currentFitness == TARGET_IS_REACHED_FLAG) {
                return i;
            }
        }
        return TARGET_NOT_REACHED_FLAG;
    }

    private float getAllFitnessesSum() {
        float allFitnessesSum = .0F;
        for (int i = 0; i < POPULATION_SIZE; i++) {
            allFitnessesSum += population[i].getFitness();
        }

        return allFitnessesSum;
    }

    public void fillLikelihoods() {
        float allFitnessesSum = getAllFitnessesSum();
        float last = .0F;

        int i;
        for (i = 0; i < POPULATION_SIZE; i++) {

            float likelihood = last + (100 * population[i].getFitness() / allFitnessesSum);
            last = likelihood;
            population[i].setLikelihood(likelihood);
        }
    }
    
    public int getRandomInt(int min, int max) {
        Random randomGenerator;
        randomGenerator = new Random();
        return randomGenerator.nextInt(max + 1) + min;
    }

    public float getRandomFloat(float min, float max) {
        return (float) (Math.random() * max + min);
    }

    public int getRandomGene() {
        return getRandomInt(GENE_MIN, GENE_MAX);
    }

    private void randomizeGenes(Chromosome chromosome) {
        for (int i = 0; i < GENES_SIZE; i++) {
            chromosome.getGenes()[i] = getRandomGene();
        }
    }

    public void initiatePopulation(int targetValue, int geneMin, int geneMax, int a, int b, int c, int d, float mutationPercent) {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = new Chromosome(targetValue, geneMin, geneMax, a, b, c, d, mutationPercent);
            randomizeGenes(population[i]);
        }
    }

    public int[][] getPairsForCrossover() {
        int[][] pairs = new int[POPULATION_SIZE][2];
        for (int i = 0; i < POPULATION_SIZE; i++) {

            float rand = getRandomFloat(0, 100);
            int firstChromosome = getRandChromosomeNumber(rand);
            int secondChromosome;

            do {
                rand = getRandomFloat(0, 100);
                secondChromosome = getRandChromosomeNumber(rand);
            } while (firstChromosome == secondChromosome);

            pairs[i][0] = firstChromosome;
            pairs[i][1] = secondChromosome;
        }
        return pairs;
    }


    public void analyzePairs(int[][] pairs) {
        int[] totals = new int[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            totals[i] = 0;
        }
        for (int i = 0; i < POPULATION_SIZE; i++) {
            for (int j = 0; j < 2; j++) {
                totals[pairs[i][j]]++;
            }
        }
    }

    private int getRandChromosomeNumber(float rand) {
        int i;
        for (i = 0; i < POPULATION_SIZE; i++) {
            if (rand <= population[i].getLikelihood()) {
                return i;
            }
        }
        return i - 1;
    }

    public Chromosome[] nextGenerationWithCrossoverAndMutation(int[][] pairs) {

        Chromosome nextGeneration[] = new Chromosome[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Chromosome firstParent = population[pairs[i][0]];
            Chromosome secondParent = population[pairs[i][1]];
            Chromosome result = firstParent.singleCrossover(secondParent);
            nextGeneration[i] = result;
            nextGeneration[i] = nextGeneration[i].mutateWithGivenLikelihood();
        }
        return nextGeneration;
    }

    public Chromosome[] getPopulation() {
        return population;
    }

    public void setPopulation(Chromosome[] population) {
        this.population = population;
    }
}