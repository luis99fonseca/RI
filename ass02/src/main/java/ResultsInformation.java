public class ResultsInformation {

    private double precision;
    private double recall;
    private double f1;

    public ResultsInformation(double precision, double recall) {
        this.precision = precision;
        this.recall = recall;
        this.f1 = (2*precision*recall) / (recall + precision);
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1() {
        return f1;
    }

    @Override
    public String toString() {
        return "ResultsInformation{" +
                "precision=" + precision +
                ", recall=" + recall +
                ", f1=" + f1 +
                '}';
    }
}
