using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Grpc.Core;
using GStoreLib;

namespace ServerGStore {
    public class GStoreServicesImpl : GStoreServices.GStoreServicesBase {

        private readonly ServerDomain store;
        private readonly ServerManager man;
        private readonly int minDelay;
        private readonly int maxDelay;

        public GStoreServicesImpl(ServerDomain store, ServerManager man, int minDelay, int maxDelay) {
            this.store = store;
            this.man = man;
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
        }

        public override Task<ReadReply> Read(ReadRequest req, ServerCallContext _) {
            man.CheckFreeze();
            ReadReply res = new ReadReply {
                Val = store.Read(req.IdPart, req.IdObj),
                Tag = ServerManager.RInfos[req.IdPart].PInfo.Tag
            };

            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(res);
        }

        public override Task<WriteReply> Write(WriteRequest req, ServerCallContext _) {
            man.CheckFreeze();

            if (ServerManager.RInfos[req.IdPart].IsMaster()) {
                if (man.Write(req.IdPart, req.IdObj, req.Val)) {
                    store.Write(req.IdPart, req.IdObj, req.Val);
                }
            }
            var res = new WriteReply() { Mid = ServerManager.SParts[req.IdPart].Mid };

            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(res);
        }

        public override Task<ListServerReply> ListServer(ListServerRequest req, ServerCallContext _) {
            man.CheckFreeze();
            var res = new ListServerReply();
            var objs = store.ListObjs();

            lock (objs) {
                foreach (KeyValuePair<string, string> e in objs) {
                    res.Objs.Add(e.Key, e.Value);
                }
            }
            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(res);
        }
    }
}
