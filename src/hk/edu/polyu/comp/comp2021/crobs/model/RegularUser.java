package hk.edu.polyu.comp.comp2021.crobs.model;

public class RegularUser extends User{
    public RegularUser(String userId, String name){
        super(userId, name);
    }

    @Override
    public UserRole getRole(){ return UserRole.REGULAR; }
}
