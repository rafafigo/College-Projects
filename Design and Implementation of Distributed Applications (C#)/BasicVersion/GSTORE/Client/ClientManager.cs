using GStoreLib;
using System.Collections.Generic;
using System.Threading;

namespace ClientGStore {
    class ClientManager {
        public static readonly string NA = "N/A";
        public static readonly string ANY = "-1";

        private IDictionary<string, ClientFrontend> fronts = new Dictionary<string, ClientFrontend>();
        private ClientFrontend frontend;
        public static IDictionary<string, Partition> SParts;
        public static IDictionary<string, Server> Servs;

        public ClientManager(IDictionary<string, Partition> sParts, IDictionary<string, Server> servs) {
            SParts = sParts;
            Servs = servs;

            foreach (var s in servs) fronts.Add(s.Key, new ClientFrontend(s.Key, s.Value.URL));
        }

        public string Read(string idPart, string idObj, string idServ) {

            var sReq = new List<string>();

            if (!CheckAvailFrontend() || !SParts[idPart].IdServs.Contains(frontend.IdServ)) {
                frontend = null;
            }

            if (frontend != null) {
                var res = DoRead(idPart, idObj);
                if (!res.Equals(NA)) return res;
                sReq.Add(frontend.IdServ);
            }

            if (idServ != ANY &&
                !sReq.Contains(idServ) &&
                SParts[idPart].IdServs.Contains(idServ) &&
                Servs[idServ].GetAvailSync()) {

                frontend = fronts[idServ];
                var res = DoRead(idPart, idObj);
                if (!res.Equals(NA)) return res;
                sReq.Add(idServ);
            }

            foreach (var s in SParts[idPart].IdServs) {
                if (sReq.Contains(s) || !Servs[s].GetAvailSync()) continue;

                frontend = fronts[s];
                var res = DoRead(idPart, idObj);
                if (!res.Equals(NA)) return res;
            }
            return NA;
        }

        private string DoRead(string idPart, string idObj) {
            var res = frontend.Read(idPart, idObj);
            return res ?? NA;
        }

        public int Write(string idPart, string idObj, string val) {

            var mid = SParts[idPart].Mid;

            if (!Servs[mid].GetAvailSync()) return -1;

            if (!CheckAvailFrontend() || frontend.IdServ != mid) {
                frontend = fronts[mid];
            }

            return frontend.Write(idPart, idObj, val);
        }

        public IDictionary<string, string> ListServer(string idServ) {

            if (!Servs[idServ].GetAvailSync()) {
                return ListServerErrorOutput(idServ);
            }

            var front = frontend;
            if (!CheckAvailFrontend() || frontend.IdServ != idServ) {
                front = fronts[idServ];
            }

            var res = front.ListServer();
            if (res == null) {
                return ListServerErrorOutput(idServ);
            }
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

        private bool CheckAvailFrontend() {
            return frontend != null && Servs[frontend.IdServ].GetAvailSync();
        }

        private IDictionary<string, string> ListServerErrorOutput(string idServ) {
            return new Dictionary<string, string> {{
                    string.Format("Server <{0}>", idServ), "Not Available!"
                }};
        }
    }
}
