package hk.edu.polyu.comp.comp2021.crobs.model;

public class RegularUser extends User{
    public RegularUser(String user_id, String name){
        super(user_id, name);
    }

    @Override
    public UserRole get_role(){ return UserRole.REGULAR; }
}
