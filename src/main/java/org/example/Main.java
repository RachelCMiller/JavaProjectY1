package org.example;

//the project plan included user profiles for logging in which hasn't been added to focus on developing the key functionality of the program
//login options could be added during further development

//the project plan also included functional requirement 2.2 and 3.4, an extraConsideration/applicantFollowup boolean to indicate applicants who may need more careful consideration
//such as having declared a disability, had a career break for "valid reasons" such as parental leave, or were self-taught developers
//however the actual implementation of this would likely need greater consideration and discussion with other teams/stakeholders such as HR or legal
//for example:
// - a detailed list of everything that would warrant an extraConsideration flag
// - what should be done with applicants with an extraConsideration flag
// (Should they always be printed in rankings? Should an extra column be added to indicate tru/false for extraConsideration? Rather than boolean being enum for the reason for extra consideration?)

import ioprocessing.*;

public class Main {

    public static void main(String[] args) {
        MainMenu mm = new MainMenu();
        mm.runMainMenu();
    }
}