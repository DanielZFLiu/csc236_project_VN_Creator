package Gateway;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * A Gateway class used to implement <I>LoadSave</I> for loading and saving users.
 *
 */

public class UserGate {

    String myPath;

    /**
     * The gateway's constructor that gets a relative path for the current directory.
     */

    public UserGate(){
        this.myPath = findSaveUserFile(System.getProperty("user.dir"));
    }

    private String findSaveUserFile(String filePath){
        File dir = new File(filePath);
        File[] directoryListing = dir.listFiles();
        String foundPath = "";

        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.getAbsolutePath().contains("data") && child.getAbsolutePath().contains("UserData.txt")){
                    System.out.println(child.getAbsolutePath());
                    return child.getAbsolutePath();
                }
                else{
                    String path = findSaveUserFile(child.getAbsolutePath());
                    if (!path.equals("")){
                        return path;
                    }
                }
            }
        }
        return foundPath;
    }

    /**
     * The load method reads the serialized txt file and returns a /List</Hashmap>>
     * which represents the users.
     * @return A list of Hashmaps that represents the saved users in the file.
     */

    public List<HashMap> load() {

        // Source: https://www.geeksforgeeks.org/how-to-serialize-hashmap-in-java/

        List<HashMap> myMaps = new ArrayList<>();

        try {
//            File userFile = new File(myPath);
//            userFile.createNewFile();
            FileInputStream fileInput = new FileInputStream(myPath);

            System.out.println("bitch");
            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);
            System.out.println("be;iwof");

            System.out.println("c");
            myMaps = (List<HashMap>) objectInput.readObject();
            System.out.println("d");

            objectInput.close();
            fileInput.close();
        }

        catch (IOException obj1) {
            System.out.println(myPath);
            myMaps.add(new HashMap<String, String>());
            System.out.println("Loading...\n" +
                    "No Saved Users.");
            obj1.printStackTrace();
        }

        catch (ClassNotFoundException obj2) {
            System.out.println("Class not found");
//            obj2.printStackTrace();
        }

        return myMaps;
    }

    /**
     * The save method takes a /List</Hashmap>> which represents the users
     * and saves it to a serialized txt file.
     * @param myMap The list of Hashmaps to be saved.
     */

    public void save(List<HashMap> myMap){
        // Source: https://www.geeksforgeeks.org/how-to-serialize-hashmap-in-java/
        try {
            FileOutputStream myFileOutStream
                    = new FileOutputStream(myPath);

            ObjectOutputStream myObjectOutStream
                    = new ObjectOutputStream(myFileOutStream);

            myObjectOutStream.writeObject(myMap);

            // closing FileOutputStream and
            // ObjectOutputStream
            myObjectOutStream.close();
            myFileOutStream.close();
        }
        catch (IOException e) {
            System.out.println(e);
//            e.printStackTrace();
        }
    }
}