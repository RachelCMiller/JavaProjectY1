package applicantclasses;

import ioprocessing.AIRequests;
import ioprocessing.FileProcessor;
import ioprocessing.GetUserInput;
import ranking.RankingManagement;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ApplicantManagement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1776216122314032506L;
    private HashMap<Integer, Applicant> applicantMap; //Integer is ApplicantID
    private static final String applicantFile = "applicantSaveFile.dat";
    private RankingManagement rankingManagement;

    //initial constructor
    public ApplicantManagement(RankingManagement rankingManagement) {
        this.rankingManagement = rankingManagement;
        applicantMap = restoreApplicants(applicantFile);
    }

    //function to run the sub menu for managing applicants (try failed extraction again or delete)
    public void manageApplicants() {
        boolean goToMainMenu = false;
        while (!goToMainMenu) {
            int manageApplicantChoice = GetUserInput.runMenu("Manage applicant options:", "Add Applicant", "Retry Detail Extraction", "Delete Applicant", "Return to Main Menu");
            switch (manageApplicantChoice) {
                case 1:
                    uploadNewCV();
                    GetUserInput.pauseProgram("manage applicants");
                    break;
                case 2:
                    retryApplicantDetailExtraction();
                    GetUserInput.pauseProgram("manage applicants");
                    break;
                case 3:
                    deleteApplicant();
                    GetUserInput.pauseProgram("manage applicants");
                    break;
                case 4:
                default:
                    goToMainMenu = true;
                    break;
            }
        }
    }

    //function for overall actions to be taken during Upload CVs main menu option (or adding an applicant during View Ranking)
    public boolean uploadNewCV() {
        int currentApplicantID;
        //get current applicants to set the id for the next upload
        if (!applicantMap.isEmpty()) {
            currentApplicantID = Collections.max(applicantMap.keySet()) + 1;
        } else {
            //when no applicants, set to 1
            currentApplicantID = 1;
        }
        //user chooses applicants to upload from files and applicant objects are created
        return GetUserInput.printAndChooseFile("CVs", "uploadedCVs.csv", currentApplicantID, this::createApplicantObject, false);
    }

    //function that combines functions that process a new applicant and print statements to update the user on progress
    public boolean createApplicantObject(int newApplicantID, String cvFile) {
        System.out.println("Processing " + cvFile);
        Applicant applicantObj = processCV(newApplicantID, cvFile);
        applicantMap.put(applicantObj.getApplicantID(), applicantObj);
        updateUploadedCVsCSV(cvFile);
        System.out.println("Applicant " + newApplicantID + " uploaded from " + cvFile + "\n");
        //returning boolean for if contact detail extraction worked or not to report on multiple extractions elsewhere
        return !applicantObj.getName().equalsIgnoreCase("n/a");
    }

    //function to create a new applicant. Read text from CV, initially create applicant, then attempt to fill out contact details
    public static Applicant processCV(int applicantID, String fName) {
        String filePath = FileProcessor.getPathName("CVs", fName);
        String cvText = FileProcessor.extractText(fName, filePath);
        Applicant newApplicant = new Applicant(applicantID, cvText, fName);
        fillOutApplicantDetails(newApplicant);
        return newApplicant;
    }

    //function to pick out details from Gemini's response to construct applicant contact info
    public static boolean fillOutApplicantDetails(Applicant newApplicant) {
        try {
            System.out.println("Extracting applicant " + newApplicant.getApplicantID() + " details.");
            String aiString = AIRequests.getAIResponse(newApplicant.getCvText(), newApplicant.getApplicantID());
            //return from api and match parts
            String[] aiStringParts = aiString.split(",");
            newApplicant.setName(aiStringParts[0]);
            newApplicant.setPhoneNumber(aiStringParts[1]);
            newApplicant.setEmail(aiStringParts[2]);
            newApplicant.setLinkedIn(aiStringParts[3]);
            newApplicant.setAddress(aiStringParts[4]);
            newApplicant.setCvDetail(aiStringParts[5]);
            newApplicant.setExtractionError(false);
            System.out.println("Applicant " + newApplicant.getApplicantID() + " details extracted.");
            return true;
        } catch (Exception e) {
            newApplicant.setName("n/a");
            newApplicant.setPhoneNumber("n/a");
            newApplicant.setEmail("n/a");
            newApplicant.setLinkedIn("n/a");
            newApplicant.setAddress("n/a");
            newApplicant.setCvDetail("n/a");
            newApplicant.setExtractionError(true);
            return false;
        }
    }

    //function to update the file that logs which CVs have been uploaded from the CVs folder
    public void updateUploadedCVsCSV(String cvFile) {
        String fileName = cvFile.toLowerCase();
        String toAppend = fileName.concat(",");
        FileProcessor.appendFile("uploadedCVs.csv", toAppend);
    }

    //function to retry applicant detail extraction
    public void retryApplicantDetailExtraction() {
        if (applicantMap.isEmpty()) {
            System.out.println("No applicants have been uploaded, please select Add Applicants from the Manage Applicants menu to add new applicants.");
            return;
        }
        ArrayList<Integer> applicantsWithExtractionErrors = new ArrayList<>();
        int totalApplicantCounter = 1;
        //check which applicants need re-trying
        for (int id : applicantMap.keySet()) {
            if (applicantMap.get(id).getExtractionError()) {
                System.out.println(applicantMap.get(id).applicantShortDetails());
                applicantsWithExtractionErrors.add(applicantMap.get(id).getApplicantID());
            }
            totalApplicantCounter++;
        }
        if (applicantsWithExtractionErrors.isEmpty()) {
            System.out.println("No applicants require retrying detail extraction.");
            return;
        }
        int allApplicants = totalApplicantCounter;
        System.out.printf("%4d. RETRY ALL APPLICANTS\n", allApplicants);
        totalApplicantCounter++;
        int cancelCounter = totalApplicantCounter;
        System.out.printf("%4d. CANCEL\n", cancelCounter);
        //get user choice of whom to retry
        ArrayList<Integer> toExtract = GetUserInput.getUserArrayListInt("Which applicant do you want to retry extracting? Enter each applicant number", totalApplicantCounter, allApplicants, cancelCounter, totalApplicantCounter, applicantsWithExtractionErrors);
        if (toExtract == null) { //didn't choose anyone to retry
            return;
        }
        int successfulExtractions = 0;
        //attempt to extract all applicants the user chose
        for (int currentExtraction : toExtract) {
            if (fillOutApplicantDetails(applicantMap.get(currentExtraction))) {
                successfulExtractions++;
            }
        }
        System.out.println("Applicants with details extracted: " + successfulExtractions + " out of " + toExtract.size());
    }

    //function to delete applicant
    public void deleteApplicant() {
        if (applicantMap.isEmpty()) {
            System.out.println("There are not currently any applicants.");
            return;
        }
        //print options
        int counter = 0;
        for (int id : applicantMap.keySet()) {
            counter = applicantMap.get(id).getApplicantID();
            System.out.println(applicantMap.get(id).applicantShortDetails());
        }
        counter++;
        System.out.printf("%4d. CANCEL\n", counter);
        //get user choice of whom to delete
        int toDelete;
        do {
            toDelete = GetUserInput.getInt("Which applicant do you want to delete?: ");
            if (!applicantMap.containsKey(toDelete) && toDelete != counter) {
                System.out.println("Apologies, that applicant doesn't exist.");
            }
        } while (!applicantMap.containsKey(toDelete) && toDelete != counter);
        //abort deletion if user selected cancel
        if (toDelete == counter) {
            System.out.println("Cancelling applicant deletion.");
            return;
        }
        //confirm user wants to delete the selected applicant
        System.out.println(applicantMap.get(toDelete).applicantContactDetails());
        if (!GetUserInput.getUserYesOrNo("Are you sure you want to delete the above applicant?")) {
            System.out.println("Applicant " + toDelete + " not being deleted.");
            return;
        }
        //remove applicant from all rankings they're in and then from applicants
        rankingManagement.deleteApplicantIDFromRanking(toDelete);
        applicantMap.remove(toDelete);
    }

    //function to save hashmap of applicants
    public void saveApplicants(String fName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fName));
            oos.writeObject(applicantMap);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //function to restore hashmap of applicants
    public HashMap<Integer, Applicant> restoreApplicants(String fName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fName));
            applicantMap = (HashMap<Integer, Applicant>)ois.readObject();
            ois.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("No applicants yet.");
            applicantMap = new HashMap<>();
        } catch(Exception e) {
            e.printStackTrace();
            applicantMap = new HashMap<>();
        }
        return applicantMap;
    }

    //function to perform actions when the user exits the program
    public void close() {
        saveApplicants(applicantFile);
    }

    //function to get the applicantMap in RankingManagement
    public HashMap<Integer, Applicant> getApplicantMap() {
        return applicantMap;
    }
}
