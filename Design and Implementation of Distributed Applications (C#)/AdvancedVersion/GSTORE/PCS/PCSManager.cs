using System;
using System.Diagnostics;
using System.IO;
using Google.Protobuf.Collections;

namespace PCS {
    class PCSManager {

        public static string ServerHeader(string idServ, string URL, int minDelay, int maxDelay) {
            return $"SERVER {idServ} {URL} {minDelay} {maxDelay}{Environment.NewLine}";
        }

        public static string ClientHeader(string uname, string URL, string script) {
            return $"CLIENT {uname} {URL} {script}{Environment.NewLine}";
        }

        public static string Partitions(RepeatedField<Partition> sParts) {
            var res = "";
            foreach (var p in sParts) {
                res += Partition(p.IdPart, p.IdServs);
            }
            return res;
        }

        public static string Partition(string idPart, RepeatedField<string> idServs) {
            var res = $"PARTITION {idServs.Count} {idPart}";

            foreach (var s in idServs) {
                res += $" {s}";
            }
            return $"{res}{Environment.NewLine}";
        }

        public static string Servers(MapField<string, string> servs) {
            var res = "";

            foreach (var s in servs) {
                res += $"SERVER {s.Key} {s.Value}{Environment.NewLine}";
            }
            return res;
        }

        public static void NewProcess(string exePath, string input) {
            var tmp = Path.GetTempFileName();
            using (var sw = new StreamWriter(tmp)) sw.Write(input);
            var psi = new ProcessStartInfo("CMD.exe") {
                Arguments = $"/K CALL {Path.GetFullPath(exePath)} < {tmp}",
                UseShellExecute = true
            };
            Process.Start(psi);
        }
    }
}
