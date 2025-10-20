package job;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class JobDescription implements Serializable {
    @Serial
    private static final long serialVersionUID = -6672202997952000802L;
    private String title;
    private int iD;
    private HashMap<DetailType, ArrayList<JobDetail>> jobSections;

    //default constructor
    public JobDescription() {}

    //constructor for creating a job description
    public JobDescription(int newJobID, String title, HashMap<DetailType, ArrayList<JobDetail>> jobSections) {
        this.title = title;
        this.iD = newJobID;
        this.jobSections = jobSections;
    }

    //function to return a string of the job ID and title (for menu selections)
    public String shortJobDescription() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("%4d. ", iD));
        s.append(title);
        return s.toString();
    }

    //function to return string of all DetailType keywords and weightings that will be added to the toString function below
    public String sectionsString(HashMap<DetailType, ArrayList<JobDetail>> sections) {
        StringBuilder details = new StringBuilder();
        //ref notnoop (2009) 'looping over enums in a for each loop with .values' [Stack Overflow] 9 July. Available at: https://stackoverflow.com/questions/1104975/a-for-loop-to-iterate-over-an-enum-in-java (Accessed: 19 March 2025)
        for (DetailType i : DetailType.values()) {
            details.append(i).append("\n");
            //ref OpenAI ChatGPT (2025) ChatGPT response to Rachel Miller asking about looping over arraylists within hashmaps in java, 24 March. Available at: https://chatgpt.com/share/67e1747d-4be4-8002-922c-c281b20f7add
            ArrayList<JobDetail> detailList = sections.get(i);
            for (int j = 0; j < detailList.size(); j++) {
                details.append(detailList.get(j).toString());
                if (j < detailList.size() - 1) {
                    details.append("\n");
                }
            }
            details.append("\n");
        }
        return details.toString();
    }

    //function to return string of all DetailType keywords and weightings that will be added to the toString function below
    public String detailsString(HashMap<DetailType, ArrayList<JobDetail>> sections) {
        StringBuilder details = new StringBuilder();
        for (DetailType i : DetailType.values()) {
            details.append(i).append(":");
            ArrayList<JobDetail> detailList = sections.get(i);
            for (int j = 0; j < detailList.size(); j++) {
                details.append(detailList.get(j).getDetail());
                if (j < detailList.size() - 1) {
                    details.append(",");
                }
            }
        }
        return details.toString();
    }

    //function to override the default toString for JobDescription when printing
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(100);
        s.append("JOB TITLE: ").append(title);
        s.append("\nID: ").append(iD);
        s.append("\n");
        s.append(sectionsString(jobSections));
        return s.toString();
    }

    //getter and setter functions below

    public String getJobTitle() {
        return title;
    }

    public void setJobTitle(String title) {
        this.title = title;
    }

    public void setJobID(int iD) {
        this.iD = iD;
    }

    public HashMap<DetailType, ArrayList<JobDetail>> getJobSections() {
        return jobSections;
    }

    public void setJobSections(HashMap<DetailType, ArrayList<JobDetail>> jobSections) {
        this.jobSections = jobSections;
    }

    public int getSectionLength(DetailType dType) {
        return jobSections.get(dType).size();
    }
}
