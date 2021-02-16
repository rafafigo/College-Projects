using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using GStoreLib;

namespace PuppetMaster {
    class PupExec {

        private int rFactor = -1;
        public static readonly IDictionary<string, GStoreLib.Partition> SParts = new Dictionary<string, GStoreLib.Partition>();
        public static readonly IDictionary<string, Server> Servs = new Dictionary<string, Server>();
        public static readonly IDictionary<string, Client> Clients = new Dictionary<string, Client>();
        private bool sCreated = false;

        public async Task<string> CommandExec(string command) {
            var pArgs = command.Trim().Split();
            var res = $"$ {command}{Environment.NewLine}";

            switch (pArgs[0].ToUpper()) {

                case "REPLICATIONFACTOR":
                    ParseRF(pArgs);
                    break;

                case "PARTITION":
                    ParsePartition(pArgs);
                    break;

                case "SERVER":
                    ParseServer(pArgs);
                    break;

                case "CLIENT":
                    CheckServerCreation();
                    ParseClient(pArgs);
                    break;

                case "STATUS":
                    CheckServerCreation();
                    res += await DoStatusAsync(pArgs);
                    break;

                case "CRASH":
                    CheckServerCreation();
                    DoCrash(pArgs);
                    break;

                case "FREEZE":
                    CheckServerCreation();
                    DoFreeze(pArgs);
                    break;

                case "UNFREEZE":
                    CheckServerCreation();
                    DoUnfreeze(pArgs);
                    break;

                case "WAIT":
                    CheckServerCreation();
                    DoWait(pArgs);
                    break;

                case "": break;

                default:
                    Lib.Exit("Invalid Arg(s)!");
                    break;
            }
            return res + Environment.NewLine;
        }

        private void CheckServerCreation() {
            if (sCreated) return;

            foreach (var s in Servs) {
                var pcsFrontend = new PupFrontendPCS(GetPCSURL(s.Value.URL));
                _ = pcsFrontend.NewServerAsync(s.Key, s.Value.URL, s.Value.MinDelay, s.Value.MaxDelay);
            }
            sCreated = true;
        }

        private void ParseRF(string[] pArgs) {
            if (pArgs.Length != 2)
                Lib.Exit("Invalid Arg(s)!");
            rFactor = Convert.ToInt32(pArgs[1]);
        }

        private void ParsePartition(string[] pArgs) {
            var res = Lib.ParsePartition(pArgs);

            if (res.Value.IdServs.Count != rFactor)
                Lib.Exit("Invalid Arg(s)!");
            SParts.Add(res);
        }

        private void ParseServer(string[] pArgs) {
            if (pArgs.Length != 5)
                Lib.Exit("Invalid Arg(s)!");

            Servs.Add(pArgs[1], new Server(pArgs[2], Convert.ToInt32(pArgs[3]), Convert.ToInt32(pArgs[4])));
        }

        private void ParseClient(string[] pArgs) {
            if (pArgs.Length != 4)
                Lib.Exit("Invalid Arg(s)!");
            Clients.Add(pArgs[1], new Client(pArgs[2]));
            var pcsFrontend = new PupFrontendPCS(GetPCSURL(pArgs[2]));
            _ = pcsFrontend.NewClientAsync(pArgs[1], pArgs[2], pArgs[3]);
        }

        private async Task<string> DoStatusAsync(string[] pArgs) {
            var res = "";
            if (pArgs.Length != 1)
                Lib.Exit("Invalid Arg(s)!");

            foreach (var c in Clients) {
                if (!c.Value.GetAvailSync()) {
                    res += MapCode(c.Key, -1);
                    continue;
                }

                var code = await new PupFrontendNode(c.Key, c.Value.URL).StatusAsync();
                if (code < 0) Clients[c.Key].NegAvailSync();
                res += MapCode(c.Key, code);
            }
            foreach (var s in Servs) {
                if (!s.Value.GetAvailSync()) {
                    res += MapCode(s.Key, -1);
                    continue;
                }

                var code = await new PupFrontendNode(s.Key, s.Value.URL).StatusAsync();
                if (code < 0) Servs[s.Key].NegAvailSync();
                res += MapCode(s.Key, code);
            }
            return res;
        }

        private string MapCode(string key, int code) {
            return code switch
            {
                0 => $"<{key}>: Available{Environment.NewLine}",
                1 => $"<{key}>: Freezed{Environment.NewLine}",
                -1 => $"<{key}>: Crashed{Environment.NewLine}",
                _ => $"<{key}>: Unknown Code {Environment.NewLine}",
            };
        }

        private void DoCrash(string[] pArgs) {
            if (pArgs.Length != 2)
                Lib.Exit("Invalid Arg(s)!");
            _ = new PupFrontendNode(pArgs[1], Servs[pArgs[1]].URL).CrashAsync();
        }

        private void DoFreeze(string[] pArgs) {
            if (pArgs.Length != 2)
                Lib.Exit("Invalid Arg(s)!");
            _ = new PupFrontendNode(pArgs[1], Servs[pArgs[1]].URL).FreezeAsync();
        }

        private void DoUnfreeze(string[] pArgs) {
            if (pArgs.Length != 2)
                Lib.Exit("Invalid Arg(s)!");
            _ = new PupFrontendNode(pArgs[1], Servs[pArgs[1]].URL).UnfreezeAsync();
        }

        private void DoWait(string[] pArgs) {
            if (pArgs.Length != 2)
                Lib.Exit("Invalid Arg(s)!");
            Lib.Sleep(Convert.ToInt32(pArgs[1]));
        }

        private string GetPCSURL(string URL) {
            return $"http://{Lib.ParseURL(URL)[0]}:10000";
        }
    }
}
