import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reader {
  private static FileReader fr;
  private static BufferedReader br;

  public Reader(String filePath) throws FileNotFoundException {
    fr = new FileReader(filePath);
    br = new BufferedReader(fr);
  }

  public InputData readFile() throws IOException {
    InputData inputData = new InputData();
    String line;
    boolean eof = false;
    String[] findCharRegex = {
        "(v[ ])",
        "(a[ ])",
        "(b[0-9])",
        "(c[ ])",
        "(d[0-9])",
        "(e[0-9])",
        "(f[0-9])",
        "(g[0-9])",
        "(h[0-9])",
        "(i[0-9])",
        "(l[ ])",
        "(V)"
    };
    String removeCharRegex = "(?i)[a-z=() ]";
    String removeCharNumberRegex = "([a-z][0-9])";
    Pattern pattern;
    Matcher matcher;

    while (!eof) {
      line = br.readLine();

      if (line != null) {

        if (inputData.costi == null) {
          for (int i = 0; i < findCharRegex.length; i++) {
            pattern = Pattern.compile(findCharRegex[i]);
            matcher = pattern.matcher(line);

            if (matcher.find()) {
              String tmp;
              String[] sc;
              int dim;

              switch (i) {
                case 0:
                  tmp = line.replaceAll(removeCharRegex, "");
                  inputData.v = Integer.parseInt(tmp);
                  break;

                case 1:
                  tmp = line.replaceAll(removeCharRegex, "");
                  inputData.a = Integer.parseInt(tmp);
                  break;

                case 2:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.b1b2 = new int[2];
                  inputData.b1b2[0] = Integer.parseInt(sc[1]);
                  inputData.b1b2[1] = Integer.parseInt(sc[2]);
                  break;

                case 3:
                  tmp = line.replaceAll(removeCharRegex, "");
                  inputData.c = Integer.parseInt(tmp);
                  break;

                case 4:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.d1d2 = new int[2];
                  inputData.d1d2[0] = Integer.parseInt(sc[1]);
                  inputData.d1d2[1] = Integer.parseInt(sc[2]);
                  break;

                case 5:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.e1e2 = new int[2];
                  inputData.e1e2[0] = Integer.parseInt(sc[1]);
                  inputData.e1e2[1] = Integer.parseInt(sc[2]);
                  break;

                case 6:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.f1f2 = new int[2];
                  inputData.f1f2[0] = Integer.parseInt(sc[1]);
                  inputData.f1f2[1] = Integer.parseInt(sc[2]);
                  break;

                case 7:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.g1g2 = new int[2];
                  inputData.g1g2[0] = Integer.parseInt(sc[1]);
                  inputData.g1g2[1] = Integer.parseInt(sc[2]);
                  break;

                case 8:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.h1h2 = new int[2];
                  inputData.h1h2[0] = Integer.parseInt(sc[1]);
                  inputData.h1h2[1] = Integer.parseInt(sc[2]);
                  break;

                case 9:
                  tmp = line.replaceAll(removeCharNumberRegex, "");
                  tmp = tmp.replaceAll(removeCharRegex, "");
                  sc = tmp.split(",");
                  inputData.i1i2 = new int[2];
                  inputData.i1i2[0] = Integer.parseInt(sc[1]);
                  inputData.i1i2[1] = Integer.parseInt(sc[2]);
                  break;

                case 10:
                  tmp = line.replaceAll(removeCharRegex, "");
                  inputData.l = Integer.parseInt(tmp);
                  break;

                case 11:
                  tmp = line.replaceAll(removeCharRegex, "");
                  dim = Integer.parseInt(tmp);
                  inputData.N = dim;
                  inputData.costi = new int[dim][dim];
                  break;
              }

              i = findCharRegex.length;
            }
          }
        } else {
          String[] rowColVal = line.split(" ");
          int row = Integer.parseInt(rowColVal[0]);
          int col = Integer.parseInt(rowColVal[1]);
          int val = Integer.parseInt(rowColVal[2]);

          inputData.costi[row][col] = val;
          inputData.costi[col][row] = val;
        }
      } else {
        eof = true;
      }

      /*
       * if (i == 13) {
       * int dim = Integer.parseInt(line.replaceAll("(?i)[a-z ]", ""));
       * graph = new int[dim][dim];
       * }
       * 
       * if (i > 13)
       * if (line != null) {
       * String[] rowColVal = line.split(" ");
       * int row = Integer.parseInt(rowColVal[0]);
       * int col = Integer.parseInt(rowColVal[1]);
       * int val = Integer.parseInt(rowColVal[2]);
       * 
       * graph[row][col] = val;
       * graph[col][row] = val;
       * } else {
       * eof = true;
       * }
       * i++;
       */
    }

    return inputData;
  }
}