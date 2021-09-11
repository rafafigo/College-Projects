using System.Threading.Tasks;
using Grpc.Core;

namespace ClientGStore {
    class PupSyncServicesImpl : PupSyncServices.PupSyncServicesBase {

        public override Task<StatusReply> Status(StatusRequest req, ServerCallContext _) {
            return Task.FromResult(new StatusReply() { Code = 0 });
        }
    }
}
