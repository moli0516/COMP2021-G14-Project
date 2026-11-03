package hk.edu.polyu.comp.comp2021.crobs.model;

public class SuperAdmin extends AdminUser{
    public SuperAdmin(String userId, String name){
        super(userId, name);
    }

    @Override
    public UserRole getRole(){ return UserRole.SUPERADMIN; }

    @Override
    public boolean canChangeRoles(){ return true; }
}