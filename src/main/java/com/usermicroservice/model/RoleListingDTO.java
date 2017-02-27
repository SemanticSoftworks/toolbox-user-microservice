package com.usermicroservice.model;

import java.util.List;

/**
 * Created by dani on 2017-02-27.
 */
public class RoleListingDTO {

    private List<RoleDTO> roles;

    public List<RoleDTO> getRoles() { return roles; }

    public void setRoles(List<RoleDTO> roles) { this.roles = roles; }
}
