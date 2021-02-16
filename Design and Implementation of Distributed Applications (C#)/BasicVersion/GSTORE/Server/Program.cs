using Grpc.Core;
using System;
using GStoreLib;
using System.Collections.Generic;

namespace ServerGStore {

    class Program {

        private static string URL;
        private static int minDelay;
        private static int maxDelay;
        private static ServerManager man;

        static void Main() {
            // Enable Support For Unencrypted HTTP2  
            AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);

            ParseArgs();
            var hp = Lib.ParseURL(URL);
            var store = new ServerDomain();
            
            Grpc.Core.Server server = new Grpc.Core.Server {
                Services = {
                    GStoreServices.BindService(new GStoreServicesImpl(store, man, minDelay, maxDelay)),
                    GStoreSync.BindService(new GStoreSyncImpl(store, man, minDelay, maxDelay)),
                    PupSyncServices.BindService(new PupSyncServicesImpl(store, man))
                },
                Ports = { new ServerPort(hp[0], Convert.ToInt16(hp[1]), ServerCredentials.Insecure) }
            };
            server.Start();
            Lib.Block(URL);
        }
        
        static void ParseArgs() {
            var sArgs = Console.ReadLine().Trim().Split();
            if (sArgs.Length != 5 || !sArgs[0].ToUpper().Equals("SERVER"))
                Lib.Exit("Missing Arg(s)!");

            var sParts = new Dictionary<string, Partition>();
            var servs = new Dictionary<string, GStoreLib.Server>();
            Lib.ParseServsParts(sParts, servs);
            man = new ServerManager(sArgs[1], sParts, servs);
            URL = sArgs[2];
            minDelay = Convert.ToInt32(sArgs[3]);
            maxDelay = Convert.ToInt32(sArgs[4]);
        }
    }
}
