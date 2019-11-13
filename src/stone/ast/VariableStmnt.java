package stone.ast;

import java.util.List;

public class VariableStmnt extends ASTList {

    public VariableStmnt(List<ASTree> list) {
        super(list);
    }

    public String name() {
        return ((ASTLeaf) child(0)).token().getText();
    }

    public Object value() {
        // default value
        return 0;
    }
}
