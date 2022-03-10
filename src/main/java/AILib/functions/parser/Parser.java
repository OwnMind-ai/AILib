package AILib.functions.parser;

import AILib.functions.tokenizer.Tokenizer;
import AILib.functions.tokenizer.tokens.*;

import java.util.ArrayList;

public class Parser {
    public static final PunctuationToken delimiterStart = new PunctuationToken("(");
    private static final PunctuationToken delimiterStop = new PunctuationToken(")");
    public static final PunctuationToken delimiterSeparator = new PunctuationToken(",");

    private final Tokenizer tokenizer;

    private static int getPrecedence(OperatorToken operator) throws Exception {
        switch (operator.operator){
            case "+": case "-":
                return 10;
            case "*": case "/": case "%":
                return 20;
            case "**":
                return 25;
        }

        throw new Exception("Invalid operator");
    }

    public Parser(Tokenizer tokenizer){
        this.tokenizer = tokenizer;
    }

    private void skipToken(IToken token) throws Exception {
        if (this.tokenizer.peek().equals(token)) this.tokenizer.next();
        else throw new Exception("Excepting " + token);
    }

    private boolean isFollowingToken(IToken token){
        if(this.tokenizer.peek() == null) return false;
        return this.tokenizer.peek().equals(token);
    }

    private IToken[] delimited(IParserMethod parser) throws Exception {
        ArrayList<IToken> tokens = new ArrayList<>();
        boolean isFirst = true;

        this.skipToken(Parser.delimiterStart);
        while (!this.tokenizer.eof()){
            if(isFollowingToken(Parser.delimiterStop)) break;
            if(isFirst){ isFirst = false; } else {
                this.skipToken(Parser.delimiterSeparator);
            }
            if(isFollowingToken(Parser.delimiterStop)) break;
            tokens.add(parser.run());
        }

        this.skipToken(Parser.delimiterStop);
        return tokens.toArray(new IToken[0]);
    }

    private IToken buildExpressionTree(IToken previousToken, int currentPrecedence) throws Exception {
        IToken token = this.tokenizer.peek();
        if(token instanceof OperatorToken){
            OperatorToken operator = (OperatorToken) token;
            int nextPrecedence = Parser.getPrecedence(operator);

            if(currentPrecedence < nextPrecedence){
                this.tokenizer.next();

                return this.buildExpressionTree(
                        new ExpressionToken(
                                operator,
                                previousToken,
                                this.buildExpressionTree(this.parseToken(), nextPrecedence)
                        ),
                        currentPrecedence
                );
            }
        }

        return previousToken;
    }

    private IToken parseToken() throws Exception {
        if(this.isFollowingToken(Parser.delimiterStart)){
            this.tokenizer.next();
            ExpressionToken expression = (ExpressionToken) this.parseExpression();
            this.skipToken(Parser.delimiterStop);

            return expression;
        }

        IToken token = this.tokenizer.next();
        return this.parseCall(token);
    }

    private IToken parseExpression() throws Exception {
        IToken token = this.buildExpressionTree(this.parseToken(), 0);
        return this.parseCall(token);
    }

    private IToken parseCall(IToken token) throws Exception {
        if(!this.isFollowingToken(Parser.delimiterStart) || !(token instanceof NameToken)) return token;

        return new CallToken(
                ((NameToken) token).name,
                this.delimited(
                        this::parseExpression)
        );
    }

    public ExpressionToken parse() throws Exception {
        IToken token = parseExpression();
        if(token instanceof ExpressionToken)
            return (ExpressionToken) token;
        else return new ExpressionToken(new OperatorToken("+"), token, new NumberToken(0));
    }

    interface IParserMethod{
        IToken run() throws Exception;
    }
}