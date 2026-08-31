package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class AttributeAccessVisitor extends ASTVisitor {

    private final List<SimpleName> fields =
            new ArrayList<>();

    @Override
    public boolean visit(SimpleName node) {

        if (node == null) {
            return true;
        }

        // Ignore declarations.
        if (node.isDeclaration()) {
            return true;
        }

        // Resolve binding only once.
        Object binding = node.resolveBinding();

        if (binding instanceof IVariableBinding) {

            IVariableBinding variableBinding =
                    (IVariableBinding) binding;

            /*
             * Keep only fields/attributes.
             *
             * getVariableDeclaration() gives the original
             * variable declaration.
             */
            if (variableBinding.isField()) {
                fields.add(node);
            }
        }

        return true;
    }

    public List<SimpleName> getFields() {
        return fields;
    }
}