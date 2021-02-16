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
            ReadReply reply = new ReadReply {
                Val = store.Read(req.IdPart, req.IdObj)
            };

            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(reply);
        }

        public override Task<WriteReply> Write(WriteRequest req, ServerCallContext _) {
            man.CheckFreeze();

            if (!man.WritePermission(req.IdPart)) {
                throw new GRPCException(StatusCode.PermissionDenied, ErrorMessage.W_DENIED, req.IdPart);
            }

            store.Lock(req.IdPart, req.IdObj);
            man.Lock(req.IdPart, req.IdObj);

            store.Write(req.IdPart, req.IdObj, req.Val);
            man.Update(req.IdPart, req.IdObj, req.Val);

            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(new WriteReply());
        }

        public override Task<ListServerReply> ListServer(ListServerRequest req, ServerCallContext _) {
            man.CheckFreeze();
            var res = new ListServerReply();
            var objs = store.ListObjs();

            lock (objs) {
                foreach (KeyValuePair<string, Obj> e in objs) {
                    res.Objs.Add(e.Key, e.Value.Value);
                }
            }

            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(res);
        }
    }
}
