package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationVisitor extends ASTVisitor {

    private final List<MethodDeclaration> methods =
            new ArrayList<>();

    @Override
    public boolean visit(MethodDeclaration node) {

        if (node != null) {
            methods.add(node);
        }

        return true;
    }

    public List<MethodDeclaration> getMethods() {
        return methods;
    }
}