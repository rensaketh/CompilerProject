package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.Declaration;

import java.util.HashMap;

public class SymbolTable {
    HashMap<String, Declaration> entries = new HashMap<>();
//TODO:  Implement a symbol table class that is appropriate for this language. 
    public boolean insert(String name, Declaration declaration) {
        return (entries.putIfAbsent(name, declaration) == null);
    }

    public Declaration lookup(String name) {
        return entries.get(name);
    }

    public boolean remove(String name) {
        return (entries.remove(name) != null);
    }
}
