package com.napier.sem;

/* Imports the JUnit 5 Test annotation.
 @Test tells JUnit which methods should be executed as tests.*/
import org.junit.jupiter.api.Test;

/* Imports the assertDoesNotThrow assertion method.
 This checks that a piece of code runs without throwing an exception.*/
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

// Defines the test class for our main application (App.java).
class AppTest {

    /* Marks the following method as a JUnit test.
     Maven Surefire will automatically discover and run this test.*/
    @Test
    void applicationStartsSuccessfully() {

        /* Calls the main() method from the App class.
         new String[0] represents an empty array of command-line arguments.

         assertDoesNotThrow() checks that the main method
         executes without throwing any exceptions.

        If no exception occurs, the test PASSES.
        If an exception occurs, the test FAILS.*/
        assertDoesNotThrow(() -> App.main(new String[0]));
    }}