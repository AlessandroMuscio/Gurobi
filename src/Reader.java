import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
  private static FileReader fr;
  private static BufferedReader br;

  public Reader(String filePath) throws FileNotFoundException {
    fr = new FileReader(filePath);
    br = new BufferedReader(fr);
  }

  public int[][] readFile() throws IOException {
    String line;
    boolean eof = false;
    int[][] graph = new int[1][1];

    int i = 0;
    while (!eof) {
      line = br.readLine();

      if (i == 13) {
        int dim = Integer.parseInt(line.replaceAll("(?i)[a-z ]", ""));
        graph = new int[dim][dim];
      }

      if (i > 13)
        if (line != null) {
          String[] rowColVal = line.split(" ");
          int row = Integer.parseInt(rowColVal[0]);
          int col = Integer.parseInt(rowColVal[1]);
          int val = Integer.parseInt(rowColVal[2]);

          graph[row][col] = val;
          graph[col][row] = val;
        } else {
          eof = true;
        }
      i++;
    }

    return graph;
  }
}