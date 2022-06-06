import java.io.IOException;

public class ProvaLettura {
  public static void main(String[] args) {
    try {
      Reader reader = new Reader("assets/coppia81.txt");

      InputData inputData = reader.readFile();

      System.out.println(inputData);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}