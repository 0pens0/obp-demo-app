package com.obp.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObpUser {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}
