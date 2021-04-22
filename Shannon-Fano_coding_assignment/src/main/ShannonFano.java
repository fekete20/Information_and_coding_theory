package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShannonFano {

	void calcShannonFano() throws IOException {

		ArrayList<Probabilities> elements = new ArrayList<>();
		ArrayList<Double> probabilities = new ArrayList<>();
		ArrayList<Double> order = new ArrayList<>();
		Map<Character, Long> freqMap = null;
		boolean isCharacter = false;
		String path = "files" + File.separator;
		Scanner scanner;
		ArrayList<Character> characters = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			File resultFile = new File(System.getProperty("user.dir"), path + "result" + (i + 1) + ".txt");

			Files.deleteIfExists(resultFile.toPath());
			resultFile.createNewFile();
			resultFile.setWritable(true, false);
			FileWriter fileWriter = new FileWriter(resultFile);
			System.out.println((i + 1) + ". f�jl");
			File sourceFile = new File(System.getProperty("user.dir"), path + "source" + (i + 1) + ".txt");
			scanner = new Scanner(sourceFile);
			while (scanner.hasNextLine()) {
				String tmp = scanner.nextLine();
				if (isDouble(tmp)) {
					probabilities.add(Double.parseDouble(tmp));
				} else {
					isCharacter = true;
					for (int j = 0; j < tmp.length(); j++) {
						characters.add(Character.toLowerCase(tmp.charAt(j)));
					}
					freqMap = characters.stream()
							.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
					for (Map.Entry<Character, Long> entry : freqMap.entrySet()) {
						probabilities.add((double) entry.getValue() / characters.size());
					}
				}
			}
			scanner.close();
			for (int j = 0; j < probabilities.size(); j++) {
				Probabilities probability = new Probabilities();
				probability.setValue(probabilities.get(j));
				elements.add(probability);
			}

			System.out.println((i + 1) + ". forr�sn�l " + "P halmaz:");
			fileWriter.write("P halmaz elemei: \n");
			for (Probabilities prob : elements) {
				System.out.print(prob.getValue() + " ");
				fileWriter.write(prob.getValue() + " ");
			}

			Collections.sort(elements, Collections.reverseOrder());
			System.out.println("\n" + (i + 1) + ". forr�sn�l " + "P* halmaz:");
			fileWriter.write("\nP* halmaz elemei: \n ");
			for (Probabilities prob : elements) {
				System.out.print(prob.getValue() + " ");
				fileWriter.write(prob.getValue() + " ");
			}

			// X*
			elements.get(0).setxStar(0.00);
			for (int j = 1; j < elements.size(); j++) {
				elements.get(j).setxStar(sum(elements, j - 1));
			}

			System.out.println("\n" + (i + 1) + ". forr�sn�l " + "X* halmaz: ");
			fileWriter.write("\nX* halmaz elemei: \n ");
			for (Probabilities prob : elements) {
				System.out.print(prob.getxStar() + " ");
				fileWriter.write(prob.getxStar() + " ");
			}

			// division and coding
			codewords(elements, 0, 1, 0.5);

			System.out.println("\n" + (i + 1) + ". forr�sn�l " + " k�dszavak: ");
			freqMap = freqMap.entrySet().stream().sorted((Map.Entry.<Character, Long>comparingByValue().reversed()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
							LinkedHashMap::new));
			if (isCharacter) {
				int j = 0;
				for (Map.Entry<Character, Long> entry : freqMap.entrySet()) {
					if (j == 0) {
						System.out.println("Karakter\tDarabsz�m\tRelat�v gyakoris�g\tK�dsz�");
						fileWriter.write("\nKarakter\tDarabsz�m\tRelat�v gyakoris�g\tK�dsz�\n");
					}
					System.out.print(entry.getKey());
					fileWriter.write(entry.getKey());
					System.out.print("\t\t" + entry.getValue());
					fileWriter.write("\t\t" + entry.getValue());
					System.out.print("\t\t" + elements.get(j).getValue());
					fileWriter.write("\t\t" + elements.get(j).getValue());
					System.out.println("\t" + elements.get(j).getCodeword());
					fileWriter.write("\t" + elements.get(j).getCodeword() + "\n");
					j++;
				}
				freqMap.clear();
			}

			System.out.println((i + 1) + ". forr�sn�l " + "entr�pia hat�sfok: " + codingEfficiency(elements));
			fileWriter.write("\nEntr�pia hat�sfok �rt�ke: " + codingEfficiency(elements));
			fileWriter.close();
			probabilities.clear();
			elements.clear();
			characters.clear();
			order.clear();
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
