package hk.edu.polyu.comp.comp2021.crobs.model;

public class AdminUser extends User{
    public AdminUser(String user_id, String name){
        super(user_id, name);
    }

    @Override
    public UserRole get_role(){ return UserRole.ADMIN; }

    @Override
    public boolean is_admin(){ return true; }
}