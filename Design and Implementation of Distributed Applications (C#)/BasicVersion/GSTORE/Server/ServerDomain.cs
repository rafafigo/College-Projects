using System.Threading;
using System.Collections.Generic;
using GStoreLib;

namespace ServerGStore {
    public class ServerDomain {

        private readonly IDictionary<string, Obj> objs;

        public ServerDomain() {
            objs = new Dictionary<string, Obj>();
        }

        public string Read(string idPart, string idObj) {

            var key = Lib.FormObjKey(idPart, idObj);

            lock (objs) {
                if (!objs.ContainsKey(key)) {
                    return "N/A";
                }
            }

            lock (objs[key]) {
                while (objs[key].Lock) Monitor.Wait(objs[key]);
                Monitor.Pulse(objs[key]);
                return objs[key].Value;
            }
        }

        public void Write(string idPart, string idObj, string value) {

            var key = Lib.FormObjKey(idPart, idObj);

            lock (objs[key]) {
                objs[key].Value = value;
            }
            Unlock(key);
        }

        public void Lock(string idPart, string idObj) {

            var key = Lib.FormObjKey(idPart, idObj);

            lock (objs) {
                if (!objs.ContainsKey(key)) {
                    objs.Add(key, new Obj(null));
                }
            }

            lock (objs[key]) {
                while (objs[key].Lock) Monitor.Wait(objs[key]);
                objs[key].Lock = true;
            }
        }

        public void Unlock(string key) {

            lock (objs[key]) {
                objs[key].Lock = false;
                Monitor.Pulse(objs[key]);
            }
        }

        public IDictionary<string, Obj> ListObjs() {
            return objs;
        }
    }
}
