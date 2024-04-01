import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

// Starter by Ian Gent, Oct 2022
//
// // This class is provided to save you writing some of the basic parts of the code
// // Also to provide a uniform command line structure
//
// // You may freely edit this code if you wish, e.g. adding methods to it. 
// // Obviously we are aware the starting point is provided so there is no need to explicitly credit us
// // Please clearly mark any new code that you have added/changed to make finding new bits easier for us
//
//
// // Edit history:
// // V1 released 3 Oct 2022
//
//

public class LBSMain {

	public static void printUsage() {
		System.out.println("Input not recognised.  Usage is:");
		System.out.println("java LBSmain GEN|CHECK|SOLVE|GRACECHECK|GRACESOLVE <arguments>");
		System.out.println("     GEN arguments are seed [numpiles=17] [numranks=13] [numsuits=4] ");
		System.out.println("                       all except seed may be omitted, defaults shown");
		System.out.println("     SOLVE/GRACESOLVE argument is file]");
		System.out.println("                     if file is or is - then stdin is used");
		System.out.println("     CHECK/GRACECHECK argument is file1 [file2]");
		System.out.println("                     if file1 is - then stdin is used");
		System.out.println("                     if file2 is ommitted or is - then stdin is used");
		System.out.println("                     at least one of file1/file2 must be a filename and not stdin");
	}

	// File opening sample code from
	// https://www.w3schools.com/java/java_files_read.asp
	// This function returns an arraylist of integer type
	public static ArrayList<Integer> readIntArray(String filename) {

		ArrayList<Integer> result;
		Scanner reader;
		try {
			File file = new File(filename);
			reader = new Scanner(file);
			result = readIntArray(reader);
			reader.close();
			return result;
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		// drop through case
		return new ArrayList<Integer>(0);

	}

	public static ArrayList<Integer> readIntArray(Scanner reader) {
		ArrayList<Integer> result = new ArrayList<Integer>(0);
		while (reader.hasNextInt()) {
			result.add(reader.nextInt());
		}
		return result;
	}

	// ************************************ Main Function Below
	// *************************************

	public static void main(String[] args) throws StackEmptyException {

		Scanner stdInScanner = new Scanner(System.in);
		ArrayList<Integer> workingList;

		LBSLayout layout;

		int seed;
		int ranks;
		int suits;
		int numpiles;


		if (args.length < 1) {
			printUsage();
			return;
		}
		;

		switch (args[0].toUpperCase()) {

		case "GEN": {
			if (args.length < 2) {
				printUsage();
				return;
			}
			;
			seed = Integer.parseInt(args[1]);
			numpiles = (args.length < 3 ? 17 : Integer.parseInt(args[2]));
			ranks = (args.length < 4 ? 13 : Integer.parseInt(args[3]));
			suits = (args.length < 5 ? 4 : Integer.parseInt(args[4]));

			layout = new LBSLayout(ranks, suits);
			layout.randomise(seed, numpiles);
			layout.print();
			stdInScanner.close();
			return;
		}
		case "SOLVE": {

			if (args.length < 2 || args[1].equals("-")) {
				layout = new LBSLayout(readIntArray(stdInScanner));
			} else {
				layout = new LBSLayout(readIntArray(args[1]));
			}

			// Parse input data
			int numberOfRanksPerSuit = layout.numRanks();
			int numberOfSuitsInDeck = layout.numSuits();
			int numberOfPiles = layout.numPiles();
	
			// Represent the board layout as elements of a list called piles
			ArrayList<Integer> piles = new ArrayList<>(numberOfPiles);
			for (int i = 0; i < numberOfPiles; i++) {
				piles.add(layout.cardAt(i));
			}

			// Creating a set of possible cards in deck to check legality later on
			int[][] ranksAndSuits = new int[numberOfRanksPerSuit][numberOfSuitsInDeck];
			for (int i = 0; i < numberOfRanksPerSuit; i++) {
				for (int j = 0; j < numberOfSuitsInDeck; j++) {
					ranksAndSuits[i][j] = (1 + i) + (numberOfRanksPerSuit * j);
				}
			}

			//This for loop checkes that the board layout is constructabe from the card deck provided
			for(int i=0 ; i < piles.size();i++){
				boolean cardExists = false;
				for (int k = 0; k < numberOfRanksPerSuit; k++) {
					for (int j = 0; j < numberOfSuitsInDeck; j++) {
						if (ranksAndSuits[k][j]== piles.get(i)){
							cardExists = true;
						}
					}
				}

				if (cardExists == false){
					System.out.println(-1);
					System.exit(0);
				}
			}
			
			// Create the rootNode for traversal with intial state which is unsolved board
			// layout
			Node rootNode = new Node(piles);

			// Push the rootNode to a stack for iterative implementation
			Stack stack = new Stack();
			stack.push(rootNode);

			
			//Another stack stores the solution node if one exists.
			Stack solutionNodes = new Stack();

			while (stack.isEmpty() == false) {
				Node nodeBeingVisited = stack.pop();

				// If the size of board piles is 1, that indicates a successful solution
				if (nodeBeingVisited.getSize() == 1) {
						solutionNodes.push(nodeBeingVisited);
						break;
					}
				

				// All cards except the first cards might be available for a move
				ArrayList<Integer> availablePiecesForMove = new ArrayList<>();

				//The for loop adds pieces that are available for a move to a list
				for (int i = 1; i < nodeBeingVisited.getSize(); i++) {
					availablePiecesForMove.add(nodeBeingVisited.getCard(i));
				}

				//This for loop makes sure every child node which can be created is created
				for (int counter = 0; counter < availablePiecesForMove.size(); counter++) {
					int cardPickedForMove = availablePiecesForMove.get(counter);

					// A card might legally slide or jump at the same time which means there might
					// be 2 possible legal destination piles
					ArrayList<Integer> destinationPile = new ArrayList<>();
					int sourcePile = 0;
					int cardAdjacent = -1;
					int cardInJump = -1;

					ArrayList<Integer> legalCards = new ArrayList<>();

					// Iterating over 2d array ranksAndSuits to find the row number and column of
					// the picked card
					for (int i = 0; i < numberOfRanksPerSuit; i++) {
						for (int j = 0; j < numberOfSuitsInDeck; j++) {

							/*
							 * When the cardPicked is found in the ranksAndSuits 2d array, the cards which
							 * the picked card can move on top of is, by definition of the game, should be
							 * either of same rank or suit. These cards correlate to the cards which are on
							 * the same row or column of the picked card.
							 */
							if (ranksAndSuits[i][j] == cardPickedForMove) {

								// This for loop adds the cards in the same rank to the legalCards arraylist
								for (int k = 0; k < numberOfSuitsInDeck; k++) {
									if (k != j) {
										legalCards.add(ranksAndSuits[i][k]); // card itself isn't in the legal card set
																				// cause all cards are unique
									}
								}

								// This for loop adds the cards in the same suit to the legalCards arraylist
								for (int k = 0; k < numberOfRanksPerSuit; k++) {
									if (k != i) {
										legalCards.add(ranksAndSuits[k][j]);
									}
								}

							}

						}
					}

					// For loop finds which pile the card picked is located on the board
					for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
						if (nodeBeingVisited.getCard(i) == cardPickedForMove) {
							sourcePile = i;
							break;
						}
					}

					cardAdjacent = nodeBeingVisited.getCard(sourcePile - 1);

					// If condition is necessary security for index out of bound error
					if ((sourcePile - 3) >= 0) {
						cardInJump = nodeBeingVisited.getCard(sourcePile - 3);
					}

					// If the card adjacent is one of the legal cards the card picked can move on
					// top of,
					// the destination pile is added to the legal destination pile list
					for (int i = 0; i < legalCards.size(); i++) {
						if (legalCards.get(i) == cardAdjacent) {
							destinationPile.add(sourcePile - 1);
							break;
						}
					}

					// Makes sure no unnecessary looping occurs if there isn't a card to jump to
					if (cardInJump != -1) {
						// If the card at jump is one of the legal cards the card picked can move on top
						// of,
						// the destination pile is added to the legal destination pile list
						for (int i = 0; i < legalCards.size(); i++) {
							if (legalCards.get(i) == cardInJump) {
								destinationPile.add(sourcePile - 3);
								break;
							}
						}
					}

					// If the destination pile size is 1, that means only one legal child node can
					// be created
					if (destinationPile.size() == 1) {
						ArrayList<Integer> newBoardLayout = new ArrayList<>();
						ArrayList<Integer> newMovesMadeSoFar = new ArrayList<>();

						for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
							newBoardLayout.add(nodeBeingVisited.getCard(i));
						}

						for (int i = 0; i < nodeBeingVisited.getMovesMadeSize(); i++) {
							newMovesMadeSoFar.add(nodeBeingVisited.getSolution(i));
						}

						newBoardLayout.remove(sourcePile);
						newBoardLayout.set(destinationPile.get(0), cardPickedForMove);
						newMovesMadeSoFar.add(cardPickedForMove);
						newMovesMadeSoFar.add(destinationPile.get(0));

						Node newNode = new Node(newBoardLayout, newMovesMadeSoFar);

						stack.push(newNode);
					}

					// If the destination pile size is 2, that means 2 legal child nodes can be
					// created
					if (destinationPile.size() == 2) {
						ArrayList<Integer> newBoardLayout1 = new ArrayList<>();
						ArrayList<Integer> newBoardLayout2 = new ArrayList<>();

						ArrayList<Integer> newMovesMadeSoFar1 = new ArrayList<>();
						ArrayList<Integer> newMovesMadeSoFar2 = new ArrayList<>();

						for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
							newBoardLayout1.add(nodeBeingVisited.getCard(i));
							newBoardLayout2.add(nodeBeingVisited.getCard(i));
						}

						for (int i = 0; i < nodeBeingVisited.getMovesMadeSize(); i++) {
							newMovesMadeSoFar1.add(nodeBeingVisited.getSolution(i));
							newMovesMadeSoFar2.add(nodeBeingVisited.getSolution(i));
						}

						newBoardLayout1.remove(sourcePile);
						newBoardLayout2.remove(sourcePile);

						newBoardLayout1.set(destinationPile.get(0), cardPickedForMove);
						newBoardLayout2.set(destinationPile.get(1), cardPickedForMove);

						newMovesMadeSoFar1.add(cardPickedForMove);
						newMovesMadeSoFar1.add(destinationPile.get(0));

						newMovesMadeSoFar2.add(cardPickedForMove);
						newMovesMadeSoFar2.add(destinationPile.get(1));

						Node newNode1 = new Node(newBoardLayout1, newMovesMadeSoFar1);
						Node newNode2 = new Node(newBoardLayout2, newMovesMadeSoFar2);

						stack.push(newNode1);
						stack.push(newNode2);

					}

				}

			}

			if (solutionNodes.isEmpty() != true) {
				Node solution = solutionNodes.pop();
				System.out.print(solution.getNumberOfSolutionSteps() + " ");
				for (int i = 0; i < solution.getMovesMadeSize(); i++) {
					System.out.print(solution.getSolution(i) + " ");
				}
			} else {
				System.out.print(-1);
			}
			stdInScanner.close();
			return;
		}

		case "SOLVEGRACE": {
			if (args.length < 2 || args[1].equals("-")) {
				layout = new LBSLayout(readIntArray(stdInScanner));
			} else {
				layout = new LBSLayout(readIntArray(args[1]));
			}

			// Parse input data
			int numberOfRanksPerSuit = layout.numRanks();
			int numberOfSuitsInDeck = layout.numSuits();
			int numberOfPiles = layout.numPiles();

			// Represent the board layout as elements of a list called piles
			ArrayList<Integer> piles = new ArrayList<>(numberOfPiles);
			for (int i = 0; i < numberOfPiles; i++) {
				piles.add(layout.cardAt(i));
			}

			// Creating a set of possible cards in deck to check legality later on
			int[][] ranksAndSuits = new int[numberOfRanksPerSuit][numberOfSuitsInDeck];
			for (int i = 0; i < numberOfRanksPerSuit; i++) {
				for (int j = 0; j < numberOfSuitsInDeck; j++) {
					ranksAndSuits[i][j] = (1 + i) + (numberOfRanksPerSuit * j);
				}
			}

			//This for loop checkes that the board layout is constructabe from the card deck provided
			for(int i=0 ; i < piles.size();i++){
				boolean cardExists = false;
				for (int k = 0; k < numberOfRanksPerSuit; k++) {
					for (int j = 0; j < numberOfSuitsInDeck; j++) {
						if (ranksAndSuits[k][j]== piles.get(i)){
							cardExists = true;
						}
					}
				}

				if (cardExists == false){
					System.out.println(-1);
					System.exit(0);
				}
			}

			// Create the rootNode for traversal with intial state which is unsolved board
			// layout
			Node rootNode = new Node(piles);

			// Push the rootNode to a stack for iterative implementation
			Stack stack = new Stack();
			stack.push(rootNode);

			//Another stack stores the solution node if one exists.
			Stack solutionNodes = new Stack();

			while (stack.isEmpty() == false) {
				Node nodeBeingVisited = stack.pop();

				// If the size of board piles is 1, that indicates a successful solution
				if (nodeBeingVisited.getSize() == 1) {
						solutionNodes.push(nodeBeingVisited);
						break;
					}
				// All cards except the first cards might be available for a move
				ArrayList<Integer> availablePiecesForMove = new ArrayList<>();
				
				//The for loop adds pieces that are available for a move to a list
				for (int i = 1; i < nodeBeingVisited.getSize(); i++) {
					availablePiecesForMove.add(nodeBeingVisited.getCard(i));
				}

				//This for loop makes sure every child node which can be created is created
				for (int counter = 0; counter < availablePiecesForMove.size(); counter++) {
					int cardPickedForMove = availablePiecesForMove.get(counter);

				// A card might legally slide, jump or use saving grace given it hasn't been used before
				// Therefore, there might be up to 3 possible legal destination piles which makes ArrayList a good choice for data storage
				ArrayList<Integer> destinationPile = new ArrayList<>();
				int sourcePile = 0;
				int cardAdjacent = -1;
				int cardInJump = -1;

				ArrayList<Integer> legalCards = new ArrayList<>();

				// Iterating over 2d array ranksAndSuits to find the row number and column of
				// the picked card
					for (int i = 0; i < numberOfRanksPerSuit; i++) {
						for (int j = 0; j < numberOfSuitsInDeck; j++) {

							/*
							 * When the cardPicked is found in the ranksAndSuits 2d array, the cards which
							 * the picked card can move on top of is, by definition of the game, should be
							 * either of same rank or suit. These cards correlate to the cards which are on
							 * the same row or column of the picked card.
							 */
							if (ranksAndSuits[i][j] == cardPickedForMove) {

								// This for loop adds the cards in the same rank to the legalCards arraylist
								for (int k = 0; k < numberOfSuitsInDeck; k++) {
									if (k != j) {
										legalCards.add(ranksAndSuits[i][k]); // card itself isn't in the legal card set
																				// cause all cards are unique
									}
								}

								// This for loop adds the cards in the same suit to the legalCards arraylist
								for (int k = 0; k < numberOfRanksPerSuit; k++) {
									if (k != i) {
										legalCards.add(ranksAndSuits[k][j]);
									}
								}

							}

						}
					}
				
					// For loop finds which pile the card picked is located on the board
				for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
					if (nodeBeingVisited.getCard(i) == cardPickedForMove) {
						sourcePile = i;
						break;
					}
				}
				cardAdjacent = nodeBeingVisited.getCard(sourcePile - 1);

				// If condition is necessary security for index out of bound error
				if ((sourcePile - 3) >= 0) {
					cardInJump = nodeBeingVisited.getCard(sourcePile - 3);
				}

				// If the card adjacent is one of the legal cards the card picked can move on
				// top of,
				// the destination pile is added to the legal destination pile list
				for (int i = 0; i < legalCards.size(); i++) {
					if (legalCards.get(i) == cardAdjacent) {
						destinationPile.add(sourcePile - 1);
						break;
						}
				}

				// Makes sure no unnecessary looping occurs if there isn't a card to jump to
				if (cardInJump != -1) {
					// If the card at jump is one of the legal cards the card picked can move on top
					// of,
					// the destination pile is added to the legal destination pile list
					for (int i = 0; i < legalCards.size(); i++) {
						if (legalCards.get(i) == cardInJump) {
							destinationPile.add(sourcePile - 3);
							break;
						}
					}
				}	

					// If the destination pile size is 0, that means there is no legal child node
					//to create without using saving grace if not used previously
					if (nodeBeingVisited.getSavingGrace() == 1 ) {
						ArrayList<Integer> newBoardLayout = new ArrayList<>();
						ArrayList<Integer> newMovesMadeSoFar = new ArrayList<>();

						for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
							newBoardLayout.add(nodeBeingVisited.getCard(i));
						}

						for (int i = 0; i < nodeBeingVisited.getMovesMadeSize(); i++) {
							newMovesMadeSoFar.add(nodeBeingVisited.getSolution(i));
						}

						newBoardLayout.remove(sourcePile);
						newBoardLayout.set(0, cardPickedForMove);
						newMovesMadeSoFar.add(cardPickedForMove);
						newMovesMadeSoFar.add(0);


						Node newNode = new Node(newBoardLayout, newMovesMadeSoFar);
						newNode.useSavingGrace();
						stack.push(newNode);
						
					}


					// If the destination pile size is 1, that means only one legal child node can
					// be created
					if (destinationPile.size() == 1) {
						ArrayList<Integer> newBoardLayout = new ArrayList<>();
						ArrayList<Integer> newMovesMadeSoFar = new ArrayList<>();

						for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
							newBoardLayout.add(nodeBeingVisited.getCard(i));
						}

						for (int i = 0; i < nodeBeingVisited.getMovesMadeSize(); i++) {
							newMovesMadeSoFar.add(nodeBeingVisited.getSolution(i));
						}


						newBoardLayout.remove(sourcePile);
						newBoardLayout.set(destinationPile.get(0), cardPickedForMove);
						newMovesMadeSoFar.add(cardPickedForMove);
						newMovesMadeSoFar.add(destinationPile.get(0));

						Node newNode = new Node(newBoardLayout, newMovesMadeSoFar);

						if(nodeBeingVisited.getSavingGrace() == 0){
							newNode.useSavingGrace();
						}

						stack.push(newNode);
					}

					// If the destination pile size is 2, that means 2 legal child nodes can be
					// created
					if (destinationPile.size() == 2) {
						ArrayList<Integer> newBoardLayout1 = new ArrayList<>();
						ArrayList<Integer> newBoardLayout2 = new ArrayList<>();

						ArrayList<Integer> newMovesMadeSoFar1 = new ArrayList<>();
						ArrayList<Integer> newMovesMadeSoFar2 = new ArrayList<>();

						for (int i = 0; i < nodeBeingVisited.getSize(); i++) {
							newBoardLayout1.add(nodeBeingVisited.getCard(i));
							newBoardLayout2.add(nodeBeingVisited.getCard(i));
						}

						for (int i = 0; i < nodeBeingVisited.getMovesMadeSize(); i++) {
							newMovesMadeSoFar1.add(nodeBeingVisited.getSolution(i));
							newMovesMadeSoFar2.add(nodeBeingVisited.getSolution(i));
						}

						newBoardLayout1.remove(sourcePile);
						newBoardLayout2.remove(sourcePile);

						newBoardLayout1.set(destinationPile.get(0), cardPickedForMove);
						newBoardLayout2.set(destinationPile.get(1), cardPickedForMove);

						newMovesMadeSoFar1.add(cardPickedForMove);
						newMovesMadeSoFar1.add(destinationPile.get(0));

						newMovesMadeSoFar2.add(cardPickedForMove);
						newMovesMadeSoFar2.add(destinationPile.get(1));

						Node newNode1 = new Node(newBoardLayout1, newMovesMadeSoFar1);
						Node newNode2 = new Node(newBoardLayout2, newMovesMadeSoFar2);

						if(nodeBeingVisited.getSavingGrace() == 0){
							newNode1.useSavingGrace();
							newNode2.useSavingGrace();
						}

						stack.push(newNode1);
						stack.push(newNode2);

					}

				}
			}
				
			if (solutionNodes.isEmpty() != true) {
				Node solution = solutionNodes.pop();
				System.out.print(solution.getNumberOfSolutionSteps() + " ");
				for (int i = 0; i < solution.getMovesMadeSize(); i++) {
					System.out.print(solution.getSolution(i) + " ");
				}
			} else {
				System.out.print(-1);
			}
			stdInScanner.close();
			return;
		}

		case "CHECK": {
			// Command line inputs: Arg 0 = check / Arg 1 = problem instance / Arg 2 =
			// solution

			// Handles wrong input case
			if (args.length < 2 || (args[1].equals("-") && args.length < 3)
					|| (args[1].equals("-") && args[2].equals("-"))) {
				printUsage();
				stdInScanner.close();
				return;
			}

			// Parses the problem instance (board layout) according to input type (Stdin or
			// file)
			if (args[1].equals("-")) {
				layout = new LBSLayout(readIntArray(stdInScanner));
			} else {
				layout = new LBSLayout(readIntArray(args[1]));
			}

			// Parses solution (moves) according to input type (Stdin or file)
			if (args.length < 3 || args[2].equals("-")) {
				workingList = readIntArray(stdInScanner);
			} else {
				workingList = readIntArray(args[2]);
			}

			if(workingList.size() == 0){
				System.out.println("false");
				System.exit(0);
			}
			
			int numberOfSolutionStepsLeft = workingList.get(0);
			workingList.remove(0);

			if (workingList.size() / 2 != numberOfSolutionStepsLeft) {
				System.out.println("false");
				System.exit(0);
			}

			int numberOfRanksPerSuit = layout.numRanks();
			int numberOfSuitsInDeck = layout.numSuits();
			int numberOfPiles = layout.numPiles();

			//The list "piles" will represent the board state
			ArrayList<Integer> piles = new ArrayList<>(numberOfPiles);

			for (int i = 0; i < numberOfPiles; i++) {
				piles.add(layout.cardAt(i));
			}

			// Rows and columns --> 2d array structure is suited well for storing the entire legal deck
			int[][] ranksAndSuits = new int[numberOfRanksPerSuit][numberOfSuitsInDeck];

			// initializing the 2d array with appropriate cards according to input data
			for (int i = 0; i < numberOfRanksPerSuit; i++) {
				for (int j = 0; j < numberOfSuitsInDeck; j++) {
					ranksAndSuits[i][j] = (1 + i) + (numberOfRanksPerSuit * j);
				}
			}

			//This for loop checkes that the board layout is constructabe from the card deck provided
			for(int i=0 ; i < piles.size();i++){
				boolean cardExists = false;
				for (int k = 0; k < numberOfRanksPerSuit; k++) {
					for (int j = 0; j < numberOfSuitsInDeck; j++) {
						if (ranksAndSuits[k][j]== piles.get(i)){
							cardExists = true;
						}
					}
				}

				if (cardExists == false){
					System.out.println("false");
					System.exit(0);
				}
			}
			


			

			
			while (numberOfSolutionStepsLeft > 0 && piles.size() > 1) {
				boolean matchingCard = false;
				int cardPickedForMove = workingList.get(0);
				int destinationPile = workingList.get(1);
				int sourcePile = 0;
				ArrayList<Integer> legalCards = new ArrayList<>();
				

				// Iterating over 2d array ranksAndSuits to find the index of the picked card
				for (int i = 0; i < numberOfRanksPerSuit; i++) {
					for (int j = 0; j < numberOfSuitsInDeck; j++) {

						/*
						 * When the cardPicked is found in the ranksAndSuits 2d array, the cards which
						 * the picked card can move on top of is, by definition of the game, should be
						 * either of same rank or suit. These cards correlate to the cards which are on
						 * the same row or column of the picked card.
						 */
						if (ranksAndSuits[i][j] == cardPickedForMove) {

							// This for loop adds the cards in the same rank to the legalCards arraylist
							for (int k = 0; k < numberOfSuitsInDeck; k++) {
								if (k != j) {
									legalCards.add(ranksAndSuits[i][k]); // card itself isn't in the legal card set
																			// cause all cards are unique
								}
							}

							// This for loop adds the cards in the same suit to the legalCards arraylist
							for (int k = 0; k < numberOfRanksPerSuit; k++) {
								if (k != i) {
									legalCards.add(ranksAndSuits[k][j]);
								}
							}

						}

					}
				}

				//This for loop finds which pile the card is located at
				for (int i = 0; i < piles.size(); i++) {
					if (piles.get(i) == cardPickedForMove) {
						sourcePile = i;
						break;
					}
				}

				//This if statement makes sure the destination pile is either adjacent of the selected card
				//Or jumpable to the selected card
				if (destinationPile + 1 == sourcePile || destinationPile + 3 == sourcePile) {
					for (int i = 0; i < legalCards.size(); i++) {
						if (legalCards.get(i).equals(piles.get(destinationPile))) {
							matchingCard = true;
							piles.remove(sourcePile);
							piles.set(destinationPile, cardPickedForMove);
							workingList.remove(0);
							workingList.remove(0);
							numberOfSolutionStepsLeft--;
							break;
						}
					}
				}

	
				if (matchingCard == false) {
					System.out.println("false");
					System.exit(0);
				}

			}

			if (piles.size() == 1 && numberOfSolutionStepsLeft == 0) {
				System.out.println("true");
			} else {
				System.out.println("false");
			}

			stdInScanner.close();
			return;
		}

		case "CHECKGRACE": {
			if (args.length < 2 || (args[1].equals("-") && args.length < 3)
					|| (args[1].equals("-") && args[2].equals("-"))) {
				printUsage();
				return;
			}
			;
			if (args[1].equals("-")) {
				layout = new LBSLayout(readIntArray(stdInScanner));
			} else {
				layout = new LBSLayout(readIntArray(args[1]));
			}
			if (args.length < 3 || args[2].equals("-")) {
				workingList = readIntArray(stdInScanner);
			} else {
				workingList = readIntArray(args[2]);
			}

			if(workingList.size() == 0){
				System.out.println("false");
				System.exit(0);
			}

			int numberOfSolutionStepsLeft = workingList.get(0);
			workingList.remove(0);

			if (workingList.size() / 2 != numberOfSolutionStepsLeft) {
				System.out.println("false");
				System.exit(0);
			}


			int numberOfRanksPerSuit = layout.numRanks();
			int numberOfSuitsInDeck = layout.numSuits();
			int numberOfPiles = layout.numPiles();
			int savingGraceAllowence = 1;

			//The list "piles" will represent the board state
			ArrayList<Integer> piles = new ArrayList<>(numberOfPiles);

			for (int i = 0; i < numberOfPiles; i++) {
				piles.add(layout.cardAt(i));
			}

			// Rows and columns --> 2d array structure is suited well for storing the entire legal deck
			int[][] ranksAndSuits = new int[numberOfRanksPerSuit][numberOfSuitsInDeck];

			// initializing the 2d array with appropriate cards according to input data
			for (int i = 0; i < numberOfRanksPerSuit; i++) {
				for (int j = 0; j < numberOfSuitsInDeck; j++) {
					ranksAndSuits[i][j] = (1 + i) + (numberOfRanksPerSuit * j);
				}
			}

			//This for loop checkes that the board layout is constructabe from the card deck provided
			for(int i=0 ; i < piles.size();i++){
				boolean cardExists = false;
				for (int k = 0; k < numberOfRanksPerSuit; k++) {
					for (int j = 0; j < numberOfSuitsInDeck; j++) {
						if (ranksAndSuits[k][j]== piles.get(i)){
							cardExists = true;
						}
					}
				}

				if (cardExists == false){
					System.out.println("false");
					System.exit(0);
				}
			}

			while (numberOfSolutionStepsLeft > 0 && piles.size() > 1) {
				boolean matchingCard = false;
				int cardPickedForMove = workingList.get(0);
				int destinationPile = workingList.get(1);
				int sourcePile = 0;
				ArrayList<Integer> legalCards = new ArrayList<>();
				// Iterating over 2d array ranksAndSuits to find the index of the picked card
				for (int i = 0; i < numberOfRanksPerSuit; i++) {
					for (int j = 0; j < numberOfSuitsInDeck; j++) {

						/*
						 * When the cardPicked is found in the ranksAndSuits 2d array, the cards which
						 * the picked card can move on top of is, by definition of the game, should be
						 * either of same rank or suit. These cards correlate to the cards which are on
						 * the same row or column of the picked card.
						 */
						if (ranksAndSuits[i][j] == cardPickedForMove) {

							// This for loop adds the cards in the same rank to the legalCards arraylist
							for (int k = 0; k < numberOfSuitsInDeck; k++) {
								if (k != j) {
									legalCards.add(ranksAndSuits[i][k]); // card itself isn't in the legal card set
																			// cause all cards are unique
								}
							}

							// This for loop adds the cards in the same suit to the legalCards arraylist
							for (int k = 0; k < numberOfRanksPerSuit; k++) {
								if (k != i) {
									legalCards.add(ranksAndSuits[k][j]);
								}
							}

						}

					}
				}

				//This for loop finds which pile the card is located at
				for (int i = 0; i < piles.size(); i++) {
					if (piles.get(i) == cardPickedForMove) {
						sourcePile = i;
						break;
					}
				}

				//This if statement makes sure the destination pile is either adjacent of the selected card
				//Or jumpable to the selected card
				if (destinationPile + 1 == sourcePile || destinationPile + 3 == sourcePile) {
					for (int i = 0; i < legalCards.size(); i++) {
						if (legalCards.get(i).equals(piles.get(destinationPile))) {
							matchingCard = true;
							piles.remove(sourcePile);
							piles.set(destinationPile, cardPickedForMove);
							workingList.remove(0);
							workingList.remove(0);
							numberOfSolutionStepsLeft--;
							break;
						}
					}
				}

				if(destinationPile == 0 && matchingCard == false && savingGraceAllowence == 1){
					matchingCard = true; 
					piles.remove(sourcePile);
					piles.set(destinationPile, cardPickedForMove);
					workingList.remove(0);
					workingList.remove(0);
					numberOfSolutionStepsLeft--;
					savingGraceAllowence = 0;
				}
				
				if (matchingCard == false) {
					System.out.println("false");
					System.exit(0);
				}
			}

			if (piles.size() == 1 && numberOfSolutionStepsLeft == 0) {
				System.out.println("true");
			} else {
				System.out.println("false");
			}

			stdInScanner.close();
			return;
		}
		default: {
			printUsage();
		}

		}
	}
}
