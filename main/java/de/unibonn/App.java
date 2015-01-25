package de.unibonn;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.Console;
import java.util.StringTokenizer;

public class App {
  public static void main(String[] args) {
    OptionParser parser = new OptionParser();
    parser.accepts("listen").withRequiredArg().defaultsTo("127.0.0.1").ofType(String.class);
    parser.accepts("join").withRequiredArg();
    parser.accepts("help").forHelp();
    final OptionSet opts = parser.parse(args);

    if (opts.hasArgument("help")) {
      System.out.println("usage: pdc --listen 127.0.0.1 --join 127.0.0.2");
    } else {
      Node n = new Node((String) opts.valueOf("listen"));
      n.start();
      System.out.println(opts.asMap());
      Console console = System.console();
      String command;
      if (console != null) {
        boolean run = true;
        while (run) {
          command = console.readLine("> ");
          if (command.isEmpty()) continue;
          StringTokenizer st = new StringTokenizer(command);
          switch (st.nextToken()) {
            case "quit":
              run = false;
              n.stop();
              break;
            case "join":
              if (!st.hasMoreTokens()) {
                console.format("Please specify IP address to join.\n");
              } else {
                n.join(st.nextToken());
              }
              break;
            case "start":
              n.startCalculation();
              break;
            case "signoff":
              n.leave();
              break;
            case "algorithm":
              if (!st.hasMoreTokens()) {
                console.format("Please select algorithm to use (tr|ra).\n");
              } else {
                String alg = st.nextToken();
                if (alg.equals("ra")) {
                  n.setAlg(new RicartAgrawala(n, true));
                } else if (alg.equals("tr")) {
                  n.setAlg(new TokenRing());
                } else {
                  console.format("Invalid algorithm %s. Available options: tr, ra.\n", alg);
                }
              }
              break;
            case "show":
              System.out.println(n.network.toString());
              break;
            default:
              console.format("Unknown command\n");
              break;
          }
        }
      } else {
        System.out.println("No Console");
      }
    }
  }
}
