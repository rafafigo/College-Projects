using System;
using System.Diagnostics;
using System.IO;
using Google.Protobuf.Collections;

namespace PCS {
    class PCSManager {

        public static string ServerHeader(string idServ, string URL, int minDelay, int maxDelay) {
            return string.Format("SERVER {0} {1} {2} {3}{4}", idServ, URL, minDelay, maxDelay, Environment.NewLine);
        }

        public static string ClientHeader(string uname, string URL, string script) {
            return string.Format("CLIENT {0} {1} {2}{3}", uname, URL, script, Environment.NewLine);
        }

        public static string Partitions(RepeatedField<Partition> sParts) {
            var res = "";
            foreach (var p in sParts) {
                res += Partition(p.IdPart, p.IdServs);
            }
            return res;
        }

        public static string Partition(string idPart, RepeatedField<string> idServs) {
            var res = string.Format("PARTITION {0} {1}", idServs.Count, idPart);

            foreach (var s in idServs) {
                res += string.Format(" {0}", s);
            }
            return string.Format("{0}{1}", res, Environment.NewLine);
        }

        public static string Servers(MapField<string, string> servs) {
            var res = "";

            foreach (var s in servs) {
                res += string.Format("SERVER {0} {1}{2}", s.Key, s.Value, Environment.NewLine);
            }
            return res;
        }

        public static void NewProcess(string exePath, string input) {

            var tmp = Path.GetTempFileName();
            using (var sw = new StreamWriter(tmp)) sw.Write(input);
            var psi = new ProcessStartInfo("CMD.exe") {
                Arguments = string.Format("/K CALL {0} < {1}", Path.GetFullPath(exePath), tmp),
                UseShellExecute = true
            };
            Process.Start(psi);
        }
    }
}
