package ranking;

import applicantclasses.Applicant;
import applicantclasses.ApplicantManagement;
import ioprocessing.AIRequests;
import ioprocessing.FileProcessor;
import ioprocessing.GetUserInput;
import job.DetailType;
import job.JobDescription;
import job.JobManagement;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class RankingManagement implements Serializable {
    @Serial
    private static final long serialVersionUID = -2066535545963607963L;
    private HashMap<Integer, JobDescription> jobDescriptionMap;
    private HashMap<Integer, Applicant> applicantMap;
    private HashMap<Integer, ArrayList<ApplicantJobScore>> applicantRankingMap; //Integer is a JobDescription ID
    private static final String applicantRankingFile = "arSaveFile.dat";
    private ApplicantManagement applicantManagement;
    private JobManagement jobManagement;

    //constructor which restores previous rankings
    public RankingManagement() {
        restoreApplicantRanking(applicantRankingFile);
    }

    //function to restore hashmap of applicant ranking
    public void restoreApplicantRanking(String fName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fName));
            applicantRankingMap = (HashMap<Integer, ArrayList<ApplicantJobScore>>)ois.readObject();
            ois.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("No applicant rankings yet.");
            applicantRankingMap = new HashMap<>();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //function to delete one applicant from all rankings they appear in
    public void deleteApplicantIDFromRanking(int toDelete) {
        for (int jobID : applicantRankingMap.keySet()) {
            //ref Ramesh PVK (2012) 'Delete from the end of ArrayList while inside a loop' [Stack Overflow] 24 May. Available at: https://stackoverflow.com/revisions/10738775/1 (Accessed: 15 April 2025)
            for (int i = applicantRankingMap.get(jobID).size() - 1; i >= 0; i--) {
                if (applicantRankingMap.get(jobID).get(i).getApplicantID() == toDelete) {
                    applicantRankingMap.get(jobID).remove(applicantRankingMap.get(jobID).get(i));
                }
            }
        }
    }

    //function to delete one job from the ranking hashmap
    public void deleteJobDescriptionIDFromRanking(int toDelete) {
        if (!applicantRankingMap.containsKey(toDelete)) {
            System.out.println("No applicants had been scored against job " + toDelete + ".");
            return;
        }
        applicantRankingMap.remove(toDelete);
        System.out.println("Applicant scores for job " + toDelete + " have been deleted.");
    }

    //function for overall actions to be taken during View Ranking main menu option
    public void viewRanking() {
        if (jobDescriptionMap.isEmpty()) {
            System.out.println("There are not currently any job descriptions that rankings could be viewed for. Please add a job description from the main menu.");
            return;
        }
        int toRank = jobManagement.doesJobDescriptionExist("view the ranking for");
        if (toRank == 0) {
            System.out.println("Cancelling ranking view.");
            return;
        }
        if (!applicantRankingMap.containsKey(toRank)) {
            System.out.println("No applicants have been scored against job " + toRank + " yet.");
            ArrayList<ApplicantJobScore> applicantsForConsideration = new ArrayList<>();
            if (scoreApplicantMenu(toRank, applicantsForConsideration) == 0) {
                return;
            }
        }
        FilterSortRanking filterSort = sortAndFilter(toRank, ApplicantRanking.filterApplicants(applicantRankingMap.get(toRank), jobDescriptionMap.get(toRank).getJobSections()), ApplicantRanking.printSortingOptions(applicantRankingMap.get(toRank)), 0);
        GetUserInput.pauseProgram("applicant ranking");

        int rankingContinue = 0;
        while (rankingContinue != 5) {
            rankingContinue = GetUserInput.runMenu("The actions that can be taken are: ", "View an applicants details", "Score another applicant", "Filter differently", "Sort differently", "Return to main menu");
            switch (rankingContinue) {
                case 1: //View an applicants details
                    System.out.println(viewApplicantDetails(filterSort.getRequirementFilteredApplicants(), toRank));
                    GetUserInput.pauseProgram("applicant ranking");
                    break;
                case 2: //Score another applicant
                    if (scoreApplicantMenu(toRank, applicantRankingMap.get(toRank)) == 0) {
                        GetUserInput.pauseProgram("applicant ranking");
                        break;
                    }
                    filterSort = sortAndFilter(toRank, ApplicantRanking.filterApplicants(applicantRankingMap.get(toRank), jobDescriptionMap.get(toRank).getJobSections()), ApplicantRanking.printSortingOptions(applicantRankingMap.get(toRank)), filterSort.getLimitNumberApplicants());
                    GetUserInput.pauseProgram("applicant ranking");
                    break;
                case 3: //filter differently
                    if (applicantRankingMap.get(toRank).size() == 1) {
                        System.out.println("As there is only one applicant who has been scored for this job there is no alternative filtering option.");
                    } else {
                        filterSort = sortAndFilter(toRank, ApplicantRanking.filterApplicants(applicantRankingMap.get(toRank), jobDescriptionMap.get(toRank).getJobSections()), filterSort.getSortingChoice(), 0);
                    }
                    GetUserInput.pauseProgram("applicant ranking");
                    break;
                case 4: //Sort differently
                    if (filterSort.getRequirementFilteredApplicants().size() == 1) {
                        System.out.println("As there is only one applicant in the current filtering criteria there is no alternative sorting option.");
                    } else {
                        filterSort = sortAndFilter(toRank, filterSort.getRequirementFilteredApplicants(), ApplicantRanking.printSortingOptions(applicantRankingMap.get(toRank)), filterSort.getLimitNumberApplicants());
                    }
                    GetUserInput.pauseProgram("applicant ranking");
                    break;
                case 5: //Return to main menu
                default:
                    break;
            }
        }
    }

    //function to run through sorting and filtering choices. If they aren't being changed then values are passed to the function, if they are being changed then functions are passed
    public FilterSortRanking sortAndFilter(int toRank, ArrayList<ApplicantJobScore> applicantList, int currentSortOption, int currentFilterOption) {
        FilterSortRanking filterSort = new FilterSortRanking();
        ArrayList<ApplicantJobScore> requirementFilteredApplicants;
        //if only one applicant has been scored against the job
        if (applicantRankingMap.get(toRank).size() == 1) {
            requirementFilteredApplicants = applicantRankingMap.get(toRank);
        //if multiple candidates have been scored, check how the user wants to filter them
        } else {
            requirementFilteredApplicants = applicantList;
        }
        //if only one applicant after filtering so no need to sort
        if (requirementFilteredApplicants.size() == 1) {
            ApplicantRanking.printRanking(jobDescriptionMap.get(toRank), requirementFilteredApplicants);
            filterSort.setRequirementFilteredApplicants(requirementFilteredApplicants);
            return filterSort;
        }
        //more than one applicant after filtering, so can sort and limit number of results
        int sortOption = currentSortOption;
        ApplicantRanking.sortRanking(requirementFilteredApplicants, jobDescriptionMap.get(toRank), sortOption);
        int filterOption;
        if (currentFilterOption == 0 || currentFilterOption == requirementFilteredApplicants.size() - 1 || requirementFilteredApplicants.size() == 2) {
            filterOption = GetUserInput.getInt("How many applicants do you want displayed up to " + requirementFilteredApplicants.size() + "?", 1, requirementFilteredApplicants.size());
        } else {
            filterOption = currentFilterOption;
        }
        ApplicantRanking.printRanking(jobDescriptionMap.get(toRank), requirementFilteredApplicants, filterOption);
        filterSort.setRequirementFilteredApplicants(requirementFilteredApplicants);
        filterSort.setSortingChoice(sortOption);
        filterSort.setLimitNumberApplicants(filterOption);
        return filterSort;
    }

    //function to print an applicants details out of those in the ranking
    public String viewApplicantDetails(ArrayList<ApplicantJobScore> applicantsForConsideration, int toRank) {
        HashMap<Integer, Applicant> applicantMap = applicantManagement.getApplicantMap();
        ApplicantJobScore newAJS = new ApplicantJobScore();
        boolean applicantHere = false;
        int selectedApplicant = 0;
        while (!applicantHere) {
            selectedApplicant = GetUserInput.getInt("Which applicant do you want to view the details of?");
            for (ApplicantJobScore singleAJS : applicantsForConsideration) {
                if (singleAJS.getApplicantID() == applicantMap.get(selectedApplicant).getApplicantID()) {
                    applicantHere = true;
                    newAJS = singleAJS;
                }
            }
            if (!applicantHere) {
                System.out.println("The applicant selected wasn't among those in the ranking.");
            }
        }
        return newAJS.getApplicantJobScoreDetails(jobDescriptionMap.get(toRank), applicantMap.get(selectedApplicant), newAJS.getJobSections());

    }

    //function to score extra applicant(s) against the job
    public int scoreApplicantMenu(int jobBeingScored, ArrayList<ApplicantJobScore> applicantsForConsideration) {
        try {
            //if no applicants have been added, see if the user wants to upload any (shouldn't be able to get here as viewing the ranking checks for empty applicants first)
            if (applicantMap.isEmpty()) {
                System.out.println("No applicants have been added.");
                if (!GetUserInput.getUserYesOrNo("Do you want to upload an applicant?")) {
                    System.out.println("Returning to menu.");
                    return 0;
                }
                if (!applicantManagement.uploadNewCV()) {
                    return 0;
                }
            }
            //print numbered list of applicants that can be scored against the job (that haven't been already)
            ArrayList<Integer> applicantsAvailableForScoring = new ArrayList<Integer>();
            int counter = listApplicantsAvailableForScore(applicantsForConsideration, applicantsAvailableForScoring);
            String[] cvFiles = FileProcessor.getFiles("CVs", "uploadedCVs.csv");
            if (applicantsAvailableForScoring.isEmpty() && cvFiles.length == 0) {
                System.out.println("There are no further applicants available for scoring.");
                System.out.println(FileProcessor.correctPath("CVs") + "\nif you would like to score applicants more applicants.");
                return 0;
            }
            //all the non-applicantID counters are getting their own name, with both counter then being set to cancel and upload to allow further statements to work for both conditions
            int allApplicantsCounter = counter;
            //if there aren't applicants that can still be scored:
            if (applicantsAvailableForScoring.isEmpty()) {
                System.out.println("All applicants have already been scored against this job.");
                //return 0;
            } else {
                System.out.printf("%4d. ALL AVAILABLE APPLICANTS\n", allApplicantsCounter);
            }
            int cancelCounter = allApplicantsCounter + 1;
            counter = cancelCounter;
            System.out.printf("%4d. CANCEL SCORING\n", counter);
            int uploadNewCounter = 0;
            //if there are applicants in the CVs folder that haven't been uploaded, include upload new applicant as an option
            if (cvFiles.length != 0) {
                uploadNewCounter = cancelCounter + 1;
                counter = uploadNewCounter;
                System.out.printf("%4d. UPLOAD NEW APPLICANT\n", uploadNewCounter);
            } else {
                System.out.println(FileProcessor.correctPath("CVs") + "\nif you would like to score applicants not listed here.");
            }
            //ask which applicants to score
            ArrayList<Integer> applicantsSelected = GetUserInput.getUserArrayListInt("Which applicant(s) do you want to score? Enter each applicant number", counter, allApplicantsCounter, cancelCounter, uploadNewCounter, applicantsAvailableForScoring);
            if (applicantsSelected == null) {
                return 0;
            }
            //select upload new applicant
            if (applicantsSelected.contains(0)) {
                if (!applicantManagement.uploadNewCV()) {
                    return 0;
                }
                ArrayList<Integer> newApplicantsAvailableForScoring = new ArrayList<>();
                int secondCounter = listApplicantsAvailableForScore(applicantsForConsideration, newApplicantsAvailableForScoring);
                allApplicantsCounter = secondCounter;
                System.out.printf("%4d. ALL AVAILABLE APPLICANTS\n", allApplicantsCounter);
                secondCounter++;
                cancelCounter = secondCounter;
                System.out.printf("%4d. CANCEL SCORING\n", cancelCounter);
                ArrayList<Integer> newApplicantsSelected = GetUserInput.getUserArrayListInt("Which applicant(s) do you want to score? Enter each applicant number", counter, allApplicantsCounter, cancelCounter, uploadNewCounter, newApplicantsAvailableForScoring);
                if (newApplicantsSelected == null) {
                    return 0;
                }
                int successfulScoring = 0;
                for (int i : newApplicantsSelected) {
                    if (scoreApplicant(jobBeingScored, i)) {
                        successfulScoring++;
                    }
                }
                if (successfulScoring > 0) {
                    return 1;
                } else {
                    return 0;
                }
            //else not uploading new applicant(s)
            } else {
                int successfulScoring = 0;
                for (int i : applicantsSelected) {
                    if (scoreApplicant(jobBeingScored, i)) {
                        successfulScoring++;
                    }
                }
                if (successfulScoring > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error scoring the applicant. Please try again later.");
            return 0;
        }
    }

    //function to print out applicants that haven't been scored against the job (and add them to the arraylist for the user to select from
    public int listApplicantsAvailableForScore(ArrayList<ApplicantJobScore> applicantsForConsideration, ArrayList<Integer> applicantsAvailableForScoring) {
        int counter = 1;
        for (int applicant : applicantMap.keySet()) {
            boolean applicantAlreadyScored = false;
            for (ApplicantJobScore ajs : applicantsForConsideration) {
                if (applicantMap.get(applicant).getApplicantID() == ajs.getApplicantID()) {
                    applicantAlreadyScored = true;
                }
            }
            if (!applicantAlreadyScored) {
                System.out.println(applicantMap.get(applicant).applicantShortDetails());
                applicantsAvailableForScoring.add(applicantMap.get(applicant).getApplicantID());
            }
            counter++;
        }
        return counter;
    }

    //function to score an applicant against the job (by sending applicant and job to Gemini)
    public boolean scoreApplicant(int jobBeingScored, int applicantID) {
        ApplicantJobScore newAJS = new ApplicantJobScore(applicantID);
        if (applicantMap.get(applicantID).getCvDetail() == null || applicantMap.get(applicantID).getCvDetail().equalsIgnoreCase("n/a")) {
            if (!ApplicantManagement.fillOutApplicantDetails(applicantMap.get(applicantID))) {
                //abort if the applicant details couldn't be extracted
                return false;
            }
        }
        String applicantInfo = applicantMap.get(applicantID).getCvDetail();
        String jobInfo = jobDescriptionMap.get(jobBeingScored).detailsString(jobDescriptionMap.get(jobBeingScored).getJobSections());
        System.out.println("Scoring applicant " + applicantID);
        String booleanReturn = AIRequests.getAIResponse(jobInfo, applicantInfo);
        if (booleanReturn.equalsIgnoreCase("")) {
            //abort if Gemini processing failed
            return false;
        }
        HashMap<DetailType, ArrayList<Boolean>> sections = newAJS.fillOutAJSSections(booleanReturn, jobDescriptionMap.get(jobBeingScored));
        if (sections == null) {
            return false;
        }
        newAJS.setJobSections(sections);
        ArrayList<ApplicantJobScore> ajsList = new ArrayList<>();
        if (applicantRankingMap.get(jobBeingScored) != null) {
            ajsList = applicantRankingMap.get(jobBeingScored);
        } else {
            applicantRankingMap.put(jobBeingScored, ajsList);
        }
        ajsList.add(newAJS);
        applicantRankingMap.replace(jobBeingScored, ajsList);
        return true;
    }

    //function to save hashmap of applicant ranking
    public void saveApplicantRanking(String fName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fName));
            oos.writeObject(applicantRankingMap);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //function to perform actions to be taken when the program exits
    public void close() {
        saveApplicantRanking(applicantRankingFile);
    }

    //setter functions

    public void setJobManagement(JobManagement jobManagement) {
        this.jobManagement = jobManagement;
        jobDescriptionMap = jobManagement.getJobDescriptionMap();
    }

    public void setApplicantManagement(ApplicantManagement applicantManagement) {
        this.applicantManagement = applicantManagement;
        applicantMap = applicantManagement.getApplicantMap();
    }
}
