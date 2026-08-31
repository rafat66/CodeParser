package Visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

public class AssignmentVisitor extends ASTVisitor {

    private final List<Assignment> assignments =
            new ArrayList<>();

    @Override
    public boolean visit(Assignment node) {

        if (node != null) {
            assignments.add(node);
        }

        return true;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public Expression getLeftHandSide() {

        if (assignments.isEmpty()) {
            return null;
        }

        return assignments.get(assignments.size() - 1)
                .getLeftHandSide();
    }

    public Assignment.Operator getOperator() {

        if (assignments.isEmpty()) {
            return null;
        }

        return assignments.get(assignments.size() - 1)
                .getOperator();
    }

    public Expression getRightHandSide() {

        if (assignments.isEmpty()) {
            return null;
        }

        return assignments.get(assignments.size() - 1)
                .getRightHandSide();
    }
}