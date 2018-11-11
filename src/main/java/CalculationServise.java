import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Anna on 11.11.2018.
 */
public class CalculationServise {
    public static List<Double> calculatMeanMark(ListOfMarks listOfMarks) {
        List<Double> meanMarks = new ArrayList<>();
        for (int i = 0; i < listOfMarks.getUser(0).size(); i++) {
            double sumOfMarks = 0;
            int countOfMarks = 0;
            for (int j = 0; j < listOfMarks.size(); j++) {
                if (listOfMarks.getUser(j).getMark(i) != null) {
                    sumOfMarks += listOfMarks.getUser(j).getMark(i);
                    countOfMarks += 1;
                }
            }
            double mean = sumOfMarks / countOfMarks;
            meanMarks.add(i, mean);
        }
        return meanMarks;
    }

    public static List<List<Double>> countProximityMeasurePirson(ListOfMarks listOfMarks,
                                                                 List<Double> meanMarks,
                                                                 double constOfRegularisation) {
        List<List<Double>> proximityMeasure = new ArrayList<>();
        int dimension = listOfMarks.getUser(0).size();
        for (int i = 0; i < dimension; i++) {
            proximityMeasure.add(Arrays.asList(new Double[dimension]));
        }

        for (int i = 0; i < listOfMarks.getUser(0).size(); i++) {
            for (int j = 1; j < listOfMarks.getUser(0).size(); j++) {
                if (i < j) {
                    double numerator = 0;
                    double denominatorLeft = 0;
                    double denominatorRight = 0;
                    int countUserNotNull = 0;
                    for (User user : listOfMarks) {
                        if ((user.getMark(i) != null) && (user.getMark(j) != null)) {
                            numerator += (user.getMark(i) - meanMarks.get(i)) * (user.getMark(j) - meanMarks.get(j));
                            denominatorLeft += Math.pow(user.getMark(i) - meanMarks.get(i), 2);
                            denominatorRight += Math.pow(user.getMark(j) - meanMarks.get(j), 2);
                            countUserNotNull += 1;
                        }
                    }
                    double sPirsonIJ = countUserNotNull / (countUserNotNull + constOfRegularisation) * (numerator / (Math.sqrt(denominatorLeft) * Math.sqrt(denominatorRight)));
                    proximityMeasure.get(i).set(j, sPirsonIJ);
                    proximityMeasure.get(j).set(i, sPirsonIJ);
                }
            }
        }
        return proximityMeasure;
    }

    public static List<List<Integer>> getClosestElements(List<List<Double>> pearsonMarks) {
        List<List<Integer>> resultList = new ArrayList<>();
        for (List<Double> marks : pearsonMarks) {
            Map<Integer, Double> mapList = new HashMap<>();
            for (int i = 0; i < marks.size(); i++) {
                if (marks.get(i) != null) {
                    mapList.put(i, marks.get(i));
                }
            }
            List<Integer> listOfNumber = mapList.entrySet().stream().sorted((el1, el2) -> el2.getValue().compareTo(el1.getValue()))
                    .limit(30).map(Map.Entry::getKey).collect(Collectors.toList());
            resultList.add(listOfNumber);
        }
        return resultList;
    }
}
