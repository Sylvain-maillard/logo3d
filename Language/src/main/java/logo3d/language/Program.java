package logo3d.language;

import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.apache.commons.lang3.math.NumberUtils.createFloat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by SylvainMaillard on 01/04/2015.
 */
public class Program {

    private static final Logger LOG = getLogger(Program.class);

    private final SyntaxErrorHandler syntaxErrorHandler;
    private final TurtleActionCallbacks turtleActionCallbacks;

    private final LogoRuntime runtime;

    // store variables (there's only one global scope!)
    Map<String, LogoValue> memory = new HashMap<>();

    private void inc(String var, float step){
        LogoValue logoValue = memory.get(var);
        if (logoValue != null) {
            memory.put(var, new LogoValue(logoValue.asFloat() + step));
        } else {
            memory.put(var, new LogoValue(step));
        }
    }

    private void set(String var, float value){
        memory.put(var, new LogoValue(value));
    }

    public Program(TurtleActionCallbacks turtleActionCallbacks, SyntaxErrorHandler syntaxErrorHandler) {
        this.turtleActionCallbacks = turtleActionCallbacks;
        this.syntaxErrorHandler = syntaxErrorHandler;
        this.runtime = new LogoRuntime();
    }

    public Program(TurtleActionCallbacks turtleActionCallbacks) {
        this(turtleActionCallbacks, syntaxErrorMsg -> {
            LOG.error("Syntax error: {}", syntaxErrorMsg);
        });
    }

    public void interpret(String sourceCode) {
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

        LogoParser.ProgContext prog = logoParser.prog();
        ValueVisitor progVisitor = new ValueVisitor();
        progVisitor.visit(prog);
    }

    class ValueVisitor extends LogoBaseVisitor<LogoValue> {

        @Override
        public LogoValue visitSignExpression(LogoParser.SignExpressionContext ctx) {
            // this is quite bad :(
            if (ctx.deref() != null) {
                return this.visit(ctx.deref());
            }
            return new LogoValue(NumberUtils.createFloat(ctx.getText()));
        }

        @Override
        public LogoValue visitAdditiveExpression(LogoParser.AdditiveExpressionContext ctx) {
            LogoValue left = this.visit(ctx.expression(0));
            LogoValue right = this.visit(ctx.expression(1));

            switch (ctx.op.getType()){
                case LogoParser.PLUS  : return new LogoValue(left.asFloat() + right.asFloat());
                case LogoParser.MINUS : return new LogoValue(left.asFloat() - right.asFloat());
                default:
                    throw new RuntimeException("unknown operator: " + LogoParser.VOCABULARY.getDisplayName(ctx.op.getType()));
            }
        }

        @Override
        public LogoValue visitMultiplicationExpression(LogoParser.MultiplicationExpressionContext ctx) {
            LogoValue left = this.visit(ctx.expression(0));
            LogoValue right = this.visit(ctx.expression(1));

            switch (ctx.op.getType()) {
                case LogoParser.MULT:
                    return new LogoValue(left.asFloat() * right.asFloat());
                case LogoParser.DIVIDE:
                    return new LogoValue(left.asFloat() / right.asFloat());
                default:
                    throw new RuntimeException("unknown operator: " + LogoParser.VOCABULARY.getDisplayName(ctx.op.getType()));
            }
        }

        @Override
        public LogoValue visitFd(LogoParser.FdContext ctx) {
            LogoValue value = this.visit(ctx.expression());
            if (value.asFloat() < 0f) {
                turtleActionCallbacks.backward(-value.asFloat());
            } else {
                turtleActionCallbacks.forward(value.asFloat());
            }
            return LogoValue.VOID;
        }

        @Override
        public LogoValue visitBk(LogoParser.BkContext ctx) {
            LogoValue value = this.visit(ctx.expression());
            if (value.asFloat() < 0f) {
                turtleActionCallbacks.forward(-value.asFloat());
            } else {
                turtleActionCallbacks.backward(value.asFloat());
            }
            return LogoValue.VOID;
        }

        @Override
        public LogoValue visitRt(LogoParser.RtContext ctx) {
            turtleActionCallbacks.turnRight(this.visit(ctx.expression()).asFloat());
            return LogoValue.VOID;
        }

        @Override
        public LogoValue visitLt(LogoParser.LtContext ctx) {
            turtleActionCallbacks.turnLeft(this.visit(ctx.expression()).asFloat());
            return LogoValue.VOID;
        }

        @Override
        public LogoValue visitPrint(LogoParser.PrintContext ctx) {

            if (ctx.quotedstring() != null) {
                turtleActionCallbacks.print(this.visit(ctx.quotedstring()).asString());
            } else if (ctx.value() != null) {
                turtleActionCallbacks.print(this.visit(ctx.value()).asString());
            }
            return LogoValue.VOID;
        }

        @Override
        public LogoValue visitDeref(LogoParser.DerefContext ctx) {
            String variableName = this.visit(ctx.name()).asString();
            if (!memory.containsKey(variableName)) {
                throw new RuntimeException("No such variable: " + variableName);
            }
            return memory.get(variableName);
        }

        @Override
        public LogoValue visitDerefValue(LogoParser.DerefValueContext ctx) {
            return this.visit(ctx.deref());
        }

        @Override
        public LogoValue visitLiteralValue(LogoParser.LiteralValueContext ctx) {
            return this.visit(ctx.stringliteral());
        }

        @Override
        public LogoValue visitExpressionValue(LogoParser.ExpressionValueContext ctx) {
            return this.visit(ctx.expression());
        }

        @Override
        public LogoValue visitStringliteral(LogoParser.StringliteralContext ctx) {
            return new LogoValue(ctx.STRING().getText());
        }

        @Override
        public LogoValue visitMake(LogoParser.MakeContext ctx) {
            String variableName = this.visit(ctx.stringliteral()).asString();
            LogoValue value = this.visit(ctx.value());
            memory.put(variableName, value);
            return LogoValue.VOID;
        }

        @Override
        public LogoValue visitQuotedstring(LogoParser.QuotedstringContext ctx) {
            // strip leading and trailing [ and ]
            String s = "";
            String text = ctx.getText();
            if (text != null && text.length() > 2) {
                s = StringUtils.substring(text, 1, text.length() - 1);
            }
            return new LogoValue(s);
        }

        @Override
        public LogoValue visitName(LogoParser.NameContext ctx) {
            return new LogoValue(ctx.getText());
        }

        @Override
        public LogoValue visitFore(LogoParser.ForeContext ctx) {

            // for control name:
            String controlLoopValueName = ctx.name().getText();

            Float startIndex = this.visit(ctx.expression(0)).asFloat();
            Float lastIndex = this.visit(ctx.expression(1)).asFloat();
            Float step = this.visit(ctx.expression(2)).asFloat();

            for (float i = startIndex; i <= lastIndex; i += step) {

                // update counter:
                set(controlLoopValueName, i);

                // do something.
                this.visit(ctx.block());
            }
            return LogoValue.VOID;
        }
    }

    public LogoRuntime getRuntime() {
        return runtime;
    }

    public interface SyntaxErrorHandler {
        void onSyntaxError(String msg);
    }
}
