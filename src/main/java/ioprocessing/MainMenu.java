package ioprocessing;

import applicantclasses.ApplicantManagement;
import job.JobManagement;
import ranking.RankingManagement;

public class MainMenu {
    private JobManagement jobManagement;
    private ApplicantManagement applicantManagement;
    private RankingManagement rankingManagement;

    //constructor with required objects and HashMaps
    public MainMenu() {
        rankingManagement = new RankingManagement();
        jobManagement = new JobManagement(rankingManagement);
        applicantManagement = new ApplicantManagement(rankingManagement);
        rankingManagement.setApplicantManagement(applicantManagement);
        rankingManagement.setJobManagement(jobManagement);
    }

    //function to give the user the main menu options and action their choice
    public void runMainMenu() {
        boolean exit = false;
        while (!exit) {
            System.out.println("Welcome to the Smart CV Analyser!");
            int menuOption = GetUserInput.runMenu("The actions that can be taken are:", "Add Job Description",
                    "Delete Job Description", "Upload CVs", "Manage Applicants", "View Applicant Ranking", "Exit");
            switch (menuOption) {
                case 1: //add job description
                    jobManagement.addJobDescription();
                    GetUserInput.pauseProgram();
                    break;
                case 2: //delete a job description (e.g. when the position has been filled)
                    jobManagement.deleteJobDescription();
                    GetUserInput.pauseProgram();
                    break;
                case 3: //uploading CVs (add applicant)
                    applicantManagement.uploadNewCV();
                    GetUserInput.pauseProgram();
                    break;
                case 4: //manage applicants
                    applicantManagement.manageApplicants();
                    GetUserInput.pauseProgram();
                    break;
                case 5: //view applicant ranking
                    rankingManagement.viewRanking();
                    GetUserInput.pauseProgram();
                    break;
                case 6: //exit
                    System.out.println("Exiting...");
                    if (GetUserInput.getUserYesOrNo("Are you sure you want to exit?")) {
                        exit = true;
                    }
                    break;
                default:
                    System.out.println("Unexpected input to main menu options.");
                    GetUserInput.pauseProgram();
                    break;
            }
        }
        applicantManagement.close();
        jobManagement.close();
        rankingManagement.close();
    }
}
