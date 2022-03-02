package ast;

import java.util.List;

public interface ReferenceableList {
    public Referenceable match(List<Type> args);
}
