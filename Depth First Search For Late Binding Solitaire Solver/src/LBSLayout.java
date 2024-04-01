import java.util.ArrayList;
import java.util.Random ;
import java.util.Iterator ;
import java.io.File;  
import java.io.FileNotFoundException;  
import java.util.Scanner; 

// Starter by Ian Gent, Oct 2022
// 
// // This class is provided to save you writing some of the basic parts of the code
// // Also to provide a uniform layout generator
//
// // You may freely edit this code if you wish, e.g. adding methods to it. 
// // Obviously we are aware the starting point is provided so there is no need to explicitly credit us
// // Please clearly mark any new code that you have added/changed to make finding new bits easier for us
//
// V1 3 Oct 2022

public class LBSLayout {

    
    protected int numranks; //Number of ranks in game per suit
    protected int numsuits; //Number of suits in the game
    protected int numpiles; //Number of piles in the game

    //Arraylist data structure used in the initial layout
    protected ArrayList<Integer> layout;

    //Returns numRanks variable value
    public int numRanks() { 
        return numranks;
    }

    //Returns numSuits variable value
    public int numSuits() { 
        return numsuits;
    }

    //Returns numPiles variable value
    public int numPiles() { 
        return layout.size();
    }

    //Calculates and returns number of cards in the deck
    public int cardsInDeck() { 
        return numranks * numsuits; 
    }

    
    //Returns the card at the top of the pile
    public int cardAt(int pile) { 
        if(pile >= numPiles()) { 
            return -1; }
        else {
            return layout.get(pile);
        }
    }

    //Copies the layout of the cards to another arraylist of integer type
    private ArrayList<Integer> copyLayout() {
         ArrayList<Integer> copy = new  ArrayList<Integer>(0);
         for(int i=0;i<numPiles();i++) { 
             copy.add(cardAt(i));
         }
         return copy; 
    }


    public void randomise (int seed, int numInLayout)   { 
        int maxindex = cardsInDeck();

        int[] cards = new int[maxindex] ;
        for (int i = 0; i < maxindex; i++) {
          cards[i] = i+1 ;
        }
        
        // Completely shuffle the deck 
        // Choose the sequence uniformly at random
        // NB random.nextInt(k) gives a value in range 0..k-1

        Random random = new Random(seed) ;

        for (int i = 0; i < maxindex-1; i++) {
          int temp = cards[i] ;
          int index = i+random.nextInt(maxindex-i) ;
          cards[i] = cards[index] ;
          cards[index] = temp ;
        }

        // Now empty the piles
        //
        layout.clear();

        // Now put the right number of cards on the layout
        // For safety don't allow more cards than are available 

        int remaining=Math.min(numInLayout,maxindex);

        int nextpile = 0; 

        for (int i = 0; i < remaining ; i++) { 
            layout.add(cards[i]);
        }

    }

	public void print() {

		System.out.println(numRanks() + " " + numSuits() + " " + numPiles());
        for (int i=0; i < numPiles() ; i++) { 
                System.out.print(layout.get(i)+" ");
            }
        System.out.println();
	}



    // Helper function for constructors 
    private void createLayout() { 
        this.layout = new ArrayList<Integer>(0);
    }

    /*
    Variety of Constructors Below
    */
	

    //Creates empty problem for standard deck 
    public LBSLayout() {
        this(13,4) ; 
    }

   //Creates empty problem with given parameters 
   public LBSLayout(Integer ranks, Integer suits) {
        this.numranks = ranks;
        this.numsuits = suits;
        createLayout();
	}


    /*Creates a problem with input format specified in the practical sheet
    First 3 numbers represent: 
    1- The number of ranks in each suit
    2- the number of suits in the deck of cards
    3- The number of cards in total 
    4- Other integers represents the cards
    */
    public LBSLayout(ArrayList<Integer> integers) { 
		Iterator<Integer> reader = integers.iterator();
        int r = (reader.hasNext() ? reader.next() : 0);
        int s = (reader.hasNext() ? reader.next() : 0);
        int p = (reader.hasNext() ? reader.next() : 0);
        this.numranks = r;
        this.numsuits = s;
        this.numpiles = p;
        createLayout();
        int nextpile = 0;
        while( reader.hasNext() && (nextpile < p) ) { 
            int card = reader.next();
            layout.add(card);
        }
    }

    // Copies Constructor
    public LBSLayout(LBSLayout old) { 
        this.numranks = old.numRanks();
        this.numsuits = old.numSuits();
        this.numpiles = old.numPiles();
        this.layout = old.copyLayout();
    }

}




