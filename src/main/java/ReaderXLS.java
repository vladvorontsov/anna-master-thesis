import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by Anna on 04.11.2018.
 */
public class ReaderXLS {

    public ListOfMarks readTableData(String pathToInitialFile) {
        File file = new File(pathToInitialFile);

        if (!file.exists()) {
            throw new RuntimeException("File: \"" + pathToInitialFile + "\" doesn't exists");
        }

        try (FileInputStream originalFIS = new FileInputStream(pathToInitialFile)) {

            Workbook originalFile = new HSSFWorkbook(originalFIS);
            Sheet originalData = originalFile.getSheetAt(0);

            Iterator<Row> originalDataRows = originalData.rowIterator();
            //Row title = originalDataRows.next();
            ListOfMarks listOfMarks = new ListOfMarks();

            while (originalDataRows.hasNext()) {
                Row line = originalDataRows.next();
                Iterator<Cell> cells = line.cellIterator();
                User user = new User();
                cells.next();
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    if (cell.getNumericCellValue() == 99.) {
                        user.addMark(null);
                    } else {
                        user.addMark(cell.getNumericCellValue());
                    }
                }
                listOfMarks.addUser(user);
            }
            return listOfMarks;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
