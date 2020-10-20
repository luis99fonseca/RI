import java.util.Map;

public class SimpleTokenizer implements Tokenizer {

    @Override
    public String[] process_tokens(String corpus) {
        return corpus.replaceAll("[^A-Za-z0-9]", " ")   // remove non-alphabetic chars
                .replaceAll("\\b\\w{1,3}\\b\\s?", "")   // remove words less 3 chars
                .toLowerCase()                                              // lowercase
                .split("\\s+");                                     // split whitespaces
    }
}
