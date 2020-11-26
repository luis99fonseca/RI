public class ResultsInformation {

    private double precision;
    private double avg_precision;
    private double recall;
    private double f1;

    public ResultsInformation(double avg_precision, double precision, double recall) {
        this.avg_precision = avg_precision;
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
                "avg_precision=" + avg_precision +
                "precision=" + precision +
                ", recall=" + recall +
                ", f1=" + f1 +
                '}';
    }
}