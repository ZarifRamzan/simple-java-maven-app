package com.mycompany.app;

/** Prints a fixed greeting and exposes it to callers. */
public class App {

  private static final String MESSAGE = "Hello World!";

  /** Creates the application. */
  public App() {}

  /**
   * Entry point: prints the greeting to standard output.
   *
   * @param args command-line arguments (ignored)
   */
  public static void main(String[] args) {
    System.out.println(MESSAGE);
  }

  /**
   * Returns the greeting.
   *
   * @return the greeting text
   */
  public String getMessage() {
    return MESSAGE;
  }
}
