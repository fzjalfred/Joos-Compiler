package ast;

import visitors.Visitor;

import java.util.List;

public class CompilationUnit extends ASTNode implements Referenceable{
    public CompilationUnit(List<ASTNode> children, String value){
        super(children, value);
    }
    public PackageDecl getPackageDecl(){
        assert !children.isEmpty();
        assert children.get(0) instanceof PackageDecl;
        return (PackageDecl) children.get(0);
    }

    public ImportDecls getImportDecls(){
        assert !children.isEmpty();
        assert children.get(1) instanceof ImportDecls;
        return (ImportDecls) children.get(1);
    }

    public TypeDecls getTypeDecls(){
        assert !children.isEmpty();
        assert children.get(2) instanceof TypeDecls;
        return (TypeDecls) children.get(2);
    }

    public String fileName = "";

    @Override
    public void accept(Visitor v){
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }
    }

    @Override
    public Type getType() {
        return null;
    }
}
