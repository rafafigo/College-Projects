using System;
using System.Collections.Generic;
using System.IO;
using Grpc.Core;
using GStoreLib;

namespace ClientGStore {
    class Program {

        private static ClientExec exec;
        private static string URL;

        static void Main() {
            // Enable Support For Unencrypted HTTP2
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
            ParseArgs();

            var hp = Lib.ParseURL(URL);
            Grpc.Core.Server server = new Grpc.Core.Server {
                Services = {
                    PupSyncServices.BindService(new PupSyncServicesImpl())
                },
                Ports = { new ServerPort(hp[0], Convert.ToInt16(hp[1]), ServerCredentials.Insecure) }
            };
            server.Start();

            try {
                exec.Run();
            } catch (Exception e) when (e is OverflowException || e is FormatException) {
                Lib.Exit("Invalid Argument!");
            } catch (ClientException e) {
                Lib.Exit(e.Message);
            }
            Lib.Block(URL);
        }

        static void ParseArgs() {
            var cArgs = Console.ReadLine().Trim().Split();
            if (cArgs.Length != 4 || !cArgs[0].ToUpper().Equals("CLIENT"))
                Lib.Exit("Missing Arg(s)!");

            if (!File.Exists(cArgs[3]))
                Lib.Exit($"File {cArgs[3]} does not Exist!");

            URL = cArgs[2];
            var sParts = new Dictionary<string, Partition>();
            var servs = new Dictionary<string, GStoreLib.Server>();
            Lib.ParseServsParts(sParts, servs);
            exec = new ClientExec(cArgs[1], File.ReadAllLines(cArgs[3]), sParts, servs);
        }
    }
}
