package demo.pnw.moths.app.ui;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.io.BufferedReader;
/**
 * Represents the data structure for ID keys and handles parsing and organization of attributes.
 */
public class IdData {
    ArrayList<HashSet<Integer>> attributes = new ArrayList<HashSet<Integer>>();
    ArrayList<String> attributeNames = new ArrayList<String>();

    ArrayList<String> mothNames = new ArrayList<String>();
    ArrayList<ArrayList<String>> attributeNamesSplit = new ArrayList<ArrayList<String>>();

    // Set up "Categories" to select different attributes in each category
    // then compute moths based on various categories
    HashMap<String, HashSet<Integer>> categories=new HashMap<String, HashSet<Integer>>();
     String categoryName="";
    String categoryNo="-1";
    ArrayList<Integer> activeCategories =new ArrayList<Integer>();


    // For computing the results
    ArrayList<Integer> resultIndices = new ArrayList<Integer>();
    HashSet<Integer> everyMothPossible=new HashSet<>();

    Attribute root = new Attribute("0","root attribute", 0,true,"-1");
    private File csvKey;

    /**
     * Constructs an IdData object with the provided CSV file.
     * @param csvFile The CSV file containing the ID key data.
     */
    public IdData(File csvFile){
        this.csvKey=csvFile;
    }

    /**
     * Reads the CSV file and builds a tree structure based on the attribute hierarchy.
     * @throws Exception if there's an error reading the file.
     */
    public void setUpTree() throws Exception {

        generateAttributeSets(attributes, attributeNames);
        for (int i=0; i < attributeNames.size(); i++) {
            String[] data= attributeNames.get(i).split(":");
            List<String> l = Arrays.<String>asList(data);
            ArrayList<String> temp = new ArrayList<String>(l);
            attributeNamesSplit.add(temp);
        }

        for (int i=0; i < attributeNamesSplit.size(); i++) {
            int catInd=attributeNamesSplit.get(i).size()-2;
            if(!categoryName.equals(attributeNamesSplit.get(i).get(catInd))) {
                categoryName=attributeNamesSplit.get(i).get(catInd);
                categoryNo=Integer.toString(Integer.parseInt(categoryNo) + 1);
                categories.put(categoryNo,new HashSet<Integer>());
            }
            makeTreeFromList(attributeNamesSplit.get(i), root, Integer.toString(i));
        }
    }

    /**
     * Generates sets of moth indices for each attribute from the CSV file.
     * @param attrs The list to store attribute sets.
     * @param attrNames The list to store attribute names.
     * @throws Exception if there's an error reading the file.
     */
    public void generateAttributeSets(ArrayList<HashSet<Integer>> attrs,ArrayList<String> attrNames) throws Exception {
        // Scan csv
        // For each moth in row corresponding to attr put each moth into the attr set if value for attr 1 else 0
        BufferedReader csvReader = new BufferedReader(new InputStreamReader(new FileInputStream(csvKey)));
        String row;

        // Read first line (it's the names of the moths)
        row = csvReader.readLine();
        String[] data = row.split(",");

        for(int i = 1; i < data.length; i++) {
            mothNames.add(data[i]);
            everyMothPossible.add(i);
        }

        while ((row = csvReader.readLine()) != null) {
            HashSet<Integer> tempAttr = new HashSet<Integer>();
            data = row.split(",");

            // Put attribute Name into attrNames arrayList
            attrNames.add(data[0]);

            // Input data into the sets for each attribute in the arrayList of all attributes
            for(int i=1; i < data.length; i++){
                if(data[i].equals("1")) {
                    // Add the moth id (i) to the hashset within attrs arrayList
                    tempAttr.add(i+1);
                }
            }
            attrs.add(tempAttr);
        }
        csvReader.close();
    }

    /**
     * Organizes attributes into a hierarchical tree structure.
     * @param attr The attribute parts to be organized.
     * @param root The root attribute to attach the hierarchy to.
     * @param number The index number of the attribute.
     */
    public void makeTreeFromList(ArrayList<String> attr, Attribute root, String number) {
        Attribute node = root;
        int childIndex = 0;
        int size=attr.size();

        for(int i=0; i < size; i++) {
            childIndex=getChildIndex(node.children,attr.get(i));
            if(childIndex == -1) {
                if(i == size-1) {
                    node.addChild(new Attribute(number,attr.get(i), i+1,false,categoryNo));
                    categories.get(categoryNo).add(Integer.parseInt(number));
                }
                else {
                    node.addChild(new Attribute(number,attr.get(i), i+1,true,categoryNo));
                }
                node=node.children.get(node.children.size()-1);
            }
            else {
                node=node.children.get(childIndex);
            }
        }
    }

    /**
     * Finds the index of a child attribute that matches the given attribute name.
     * @param children The list of child attributes.
     * @param next The attribute name to match.
     * @return The index of the matching child, or -1 if not found.
     */
    public int getChildIndex(ArrayList<Attribute> children, String next) {
        for(int i=0; i < children.size(); i++){
            // Fnd the index of which child matches the next attribute in the array
            if(children.get(i).name.equals(next)){
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves a list of top-level buttons based on the first level of attributes.
     * @param level The tree level to retrieve buttons for.
     * @return A list of attributes at the specified level.
     */
    public ArrayList<Attribute> getButtons(int level) {
        // Currently just returns arraylist of first level's buttons
        ArrayList<Attribute> temp=new ArrayList<Attribute>();
        Attribute node=this.root;

        for(int i=0; i < node.children.size(); i++) {
            temp.add(node.children.get(i));
        }
        return temp;
    }

    /**
     * Searches for moths that match all selected attributes.
     * @param searchNode The attribute to add to the search.
     * @return A set of moth indices that match the search criteria.
     */
    public HashSet<Integer> searchResults (Attribute searchNode){
        // Edge cases for removal: none left?
        //Log.e("searchAdd",searchNode.number+" was added" );
        if(resultIndices.size()==0){
            activeCategories.clear();
        }
        if(!activeCategories.contains(Integer.parseInt(searchNode.parentNo))){
            activeCategories.add(Integer.parseInt(searchNode.parentNo));
        }
        //Log.e("searchAdd",Integer.toString(activeCategories.size() ));
        resultIndices.add(Integer.parseInt(searchNode.number));
        //Log.e("searchAdd","resultInd" +resultIndices.toString() );
        return search();
    }

    /**
     * Removes an attribute from the search criteria and updates the results.
     * @param searchNode The attribute to remove from the search.
     * @return A set of moth indices that match the updated search criteria.
     */
    public HashSet<Integer> removeResults (Attribute searchNode){

        //Log.e("searchRemove",searchNode.number+" was removed" );
        HashSet<Integer> temp=new HashSet<Integer>();
        if(resultIndices.size()==0){
            return temp;
        }
        resultIndices.remove((Integer)Integer.parseInt(searchNode.number));

        HashSet<Integer> attrsInCategory;
        for(int i = 0; i< activeCategories.size(); i++){
            attrsInCategory=new HashSet<>(categories.get(Integer.toString(activeCategories.get(i))));
            attrsInCategory.retainAll(resultIndices);
            if(attrsInCategory.isEmpty()){
                activeCategories.remove(i);
            }
        }
        if(resultIndices.size()==0){
            return temp;
        }
        return search();
    }

    /**
     * Performs the search operation based on the currently active categories and attributes.
     * @return A set of moth indices that match all active search criteria.
     */
    public HashSet<Integer> search(){
        HashSet<Integer> temp= new HashSet<>(everyMothPossible);
        HashSet<Integer> attrsInCategory;
        HashSet<Integer> mothsInCategory;
        for(int i = 0; i < activeCategories.size(); i++) {
            attrsInCategory=new HashSet<>(categories.get(Integer.toString(activeCategories.get(i))));
            //Log.e("searchAdd","attrInCat"+attrsInCategory.toString() );
            attrsInCategory.retainAll(resultIndices);
            mothsInCategory=new HashSet<Integer>();
            for(Integer a : attrsInCategory){
                mothsInCategory.addAll(attributes.get(a));
            }
            temp.retainAll(mothsInCategory);
        }
        return temp;
    }

    /**
     * Retrieves the name of a moth by its index.
     * @param number The index of the moth.
     * @return The name of the moth.
     */
    public String getName (int number){
        return mothNames.get(number-2);
    }
}

/**
 * Represents an attribute in the ID key hierarchy.
 */
class Attribute {

    String number;
    String name;
    int layer;
    boolean parent;
    boolean selected;
    String parentNo;
    ArrayList<Attribute> children;


    Attribute(String number, String name, int layer, boolean parent,String parentNo) {
        this.number=number;
        this.name=name;
        this.layer=layer;
        this.parent=parent;
        this.selected=false;
        this.parentNo=parentNo;
        children= new ArrayList<Attribute>();
    }
    /**
     * Adds a child attribute to this attribute.
     * @param attr The child attribute to add.
     */
    public void addChild(Attribute attr) {
        children.add(attr);
    }
}
