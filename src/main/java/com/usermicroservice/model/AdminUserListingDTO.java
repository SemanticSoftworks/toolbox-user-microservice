package com.usermicroservice.model;

import java.util.List;

/**
 * Created by dani on 2017-02-27.
 */
public class AdminUserListingDTO {

    private List<AdminUserDTO> adminUserDTOList;

    public List<AdminUserDTO> getAdminUserDTOList() { return adminUserDTOList; }

    public void setAdminUserDTOList(List<AdminUserDTO> adminUserDTOList) { this.adminUserDTOList = adminUserDTOList; }
}
