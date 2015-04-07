package logo3d.language;

import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Stack;

import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.math.NumberUtils.createFloat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 01/04/2015.
 */
public class Program {

    private static final Logger LOG = getLogger(Program.class);

    private final SyntaxErrorHandler syntaxErrorHandler;
    private final TurtleActionCallbacks turtleActionCallbacks;

    public Program(TurtleActionCallbacks turtleActionCallbacks, SyntaxErrorHandler syntaxErrorHandler) {
        this.turtleActionCallbacks = turtleActionCallbacks;
        this.syntaxErrorHandler = syntaxErrorHandler;
    }

    public Program(TurtleActionCallbacks turtleActionCallbacks) {
        this(turtleActionCallbacks, syntaxErrorMsg -> {
            LOG.error("Syntax error: {}", syntaxErrorMsg);
        });
    }

    public interface SyntaxErrorHandler {
        void onSyntaxError(String msg);
    }

    public interface TurtleActionCallbacks {
        default void forward(float value) { LOG.info("Should forward by {}", value);}
        default void backward(float value) { LOG.info("Should backward by {}", value);}
        default void turnLeft(float degree) { LOG.info("Should turn left by {} degree", degree);}
        default void turnRight(float degree) { LOG.info("Should turn right by {} degree", degree);}
        default void print(String msg) { LOG.info("Should print {}", msg);}
    }

    void interpret(String sourceCode) {
        // append EOL to finish the command
        String consoleInput = sourceCode + "\n";
        // parse the commande line:
        BufferedTokenStream tokenStream = new BufferedTokenStream(new LogoLexer(new ANTLRInputStream(consoleInput)));
        LogoParser logoParser = new LogoParser(tokenStream);

        // deal with errors.
        logoParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                syntaxErrorHandler.onSyntaxError(msg);
            }
        });

        // deal with expressions:
        logoParser.addParseListener(new ExpressionsListener());

        // deal with for loop:
        logoParser.addParseListener(new ForListener());


        logoParser.addParseListener(new LogoBaseListener() {
            @Override
            public void exitPrint(LogoParser.PrintContext ctx) {
                if (ctx.value() != null) {
                    turtleActionCallbacks.print(""+valueStack.peek());
                } else {
                    // strip leading and trailing [ and ]
                    String s = "";
                    String text = ctx.quotedstring().getText();
                    if (text != null && text.length() > 2) {
                        s = StringUtils.substring(text, 1, text.length() - 1);
                    }
                    turtleActionCallbacks.print(s);
                }
            }
        });

        // parse input.
        logoParser.prog();
    }

    // current values stack
    private Stack<Float> valueStack = new Stack<>();

    private class ExpressionsListener extends LogoBaseListener {

        @Override
        public void exitExpression(LogoParser.ExpressionContext ctx) {
            if (valueStack.size() < 2) return;

            if (!ctx.PLUS().isEmpty()) {
                float value1 = valueStack.pop();
                float value2 = valueStack.pop();
                valueStack.push(value1 + value2);
            } else if (!ctx.MINUS().isEmpty()) {
                float value1 = valueStack.pop();
                float value2 = valueStack.pop();
                valueStack.push(value2 - value1);
            }
            LOG.debug("interpreting expression: {}, last stack value: {} ", ctx.getText(), valueStack.peek());
        }

        @Override
        public void exitMultiplyingExpression(LogoParser.MultiplyingExpressionContext ctx) {
            if (valueStack.size() < 2) return;

            if (!ctx.MULT().isEmpty()) {
                float value1 = valueStack.pop();
                float value2 = valueStack.pop();
                valueStack.push(value1 * value2);
            } else if (!ctx.DIVIDE().isEmpty()) {
                float value1 = valueStack.pop();
                float value2 = valueStack.pop();
                valueStack.push(value2 / value1);
            }
        }

        @Override
        public void exitSignExpression(LogoParser.SignExpressionContext ctx) {
            if (ctx.number() != null) {
                valueStack.push(createFloat(ctx.getText()));
            } else {
                // not implemented for the moment;

            }
        }

        @Override
        public void exitFd(LogoParser.FdContext ctx) {
            if (valueStack.size() < 1) return;
            Float currentRegistry = valueStack.pop();
            if (currentRegistry < 0) {
                turtleActionCallbacks.backward(-currentRegistry);
            } else {
                turtleActionCallbacks.forward(currentRegistry);
            }
        }

        @Override
        public void exitBk(LogoParser.BkContext ctx) {
            if (valueStack.size() < 1) return;
            turtleActionCallbacks.backward(valueStack.pop());
        }

        @Override
        public void exitLt(LogoParser.LtContext ctx) {
            if (valueStack.size() < 1) return;
            turtleActionCallbacks.turnLeft(valueStack.pop());
        }

        @Override
        public void exitRt(LogoParser.RtContext ctx) {
            if (valueStack.size() < 1) return;
            turtleActionCallbacks.turnRight(valueStack.pop());
        }
    }

    private class ForListener extends LogoBaseListener{

        @Override
        public void exitFore(LogoParser.ForeContext ctx) {
            LOG.debug("boucle for en cours !! {}", ctx.getText());
        }
    }
}
