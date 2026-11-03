package hk.edu.polyu.comp.comp2021.crobs.model;

public class AdminUser extends User{
    public AdminUser(String userId, String name){
        super(userId, name);
    }

    @Override
    public UserRole getRole(){ return UserRole.ADMIN; }

    @Override
    public boolean isAdmin(){ return true; }
}