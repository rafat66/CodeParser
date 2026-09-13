package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

public class AttributeAccessVisitor extends ASTVisitor {

    private final List<SimpleName> fields =
            new ArrayList<SimpleName>();


    public List<SimpleName> getFields() {
        return fields;
    }


    // =========================================================
    // SIMPLE FIELD ACCESS
    // =========================================================

    @Override
    public boolean visit(SimpleName node) {

        /*
         * Ignore declaration names.
         *
         * Example:
         *
         * private int age;
         *
         * "age" here is a declaration,
         * not an attribute access.
         */
        if (node.isDeclaration()) {
            return true;
        }


        /*
         * Do not process the SimpleName here if it belongs
         * to one of the specialized field-access structures.
         *
         * Otherwise the same field can be added twice.
         */
        if (node.getParent() instanceof FieldAccess) {
            return true;
        }

        if (node.getParent() instanceof SuperFieldAccess) {
            return true;
        }


        /*
         * QualifiedName example:
         *
         * object.age
         *
         * ClassName.count
         */
        if (node.getParent() instanceof QualifiedName) {
            return true;
        }


        /*
         * Resolve the binding.
         */
        IBinding binding =
                node.resolveBinding();


        /*
         * We only want variables.
         */
        if (!(binding instanceof IVariableBinding)) {
            return true;
        }


        IVariableBinding variableBinding =
                (IVariableBinding) binding;


        /*
         * We only want class fields.
         */
        if (!variableBinding.isField()) {
            return true;
        }


        fields.add(node);

        return true;
    }


    // =========================================================
    // THIS.FIELD / EXPRESSION.FIELD
    // =========================================================

    @Override
    public boolean visit(FieldAccess node) {

        SimpleName fieldName =
                node.getName();


        if (fieldName == null) {
            return true;
        }


        IBinding binding =
                fieldName.resolveBinding();


        if (!(binding instanceof IVariableBinding)) {
            return true;
        }


        IVariableBinding variableBinding =
                (IVariableBinding) binding;


        if (!variableBinding.isField()) {
            return true;
        }


        fields.add(fieldName);

        return true;
    }


    // =========================================================
    // SUPER.FIELD
    // =========================================================

    @Override
    public boolean visit(SuperFieldAccess node) {

        SimpleName fieldName =
                node.getName();


        if (fieldName == null) {
            return true;
        }


        IBinding binding =
                fieldName.resolveBinding();


        if (!(binding instanceof IVariableBinding)) {
            return true;
        }


        IVariableBinding variableBinding =
                (IVariableBinding) binding;


        if (!variableBinding.isField()) {
            return true;
        }


        fields.add(fieldName);

        return true;
    }


    // =========================================================
    // OBJECT.FIELD / CLASS.FIELD
    // =========================================================

    @Override
    public boolean visit(QualifiedName node) {

        IBinding binding =
                node.resolveBinding();


        /*
         * QualifiedName can represent:
         *
         * object.field
         * ClassName.staticField
         *
         * We only care if the complete qualified name
         * resolves to a field.
         */
        if (!(binding instanceof IVariableBinding)) {
            return true;
        }


        IVariableBinding variableBinding =
                (IVariableBinding) binding;


        if (!variableBinding.isField()) {
            return true;
        }


        /*
         * Get the final name:
         *
         * object.field
         *       ^^^^^
         */
        SimpleName fieldName =
                node.getName();


        if (fieldName != null) {
            fields.add(fieldName);
        }


        return true;
    }
}