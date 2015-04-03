package logo3d.desktop;

import de.lessvoid.nifty.controls.ConsoleExecuteCommandEvent;
import logo3d.language.LogoBaseListener;
import logo3d.language.LogoLexer;
import logo3d.language.LogoParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import java.util.BitSet;
import java.util.Stack;

import static org.apache.commons.lang3.math.NumberUtils.createFloat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 01/04/2015.
 */
public class CommandInterpreter {

    private static final Logger LOG = getLogger(CommandInterpreter.class);

    private final TurtleControl turtleControl;

    public CommandInterpreter(TurtleControl turtleControl) {
        this.turtleControl = turtleControl;
    }

    void interpret(final ConsoleExecuteCommandEvent cEvent) {
        // append EOL to finish the command
        String consoleInput = cEvent.getCommandLine() + "\n";
        // parse the commande line:
        BufferedTokenStream tokenStream = new BufferedTokenStream(new LogoLexer(new ANTLRInputStream(consoleInput)));
        LogoParser logoParser = new LogoParser(tokenStream);

        // deal with errors.
        logoParser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                cEvent.getConsole().outputError("Syntax error: " + msg);
            }

            @Override
            public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
                cEvent.getConsole().outputError("Ambiguity detected.");
            }

            @Override
            public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
                cEvent.getConsole().outputError("AttemptingFullContext");
            }

            @Override
            public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
                cEvent.getConsole().outputError("ContextSensitivity");
            }
        });

        // deal with commandes:
        logoParser.addParseListener(new LogoBaseListener() {

            private Stack<Float> valueStack = new Stack<>();

            @Override
            public void exitExpression(LogoParser.ExpressionContext ctx) {
                LOG.debug("interpreting expression: " + ctx.getText());
                if (valueStack.size() < 2) return;

                if (!ctx.PLUS().isEmpty()) {
                    float value1 = valueStack.pop();
                    float value2 = valueStack.pop();
                    valueStack.push(value1 + value2);
                }
            }

            @Override
            public void exitMultiplyingExpression(LogoParser.MultiplyingExpressionContext ctx) {
                if (valueStack.size() < 2) return;

                if (!ctx.MULT().isEmpty()) {
                    float value1 = valueStack.pop();
                    float value2 = valueStack.pop();
                    valueStack.push(value1 * value2);
                }
            }

            @Override
            public void exitSignExpression(LogoParser.SignExpressionContext ctx) {
                if (ctx.number() != null) {
                    valueStack.push(createFloat(ctx.number().getText()));
                } else {
                    // not implemented for the moment;

                }
            }

            @Override
            public void exitNumber(LogoParser.NumberContext ctx) {
//                valueStack.push(createFloat(ctx.NUMBER().getText()));
            }


            @Override
            public void exitFd(LogoParser.FdContext ctx) {
                if (valueStack.size() < 1) return;
                Float currentRegistry = valueStack.pop();
                if (currentRegistry < 0) {
                    turtleControl.backward(-currentRegistry);
                } else {
                    turtleControl.forward(currentRegistry);
                }
            }

            @Override
            public void exitBk(LogoParser.BkContext ctx) {
                if (valueStack.size() < 1) return;
                turtleControl.backward(valueStack.pop());
            }

            @Override
            public void exitLt(LogoParser.LtContext ctx) {
                if (valueStack.size() < 1) return;
                turtleControl.turnLeft(valueStack.pop());
            }

            @Override
            public void exitRt(LogoParser.RtContext ctx) {
                if (valueStack.size() < 1) return;
                turtleControl.turnRight(valueStack.pop());
            }
        });

        // parse input.
        logoParser.prog();
    }
}
