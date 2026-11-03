package hk.edu.polyu.comp.comp2021.crobs.model;

public abstract class User {
    private String userId;
    private String name;

    protected User(String userId, String name){
        this.userId = userId;
        this.name = name;
    }

    public String getUserId(){ return userId; }
    public String getName(){ return name; }

    public abstract UserRole getRole();
    public boolean isAdmin(){ return false; }
    public boolean canChangeRoles(){ return false; }
}
