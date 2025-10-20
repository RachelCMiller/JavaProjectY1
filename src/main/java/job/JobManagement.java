package job;

import ioprocessing.*;
import ranking.RankingManagement;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class JobManagement implements Serializable {
    @Serial
    private static final long serialVersionUID = 812955077841537546L;
    private HashMap<Integer, JobDescription> jobDescriptionMap; //Integer is a JobDescription ID
    private static final String jobDescriptionFile = "jobSaveFile.dat";
    private final RankingManagement rankingManagement;

    //constructor - restores the job HashMap once and allows use of RankingManagement functions
    public JobManagement(RankingManagement rankingManagement) {
        restoreJobs(jobDescriptionFile);
        this.rankingManagement = rankingManagement;
    }

    //function to restore the hashmap of jobs
    public void restoreJobs(String fName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fName));
            jobDescriptionMap = (HashMap<Integer, JobDescription>)ois.readObject();
            ois.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("No job descriptions yet.");
            jobDescriptionMap = new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            jobDescriptionMap = new HashMap<>();
        }
    }

    //function for overall actions to be taken during Add Job Description main menu option
    public void addJobDescription() {
        int newJobID = 1;
        //if there are already job descriptions, set the next job description id to be one bigger than the current biggest
        if (!jobDescriptionMap.isEmpty()) {
            //ref GeeksForGeeks (2018) 'Finding minimum and maximum element of a Collection in Java'. Available at: https://www.geeksforgeeks.org/finding-minimum-and-maximum-element-of-a-collection-in-java/ (Accessed: 28 March 2025)
            newJobID = Collections.max(jobDescriptionMap.keySet()) + 1;
        }
        int jobInput = GetUserInput.runMenu("How would you like to add a job?", "Text input", "File upload", "CANCEL");
        switch (jobInput) {
            case 1: //text input
                inputJob(newJobID);
                break;
            case 2: //file upload
                GetUserInput.printAndChooseFile("Jobs", "uploadedJobs.csv", newJobID, this::createJobObject, true);
                break;
            case 3: //cancel
                System.out.println("Cancelling new job description creation.");
                break;
        }
    }

    //function to allow the user to add a new job description through text input
    public void inputJob(int newJobID) {
        JobDescription jd = new JobDescription();
        System.out.println("If you want to abort the job creation process respond with \"cancel\" during textual input.");
        String title = GetUserInput.getString("What's the job title?: ", "Please don't leave this blank.");
        if (!title.equalsIgnoreCase("CANCEL")) {
            HashMap<DetailType, ArrayList<JobDetail>> jobSections = inputJobSections();
            if (jobSections != null && editJobDescription(jd, title, jobSections)) {
                jd.setJobID(newJobID);
                jd.setJobTitle(title);
                jd.setJobSections(jobSections);
                jobDescriptionMap.put(newJobID, jd);
                return;
            }
        }
        //if text input at some point was "cancel"
        System.out.println("New job description not created.");
    }

    //function to add the job requirements and their weightings for each DetailType (education, experience, skills)
    public static HashMap<DetailType, ArrayList<JobDetail>> inputJobSections() {
        HashMap<DetailType, ArrayList<JobDetail>> jobSections = new HashMap<>();
        int emptyCounter = 0;
        //ref W3Schools (no date) 'Java How To Loop Through an Enum'. Available at: https://www.w3schools.com/java/java_howto_loop_through_enum.asp (Accessed: 24 March 2025)
        for (DetailType i : DetailType.values()) {
            System.out.println("Please add the details and their weightings for " + i + ". Type \"done\" when you want to move to the next category.");
            ArrayList<JobDetail> details = new ArrayList<>();
            while (true) {
                String detailKeyword = GetUserInput.getString("Detail: ", 20);
                if (detailKeyword.equalsIgnoreCase("CANCEL")) {
                    return null;
                } else if (detailKeyword.equalsIgnoreCase("DONE")) {
                    break;
                }
                JobDetail detail = new JobDetail(detailKeyword, GetUserInput.getInt("Weighting: ", 1, 5));
                details.add(detail);
            }
            jobSections.put(i, details);
            if (details.isEmpty()) {
                emptyCounter++;
            }
        }
        if (emptyCounter == 3) {
            System.out.println("Job descriptions can't have no requirements.");
            return null;
        }
        return jobSections;
    }

    //function to create a job object when uploading a file
    public boolean createJobObject(int newJobID, String jobFile) {
        System.out.println("Processing " + jobFile);
        JobDescription jobObj = extractJob(newJobID, jobFile);
        //if the user cancelled or Gemini response failed, exit without creating a new job
        if (jobObj == null) {
            return false;
        }
        jobDescriptionMap.put(newJobID, jobObj);
        FileProcessor.updateUploadedCSV(jobFile, "uploadedJobs.csv");
        System.out.println("Job " + newJobID + " uploaded from " + jobFile + "\n");
        return true;
    }

    //function to get the job text, send to Gemini for processing, and form a new job description from the returned string
    public static JobDescription extractJob(int jobID, String fName) {
        String filePath = FileProcessor.getPathName("Jobs", fName);
        String jobText = FileProcessor.extractText(fName, filePath);
        JobDescription newJob = new JobDescription();
        System.out.println("Processing job " + jobID + " details.");
        HashMap<DetailType, ArrayList<JobDetail>> jobSections = new HashMap<>();
        String jobSummary = getAIJobDescriptionString(jobID, jobText);
        //abort if Gemini didn't return an answer
        if (jobSummary.equalsIgnoreCase("")) {
            return null;
        }
        //sort the response string into job description fields
        String[] jobParts = jobSummary.split("\\^");
        String title;
        if (jobParts.length == 4) {
            title = jobParts[0];
            jobSections.put(DetailType.EDUCATION, fillOutJobSection(jobParts[1]));
            jobSections.put(DetailType.EXPERIENCE, fillOutJobSection(jobParts[2]));
            jobSections.put(DetailType.SKILLS, fillOutJobSection(jobParts[3]));
        } else {
            System.out.println("Unexpected response from " + AIRequests.getAI());
            System.out.println(jobSummary);
            return null;
        }
        //abort creation if user isn't happy with extracted job
        if (!editJobDescription(newJob, title, jobSections)) {
            System.out.println("Cancelling job upload.");
            return null;
        }
        return new JobDescription(jobID, title, jobSections);
    }

    //function to send the job text to Gemini and return the response
    public static String getAIJobDescriptionString(int jobID, String jobText) {
        try {
            System.out.println("Extracting job " + jobID + " details.");
            return AIRequests.getAIResponse(jobText);
        } catch (Exception e) {
            return "";
        }
    }

    //function to process the job section part of Gemini's response (used for each of education, experience and skills)
    public static ArrayList<JobDetail> fillOutJobSection(String sectionString) {
        ArrayList<JobDetail> jobSection = new ArrayList<>();
        String section = sectionString.substring(sectionString.indexOf(':') + 1);
        String[] details = section.split(",");
        for (int i = 0; i < details.length - 1; i += 2) {
            int detailWeighting = Integer.parseInt(details[i + 1]);
            jobSection.add(new JobDetail(details[i], detailWeighting));
        }
        return jobSection;
    }

    //function to print out current state of the job description and allow the user to edit it if they wish to before finalising its creation
    public static boolean editJobDescription(JobDescription jd, String title, HashMap<DetailType, ArrayList<JobDetail>> jobSections) {
        while (true) {
            System.out.println("** NEW JOB **");
            System.out.println("TITLE: " + title);
            System.out.println(jd.sectionsString(jobSections));
            if (GetUserInput.getUserYesOrNo("Are you happy with the job description?")) {
                System.out.println("Adding new job description.");
                return true;
            }
            int userChoice = GetUserInput.runMenu("The parts that can be changed are: ", "TITLE", "EDUCATION", "EXPERIENCE", "SKILLS", "CANCEL JOB CREATION");
            switch (userChoice) {
                case 1:
                    title = GetUserInput.getString("What's the job title?: ", "Please don't leave this blank.");
                    break;
                case 2:
                    editKeywordAndWeighting(jobSections.get(DetailType.EDUCATION));
                    break;
                case 3:
                    editKeywordAndWeighting(jobSections.get(DetailType.EXPERIENCE));
                    break;
                case 4:
                    editKeywordAndWeighting(jobSections.get(DetailType.SKILLS));
                    break;
                case 5:
                    return false;
                default:
                    System.out.println("Unexpected path.");
                    break;
            }
            if (jobSections.get(DetailType.EDUCATION).isEmpty() && jobSections.get(DetailType.EXPERIENCE).isEmpty() && jobSections.get(DetailType.SKILLS).isEmpty()) {
                System.out.println("Job descriptions can't have no requirements.");
                return false;
            }
        }
    }

    //function to allow the user to select a keyword and weighting to edit out of all the ones in a job section
    public static void editKeywordAndWeighting(ArrayList<JobDetail> section) {
        if (section.isEmpty()) {
            System.out.println("There are no current details.");
            int actionToTake = GetUserInput.runMenu("The actions that you can take are:", "Add a new detail", "Cancel");
            switch (actionToTake) {
                case 1: //add a detail
                    section.add(new JobDetail(GetUserInput.getString("Detail: ", 20), GetUserInput.getInt("Weighting: ", 1, 5)));
                    break;
                case 2:
                default: //cancel changing details
                    System.out.println("Cancelling detail edit.");
                    break;
            }
            return;
        }
        StringBuilder keywordAndWeighting = new StringBuilder();
        System.out.println("The current details and weighting are:");
        for (JobDetail jobDetail : section) {
            keywordAndWeighting.append(jobDetail.toString()).append("\n");
        }
        System.out.print(keywordAndWeighting);
        StringBuilder kwNumbered = new StringBuilder();
        int i;
        for (i = 0; i < section.size(); i++) {
            kwNumbered.append(String.format("%4d. ", i));
            kwNumbered.append(section.get(i).toString()).append("\n");
        }
        int actionToTake = GetUserInput.runMenu("The actions that you can take are:", "Edit a current detail", "Delete a detail", "Add a new detail", "Cancel");
        switch (actionToTake) {
            case 1: //edit a detail
                System.out.println(kwNumbered);
                int detailToEdit = GetUserInput.getInt("What detail do you want to edit?: ", 0, i - 1);
                section.set(detailToEdit, new JobDetail(GetUserInput.getString("Detail: ", 20), GetUserInput.getInt("Weighting: ", 1, 5)));
                break;
            case 2: //delete a detail
                System.out.println(kwNumbered);
                int detailToDelete = GetUserInput.getInt("What detail do you want to delete?: ", 0, i - 1);
                section.remove(detailToDelete);
                break;
            case 3: //add a detail
                section.add(new JobDetail(GetUserInput.getString("Detail: ", 20), GetUserInput.getInt("Weighting: ", 1, 5)));
                break;
            case 4:
            default: //cancel changing details
                System.out.println("Cancelling detail edit.");
                break;
        }
    }

    //function to list job descriptions and get the ID of the one the user wants to action (e.g. view, delete)
    public int doesJobDescriptionExist(String action) {
        System.out.println("The current job descriptions are:");
        int counter = 1;
        for (int id : jobDescriptionMap.keySet()) {
            System.out.println(jobDescriptionMap.get(id).shortJobDescription());
            counter = id + 1;
        }
        System.out.printf("%4d. CANCEL\n", counter);
        int toAction = GetUserInput.getInt("Which job do you want to " + action + "?: ");
        while (!jobDescriptionMap.containsKey(toAction) && toAction != counter) {
            System.out.println("Apologies, that job description doesn't exist.");
            toAction = GetUserInput.getInt("Which job do you want to " + action + "?: ");
        }
        if (toAction == counter) {
            return 0;
        }
        return toAction;
    }

    //function for overall actions to be taken during Delete Job Description main menu option
    public void deleteJobDescription() {
        if (jobDescriptionMap.isEmpty()) {
            System.out.println("No jobs are currently in the system.");
            return;
        }
        int toDelete = doesJobDescriptionExist("delete");
        if (toDelete == 0) {
            System.out.println("Cancelling job deletion.");
            return;
        }
        System.out.println(jobDescriptionMap.get(toDelete).toString());
        if (!GetUserInput.getUserYesOrNo("Are you sure you want to delete the above job?")) {
            System.out.println("No job descriptions deleted.");
            return;
        }
        rankingManagement.deleteJobDescriptionIDFromRanking(toDelete);
        jobDescriptionMap.remove(toDelete);
        System.out.println("Job " + toDelete + " deleted.");
    }

    //function to save hashmap of jobs
    public void saveJobs(String fName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fName));
            oos.writeObject(jobDescriptionMap);
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //function for actions to take before exiting the program
    public void close() {
        saveJobs(jobDescriptionFile);
    }

    //getter function
    public HashMap<Integer, JobDescription> getJobDescriptionMap() {
        return jobDescriptionMap;
    }
}
