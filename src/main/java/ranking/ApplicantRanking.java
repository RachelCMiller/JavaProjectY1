package ranking;

import ioprocessing.*;
import job.DetailType;
import job.JobDescription;
import job.JobDetail;
import java.util.HashMap;
import java.util.ArrayList;

public class ApplicantRanking {

    //function to allow the user to choose how to filter applicants in the ranking
    public static ArrayList<ApplicantJobScore> filterApplicants(ArrayList<ApplicantJobScore> ajs, HashMap<DetailType, ArrayList<JobDetail>> jobSections) {
        if (ajs.size() == 1) {
            return ajs;
        }
        HashMap<DetailType, ArrayList<Integer>> requirementPosition = new HashMap<>(); //hashmap to link number printed out to the requirement printed from the original hashmap
        ArrayList<String> requirementPhrase = new ArrayList<>(); //arraylist to access requirement phrase chosen later
        int counter = 0; //for each requirement position
        for (DetailType i : DetailType.values()) {
            ArrayList<Integer> requirementArray = new ArrayList<>(); //array that'll go in the requirementPosition hashmap
            ArrayList<JobDetail> sectionDetails = jobSections.get(i);
            System.out.println(i);
            for (JobDetail j : sectionDetails) {
                System.out.printf("%4d. " + j.getDetail() + "\n", counter);
                requirementArray.add(counter);
                requirementPhrase.add(j.getDetail());
                counter++;
            }
            requirementPosition.put(i, requirementArray);
        }
        System.out.println("NO FILTER");
        System.out.printf("%4d. No filter\n", counter);
        boolean goodFilter = false;
        ArrayList<ApplicantJobScore> filteredApplicants = new ArrayList<>(); //arraylist that'll be added to and returned at the end of the function
        while (!goodFilter) {
            int filterChoice = GetUserInput.getInt("Which requirement do you want to filter for?", 0, counter);
            DetailType detailIndex;
            int detailPosition;
            if (filterChoice == requirementPhrase.size()) {
                //if no filter chosen return original ranking
                return ajs;
            }
            //new variables to save multiple calls
            int positionIfEducation = requirementPosition.get(DetailType.EDUCATION).indexOf(filterChoice);
            int positionIfExperience = requirementPosition.get(DetailType.EXPERIENCE).indexOf(filterChoice);
            //if the chosen requirement is in the education part of the hashmap (i.e. index is not -1), set locating variables to that position
            if (positionIfEducation != -1) {
                detailIndex = DetailType.EDUCATION;
                detailPosition = positionIfEducation;
            } else if (positionIfExperience != -1) {
                detailIndex = DetailType.EXPERIENCE;
                detailPosition = positionIfExperience;
            } else {
                detailIndex = DetailType.SKILLS;
                detailPosition = requirementPosition.get(DetailType.SKILLS).indexOf(filterChoice);
            }
            //for each applicant in original ranking, check if their detailIndex and detailPosition is true, if it is, add them to filteredApplicants
            for (ApplicantJobScore applicant : ajs) {
                if (applicant.getJobSections().get(detailIndex).get(detailPosition)) {
                    filteredApplicants.add(applicant);
                }
            }
            if (filteredApplicants.isEmpty()) {
                System.out.println("No applicants match the requirement " + requirementPhrase.get(filterChoice));
            } else {
                goodFilter = true;
            }
        }
        return filteredApplicants;
    }

    //function to print the sorting options and get which the user wants
    public static int printSortingOptions(ArrayList<ApplicantJobScore> ajs) {
        if (ajs.size() == 1) {
            return 1; //if only one applicant in ranking automatically set sorting to total score
        }
        return GetUserInput.runMenu("Ranking Sorting", "Total Score", "Education Score", "Experience Score", "Skills Score");
    }

    //function sort applicants by users choice
    public static void sortRanking(ArrayList<ApplicantJobScore> ajs, JobDescription job, int userChoice) {
        //ref W3Schools (no date) 'Java Advanced Sorting (Comparator and Comparable)'. Available at: https://www.w3schools.com/java/java_advanced_sorting.asp (Accessed: 22 March 2025)
        switch (userChoice) {
            case 1: //sort highest to lowest total score
                ajs.sort((ajs1, ajs2) -> Integer.compare(ajs2.getApplicantTotalScore(job), ajs1.getApplicantTotalScore(job)));
                break;
            case 2: //sort highest to lowest education score
                ajs.sort((ajs1, ajs2) -> Integer.compare(ajs2.getSectionScore(DetailType.EDUCATION, ajs2.getJobSections().get(DetailType.EDUCATION), job), ajs1.getSectionScore(DetailType.EDUCATION, ajs1.getJobSections().get(DetailType.EDUCATION), job)));
                break;
            case 3: //sort highest to lowest experience score
                ajs.sort((ajs1, ajs2) -> Integer.compare(ajs2.getSectionScore(DetailType.EXPERIENCE, ajs2.getJobSections().get(DetailType.EXPERIENCE), job), ajs1.getSectionScore(DetailType.EXPERIENCE, ajs1.getJobSections().get(DetailType.EXPERIENCE), job)));
                break;
            case 4: //sort highest to lowest skills score
                ajs.sort((ajs1, ajs2) -> Integer.compare(ajs2.getSectionScore(DetailType.SKILLS, ajs2.getJobSections().get(DetailType.SKILLS), job), ajs1.getSectionScore(DetailType.SKILLS, ajs1.getJobSections().get(DetailType.SKILLS), job)));
                break;
            default: //shouldn't be able to not make a choice
                System.out.println("No sorting choice made.");
                break;
        }
    }

    //function to print horizontal borders around tables
    public static void printTableBorders(int width) {
        for (int i = 0; i < width; i++) {
            System.out.print("*");
        }
        System.out.print("\n");
    }

    //function to print out the table of sorted applicant job scores (when multiple applicant scores)
    public static void printRanking(JobDescription job, ArrayList<ApplicantJobScore> ajs, int limitNumberApplicants) {
        ArrayList<String> applicantScores = new ArrayList<>();
        int width = 75;
        for (int i = 0; i < limitNumberApplicants; i++) {
            String iScore = printApplicantScoresLine(job, ajs, i);
            applicantScores.add(iScore);
            if (iScore.length() > width) {
                width = iScore.length();
            }
        }
        printRankingHeader(job.getJobTitle(), width);
        for (String applicantScore : applicantScores) {
            System.out.printf("%-"+width+"s*\n", applicantScore, " ");
        }
        printTableBorders(width + 1);
    }

    //function to print out the table of applicant job scores when there's only one applicant scored
    public static void printRanking(JobDescription job, ArrayList<ApplicantJobScore> ajs) {
        String applicantScore = printApplicantScoresLine(job, ajs, 0);
        int width = 75;
        if (applicantScore.length() > width) {
            width = applicantScore.length();
        }
        printRankingHeader(job.getJobTitle(), width);
        System.out.printf("%-"+width+"s*\n", applicantScore);
        printTableBorders(width + 1);
    }

    //function to print the header row of the ranking table
    public static void printRankingHeader(String jobTitle, int width) {
        System.out.println("Job: " + jobTitle);
        printTableBorders(width + 1);
        String header = String.format("* Applicant ID | Total |  Education | Experience | %4sSkills | Best Match", " ");
        System.out.printf("%-"+width+"s*\n", header, " ");
        printTableBorders(width + 1);
    }

    //function to print an applicants ID, scores for each section and highest match
    public static String printApplicantScoresLine(JobDescription job, ArrayList<ApplicantJobScore> ajs, int applicant) {
        //print applicant score for each category
        StringBuilder s = new StringBuilder();
        //ref W3Schools (no date) 'Java String format() Method'. Available at: https://www.w3schools.com/java/ref_string_format.asp (Accessed: 13 April 2025)
        s.append(String.format("* %-12d | %5s ", ajs.get(applicant).getApplicantID(), ajs.get(applicant).getApplicantTotalScore(job)));
        for (DetailType j : DetailType.values()) {
            int sectionScore = ajs.get(applicant).getSectionScore(j, ajs.get(applicant).getJobSections().get(j), job);
            String section = "| %10s ";
            s.append(String.format(section, sectionScore));
        }
        //and highest scoring match
        s.append("| ").append(ajs.get(applicant).getBestMatch(job)).append(" ");
        return s.toString();
    }
}
