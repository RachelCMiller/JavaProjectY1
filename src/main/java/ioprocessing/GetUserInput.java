package ioprocessing;

import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Character;

public class GetUserInput {

    //function to get an integer from the user (no restrictions on what the int can be)
    public static int getInt(String message) {
        Scanner sc = new Scanner(System.in);
        int num = 0;
        String input;
        boolean isInt = false;
        while (!isInt) {
            try {
                System.out.println(message);
                input = sc.nextLine();
                num = Integer.parseInt(input);
                isInt = true;
            } catch (Exception e) {
                System.out.println("Please only enter an integer.");
            }
        }
        return num;
    }

    //function to get an integer from the user between 2 values
    public static int getInt(String message, int min, int max) {
        boolean inRange = false;
        int num = 0;
        while (!inRange) {
            num = getInt(message);
            if (num >= min && num <= max) {
                inRange = true;
            } else {
                System.out.println("Please only enter an integer between " + min + " and " + max);
            }
        }
        return num;
    }

    //function to get a string (can be empty) from the user
    public static String getString(String message) {
        Scanner sc = new Scanner(System.in);
        System.out.println(message);
        return sc.nextLine();
    }

    //function to get a string (can not be empty) from the user
    public static String getString(String message, String errorMessage) {
        String userText;
        do {
            userText = getString(message);
            if (userText.isEmpty()) {
                System.out.println(errorMessage);
            }
        } while (userText.isEmpty());
        return userText;
    }

    //function to get a string from the user that's less than a certain number of words and doesn't contain any commas
    public static String getString(String message, int maxWords) {
        String word = getString(message, "Please don't leave this field empty.");
        boolean comma = word.contains(",");
        //ref W3Schools (no date) 'Count Number of Words in a String'. Available at: https://www.w3schools.com/java/java_howto_count_words.asp (Accessed: 24 March 2025)
        int sentenceWords = word.split("\\s").length;
        while (comma || sentenceWords > maxWords) {
            if (comma && sentenceWords > maxWords) {
                System.out.println("Please don't include any commas and keep your text to 10 words or fewer.");
            } else if (sentenceWords > maxWords) {
                System.out.println("Please keep your text to 10 characters or fewer.");
            } else {
                System.out.println("Please don't include any commas in your text.");
            }
            word = getString("Re-enter text:", "Please don't leave this field empty.");
            comma = word.contains(",");
            sentenceWords = word.split("\\s").length;
        }
        return word;
    }

    //function to get a single character from the user
    public static char getSingleLetter(String message) {
        Scanner sc = new Scanner(System.in);
        System.out.println(message);
        String input = sc.nextLine();
        char letter;
        while (true) {
            if (input.length() == 1) {
                letter = input.charAt(0);
                break;
            } else if (input.isEmpty()) {
                System.out.println("Please enter your response.");
            } else {
                System.out.println("Please only enter one character");
            }
            input = sc.nextLine();
        }
        return letter;
    }

    //function to get a single character from the user out of a number of possible options
    public static char getSingleLetter(String message, char ...options) {
        char input = getSingleLetter(message);
        boolean correctInput = false;
        while (true) {
            StringBuilder allOptions = new StringBuilder();
            for (char option : options) {
                if (Character.toUpperCase(input) == Character.toUpperCase(option)) {
                    correctInput = true;
                } else {
                    allOptions.append(option).append(", ");
                }
            }
            if (correctInput) {
                break;
            }
            allOptions.deleteCharAt(allOptions.length() - 1).deleteCharAt(allOptions.length() - 1);
            System.out.println("Please only enter either one of: " + allOptions);
            input = getSingleLetter(message);
        }
        return Character.toUpperCase(input);
    }

    //function to get Y or N (yes or no) from the user
    public static boolean getUserYesOrNo(String message) {
        char letter = getSingleLetter(message + " Y or N:", 'Y', 'N');
        return letter == 'Y';
    }

    //function to print out initial message, menu options and get which the user wants to do
    //ref multiple options using ... in parameters from GeeksForGeeks (2025) 'Variable Arguments (Varargs) in Java'. Available at: https://www.geeksforgeeks.org/variable-arguments-varargs-in-java/ (Accessed: 23 March 2025)
    public static int runMenu(String message, String ...option) {
        System.out.println(message);
        int optionNo = 0;
        for (String i : option) {
            ++optionNo;
            System.out.printf("%4d. " + i + "\n", optionNo);
        }
        return GetUserInput.getInt("What would you like to do?:", 1, optionNo);
    }

    //interface to allow having a function as an argument for another function (createJobObject or createApplicantObject)
    //ref Teo, W (2024) 'How to Pass Method as Parameter in Java'. Available at: https://www.baeldung.com/java-passing-method-parameter (Accessed: 23 April 2025)
    //ref OpenAI ChatGPT (2025) ChatGPT responses to Rachel Miller asking for examples of passing a method as an argument to another method, 23 April. Available at: https://chatgpt.com/share/6808f897-09e4-8002-9554-7344d734ff15
    @FunctionalInterface
    public interface CreateObject {
        boolean createObject(int newID, String file);
    }

    //function to print out the files that haven't been uploaded yet and create objects from the files chosen
    public static boolean printAndChooseFile(String subDir, String csvFile, int currentID, CreateObject createObject, boolean canFail) {
        //get the file names in the folder, exclude those already uploaded
        String[] files = FileProcessor.getFiles(subDir, csvFile);
        int newID = currentID;
        ArrayList<Integer> filesAvailable = new ArrayList<>();
        //print file options
        for (String file : files) {
            System.out.printf("%4d. " + file + "\n", newID);
            filesAvailable.add(newID);
            newID++;
        }
        //if all files have been uploaded already exit the function
        if (newID == currentID) {
            System.out.println(FileProcessor.correctPath(subDir));
            return false;
        }
        //print all and cancel options
        int allFiles = newID;
        System.out.printf("%4d. ALL FILES\n", allFiles);
        newID++;
        int cancelFiles = newID;
        System.out.printf("%4d. CANCEL UPLOAD\n", cancelFiles);
        //get user file choices
        ArrayList<Integer> filesSelected = getUserArrayListInt("Which file(s) do you want to upload? Enter the file numbers", newID, allFiles, cancelFiles, allFiles, filesAvailable);
        if (filesSelected == null) {
            System.out.println("File upload cancelled.");
            return true;
        }
        int startID = currentID;
        int successfulExtractions = 0;
        for (int file : filesSelected) {
            if (createObject.createObject(currentID, files[file - startID])) {
                successfulExtractions++;
            }
            currentID++;
        }
        if (canFail) {
            System.out.println("Files successfully uploaded: " + successfulExtractions);
        } else {
            System.out.println("Files uploaded: " + filesSelected.size() + "\nFiles with data successfully extracted: " + successfulExtractions);
        }
        return true;
    }

    //function to let the user review the final screen before printing some blank blanks and returning to the main menu options
    public static void pauseProgram() {
        GetUserInput.getString("Press Enter to return to the main menu.");
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }

    //function to let the user review the final screen before returning to the current sub-menu options
    public static void pauseProgram(String option) {
        GetUserInput.getString("Press Enter to return to the " + option + " menu.");
    }

    //function to return an ArrayList of integers that the user wants to process
    public static ArrayList<Integer> getUserArrayListInt(String question, int counter, int allCounter, int cancelCounter, int uploadNewCounter, ArrayList<Integer> allOptions) {
        ArrayList<Integer> toReturn = new ArrayList<>();
        while (true) {
            int singleChoice = GetUserInput.getInt(question + " individually and 0 when done.", 0, counter);
            if (singleChoice == 0) {
                break;
            } else if (!allOptions.isEmpty() && singleChoice == allCounter) {
                //ref W3Schools (no date) 'Java ArrayList clone() Method'. Available at: https://www.w3schools.com/java/ref_arraylist_clone.asp (Accessed: 15 April 2025)
                toReturn = (java.util.ArrayList<Integer>)allOptions.clone();
                break;
            } else if (singleChoice == cancelCounter) {
                System.out.println("Cancelling.");
                //ref OpenAI ChatGPT (2025) ChatGPT response to Rachel Miller asking how to return from a function without creating an object, 15 April https://chatgpt.com/share/67fe8645-1ce0-8002-a9f1-c398c0aa1325
                return null;
            } else if (allOptions.contains(singleChoice)) {
                toReturn.add(singleChoice);
            } else if (singleChoice == uploadNewCounter) { //only for scoring applicants against jobs
                toReturn.add(0);
                return toReturn;
            } else {
                System.out.println(singleChoice + " isn't an available option.");
            }
            if (toReturn.size() == allOptions.size()) {
                return toReturn;
            }
        }
        return toReturn;
    }

}
