import java.io.File;
import java.io.IOException;
import java.util.*;

public class PrintingMain {

    public static void main(String[] args) throws IOException {

        Map<String, ResultsInformation> results_rank10 = new HashMap<>();
        Map<String, ResultsInformation> results_rank20 = new HashMap<>();
        Map<String, ResultsInformation> results_rank50 = new HashMap<>();

        Random r = new Random();

        String measures[] = new String[]{
                "", "Precision", "Recall", "F-measure", "Avg Precision", "NDCG", "\tLatency"};

        int queries = 3;

        for (int i = 0; i < queries; i++) {
            results_rank10.put(i + "", new ResultsInformation(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()));
            results_rank20.put(i + "", new ResultsInformation(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()));
            results_rank50.put(i + "", new ResultsInformation(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble()));
        }

        // MAIN HEADER
        for (String m : measures) {
            if (m.isEmpty()) {
                System.out.printf("%10s", "");
            } else {
                System.out.printf("%21s", m);
            }
        }
        System.out.println();

        // SECUNDARY HEADER
        for (int i = 0; i < measures.length; i++) {
            if (i == 0) {
                System.out.printf("%10s", "Query #");
            } else if (i == measures.length - 1) {
                System.out.printf("%21s", "");
            } else {
                System.out.printf("%7s %7s %7s", "@10", "@20", "@30");
            }
        }
        System.out.println();
        for (int line = 0; line < queries; line++) {

            // RESULTS
            for (int i = 0; i < measures.length; i++) {
                if (i == 0) {
                    System.out.printf("%10d", line);
                } else if (i == measures.length - 1) {
                    System.out.printf("%21.2f", 231312.1);
                } else {
                    double value01;
                    double value02;
                    double value03;
                    switch (i) {
                        case 1:
                            value01 = results_rank10.get(line + "").getPrecision();
                            value02 = results_rank20.get(line + "").getPrecision();
                            value03 = results_rank50.get(line + "").getPrecision();
                            break;
                        case 2:
                            value01 = results_rank10.get(line + "").getRecall();
                            value02 = results_rank20.get(line + "").getRecall();
                            value03 = results_rank50.get(line + "").getRecall();
                            break;
                        case 3:
                            value01 = results_rank10.get(line + "").getF1();
                            value02 = results_rank20.get(line + "").getF1();
                            value03 = results_rank50.get(line + "").getF1();
                            break;
                        case 4:
                            value01 = results_rank10.get(line + "").getAvg_precision();
                            value02 = results_rank20.get(line + "").getAvg_precision();
                            value03 = results_rank50.get(line + "").getAvg_precision();
                            break;
                        case 5:
                            value01 = results_rank10.get(line + "").getNdcg();
                            value02 = results_rank20.get(line + "").getNdcg();
                            value03 = results_rank50.get(line + "").getNdcg();
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + i);
                    }

                    System.out.printf("%7.3f %7.3f %7.3f",
                            value01,
                            value02,
                            value03);
                }
            }

            System.out.println();

        }

//                  MEAN
        for (int i = 0; i < measures.length; i++) {
            if (i == 0) {
                System.out.printf("%10s", "Mean");
            } else if (i == measures.length - 1) {
                System.out.printf("%21.2f", 6969.0);
            } else {
                double value01 = 0;
                double value02 = 0;
                double value03 = 0;
                switch (i) {
                    case 1:
                        for (int q = 0; q < queries; q++) {
                            value01 += results_rank10.get(q + "").getPrecision();
//                            System.out.println(">>" + results_rank10.get(q + "").getPrecision());
                            value02 += results_rank20.get(q + "").getPrecision();
                            value03 += results_rank50.get(q + "").getPrecision();
                        }
                        break;
                    case 2:
                        for (int q = 0; q < queries; q++) {
                            value01 += results_rank10.get(q + "").getRecall();
                            value02 += results_rank20.get(q + "").getRecall();
                            value03 += results_rank50.get(q + "").getRecall();
                        }
                        break;
                    case 3:
                        for (int q = 0; q < queries; q++) {
                            value01 += results_rank10.get(q + "").getF1();
                            value02 += results_rank20.get(q + "").getF1();
                            value03 += results_rank50.get(q + "").getF1();
                        }
                        break;
                    case 4:
                        for (int q = 0; q < queries; q++) {
                            value01 += results_rank10.get(q + "").getAvg_precision();
                            value02 += results_rank20.get(q + "").getAvg_precision();
                            value03 += results_rank50.get(q + "").getAvg_precision();
                        }
                        break;
                    case 5:
                        for (int q = 0; q < queries; q++) {
                            value01 += results_rank10.get(q + "").getNdcg();
                            value02 += results_rank20.get(q + "").getNdcg();
                            value03 += results_rank50.get(q + "").getNdcg();
                        }
                        break;
                }
                System.out.printf("%7.3f %7.3f %7.3f",
                        value01 / (queries),
                        value02 / (queries),
                        value03 / (queries));
            }
        }
    }
}



