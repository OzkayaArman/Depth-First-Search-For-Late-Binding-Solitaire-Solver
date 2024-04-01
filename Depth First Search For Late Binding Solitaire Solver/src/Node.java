import java.util.ArrayList;
import java.util.List;

public class Node {
    /**
     * Search state stores the boardlayout as an ArrayList
     * Indexes correlate to pile numbers
     * Element at index is the card at that pile represented by an integer 
     */
    private ArrayList<Integer> boardLayout = new ArrayList<>();

    /**
     * Search state stores the moves made so far as an ArrayList
     * For a given move, 2 adjacent elements are used
     * First element represents the card moved as an integer
     * Second element represents the pile number the card moved to 
     */
    private ArrayList<Integer> movesMadeSoFar = new ArrayList<>();
    private int savingGraceAllowance = 1;
    
    /**
     *Constructor for the node class
     * @param boardLayoutIn is an arraylist that represents the board layout of the current node
     */
    public Node(List<Integer> boardLayoutIn) {
        boardLayout = (ArrayList<Integer>) boardLayoutIn;
    }


    /**
     *Constructor for the node class
     * @param boardLayoutIn is an arraylist that represents the board layout of the current node
     * @param movesMadeIn is an arraylist that represents the moves made so far
     */
    public Node(List<Integer> boardLayoutIn, List<Integer>movesMadeIn) {
        boardLayout = (ArrayList<Integer>) boardLayoutIn;
        movesMadeSoFar =(ArrayList<Integer>) movesMadeIn;
    }

    /**
     * Get method to read the savingGraceAllowance 
     * @return the saving grace allowance value
     */
    public int getSavingGrace(){
        return savingGraceAllowance;
    }

    /**
     * Set method to decrement savingGraceAllowance to indace use of saving grace
     */
    public void useSavingGrace(){
        savingGraceAllowance = 0;
    }

    /**
     * Set method to set moves made
     * @param cardRankIn the card moved as an integer
     * @param destinationPileIn the pile number
     */
    public void addMovesMade(int cardRankIn, int destinationPileIn) {
        movesMadeSoFar.add(cardRankIn);
        movesMadeSoFar.add(destinationPileIn);
    }

     /**
     * Get method to get last card in the board layout moves made
     */
    public int getLastCard() {
        int topIndex = boardLayout.size() - 1;
        return boardLayout.get(topIndex);
    }

    /**
     * Get method to get a card at a specified pile number
     * @param indexIn represents the pile number
     */
    public int getCard(int indexIn) {
        return boardLayout.get(indexIn);
    }
     /**
     * Get method to get the solution at a specified index
     * @param indexIn used for access into the arrayList
     */
    public int getSolution(int indexIn) {
        return movesMadeSoFar.get(indexIn);
    }

     /**
     * Get method to get the number of pile numbers left
     * @return the number of pile numbers
     */
    public int getSize() {
        return boardLayout.size();
    }

    /**
     * Get method to get the number of solution steps
     */
    public int getNumberOfSolutionSteps() {
        return (movesMadeSoFar.size() / 2);
    }

     /**
     * Get method to get the number of solution steps * 2
     */
    public int getMovesMadeSize() {
        return (movesMadeSoFar.size());
    }

    
     /**
     * Get method to get the board state
     */
    public ArrayList<Integer> getBoardState() {
        return boardLayout;
    }

}
