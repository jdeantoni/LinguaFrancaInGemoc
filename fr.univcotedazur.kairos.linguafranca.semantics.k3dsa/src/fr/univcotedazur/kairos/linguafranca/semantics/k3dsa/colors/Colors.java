package fr.univcotedazur.kairos.linguafranca.semantics.k3dsa.colors;
public class Colors {

  public static final String RESET  = "\u001B[0m";

  public static final String BLACK  = "\u001B[30m";
  public static final String RED    = "\u001B[31m";
  public static final String GREEN  = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE   = "\u001B[34m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN   = "\u001B[36m";
  public static final String WHITE  = "\u001B[37m";

  public static final String BRIGHT_BLACK  = "\u001B[90m";
  public static final String BRIGHT_RED    = "\u001B[91m";
  public static final String BRIGHT_GREEN  = "\u001B[92m";
  public static final String BRIGHT_YELLOW = "\u001B[93m";
  public static final String BRIGHT_BLUE   = "\u001B[94m";
  public static final String BRIGHT_PURPLE = "\u001B[95m";
  public static final String BRIGHT_CYAN   = "\u001B[96m";
  public static final String BRIGHT_WHITE  = "\u001B[97m";

  public static final String[] FOREGROUNDS = {
    BLACK, RED, GREEN, YELLOW,
    BLUE, PURPLE, CYAN, WHITE,
    BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW,
    BRIGHT_BLUE, BRIGHT_PURPLE, BRIGHT_CYAN, BRIGHT_WHITE 
  };

  public static final String BG_BLACK  = "\u001B[40m";
  public static final String BG_RED    = "\u001B[41m";
  public static final String BG_GREEN  = "\u001B[42m";
  public static final String BG_YELLOW = "\u001B[43m";
  public static final String BG_BLUE   = "\u001B[44m";
  public static final String BG_PURPLE = "\u001B[45m";
  public static final String BG_CYAN   = "\u001B[46m";
  public static final String BG_WHITE  = "\u001B[47m";

  public static final String BRIGHT_BG_BLACK  = "\u001B[100m";
  public static final String BRIGHT_BG_RED    = "\u001B[101m";
  public static final String BRIGHT_BG_GREEN  = "\u001B[102m";
  public static final String BRIGHT_BG_YELLOW = "\u001B[103m";
  public static final String BRIGHT_BG_BLUE   = "\u001B[104m";
  public static final String BRIGHT_BG_PURPLE = "\u001B[105m";
  public static final String BRIGHT_BG_CYAN   = "\u001B[106m";
  public static final String BRIGHT_BG_WHITE  = "\u001B[107m";

  public static final String[] BACKGROUNDS = {
    BG_BLACK, BG_RED, BG_GREEN, BG_YELLOW,
    BG_BLUE, BG_PURPLE, BG_CYAN, BG_WHITE,
    BRIGHT_BG_BLACK, BRIGHT_BG_RED, BRIGHT_BG_GREEN, BRIGHT_BG_YELLOW,
    BRIGHT_BG_BLUE, BRIGHT_BG_PURPLE, BRIGHT_BG_CYAN, BRIGHT_BG_WHITE };

  public static void main(String[] args) {

    System.out.println("\n  Default text\n");

    for (String fg : FOREGROUNDS) {
      for (String bg : BACKGROUNDS)
        System.out.println(fg + bg + "  TEST  ");
      System.out.println(RESET);
    }

    System.out.println(RESET + "\n  Back to default.\n");

  }

}
