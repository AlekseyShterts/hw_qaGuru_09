import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FileParsingTest {

    private ClassLoader cl = FileParsingTest.class.getClassLoader();

    @ParameterizedTest(name = "В pdf-файле присуствует текст {0}")
    @ValueSource(strings = {
            "Your Name", "EXPERIENCE", "EDUCATION"
    })
    void pdfFileContainsTextTest(String pdfText) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("example.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("embed-pdf-file.pdf")) {
                    PDF pdfFile = new PDF(zis);
                    assertThat(pdfFile.text).contains(pdfText);
                }
            }
        }
    }

    @Test
    void xlsxFileContainsExpectedCodeValueTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("example.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("example.xlsx")) {
                    XLS xlsFile = new XLS(zis);
                    double actualStringValue = xlsFile.excel.getSheetAt(0).getRow(3).getCell(3).getNumericCellValue();
                    assertThat(actualStringValue).isEqualTo(944);
                }
            }
        }
    }

    @Test
    void csvFileContainsExpectedInformationTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("example.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("animals.csv")) {
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> data = csvReader.readAll();
                    Assertions.assertArrayEquals(
                            new String[]{"badger", "beaver", "boar", "cheetah"}, data.get(0));
                    Assertions.assertArrayEquals(
                            new String[]{"", "", "", ""}, data.get(2));
                }
            }
        }
    }

    @CsvSource(value = {
            "1 : News",
            "2 : Weather",
            "3 : Sport",
            "4 : Films"
    }, delimiter = ':')

    @ParameterizedTest(name = "У id={0}, называние {1}")
    void jsonFileContainsExpectedRouterTest(int expectedRouterId, String expectedSegment) throws Exception {
        try (InputStream is = cl.getResourceAsStream("simple.json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(is);
            JsonNode routerNode = rootNode.get("router");
            JsonNode router = routerNode.get(expectedRouterId - 1);

            Assertions.assertEquals(expectedRouterId, router.get("id").asInt());
            Assertions.assertEquals(expectedSegment, router.get("segment").asText());
        }
    }

}
