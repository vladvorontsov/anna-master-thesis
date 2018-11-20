import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

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
                                                   Double meanMark, List<List<Double>> proximityMeasure, Double lambda3) {
        final Random random = new Random();
        final int alpfa = 1;
        final double beta = 0.5;
        final int gamma = 2;
        List<List<Double>> deviationUserFromMean = new ArrayList<>();
        List<List<Double>> deviationElementFromMean = new ArrayList<>();
        List<Pair> listOfFunctions = new LinkedList<>();
        /*List<Double> vectorX0Users = new ArrayList<>(listOfMarks.size());
        List<Double> vectorX0Elements = new ArrayList<>(listOfMarks.getUser(0).size());*/

        for (int i = 0; i < (listOfMarks.getUser(0).size() + listOfMarks.size() + 1); i++) {
            //initialization of (n+1) random vector X
            List<Double> listDeviationUsers = random.doubles().map(n -> -2 + n * 4).limit(listOfMarks.size()).boxed().
                    collect(Collectors.toList());
            List<Double> listDeviationElements = random.doubles().map(n -> -2 + n * 4).limit(listOfMarks.getUser(0).
                    size()).boxed().collect(Collectors.toList());
            deviationElementFromMean.add(listDeviationElements);
            deviationUserFromMean.add(listDeviationUsers);

            //calculating value of minimization function and adding it to list with index of position vector of X
            System.out.println("calculating function");
            Double functionValue = calculateFunctionOfMinimization(listOfMarks, closestElements, listDeviationUsers,
                    listDeviationElements, proximityMeasure, meanMark, lambda3);
            Pair pair = new Pair(functionValue, i);
            listOfFunctions.add(pair);

        }

        double epsilon = 1;
        System.out.println("Before while");
        while (epsilon > 0.1) {
            System.out.println("epsilon: " + epsilon);
            List<Double> vectorX0Users = DoubleStream.generate(() -> 0).limit(listOfMarks.size()).boxed().collect(Collectors.toList());
            List<Double> vectorX0Elements = DoubleStream.generate(() -> 0).limit(listOfMarks.getUser(0).size()).boxed().collect(Collectors.toList());

            for (int i = 0; i < (listOfMarks.getUser(0).size() + listOfMarks.size() + 1); i++) {
                //calculating X0 with Xh
                vectorX0Users = creatingNewVectorXforUsersOrElements(listOfMarks.size(), 1, 1, vectorX0Users,
                        deviationUserFromMean.get(i));
                vectorX0Elements = creatingNewVectorXforUsersOrElements(listOfMarks.getUser(0).size(), 1,
                        1, vectorX0Elements, deviationElementFromMean.get(i));
            }
            vectorX0Users = creatingNewVectorXforUsersOrElements(listOfMarks.size(), 1 / listOfMarks.size(),
                    -1 / listOfMarks.size(), vectorX0Users, deviationUserFromMean.get(listOfFunctions.
                            get(listOfFunctions.size() - 1).getIntegerOfX()));

            vectorX0Elements = creatingNewVectorXforUsersOrElements(listOfMarks.size(), 1 / listOfMarks.getUser(0).size(),
                    -1 / listOfMarks.getUser(0).size(), vectorX0Elements, deviationElementFromMean.
                            get(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX()));

            List<Double> vectorXrUsers = creatingNewVectorXforUsersOrElements(listOfMarks.size(), 1 + alpfa, -alpfa,
                    vectorX0Users, deviationUserFromMean.get(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX()));

            List<Double> vectorXrElements = creatingNewVectorXforUsersOrElements(listOfMarks.getUser(0).size(),
                    1 + alpfa, -alpfa, vectorX0Elements, deviationElementFromMean.
                            get(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX()));

            //calculation f(Xr) (Step 7. Part 2 finding f(Xr))
            Double functionValueOfXr = calculateFunctionOfMinimization(listOfMarks, closestElements, vectorXrUsers,
                    vectorXrElements, proximityMeasure, meanMark, lambda3);

            //Step 8. Check Fr<Fl
            if (functionValueOfXr < listOfFunctions.get(0).valueOfF) {
                //Step 9. Creating Xe
                List<Double> vectorXeUsers = creatingNewVectorXforUsersOrElements(listOfMarks.size(), gamma, 1 - gamma,
                        vectorXrUsers, vectorX0Users);

                List<Double> vectorXeElements = creatingNewVectorXforUsersOrElements(listOfMarks.getUser(0).size(), gamma,
                        1 - gamma, vectorXrElements, vectorX0Elements);
                //calculation f(Xe) (Step 9. Part 2 finding f(Xe))
                Double functionValueOfXe = calculateFunctionOfMinimization(listOfMarks, closestElements, vectorXeUsers,
                        vectorXeElements, proximityMeasure, meanMark, lambda3);

                //Step 10, Part 1. Check f(Xe)<f(Xh)
                if (functionValueOfXe < listOfFunctions.get(0).valueOfF) {
                    //Step 10, Part 2. Replace Xh for Xe
                    deviationUserFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXeUsers);
                    deviationElementFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXeElements);

                    listOfFunctions.get(listOfFunctions.size() - 1).setValueOfF(calculateFunctionOfMinimization(listOfMarks,
                            closestElements, deviationUserFromMean.get(listOfFunctions.size() - 1), deviationElementFromMean.
                                    get(listOfFunctions.size() - 1), proximityMeasure, meanMark, lambda3));
                } else {
                    //Step 11. Replace Xh for Xr
                    deviationUserFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXrUsers);
                    deviationElementFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXrElements);

                    listOfFunctions.get(listOfFunctions.size() - 1).setValueOfF(calculateFunctionOfMinimization(listOfMarks,
                            closestElements, deviationUserFromMean.get(listOfFunctions.size() - 1), deviationElementFromMean.
                                    get(listOfFunctions.size() - 1), proximityMeasure, meanMark, lambda3));
                }
            } else if (functionValueOfXr < listOfFunctions.get(listOfFunctions.size() - 2).valueOfF) //Step 12, Part 1. f(Xr)<f(Xg) or not
            {
                //Step 12, Part 2. Replace Xh for Xr
                deviationUserFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXrUsers);
                deviationElementFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXrElements);

                listOfFunctions.get(listOfFunctions.size() - 1).setValueOfF(calculateFunctionOfMinimization(listOfMarks,
                        closestElements, deviationUserFromMean.get(listOfFunctions.size() - 1), deviationElementFromMean.
                                get(listOfFunctions.size() - 1), proximityMeasure, meanMark, lambda3));
            } else {
                Double functionValueOfXc;
                List<Double> vectorXcUsers;
                List<Double> vectorXcElements;
                if (functionValueOfXr > listOfFunctions.get(listOfFunctions.size() - 1).valueOfF) {
                    //Step 14. Creating Xc

                    vectorXcUsers = creatingNewVectorXforUsersOrElements(listOfMarks.size(), beta, 1 - beta,
                            deviationUserFromMean.get(listOfFunctions.get(listOfFunctions.size() - 1).
                                    getIntegerOfX()), vectorX0Users);

                    vectorXcElements = creatingNewVectorXforUsersOrElements(listOfMarks.getUser(0).size(), beta,
                            1 - beta, deviationElementFromMean.get(listOfFunctions.get(listOfFunctions.size() - 1).
                                    getIntegerOfX()), vectorX0Elements);

                    //calculation f(Xc) (Step 14. Part 2 finding f(Xc))
                    functionValueOfXc = calculateFunctionOfMinimization(listOfMarks, closestElements, vectorXcUsers,
                            vectorXcElements, proximityMeasure, meanMark, lambda3);
                } else {
                    //Step 15. Creating Xc
                    vectorXcUsers = creatingNewVectorXforUsersOrElements(listOfMarks.size(), beta, 1 - beta,
                            vectorXrUsers, vectorX0Users);

                    vectorXcElements = creatingNewVectorXforUsersOrElements(listOfMarks.getUser(0).size(), beta,
                            1 - beta, vectorXrElements, vectorX0Elements);

                    //calculation f(Xc) (Step 14. Part 2 finding f(Xc))
                    functionValueOfXc = calculateFunctionOfMinimization(listOfMarks, closestElements, vectorXcUsers,
                            vectorXcElements, proximityMeasure, meanMark, lambda3);
                }

                //Step 16
                if (functionValueOfXc < listOfFunctions.get(0).valueOfF) {
                    //Step 16 Part 2. Replace Xh for Xc
                    deviationUserFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(), vectorXcUsers);
                    deviationElementFromMean.set(listOfFunctions.get(listOfFunctions.size() - 1).getIntegerOfX(),
                            vectorXcElements);

                    listOfFunctions.get(listOfFunctions.size() - 1).setValueOfF(calculateFunctionOfMinimization(listOfMarks,
                            closestElements, deviationUserFromMean.get(listOfFunctions.size() - 1), deviationElementFromMean.
                                    get(listOfFunctions.size() - 1), proximityMeasure, meanMark, lambda3));
                } else {
                    for (int i = 0; i < (listOfMarks.getUser(0).size() + listOfMarks.size()); i++) {
                        if (deviationElementFromMean.get(i) != deviationElementFromMean.get(listOfFunctions.get(0).integerOfX)) {
                            deviationElementFromMean.set(i, creatingNewVectorXforUsersOrElements(listOfMarks.getUser(0).
                                    size(), beta, beta, deviationElementFromMean.get(i), deviationElementFromMean.
                                    get(listOfFunctions.get(0).integerOfX)));

                            deviationUserFromMean.set(i, creatingNewVectorXforUsersOrElements(listOfMarks.size(),
                                    beta, beta, deviationUserFromMean.get(i), deviationUserFromMean.get(listOfFunctions.get(0).
                                            integerOfX)));

                            listOfFunctions.get(i + 1).setIntegerOfX(i);
                            listOfFunctions.get(i + 1).setValueOfF(calculateFunctionOfMinimization(listOfMarks,
                                    closestElements, deviationUserFromMean.get(i), deviationElementFromMean.get(i),
                                    proximityMeasure, meanMark, lambda3));
                        }
                    }
                }
            }

            //calculating epsilon
            double meanF = 0;
            for (int i = 0; i < listOfFunctions.size(); i++) {
                meanF += listOfFunctions.get(i).valueOfF;
            }
            meanF = meanF / listOfFunctions.size();

            for (int i = 0; i < listOfFunctions.size(); i++) {
                epsilon += (listOfFunctions.get(i).valueOfF - meanF) * (listOfFunctions.get(i).valueOfF - meanF);
            }
            epsilon = Math.pow(epsilon / listOfFunctions.size(), 0.5);

            listOfFunctions.sort(Comparator.comparing(el -> el.valueOfF));
        }

        List<List<Double>> idealMeaningX = new ArrayList<>();
        idealMeaningX.add(deviationElementFromMean.get(listOfFunctions.get(0).integerOfX));
        idealMeaningX.add(deviationUserFromMean.get(listOfFunctions.get(0).integerOfX));

        return idealMeaningX;
    }


    public static List<Double> creatingNewVectorXforUsersOrElements(int sizeVector, double paramLeft, double paramRight,
                                                                    List<Double> vectorLeft, List<Double> vectorRight) {
        List<Double> vectorUsers = DoubleStream.generate(() -> 0).limit(sizeVector).boxed().collect(Collectors.toList());
        for (int j = 0; j < sizeVector; j++) {
            vectorUsers.set(j, paramLeft * vectorLeft.get(j) + paramRight * vectorRight.get(j));
        }
        return vectorUsers;
    }


    public static Double calculateFunctionOfMinimization(ListOfMarks listOfMarks, List<List<Integer>> closestElements,
                                                         List<Double> listDeviationUsers, List<Double> listDeviationElements,
                                                         List<List<Double>> proximityMeasure, Double meanMark, Double lyambda3) {
        Double valueOfFunctionMinimization = new Double(0);

        for (int k = 0; k < listOfMarks.getUser(0).size(); k++) {
            for (int j = 0; j < listOfMarks.size(); j++) {
                if (listOfMarks.getUser(j).getMark(k) != null) {
                    double denominator = 0;
                    double numerator = 0;
                    for (int i = 0; i < closestElements.get(0).size(); i++) {
                        if (listOfMarks.getUser(j).getMark(closestElements.get(k).get(i)) != null){
                            denominator += proximityMeasure.get(k).get(closestElements.get(k).get(i)) *
                                    (listOfMarks.getUser(j).getMark(closestElements.get(k).get(i)) - meanMark -
                                            listDeviationUsers.get(j) - listDeviationElements.get(closestElements.get(k).get(i)));
                            numerator += Math.abs(proximityMeasure.get(k).get(closestElements.get(k).get(i)));
                        }
                    }
                    valueOfFunctionMinimization += Math.pow(listOfMarks.getUser(j).getMark(k) - meanMark - listDeviationUsers.get(j) -
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

        valueOfFunctionMinimization += lyambda3 * (sumD + sumS + sumU);
        return valueOfFunctionMinimization;
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
