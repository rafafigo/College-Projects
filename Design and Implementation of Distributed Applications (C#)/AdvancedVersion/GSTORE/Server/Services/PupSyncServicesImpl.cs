using System.Threading.Tasks;
using Grpc.Core;
using GStoreLib;

namespace ServerGStore {
    class PupSyncServicesImpl : PupSyncServices.PupSyncServicesBase {

        private readonly ServerDomain store;
        private readonly ServerManager man;

        public PupSyncServicesImpl(ServerDomain store, ServerManager man) {
            this.store = store;
            this.man = man;
        }

        public override Task<StatusReply> Status(StatusRequest req, ServerCallContext _) {

            if (man.IsFreeze) Lib.WriteLine("Status: Freezed!");
            else Lib.WriteLine("Status: Available!");

            var objs = store.ListObjs();
            lock (objs) {
                foreach (var o in objs) {
                    Lib.WriteLine(Lib.FormObj(o.Key, o.Value));
                }
            }
            return Task.FromResult(new StatusReply() { Code = man.IsFreeze ? 1 : 0 });
        }

        public override Task<CrashReply> Crash(CrashRequest req, ServerCallContext _) {
            Lib.Exit("Crashed By PuppetMaster!");
            return Task.FromResult(new CrashReply());
        }

        public override Task<FreezeReply> Freeze(FreezeRequest req, ServerCallContext _) {
            man.Freeze();
            return Task.FromResult(new FreezeReply());
        }

        public override Task<UnfreezeReply> Unfreeze(UnfreezeRequest req, ServerCallContext _) {
            man.Unfreeze();
            return Task.FromResult(new UnfreezeReply());
        }
    }
}
