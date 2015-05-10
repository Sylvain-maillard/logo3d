package logo3d.language;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by SylvainMaillard on 08/04/2015.
 */
public class LogoRuntime {

    Node root;
    private Node currentExecutionPointer;

    private final RuntimeContext globalContext = new RuntimeContext();

    class Value {
    }

    class RuntimeContext {
        Map<String, Value> variables = new HashMap<>();
    }

    interface Node {
        void process();
        Node moveNext();
    }

    abstract class DefaultNode implements Node {
        Node next;

        @Override
        public Node moveNext() {
            return next;
        }
    }

    interface PrimitiveAction {
        void doIt();
    }

    public void run() {
        currentExecutionPointer = root;
        while (currentExecutionPointer != null) {
            root.process();
            currentExecutionPointer = root.moveNext();
        }
    }
}
