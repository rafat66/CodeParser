package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclarationVisitor extends ASTVisitor {

    private final List<TypeDeclaration> types =
            new ArrayList<>();

    @Override
    public boolean visit(TypeDeclaration node) {

        if (node != null) {
            types.add(node);
        }

        return true;
    }

    public List<TypeDeclaration> getTypes() {
        return types;
    }
}