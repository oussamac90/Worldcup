package com.goalkeeperdash.user.api;

import java.util.List;

/** Nation configuration exposed to the back-office (§7.2). */
public interface NationAdminService {

    List<NationView> listAll();

    void setActive(String code, boolean active);

    void update(String code, String name, String flagColors);
}
