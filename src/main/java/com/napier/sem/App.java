package com.napier.sem;

import java.sql.SQLException;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== CAPITAL CITY REPORTS =====");
            System.out.println("1. All capital cities worldwide");
            System.out.println("2. Capital cities by continent");
            System.out.println("3. Capital cities by region");
            System.out.println("4. Top N capital cities worldwide");
            System.out.println("5. Top N capital cities by continent");
            System.out.println("6. Top N capital cities by region");
            System.out.println("0. Exit");
            System.out.print("Choose a report: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("Exiting Semcode.");
                break;
            }

            try {
                switch (choice) {
                    case "1":
                        CapitalCityReports.allWorld();
                        break;
                    case "2":
                        System.out.print("Enter continent: ");
                        CapitalCityReports.byContinent(scanner.nextLine().trim());
                        break;
                    case "3":
                        System.out.print("Enter region: ");
                        CapitalCityReports.byRegion(scanner.nextLine().trim());
                        break;
                    case "4":
                        System.out.print("Enter number of cities: ");
                        CapitalCityReports.topNWorld(
                                Integer.parseInt(scanner.nextLine().trim()));
                        break;
                    case "5":
                        System.out.print("Enter continent: ");
                        String continent = scanner.nextLine().trim();
                        System.out.print("Enter number of cities: ");
                        int continentLimit =
                                Integer.parseInt(scanner.nextLine().trim());
                        CapitalCityReports.topNByContinent(
                                continent, continentLimit);
                        break;
                    case "6":
                        System.out.print("Enter region: ");
                        String region = scanner.nextLine().trim();
                        System.out.print("Enter number of cities: ");
                        int regionLimit =
                                Integer.parseInt(scanner.nextLine().trim());
                        CapitalCityReports.topNByRegion(region, regionLimit);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
