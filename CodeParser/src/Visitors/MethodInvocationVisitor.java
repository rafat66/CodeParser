package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {

    private final List<MethodInvocation> methods =
            new ArrayList<>();

    @Override
    public boolean visit(MethodInvocation node) {

        if (node != null) {
            methods.add(node);
        }

        return true;
    }

    public List<MethodInvocation> getMethods() {
        return methods;
    }
}