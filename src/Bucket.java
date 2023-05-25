import java.util.*;

public class Bucket {
    private int bucket_no;
    private int localDepth;
    private int size;
    private int current_index;
    private String hash_prefix;
    private SalesRecord[] records;
    private boolean overflow = false;
    private Bucket overflowBucket;

    public String getHash_prefix() {
        return hash_prefix;
    }

    public void setHash_prefix(String hash_prefix) {
        this.hash_prefix = hash_prefix;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    public Bucket getOverflowBucket() {
        return overflowBucket;
    }

    public void setOverflowBucket(Bucket overflowBucket) {
        this.overflowBucket = overflowBucket;
    }

    public int getCurrent_index() {
        return current_index;
    }

    public void setCurrent_index(int current_index) {
        this.current_index = current_index;
    }

    public Bucket() {
    }

    public Bucket(int localDepth, int size, int bucket_index) {
        this.localDepth = localDepth;
        this.bucket_no = bucket_index;
        this.size = size;
        this.records = new SalesRecord[size];
        this.current_index = 0;
        this.setHash_prefix("");
    }


    public boolean isFull(){
        if(this.current_index < this.size){
            return false;
        }
        return true;
    }

    public boolean isEmpty(){
        return this.current_index == 0;
    }

    public boolean insertNewRecord(SalesRecord record){
        if(!this.isFull()){
            this.records[this.current_index] = record;
            this.current_index++;
            return true;
        }else if(this.isOverflow()){
            Bucket overflowBucket = this.getOverflowBucket();
            while(overflowBucket != null){
                if(!overflowBucket.isFull()){
                    this.overflowBucket.insertNewRecord(record);
                    return true;
                }
                overflowBucket = overflowBucket.getOverflowBucket();
            }
            return false;
        }

        return false;
    }

    public int getBucket_no() {
        return bucket_no;
    }

    public void setBucket_no(int bucket_no) {
        this.bucket_no = bucket_no;
    }

    public int getLocalDepth() {
        return localDepth;
    }

    public void setLocalDepth(int localDepth) {
        this.localDepth = localDepth;
    }


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SalesRecord[] getRecords() {
        return records;
    }

    public void setRecords(SalesRecord[] records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "bucket_no=" + bucket_no +
                ", localDepth=" + localDepth +
                ", size=" + size +
                ", current_record=" + current_index +
                // ", records=" + Arrays.toString(records) +
                ", isOverFlow=" + overflow +
                '}';
    }

}
