package ranking;

import applicantclasses.Applicant;
import ioprocessing.AIRequests;
import job.DetailType;
import job.JobDescription;
import job.JobDetail;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class ApplicantJobScore implements Serializable {
    @Serial
    private static final long serialVersionUID = -8045777493922882980L;
    private int applicantID;
    private HashMap<DetailType, ArrayList<Boolean>> jobSections;

    //default constructor
    public ApplicantJobScore() {}

    //constructor when creating a new AJS
    public ApplicantJobScore(int applicantID) {
        this.applicantID = applicantID;
    }

    //function to fill out all the true/false for an applicant for each detail type section from the string returned from Gemini
    public HashMap<DetailType, ArrayList<Boolean>> fillOutAJSSections(String booleanReturn, JobDescription job) {
        HashMap<DetailType, ArrayList<Boolean>> jobSections = new HashMap<>();
        String[] sections = booleanReturn.split("\\^");
        if (sections.length == 3) {
            String education = sections[0].substring(sections[0].indexOf(':') + 1);
            jobSections.put(DetailType.EDUCATION, fillOutJobSectionArrayList(education, job.getSectionLength(DetailType.EDUCATION)));
            String experience = sections[1].substring(sections[1].indexOf(':') + 1);
            jobSections.put(DetailType.EXPERIENCE, fillOutJobSectionArrayList(experience, job.getSectionLength(DetailType.EXPERIENCE)));
            String skills = sections[2].substring(sections[2].indexOf(':') + 1);
            jobSections.put(DetailType.SKILLS, fillOutJobSectionArrayList(skills, job.getSectionLength(DetailType.SKILLS)));
            return jobSections;
        } else {
            System.out.println("Error with returned response. Expected parts: 3. Actual parts: " + sections.length);
            System.out.println(booleanReturn);
            return null;
        }
    }

    //function to fill out true/false for a specific section
    public ArrayList<Boolean> fillOutJobSectionArrayList(String section, int sectionLength) {
        String[] sentences = section.split("\n");
        String[] sectionBooleans = sentences[0].split(",");
        ArrayList<Boolean> sectionBoolList = new ArrayList<>();
        for (int i = 0; i < sectionLength; i++) {
            boolean sectionDetailBool = false;
            if (i < sectionBooleans.length) {
                sectionDetailBool = Boolean.parseBoolean(sectionBooleans[i]);
            }
            sectionBoolList.add(sectionDetailBool);
        }
        return sectionBoolList;
    }

    //function to return an applicants score for a section
    public int getSectionScore(DetailType type, ArrayList<Boolean> details, JobDescription jd) {
        int score = 0;
        int counter = 0;
        for (boolean i : details) {
            if (i) {
                HashMap<DetailType, ArrayList<JobDetail>> allSections = jd.getJobSections();
                ArrayList<JobDetail> section = allSections.get(type);
                JobDetail detail = section.get(counter);
                score += detail.getDetailWeighting();
            }
            counter++;
        }
        return score;
    }

    //function to return the detail that an applicant matches that has the biggest weighting
    public String getBestMatch(JobDescription jd) {
        int score = 0;
        String keyword = "No requirements matched";
        for (DetailType type : DetailType.values()) {
            int counter = 0;
            ArrayList<Boolean> details = jobSections.get(type);
            for (boolean i : details) {
                if (!i) {
                    counter++;
                    continue;
                }
                HashMap<DetailType, ArrayList<JobDetail>> allSections = jd.getJobSections();
                ArrayList<JobDetail> section = allSections.get(type);
                JobDetail detail = section.get(counter);
                if (detail.getDetailWeighting() >= score) {
                    score = detail.getDetailWeighting();
                    keyword = detail.getDetail();
                }
                counter++;
            }
        }
        return keyword;
    }

    //function to return all the details an applicant matched (for view applicant details after a ranking)
    public String getApplicantJobScoreDetails(JobDescription jd, Applicant a, HashMap<DetailType, ArrayList<Boolean>> ajsSections) {
        StringBuilder s = new StringBuilder();
        s.append(a.applicantContactDetails());
        for (DetailType type : DetailType.values()) {
            int counter = 0;
            int sectionMatches = 0;
            ArrayList<Boolean> details = ajsSections.get(type);
            s.append(type).append(": ");
            HashMap<DetailType, ArrayList<JobDetail>> allSections = jd.getJobSections();
            ArrayList<JobDetail> section = allSections.get(type);
            for (boolean i : details) {
                if (!i) {
                    counter++;
                    continue;
                }
                JobDetail detail = section.get(counter);
                s.append(detail.getDetail()).append(", ");
                sectionMatches++;
                counter++;
            }
            if (sectionMatches == 0 && !section.isEmpty()) {
                s.append("NO MATCHES");
            } else if (section.isEmpty()) {
                s.append("SECTION EMPTY");
            } else { //delete hanging space and comma
                s.deleteCharAt(s.length() - 1);
                s.deleteCharAt(s.length() - 1);
            }
            s.append("\n");
        }
        return s.toString();
    }

    //function to get an applicants total score
    public int getApplicantTotalScore(JobDescription jd) {
        int totalScore = 0;
        for (DetailType i : DetailType.values()) {
            totalScore += getSectionScore(i, jobSections.get(i), jd);
        }
        return totalScore;
    }

    //getter and setter functions

    public int getApplicantID() {
        return applicantID;
    }

    public HashMap<DetailType, ArrayList<Boolean>> getJobSections() {
        return jobSections;
    }

    public void setJobSections(HashMap<DetailType, ArrayList<Boolean>> jobSections) {
        this.jobSections = jobSections;
    }
}
