import java.util.Map;

public class SimpleTokenizer implements Tokenizer {

    @Override
    public String[] process_tokens(String corpus) {
        return corpus.replaceAll("[^a-zA-Z]", " ")      // remove non-alphabetic chars
                .replaceAll("\\b\\w{1,2}\\b\\s?", "")   // remove words less 2 chars
                .trim()
                .toLowerCase()                                            // lowercase
                .split("\\s+");                                    // split whitespaces
    }
}
