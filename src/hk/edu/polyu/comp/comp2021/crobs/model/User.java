package hk.edu.polyu.comp.comp2021.crobs.model;

public abstract class User {
    private String user_id;
    private String name;

    protected User(String user_id, String name){
        this.user_id = user_id;
        this.name = name;
    }

    public String get_user_id(){ return user_id; }
    public String get_name(){ return name; }

    public abstract UserRole get_role();
    public boolean is_admin(){ return false; }
    public boolean can_change_roles(){ return false; }
}
