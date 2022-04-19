package ast;

import visitors.Visitor;

import java.util.*;

import utils.tools;

public class ClassDecl extends TypeDecl{
    // children( modifiers ID Super Interface ClassBody)
    public List<ClassDecl> subclasses = new ArrayList<ClassDecl>();
    public ClassDecl parentClass = null;
    public ConstructorDecl supercall = null;

    public Map<FieldDecl, Integer> fieldMap = new HashMap<FieldDecl, Integer>();
    public Map<MethodDecl, Integer> methodMap = new HashMap<MethodDecl, Integer>();
    public Map<AbstractMethodDecl, Integer> interfaceMethodMap = new HashMap<AbstractMethodDecl, Integer>();
    public Integer itable_offset_counter = 0;
    public Set<MethodDecl> selfMethodMap = new HashSet<>();

    public Map<String, List<ASTNode>> containMap = null;

    public ClassDecl(List<ASTNode> children, String value){
        super(children, value);
    }
    public String getName(){
        return children.get(1).value;
    }
    public Modifiers getModifiers() {
        assert children.get(0) instanceof Modifiers;
        return (Modifiers)children.get(0);
    }
    public ClassBodyDecls getClassBodyDecls(){
        if (children.get(4) == null) return null;
        assert children.get(4).children.get(0) instanceof ClassBodyDecls;
        return (ClassBodyDecls)children.get(4).children.get(0);
    }

    public List<FieldDecl> getAllFieldDecls() {
        if (parentClass != null) {
            List <FieldDecl> res =  parentClass.getAllFieldDecls();
            res.addAll(getFieldDecls());
            return res;
        }
        return getFieldDecls();
    }

    public List<FieldDecl> getFieldDecls(){
        return getClassBodyDecls().getFieldDecls();
    }

    public List<MethodDecl> getMethodDecls(){
        List <MethodDecl> methodDecls = new ArrayList<MethodDecl>();

        for (String key : containMap.keySet()) {
            List <ASTNode> nodes = containMap.get(key);
            for (ASTNode node : nodes) {
                if (node instanceof MethodDecl && !((MethodDecl)node).isStatic()) {
                    methodDecls.add((MethodDecl) node);
                }
            }
        }
        return methodDecls;
    }

    public boolean isStatic() {
        Modifiers modifiers = (Modifiers) children.get(0);

        for (ASTNode modifier : modifiers.children) {
            if (modifier.value.equals( "static")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(Visitor v){
        v.visit(this);
        for (ASTNode node: children){
            if (node != null) node.accept(v);
        }

    }

    @Override
    public Type getType() {
        return tools.getClassType(getName(), this);
    }
}
