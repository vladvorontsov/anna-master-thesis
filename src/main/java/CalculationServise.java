import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Anna on 11.11.2018.
 */
public class CalculationServise {
    public static List<Double> calculatMeanMarksList(ListOfMarks listOfMarks) {
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
                    double sPirsonIJ = countUserNotNull / (countUserNotNull + constOfRegularisation) *
                            (numerator / (Math.sqrt(denominatorLeft) * Math.sqrt(denominatorRight)));
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
            List<Integer> listOfNumber = mapList.entrySet().stream().sorted((el1, el2) -> el2.getValue().
                    compareTo(el1.getValue())).limit(30).map(Map.Entry::getKey).collect(Collectors.toList());
            resultList.add(listOfNumber);
        }
        return resultList;
    }

    public static List<List<Double>> getParamBandD(ListOfMarks listOfMarks, List<List<Integer>> closestElements,
                                                   Double meanMark, List<List<Double>> proximityMeasure, Double lyambda3) {
        final Random random = new Random();
        final int alpfa = 1;
        final double beta = 0.5;
        final int gamma = 2;
        List<List<Double>> deviationUserFromMean = new ArrayList<>();
        List<List<Double>> deviationElementFromMean = new ArrayList<>();
        List<Pair> listOfFunctions = new LinkedList<>();
        List<Double> vectorX0Users = new ArrayList<>(listOfMarks.size());
        List<Double> vectorX0Elements = new ArrayList<>(listOfMarks.getUser(0).size());

        for (int i = 0; i < (listOfMarks.getUser(0).size() + listOfMarks.size() + 1); i++) {
            //initialization of (n+1) random vector X
            List<Double> listDeviationUsers = random.doubles().map(n -> -2 + n * 4).limit(listOfMarks.size()).boxed().
                    collect(Collectors.toList());
            List<Double> listDeviationElements = random.doubles().map(n -> -2 + n * 4).limit(listOfMarks.getUser(0).
                    size()).boxed().collect(Collectors.toList());
            deviationElementFromMean.add(listDeviationElements);
            deviationUserFromMean.add(listDeviationUsers);

            //calculating value of minimization function and adding it to list with index of position vector of X
            Double functionValue = calculateFunctionOfMinimization(listOfMarks, closestElements, listDeviationUsers,
                    listDeviationElements, proximityMeasure, meanMark, lyambda3);
            Pair pair = new Pair(functionValue, i);
            listOfFunctions.add(pair);

            //calculating X0 with Xh
            for (int j = 0; j < listOfMarks.size(); j++) {
                vectorX0Users.set(j, vectorX0Users.get(j) + listDeviationUsers.get(j));
            }
            for (int k = 0; k < listOfMarks.getUser(0).size(); k++) {
                vectorX0Elements.set(k, vectorX0Elements.get(k) + listDeviationElements.get(k));
            }
        }
        listOfFunctions.sort(Comparator.comparing(el -> el.valueOfF));

        List<Double> vectorXrUsers = new ArrayList<>(listOfMarks.size());
        List<Double> vectorXrElements = new ArrayList<>(listOfMarks.getUser(0).size());

        for (int j = 0; j < listOfMarks.size(); j++) {
            //calculating X0 without Xh
            vectorX0Users.set(j, 1 / listOfMarks.size() * (vectorX0Users.get(j) - deviationUserFromMean.get(listOfFunctions.
                    get(listOfFunctions.size() - 1).getIntegerOfX()).get(j)));
            //calculation Xr (Step 7. Part 1)
            vectorXrUsers.set(j, (1 + alpfa) * vectorX0Users.get(j) - alpfa * deviationUserFromMean.get(listOfFunctions.
                    get(listOfFunctions.size() - 1).getIntegerOfX()).get(j));
        }

        for (int k = 0; k < listOfMarks.getUser(0).size(); k++) {
            //calculating X0 without Xh
            vectorX0Elements.set(k, 1 / listOfMarks.getUser(0).size() * (vectorX0Elements.get(k) - deviationElementFromMean.
                    get(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX()).get(k)));
            //calculation Xr (Step 7. Part 1)
            vectorXrElements.set(k, (1 + alpfa) * vectorX0Elements.get(k) - alpfa * deviationElementFromMean.
                    get(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX()).get(k));
        }

        //calculation f(Xr) (Step 7. Part 2 finding f(Xr))
        


    }


    public static Double calculateFunctionOfMinimization(ListOfMarks listOfMarks, List<List<Integer>> closestElements,
                                                         List<Double> listDeviationUsers, List<Double> listDeviationElements,
                                                         List<List<Double>> proximityMeasure, Double meanMark, Double lyambda3) {
        Double valueOfFunktionMinimization = new Double(0);

        for (int k = 0; k < listOfMarks.getUser(0).size(); k++) {
            for (int j = 0; j < listOfMarks.size(); j++) {
                if (listOfMarks.getUser(j).getMark(k) != null) {
                    double denominator = 0;
                    double numerator = 0;
                    for (int i = 0; i < closestElements.get(0).size(); i++) {
                        denominator += proximityMeasure.get(k).get(closestElements.get(k).get(i)) *
                                (listOfMarks.getUser(j).getMark(closestElements.get(k).get(i)) - meanMark -
                                        listDeviationUsers.get(j) - listDeviationElements.get(closestElements.get(k).get(i)));
                        numerator += Math.abs(proximityMeasure.get(k).get(closestElements.get(k).get(i)));
                    }
                    valueOfFunktionMinimization += Math.pow(listOfMarks.getUser(j).getMark(k) - meanMark - listDeviationUsers.get(j) -
                            listDeviationElements.get(k) - denominator / numerator, 2);
                }
            }
        }

        double sumD = 0;
        double sumU = 0;
        double sumS = 0;

        for (int k = 0; k < listOfMarks.getUser(0).size(); k++) {
            for (int i = 0; i < closestElements.get(0).size(); i++) {
                sumS += proximityMeasure.get(k).get(closestElements.get(k).get(i)) *
                        proximityMeasure.get(k).get(closestElements.get(k).get(i));
            }
            sumD += listDeviationElements.get(k) * listDeviationElements.get(k);
        }

        for (int j = 0; j < listOfMarks.size(); j++) {
            sumU += listDeviationUsers.get(j) * listDeviationUsers.get(j);
        }

        valueOfFunktionMinimization += lyambda3 * (sumD + sumS + sumU);
        return valueOfFunktionMinimization;
    }


    public static Double calculatMeanMark(ListOfMarks listOfMarks) {
        double sumOfMarks = 0;
        int countOfMarks = 0;
        for (int i = 0; i < listOfMarks.getUser(0).size(); i++) {
            for (int j = 0; j < listOfMarks.size(); j++) {
                if (listOfMarks.getUser(j).getMark(i) != null) {
                    sumOfMarks += listOfMarks.getUser(j).getMark(i);
                    countOfMarks += 1;
                }
            }
        }
        double meanMark = sumOfMarks / countOfMarks;
        return meanMark;
    }
}
