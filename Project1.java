import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 *
 * @author Jamie
 */
public class Project1
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        long startTime, endTime;
        String fileName = "usconstitution.txt";
        
        // single thread
        System.out.println("Single Thread:");
        startTime = System.currentTimeMillis();
        HuffmanCodingTree huffmanCodingTree = new HuffmanCodingTree(getFile(fileName), 1);
        endTime = System.currentTimeMillis();
        System.out.println("Time to generate huffman coding tree: " + (endTime-startTime) + " ms.");
        
        // write compressed file to compressed.txt
        startTime = System.currentTimeMillis();
        String compression = huffmanCodingTree.getCompression();
        endTime = System.currentTimeMillis();
        System.out.println("Time to encode file: " + (endTime-startTime) + " ms.");
//        System.out.println(compression);
        writeToFile("compressed.txt", compression);
        
        // output decompressed input
        System.out.println("Decompress");
        String decompression = huffmanCodingTree.getDecompression(compression);
        System.out.println(decompression);
        
        // multithread 
        int threadCount = 3;
        System.out.println("Multithread:");
        startTime = System.currentTimeMillis();
        HuffmanCodingTree huffmanCodingTreeMultithread = new HuffmanCodingTree(getFile(fileName), threadCount);
        endTime = System.currentTimeMillis();
        System.out.println("Time to generate huffman coding tree: " + (endTime-startTime) + " ms.");
        
        // write compressed file to compressed.txt
        startTime = System.currentTimeMillis();
        String compression2 = huffmanCodingTreeMultithread.getCompressionMultithread(threadCount);
        endTime = System.currentTimeMillis();
        System.out.println("Time to encode file: " + (endTime-startTime) + " ms.");
//        System.out.println(compression2);
        writeToFile("compressedMultithread.txt", compression2);
        
//         // output decompressed input       
        System.out.println("Decompress");
        String decompression2 = huffmanCodingTreeMultithread.getDecompression(compression2);
        System.out.println(decompression2);
        
        // calculate compression percentage
        double ofs = huffmanCodingTree.getCharacterCount();
        System.out.println("Original File Size: " + ofs+ " bytes.");
        double cfs = Math.ceil(compression.length()/8);
        System.out.println("Compressed File Size: " + cfs + " bytes.");
        System.out.println("File size compressed by " + 100*(ofs-cfs)/ofs + "%");
    }
    
    public static void writeToFile(String fileName, String data)
    {
        try
        {
            byte[] bytes = HuffmanCodingTree.binaryStringToByteArray(data);
            Path path = Paths.get(fileName);
            Files.write(path, bytes);
        }
        catch ( FileNotFoundException e )
        {
            System.out.println("File not found.\n" + e.getMessage());
        }
        catch ( IOException e )
        {
            System.out.println("IOException.\n" + e.getMessage());
        }
    }
    
    public static Scanner getFile(String fileName)
    {
        try
        {
            File inputFile = new File(fileName);
            Scanner scanner = new Scanner(inputFile);
            return scanner;
        }
        catch ( FileNotFoundException e )
        {
            System.out.println("File not found.\n" + e.getMessage());
        }
        return null;
    }
}
