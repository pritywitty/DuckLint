import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseText {
    ArrayList<ArrayList<Character>> linesAsChars = new ArrayList<>();
    ArrayList<OneLine> linesOfCodeInTypes = new ArrayList<>();
    ArrayList<String> lines = new ArrayList<>();
    ArrayList<Character> charsInLine;
    StringBuilder stringBuilder;
    Scanner scanner;
    OneLine thisLine; //TODO change var name

    String theLine, sendString = "";
    int indent = 0, index = 0, indexIncrease = 0;
    char charB = '\0', charA;
    boolean lineComment = false, blockComment = false, literalString = false, literalChar = false,
            codeBlock = true, EOL = false, emptySpace = false, header = false,
            includeLibrary = false, define = false, newLine = false;

    ParseText(String allCode) {
        scanner = new Scanner(allCode);
        while (scanner.hasNextLine()) {
            theLine = scanner.nextLine();
            lines.add(theLine);
        }

        for (String line : lines) {
            charsInLine = new ArrayList<>();
            for (int i = 0; i < line.length(); i++) { charsInLine.add(line.charAt(i)); }
            linesAsChars.add(charsInLine);
        }

        breakIntoTypes();
    } // ParseText

    void addNewLine(StringBuilder stringBuilder){
        stringBuilder.append("\n");
        index++;
    } // addNewLine

    void addCharAndIncrement(StringBuilder stringBuilder, char character){
        if(character != '\0') {
            stringBuilder.append(character);
            index++;
        }
    } // addCharAndIncrement

    void addLastLine(StringBuilder s, OneLine.lineType lineType){
        sendString = s.toString();
        thisLine = new OneLine(lineType, sendString, indent, index);
        linesOfCodeInTypes.add(thisLine);
    } // addLastLine

    //function to parse chars / identify elements / categorize
    void breakIntoTypes() {
        stringBuilder = new StringBuilder();
        Pattern pattern = Pattern.compile("\s");
        Matcher matcherB, matcherA;
        String lastCharString, thisCharString;
        OneLine.lineType
                coType = OneLine.lineType.code,
                stType = OneLine.lineType.string_literal,
                chType = OneLine.lineType.char_literal,
                hType = OneLine.lineType.header,
                lcType = OneLine.lineType.line_comment,
                bcType = OneLine.lineType.block_comment;

        for (ArrayList<Character> lineOfChars : linesAsChars) {
            for (int i = 0; i < lineOfChars.size(); i++) {
                do { //remove whitespace
                    charA = charB;
                    charB = lineOfChars.get(i);
                    if(charA == '\t') { charA = ' '; }
                    if(charB == '\t') { charB = ' '; }
                    if(charA == '\n') { charA = '\0'; }
                    if(charB == '\n') { charB = '\0'; }

                    EOL = i + 1 == lineOfChars.size();
                    lastCharString = charA + "";
                    matcherA = pattern.matcher(lastCharString);
                    thisCharString = charB + "";
                    matcherB = pattern.matcher(thisCharString);
                    emptySpace = matcherA.matches() && matcherB.matches();
                    if (!EOL && emptySpace) { i++; }
                    else if (EOL && emptySpace) { charB = '\0'; }
                } while (emptySpace && !EOL);

                if (codeBlock) {
                    switch (charA){
                        case '"':
                            if(stringBuilder.length() > 0) {
                                addLastLine(stringBuilder, coType);
                                stringBuilder = new StringBuilder();
                            }
                            addCharAndIncrement(stringBuilder,charA); addCharAndIncrement(stringBuilder, charB);
                            if(charB == '"'){
                                addLastLine(stringBuilder, stType);
                                stringBuilder = new StringBuilder();
                                charB = '\0';
                            }
                            else if(i + 1 == lineOfChars.size() && charB != '\\'){
                                addNewLine(stringBuilder); addLastLine(stringBuilder, stType);
                                stringBuilder = new StringBuilder(); newLine = true;
                                charB = '\0';
                            }
                            else { literalString = true; codeBlock = false; }
                            break;
                        case '\'': literalChar = true; codeBlock = false;
                            if(stringBuilder.length() > 0) {
                                addLastLine(stringBuilder, coType);
                                stringBuilder = new StringBuilder();
                            }
                            addCharAndIncrement(stringBuilder,charA);
                            break;
                        case '#':
                            if(stringBuilder.length() > 0) {
                                addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
                                stringBuilder = new StringBuilder(); newLine = true;
                            }
                            addCharAndIncrement(stringBuilder,charA);
                            if(i + 1 == lineOfChars.size()) {
                                addCharAndIncrement(stringBuilder,charB);
                                addNewLine(stringBuilder); addLastLine(stringBuilder, hType);
                                stringBuilder = new StringBuilder(); newLine = true;
                                charB = '\0';
                            }
                            else { header = true; codeBlock = false; }
                            break;
                        case '{': // new block
                            if(stringBuilder.length() > 0) {
                                addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
                                stringBuilder = new StringBuilder(); newLine = true;
                            }
                            addCharAndIncrement(stringBuilder,charA);
                            addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
                            stringBuilder = new StringBuilder(); newLine = true;
                            indent++;
                            break;
                        case '}': // end of block
                            if(stringBuilder.length() > 0) {
                                addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
                                stringBuilder = new StringBuilder(); newLine = true;
                            }
                            indent--;
                            addCharAndIncrement(stringBuilder,charA);
                            addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
                            stringBuilder = new StringBuilder(); newLine = true;
                            break;
                        case ';': // end of a statement
                            addCharAndIncrement(stringBuilder,charA);
                            addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
                            stringBuilder = new StringBuilder(); newLine = true;
                            break;
                        case '/':
                            if (charB == '/') {
                                if(stringBuilder.length() > 0) {
                                    addNewLine(stringBuilder);
                                    addLastLine(stringBuilder, coType);
                                    stringBuilder = new StringBuilder(); newLine = true;
                                }
                                addCharAndIncrement(stringBuilder, charA); addCharAndIncrement(stringBuilder, charB);
                                if(i + 1 == lineOfChars.size()){
                                    addNewLine(stringBuilder);
                                    addLastLine(stringBuilder, lcType);
                                    stringBuilder = new StringBuilder(); newLine = true;
                                    charB = '\0';
                                }
                                else { lineComment = true; codeBlock = false; }
                            } // line comment
                            else if (charB == '*') { blockComment = true; codeBlock = false;
                                if(stringBuilder.length() > 0) {
                                    addNewLine(stringBuilder);
                                    addLastLine(stringBuilder, coType);
                                    newLine = true;
                                }
                                stringBuilder = new StringBuilder();
                                addCharAndIncrement(stringBuilder, charA); addCharAndIncrement(stringBuilder, charB);
                            } // block comment
                            else { addCharAndIncrement(stringBuilder, charA); } // not a comment
                            break;
                        default:
                            if (charA != '\n' && charA != '\0') {
                                if(stringBuilder.length() != 0 || charA != ' ') {
                                    addCharAndIncrement(stringBuilder, charA);
                                }
                            }
                            break;
                    } // switch
                } // codeBlock

                //TODO
/*
#define \
myfunc(arg1) \
std::cout << arg1;
* */
                else if (header) {
                    if(!includeLibrary && !define) {
                        if(stringBuilder.indexOf("include") > 0){ includeLibrary = true; }
                        int macro = stringBuilder.length();
                        if(stringBuilder.indexOf("if") > 0 ||
                            stringBuilder.indexOf("elif") > 0 ||
                            stringBuilder.indexOf("else") > 0 ||
                            stringBuilder.indexOf("line") > 0 ||
                            stringBuilder.indexOf("ifdef") > 0 ||
                            stringBuilder.indexOf("undef") > 0 ||
                            stringBuilder.indexOf("endif") > 0 ||
                            stringBuilder.indexOf("error") > 0 ||
                            stringBuilder.indexOf("define") > 0 ||
                            stringBuilder.indexOf("ifndef") > 0 ||
                            stringBuilder.indexOf("pragma") > 0){ define = true; }

                        if(includeLibrary && define){
                            define = stringBuilder.indexOf("include") > 2;
                            includeLibrary = !define;
                        }
                    }

                    if(charA == '/' && !define) {
                        if (charB == '/') {
                            addNewLine(stringBuilder); addLastLine(stringBuilder, hType);
                            stringBuilder = new StringBuilder(); newLine = true;
                            addCharAndIncrement(stringBuilder, charA); addCharAndIncrement(stringBuilder, charB);
                            if (i + 1 == lineOfChars.size()) {
                                addNewLine(stringBuilder); addLastLine(stringBuilder, lcType);
                                stringBuilder = new StringBuilder(); newLine = true;
                                charB = '\0';
                                lineComment = false; header = false; codeBlock = true;
                            }
                            else { lineComment = true; header = false; define = false; includeLibrary = false; }
                        } // line comment
                        else if (charB == '*') {
                            blockComment = true; header = false; define = false; includeLibrary = false;
                            addNewLine(stringBuilder); addLastLine(stringBuilder, hType);
                            stringBuilder = new StringBuilder(); newLine = true;
                            addCharAndIncrement(stringBuilder, charA); addCharAndIncrement(stringBuilder, charB);
                        } // block comment
                        else { addCharAndIncrement(stringBuilder, charA); } // not a comment
                    }
                    else{ addCharAndIncrement(stringBuilder, charA); }

                    if (i + 1 == lineOfChars.size()){
                        if(!define || charB != '\\') {
                            addCharAndIncrement(stringBuilder, charB); addNewLine(stringBuilder);
                            addLastLine(stringBuilder, hType);
                            stringBuilder = new StringBuilder(); newLine = true;
                            define = false; includeLibrary = false; header = false; codeBlock = true;
                            charB = '\0';
                        }
                        else {
                            addCharAndIncrement(stringBuilder, charB);
                            addCharAndIncrement(stringBuilder, ' ');
                            define = true; header = true;
                            charB = '\0';
                        }
                    }
                } // header

                else if (lineComment) {
                    addCharAndIncrement(stringBuilder,charB);
                    if (i + 1 == lineOfChars.size()) {
                        addNewLine(stringBuilder); addLastLine(stringBuilder, lcType);
                        stringBuilder = new StringBuilder(); newLine = true;
                        lineComment = false; codeBlock = true;
                        charB = '\0';
                    }
                } // line comment

                else if (blockComment) {
                    addCharAndIncrement(stringBuilder,charB);
                    if (charA == '*' && charB == '/') {
                        addNewLine(stringBuilder); addLastLine(stringBuilder, bcType);
                        stringBuilder = new StringBuilder(); newLine = true;
                        blockComment = false; codeBlock = true;
                        charB = '\0';

                    }
                } // block comment

                else if (literalString) {
                    addCharAndIncrement(stringBuilder,charB);
                    if (charB == '"' && charA != '\\') {
                            addLastLine(stringBuilder, stType);
                            stringBuilder = new StringBuilder();
                            literalString = false; codeBlock = true;
                            charB = '\0';
                    }
                    else if(i + 1 == lineOfChars.size() && charB != '\\'){
                        addNewLine(stringBuilder); addLastLine(stringBuilder, stType);
                        stringBuilder = new StringBuilder(); newLine = true;
                        literalString = false; codeBlock = true;
                        charB = '\0';
                    }
                } //string literal

                //TODO : char literals can only be up to 4 characters
                else if (literalChar) {
                    addCharAndIncrement(stringBuilder,charA);
                    if (charA == '\'') {
                        addLastLine(stringBuilder, chType);
                        stringBuilder = new StringBuilder();
                        literalChar = false; codeBlock = true;
                    }
                } // char literal

            } //for each char in each line

        } //for each line
        if(charB == '}'){
            addNewLine(stringBuilder); addLastLine(stringBuilder, coType);
            indent--;
            stringBuilder = new StringBuilder(); newLine = true;
        }
        addCharAndIncrement(stringBuilder,charB);
        addNewLine(stringBuilder);
        OneLine.lineType finalType = (lineComment ? lcType : (blockComment ? bcType :
                (literalChar ? chType : (literalString ? stType : (header ? hType : coType)))));
        addLastLine(stringBuilder, finalType);

    } // breakIntoTypes

    class OneLine {
        enum lineType {line_comment, block_comment, string_literal, char_literal, code, header, include, define}
        lineType typeOfLine;
        String textOfLine, textOnly;
        int indent, startIndex, endIndex;
        StringBuilder sB;
        ArrayList<Token> tokens;

        OneLine(lineType l, String t, int i, int e) {
            typeOfLine = l;
            textOfLine = t;
            indent = i;
            endIndex = e;
            startIndex = e - textOfLine.length();
            textOnly = t;

            sB = new StringBuilder();

            if(newLine) {
                endIndex--;
                for (int j = 0; j < indent; j++) {
                    sB.append("    ");
                    indexIncrease += 4;
                }
                newLine = false;
            }
            startIndex += indexIncrease;
            endIndex += indexIncrease;

            sB.append(textOfLine);
            textOfLine = sB.toString();
        }

        void getTokens(){
            tokens = new ArrayList<>();
            StringBuilder s = new StringBuilder();
            ArrayList<Character> charsInThisLine = new ArrayList<>();

            for (int j = 0; j < textOfLine.length(); j++) { charsInThisLine.add(textOfLine.charAt(j)); }

            boolean decimal = false, numeric = false, specialChar = false, alphaNum = false;
            for(int j = 0; j < charsInThisLine.size(); j++){

                if(!(numeric || specialChar || alphaNum)) {
                    if (Pattern.compile("\\d").matcher("" + charsInThisLine.get(j)).matches() ||
                            Pattern.compile("\\.").matcher("" + charsInThisLine.get(j)).matches()) {
                        numeric = true; decimal = false;
                    }
                    else if (Pattern.compile("\\p{Alpha}").matcher("" + charsInThisLine.get(j)).matches() ||
                            '_' == charsInThisLine.get(j)) { alphaNum = true; }
                    else if (Pattern.compile("\\p{Punct}").matcher("" + charsInThisLine.get(j)).matches()) {
                        specialChar = true;
                    }
                }

                if(numeric){
                    if(charsInThisLine.get(j) == '.') {
                        if (!decimal) { decimal = true; }
                        else {
                            tokens.add(new Token(s.toString(), startIndex+j));
                            s = new StringBuilder();
                            decimal = false; numeric = false;
                        }
                    }
                    else if (Pattern.compile("\\p{Alpha}").matcher("" + charsInThisLine.get(j)).matches() ||
                            '_' == charsInThisLine.get(j)) {
                        tokens.add(new Token(s.toString(), startIndex+j));
                        decimal = false; numeric = false; alphaNum = true;
                        s = new StringBuilder();
                    }
                    else if (Pattern.compile("\\p{Punct}").matcher("" + charsInThisLine.get(j)).matches()) {
                        tokens.add(new Token(s.toString(), startIndex+j));
                        decimal = false; numeric = false; specialChar = true;
                        s = new StringBuilder();
                    }
                    if(charsInThisLine.get(j) != ' '){ s.append(charsInThisLine.get(j)); }
                    else {
                        tokens.add(new Token(s.toString(), startIndex+j));
                        s = new StringBuilder();
                        decimal = false; numeric = false;
                    }
                }
                if(alphaNum){
                    if (Pattern.compile("\\p{Alnum}").matcher("" + charsInThisLine.get(j)).matches() ||
                            '_' == charsInThisLine.get(j)) {
                    }
                    else if(Pattern.compile("\\p{Punct}").matcher("" + charsInThisLine.get(j)).matches()) {
                        tokens.add(new Token(s.toString(), startIndex+j));
                        alphaNum = false; specialChar = true;
                        s = new StringBuilder();
                    }
                    if(charsInThisLine.get(j) != ' '){ s.append(charsInThisLine.get(j)); }
                    else {
                        tokens.add(new Token(s.toString(), startIndex+j));
                        s = new StringBuilder();
                        alphaNum = false;
                    }
                }
                if(specialChar){
                    if (Pattern.compile("\\d").matcher("" + charsInThisLine.get(j)).matches() ||
                            Pattern.compile("\\.").matcher("" + charsInThisLine.get(j)).matches()) {
                        numeric = true; decimal = false; specialChar = false;
                    }
                    else if (Pattern.compile("\\p{Alpha}").matcher("" + charsInThisLine.get(j)).matches() ||
                            '_' == charsInThisLine.get(j)){
                        tokens.add(new Token(s.toString(), startIndex+j));
                        alphaNum = true; specialChar = false;
                        s = new StringBuilder();
                    }

                }
                //if it's a letter, it can be a keyword or variable
                //if it's a number, it must be a numeric literal
                //if it's a special character, it must be a specific thing

            }

        }

        // this class should be for identifying the tokens within each line
        class Token{
            enum tokenType {keyword, named_variable, special_character, header_file, integer, floating_point}
            int starting, ending;
            String tokenText, headerName;
            tokenType typeOfToken;
            //TODO : __LINE__ __FILE__ __DATE__ __TIME__ __cplusplus __STDC_HOSTED__ __STDC__ __STDC_VERSION__
            // __STDC_MB_MIGHT_NEQ_WC__ __STDC_ISO_10646__ __STDCPP_STRICT_POINTER_SAFETY__ __STDCPP_THREADS__
            String keywords[] =  {"alignas", "alignof", "asm", "auto", "bool", "break", "case", "catch", "char",
                    "char16_t", "char32_t", "class", "const", "constexpr", "const_cast", "continue", "decltype",
                    "default", "delete ", "do", "double", "dynamic_cast", "else", "enum", "explicit", "export",
                    "extern", "false", "float", "for", "friend", "goto", "if", "inline", "int", "long", "mutable",
                    "namespace", "new", "noexcept", "nullptr", "operator", "private", "protected", "public", "register",
                    "reinterpret_cast", "return", "short", "signed", "sizeof", "static", "static_assert", "static_cast",
                    "struct", "switch", "template", "this", "thread_local", "throw", "true", "try", "typedef", "typeid",
                    "typename", "union", "unsigned", "using", "virtual", "void", "volatile", "wchar_t", "while"};
            String specialCharacters[] = {"+", "++", "+=", "-", "--", "-= ", "*", "*=",  "/", "/= ", "%", "%= ",
                    "=", "==",  ">", ">=", "<", "<=",
                    "!", "!=",  "&&", "|| ", "&", "&=", "|", "|=", "^", "^=", ">>", ">>=", "<<", "<<= ",
                    "{", "}", "[ ", "] ", "(", ")",
                    "?", ":", ";", "::", "->", ".", ",", "'", "\"", "\\", "@", "$", "_", "`", "~"};
            Token(String t, int s){
                headerName = "";
                tokenText = t;
                starting = s;
                ending = s + t.length();
                boolean typeFound = false;
                int j = 0;
                if(Pattern.compile("-?\\d+").matcher(tokenText).matches()){
                    typeOfToken = tokenType.integer;
                    typeFound = true;
                } // if int
                else if(Pattern.compile("-?\\d*\\.?\\d+").matcher(tokenText).matches() ||
                        Pattern.compile("-?\\d+\\.?\\d*").matcher(tokenText).matches() ){
                    typeOfToken = tokenType.floating_point;
                    typeFound = true;
                } // if float
                else if(Pattern.compile("\\p{Punct}+").matcher(tokenText).matches()){
                    typeOfToken = tokenType.special_character;
                    typeFound = true;
                }
                while(!typeFound){
                    if(j < keywords.length){
                        if (tokenText.equals(keywords[j])){
                            typeOfToken = tokenType.keyword;
                            typeFound = true;
                        }
                    } // if keyword
                    if(j < specialCharacters.length && !typeFound){
                        if(tokenText.equals(specialCharacters[j])){
                            typeOfToken = tokenType.special_character;
                            typeFound = true;
                        }
                    } // if special character
                    j++;
                    if(Math.max(keywords.length, specialCharacters.length) <= j && !typeFound){
                        typeOfToken = tokenType.named_variable;
                        typeFound = true;
                    } // if named variable
                }

            } // non-header

            Token(String t, int s, boolean unused) {
                //TODO header include can be <> or ""
                tokenText = t;
                starting = s;
                ending = s+t.length();

                if(Pattern.compile("# ?include ?< ?[a-zA-Z]+[a-zA-Z_]*(\\.h)? ?>").matcher(t).matches()){
                    typeOfToken = tokenType.header_file;
                    headerName =
                            Pattern.compile("(# ?include ?< ?)([a-zA-Z]+[a-zA-Z_]*(\\.h)?)( ?>)").matcher(t).group(1);
                }


            } // header
        }
    }
}
