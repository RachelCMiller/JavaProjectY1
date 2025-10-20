package applicantclasses;

import java.io.Serial;
import java.io.Serializable;

public class Applicant implements Serializable {
    @Serial
    private static final long serialVersionUID = 6816839542658243745L;
    private final int applicantID;
    private final String cvText;
    private final String fName;
    private String name;
    private String email;
    private String linkedIn;
    private String address;
    private String phoneNumber;
    private String cvDetail;
    private boolean extractionError;

    //initial constructor
    public Applicant(int applicantID, String cvText, String fName) {
        this.applicantID = applicantID;
        this.cvText = cvText;
        this.fName = fName;
        this.extractionError = true;
        this.name = "n/a";
    }

    //to print ID, file name and name (when choosing applicants)
    public String applicantShortDetails() {
        return String.format("%4d. %-30s %-30s", this.applicantID, this.fName, this.name);
    }

    //to print applicant contact details
    public String applicantContactDetails() {
        StringBuilder s = new StringBuilder();
        s.append("APPLICANT ID: ").append(this.applicantID).append("\n");
        s.append("NAME: ").append(this.name).append("\n");
        s.append("LINKEDIN: ").append(this.linkedIn).append("\n");
        s.append("EMAIL: ").append(this.email).append("\n");
        s.append("PHONE NUMBER: ").append(this.phoneNumber).append("\n");
        s.append("ADDRESS: ").append(this.address).append("\n");
        return s.toString();
    }

    //below are various getters and setters for different fields

    public int getApplicantID() {
        return this.applicantID;
    }

    public String getCvText() {
        return this.cvText;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    public String getCvDetail() {
        return this.cvDetail;
    }

    public void setCvDetail(String cvDetail) {
        this.cvDetail = cvDetail;
    }

    public boolean getExtractionError() {
        return this.extractionError;
    }

    public void setExtractionError(boolean extractionError) {
        this.extractionError = extractionError;
    }
}
