using Grpc.Core;
using GStoreLib;
using System;
using System.Threading.Tasks;

namespace ServerGStore {
    class GStoreSyncImpl : GStoreSync.GStoreSyncBase {

        private readonly ServerDomain store;
        private readonly ServerManager man;
        private readonly int minDelay;
        private readonly int maxDelay;

        public GStoreSyncImpl(ServerDomain store, ServerManager man, int minDelay, int maxDelay) {
            this.store = store;
            this.man = man;
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
        }

        public override Task<LockReply> Lock(LockRequest req, ServerCallContext _) {
            man.CheckFreeze();
            try {
                store.Lock(req.IdPart, req.IdObj);
            } catch (DomainException e) {
                throw new GRPCException(StatusCode.NotFound, e.Message);
            }

            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(new LockReply());
        }

        public override Task<UpdateReply> Update(UpdateRequest req, ServerCallContext _) {
            man.CheckFreeze();
            store.Write(req.IdPart, req.IdObj, req.Val);
            
            Lib.Sleep(new Random().Next(minDelay, maxDelay));
            return Task.FromResult(new UpdateReply());
        }
    }
}
