/*
This program is used to count and rank the most commonly-used words in a text document.
Ryan Wells
11/12/2017
*/
import java.util.*;
import java.io.*;


public class HashTable{

   private int[] primeSize = {97,197,397,797,1597,2999,6007,12007,25097,49297,98297,196593,393977};
   private int primeIndex = 0;
   int capacity = 0;
   int count = 1;
   
      
   //My hash function.
   public int hash(String word){
      int index = 0;
      for(int i = 0;i < word.length(); i++){
         index += word.charAt(i) - 'a' + 1;     
      }   
      index = ((word.length()) +  word.charAt(0) + index ) % primeSize[primeIndex]; 
      if(index < 0){
         index = index * -1;
      }
      return index;
   }
   

   //the LinkedList class I made
   public class LinkedList{
      private LinkedList next;  
      private DataItem word;
      
      public LinkedList(DataItem word, LinkedList next) {
         this.word = word;
         this.next = next;
      }
   }
   
   //An empty hash table.   
   LinkedList[] table = new LinkedList[primeSize[primeIndex]];
     
     
   //Inserts a word into the hash table, or increments a repeating word's occurence count.
   public void add(String word){
      
      //if we reach 50% capacity, rehash into a larger table
      if(capacity >= (primeSize[primeIndex] / 2)){
         rehash();
      }   
      
      word = word.toLowerCase();
      int index = hash(word);
      DataItem newItem = new DataItem(word);  
   
      //if index is empty, add it to the beginning of the linked list;
      if(table[index] == null){
         table[index] = new LinkedList(newItem, null);
         table[index].word.count = count;
         count = 1;
         capacity++;
         return;
      }         
         
      //if the word at index is not null, add the new word to the final link in the linked list unless it is a duplicate word.
      if(table[index] != null){     
         LinkedList pointer = table[index];
            
         //check to see if the first link has the same word:
         if(table[index].word.word == newItem.word){
            table[index].word.count += 1;
            pointer = table[index];
            return;
         }
            
         //check each link in the array to find any duplicate
         while(pointer.next != null){            
            if(pointer.word.word.equals(newItem.word)){
               pointer.word.count += 1;
               pointer = table[index];
               return;
            }
            pointer = pointer.next;
         }       
          
         //now pointer.next is null and no duplicate was found. Add newItem to the end of the list.
         pointer.next = new LinkedList(newItem, null);
         capacity++;
         pointer.next.word.count = count;
         count = 1;
      }
       
      //check to make sure each word is in only one link (this speeds up the program and ensures accuracy)
      LinkedList checker = table[index];       
      while(checker.next != null){
         if(checker.word.word.equals(checker.next.word.word)){   
            if(checker.next.next == null){
               checker.word.count += 1;
               checker.next = null;
               capacity--;
               return;
            }
         }
         checker = checker.next;
      }     
   }
  

   //Rehashes the table into one that is (roughly) twice as large
   public void rehash(){
      DataItem[] oldTable = extractAll();
      primeIndex++;
      int newSize = primeSize[primeIndex];
      
      //clear table and increase it's size.
      table = new LinkedList[newSize];                                 
      
      //iterate through the array oldTable and add the DataItems to the new table, manually setting the count of each word
      for(int i = 0; i < oldTable.length; i++){
         capacity--;
         count = oldTable[i].count;
         add(oldTable[i].word); 
      }
   }


   //This method is used to make an array filled with the dataitems in the given hashtable, so that they can be rehashed into a larger table
   DataItem[] extractAll(){
      int newSize = capacity;
      int index = 0;
      DataItem[]result = new DataItem[newSize];
      
      for(int i = 0; i < primeSize[primeIndex]; i++){
         LinkedList current = table[i];
                
         if(current != null){
            while(current != null){
               if(current.word != null){
                  result[index] = current.word;
                  index++;
                  current = current.next;
               }
               else{
                  current = current.next;
               }
            }
         }
      }
      return result;
   }


   //Used to delete a word from the hash table
   public void delete(String word){
      int index = hash(word);
      LinkedList locator = table[index];
      if(locator == null){
         return;
      } 
      while(locator.word != null){
         if(word.equals(locator.word.word)){         
            //do the deleting.         
            if(locator.next == null){
               locator.word = null;    
            }           
            if(locator.next != null){
               locator.word = locator.next.word;              
            
               //shift links around
               while(locator.next.next != null){                  
                  locator.next.word = locator.next.next.word;      
                  locator = locator.next;
               }
               if(locator.next.next == null){
                  locator.next.word = null;
               }
            }        
            else{
               locator.word = null;
            }
         }      
         if(locator.next == null){
            return;
         }
         if(locator.next != null){  
            locator = locator.next;
         }
      }
   }
   

   //Returns the DataItem containing the word that occurrs the most
   public DataItem highcount(){
      DataItem biggest = new DataItem("_____");
      biggest.count = 0;
   
      //iterate through the array and linked lists
      for(int i = 0; i < primeSize[primeIndex]; i++){
         LinkedList highCounter = table[i];
      
         //if we find a higher count, update biggest
         while(highCounter != null){       
            if(highCounter.word != null){
               if(highCounter.word.count > biggest.count){
                  biggest = highCounter.word;                     
               }         
            }     
            highCounter = highCounter.next;
         }
      }   
      return biggest;
   }
   
   //adds each word in the provided .txt file to the hash table
   public static void addWords(String filename, HashTable table) {
   
      BufferedReader in;
      try{
         in = new BufferedReader(new FileReader(filename));
      }
      catch(Exception e){
         System.out.println("Cannot open file " + filename);
         return;
      }
   
   	//read each line of the file
      try{
         String line = in.readLine();
         while(line != null){
         	
         	//tokenize the line to ignore punctuation
            String delimiters = " ,;.!@#$%^&*()_+-=<>?[]{}:\"'";
            StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
         
         	//add each word to the hash table
            while(tokenizer.hasMoreTokens()){
               String word = tokenizer.nextToken();
               table.add(word);
            }
            
         	//get the next line from the file
            line = in.readLine();
         }
      }		
      catch (Exception e) {
         System.out.println("I/O error: " + e);
      }	
   }
   
	
   //main method
   public static void main(String[] args) {
   	
      int howmany;
      try{
         howmany = Integer.parseInt(args[0]);
      } 
      catch(Exception e){
         System.out.println("Please provide the number of words you want, followed by the .txt file(s)");
         return;
      }
   	
   	//create the hash table
      HashTable table = new HashTable();
   	
   	//for each file specified in a command-line argument, extract each word and add it to the hash table
      for(int file = 1; file < args.length; file++){
         addWords(args[file], table);
      }
   	
   	//display the top howmany words
      for(int i = 1; i <= howmany; i++){
         DataItem data = table.highcount();
         if(data == null){
            return;
         }
         System.out.println(data.word + ": " + data.count);
         table.delete(data.word);
      }
   }

}
   
   
