public class SalesRecord {

    int t_id;
    int sale_amount;
    String name;
    int category;

    public int getT_id() {
        return t_id;
    }

    public void setT_id(int t_id) {
        this.t_id = t_id;
    }

    public int getSale_amount() {
        return sale_amount;
    }

    public void setSale_amount(int sale_amount) {
        this.sale_amount = sale_amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String salesRecordToLine() {
        return t_id + " " + sale_amount + " " + name + " " + category;
    }

    @Override
    public String toString() {
        return "SalesRecord{" +
                "t_id=" + t_id +
                ", sale_amount=" + sale_amount +
                ", name='" + name + '\'' +
                ", category=" + category +
                '}';
    }
}
