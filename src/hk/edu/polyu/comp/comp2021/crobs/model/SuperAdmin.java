package hk.edu.polyu.comp.comp2021.crobs.model;

public class SuperAdmin extends AdminUser{
    public SuperAdmin(String user_id, String name){
        super(user_id, name);
    }

    @Override
    public UserRole get_role(){ return UserRole.SUPERADMIN; }

    @Override
    public boolean can_change_roles(){ return true; }
}