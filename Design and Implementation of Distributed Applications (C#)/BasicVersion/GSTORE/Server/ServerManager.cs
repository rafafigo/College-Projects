using System;
using System.Collections.Generic;
using System.Threading;
using GStoreLib;

namespace ServerGStore {
    public class ServerManager {
        private readonly string id;
        public bool IsFreeze { get; set; }
        private readonly object freezeLock;
        public static IDictionary<string, Partition> SParts;
        public static IDictionary<string, Server> Servs;
        private IDictionary<string, ServerFrontend> fronts = new Dictionary<string, ServerFrontend>();

        public ServerManager(string id, IDictionary<string, Partition> sParts, IDictionary<string, Server> servs) {
            this.id = id;
            SParts = sParts;
            Servs = servs;
            IsFreeze = false;
            freezeLock = new object();

            foreach (var s in servs) fronts.Add(s.Key, new ServerFrontend(s.Key, s.Value.URL));
        }

        public bool WritePermission(string idPart) {
            return SParts[idPart] != null && SParts[idPart].Mid == id;
        }

        public void Lock(string idPart, string idObj) {
            CallServers(idPart, (idServ, URL) => {
                fronts[idServ].Lock(idPart, idObj);
            });
        }

        public void Update(string idPart, string idObj, string val) {
            CallServers(idPart, (idServ, URL) => {
                fronts[idServ].Update(idPart, idObj, val);
            });
        }

        public void CallServers(string idPart, Action<string, string> act) {
            var thrs = new List<Thread>();
            foreach (var s in SParts[idPart].IdServs) {
                if (s == id || !Servs[s].GetAvailSync()) continue;

                var thr = new Thread(() => act(s, Servs[s].URL));
                thr.Start();
                thrs.Add(thr);
            }
            thrs.ForEach(thr => thr.Join());
        }

        public void CheckFreeze() {
            lock (freezeLock) {
                while (IsFreeze) Monitor.Wait(freezeLock);
            }
        }

        public void Freeze() {
            lock (freezeLock) IsFreeze = true;
        }

        public void Unfreeze() {
            lock (freezeLock) {
                IsFreeze = false;
                Monitor.PulseAll(freezeLock);
            }
        }
    }
}
