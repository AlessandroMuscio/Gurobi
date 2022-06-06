public class InputData {
  public int v;
  public int a;
  public int[] b1b2;
  public int c;
  public int[] d1d2;
  public int[] e1e2;
  public int[] f1f2;
  public int[] g1g2;
  public int[] h1h2;
  public int[] i1i2;
  public int l;
  public int[][] graph;

  @Override
  public String toString() {
    StringBuffer out = new StringBuffer();

    out.append(String.format("v = %d\n", v));
    out.append(String.format("a = %d\n", a));
    out.append(String.format("(b1, b2) = (%d, %d)\n", b1b2[0], b1b2[1]));
    out.append(String.format("c = %d\n", c));
    out.append(String.format("(d1, d2) = (%d, %d)\n", d1d2[0], d1d2[1]));
    out.append(String.format("(e1, e2) = (%d, %d)\n", e1e2[0], e1e2[1]));
    out.append(String.format("(f1, f2) = (%d, %d)\n", f1f2[0], f1f2[1]));
    out.append(String.format("(g1, g2) = (%d, %d)\n", g1g2[0], g1g2[1]));
    out.append(String.format("(h1, h2) = (%d, %d)\n", h1h2[0], h1h2[1]));
    out.append(String.format("(i1, i2) = (%d, %d)\n", i1i2[0], i1i2[1]));
    out.append(String.format("l = %d\n", l));

    out.append("\nGrafo\n");
    for (int i = 0; i < graph.length; i++) {
      StringBuffer line = new StringBuffer();

      for (int j = 0; j < graph[i].length; j++)
        if (j != (graph[i].length - 1))
          line.append(String.format("%d\t", graph[i][j]));
        else
          line.append(String.format("%d", graph[i][j]));

      line.append("\n");
      out.append(line);
    }

    return out.toString();
  }
}
