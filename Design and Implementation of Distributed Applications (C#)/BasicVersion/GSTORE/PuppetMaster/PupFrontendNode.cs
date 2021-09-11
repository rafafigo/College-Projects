using System;
using System.Threading.Tasks;
using System.Windows.Forms;
using Grpc.Core;
using Grpc.Net.Client;

namespace PuppetMaster {
    class PupFrontendNode {

        public string IdServ { get; set; }
        private readonly GrpcChannel channel;
        private readonly PupSyncServices.PupSyncServicesClient stub;

        public PupFrontendNode(string idServ, string URL) {
            IdServ = idServ;
            channel = GrpcChannel.ForAddress(URL);
            stub = new PupSyncServices.PupSyncServicesClient(channel);
        }

        public async Task<int> StatusAsync() {
            try {
                var res = await stub.StatusAsync(new StatusRequest());
                return res.Code;
            } catch (RpcException) {
                PupExec.Servs[IdServ].NegAvailSync();
                return -1;
            }
        }

        public async Task CrashAsync() {
            try {
                await stub.CrashAsync(new CrashRequest());
            } catch (RpcException) { }
            PupExec.Servs[IdServ].NegAvailSync();
        }

        public async Task FreezeAsync() {
            try {
                await stub.FreezeAsync(new FreezeRequest());
            } catch (RpcException e) {
                PupExec.Servs[IdServ].NegAvailSync();
                MessageBox.Show(string.Format("FreezeAsync: Not Executed!{0}{1}", Environment.NewLine, e.Message));
            }
        }

        public async Task UnfreezeAsync() {
            try {
                await stub.UnfreezeAsync(new UnfreezeRequest());
            } catch (RpcException e) {
                PupExec.Servs[IdServ].NegAvailSync();
                MessageBox.Show(string.Format("UnfreezeAsync: Not Executed!{0}{1}", Environment.NewLine, e.Message));
            }
        }
    }
}
