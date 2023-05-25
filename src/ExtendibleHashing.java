import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;

public class ExtendibleHashing {
    final static int BUCKET_SIZE = 10;
    static int global_depth = 0;
    static int bucket_index = 1;
    final static int N = 10;
    static int MAX_LENGTH_OF_HASHKEY = 0;

    static Map<String, Bucket> bucketAddressTable = new TreeMap<>();
    static Bucket[] BucketList = new Bucket[100000];

    public static void initialiseBuckets(){
        for(int i = 0; i<100000; i++){
            Bucket bucket = new Bucket(0,BUCKET_SIZE, i);
            BucketList[i] = bucket;
        }
    }

    public static void setMaxLengthOfHashKey(){
//        int num = N, cnt = 0;
//        while(num>0){
//            cnt++;
//            num = num>>1;
//        }
        MAX_LENGTH_OF_HASHKEY = 16;
    }

    public static String getHashTransactionKey(int t_id){
        String idInBinary = Integer.toBinaryString(t_id);
        int len = idInBinary.length();
        int diff = MAX_LENGTH_OF_HASHKEY - len;

        StringBuilder stb = new StringBuilder(idInBinary);

        for(int i = 0; i<diff; i++){
            stb.insert(0,'0');
        }

        return stb.toString();
    }

    public static int findNoOfRecordsInDatabase() {
        int count = 0;

        Bucket bucket = null;

        if(bucketAddressTable.size() == 0){
            bucket = BucketList[0];

            return bucket.getCurrent_index();
        }

        for (Map.Entry<String, Bucket> elm : bucketAddressTable.entrySet()) {
            String key = elm.getKey();
            Bucket value = elm.getValue();

            if (bucket == null) {
                bucket = value;

                Bucket iterator = bucket;
                while (iterator != null) {
                    count += iterator.getCurrent_index();
                    iterator = iterator.getOverflowBucket();
                }
            } else if (value != bucket) {
                bucket = value;

                Bucket iterator = bucket;
                while (iterator != null) {
                    count += iterator.getCurrent_index();
                    iterator = iterator.getOverflowBucket();
                }
            }
        }


        return count;
    }

    public static void printStatus() {
        System.out.println("Global Depth = " + global_depth);
        System.out.println("Bucket Address Table Size : " + bucketAddressTable.size());
        System.out.println("--------------Bucket Address Table----------------------");
        for (Map.Entry<String, Bucket> elm : bucketAddressTable.entrySet()) {
            String key = elm.getKey();
            Bucket value = elm.getValue();
            System.out.println("Key = " + key + " value = " + value.getHash_prefix());

            System.out.println("-----Bucket Data-----");
            for(int i = 0; i< value.getCurrent_index(); i++){
                System.out.println(value.getRecords()[i].toString());
            }

            System.out.println("-----Overflow Buckets Data-----");
            Bucket it = value.getOverflowBucket();
            while(it != null){
                for(int i = 0; i<it.getCurrent_index(); i++){
                    System.out.println(it.getRecords()[i].toString());
                }
                it = it.getOverflowBucket();
                System.out.println("-----------------------------------------------");
            }
            System.out.println();

        }

        int no_of_records = findNoOfRecordsInDatabase();
        System.out.println("Current number of record = " + no_of_records);
    }

    public static void increaseHashTable() {
        Map<String, Bucket> newHashMap = new TreeMap<>();

        for (int i = 0; i < (1 << global_depth); i++) {
            StringBuilder key = new StringBuilder(Integer.toBinaryString(i));
            if (key.length() < global_depth) {
                int diff = global_depth - key.length();
                while (diff > 0) {
                    key.insert(0, '0');
                    diff--;
                }
            }

            newHashMap.put(key.toString(), null);
        }

        if(bucketAddressTable.size() == 0){
            newHashMap.put("0", BucketList[0]);
            newHashMap.put("1", BucketList[0]);
            bucketAddressTable = newHashMap;
            return;
        }

        for (Map.Entry<String, Bucket> elm : bucketAddressTable.entrySet()) {
            StringBuilder oldKey = new StringBuilder(elm.getKey());
            Bucket value = elm.getValue();

            oldKey.append('0');
            newHashMap.put(oldKey.toString(), value);
            oldKey.deleteCharAt(oldKey.length() - 1);
            oldKey.append('1');
            newHashMap.put(oldKey.toString(), value);
        }

        bucketAddressTable = newHashMap;
    }

    public static void insertRecordToOverflowBucket(Bucket bucket, SalesRecord record) {

        if (!bucket.isOverflow()) {
            bucket.setOverflow(true);
            Bucket overflowBucket = BucketList[bucket_index++];
            overflowBucket.setLocalDepth(bucket.getLocalDepth());
            overflowBucket.setHash_prefix(bucket.getHash_prefix());
            overflowBucket.insertNewRecord(record);
            bucket.setOverflowBucket(overflowBucket);
        } else {
            Bucket iterator = bucket;
            while (iterator.getOverflowBucket() != null) {
                iterator = iterator.getOverflowBucket();
            }

            iterator.setOverflow(true);
            Bucket overflowBucket = BucketList[bucket_index++];
            overflowBucket.setLocalDepth(bucket.getLocalDepth());
            overflowBucket.setHash_prefix(bucket.getHash_prefix());
            overflowBucket.insertNewRecord(record);
            iterator.setOverflowBucket(overflowBucket);
        }
    }

    public static Bucket[] reHashRecords(Bucket bucket, int local_depth, String hash_old_bucket, String hash_new_bucket) {
        Bucket[] returnNewBuckets = new Bucket[2];

        Bucket newbucket1 = BucketList[bucket_index++];
        newbucket1.setLocalDepth(local_depth);
        newbucket1.setHash_prefix(hash_old_bucket);

        Bucket newbucket2 = BucketList[bucket_index++];
        newbucket2.setLocalDepth(local_depth);
        newbucket2.setHash_prefix(hash_new_bucket);

        while (bucket != null) {
            SalesRecord[] sr = bucket.getRecords();
            for (SalesRecord record : sr) {
                int id = record.t_id;
                String hash = getHashTransactionKey(id).substring(0, local_depth);
                if (hash.equals(hash_old_bucket)) {
                    boolean inserted = newbucket1.insertNewRecord(record);
                    if (inserted == false) {
                        insertRecordToOverflowBucket(newbucket1, record);
                    }
                } else {
                    boolean inserted = newbucket2.insertNewRecord(record);
                    if (inserted == false) {
                        insertRecordToOverflowBucket(newbucket2, record);
                    }
                }
            }
            bucket = bucket.getOverflowBucket();
        }

        returnNewBuckets[0] = newbucket1;
        returnNewBuckets[1] = newbucket2;

        return returnNewBuckets;

    }

    public static void splitBucket(Bucket bucket) {
        int local_depth = bucket.getLocalDepth();
        local_depth = local_depth + 1;

        bucket.setLocalDepth(local_depth);

        StringBuilder stb1 = new StringBuilder(bucket.getHash_prefix());
        stb1.append('0');
        StringBuilder stb2 = new StringBuilder(bucket.getHash_prefix());
        stb2.append('1');

        Bucket[] newBucketArr = reHashRecords(bucket, local_depth, stb1.toString(), stb2.toString());


        for (Map.Entry<String, Bucket> elm : bucketAddressTable.entrySet()) {
            StringBuilder key = new StringBuilder(elm.getKey());
            Bucket value = elm.getValue();

            if (key.substring(0, local_depth).equals(stb1.toString())) {
                bucketAddressTable.put(key.toString(), newBucketArr[0]);
            } else if (key.substring(0, local_depth).equals(stb2.toString())) {
                bucketAddressTable.put(key.toString(), newBucketArr[1]);
            }
        }


    }

    public static void insertRecord(SalesRecord record) {

        /*
           if bucket and its overflow buckets is not full
                insert
           else if(globalDepth == localDepth)
                globalDepth++, reasign pointer and insert  -> overflow -> split -> reinsert -> overflow -> chaining
                                                                                                  |
                                                                                                  insert

           else
               split -> overflow -> chaining
                           |
                           insert
         */

        int id = record.getT_id();
        String hash_prefix = getHashTransactionKey(id).substring(0, global_depth);

        Bucket bucket;

        if (global_depth == 0 && bucketAddressTable.size() == 0) {
            bucket = BucketList[0];
        } else {
            bucket = bucketAddressTable.get(hash_prefix);
        }

        boolean inserted = bucket.insertNewRecord(record);
        if (inserted == true) {
            return;
        } else {
            int local_depth = bucket.getLocalDepth();
            if (global_depth == local_depth) {
                global_depth++;
                increaseHashTable();

                String newHashPrefix = getHashTransactionKey(record.t_id).substring(0, global_depth);
                bucket = bucketAddressTable.get(newHashPrefix);
                boolean reinsertion = bucket.insertNewRecord(record);

                if (reinsertion == true) {
                    return;
                } else {
                    splitBucket(bucket);
                    newHashPrefix = getHashTransactionKey(record.t_id).substring(0,global_depth);
                    bucket = bucketAddressTable.get(newHashPrefix);
                    boolean secondReinsertion = bucket.insertNewRecord(record);
                    if (secondReinsertion == true) {
                        return;
                    } else {
                        insertRecordToOverflowBucket(bucket, record);
                    }
                }

            } else {
                splitBucket(bucket);

                String newHashPrefix = getHashTransactionKey(record.t_id).substring(0,global_depth);
                bucket = bucketAddressTable.get(newHashPrefix);

                boolean reinsertion = bucket.insertNewRecord(record);
                if (reinsertion == true) {
                    return;
                } else {
                    insertRecordToOverflowBucket(bucket, record);
                }
            }
        }
    }

    public static void main(String arg[]) throws IOException {
        initialiseBuckets();
        setMaxLengthOfHashKey();
        DatasetCreator datasetCreator = new DatasetCreator();
        datasetCreator.initializeRandomCategoryList(DatasetCreator.RANDOM_CATEGORY_START_RANGE, DatasetCreator.RANDOM_CATEGORY_END_RANGE, N);
        SalesRecord[] salesRecords = datasetCreator.getSalesRecord(N);

        try{
            BufferedReader br = new BufferedReader(new FileReader("sample"));
            String line = null;
            while((line = br.readLine()) != null){
                String[] temp = line.split(" ");
                SalesRecord record = new SalesRecord();
                record.t_id = Integer.parseInt(temp[0]);
                record.sale_amount = Integer.parseInt(temp[1]);
                record.name = temp[2];
                record.category = Integer.parseInt(temp[3]);
//                System.out.println("=============================================================================");
//                System.out.println(record.salesRecordToLine());
                insertRecord(record);
                //  printStatus();
//                System.out.println("Global Depth = " + global_depth);
            }
            printStatus();
        }catch(IOException e){
            e.printStackTrace();
        }
//        for (SalesRecord record : salesRecords) {
//            System.out.println("=============================================================================");
//            System.out.println(record.salesRecordToLine());
//            insertRecord(record);
//            printStatus();
//        }
    }
}
