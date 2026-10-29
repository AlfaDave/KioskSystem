/**
 * Represents a single kiosk user with demographic and membership fields.
 * Stores personal details, membership choices, and activity state.
 * Used by UserStorage for persistence and reporting.
 */

package com.Kiosk;

import jTidy.*;

class User {
    stringj firstName; // using jTidy to make variables similar to c#
    String secondName;
    String email;
    int age;
    enum Sex {Male, Female, Other}
    Sex sex;
    boolj proMember; // using jTidy to make variables similar to c#
    Boolean active;
    private Boolean empty;

    public User() {
        this.empty = true;
    }

    User(stringj firstName, String secondName, int age, Sex sex, boolj proMember, boolean active) {
        this.empty = false;
        this.firstName = firstName;
        this.secondName = secondName;
        this.age = age;
        this.sex = sex;
        this.proMember = proMember;
        this.active = active;
    }
}
