package cliclient.parser;

import cliclient.command.Command;
import cliclient.command.FlagType;
import cliclient.util.ObjectUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static cliclient.parser.Token.FLAG_MARKER;
import static cliclient.parser.Token.SEPARATOR;

@Component
class CommandLineInputTokenizer {

    List<Token> parseIntoTokens(String input) {
        ObjectUtil.requireNonNullOrElseThrowIAE(input, "input can not be null");
        return parse(input.toCharArray(), new LinkedList<>());
    }

    private List<Token> parse(char[] chars, List<Token> tokens) {
        if (chars.length == 0) {
            return tokens;
        }
        return parse(eatFrontChunk(chars, tokens), tokens);
    }

    private char[] eatFrontChunk(char[] chars, List<Token> tokens) {
        char firstChar = chars[0];
        if (isWhitespace(firstChar)) {
            return getRidOfFrontWhitespaces(chars);
        } else if (isLetter(firstChar)) {
            return eatFrontWhileNotCommandFlag(chars, tokens);
        } else if (isFlagMarker(firstChar)) {
            return eatFrontFlagOrText(chars, tokens);
        } else if (isSeparator(firstChar)) {
            return eatFrontSeparator(chars, tokens);
        } else if (isDigit(firstChar)) {
            return eatFrontDigitOrText(chars, tokens);
        }
        return eatFrontUntilWhitespaceEncountered(chars, tokens);
    }

    private char[] eatFrontUntilWhitespaceEncountered(char[] chars, List<Token> tokens) {
        int i = 0;
        var sb = new StringBuilder();
        while (i < chars.length && !isWhitespace(chars[i])) {
            sb.append(chars[i++]);
        }
        tokens.add(Token.text(sb.toString()));
        return Arrays.copyOfRange(chars, i, chars.length);
    }

    private boolean isDigit(char firstChar) {
        return Character.isDigit(firstChar);
    }

    private boolean isLetter(char firstChar) {
        return Character.isLetter(firstChar);
    }

    private boolean isWhitespace(char firstChar) {
        return Character.isWhitespace(firstChar);
    }

    private char[] getRidOfFrontWhitespaces(char[] chars) {
        int i = 0;
        while (i < chars.length && isWhitespace(chars[i])) {
            ++i;
        }
        return Arrays.copyOfRange(chars, i, chars.length);
    }

    private char[] eatFrontDigitOrText(char[] chars, List<Token> tokens) {
        int i = 0;
        var sb = new StringBuilder();
        while (i < chars.length && (Character.isLetterOrDigit(chars[i]) || !isWhitespace(chars[i]))) {
            sb.append(chars[i++]);
        }
        if (isValidPositiveLongNumber(sb.toString())) {
            tokens.add(Token.number(sb.toString()));
        } else {
            tokens.add(Token.text(sb.toString()));
        }
        return Arrays.copyOfRange(chars, i, chars.length);
    }

    // todo: parse negative numbers and return validation errors later
    private boolean isValidPositiveLongNumber(String toString) {
        try {
            return Long.parseLong(toString) > 0;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private char[] eatFrontSeparator(char[] chars, List<Token> tokens) {
        tokens.add(Token.separator());
        return Arrays.copyOfRange(chars, 1, chars.length);
    }

    private boolean isSeparator(char character) {
        return Character.toString(character).equals(SEPARATOR);
    }

    private char[] eatFrontFlagOrText(char[] chars, List<Token> tokens) {
        int i = 0;
        var sb = new StringBuilder();
        while (i < chars.length && (Character.isLetterOrDigit(chars[i]) || FLAG_MARKER.equals(Character.toString(chars[i])))) {
            sb.append(chars[i++]);
        }
        addFlagOrText(tokens, sb);
        return Arrays.copyOfRange(chars, i, chars.length);
    }

    private void addFlagOrText(List<Token> tokens, StringBuilder sb) {
        if (FlagType.isKnown(FlagType.fromString(sb.substring(1)))) {
            tokens.add(Token.flag(sb.substring(1)));
        } else {
            tokens.add(Token.text(sb.toString()));
        }
    }

    // todo: feat: flexible flag recognition. change one place to change flag from \<flag> to --<flag> etc
    private boolean isFlagMarker(char character) {
        return FLAG_MARKER.equals(Character.toString(character));
    }

    private char[] eatFrontWhileNotCommandFlag(char[] chars, List<Token> tokens) {
        int i = 0;
        var sb = new StringBuilder();
        while (i < chars.length && (!FLAG_MARKER.equals(Character.toString(chars[i])))) {
            sb.append(chars[i++]);
        }
        addCommandOrText(tokens, sb);
        return Arrays.copyOfRange(chars, i, chars.length);
    }

    private void addCommandOrText(List<Token> tokens, StringBuilder sb) {
        String str = sb.toString().strip();
        if (Command.isRecognizable(Command.fromString(str))) {
            tokens.add(Token.command(str));
        } else {
            tokens.add(Token.text(str));
        }
    }

}