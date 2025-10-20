<p style="font-weight:600; font-size:36px">Read Me</p>

**Table of Contents**
<!-- TOC -->
* [System info:](#system-info)
* [Using the Program](#using-the-program)
  * [Running the Program](#running-the-program)
  * [Adding a Job Description](#adding-a-job-description)
    * [Manual input](#manual-input)
    * [File upload](#file-upload)
  * [Deleting Job Descriptions](#deleting-job-descriptions)
  * [Uploading CVs](#uploading-cvs)
  * [Manage Applicants](#manage-applicants)
    * [Retry detail extraction](#retry-detail-extraction)
    * [Deleting Applicants](#deleting-applicants)
  * [Viewing a Job Ranking](#viewing-a-job-ranking)
    * [View Applicant Details](#view-applicant-details)
    * [Score Another Applicant](#score-another-applicant)
    * [Filter and Sort Differently](#filter-and-sort-differently)
  * [Exit](#exit)
* [Resources](#resources)
<!-- TOC -->

# System info:
OS Name	- Microsoft Windows 11 Home

Version	- 10.0.26100 Build 26100

System Type - x64-based PC

Processor - Intel(R) Core(TM) Ultra 7 155H, 1400 Mhz, 16 Core(s), 22 Logical Processor(s)

Installed Physical Memory (RAM)	- 32.0 GB

Need JDK 21/23 installed

# Using the Program

**Note: data is only saved when the program exits through the main menu exit option.**

The main actions that can be taken within the program are:
- Adding job descriptions
- Deleting job descriptions
- Uploading applicant CVs
- Managing applicants (retrying their detail extraction, deleting an applicant)
- Viewing applicants ranking for a job (then: viewing an applicants details, scoring more applicants, applying different filtering/sorting options)
- Exit the program

Selecting an action to take is done through entering an assigned number.

There is already data saved in the program. To quickly delete the data to start from scratch the top-level files "applicantSaveFile.dat", "arSaveFile.dat", "jobSaveFile.dat", "uploadedCVs.csv", and "uploadedJobs.csv".

For uploading files, the files need to be placed in the correct top-level directory (either "Jobs" or "CVs") for the program to find them. Files need to be .txt, .pdf, .doc, or .docx formats. You can select the option to upload all files in the folder at once, or select specific files to upload individually then entering 0 when you're done.

As extraction of details from CVs and job description files, and scoring applicants against a job is done with a free API request, if too many requests are placed a rate-limit will start applying. A full professional solution would look to pay for more requests/look at other APIs that could be used instead.

The API key and model being used is a static final variable (ioprocessing > AIRequests > apikey). This can be replaced with a new API key created at https://openrouter.ai/settings/keys (after making an account).

The current model is google/gemini-2.0-flash-exp:free. Most of the initial testing was done with deepseek/deepseek-v3-base:free but Gemini seemed more reliable.

## Running the Program

For running through the command line, make sure Maven is installed and that it's set up in the system path as needed. Then use the commands:

```mvn dependency:resolve```

```mvn compile```

```mvn package```

```set OPENROUTERAPIKEY=XX-XX-XX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX``` on Windows or ```export OPENROUTERAPIKEY="XX-XX-XX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"``` on other OS

```java -jar target\SmartCVAnalyserTake4-1.0-SNAPSHOT.jar``` on Windows or ```java -jar target/SmartCVAnalyserTake4-1.0-SNAPSHOT.jar``` on other OS.

You may want to hit tab after typing Smart so it autocompletes the correct name.

## Adding a Job Description
Job Descriptions can be added through manual textual input or by file upload.

### Manual input
- To abandon creating a new job, type "cancel" when asked to input the job title or a keyword (for education, experience, or skills)
- The job ID is automatically generated
- Title is required
- Keywords can be up to 20 words
- Weightings must be an integer between 1 and 5
- Details (keywords and weighting) can be edited or deleted before saving, extras can be added

### File upload
- Select the files you want to upload from those available
- The text is read from the file using Apache libraries
- The text is then passed to Gemini for categorising into title, requirements and weightings
- You are given the option to edit these sections if you would like to

## Deleting Job Descriptions
Only one job can be deleted at a time to avoid accidental deletion of multiple/all jobs. When a job is deleted the scores for that job are also deleted.

## Uploading CVs
- Select the files you want to upload from those available
- The text from the CV is passed to Gemini for detail extraction
- If the details aren't extracted successfully you can try again later from Manage Applicants

## Manage Applicants
Applicant detail extraction can be re-attempted from here, or applicants can be deleted.

### Retry detail extraction
You can select specific or all applicants to retry detail extraction. Detail extraction will also be done before scoring an applicant against a job if their details weren't extracted initially.

### Deleting Applicants
You can only select one applicant to delete at a time to avoid accidental deletion of multiple/all applicants. When an applicant is deleted their scores for jobs will also be deleted.

## Viewing a Job Ranking
- You can only view one job ranking at a time (chosen with job ID)
- Select the number of the filter you want to use (no filter/view all applicants scored is the last option)
- If more than one applicant is left after the filter you're prompted to choose a sorting method (total, education, experience, or skills score)
- If more than one applicant is left after the filter you will also be prompted to choose how many applicants to display
- The table of applicant scores will be displayed with the highest matching requirement displayed for each applicant

### View Applicant Details
Select the number of an applicant from the ranking returned to view the applicant's contact information and what requirements they matched.

### Score Another Applicant
- If there are files available in the CV folder that haven't been uploaded as applicants then "UPLOAD NEW APPLICANT" will also be given as an option, otherwise you can only select from the currently uploaded applicants.
- Select the applicant number(s) you want to score
- You're then prompted to choose filtering and sorting options again

### Filter and Sort Differently
There are also options to re-filter and re-sort the ranking list.

## Exit
This will save new data added (changes to applicant information, job descriptions and applicant job scores) and close the program.

# Resources
Apache libraries have been used for parsing different document types.

The Gemini API is used for extracting information and analysis.

Job descriptions used for testing are a mix of made up, amalgamations of listings from recruitment sites and a couple are directly from Google's jobs.

The CVs used for testing were found online, the majority from https://www.beamjobs.com/resumes/software-engineer-resume-examples 