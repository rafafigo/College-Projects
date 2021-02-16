using System.Collections.Generic;
using GStoreLib;

namespace ServerGStore {
    public class ServerDomain {

        private readonly IDictionary<string, string> objs;

        public ServerDomain() {
            objs = new Dictionary<string, string>();
        }

        public string Read(string idPart, string idObj) {

            var key = Lib.FormObjKey(idPart, idObj);
            lock (objs) return objs.ContainsKey(key) ? objs[key] : "N/A";
        }

        public void Write(string idPart, string idObj, string value) {

            var key = Lib.FormObjKey(idPart, idObj);
            lock (objs) objs[key] = value;
        }

        public IDictionary<string, string> ListObjs() {
            return objs;
        }
    }
}
