package hk.edu.polyu.comp.comp2021.crobs.model;

import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.AuthorizationException;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.ValidationException;

public abstract class User {
    private final String userId;
    private final String name;

    protected User(String userId, String name){
        if(name == null || name.isBlank()){
            throw new ValidationException("Name cannot be null");
        }
        this.userId = userId;
        this.name = name;
    }

    public String getUserId(){ return userId; }
    public String getName(){ return name; }

    public abstract UserRole getRole();
    public boolean isAdmin(){ return false; }
    public boolean canChangeRoles(){ return false; }
}
