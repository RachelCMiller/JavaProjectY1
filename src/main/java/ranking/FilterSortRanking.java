package ranking;

import java.util.ArrayList;

public class FilterSortRanking {
    //this class is to allow 3 values to be returned from a function
    private ArrayList<ApplicantJobScore> requirementFilteredApplicants;
    private int sortingChoice;
    private int limitNumberApplicants;

    //constructor with default values for sorting and limiting results
    public FilterSortRanking() {
        this.sortingChoice = 1;
        this.limitNumberApplicants = 1;
    }

    //getter and setter functions

    public ArrayList<ApplicantJobScore> getRequirementFilteredApplicants() {
        return requirementFilteredApplicants;
    }

    public void setRequirementFilteredApplicants(ArrayList<ApplicantJobScore> requirementFilteredApplicants) {
        this.requirementFilteredApplicants = requirementFilteredApplicants;
    }

    public int getSortingChoice() {
        return sortingChoice;
    }

    public void setSortingChoice(int sortingChoice) {
        this.sortingChoice = sortingChoice;
    }

    public int getLimitNumberApplicants() {
        return limitNumberApplicants;
    }

    public void setLimitNumberApplicants(int limitNumberApplicants) {
        this.limitNumberApplicants = limitNumberApplicants;
    }
}
