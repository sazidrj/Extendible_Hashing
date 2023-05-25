import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetCreator {

    public static final int SALE_AMOUNT_MAX_LIMIT = 500000;
    public static final int SALE_AMOUNT_MIN_LIMIT = 1;
    public static final int NAME_LEN_LIMIT = 3;
    public static final int RANDOM_CATEGORY_START_RANGE = 1;
    public static final int RANDOM_CATEGORY_END_RANGE = 1500;

    private List<Integer> randomCategoryList = new ArrayList<>();

    public static void main(String[] args) {
        DatasetCreator datasetCreator = new DatasetCreator();
        try {
            int totalEntry = 10;
            datasetCreator.initializeRandomCategoryList(RANDOM_CATEGORY_START_RANGE,
                    RANDOM_CATEGORY_END_RANGE, totalEntry);
            datasetCreator.writeSalesRecordFile(totalEntry, "sample");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeRandomCategoryList(int start, int end, int totalEntry) {
        randomCategoryList = new ArrayList<>();
        for (int i = 0; i < totalEntry; i++) {
            randomCategoryList.add(start);
            // if start reaches end and start with beginning
            start = (start == end) ? 1 : start + 1;
        }
    }

    public void writeSalesRecordFile(int totalEntries, String fileName) throws IOException {
        SalesRecord[] salesRecords = getSalesRecord(totalEntries);
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        for (SalesRecord salesRecord : salesRecords) {
            writer.write(salesRecord.salesRecordToLine());
            writer.write("\n");
        }
        writer.close();
    }

    public SalesRecord[] getSalesRecord(int totalEntries) {
        SalesRecord[] salesRecords = new SalesRecord[totalEntries];
        for (int i = 0; i < totalEntries; i++) {
            SalesRecord record = new SalesRecord();
            record.setT_id(i);
            record.setSale_amount(getRandomNumInARange(SALE_AMOUNT_MIN_LIMIT, SALE_AMOUNT_MAX_LIMIT));
            record.setName(getRandomString(NAME_LEN_LIMIT));
            record.setCategory(getRandomCategory());
            salesRecords[i] = record;
        }
        for (int i = 0; i < totalEntries; i++) {
            int first = getRandomNumInARange(0, totalEntries);
            int second = getRandomNumInARange(0, totalEntries);
            SalesRecord temp = salesRecords[first];
            salesRecords[first] = salesRecords[second];
            salesRecords[second] = temp;
        }
        return salesRecords;
    }

    public int getRandomCategory() {
        return randomCategoryList.remove(getRandomNumInARange(0, randomCategoryList.size()));
    }

    private int getRandomNumInARange(int min, int max) {
        return (int) (Math.random() * (max - min));
    }

    private String getRandomString(int len) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            stringBuilder.append((char) (getRandomNumInARange(0, 26) + 'a'));
        }
        return stringBuilder.toString();
    }
}