import java.util.List;

/**
 * Created by Anna on 04.11.2018.
 */
public class Main {
    private static final ReaderXLS readerXLS = new ReaderXLS();

    public static void main(String[] args) {
        ListOfMarks listOfMarks = readerXLS.readTableData("src/main/resources/jester-data-1.xls");
        //System.out.println(listOfMarks);
        List<Double> meanMarks = CalculationServise.calculatMeanMark(listOfMarks);
        List<List<Double>> proximityMeasure = CalculationServise.countProximityMeasurePirson(listOfMarks, meanMarks, 1);
        for (List<Double> list : proximityMeasure) {
            for (Double value : list) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
        List<List<Integer>> closestElements = CalculationServise.getClosestElements(proximityMeasure);
        for (List<Integer> list : closestElements) {
            for (Integer value : list) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
