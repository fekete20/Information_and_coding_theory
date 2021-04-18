package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShannonFano {

	void calcShannonFano() throws IOException {

		ArrayList<Probabilities> elements = new ArrayList<>();
		ArrayList<Double> probabilities = new ArrayList<>();
		String path = "files" + File.separator;
		Scanner scanner;
		for (int i = 0; i < 5; i++) {
			System.out.println((i + 1) + ". fájl");
			File sourceFile = new File(System.getProperty("user.dir"), path + "source" + (i + 1) + ".txt");
			scanner = new Scanner(sourceFile);
			while (scanner.hasNextLine()) {
				String tmp = scanner.nextLine();
				if (isDouble(tmp)) {
					probabilities.add(Double.parseDouble(tmp));
				} else {
					ArrayList<Character> characters = new ArrayList<>();
					for (int j = 0; j < tmp.length(); j++) {
						characters.add(Character.toLowerCase(tmp.charAt(j)));
					}
					Map<Character, Long> freqMap = characters.stream()
							.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					for (Map.Entry<Character, Long> entry : freqMap.entrySet()) {
						probabilities.add((double) entry.getValue() / characters.size());
					}
					characters.clear();
					freqMap.clear();
				}
			}
			scanner.close();
			for (int j = 0; j < probabilities.size(); j++) {
				Probabilities probability = new Probabilities();
				probability.setValue(probabilities.get(j));
				elements.add(probability);
			}

			System.out.println((i + 1) + ". forrásnál " + "P halmaz:");
			for (Probabilities prob : elements) {
				System.out.print(prob.getValue() + " ");
			}

			Collections.sort(elements, Collections.reverseOrder());

			System.out.println("\n" + (i + 1) + ". forrásnál " + "P* halmaz:");
			for (Probabilities prob : elements) {
				System.out.print(prob.getValue() + " ");
			}

			// X*
			elements.get(0).setxStar(0.00); // fix
			for (int j = 1; j < elements.size(); j++) {
				elements.get(j).setxStar(sum(elements, j - 1));
			}

			System.out.println("\n" + (i + 1) + ". forrásnál " + "X* halmaz: ");
			for (Probabilities prob : elements) {
				System.out.print(prob.getxStar() + " ");
			}

			// division and coding
			codewords(elements, 0, 1, 0.5);

			System.out.println("\n" + (i + 1) + ". forrásnál " + " kódszavak: ");

			File resultFile = new File(System.getProperty("user.dir"), path + "result" + (i + 1) + ".txt");

			Files.deleteIfExists(resultFile.toPath());
			resultFile.createNewFile();
			resultFile.setWritable(true, false);
			FileWriter fileWriter = new FileWriter(resultFile);
			for (Probabilities prob : elements) {
				System.out.print(prob.getCodeword() + " ");
				fileWriter.write(prob.getCodeword());
			}

			System.out.println("\n" + (i + 1) + ". forrásnál " + "hatásfok: " + codingEfficiency(elements));
			fileWriter.write("\nHatásfok: " + codingEfficiency(elements));
			fileWriter.close();
			probabilities.clear();
			elements.clear();
			System.out.println();
		}
	}

// auxiliary functions

	int codewords(ArrayList<Probabilities> elements, double start, double end, double length) {
		int multiplicityOfInterval = 0;
		for (double i = start, intervalNo = 0; i < end && intervalNo < 2; i += length, intervalNo++) {
			multiplicityOfInterval = 0;
			int temp = 0;
			for (int j = 0; j < elements.size(); j++) {
				temp = j;
				if (elements.get(j).getxStar() >= i && elements.get(j).getxStar() < i + length) {
					multiplicityOfInterval++;
					elements.get(j).setCodeword(elements.get(j).getCodeword() + (int) intervalNo); // !
				} else if (elements.get(j).getxStar() < i || elements.get(j).getxStar() >= i + length
						|| j == elements.size() - 1) {
					if (multiplicityOfInterval > 1) {
						multiplicityOfInterval = codewords(elements, i, i + length, length / 2);
						break;
					} else if (multiplicityOfInterval == 1) {
						break;
					}
				}
			}
			if (multiplicityOfInterval > 1 && temp == elements.size() - 1) {
				codewords(elements, i, i + length, length / 2);
			}
		}
		return multiplicityOfInterval;
	}

	double sum(ArrayList<Probabilities> list, int untilIndex) {
		double sum = 0.0;
		for (int i = 0; i <= untilIndex; i++)
			sum += list.get(i).getValue();
		return sum;
	}

	double entropy(ArrayList<Probabilities> list) {
		double entropy = 0;
		for (Probabilities prob : list) {
			entropy += -1 * prob.getValue() * (Math.log10(prob.getValue()) / Math.log10(2));
		}
		return entropy;
	}

	double avgLength(ArrayList<Probabilities> list) {
		double avgLength = 0;
		for (Probabilities prob : list) {
			avgLength += prob.getValue() * prob.getCodeword().length();
		}
		return avgLength;
	}

	double codingEfficiency(ArrayList<Probabilities> list) {
		return entropy(list) / avgLength(list);
	}

	boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
