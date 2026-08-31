package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class VariableDeclarationFragmentVisitor extends ASTVisitor {

    private final List<VariableDeclarationFragment> variables =
            new ArrayList<>();

    @Override
    public boolean visit(VariableDeclarationFragment node) {

        if (node != null) {
            variables.add(node);
        }

        return true;
    }

    public List<VariableDeclarationFragment> getVariables() {
        return variables;
    }
}