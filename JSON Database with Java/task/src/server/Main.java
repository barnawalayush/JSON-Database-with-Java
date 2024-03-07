package server;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String[] database = new String[100];
        Arrays.fill(database, "");

        Scanner sc = new Scanner(System.in);

        while(sc.hasNext()){
            String inputCommand = sc.nextLine();

            if(inputCommand.equals("exit"))break;

            String[] input = inputCommand.split(" ");
            String command = input[0];
            int value = Integer.parseInt(input[1])-1;

            if(command.equals("get")){
                if(database[value].isEmpty()){
                    System.out.println("Error");
                }else{
                    System.out.println(database[value]);
                }
            } else if(command.equals("set")){
                if(value < 0 || value >= 100){
                    System.out.println("Error");
                }else{
                    StringBuilder content = new StringBuilder();
                    int ind = 2;
                    while(ind < input.length){
                        content.append(input[ind]);
                        if(ind != input.length-1){
                            content.append(" ");
                        }
                        ind++;
                    }
                    database[value] = content.toString();
                    System.out.println("OK");
                }
            } else if(command.equals("delete")){
                if(value < 0 || value >= 100){
                    System.out.println("Error");
                }else{
                    database[value] = "";
                    System.out.println("OK");
                }
            }

        }

    }
}
