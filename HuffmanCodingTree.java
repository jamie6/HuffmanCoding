import java.util.Scanner;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author Jamie
 */
public class HuffmanCodingTree
{
    private HuffmanNode root;
    String lines;
    HashMap<Character, String> huffmanTable; // this will store char and relevant code(String)
    ConcurrentHashMap<Character, LongAdder> tempCharFreq;
    ReentrantLock lock = new ReentrantLock();
    
    public HuffmanCodingTree(Scanner scanner, int threadCount)
    {
        tempCharFreq = new ConcurrentHashMap<>();
        
        if ( threadCount == 1 )
            root = generateHuffmanCodingTree(generateCharacterFrequency(scanner));
        else root = generateHuffmanCodingTree(generateCharacterFrequencyMultithread(scanner, threadCount));
        generateHuffmanTable();
    }
    
    public int getCharacterCount()
    {
        return root.getWeight();
    }
    
    public String getCompression()
    {
        java.lang.StringBuilder compression = new java.lang.StringBuilder();
        for ( int i = 0; i < lines.length(); i++ )
        {
            compression.append(huffmanTable.get(lines.charAt(i)));
        }
        return compression.toString();
    }
    
    public String getCompressionMultithread(int threadCount)
    {
        java.lang.StringBuilder sbs[] = new java.lang.StringBuilder[threadCount+1];
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        int start, end=0, inc = lines.length()/threadCount;
        for ( int i = 0; i < threadCount; i++ )
        {
            sbs[i] = new java.lang.StringBuilder();
            start = end;
            end = inc*(i+1);
            executor.execute( new EncodeThread(sbs[i], start, end));
        }
        executor.shutdown();
        try
        {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch( InterruptedException e )
        {
            System.out.println("Interrupted genCharFreqGen\n" + e.getMessage());
        }
        sbs[threadCount] = new java.lang.StringBuilder();
        for ( int i = 0; i < threadCount; i++ ) sbs[threadCount].append(sbs[i]);
        
        return sbs[threadCount].toString();
    }
    
    private class EncodeThread implements Runnable
    {
        java.lang.StringBuilder sb;
        int start, end;
        public EncodeThread(java.lang.StringBuilder sb, int start, int end)
        {
            this.sb = sb;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public void run()
        {
            for ( int i = start; i < end; i++ )
                sb.append(huffmanTable.get(lines.charAt(i)));
        }
    }
    
    public String getDecompression(String compression)
    {
        String decompression = "";
        HuffmanNode currentNode = root;
        for ( int i = 0; i < compression.length(); i++ )
        {
            if ( compression.charAt(i) == '0' )
            {
                currentNode = currentNode.getLeft();
            }
            else if ( compression.charAt(i) == '1' )
            {
                currentNode = currentNode.getRight();
            }
            if ( currentNode.isLeaf() )
            {
                decompression += currentNode.getValue();
                currentNode = root;
            }
        }
        return decompression;
    }
    
    public static byte[] binaryStringToByteArray(String binaryString)
    {
        // this is so everything fits into bytes
        if ( binaryString.length() % 8 != 0 )
        {
            int remainder = binaryString.length() % 8;
            for ( int i = 0; i < remainder; i++ )
                binaryString += "0";
        }
        char[] binaryChar = binaryString.toCharArray();
        byte[] bytes = new byte[binaryChar.length/8];
        int bytesIndex=0;
        int powerValue = 7;
        int tempByte=0;
        for ( int i = 0; i < binaryChar.length; i++ )
        {
            if ( binaryChar[i] == '1' ) tempByte += Math.pow(2, powerValue); 
            powerValue--;
            if ( powerValue == -1 ) 
            {
                bytes[bytesIndex] = (byte)tempByte;
                powerValue = 7; // reset index
                bytesIndex++; // increment indext
                tempByte = 0; // reset tempByte to 0
            }
        }
        return bytes;
    }
    
    private PriorityQueue<HuffmanNode> generateCharacterFrequency(Scanner scanner)
    {
        lines = "";
        PriorityQueue<HuffmanNode> characterFrequency = new PriorityQueue<>(
        1, (a,b)->(a.getWeight()-b.getWeight()));
        
        int[] tempCharFreq = new int[Character.MAX_VALUE+1];
        String line;
        while ( scanner.hasNextLine() )
        {
            line = scanner.nextLine();
            lines += line + '\n';
            for ( int i = 0; i < line.length(); i++ )
            {
                tempCharFreq[line.charAt(i)]++;
            }
            tempCharFreq['\n']++;
        }
        for ( int i = 0; i < tempCharFreq.length; i++ )
        {
            if ( tempCharFreq[i] != 0 )
            {
                characterFrequency.add(new HuffmanNode((char)i, tempCharFreq[i]));
            }
        }
        return characterFrequency;
    }
    
    private PriorityQueue<HuffmanNode> generateCharacterFrequencyMultithread(Scanner scanner, int threadCount)
    {
        PriorityQueue<HuffmanNode> characterFrequency = new PriorityQueue<>( 1, (a,b)->(a.getWeight()-b.getWeight()));
        
        //int[] tempCharFreq = new int[Character.MAX_VALUE+1];
        String line;
        lines = "";
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        while ( scanner.hasNextLine())
        {
            line = scanner.nextLine();
            lines += line + '\n';
            executor.execute(new FrequencyThread(/*tempCharFreq,*/ line));
        }
        executor.shutdown();
        try
        {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch( InterruptedException e )
        {
            System.out.println("Interrupted genCharFreqGen\n" + e.getMessage());
        }
//        executor = Executors.newFixedThreadPool(threadCount);
//        int start, end=0, inc = tempCharFreq.size()/threadCount; 
//        for ( int i = 0; i < threadCount; i++ )
//        {
//            start = end;
//            end = inc*(i+1);
//            executor.execute(new Thread(new GenerateFrequencyPriorityQueue(characterFrequency, /*tempCharFreq,*/ start, end)));
//        }
//        executor.shutdown();
//        try
//        {
//            executor.awaitTermination(10, TimeUnit.SECONDS);
//        }
//        catch( InterruptedException e )
//        {
//            System.out.println("Interrupted genCharFreqGen\n" + e.getMessage());
//        }

        // sort the frequencies
        tempCharFreq.forEach( (k,v) ->characterFrequency.add(new HuffmanNode(k, v.intValue())));
        return characterFrequency;
    }
    
    private class FrequencyThread implements Runnable
    {
        //int[] tempCharFreq;
        String line;
        public FrequencyThread( /*int[] tempCharFreq,*/ String line )
        {
            //this.tempCharFreq = tempCharFreq;
            this.line = line;
        }
        
        @Override
        /*synchronized*/ public void run()
        {
            for ( int i = 0; i < line.length(); i++ )
            {
//                tempCharFreq[line.charAt(i)]++;
                tempCharFreq.computeIfAbsent(line.charAt(i), k -> new LongAdder()).increment();
            }
//            tempCharFreq['\n']++;
            tempCharFreq.computeIfAbsent('\n', k -> new LongAdder()).increment();
        }
    }
    
    private class GenerateFrequencyPriorityQueue implements Runnable
    {
        PriorityQueue<HuffmanNode> characterFrequency;
        int[] tempCharFreq;
        int start, end;
        public GenerateFrequencyPriorityQueue(PriorityQueue<HuffmanNode> characterFrequency, int[] tempCharFreq, int start, int end)
        {
            this.characterFrequency = characterFrequency;
            //this.tempCharFreq = tempCharFreq;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public void run()
        {
            for ( int i = start; i < end; i++ )
            {
                if ( tempCharFreq[i] != 0 )
                {
                    lock.lock();
                    characterFrequency.add(new HuffmanNode((char)i, tempCharFreq[i]));
                    lock.unlock();
                }
                
            }
        }
    }
    
    private HuffmanNode generateHuffmanCodingTree(PriorityQueue<HuffmanNode> characterFrequency)
    {
        HuffmanNode tempRoot = null, tempLeft, tempRight;
        while (characterFrequency.size() > 1)
        {
            tempLeft = characterFrequency.poll(); // get the lowest freq in the queue
            tempRight = characterFrequency.poll(); // get the next lowest freq in the queue
            tempRoot = new HuffmanNode(tempLeft.getWeight()+tempRight.getWeight()); // make a new node with combined weights
            tempRoot.setLeft(tempLeft); // create a subtree
            tempRoot.setRight(tempRight);
            characterFrequency.add(tempRoot); // add subtree to the queue
        }
        return tempRoot;
    }
    
    private void generateHuffmanTable()
    {
        huffmanTable = new HashMap<>();
        treeTraversal(root, "");
    }
    
    private void treeTraversal(HuffmanNode node, String code)
    {
        if ( node == null ) return;
        treeTraversal(node.getLeft(), code + "0");
        treeTraversal(node.getRight(), code + "1");
        if ( node.getValue() != null )
            huffmanTable.put( node.getValue(), code);
    }
    
    private class HuffmanNode
    {
        private Character value;
        private int weight;
        private HuffmanNode left, right;
        
        public HuffmanNode( Character value, int weight, HuffmanNode left, HuffmanNode right)
        {
            this.value = value;
            this.weight = weight;
            this.left = left;
            this.right = right;
        }
        
        public HuffmanNode( int weight, HuffmanNode left, HuffmanNode right)
        {
            this(null, weight, left, right);
        }
        
        public HuffmanNode( Character value, int weight )
        {
            this(value, weight, null, null);
        }
        
        public HuffmanNode( char value )
        {
            this(value, 0, null, null);
        }
        
        public HuffmanNode( int weight )
        {
            this(null, weight, null, null);
        }
        
        public void increment()
        {
            weight++;
        }
        
        public int getWeight()
        {
            return weight;
        }
        
        public Character getValue()
        {
            return value;
        }
        
        public void setLeft(HuffmanNode left)
        {
            this.left = left;
        }
        
        public HuffmanNode getLeft()
        {
            return left;
        }
        
        public void setRight(HuffmanNode right)
        {
            this.right = right;
        }
        
        public HuffmanNode getRight()
        {
            return right;
        }
        
        public boolean isLeaf()
        {
            return left == null && right == null;
        }
        
        public boolean hasLeft()
        {
            return left != null;
        }
        
        public boolean hasRight()
        {
            return right != null;
        }
        
        @Override
        public String toString()
        {
            return "Value: " + value + ", Weight: " + weight;
        }
    }
}
