/**
 * Created by Anna on 04.11.2018.
 */
public class Main {
    private static final ReaderXLS readerXLS = new ReaderXLS();
    public static void main(String[] args) {
        ListOfMarks listOfMarks = readerXLS.readTableData("src/main/resources/jester-data-1.xls");
        System.out.println(listOfMarks);
    }
}
