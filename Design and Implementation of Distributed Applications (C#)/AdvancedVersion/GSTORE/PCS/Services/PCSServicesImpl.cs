using System.Threading.Tasks;
using Grpc.Core;

namespace PCS {
    class PCSServicesImpl : PCSServices.PCSServicesBase {

        private readonly string exeServer;
        private readonly string exeClient;

        public PCSServicesImpl(string exeServer, string exeClient) {
            this.exeServer = exeServer;
            this.exeClient = exeClient;
        }

        public override Task<NewServerReply> NewServer(NewServerRequest req, ServerCallContext _) {

            var input = string.Concat(
                PCSManager.ServerHeader(req.IdServ, req.URL, req.MinDelay, req.MaxDelay),
                PCSManager.Partitions(req.SParts),
                PCSManager.Servers(req.Servs)
            );
            PCSManager.NewProcess(exeServer, input);
            return Task.FromResult(new NewServerReply());
        }

        public override Task<NewClientReply> NewClient(NewClientRequest req, ServerCallContext _) {

            var input = string.Concat(
                PCSManager.ClientHeader(req.Uname, req.URL, req.Script),
                PCSManager.Partitions(req.SParts),
                PCSManager.Servers(req.Servs)
            );
            PCSManager.NewProcess(exeClient, input);
            return Task.FromResult(new NewClientReply());
        }
    }
}
