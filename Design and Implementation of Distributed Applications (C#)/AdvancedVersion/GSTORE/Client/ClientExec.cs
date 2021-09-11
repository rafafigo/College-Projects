using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using GStoreLib;

namespace ClientGStore {
    class ClientExec {
        private readonly string uname;
        private readonly string[] commands;
        private readonly ClientManager man;

        public ClientExec(string uname, string[] commands, IDictionary<string, Partition> sParts, IDictionary<string, Server> servs) {
            this.uname = uname;
            this.commands = commands;
            man = new ClientManager(sParts, servs);
        }

        public void Run() {
            var nRepeats = 0;
            var iRepeat = 0;
            var sPoint = 0;
            Lib.WriteLine($"Client <{uname}> Running:");
            for (var i = 0; i < commands.Length; i++) {

                var command = commands[i];

                if (iRepeat < nRepeats) {
                    command = command.Replace("$i", (iRepeat + 1).ToString());
                }
                var cmd = command.Trim().Split();

                switch (cmd[0].ToUpper()) {

                    case "READ":
                        if (cmd.Length != 4) Lib.Exit("Invalid Command!");
                        Read(cmd[1], cmd[2], cmd[3]);
                        break;

                    case "WRITE":
                        Match m = new Regex("\".*\"").Match(command);
                        if (!m.Success) Lib.Exit("Invalid Command!");
                        Write(cmd[1], cmd[2], m.Value.Replace("\"", ""));
                        break;

                    case "LISTSERVER":
                        if (cmd.Length != 2) Lib.Exit("Invalid Command!");
                        ListServer(cmd[1]);
                        break;

                    case "LISTGLOBAL":
                        if (cmd.Length != 1) Lib.Exit("Invalid Command!");
                        ListGlobal();
                        break;

                    case "WAIT":
                        if (cmd.Length != 2) Lib.Exit("Invalid Command!");
                        Lib.Sleep(Convert.ToInt32(cmd[1]));
                        break;

                    case "BEGIN-REPEAT":
                        if (cmd.Length != 2 || iRepeat < nRepeats) Lib.Exit("Invalid Command!");
                        sPoint = i;
                        iRepeat = 0;
                        nRepeats = Convert.ToInt32(cmd[1]);
                        break;

                    case "END-REPEAT":
                        if (cmd.Length != 1 || iRepeat > nRepeats) Lib.Exit("Invalid Command!");
                        if (++iRepeat < nRepeats) i = sPoint;
                        break;

                    case "": break;

                    default:
                        Lib.Exit("Invalid Command!");
                        break;
                }
            }
        }

        public void Read(string idPart, string idObj, string idServ) {

            Lib.WriteLine("Read:");
            var val = man.Read(idPart, idObj, idServ);
            if (val == "N/A") {
                Lib.WriteLine($"Not Succeed: {Lib.FormObj(idPart, idObj, val)}");
            } else {
                Lib.WriteLine($"Succeed: {Lib.FormObj(idPart, idObj, val)}");
            }
        }

        public void Write(string idPart, string idObj, string val) {
            Lib.WriteLine("Write:");
            if (man.Write(idPart, idObj, val) < 0) {
                Lib.WriteLine($"Not Succeed: {Lib.FormObj(idPart, idObj, val)}");
            } else {
                Lib.WriteLine($"Succeed: {Lib.FormObj(idPart, idObj, val)}");
            }
        }

        public void ListServer(string idServ) {
            Lib.WriteLine("ListServer:");
            Lib.WriteLine(ObjsToString(man.ListServer(idServ)));
        }

        public void ListGlobal() {
            Lib.WriteLine("ListGlobal:");
            foreach (var e in man.ListGlobal()) {
                Lib.WriteLine($"Server <{e.Key}>:");
                Lib.WriteLine(ObjsToString(e.Value));
            }
        }

        private string ObjsToString(IDictionary<string, string> objs) {
            return string.Join(Environment.NewLine, objs.Select(e => Lib.FormObj(e.Key, e.Value)));
        }
    }
}
