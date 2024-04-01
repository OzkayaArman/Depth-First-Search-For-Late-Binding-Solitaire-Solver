
/**
 * This class is an implementation of a Stack data structure.
 */
public class Stack {

    private Node[] nodes = new Node[200];
    private int maxStackSize = 50;
    private int topIndex = 0;
    
    /**
     * Constructor for the stack class.
     * 
     * @param size        the size of the common array
     * @param sharedArray passes the common array
     */
    public Stack(Node nodeIn) {
        nodes[0] = nodeIn;
        topIndex++;
    }

     /**
     * Constructor for the stack class.
     * 
     * @param sharedArray passes the common array
     */
    public Stack(){
    }

    /**
     * Pushes a node onto the stack.
     * 
     * @param pushedNode the element to be pushed
     */   
    public void push(Node pushedNode){
        if (isFull()== false) {
            nodes[topIndex + 1] = pushedNode;
            topIndex++;
        } else {
            Node[] newStack = new Node[maxStackSize * 2];
            for(int i=0;i<maxStackSize;i++){
                newStack[i] = nodes[i];
            }
            nodes = newStack;
            maxStackSize = maxStackSize * 2;   
        
            nodes[topIndex+1] = pushedNode;
            topIndex++;  
        }
    }

    /**
     * Pops a node from the stack.
     * 
     * @return the popped node
     * @throws StackEmptyException if the stack is empty
     */
    public Node pop() throws StackEmptyException {
        Node nodeOut = top();
        topIndex--;
        return nodeOut;
    }

    /**
     * Accesses the top element on the stack without removing it.
     * 
     * @return the top element
     * @throws StackEmptyException if the stack is empty
     */
    public Node top() throws StackEmptyException {
        if (!isEmpty()) {
            return nodes[topIndex];
        } else {
            throw new StackEmptyException();
        }
    }

    /**
     * Returns the number of nodes on the stack.
     * 
     * @return the number of nodes on the stack
     */
    public int size() {
        return topIndex+1;
    }

    /**
     * Checks whether the stack is empty.
     * 
     * @return true if the stack is empty
     */
    public boolean isEmpty() {
        if (topIndex == 0){
            return true;
        }else{
            return false;
        }

    }

    /**
     * Checks whether the stack is full
     * 
     * @return true if the stack is full
     */
    public boolean isFull() {
        if(topIndex + 1 == maxStackSize){
            return true;
        }else{
            return false;
        }
       

    }

    /**
     * Removes all nodes from the stack.
     */
    public void clear() {
        while (!isEmpty()) {
            try {
                pop();
            } catch (StackEmptyException e) {
                e.printStackTrace();
            }
        }
    }
}
