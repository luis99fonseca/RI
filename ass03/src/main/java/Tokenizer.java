public interface Tokenizer {

    /*
    As the name indicates, this method it's responsible for
    processing the words received from documents into clean tokens.
    The complexity of this method depends of the implementation.
     */
    public String[] process_tokens(String corpus);
}
