using GStoreLib;
using System;
using System.Collections.Generic;
using Grpc.Core;
using Grpc.Net.Client;


namespace ServerGStore {
    public class ServerFrontend {
        public string IdServ { get; set; }
        private readonly GrpcChannel channel;
        private readonly GStoreSync.GStoreSyncClient stub;

        public ServerFrontend(string idServ, string URL) {
            IdServ = idServ;
            channel = GrpcChannel.ForAddress(URL);
            stub = new GStoreSync.GStoreSyncClient(channel);
        }

        public ServerInfo AppendEntries(ServerInfo sInfo, string idPart, List<KeyValuePair<string, string>> entries) {

            var req = new AppendEntriesRequest() {
                Term = sInfo.Term,
                Tag = sInfo.Tag,
                Mid = sInfo.Mid,
                IdPart = idPart
            };
            if (entries != null) foreach (var e in entries) req.Entries.Add(new Entry() { IdObj = e.Key, Val = e.Value });

            try {
                var res = stub.AppendEntries(req);
                return new ServerInfo(res.Mid, res.Tag, res.Term);
            } catch (RpcException) {
                NegAvail();
                return null;
            }
        }

        public int RequestVote(ServerInfo sInfo, string idPart) {
            try {
                var res = stub.RequestVote(new RequestVoteRequest() {
                    Term = sInfo.Term,
                    Tag = sInfo.Tag,
                    IdPart = idPart
                }, deadline: DateTime.UtcNow.AddSeconds(5));
                return res.Accept ? 1 : 0;
            } catch (RpcException e) {
                if (e.StatusCode == StatusCode.DeadlineExceeded) return 0;
                NegAvail();
                return -1;
            }
        }

        private void NegAvail() {
            lock (this) {
                if (ServerManager.Servs[IdServ].GetAvailSync()) {
                    foreach (var p in ServerManager.SParts) {
                        if (p.Value.IdServs.Contains(IdServ)) ServerManager.RInfos[p.Key].PInfo.Down();
                    }
                    ServerManager.Servs[IdServ].NegAvailSync();
                    Lib.WriteLine($"Server {IdServ}: Has Crashed!");
                }
            }
        }
    }
}
