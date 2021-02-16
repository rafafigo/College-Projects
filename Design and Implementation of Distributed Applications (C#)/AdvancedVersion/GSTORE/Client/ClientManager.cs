using GStoreLib;
using System.Collections.Generic;
using System.Threading;

namespace ClientGStore {

    class ClientManager {
        public static IDictionary<string, Partition> SParts;
        public static IDictionary<string, Server> Servs;
        public static IDictionary<string, ClientFrontend> Frontends;
        public static IDictionary<string, PartitionInfo> PInfos;

        public ClientManager(IDictionary<string, Partition> sParts, IDictionary<string, Server> servs) {
            SParts = sParts;
            Servs = servs;
            Frontends = new Dictionary<string, ClientFrontend>();
            PInfos = new Dictionary<string, PartitionInfo>();

            foreach (var e in Servs) {
                Frontends.Add(e.Key, new ClientFrontend(e.Key, e.Value.URL));
            }
            foreach (var p in SParts) {
                PInfos.Add(p.Key, new PartitionInfo(p.Value.IdServs.Count));
            }
        }

        public string Read(string idPart, string idObj, string _) {

            var res = new List<KeyValuePair<int, string>>();

            foreach (var s in SParts[idPart].IdServs) {
                if (!Servs[s].GetAvailSync()) continue;
                new Thread(() => {
                    var r = Frontends[s].Read(idPart, idObj);
                    lock (res) {
                        if (r.Value != null) res.Add(r);
                        Monitor.Pulse(res);
                    }
                }).Start();
            }

            var r = 0;
            var maxTagVal = new KeyValuePair<int, string>(0, "N/A");
            bool CheckQuorum(int r) => r < PInfos[idPart].GetQuorum() || maxTagVal.Key < PInfos[idPart].Tag;

            lock (res) {
                if (PInfos[idPart].Avail == 0) return "N/A";
                do {
                    if (r == res.Count) Monitor.Wait(res);

                    for (; r < res.Count; r++)
                        if (res[r].Key > maxTagVal.Key) maxTagVal = res[r];

                } while (CheckQuorum(r) && PInfos[idPart].Avail > res.Count);
            }
            PInfos[idPart].Tag = maxTagVal.Key;
            return maxTagVal.Value;
        }

        public int Write(string idPart, string idObj, string val) {
            while (PInfos[idPart].Avail > 0) {
                var mid = FindMaster(idPart, idObj, val);
                if (mid.Value) return 0;
                if (mid.Key == null) continue;
                SParts[idPart].IdServs.Remove(mid.Key);
                SParts[idPart].IdServs.Add(mid.Key);
                var res = DoWrite(idPart, mid.Key, idObj, val);
                if (res == mid.Key) return 0;
            }
            return -1;
        }

        private KeyValuePair<string, bool> FindMaster(string idPart, string idObj, string val) {
            var mid = SParts[idPart].Mid;
            if (mid != null && Servs[mid].GetAvailSync()) return new KeyValuePair<string, bool>(mid, false);

            foreach (var s in SParts[idPart].IdServs) {
                if (!Servs[s].GetAvailSync()) continue;
                var res = DoWrite(idPart, s, idObj, val);
                if (res == s) return new KeyValuePair<string, bool>(res, true);
                if (res != null) return new KeyValuePair<string, bool>(res, false);
            }
            return new KeyValuePair<string, bool>(null, false);
        }

        private string DoWrite(string idPart, string idServ, string idObj, string val) {
            var res = Frontends[idServ].Write(idPart, idObj, val);
            SParts[idPart].Mid = res;
            return res;
        }

        public IDictionary<string, string> ListServer(string idServ) {

            if (!Servs.ContainsKey(idServ) || !Servs[idServ].GetAvailSync())
                return ListServerErrorOutput(idServ);

            var res = Frontends[idServ].ListServer();
            if (res == null) return ListServerErrorOutput(idServ);
            return res;
        }

        public IDictionary<string, IDictionary<string, string>> ListGlobal() {

            var res = new Dictionary<string, IDictionary<string, string>>();
            var thrs = new List<Thread>();

            foreach (var e in Servs) {
                res.Add(e.Key, new Dictionary<string, string>());
                var thr = new Thread(() => {
                    var lServs = ListServer(e.Key);
                    foreach (var le in lServs) lock (res[e.Key]) res[e.Key].Add(le);
                });
                thr.Start();
                thrs.Add(thr);
            }

            thrs.ForEach(thr => thr.Join());
            return res;
        }

        private IDictionary<string, string> ListServerErrorOutput(string idServ) {
            return new Dictionary<string, string> {{
                    $"Server <{idServ}>", "Not Available!"
                }};
        }
    }
}
