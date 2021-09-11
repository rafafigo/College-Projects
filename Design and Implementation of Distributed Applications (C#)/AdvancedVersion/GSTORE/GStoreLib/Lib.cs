using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace GStoreLib {
    public class Lib {

        public static string[] ParseURL(string URL) {
            return URL.Split("://")[1].Split(":");
        }

        public static void ParseServsParts(IDictionary<string, Partition> sParts, IDictionary<string, Server> servs) {

            while (true) {
                var line = Console.ReadLine();
                if (line == null) break;

                var pArgs = line.Trim().Split();
                if (pArgs.Length == 0) break;

                switch (pArgs[0].ToUpper()) {

                    case "PARTITION":
                        sParts.Add(ParsePartition(pArgs));
                        break;

                    case "SERVER":
                        servs.Add(ParseServer(pArgs));
                        break;

                    case "": break;

                    default:
                        Exit("Invalid Arg(s)!");
                        break;
                }
            }
        }

        public static KeyValuePair<string, Partition> ParsePartition(string[] pArgs) {
            if (Convert.ToInt32(pArgs[1]) + 3 != pArgs.Length)
                Exit("Invalid Arg(s)!");

            var idServs = new List<string>();
            for (var i = 3; i < pArgs.Length; i++) idServs.Add(pArgs[i]);

            return new KeyValuePair<string, Partition>(
                pArgs[2],
                new Partition(idServs[0], idServs)
            );
        }

        public static KeyValuePair<string, Server> ParseServer(string[] pArgs) {
            if (pArgs.Length != 3)
                Exit("Invalid Arg(s)!");

            return new KeyValuePair<string, Server>(
                pArgs[1],
                new Server(pArgs[2])
            );
        }

        public static string FormObjKey(string idPart, string idObj) {
            return $"<{idPart}, {idObj}>";
        }

        public static string FormObj(string idPart, string idObj, string val) {
            return $"<{idPart}, {idObj}>: {val}";
        }

        public static string FormObj(string key, string val) {
            return $"{key}: {val}";
        }

        public static void Block(string URL) {
            WriteLine($"Listening on {URL}! (...)");
            Sleep(Timeout.Infinite);
        }

        public static void Sleep(int millis) {
            if (millis != 0) Task.Run(async () => { await Task.Delay(millis); }).Wait();
        }

        public static void WriteLine(string msg) {
            // WriteLine in a New Thread in Order To Not Be Blocked!
            new Thread(() => { Console.WriteLine(msg); }).Start();
        }

        public static void Debug(string msg) {
            if (Environment.GetEnvironmentVariable("DEBUG") != null) WriteLine($"Debug: {msg}");
        }

        public static void Exit(string msg) {
            WriteLine(msg);
            Environment.Exit(1);
        }
    }
}
