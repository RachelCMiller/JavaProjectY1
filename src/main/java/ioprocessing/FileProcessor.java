package ioprocessing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import java.io.*;
import java.util.*;

public class FileProcessor {

    //function to get the full path name for the file to be uploaded
    public static String getPathName(String folder, String fName) {
        String os = System.getProperty("os.name");
        String pathName;
        if (os.toLowerCase().contains("win")) {
            pathName = ".\\" + folder + "\\";
        } else {
            pathName = "./" + folder + "/";
        }
        return pathName.concat(fName);
    }

    //function to return file names of types that can be handled
    public static String[] getFiles(String subDir, String alreadyUploadedFiles) {
        //ref Krishna, M (2023) 'The List(FilenameFilter filter) method'. Available at: https://www.tutorialspoint.com/how-to-get-list-of-all-files-folders-from-a-folder-in-java (Accessed: 29 March 2025)
        //Creating a File object for directory
        String os = System.getProperty("os.name");
        File directoryPath;
        String dirName;
        //ref Baeldung (2024) 'How to Detect the OS Using Java'. Available at: https://www.baeldung.com/java-detect-os (Accessed: 29 March 2025)
        if (os.toLowerCase().contains("win")) {
            dirName = ".\\";
        } else {
            dirName = "./";
        }
        directoryPath = new File(dirName.concat(subDir));
        File f = new File(alreadyUploadedFiles);
        String[] individualUploadedFiles = new String[0];
        if (f.exists()) {
            String uploadedFiles = readFile(alreadyUploadedFiles);
            individualUploadedFiles = uploadedFiles.split(",");
        } else {
            createFile(alreadyUploadedFiles);
        }
        String[] finalIndividualUploadedFiles = individualUploadedFiles;
        FilenameFilter textFilefilter = (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return (lowercaseName.endsWith(".txt") || lowercaseName.endsWith(".pdf") || lowercaseName.endsWith(".doc") || lowercaseName.endsWith(".docx")) && !alreadyUploaded(f, finalIndividualUploadedFiles, lowercaseName);
        };
        //List of all the text files
        return directoryPath.list(textFilefilter);
    }

    //function to get the path to the folder needed for different operating systems
    public static String correctPath(String folder) {
        String os = System.getProperty("os.name");
        //ref Patel, A (2011) 'Get the current working directory in Java with System.getProperty("user.dir")' [Stack Overflow] 29 September. Available at: https://stackoverflow.com/questions/4871051/how-to-get-the-current-working-directory-in-java  (Accessed: 20 March 2025)
        String filePath = System.getProperty("user.dir");
        String pathName;
        if (os.toLowerCase().contains("win")) {
            pathName = "\\" + folder;
        } else {
            pathName = "/"+ folder + "/";
        }
        filePath = filePath.concat(pathName);
        return String.format("No files available to upload. Please place files of formats .txt, .pdf, .doc, or .docx in:\n" + filePath);
    }

    //function to get the file contents depending on what the file type is
    public static String extractText(String fName, String filePath) {
        String text = "";
        try {
            if (fName.endsWith(".docx")) {
                //ref Ahmed, R (2015) 'Reading docx in Java' [Stack Overflow] 31 December. Available at: https://stackoverflow.com/questions/16682942/reading-docx-file-in-java (Accessed: 2 April 2025)
                File newFile = new File(filePath);
                FileInputStream fis = new FileInputStream(newFile.getAbsolutePath());
                XWPFWordExtractor docx = new XWPFWordExtractor(OPCPackage.open(fis));
                text = docx.getText();
                fis.close();
            } else if (fName.endsWith(".doc")) {
                //org.apache.poi:poi:5.4.0
                //ref The Apache Software Foundation (2025) 'Apache POI™ - Text Extraction'. Available at: https://poi.apache.org/text-extraction.html (Accessed: 2 April 2025)
                File newFile = new File(filePath);
                FileInputStream fis = new FileInputStream(newFile.getAbsolutePath());
                WordExtractor doc = new WordExtractor(fis);
                text = doc.getText();
                fis.close();
            } else if (fName.endsWith(".pdf")) {
                //ref Tutorials Point (no date) 'PDFBox - Loading a Document'. Available at: https://www.tutorialspoint.com/pdfbox/pdfbox_loading_a_document.htm (Accessed: 2 April 2025)
                //ref The Apache Software Foundation (no date) 'Use Loader to get a PDF document'. Available at: https://pdfbox.apache.org/3.0/migration.html (Accessed: 2 April 2025)
                File newFile = new File(filePath);
                PDDocument document = Loader.loadPDF(newFile);
                PDFTextStripper pdfStripper = new PDFTextStripper();
                text = pdfStripper.getText(document);
                document.close();
            } else {
                text = readFile(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unsupported file type.");
        }
        return text;
    }

    //function to check if a file was previously uploaded
    public static boolean alreadyUploaded(File f, String[] finalIndividualUploadedFiles, String lowercaseName) {
        if (!f.exists())
            return false;
        for (String file : finalIndividualUploadedFiles) {
            if (Objects.equals(file, lowercaseName)) {
                return true;
            }
        }
        return false;
    }

    //function to update the CSV file with what files have already been uploaded
    public static void updateUploadedCSV(String file, String fName) {
        String fileName = file.toLowerCase();
        String toAppend = fileName.concat(",");
        appendFile(fName, toAppend);
    }

    //function to try creating a new file
    public static void createFile(String fName) {
        try {
            File newFile = new File(fName);
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getName());
            } else {
                System.out.println(("File already exists."));
            }
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
    }

    //function to read text from basic text file
    public static String readFile(String fName) {
        StringBuilder data = new StringBuilder();
        try {
            File obj = new File(fName);
            Scanner fileToRead = new Scanner(obj);
            while (fileToRead.hasNextLine()) {
                data.append(fileToRead.nextLine()).append("\n");
            }
            fileToRead.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error Reading file.");
        }
        return data.toString();
    }

    //function to append text to the end of a file
    public static void appendFile(String fName, String toAppend) {
        try {
            FileWriter myWriter = new FileWriter(fName, true);
            BufferedWriter out = new BufferedWriter(myWriter);
            out.write(toAppend);
            out.close();
            myWriter.close();
            System.out.println(fName + " written successfully.");
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
    }
}
