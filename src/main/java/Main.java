import java.util.List;

/**
 * Created by Anna on 04.11.2018.
 */
public class Main {
    private static final ReaderXLS readerXLS = new ReaderXLS();

    public static void main(String[] args) {
        ListOfMarks listOfMarks = readerXLS.readTableData("src/main/resources/testData.xls");
        //System.out.println(listOfMarks);
        List<Double> meanMarks = CalculationServise.calculatMeanMarksList(listOfMarks);
        List<List<Double>> proximityMeasure = CalculationServise.countProximityMeasurePirson(listOfMarks, meanMarks, 1);

        for (List<Double> list : proximityMeasure) {
            for (Double value : list) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
        proximityMeasure.stream().flatMap(el -> el.stream()).forEach(el -> {
            if (el != null && el.compareTo(0.) < 0) {
                System.out.println("Pearson less than 0");
            }
        });
        List<List<Integer>> closestElements = CalculationServise.getClosestElements(proximityMeasure);
        for (List<Integer> list : closestElements) {
            for (Integer value : list) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
        Double meanMark = CalculationServise.calculatMeanMark(listOfMarks);
        List<List<Double>> paramBandD = CalculationServise.getParamBandD(listOfMarks,closestElements,meanMark,
                proximityMeasure,0.1);
    }
}
