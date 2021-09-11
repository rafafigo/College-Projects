using System.Collections.Generic;
using GStoreLib;
using System.Threading;

namespace ServerGStore {

    public class ServerManager {
        private readonly string id;
        public static IDictionary<string, Server> Servs;
        public static IDictionary<string, Partition> SParts = new Dictionary<string, Partition>();
        public static IDictionary<string, RaftInfo> RInfos = new Dictionary<string, RaftInfo>();
        public static IDictionary<string, ServerFrontend> Frontends = new Dictionary<string, ServerFrontend>();
        public static IDictionary<string, System.Timers.Timer> Timers = new Dictionary<string, System.Timers.Timer>();
        private bool isFreeze;
        public bool IsFreeze { get { lock (freezeLock) return isFreeze; } set { lock (freezeLock) isFreeze = value; } }
        private readonly object freezeLock;

        public ServerManager(string id, IDictionary<string, Partition> sParts, IDictionary<string, Server> servs) {
            this.id = id;
            Servs = servs;
            isFreeze = false;
            freezeLock = new object();

            foreach (var e in Servs) Frontends.Add(e.Key, new ServerFrontend(e.Key, e.Value.URL));
            foreach (var p in sParts) {
                if (!p.Value.IdServs.Contains(id)) continue;
                SParts.Add(p);
                RInfos.Add(p.Key, new RaftInfo(new PartitionInfo(p.Value.IdServs.Count), id == p.Value.Mid));
                Timers.Add(p.Key, MasterTimeout.NewTimer(this, p.Key, id == p.Value.Mid));
            }
        }

        public void SendHeartbeat(string idPart) {

            var sInfo = new ServerInfo(id, RInfos[idPart].PInfo.Tag, RInfos[idPart].Term);

            foreach (var s in SParts[idPart].IdServs) {
                if (id == s || !Servs[s].GetAvailSync()) continue;

                Lib.Debug($"SendHeartbeat On Partition: {idPart} | " +
                    $"From: {id} | To: {s} | " +
                    $"Term: {sInfo.Term} | Tag: {sInfo.Tag}");

                new Thread(() => {
                    var r = Frontends[s].AppendEntries(sInfo, idPart, null);
                    if (r != null && r.Term > RInfos[idPart].Term) BecomeFollower(idPart, r);
                }).Start();
            }
        }

        private void BecomeFollower(string idPart, ServerInfo sInfo) {

            lock (RInfos[idPart]) {
                if (sInfo.Term >= RInfos[idPart].Term) {
                    RInfos[idPart].BecomeFollower(Timers[idPart]);
                    RInfos[idPart].Term = sInfo.Term;
                    SParts[idPart].Mid = sInfo.Mid;
                }
            }
        }

        public void BecomeCandidate(string idPart) {
            RInfos[idPart].BecomeCandidate(Timers[idPart]);
            var sInfo = new ServerInfo(id, RInfos[idPart].PInfo.Tag, RInfos[idPart].Term);
            var myVote = RInfos[idPart].GetVote(sInfo.Term) ? 1 : 0;
            var res = new Dictionary<string, int>() { { "AcceptedVotes", myVote }, { "CountVotes", 1 } };

            Lib.Debug($"BecomeCandidate On Partition: {idPart} | " +
                $"Term: {sInfo.Term} | Tag: {sInfo.Tag}");

            foreach (var s in SParts[idPart].IdServs) {
                if (id == s || !Servs[s].GetAvailSync()) continue;

                new Thread(() => {
                    var r = Frontends[s].RequestVote(sInfo, idPart);
                    lock (res) {
                        if (r >= 0) {
                            if (r == 1) res["AcceptedVotes"]++;
                            res["CountVotes"]++;
                        }
                        Monitor.Pulse(res);
                    }
                }).Start();
            }

            lock (res) {
                while (RInfos[idPart].IsCandidate() &&
                    res["AcceptedVotes"] < RInfos[idPart].PInfo.GetQuorum() &&
                    res["CountVotes"] < RInfos[idPart].PInfo.Avail)
                    Monitor.Wait(res);
            }
            if (res["AcceptedVotes"] >= RInfos[idPart].PInfo.GetQuorum()) BecomeMaster(idPart);
            else RInfos[idPart].CandidateTimeout(Timers[idPart]);
        }

        public void BecomeMaster(string idPart) {

            Lib.Debug($"BecomeMaster On Partition: {idPart}");
            lock (RInfos[idPart]) {
                RInfos[idPart].BecomeMaster(Timers[idPart]);
                SParts[idPart].Mid = id;
            }
            SendHeartbeat(idPart);
        }

        public bool RcvRequestVote(string idPart, int term, int tag) {

            var vote = tag >= RInfos[idPart].PInfo.Tag && RInfos[idPart].GetVote(term);

            Lib.Debug($"RcvRequestVote On Partition: {idPart} | " +
                $"Term: {RInfos[idPart].Term} | Term-Recebido: {term} | " +
                $"Tag: {RInfos[idPart].PInfo.Tag} | Tag-Recebida: {tag} | " +
                $"Vote: {vote}");
            return vote;
        }

        public bool Write(string idPart, string idObj, string val) {

            var sInfo = new ServerInfo(id, RInfos[idPart].AddLog(idObj, val), RInfos[idPart].Term);
            var res = new List<ServerInfo>();

            Lib.Debug($"Write On Partition: {idPart} | " +
                $"Term: {sInfo.Term} | Tag: {sInfo.Tag} | " +
                $"Value: {Lib.FormObj(Lib.FormObjKey(idPart, idObj), val)}");

            foreach (var s in SParts[idPart].IdServs) {
                if (id == s || !Servs[s].GetAvailSync()) continue;
                new Thread(() => {
                    var r = Frontends[s].AppendEntries(sInfo, idPart, RInfos[idPart].GetLogsRange(sInfo.Tag - 1, 1));
                    if (r != null && r.Term == sInfo.Term && r.Tag < sInfo.Tag)
                        r = Frontends[s].AppendEntries(sInfo, idPart, RInfos[idPart].GetLogsRange(r.Tag, sInfo.Tag - r.Tag));
                    lock (res) {
                        if (r != null) res.Add(r);
                        Monitor.Pulse(res);
                    }
                }).Start();
            }

            lock (res) {
                for (var w = 0; w + 1 < RInfos[idPart].PInfo.GetQuorum(); w = res.Count) {
                    if (w == res.Count) Monitor.Wait(res);

                    for (var i = w; i < res.Count; i++) {
                        if (res[i].Term > RInfos[idPart].Term) {
                            BecomeFollower(idPart, res[i]);
                            RInfos[idPart].RemoveLog(sInfo.Tag);
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public ServerInfo RcvAppendEntries(string idPart, ServerInfo r, List<KeyValuePair<string, string>> entries) {

            lock (RInfos[idPart]) {
                var sInfo = new ServerInfo(SParts[idPart].Mid, RInfos[idPart].PInfo.Tag, RInfos[idPart].Term);

                Lib.Debug($"RcvAppendEntries On Partition: {idPart} | " +
                    $"From: {r.Mid} | To: {id} | " +
                    $"Term: {sInfo.Term} | Term-Recebido: {r.Term} | " +
                    $"Tag: {sInfo.Tag} | Tag-Recebida: {r.Tag}");

                if (sInfo.Term > r.Term) return sInfo;
                BecomeFollower(idPart, r);

                if (r.Tag - entries.Count < sInfo.Tag) RInfos[idPart].RemoveAllLogs(r.Tag - entries.Count);
                RInfos[idPart].AddAllLogs(entries);

                return new ServerInfo(SParts[idPart].Mid, RInfos[idPart].PInfo.Tag, RInfos[idPart].Term);
            }
        }

        public void CheckFreeze() {
            lock (freezeLock) {
                while (isFreeze) Monitor.Wait(freezeLock);
            }
        }

        public void Freeze() {
            IsFreeze = true;
        }

        public void Unfreeze() {
            lock (freezeLock) {
                isFreeze = false;
                Monitor.PulseAll(freezeLock);
            }
        }
    }
}
