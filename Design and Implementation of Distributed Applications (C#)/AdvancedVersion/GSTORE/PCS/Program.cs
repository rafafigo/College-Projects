using System;
using Grpc.Core;
using GStoreLib;

namespace PCS {
    class Program {

        private static string URL;
        private static string exeServer;
        private static string exeClient;

        static void Main() {
            // Enable Support For Unencrypted HTTP2  
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);

            ParseArgs();
            var hp = Lib.ParseURL(URL);

            Grpc.Core.Server server = new Grpc.Core.Server {
                Services = {
                    PCSServices.BindService(new PCSServicesImpl(exeServer, exeClient))
                },
                Ports = { new ServerPort(hp[0], Convert.ToInt16(hp[1]), ServerCredentials.Insecure) }
            };
            server.Start();
            Lib.Block(URL);
        }

        static void ParseArgs() {

            for (var i = 0; i < 3; i++) {

                var pArgs = Console.ReadLine().Trim().Split();
                if (pArgs.Length != 2)
                    Lib.Exit("Missing Arg(s)!");

                switch (pArgs[0].ToUpper()) {

                    case "PCS":
                        URL = pArgs[1];
                        break;

                    case "SERVER":
                        exeServer = pArgs[1];
                        break;

                    case "CLIENT":
                        exeClient = pArgs[1];
                        break;

                    case "":
                        i--;
                        break;

                    default:
                        Lib.Exit("Invalid Arg(s)!");
                        break;
                }
            }
        }
    }
}
