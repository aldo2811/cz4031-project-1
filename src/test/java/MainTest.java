import com.cz4031.BPlusTree;
import com.cz4031.RecordAddress;
import com.cz4031.Storage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class MainTest {
    Storage st;
    BPlusTree bpt;
    HashMap<Integer, Integer> hm;

    @BeforeEach
    void setUp() {
        String path = "test_data.tsv";
        st = new Storage();
        st.initWithTSV(path);
        bpt = st.buildIndex();

        hm = new HashMap<>();
        try {
            Reader in = new FileReader(path);
            Iterable<CSVRecord> records = CSVFormat.TDF.builder().setHeader().setSkipHeaderRecord(true).build().parse(in);

            for (CSVRecord record : records) {
                hm.put(Integer.parseInt(record.get("numVotes")), hm.getOrDefault(Integer.parseInt(record.get("numVotes")), 0) + 1);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Wrong file path");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error while reading file");
            e.printStackTrace();
        }
    }

    @TestFactory
    @DisplayName("Ensure that the number of records in the tree matches the number of records in the tsv")
    Stream<DynamicTest> checkNumberRecord() {
         return hm.entrySet().stream().map(e -> {
            List<RecordAddress> recordAddresses = bpt.search(e.getKey());
            return dynamicTest(String.format("record %d, expected %d", e.getKey(), e.getValue()), () -> assertEquals(e.getValue().intValue(), recordAddresses.size(), String.format("mismatch of record %d, actual %d, found %d\n", e.getKey(), e.getValue(), recordAddresses.size()))
            );
        });
    }

    @Test
    @DisplayName("Ensure that after deletion, the list are empty")
    void emptyAfterDelete() {
        for(int i = 10 ; i <= 200 ; i += 10) {
            bpt.delete(i);
            List<RecordAddress> rr = bpt.search(i);
            assertTrue(rr.isEmpty(), String.format("not empty after deleting %d\n", i));
        }
    }
}