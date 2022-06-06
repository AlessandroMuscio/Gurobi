import java.io.IOException;

public class ProvaLettura {
  public static void main(String[] args) {
    try {
      Reader reader = new Reader("assets/coppia81.txt");

      int[][] graph = reader.readFile();

      for (int i = 0; i < graph.length; i++) {
        for (int j = 0; j < graph.length; j++)
          System.out.printf("%d\t", graph[i][j]);

        System.out.println();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}